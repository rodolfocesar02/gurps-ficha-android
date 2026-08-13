package com.gurps.ficha.domain.rules.traits

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **Conferência cruzada** entre os dois catálogos que descrevem a MESMA regra
 * (Lote P-CRUZ).
 *
 * ## A ideia
 *
 * Toda regra de bônus em perícia está escrita **duas vezes** no livro, e o app
 * guarda as duas:
 *
 * - a página da **vantagem** diz *"+1 em Acrobacia, Escalada e Pilotagem"* →
 *   virou o campo `efeitos` de `vantagens.v3.json`;
 * - a página da **perícia** repete no rodapé *"Modificadores: … +1 c/ Equilíbrio
 *   Perfeito"* → está em `pericias_v2_rules_map.json`, extraído mas **nunca
 *   lido pelo app**.
 *
 * Se as duas discordam, uma das duas está errada. Este teste é o encontro delas.
 *
 * Analogia: é conferir a nota fiscal contra o extrato do cartão. Nenhum dos dois
 * prova nada sozinho; juntos, qualquer diferença aparece.
 *
 * ## ⚠️ Por que ele nasceu com uma lista de exceções
 *
 * Porque o texto de `Modificadores:` é **resumo**, e resumo omite. Três classes
 * de diferença são legítimas e estão listadas em [LEGITIMAS]:
 *
 * 1. **Regra geral disfarçada de traço** — *"Modificadores de Idioma"* e
 *    *"Modificadores de Familiaridade Cultural"* citam regras do livro (p.23 e
 *    p.24), não vantagens da ficha.
 * 2. **Modificador que age no ALVO** — *"subtrai Destemor do alvo"* em
 *    Intimidação, *"+4 se alvo for Fácil de Decifrar"*. O app não tem a ficha do
 *    alvo; é a mesma classe do *"+4 do Ingênuo para resistir a Sex Appeal"*.
 * 3. **Automação que vive em Kotlin** — a checagem lê o JSON, e quem tem classe
 *    própria (Flexibilidade, Idioma) não aparece lá. Isso já mordeu: o lote
 *    P-CRUZ quase declarou Flexibilidade de novo, e o JSON seria **ignorado em
 *    silêncio**, porque a Kotlin vence.
 *
 * Crescer a lista de exceções é decisão consciente, igual à do curinga.
 */
class ConferenciaCruzadaPericiasTest {

    private val gson = Gson()

    private fun asset(nome: String): File {
        val direto = File("src/main/assets/$nome")
        return if (direto.exists()) direto else File("app/src/main/assets/$nome")
    }

    private data class TracoCru(
        val id: String = "",
        val nome: String = "",
        val efeitos: List<EfeitoDeclarado> = emptyList()
    )

    private data class PericiaCrua(val id: String = "", val nome: String = "")

    private data class Modificadores(val raw: String = "")
    private data class ItemDoMapa(
        val id: String = "",
        val nome: String = "",
        val modificadores: Modificadores? = null
    )

    private data class MapaDeRegras(val items: List<ItemDoMapa> = emptyList())

    private fun <T> ler(nome: String, tipo: java.lang.reflect.Type): T {
        val arquivo = asset(nome)
        assertTrue("asset nao encontrado: ${arquivo.absolutePath}", arquivo.exists())
        return gson.fromJson(arquivo.readText(Charsets.UTF_8), tipo)
    }

    private fun tracos(): List<TracoCru> {
        val tipo = object : TypeToken<List<TracoCru>>() {}.type
        return ler<List<TracoCru>>("vantagens.v3.json", tipo) +
            ler<List<TracoCru>>("desvantagens.v2.json", tipo)
    }

    private fun pericias(): List<PericiaCrua> =
        ler("pericias.json", object : TypeToken<List<PericiaCrua>>() {}.type)

    private fun mapa(): List<ItemDoMapa> =
        ler<MapaDeRegras>("pericias_v2_rules_map.json", MapaDeRegras::class.java).items

