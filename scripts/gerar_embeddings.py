#!/usr/bin/env python3
"""
Lote 259+260+261: Script offline de geração de embeddings + rechunking.

O que faz:
  1. Lê o chunks.jsonl existente
  2. (Opcional) Rechunka chunks grandes em sub-chunks de ~600 chars
  3. Gera embedding de 384 dims (all-MiniLM-L6-v2) para cada chunk
  4. Salva chunks.jsonl atualizado com campo "embedding"

O app importa automaticamente os embeddings ao instalar/atualizar,
sem nenhuma mudança no código — basta incluir o chunks.jsonl no build.

Uso:
  pip install sentence-transformers torch
  python scripts/gerar_embeddings.py
  python scripts/gerar_embeddings.py --rechunk          # também rechunka
  python scripts/gerar_embeddings.py --input outro.jsonl --output saida.jsonl
"""

import argparse
import json
import sys
import time
from pathlib import Path

# Caminho padrão relativo ao root do projeto Android
DEFAULT_INPUT  = Path(__file__).parent.parent / "app/src/main/assets/chunks.jsonl"
DEFAULT_OUTPUT = DEFAULT_INPUT  # sobrescreve in-place (faz backup antes)


def rechunk(texto: str, max_chars: int = 600, overlap: int = 100) -> list[str]:
    """Quebra texto em sub-chunks de max_chars com overlap."""
    if len(texto) <= max_chars:
        return [texto]
    chunks = []
    inicio = 0
    while inicio < len(texto):
        fim = min(inicio + max_chars, len(texto))
        if fim < len(texto):
            # Quebra no final de frase mais próximo
            pos_ponto = texto.rfind('. ', inicio, fim)
            if pos_ponto > inicio + max_chars // 2:
                fim = pos_ponto + 1
        chunks.append(texto[inicio:fim].strip())
        inicio = fim - overlap
    return [c for c in chunks if c]


