package com.gurps.ficha.viewmodel.delegates

import com.gurps.ficha.model.*

class FichaHistoryDelegate {
    
    fun diffAndLog(antigo: Personagem, novo: Personagem): Personagem {
        val mudancas = mutableListOf<String>()
        
        // Informações Básicas
        if (antigo.nome != novo.nome) mudancas.add("Alterou Nome do Personagem de '${antigo.nome}' para '${novo.nome}'")
        if (antigo.jogador != novo.jogador) mudancas.add("Alterou Nome do Jogador de '${antigo.jogador}' para '${novo.jogador}'")
        if (antigo.pontosIniciais != novo.pontosIniciais) mudancas.add("Alterou Pontos Iniciais de ${antigo.pontosIniciais} para ${novo.pontosIniciais}")
        if (antigo.xpGanhos != novo.xpGanhos) mudancas.add("Alterou XP Ganhos de ${antigo.xpGanhos} para ${novo.xpGanhos}")
        if (antigo.nivelTecnologico != novo.nivelTecnologico) mudancas.add("Alterou o NT da campanha de ${antigo.nivelTecnologico} para ${novo.nivelTecnologico}")
        
        // Atributos Primários
        if (antigo.forca != novo.forca) mudancas.add("Alterou ST de ${antigo.forca} para ${novo.forca}")
        if (antigo.destreza != novo.destreza) mudancas.add("Alterou DX de ${antigo.destreza} para ${novo.destreza}")
        if (antigo.inteligencia != novo.inteligencia) mudancas.add("Alterou IQ de ${antigo.inteligencia} para ${novo.inteligencia}")
        if (antigo.vitalidade != novo.vitalidade) mudancas.add("Alterou HT de ${antigo.vitalidade} para ${novo.vitalidade}")
        
        // Atributos Secundários
        if (antigo.modPontosVida != novo.modPontosVida) mudancas.add("Alterou mod. PV de ${antigo.modPontosVida} para ${novo.modPontosVida}")
        if (antigo.modVontade != novo.modVontade) mudancas.add("Alterou mod. Vontade de ${antigo.modVontade} para ${novo.modVontade}")
        if (antigo.modPercepcao != novo.modPercepcao) mudancas.add("Alterou mod. Percepção de ${antigo.modPercepcao} para ${novo.modPercepcao}")
        if (antigo.modPontosFadiga != novo.modPontosFadiga) mudancas.add("Alterou mod. PF de ${antigo.modPontosFadiga} para ${novo.modPontosFadiga}")
        
        // Vantagens
        val vantagensAdicionadas = novo.vantagens.filter { v -> antigo.vantagens.none { it.definicaoId == v.definicaoId } }
        val vantagensRemovidas = antigo.vantagens.filter { v -> novo.vantagens.none { it.definicaoId == v.definicaoId } }
        val vantagensEditadas = novo.vantagens.filter { nv -> antigo.vantagens.any { it.definicaoId == nv.definicaoId && (it.nivel != nv.nivel || it.custoFinal != nv.custoFinal) } }
        
        vantagensAdicionadas.forEach { mudancas.add("Adicionou Vantagem: ${it.nome} (${it.custoFinal} pts)") }
        vantagensRemovidas.forEach { mudancas.add("Removeu Vantagem: ${it.nome}") }
        vantagensEditadas.forEach { nv -> 
            val ov = antigo.vantagens.first { it.definicaoId == nv.definicaoId }
            mudancas.add("Editou Vantagem ${nv.nome}: Nível ${ov.nivel}->${nv.nivel}, Custo ${ov.custoFinal}->${nv.custoFinal}")
        }
        
        // Desvantagens
        val desvAdicionadas = novo.desvantagens.filter { v -> antigo.desvantagens.none { it.definicaoId == v.definicaoId } }
        val desvRemovidas = antigo.desvantagens.filter { v -> novo.desvantagens.none { it.definicaoId == v.definicaoId } }
        val desvEditadas = novo.desvantagens.filter { nv -> antigo.desvantagens.any { it.definicaoId == nv.definicaoId && (it.nivel != nv.nivel || it.custoFinal != nv.custoFinal || it.autocontrole != nv.autocontrole) } }
        
        desvAdicionadas.forEach { mudancas.add("Adicionou Desvantagem: ${it.nome} (${it.custoFinal} pts)") }
        desvRemovidas.forEach { mudancas.add("Removeu Desvantagem: ${it.nome}") }
        desvEditadas.forEach { mudancas.add("Editou Desvantagem ${it.nome}") }
        
        // Perícias
        val periciasAdicionadas = novo.pericias.filter { v -> antigo.pericias.none { it.nome == v.nome && it.especializacao == v.especializacao } }
        val periciasRemovidas = antigo.pericias.filter { v -> novo.pericias.none { it.nome == v.nome && it.especializacao == v.especializacao } }
        val periciasEditadas = novo.pericias.filter { nv -> antigo.pericias.any { it.nome == nv.nome && it.especializacao == nv.especializacao && it.pontosGastos != nv.pontosGastos } }
        
        periciasAdicionadas.forEach { mudancas.add("Adicionou Perícia: ${it.nome} (${it.pontosGastos} pts)") }
        periciasRemovidas.forEach { mudancas.add("Removeu Perícia: ${it.nome}") }
        periciasEditadas.forEach { nv -> 
            val ov = antigo.pericias.first { it.nome == nv.nome && it.especializacao == nv.especializacao }
            mudancas.add("Alterou pontos da Perícia ${nv.nome} de ${ov.pontosGastos} para ${nv.pontosGastos}")
        }
        
        // Magias
        val magiasAdicionadas = novo.magias.filter { v -> antigo.magias.none { it.nome == v.nome } }
        val magiasRemovidas = antigo.magias.filter { v -> novo.magias.none { it.nome == v.nome } }
        val magiasEditadas = novo.magias.filter { nv -> antigo.magias.any { it.nome == nv.nome && it.pontosGastos != nv.pontosGastos } }
        
        magiasAdicionadas.forEach { mudancas.add("Adicionou Magia: ${it.nome} (${it.pontosGastos} pts)") }
        magiasRemovidas.forEach { mudancas.add("Removeu Magia: ${it.nome}") }
        magiasEditadas.forEach { nv -> 
            val ov = antigo.magias.first { it.nome == nv.nome }
            mudancas.add("Alterou pontos da Magia ${nv.nome} de ${ov.pontosGastos} para ${nv.pontosGastos}")
        }
        
        // Equipamentos
        val equipAdicionadas = novo.equipamentos.filter { v -> antigo.equipamentos.none { it.nome == v.nome } }
        val equipRemovidas = antigo.equipamentos.filter { v -> novo.equipamentos.none { it.nome == v.nome } }
        val equipEditadas = novo.equipamentos.filter { nv -> antigo.equipamentos.any { it.nome == nv.nome && (it.quantidade != nv.quantidade || it.notas != nv.notas) } }
        
        equipAdicionadas.forEach { mudancas.add("Adicionou Equipamento: ${it.nome}") }
        equipRemovidas.forEach { mudancas.add("Removeu Equipamento: ${it.nome}") }
        equipEditadas.forEach { nv -> mudancas.add("Editou Equipamento ${nv.nome}") }

        // Técnicas
        val tecnicasAdicionadas = novo.tecnicas.filter { v -> antigo.tecnicas.none { it.nome == v.nome } }
        val tecnicasRemovidas = antigo.tecnicas.filter { v -> novo.tecnicas.none { it.nome == v.nome } }
        val tecnicasEditadas = novo.tecnicas.filter { nv -> antigo.tecnicas.any { it.nome == nv.nome && it.pontosGastos != nv.pontosGastos } }

        tecnicasAdicionadas.forEach { mudancas.add("Adicionou Técnica: ${it.nome}") }
        tecnicasRemovidas.forEach { mudancas.add("Removeu Técnica: ${it.nome}") }
        tecnicasEditadas.forEach { nv -> mudancas.add("Alterou pontos da Técnica ${nv.nome}") }

        // Qualidades/Peculiaridades
        val qualAdicionadas = novo.qualidades.filter { v -> antigo.qualidades.none { it == v } }
        val qualRemovidas = antigo.qualidades.filter { v -> novo.qualidades.none { it == v } }
        qualAdicionadas.forEach { mudancas.add("Adicionou Qualidade: $it") }
        qualRemovidas.forEach { mudancas.add("Removeu Qualidade: $it") }

        val pecAdicionadas = novo.peculiaridades.filter { v -> antigo.peculiaridades.none { it == v } }
        val pecRemovidas = antigo.peculiaridades.filter { v -> novo.peculiaridades.none { it == v } }
        pecAdicionadas.forEach { mudancas.add("Adicionou Peculiaridade: $it") }
        pecRemovidas.forEach { mudancas.add("Removeu Peculiaridade: $it") }

        if (mudancas.isEmpty()) return novo
        
        val timestamp = System.currentTimeMillis()
        val novosLogs = mudancas.map { RegistroLog(timestamp, it) }
        
        return novo.copy(historicoLog = novosLogs + antigo.historicoLog)
    }

    fun limparHistorico(personagem: Personagem): Personagem {
        return personagem.copy(historicoLog = emptyList())
    }
}
