package com.gurps.ficha.vtt

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Test

class VttBridgeCodecStressTest {

    @Test
    fun toJavascriptStringLiteral_handles_special_chars_in_stress_loop() {
        val samples = listOf(
            """{"nome":"teste","aparencia":"olhos \"azuis\"","notas":"linha1\nlinha2","idx":__IDX__}""",
            """{"nome":"O'Hara","historico":"c:\\temp\\ficha.json","idx":__IDX__}""",
            """{"nome":"çãé","notas":"unicode ✅ sem quebrar","idx":__IDX__}""",
            """{"nome":"emoji","notas":"🧙‍♂️⚔️🎲","idx":__IDX__}"""
        )

        repeat(300) { i ->
            val raw = samples[i % samples.size].replace("__IDX__", i.toString())
            val literal = VttBridgeCodec.toJavascriptStringLiteral(raw)
            val restored = JsonParser.parseString(literal).asString
            assertEquals(raw, restored)
        }
    }
}
