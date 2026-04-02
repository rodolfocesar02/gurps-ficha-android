import re

src_file = r'motor modo alvo/src/NexusArcanoEngine.kt'
dest_file = r'motor modo alvo/src/NexusArcanoStrings.kt'

with open(src_file, 'r', encoding='utf-8') as f:
    orig = f.read()

match = re.search(r'    internal fun sha256Hex\(.*?\}\n\}', orig, re.DOTALL)
if not match:
    print("Could not find sha256 block")
    exit(1)

content = match.group(0)

# Extract new strings without modifying the original matcher variable
extracted_content = content
extracted_content = extracted_content.replace('    internal fun sha256Hex(', 'internal fun NexusArcanoEngine.sha256Hex(')
extracted_content = extracted_content.replace('    internal fun preRequisitoSemConteudo(', 'internal fun NexusArcanoEngine.preRequisitoSemConteudo(')
extracted_content = extracted_content.replace('    internal fun nomeMagia(', 'internal fun NexusArcanoEngine.nomeMagia(')
extracted_content = extracted_content.replace('    internal fun nomeMagiaNorm(', 'internal fun NexusArcanoEngine.nomeMagiaNorm(')
extracted_content = extracted_content.replace('    internal fun preRaw(', 'internal fun NexusArcanoEngine.preRaw(')
extracted_content = extracted_content.replace('    internal fun preNorm(', 'internal fun NexusArcanoEngine.preNorm(')
extracted_content = extracted_content.replace('    internal fun escolasNorm(', 'internal fun NexusArcanoEngine.escolasNorm(')
extracted_content = extracted_content.replace('    internal fun escolaPrincipalNorm(', 'internal fun NexusArcanoEngine.escolaPrincipalNorm(')
extracted_content = extracted_content.replace('    internal fun escolaBloqueadaPorPolitica(', 'internal fun NexusArcanoEngine.escolaBloqueadaPorPolitica(')
extracted_content = extracted_content.replace('    internal fun variantesSingularPlural(', 'internal fun NexusArcanoEngine.variantesSingularPlural(')
extracted_content = extracted_content.replace('    internal fun singularizarTokenPt(', 'internal fun NexusArcanoEngine.singularizarTokenPt(')
extracted_content = extracted_content.replace('    internal fun pluralizarTokenPt(', 'internal fun NexusArcanoEngine.pluralizarTokenPt(')
extracted_content = extracted_content.replace('    internal fun normalize(', 'internal fun NexusArcanoEngine.normalize(')
extracted_content = extracted_content.replace('    internal fun pareceReferenciaDeEscola(', 'internal fun NexusArcanoEngine.pareceReferenciaDeEscola(')
extracted_content = extracted_content.replace('    internal fun valorAtributo(', 'internal fun NexusArcanoEngine.valorAtributo(')

content_to_write = extracted_content[:-2] # drop \n}

lines = content_to_write.split('\n')
dedented = []
for line in lines:
    if line.startswith('    '):
        dedented.append(line[4:])
    else:
        dedented.append(line)

final_content = "package nexus.arcano\n\nimport java.security.MessageDigest\nimport java.text.Normalizer\n\n" + '\n'.join(dedented).strip() + "\n"

with open(dest_file, 'w', encoding='utf-8') as f:
    f.write(final_content)

# We use the UNMODIFIED content string to replace from orig!!!
new_engine = orig.replace(content, "\n}\n")

with open(src_file, 'w', encoding='utf-8') as f:
    f.write(new_engine)

print("Lote 2.4 Strings Extraction Completed PROPERLY.")
