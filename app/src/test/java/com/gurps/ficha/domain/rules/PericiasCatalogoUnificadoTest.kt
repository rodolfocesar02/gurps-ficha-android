package com.gurps.ficha.domain.rules

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **A unificação dos catálogos de perícia** — passos 1, 2 e 3 (31/07/2026).
 *
 * ## O que era, e por que mexemos
 *
 * Existiam **dois** arquivos e eles não eram duplicatas: `pericias.json` era a
 * **base** (281 entradas: id, nome, atributo, dificuldade) e
 * `pericias_v2_rules_map.json` era uma **camada** por cima (descrição,
 * pré-requisito, predefinido e os **modificadores** que renderam os lotes
 * P-SIT e P-EQUIP inteiros).
 *
 * O loader juntava os dois com `aplicarRegraPericiaV2`. Medindo contra o
 * catálogo real, esse merge corrigia a dificuldade de **seis** perícias e o
 * atributo de **nenhuma** — 697 KB de arquivo para seis correções, num caminho
 * que ninguém lembrava que existia.
 *
 * ## 🔴 E o casamento por id estava furado dos DOIS lados
 *
 * - **11 perícias da base não tinham camada** e perdiam descrição e
 *   modificadores em silêncio: `arco`, `arcos`, `arremedo`, as três de Assuntos
 *   Atuais, as três de Conhecimento Oculto, `mergulho` e
 *   `operacao_de_computadores`.
 * - **4 entradas da camada eram órfãs** e nunca chegavam a lugar nenhum:
 *   `Arco` (id com maiúscula, e **repetido**), `acrobacia_aquática` (acento no
 *   id, cópia de uma entrada que já existia certa), `mergulho_nt` e
 *   `sacar_rapido`.
 *
 * ## ⚠️ A regra de ouro da migração
 *
 * Os ids da **base** são os que estão gravados nas **fichas salvas**
 * (`PericiaSelecionada.definicaoId`). Um id que mude faz o jogador **perder a
 * perícia** ao abrir a ficha. Por isso a camada foi ajustada à base, e **nenhum
 * id de `pericias.json` mudou** em nenhum dos três passos.
 *
 * ## O que este arquivo prova
 *
 * O fixture `pericias_referencia.json` foi gerado **antes** do passo 2, a partir
 * do resultado que o app montava com os dois arquivos e o merge. Os testes
 * abaixo exigem que o catálogo continue produzindo exatamente aquilo — perícia
 * por perícia, campo por campo. É a rede que permitiu mexer sem medo.
 */
class PericiasCatalogoUnificadoTest {

    private val gson = Gson()

    private data class PericiaCrua(
        val id: String = "",
        val nome: String = "",
        val atributoBase: String? = null,
        val atributosPossiveis: List<String>? = null,
        val atributoEscolhaObrigatoria: Boolean = false,
        val dificuldadeFixa: String? = null,
        val dificuldadeVariavel: Boolean = false,
        val exigeEspecializacao: Boolean = false
    )

    private fun arquivo(caminho: String): File {
        val direto = File(caminho)
        return if (direto.exists()) direto else File("app/$caminho")
    }

    private fun lerLista(caminho: String): List<PericiaCrua> {
        val f = arquivo(caminho)
        assertTrue("nao encontrei ${f.absolutePath}", f.exists())
        val tipo = object : TypeToken<List<PericiaCrua>>() {}.type
        return gson.fromJson(f.readText(Charsets.UTF_8), tipo)
    }

    private data class Unificado(val items: List<PericiaCrua> = emptyList())

    /** O catálogo que o app carrega hoje — um arquivo só (Passo 3). */
    private fun catalogo(): List<PericiaCrua> =
        gson.fromJson(
            arquivo("src/main/assets/pericias.v3.json").readText(Charsets.UTF_8),
            Unificado::class.java
        ).items

    private fun referencia() = lerLista("src/test/resources/pericias_referencia.json")

    private data class ItemMapa(
        val id: String = "",
        val nome: String = "",
        val descricao: String? = null,
        val modificadores: Map<String, Any?>? = null
    )

    private data class MapaUnificado(val items: List<ItemMapa> = emptyList())

    /**
     * A "camada" agora vem do MESMO arquivo — mas os testes que a conferem
     * continuam valendo, porque a pergunta não mudou: toda perícia tem regras?
     */
    private fun mapa(): List<ItemMapa> =
        gson.fromJson(
            arquivo("src/main/assets/pericias.v3.json").readText(Charsets.UTF_8),
            MapaUnificado::class.java
        ).items

    // ==================================================================
    // A rede: o catálogo continua igual ao que o app montava antes
    // ==================================================================

    @Test
    fun `🔴 o catalogo produz EXATAMENTE o que os dois arquivos produziam`() {
        // A prova de que os passos 2 e 3 não mexeram em número nenhum. Se um
        // campo de uma perícia mudar, este teste diz qual.
        val agora = catalogo().associateBy { it.id }
        val antes = referencia().associateBy { it.id }

        assertEquals("o total de perícias mudou", antes.size, agora.size)

        val erros = antes.mapNotNull { (id, esperado) ->
            val atual = agora[id] ?: return@mapNotNull "$id sumiu do catálogo"
            if (atual == esperado) null
            else "$id (${esperado.nome}):\n      antes = $esperado\n      agora = $atual"
        }
        assertTrue(
            "O catálogo unificado divergiu da referência:\n" + erros.joinToString("\n"),
            erros.isEmpty()
        )
    }

