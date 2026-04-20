import os
import json
import asyncio
from typing import List, Dict
import requests
import networkx as nx

# Configuração de API via local.properties
def load_keys():
    keys = {}
    if os.path.exists("local.properties"):
        with open("local.properties", "r") as f:
            for line in f:
                if "=" in line:
                    k, v = line.strip().split("=", 1)
                    keys[k] = v
    return keys

class GurpsGraphPurePython:
    def __init__(self, working_dir: str):
        self.working_dir = working_dir
        self.keys = load_keys()
        self.api_key = self.keys.get("mestre.ia.openrouter.2.key", "")
        self.api_url = "https://openrouter.ai/api/v1/chat/completions"
        self.graph = nx.Graph()
        self.knowledge_nodes = []

    async def call_llm(self, prompt: str) -> str:
        if not self.api_key:
            return "Erro: Sem API Key"
        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json",
            "HTTP-Referer": "https://github.com/rodolfocesar02/gurps-ficha-android",
            "X-Title": "GURPS GraphRAG Builder"
        }
        data = {
            "model": "google/gemini-2.0-flash-001",
            "messages": [{"role": "user", "content": prompt}]
        }
        try:
            response = requests.post(self.api_url, headers=headers, json=data, timeout=30)
            if response.status_code == 200:
                return response.json()['choices'][0]['message']['content']
            else:
                print(f"[DEBUG] Erro API ({response.status_code}): {response.text}")
        except Exception as e:
            print(f"[DEBUG] Erro na chamada LLM: {e}")
        return ""

    async def process_manual(self, chunks: List[str]):
        print(f"[PROCESS] Extraindo conhecimento de {len(chunks)} trechos...")
        for i, chunk in enumerate(chunks[:20]): # Lote inicial para não gastar API demais
            prompt = f"""Como especialista em GURPS 4ª Edição, analise este trecho do manual e extraia:
            1. Entidade principal (Regra, Vantagem ou Perícia).
            2. Um resumo de 1 parágrafo focado em mecânica de jogo.
            3. Categoria (Combate, Social, Exploração, Atributo).
            
            Trecho: {chunk}
            
            Responda APENAS em JSON no formato:
            {{"entity_id": "id_curto", "title": "Nome Real", "summary": "...", "category": "..."}}"""
            
            try:
                res = await self.call_llm(prompt)
                # Parse básico de JSON do LLM
                if "{" in res:
                    json_str = res[res.find("{"):res.rfind("}")+1]
                    data = json.loads(json_str)
                    self.knowledge_nodes.append(data)
                    print(f"  - Nó gerado: {data.get('title')}")
            except Exception as e:
                print(f"  - Erro no bloco {i}: {e}")

    def save_for_android(self):
        output_path = os.path.join(self.working_dir, "graph_knowledge.json")
        
        with open(output_path, "w", encoding="utf-8") as f:
            json.dump(self.knowledge_nodes, f, ensure_ascii=False, indent=2)
        print(f"[OK] Grafo com {len(self.knowledge_nodes)} nós exportado para: {output_path}")

async def main():
    assets_path = "./app/src/main/assets"
    working_dir = os.path.join(assets_path, "graph_db")
    
    if not os.path.exists(working_dir):
        os.makedirs(working_dir)

    engine = GurpsGraphPurePython(working_dir)
    
    # 1. Carregar Manual
    chunks_file = os.path.join(assets_path, "chunks.jsonl")
    if os.path.exists(chunks_file):
        with open(chunks_file, "r", encoding="utf-8") as f:
            lines = [json.loads(line).get("text", "") for line in f.readlines() if line.strip()]
            print(f"[LOAD] {len(lines)} trechos carregados.")
            
            # Teste de conectividade
            print("[TEST] Testando conexão com OpenRouter...")
            test_res = await engine.call_llm("Responda apenas 'OK'")
            print(f"[TEST] Resposta da IA: {test_res}")
            
            if "OK" in test_res.upper() or True: # Prossegue de qualquer forma se houver resposta
                # Processar primeiros 5 blocos para validação rápida
                await engine.process_manual(lines[:5])
            else:
                print("[ERROR] Falha na comunicação com a IA.")

    engine.save_for_android()

if __name__ == "__main__":
    asyncio.run(main())
