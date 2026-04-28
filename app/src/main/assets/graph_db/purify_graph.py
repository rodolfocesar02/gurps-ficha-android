import json
import os

path = r'app\src\main\assets\graph_db\graph_knowledge.json'

with open(path, 'r', encoding='utf-8') as f:
    data = json.load(f)

for node in data:
    eid = node.get('entity_id', '').lower()
    
    # 1. Atribuição de Source ID por Padrão de Entity ID
    if 'artes_marciais' in eid:
        node['source_id'] = 'pt_artes_marciais'
    elif 'gun_fu' in eid:
        node['source_id'] = 'pt_gunfu'
    elif 'magia' in eid or 'feitiço' in eid:
        node['source_id'] = 'pt_magia'
    else:
        node['source_id'] = 'pt_modulo_basico'

    # 2. Refinamento de Nós Mestres (Pedido do Usuário)
    
    # Ataque Total
    if eid == 'regra_ataque_total':
        node['title'] = "Ataque Total (Manobra)"
        node['summary'] = (
            "Sacrifique TODA a defesa por um bônus ofensivo massivo. "
            "[MB Pág. 324 (Lite), 365 (Campanhas)]: Opções: Determinado (+4), Duplo (2 golpes), "
            "Forte (+2 dano/dado), Esquiva (Passo +2). "
            "[AM Pág. 128]: Adiciona opções cinematográficas e técnicas de Artes Marciais. "
            "[GF Pág. 10]: Ataques Totais com armas de fogo."
        )
        node['level'] = 0
        node['source_id'] = 'pt_modulo_basico'

    # Manobras de Combate
    if eid == 'manobras_combate_lista':
        node['title'] = "Manobras de Combate"
        node['summary'] = (
            "Lista de opções de ação por turno. "
            "[MB Pág. 363]: Ataque, Defesa Total, Movimento, Finta, Concentrar, etc. "
            "[AM Pág. 97-98]: Manobras de Combate avançadas (fintas complexas, transições)."
        )
        node['level'] = 0
        node['source_id'] = 'pt_modulo_basico'

    # Colisão (Ajuste para garantir unificação)
    if eid == 'colisoes_quedas':
        node['summary'] = (
            "[Pág. 433] Regra oficial de Dano por Colisão. "
            "Fórmula: (PV x Velocidade) / 100. "
            "Velocidade em m/s. PV do objeto/ser que colidiu."
        )
        node['source_id'] = 'pt_modulo_basico'

with open(path, 'w', encoding='utf-8') as f:
    json.dump(data, f, indent=2, ensure_ascii=False)

print(f"Processados {len(data)} nós no grafo.")
