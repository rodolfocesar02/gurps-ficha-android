import os
import json

def restore_mojibake(file_path):
    print(f"Restoring mojibake in {file_path}...")
    try:
        # Step 1: Read the file as UTF-8 (it currently has double-encoded UTF-8)
        with open(file_path, 'r', encoding='utf-8') as f:
            text = f.read()

        # Step 2: Attempt to reverse the "Read UTF-8 as Latin-1 and write as UTF-8" action.
        # Original (UTF-8) -> (Latin-1 read) -> (UTF-8 write)
        # Current (UTF-8) -> (UTF-8 bytes) -> (Latin-1 decode)
        
        # If the string has 'Ã¡', it means it was 'C3 A1' (á) read as latin-1
        # and then saved as utf-8 (C3 83 C2 A1).
        
        # Reverse: Current string -> Encode to Latin-1 -> Decode as UTF-8
        try:
            # We must encode to latin-1 to get the 'raw bytes' that were mistakenly interpreted
            raw_bytes = text.encode('latin-1')
            fixed_text = raw_bytes.decode('utf-8')
            
            # Basic validation: if we find more common Portuguese characters, it's probably successful
            if "á" in fixed_text or "ã" in fixed_text or "é" in fixed_text:
                print(f"Success: Moijbake reversed in {file_path}")
                text = fixed_text
            else:
                print(f"Warning: Reversal tried but 'á' not found in result for {file_path}. Skipping.")
                return
        except (UnicodeEncodeError, UnicodeDecodeError) as e:
            print(f"Skipping {file_path}: Not double-encoded or different corruption ({e})")
            return

        # Parse JSON to ensure it's still valid
        data = json.loads(text)
        
        # Save as clean UTF-8
        with open(file_path, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
            
        print(f"Final save completed for {file_path}")
    except Exception as e:
        print(f"Error restoring {file_path}: {e}")

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
        restore_mojibake(full_path)
