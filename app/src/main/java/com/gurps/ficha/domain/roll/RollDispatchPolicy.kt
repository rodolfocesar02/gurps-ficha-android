package com.gurps.ficha.domain.roll

object RollDispatchPolicy {
    fun deveRetentar(statusCode: Int?): Boolean {
        return statusCode == null
    }

    fun mensagemErro(statusCode: Int?, erroBruto: String?): String {
        return when {
            statusCode == 401 -> "chave de acesso inválida (401)"
            statusCode == 400 -> "canal de envio não definido (400)"
            statusCode == 500 -> "servidor não configurado corretamente (500)"
            statusCode == 502 -> "falha ao publicar no Discord (502)"
            statusCode != null -> "erro HTTP $statusCode"
            else -> {
                val detalhe = erroBruto?.trim().orEmpty()
                if (detalhe.isBlank()) {
                    "falha de internet/timeout ao conectar no servidor"
                } else {
                    "falha de internet/timeout ao conectar no servidor: $detalhe"
                }
            }
        }
    }
}
