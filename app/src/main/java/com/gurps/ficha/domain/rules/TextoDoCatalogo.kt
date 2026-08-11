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
 * ⚠️ Isto trata só o estrago em que a letra **virou uma marca**. O caso em que
 * ela **sumiu** (`crnio`, `pescoo`) não tem marca para procurar, e por isso não
 * se resolve aqui — resolve-se na fonte, lendo o campo que o próprio catálogo
 * já publica normalizado (Lote EQP-3).
 */
object TextoDoCatalogo {

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
        return Normalizer.normalize(reparado, Normalizer.Form.NFC)
    }
}
