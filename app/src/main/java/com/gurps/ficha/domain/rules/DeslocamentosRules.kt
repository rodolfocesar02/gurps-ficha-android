package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem
import kotlin.math.floor

/**
 * **Todos os deslocamentos do personagem** (Lote DESL-2, MB p.17-19 e p.395).
 *
 * ## Por que virou um lugar só
 *
 * O DESL-1 somava **células condicionais** na linha de Características Derivadas:
 * *Vel. Básica · Desloc. · BC · Voando · Escalando*. No aparelho isso já ficou
 * apertado com cinco números, e faltavam pelo menos três tipos — cada vantagem
 * nova empurrava a linha até quebrar.
 *
 * Ideia do usuário (T-L7): um botão **"Desloc."** que abre a lista inteira. A
 * linha volta a ter tamanho fixo e **cabe tudo**, inclusive o que não tinha onde
 * aparecer.
 *
 * ## ⚠️ Todas as linhas aparecem, inclusive as de valor ZERO
 *
 * Ver *"Aéreo: 0 — sem a vantagem Voo"* **ensina a regra**. A célula que
 * simplesmente não existe deixa o jogador sem saber se é zero ou se o app
 * esqueceu — mesmo motivo do aviso de alcance do MIRA-2b: silêncio é resposta
 * ambígua.
 *
 * ## O que cada conta é, conferido no livro
 *
 * - **Terrestre**: Velocidade Básica **sem a fração** (5,75 → 5).
 * - **Carga** (p.17): Leve **×0,8**, Média **×0,6**, Pesada **×0,4**, Muito
 *   Pesada **×0,2**. *"Ignore todas as frações. A Carga nunca reduz o
 *   Deslocamento (…) a um valor inferior a 1."*
 * - **Disparada** (p.395): *"velocidade até 20% maior que seu Deslocamento (no
 *   mínimo, Deslocamento +1)"*.
 * - **Caminhada** (p.352): *"A distância em quilômetros que o personagem é capaz
 *   de marchar em um dia (…) é igual a 15 × Deslocamento"*.
 * - **Aquático** (p.19): Deslocamento Básico **÷ 5**, arredondado para baixo, mais
 *   o bônus comprado. **Anfíbio** iguala ao terrestre.
 * - **Aéreo** (p.19): **zero** sem vantagem; com Voo, **Velocidade Básica × 2**
 *   — o livro avisa entre parênteses *"(não Deslocamento Básico × 2)"*, sinal de
 *   que erram muito nisso. **Caminhar no Ar** iguala ao terrestre.
 * - **Escalando** (p.91): Deslocamento + Super Escalada.
 *
 * Kotlin puro e testável.
 */
object DeslocamentosRules {

    const val ID_ANFIBIO = "anfibio"
    const val ID_CAMINHAR_NO_AR = "caminhar_no_ar"
    const val ID_DESLOCAMENTO_AQUATICO = "deslocamento_aquatico"

    /**
     * **Invertebrado** (MB p.148) — Lote D-MIRA.
     *
     * > Ele utiliza sua Base de Carga total para **empurrar** coisas, mas apenas
     * > **1/4 da BC** para calcular o peso de objetos que o personagem é capaz
     * > de **erguer, carregar ou puxar**.
     *
     * ⚠️ Sem isto o personagem carregava **quatro vezes** mais do que deveria —
     * e é o tipo de erro que ninguém confere, porque o número parece normal.
     */
    const val ID_INVERTEBRADO = "invertebrado"

    /** Se a ficha tem Invertebrado: sem coluna, sem força para carregar. */
    fun ehInvertebrado(personagem: Personagem): Boolean =
        personagem.desvantagensTotais.any { it.definicaoId == ID_INVERTEBRADO }

    /**
     * A Base de Carga que vale para **erguer, carregar e puxar**.
     *
     * ⚠️ **Não** é a BC de empurrar, que continua inteira. O app só mostra a de
     * carregar — é a que decide o nível de carga e, por tabela, o Deslocamento.
     */
    fun baseDeCargaEfetiva(personagem: Personagem): Float =
        if (ehInvertebrado(personagem)) personagem.baseCarga / 4f else personagem.baseCarga