    private companion object {
        /**
         * Diferenças que o teste NÃO deve acusar, com o motivo.
         *
         * Chave: `idDoTraco -> nomeDaPericia`. Ver o KDoc da classe para as três
         * classes de motivo.
         */
        val LEGITIMAS: Set<Pair<String, String>> = setOf(
            // 4. 🔴 COLISÃO DE PALAVRA COMUM — lote POD-8.
            // A vantagem `Controle` (GURPS Poderes, p.90) tem por nome uma
            // palavra corriqueira em português, e a varredura procura o nome do
            // traço dentro do texto da perícia. Ela casou com a página de
            // Artilharia, que fala de "controle" no sentido comum.
            // ⚠️ Não é caso de renomear a vantagem: o nome é do livro. É caso de
            // dizer que a coincidência não é regra.
            "controle_poderes" to "Artilharia/NT",
            // 3. automação em Kotlin, não em JSON
            "flexibilidade" to "Escalada",
            "flexibilidade" to "Fuga",
            "flexibilidade" to "Arte Erótica",
            // 2. o modificador age na ficha do ALVO, que o app não tem
            "destemor" to "Intimidação",
            "facil_de_decifrar" to "Detecção de Mentiras",
            "facil_de_decifrar" to "Linguagem Corporal",
            "duro_de_ouvido" to "Kiai",
            // Exorcismo é o inverso: o livro dá −4 a quem NÃO tem nenhum dos
            // três. Penalidade por ausência não cabe no campo `efeitos`, que
            // parte do traço que a ficha TEM.
            "abencoado" to "Exorcismo",
            "fe_verdadeira" to "Exorcismo",
            "investidura_de_poder" to "Exorcismo",
            // Sentidos Apurados: a página deles é uma entrada só com variantes;
            // o vínculo perícia a perícia ainda não foi lido. Fica na fila.
            "tato_apurado" to "Revistar",
            "visao_agucada" to "Armadilhas/NT",
            "paladar_olfato_apurado" to "Venefício/NT"
        )

        /**
         * Frases do rodapé que citam REGRA do livro, não traço da ficha.
         *
         * Sem esta limpeza o casador acusa a vantagem *Idioma* toda vez que lê
         * "Modificadores de Idioma" — e são 7 perícias.
         */
        val REGRA_GERAL = listOf(
            "modificadores de idioma", "penalidades de idioma",
            "modificadores de familiaridade", "modificadores de tempo",
            "todos de equipamento", "bonus de talento",
            "por aparencia", "por reputacao", "reputacao na area", "distracao"
        )

        /**
         * Traços cujo NOME é, no rodapé das perícias, sempre o nome de uma
         * **regra do livro** — nunca o traço da ficha.
         *
         * Perseguir as variações de escrita não fecha: o rodapé traz
         * *"modificadores de Familiaridade Cultural/Idioma"* na Oratória e
         * *"Modificadores de tempo, Familiaridade Cultural e Idioma"* na Poesia,
         * e amanhã traria uma terceira forma. Excluir os dois pelo **id**, com o
         * motivo escrito, é mais honesto que uma lista de frases que envelhece.
         *
         * - **Familiaridade Cultural** (MB p.23) é a regra de conhecer a cultura.
         * - **Idioma** (p.23) é a tabela de compreensão da língua, e já tem
         *   `IdiomaRule` em Kotlin.
         */
        val TRACOS_QUE_SAO_REGRA = setOf("familiaridade_cultural", "idioma")
    }

