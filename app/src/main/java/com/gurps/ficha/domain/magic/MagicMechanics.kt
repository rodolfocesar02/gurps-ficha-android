package com.gurps.ficha.domain.magic

/**
 * Lote AR-1 (mecânica estruturada das magias): a `descricao` de cada feitiço no catálogo é FIEL ao
 * livro, mas é PROSA — o motor não a executa. Este é o campo `mecanica` legível pela máquina, ao lado
 * da descrição (que fica intocada): a versão CURADA das regras que o motor aplica.
 *
 * O `efeito` é um conjunto FECHADO; o que não couber vira "narrado" (honesto — Criar Ar, Convocar
 * Elemental). Escola por escola, começando por Ar.
 */
data class MagiaMecanica(
    /** "dano" | "cura" | "condicao" | "buff" | "ambiente" | "controle" | "informacao" | "narrado". */
    val efeito: String = "narrado",

    // ── efeito "cura" (Lote MEC-10) ──────────────────────────────────────────────────────────────
    /**
     * PV restaurados por ponto de energia: Cura Superficial = 1 ("restaura o mesmo valor", até 3 PV);
     * Cura Profunda = 2 ("o dobro", até 8 PV).
     *
     * Existe porque o `efeito` NÃO tinha valor para cura — as magias de curar ficavam todas em
     * "narrado", sem restaurar PV e **sem seletor de energia** (o jogador nem escolhia quanto gastar).
     * A auditoria do LIMPEZA-4 não pegou isto: ela só olhou as 84 magias que o motor JÁ executava, e
     * cura não era uma delas — ponto cego estrutural daquela varredura.
     */
    val curaPvPorEnergia: Int = 0,
    /** Teto de PV que a magia cura numa operação (Superficial 3, Profunda 8). 0 = sem teto próprio. */
    val curaMaxPv: Int = 0,
    /** true = restaura TODOS os PV perdidos (Cura Superior, custo fixo 20). Ignora os campos acima. */
    val curaTotal: Boolean = false,

    // ── efeito "dano" ──
    /** Dado por unidade de energia (ex.: "1d-1" no Relâmpago, "1d+1" no Toque Chocante). */
    val danoPorEnergia: String? = null,
    /** Quantos pontos de energia compram 1 "unidade" de dado (1 no Relâmpago, 2 na Concussão). */
    val energiaPorDado: Int = 1,
    /**
     * true quando o dano NÃO escala com a energia investida — a energia compra outra coisa (alcance
     * no Chicote de Relâmpago, raio/duração na Nuvem de Faíscas) ou é custo fixo (Géiser = 3d sempre).
     * Sem isto o Géiser (custo 5) sairia como 15d.
     */
    val danoFixo: Boolean = false,
    /** "quei" (queimadura) | "cont" | "projecao" | "corte"… (default contusão no motor). */
    val tipoDano: String? = null,
    /** null = RD normal; "ignora" = armadura não protege (Toque Chocante); "metal_rd_1" = metal vira RD 1 (aprox.: mantém RD). */
    val armadura: String? = null,
    /** Como acerta: "projetil" | "toque" | "feixe" (DX−4) | "area" | "auto". Complementa a `classe`. */
    val entrega: String? = null,
    /**
     * Lote MEC-13: restrição de ALVO VÁLIDO. `"objeto"` = só objetos inanimados — Desintegrar ("afeta
     * apenas objetos inanimados"), Enfraquecer ("funciona apenas em itens inanimados"), Fender ("faz
     * buracos em objetos inanimados, paredes"), Explodir. Sem este campo o motor deixava **desintegrar
     * um NPC vivo**, o que o livro proíbe.
     *
     * ⚠️ NÃO existe valor para "morto-vivo" de propósito: a Espantar Zumbi só afeta o que foi animado
     * PELA MÁGICA ZUMBI, e o motor não tem esse conceito (o bestiário nem marca morto-vivo — o
     * esqueleto só diz isso na prosa). Fingir seria meia-regra errada; fica narrado.
     */
    val alvoValido: String? = null,
    /**
     * Lote MEC-14: EXPLOSÃO com decaimento por distância. Regra (Bola de Fogo Explosiva, Relâmpago
     * Explosivo): *"O alvo e qualquer um mais próximo do alvo que um metro recebe dano total. Os mais
     * afastados **dividem o dano em três vezes a distância em metros** (arredondado para baixo)."*
     *
     * Guarda o divisor por metro (3 nas magias do livro). 0 = não é explosão (todos na área levam o
     * dano cheio, que é o certo para chuva/nuvem — dano ambiental, não onda de choque).
     *
     * ⚠️ A Concussão NÃO entra aqui: o "raio de 10 metros" dela é do ATORDOAMENTO (`condicaoRaioM`),
     * não decaimento de dano — a descrição deixa claro, apesar de o auditor tê-la agrupado junto.
     */
    val explosaoDivisorPorMetro: Int = 0,
    /**
     * Lote MEC-15: distâncias do Projétil, em metros (MB, "Distância" e "Metade do Dano (1/2D)").
     * `alcanceMeioDano` = 1/2D: *"Se o alvo estiver a uma distância **maior ou igual** à distância
     * 1/2D, divida o dano básico por 2, arredondando para baixo."* (é ≥, não >).
     * `alcanceMaximo` = Max: *"O alvo não pode estar a uma distância maior que Distância Max; 1/2D
     * afeta apenas o dano."*
     * 0 nos dois = sem limite (o catálogo traz "n/a"/"n/d" em algumas).
     */
    val alcanceMeioDano: Int = 0,
    val alcanceMaximo: Int = 0,
    /**
     * Lote MEC-17: por quantos SEGUNDOS a condição dura (Cegar = 10 seg; Lampejo cega 3 seg).
     * 0 = sem prazo — a condição sai pela regra dela (o Sono acaba quando acordam o alvo; a paralisia
     * do Toque Congelante, quando ele rompe o gelo). Sem este campo a condição era ETERNA: um goblin
     * cegado ficava cego a luta inteira.
     */
    val condicaoDuracaoSeg: Int = 0,
    /**
     * Lote MEC-18: bônus ao atributo de resistência a cada N pontos de RD (Jato de Som: *"A RD
     * atribui um bônus de +1 ao HT efetivo do alvo para cada cinco pontos de RD"*). 0 = sem bônus.
     */
    val condicaoRdBonusPor: Int = 0,
    /**
     * Lote MEC-18: segundos de condição POR PONTO DE ENERGIA investido (Jato de Areia/Lama/Neve/
     * Vapor: *"cada ponto de energia na mágica o cega por um segundo"*). Soma-se ao prazo fixo
     * (`condicaoDuracaoSeg`), que nestas magias é 0 — a duração é toda escalada.
     */
    val condicaoDuracaoSegPorEnergia: Int = 0,
    /**
     * Lote MEC-19: a condição não sai por tempo nem por HT — a vítima ESCAPA testando um atributo.
     * Toque Congelante: *"não pode tomar nenhuma ação até que ele rompa o gelo com um teste de ST
     * bem-sucedido com uma penalidade de -1 por cada 0,5cm de gelo"*, e *"Custo: 2 por 0,5cm"* —
     * logo −1 a cada [condicaoEscapeEnergiaPorPonto] pontos de energia investidos.
     */
    val condicaoEscapeAtributo: String? = null,
    val condicaoEscapeEnergiaPorPonto: Int = 0,
    /**
     * Lote MEC-22: dano RECORRENTE por turno, com teste da vítima a cada turno.
     *
     * Morte Candente (literal): *"Toda vez, a vítima deve fazer um teste de HT; em uma falha
     * (crítica ou não), ele recebe 1d-1 de dano por fogo. Em um sucesso, ele não leva dano naquele
     * turno; em um sucesso decisivo, a mágica está quebrada."* — *"Nem RD nem Resistência ao Fogo
     * protegem contra esta lesão!"*
     *
     * Morte Putrefata é igual, mas *"6 pontos em uma falha crítica"*.
     *
     * [danoPorTurnoExpr] é FIXO por turno — **não escala com a energia** (a energia paga o custo de
     * operar/manter, não o dano). Por isso é campo próprio, e não o `danoPorEnergia`.
     */
    val danoPorTurnoExpr: String? = null,
    /** Atributo testado pela vítima a cada turno ("HT"). */
    val danoPorTurnoTeste: String? = null,
    /** Dano fixo quando a vítima tira FALHA CRÍTICA (6 na Morte Putrefata; 0 = usa o normal). */
    val danoPorTurnoCriticoFixo: Int = 0,
    /** Sucesso DECISIVO da vítima quebra a mágica. */
    val quebraEmSucessoDecisivo: Boolean = false,

    // ── condição embutida (rider no dano ou standalone) ──
    /** Condição imposta ("atordoado", "cego"…). */
    val condicao: String? = null,
    /** Teste para resistir à condição: "HT" | "HT-3" | "HT_por_pv" (Relâmpago: −1 por 2 PV). */
    val condicaoResistencia: String? = null,
    /** Raio (m) em que a condição se espalha (Concussão = 10). 0 = só o alvo. */
    val condicaoRaioM: Int = 0,

    // ── efeito "buff" (rastreado como magia ativa; bônus numérico quando houver) ──
    /** Rótulo curto do efeito, para o feed e para o Narrador ("Pele de Crocodilo RD 4"). */
    val buffRotulo: String? = null,
    /** Bônus de dano numa arma, aplicado DEPOIS de penetrar a RD (Arma de Relâmpago = +2). */
    val buffDanoArma: Int = 0,
    /**
     * Em que arma o [buffDanoArma] vale: "cac" (Arma Flamejante é só corpo a corpo) ou "distancia"
     * (Projéteis Flamejantes). null = qualquer. Sem isto o +2 do gume vazaria para o arco.
     */
    val buffArmaTipo: String? = null,
    /**
     * Lote MEC-2 — os números que faltavam. Desenho tirado dos 93 buffs JÁ curados, não de palpite:
     * quase todo buff do livro é "N de bônus POR nível, X de energia por nível, teto de M níveis"
     * (Força = 2 energia por +1 ST, máx 5) ou um valor fixo (Pele de Crocodilo RD 4; Voo = Desloc 10).
     *
     * Níveis = if (buffEnergiaPorNivel > 0) min(energia / buffEnergiaPorNivel, buffMaxNiveis) else 1.
     * Cada campo abaixo é o valor POR NÍVEL — o motor multiplica.
     */
    /** Energia que compra 1 nível do buff (0 = não escala: vale 1 nível fixo). */
    val buffEnergiaPorNivel: Int = 0,
    /** Teto de níveis (Força/Graça/Vigor = 5; Apressar = 3; Nublar = 5). 0 = sem teto. */
    val buffMaxNiveis: Int = 0,
    /** RD concedida por nível (Pele de Crocodilo 4, Proteger Animal 5). */
    val buffRd: Int = 0,
    /** Bônus de Esquiva por nível (Apressar +1/nível). */
    val buffEsquiva: Int = 0,
    /**
     * Lote MEC-4 — Bônus de Defesa por nível (Escudo = +1 BD por 2 de energia, teto +4). BD soma em
     * TODAS as defesas ativas (esquiva, aparar, bloquear), como o BD do escudo real (MB p.374).
     * Sem este campo, Escudo/Bloquear — as magias de proteção mais usadas — ficariam inertes.
     */
    val buffBd: Int = 0,
    /** Atributo alterado: "ST" | "DX" | "HT" (IQ não entra no motor de combate → narrado). */
    val buffAtributo: String? = null,
    /** Valor do atributo por nível — NEGATIVO nos debuffs (Debilitar = −1 ST/nível). */
    val buffAtributoValor: Int = 0,
    /** Delta de Deslocamento por nível (Apressar +1/nível). */
    val buffDeslocamento: Int = 0,
    /** Deslocamento ABSOLUTO — substitui o do combatente (Voo = 10; Voo do Falcão = 40). 0 = não usa. */
    val buffDeslocamentoFixo: Int = 0,
    /** Penalidade por nível ao NH de QUEM ATACA o alvo (Nublar = −1/nível, máx −5). Valor positivo. */
    val buffPenalidadeAtacantes: Int = 0,
    /**
     * Lote MEC-6 — buff de UM ÚNICO USO: duração "Instant.", vale para um único teste/ação curta
     * (Aumentar Força: "+1 ST por energia, máx 5, só um teste"). Estes NÃO são mágicas ativas: sem
     * este campo, `registrarSeMagiaAtiva` os descartava por não serem temporários — o herói pagava o
     * PF e não acontecia NADA.
     *
     * O motor aplica e consome ao FIM da próxima ação do dono (aproximação honesta de "um teste":
     * o turno da conjuração não conta, senão o buff morreria antes de poder ser usado).
     */
    val buffUmUnicoUso: Boolean = false,

    // ── notas para o Narrador (ambiente/controle/utilidade: o motor tagueia, o Mestre descreve) ──
    val notas: String? = null,
)

