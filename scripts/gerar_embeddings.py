#!/usr/bin/env python3
"""
Lote 259+260+261: Script offline de geração de embeddings via API Gemini.

Usa a mesma API que o app usa em runtime (text-embedding-004, 768 dims).
Não precisa baixar nenhum modelo local — funciona com a chave Gemini do projeto.

Uso:
  python scripts/gerar_embeddings.py
  python scripts/gerar_embeddings.py --rechunk    # também rechunka chunks grandes
  python scripts/gerar_embeddings.py --dry-run    # mostra estatísticas sem gravar

A chave Gemini é lida de local.properties automaticamente.
"""

import argparse
import json
import time
import shutil
import urllib.request
import urllib.error
import ssl
from pathlib import Path

DEFAULT_JSONL = Path(__file__).parent.parent / "app/src/main/assets/chunks.jsonl"
LOCAL_PROPS   = Path(__file__).parent.parent / "local.properties"

GEMINI_EMBED_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent?key={key}"
EMBED_DIMS = 3072  # gemini-embedding-001 gera 3072 dims
BATCH_DELAY = 0.1  # segundos entre requests para não estourar quota


def ler_gemini_key():
    if not LOCAL_PROPS.exists():
        raise FileNotFoundError(f"local.properties não encontrado em {LOCAL_PROPS}")
    for linha in LOCAL_PROPS.read_text(encoding="utf-8").splitlines():
        if linha.startswith("mestre.ia.gemini.key="):
            return linha.split("=", 1)[1].strip()
    raise ValueError("mestre.ia.gemini.key não encontrado em local.properties")


def gerar_embedding(texto: str, api_key: str, ctx: ssl.SSLContext) -> list[float] | None:
    """Gera embedding via Gemini text-embedding-004. Retorna lista de 768 floats."""
    url = GEMINI_EMBED_URL.format(key=api_key)
    payload = json.dumps({
        "content": {"parts": [{"text": texto[:2000]}]},
        "taskType": "RETRIEVAL_DOCUMENT"
    }).encode("utf-8")

    req = urllib.request.Request(
        url, data=payload,
        headers={"Content-Type": "application/json"},
        method="POST"
    )
    try:
        with urllib.request.urlopen(req, context=ctx, timeout=15) as resp:
            data = json.loads(resp.read().decode("utf-8"))
            return data["embedding"]["values"]
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8", errors="replace")[:200]
        print(f"  HTTP {e.code}: {body}")
        return None
    except Exception as e:
        print(f"  Erro: {e}")
        return None


