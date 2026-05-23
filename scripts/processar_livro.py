#!/usr/bin/env python3
"""
Lote 261: Pipeline offline de processamento de livros GURPS.

Adicionar um livro novo ao RAG:
  python scripts/processar_livro.py MeuLivro.pdf --source-id pt_meu_livro --title "Meu Livro GURPS"

O script:
  1. Extrai texto do PDF (PyMuPDF)
  2. Chunka em sub-chunks de ~600 chars com overlap
  3. Gera embeddings (all-MiniLM-L6-v2)
  4. Faz append no chunks.jsonl existente

Depois: inclua o chunks.jsonl no próximo build — o app importa automaticamente.

Instalação das dependências:
  pip install pymupdf sentence-transformers torch
"""

import argparse
import json
import re
import sys
import time
from pathlib import Path

DEFAULT_OUTPUT = Path(__file__).parent.parent / "app/src/main/assets/chunks.jsonl"


def extrair_texto_pdf(pdf_path: Path) -> dict[int, str]:
    """Extrai texto por página via PyMuPDF."""
    try:
        import fitz  # PyMuPDF
    except ImportError:
        print("ERRO: PyMuPDF não instalado. Execute: pip install pymupdf")
        sys.exit(1)

    doc = fitz.open(str(pdf_path))
    paginas = {}
    for i, page in enumerate(doc, start=1):
        texto = page.get_text("text").strip()
        if texto:
            paginas[i] = texto
    doc.close()
    print(f"  {len(paginas)} páginas extraídas de {pdf_path.name}")
    return paginas


def rechunk(texto: str, max_chars: int = 600, overlap: int = 100) -> list[str]:
    """Quebra texto em sub-chunks de max_chars com overlap."""
    if len(texto) <= max_chars:
        return [texto.strip()]
    chunks = []
    inicio = 0
    while inicio < len(texto):
        fim = min(inicio + max_chars, len(texto))
        if fim < len(texto):
            pos_ponto = texto.rfind('. ', inicio, fim)
            if pos_ponto > inicio + max_chars // 2:
                fim = pos_ponto + 1
        sub = texto[inicio:fim].strip()
        if sub:
            chunks.append(sub)
        inicio = fim - overlap
    return chunks


def normalizar_source_id(nome: str) -> str:
    """Gera source_id padrão a partir do nome do arquivo."""
    nome = re.sub(r'[^a-z0-9_]', '_', nome.lower())
    nome = re.sub(r'_+', '_', nome).strip('_')
    if not nome.startswith('pt_'):
        nome = 'pt_' + nome
    return nome


def main():
    parser = argparse.ArgumentParser(description="Processa livro PDF para o RAG do Mestre IA")
    parser.add_argument("pdf", help="Arquivo PDF do livro GURPS")
    parser.add_argument("--source-id", help="ID da fonte (ex: pt_ultra_tech). Default: derivado do nome do arquivo")
    parser.add_argument("--title", required=True, help="Título do livro (ex: 'GURPS Ultra-Tech 4ª Ed.')")
    parser.add_argument("--output", default=str(DEFAULT_OUTPUT), help="Arquivo chunks.jsonl de saída")
    parser.add_argument("--max-chars", type=int, default=600, help="Chars máx por chunk (default 600)")
    parser.add_argument("--overlap", type=int, default=100, help="Overlap entre chunks (default 100)")
    parser.add_argument("--no-embed", action="store_true", help="Pula geração de embeddings (rápido, sem semântica)")
    parser.add_argument("--dry-run", action="store_true", help="Mostra estatísticas sem gravar")
    args = parser.parse_args()

    pdf_path = Path(args.pdf)
    output_path = Path(args.output)

    if not pdf_path.exists():
        print(f"ERRO: PDF não encontrado: {pdf_path}")
        sys.exit(1)

    source_id = args.source_id or normalizar_source_id(pdf_path.stem)
    print(f"\n{'='*60}")
    print(f"Processando: {pdf_path.name}")
    print(f"  source_id: {source_id}")
    print(f"  title: {args.title}")
    print(f"  output: {output_path}")
    print(f"{'='*60}\n")

    # 1. Extrai texto do PDF
    paginas = extrair_texto_pdf(pdf_path)

    # 2. Chunking por página
    chunks_raw = []
    for pagina, texto in paginas.items():
        sub_textos = rechunk(texto, args.max_chars, args.overlap)
        for i, sub in enumerate(sub_textos):
            chunk_id = f"{source_id}_p{pagina}_c{i+1}"
            chunks_raw.append({
                "chunk_id": chunk_id,
                "source_id": source_id,
                "source_title": args.title,
                "page_number": pagina,
                "language": "pt",
                "page_display": f"[{args.title} Pág. {pagina}]",
                "text": sub
            })

    print(f"  {len(chunks_raw)} chunks gerados ({len(paginas)} páginas × rechunk ~{args.max_chars} chars)")

    if args.dry_run:
        print(f"\n[DRY RUN] Geraria {len(chunks_raw)} chunks. Nada gravado.")
        return

    # 3. Embeddings (opcional)
    if not args.no_embed:
        print(f"\nGerando embeddings (all-MiniLM-L6-v2)...")
        try:
            from sentence_transformers import SentenceTransformer
        except ImportError:
            print("  AVISO: sentence-transformers não instalado. Pulando embeddings.")
            print("  Execute depois: python scripts/gerar_embeddings.py")
            args.no_embed = True

        if not args.no_embed:
            modelo = SentenceTransformer("all-MiniLM-L6-v2")
            textos = [c["text"] for c in chunks_raw]
            t0 = time.time()
            embeddings = modelo.encode(textos, batch_size=64, show_progress_bar=True, normalize_embeddings=True)
            print(f"  Concluído em {time.time()-t0:.1f}s")
            for chunk, emb in zip(chunks_raw, embeddings):
                chunk["embedding"] = emb.tolist()

    # 4. Verifica duplicatas (source_id já existe no jsonl?)
    existentes_ids = set()
    if output_path.exists():
        with open(output_path, encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if line:
                    try:
                        obj = json.loads(line)
                        existentes_ids.add(obj.get("chunk_id", ""))
                        if obj.get("source_id") == source_id:
                            print(f"\nAVISO: source_id '{source_id}' já existe no chunks.jsonl!")
                            print("  Use --source-id para especificar um ID diferente,")
                            print("  ou remova os chunks existentes manualmente antes de continuar.")
                            resp = input("  Continuar mesmo assim? (s/N): ").strip().lower()
                            if resp != 's':
                                print("Operação cancelada.")
                                sys.exit(0)
                            break
                    except json.JSONDecodeError:
                        pass

    novos = [c for c in chunks_raw if c["chunk_id"] not in existentes_ids]
    print(f"\n  {len(novos)} chunks novos (de {len(chunks_raw)} gerados)")

    # 5. Append no chunks.jsonl
    with open(output_path, "a", encoding="utf-8") as f:
        for chunk in novos:
            f.write(json.dumps(chunk, ensure_ascii=False) + "\n")

    print(f"\n✓ {len(novos)} chunks adicionados ao {output_path.name}")
    print(f"  Tamanho total do arquivo: {output_path.stat().st_size / 1024 / 1024:.1f} MB")
    print()
    print("Próximos passos:")
    print("  1. Incluir o chunks.jsonl atualizado no próximo build do app")
    print("  2. Se usou --no-embed, rodar depois: python scripts/gerar_embeddings.py")
    print("  3. O app importará tudo automaticamente na primeira abertura após update")


if __name__ == "__main__":
    main()
