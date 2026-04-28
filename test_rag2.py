# -*- coding: utf-8 -*-
import json

chunks = []
with open('app/src/main/assets/chunks.jsonl', encoding='utf-8') as f:
    for line in f:
        try:
            chunks.append(json.loads(line))
        except:
            pass

for c in chunks:
    if c.get('page_number') == 355 and 'cava' in c.get('text', '').lower():
        print(f"Pag: {c.get('page_number')} | Source: {c.get('source_title')} | Text: {c.get('text', '')[:500]}")