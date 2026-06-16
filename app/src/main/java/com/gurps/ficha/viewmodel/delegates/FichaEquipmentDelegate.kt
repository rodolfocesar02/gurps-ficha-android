package com.gurps.ficha.viewmodel.delegates

import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.model.*
import java.text.Normalizer

class FichaEquipmentDelegate(private val dataRepository: DataRepository) {

    fun tagsArmaduras(): List<String> = dataRepository.armadurasCatalogo
        .asSequence()
        .flatMap { armadura ->
            sequenceOf(armadura.tags) + armadura.componentes.map { it.tags }
        }
        .flatMap { it.asSequence() }
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .filterNot { it.startsWith("local:", ignoreCase = true) }
        .filterNot { it.startsWith("local_exp:", ignoreCase = true) }
        .filterNot { it.startsWith("nt:", ignoreCase = true) }
        .filterNot { it.startsWith("tipo:", ignoreCase = true) }
        .distinct()
        .sorted()
        .toList()

    private enum class ClasseArmaFogo {
        PISTOLA_MM,
        RIFLE_ESPINGARDA,
        ULTRATECH,
        PESADA
    }

    fun adicionarEquipamento(personagem: Personagem, equipamento: Equipamento): List<Equipamento> {
        return personagem.equipamentos + equipamento
    }

    fun adicionarEquipamentoArma(personagem: Personagem, arma: ArmaCatalogoItem): List<Equipamento> {
        val notasArma = buildString {
            if (!arma.aparar.isNullOrBlank()) {
                if (isNotBlank()) append("\n")
                append("Aparar: ${arma.aparar} (${explicarAparar(arma.aparar)})")
            }
            val observacoes = observacoesArmaFormatadas(arma)
            if (observacoes.isNotBlank()) {
                if (isNotBlank()) append("\n")
                append(observacoes)
            }
        }
        val equipamento = Equipamento(
            nome = arma.nome,
            peso = arma.pesoBaseKg ?: 0f,
            custo = arma.custoBase ?: 0f,
            quantidade = 1,
            notas = notasArma,
            tipo = if (arma.nome.contains("escudo", ignoreCase = true)) TipoEquipamento.ESCUDO else TipoEquipamento.ARMA,
            bonusDefesa = 0,
            armaCatalogoId = arma.id,
            armaGrupo = arma.grupo?.substringBefore("(")?.trim(), // Mestre de Armas (Limpo)
            armaTipoCombate = arma.tipoCombate,
            armaDanoRaw = arma.danoRaw,
            armaStMinimo = arma.stMinimo,
            // Stats de combate (Lote 371) — copiados do catálogo p/ a ficha.
            armaAlcanceCorpoACorpo = arma.alcanceCorpoACorpo,
            armaDuasMaos = arma.duasMaos,
            armaPrecisao = arma.precisao,
            armaMeioDanoMetros = arma.meioDanoMetros,
            armaMaximoMetros = arma.maximoMetros,
            armaAlcanceMultStRaw = arma.alcanceMultStRaw,
            armaCadenciaTiro = arma.cadenciaTiro,
            armaTirosRaw = arma.tirosRaw,
            armaMagnitude = arma.magnitude,
            armaRecuo = arma.recuo,
            armaAparar = arma.aparar
        )
        return adicionarEquipamento(personagem, equipamento)
    }

    fun adicionarEquipamentoEscudo(personagem: Personagem, escudo: EscudoCatalogoItem): List<Equipamento> {
        val equipamento = Equipamento(
            nome = escudo.nome,
            peso = escudo.pesoKg ?: 0f,
            custo = escudo.custo ?: 0f,
            quantidade = 1,
            notas = escudo.observacoes,
            tipo = TipoEquipamento.ESCUDO,
            bonusDefesa = escudo.db
        )
        return adicionarEquipamento(personagem, equipamento)
    }

