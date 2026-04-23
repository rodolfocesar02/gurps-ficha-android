import json
import os

def simulate_graph_rag(query, graph_json_path):
    with open(graph_json_path, 'r', encoding='utf-8') as f:
        graph_data = json.load(f)
    
    # Simulação simples de busca por palavra-chave (FTS manual)
    query_terms = query.lower().split()
    results = []
    for node in graph_data:
        match_score = 0
        text_to_search = (node['title'] + " " + node['summary'] + " " + node['category']).lower()
        for term in query_terms:
            if term in text_to_search:
                match_score += 1
        
        if match_score > 0:
            results.append((match_score, node))
    
    results.sort(key=lambda x: x[0], reverse=True)
    return [r[1] for r in results[:5]]

def format_for_ai(nodes):
    output = "\nCONHECIMENTO DO GRAFO (Contexto Macro e Relações):\n"
    for node in nodes:
        output += f"Tópico: {node['title']} | Resumo: {node['summary']}\n"
    return output

if __name__ == "__main__":
    GRAPH_PATH = r"app/src/main/assets/graph_db/graph_knowledge.json"
    TEST_QUERIES = [
        "Furtividade e sombras",
        "Dano de colisão e queda",
        "Magia de fogo e resistência térmica"
    ]
    
    print("=== DIAGNÓSTICO DO MOTOR GRAPHRAG LITE ===")
    
    for q in TEST_QUERIES:
        print(f"\nQUERY: '{q}'")
        nodes = simulate_graph_rag(q, GRAPH_PATH)
        if nodes:
            print(format_for_ai(nodes))
        else:
            print("Nenhum nó de grafo encontrado.")