/**
 * Lote MEC-2: os deltas CONCRETOS que um buff aplicou num combatente. Guardado na mágica ativa para
 * que a expiração reverta EXATAMENTE o que entrou (e não um recálculo, que daria drift se a energia,
 * a postura ou outro buff mudarem no meio).
 */
data class BuffAplicado(
    val alvoId: String,
    val rotulo: String = "",
    val rd: Int = 0,
    val esquiva: Int = 0,
    /** Bônus de Defesa: soma em esquiva, aparar E bloquear (Escudo). */
    val bd: Int = 0,
    val st: Int = 0,
    val dx: Int = 0,
    val ht: Int = 0,
    val deslocamento: Int = 0,
    /** Deslocamento absoluto imposto (Voo). null = não mexeu. */
    val deslocamentoFixo: Int? = null,
    val danoArma: Int = 0,
    /** "cac" | "distancia" | null (qualquer) — em que arma o [danoArma] vale. */
    val armaTipo: String? = null,
    val penalidadeAtacantes: Int = 0,
    /** Lote MEC-6: vale para UM único teste/ação — some ao fim da próxima ação do dono. */
    val umUnicoUso: Boolean = false,
) {
    /**
     * Lote MEC-6: true depois que o dono FECHOU o turno em que conjurou. O turno da conjuração não
     * conta — senão o buff de um uso expiraria antes de o herói poder usá-lo.
     */
    var estreou: Boolean = false
    /** true se o bônus de dano vale para um ataque com este alcance. */
    fun danoArmaVale(aDistancia: Boolean): Boolean = when (armaTipo) {
        "cac" -> !aDistancia
        "distancia" -> aDistancia
        else -> true
    }
    /** true se nada numérico foi aplicado — o efeito é só narrado (Corpo de Água, Ambidestria). */
    val soNarrado: Boolean get() = rd == 0 && esquiva == 0 && bd == 0 && st == 0 && dx == 0 && ht == 0 &&
        deslocamento == 0 && deslocamentoFixo == null && danoArma == 0 && penalidadeAtacantes == 0
}

