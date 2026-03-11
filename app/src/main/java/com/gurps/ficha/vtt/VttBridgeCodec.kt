package com.gurps.ficha.vtt

object VttBridgeCodec {
    fun toJavascriptStringLiteral(rawJson: String): String {
        val out = StringBuilder(rawJson.length + 2)
        out.append('"')
        rawJson.forEach { ch ->
            when (ch) {
                '\\' -> out.append("\\\\")
                '"' -> out.append("\\\"")
                '\n' -> out.append("\\n")
                '\r' -> out.append("\\r")
                '\t' -> out.append("\\t")
                '\b' -> out.append("\\b")
                '\u000C' -> out.append("\\f")
                else -> {
                    if (ch.code < 0x20) {
                        out.append(String.format("\\u%04x", ch.code))
                    } else {
                        out.append(ch)
                    }
                }
            }
        }
        out.append('"')
        return out.toString()
    }
}
