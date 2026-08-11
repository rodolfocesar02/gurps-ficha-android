package com.gurps.ficha.domain.rules

import java.text.Normalizer

/**
 * Conserta o texto que vem torto dos catálogos.
 *
 * Os JSONs de armadura foram gerados a partir de planilhas de OCR, e alguns
 * acentos não sobreviveram à viagem: `cr?nio`, `bra<?>os`, `m?os`.
 *
 * Estava escrito à mão dentro de `ui/TabEquipamentos.kt`, onde nenhum teste
 * alcançava. Mudou de lugar no Lote EQP-2 — o comportamento é o mesmo.
 *
 * ⚠️ **O destino é sem acento, de propósito.** Quem compara esses nomes é o
 * [CoberturaDaArmadura], que casa a armadura com o local do ferimento. Se aqui
 * saísse `crânio`, todo o resto do app teria de acertar o acento para o
 * casamento funcionar.
 *
 * ## 🔴 O estrago tem duas formas, e só uma tinha conserto
 *
 * 1. **A letra virou uma marca** — `cr?nio`, `bra<?>os`. Sobrou um sinal no
 *    lugar dela, e dá para procurar por ele.
 * 2. **A letra sumiu** — `crnio`, `pescoo`. Não sobrou nada.
 *
 * Só a forma (1) era tratada. Por isso `crnio` e `pescoo` continuavam aparecendo
 * na lista de armaduras (foto do usuário, 11/08): a regra procurava uma marca
 * que naquele texto nunca esteve.
 *
 * ⚠️ **Palavra inteira, sempre** (Lote EQP-3). Trocar `crnio` como pedaço de
 * texto é inofensivo, mas a regra é ancorada com `\b` de propósito: a lista de
 * palavras saiu de uma varredura do catálogo de verdade — `crnio` e `pescoo` são
 * as **únicas** formas em que a letra sumiu —, e sem a âncora a próxima entrada
 * da lista poderia acertar o meio de uma palavra legítima. `CatalogoSemTextoQuebradoTest`
 * varre os assets e reprova se aparecer uma terceira.
 */
object TextoDoCatalogo {

    /** Onde a letra sumiu: só `\b`palavra inteira`\b`. */
    private val LETRA_SUMIDA = listOf(
        Regex("""\bcrnio\b""", RegexOption.IGNORE_CASE) to "cranio",
        Regex("""\bpescoo\b""", RegexOption.IGNORE_CASE) to "pescoco"
    )

    private val REGRAS = listOf(
        "cr?nio" to "cranio",
        "cr�nio" to "cranio",
        "crânio" to "cranio",
        "pesco?o" to "pescoco",
        "pesco�o" to "pescoco",
        "pescoço" to "pescoco",
        "bra?os" to "bracos",
        "bra�os" to "bracos",
        "braços" to "bracos",
        "m?os" to "maos",
        "m�os" to "maos",
        "mãos" to "maos",
        "p?s" to "pes",
        "p�s" to "pes",
        "pés" to "pes"
    )

    fun corrigir(texto: String): String {
        if (texto.isBlank()) return texto
        var reparado = texto
        REGRAS.forEach { (errado, certo) -> reparado = reparado.replace(errado, certo, ignoreCase = true) }
        LETRA_SUMIDA.forEach { (regex, certo) -> reparado = regex.replace(reparado, certo) }
        return Normalizer.normalize(reparado, Normalizer.Form.NFC)
    }
}
