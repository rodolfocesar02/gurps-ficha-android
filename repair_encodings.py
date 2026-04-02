import os
import json

def force_latin1_to_utf8(file_path):
    print(f"Forcing Latin-1 to UTF-8 for {file_path}...")
    try:
        # Read as Latin-1 which we confirmed looks correct
        with open(file_path, 'r', encoding='latin-1') as f:
            text = f.read()

        # Parse JSON
        data = json.loads(text)
        
        # Save as UTF-8
        with open(file_path, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
            
        print(f"Successfully converted {file_path}")
    except Exception as e:
        print(f"Error converting {file_path}: {e}")

assets_dir = r"c:\Users\Rodolfo\Desktop\ficha gurps\ficha-gurps\gurps_app\gurps-ficha-android\app\src\main\assets"
files_to_fix = [
    "vantagens.v3.json",
    "vantagens.v2.json",
    "vantagens.json",
    "desvantagens.v2.json",
    "desvantagens.json",
    "pericias.json",
    "tecnicas.v1.json",
    "magias.json",
    "magias2versao.json",
    "pericias_artes_marciais.v1.json",
    "vantagens_artes_marciais.v1.json"
]

for filename in files_to_fix:
    full_path = os.path.join(assets_dir, filename)
    if os.path.exists(full_path):
        force_latin1_to_utf8(full_path)
