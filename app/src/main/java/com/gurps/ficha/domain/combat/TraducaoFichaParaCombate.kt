package com.gurps.ficha.domain.combat

import com.gurps.ficha.domain.filters.CatalogFilters
import com.gurps.ficha.model.ArmaCatalogoItem
import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.MagiaDefinicao
import com.gurps.ficha.model.MagiaSelecionada
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.PericiaSelecionada
import com.gurps.ficha.model.TipoEquipamento

/**
 * Lote REFACTOR-2: TRADUÇÃO ficha/catálogo → objetos do motor de combate.
 *
 * Estas funções viviam dentro do `SagaCombatController`, que depende do `FichaViewModel`
 * (`AndroidViewModel`) e portanto **não roda na JVM** — logo nada delas entrava na suíte de testes.
 * Mas o que elas fazem é **pura tradução**: recebem um `Personagem`/`Equipamento`/`MagiaDefinicao` e
 * devolvem um `HeroiPerfilCombate`/`AtaqueHeroi`/String. Sem Android, sem Compose, sem estado
 * observável. Movidas para cá, viram testáveis — e o controller só chama e obedece.
 *
 * Nada de comportamento mudou: o código é o mesmo, só de lugar.
 */
object TraducaoFichaParaCombate {

    // ── Magia: catálogo × cópia da ficha ───────────────────────────────────────────────────────

    /**
     * A CLASSE efetiva da mágica. **O catálogo manda**; a cópia da ficha é só fallback (magia
     * caseira ou catálogo ausente).
     *
     * Foi a causa-raiz do MEC-42: a `MagiaSelecionada` guarda uma cópia velha de `classe`, então
     * consertar o catálogo não bastava — a Bola de Relâmpagos continuava "Comum" porque a ficha
     * ainda dizia isso. Regra: def do catálogo primeiro, ficha só se ele faltar.
     */
    fun classeDaMagia(def: MagiaDefinicao?, m: MagiaSelecionada): String? =
        def?.classe?.takeIf { it.isNotBlank() } ?: m.classe

    fun energiaDaMagia(def: MagiaDefinicao?, m: MagiaSelecionada): String? =
        def?.energia?.takeIf { it.isNotBlank() } ?: m.energia

    /**
     * Chave normalizada do NOME de uma mágica (MEC-43): sem acento, sem caixa, só letras e números.
     * Usada para achar a definição no catálogo quando a busca por `definicaoId` falha (fichas antigas
     * com id vazio ou de esquema anterior). Extraída para cá porque é pura e agora tem teste.
     */
    fun chaveNome(s: String?): String = (s ?: "").lowercase()
        .replace(Regex("[àáâãä]"), "a").replace(Regex("[éêë]"), "e").replace(Regex("[íî]"), "i")
        .replace(Regex("[óôõö]"), "o").replace(Regex("[úû]"), "u").replace("ç", "c")
        .replace(Regex("[^a-z0-9]"), "")

    // ── Herói: ficha → perfil e ataques ─────────────────────────────────────────────────────────

    /** RD do herói: maior RD entre as armaduras equipadas (aproximação de torso). Confiscada não conta. */
    fun rdHeroi(p: Personagem): Int = p.equipamentos
        .filter { it.tipo == TipoEquipamento.ARMADURA && !it.confiscado }
        .mapNotNull { it.rdArmaduraExibicao()?.let { s -> Regex("\\d+").find(s)?.value?.toIntOrNull() } }
        .maxOrNull() ?: 0

    fun construirPerfilHeroi(p: Personagem): HeroiPerfilCombate = HeroiPerfilCombate(
        esquiva = p.defesasAtivas.calcularEsquiva(p),
        apara = p.defesasAtivas.calcularApara(p),
        bloqueio = p.defesasAtivas.calcularBloqueio(p),
        ht = p.ht,
        rd = rdHeroi(p),
        // BD do escudo já embutido acima; guardado à parte p/ removê-lo quando não vale (Lote 380, MB p.375).
        bonusEscudo = p.defesasAtivas.getBonusEscudo(p),
        modificadorTamanho = p.modificadorTamanho,
        st = p.forca, dx = p.dx,
        vontade = p.vontade,
        danoGdP = p.danoGdP,
        acrobacia = p.periciasTotais.firstOrNull {
            CatalogFilters.normalizarBusca(it.definicaoId).removePrefix("racial_") == "acrobacia"
        }?.calcularNivel(p),
        // Lote MEC-45: Ataque Inato é a perícia CORRETA para acertar com projétil mágico (Magia p.12).
        nhAtaqueInato = p.periciasTotais.firstOrNull {
            CatalogFilters.normalizarBusca(it.definicaoId).removePrefix("racial_") == "ataqueinato" ||
                CatalogFilters.normalizarBusca(it.nome) == "ataqueinato"
        }?.calcularNivel(p),
    )

