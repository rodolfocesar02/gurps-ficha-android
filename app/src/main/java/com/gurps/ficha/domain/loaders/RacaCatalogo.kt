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

// tipo: "CONCEDIDA" (p.454, tabela p.170 — diff importa; ex. Anão
// "Comércio (M) IQ [2]-10") | "BONUS" (p.453, +1=2/+2=4/+3=6 linear,
// não concede a perícia; ex. Elfo "+1 em Arco [2]"). nivelRelativo é
// o NR (CONCEDIDA) ou o bônus +N ao NH (BONUS).
data class RacaPericiaRef(
    val nome: String = "",
    val diff: String = "M",
    val baseAtributo: String = "DX",
    val nivelRelativo: Int = 0,
    val tipo: String = "CONCEDIDA"
)

// JSON: { "atributo":"ST", "tipo":"TAMANHO", "percentual":-10 }
data class RacaLimitacaoRef(
    val atributo: String = "ST",
    val tipo: String = "TAMANHO",
    val percentual: Int = 0
)

data class RacaDefinicao(
    val id: String = "",
    val nome: String = "",
    // String livre: o livro/edição varia ("Cataclismo 189"), não é só
    // número. Int quebrava o parse Gson e a raça sumia silenciosamente.
    val pagina: String = "",
    val descricao: String = "",
    val atributos: Map<String, Int> = emptyMap(),
    // Limitações % de custo de atributo (Tamanho ST/PV; Manuseadores
    // Precários ST/DX). A IA informa atributo+tipo+% lidos do texto
    // do livro ("ST+8 (Tamanho, -10%)"). Vazio = sem limitação.
    val limitacoesAtributo: List<RacaLimitacaoRef> = emptyList(),
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
                // 1) modificador específico DA vantagem (ex: pele_resistente)
                val esp = def.modificadoresEspecificos.firstOrNull { it.id.equals(modId, ignoreCase = true) }
                if (esp != null) {
                    return@mapNotNull ModificadorSelecao(
                        id = esp.id, nome = esp.nome,
                        valor = esp.valor.replace(Regex("[^0-9-]"), "").toIntOrNull() ?: 0,
                        porNivel = false, niveis = 1, pagina = esp.pagina,
                        bonusBase = 0 // mods_especificos não têm base fixa hoje
                    )
                }
                // 2) fallback: catálogo GLOBAL de modificadores
                // (modificadores.v1.json) — necessário p/ metacaracterísticas
                // (ex: Insubstancialidade + "Afeta a Matéria +100%"). Casa
                // por id; senão por nome normalizado.
                val ger = repo.modificadoresGerais.firstOrNull {
                    it.id.equals(modId, ignoreCase = true)
                } ?: repo.modificadoresGerais.firstOrNull {
                    norm(it.nome) == norm(modId)
                }
                if (ger != null) {
                    ModificadorSelecao(
                        id = ger.id, nome = ger.nome,
                        valor = ger.valor.replace(Regex("[^0-9-]"), "").toIntOrNull() ?: 0,
                        // Propaga porNivel do catálogo (Lote 194: Cíclico
                        // é porNivel=true; antes era hardcoded false).
                        porNivel = ger.porNivel, niveis = 1,
                        pagina = ger.pagina,
                        bonusBase = ger.bonusBase
                    )
                } else { naoResolvidos.add("modificador: $modId (${def.id})"); null }
            }
            // Resistente: o custo vem de raridade×grau (calculado pelo
            // app, igual ao dialog) — a IA/catálogo só fornece os
            // metadados do texto do livro, sem custo hardcoded. Demais
            // tipos especiais (variável/escolha) usam o custoEscolhido
            // informado; por_nivel/fixo derivam do catálogo.
            val custoEsc = run {
                val md = ref.metadados
                if (md != null && def.id.equals("resistente", ignoreCase = true)) {
                    val rar = md["raridade"]?.toIntOrNull()
                    val grau = md["grau"]?.toFloatOrNull()
                    if (rar != null && grau != null)
                        com.gurps.ficha.domain.rules.CharacterRules
                            .calcularCustoResistente(rar, grau)
                    else ref.custoEscolhido ?: def.getCustoBase()
                } else {
                    ref.custoEscolhido ?: def.getCustoBase()
                }
            }
            VantagemSelecionada(
                definicaoId = def.id,
                nome = def.nome,
                custoBase = if (def.tipoCusto == TipoCusto.POR_NIVEL) def.getCustoPorNivel() else def.getCustoBase(),
                nivel = ref.nivel,
                custoEscolhido = custoEsc,
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
            val tipoP = runCatching {
                com.gurps.ficha.model.TipoPericiaRacial.valueOf(p.tipo.uppercase())
            }.getOrDefault(com.gurps.ficha.model.TipoPericiaRacial.CONCEDIDA)
            val custoP = if (tipoP == com.gurps.ficha.model.TipoPericiaRacial.BONUS)
                com.gurps.ficha.domain.rules.CharacterRules
                    .calcularCustoBonusPericiaRacial(p.nivelRelativo)   // p.453
            else
                com.gurps.ficha.domain.rules.CharacterRules
                    .calcularCustoPericiaRacial(p.diff, p.nivelRelativo) // p.454/170
            PericiaRacial(
                nome = p.nome,
                diff = p.diff,
                baseAtributo = p.baseAtributo,
                nivelRelativo = p.nivelRelativo,
                custo = custoP,
                tipo = tipoP
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
            limitacoesAtributo = raca.limitacoesAtributo.mapNotNull { ref ->
                val attr = runCatching {
                    com.gurps.ficha.model.AtributoLimitavel.valueOf(ref.atributo.uppercase())
                }.getOrNull()
                val tipo = runCatching {
                    com.gurps.ficha.model.TipoLimitacaoAtributo.valueOf(ref.tipo.uppercase())
                }.getOrNull()
                if (attr == null || tipo == null || ref.percentual == 0) {
                    naoResolvidos.add("limitação: ${ref.atributo}/${ref.tipo}"); null
                } else if (attr !in tipo.aceitaEm) {
                    naoResolvidos.add("limitação ${tipo.rotulo} não vale p/ ${attr.name}"); null
                } else com.gurps.ficha.model.LimitacaoAtributo(attr, tipo, ref.percentual)
            },
            descricao = raca.descricao
        )
        return ResultadoResolucao(modelo, naoResolvidos)
    }

    data class ResultadoResolucao(
        val modelo: ModeloRacial,
        val naoResolvidos: List<String>
    )
}
