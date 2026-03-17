package com.gurps.ficha.viewmodel

import com.gurps.ficha.model.TipoCusto

data class TraitSearchState(
    val query: String = "",
    val costType: TipoCusto? = null
)

data class SkillSearchState(
    val query: String = "",
    val attribute: String? = null,
    val difficulty: String? = null
)

data class MagicSearchState(
    val query: String = "",
    val school: String? = null,
    val magicClass: String? = null
)

data class TechniqueSearchState(
    val query: String = "",
    val source: String? = null
)

data class EquipmentSearchState(
    val query: String = "",
    val type: String? = null,
    val fireArmCategory: String? = null,
    val armorerNt: Int? = null,
    val armorerLocation: String? = null,
    val armorerTag: String? = null
)
