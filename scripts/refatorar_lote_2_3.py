import re

src_file = r'motor modo alvo/src/NexusArcanoEngine.kt'
dest_file = r'motor modo alvo/src/NexusArcanoDiagnostics.kt'

with open(src_file, 'r', encoding='utf-8') as f:
    orig = f.read()

match_ranking = re.search(r'    fun diagnosticarRankingAlvo\(.*?    \}\n(?=\s*fun diagnosticarMetasAlvo\()', orig, re.DOTALL)
match_metas = re.search(r'    fun diagnosticarMetasAlvo\(.*?    \}\n(?=\s*fun checksumMetasAlvo\()', orig, re.DOTALL)
match_check = re.search(r'    fun checksumMetasAlvo\(.*?    \}\n(?=\s*fun calcularEstadoAlvoIncremental\()', orig, re.DOTALL)

if not match_ranking or not match_metas or not match_check:
    print("Could not find one of the diagnostic methods!")
    if not match_ranking: print("Missing Ranking")
    if not match_metas: print("Missing Metas")
    if not match_check: print("Missing Checksum")
    exit(1)

diag_content = match_ranking.group(0) + '\n' + match_metas.group(0) + '\n' + match_check.group(0)

diag_content = diag_content.replace('    fun diagnosticarRankingAlvo(', 'fun NexusArcanoEngine.diagnosticarRankingAlvo(')
diag_content = diag_content.replace('    fun diagnosticarMetasAlvo(', 'fun NexusArcanoEngine.diagnosticarMetasAlvo(')
diag_content = diag_content.replace('    fun checksumMetasAlvo(', 'fun NexusArcanoEngine.checksumMetasAlvo(')

lines = diag_content.split('\n')
dedented = []
for line in lines:
    if line.startswith('    '):
        dedented.append(line[4:])
    else:
        dedented.append(line)

new_content = "package nexus.arcano\n\nimport java.security.MessageDigest\n\n" + '\n'.join(dedented).strip() + "\n"

with open(dest_file, 'w', encoding='utf-8') as f:
    f.write(new_content)

new_engine = orig.replace(match_ranking.group(0), "")
new_engine = new_engine.replace(match_metas.group(0), "")
new_engine = new_engine.replace(match_check.group(0), "")

# Adjust visibility of sha256Hex (which checksumMetasAlvo uses)
new_engine = new_engine.replace('    private fun sha256Hex(', '    internal fun sha256Hex(')

with open(src_file, 'w', encoding='utf-8') as f:
    f.write(new_engine)

print("Lote 2.3 Refactor completed.")
