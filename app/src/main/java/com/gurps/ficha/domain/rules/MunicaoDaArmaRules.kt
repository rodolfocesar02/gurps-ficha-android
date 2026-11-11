package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.AcessorioDaArma
import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.ItemDoCatalogo
import com.gurps.ficha.model.MunicaoDaArma
import com.gurps.ficha.model.TipoEquipamento

/**
 * **A munição e os acessórios dentro da arma** — IC-3B do `mesa-virtual/planos/itens/PLANO.md` (as 4 decisões do Rodolfo, 11/nov):
 *
 * 1. os acessórios (mira, luneta, silenciador) vão pelo mesmo caminho da munição: sub-itens da arma;
 * 2. a ficha antiga migra sozinha: o item "Munição" (versão 216) entra na arma que ele servia e sai da lista;
 * 3. a munição pesa na carga, calculada: o livro dá o peso da carga cheia depois da barra (MB p.279), e o custo é $40 por quilo;
 * 4. o campo vale para toda arma de longe, mesmo a digitada à mão sem "Tiros".
 */
object MunicaoDaArmaRules {

    /** Custo da munição: $40 vezes o peso (MB p.279). */
    const val CUSTO_POR_KG = 40f

    /** A arma que atira (arco, besta, arma de fogo, funda): a que leva munição. Arma de arremesso não leva: ela é a munição. */
    fun usaMunicao(e: Equipamento): Boolean {
        if (e.tipo != TipoEquipamento.ARMA) return false
        val t = (e.armaTipoCombate ?: "").lowercase()
        if (t.contains("arremess")) return false
        return !e.armaTirosRaw.isNullOrBlank() || t.contains("dist") || t.contains("fogo") || t.contains("projet")
    }

    /**
     * Quantos tiros cabem no carregador: o número que ABRE o "Tiros" ("30+1(3)" → 30, "1(2)" → 1). `null` sem o campo, e
     * também no "A(1)" da arma de arremesso (ali não há carregador: a arma é a munição).
     */
    fun capacidade(tirosRaw: String?): Int? =
        Regex("^\\s*(\\d+)").find(tirosRaw ?: "")?.groupValues?.get(1)?.toIntOrNull()?.takeIf { it > 0 }

    /** Quanta munição a arma leva, somando os tipos. */
    fun total(e: Equipamento): Int = e.municoes.orEmpty().sumOf { it.quantidade.coerceAtLeast(0) }

    /**
     * O peso de UM tiro, em kg: o peso da carga cheia (`armaMunicaoKg`) dividido pelo carregador.
     * `null` quando o livro não dá o peso da munição desta arma (arco digitado à mão, por exemplo).
     */
    fun pesoPorTiro(e: Equipamento): Float? {
        val carga = e.armaMunicaoKg?.takeIf { it > 0f } ?: return null
        val cabe = capacidade(e.armaTirosRaw) ?: return null
        return carga / cabe
    }

    /** O peso de toda a munição que a arma leva (0 quando o livro não dá o peso). */
    fun pesoDaMunicao(e: Equipamento): Float = (pesoPorTiro(e) ?: 0f) * total(e)

    /** O peso que a arma soma à carga além do dela: a munição e os acessórios. */
    fun pesoExtra(e: Equipamento): Float =
        if (e.tipo != TipoEquipamento.ARMA) 0f else pesoDaMunicao(e) + e.acessorios.orEmpty().sumOf { it.pesoKg.toDouble() }.toFloat()

    /** O custo da munição que ela leva ($40 por quilo, MB p.279) e dos acessórios. */
    fun custoExtra(e: Equipamento): Float =
        if (e.tipo != TipoEquipamento.ARMA) 0f else pesoDaMunicao(e) * CUSTO_POR_KG + e.acessorios.orEmpty().sumOf { it.custo.toDouble() }.toFloat()

    /**
     * **A ficha antiga migra sozinha** (decisão 2): cada item MUNICAO com "Serve para" = uma arma do inventário entra
     * nela como munição e sai da lista. O que não acha a arma fica onde está (nada se perde). Rodar de novo não muda nada.
     */
    fun migrar(equipamentos: List<Equipamento>): List<Equipamento> {
        val orfas = equipamentos.filter { it.tipo == TipoEquipamento.MUNICAO && !it.municaoDe.isNullOrBlank() }
        if (orfas.isEmpty()) return equipamentos
        val doArma = mutableMapOf<Int, MutableList<MunicaoDaArma>>()
        val migradas = mutableSetOf<Equipamento>()
        orfas.forEach { m ->
            val i = equipamentos.indexOfFirst { it.tipo == TipoEquipamento.ARMA && it.nome.trim().equals(m.municaoDe!!.trim(), ignoreCase = true) }
            if (i < 0) return@forEach
            doArma.getOrPut(i) { equipamentos[i].municoes.orEmpty().toMutableList() }.also { lista ->
                val nome = m.nome.trim().ifBlank { MunicaoDaArma.NOME_COMUM }
                val j = lista.indexOfFirst { it.nome.equals(nome, ignoreCase = true) }
                if (j >= 0) lista[j] = lista[j].copy(quantidade = lista[j].quantidade + m.quantidade)
                else lista.add(MunicaoDaArma(nome = nome, quantidade = m.quantidade))
            }
            migradas.add(m)
        }
        if (migradas.isEmpty()) return equipamentos
        return equipamentos.mapIndexedNotNull { i, e ->
            when {
                migradas.any { it === e } -> null
                doArma.containsKey(i) -> e.copy(municoes = doArma.getValue(i).toList())
                else -> e
            }
        }
    }

    /** As linhas do cartão da arma: "Tiros 30+1(3) · Munição: 90" e os acessórios. Vazio quando não há o que dizer. */
    fun linhas(e: Equipamento): List<String> = buildList {
        if (usaMunicao(e)) {
            val tipos = e.municoes.orEmpty()
            val tiros = e.armaTirosRaw?.takeIf { it.isNotBlank() }?.let { "Tiros $it · " } ?: ""
            val quanto = when {
                tipos.isEmpty() -> "Munição: não informada (a Mesa não conta)"
                tipos.size == 1 -> "Munição: ${tipos[0].quantidade}" + if (tipos[0].nome != MunicaoDaArma.NOME_COMUM) " (${tipos[0].nome})" else ""
                else -> "Munição: " + tipos.joinToString(" · ") { "${it.nome} ${it.quantidade}" }
            }
            val peso = pesoDaMunicao(e).takeIf { it > 0f }?.let { " · ${"%.2f".format(it).replace('.', ',')} kg" } ?: ""
            add(tiros + quanto + peso)
        }
        e.acessorios.orEmpty().takeIf { it.isNotEmpty() }?.let { a -> add("Acessórios: " + a.joinToString(", ") { it.nome }) }
    }

    /** O item do catálogo (tipo "municao") vira um tipo de munição da arma, com a regra dentro. */
    fun municaoDoItem(item: ItemDoCatalogo, quantidade: Int = 0) =
        MunicaoDaArma(nome = item.nome, quantidade = quantidade.coerceAtLeast(0), itemCatalogoId = item.id, regra = item.regra?.deepCopy())

    /** O item do catálogo (tipo "acessorio") vira acessório da arma, com a regra, o peso e o custo do livro. */
    fun acessorioDoItem(item: ItemDoCatalogo) =
        AcessorioDaArma(nome = item.nome, itemCatalogoId = item.id, regra = item.regra?.deepCopy(), pesoKg = item.peso, custo = item.custo ?: 0f)
}
