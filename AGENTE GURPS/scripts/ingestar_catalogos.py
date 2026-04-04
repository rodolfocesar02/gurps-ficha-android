#!/usr/bin/env python3
import json
import os
import sys
from pathlib import Path
from typing import List, Dict, Any

# Adicionar caminho do backend para importar rag_runtime
sys.path.append(str(Path(__file__).resolve().parent.parent / "backend"))
import rag_runtime

def load_json(path: Path) -> Any:
    with path.open("r", encoding="utf-8") as f:
        return json.load(f)

def extract_names_to_txt(items: List[Dict[str, Any]], output_path: Path, field: str = "nome"):
    names = sorted(list(set([str(item.get(field, "")) for item in items if item.get(field)])))
    output_path.write_text(", ".join(names), encoding="utf-8")
    print(f"Gerado catálogo de nomes: {output_path}")

def process_catalogs():
    backend_dir = Path(__file__).resolve().parent.parent if "scripts" in str(Path(__file__)) else Path(__file__).resolve().parent
    # Tenta encontrar a pasta de assets padrão ou usa a raiz do projeto (HF Space)
    potential_assets = [
        Path(__file__).resolve().parent.parent.parent / "app" / "src" / "main" / "assets",
        Path.cwd(),
        backend_dir
    ]
    
    assets_dir = potential_assets[0]
    for p in potential_assets:
        if (p / "magias2versao.json").exists():
            assets_dir = p
            break

    catalogs_dir = backend_dir
    # Se estiver rodando no scripts/, movemos para a raiz se necessário
    if (backend_dir / "backend").exists():
        catalogs_dir = backend_dir / "backend"

    settings = rag_runtime.load_settings()
    collection = rag_runtime.get_chroma_collection(settings)

    # 1. MAGIAS
    magias_file = assets_dir / "magias2versao.json"
    if magias_file.exists():
        print("Processando Magias...")
        magias = load_json(magias_file)
        extract_names_to_txt(magias, catalogs_dir / "magias_nomes.txt")
        
        chunks = []
        for m in magias:
            nome = m.get("nome", "Sem Nome")
            pre = m.get("preRequisitos", [])
            descr = m.get("descricao", "")
            texto = f"MAGIA GURPS: {nome}. Requisitos: {', '.join(pre) if pre else 'Nenhum'}. Descrição: {descr}"
            
            chunks.append({
                "chunk_id": f"magia_{m.get('id', nome)}",
                "text": texto,
                "metadata": {
                    "tipo": "magia",
                    "nome": nome,
                    "id_app": m.get("id"),
                    "pre_requisitos": json.dumps(pre),
                    "source_id": "magias2versao.json",
                    "source_title": "Catálogo Oficial de Magias",
                    "page_number": 0
                }
            })
        
        # Upsert em lotes
        for start in range(0, len(chunks), 100):
            batch = chunks[start:start+100]
            ids = [c["chunk_id"] for c in batch]
            docs = [c["text"] for c in batch]
            metas = [c["metadata"] for c in batch]
            embs = rag_runtime.build_embeddings(settings, docs)
            collection.upsert(ids=ids, documents=docs, metadatas=metas, embeddings=embs)
        print(f"Indexadas {len(chunks)} magias.")

    # 2. PERÍCIAS (Rules Map)
    pericias_file = assets_dir / "pericias_v2_rules_map.json"
    if pericias_file.exists():
        print("Processando Perícias...")
        p_data = load_json(pericias_file)
        # Se for um dicionário de regras, pegamos as chaves ou o campo nome
        pericias = []
        if isinstance(p_data, dict):
            if "items" in p_data:
                pericias = p_data["items"]
            else:
                # Caso seja um dict mapeado por ID
                for k, v in p_data.items():
                    if isinstance(v, dict):
                        v["id"] = k
                        pericias.append(v)
        else:
            pericias = p_data

        extract_names_to_txt(pericias, catalogs_dir / "pericias_nomes.txt")
        
        chunks = []
        for p in pericias:
            nome = p.get("nome", p.get("id", "Sem Nome"))
            tipo_info = p.get("tipo", {})
            attr = "IQ" # Default
            if isinstance(tipo_info, dict):
                opts = tipo_info.get("attributeOptions", [])
                attr = opts[0] if opts else "IQ"
            
            texto = f"PERÍCIA GURPS: {nome}. Atributo: {attr}. Dificuldade: {p.get('dificuldadeFixa', 'M')}."
            
            chunks.append({
                "chunk_id": f"pericia_{p.get('id', nome)}",
                "text": texto,
                "metadata": {
                    "tipo": "pericia",
                    "nome": nome,
                    "id_app": p.get("id"),
                    "source_id": "pericias_v2_rules_map.json",
                    "source_title": "Catálogo Oficial de Perícias",
                    "page_number": 0
                }
            })
        
        for start in range(0, len(chunks), 100):
            batch = chunks[start:start+100]
            ids = [c["chunk_id"] for c in batch]
            docs = [c["text"] for c in batch]
            metas = [c["metadata"] for c in batch]
            embs = rag_runtime.build_embeddings(settings, docs)
            collection.upsert(ids=ids, documents=docs, metadatas=metas, embeddings=embs)
        print(f"Indexadas {len(chunks)} perícias.")

    # 4. TÉCNICAS
    tecnicas_file = assets_dir / "tecnicas.v1.json"
    if tecnicas_file.exists():
        print("Processando Técnicas...")
        t_data = load_json(tecnicas_file)
        tecnicas = t_data.get("items", []) if isinstance(t_data, dict) else t_data
        
        extract_names_to_txt(tecnicas, catalogs_dir / "tecnicas_nomes.txt")
        
        chunks = []
        for t in tecnicas:
            nome = t.get("nome", "Sem Nome")
            pre = t.get("preRequisitoRaw", "")
            base = t.get("preDefinidoRaw", "")
            texto = f"TÉCNICA GURPS: {nome}. Requisito: {pre}. Base: {base}. Descrição: {t.get('descricao','')}"
            
            chunks.append({
                "chunk_id": f"tecnica_{t.get('id', nome)}",
                "text": texto,
                "metadata": {
                    "tipo": "tecnica",
                    "nome": nome,
                    "id_app": t.get("id"),
                    "source_id": "tecnicas.v1.json",
                    "source_title": "Catálogo Oficial de Técnicas",
                    "page_number": 0
                }
            })
            
        for start in range(0, len(chunks), 100):
            batch = chunks[start:start+100]
            ids = [c["chunk_id"] for c in batch]
            docs = [c["text"] for c in batch]
            metas = [c["metadata"] for c in batch]
            embs = rag_runtime.build_embeddings(settings, docs)
            collection.upsert(ids=ids, documents=docs, metadatas=metas, embeddings=embs)
        print(f"Indexadas {len(chunks)} técnicas.")

    # 3. VANTAGENS
    vantagens_file = assets_dir / "vantagens.v3.json"
    if vantagens_file.exists():
        print("Processando Vantagens...")
        vantagens = load_json(vantagens_file)
        extract_names_to_txt(vantagens, catalogs_dir / "vantagens_nomes.txt")
        
        chunks = []
        for v in vantagens:
            nome = v.get("nome", "Sem Nome")
            pontos = v.get("pontos", "Varia")
            texto = f"VANTAGEM GURPS: {nome}. Custo: {pontos} pontos."
            
            chunks.append({
                "chunk_id": f"vantagem_{v.get('id', nome)}",
                "text": texto,
                "metadata": {
                    "tipo": "vantagem",
                    "nome": nome,
                    "id_app": v.get("id"),
                    "source_id": "vantagens.v3.json",
                    "source_title": "Catálogo Oficial de Vantagens",
                    "page_number": 0
                }
            })
        
        for start in range(0, len(chunks), 100):
            batch = chunks[start:start+100]
            ids = [c["chunk_id"] for c in batch]
            docs = [c["text"] for c in batch]
            metas = [c["metadata"] for c in batch]
            embs = rag_runtime.build_embeddings(settings, docs)
            collection.upsert(ids=ids, documents=docs, metadatas=metas, embeddings=embs)
        print(f"Indexadas {len(chunks)} vantagens.")

    print("\n--- INGESTÃO DE CATÁLOGOS CONCLUÍDA ---")

if __name__ == "__main__":
    process_catalogs()
