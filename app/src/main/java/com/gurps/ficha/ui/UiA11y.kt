package com.gurps.ficha.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import com.gurps.ficha.BuildConfig

fun Modifier.pracegoTraversal(index: Int): Modifier {
    if (!BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)) return this
    return this.semantics { traversalIndex = index.toFloat() }
}