object MagicMechanics {

    /** true se a mágica tem dano estruturado que o motor aplica automaticamente. */
    fun temDanoEstruturado(m: MagiaMecanica?): Boolean = m?.efeito == "dano" && m.danoPorEnergia != null

    /**
     * Lote MEC-13: true se a mágica NÃO pode ser lançada em criatura (só serve para objeto inanimado).
     * O motor recusa o alvo em vez de aplicar dano num ser vivo.
     */
    fun soAfetaObjeto(m: MagiaMecanica?): Boolean = m?.alvoValido == "objeto"

    /**
     * Lote MEC-14: dano da explosão a [distanciaM] metros do centro.
     * Até 1m → dano cheio; além disso, dividido por (divisor × distância), arredondando para baixo.
     */
    /** Lote MEC-15: o alvo está além do Máx? (Max 0 = sem limite.) */
    fun foraDoAlcanceMaximo(m: MagiaMecanica?, distanciaM: Int): Boolean {
        val max = m?.alcanceMaximo ?: 0
        return max > 0 && distanciaM > max
    }

    /** Lote MEC-15: a partir de 1/2D (inclusive) o dano básico cai pela metade, arredondando p/ baixo. */
    fun aplicarMeioDano(dano: Int, m: MagiaMecanica?, distanciaM: Int): Int {
        val meia = m?.alcanceMeioDano ?: 0
        return if (meia > 0 && distanciaM >= meia) dano / 2 else dano
    }

