package com.gurps.ficha.model

/**
 * **Os numeros que a ficha CALCULA** — lote CAMPO-16.
 *
 * ## 🔴 O buraco que isto tapa
 *
 * O JSON que o app exporta tem 43 campos e **so dados crus**: `forca`,
 * `destreza`, `pericias[].pontosGastos`, `modVelocidadeBasica`. Ele **nao tem**
 * NH de pericia nenhuma, nao tem Esquiva, nao tem Velocidade Basica, nao tem PV
 * maximo.
 *
 * O motivo e uma linha de Kotlin: os derivados sao propriedades `get()`, e o
 * Gson serializa **campos**, nao propriedades. Quem le o arquivo do lado de fora
 * ve os ingredientes e nao ve o bolo.
 *
 * ⚠️ E a alternativa era pior: a Mesa Virtual recalcular tudo do lado dela. Isso
 * seriam **duas contas para a mesma coisa** -- o defeito numero um deste projeto,
 * que ja custou caro tres vezes (a chave do retrato, o formatador da rolagem, a
 * leitura de `data:`). Com o tempo elas divergem, e ai a ficha diz uma Esquiva e
 * o tabuleiro joga com outra.
 *
 * ## 🔴 Este bloco e SO DE SAIDA
 *
 * Ele nunca volta para dentro do personagem. Ao importar uma ficha, o
 * `calculado` e **ignorado** e tudo e recalculado dos dados crus.
 *
 * Sem isso, um arquivo mexido a mao poria uma Esquiva 20 no bloco e o app
 * acreditaria -- e o pior e que ela sobreviveria a tudo, porque nao ha nada nos
 * dados crus que a contradiga.
 *
 * ## ⚠️ Como ele NAO envelhece calado
 *
 * O perigo real nao e o numero sair errado: e alguem acrescentar um derivado
 * novo ao `Personagem` daqui a seis meses e esquecer de o pôr aqui. O bloco
 * continua a existir, continua certo no que tem, e fica **incompleto sem que
 * ninguem perceba**.
 *
 * Por isso ha um teste que varre as propriedades calculadas do `Personagem` por
 * reflexao e cobra que **cada uma** esteja aqui ou numa lista de exclusoes
 * escrita a mao, com motivo. Acrescentar um derivado passa a obrigar uma
 * decisao -- e nao permite o esquecimento.
 */
data class PericiaCalculada(
    /** O id da definicao, que e o que a Mesa usa para casar com o catalogo. */
    val id: String = "",
    val nome: String = "",
    /** O Nivel de Habilidade, ja com pontos, atributo, bonus racial e o resto. */
    val nh: Int = 0
)

data class FichaCalculada(
    // Atributos ja somados com o modelo racial e os bonus de vantagem.
    val st: Int = 10,
    val dx: Int = 10,
    val iq: Int = 10,
    val ht: Int = 10,

    val pontosVida: Int = 10,
    val pontosFadiga: Int = 10,
    val vontade: Int = 10,
    val percepcao: Int = 10,

    /**
     * A **Velocidade Basica** (MB p.17).
     *
     * 🔴 E ela que o contador de turnos da Mesa usa para ordenar a iniciativa
     * (CAMPO-15) -- maior primeiro, que e como o GURPS decide. Sem este numero,
     * a fila cai em ordem alfabetica.
     *
     * ⚠️ Vai com casas decimais de proposito: 5,25 e 5,50 sao ordens diferentes
     * na mesa, e arredondar aqui empataria quem nao empata.
     */
    val velocidadeBasica: Float = 5f,
    val deslocamentoBasico: Int = 5,
    /** O Deslocamento ja com a penalidade da carga que a pessoa carrega. */
    val deslocamentoAtual: Int = 5,
    val nivelCarga: Int = 0,

    val esquiva: Int = 8,
    /**
     * ⚠️ `null` quando a pessoa nao tem pericia de Apara escolhida, e `null` **nao
     * e zero**: zero seria "apara e falha sempre", e o certo e "nao apara".
     */
    val apara: Int? = null,
    /** ⚠️ `null` quando nao ha escudo. Mesmo motivo. */
    val bloqueio: Int? = null,

    val danoGdP: String = "1d-2",
    val danoGeB: String = "1d",

    val pericias: List<PericiaCalculada> = emptyList(),

    /**
     * O cabecalho de pontos: quanto a ficha vale e quanto sobra.
     *
     * ⚠️ So os TRES numeros do cabecalho. A conta por categoria (quanto foi
     * para pericias, quanto para vantagens) fica de fora: quem audita uma ficha
     * faz isso no app, que tem a tela para o mostrar. A Mesa mostra a ficha, nao
     * a auditoria dela.
     */
    val pontosGastos: Int = 0,
    val pontosRestantes: Int = 0,
    val pontosTotaisDisponiveis: Int = 0
) {
    companion object {
        /**
         * Calcula o bloco a partir do personagem.
         *
         * 🔴 Chamando as **mesmas** propriedades que a tela usa. Nao ha aqui uma
         * segunda implementacao das regras -- se houvesse, seria exatamente o
         * defeito que este arquivo existe para evitar.
         */
        fun de(personagem: Personagem): FichaCalculada {
            val defesas = personagem.defesasAtivas
            return FichaCalculada(
                st = personagem.st,
                dx = personagem.dx,
                iq = personagem.iq,
                ht = personagem.ht,
                pontosVida = personagem.pontosVida,
                pontosFadiga = personagem.pontosFadiga,
                vontade = personagem.vontade,
                percepcao = personagem.percepcao,
                velocidadeBasica = personagem.velocidadeBasica,
                deslocamentoBasico = personagem.deslocamentoBasico,
                deslocamentoAtual = personagem.deslocamentoAtual,
                nivelCarga = personagem.nivelCarga,
                esquiva = defesas.calcularEsquiva(personagem),
                apara = defesas.calcularApara(personagem),
                bloqueio = defesas.calcularBloqueio(personagem),
                danoGdP = personagem.danoGdP,
                danoGeB = personagem.danoGeB,
                // ⚠️ `periciasTotais`, e nao `pericias`: as raciais concedidas
                // tambem se rolam, e uma ficha que as escondesse faria o Mestre
                // achar que o orc nao sabe rastrear.
                pericias = personagem.periciasTotais.map {
                    PericiaCalculada(
                        id = it.definicaoId,
                        nome = it.nome,
                        nh = it.calcularNivel(personagem)
                    )
                },
                pontosGastos = personagem.pontosGastos,
                pontosRestantes = personagem.pontosRestantes,
                pontosTotaisDisponiveis = personagem.pontosTotaisDisponiveis
            )
        }
    }
}
