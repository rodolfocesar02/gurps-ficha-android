import os
import json
import requests
import sys

# Força o console a usar UTF-8
sys.stdout.reconfigure(encoding='utf-8')

API_KEY = os.getenv("OPENROUTER_API_KEY")
MODEL_NAME = "meta-llama/llama-3.3-70b-instruct" 
GRAPH_PATH = r"c:\Users\Rodolfo\Desktop\ficha gurps\ficha-gurps\gurps_app\gurps-ficha-android\app\src\main\assets\graph_db\graph_knowledge.json"

def search_in_graph(query):
    with open(GRAPH_PATH, 'r', encoding='utf-8') as f:
        graph = json.load(f)
    
    results = []
    query_lower = query.lower()
    
    is_combat = any(k in query_lower for k in ["dano", "ataque", "defesa", "atropela", "carga"])
    
    for node in graph:
        score = 0
        title_lower = node["title"].lower()
        
        # Se bater palavra exata (ex: colisão), ganha muito ponto
        for word in query_lower.split():
            if len(word) > 3 and word in title_lower:
                score += 10
        
        # Se for combate e for da categoria certa, ganha ponto
        if is_combat and node.get("category") == "Regra Combat":
            score += 5
            
        if score > 0:
            node["_score"] = score
            results.append(node)
            
    # Ordena pelos mais relevantes primeiro
    results.sort(key=lambda x: x["_score"], reverse=True)
            
    seen = set()
    unique_results = []
    for r in results:
        if r["entity_id"] not in seen:
            unique_results.append(r)
            seen.add(r["entity_id"])
            
    return unique_results[:15] # Limite maior para não perder nada

def ask_auditor(prompt):
    nodes = search_in_graph(prompt)
    context = "\n".join([f"Tópico: {n['title']} | Resumo: {n['summary']}" for n in nodes])
    
    messages = [
        {"role": "system", "content": "Você é o Mestre IA. Use o CONTEXTO abaixo. Calcule passo a passo. Cite a página [MB pág. X]."},
        {"role": "user", "content": f"CONTEXTO DO GRAFO:\n{context}\n\nPERGUNTA: {prompt}"}
    ]
    
    response = requests.post(
        url="https://openrouter.ai/api/v1/chat/completions",
        headers={"Authorization": f"Bearer {API_KEY}"},
        json={"model": MODEL_NAME, "messages": messages, "temperature": 0.0}
    )
    return response.json()['choices'][0]['message']['content'], context

def run_test():
    q = "Um cavaleiro em carga a cavalo (Move 8) atinge um soldado. O cavalo tem ST 24. Como calculo o dano de colisão?"
    
    print(f"\n--- TESTE HONESTO (ALTA RELEVÂNCIA) ---\n")
    veredito, contexto_usado = ask_auditor(q)
    
    print(f"REGRAS ENCONTRADAS:\n{contexto_usado}")
    print(f"\nRESPOSTA:\n{veredito}")

if __name__ == "__main__":
    run_test()