    fun adicionarEquipamentoArmadura(personagem: Personagem, armadura: ArmaduraCatalogoItem): List<Equipamento> {
        val componentesTexto = if (armadura.componentes.isEmpty()) {
            ""
        } else {
            armadura.componentes.joinToString(" | ") { c ->
                val custo = c.custoBase?.let { "$$it" } ?: "—"
                val peso = c.pesoKg?.let { "${it}kg" } ?: "—"
                "${c.local} RD ${c.rd} Custo $custo Peso $peso"
            }
        }
        val observacoes = montarObservacoesArmadura(armadura)
        val notas = buildString {
            append("Local: ${armadura.local}; RD: ${armadura.rd}")
            if (observacoes.isNotBlank()) append("\n$observacoes")
            if (componentesTexto.isNotBlank()) append("\nComponentes: $componentesTexto")
        }
        val equipamento = Equipamento(
            nome = armadura.nome,
            peso = armadura.pesoBaseKg ?: 0f,
            custo = armadura.custoBase ?: 0f,
            quantidade = 1,
            notas = notas,
            tipo = TipoEquipamento.ARMADURA,
            armaduraLocal = armadura.local,
            armaduraRd = armadura.rd
        )
        return adicionarEquipamento(personagem, equipamento)
    }

    fun adicionarEquipamentoArmaduraComSelecao(
        personagem: Personagem,
        armadura: ArmaduraCatalogoItem,
        locaisSelecionados: List<String>
    ): List<Equipamento> {
        val selecionadosNorm = locaisSelecionados.map { it.trim() }.filter { it.isNotBlank() }
        val locaisFinais = if (selecionadosNorm.isEmpty()) listOf(armadura.local) else selecionadosNorm
        val custoBase = armadura.custoBase ?: 0f
        val pesoBase = armadura.pesoBaseKg ?: 0f
        val possuiComponentes = armadura.componentes.isNotEmpty()
        val divisor = locaisFinais.size.coerceAtLeast(1).toFloat()

        val novaLista = personagem.equipamentos.toMutableList()
        locaisFinais.forEach { localSel ->
            val componente = armadura.componentes.firstOrNull { it.local.equals(localSel, ignoreCase = true) }
            val custoLocal = when {
                componente?.custoBase != null -> componente.custoBase
                possuiComponentes -> custoBase
                else -> (custoBase / divisor)
            }
            val pesoLocal = when {
                componente?.pesoKg != null -> componente.pesoKg
                possuiComponentes -> pesoBase
                else -> (pesoBase / divisor)
            }
            val rdLocal = componente?.rd ?: armadura.rd
            val observacoes = montarObservacoesArmadura(armadura)
            val notas = buildString {
                append("Local: $localSel; RD: $rdLocal")
                if (observacoes.isNotBlank()) append("\n$observacoes")
            }
            val equipamentoItem = Equipamento(
                nome = "${armadura.nome} ($localSel)",
                peso = pesoLocal,
                custo = custoLocal,
                quantidade = 1,
                notas = notas,
                tipo = TipoEquipamento.ARMADURA,
                armaduraLocal = localSel,
                armaduraRd = rdLocal
            )
            novaLista.add(equipamentoItem)
        }
        return novaLista
    }

    fun removerEquipamento(personagem: Personagem, index: Int): List<Equipamento> {
        val lista = personagem.equipamentos.toMutableList()
        if (index in lista.indices) {
            lista.removeAt(index)
        }
        return lista
    }

    fun atualizarEquipamento(personagem: Personagem, index: Int, equipamento: Equipamento): List<Equipamento> {
        val lista = personagem.equipamentos.toMutableList()
        if (index in lista.indices) {
            lista[index] = equipamento
        }
        return lista
    }

    private fun explicarAparar(valor: String): String {
        val v = valor.trim().uppercase()
        return when {
            v == "NÃO" || v == "NAO" -> "Nao pode aparar"
            v.endsWith("E") -> "Arma de esgrima"
            v.endsWith("D") -> "Arma desbalanceada"
            v == "0" -> "Sem modificador"
            v.startsWith("+") || v.startsWith("-") -> "Modificador na aparada"
            else -> "Valor de aparar"
        }
    }

