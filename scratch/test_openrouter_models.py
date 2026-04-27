import requests
import json

def test_openrouter():
    api_key = "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"
    url = "https://openrouter.ai/api/v1/models"
    
    headers = {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json"
    }
    
    print("--- TESTANDO CONEXÃO COM OPENROUTER ---")
    try:
        response = requests.get(url, headers=headers)
        if response.status_code == 200:
            models = response.json().get('data', [])
            # Filtrar apenas modelos grátis ou os principais autos
            free_models = [m for m in models if 'free' in m.get('id', '').lower() or m.get('pricing', {}).get('prompt') == '0']
            
            print(f"Sucesso! Encontrados {len(models)} modelos no total.")
            print(f"Modelos GRATUITOS detectados ({len(free_models)}):")
            for m in free_models[:15]: # Limitar aos 15 primeiros para o log
                print(f"- {m.get('id')} ({m.get('name')})")
            
            if not free_models:
                print("Aviso: Nenhum modelo explicitamente 'free' encontrado, mas o 'openrouter/auto' deve funcionar com modelos de baixo custo.")
                
        else:
            print(f"Erro na API: {response.status_code}")
            print(response.text)
            
    except Exception as e:
        print(f"Erro de conexão: {str(e)}")

if __name__ == "__main__":
    test_openrouter()
