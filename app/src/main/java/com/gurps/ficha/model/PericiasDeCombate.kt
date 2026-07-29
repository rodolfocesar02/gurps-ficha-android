package com.gurps.ficha.model

/**
 * As perícias de combate, separadas em **corpo a corpo** e **à distância**.
 *
 * ## Por que a separação existe
 *
 * A lista sempre foi um `setOf` único dentro de `Personagem.kt`, com um
 * comentário `// Pericias de Ataque a Distancia` no meio. O comentário separava
 * para **humano ler** — o código não tinha como perguntar "este ataque é à
 * distância?".
 *
 * O Lote MIRA-2 precisa dessa pergunta: a linha de distância do alvo só faz
 * sentido para arco, besta e arma de fogo. Numa Espada, perguntar "a que
 * distância?" é ruído.
 *
 * [PERICIAS_COMBATE] continua existindo como a **união** das duas, com o mesmo
 * conteúdo de antes, para que Apara, Bloqueio e o resto não mudem em nada.
 *
 * Mora em arquivo próprio porque `Personagem.kt` já passa de 1200 linhas.
 */

/** Perícias de combate **corpo a corpo** (e as duas de defesa: Escudo e Capa). */
val PERICIAS_COMBATE_CORPO_A_CORPO = setOf(
    // IDs atuais do dataset
    "adaga_de_esgrima",
    "armas_de_haste",
    "bastao",
    "boxe",
    "briga",
    "capa",
    "carate",
    "caratê",
    "chicote",
    "escudo",
    "espada_curta",
    "espada_de_duas_maos",
    "espada_de_energia",
    "espada_de_lamina_larga",
    "faca",
    "jittesai",
    "judo",
    "judô",
    "kusari",
    "lanca",
    "lanca_de_justa",
    "luta_grecoromana",
    "luta_greco_romana",
    "macamachado",
    "macamachado_de_duas_maos",
    "mangual",
    "mangual_de_duas_maos",
    "sumo",
    "sumô",
    "luta_de_sumo",
    "garrote",
    // Aliases legados para fichas antigas
    "adaga",
    "alabarda",
    "armas_de_corrente",
    "armas_de_duas_maos",
    "cajado",
    "espada_larga",
    "kama",
    "karate",
    "karatê",
    "kusarigama",
    "maca",
    "machado_de_duas_maos",
    "machado_ou_machadinha",
    "nunchaku",
    "rapieira",
    "sabre",
    "sai",
    "tonfa",
    "wrestling"
)

/**
 * Perícias de **ataque à distância** — as que ganham a linha de distância do
 * alvo no diálogo de Mira.
 *
 * ⚠️ Três que estavam no bloco de corpo a corpo por engano de arrumação e vieram
 * para cá, porque o livro é claro sobre elas: **Sopro** (zarabatana),
 * **Lançador de Lanças** (propulsor tipo atlatl) e **Projetor de Pressão**.
 * Todas atiram alguma coisa.
 */
val PERICIAS_COMBATE_DISTANCIA = setOf(
    "arco",
    "arcos",
    "besta",
    "zarabatana",
    "sopro",
    "funda",
    "lancador_de_lancas",
    "projetor_de_pressao",
    "armas_de_fogo_nt",
    "armas_de_feixe_nt",
    "artilharia_nt",
    "artilheiro_nt",
    "projetor_de_liquidos_nt",
    "bolas",
    "laco",
    "rede",
    "arma_de_fogo_pistola",
    "arma_de_fogo_fuzil",
    "arma_de_fogo_espingarda",
    "arma_de_fogo_submetralhadora",
    "arremesso",
    "facas_de_arremesso",
    "shuriken",
    "pericia_de_arma_de_fogo",
    "pericia_de_arco",
    "pericia_de_besta",
    // Ataque Inato é à distância POR PADRÃO (MB p.46) — só vira corpo a corpo
    // com a limitação Ataque Corpo a Corpo. Como o app não guarda essa
    // limitação, fica no grupo do padrão: oferecer a distância e o jogador
    // ignora é bem melhor que esconder de quem precisa.
    "ataque_inato"
)

/**
 * Perícias de combate para Apara/Bloqueio — a união das duas listas.
 *
 * Mesmo conteúdo de sempre. Continua sendo a lista que o resto do app usa.
 */
val PERICIAS_COMBATE = PERICIAS_COMBATE_CORPO_A_CORPO + PERICIAS_COMBATE_DISTANCIA