    fun danoDaExplosao(danoCheio: Int, distanciaM: Int, divisorPorMetro: Int): Int {
        if (divisorPorMetro <= 0 || distanciaM <= 1) return danoCheio
        return danoCheio / (divisorPorMetro * distanciaM)
    }

    /** true se a mágica CURA PV de forma estruturada (Lote MEC-10). */
    fun temCuraEstruturada(m: MagiaMecanica?): Boolean =
        m?.efeito == "cura" && (m.curaTotal || m.curaPvPorEnergia > 0)

    /**
     * PV que a magia restaura com [energia] investida, respeitando o teto da própria magia e o que o
     * alvo REALMENTE perdeu (curar 8 em quem perdeu 2 restaura 2 — não estoura o PV máximo).
     */
    fun pvCurados(m: MagiaMecanica, energia: Int, pvPerdidos: Int): Int {
        if (pvPerdidos <= 0) return 0
        if (m.curaTotal) return pvPerdidos
        val porEnergia = m.curaPvPorEnergia.coerceAtLeast(0)
        if (porEnergia == 0) return 0
        val bruto = porEnergia * energia.coerceAtLeast(1)
        val comTeto = if (m.curaMaxPv > 0) minOf(bruto, m.curaMaxPv) else bruto
        return minOf(comTeto, pvPerdidos)
    }

