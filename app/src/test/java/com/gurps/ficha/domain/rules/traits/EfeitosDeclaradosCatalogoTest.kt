package com.gurps.ficha.domain.rules.traits

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Valida os `efeitos` declarados no CATÁLOGO REAL, não em dado inventado.
 *
 * O [EfeitoInterpretadorTest] prova que o interpretador funciona; este prova que
 * o que foi escrito no JSON está certo. São coisas diferentes: um `alvo` com
 * erro de digitação passa por todo o interpretador sem erro e simplesmente
 * nunca aplica — falha invisível.
 *
 * Espelha o `scripts/validar_efeitos.py`, mas roda no gate de testes: quem
 * declarar efeito novo e errar o nome da perícia descobre no build, não no
 * aparelho.
 */
class EfeitosDeclaradosCatalogoTest {

    private companion object {
        /**
         * Alvos que NÃO são perícia do catálogo, mas são válidos de propósito.
         *
         * - `reacao` é o alvo reservado do Teste de Reação (`ReacaoRules`) — reusa
         *   o tipo "pericia" em vez de inventar um tipo novo só para ele.
         * - `*` é o **curinga "qualquer perícia"** (Lote TAL-1), para as três
         *   vantagens em que o livro dá uma **situação** e não uma lista: Toque
         *   Sensível (*"qualquer tarefa que utiliza o tato"*), Venturoso e
         *   Versátil. Enumerar as 278 perícias seria absurdo.
         *
         * Precisa estar em sincronia com `scripts/validar_efeitos.py`.
         */
        val ALVOS_RESERVADOS = setOf("reacao", TraitRuleRegistry.CURINGA_PERICIA)

        /** Os dez Talentos do livro (MB p.91-92), pelos ids do catálogo. */
        val OS_DEZ_TALENTOS = setOf(
            "artifice", "artista_talentoso", "companheiro_animal", "curandeiro",
            "dedos_verdes", "agente_cativante", "explorador",
            "habilidade_matematica", "habilidade_musical", "perspicacia_comercial"
        )
    }

    private val gson = Gson()

    /** Lê do módulo `app/` — é o diretório de trabalho do Gradle nos testes. */
    private fun asset(nome: String): File {
        val direto = File("src/main/assets/$nome")
        return if (direto.exists()) direto else File("app/src/main/assets/$nome")
    }

    private fun <T> lerLista(nome: String, tipo: java.lang.reflect.Type): List<T> {
        val arquivo = asset(nome)
        assertTrue("asset nao encontrado: ${arquivo.absolutePath}", arquivo.exists())
        return gson.fromJson(arquivo.readText(Charsets.UTF_8), tipo)
    }

    private data class TracoCru(
        val id: String = "",
        val nome: String = "",
        val options: List<Int>? = null,
        val efeitos: List<EfeitoDeclarado> = emptyList()
    )

    /** Traços crus de UM catálogo — quem precisa saber de qual lado veio. */
    private fun lerCru(nome: String): List<TracoCru> {
        val tipo = object : TypeToken<List<TracoCru>>() {}.type
        return lerLista(nome, tipo)
    }

    private data class PericiaCrua(val nome: String = "")

    private fun tracosComEfeitos(): List<TracoCru> {
        val tipo = object : TypeToken<List<TracoCru>>() {}.type
        return (lerLista<TracoCru>("vantagens.v3.json", tipo) +
                lerLista<TracoCru>("desvantagens.v2.json", tipo))
            .filter { it.efeitos.isNotEmpty() }
    }

    private fun nomesDePericia(): Set<String> {
        val tipo = object : TypeToken<List<PericiaCrua>>() {}.type
        return lerLista<PericiaCrua>("pericias.json", tipo)
            .mapNotNull { it.nome.takeIf { n -> n.isNotBlank() } }
            .toSet()
    }

    // --- as invariantes ---

    @Test
    fun `todo alvo de pericia existe com o nome EXATO no catalogo`() {
        // A armadilha: o catalogo usa "Navegação/NT" e "Perícia Forense/NT".
        // Declarar "Navegação" nao pega nada -- e nao gera erro nenhum.
        val pericias = nomesDePericia()
        val erros = mutableListOf<String>()
        tracosComEfeitos().forEach { traco ->
            traco.efeitos
                .filter { it.tipoResolvido == TipoEfeito.PERICIA }
                .forEach { efeito ->
                    if (efeito.alvo !in pericias && efeito.alvo !in ALVOS_RESERVADOS) {
                        erros.add("${traco.nome} [${traco.id}] -> pericia '${efeito.alvo}' nao existe")
                    }
                }
        }
        assertTrue(erros.joinToString("\n"), erros.isEmpty())
    }

