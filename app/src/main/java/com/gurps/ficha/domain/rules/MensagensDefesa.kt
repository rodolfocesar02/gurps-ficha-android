package com.gurps.ficha.domain.rules

/**
 * Mensagens que explicam ao jogador POR QUE uma defesa ativa não está
 * disponível.
 *
 * Separado do [CombatRules] de propósito: lá é só fórmula (números puros);
 * aqui é texto voltado ao usuário, que cita abas do app. Misturar os dois
 * sujaria um objeto que hoje é matemática limpa.
 *
 * Origem: a função nasceu dentro de `ui/TabCombate.kt`. Aquela tela foi
 * removida (não era renderizada por ninguém), mas a regra é útil e tinha
 * teste — então foi preservada aqui.
 *
 * ⚠️ Sem chamador em produção no momento. A aba Rolagem exibe o Bloqueio mas
 * não explica quando ele está indisponível; este é o texto pronto para quando
 * essa orientação for adicionada lá.
 */
object MensagensDefesa {

    /**
     * Bloqueio exige DUAS coisas: a perícia (Escudo/Capa) e um escudo
     * equipado. Devolve a orientação do que está faltando, ou `null` quando o
     * personagem pode bloquear normalmente.
     */
    fun bloqueioPendente(
        temPericiaEscudo: Boolean,
        temEscudoEquipado: Boolean
    ): String? = when {
        temPericiaEscudo && temEscudoEquipado -> null
        !temPericiaEscudo && !temEscudoEquipado ->
            "Sem Bloqueio: adicione perícia de Escudo na aba Perícias e equipe ao menos um escudo."
        !temPericiaEscudo ->
            "Sem Bloqueio: falta perícia de Escudo na aba Perícias."
        else ->
            "Sem Bloqueio: equipe ao menos um escudo na aba Equipamentos."
    }
}
