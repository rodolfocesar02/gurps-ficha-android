package com.gurps.ficha.model

/**
 * O conteúdo REAL de um modelo racial: o dele **mais** o das
 * metacaracterísticas embutidas.
 *
 * ## O problema que este arquivo resolve
 *
 * Uma metacaracterística é um pacote dentro do pacote (MB p.262). "Espírito"
 * guarda Idade Imutável, Imunidade a Dano e mais uma dúzia de traços; "Corpo de
 * Madeira" guarda −1 de Velocidade Básica. Na ficha ela aparece como **uma
 * linha só** — que é exatamente o que o livro manda ("anote a
 * metacaracterística, não seus componentes") — mas os componentes existem, e são
 * eles que fazem efeito.
 *
 * O `custoTotal` já somava os componentes (`custoMeta`). O **efeito** não. O
 * personagem pagava 20 pontos pelo −1 de Velocidade do Corpo de Madeira e
 * continuava com a Velocidade cheia.
 *
 * Analogia: é uma caixa fechada dentro da mala. A balança do aeroporto pesa a
 * caixa junto — mas quem só olha o que está solto na mala jura que ela está
 * vazia.
 *
 * ## Por que fica aqui e não dentro de `ModeloRacial`
 *
 * `Personagem.kt` já passa de 1300 linhas. A regra do projeto é criar ao lado
 * em vez de engordar. Como são extensões no mesmo pacote, quem usa não precisa
 * nem importar — a chamada fica idêntica a um campo da classe.
 *
 * ## O parâmetro `profundidade`
 *
 * ⚠️ Uma metacaracterística pode conter outra. O limite **não é regra do
 * GURPS**: é freio contra ficha salva defeituosa que aponte para si mesma e
 * travaria o app num laço infinito. Quatro níveis é folgado — o catálogo hoje
 * usa um.
 */
private const val PROFUNDIDADE_PADRAO = 4

/** Vantagens do pacote **e** as de dentro das metacaracterísticas. */
fun ModeloRacial.todasAsVantagens(profundidade: Int = PROFUNDIDADE_PADRAO): List<VantagemSelecionada> =
    vantagens + filhos(profundidade).flatMap { it.todasAsVantagens(profundidade - 1) }

/** O mesmo para desvantagens. */
fun ModeloRacial.todasAsDesvantagens(profundidade: Int = PROFUNDIDADE_PADRAO): List<DesvantagemSelecionada> =
    desvantagens + filhos(profundidade).flatMap { it.todasAsDesvantagens(profundidade - 1) }

/** O mesmo para as perícias raciais. */
fun ModeloRacial.todasAsPericias(profundidade: Int = PROFUNDIDADE_PADRAO): List<PericiaRacial> =
    pericias + filhos(profundidade).flatMap { it.todasAsPericias(profundidade - 1) }

/** O mesmo para qualidades (+1 pt cada) e peculiaridades (−1 pt cada). */
fun ModeloRacial.todasAsQualidades(profundidade: Int = PROFUNDIDADE_PADRAO): List<String> =
    qualidades + filhos(profundidade).flatMap { it.todasAsQualidades(profundidade - 1) }

/** Ver [todasAsQualidades]. */
fun ModeloRacial.todasAsPeculiaridades(profundidade: Int = PROFUNDIDADE_PADRAO): List<String> =
    peculiaridades + filhos(profundidade).flatMap { it.todasAsPeculiaridades(profundidade - 1) }

// --- modificadores de atributo ---
//
// O catálogo pronto só usa um destes hoje (Corpo de Madeira, −1 de Velocidade),
// mas o Mestre pode montar a metacaracterística que quiser na tela de edição.
// Deixar de fora seria criar o mesmo buraco de novo, só que mais raro — e bug
// raro é pior de achar.

fun ModeloRacial.modForcaTotal(p: Int = PROFUNDIDADE_PADRAO): Int =
    modForca + filhos(p).sumOf { it.modForcaTotal(p - 1) }

fun ModeloRacial.modDestrezaTotal(p: Int = PROFUNDIDADE_PADRAO): Int =
    modDestreza + filhos(p).sumOf { it.modDestrezaTotal(p - 1) }

fun ModeloRacial.modInteligenciaTotal(p: Int = PROFUNDIDADE_PADRAO): Int =
    modInteligencia + filhos(p).sumOf { it.modInteligenciaTotal(p - 1) }

fun ModeloRacial.modVitalidadeTotal(p: Int = PROFUNDIDADE_PADRAO): Int =
    modVitalidade + filhos(p).sumOf { it.modVitalidadeTotal(p - 1) }

fun ModeloRacial.modPontosVidaTotal(p: Int = PROFUNDIDADE_PADRAO): Int =
    modPontosVida + filhos(p).sumOf { it.modPontosVidaTotal(p - 1) }

fun ModeloRacial.modVontadeTotal(p: Int = PROFUNDIDADE_PADRAO): Int =
    modVontade + filhos(p).sumOf { it.modVontadeTotal(p - 1) }

fun ModeloRacial.modPercepcaoTotal(p: Int = PROFUNDIDADE_PADRAO): Int =
    modPercepcao + filhos(p).sumOf { it.modPercepcaoTotal(p - 1) }

fun ModeloRacial.modPontosFadigaTotal(p: Int = PROFUNDIDADE_PADRAO): Int =
    modPontosFadiga + filhos(p).sumOf { it.modPontosFadigaTotal(p - 1) }

fun ModeloRacial.modVelocidadeBasicaTotal(p: Int = PROFUNDIDADE_PADRAO): Float =
    modVelocidadeBasica + filhos(p).map { it.modVelocidadeBasicaTotal(p - 1) }.sum()

fun ModeloRacial.modDeslocamentoBasicoTotal(p: Int = PROFUNDIDADE_PADRAO): Int =
    modDeslocamentoBasico + filhos(p).sumOf { it.modDeslocamentoBasicoTotal(p - 1) }

/**
 * Os pacotes de dentro — vazio quando a profundidade acabou.
 *
 * Centralizar a parada num lugar só evita o clássico "esqueci o guarda em uma
 * das dez funções".
 */
private fun ModeloRacial.filhos(profundidade: Int): List<ModeloRacial> =
    if (profundidade <= 0) emptyList() else metacaracteristicas.map { it.conteudo }