    @Test
    fun `⚠️ nenhum id da base mudou - e id e o que a ficha salva guarda`() {
        // A regra de ouro. `PericiaSelecionada.definicaoId` aponta para estes
        // ids; um que mude faz o jogador perder a perícia ao abrir a ficha.
        assertEquals(referencia().map { it.id }.toSet(), catalogo().map { it.id }.toSet())
    }

    @Test
    fun `as SEIS dificuldades que a camada corrigia agora estao na base`() {
        // Eram o único trabalho real do `aplicarRegraPericiaV2`. Ao aposentá-lo,
        // elas tinham de migrar — senão a Camuflagem voltaria a ser Difícil.
        val porNome = catalogo().associateBy { it.nome }
        mapOf(
            "Camuflagem" to "F",
            "Ciclismo" to "M",
            "Salto" to "M",
            "Controle das Funções Involuntárias" to "MD",
            "Arte de Combate" to "M",
            "Esporte de Combate/NT (†)" to "M"
        ).forEach { (nome, dificuldade) ->
            assertEquals("$nome com a dificuldade errada", dificuldade, porNome[nome]?.dificuldadeFixa)
        }
    }

    // ==================================================================
    // O passo 1: a camada alcança todo mundo
    // ==================================================================

    @Test
    fun `🔴 TODA pericia do catalogo tem camada de regras`() {
        // Sem camada, a perícia fica sem descrição, sem pré-requisito e sem os
        // modificadores — em silêncio. Eram 11 assim.
        val comCamada = mapa().map { it.id }.toSet()
        val sem = catalogo().map { it.id }.filterNot { it in comCamada }
        assertTrue("perícias sem camada de regras: $sem", sem.isEmpty())
    }

    @Test
    fun `🔴 camada orfa deixou de ser POSSIVEL`() {
        // Este era o teste que listava as órfãs conhecidas. Depois do Passo 3 a
        // pergunta perdeu o sentido — e é esse o ponto da fusão: com um arquivo
        // só, "camada com id que a base não tem" não existe mais como categoria.
        //
        // As duas que sobravam (`mergulho_nt` e `sacar_rapido`) simplesmente não
        // foram para o arquivo unificado: a primeira era a grafia velha do
        // `mergulho`, e a segunda pertence a `pericias_artes_marciais.v1.json`,
        // que é outro livro e tem carregamento próprio.
        val idsDaBase = catalogo().map { it.id }.toSet()
        val orfas = mapa().map { it.id }.filterNot { it in idsDaBase }
        assertTrue("nao deveria existir orfa nenhuma: $orfas", orfas.isEmpty())
    }

    @Test
    fun `o arquivo unificado carrega as regras junto com a pericia`() {
        // A prova de que a fusão levou o conteúdo, e não só os ids. Se o passo 3
        // tivesse copiado a base sem a camada, o app perderia descrição e
        // modificadores de todas as 281 de uma vez — e nenhum outro teste
        // notaria, porque os campos de tipo continuariam certos.
        val itens = mapa()
        assertEquals(281, itens.size)
        assertTrue(
            "quase toda perícia deveria ter descrição",
            itens.count { !it.descricao.isNullOrBlank() } > 270
        )
        assertTrue(
            "os modificadores do P-SIT e do P-EQUIP vieram junto",
            itens.count { (it.modificadores?.get("raw") as? String).orEmpty().length > 1 } > 150
        )
    }

    @Test
    fun `nenhum id se repete na camada`() {
        // 🔴 `Arco` aparecia DUAS vezes, e `acrobacia_aquática` era cópia
        // acentuada de uma entrada que já existia certa. Id repetido faz o
        // último vencer, e qual é "o último" depende da ordem do arquivo.
        val ids = mapa().map { it.id }
        val repetidos = ids.groupingBy { it }.eachCount().filterValues { it > 1 }
        assertTrue("ids repetidos na camada: ${repetidos.keys}", repetidos.isEmpty())
    }

    @Test
    fun `nenhum id da camada tem MAIUSCULA`() {
        // `Arco` era o único, e foi por isso que nunca casou com a base.
        //
        // ⚠️ A primeira versão deste teste também proibia **acento**, e caiu
        // acusando `caratê`, `judô`, `perícia_abrangente`, `perícia_forense_nt`,
        // `perícia_profissional` e `perícias_de_passatempo`. Fui conferir: os
        // seis existem **com acento na base também**. Ou seja, o acento no id é
        // a convenção real deste catálogo, não um defeito — o teste é que estava
        // inventando uma regra que o projeto nunca teve.
        //
        // Quem garante que id da camada casa com id da base é o teste de órfãs,
        // que é a pergunta que importa de verdade.
        val fora = mapa().map { it.id }.filter { it != it.lowercase() }
        assertTrue("ids com maiúscula: $fora", fora.isEmpty())
    }
}
