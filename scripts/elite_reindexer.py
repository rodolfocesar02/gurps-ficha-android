import os
import fitz  # PyMuPDF
import pdfplumber
import json
import re

# Configurações de Caminho
PATH_LIVROS = r'C:\Users\Rodolfo\Desktop\rpg\gurps\livros'
PATH_OUTPUT = r'c:\Users\Rodolfo\Desktop\ficha gurps\ficha-gurps\gurps_app\gurps-ficha-android\app\src\main\assets\chunks.jsonl'

# Mapeamento de Arquivos Selecionados
MANUAIS = [
    {"id": "modulo_basico", "title": "GURPS 4ª Edição - Módulo Básico", "file_pattern": "Mdulo Bsico.pdf"}, # Nome exato do Personagens
    {"id": "artes_marciais", "title": "GURPS 4ª Edição - Artes Marciais", "file_pattern": "Artes Marciais"},
    {"id": "magia", "title": "GURPS 4ª Edição - Magia", "file_pattern": "GURPS 4 Edio - Magia.pdf"}, # Use o padrão que funcionou antes
    {"id": "gun_fu", "title": "GURPS 4ª Edição - Gun Fu", "file_pattern": "Gun Fu"},
    {"id": "categorias_pericias", "title": "Categorias de Perícias", "file_pattern": "Categorias"},
    {"id": "dicionario", "title": "GURPS 4e - Dicionário de Características", "file_pattern": "Dicionrio"},
]

def clean_text(text):
    if not text: return ""
    # Remove hifens órfãos no final da linha (ex: ar- \n tes -> artes)
    text = re.sub(r'(\w)-\s*\n\s*(\w)', r'\1\2', text)
    # Remove quebras de linha que não são parágrafos
    text = re.sub(r'(?<!\.)\n\s*(?![\dA-Z])', ' ', text)
    # Limpeza de espaços duplos
    text = re.sub(r'\s+', ' ', text).strip()
    return text

def extract_tables_as_markdown(pdf_path, page_num):
    try:
        with pdfplumber.open(pdf_path) as pdf:
            page = pdf.pages[page_num]
            tables = page.extract_tables()
            if not tables: return ""
            
            md_tables = []
            for table in tables:
                rows = []
                for row in table:
                    cleaned_row = [str(cell).replace('\n', ' ').strip() if cell else "" for cell in row]
                    rows.append("| " + " | ".join(cleaned_row) + " |")
                
                if len(rows) > 0:
                    header_sep = "| " + " | ".join(["---"] * len(table[0])) + " |"
                    rows.insert(1, header_sep)
                    md_tables.append("\n" + "\n".join(rows) + "\n")
            
            return "\n".join(md_tables)
    except:
        return ""

def process_manual(manual):
    # Encontra o arquivo real usando normalização radical (agnóstico a acentos/encoding/espaços)
    files = os.listdir(PATH_LIVROS)
    
    def normalize(s):
        return re.sub(r'[^a-zA-Z0-9]', '', s).lower()

    target = normalize(manual["file_pattern"])
    filename = None
    for f in files:
        f_norm = normalize(f)
        if target in f_norm:
            # Filtro específico para o Módulo Básico (Personagens vs Campanhas)
            if manual["id"] == "modulo_basico" and "campanha" in f.lower():
                continue
            filename = f
            break
    
    if not filename:
        print(f"PULANDO: {manual['title']} (Padrão '{target}' não encontrado em {files})")
        return []

    file_path = os.path.join(PATH_LIVROS, filename)
    print(f"PROCESSANDO: {manual['title']} ({filename})...")
    
    chunks = []
    doc = fitz.open(file_path)
    
    for page_idx in range(len(doc)):
        page = doc[page_idx]
        width = page.rect.width
        mid = width / 2
        
        # Extrai blocos (x0, y0, x1, y1, text, block_no, block_type)
        blocks = page.get_text("blocks")
        
        # Filtra apenas texto (block_type 0)
        text_blocks = [b for b in blocks if b[6] == 0]
        
        # Separa em colunas (Esquerda / Direita)
        left_col = [b for b in text_blocks if b[0] < mid]
        right_col = [b for b in text_blocks if b[0] >= mid]
        
        # Ordena cada coluna de cima para baixo
        left_col.sort(key=lambda x: x[1])
        right_col.sort(key=lambda x: x[1])
        
        # Reune o texto na ordem correta
        full_page_text = ""
        for b in left_col: full_page_text += b[4] + "\n"
        for b in right_col: full_page_text += b[4] + "\n"
        
        cleaned_text = clean_text(full_page_text)
        
        # Tenta extrair tabelas se houver suspeita de dados tabulares
        if "|" not in cleaned_text: # Se já não processou como markdown manual
             md_table = extract_tables_as_markdown(file_path, page_idx)
             if md_table:
                 cleaned_text += "\n\n### Dados da Tabela:\n" + md_table

        if cleaned_text:
            chunks.append({
                "chunk_id": f"pt_{manual['id']}_p{page_idx+1}_c1",
                "source_id": f"pt_{manual['id']}",
                "source_title": manual["title"],
                "page_number": page_idx + 1,
                "text": cleaned_text,
                "language": "pt"
            })
            
    doc.close()
    return chunks

def main():
    all_chunks = []
    for manual in MANUAIS:
        try:
            manual_chunks = process_manual(manual)
            all_chunks.extend(manual_chunks)
        except Exception as e:
            print(f"ERRO ao processar {manual['title']}: {e}")

    with open(PATH_OUTPUT, 'w', encoding='utf-8') as f:
        for chunk in all_chunks:
            f.write(json.dumps(chunk, ensure_ascii=False) + '\n')
            
    print(f"\nFINALIZADO! {len(all_chunks)} fragmentos gerados em {PATH_OUTPUT}")

if __name__ == "__main__":
    main()
