import re

src_file = r'motor modo alvo/src/NexusArcanoEngine.kt'
dest_file = r'motor modo alvo/src/NexusArcanoParser.kt'

with open(src_file, 'r', encoding='utf-8') as f:
    orig = f.read()

# We need to extract the parser methods.
# They are scattered, but we can match them individually and remove them from orig.

methods_to_extract = [
    r'    internal fun regrasEscolasRelevantes\(.*?\n    \}',
    r'    internal fun regrasNumericasRelevantes\(.*?\n    \}',
    r'    internal fun coletarRegrasEscolas\(.*?\n    \}',
    r'    internal fun coletarRegrasNumericas\(.*?\n    \}',
    r'    internal fun snapshotAlvo\(.*?\n    \}',
    r'    internal fun chavesEsperadasOrdem\(.*?\n    \}',
    r'    internal fun regrasEscolasPorMagia\(.*?\n    \}',
    r'    internal fun regrasNumericasPorMagia\(.*?\n    \}',
    r'    internal fun parseNumeroToken\(.*?\n    \}'
]

extracted_blocks = []
new_engine = orig

for pattern in methods_to_extract:
    # Use DOTALL to match accross multiple lines
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

# Now, refactor them into extension functions
final_blocks = []
for block in extracted_blocks:
    # Replace the signature '    internal fun NAME(' with 'internal fun NexusArcanoEngine.NAME('
    sig_match = re.search(r'    internal fun ([a-zA-Z0-9_]+)\(', block)
    if sig_match:
        func_name = sig_match.group(1)
        new_sig = f"internal fun NexusArcanoEngine.{func_name}("
        block = block.replace(f"    internal fun {func_name}(", new_sig)
    
    # Dedent by 4 spaces
    lines = block.split('\n')
    dedented = []
    for line in lines:
        if line.startswith('    '):
            dedented.append(line[4:])
        else:
            dedented.append(line)
    
    final_blocks.append('\n'.join(dedented).strip())

final_content = "package nexus.arcano\n\nimport java.util.concurrent.ConcurrentHashMap\n\n" + '\n\n'.join(final_blocks) + "\n"

with open(dest_file, 'w', encoding='utf-8') as f:
    f.write(final_content)

with open(src_file, 'w', encoding='utf-8') as f:
    f.write(new_engine)

print("Lote 2.4 Parser Extraction Completed.")
