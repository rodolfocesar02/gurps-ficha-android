package com.gurps.ficha.domain.loaders

import android.content.Context
import com.google.gson.JsonParser
import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.model.DesvantagemSelecionada
import com.gurps.ficha.model.ModeloRacial
import com.gurps.ficha.model.ModificadorSelecao
import com.gurps.ficha.model.PericiaRacial
import com.gurps.ficha.model.TipoCusto
import com.gurps.ficha.model.VantagemSelecionada

/**
 * Catálogo de raças (racas.v1.json, formato ENXUTO).
 *
 * Estratégia (validada com o Anão = 35 pts exatos): o JSON da raça guarda
 * só id/nível/autocontrole/modificador/NR — SEM custos. O [resolver]
 * reconstrói um [ModeloRacial] casando os ids contra os catálogos reais
 * (vantagens.v3/desvantagens.v2 via [DataRepository]) e deixa o
 * `custoTotal`/`custoFinal` recalcular pela regra (CharacterRules) —
 * imune a custo salvo errado. É também o "schema da IA" do modo híbrido.
 */

data class RacaTracoRef(
    val id: String? = null,
    val nome: String? = null,
    val nivel: Int = 1,
    val autocontrole: Int? = null,
    val custoEscolhido: Int? = null,
    val descricao: String? = null,
    val mods: List<String> = emptyList(),
    val metadados: Map<String, String>? = null
)

data class RacaPericiaRef(
    val nome: String = "",
    val diff: String = "M",
    val baseAtributo: String = "DX",
    val nivelRelativo: Int = 0
)

data class RacaDefinicao(
    val id: String = "",
    val nome: String = "",
    val pagina: Int = 0,
    val descricao: String = "",
    val atributos: Map<String, Int> = emptyMap(),
    val secundarios: Map<String, Int> = emptyMap(),
    val vantagens: List<RacaTracoRef> = emptyList(),
    val desvantagens: List<RacaTracoRef> = emptyList(),
    val qualidades: List<String> = emptyList(),
    val peculiaridades: List<String> = emptyList(),
    val pericias: List<RacaPericiaRef> = emptyList()
)

object RacaCatalogo {

