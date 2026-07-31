package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.traits.BonusCondicional
import com.gurps.ficha.domain.rules.traits.TraitRuleRegistry
import com.gurps.ficha.model.Personagem

/**
 * Os testes de **resistir** que a ficha sabe montar (Lote RESIST-1).
 *
 * O GURPS é cheio de teste que não é perícia nem atributo puro: manter
 * consciência, evitar a morte, resistir a doença, veneno, medo. Antes deste
 * lote a aba Rolagem não tinha onde pô-los — o Teste de Reação e o Autocontrole
 * acabaram como painéis soltos no fim da tela, e o resto simplesmente não
 * existia.
 *
 * Este objeto é o catálogo desses testes. A UI (`DialogoReacaoEResistencia`)
 * só desenha o que vem daqui.
 *
 * **Todos são testes que o JOGADOR rola** — é o que serve à mesa via Discord.
 * O que depende de um PdM agindo (o −N no NH do mago inimigo, por exemplo) fica
 * de fora de propósito: só existiria dentro do combate tático.
 */
object ResistenciaRules {

    /** De onde o teste sai — serve para agrupar na tela. */
    enum class Familia(val rotulo: String) {
        CORPO("Corpo"),
        MENTE("Mente"),
        SOBRENATURAL("Sobrenatural")
    }

    /**
     * Um teste de resistência já montado.
     *
     * [origens] existe pelo mesmo motivo da notinha das perícias: número que
     * não diz de onde veio é caixa preta.
     */
    data class TesteDeResistencia(
        val rotulo: String,
        val alvo: Int,
        val explicacao: String,
        val familia: Familia,
        val origens: List<String> = emptyList(),
        /**
         * Modificadores que só valem em certa situação — viram caixinha na tela
         * (Lote D-NA).
         *
         * ⚠️ **Não estão somados no [alvo].** A Covardia dá −3 na Verificação de
         * Pânico *"sempre que houver risco de dano físico"*, e a Xenofilia dá
         * **+2** *"quando encontra criaturas estranhas"*. Somar sempre daria o
         * número em situação onde o livro não o prevê; quem sabe se a situação
         * vale é o jogador, na hora do teste.
         */
        val condicionais: List<BonusCondicional> = emptyList()
    ) {
        val descricaoAcessivel: String
            get() = "$rotulo. Alvo $alvo. $explicacao" +
                if (origens.isEmpty()) "" else " Inclui ${origens.joinToString(", ")}."
    }

    /**
     * Alvo reservado no campo `efeitos` para a **Verificação de Pânico**.
     *
     * Mesma ideia do `reacao` do [ReacaoRules]: reusa o tipo `pericia` em vez de
     * inventar um tipo novo. Antes disto, Destemor e Temor eram os únicos traços
     * que a Verificação de Pânico conhecia, e eram lidos por id no código — o
     * que obrigava uma edição em Kotlin para cada traço novo.
     */
    const val ALVO_PANICO = "panico"

    private const val ID_BOA_FORMA = "boa_forma"
    private const val ID_DESTEMOR = "destemor"
    /**
     * ⚠️ O id do catálogo é `abascanto_resistencia_a_magia`, não `abascanto`.
     *
     * Escrevi `abascanto` no Lote RESIST-1 e o teste passou — porque o teste
     * inventava o id em vez de ler o catálogo. Mesma falha do bug do V-1: cada
     * pedaço verde e o conjunto quebrado. Agora há um teste que confronta esta
     * constante com `vantagens.v3.json`.
     */
    internal const val ID_RESISTENCIA_MAGIA = "abascanto_resistencia_a_magia"
    private const val ID_DIFICIL_DE_SUBJUGAR = "dificil_de_subjugar"
    private const val ID_DURO_DE_MATAR = "duro_de_matar"

    /**
     * **Fácil de Matar** (MB p.140) — o espelho do Duro de Matar.
     *
     * > Cada nível impõe **-1 nos testes de HT feitos para verificar a
     * > sobrevivência** (…). **Isso não afeta a maioria dos testes normais de HT**
     * > — apenas aqueles que servem para evitar a morte. Os testes de HT **não
     * > podem ser reduzidos abaixo de 3**.
     *
     * ⚠️ Duas ressalvas que o livro faz questão de deixar claras, e que sem código
     * viram erro silencioso: ela **não** toca resistir a veneno, doença nem
     * esforço, e o alvo **nunca desce abaixo de 3**.
     */
    private const val ID_FACIL_DE_MATAR = "facil_de_matar"

