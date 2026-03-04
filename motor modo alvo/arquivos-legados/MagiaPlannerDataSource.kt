package com.gurps.ficha.domain.magias

import com.gurps.ficha.model.MagiaDefinicao
import com.gurps.ficha.model.Personagem

interface MagiaPlannerDataSource {
    val magias: List<MagiaDefinicao>
    fun validarPreRequisitosMagia(definicao: MagiaDefinicao, personagem: Personagem): String?
    fun preRequisitoNormalizadoParaAnalise(definicao: MagiaDefinicao): String
    fun magiaSemPreRequisito(definicao: MagiaDefinicao): Boolean
}

