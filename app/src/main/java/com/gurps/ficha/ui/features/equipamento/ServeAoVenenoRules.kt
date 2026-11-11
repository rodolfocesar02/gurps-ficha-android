package com.gurps.ficha.ui.features.equipamento

import com.gurps.ficha.domain.loaders.ItensCatalogLoader
import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.ItemDoCatalogo
import com.gurps.ficha.model.ServeAoVeneno
import java.text.Normalizer

/**
 * **As regras do "Serve para" do antídoto** — IC-4B do `mesa-virtual/planos/itens/PLANO.md`. Puro (sem Android), para o teste JVM varrer.
 */
object ServeAoVenenoRules {

    private fun semAcento(s: String) = Normalizer.normalize(s, Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "").lowercase().trim()

    /** Os 7 venenos do livro (MB p.439), lidos do catálogo de itens: o que o jogador escolhe na compra. */
    fun doLivro(catalogo: List<ItemDoCatalogo>): List<ServeAoVeneno> =
        catalogo.filter { ItensCatalogLoader.tipoDe(it) == "veneno" }.map { ServeAoVeneno(id = it.id, nome = it.nome) }

    /**
     * Os venenos que o personagem já carrega: o item com regra de veneno (do catálogo) e o digitado à mão que se chama veneno, peçonha
     * ou toxina. Sem repetir o que o livro já lista.
     */
    fun doInventario(equipamentos: List<Equipamento>, doLivro: List<ServeAoVeneno>): List<ServeAoVeneno> =
        equipamentos.asSequence()
            .filter { !it.confiscado }
            .filter { e ->
                (e.regra?.get("tipo")?.takeIf { it.isJsonPrimitive }?.asString == "veneno") ||
                    (e.regra == null && Regex("veneno|pecon|toxina").containsMatchIn(semAcento(e.nome)) && e.tipo == com.gurps.ficha.model.TipoEquipamento.GERAL)
            }
            .map { ServeAoVeneno(id = e1(it), nome = it.nome.trim()) }
            .filter { v -> v.nome.isNotBlank() && doLivro.none { mesmo(it, v) } }
            .distinctBy { semAcento(it.nome) }
            .toList()

    private fun e1(e: Equipamento): String? = e.itemCatalogoId?.takeIf { it.isNotBlank() }

    /** Os chips, na ordem: o livro, depois o inventário. */
    fun opcoes(doLivro: List<ServeAoVeneno>, doInventario: List<ServeAoVeneno>): List<ServeAoVeneno> = doLivro + doInventario

    /** Duas escolhas são o mesmo veneno: o mesmo id do catálogo, ou (sem id) o mesmo nome sem acento. */
    fun mesmo(a: ServeAoVeneno, b: ServeAoVeneno): Boolean =
        if (!a.id.isNullOrBlank() && !b.id.isNullOrBlank()) a.id == b.id else semAcento(a.nome) == semAcento(b.nome)

    /** O item é um antídoto que serve a um veneno só (o Kit Antitoxinas, MB p.289)? */
    fun ehAntidoto(e: Equipamento): Boolean =
        e.regra?.get("antidoto")?.takeIf { it.isJsonPrimitive }?.asBoolean == true

    fun ehAntidoto(item: ItemDoCatalogo): Boolean =
        item.regra?.get("antidoto")?.takeIf { it.isJsonPrimitive }?.asBoolean == true

    /** A linha do cartão do kit no inventário: "Para: Veneno de Cobra", ou o aviso de que falta escolher. */
    fun linha(e: Equipamento): String? = when {
        !ehAntidoto(e) -> null
        e.serveAoVeneno?.escolhido == true -> "Para: ${e.serveAoVeneno!!.nome}"
        else -> "Sem veneno escolhido: abra o lápis e escolha (a Mesa não deixa usar)"
    }
}