def rechunk(texto: str, max_chars: int = 600, overlap: int = 100) -> list[str]:
    if len(texto) <= max_chars:
        return [texto.strip()]
    chunks = []
    inicio = 0
    while inicio < len(texto):
        fim = min(inicio + max_chars, len(texto))
        if fim < len(texto):
            pos = texto.rfind('. ', inicio, fim)
            if pos > inicio + max_chars // 2:
                fim = pos + 1
        sub = texto[inicio:fim].strip()
        if sub:
            chunks.append(sub)
        inicio = fim - overlap
    return chunks


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--input",    default=str(DEFAULT_JSONL))
    parser.add_argument("--output",   default=str(DEFAULT_JSONL))
    parser.add_argument("--rechunk",  action="store_true")
    parser.add_argument("--max-chars",type=int, default=600)
    parser.add_argument("--dry-run",  action="store_true")
    parser.add_argument("--limit",    type=int, default=0, help="Limitar N chunks (teste)")
    args = parser.parse_args()

    input_path  = Path(args.input)
    output_path = Path(args.output)

    # SSL sem verificação (contorna bloqueio de certificado corporativo)
    ctx = ssl.create_default_context()
    ctx.check_hostname = False
    ctx.verify_mode = ssl.CERT_NONE

    api_key = ler_gemini_key()
    print(f"Chave Gemini: ...{api_key[-6:]}")

    # Carrega chunks
    print(f"\nCarregando chunks de: {input_path}")
    chunks = []
    with open(input_path, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if line:
                try:
                    chunks.append(json.loads(line))
                except:
                    pass
    print(f"  {len(chunks)} chunks carregados")

    # Rechunking opcional
    if args.rechunk:
        processados = []
        grandes = 0
        for c in chunks:
            texto = c.get("text", "")
            if len(texto) > args.max_chars:
                grandes += 1
                for i, sub in enumerate(rechunk(texto, args.max_chars)):
                    novo = c.copy()
                    novo["chunk_id"] = f"{c['chunk_id']}_r{i}"
                    novo["text"] = sub
                    novo.pop("embedding", None)
                    processados.append(novo)
            else:
                processados.append(c)
        chunks = processados
        print(f"  Rechunk: {grandes} chunks grandes → {len(chunks)} sub-chunks")

    sem_embedding = [c for c in chunks if "embedding" not in c]
    com_embedding = [c for c in chunks if "embedding" in c]
    print(f"  {len(com_embedding)} já têm embedding | {len(sem_embedding)} precisam gerar")

    if not sem_embedding:
        print("Todos os chunks já têm embedding!")
        return

    if args.limit > 0:
        sem_embedding = sem_embedding[:args.limit]
        print(f"  Limitado a {args.limit} chunks (--limit)")

    if args.dry_run:
        print(f"\n[DRY RUN] Geraria {len(sem_embedding)} embeddings via Gemini text-embedding-004.")
        print(f"  Estimativa: ~{len(sem_embedding) * BATCH_DELAY:.0f}s de delay de throttle")
        return

    # Teste de conectividade
    print(f"\nTestando API Gemini...")
    teste = gerar_embedding("teste de conectividade GURPS", api_key, ctx)
    if teste is None:
        print("ERRO: Falha na API Gemini. Verifique a chave e conexão.")
        return
    print(f"  API OK — {len(teste)} dims por embedding")

    # Gera embeddings
    print(f"\nGerando {len(sem_embedding)} embeddings (Gemini text-embedding-004)...")
    t0 = time.time()
    ok = 0
    falhas = 0

    for i, chunk in enumerate(sem_embedding):
        texto = chunk.get("text", "")
        emb = gerar_embedding(texto, api_key, ctx)
        if emb:
            chunk["embedding"] = emb
            ok += 1
        else:
            falhas += 1
            print(f"  FALHA chunk {chunk.get('chunk_id', i)}")

        # Progresso a cada 50
        if (i + 1) % 50 == 0:
            elapsed = time.time() - t0
            restantes = len(sem_embedding) - (i + 1)
            eta = (elapsed / (i + 1)) * restantes
            print(f"  [{i+1}/{len(sem_embedding)}] {ok} OK | {falhas} falhas | ETA: {eta:.0f}s")

        time.sleep(BATCH_DELAY)

    elapsed = time.time() - t0
    print(f"\n  Concluído em {elapsed:.0f}s — {ok} embeddings gerados | {falhas} falhas")

    if ok == 0:
        print("Nenhum embedding gerado. Abortando gravação.")
        return

    # Backup
    if output_path == input_path and input_path.exists():
        backup = input_path.with_suffix(".jsonl.bak")
        shutil.copy2(input_path, backup)
        print(f"Backup: {backup}")

    # Salva
    todos = {c["chunk_id"]: c for c in (com_embedding + sem_embedding)}
    ordenados = [todos[c["chunk_id"]] for c in chunks if c["chunk_id"] in todos]

    with open(output_path, "w", encoding="utf-8") as f:
        for c in ordenados:
            f.write(json.dumps(c, ensure_ascii=False) + "\n")

    tamanho_mb = output_path.stat().st_size / 1024 / 1024
    print(f"\n✓ chunks.jsonl atualizado: {len(ordenados)} chunks | {tamanho_mb:.1f} MB")
    print(f"\nPróximos passos:")
    print(f"  1. Build + instalar o APK no emulador/device")
    print(f"  2. Logcat confirmará: 'TOTAL: {len(ordenados)} chunks | {ok} embeddings semânticos'")


if __name__ == "__main__":
    main()
