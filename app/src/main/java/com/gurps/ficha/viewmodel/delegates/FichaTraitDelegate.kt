package com.gurps.ficha.viewmodel.delegates

import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.model.*

class FichaTraitDelegate(private val dataRepository: DataRepository) {

    fun adicionarVantagem(
        personagem: Personagem,
        definicao: VantagemDefinicao,
        nivel: Int = 1,
        custoEscolhido: Int = 0,
        descricao: String = "",
        modificadores: List<ModificadorSelecao> = emptyList(),
        metadados: Map<String, String>? = null
    ): List<VantagemSelecionada> {
        val ehAcumulativa = definicao.tipoCusto == TipoCusto.POR_NIVEL
        val jaExiste = personagem.hasVantagem(definicao.id)

        // Se for única (nâo-acumulativa) e já existir (na raça ou na ficha), bloqueia duplicata
        if (jaExiste && !ehAcumulativa) {
            return personagem.vantagens
        }

        // Se já existe EXATAMENTE a mesma vantagem com a mesma descrição na ficha comprada, evita duplicar
        if (personagem.vantagens.any { it.definicaoId == definicao.id && it.descricao == descricao }) {
            return personagem.vantagens
        }

        val nivelNormalizado = normalizarNivelVantagem(definicao.id, nivel)
        val vantagem = dataRepository.criarVantagemSelecionada(
            definicao,
            nivelNormalizado,
            custoEscolhido,
            descricao,
            modificadores,
            metadados
        )
        return personagem.vantagens + vantagem
    }

    fun removerVantagem(personagem: Personagem, index: Int): List<VantagemSelecionada> {
        val lista = personagem.vantagens.toMutableList()
        if (index in lista.indices) {
            lista.removeAt(index)
        }
        return lista
    }

    fun atualizarVantagem(personagem: Personagem, index: Int, vantagem: VantagemSelecionada): List<VantagemSelecionada> {
        val lista = personagem.vantagens.toMutableList()
        if (index in lista.indices) {
            val nivelNormalizado = normalizarNivelVantagem(vantagem.definicaoId, vantagem.nivel)
            lista[index] = vantagem.copy(nivel = nivelNormalizado)
        }
        return lista
    }

    fun adicionarModificadorAVantagem(personagem: Personagem, index: Int, mod: ModificadorSelecao): List<VantagemSelecionada> {
        val lista = personagem.vantagens.toMutableList()
        if (index in lista.indices) {
            val vantagem = lista[index]
            val mods = vantagem.modificadores.toMutableList()
            mods.add(mod)
            lista[index] = vantagem.copy(modificadores = mods)
        }
        return lista
    }

    fun removerModificadorDeVantagem(personagem: Personagem, vantagemIndex: Int, modificadorIndex: Int): List<VantagemSelecionada> {
        val lista = personagem.vantagens.toMutableList()
        if (vantagemIndex in lista.indices) {
            val vantagem = lista[vantagemIndex]
            val mods = vantagem.modificadores.toMutableList()
            if (modificadorIndex in mods.indices) {
                mods.removeAt(modificadorIndex)
                lista[vantagemIndex] = vantagem.copy(modificadores = mods)
            }
        }
        return lista
    }

    fun adicionarDesvantagem(
        personagem: Personagem,
        definicao: DesvantagemDefinicao,
        nivel: Int = 1,
        custoEscolhido: Int = 0,
        descricao: String = "",
        autocontrole: Int? = null,
        modificadores: List<ModificadorSelecao> = emptyList(),
        metadados: Map<String, String>? = null
    ): List<DesvantagemSelecionada> {
        val ehAcumulativa = definicao.tipoCusto == TipoCusto.POR_NIVEL
        val jaExiste = personagem.hasDesvantagem(definicao.id)
        
        if (jaExiste && !ehAcumulativa) {
            return personagem.desvantagens
        }

        if (personagem.desvantagens.any { it.definicaoId == definicao.id && it.descricao == descricao }) {
            return personagem.desvantagens
        }

        val autocontroleNormalizado = if (definicao.usaAutocontroleMental()) autocontrole else null
        val desvantagem = dataRepository.criarDesvantagemSelecionada(
            definicao,
            nivel,
            custoEscolhido,
            descricao,
            autocontroleNormalizado,
            modificadores
        )
        return personagem.desvantagens + desvantagem
    }

    fun removerDesvantagem(personagem: Personagem, index: Int): List<DesvantagemSelecionada> {
        val lista = personagem.desvantagens.toMutableList()
        if (index in lista.indices) {
            lista.removeAt(index)
        }
        return lista
    }

    fun atualizarDesvantagem(personagem: Personagem, index: Int, desvantagem: DesvantagemSelecionada): List<DesvantagemSelecionada> {
        val lista = personagem.desvantagens.toMutableList()
        if (index in lista.indices) {
            val definicao = dataRepository.desvantagens.firstOrNull { it.id == desvantagem.definicaoId }
            val autocontroleNormalizado = if (definicao?.usaAutocontroleMental() == true) desvantagem.autocontrole else null
            lista[index] = desvantagem.copy(autocontrole = autocontroleNormalizado)
        }
        return lista
    }

    fun adicionarModificadorADesvantagem(personagem: Personagem, index: Int, mod: ModificadorSelecao): List<DesvantagemSelecionada> {
        val lista = personagem.desvantagens.toMutableList()
        if (index in lista.indices) {
            val desvantagem = lista[index]
            val mods = desvantagem.modificadores.toMutableList()
            mods.add(mod)
            lista[index] = desvantagem.copy(modificadores = mods)
        }
        return lista
    }

    fun removerModificadorDeDesvantagem(personagem: Personagem, desvantagemIndex: Int, modificadorIndex: Int): List<DesvantagemSelecionada> {
        val lista = personagem.desvantagens.toMutableList()
        if (desvantagemIndex in lista.indices) {
            val desvantagem = lista[desvantagemIndex]
            val mods = desvantagem.modificadores.toMutableList()
            if (modificadorIndex in mods.indices) {
                mods.removeAt(modificadorIndex)
                lista[desvantagemIndex] = desvantagem.copy(modificadores = mods)
            }
        }
        return lista
    }

    fun adicionarQualidade(personagem: Personagem, qualidade: String): List<String> {
        if (personagem.qualidades.size >= 5) return personagem.qualidades
        if (personagem.qualidades.contains(qualidade)) return personagem.qualidades
        return personagem.qualidades + qualidade
    }

    fun removerQualidade(personagem: Personagem, index: Int): List<String> {
        val lista = personagem.qualidades.toMutableList()
        if (index in lista.indices) {
            lista.removeAt(index)
        }
        return lista
    }

    fun adicionarPeculiaridade(personagem: Personagem, peculiaridade: String): List<String> {
        if (personagem.peculiaridades.size >= 5) return personagem.peculiaridades
        if (personagem.peculiaridades.contains(peculiaridade)) return personagem.peculiaridades
        return personagem.peculiaridades + peculiaridade
    }

    fun removerPeculiaridade(personagem: Personagem, index: Int): List<String> {
        val lista = personagem.peculiaridades.toMutableList()
        if (index in lista.indices) {
            lista.removeAt(index)
        }
        return lista
    }

    private fun normalizarNivelVantagem(definicaoId: String, nivel: Int): Int {
        if (definicaoId.equals("aptidao_astral", ignoreCase = true)) {
            return nivel.coerceIn(1, 4)
        }
        return nivel.coerceAtLeast(1)
    }
}