    /** Energia que ainda compra cura (acima disso o teto da magia trava). Ex.: Profunda 8/2 = 4. */
    fun tetoEnergiaCura(m: MagiaMecanica): Int {
        if (m.curaTotal || m.curaPvPorEnergia <= 0) return 1
        return if (m.curaMaxPv > 0) (m.curaMaxPv / m.curaPvPorEnergia).coerceAtLeast(1) else 1
    }

    /** true se o buff tem NÚMERO que o motor aplica (senão é narrado — regra de ouro). */
    fun temBuffEstruturado(m: MagiaMecanica?): Boolean = m != null && m.efeito == "buff" &&
        (m.buffRd != 0 || m.buffEsquiva != 0 || m.buffBd != 0 || m.buffAtributoValor != 0 || m.buffDeslocamento != 0 ||
         m.buffDeslocamentoFixo != 0 || m.buffDanoArma != 0 || m.buffPenalidadeAtacantes != 0)

    /**
     * Lote MEC-7: quanto de energia ainda COMPRA efeito, e o que ela compra em português.
     * null = o efeito não escala (custo fixo → o jogador não tem o que escolher).
     *
     * Vive aqui, no domínio puro, porque é REGRA (o teto vem do livro) e precisa de teste: o mesmo
     * erro de deixar isto privado na UI foi o que manteve 325 mágicas com a duração errada.
     *
     * O teto é `energiaPorNivel × maxNiveis` — acima disso o jogador só queimaria fadiga à toa,
     * porque a regra trava o efeito no teto de níveis (Escudo: 2 PF por +1 de BD, máx +4 → 8 PF).
     */
    data class EscalaDeEnergia(val energiaMax: Int, val dica: String)