    fun observacoesArmaFormatadas(arma: ArmaCatalogoItem): String {
        if (arma.tipoCombate != "corpo_a_corpo" && arma.tipoCombate != "distancia" && arma.tipoCombate != "armas_de_fogo") {
            return ""
        }
        val refs = extrairReferenciasObservacoes(arma.observacoes)
        if (arma.tipoCombate == "armas_de_fogo") {
            val classe = classificarArmaDeFogo(arma)
            val linhas = mutableListOf<String>()
            if (classe == ClasseArmaFogo.ULTRATECH) {
                linhas.add("Todas as armas de feixe incluem sistemas eletronicos das armas inteligentes (pag. 278).")
            }
            val mapa = when (classe) {
                ClasseArmaFogo.PISTOLA_MM -> OBS_ARMA_FOGO_PISTOLA_MM
                ClasseArmaFogo.RIFLE_ESPINGARDA -> OBS_ARMA_FOGO_RIFLE
                ClasseArmaFogo.ULTRATECH -> OBS_ARMA_FOGO_ULTRATECH
                ClasseArmaFogo.PESADA -> OBS_ARMA_FOGO_PESADA
            }
            refs.mapNotNull { ref -> mapa[ref]?.let { "[$ref] $it" } }.forEach { linhas.add(it) }
            return linhas.joinToString("\n")
        }

        if (refs.isEmpty()) return ""
        val mapa = if (arma.tipoCombate == "distancia") OBS_ARMA_DISTANCIA else OBS_ARMA_CORPO_A_CORPO
        return refs.mapNotNull { ref -> mapa[ref]?.let { "[$ref] $it" } }.joinToString("\n")
    }

    private fun extrairReferenciasObservacoes(observacoes: String): List<Int> {
        if (observacoes.isBlank() || !observacoes.contains("[")) return emptyList()
        return Regex("\\d+")
            .findAll(observacoes)
            .mapNotNull { it.value.toIntOrNull() }
            .distinct()
            .toList()
    }

    private fun classificarArmaDeFogo(arma: ArmaCatalogoItem): ClasseArmaFogo {
        val grupo = arma.grupo.lowercase()
        val nome = arma.nome.lowercase()
        if (
            grupo.contains("feixe") ||
            nome.contains("laser") ||
            nome.contains("eletrolaser") ||
            nome.contains("ionico") ||
            nome.contains("iônico")
        ) return ClasseArmaFogo.ULTRATECH

        if (
            grupo.contains("artilharia") ||
            grupo.contains("canhoneiro") ||
            grupo.contains("lancador") ||
            grupo.contains("lançador") ||
            grupo.contains("ala")
        ) return ClasseArmaFogo.PESADA

        if (grupo.contains("rifle")) return ClasseArmaFogo.RIFLE_ESPINGARDA

        return ClasseArmaFogo.PISTOLA_MM
    }

    fun categoriaArmaFogoParaFiltro(arma: ArmaCatalogoItem): String {
        return when (classificarArmaDeFogo(arma)) {
            ClasseArmaFogo.PISTOLA_MM -> "pistolas_mm"
            ClasseArmaFogo.RIFLE_ESPINGARDA -> "rifles_espingardas"
            ClasseArmaFogo.ULTRATECH -> "ultratech"
            ClasseArmaFogo.PESADA -> "pesadas"
        }
    }

    fun observacoesArmaPorEquipamento(equipamento: Equipamento): String {
        val porId = equipamento.armaCatalogoId
            ?.takeIf { it.isNotBlank() }
            ?.let { id -> dataRepository.armasCatalogo.firstOrNull { it.id == id } }
        if (porId != null) return observacoesArmaFormatadas(porId)

        val nomeBase = equipamento.nome.substringBefore(" (").trim()
        if (nomeBase.isBlank()) return ""
        val nomeBaseNorm = normalizarChaveTexto(nomeBase)

        val porNome = dataRepository.armasCatalogo.firstOrNull { arma ->
            val tipoOk = equipamento.armaTipoCombate.isNullOrBlank() ||
                arma.tipoCombate.equals(equipamento.armaTipoCombate, ignoreCase = true)
            val danoOk = equipamento.armaDanoRaw.isNullOrBlank() ||
                arma.danoRaw.equals(equipamento.armaDanoRaw, ignoreCase = true)
            val nomeOk = normalizarChaveTexto(arma.nome) == nomeBaseNorm
            tipoOk && danoOk && nomeOk
        } ?: dataRepository.armasCatalogo.firstOrNull { arma ->
            normalizarChaveTexto(arma.nome) == nomeBaseNorm
        }

        return if (porNome != null) observacoesArmaFormatadas(porNome) else ""
    }

