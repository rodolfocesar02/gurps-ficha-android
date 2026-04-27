import json
import re

outline_path = r'c:\Users\Rodolfo\Desktop\ficha gurps\ficha-gurps\gurps_app\gurps-ficha-android\build\gurps_book_outline.txt'
graph_path = r'c:\Users\Rodolfo\Desktop\ficha gurps\ficha-gurps\gurps_app\gurps-ficha-android\app\src\main\assets\graph_db\graph_knowledge.json'

with open(graph_path, 'r', encoding='utf-8') as f:
    graph = json.load(f)

existing_titles = {node['title'].lower() for node in graph}

# Regex para capturar "- Nome :: Pagina"
pattern = re.compile(r'^\s*-\s+(.+?)\s+::\s+(\d+)')

new_nodes = []
with open(outline_path, 'r', encoding='utf-8') as f:
    in_target_section = False
    for line in f:
        # Focar em seções de Regras (10, 11, 12, 13, 14)
        if "10. Testes de Habilidade" in line or "11. Combate" in line or "12. Combate Tático" in line or "13. Situações Especiais" in line or "14. Lesões" in line:
            in_target_section = True
        
        if in_target_section:
            match = pattern.match(line)
            if match:
                title, page = match.groups()
                if title.lower() not in existing_titles:
                    entity_id = title.lower().replace(' ', '_').replace('(', '').replace(')', '').replace(',', '')
                    # Normalizar ID
                    entity_id = re.sub(r'[^a-z0-9_]', '', entity_id)
                    
                    new_nodes.append({
                        "entity_id": f"regra_{entity_id}",
                        "title": title,
                        "summary": f"[Pág. {page}] Regra do sistema catalogada via índice.",
                        "category": "Regra",
                        "level": 1
                    })
            
            # Se chegar na seção 15, para
            if "15. Criando Modelos" in line:
                in_target_section = False

print(f"Encontrados {len(new_nodes)} novos nós potenciais.")

# Adicionar ao grafo (sem duplicar IDs)
existing_ids = {node['entity_id'] for node in graph}
added_count = 0
for node in new_nodes:
    if node['entity_id'] not in existing_ids:
        graph.append(node)
        existing_ids.add(node['entity_id'])
        added_count += 1

with open(graph_path, 'w', encoding='utf-8') as f:
    json.dump(graph, f, ensure_ascii=False, indent=2)

print(f"Sucesso: {added_count} nós adicionados ao grafo.")