    fun escalaDeEnergia(m: MagiaMecanica?): EscalaDeEnergia? {
        if (m == null || m.efeito != "buff") return null
        val porNivel = m.buffEnergiaPorNivel
        val maxNiveis = m.buffMaxNiveis
        if (porNivel <= 0 || maxNiveis <= 0) return null
        val (porUm, oQue) = when {
            m.buffBd != 0 -> m.buffBd to "de Defesa"
            m.buffRd != 0 -> m.buffRd to "de RD"
            m.buffAtributoValor != 0 -> m.buffAtributoValor to "de ${m.buffAtributo ?: "atributo"}"
            m.buffPenalidadeAtacantes != 0 -> -m.buffPenalidadeAtacantes to "para quem o atacar"
            m.buffEsquiva != 0 -> m.buffEsquiva to "de Esquiva"
            m.buffDeslocamento != 0 -> m.buffDeslocamento to "de Deslocamento"
            else -> return null
        }
        fun sinal(n: Int) = if (n >= 0) "+$n" else "$n"
        return EscalaDeEnergia(
            energiaMax = porNivel * maxNiveis,
            dica = "cada $porNivel PF = ${sinal(porUm)} $oQue (até ${sinal(porUm * maxNiveis)})",
        )
    }

    /**
     * Níveis efetivos do buff dada a energia investida: `energia / buffEnergiaPorNivel`, com teto em
     * `buffMaxNiveis`. Sem escala por energia (buffEnergiaPorNivel = 0) o buff vale 1 nível fixo.
     * Piso 1 — quem conjurou pagou, leva pelo menos 1 nível.
     */
    fun niveisDoBuff(m: MagiaMecanica, energia: Int): Int {
        if (m.buffEnergiaPorNivel <= 0) return 1
        val n = energia / m.buffEnergiaPorNivel
        val teto = if (m.buffMaxNiveis > 0) m.buffMaxNiveis else Int.MAX_VALUE
        return n.coerceIn(1, teto)
    }

    /**
     * Converte a `mecanica` curada + energia investida nos deltas concretos a aplicar no alvo.
     * Cada campo é "por nível" no catálogo; aqui multiplica pelos níveis comprados com a energia.
     */
    fun calcularBuff(m: MagiaMecanica, energia: Int, alvoId: String): BuffAplicado {
        val n = niveisDoBuff(m, energia)
        val atr = m.buffAtributoValor * n
        return BuffAplicado(
            alvoId = alvoId,
            rotulo = m.buffRotulo ?: "",
            rd = m.buffRd * n,
            esquiva = m.buffEsquiva * n,
            bd = m.buffBd * n,
            st = if (m.buffAtributo.equals("ST", true)) atr else 0,
            dx = if (m.buffAtributo.equals("DX", true)) atr else 0,
            ht = if (m.buffAtributo.equals("HT", true)) atr else 0,
            deslocamento = m.buffDeslocamento * n,
            deslocamentoFixo = if (m.buffDeslocamentoFixo > 0) m.buffDeslocamentoFixo else null,
            danoArma = m.buffDanoArma * n,
            armaTipo = m.buffArmaTipo,
            penalidadeAtacantes = m.buffPenalidadeAtacantes * n,
            umUnicoUso = m.buffUmUnicoUso,
        )
    }

