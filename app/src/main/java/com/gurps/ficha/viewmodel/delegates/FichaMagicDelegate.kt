package com.gurps.ficha.viewmodel.delegates

import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.domain.engine.MagicEngine
import com.gurps.ficha.domain.magias.*
import com.gurps.ficha.model.*
import kotlinx.coroutines.*

class FichaMagicDelegate(
    private val dataRepository: DataRepository,
    private val nexusArcanoModoAlvoAdapter: NexusArcanoModoAlvoAdapter
) {

    fun todasEscolasMagia(): List<String> = dataRepository.magias
        .flatMap { it.escola ?: emptyList() }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()

    fun todasClassesMagia(): List<String> = dataRepository.magias
        .mapNotNull { dataRepository.agruparClasseMagia(it.classe) }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()

    fun adicionarMagia(
        personagem: Personagem,
        definicao: MagiaDefinicao,
        pontosGastos: Int = 1,
        encantamentoAlvo: String? = null,
        especializacaoMagia: String? = null,
        ignorarPreRequisito: Boolean = false,
        nivelAptidaoMagica: Int
    ): Result<List<MagiaSelecionada>> {
        if (!MagicEngine.permiteMultiplasInstanciasMagia(definicao.id) && personagem.magias.any { it.definicaoId == definicao.id }) {
            return Result.failure(Exception("Magia já adicionada."))
        }

        if (MagicEngine.permiteMultiplasInstanciasPorEscola(definicao.id)) {
            val escolaNorm = especializacaoMagia?.trim()?.lowercase()
            if (escolaNorm.isNullOrBlank()) return Result.failure(Exception("Informe a escola da magia."))
            val duplicadaEscola = personagem.magias.any {
                it.definicaoId == definicao.id &&
                    it.especializacaoMagia?.trim()?.equals(escolaNorm, ignoreCase = true) == true
            }
            if (duplicadaEscola) return Result.failure(Exception("Esta magia já foi adicionada para essa escola."))
        }

        if (definicao.id.equals("imunidade_a_encantamento", ignoreCase = true) &&
            encantamentoAlvo.isNullOrBlank()
        ) {
            return Result.failure(Exception("Informe qual encantamento sera protegido."))
        }

        if (!ignorarPreRequisito) {
            val erroEspecializacao = MagicEngine.validarEspecializacaoObrigatoria(definicao.id, especializacaoMagia)
            if (erroEspecializacao != null) return Result.failure(Exception(erroEspecializacao))

            val erroRegraEspecial = MagicEngine.validarRegrasEspeciaisMagia(personagem, definicao, dataRepository, nivelAptidaoMagica)
            if (erroRegraEspecial != null) return Result.failure(Exception(erroRegraEspecial))
        }

        val magia = dataRepository.criarMagiaSelecionada(
            definicao = definicao,
            pontosGastos = pontosGastos.coerceAtLeast(1),
            encantamentoAlvo = encantamentoAlvo,
            especializacaoMagia = especializacaoMagia
        )
        return Result.success(personagem.magias + magia)
    }

    fun removerMagia(personagem: Personagem, index: Int): List<MagiaSelecionada> {
        val lista = personagem.magias.toMutableList()
        if (index in lista.indices) {
            lista.removeAt(index)
        }
        return lista
    }

    fun atualizarMagia(personagem: Personagem, index: Int, magia: MagiaSelecionada): List<MagiaSelecionada> {
        val lista = personagem.magias.toMutableList()
        if (index in lista.indices) {
            lista[index] = magia.copy(pontosGastos = magia.pontosGastos.coerceAtLeast(1))
        }
        return lista
    }

    fun prereqFailureForMagiaUnificada(
        personagem: Personagem,
        def: MagiaDefinicao,
        nivelAptidaoMagica: Int
    ): String? {
        val regraEspecial = MagicEngine.validarRegrasEspeciaisMagia(personagem, def, dataRepository, nivelAptidaoMagica)
        if (regraEspecial != null) return regraEspecial

        return nexusArcanoModoAlvoAdapter.falhaPreRequisitoHierarquica(
            alvoId = def.id,
            magiasConhecidasIds = personagem.magias.asSequence().map { it.definicaoId }.toSet(),
            iq = personagem.inteligencia,
            dx = personagem.destreza,
            am = nivelAptidaoMagica,
            vantagensConhecidasNorm = vantagensNormDe(personagem),
            periciasConhecidasNorm = periciasNormDe(personagem),
            desvantagensConhecidasNorm = desvantagensNormDe(personagem)
        )
    }

    fun calcularSnapshotModoAlvo(
        alvoId: String,
        personagem: Personagem,
        nivelAptidaoMagica: Int
    ): NexusArcanoModoAlvoSnapshot {
        return nexusArcanoModoAlvoAdapter.calcular(
            alvoId = alvoId,
            magiasConhecidasIds = personagem.magias.asSequence().map { it.definicaoId }.toSet(),
            iq = personagem.inteligencia,
            dx = personagem.destreza,
            am = nivelAptidaoMagica,
            vantagensConhecidasNorm = vantagensNormDe(personagem),
            periciasConhecidasNorm = periciasNormDe(personagem),
            desvantagensConhecidasNorm = desvantagensNormDe(personagem)
        )
    }

    // Lote 334: extrai vantagens/perícias da ficha NORMALIZADAS no MESMO formato do
    // motor (NexusArcanoEngine.normalize): sem acento, minúsculas, só [a-z0-9 espaço].
    // Assim o pré-requisito "ou Empatia com Animais" casa com a vantagem da ficha.
    private fun vantagensNormDe(personagem: Personagem): Set<String> {
        val base = personagem.vantagens.asSequence().map { normalizarNome(it.nome) }.filter { it.isNotBlank() }.toMutableSet()
        base += tokensIdioma(personagem)
        return base
    }

    /**
     * Lote 336: pré-requisitos de magia como "1 idioma com Sotaque" / "3 idiomas Com Sotaque"
     * são parseados como uma "vantagem" de nome literal (ex: "1 idioma com sotaque"). Para
     * casá-los, geramos TOKENS sintéticos a partir das vantagens Idioma da ficha: contamos
     * quantos idiomas a ficha tem em cada nível (Rudimentar<Sotaque<Materna; um nível superior
     * conta para os inferiores) e emitimos "N idioma(s) <nível>" para N=1..total, em singular,
     * plural e com numeral por extenso. Assim o motor (atendeVantagemRequerida) casa.
     */
    private fun tokensIdioma(personagem: Personagem): Set<String> {
        val idiomas = personagem.vantagens.filter { it.definicaoId.equals("idioma", ignoreCase = true) }
        if (idiomas.isEmpty()) return emptySet()
        // nível efetivo de cada idioma = melhor entre falado e escrito (o que dá mais alcance)
        fun ordem(n: String?) = when (n?.lowercase()) {
            "rudimentar" -> 1; "sotaque", "com sotaque", "com_sotaque" -> 2; "materna" -> 3; else -> 0
        }
        val niveis = idiomas.map {
            maxOf(ordem(it.metadados?.get("nivelFalado")), ordem(it.metadados?.get("nivelEscrito")))
        }.filter { it > 0 }
        if (niveis.isEmpty()) return emptySet()
        // quantos idiomas atingem PELO MENOS cada nível
        val countRud = niveis.count { it >= 1 }
        val countSot = niveis.count { it >= 2 }
        val countMat = niveis.count { it >= 3 }
        val extenso = listOf("", "um", "dois", "tres", "quatro", "cinco", "seis", "sete", "oito", "nove", "dez")
        val out = mutableSetOf<String>()
        fun emitir(qtd: Int, rotulos: List<String>) {
            for (n in 1..qtd) {
                val nums = listOfNotNull(n.toString(), extenso.getOrNull(n)?.takeIf { it.isNotBlank() })
                for (num in nums) for (rot in rotulos) {
                    out += "$num idioma $rot"
                    out += "$num idiomas $rot"
                }
            }
        }
        // "rudimentar" aceita qualquer nível; "com sotaque" aceita sotaque+materna; "materna" só materna
        emitir(countRud, listOf("rudimentar"))
        emitir(countSot, listOf("com sotaque", "sotaque"))
        emitir(countMat, listOf("materna"))
        return out
    }

    private fun periciasNormDe(personagem: Personagem): Set<String> =
        personagem.pericias.asSequence().map { normalizarNome(it.nome) }.filter { it.isNotBlank() }.toSet()

    // Lote 337: desvantagens da ficha NORMALIZADAS, p/ pré-requisitos "não ter Desvantagem X".
    private fun desvantagensNormDe(personagem: Personagem): Set<String> =
        personagem.desvantagens.asSequence().map { normalizarNome(it.nome) }.filter { it.isNotBlank() }.toSet()

    private fun normalizarNome(raw: String): String {
        val semAcento = java.text.Normalizer.normalize(raw, java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return semAcento.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun assinaturaEstadoMagias(personagem: Personagem, nivelAptidaoMagica: Int): String {
        val ids = personagem.magias.asSequence()
            .map { it.definicaoId }
            .distinct()
            .sorted()
            .joinToString("|")
        return "$ids#am=$nivelAptidaoMagica#iq=${personagem.inteligencia}#dx=${personagem.destreza}"
    }
}
