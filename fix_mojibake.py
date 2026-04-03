import os

def fix_mojibake(content):
    # Comprehensive replacements for common UTF-8 Mojibake in Brazil/Portuguese
    replacements = {
        'Ã¡': 'á', 'Ã¢': 'â', 'Ã£': 'ã', 'Ã ': 'à',
        'Ã©': 'é', 'Ãª': 'ê',
        'Ã­': 'í',
        'Ã³': 'ó', 'Ã´': 'ô', 'Ãµ': 'õ',
        'Ãº': 'ú',
        'Ã§': 'ç',
        'Ã ': 'Á', 'Ã‚': 'Â', 'Ãƒ': 'Ã', 'Ã€': 'À',
        'Ã‰': 'É', 'ÃŠ': 'Ê',
        'Ã ': 'Í',
        'Ã“': 'Ó', 'Ã”': 'Ô', 'Ã•': 'Õ',
        'Ãš': 'Ú',
        'Ã‡': 'Ç',
        'â€œ': '“', 'â€': '”', 'â€˜': '‘', 'â€™': '’',
        'â€“': '–', 'â€”': '—',
        'Âº': 'º', 'Âª': 'ª', 'Â°': '°',
        'â€¦': '...',
        'Â': '' # Common artifact of double encoding
    }
    for old, new in replacements.items():
        content = content.replace(old, new)
    return content

files_to_fix = [
    r'app/src/main/java/com/gurps/ficha/ui/features/rolagem/RolagemComponents.kt',
    r'app/src/main/java/com/gurps/ficha/ui/features/rolagem/RolagemDialogs.kt',
    r'app/src/main/java/com/gurps/ficha/ui/features/rolagem/RolagemModels.kt',
    r'app/src/main/java/com/gurps/ficha/ui/TabRolagem.kt',
    r'app/src/main/java/com/gurps/ficha/ui/features/traits/TraitDialogsV2.kt',
    r'app/src/main/java/com/gurps/ficha/ui/features/magic/MagicDialogs.kt',
    r'app/src/main/java/com/gurps/ficha/ui/TabEquipamentos.kt',
    r'app/src/main/java/com/gurps/ficha/ui/DialogsAssistente.kt',
    r'app/src/main/java/com/gurps/ficha/ui/TabGeral.kt'
]

base_path = r'C:\Users\Rodolfo\Desktop\ficha gurps\ficha-gurps\gurps_app\gurps-ficha-android'

for rel_path in files_to_fix:
    abs_path = os.path.join(base_path, rel_path)
    if os.path.exists(abs_path):
        print(f"Fixing {rel_path}...")
        try:
            with open(abs_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            fixed_content = fix_mojibake(content)
            
            if content != fixed_content:
                with open(abs_path, 'w', encoding='utf-8') as f:
                    f.write(fixed_content)
                print(f"  Fixed!")
            else:
                print(f"  No changes needed.")
        except Exception as e:
            print(f"  Error reading file: {e}")
    else:
        print(f"File not found: {rel_path}")
