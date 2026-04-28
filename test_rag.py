# -*- coding: utf-8 -*-
import json

termos_base = ['cavar', 'buraco']
termos_expandidos = ['escavacao', 'trabalho', 'bracal', 'escavar', 'valeta', 'fender', 'cavar', 'buraco']

chunks = []
with open('app/src/main/assets/chunks.jsonl', encoding='utf-8') as f:
    for line in f:
        try:
            chunks.append(json.loads(line))
        except:
            pass

def extrair_radical(term):
    term = term.lower()
    if len(term) >= 5 and term.endswith('r'): return term[:-1]
    if len(term) >= 5 and term.endswith('s'): return term[:-1]
    if len(term) >= 6 and term.endswith('cao'): return term[:-3]
    return term

pontuados = []
for chunk in chunks:
    texto = chunk.get('text', '').lower()
    score = 0
    base_count = 0
    
    for t in termos_base:
        rad = extrair_radical(t)
        if rad in texto:
            score += 100
            base_count += 1
            
    if base_count > 1:
        score += (base_count * 300)
        
    for t in (set(termos_expandidos) - set(termos_base)):
        if t in texto: score += 10
        
    pontuados.append((score, chunk))

pontuados.sort(key=lambda x: x[0], reverse=True)

print('--- TOP CHUNKS ---')
for score, c in pontuados[:5]:
    text_preview = c.get('text', '').replace('\n', ' ')[:200]
    print(f"Score: {score} | Pag: {c.get('page_number')} | Source: {c.get('source_title')} | Text: {text_preview}")