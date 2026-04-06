import os

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
        'â€œ': '“', 'â€ ': '”', 'â€˜': '‘', 'â€™': '’',
        'â€“': '–', 'â€”': '—',
        'Âº': 'º', 'Âª': 'ª', 'Â°': '°',
        'â€¦': '...',
        'Â': '' # Common artifact of double encoding
    }
    for old, new in replacements.items():
        content = content.replace(old, new)
    return content

# Agora o script varre tudo automaticamente
base_path = os.getcwd()
folders_to_scan = [
    os.path.join(base_path, 'app', 'src', 'main', 'assets'),
    os.path.join(base_path, 'app', 'src', 'main', 'java')
]

print("Iniciando Limpeza Global de Mojibake...")

for folder in folders_to_scan:
    if not os.path.exists(folder):
        continue
    
    for root, dirs, files in os.walk(folder):
        for file in files:
            if file.endswith(('.json', '.kt')):
                abs_path = os.path.join(root, file)
                try:
                    with open(abs_path, 'r', encoding='utf-8') as f:
                        content = f.read()
                    
                    fixed_content = fix_mojibake(content)
                    
                    if content != fixed_content:
                        with open(abs_path, 'w', encoding='utf-8') as f:
                            f.write(fixed_content)
                        print(f" Corrigido: {os.path.relpath(abs_path, base_path)}")
                except Exception as e:
                    print(f" Erro em {file}: {e}")

print("\nLimpeza concluida!")