    private fun normalizarChaveTexto(valor: String): String {
        val semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return semAcento
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun montarObservacoesArmadura(armadura: ArmaduraCatalogoItem): String {
        val refs = Regex("\\[(\\d+)]")
            .findAll(armadura.observacoes)
            .mapNotNull { it.groupValues.getOrNull(1)?.toIntOrNull() }
            .toList()
        var detalhes = armadura.observacoesDetalhadas
            .map { it.trim() }
            .filter { it.isNotBlank() }
        if (detalhes.isEmpty()) return ""

        val linhas = mutableListOf<String>()
        val primeira = detalhes.firstOrNull()
        if (primeira != null && primeira.contains("NT7+", ignoreCase = true)) {
            linhas.add(primeira)
            detalhes = detalhes.drop(1)
        }

        if (refs.isEmpty()) {
            linhas.addAll(detalhes)
            return linhas.joinToString("\n")
        }

        refs.zip(detalhes).forEach { (ref, texto) ->
            linhas.add("[$ref] $texto")
        }
        if (detalhes.size > refs.size) {
            linhas.addAll(detalhes.drop(refs.size))
        }
        return linhas.joinToString("\n")
    }

    companion object {
        private val OBS_ARMA_CORPO_A_CORPO = mapOf(
            1 to "Pode ser de arremesso. Veja Tabela de Armas Motoras de Combate a Distancia (pag. 275).",
            2 to "Pode ficar presa; veja Picaretas (pag. 406).",
            3 to "Briga aumenta dano sem armas; Garras e Carate aumentam dano de socos/chutes; Boxe aumenta dano por soco.",
            4 to "Se fracassar em um chute, precisa passar em teste de DX para nao cair.",
            5 to "Em fracasso de HT, a vitima fica atordoada enquanto houver contato e por mais (20-HT) segundos. Depois testa HT-3 para recuperar.",
            6 to "Aparar manguais sofre -4 e armas de esgrima (E) nao apararam. Bloquear manguais sofre -2. No nunchaku, redutores pela metade.",
            7 to "Lamina de energia. Exige manobra Preparar para ativar/desativar. Lamina inquebravel e danifica armas/corpo ao aparar/bloquear. Celula extra: $100, 0,25 kg, 300s.",
            8 to "Corda para estrangular; veja Garrote (pag. 406).",
            9 to "Dano maior quando usada montado; veja Armas de Cavalaria (pag. 397).",
            10 to "O cabo da arma pode ser usado como soco ingles em combate corporal.",
            11 to "Muito barulhento. Funciona 2 horas com 2,5 l de gasolina.",
            12 to "Especifique alcance maximo (ate 7 m) na compra. Custo/peso por metro. ST 5 +1 por metro. Veja Chicotes (pag. 405)."
        )

        private val OBS_ARMA_DISTANCIA = mapOf(
            1 to "Ataque de acompanhamento para dopar/envenenar se o dano ultrapassar a RD. Efeito depende do veneno (pag. 437).",
            2 to "Exige duas maos para preparar, mas apenas uma para atacar.",
            3 to "Municao: flecha/virote $2; dardo/bolinha $0,1; pedra de funda e gratuita.",
            4 to "Pode enredar ou apanhar o alvo (pag. 410).",
            5 to "Sarilho/pole para recarregar besta de ST alta. Permite recarregar arma com ST ate +4 da sua em 20 manobras Preparar.",
            6 to "Rede nao tem 1/2D. Distancia Max: (ST/2 + NH/5) rede grande; (ST + NH/5) rede de combate.",
            7 to "Pode disparar pedras (NT0) ou balas de chumbo (NT2). Bala de chumbo: +1 dano e dobra distancia.",
            8 to "Preparado: exige manobra Preparar e teste de ST para disparar; remover projetil causa metade do dano de entrada."
        )

        private val OBS_ARMA_FOGO_PISTOLA_MM = mapOf(
            1 to "Inclui sistemas eletronicos das armas inteligentes (veja o quadro).",
            2 to "Os foguetes demoram um pouco para acelerar. Divida o dano por 3 a 1-2 metros e por 2 a 3-10 metros.",
            3 to "A versao civil de uma arma semiautomatica tem CdT 3, -25% no custo e recebe um bonus de +1 na CL."
        )

        private val OBS_ARMA_FOGO_RIFLE = mapOf(
            1 to "A versao civil de uma arma semiautomatica tem CdT 3, -25% no custo e um bonus de +1 na CL.",
            2 to "Se o dano ultrapassar a RD, o dardo injeta uma droga ou veneno como ataque de acompanhamento. No caso de dardo tranquilizador, faca um teste de HT-3; um fracasso deixa o alvo inconsciente por uma quantidade de minutos igual a margem pela qual o teste falhou.",
            3 to "Inclui os sistemas eletronicos das armas inteligentes (pag. 278).",
            4 to "Inclui um lancador de granadas completo de 25 mm (pag. 281)."
        )

        private val OBS_ARMA_FOGO_ULTRATECH = mapOf(
            1 to "A arma precisa de atmosfera para funcionar. Ela nao produz nenhum efeito em atmosferas rarefeitas ou no vacuo.",
            2 to "O dano por queimadura recebe o modificador de dano de Sobretensao (pag. 108). Alem disso, mesmo quando nenhum dano penetre, o alvo deve obter sucesso em um teste de HT-4 mais metade da RD do local atingido (devido ao divisor de armadura). No caso de fracasso, o choque eletrico deixa o alvo atordoado. O alvo pode fazer novo teste de HT a cada turno sob a mesma penalidade (mas sem o bonus de RD) para se recuperar.",
            3 to "Fumaca, nevoa, chuva, nuvens etc. concedem ao alvo uma RD adicional igual a penalidade de visibilidade. Exemplo: se a chuva impuser -1 a cada 100 metros, um laser percorrendo 2.000 metros de chuva deve superar RD adicional de 20.",
            4 to "O dano por queimadura recebe modificador de dano de Sobretensao (pag. 108).",
            5 to "Em aventuras com superciencia, um onidisparador custa o dobro, mas tem regulagem para atordoamento: o dano se torna HT-3(3) at para pistola e HT-6(3) at para rifle. Um fracasso em teste de HT deixa a vitima inconsciente por uma quantidade de minutos igual a margem de erro."
        )

        private val OBS_ARMA_FOGO_PESADA = mapOf(
            1 to "Tem uma distancia minima: 10 metros no caso de um LG de 40 mm, 30 metros no caso de um MTA de 115 mm e 200 metros no caso de um MAS de 70 mm.",
            2 to "Contra-disparo de risco: 1d ponto de dano por queimadura em qualquer pessoa que se encontre atras do atirador a uma distancia de ate 15 metros (30 no caso da MTA).",
            3 to "Ataque Guiado (pag. 412). O Canhoneiro usa Artilharia (Missil Guiado) para atacar. 1/2D e igual a velocidade do projetil (m/s). O peso se refere ao lancador vazio/um projetil.",
            4 to "Ataque Teleguiado (Visao Hiperespectral) (pag. 413) com NH 10 do projetil. O atirador faz teste de Bombardeiro (Missil Guiado) para apontar. Em sucesso, o missil recebe bonus de Prec. 1/2D e igual a velocidade (m/s) do projetil. O peso se refere ao lancador vazio/um projetil.",
            5 to "Um tripe destacavel pesa mais 22 kg.",
            6 to "Pode ser anexada a parte inferior do cano de qualquer rifle ou carabina de NT7+. Utilize a Magnitude do Rifle.",
            7 to "O dano nao e reduzido pela metade na distancia de 1/2D, mas perdera seu divisor de armadura que e de (10).",
            8 to "Embutido na ACI de NT9 (pag. 279). Utilize a Magnitude da ACI. Possui sistemas eletronicos das armas inteligentes (pag. 278)."
        )
    }
}
