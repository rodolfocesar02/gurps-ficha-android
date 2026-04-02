import re

src_file = r'motor modo alvo/src/NexusArcanoEngine.kt'
dest_file = r'motor modo alvo/src/NexusArcanoPathfinder.kt'

with open(src_file, 'r', encoding='utf-8') as f:
    orig = f.read()

# Match the logic exactly
match = re.search(r'    fun planejarCaminhoMinimo\(.*?    \}\n(?=\n    fun calcularEstadoAlvoIncremental\()', orig, re.DOTALL)
if not match:
    print("Could not find planejarCaminhoMinimo!")
    exit(1)

pathfinder_content = match.group(0)

# Replace 'fun planejarCaminhoMinimo(' with 'fun NexusArcanoEngine.planejarCaminhoMinimo('
pathfinder_content = pathfinder_content.replace('    fun planejarCaminhoMinimo(', 'fun NexusArcanoEngine.planejarCaminhoMinimo(')

# Dedent
lines = pathfinder_content.split('\n')
dedented = []
for line in lines:
    if line.startswith('    '):
        dedented.append(line[4:])
    else:
        dedented.append(line)

new_content = "package nexus.arcano\n\n" + '\n'.join(dedented).strip() + "\n"

with open(dest_file, 'w', encoding='utf-8') as f:
    f.write(new_content)

# Remove from engine and adjust visibility
new_engine = orig.replace(match.group(0), "")
new_engine = new_engine.replace("    private ", "    internal ")
new_engine = new_engine.replace("(private val catalogo", "(internal val catalogo")
new_engine = new_engine.replace("(\n    private val catalogo", "(\n    internal val catalogo")

with open(src_file, 'w', encoding='utf-8') as f:
    f.write(new_engine)

print("Lote 2.2 Refactor completed.")