    /**
     * Expande o dano por energia para a expressão total, escalando pela energia investida.
     * Ex.: "1d-1" por 1 energia, energia 3 → "3d-3"; "1d" por 2 energia, energia 4 → "2d".
     * Regras: dados = (energia / energiaPorDado) coerçado a ≥1; o modificador escala com a contagem.
     *
     * `danoFixo` trava a contagem em 1 (Géiser = 3d sempre, custe o que custar).
     * Dano em PONTOS (Nuvem de Faíscas = "1" ponto/seg) vira "0d+N" — o rolador exige `<n>d` e
     * devolveria 0 para um "1" pelado.
     */
    fun expandirDano(danoPorEnergia: String, energia: Int, energiaPorDado: Int, danoFixo: Boolean = false): String {
        val n = if (danoFixo) 1 else (energia / energiaPorDado.coerceAtLeast(1)).coerceAtLeast(1)
        val txt = danoPorEnergia.replace(" ", "").lowercase()
        val m = Regex("""(\d*)d([+-]\d+)?""").find(txt)
            ?: return txt.toIntOrNull()?.let { "0d+${it * n}" } ?: "${n}d"
        val dadosBase = m.groupValues[1].toIntOrNull() ?: 1
        val modBase = m.groupValues[2].toIntOrNull() ?: 0
        val dados = dadosBase * n
        val mod = modBase * n
        return "${dados}d" + when {
            mod > 0 -> "+$mod"
            mod < 0 -> "$mod"
            else -> ""
        }
    }

    /** Penalidade da condição imposta pela mágica, dado o dano sofrido (ex.: Relâmpago −1 por 2 PV). */
    fun penalidadeCondicaoPorPv(resistencia: String?, pvSofridos: Int): Int = when {
        resistencia == "HT_por_pv" -> -(pvSofridos / 2)
        resistencia != null && resistencia.startsWith("HT-") -> resistencia.removePrefix("HT-").toIntOrNull()?.let { -it } ?: 0
        else -> 0
    }

    /**
     * Lote MEC-18: a magia impõe a condição com teste PRÓPRIO do alvo (e não pela classe R-XXX)?
     * É o caso do Jato de Som, cuja classe é "Comum" — sem isto ele atordoava sem teste nenhum.
     */
    /** Lote MEC-18: prazo total da condição = fixo + (por energia × energia investida). */
    fun duracaoCondicaoSeg(m: MagiaMecanica?, energiaInvestida: Int): Int {
        if (m == null) return 0
        return m.condicaoDuracaoSeg + m.condicaoDuracaoSegPorEnergia * energiaInvestida.coerceAtLeast(0)
    }

    /** Lote MEC-22: esta magia fere a vítima a cada turno, contra um teste dela? */
    fun temTiquePorTurno(m: MagiaMecanica?): Boolean =
        m?.danoPorTurnoExpr != null && !m.danoPorTurnoTeste.isNullOrBlank()

    /** Lote MEC-19: penalidade no teste de fuga, pela energia investida (−1 a cada N pontos). */
    fun penalidadeEscapeCondicao(m: MagiaMecanica?, energiaInvestida: Int): Int {
        val porPonto = m?.condicaoEscapeEnergiaPorPonto ?: 0
        if (porPonto <= 0) return 0
        return -(energiaInvestida.coerceAtLeast(0) / porPonto)
    }

    fun temTesteProprioDeCondicao(m: MagiaMecanica?): Boolean =
        m?.efeito == "condicao" && !m.condicaoResistencia.isNullOrBlank()

    /**
     * Lote MEC-18: valor efetivo do teste de resistência à condição.
     * `HT_menos_energia` (Jato de Som): *"teste contra seu HT MENOS o custo de energia da mágica"*,
     * mais *"+1 ao HT efetivo a cada cinco pontos de RD"* quando `condicaoRdBonusPor` estiver setado.
     */
    fun resistenciaEfetivaDaCondicao(m: MagiaMecanica, atributoBase: Int, energiaGasta: Int, rd: Int): Int {
        val porEnergia = if (m.condicaoResistencia == "HT_menos_energia") -energiaGasta else 0
        val fixo = m.condicaoResistencia?.takeIf { it.startsWith("HT-") }
            ?.removePrefix("HT-")?.toIntOrNull()?.let { -it } ?: 0
        val bonusRd = if (m.condicaoRdBonusPor > 0) rd / m.condicaoRdBonusPor else 0
        return atributoBase + porEnergia + fixo + bonusRd
    }
}