    fun carregar(context: Context): List<RacaDefinicao> {
        return try {
            val texto = context.assets.open("racas.v1.json").bufferedReader().use { it.readText() }
            val root = JsonParser.parseString(texto)
            if (!root.isJsonObject) return emptyList()
            val arr = root.asJsonObject.getAsJsonArray("racas") ?: return emptyList()
            val gson = com.google.gson.Gson()
            arr.mapNotNull { el ->
                runCatching { gson.fromJson(el, RacaDefinicao::class.java) }.getOrNull()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Casa o ref (id preferido; senão nome, normalizado) contra o catálogo.
     * O fuzzy por nome é o que torna o modo IA viável: a IA escreve o nome
     * do livro, não o id interno.
     */
    private fun <T> casar(
        ref: RacaTracoRef,
        catalogo: List<T>,
        idDe: (T) -> String,
        nomeDe: (T) -> String
    ): T? {
        ref.id?.let { rid ->
            catalogo.firstOrNull { idDe(it).equals(rid, ignoreCase = true) }?.let { return it }
        }
        val alvo = norm(ref.nome ?: ref.id ?: return null)
        if (alvo.isBlank()) return null
        return catalogo.firstOrNull { norm(nomeDe(it)) == alvo }
            ?: catalogo.firstOrNull { norm(nomeDe(it)).contains(alvo) || alvo.contains(norm(nomeDe(it))) }
    }

    private fun norm(s: String): String =
        java.text.Normalizer.normalize(s.lowercase().trim(), java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')

    /**
     * Reconstrói o [ModeloRacial] da raça usando os catálogos reais.
     * Traço não encontrado é PULADO (não inventa custo) — o resolver
     * reporta isso via [naoResolvidos] para diagnóstico do Teste B.
     */
    fun resolver(raca: RacaDefinicao, repo: DataRepository): ResultadoResolucao {
        val naoResolvidos = mutableListOf<String>()

        val vantagens = raca.vantagens.mapNotNull { ref ->
            val def = casar(ref, repo.vantagens, { it.id }, { it.nome })
            if (def == null) { naoResolvidos.add("vantagem: ${ref.id ?: ref.nome}"); return@mapNotNull null }
            val mods = ref.mods.mapNotNull { modId ->
                def.modificadoresEspecificos.firstOrNull { it.id.equals(modId, ignoreCase = true) }
                    ?.let { md ->
                        ModificadorSelecao(
                            id = md.id, nome = md.nome,
                            valor = md.valor.replace(Regex("[^0-9-]"), "").toIntOrNull() ?: 0,
                            porNivel = false, niveis = 1, pagina = md.pagina
                        )
                    } ?: run { naoResolvidos.add("modificador: $modId (${def.id})"); null }
            }
            VantagemSelecionada(
                definicaoId = def.id,
                nome = def.nome,
                custoBase = if (def.tipoCusto == TipoCusto.POR_NIVEL) def.getCustoPorNivel() else def.getCustoBase(),
                nivel = ref.nivel,
                custoEscolhido = ref.custoEscolhido ?: def.getCustoBase(),
                descricao = ref.descricao ?: "",
                tipoCusto = def.tipoCusto,
                pagina = def.pagina,
                specialRule = def.specialRule,
                modificadores = mods,
                metadados = ref.metadados
            )
        }

        val desvantagens = raca.desvantagens.mapNotNull { ref ->
            val def = casar(ref, repo.desvantagens, { it.id }, { it.nome })
            if (def == null) { naoResolvidos.add("desvantagem: ${ref.id ?: ref.nome}"); return@mapNotNull null }
            DesvantagemSelecionada(
                definicaoId = def.id,
                nome = def.nome,
                custoBase = def.getCustoBase(),
                nivel = ref.nivel,
                custoEscolhido = ref.custoEscolhido ?: def.getCustoBase(),
                descricao = ref.descricao ?: "",
                autocontrole = ref.autocontrole,
                tipoCusto = def.tipoCusto,
                pagina = def.pagina,
                specialRule = def.specialRule
            )
        }

        val pericias = raca.pericias.map { p ->
            PericiaRacial(
                nome = p.nome,
                diff = p.diff,
                baseAtributo = p.baseAtributo,
                nivelRelativo = p.nivelRelativo,
                custo = com.gurps.ficha.domain.rules.CharacterRules
                    .calcularCustoPericiaRacial(p.diff, p.nivelRelativo)
            )
        }

        val modelo = ModeloRacial(
            nome = raca.nome,
            modForca = raca.atributos["st"] ?: 0,
            modDestreza = raca.atributos["dx"] ?: 0,
            modInteligencia = raca.atributos["iq"] ?: 0,
            modVitalidade = raca.atributos["ht"] ?: 0,
            modPontosVida = raca.secundarios["pv"] ?: 0,
            modVontade = raca.secundarios["vontade"] ?: 0,
            modPercepcao = raca.secundarios["percepcao"] ?: 0,
            modPontosFadiga = raca.secundarios["pf"] ?: 0,
            modVelocidadeBasica = (raca.secundarios["velocidadeBasica"] ?: 0).toFloat(),
            modDeslocamentoBasico = raca.secundarios["deslocamentoBasico"] ?: 0,
            vantagens = vantagens,
            desvantagens = desvantagens,
            pericias = pericias,
            qualidades = raca.qualidades,
            peculiaridades = raca.peculiaridades,
            descricao = raca.descricao
        )
        return ResultadoResolucao(modelo, naoResolvidos)
    }

    data class ResultadoResolucao(
        val modelo: ModeloRacial,
        val naoResolvidos: List<String>
    )
}
