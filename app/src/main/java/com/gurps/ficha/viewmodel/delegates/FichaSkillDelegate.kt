package com.gurps.ficha.viewmodel.delegates

import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.domain.engine.SkillEngine
import com.gurps.ficha.model.*
import java.text.Normalizer

class FichaSkillDelegate(private val dataRepository: DataRepository) {

    private val tecnicasNomesNormalizados: Set<String>
        get() = dataRepository.tecnicasCatalogo
            .asSequence()
            .map { normalizarTexto(it.nome) }
            .filter { it.isNotBlank() }
            .toSet()

    fun adicionarPericia(
        personagem: Personagem,
        definicao: PericiaDefinicao,
        pontosGastos: Int = 1,
        especializacao: String = "",
        atributoEscolhido: AtributoBase? = null,
        dificuldadeEscolhida: Dificuldade? = null
    ): Result<List<PericiaSelecionada>> {
        if (personagem.pericias.any { it.definicaoId == definicao.id && it.especializacao == especializacao }) {
            return Result.failure(Exception("Essa perícia já foi adicionada."))
        }
        val erroPreRequisito = dataRepository.validarPreRequisitosPericia(definicao, personagem)
        if (erroPreRequisito != null) {
            return Result.failure(Exception("Pré-requisito não atendido: $erroPreRequisito"))
        }
        val pericia = dataRepository.criarPericiaSelecionada(definicao, pontosGastos, especializacao, atributoEscolhido, dificuldadeEscolhida)
        return Result.success(personagem.pericias + pericia)
    }

    fun removerPericia(personagem: Personagem, index: Int): List<PericiaSelecionada> {
        val lista = personagem.pericias.toMutableList()
        if (index in lista.indices) {
            lista.removeAt(index)
        }
        return lista
    }

    fun atualizarPericia(personagem: Personagem, index: Int, pericia: PericiaSelecionada): List<PericiaSelecionada> {
        val lista = personagem.pericias.toMutableList()
        if (index in lista.indices) {
            lista[index] = pericia
        }
        return lista
    }

    // === TECNICAS ===

    fun adicionarTecnica(
        personagem: Personagem,
        definicao: TecnicaCatalogoItem,
        periciaBase: PericiaSelecionada,
        nivelRelativoPredefinido: Int = 0
    ): Result<List<TecnicaSelecionada>> {
        if (personagem.tecnicas.any { it.definicaoId == definicao.id && it.periciaBaseDefinicaoId == periciaBase.definicaoId && it.periciaBaseEspecializacao == periciaBase.especializacao }) {
            return Result.failure(Exception("Esta técnica já foi adicionada para esta perícia base."))
        }
        if (!tecnicaAtendePreRequisito(definicao, periciaBase)) {
            return Result.failure(Exception("A perícia selecionada não atende o pré-requisito desta técnica."))
        }
        val limiteMaximo = limiteMaximoTecnica(definicao)
        if (limiteMaximo != null && nivelRelativoPredefinido > limiteMaximo) {
            return Result.failure(Exception("Esta técnica permite no máximo predefinido +$limiteMaximo."))
        }
        val tecnica = dataRepository.criarTecnicaSelecionada(
            definicao = definicao,
            periciaBase = periciaBase,
            nivelRelativoPredefinido = nivelRelativoPredefinido
        )
        return Result.success(personagem.tecnicas + tecnica)
    }

    fun removerTecnica(personagem: Personagem, index: Int): List<TecnicaSelecionada> {
        val lista = personagem.tecnicas.toMutableList()
        if (index in lista.indices) {
            lista.removeAt(index)
        }
        return lista
    }

    fun atualizarTecnica(personagem: Personagem, index: Int, tecnica: TecnicaSelecionada): List<TecnicaSelecionada> {
        val lista = personagem.tecnicas.toMutableList()
        if (index in lista.indices) {
            val dificuldade = dataRepository.tecnicaDificuldade(tecnica.dificuldadeRaw)
            val nivelRelativo = tecnica.nivelRelativoPredefinido.coerceAtLeast(0)
            val custo = dataRepository.calcularCustoTecnica(dificuldade, nivelRelativo)
            lista[index] = tecnica.copy(
                pontosGastos = custo,
                nivelRelativoPredefinido = nivelRelativo
            )
        }
        return lista
    }

    fun custoTecnica(definicao: TecnicaCatalogoItem, nivelRelativoPredefinido: Int): Int {
        val dificuldade = dataRepository.tecnicaDificuldade(definicao.dificuldadeRaw)
        return dataRepository.calcularCustoTecnica(dificuldade, nivelRelativoPredefinido.coerceAtLeast(0))
    }

    fun limiteMaximoTecnica(definicao: TecnicaCatalogoItem): Int? {
        return SkillEngine.getRegraPerfilTecnica(definicao, dataRepository).limiteRelativo
    }

    fun preRequisitoExibicaoTecnica(definicao: TecnicaCatalogoItem): String {
        return SkillEngine.getRegraPerfilTecnica(definicao, dataRepository).preRequisitoExibicao
    }

    fun calcularNivelTecnicaPreview(
        personagem: Personagem,
        definicao: TecnicaCatalogoItem,
        periciaBase: PericiaSelecionada,
        nivelRelativoPredefinido: Int
    ): Int? {
        val tecnica = dataRepository.criarTecnicaSelecionada(
            definicao = definicao,
            periciaBase = periciaBase,
            nivelRelativoPredefinido = nivelRelativoPredefinido
        )
        return tecnica.calcularNivel(personagem)
    }

