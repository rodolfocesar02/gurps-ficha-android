package com.gurps.ficha.data.storage

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.model.ModeloRacial
import java.io.File

/**
 * Persistência LEVE das metacaracterísticas criadas pelo usuário.
 *
 * Decisão de design (com o usuário): uma metacaracterística é o MESMO
 * pacote de um ModeloRacial (atributos + traços + custo), só salva como
 * traço reutilizável. Em vez de tabela Room nova (migration = risco
 * alto, já registrado como perigo), usa um arquivo JSON em filesDir —
 * padrão já usado pelo app (ex.: filesDir/maps). Sem migration, sem
 * mexer no FichaDatabase.
 *
 * Cada item é um ModeloRacial com tipo = METACARACTERISTICA, então os
 * componentes ficam guardados (o Mestre pode reabrir e editar — GURPS
 * p.262 "é possível modificar os elementos, alterando o custo").
 */
class MetacaracteristicaStore(context: Context) {

    private val gson = Gson()
    private val arquivo = File(context.filesDir, "metacaracteristicas_usuario.json")

    fun listar(): List<ModeloRacial> {
        if (!arquivo.exists()) return emptyList()
        return runCatching {
            val tipo = object : TypeToken<List<ModeloRacial>>() {}.type
            gson.fromJson<List<ModeloRacial>>(arquivo.readText(), tipo) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    /** Salva/atualiza por nome (case-insensitive). Mantém o resto. */
    fun salvar(meta: ModeloRacial) {
        val atual = listar().filterNot { it.nome.equals(meta.nome, ignoreCase = true) }
        gravar(atual + meta)
    }

    fun remover(nome: String) {
        gravar(listar().filterNot { it.nome.equals(nome, ignoreCase = true) })
    }

    private fun gravar(lista: List<ModeloRacial>) {
        runCatching { arquivo.writeText(gson.toJson(lista)) }
    }
}
