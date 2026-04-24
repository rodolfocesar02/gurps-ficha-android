import os
from nano_graphrag import GraphRAG, QueryParam
import asyncio

# Configura\u00e7\u00e3o do ambiente
def load_keys():
    keys = {}
    path = os.path.join(os.path.dirname(__file__), "..", "local.properties")
    if os.path.exists(path):
        with open(path, "r") as f:
            for line in f:
                if "=" in line:
                    k, v = line.strip().split("=", 1)
                    keys[k] = v
    return keys

KEYS = load_keys()
os.environ["OPENROUTER_API_KEY"] = KEYS.get("mestre.ia.openrouter.2.key") or os.getenv("OPENROUTER_API_KEY") or ""

WORKING_DIR = r"c:\Users\Rodolfo\Desktop\ficha gurps\ficha-gurps\gurps_app\gurps-ficha-android\scratch\nano_graph_working"
MANUAL_PATH = r"c:\Users\Rodolfo\Desktop\ficha gurps\ficha-gurps\gurps_app\gurps-ficha-android\scratch\manual_gurps_limpo.txt"

if not os.path.exists(WORKING_DIR):
    os.makedirs(WORKING_DIR)

# Inicializar o GraphRAG
# Vamos usar o Llama 3.3 via OpenRouter para a extra\u00e7\u00e3o
# (O Nano-GraphRAG suporta LiteLLM ent\u00e3o a URL do OpenRouter deve funcionar)
rag = GraphRAG(
    working_dir=WORKING_DIR,
    best_model_id="openrouter/meta-llama/llama-3.3-70b-instruct",
    cheap_model_id="openrouter/meta-llama/llama-3.1-8b-instruct"
)

async def run_extraction():
    print("Iniciando Extra\u00e7\u00e3o do Grafo de COMBATE (IA lendo o manual)...")
    with open(MANUAL_PATH, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Vamos pegar apenas uma parte focada em combate para n\u00e3o queimar todos os cr\u00e9ditos
    # Geralmente o combate come\u00e7a ap\u00f3s a p\u00e1gina 300.
    combat_content = content[200000:300000] # Trecho aproximado do manual focado em regras
    
    # Extrair entidades e relacionamentos
    await rag.insert(combat_content)
    print("Extra\u00e7\u00e3o de Combate finalizada!")

if __name__ == "__main__":
    asyncio.run(run_extraction())
