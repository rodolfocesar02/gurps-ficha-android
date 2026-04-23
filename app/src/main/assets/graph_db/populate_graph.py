import json
import os

assets_path = r'c:\Users\Rodolfo\Desktop\ficha gurps\ficha-gurps\gurps_app\gurps-ficha-android\app\src\main\assets'
output_file = os.path.join(assets_path, 'graph_db', 'graph_knowledge.json')

nodes = []

# 1. Regras Base (Mantendo o fundamental)
base_rules = [
    {"entity_id": "success_roll", "title": "Teste de Sucesso (3d6)", "summary": "Mecânica básica: Role 3d6 <= NH (Atributo + Perícia + Modificadores).", "category": "Sistema", "level": 1},
    {"entity_id": "st_atributo", "title": "Força (ST)", "summary": "Atributo de poder físico. Afeta Dano (Gpd/Bal), Carga e PV. Pág. 14.", "category": "Atributo", "level": 1},
    {"entity_id": "dx_atributo", "title": "Destreza (DX)", "summary": "Atributo de agilidade e coordenação. Base para combate. Pág. 15.", "category": "Atributo", "level": 1},
    {"entity_id": "iq_atributo", "title": "Inteligência (IQ)", "summary": "Atributo mental e percepção. Base para magias. Pág. 15.", "category": "Atributo", "level": 1},
    {"entity_id": "ht_atributo", "title": "Vitalidade (HT)", "summary": "Atributo de saúde e vigor. Afeta PF e sobrevivência. Pág. 15.", "category": "Atributo", "level": 1}
]
nodes.extend(base_rules)

def format_summary(item, category):
    """Cria um resumo inteligente dependendo da categoria do item."""
    summary_parts = []
    
    # Adiciona página se houver
    page = item.get('pagina') or item.get('page')
    if page:
        summary_parts.append(f"[Pág. {page}]")
    
    # Lógica por Categoria
    if category == 'Equipamento':
        # Se não tiver página no JSON, usa a página base da tabela no manual
        if not page:
            if 'pistola' in item.get('id', '').lower() or 'revolver' in item.get('id', '').lower() or 'rifle' in item.get('id', '').lower():
                page = 278
            elif 'armadura' in item.get('id', '').lower() or 'capacete' in item.get('id', '').lower() or 'traje' in item.get('id', '').lower():
                page = 282
            else:
                page = 271 # Armas brancas e outros
        
        if page:
            summary_parts.append(f"[Pág. {page}]")

        # Tenta pegar campos de arma/armadura
        dano_obj = item.get('dano')
        dano = dano_obj.get('raw') if isinstance(dano_obj, dict) else dano_obj
        
        rd_obj = item.get('rd')
        rd = rd_obj.get('raw') if isinstance(rd_obj, dict) else rd_obj
        
        if dano: summary_parts.append(f"Dano: {dano}")
        if rd: summary_parts.append(f"RD: {rd}")
        
        # Adiciona descrição se houver
        desc = item.get('descricao')
        if desc and desc != "Sem descrição disponível.":
            summary_parts.append(desc)
            
    elif category == 'Magia':
        # Tenta pegar campos de magia
        escola = item.get('escola')
        custo = item.get('custo')
        tempo = item.get('tempoConjuracao')
        if escola: summary_parts.append(f"Escola: {escola}")
        if custo: summary_parts.append(f"Custo: {custo}")
        if tempo: summary_parts.append(f"Tempo: {tempo}")
        
        desc = item.get('descricao')
        if desc: summary_parts.append(desc)
        
    else:
        # Vantagens, Perícias, Técnicas
        desc = item.get('descricao') or item.get('summary')
        if desc:
            summary_parts.append(desc)
            
    if not summary_parts:
        return "Item catalogado. Consulte o manual para detalhes."
        
    return " ".join(summary_parts)

def add_from_json(filename, category, id_field='id', name_field='nome'):
    path = os.path.join(assets_path, filename)
    if not os.path.exists(path):
        print(f"Arquivo não encontrado: {filename}")
        return
    
    try:
        with open(path, 'r', encoding='utf-8') as f:
            data = json.load(f)
            
            if isinstance(data, dict) and 'items' in data:
                items_list = data['items']
            elif isinstance(data, list):
                items_list = data
            else:
                return

            count = 0
            for item in items_list:
                node = {
                    "entity_id": f"{category.lower()}_{item.get(id_field)}",
                    "title": item.get(name_field, "Sem Nome"),
                    "summary": format_summary(item, category),
                    "category": category,
                    "level": 2
                }
                nodes.append(node)
                count += 1
            print(f"Adicionados {count} nós de {filename}")
    except Exception as e:
        print(f"Erro ao processar {filename}: {e}")

# Execução massiva
add_from_json('vantagens.v3.json', 'Vantagem')
add_from_json('desvantagens.v2.json', 'Desvantagem')
add_from_json('pericias.json', 'Perícia')
add_from_json('magias2versao.json', 'Magia')
add_from_json('tecnicas.v1.json', 'Técnica')
add_from_json('armas_fogo.v1.normalized.json', 'Equipamento')
add_from_json('armaduras.v2.json', 'Equipamento')

# Salvar o Super Grafo
with open(output_file, 'w', encoding='utf-8') as f:
    json.dump(nodes, f, ensure_ascii=False, indent=2)

print(f"Grafo finalizado com {len(nodes)} nós informativos!")
