
import os

filepath = r"c:\Users\Rodolfo\Desktop\ficha gurps\ficha-gurps\gurps_app\gurps-ficha-android\app\src\main\java\com\gurps\ficha\domain\MestreIAUseCase.kt"

# Tabela de tradução de Mojibake (UTF-8 lido como ISO-8859-1)
mojibake_map = {
    "Ã¡": "á", "Ã ": "à", "Ã¢": "â", "Ã£": "ã",
    "Ã©": "é", "Ã¨": "è", "Ãª": "ê",
    "Ã­": "í", "Ã¯": "ï",
    "Ã³": "ó", "Ã´": "ô", "Ãµ": "õ",
    "Ãº": "ú", "Ã¼": "ü",
    "Ã§": "ç",
    "Ã": "Á", # Caso especial para maiúsculas se houver
    "Â": ""    # Caractere fantasma comum
}

with open(filepath, 'r', encoding='latin-1') as f:
    content = f.read()

for bad, good in mojibake_map.items():
    content = content.replace(bad, good)

# Salva como UTF-8 com BOM para blindar o Windows
with open(filepath, 'w', encoding='utf-8-sig') as f:
    f.write(content)

print("Global Cleanup Success!")
