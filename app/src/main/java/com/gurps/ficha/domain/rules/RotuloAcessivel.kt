package com.gurps.ficha.domain.rules

/**
 * **Como um número é falado** — Lote ACESS-1.
 *
 * ## 🔴 Quem ouve não vê o sinal
 *
 * O leitor de tela lê `-4` como travessão, ou simplesmente **pula o hífen** e
 * fala "quatro". Um redutor vira um bônus sem ninguém perceber — e o defeito é
 * invisível para quem testa com os olhos, porque na tela está escrito certo.
 *
 * ## ⚠️ Texto visível e descrição acessível são coisas DIFERENTES
 *
 * O padrão que o app já usava (e que eu não segui nas telas novas do MB-6/MB-7):
 * o `Text` mostra **`-4`**, e a `contentDescription` da linha fala **"menos
 * quatro"**. Não é escolher um dos dois — são dois destinos, cada um com a sua
 * forma.
 *
 * Este arquivo existe porque a mesma linha estava **copiada em cinco lugares**
 * (`AvancarEAtacarRules`, `IluminacaoRules`, `LocaisDeAtaque`,
 * `TamanhoDoAlvoRules` e mais), com três comportamentos diferentes para o zero e
 * para o positivo. Cópia com divergência é como um rótulo acaba certo numa tela
 * e errado na vizinha.
 */
object RotuloAcessivel {

    /**
     * Um **modificador**: o sinal importa e o zero é notícia.
     *
     * `+3` → "mais 3" · `-4` → "menos 4" · `0` → "zero".
     */
    fun modificador(n: Int): String = when {
        n > 0 -> "mais $n"
        n < 0 -> "menos ${-n}"
        else -> "zero"
    }

    /**
     * Um **valor** que pode ser negativo (PV abaixo de zero, por exemplo).
     *
     * Aqui o positivo é falado sem "mais", porque "mais nove pontos de vida"
     * soa como um ganho quando é só o total.
     */
    fun valor(n: Int): String = if (n < 0) "menos ${-n}" else "$n"

    /**
     * ⚠️ Confere se um texto tem sinal cru. Serve ao teste — e serve para
     * lembrar por que a regra existe.
     */
    fun temSinalCru(texto: String): Boolean =
        Regex("[+\\-−]\\s?\\d").containsMatchIn(texto)
}