    private fun semAcento(s: String): String =
        java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}"), "")
            .lowercase()

    // ==================================================================
    // 1. Os dois catálogos de PERÍCIA têm de concordar no nome
    // ==================================================================

    @Test
    fun `o nome da pericia e o mesmo nos dois catalogos`() {
        // 🔴 Foi assim que apareceu "Leitura Dinmica" (sem o â) em
        // `pericias.json`. O casamento dos efeitos é por nome EXATO, então um
        // acento comido deixa o bônus mudo para sempre — e a regex de
        // consoantes seguidas não pega este caso, porque "nm" são só duas.
        //
        // O obelisco (†) marca especialização e só um dos catálogos o carrega;
        // fora isso, os nomes têm de bater letra por letra.
        val doMapa = mapa().associate { it.id to it.nome.replace("(†)", "").replace("†", "").trim() }
        val erros = pericias().mapNotNull { p ->
            val noMapa = doMapa[p.id] ?: return@mapNotNull null
            val limpo = p.nome.replace("(†)", "").replace("†", "").trim()
            if (limpo == noMapa) null
            else "${p.id}: pericias.json='$limpo' vs mapa='$noMapa'"
        }
        assertTrue(erros.joinToString("\n"), erros.isEmpty())
    }

    // ==================================================================
    // 2. O que o livro promete na perícia, o traço tem de entregar
    // ==================================================================

    /** `nome da perícia -> ids de traço citados no rodapé Modificadores`. */
    private fun citadosPelasPericias(): Map<String, Set<String>> {
        // Do nome mais LONGO para o mais curto: sem isso "Pouca Empatia" casa
        // com "Empatia" e o teste acusa a vantagem errada, em cascata.
        val porNome = tracos()
            .filter { it.nome.isNotBlank() && it.id !in TRACOS_QUE_SAO_REGRA }
            .map { semAcento(it.nome) to it.id }
            .sortedByDescending { it.first.length }

        return mapa().mapNotNull { item ->
            val raw = item.modificadores?.raw.orEmpty()
            if (raw.isBlank() || raw == "-") return@mapNotNull null
            var texto = semAcento(raw.replace("\n", " "))
            REGRA_GERAL.forEach { texto = texto.replace(it, " ") }

            val achados = mutableSetOf<String>()
            porNome.forEach { (nome, id) ->
                if (nome.length < 6) return@forEach
                val re = Regex("(?<![a-z])${Regex.escape(nome)}(?![a-z])")
                if (re.containsMatchIn(texto)) {
                    achados += id
                    texto = re.replace(texto, " ")
                }
            }
            val limpo = item.nome.replace("(†)", "").replace("†", "").trim()
            if (achados.isEmpty()) null else limpo to achados
        }.toMap()
    }

    @Test
    fun `todo bonus que a pagina da pericia promete existe no traco`() {
        // A invariante central do lote. Cada divergência aqui é um número que o
        // livro dá e a ficha não entrega — ou o contrário.
        val declarado = tracos().associate { t ->
            t.id to t.efeitos.filter { it.tipoResolvido == TipoEfeito.PERICIA }
                .map { it.alvo }.toSet()
        }
        val nomesDePericia = pericias().map { it.nome }.toSet()

        val erros = mutableListOf<String>()
        citadosPelasPericias().forEach { (pericia, ids) ->
            // Perícia que não existe em pericias.json não tem como ser alvo.
            if (pericia !in nomesDePericia) return@forEach
            ids.forEach { id ->
                if (id to pericia in LEGITIMAS) return@forEach
                val alvos = declarado[id] ?: return@forEach
                if (alvos.isEmpty()) {
                    erros += "$id nao tem efeito nenhum, mas '$pericia' promete um"
                } else if (pericia !in alvos && TraitRuleRegistry.CURINGA_PERICIA !in alvos) {
                    erros += "$id tem efeitos, mas nao inclui '$pericia'"
                }
            }
        }
        assertTrue(
            "Divergencias entre a pagina da pericia e o campo `efeitos`:\n" +
                erros.joinToString("\n") +
                "\n\nOu falta declarar, ou a diferenca e legitima e entra em LEGITIMAS.",
            erros.isEmpty()
        )
    }

    // ==================================================================
    // 3. Os furos concretos que o P-CRUZ fechou
    // ==================================================================

    private fun efeitosDe(id: String) = tracos().first { it.id == id }.efeitos

    private fun valorEm(id: String, pericia: String): Int? =
        efeitosDe(id).firstOrNull { it.alvo == pericia }?.valor

    @Test
    fun `Nocao Tridimensional herda os bonus do Senso de Direcao`() {
        // 🔴 O maior furo do lote. MB p.88, literal: "Ele recebe OS BONUS DO
        // SENSO DE DIRECAO, alem de +1 em Pilotagem e +2 em Acrobacia, Queda
        // Livre e Navegacao". O app tinha so a segunda metade da frase: quem
        // pagava 10 pontos perdia os +3 que o traco de 5 pontos dava.
        assertEquals(3, valorEm("nocao_tridimensional_do_espaco", "Percepção do Corpo"))
        assertEquals(3, valorEm("nocao_tridimensional_do_espaco", "Navegação/NT"))
        // E o que ela ja tinha continua de pe.
        assertEquals(1, valorEm("nocao_tridimensional_do_espaco", "Pilotagem/NT"))
        assertEquals(2, valorEm("nocao_tridimensional_do_espaco", "Queda Livre"))
    }

    @Test
    fun `a especializacao NAO herda o bonus da pericia base`() {
        // O casamento e por nome EXATO. "Acrobacia" nao alcanca "Acrobacia
        // Aerea", e "Deslumbrar" nao alcanca "Deslumbrar (Persuadir)" -- por
        // isso as especializacoes precisam de linha propria.
        assertEquals(2, valorEm("nocao_tridimensional_do_espaco", "Acrobacia Aérea"))
        listOf(
            "Deslumbrar (Cativar)", "Deslumbrar (Despertar Emoção)",
            "Deslumbrar (Persuadir)", "Deslumbrar (Sugerir)"
        ).forEach { assertEquals("faltou -3 em $it", -3, valorEm("pouca_empatia", it)) }
        // E a base continua declarada.
        assertEquals(-3, valorEm("pouca_empatia", "Deslumbrar"))
    }

    @Test
    fun `Sensivel e o irmao pobre da Empatia, com o mesmo alcance`() {
        // MB p.58: os dois niveis mexem nas MESMAS tres pericias; muda so o
        // numero (+1 e +3). A Empatia estava feita e a Sensivel, zerada.
        val alvosSensivel = efeitosDe("sensivel").map { it.alvo }.toSet()
        val alvosEmpatia = efeitosDe("empatia").map { it.alvo }.toSet()
        assertEquals(alvosEmpatia, alvosSensivel)
        assertEquals(1, valorEm("sensivel", "Detecção de Mentiras"))
        assertEquals(3, valorEm("empatia", "Detecção de Mentiras"))
    }

    @Test
    fun `Carisma alcanca as seis pericias de Influenciar`() {
        // MB p.48: "+1 nos testes de INFLUENCIA (v. pag. 359) E +1 em
        // Adivinhacao, Lideranca, Mendicancia e Oratoria". A segunda metade
        // estava declarada; a primeira, nao.
        listOf("Diplomacia", "Intimidação", "Lábia", "Manha", "Sex Appeal", "Trato-Social")
            .forEach { assertEquals("faltou +1 em $it", 1, valorEm("carisma", it)) }
        assertTrue(
            "o Carisma e por nivel",
            efeitosDe("carisma").filter { it.alvo == "Lábia" }.all { it.porNivel }
        )
    }

    @Test
    fun `Lamentavel da o mais 3 em Mendicancia, alem da reacao`() {
        // As duas paginas dizem coisas diferentes e as DUAS valem: a da vantagem
        // (p.22) fala de reacao, a da pericia (p.212) fala de Mendicancia.
        assertEquals(3, valorEm("lamentavel", "Mendicância"))
        assertEquals(3, valorEm("lamentavel", "reacao"))
    }

    @Test
    fun `Equilibrio Perfeito da o mais 4 em Postura Imovel`() {
        // MB p.59: "+4 nos testes de DX e de pericias baseadas em DX para SE
        // MANTER EM PE OU NAO SER DERRUBADO", e Postura Imovel (p.220) e
        // exatamente a pericia de nao ser projetado nem cair.
        assertEquals(4, valorEm("equilibrio_perfeito", "Postura Imóvel"))
        // Os +1 antigos continuam.
        assertEquals(1, valorEm("equilibrio_perfeito", "Acrobacia"))
    }

    @Test
    fun `Pele Elastica e as duas Memorias entregam o numero da pagina`() {
        assertEquals(4, valorEm("pele_elastica", "Disfarce/NT"))
        assertEquals(5, valorEm("memoria_eidetica", "Leitura Dinâmica"))
        assertEquals(10, valorEm("memoria_fotografica", "Leitura Dinâmica"))
    }

    @Test
    fun `o Escorregadio e condicional - o livro nomeia tres situacoes`() {
        // "+1 nos testes de ST, DX e Fuga para SE LIBERTAR DE AMARRAS, se
        // desvencilhar em um combate corporal ou se espremer por aberturas
        // estreitas". Somar sempre daria o bonus numa fuga de prisao comum.
        val fuga = efeitosDe("escorregadio").single { it.alvo == "Fuga" }
        assertEquals(1, fuga.valor)
        assertTrue("e por nivel (ate 5)", fuga.porNivel)
        assertTrue("tem de ser condicional", fuga.ehCondicional)
    }
}