    /**
     * **Fora de Forma** (MB p.143) — o espelho da Boa Forma.
     *
     * > **-1** (Fora de Forma) ou **-2** (Muito Fora de Forma) em todos os testes
     * > de HT para permanecer consciente, evitar a morte, resistir aos efeitos de
     * > doenças e venenos, etc.
     *
     * ⚠️ **Contraste de propósito com o Fácil de Matar**, que está logo acima: a
     * Fácil de Matar toca **só** os testes de morte; esta toca **todos** os de
     * resistência do corpo. Ter as duas lado a lado, com a diferença escrita,
     * é o que impede alguém "unificar" as duas por engano mais tarde.
     *
     * O livro também é explícito no que ela **não** faz: *"Isso não reduz sua HT
     * nem as perícias baseadas nesse atributo"*.
     */
    private const val ID_FORA_DE_FORMA = "fora_de_forma"

    /**
     * **Temor** (MB p.159) — o espelho do Destemor.
     *
     * > Subtraia o nível de Temor da Vontade sempre que fizer uma **Verificação
     * > de Pânico** ou tiver que **resistir à perícia Intimidação** ou a um poder
     * > sobrenatural que cause medo.
     */
    private const val ID_TEMOR = "temor"

    /**
     * **Suscetibilidade à Magia** (MB p.159) — o espelho da Resistência à Magia.
     *
     * > Acrescente o nível ao NH de quem estiver fazendo uma mágica contra ele e
     * > **subtraia o mesmo valor dos testes para resistir**.
     *
     * ⚠️ O id do catálogo grafa "susceptibilidade" (com **p**); o livro escreve
     * "Suscetibilidade". O id fica como está para não quebrar ficha salva.
     */
    private const val ID_SUSCETIBILIDADE_MAGIA = "susceptibilidade_a_magia"

    /** **Suscetível** (MB p.159) — o espelho do Resistente. */
    private const val ID_SUSCETIVEL = "suscetivel"

    /**
     * Penalidade de uma DESVANTAGEM por nível — devolvida negativa.
     *
     * Irmã de `nivelDe`, que lê o lado das vantagens. Separadas porque o sinal é
     * decidido aqui: quem chama soma, sempre.
     */
    private fun penalidadeDe(personagem: Personagem, id: String): Int =
        -personagem.desvantagensTotais.filter { it.definicaoId == id }
            .sumOf { it.nivel.coerceAtLeast(1) }

    /** A origem, para a notinha poder nomear a desvantagem. */
    private fun origemDaDesvantagem(personagem: Personagem, id: String): List<String> =
        personagem.desvantagensTotais.filter { it.definicaoId == id }
            .map { "${it.nome} -${it.nivel.coerceAtLeast(1)}" }

    /**
     * **Fora de Forma**: -1 ou -2, escolhido pelo custo pago (MB p.143).
     *
     * ⚠️ Lê o `custoEscolhido`, não o nível: o catálogo guarda esta desvantagem
     * como escolha de custo (-5 ou -15), igual à Boa Forma do outro lado.
     */
    private fun penalidadeForaDeForma(personagem: Personagem): Pair<Int, List<String>> {
        var total = 0
        val origens = mutableListOf<String>()
        personagem.desvantagensTotais.filter { it.definicaoId == ID_FORA_DE_FORMA }.forEach { d ->
            val p = if (d.custoEscolhido <= -15) -2 else -1
            total += p
            origens += "${d.nome} $p"
        }
        return total to origens
    }

    /** Níveis de Fácil de Matar — devolvidos como número NEGATIVO. */
    internal fun penalidadeFacilDeMatar(personagem: Personagem): Int =
        -personagem.vantagensTotais.filter { it.definicaoId == ID_FACIL_DE_MATAR }
            .sumOf { it.nivel.coerceAtLeast(1) } -
            personagem.desvantagensTotais.filter { it.definicaoId == ID_FACIL_DE_MATAR }
                .sumOf { it.nivel.coerceAtLeast(1) }

