
import os

filepath = r"c:\Users\Rodolfo\Desktop\ficha gurps\ficha-gurps\gurps_app\gurps-ficha-android\app\src\main\java\com\gurps\ficha\domain\MestreIAUseCase.kt"

with open(filepath, 'rb') as f:
    content = f.read()

target = b'val novoPrompt = '
replacement_text = """
                                 // --- LOTE 73: DETECTOR DE ALUCINAÇÃO (O Filtro de Verdade) ---
                                 val isHallucinating = detectarAlucinacao(resposta.text, extraContext)
                                 
                                 val novoPrompt = if (isHallucinating) {
                                     "[ERRO DE AUDITORIA] Sua resposta anterior inventou regras (ex: níveis de magia). RECOMECE usando APENAS:\\n$extraContext"
                                 } else {
                                     "[SISTEMA AUTOMÁTICO] Resultado da investigação ('$query'):\\n$extraContext\\n\\nAnalise e prossiga."
                                 }
"""
replacement = replacement_text.encode('utf-8')

if target in content:
    start_idx = content.find(target)
    end_idx = content.find(b'\\n', start_idx) # Nota: usei \\n para bater no binário
    if end_idx == -1: end_idx = content.find(b'\n', start_idx) + 1
    else: end_idx += 1

    new_content = content[:start_idx] + replacement + content[end_idx:]
    
    with open(filepath, 'wb') as f:
        f.write(new_content)
    print("Success!")
else:
    print("Target not found!")
