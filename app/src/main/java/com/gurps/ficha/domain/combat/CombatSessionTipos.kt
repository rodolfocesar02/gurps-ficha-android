package com.gurps.ficha.domain.combat

/**
 * Lote MOTOR-5: os TIPOS de fronteira do `CombatSession` (o que o controller passa para dentro e o
 * que sai como resultado), separados do motor. São puro dado — sem lógica — e o `CombatSession`
 * continua no mesmo pacote, então quem usa `HeroiPerfilCombate`/`AtaqueHeroi`/`DefesaHeroi` não muda
 * nada. Corte de risco zero: só encolhe o arquivo do motor.
 */

/** Defesas do herói — o controller extrai da ficha; mantém a sessão pura. (Lote 368: só defesa.) */
data class HeroiPerfilCombate(
    val esquiva: Int,
    val apara: Int? = null,
    val bloqueio: Int? = null,
    val ht: Int = 10,
    val rd: Int = 0,
    /** Lote 380: BD do escudo JÁ embutido em esquiva/apara/bloqueio — guardado à parte para poder
     *  REMOVÊ-LO quando o escudo não conta (arma de 2 mãos sem mão livre, ou ataque de arma de fogo; MB p.375). */
    val bonusEscudo: Int = 0,
    /** Lote 381: Modificador de Tamanho (MT) do herói — somado ao acerto quando um NPC atira NELE (MB p.549). */
    val modificadorTamanho: Int = 0,
    /** Lote 386: ST e DX do herói — para as Disputas Rápidas de luta agarrada (Agarrar/Derrubar, MB p.370/371). */
    val st: Int = 10,
    val dx: Int = 10,
    /**
     * Lote MEC-45: NH da perícia **Ataque Inato**, se o herói a tiver na ficha. É o teste correto
     * para acertar com projétil mágico (Magia p.12). `null` = não tem a perícia → o motor cai na DX,
     * que era a aproximação usada até aqui.
     */
    val nhAtaqueInato: Int? = null,
    /** Lote 395: Vontade — teste para não perder a pontaria (Apontar) ao ser ferido (MB p.364). */
    val vontade: Int = 10,
    /** Lote 410: dano por GdP (golpe de ponta/empurrão) do herói, p/ o Empurrão (MB p.371). */
    val danoGdP: String = "1d-2",
    /** Lote 414: NH em Acrobacia (null se o herói não tem) — p/ a Esquiva Acrobática (MB p.377). */
    val acrobacia: Int? = null
)

/**
 * Um ataque utilizável do herói (Lote 368): arma empunhada + perícia. O jogador ESCOLHE qual usar.
 * @param aDistancia true para arma de fogo/arremesso (defesa do alvo só por Esquiva; sofre penal. de distância).
 * @param precisao Acc da arma (bônus ao Apontar — usado no lote de manobras).
 */
/** Comportamento de Aparar da arma (coluna Aparar, MB p.270): normal / esgrima (E) / desbalanceada (D) / não. */
enum class ApararTipo { NORMAL, ESGRIMA, DESBALANCEADA, NAO }

data class AtaqueHeroi(
    val rotulo: String,          // ex.: "Revólver (Pistola)"
    val nh: Int,
    val danoExpr: String,        // expressão já resolvida por ST, ex.: "2d-1 pa+"
    val tipo: DanoTipo,
    val aDistancia: Boolean = false,
    val alcance: Int = 1,        // à distância: alcance Máximo (m). Além disso, não acerta.
    val precisao: Int = 0,       // Acc — bônus ao Apontar (MB p.364)
    val meioDano: Int = 0,       // à distância: 1/2D (m). A partir daí, dano pela metade. 0 = sempre cheio.
    val magnitude: Int = 0,      // Bulk — penalidade no Avançar e Atacar à distância (MB p.271)
    val apararTipo: ApararTipo = ApararTipo.NORMAL,
    val cadenciaTiro: Int = 1,   // CdT/RoF — tiros por ataque (MB p.373). >=2 permite rajada.
    val recuo: Int = 1,          // Rco/Rcl — controla quantos tiros da rajada acertam (MB p.374).
    val duasMaos: Boolean = false, // Lote 380: ocupa as duas mãos → sem mão livre p/ o escudo (MB p.375).
    val desarmado: Boolean = false, // Lote 384: ataque desarmado (usa a Tabela de Erro Crítico desarmada).
    val aparaMarcial: Boolean = false, // Lote 391: apara desarmada por Caratê/Judô → sem o −3 vs armas (MB p.376).
    val armaDeFogo: Boolean = false, // Lote 395: arma de fogo → pode "firmar" ao Apontar (+1 Acc, MB p.364).
    val stMinimo: Int = 0, // Lote 398: ST mínima da arma — desbalanceada fica despreparada se ST < 1,5× isto (MB p.270).
    val temPericia: Boolean = true
)

/** Defesa escolhida pelo jogador no card "Defenda-se!" (tipo + valor final + 3d6 rolado). */
data class DefesaHeroi(
    val tipo: CombatResolver.TipoDefesa,
    val valorFinal: Int,
    val soma: Int,
    val recuo: Boolean = false, // Lote 389: a defesa veio com Retirada (recuar um passo) — marca 1×/turno (MB p.377)
    val jogarSeAoChao: Boolean = false, // Lote 404: Esquiva e Queda — após defender, o herói fica deitado (MB p.377)
    val acrobatica: Boolean = false // Lote 414: Esquiva Acrobática — testa Acrobacia (+2/−2) antes da esquiva (MB p.377)
)

enum class ResultadoCombate { VITORIA, DERROTA, FUGA }
