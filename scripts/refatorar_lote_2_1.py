import re

src_file = r'motor modo alvo/src/NexusArcanoEngine.kt'
dest_file = r'motor modo alvo/src/ArcanoModels.kt'

with open(src_file, 'r', encoding='utf-8') as f:
    orig = f.read()

# Models are located from 'data class ArcanoEstadoPersonagem' until right before 'class NexusArcanoEngine'
match = re.search(r'(data class ArcanoEstadoPersonagem.*?\n)(?=class NexusArcanoEngine\()', orig, re.DOTALL)
if not match:
    print("Could not find models block!")
    exit(1)

models_content = match.group(1)

# Write to ArcanoModels.kt
new_models = f"""package nexus.arcano

{models_content.strip()}
"""

with open(dest_file, 'w', encoding='utf-8') as f:
    f.write(new_models)

# Remove the models from NexusArcanoEngine.kt
new_engine = orig.replace(models_content, '\n')

with open(src_file, 'w', encoding='utf-8') as f:
    f.write(new_engine)

print("Lote 2.1 Refactor completed.")