    fun tecnicaAtendePreRequisito(definicao: TecnicaCatalogoItem, periciaBase: PericiaSelecionada): Boolean {
        val prerequisitoRaw = definicao.preRequisitoRaw
        val prerequisito = normalizarTexto(prerequisitoRaw)
        if (prerequisito.isBlank() || prerequisito == "-") return true

        if (!periciaCompativelComFamilia(prerequisito, periciaBase)) return false

        val ancoraPericia = extrairAncoraPericiaNoLimite(prerequisito)
        if (!ancoraPericia.isNullOrBlank()) {
            val matchAncora = SkillEngine.periciaCorrespondeTermo(periciaBase, ancoraPericia, tecnicasNomesNormalizados)
            if (matchAncora != null) return matchAncora
        }

        val blocoPrincipal = normalizarTexto(prerequisitoRaw.substringBefore(";"))
        val termos = blocoPrincipal
            .replace(" ou ", ",")
            .replace(" e ", ",")
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
        if (termos.isEmpty()) return true

        val avaliacao = termos
            .mapNotNull { termo -> SkillEngine.periciaCorrespondeTermo(periciaBase, termo, tecnicasNomesNormalizados) }
        if (avaliacao.isNotEmpty()) {
            return avaliacao.any { it }
        }
        return true
    }

    fun periciaCompativelComFamilia(
        prerequisitoNormalizado: String,
        periciaBase: PericiaSelecionada
    ): Boolean {
        val exigeTiro =
            prerequisitoNormalizado.contains("pericia de tiro") ||
                prerequisitoNormalizado.contains("qualquer pericia de tiro") ||
                prerequisitoNormalizado.contains("arma de longo alcance")
        if (exigeTiro && !SkillEngine.periciaEhTiro(periciaBase)) return false

        val exigeEsgrima = prerequisitoNormalizado.contains("arma de esgrima")
        if (exigeEsgrima && !SkillEngine.periciaEhArmaEsgrima(periciaBase)) return false

        val exigeDefesaAtiva =
            prerequisitoNormalizado.contains("defesa ativa") ||
                prerequisitoNormalizado.contains("bloquear ou aparar")
        if (exigeDefesaAtiva && !SkillEngine.periciaEhDefesaAtiva(periciaBase)) return false

        val exigeCorpoACorpo =
            prerequisitoNormalizado.contains("arma corpo a corpo") ||
                prerequisitoNormalizado.contains("arma de combate corpo a corpo") ||
                prerequisitoNormalizado.contains("ataque corpo a corpo") ||
                (
                    (prerequisitoNormalizado.contains("pericia com arma apropriada") ||
                        prerequisitoNormalizado.contains("pericia de arma apropriada") ||
                        prerequisitoNormalizado.contains("arma apropriada")) &&
                        !prerequisitoNormalizado.contains("tiro") &&
                        !prerequisitoNormalizado.contains("longo alcance") &&
                        !prerequisitoNormalizado.contains("arma de fogo") &&
                        !prerequisitoNormalizado.contains("armas de fogo") &&
                        !prerequisitoNormalizado.contains("arma de esgrima")
                    )
        if (exigeCorpoACorpo) {
            val permiteDesarmado =
                prerequisitoNormalizado.contains("desarmado") ||
                    prerequisitoNormalizado.contains("judo") ||
                    prerequisitoNormalizado.contains("luta greco romana") ||
                    prerequisitoNormalizado.contains("carate") ||
                    prerequisitoNormalizado.contains("briga") ||
                    prerequisitoNormalizado.contains("boxe")
            val ok =
                SkillEngine.periciaEhCorpoACorpo(periciaBase) ||
                    (permiteDesarmado && SkillEngine.periciaEhDesarmado(periciaBase))
            if (!ok) return false
        }

        return true
    }

    fun normalizarTexto(valor: String): String {
        val semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return semAcento
            .lowercase()
            .replace("-", " ")
            .replace(Regex("[^a-z0-9\\s/+_-]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun extrairAncoraPericiaNoLimite(prerequisitoNormalizado: String): String? {
        val trechoLimite = prerequisitoNormalizado.substringAfter("nao pode exceder", "")
        if (trechoLimite.isBlank()) return null
        val semPrefixo = trechoLimite
            .trim()
            .replace(Regex("^(o|a|os|as)\\s+"), "")
            .replace(Regex("^nivel\\s+de\\s+"), "")
            .replace(Regex("^nivel\\s+da\\s+"), "")
            .replace(Regex("^nivel\\s+do\\s+"), "")
            .replace(Regex("^pericias?\\s+"), "")
            .trim()
        val candidata = semPrefixo
            .substringBefore(" +")
            .substringBefore(" -")
            .substringBefore(" baseada")
            .substringBefore(" ou ")
            .trim()
        if (candidata.isBlank()) return null
        val candidataSemBonus = candidata
            .replace(Regex("[+-]\\d+.*$"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
        val termosGenericos = listOf(
            "pre requisito",
            "pre requisito aparar",
            "pre requisito bloquear",
            "pericia pre requisito",
            "pericia de tiro",
            "pericia com arma",
            "defesa ativa",
            "bloquear",
            "aparar",
            "st",
            "dx",
            "ht",
            "iq",
            "per"
        )
        if (termosGenericos.any { termo ->
                candidata == termo ||
                    candidata.startsWith("$termo ") ||
                    candidataSemBonus == termo ||
                    candidataSemBonus.startsWith("$termo ")
            }) {
            return null
        }
        return candidataSemBonus.ifBlank { candidata }
    }
}
