import json
import os

def analisar_pericias():
    p_file = r'app/src/main/assets/pericias.json'
    r_file = r'app/src/main/assets/pericias_v2_rules_map.json'
    
    if not os.path.exists(p_file) or not os.path.exists(r_file):
        print("Erro: Arquivos não encontrados no caminho especificado.")
        return

    def get_data(f):
        with open(f, 'r', encoding='utf-8') as file:
            return json.load(file)

    data_p = get_data(p_file)
    data_r = get_data(r_file)

    ids_p = {str(i.get('id')).strip().lower() for i in data_p if i.get('id')}
    ids_r = {str(i.get('id')).strip().lower() for i in data_r.get('items', []) if i.get('id')}

    missing_in_p = sorted(list(ids_r - ids_p))
    
    print(f"Análise de Consistência GURPS:")
    print(f"------------------------------")
    print(f"Perícias no Seletor (pericias.json): {len(ids_p)}")
    print(f"Perícias no Motor (rules_map): {len(ids_r)}")
    print(f"\n[!] Encontradas {len(missing_in_p)} perícias que NÃO aparecem no seletor:")
    
    for idx, item_id in enumerate(missing_in_p, 1):
        # Tenta pegar o nome amigável no rules_map
        detalhe = next((i for i in data_r.get('items', []) if str(i.get('id')).lower() == item_id), {})
        nome = detalhe.get('nome', item_id)
        print(f"{idx}. {item_id} ({nome})")

if __name__ == "__main__":
    analisar_pericias()