    /** Uma linha da lista, pronta para a tela. */
    data class Linha(
        val rotulo: String,
        val valor: String,
        val conta: String,
        /** Marca a linha que está valendo agora — só a carga usa isto. */
        val ehAtual: Boolean = false
    )

    /** Os cinco níveis de carga do livro (p.17). */
    enum class NivelCarga(
        val indice: Int,
        val rotulo: String,
        val fator: Double,
        val esquiva: Int
    ) {
        NENHUMA(0, "Nenhuma", 1.0, 0),
        LEVE(1, "Leve", 0.8, -1),
        MEDIA(2, "Média", 0.6, -2),
        PESADA(3, "Pesada", 0.4, -3),
        MUITO_PESADA(4, "Muito Pesada", 0.2, -4);

        companion object {
            fun de(indice: Int): NivelCarga =
                entries.firstOrNull { it.indice == indice } ?: NENHUMA
        }
    }

    /**
     * O Deslocamento com um nível de carga aplicado.
     *
     * ⚠️ **Corta a fração e nunca desce abaixo de 1** — as duas coisas na mesma
     * frase do livro. Sem o piso, uma carga muito pesada num Deslocamento 4 daria
     * **0**, e o personagem ficaria imóvel por arredondamento.
     */
    fun deslocamentoComCarga(deslocamentoBasico: Int, nivel: NivelCarga): Int =
        floor(deslocamentoBasico * nivel.fator).toInt().coerceAtLeast(1)

    /**
     * O número que a ficha mostra: o Deslocamento **já descontado** pela carga
     * que o personagem está carregando agora.
     *
     * É o que o jogador quer saber sem abrir nada — "quanto eu ando?" — e é o que
     * o botão exibe.
     */
    fun deslocamentoAtual(personagem: Personagem): Int =
        deslocamentoComCarga(
            personagem.deslocamentoBasico,
            NivelCarga.de(personagem.nivelCarga)
        )

    /** Se a ficha tem Anfíbio (MB p.39): nada e anda igual. */
    fun ehAnfibio(personagem: Personagem): Boolean =
        personagem.vantagensTotais.any { it.definicaoId == ID_ANFIBIO }

    /** Se a ficha tem Caminhar no Ar (MB p.46): o ar vira chão. */
    fun temCaminharNoAr(personagem: Personagem): Boolean =
        personagem.vantagensTotais.any { it.definicaoId == ID_CAMINHAR_NO_AR }

    /**
     * Deslocamento aquático.
     *
     * 🔴 O app calculava **sempre** `Básico ÷ 5 + bônus` e ignorava o **Anfíbio**,
     * que iguala o aquático ao terrestre. Achado ao escrever este lote.
     */
    fun deslocamentoNadando(personagem: Personagem): Int =
        if (ehAnfibio(personagem)) {
            personagem.deslocamentoBasico
        } else {
            personagem.deslocamentoBasico / 5 + personagem.bonusDeslocamentoAquatico
        }

    /** Deslocamento aéreo: zero sem vantagem nenhuma. */
    fun deslocamentoVoando(personagem: Personagem): Int = when {
        DeslocamentosEspeciais.podeVoar(personagem) ->
            DeslocamentosEspeciais.deslocamentoVoando(personagem)
        temCaminharNoAr(personagem) -> personagem.deslocamentoBasico
        else -> 0
    }

    /** Disparada: 20% a mais, com piso de Deslocamento + 1 (MB p.395). */
    fun disparada(deslocamento: Int): Int =
        maxOf(floor(deslocamento * 1.2).toInt(), deslocamento + 1)

