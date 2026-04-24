import json
import re
import sys

# Forçar UTF-8 para evitar erros de caracteres no console
sys.stdout.reconfigure(encoding='utf-8')

def load_magias():
    with open('app/src/main/assets/magias2versao.json', 'r', encoding='utf-8') as f:
        return json.load(f)

def get_magia_by_nome(magias, nome):
    nome_clean = nome.lower().strip()
    for m in magias:
        if m['nome'].lower().strip() == nome_clean:
            return m
    return None

def get_simplest_magic_for_school(magias, school_name, exclude_names):
    candidates = []
    for m in magias:
        if m['escola'] and school_name in m['escola'] and m['nome'] not in exclude_names:
            req = m.get('preRequisitos', '') or ''
            if not req or len(req) < 15:
                candidates.append(m)
    
    if not candidates:
        return None
    candidates.sort(key=lambda x: len(x.get('preRequisitos', '') or ''))
    return candidates[0]

def resolve_path(magias, target_name, visited=None):
    if visited is None:
        visited = set()
    
    if target_name in visited:
        return []
    
    magia = get_magia_by_nome(magias, target_name)
    if not magia:
        return [f"ERRO: Magia '{target_name}' não encontrada."]
    
    visited.add(target_name)
    path = []
    
    req_str = magia.get('preRequisitos', '') or ''
    
    # Resolver escolas primeiro (mágica ou magica)
    school_match = re.search(r'(\d+) m[áa]gica em (\d+) escolas diferentes', req_str, re.IGNORECASE)
    if school_match:
        count = int(school_match.group(2))
        path.append(f"\n[Nexus] Meta: {count} Escolas Diferentes")
        found_schools = set()
        for m_check in magias:
            if m_check['escola']:
                for s in m_check['escola']:
                    if s not in found_schools and len(found_schools) < count:
                        basic = get_simplest_magic_for_school(magias, s, [target_name])
                        if basic:
                            path.append(f"  - {s}: {basic['nome']} (Pág. {basic['pagina']})")
                            found_schools.add(s)
    
    # Resolver dependências nominais
    parts = re.split(r' ou | e |,', req_str)
    for p in parts:
        p = p.strip()
        if not p or 'IQ' in p or 'escolas' in p or 'Aptidão' in p:
            continue
        
        sub_magia = get_magia_by_nome(magias, p)
        if sub_magia:
            sub_path = resolve_path(magias, sub_magia['nome'], visited)
            path.extend(sub_path)
            path.append(f"[Trilha] {sub_magia['nome']} -> {magia['nome']}")

    return path

if __name__ == "__main__":
    magias_db = load_magias()
    print("--- SIMULAÇÃO MOTOR NEXUS ARCANO (PYTHON) ---")
    target = "Teleporte"
    result = resolve_path(magias_db, target)
    
    unique_steps = []
    for s in result:
        if s not in unique_steps: unique_steps.append(s)
        
    for step in unique_steps:
        print(step)
