import java.text.Normalizer
import java.util.*

fun normalize(text: String?): String {
    if (text == null) return ""
    
    // 1. Unicode Normalization (NFD) + Regex to remove accents
    val nfdNormalizedString = Normalizer.normalize(text, Normalizer.Form.NFD)
    val regex = Regex("\\p{InCombiningDiacriticalMarks}+")
    var res = regex.replace(nfdNormalizedString, "").lowercase()
    
    // 2. Remove non-alphanumeric (keep spaces)
    res = res.replace(Regex("[^a-z0-9\\s]"), " ")
    
    // 3. Remove plurals manually for each word (very simple version: ends with 's' but not 'ss')
    // In Portuguese, many weapon names are plural (Espadas, Maças, Machados)
    res = res.split(" ").map { word ->
        if (word.length > 3 && word.endsWith("s")) {
            word.substring(0, word.length - 1)
        } else {
            word
        }
    }.joinToString(" ")
    
    // 4. Remove multiple spaces
    res = res.replace(Regex("\\s+"), " ").trim()
    
    return res
}

fun test() {
    val cases = listOf(
        "Machado" to "machado",
        "MAÇA/MACHADO" to "maca machado",
        "Espada de Lâmina Larga" to "espada de lamina larga",
        "ESPADAS DE LAMINA LARGA" to "espada de lamina larga",
        "Espada Larga" to "espada larga"
    )
    
    println("--- Teste de Normalização ---")
    var allPassed = true
    for ((input, expected) in cases) {
        val actual = normalize(input)
        val passed = actual.contains(expected) || expected.contains(actual)
        println("Input: \"$input\" -> \"$actual\" (Esperado contém/é: \"$expected\") - ${if (passed) "OK" else "FAIL"}")
        if (!passed) allPassed = false
    }
    
    // Special test for matching
    val skill = "Espada de Lâmina Larga"
    val weapon = "Espada Larga"
    val group = "ESPADAS DE LAMINA LARGA"
    
    val sN = normalize(skill)
    val wN = normalize(weapon)
    val gN = normalize(group)
    
    println("\n--- Teste de Match ---")
    println("Skill: $sN")
    println("Weapon: $wN")
    println("Group: $gN")
    
    val matchWeapon = sN.contains(wN) || wN.contains(sN)
    val matchGroup = sN.contains(gN) || gN.contains(sN)
    
    println("Skill matches Weapon: $matchWeapon")
    println("Skill matches Group: $matchGroup")
}

test()
