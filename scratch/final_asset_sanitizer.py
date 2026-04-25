import os
import re

assets_dir = r'c:\Users\Rodolfo\Desktop\ficha gurps\ficha-gurps\gurps_app\gurps-ficha-android\app\src\main\assets'

# Mapeamento de padrões comuns que geram o caractere corrompido
replacements = {
    r'BAST\ufffdO': 'BASTÃO',
    r'LAN\ufffdA': 'LANÇA',
    r'MA\ufffdA': 'MAÇA',
    r'L\ufffdmina': 'Lâmina',
    r'ARP\ufffdO': 'ARPÃO',
    r'M\ufffdOS': 'MÃOS',
    r'DUAS M\ufffdOS': 'DUAS MÃOS',
    r'S\ufffdBIA': 'SÁBIA',
    r'CORA\ufffd\ufffdO': 'CORAÇÃO',
    r'\ufffd': ' '  # Qualquer outro  vira espaço
}

def sanitize_assets():
    for filename in os.listdir(assets_dir):
        if not (filename.endswith('.json') or filename.endswith('.jsonl')):
            continue
            
        filepath = os.path.join(assets_dir, filename)
        try:
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            new_content = content
            for pattern, replacement in replacements.items():
                new_content = re.sub(pattern, replacement, new_content)
            
            if new_content != content:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                print(f"Sanitized: {filename}")
        except Exception as e:
            print(f"Error processing {filename}: {e}")

if __name__ == "__main__":
    sanitize_assets()
