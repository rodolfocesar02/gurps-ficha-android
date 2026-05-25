package com.gurps.ficha.data.storage

import android.content.Context
import io.objectbox.BoxStore

/**
 * Singleton do ObjectBox — usado exclusivamente para vector search HNSW.
 * Room continua sendo o banco principal para todos os outros dados.
 */
object ObjectBoxStore {
    private var store: BoxStore? = null

    fun init(context: Context): BoxStore {
        return store ?: MyObjectBox.builder()
            .androidContext(context.applicationContext)
            .name("gurps_vec_store")
            .build()
            .also { store = it }
    }

    fun get(): BoxStore = store ?: error("ObjectBoxStore não inicializado. Chame init() primeiro.")

    fun close() {
        store?.close()
        store = null
    }
}
