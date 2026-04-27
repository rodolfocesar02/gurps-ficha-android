import json
import os
import re
import urllib.request
import urllib.parse
import sys

# Garante que o output seja UTF-8 para evitar erros de encoding no Windows
sys.stdout.reconfigure(encoding='utf-8')

# --- CONFIGURAÇÃO ---
BASE_PATH = r'c:\Users\Rodolfo\Desktop\ficha gurps\ficha-gurps\gurps_app\gurps-ficha-android'
ASSETS_PATH = os.path.join(BASE_PATH, 'app', 'src', 'main', 'assets')
LOCAL_PROPERTIES = os.path.join(BASE_PATH, 'local.properties')

def get_api_key():
    try:
        with open(LOCAL_PROPERTIES, 'r') as f:
            content = f.read()
            match = re.search(r'mestre\.ia\.openrouter\.2\.key=(.+)', content)
            if match:
                return match.group(1).strip()
    except: pass
    return None

def search_chunks(query, limit=10):
    chunks_path = os.path.join(ASSETS_PATH, 'chunks.jsonl')
    results = []
    keywords = query.lower().split()
    
    try:
        with open(chunks_path, 'r', encoding='utf-8') as f:
            for line in f:
                if not line.strip(): continue
                chunk = json.loads(line)
                text = chunk.get('text', '').lower()
                score = sum(1 for kw in keywords if kw in text)
                if score > 0:
                    results.append((score, chunk))
    except Exception as e:
        print(f"Erro ao ler chunks: {e}")
        return []
    
    results.sort(key=lambda x: x[0], reverse=True)
    return [r[1] for r in results[:limit]]

def search_graph(query, limit=5):
    graph_path = os.path.join(ASSETS_PATH, 'graph_db', 'graph_knowledge.json')
    try:
        with open(graph_path, 'r', encoding='utf-8') as f:
            graph = json.load(f)
    except: return []
    
    keywords = query.lower().split()
    results = []
    for node in graph:
        text = (node.get('title', '') + " " + node.get('summary', '')).lower()
        score = sum(1 for kw in keywords if kw in text)
        if score > 0:
            results.append((score, node))
            
    results.sort(key=lambda x: x[0], reverse=True)
    return [r[1] for r in results[:limit]]

def call_openrouter(prompt, context_chunks, context_summaries, api_key):
    url = "https://openrouter.ai/api/v1/chat/completions"
    
    fragmentos = "\nREGRAS DO CÓDEX (Siga estas regras à risca):\n"
    for c in context_chunks:
        # Limpa um pouco o texto para o prompt
        text = c.get('text', '')[:1500] 
        fragmentos += f"[{c.get('source_title', 'Manual')} Pág. {c.get('page_number', '??')}]: {text}\n"
        
    resumos = "\nCONHECIMENTO DO GRAFO (Contexto Macro e Relações):\n"
    for s in context_summaries:
        resumos += f"Tópico: {s.get('title')} | Resumo: {s.get('summary')}\n"

    system_pulse = f"""
    Você é o Mestre Digital 2.0. Seu objetivo é ser um auditor de regras de GURPS 4ª Edição INFALÍVEL.
    
    DIRETRIZES DE BLINDAGEM:
    1. PROIBIÇÃO DE INFERÊNCIA: Você está terminantemente proibido de usar lógica interna para calcular atributos derivados.
    2. FONTE ÚNICA: Se o valor não está no CODEX fornecido, você NÃO PODE responder como se fosse regra oficial. 
    3. CITAÇÃO LITERAL OBRIGATÓRIA: Toda regra usada deve ser precedida por uma citação literal no formato [Livro, Pág. X].
    4. ADMISSÃO DE VÁCUO: Se a informação não estiver no texto injetado, admita que não sabe e não invente.
    
    {resumos}
    {fragmentos}
    """.strip()

    data = {
        "model": "qwen/qwen-2.5-72b-instruct",
        "messages": [
            {"role": "system", "content": system_pulse},
            {"role": "user", "content": prompt}
        ],
        "temperature": 0.1
    }

    req = urllib.request.Request(url, data=json.dumps(data).encode('utf-8'))
    req.add_header('Content-Type', 'application/json')
    req.add_header('Authorization', f'Bearer {api_key}')
    req.add_header('HTTP-Referer', 'https://github.com/mestre-ia-gurps')
    req.add_header('X-Title', 'GURPS Ficha Android Test')

    try:
        with urllib.request.urlopen(req) as response:
            res_body = response.read().decode('utf-8')
            return json.loads(res_body)
    except Exception as e:
        return {"error": str(e)}

def run_test(name, query, search_query):
    print(f"\n{'='*20} TESTE {name}: {query} {'='*20}")
    api_key = get_api_key()
    if not api_key:
        print("Erro: API Key não encontrada.")
        return

    chunks = search_chunks(search_query)
    summaries = search_graph(search_query)
    
    print(f"-> Injetando {len(chunks)} fragmentos e {len(summaries)} nós do grafo.")
    
    response = call_openrouter(query, chunks, summaries, api_key)
    
    if "error" in response:
        print(f"Erro na chamada: {response['error']}")
        return

    answer = response['choices'][0]['message']['content']
    print("\n--- RESPOSTA DO MESTRE IA ---")
    print(answer)
    print("------------------------------")
    
    # Validação Básica
    has_citation = "[" in answer and "Pág" in answer
    print(f"\nVERIFICAÇÃO AUTOMÁTICA:")
    print(f"- Possui Citação [Pág X]? {'SIM' if has_citation else 'NÃO'}")
    
    pages_injected = [str(c.get('page_number')) for c in chunks]
    found_pages = [p for p in pages_injected if p in answer]
    if found_pages:
        print(f"- Citou páginas injetadas ({', '.join(set(found_pages))})? SIM")
    else:
        print(f"- Citou páginas injetadas? NÃO (Verificar se usou conhecimento externo)")

# --- EXECUÇÃO ---
if __name__ == "__main__":
    # Teste 1: Básico
    run_test("BÁSICO", "Como funciona um teste de sucesso?", "teste sucesso 3d6 nh")
    
    # Teste 2: Moderado
    run_test("MODERADO", "Quais os modificadores para atacar o crânio?", "crânio cranio ataque modificador")
    
    # Teste 3: Avançado
    run_test("AVANÇADO", "Como calculo o dano de uma colisão em GURPS?", "colisão colisao dano impacto velocidade hp")