    /**
     * A lista inteira, na ordem em que aparece na tela.
     *
     * ⚠️ É **só leitura**. Nenhuma linha é selecionável e nada aqui fica "fixado"
     * — decisão do usuário: *"ele não seleciona um tipo e fica fixo, apenas
     * leitura"*. Um deslocamento escolhido e esquecido viraria número errado em
     * silêncio, do mesmo jeito que a distância do alvo no MIRA-2.
     */
    fun todos(personagem: Personagem): List<Linha> {
        val basico = personagem.deslocamentoBasico
        val atual = NivelCarga.de(personagem.nivelCarga)
        val andando = deslocamentoComCarga(basico, atual)
        val linhas = mutableListOf<Linha>()

        linhas += Linha(
            "Terrestre (agora)", "$andando m/s",
            "Deslocamento $basico com carga ${atual.rotulo}"
        )
        linhas += Linha(
            "Disparada", "${disparada(andando)} m/s",
            "correndo em linha reta a partir do 2º turno: +20% (MB p.395)"
        )
        linhas += Linha(
            "Marcha de um dia", "${15 * andando} km",
            "15 × Deslocamento, em condições ideais (MB p.352)"
        )

        // Nadando
        linhas += if (ehAnfibio(personagem)) {
            Linha("Nadando", "${deslocamentoNadando(personagem)} m/s", "Anfíbio: igual ao terrestre")
        } else {
            val bonus = personagem.bonusDeslocamentoAquatico
            Linha(
                "Nadando", "${deslocamentoNadando(personagem)} m/s",
                "Deslocamento $basico ÷ 5" + if (bonus > 0) " + $bonus comprado" else ""
            )
        }

        // Voando — a linha existe mesmo em zero, e é aí que ela ensina a regra.
        linhas += when {
            DeslocamentosEspeciais.podeVoar(personagem) -> Linha(
                "Voando", "${deslocamentoVoando(personagem)} m/s",
                "Velocidade Básica ${"%.2f".format(personagem.velocidadeBasica)} × 2, sem fração"
            )
            temCaminharNoAr(personagem) -> Linha(
                "No ar", "${deslocamentoVoando(personagem)} m/s",
                "Caminhar no Ar: o ar funciona como chão"
            )
            else -> Linha("Voando", "0", "sem a vantagem Voo, o aéreo é sempre zero (MB p.19)")
        }

        // Escalando
        linhas += if (DeslocamentosEspeciais.temSuperEscalada(personagem)) {
            Linha(
                "Escalando", "${DeslocamentosEspeciais.deslocamentoEscalando(personagem)} m/s",
                "Deslocamento $basico + Super Escalada " +
                    "${DeslocamentosEspeciais.bonusEscalada(personagem)}"
            )
        } else {
            Linha("Escalando", "$basico m/s", "sem Super Escalada, usa o Deslocamento")
        }

        return linhas
    }

    /**
     * A tabela dos cinco níveis de carga, com o atual marcado.
     *
     * Decisão do usuário: **mostrar os cinco sempre**. A tabela inteira ensina a
     * regra e é curta — e o jogador vê o que aconteceria se largasse a mochila,
     * que é a pergunta que ele faz de verdade.
     */
    fun tabelaDeCarga(personagem: Personagem): List<Linha> {
        val basico = personagem.deslocamentoBasico
        // A BC de CARREGAR — o Invertebrado usa 1/4 dela (MB p.148).
        val bc = baseDeCargaEfetiva(personagem)
        val atual = personagem.nivelCarga
        val tetos = mapOf(0 to 1f, 1 to 2f, 2 to 3f, 3 to 6f, 4 to 10f)

        return NivelCarga.entries.map { n ->
            val teto = tetos.getValue(n.indice) * bc
            Linha(
                rotulo = "${n.rotulo} (${n.indice})",
                valor = "${deslocamentoComCarga(basico, n)} m/s",
                conta = "até ${"%.1f".format(teto)} kg · Esquiva ${n.esquiva}",
                ehAtual = n.indice == atual
            )
        }
    }

    /** O rótulo do peso atual, para o cabeçalho da tabela. */
    fun resumoDaCarga(personagem: Personagem): String {
        val n = NivelCarga.de(personagem.nivelCarga)
        val bc = baseDeCargaEfetiva(personagem)
        // A nota do Invertebrado é obrigatória: sem ela o jogador vê uma BC que
        // não bate com a ST dele e acha que o app errou a conta.
        val nota = if (ehInvertebrado(personagem)) {
            " (Invertebrado: 1/4 da BC para carregar — a de empurrar continua " +
                "${"%.1f".format(personagem.baseCarga)} kg, MB p.148)"
        } else ""
        return "Carregando ${"%.1f".format(personagem.pesoTotalEquipamentos)} kg " +
            "de ${"%.1f".format(bc)} kg de Base de Carga — carga ${n.rotulo}$nota"
    }
}