def main():
    parser = argparse.ArgumentParser(description="Gera embeddings para chunks.jsonl")
    parser.add_argument("--input",   default=str(DEFAULT_INPUT),  help="Arquivo de entrada")
    parser.add_argument("--output",  default=str(DEFAULT_OUTPUT), help="Arquivo de saída")
    parser.add_argument("--rechunk", action="store_true", help="Rechunka chunks > 800 chars")
    parser.add_argument("--max-chars", type=int, default=600, help="Tamanho máximo por chunk (default 600)")
    parser.add_argument("--batch",   type=int, default=64,   help="Batch size para embeddings (default 64)")
    parser.add_argument("--dry-run", action="store_true", help="Apenas mostra estatísticas, não grava")
    args = parser.parse_args()

    input_path  = Path(args.input)
    output_path = Path(args.output)

    if not input_path.exists():
        print(f"ERRO: arquivo não encontrado: {input_path}")
        sys.exit(1)

    print(f"Carregando chunks de: {input_path}")
    chunks_originais = []
    with open(input_path, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if line:
                try:
                    chunks_originais.append(json.loads(line))
                except json.JSONDecodeError as e:
                    print(f"  AVISO: linha inválida ignorada: {e}")

    print(f"  {len(chunks_originais)} chunks carregados")

    # Rechunking (opcional)
    chunks_processados = []
    if args.rechunk:
        print(f"\nRechunkando chunks > {args.max_chars} chars...")
        grandes = 0
        for chunk in chunks_originais:
            texto = chunk.get("text", "")
            if len(texto) > args.max_chars:
                grandes += 1
                sub_textos = rechunk(texto, args.max_chars)
                for i, sub in enumerate(sub_textos):
                    novo = chunk.copy()
                    novo["chunk_id"] = f"{chunk['chunk_id']}_r{i}"
                    novo["text"] = sub
                    novo.pop("embedding", None)  # remove embedding antigo se existir
                    chunks_processados.append(novo)
            else:
                chunks_processados.append(chunk)
        print(f"  {grandes} chunks grandes rechunkados → {len(chunks_processados)} chunks totais")
    else:
        chunks_processados = chunks_originais

    # Filtra chunks que já têm embedding (não regerá se não pediu rechunk)
    chunks_sem_embedding = [c for c in chunks_processados if "embedding" not in c]
    chunks_com_embedding = [c for c in chunks_processados if "embedding" in c]
    print(f"\n  {len(chunks_com_embedding)} já têm embedding | {len(chunks_sem_embedding)} precisam gerar")

    if not chunks_sem_embedding:
        print("Todos os chunks já têm embedding. Nada a fazer.")
        if not args.dry_run:
            print(f"Arquivo já atualizado: {output_path}")
        return

    if args.dry_run:
        print(f"\n[DRY RUN] Geraria embeddings para {len(chunks_sem_embedding)} chunks.")
        tamanhos = [len(c.get("text","")) for c in chunks_processados]
        print(f"  Tamanho médio: {sum(tamanhos)//len(tamanhos)} chars")
        print(f"  Tamanho max: {max(tamanhos)} chars | min: {min(tamanhos)} chars")
        return

    # Carrega modelo
    print("\nCarregando modelo all-MiniLM-L6-v2 (primeira vez baixa ~80MB)...")
    try:
        from sentence_transformers import SentenceTransformer
    except ImportError:
        print("ERRO: sentence-transformers não instalado.")
        print("  Execute: pip install sentence-transformers torch")
        sys.exit(1)

    modelo = SentenceTransformer("all-MiniLM-L6-v2")
    print("  Modelo carregado.")

    # Gera embeddings em batches
    textos = [c.get("text", "") for c in chunks_sem_embedding]
    print(f"\nGerando embeddings para {len(textos)} chunks (batch={args.batch})...")
    t0 = time.time()
    embeddings = modelo.encode(
        textos,
        batch_size=args.batch,
        show_progress_bar=True,
        normalize_embeddings=True  # cosine similarity = dot product após normalização
    )
    dt = time.time() - t0
    print(f"  Concluído em {dt:.1f}s ({len(textos)/dt:.0f} chunks/s)")
    print(f"  Dimensões: {embeddings.shape[1]} (esperado: 384)")

    # Aplica embeddings
    for chunk, emb in zip(chunks_sem_embedding, embeddings):
        chunk["embedding"] = emb.tolist()

    # Backup do arquivo original
    if output_path == input_path and input_path.exists():
        backup = input_path.with_suffix(".jsonl.bak")
        import shutil
        shutil.copy2(input_path, backup)
        print(f"\nBackup salvo em: {backup}")

    # Salva resultado
    todos = chunks_com_embedding + chunks_sem_embedding
    # Reordena para manter ordem original (por chunk_id base)
    todos_dict = {c["chunk_id"]: c for c in todos}
    ordem_original = [c["chunk_id"] for c in chunks_processados]
    todos_ordenados = [todos_dict.get(cid, todos_dict.get(cid.split("_r")[0])) for cid in ordem_original]
    todos_ordenados = [c for c in todos_ordenados if c is not None]

    with open(output_path, "w", encoding="utf-8") as f:
        for chunk in todos_ordenados:
            f.write(json.dumps(chunk, ensure_ascii=False) + "\n")

    print(f"\n✓ chunks.jsonl atualizado: {len(todos_ordenados)} chunks com embedding")
    print(f"  Arquivo: {output_path}")
    print(f"  Tamanho estimado: {output_path.stat().st_size / 1024 / 1024:.1f} MB")
    print()
    print("Próximos passos:")
    print("  1. Incluir o chunks.jsonl atualizado no build do app")
    print("  2. O app importará os embeddings automaticamente na primeira abertura")
    print("  3. Verificar no Logcat: 'AUDITORIA: Carga concluída! ... embeddings semânticos'")


if __name__ == "__main__":
    main()
