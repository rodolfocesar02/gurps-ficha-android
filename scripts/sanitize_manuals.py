import json
import os
import re

PATH_ASSETS = r'c:\Users\Rodolfo\Desktop\ficha gurps\ficha-gurps\gurps_app\gurps-ficha-android\app\src\main\assets'
FILE_CHUNKS = os.path.join(PATH_ASSETS, 'chunks.jsonl')
FILE_BACKUP = os.path.join(PATH_ASSETS, 'chunks.jsonl.bak')

def clean_safe(text):
    if not text: return text
    
    # Mapa de Correção UTF-8/Latin-1 Híbrido (GURPS DEVIR)
    replacements = {
        '\u01dc': 'ã',  # Visǜo -> Visão
        '\u01ea': 'á',  # pǭg -> pág
        '\u01f8': 'é',  # Ǹ -> é
        '\u01e6': 'ê',  # Ǧ -> ê
        '\u01ec': 'õ',  # Ǭ -> õ
        '\u01fd': 'ã',  # ǽ -> ã
        '\ufffd': 'ç',  # Losango ? -> ç
    }
    
    for corrupted, corrected in replacements.items():
        text = text.replace(corrupted, corrected)
        
    # Correções de nomes próprios de manuais e regras
    text = text.replace('Ediǜo', 'Edição')
    text = text.replace('Edio', 'Edição')
    text = text.replace('Mdulo Bsico', 'Módulo Básico')
    text = text.replace('Viso Noturna', 'Visão Noturna')
    text = text.replace('Visǜo Noturna', 'Visão Noturna')
    text = text.replace('penalidade por iluminao', 'penalidade por iluminação')
    
    return text

def sanitize():
    if not os.path.exists(FILE_BACKUP):
        print("Backup não encontrado! Nada a restaurar.")
        return

    print(f"Restaurando e limpando com segurança a partir de: {FILE_BACKUP}")
    
    processed = 0
    # Lemos como binário para garantir que não temos erro de decode inicial
    with open(FILE_BACKUP, 'rb') as f_in:
        lines = f_in.readlines()

    with open(FILE_CHUNKS, 'w', encoding='utf-8') as f_out:
        for line_bytes in lines:
            try:
                # Tenta decodificar como UTF-8 pura
                line_text = line_bytes.decode('utf-8', errors='replace')
                data = json.loads(line_text)
                
                # Limpa os campos
                data['text'] = clean_safe(data['text'])
                data['source_title'] = clean_safe(data['source_title'])
                
                # Salva garantindo UTF-8 sem escapes \uXXXX
                f_out.write(json.dumps(data, ensure_ascii=False) + '\n')
                processed += 1
            except Exception as e:
                # Se falhar, pula a linha (não queremos lixo no JSONL)
                pass
    
    print(f"Sucesso! {processed} fragmentos restaurados e prontos.")

if __name__ == "__main__":
    sanitize()
