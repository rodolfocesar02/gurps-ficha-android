import json
import re

input_file = r"c:\Users\Rodolfo\Desktop\ficha gurps\ficha-gurps\gurps_app\gurps-ficha-android\app\src\main\assets\chunks.jsonl"
output_file = r"c:\Users\Rodolfo\Desktop\ficha gurps\ficha-gurps\gurps_app\gurps-ficha-android\scratch\manual_gurps_limpo.txt"

def fix_text(text):
    # Dicion\u00e1rio de corre\u00e7\u00e3o para os mojibakes comuns no arquivo
    replacements = {
        '\u01fd': '\u00e1', '\u01d4': '\u00e3', '\u011b': '\u00ea', '\u01d2': '\u00f3',
        '\u01d8': '\u00fa', '\u01c7': '\u00e9', '\u0190': '\u00ed', '\u013e': '\u00ed',
        '\u01ce': '\u00f4', '\u01b5': '\u00e7', '\ufffd': '', '\u0123': ' ',
        'Ǹ': 'é', 'ǜ': 'ão', 'ǭ': 'á', 'Ǧ': 'ê', '': 'ó', 'Ǒ': 'í', 'ǜ': 'ão'
    }
    for k, v in replacements.items():
        text = text.replace(k, v)
    
    # Limpeza de caracteres residuais e formata\u00e7\u00e3o
    text = re.sub(r'[^\x00-\x7F\u00C0-\u00FF\n\r\t ]', '', text)
    return text

def rebuild_manual():
    print("Iniciando reconstru\u00e7\u00e3o do manual...")
    full_text = []
    
    with open(input_file, 'r', encoding='utf-8') as f:
        for line in f:
            try:
                data = json.loads(line)
                clean_chunk = fix_text(data['text'])
                full_text.append(clean_chunk)
            except:
                continue
                
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write("\n\n".join(full_text))
        
    print(f"Manual limpo e salvo em: {output_file}")

if __name__ == "__main__":
    rebuild_manual()
