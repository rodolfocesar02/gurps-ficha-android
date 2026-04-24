import json
import os

path = r"c:\Users\Rodolfo\Desktop\ficha gurps\ficha-gurps\gurps_app\gurps-ficha-android\app\src\main\assets\graph_db\graph_knowledge.json"

def fix_encoding():
    print("Iniciando limpeza de codificação no Grafo...")
    
    # Lê o arquivo binário para não ter erro de decodificação
    with open(path, 'rb') as f:
        content = f.read()
    
    # Converte para texto substituindo o que estiver quebrado
    text = content.decode('utf-8', errors='replace')
    
    # Dicionário de conserto manual para os termos que a gente usa no RAG
    # Aqui vou usar os caracteres reais, como você pediu
    replacements = {
        'Coliso': 'Colisão',
        'pximo': 'próximo',
        'Pg.': 'Pág.',
        'Frações': 'Frações',
        'Asfixia e Afogamento (Fôlego)': 'Asfixia e Afogamento (Fôlego)',
        'você': 'você',
        'não': 'não',
        'Aparando Armas Pesadas': 'Aparando Armas Pesadas'
    }
    
    for k, v in replacements.items():
        text = text.replace(k, v)
        
    try:
        data = json.loads(text)
        with open(path, 'w', encoding='utf-8') as f:
            # ensure_ascii=False é o segredo para salvar acento de verdade no JSON
            json.dump(data, f, indent=2, ensure_ascii=False)
        print("Sucesso! Grafo agora está em UTF-8 limpo e legível.")
    except Exception as e:
        print(f"Erro ao salvar JSON: {e}")

if __name__ == "__main__":
    fix_encoding()
