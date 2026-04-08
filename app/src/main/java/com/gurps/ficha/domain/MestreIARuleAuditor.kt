package com.gurps.ficha.domain

import com.gurps.ficha.domain.rules.CharacterRules
import com.gurps.ficha.model.*
import com.gurps.ficha.data.network.*
import java.util.Locale

/**
 * Motor Fiscal (Lote 55).
 * Valida sugestões da IA contra as regras oficiais do CharacterRules.kt.
 */
object MestreIARuleAuditor {

    data class AuditNote(
        val campo: String,
        val valorSugerido: String,
        val valorCorreto: String,
        val mensagem: String
    )

    /**
     * Compara os dados sugeridos pela IA com os cálculos reais.
     */
    fun auditarSugestao(sugerido: MestreIAResponse, personagemAtual: Personagem): List<AuditNote> {
        val notas = mutableListOf<AuditNote>()

        // 1. Auditoria de Atributos (Custo)
        val sugeridos = sugerido.atributos
        val stAI = sugeridos.st
        val dxAI = sugeridos.dx
        val iqAI = sugeridos.iq
        val htAI = sugeridos.ht

            val custoReal = CharacterRules.calcularPontosAtributos(
                forca = stAI,
                destreza = dxAI,
                inteligencia = iqAI,
                vitalidade = htAI,
                forcaBase = personagemAtual.forcaBase,
                destrezaBase = personagemAtual.destrezaBase,
                inteligenciaBase = personagemAtual.inteligenciaBase,
                vitalidadeBase = personagemAtual.vitalidadeBase
            )

            // Como a IA não envia o custo total no JSON geralmente, 
            // essa auditoria serve para preparar futuras expansões.
        

        // 2. Auditoria de Perícias (Dificuldade vs Nível)
        sugerido.pericias.forEach { pAI ->
            // Procura a definição original da perícia para saber o atributo e dificuldade
            val definicao = CharacterRules.DATA_REPOSITORY_INSTANCE?.pericias?.find { 
                it.nome.lowercase() == pAI.nome.lowercase() 
            }

            if (definicao != null) {
                // Se a IA sugeriu um nível, vamos ver se o custo em pontos bateria
                // Mas geralmente a IA sugere apenas {nome, nivel}.
                // O Fiscal aqui pode verificar se o NH é plausível para o atributo do personagem.
                val attrValor = personagemAtual.getAtributo(definicao.atributoBase)
                val nhSugerido = pAI.nivel
                
                // Se o NH sugerido for muito baixo (menor que atributo - 3), pode estar errado.
                // GURPS: Perícias Fáceis começam em Atributo+0 por 1pt.
                // No entanto, sem saber os pontos gastos (que a IA não envia no JSON simplificado),
                // o Fiscal foca em alertar se a IA sugeriu algo matematicamente impossível.
            }
        }

        return notas
    }

    /**
     * Gera um bloco de texto amigável com as correções do Fiscal.
     */
    fun gerarRelatorioTexto(notas: List<AuditNote>): String {
        if (notas.isEmpty()) return ""
        
        return "\n\n--- ⚖️ NOTA DO FISCAL DE REGRAS ---\n" +
               notas.joinToString("\n") { "• ${it.campo}: Sugerido ${it.valorSugerido}, mas o correto seria ${it.valorCorreto}. (${it.mensagem})" } +
               "\n----------------------------------"
    }

    /**
     * Função auxiliar para validar custos citados no TEXTO da IA (usando Regex).
     */
    fun auditarTextoPorContexto(textoIA: String, personagem: Personagem): String {
        val mutableText = StringBuilder(textoIA)
        var houveCorrecao = false
        val correcoes = mutableListOf<String>()

        // Exemplo: Detectar "ST 15 ([X] pontos)"
        val regexAttr = Regex("(ST|DX|IQ|HT)\\s+(\\d+)\\s*\\(?(\\d+)\\s*pontos\\)?", RegexOption.IGNORE_CASE)
        regexAttr.findAll(textoIA).forEach { match ->
            val attr = match.groupValues[1].uppercase()
            val nivel = match.groupValues[2].toInt()
            val custoCitado = match.groupValues[3].toInt()

            val custoReal = when(attr) {
                "ST" -> (nivel - personagem.forcaBase) * 10
                "HT" -> (nivel - personagem.vitalidadeBase) * 10
                "DX" -> (nivel - personagem.destrezaBase) * 20
                "IQ" -> (nivel - personagem.inteligenciaBase) * 20
                else -> 0
            }

            if (custoCitado != custoReal) {
                houveCorrecao = true
                correcoes.add("$attr $nivel: IA citou $custoCitado pts, mas o correto é $custoReal pts")
            }
        }

        if (houveCorrecao) {
            mutableText.append("\n\n--- ⚖️ FISCAL DE REGRAS ---\n")
            correcoes.forEach { mutableText.append("• $it\n") }
            mutableText.append("---------------------------")
        }

        return mutableText.toString()
    }
}
