import os
import re

assets_dir = r'c:\Users\Rodolfo\Desktop\ficha gurps\ficha-gurps\gurps_app\gurps-ficha-android\app\src\main\assets'

replacements = {
    # Mojibakes específicos com caracteres de substituição
    r'Jud\ufffd': 'Judô',
    r'Carat\ufffd': 'Caratê',
    r'CARAT\ufffd': 'CARATÊ',
    r'pr\ufffd-requisito': 'pré-requisito',
    r'Pr\ufffd-requisito': 'Pré-requisito',
    r'pr\ufffd-definido': 'pré-definido',
    r'Pr\ufffd-definido': 'Pré-definido',
    
    # Casos de perda total de caracteres acentuados (comum em alguns OCRs)
    r'Brao': 'Braço',
    r'Pescoo': 'Pescoço',
    r'Crnio': 'Crânio',
    r'rgos Vitais': 'Órgãos Vitais',
    r'rgos': 'Órgãos',
    r'temporrios': 'temporários',
    r'desagradveis': 'desagradáveis',
    r'distncia': 'distância',
    r'concentrao': 'concentração',
    r'percia': 'perícia',
    r'Percia': 'Perícia',
    r'pr-requisito': 'pré-requisito',
    r'pr-definido': 'pré-definido',
    
    # Padronização de nomes de perícia para evitar conflitos de auditoria
    r'\bcarate\b': 'caratê', # Minúsculo para normalização
    r'\bjudo\b': 'judô',
}

def fix_file(filepath):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        new_content = content
        for pattern, replacement in replacements.items():
            # Usar re.IGNORECASE para carate/judo se necessário
            if pattern in [r'\bcarate\b', r'\bjudo\b']:
                new_content = re.sub(pattern, replacement, new_content, flags=re.IGNORECASE)
            else:
                new_content = re.sub(pattern, replacement, new_content)
        
        if new_content != content:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(new_content)
            print(f"Fixed: {os.path.basename(filepath)}")
            return True
    except Exception as e:
        print(f"Error in {filepath}: {e}")
    return False

for filename in os.listdir(assets_dir):
    if filename.endswith('.json') or filename.endswith('.jsonl'):
        fix_file(os.path.join(assets_dir, filename))
