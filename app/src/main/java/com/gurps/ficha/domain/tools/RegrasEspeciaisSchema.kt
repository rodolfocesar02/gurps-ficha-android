package com.gurps.ficha.domain.tools

/**
 * Schema das REGRAS ESPECIAIS de vantagens/desvantagens.
 *
 * Problema que resolve: traços como Aliado, Inimigo, Vício, Garras,
 * Resistente NÃO têm custo simples — o custo sai de campos de
 * `metadados`. A busca de catálogo antes só dizia "Aliado, -5 pts,
 * variavel"; o modelo (IA) ficava cego aos campos e CHUTAVA (vimos
 * Garras no Lote 175). Aqui está o schema EXTRAÍDO do código que
 * calcula (CharacterRules.calcularCusto*), não inventado — a tool
 * forjador_buscar_catalogo injeta isto quando o traço tem specialRule,
 * então o modelo sabe exatamente que metadados preencher.
 *
 * Fonte: CharacterRules.kt (calcularCustoInimigo/Dependencia/Reputacao/
 * Dever/DorCronica/Fraqueza/Vulnerabilidade/Manutencao/Vicio/Aliado/
 * Contato) + GarrasRule/regras modulares + VantagemDialogs (mapeamento
 * dos metadados persistidos).
 */
object RegrasEspeciaisSchema {

    /** Texto-schema por specialRule (ou id de regra modular). */
    private val schemas: Map<String, String> = mapOf(
        "aliados" to "metadados: basePoder(int: % dos seus pontos — 25/50/75/100→ratio), multFrequencia(0.5=6-,1=9-,2=12-,3=15-), grupo(bool). custo = calcularCustoAliado.",
        "contatos" to "metadados: nivelHabilidade(int), multFrequencia(0.5/1/2/3), confiabilidade. custo = calcularCustoContato.",
        "inimigos" to "metadados: basePoder(int neg: -5/-10/-20/-30/-40), multIntencao(0.25 vigia /0.5 rival /1 caçador), multFrequencia(0.5=6-,1=9-,2=12-,3=15-,4=const).",
        "dependentes" to "metadados: basePoder, multIntencao, multFrequencia (mesma fórmula de inimigos).",
        "dependencia" to "metadados: baseRaridade(int neg), multFrequencia(0.5/1/2/3), ilegal(bool).",
        "reputacao" to "metadados: baseReputacao(int neg), multGrupo(float), multReconhecimento(float).",
        "dever" to "metadados: baseDever(int neg), perigoso(bool), involuntario(bool), inofensivo(bool).",
        "dor_cronica" to "metadados: baseIntensidade(int neg), multFrequencia(float).",
        "fraqueza" to "metadados: baseRaridade(int neg), multFrequencia(float).",
        "vulnerabilidade" to "metadados: baseRaridade(int neg), multDano(float, ex 2.0).",
        "manutencao" to "metadados: baseManutencao(int neg), multIntervalo(float).",
        "vicio" to "metadados: baseVicio(int neg), modEfeito(int), modLegalidade(int).",
        "maldicao_divina" to "metadados: custoCustom(int) — custo informado direto.",
        // Regras MODULARES (TraitRuleRegistry) — custo derivado de metadados:
        "garras" to "metadados: tipoGarras(cascos=3, garras_cegas=3, garras_afiadas=5, garras_pontudas=8, longas_garras_pontudas=11). NÃO use custoEscolhido.",
        "resistente" to "metadados: raridade(5/8/15/30), grau(1.0=imune, 0.5=+8, 0.3333=+3), atributo(HT/IQ). custo = raridade×grau (floor).",
        "ataque_inato" to "metadados: tipo de dano/dados — vantagem composta; consultar dialog de Ataque Inato."
    )

    /** Retorna o schema do traço (por specialRule ou id), ou null. */
    fun para(specialRule: String?, id: String): String? {
        if (!specialRule.isNullOrBlank()) schemas[specialRule]?.let { return it }
        return schemas[id]
    }
}
