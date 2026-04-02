import re

with open('app/src/main/java/com/gurps/ficha/data/DataRepository.kt', 'r', encoding='utf-8') as f:
    orig = f.read()

# Base template
loaders_content = """package com.gurps.ficha.domain.loaders

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.model.*
import java.text.Normalizer
import com.gurps.ficha.data.DataRepository

private val gson = Gson()

class CatalogLoaders(private val context: Context) {
    val loadErrors = mutableMapOf<String, String>()

    fun clearLoadError(catalog: String) {
        synchronized(loadErrors) { loadErrors.remove(catalog) }
    }

    fun registerLoadError(catalog: String, throwable: Throwable) {
        synchronized(loadErrors) {
            loadErrors[catalog] = throwable.message ?: throwable::class.java.simpleName
        }
    }

    var vantagensArtesMarciaisIds: Set<String> = emptySet()
        private set

"""

# Extract Loaders Body
match_loaders = re.search(r'    private fun carregarVantagens\(\): List<VantagemDefinicao> \{.*?(?=\n    // === FILTROS)', orig, re.DOTALL)
if match_loaders:
    loaders_methods = match_loaders.group(0)
    # Remove "private" from load methods
    loaders_methods = re.sub(r'^    private fun', '    fun', loaders_methods, flags=re.MULTILINE)
    # Variable mappings
    loaders_methods = loaders_methods.replace('_vantagensArtesMarciaisIds', 'vantagensArtesMarciaisIds')
    # Use DataRepository singleton for domain functions left in DataRepository
    loaders_methods = loaders_methods.replace('periciasV2Rules[it.id]', 'DataRepository.getInstance(context).regraPericiaV2(it.id)')
    loaders_methods = loaders_methods.replace('aplicarRegraPericiaV2(', 'DataRepository.getInstance(context).aplicarRegraPericiaV2(')
    loaders_methods = loaders_methods.replace('preRequisitoCanonicoTexto(', 'DataRepository.getInstance(context).preRequisitoCanonicoTexto(')
    loaders_content += loaders_methods

loaders_content += """
}

// Extensions
"""

# Extract Extensions
match_exts = re.search(r'\nprivate data class VantagemV3.*', orig, re.DOTALL)
if match_exts:
    exts = match_exts.group(0).strip()
    exts = exts.replace('private fun String?.sanitized', 'fun String?.sanitized')
    exts = exts.replace('private fun String.fixMojibakeIfNeeded', 'fun String.fixMojibakeIfNeeded')
    loaders_content += '\n' + exts + '\n'

with open('app/src/main/java/com/gurps/ficha/domain/loaders/CatalogLoaders.kt', 'w', encoding='utf-8') as f:
    f.write(loaders_content)

# Refactor DataRepository.kt
new_repo = orig
# Remove the private clearLoadError/registerLoadError + replace load operations with delegation
new_repo = re.sub(r'    fun getCatalogLoadErrors\(\): Map<String, String> = synchronized\(loadErrors\) \{ loadErrors\.toMap\(\) \}\n\n    private fun clearLoadError.*?    // === FILTROS DE VANTAGENS E OUTROS', 
    '''    private val catalogLoaders = com.gurps.ficha.domain.loaders.CatalogLoaders(context)

    fun getCatalogLoadErrors(): Map<String, String> {
        return synchronized(catalogLoaders.loadErrors) { catalogLoaders.loadErrors.toMap() }
    }

    private fun carregarVantagens(): List<VantagemDefinicao> = catalogLoaders.carregarVantagens()
    private fun carregarDesvantagens(): List<DesvantagemDefinicao> = catalogLoaders.carregarDesvantagens()
    private fun carregarPericias(): List<PericiaDefinicao> = catalogLoaders.carregarPericias()
    private fun carregarPericiasV2Rules(): Map<String, PericiaV2RuleMapItem> = catalogLoaders.carregarPericiasV2Rules()
    private fun carregarPericiasSuplementares(): List<PericiaSuplementarItem> = catalogLoaders.carregarPericiasSuplementares()
    private fun carregarMagias(): List<MagiaDefinicao> = catalogLoaders.carregarMagias()
    private fun carregarTecnicasCatalogo(): List<TecnicaCatalogoItem> = catalogLoaders.carregarTecnicasCatalogo()
    private fun carregarArmasCatalogo(): List<ArmaCatalogoItem> = catalogLoaders.carregarArmasCatalogo()
    private fun carregarEscudosCatalogo(): List<EscudoCatalogoItem> = catalogLoaders.carregarEscudosCatalogo()
    private fun carregarArmadurasCatalogo(): List<ArmaduraCatalogoItem> = catalogLoaders.carregarArmadurasCatalogo()
    private fun carregarModificadoresGerais(): List<ModificadorDefinicao> = catalogLoaders.carregarModificadoresGerais()

    // === FILTROS DE VANTAGENS E OUTROS''', new_repo, flags=re.DOTALL)

# Delete loadErrors and make required methods public
new_repo = re.sub(r'    private val loadErrors = mutableMapOf<String, String>\(\)\n', '', new_repo)
# Instead of replacing EVERY _vantagensArtesMarciaisIds unconditionally, we target the one left (if any). Wait, DataRepository has `_vantagensArtesMarciaisIds.contains`. We replace it.
new_repo = new_repo.replace('_vantagensArtesMarciaisIds', 'catalogLoaders.vantagensArtesMarciaisIds')

# Make the methods public:
new_repo = new_repo.replace('private fun aplicarRegraPericiaV2(', 'fun aplicarRegraPericiaV2(')
new_repo = new_repo.replace('private fun preRequisitoCanonicoTexto(', 'fun preRequisitoCanonicoTexto(')

# Delete trailing structures from VantagemV3 onwards
new_repo = re.sub(r'\nprivate data class VantagemV3.*', '\n', new_repo, flags=re.DOTALL)

# Insert the necessary imports right after normalizer
new_repo = new_repo.replace('import com.gurps.ficha.domain.filters.CatalogFilters', 
    'import com.gurps.ficha.domain.filters.CatalogFilters\nimport com.gurps.ficha.domain.loaders.sanitized\nimport com.gurps.ficha.domain.loaders.fixMojibakeIfNeeded')

with open('app/src/main/java/com/gurps/ficha/data/DataRepository.kt', 'w', encoding='utf-8') as f:
    f.write(new_repo)

print("Refactor loaded.")
