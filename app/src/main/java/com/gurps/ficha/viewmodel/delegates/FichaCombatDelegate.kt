package com.gurps.ficha.viewmodel.delegates

import com.gurps.ficha.model.*
import com.gurps.ficha.viewmodel.*
import com.gurps.ficha.viewmodel.ActiveDefense
import com.gurps.ficha.viewmodel.DefenseType

class FichaCombatDelegate {

    fun atualizarBonusManualEsquiva(personagem: Personagem, bonus: Int): Personagem {
        val defesas = personagem.defesasAtivas.copy(bonusManualEsquiva = bonus.coerceIn(-20, 20))
        return personagem.copy(defesasAtivas = defesas)
    }

    fun atualizarPericiaApara(personagem: Personagem, periciaId: String?): Personagem {
        val defesas = personagem.defesasAtivas.copy(periciaAparaId = periciaId)
        return personagem.copy(defesasAtivas = defesas)
    }

    fun atualizarBonusManualApara(personagem: Personagem, bonus: Int): Personagem {
        val defesas = personagem.defesasAtivas.copy(bonusManualApara = bonus.coerceIn(-20, 20))
        return personagem.copy(defesasAtivas = defesas)
    }

    fun atualizarPericiaBloqueio(personagem: Personagem, periciaId: String?): Personagem {
        val defesas = personagem.defesasAtivas.copy(periciaBloqueioId = periciaId)
        return personagem.copy(defesasAtivas = defesas)
    }

    fun atualizarEscudoBloqueio(personagem: Personagem, escudoNome: String?): Personagem {
        val defesas = personagem.defesasAtivas.copy(escudoSelecionadoNome = escudoNome)
        return personagem.copy(defesasAtivas = defesas)
    }

    fun atualizarBonusManualBloqueio(personagem: Personagem, bonus: Int): Personagem {
        val defesas = personagem.defesasAtivas.copy(bonusManualBloqueio = bonus.coerceIn(-20, 20))
        return personagem.copy(defesasAtivas = defesas)
    }

    fun calcularDefesasVisiveis(personagem: Personagem): List<ActiveDefense> {
        val lista = mutableListOf<ActiveDefense>()
        
        // Esquiva
        lista.add(ActiveDefense(
            type = DefenseType.ESQUIVA,
            name = "Esquiva",
            baseValue = personagem.defesasAtivas.getEsquivaBase(personagem),
            bonus = personagem.defesasAtivas.bonusManualEsquiva,
            finalValue = personagem.defesasAtivas.calcularEsquiva(personagem)
        ))
        
        // Apara
        personagem.defesasAtivas.calcularApara(personagem)?.let { finalVal ->
            personagem.defesasAtivas.getAparaBase(personagem)?.let { baseVal ->
                lista.add(ActiveDefense(
                    type = DefenseType.APARA,
                    name = "Apara",
                    baseValue = baseVal,
                    bonus = personagem.defesasAtivas.bonusManualApara,
                    finalValue = finalVal
                ))
            }
        }
        
        // Bloqueio
        personagem.defesasAtivas.calcularBloqueio(personagem)?.let { finalVal ->
            personagem.defesasAtivas.getBloqueioBase(personagem)?.let { baseVal ->
                val db = personagem.defesasAtivas.getBonusEscudo(personagem)
                lista.add(ActiveDefense(
                    type = DefenseType.BLOQUEIO,
                    name = "Bloqueio",
                    baseValue = baseVal + db,
                    bonus = personagem.defesasAtivas.bonusManualBloqueio,
                    finalValue = finalVal
                ))
            }
        }
        return lista
    }

    fun ajustarEscudoAutomatico(personagem: Personagem, escudosEquipados: List<Equipamento>): Personagem {
        if (personagem.defesasAtivas.periciaBloqueioId.isNullOrBlank()) return personagem
        if (escudosEquipados.isEmpty()) {
            return personagem.copy(defesasAtivas = personagem.defesasAtivas.copy(escudoSelecionadoNome = null))
        }
        val atual = personagem.defesasAtivas.escudoSelecionadoNome
        val existeAtual = atual?.let { nomeSel ->
            escudosEquipados.any { it.nome.equals(nomeSel.trim(), ignoreCase = true) }
        } == true
        if (existeAtual) return personagem
        
        val melhor = escudosEquipados.maxByOrNull { it.bonusDefesa }?.nome ?: escudosEquipados.first().nome
        return personagem.copy(defesasAtivas = personagem.defesasAtivas.copy(escudoSelecionadoNome = melhor))
    }
}