    /**
     * Todos os testes que esta ficha pode rolar.
     *
     * Os de HT existem sempre — qualquer personagem pode precisar resistir a
     * veneno. Os que dependem de vantagem (Resistência à Magia) só aparecem
     * quando a vantagem está na ficha.
     */
    fun testesDe(personagem: Personagem): List<TesteDeResistencia> {
        val ht = personagem.ht
        val vontade = personagem.vontade
        val lista = mutableListOf<TesteDeResistencia>()

        val (bonusBoa, origensBoa) = bonusBoaForma(personagem)
        // Fora de Forma acompanha a Boa Forma em TODOS os testes de corpo -- e é
        // por isso que ela entra aqui, e não só no teste de morte.
        val (penalFora, origensFora) = penalidadeForaDeForma(personagem)
        val bonusHt = bonusBoa + penalFora
        val origensHt = origensBoa + origensFora

        // --- Corpo: tudo sai do HT (MB p.419-443) ---
        lista += TesteDeResistencia(
            "Manter a consciência", ht + bonusHt + nivelDe(personagem, ID_DIFICIL_DE_SUBJUGAR),
            "Com PV em 0 ou menos, a cada turno. Falha: desmaia.",
            Familia.CORPO,
            origensHt + origemDe(personagem, ID_DIFICIL_DE_SUBJUGAR)
        )
        // ⚠️ Fácil de Matar entra SÓ aqui: o livro diz que ela não afeta os
        // testes normais de HT, apenas os que evitam a morte.
        val facil = penalidadeFacilDeMatar(personagem)
        lista += TesteDeResistencia(
            "Evitar a morte",
            (ht + bonusHt + nivelDe(personagem, ID_DURO_DE_MATAR) + facil)
                .let { PisoDeTeste.aplicar(it) },
            "Ao passar de cada múltiplo negativo do PV máximo. Falha: morre.",
            Familia.CORPO,
            origensHt + origemDe(personagem, ID_DURO_DE_MATAR) +
                if (facil != 0) listOf("Fácil de Matar $facil") else emptyList()
        )
        // Suscetível entra SÓ nestes dois: são os exemplos que o livro dá, e o
        // app não guarda a qual objeto o personagem é suscetível.
        val suscetivel = penalidadeSuscetivel(personagem)
        val origemSuscetivel = origemDaDesvantagem(personagem, ID_SUSCETIVEL)
        lista += TesteDeResistencia(
            "Resistir a doença", PisoDeTeste.aplicar(ht + bonusHt + suscetivel),
            "Contra infecção e contágio. O modificador vem da doença." +
                if (suscetivel != 0) " Confirme com o Mestre se a sua Suscetibilidade vale aqui." else "",
            Familia.CORPO, origensHt + origemSuscetivel
        )
        lista += TesteDeResistencia(
            "Resistir a veneno", PisoDeTeste.aplicar(ht + bonusHt + suscetivel),
            "O modificador vem do veneno." +
                if (suscetivel != 0) " Confirme com o Mestre se a sua Suscetibilidade vale aqui." else "",
            Familia.CORPO, origensHt + origemSuscetivel
        )
        lista += TesteDeResistencia(
            "Aguentar o esforço", ht + bonusHt,
            "Correr, segurar a respiração, calor, exaustão. Falha: perde PF.",
            Familia.CORPO, origensHt
        )

        // --- Lote RESIST-3: os testes de lesão que faltavam ---------------
        //
        // ⚠️ **Difícil de Subjugar NÃO entra aqui.** A vantagem dá +1 "nos testes
        // de HT feitos para verificar se o personagem evita a INCONSCIÊNCIA"
        // (MB p.54), e este é um teste de ficar atordoado e cair. O desmaio só
        // acontece se ele fracassar por 5 ou mais — é consequência, não o
        // assunto do teste. Somar a vantagem aqui daria de graça uma proteção
        // contra o atordoamento que o livro não concede.
        //
        // ⚠️ **Fácil de Matar e Duro de Matar também não:** os dois falam de
        // testes onde o fracasso MATA, e aqui o fracasso derruba.
        lista += TesteDeResistencia(
            "Evitar atordoamento e queda", ht + bonusHt,
            "Ao sofrer um ferimento grave. Falha: atordoado e caído. " +
                "Falha por 5 ou mais: desmaia (MB p.420).",
            Familia.CORPO, origensHt
        )
        lista += TesteDeResistencia(
            "Recuperar-se do atordoamento", ht + bonusHt,
            "A cada turno de Fazer Nada, enquanto atordoado. Defesas a −4 até lá (MB p.365).",
            Familia.CORPO, origensHt
        )
        // ⚠️ O único teste desta tela que NÃO sai de HT nem de Vontade. O livro
        // separa as duas metades na mesma frase: "um teste de HT para se
        // recuperar do atordoamento físico, ou um teste de IQ para se recuperar
        // do atordoamento mental". Por isso não leva `bonusHt`: Boa Forma e
        // Fora de Forma são do corpo, e este teste é da cabeça.
        lista += TesteDeResistencia(
            "Recuperar-se do atordoamento MENTAL", personagem.iq,
            "A cada turno de Fazer Nada. Este sai da IQ, não da HT (MB p.365).",
            // ⚠️ Família MENTE, e não CORPO. Eu o pus em CORPO primeiro e dois
            // testes ANTIGOS caíram na hora: eles varrem a família inteira
            // afirmando que Boa Forma e Fora de Forma tocam **todos** os testes
            // de corpo — e este não é de corpo, é de cabeça. A família não é
            // rótulo de tela: é o contrato de quem sofre o bônus de HT.
            Familia.MENTE
        )
        // ⚠️ **Difícil de Subjugar não entra**, de novo: ela evita a
        // inconsciência, e aqui o personagem já está inconsciente — o teste é
        // para SAIR dela.
        lista += TesteDeResistencia(
            "Acordar", ht + bonusHt,
            "A cada hora inconsciente. Abaixo de −1×PV, um único teste depois " +
                "de 12 horas (MB p.424).",
            Familia.CORPO, origensHt
        )
        // Este SIM é teste de morte: "em qualquer fracasso, ele morre". Leva o
        // mesmo trio do "Evitar a morte", com o mesmo piso de 3.
        lista += TesteDeResistencia(
            "Resistir ao ferimento fatal",
            PisoDeTeste.aplicar(ht + bonusHt + nivelDe(personagem, ID_DURO_DE_MATAR) + facil),
            "A cada meia hora, depois de falhar a morte por 1 ou 2. " +
                "Falha: morre (MB p.424).",
            Familia.CORPO,
            origensHt + origemDe(personagem, ID_DURO_DE_MATAR) +
                if (facil != 0) listOf("Fácil de Matar $facil") else emptyList()
        )

        // --- Mente ---
        // Temor é o espelho do Destemor: um soma na Vontade contra o medo, o
        // outro subtrai. Os dois no mesmo número, com o piso de 3 no fim.
        val destemor = nivelDe(personagem, ID_DESTEMOR) + penalidadeDe(personagem, ID_TEMOR)
        val origemDestemor = origemDe(personagem, ID_DESTEMOR) +
            origemDaDesvantagem(personagem, ID_TEMOR)
        // Traços declarados no catálogo com alvo `panico` (Lote D-NA). O fixo
        // entra no alvo; o condicional vira caixinha e NÃO é somado aqui.
        val panicoFixo = TraitRuleRegistry.getSkillBonus(personagem, ALVO_PANICO)
        val panicoOrigens = TraitRuleRegistry.getSkillBonusOrigens(personagem, ALVO_PANICO)
            .map { "${it.nomeDoTraco} ${if (it.valor >= 0) "+${it.valor}" else "${it.valor}"}" }
        lista += TesteDeResistencia(
            "Verificação de Pânico", PisoDeTeste.aplicar(vontade + destemor + panicoFixo),
            "Diante de horror ou do sobrenatural. NÃO é disparada por dano.",
            Familia.MENTE, origemDestemor + panicoOrigens,
            TraitRuleRegistry.getBonusCondicionais(personagem, ALVO_PANICO)
        )
        // 🔴 O piso também vale aqui, e estava faltando. O livro amarra a regra
        // ao TESTE DE VONTADE, não à Verificação de Pânico: *"não é permitido
        // reduzir o número alvo do teste de Vontade a um valor menor que 3"*
        // (MB p.159) — e o Temor age nos dois. Sem o piso, Temor 12 numa
        // Vontade 8 dava alvo −4: fracasso automático permanente.
        //
        // Achado pela simulação exaustiva, não por um caso: o teste pontual
        // usava níveis realistas, e com nível realista o piso nunca é atingido.
        lista += TesteDeResistencia(
            "Resistir a Intimidação", PisoDeTeste.aplicar(vontade + destemor),
            "Contra a perícia Intimidação de outro personagem.",
            Familia.MENTE, origemDestemor
        )
        // ⚠️ **Destemor e Temor NÃO entram.** Os dois falam de MEDO — Verificação
        // de Pânico, Intimidação, poderes que causam medo (MB p.55 e p.159). Não
        // perder a pontaria é concentração, não coragem. Somar aqui seria
        // esticar a vantagem para além do que a página dela diz.
        lista += TesteDeResistencia(
            "Não perder a pontaria", vontade,
            "Ao ser ferido enquanto está Apontando. Falha: perde a mira e o " +
                "bônus acumulado (MB p.365).",
            Familia.MENTE
        )

        // --- Sobrenatural: só com a vantagem ---
        val rm = nivelDe(personagem, ID_RESISTENCIA_MAGIA)
        if (rm > 0) {
            lista += TesteDeResistencia(
                "Resistir a elixir mágico", ht + bonusHt + rm,
                "MB p.85: teste de HT somado à Resistência à Magia.",
                Familia.SOBRENATURAL,
                origensHt + origemDe(personagem, ID_RESISTENCIA_MAGIA)
            )
        }

        // 🔴 **O piso vale para a tela inteira, e não valia.** Achado pela
        // varredura do Lote RESIST-3: com HT 3 e Fora de Forma −2, o alvo de
        // "Manter a consciência" saía **1** — fracasso automático e permanente.
        // O piso estava só onde o livro o escreve com todas as letras (Fácil de
        // Matar p.140, Temor e Suscetível p.159), e faltava em "Manter a
        // consciência" e "Aguentar o esforço", que ninguém tinha pensado em
        // empurrar tão para baixo.
        //
        // Aplicar aqui, no fim, é o que garante que **nenhum** teste da tela
        // escape — inclusive os que forem acrescentados depois. As chamadas
        // individuais lá em cima continuam onde estão: elas marcam os pontos em
        // que o livro cita a regra, e `aplicar` é idempotente (há teste disso).
        return lista.map { it.copy(alvo = PisoDeTeste.aplicar(it.alvo)) }
    }

