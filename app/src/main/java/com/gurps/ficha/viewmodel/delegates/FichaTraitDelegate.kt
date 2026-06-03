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
    ): Result<List<VantagemSelecionada>> {
        val ehAcumulativa = definicao.tipoCusto == TipoCusto.POR_NIVEL
        // Vantagens que permitem múltiplas instâncias com descrições distintas (localização, tipo de ataque, etc.)
        val permiteMultiplas = definicao.id.equals("ataque_inato", ignoreCase = true)
            || definicao.id.equals("golpeadores", ignoreCase = true)
            || definicao.id.equals("resistencia_a_dano", ignoreCase = true)
            || definicao.id.equals("habilidades_modulares", ignoreCase = true)
            || definicao.id.equals("idioma", ignoreCase = true)

        val jaExisteIdentica = personagem.vantagens.any {
            it.definicaoId.equals(definicao.id, true) && it.descricao.equals(descricao, true)
        }

        if (jaExisteIdentica && !ehAcumulativa && !permiteMultiplas) {
            return Result.failure(Exception("Você já possui esta Vantagem com esta descrição."))
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
        return Result.success(personagem.vantagens + vantagem)
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
    ): Result<List<DesvantagemSelecionada>> {
        val ehAcumulativa = definicao.tipoCusto == TipoCusto.POR_NIVEL
        
        val jaExisteIdentica = personagem.desvantagens.any { 
            it.definicaoId.equals(definicao.id, true) && it.descricao.equals(descricao, true) 
        }

        if (jaExisteIdentica && !ehAcumulativa) {
            return Result.failure(Exception("Você já possui esta Desvantagem com esta descrição."))
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
        return Result.success(personagem.desvantagens + desvantagem)
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

    fun atualizarQualidade(personagem: Personagem, index: Int, novoTexto: String): List<String> {
        val lista = personagem.qualidades.toMutableList()
        if (index in lista.indices) {
            lista[index] = novoTexto
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

    fun atualizarPeculiaridade(personagem: Personagem, index: Int, novoTexto: String): List<String> {
        val lista = personagem.peculiaridades.toMutableList()
        if (index in lista.indices) {
            lista[index] = novoTexto
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