    /**
     * Ataques utilizáveis (Lote 368): cada arma EQUIPADA (corpo-a-corpo e à distância/fogo) com sua
     * perícia, NH, dano por ST e tipo; mais o desarmado como último recurso.
     */
    fun construirAtaques(p: Personagem, agarrado: Boolean = false): List<AtaqueHeroi> {
        val out = mutableListOf<AtaqueHeroi>()
        p.equipamentos.filter { it.tipo == TipoEquipamento.ARMA && !it.confiscado }.forEach { arma ->
            val modo = arma.armaTipoCombate?.lowercase().orEmpty()
            val aDistancia = modo.contains("dist") || modo.contains("fogo")
            val pericia = acharPericiaDaArma(p, arma)
            val nh = pericia?.calcularNivel(p) ?: p.dx
            val danoBruto = (arma.danoCalculadoComSt(p, pericia?.definicaoId) ?: arma.armaDanoRaw).orEmpty()
            if (danoBruto.isBlank()) return@forEach
            val tipoArma = CombatSession.tipoDano(danoBruto)
            val danoExpr = CombatSession.semTokenTipo(danoBruto)
            val alcanceReal = if (aDistancia) (arma.armaMaximoMetros ?: 50)
                else (arma.armaAlcanceCorpoACorpo?.let { reachParaMetros(it) } ?: 1)
            out.add(AtaqueHeroi(
                rotulo = arma.nome + (pericia?.let { " (${it.nome})" } ?: " (sem perícia, usa DX)"),
                nh = nh, danoExpr = danoExpr, tipo = tipoArma,
                aDistancia = aDistancia, alcance = alcanceReal, precisao = arma.armaPrecisao ?: 0,
                meioDano = if (aDistancia) (arma.armaMeioDanoMetros ?: 0) else 0,
                magnitude = arma.armaMagnitude ?: 0,
                apararTipo = CombatSession.parseAparar(arma.armaAparar).second,
                cadenciaTiro = arma.armaCadenciaTiro ?: 1,
                recuo = arma.armaRecuo ?: 1,
                duasMaos = ehDuasMaos(arma),
                armaDeFogo = modo.contains("fogo"),
                stMinimo = arma.armaStMinimo ?: 0,
                temPericia = pericia != null
            ))
        }
        val desarmada = melhorPericiaDesarmada(p)
        val aparaMarcial = desarmada?.let {
            CatalogFilters.normalizarBusca(it.definicaoId).removePrefix("racial_") in MARCIAIS_APARA
        } ?: false
        out.add(AtaqueHeroi(
            rotulo = (desarmada?.nome ?: "Desarmado"),
            nh = desarmada?.calcularNivel(p) ?: p.dx,
            danoExpr = p.danoGdP, tipo = DanoTipo.CONT, aDistancia = false, alcance = 1,
            desarmado = true, aparaMarcial = aparaMarcial, temPericia = desarmada != null
        ))
        val resultado = out.sortedByDescending { it.aDistancia }
        // Lote 422 (MB p.371): herói AGARRADO/IMOBILIZADO só ataca desarmado.
        return if (agarrado) resultado.filter { it.desarmado } else resultado
    }

    /** Alcance corpo-a-corpo ("C", "1", "1,2") em metros (o maior). "C" → 1 (adjacente). */
    fun reachParaMetros(raw: String): Int =
        Regex("\\d+").findAll(raw).mapNotNull { it.value.toIntOrNull() }.maxOrNull() ?: 1

    /** A arma ocupa as DUAS mãos (sem mão livre para escudo, MB p.375)? Flag do catálogo ou grupo. */
    fun ehDuasMaos(arma: Equipamento): Boolean =
        arma.armaDuasMaos ||
            ArmaCatalogoItem.duasMaosPorGrupo(arma.armaTipoCombate.orEmpty(), arma.armaGrupo.orEmpty())

    /**
     * Casa a arma com a perícia do herói (Lote 378): grupo/nome da arma × nome/especialização/id da
     * perícia; fallback por FAMÍLIA quando o grupo vem vazio (ficha criada pela IA).
     */
    fun acharPericiaDaArma(p: Personagem, arma: Equipamento): PericiaSelecionada? {
        val tokens = listOfNotNull(arma.armaGrupo, arma.nome)
            .map { CatalogFilters.normalizarBusca(it) }.filter { it.isNotBlank() }

        fun casa(per: PericiaSelecionada): Boolean {
            val campos = listOf(per.nome, per.especializacao, per.definicaoId)
                .map { CatalogFilters.normalizarBusca(it) }.filter { it.isNotBlank() }
            return campos.any { c -> tokens.any { a -> c == a || c.contains(a) || a.contains(c) } }
        }
        p.periciasTotais.firstOrNull { casa(it) }?.let { return it }

        val modo = arma.armaTipoCombate?.lowercase().orEmpty()
        val familia: List<String> = when {
            modo.contains("fogo") -> listOf("armas de fogo", "arma de fogo")
            modo.contains("dist") -> listOf("arco", "besta", "arremesso", "funda", "zarabatana")
            else -> emptyList()
        }
        if (familia.isEmpty()) return null
        val candidatas = p.periciasTotais.filter { per ->
            val n = CatalogFilters.normalizarBusca(per.nome)
            familia.any { n.contains(it) }
        }
        if (candidatas.isEmpty()) return null
        return candidatas.firstOrNull { per ->
            val esp = CatalogFilters.normalizarBusca(per.especializacao)
            esp.isNotBlank() && tokens.any { a -> esp == a || esp.contains(a) || a.contains(esp) }
        } ?: candidatas.maxByOrNull { it.calcularNivel(p) }
    }

    fun melhorPericiaDesarmada(p: Personagem): PericiaSelecionada? =
        p.periciasTotais.filter {
            CatalogFilters.normalizarBusca(it.definicaoId).removePrefix("racial_") in DESARMADAS
        }.maxByOrNull { it.calcularNivel(p) }

    val DESARMADAS = setOf("briga", "boxe", "carate", "judo", "luta_grecoromana", "caratê", "judô")
    /** Lote 391: aparar ARMA desarmado tem valor cheio (sem −3) com Caratê ou Judô (MB p.376). */
    val MARCIAIS_APARA = setOf("carate", "caratê", "judo", "judô")
}
