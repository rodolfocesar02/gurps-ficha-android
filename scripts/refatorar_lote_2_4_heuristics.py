import re

src_file = r'motor modo alvo/src/NexusArcanoEngine.kt'
dest_file = r'motor modo alvo/src/NexusArcanoHeuristics.kt'

with open(src_file, 'r', encoding='utf-8') as f:
    orig = f.read()

methods_to_extract = [
    r'    internal fun sugerirProximasAcoes\(.*?\n    \}',
    r'    internal fun sugerirParaRegraDeEscolas\(.*?\n    \}',
    r'    internal fun avaliarCandidatasParaRegraDeEscolas\(.*?\n    \}',
    r'    internal fun escolherBranchRelevante\(.*?\n    \}',
    r'    internal fun bloqueioNumericoParaMagia\(.*?\n    \}',
    r'    internal fun primeiroBloqueioNumerico\(.*?\n    \}',
    r'    internal fun codigoBloqueio\(.*?\n    \}',
]

extracted_blocks = []
new_engine = orig

for pattern in methods_to_extract:
    match = re.search(pattern, new_engine, re.DOTALL)
    if match:
        block = match.group(0)
        extracted_blocks.append(block)
        new_engine = new_engine.replace(block, "")
    else:
        print(f"Could not find match for pattern: {pattern[:50]}")

if not extracted_blocks:
    print("Nothing extracted")
    exit(1)

final_blocks = []
for block in extracted_blocks:
    sig_match = re.search(r'    internal fun ([a-zA-Z0-9_]+)\(', block)
    if sig_match:
        func_name = sig_match.group(1)
        new_sig = f"internal fun NexusArcanoEngine.{func_name}("
        block = block.replace(f"    internal fun {func_name}(", new_sig)
    
    lines = block.split('\n')
    dedented = []
    for line in lines:
        if line.startswith('    '):
            dedented.append(line[4:])
        else:
            dedented.append(line)
    
    final_blocks.append('\n'.join(dedented).strip())

final_content = "package nexus.arcano\n\nimport nexus.arcano.NexusArcanoEngine.AvaliacaoCandidata\nimport nexus.arcano.NexusArcanoEngine.RequisitoBranch\n\n" + '\n\n'.join(final_blocks) + "\n"

with open(dest_file, 'w', encoding='utf-8') as f:
    f.write(final_content)

with open(src_file, 'w', encoding='utf-8') as f:
    f.write(new_engine)

print("Lote 2.4 Heuristics Extraction Completed.")
