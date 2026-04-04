#!/usr/bin/env python3
from __future__ import annotations

import hashlib
import json
import os
import re
import unicodedata
from collections import defaultdict
from datetime import datetime, timezone
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Dict, List, Optional, Union

from dotenv import load_dotenv

try:
    import chromadb
except Exception as exc:  # pragma: no cover
    raise RuntimeError("Dependencia ausente: chromadb. Rode `pip install -r requirements.txt`.") from exc


def _safe_int(value: Any, default: int) -> int:
    try:
        return int(value)
    except Exception:
        return default


def _resolve_env_path(raw_value: str, backend_dir: Path, must_exist: bool) -> Path:
    raw = (raw_value or "").strip()
    candidate = Path(raw)
    if candidate.is_absolute():
        return candidate

    trial_paths = [
        Path.cwd() / raw,
        backend_dir / raw,
        backend_dir.parent / raw,
        backend_dir.parent.parent / raw,
    ]
    if must_exist:
        for path in trial_paths:
            if path.exists():
                return path.resolve()
    return trial_paths[0].resolve()


@dataclass
class RagSettings:
    repo_root: Path
    chroma_dir: Path
    chunks_file: Path
    reports_dir: Path
    collection_name: str
    top_k: int
    openai_api_key: str
    openai_base_url: str
    openai_embed_model: str
    openai_chat_model: str


def load_settings() -> RagSettings:
    backend_dir = Path(__file__).resolve().parent
    env_file = backend_dir / ".env"
    if env_file.exists():
        load_dotenv(env_file)
    else:
        load_dotenv()

    chroma_rel = os.getenv("CHROMA_DIR", "chroma")
    chunks_rel = os.getenv("CHUNKS_FILE", "chunks.jsonl")
    reports_rel = os.getenv("REPORTS_DIR", "reports")

    chunks_path = _resolve_env_path(chunks_rel, backend_dir, must_exist=False)
    if not chunks_path.exists() and Path("chunks.jsonl").exists():
        chunks_path = Path("chunks.jsonl").resolve()

    return RagSettings(
        repo_root=Path.cwd().resolve(),
        chroma_dir=_resolve_env_path(chroma_rel, backend_dir, must_exist=False),
        chunks_file=chunks_path,
        reports_dir=_resolve_env_path(reports_rel, backend_dir, must_exist=False),
        collection_name=os.getenv("RAG_COLLECTION", "gurps_pt_v1"),
        top_k=_safe_int(os.getenv("RAG_TOP_K"), 6),
        openai_api_key=os.getenv("OPENAI_API_KEY", "").strip(),
        openai_base_url=os.getenv("OPENAI_BASE_URL", "").strip(),
        openai_embed_model=os.getenv("OPENAI_EMBED_MODEL", "text-embedding-3-small"),
        openai_chat_model=os.getenv("OPENAI_CHAT_MODEL", "gpt-4.1-mini"),
    )


def _hash_embedding(text: str, dim: int = 256) -> List[float]:
    vec = [0.0] * dim
    words = text.lower().split()
    if not words:
        return vec
    for token in words:
        digest = hashlib.sha256(token.encode("utf-8")).digest()
        idx = int.from_bytes(digest[:4], byteorder="big") % dim
        vec[idx] += 1.0
    norm = sum(x * x for x in vec) ** 0.5
    if norm > 0:
        vec = [x / norm for x in vec]
    return vec


def _normalize_text(text: str) -> str:
    normalized = unicodedata.normalize("NFKD", text.lower())
    return "".join(ch for ch in normalized if not unicodedata.combining(ch))


def _expand_query(question: str) -> str:
    qn = _normalize_text(question)
    extras: List[str] = []
    if "judo" in qn:
        extras.extend(["queda", "imobilizacao", "arremesso"])
    if "aptidao magica" in qn:
        extras.extend(["magi", "nivel", "custo", "pontos"])
    if "custo" in qn and "magia" in qn:
        extras.extend(["pontos", "nivel", "aprendizado"])
    if "pre requisito" in qn or "prerequisito" in qn:
        extras.extend(["cadeia", "exigencia", "destravar"])
    if not extras:
        return question
    return question + " " + " ".join(extras)