    @Test
    fun `todo efeito tem tipo reconhecido`() {
        val erros = tracosComEfeitos().flatMap { traco ->
            traco.efeitos.filter { it.tipoResolvido == null }
                .map { "${traco.nome} [${traco.id}] -> tipo '${it.tipo}' desconhecido" }
        }
        assertTrue(erros.joinToString("\n"), erros.isEmpty())
    }

    @Test
    fun `nenhum efeito tem alvo em branco`() {
        val erros = tracosComEfeitos().flatMap { traco ->
            traco.efeitos.filter { it.alvo.isBlank() }
                .map { "${traco.nome} [${traco.id}] -> efeito sem alvo" }
        }
        assertTrue(erros.joinToString("\n"), erros.isEmpty())
    }

    @Test
    fun `nenhum efeito tem valor zero`() {
        // Valor 0 nao muda nada: ou e engano de digitacao, ou o efeito nao
        // deveria estar declarado. Quem usa `porOpcao` fica de fora: la o
        // `valor` nao e usado, a tabela e que manda.
        val erros = tracosComEfeitos().flatMap { traco ->
            traco.efeitos.filter { !it.ehPorOpcao && it.valor == 0 }
                .map { "${traco.nome} [${traco.id}] -> '${it.alvo}' com valor 0" }
        }
        assertTrue(erros.joinToString("\n"), erros.isEmpty())
    }

    @Test
    fun `toda chave de porOpcao existe entre as opcoes de custo do traco`() {
        // A armadilha do Lote OPCAO-1: chave que nao casa com nenhuma opcao do
        // traco nunca aplica -- e nao gera erro nenhum. Aparencia de desvantagem
        // guarda custo NEGATIVO; escrever "4" em vez de "-4" faria o efeito
        // existir no JSON e nao acontecer na ficha.
        val erros = mutableListOf<String>()
        (lerCru("vantagens.v3.json") + lerCru("desvantagens.v2.json"))
            .filter { it.efeitos.any { e -> e.ehPorOpcao } }
            .forEach { traco ->
                val opcoes = traco.options.orEmpty().toSet()
                if (opcoes.isEmpty()) {
                    erros.add("${traco.nome} [${traco.id}] usa porOpcao mas nao tem `options`")
                    return@forEach
                }
                traco.efeitos.filter { it.ehPorOpcao }.forEach { efeito ->
                    efeito.porOpcao.orEmpty().keys.forEach { chave ->
                        val custo = chave.toIntOrNull()
                        if (custo == null || custo !in opcoes) {
                            erros.add(
                                "${traco.nome} [${traco.id}] -> porOpcao['$chave'] " +
                                    "nao existe nas opcoes $opcoes"
                            )
                        }
                    }
                }
            }
        assertTrue(erros.joinToString("\n"), erros.isEmpty())
    }

    @Test
    fun `id repetido nos DOIS catalogos nao pode trocar de lado`() {
        // Seis ids existem em vantagens E desvantagens (aparencia, destino,
        // forma_de_sombras, reputacao, riqueza, status): sao escalas do GURPS
        // que atravessam o zero. Se os dois lados declararem efeitos, o sinal
        // TEM que ser oposto -- caso contrario alguem colou o bloco errado.
        val vant = lerCru("vantagens.v3.json").filter { it.efeitos.isNotEmpty() }.associateBy { it.id }
        val desv = lerCru("desvantagens.v2.json").filter { it.efeitos.isNotEmpty() }.associateBy { it.id }
        val erros = (vant.keys intersect desv.keys).flatMap { id ->
            val positivos = vant.getValue(id).efeitos.flatMap { it.porOpcao.orEmpty().values }
            val negativos = desv.getValue(id).efeitos.flatMap { it.porOpcao.orEmpty().values }
            buildList {
                if (positivos.any { it < 0 }) add("$id: lado VANTAGEM tem valor negativo")
                if (negativos.any { it > 0 }) add("$id: lado DESVANTAGEM tem valor positivo")
            }
        }
        assertTrue(erros.joinToString("\n"), erros.isEmpty())
    }

    @Test
    fun `traco com efeitos declarados NAO pode ter regra Kotlin`() {
        // A Kotlin vence o JSON: se os dois existem, o JSON e ignorado em
        // silencio e quem declarou acha que automatizou.
        val erros = tracosComEfeitos()
            .filter { TraitRuleRegistry.hasSpecialRule(it.id) }
            .map { "${it.nome} [${it.id}] tem `efeitos` E regra Kotlin -- o JSON seria ignorado" }
        assertTrue(erros.joinToString("\n"), erros.isEmpty())
    }

