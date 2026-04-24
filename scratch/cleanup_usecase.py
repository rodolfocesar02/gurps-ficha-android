
import os

filepath = r"c:\Users\Rodolfo\Desktop\ficha gurps\ficha-gurps\gurps_app\gurps-ficha-android\app\src\main\java\com\gurps\ficha\domain\MestreIAUseCase.kt"

with open(filepath, 'rb') as f:
    lines = f.readlines()

new_lines = []
skip_mode = False

for line in lines:
    # Corrige Mojibake nas linhas conhecidas
    if b'Investiga' in line and b'Stage' in line:
        line = b'                                android.util.Log.i("MestreIA", "Investigacao Stage $iteracoes: $query")\n'
    if b'BUSCA H' in line and b'BRIDA' in line:
        line = b'                                 // --- BUSCA HIBRIDA (Lote 71) ---\n'
    if b'hist' in line and b'rico' in line and b'itera' in line:
        line = b'                                // Adicionar ao historico do loop\n'
    if b'ATEN' in line and b'O: Esta' in line:
        line = b'                                    prompt = if (iteracoes >= maxIteracoes) "$novoPrompt\\n\\nREPOSTA FINAL." else novoPrompt,\n'

    # Remove a linha orfã 152
    if b'n$extraContext\\n\\nPor favor, analise e continue' in line:
        continue
        
    new_lines.append(line)

with open(filepath, 'wb') as f:
    f.writelines(new_lines)

print("Cleanup Success!")