    /**
     * O nível de Resistência à Magia, para o Mestre aplicar do outro lado.
     *
     * O livro manda subtrair este número do NH de quem lança magia no
     * personagem. Isso acontece na ficha DO MAGO, não nesta — então aqui o
     * número só é **exibido**, para o jogador informar ao Mestre no Discord.
     * Zero quando não há a vantagem.
     */
    fun resistenciaAMagia(personagem: Personagem): Int =
        nivelDe(personagem, ID_RESISTENCIA_MAGIA) +
            penalidadeDe(personagem, ID_SUSCETIBILIDADE_MAGIA)

    /**
     * Se o número acima é **negativo** — ou seja, a ficha tem Suscetibilidade à
     * Magia e a tela precisa dizer o contrário do texto habitual.
     *
     * ⚠️ Sem isto o card exibiria *"o mago sofre −3 ao conjurar em você"* para
     * quem, na verdade, **facilita** o feitiço. Número certo, frase invertida —
     * pior que não mostrar nada.
     */
    fun ehSuscetivelAMagia(personagem: Personagem): Boolean =
        resistenciaAMagia(personagem) < 0

    /**
     * **Suscetível** (MB p.159) — o espelho do Resistente.
     *
     * > **-1 por nível** nos testes de HT para resistir aos efeitos negativos de
     * > uma classe de objetos ou substâncias (doença, veneno, etc.).
     *
     * ⚠️ O app **não guarda a qual objeto** o personagem é suscetível — o
     * catálogo tem uma entrada só. Então a penalidade entra em **doença e
     * veneno**, que são os dois exemplos que o livro dá, e o texto do card avisa
     * que o Mestre decide se vale naquele caso.
     */
    private fun penalidadeSuscetivel(personagem: Personagem): Int =
        penalidadeDe(personagem, ID_SUSCETIVEL)

    /** Se a ficha tem Aptidão Mágica — usado pela trava do Abascanto. */
    fun temAptidaoMagica(personagem: Personagem): Boolean =
        personagem.vantagensTotais.any { it.definicaoId == "aptidao_magica" }

    private fun nivelDe(personagem: Personagem, id: String): Int =
        personagem.vantagensTotais.filter { it.definicaoId == id }
            .sumOf { it.nivel.coerceAtLeast(1) }

    private fun origemDe(personagem: Personagem, id: String): List<String> =
        personagem.vantagensTotais.filter { it.definicaoId == id }
            .map { "${it.nome} +${it.nivel.coerceAtLeast(1)}" }

    /** Boa Forma: +1 (5 pts) ou +2 (15 pts) em **todos** os testes de HT. */
    private fun bonusBoaForma(personagem: Personagem): Pair<Int, List<String>> {
        var total = 0
        val origens = mutableListOf<String>()
        personagem.vantagensTotais.filter { it.definicaoId == ID_BOA_FORMA }.forEach { v ->
            val b = if (v.custoEscolhido >= 15) 2 else 1
            total += b
            origens += "${v.nome} +$b"
        }
        return total to origens
    }
}