    // --- prova de que o trilho entrega o bônus ---

    @Test
    fun `Pendulear declarado no catalogo devolve mais 2 em Escalada`() {
        val pendulear = tracosComEfeitos().firstOrNull { it.id == "pendulear" }
        assertTrue("pendulear deveria estar declarado no catalogo", pendulear != null)

        val regra = EfeitoInterpretador.regraDe("pendulear", pendulear!!.efeitos)
        val mods = regra.getSkillModifiers(
            Personagem(nome = "Teste"),
            VantagemSelecionada(definicaoId = "pendulear", nome = "Pendulear")
        )
        assertEquals(2, mods["Escalada"])
    }

    @Test
    fun `Reconhecimento Social soma por nivel na reacao, sem condicao`() {
        // Lote REACAO-3. MB p.81: "5 pontos para cada bonus de +1 nos testes de
        // reacao". Nao tem "quando" no livro -- e o jeito como a sociedade
        // recebe o personagem, entao entra direto no total, igual ao Carisma.
        val traco = tracosComEfeitos().firstOrNull { it.id == "reconhecimento_social" }
        assertTrue("reconhecimento_social deveria estar declarado", traco != null)

        val efeito = traco!!.efeitos.single()
        assertEquals("reacao", efeito.alvo)
        assertTrue("precisa ser por nivel", efeito.porNivel)
        assertTrue("nao pode ser condicional", !efeito.ehCondicional)
        assertEquals(3, efeito.valorPara(nivel = 3))
    }

    @Test
    fun `modificador de reacao com publico especifico e CONDICIONAL`() {
        // A armadilha do lote: o livro quase sempre diz de QUEM vem o bonus
        // ("de correligionarios", "de criaturas Illuminati"). Somar sempre
        // daria bonus contra qualquer um -- inclusive contra quem o traco nao
        // alcanca. Estes PRECISAM ter condicao para virar caixinha na tela.
        val comPublico = setOf(
            "camaleao_social", "clericato", "iluminado",
            "por_dentro_da_moda", "reivindicar_hospitalidade", "destruidor_da_vida"
        )
        val erros = tracosComEfeitos()
            .filter { it.id in comPublico }
            .flatMap { traco ->
                traco.efeitos.filter { it.alvo == "reacao" && !it.ehCondicional }
                    .map { "${traco.nome} [${traco.id}] -> reacao sem condicao" }
            }
        assertTrue(erros.joinToString("\n"), erros.isEmpty())
        // E todos precisam estar declarados de fato.
        val declarados = tracosComEfeitos().map { it.id }.toSet()
        assertEquals(emptySet<String>(), comPublico - declarados)
    }

    @Test
    fun `nenhum nome de pericia do catalogo esta com acento comido`() {
        // Escrevendo os dez Talentos (Lote TAL-1) apareceram DOIS defeitos no
        // proprio catalogo: "Mendicncia" (sem o â) e "Analise de Mercado" (sem o
        // á). Os dois foram corrigidos; o segundo saiu de cruzar pericias.json
        // com pericias_v2_rules_map.json, que tinha a grafia certa.
        //
        // O sinal e uma sequencia de 3+ consoantes, que em portugues quase nao
        // existe fora de alguns encontros conhecidos. Nome errado aqui e bonus
        // mudo para sempre, porque o casamento e por nome exato.
        val encontrosValidos = listOf(
            "str", "scr", "nstr", "ntr", "mpl", "mpr", "ndr", "lstr", "nsp",
            "xpl", "rtr", "nfl", "nqu", "rqu", "lqu", "sch", "tch", "ngl",
            "ndl", "rst", "lst", "nst", "sgr", "mbr", "spr"
        )
        val suspeitos = nomesDePericia().filter { nome ->
            Regex("[bcdfghjklmnpqrstvwxz]{3,}").findAll(nome.lowercase()).any { m ->
                encontrosValidos.none { m.value.startsWith(it) || m.value.contains(it) }
            }
        }
        assertTrue("Nomes suspeitos de acento perdido: $suspeitos", suspeitos.isEmpty())
    }