def build_embeddings(settings: RagSettings, texts: List[str]) -> List[List[float]]:
    if settings.openai_api_key:
        try:
            from openai import OpenAI
            client = OpenAI(api_key=settings.openai_api_key, base_url=settings.openai_base_url or None)
            response = client.embeddings.create(model=settings.openai_embed_model, input=texts)
            return [item.embedding for item in response.data]
        except Exception:
            pass
    return [_hash_embedding(text) for text in texts]


def get_chroma_collection(settings: RagSettings):
    client = chromadb.PersistentClient(path=str(settings.chroma_dir))
    return client.get_or_create_collection(name=settings.collection_name, metadata={"hnsw:space": "cosine"})


def load_chunks(chunks_file: Path) -> List[Dict[str, Any]]:
    if not chunks_file.exists():
        raise FileNotFoundError(f"Arquivo de chunks nao encontrado: {chunks_file}")
    items: List[Dict[str, Any]] = []
    with chunks_file.open("r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line: continue
            row = json.loads(line)
            if row.get("text"): items.append(row)
    return items


def _batched(items: List[Dict[str, Any]], batch_size: int = 128):
    for start in range(0, len(items), batch_size):
        yield items[start : start + batch_size]


def reindex_collection(settings: RagSettings) -> Dict[str, Any]:
    print(f"--- INICIANDO REINDEXAÇÃO DA COLEÇÃO: {settings.collection_name} ---")
    client = chromadb.PersistentClient(path=str(settings.chroma_dir))
    try:
        client.delete_collection(settings.collection_name)
        print("Coleção antiga removida.")
    except Exception:
        pass
    collection = client.get_or_create_collection(name=settings.collection_name, metadata={"hnsw:space": "cosine"})
    
    print(f"Carregando chunks de: {settings.chunks_file}")
    chunks = load_chunks(settings.chunks_file)
    total = 0
    batch_idx = 0
    for batch in _batched(chunks):
        batch_idx += 1
        print(f"Processando lote #{batch_idx} ({len(batch)} chunks)...")
        ids = [row["chunk_id"] for row in batch]
        docs = [row["text"] for row in batch]
        metas = [{"chunk_id": r["chunk_id"], "source_id": r["source_id"], "source_title": r["source_title"], "page_number": int(r["page_number"])} for r in batch]
        embs = build_embeddings(settings, docs)
        collection.upsert(ids=ids, documents=docs, metadatas=metas, embeddings=embs)
        total += len(batch)
    print(f"--- REINDEXAÇÃO CONCLUÍDA: {total} chunks indexados ---")
    return {"total_chunks_indexados": total}


def ensure_collection_ready(settings: RagSettings) -> Dict[str, Any]:
    collection = get_chroma_collection(settings)
    try:
        count = int(collection.count())
    except Exception:
        count = 0

    auto_index = os.getenv("RAG_AUTO_INDEX_ON_STARTUP", "1").strip().lower() not in {"0", "false", "no"}
    if count > 0 or not auto_index:
        return {"ready": count > 0, "auto_index": auto_index, "count": count}
    
    if not settings.chunks_file.exists():
        return {
            "ready": False,
            "auto_index": auto_index,
            "count": 0,
            "error": f"chunks_file nao encontrado: {settings.chunks_file}",
        }

    report = reindex_collection(settings)
    return {"ready": True, "auto_index": auto_index, "count": report.get("total_chunks_indexados", 0), "reindexed": True}



def retrieve_context(settings: RagSettings, question: str, top_k: Optional[int] = None) -> Dict[str, Any]:
    collection = get_chroma_collection(settings)
    q = _expand_query(question)
    emb = build_embeddings(settings, [q])[0]
    result = collection.query(query_embeddings=[emb], n_results=top_k or settings.top_k, include=["documents", "metadatas", "distances"])
    items = []
    for doc, meta, dist in zip(result["documents"][0], result["metadatas"][0], result["distances"][0]):
        items.append({
            "text": doc, 
            "score": round(1.0 - float(dist), 4),
            "source_id": meta["source_id"],
            "source_title": meta["source_title"],
            "page_number": meta["page_number"],
            "chunk_id": meta["chunk_id"]
        })
    return {"items": items}


def _significant_tokens(question: str) -> List[str]:
    stop = {"qual", "como", "quando", "onde", "porque", "para", "com", "uma", "um", "dos", "das", "que", "os", "as"}
    return [t for t in re.findall(r"[a-z0-9áéíóúãõâêîôûç]+", question.lower()) if len(t) >= 3 and t not in stop]


def evaluate_evidence(question: str, context_items: List[Dict[str, Any]]) -> Dict[str, Any]:
    if not context_items: return {"enough": False, "best_score": 0.0, "max_overlap": 0}
    best_score = float(context_items[0]["score"])
    tokens = _significant_tokens(question)
    max_overlap = 0
    for item in context_items:
        overlap = sum(1 for tok in tokens if tok in item["text"].lower())
        if overlap > max_overlap: max_overlap = overlap
    enough = (best_score >= 0.28 and max_overlap >= 1) or (max_overlap >= 2)
    return {"enough": enough, "best_score": best_score, "max_overlap": max_overlap}


def get_catalog_names(tipo: str) -> str:
    """Carrega a lista de nomes oficiais do catálogo txt. Busca na raiz do projeto."""
    backend_dir = Path(__file__).resolve().parent
    # Busca na mesma pasta que o script (Estrutura Plana do HF Space)
    catalog_path = backend_dir / f"{tipo}_nomes.txt"
    if catalog_path.exists():
        try:
            return catalog_path.read_text(encoding="utf-8")
        except Exception:
            return ""
    return ""


def answer_with_citations(settings: RagSettings, question: str, mode: str, context_items: List[Dict[str, Any]], evidence: Optional[Dict[str, Any]] = None) -> str:
    from openai import OpenAI
    
    # 1. Definir Configurações de Provedores (Prioridade: DeepSeek Pago -> OpenRouter Backup)
    providers = []
    
    # Adiciona Provedor DeepSeek Direto se houver chave
    ds_key = os.getenv("DEEPSEEK_API_KEY", "").strip()
    if ds_key:
        providers.append({
            "name": "DeepSeek-Direct",
            "client": OpenAI(base_url="https://api.deepseek.com", api_key=ds_key),
            "models": ["deepseek-reasoner", "deepseek-chat"]
        })
    
    # Adiciona Provedor Gemini Direto se houver chave
    gem_key = os.getenv("GEMINI_API_KEY", "").strip()
    if gem_key:
        providers.append({
            "name": "Gemini-Direct",
            "client": OpenAI(base_url="https://generativelanguage.googleapis.com/v1beta/openai", api_key=gem_key),
            "models": ["gemini-1.5-pro", "gemini-1.5-flash"]
        })

    # Backup final: OpenRouter (com a lista de modelos resiliente)
    or_key = os.getenv("OPENROUTER_API_KEY", settings.openai_api_key).strip()
    if or_key:
        providers.append({
            "name": "OpenRouter",
            "client": OpenAI(base_url="https://openrouter.ai/api/v1", api_key=or_key),
            "models": [
                "meta-llama/llama-3.3-70b-instruct:free",
                "deepseek/deepseek-r1:free",
                "qwen/qwen-2-72b-instruct:free",
                "google/gemini-2.0-flash-exp:free"
            ]
        })

    if not providers:
        return "Erro Crítico: Nenhuma chave API (DeepSeek, Gemini ou OpenRouter) configurada no Hugging Face."

    last_error = ""
    rag_info = "\n\n--- LIVROS GURPS ---\n" + "\n".join([f"Ref {i+1} (Pag {item['page_number']}): {item['text']}" for i, item in enumerate(context_items)])

    # 2. Ciclo de Tentativas em Cascata
    for provider in providers:
        for model_id in provider["models"]:
            try:
                # Ajusta instruções baseadas no modo
                if mode in ["criacao", "geracao", "personagem"]:
                    # Carregar nomes oficiais para injetar no prompt
                    p_nomes = get_catalog_names("pericias")
                    m_nomes = get_catalog_names("magias")
                    v_nomes = get_catalog_names("vantagens")
                    t_nomes = get_catalog_names("tecnicas")

                    instr = (
                        "Você é o MESTRE AUDITOR GURPS 4ª Edição especializado em JSON estruturado.\n"
                        "Sua missão é criar fichas 100% compatíveis com o banco de dados do App.\n\n"
                        "--- REGRAS DE OURO ---\n"
                        "1. NOMES OFICIAIS: Use APENAS os nomes contidos nos catálogos abaixo.\n"
                        "2. PRÉ-REQUISITOS: Antes de adicionar uma Magia ou Técnica, consulte o RAG para ver os requisitos (AM, IQ, Magias anteriores).\n"
                        "3. CADEIA DE PENSAMENTO: Se uma magia exige outra, adicione AMBAS à ficha.\n"
                        "4. ATRIBUTOS: Base 10. Calcule os custos corretamente (+/- 10/20 pts).\n\n"
                        "--- CATÁLOGO DE NOMES (Sincronizado com o App) ---\n"
                        f"PERÍCIAS: {p_nomes[:2000]}...\n"
                        f"MAGIAS: {m_nomes[:2000]}...\n"
                        f"VANTAGENS: {v_nomes[:2000]}...\n"
                        f"TÉCNICAS: {t_nomes[:2000]}...\n\n"
                        "--- OUTPUT SCHEMA ---\n"
                        "Responda EXCLUSIVAMENTE com o JSON da ficha. Não use Markdown blocks (```json) se possível, apenas o texto do objeto.\n"
                        "Estrutura: {'nome':'', 'atributos':{'st':10...}, 'pericias':[{'nome','pontos','nh'}], 'vantagens':[], 'magias':[]}\n"
                    )
                elif mode == "analise":
                    instr = "Você é um AUDITOR TÉCNICO de GURPS. Analise a ficha enviada, verifique erros de pontuação e sugira melhorias baseadas nos livros anexados (RAG)."
                else:
                    instr = "Você é um MESTRE DE GURPS prestativo. Use as referências dos livros para tirar dúvidas de regras ou ambientação."

                # Chamada da API
                completion = provider["client"].chat.completions.create(
                    model=model_id,
                    messages=[{"role": "system", "content": instr}, {"role": "user", "content": f"{question}\n{rag_info}"}],
                    timeout=120.0
                )
                return completion.choices[0].message.content

            except Exception as e:
                err_msg = str(e).lower()
                # LOG DE DEPURAÇÃO PARA O USUÁRIO
                print(f"--- DEBUG: Falha no Provedor {provider['name']} (Modelo: {model_id}) ---")
                print(f"Mensagem de Erro: {str(e)}")
                
                last_error = f"Provedor {provider['name']} Model {model_id}: {str(e)}"
                
                # Se for erro de autenticação fatal, pula o provedor inteiro
                if "api key" in err_msg or "authentication" in err_msg:
                    print(f"Erro Fatal de Autenticação no provedor {provider['name']}. Verifique a Secret no HF.")
                    break 
                
                print(f"Tentando próximo modelo do mesmo provedor ou próximo provedor...")
                continue

    return f"Erro Mestre Digital (Todos os provedores e modelos falharam): {last_error}"
