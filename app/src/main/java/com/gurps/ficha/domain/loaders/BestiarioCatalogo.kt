package com.gurps.ficha.domain.loaders

import android.content.Context
import com.gurps.ficha.model.Bestiario
import com.gurps.ficha.model.BestiarioLoader

/**
 * Lote 365 (Saga B7): leitor do bestiário (assets/bestiario.v1.json), no padrão dos demais
 * catálogos. Cache simples em memória — o JSON não muda em runtime.
 */
object BestiarioCatalogo {

    @Volatile private var cache: Bestiario? = null

    fun carregar(context: Context): Bestiario {
        cache?.let { return it }
        val b = try {
            val texto = context.assets.open("bestiario.v1.json").bufferedReader().use { it.readText() }
            BestiarioLoader.parse(texto)
        } catch (e: Exception) {
            e.printStackTrace()
            Bestiario()
        }
        cache = b
        return b
    }
}