    @Test
    fun `os dez Talentos estao declarados e com teto de quatro niveis`() {
        // Antes do Lote TAL-1 as dez existiam no catalogo SEM efeito nenhum:
        // quem comprava Artifice nivel 2 gastava 20 pontos e nao ganhava um
        // unico ponto de NH. Sao ~80 pericias no total.
        //
        // O `max = 4` e a regra do livro: "nunca pode ter mais que quatro niveis
        // em um determinado Talento".
        val porId = lerComMax("vantagens.v3.json").associateBy { it.id }
        val erros = OS_DEZ_TALENTOS.mapNotNull { id ->
            val t = porId[id]
            when {
                t == null -> "$id nao existe em vantagens.v3.json"
                t.efeitos.isEmpty() -> "$id esta sem efeitos declarados"
                t.max != 4 -> "$id deveria ter max=4 (MB p.91), tem ${t.max}"
                else -> null
            }
        }
        assertTrue(erros.joinToString("\n"), erros.isEmpty())
    }

    @Test
    fun `todo Talento tem exatamente um bonus de reacao, e ele e condicional`() {
        // O livro condiciona o bonus de reacao do Talento: so vale "se existir a
        // chance de ela ficar impressionada com sua aptidao (a critério do
        // Mestre)". Somar sempre daria bonus contra qualquer um.
        val porId = tracosComEfeitos().associateBy { it.id }
        val erros = OS_DEZ_TALENTOS.mapNotNull { id ->
            val reacoes = porId[id]?.efeitos.orEmpty().filter { it.alvo == "reacao" }
            when {
                reacoes.size != 1 -> "$id tem ${reacoes.size} efeitos de reacao, deveria ter 1"
                !reacoes.first().ehCondicional -> "$id: a reacao do Talento tem de ser condicional"
                !reacoes.first().porNivel -> "$id: a reacao do Talento e por nivel"
                else -> null
            }
        }
        assertTrue(erros.joinToString("\n"), erros.isEmpty())
    }

    @Test
    fun `so os tracos aprovados usam o curinga`() {
        // O curinga e poderoso: aparece em TODA pericia. Entao a lista de quem
        // pode usa-lo fica travada aqui, e crescer nela e uma decisao consciente.
        //
        // Todos dizem "qualquer tarefa que..." ou "qualquer teste" -- o livro da
        // uma SITUACAO, nao uma lista de pericias:
        //  - Toque Sensivel (+4 pelo tato), Venturoso (+1 em risco), Versatil
        //    (+1 em criatividade) -- Lote TAL-1, MB p.96.
        //  - Baixa Autoestima (-3 quando acha que nao tem chance) e Credulidade
        //    (-3 quando a credulidade pode ser explorada) -- Lote D-JSON,
        //    MB p.125 e p.130. As duas primeiras DESVANTAGENS a usar o curinga.
        val esperadas = setOf(
            "toque_sensivel", "venturoso", "versatil",
            "baixa_autoestima", "credulidade"
        )
        val comCuringa = tracosComEfeitos()
            .filter { t -> t.efeitos.any { it.alvo == TraitRuleRegistry.CURINGA_PERICIA } }
            .map { it.id }.toSet()
        assertEquals(esperadas, comCuringa)

        // E todo curinga tem de ser condicional: quem decide se vale e o Mestre.
        val semCondicao = tracosComEfeitos().flatMap { t ->
            t.efeitos.filter { it.alvo == TraitRuleRegistry.CURINGA_PERICIA && !it.ehCondicional }
                .map { "${t.nome} [${t.id}] usa o curinga sem condicao" }
        }
        assertTrue(semCondicao.joinToString("\n"), semCondicao.isEmpty())
    }

    private data class TracoComMax(
        val id: String = "",
        val max: Int? = null,
        val efeitos: List<EfeitoDeclarado> = emptyList()
    )

    private fun lerComMax(nome: String): List<TracoComMax> {
        val tipo = object : TypeToken<List<TracoComMax>>() {}.type
        return lerLista(nome, tipo)
    }

    @Test
    fun `Voz Melodiosa entrega bonus em varias pericias sociais`() {
        val voz = tracosComEfeitos().firstOrNull { it.id == "voz_melodiosa" }
        assertTrue("voz_melodiosa deveria estar declarada", voz != null)

        val mods = EfeitoInterpretador.regraDe("voz_melodiosa", voz!!.efeitos)
            .getSkillModifiers(
                Personagem(nome = "Teste"),
                VantagemSelecionada(definicaoId = "voz_melodiosa", nome = "Voz Melodiosa")
            )
        listOf("Atuação", "Canto", "Diplomacia", "Lábia", "Oratória", "Política", "Sex Appeal")
            .forEach { assertEquals("faltou bonus em $it", 2, mods[it]) }
    }
}
