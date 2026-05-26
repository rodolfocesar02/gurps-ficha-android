package com.gurps.ficha.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.util.Base64
import com.gurps.ficha.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class EstadoLive { OCIOSO, CONECTANDO, OUVINDO, FALANDO, ERRO }

// Mantida para compatibilidade com FichaCustomNavigationBar (anel visual do ícone de voz)
enum class EstadoVoz { OCIOSO, ESCUTANDO, PROCESSANDO, ERRO }

class GeminiLiveService(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.IO)
    private var webSocket: WebSocket? = null
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var capturaJob: Job? = null
    private var reproducaoJob: Job? = null
    private var keepAliveJob: Job? = null
    // Recriado a cada sessão; máximo 200 chunks (~20s de buffer)
    private var audioChannel = Channel<ByteArray>(capacity = 200)
    private var sessaoAtiva = false
    // Controle de reconexão — Runnable salvo para poder cancelar se usuário encerrar manualmente
    private var reconexaoPendente: Runnable? = null
    // Token de session resumption — permite reconectar na mesma sessão lógica (contexto preservado)
    @Volatile private var sessionResumptionToken: String? = null
    // Acumula texto do turno inteiro (várias mensagens) para exibir no chat
    @Volatile private var pendingTextoFallback = ""
    @Volatile private var turnoTemAudio = false
    // Bloqueia envio de microfone enquanto modelo fala — evita auto-interrupção
    @Volatile private var modeloFalando = false

    // Callbacks para o FichaScreen
    var onEstado: (EstadoLive) -> Unit = {}
    var onTranscricaoUsuario: (String) -> Unit = {}
    var onRespostaMestre: (String) -> Unit = {}
    var onToolCall: (nome: String, args: JSONObject) -> JSONObject = { _, _ -> JSONObject() }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // sem timeout — WebSocket é persistente
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val systemPrompt = """
IDIOMA OBRIGATÓRIO: Responda SEMPRE em português brasileiro. NUNCA use inglês, nem para pensar em voz alta, nem para comentários internos. Todo output de texto e fala deve ser em PT-BR.

Você é o Mestre IA de GURPS — um mestre de campanha experiente, sábio e com personalidade própria.
Fale sempre em português brasileiro, de forma natural e conversacional.
Seu nome é Mestre.

FERRAMENTAS DISPONÍVEIS E QUANDO USAR:
- lerFicha(secao): lê atributos, vantagens, desvantagens, pericias, tecnicas, magias, equipamentos, pontos
- buscarCatalogo(tipo, query): OBRIGATÓRIO antes de adicionar qualquer trait — retorna IDs e nomes corretos. Tipos: vantagem, desvantagem, pericia, magia, tecnica. NUNCA invente um ID sem buscar antes.
- editarFicha(operacao, secao, alvo, valor): adiciona, remove ou altera qualquer item da ficha. operacao: adicionar|remover|alterar. secao: vantagens|desvantagens|pericias|tecnicas|magias|equipamentos|atributos
- trilhaDeMagias(magia_alvo): GPS de magias — mostra cadeia de pré-requisitos e trilha mais rápida até a magia desejada
- consultarManual(termos, livro?): busca regras no Códex de GURPS. Use ANTES de responder qualquer dúvida de regra. Use livro= quando souber de qual livro a regra vem — melhora muito a precisão.
- forjador_buscar_racas(query, tipo): lista raças e metacaracterísticas disponíveis. Use para descobrir IDs antes de aplicar. tipo=raca para raças jogáveis, tipo=meta para metacaracterísticas (Vampiro, Fantasma, etc).
- forjador_aplicar_modelo_racial(id, tipo): aplica modelo racial completo ao personagem (atributos + vantagens + desvantagens + perícias da raça). Sempre usar forjador_buscar_racas antes para obter o ID correto.

QUAL LIVRO USAR em consultarManual:
- livro="Gun Fu"           → técnicas e regras de armas de fogo, tiro cinematográfico
- livro="Artes Marciais"   → técnicas corpo a corpo, estilos marciais, combate desarmado
- livro="Magia"            → magias, escolas, alquimia, encantamentos, runas
- livro="Módulo Básico"    → tudo mais: atributos, vantagens, desvantagens, perícias, manobras, combate geral, equipamentos, tabelas de regras
- livro="Pyramid Aquático" → regras de ambientes submersos: combate subaquático, pressão, narcose, descompressão, criaturas aquáticas
- sem livro=               → quando a pergunta cruza mais de um livro

ÍNDICE DO MANUAL (use para decidir onde buscar):
Agachar, 368. Agarrar e segurar, 370. Aparar, 51, 96, 325, 327, 376. Armadura, 282–286.
Armas corpo a corpo, 271–275. Armas à distância, 275–277, 278–281.
Ataques à distância, 326, 372. Ataques enganosos, 369. Ataques surpresa, 393.
Atordoamento, 44, 420. Bloqueio, 51, 325, 327, 375. Cadência de Tiro, 270, 373.
Chave de braço, 371, 403. Chi, 33, 92, 195, 219.
Cobertura, 377, 407, 559. Combate corporal, 391. Combate desarmado, 370, 376, 379.
Combate montado, 396–398. Dano, 15, 327, 377. Dano penetrante, 378.
Defendendo, 326, 374. Defesas ativas, 326, 363, 374, 548. Derrubar, 370.
Deslocamento básico, 17. Disparo com mira, 372. Disputas, 348–349. Divisor de armadura, 378.
Encontrão, 371. Erros críticos, 381; tabela, 556–557.
Escudos, 287, 374. Esforço adicional, 356.
Esquiva, 17, 51, 325, 326, 374. Esquiva acrobática, 375. Evadir, 368.
Explosões, 414–415. Fadiga, 16, 328, 426. Ferimentos graves, 420.
Garrotes, 406. Golpe Letal, 404. Golpe Rápido, 42, 96, 370.
Golpes fulminantes, 381; tabela, 557.
Hexágonos, 384. Imobilizando o adversário, 401. Incapacitado, 51, 420–423.
Iniciativa, 393. Joelhada, 404. Lesões, 327, 377, 380, 418–425. Levantamento, 14, 15, 354.
Mágicas de Bloqueio, 242. Mágicas de Projétil, 242. Manobra Aguardar, 324, 366.
Manobra Apontar, 43, 324, 364. Manobra Ataque Total, 42, 324, 365.
Manobra Ataque, 324, 365. Manobra Avaliar, 325, 364. Manobra Avançar e Atacar, 325, 365.
Manobra Concentrar, 325, 366. Manobra Defesa Total, 325, 366.
Manobra Deslocamento, 325, 364. Manobra Fazer Nada, 325, 364.
Manobra Fintar, 325, 365. Manobra Mudança de Posição, 325, 364.
Manobra Preparar, 325, 366. Manobras, 324, 363; tabela, 551.
Mata-leão, 371, 404. Mau Funcionamento, 279, 382, 407.
Mergulho de proteção, 377, 413. Modificador de ferimento, 379.
Modificador de Tamanho, 19, 372, 402. Movimento e combate, 367.
Nocaute e atordoamento, 420. Passo em manobras, 368, 386.
Ponto de Impacto, 369, 398; tabela, 552–555. Posições, 367; tabela, 551.
Queda, 432. Recuo, 271. Retirada com defesa ativa, 377, 391.
Sangramento, 50, 420. Sequência de combate, 324, 362. Submissão em combate, 370.
Sucesso decisivo, 347. Tabela de Erro Crítico, 556. Tabela de Golpe Fulminante, 557.
Tabela de Modificadores à Distância, 548. Tabela de Modificadores de Ataque Corpo a Corpo, 547.
Tabela de Ponto de Impacto, 552–555. Tabela de Tamanho e Velocidade/Distância, 551.
Técnicas de combate, 230. Torcer Membros, 371, 404. Truques Sujos, 405.
Venenos, 43, 437–439. Verificações de Pânico, 53, 60, 94, 360.
Vantagens (lista), 32–118. Desvantagens (lista), 119–165. Perícias (lista), 167–233.

PROTOCOLO DE BUSCA — DÚVIDAS DE REGRAS:
- ESPERE saber QUAL é a dúvida antes de chamar consultarManual. Se o usuário disser "tenho uma dúvida" ou algo vago, PERGUNTE "Qual é a sua dúvida?" — NÃO chame consultarManual ainda.
- Somente quando a pergunta específica for clara, use consultarManual ANTES de responder — nunca invente
- FIDELIDADE EXCLUSIVA AO CÓDEX: use SOMENTE o que estiver nos chunks retornados
- Decomponha perguntas complexas: busque cada conceito separadamente
- Se a regra não estiver no Códex, diga: "Não localizei essa regra nos manuais disponíveis"
- Use termos técnicos de GURPS nas buscas: "ST", "DX", "penalidade", "modificador", nome exato das regras
- Queries CURTAS e ESPECÍFICAS (máx 6 palavras) — nunca coloque a pergunta inteira

QUANDO NÃO ENCONTRAR:
- Declare: "Não encontrei essa regra nos manuais disponíveis."
- Se encontrou regras parcialmente relacionadas, componha uma interpretação e avise que é interpretação, não regra oficial.
- NUNCA invente números ou afirme regras que não vieram de consultarManual.

PROTOCOLO OBRIGATÓRIO DE CÁLCULO (quando a regra envolver número ou fórmula):
1. Cite a regra: "Segundo [Livro, Pág]..."
2. Identifique os valores: "O alcance da arma é X, o divisor é Y..."
3. Calcule em voz alta: "Então X dividido por Y é igual a Z..."
4. Conclua: "Portanto, o alcance efetivo é Z metros"
NUNCA dê resultado sem explicar o cálculo. NUNCA confunda stat da arma com distância cênica.

FLUXO PARA RAÇAS E METACARACTERÍSTICAS:
1. forjador_buscar_racas() para ver quais raças/metas existem no catálogo
2. forjador_aplicar_modelo_racial(id, tipo) para aplicar — aplica TUDO automaticamente (atributos, vantagens, desvantagens, perícias)
3. Confirme em voz o que foi aplicado e quantos pontos restam

CAMPOS DIRETOS (não precisam de buscarCatalogo):
- Nome do personagem: editarFicha(alterar, atributos, nome, "Aragorn")
- História/background: editarFicha(alterar, atributos, historia, "Era um bruxo...")
- Atributos primários ST/DX/IQ/HT: editarFicha(alterar, atributos, ST, "12")
- PF extra (fadiga): editarFicha(alterar, atributos, PF, "2") — valor é o modificador sobre HT
- Pontos iniciais: editarFicha(alterar, atributos, pontosIniciais, "150")
- Qualidades: editarFicha(adicionar, qualidades, "Corajoso", "") — texto livre
- Peculiaridades: editarFicha(adicionar, peculiaridades, "Fala pouco", "") — texto livre

FLUXO PARA VANTAGENS/DESVANTAGENS/PERÍCIAS/MAGIAS/TÉCNICAS/EQUIPAMENTOS:
1. buscarCatalogo primeiro para obter o ID correto
2. editarFicha com o ID retornado
3. Confirmar em voz o que foi feito e quantos pontos restam

MÚLTIPLAS EDIÇÕES DE UMA VEZ:
- Chame editarFicha várias vezes em sequência sem pausar — o sistema aceita
- buscarCatalogo também pode ser chamado múltiplas vezes em paralelo para buscar vários itens
- Ao montar uma ficha completa: defina nome → pontosIniciais → atributos → vantagens → desvantagens → perícias → magias → historia

MAGIAS — REGRAS ESPECIAIS:
- Para adicionar uma magia, primeiro use trilhaDeMagias para verificar os pré-requisitos
- O sistema BLOQUEIA magias sem pré-requisito (igual ao botão na tela)
- Se faltarem pré-requisitos, explique a trilha e ofereça adicionar na ordem correta

ESTILO DE VOZ:
- Fale enquanto pensa — não fique em silêncio enquanto processa ferramentas
- Respostas curtas e diretas são melhores que longas
- Personalidade: sábio, justo, levemente dramático
- Nunca invente regras — se não encontrar, diga claramente

NUNCA:
- Adicionar trait sem buscarCatalogo primeiro
- Responder dúvidas de regra sem consultar o manual primeiro
- Modificar a ficha sem confirmar o resultado depois
- Usar conhecimento geral de IA sobre GURPS — usar apenas o Códex
""".trimIndent()

    private fun buildSetupMessage(): String {
        val tools = JSONArray().apply {
            put(JSONObject().apply {
                put("function_declarations", JSONArray().apply {

                    // ── Leitura da ficha ──────────────────────────────────────────────
                    put(buildFuncao("lerFicha",
                        "Lê uma seção da ficha do personagem. Use antes de qualquer modificação para verificar o estado atual.",
                        JSONObject().apply {
                            put("type", "object")
                            put("properties", JSONObject().apply {
                                put("secao", JSONObject().apply {
                                    put("type", "string")
                                    put("description", "atributos | vantagens | desvantagens | pericias | tecnicas | magias | equipamentos | qualidades | peculiaridades | pontos")
                                })
                            })
                            put("required", JSONArray().put("secao"))
                        }
                    ))

                    // ── Busca no catálogo (previne alucinação de IDs) ────────────────
                    put(buildFuncao("buscarCatalogo",
                        "Busca itens no catálogo oficial de GURPS. OBRIGATÓRIO antes de adicionar qualquer vantagem, desvantagem, perícia, magia ou técnica — retorna IDs e nomes corretos para usar em editarFicha.",
                        JSONObject().apply {
                            put("type", "object")
                            put("properties", JSONObject().apply {
                                put("tipo", JSONObject().apply {
                                    put("type", "string")
                                    put("description", "vantagem | desvantagem | pericia | magia | tecnica")
                                })
                                put("query", JSONObject().apply {
                                    put("type", "string")
                                    put("description", "Palavra-chave de busca (ex: 'combate', 'fogo', 'furtividade')")
                                })
                            })
                            put("required", JSONArray().put("tipo").put("query"))
                        }
                    ))

                    // ── Edição unificada da ficha ─────────────────────────────────────
                    put(buildFuncao("editarFicha",
                        "Edita a ficha DIRETAMENTE. ATRIBUTOS PRIMÁRIOS: secao=atributos, alvo=ST/DX/IQ/HT, valor=14. NOME: secao=atributos, alvo=nome, valor='Aragorn'. HISTÓRIA: secao=atributos, alvo=historia, valor='texto...'. PF extra: secao=atributos, alvo=PF, valor=2 (modificador sobre HT). PONTOS: secao=atributos, alvo=pontosIniciais, valor=150. LISTAS: secao=vantagens/desvantagens/pericias/tecnicas/magias/equipamentos/qualidades/peculiaridades — use ID retornado por buscarCatalogo.",
                        JSONObject().apply {
                            put("type", "object")
                            put("properties", JSONObject().apply {
                                put("operacao", JSONObject().apply {
                                    put("type", "string")
                                    put("description", "adicionar | remover | alterar")
                                })
                                put("secao", JSONObject().apply {
                                    put("type", "string")
                                    put("description", "atributos | vantagens | desvantagens | pericias | tecnicas | magias | equipamentos | qualidades | peculiaridades")
                                })
                                put("alvo", JSONObject().apply {
                                    put("type", "string")
                                    put("description", "ST/DX/IQ/HT | nome | historia | PF | pontosIniciais | ID do item do catálogo")
                                })
                                put("valor", JSONObject().apply {
                                    put("type", "string")
                                    put("description", "Atributo numérico: '14'. Nome: 'Aragorn'. Historia: texto livre. PF: '2' (mod). Perícia: 'nivel=14;esp=Florestas'. Vantagem: 'nivel=3'. Técnica: 'nivel=4;periciaBase=<id>'. Qualidade/Peculiaridade: texto livre.")
                                })
                            })
                            put("required", JSONArray().put("operacao").put("secao").put("alvo"))
                        }
                    ))

                    // ── GPS de Magias ─────────────────────────────────────────────────
                    put(buildFuncao("trilhaDeMagias",
                        "GPS de Magias: calcula a trilha mais rápida de pré-requisitos para aprender uma magia alvo. Use antes de tentar adicionar qualquer magia para verificar se é possível e qual a ordem correta.",
                        JSONObject().apply {
                            put("type", "object")
                            put("properties", JSONObject().apply {
                                put("magia_alvo", JSONObject().apply {
                                    put("type", "string")
                                    put("description", "ID da magia alvo (use buscarCatalogo tipo=magia para obter o ID)")
                                })
                            })
                            put("required", JSONArray().put("magia_alvo"))
                        }
                    ))

                    // ── RAG — Consulta ao Códex ───────────────────────────────────────
                    put(buildFuncao("consultarManual",
                        "Busca regras no Códex de GURPS (RAG). SEMPRE use antes de responder qualquer dúvida de regra — nunca invente. Pode chamar múltiplas vezes com termos diferentes. Use livro= para melhorar a precisão.",
                        JSONObject().apply {
                            put("type", "object")
                            put("properties", JSONObject().apply {
                                put("termos", JSONObject().apply {
                                    put("type", "string")
                                    put("description", "Termos técnicos de GURPS para buscar. Máximo 6 palavras, específicos por conceito isolado.")
                                })
                                put("livro", JSONObject().apply {
                                    put("type", "string")
                                    put("description", "Filtra a busca por livro. Use 'Gun Fu' para técnicas de armas de fogo. Use 'Artes Marciais' para técnicas corpo a corpo e estilos marciais. Use 'Magia' para magias, alquimia e encantamentos. Use 'Módulo Básico' para regras gerais, manobras, tabelas, atributos, vantagens, perícias. Use 'Pyramid Aquático' para combate subaquático, pressão, narcose. Omita para buscar em todos os livros.")
                                    put("enum", JSONArray().put("Módulo Básico").put("Artes Marciais").put("Magia").put("Gun Fu").put("Pyramid Aquático"))
                                })
                            })
                            put("required", JSONArray().put("termos"))
                        }
                    ))

                    // ── Raças e Metacaracterísticas ───────────────────────────────────
                    put(buildFuncao("forjador_buscar_racas",
                        "Lista raças e metacaracterísticas disponíveis no catálogo GURPS. Use ANTES de aplicar qualquer modelo racial para obter os IDs disponíveis. Tipos: 'raca' (Anão, Elfo, Halfling...) ou 'meta' (Vampiro, Fantasma, Licantropo...).",
                        JSONObject().apply {
                            put("type", "object")
                            put("properties", JSONObject().apply {
                                put("query", JSONObject().apply {
                                    put("type", "string")
                                    put("description", "Filtro por nome (opcional). Ex: 'elfo', 'anao'. Vazio lista tudo.")
                                })
                                put("tipo", JSONObject().apply {
                                    put("type", "string")
                                    put("description", "raca | meta | todos")
                                })
                            })
                            put("required", JSONArray())
                        }
                    ))

                    put(buildFuncao("forjador_aplicar_modelo_racial",
                        "Aplica um modelo racial (raça ou metacaracterística) ao personagem, adicionando automaticamente TODOS os traços da raça: modificadores de atributos, vantagens, desvantagens e perícias. Use forjador_buscar_racas primeiro para obter o ID correto.",
                        JSONObject().apply {
                            put("type", "object")
                            put("properties", JSONObject().apply {
                                put("id", JSONObject().apply {
                                    put("type", "string")
                                    put("description", "ID da raça ou metacaracterística obtido via forjador_buscar_racas. Ex: 'anao', 'elfo', 'vampiro'")
                                })
                                put("tipo", JSONObject().apply {
                                    put("type", "string")
                                    put("description", "raca | meta. Padrão: raca")
                                })
                            })
                            put("required", JSONArray().put("id"))
                        }
                    ))
                })
            })
        }

        val token = sessionResumptionToken
        return JSONObject().apply {
            put("setup", JSONObject().apply {
                put("model", BuildConfig.GEMINI_LIVE_MODEL)
                // Session resumption transparente: servidor gerencia o token automaticamente
                // e garante que mensagens enviadas durante a queda não se percam
                val resumptionCfg = JSONObject().apply {
                    put("transparent", true)
                    if (token != null) {
                        put("handle", token)
                        android.util.Log.i("GeminiLive", "║  Usando session resumption token (contexto preservado)")
                    }
                }
                put("sessionResumptionConfig", resumptionCfg)
                // Context window compression: evita limite de 15min e crescimento de 120k+ tokens
                // Quando o contexto passar de 100k tokens o servidor comprime para ~4k automaticamente
                put("contextWindowCompression", JSONObject().apply {
                    put("triggerTokens", 100000)
                    put("slidingWindow", JSONObject().apply {
                        put("targetTokens", 4000)
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseModalities", JSONArray().apply { put("AUDIO") })
                    put("speechConfig", JSONObject().apply {
                        put("voiceConfig", JSONObject().apply {
                            put("prebuiltVoiceConfig", JSONObject().apply {
                                put("voiceName", BuildConfig.GEMINI_LIVE_VOICE)
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemPrompt) })
                    })
                })
                put("tools", tools)
            })
        }.toString()
    }

    private fun buildFuncao(nome: String, descricao: String, params: JSONObject): JSONObject {
        return JSONObject().apply {
            put("name", nome)
            put("description", descricao)
            // Gemini Live exige parameters mesmo quando vazio
            val p = if (params.length() > 0) params else JSONObject().apply {
                put("type", "object")
                put("properties", JSONObject())
            }
            put("parameters", p)
        }
    }

    // Histórico resumido de turnos — sobrevive ao goAway para reinjeção na reconexão
    private data class TurnoResumido(val usuario: String, val mestre: String)
    private val historicoTurnos = ArrayDeque<TurnoResumido>(5)
    private var ultimaPerguntaUsuario: String = ""

    // Pergunta que estava sendo processada quando a conexão caiu — reinjetar como contexto
    @Volatile private var perguntaInterrompida: String? = null

    private var contextoFichaParaSaudacao: String = ""
    private var reconectandoApos: String = "" // "goAway" | "fechado" | ""

    fun iniciarSessao(contextoFicha: String) {
        if (sessaoAtiva) return
        contextoFichaParaSaudacao = contextoFicha
        mainHandler.post { onEstado(EstadoLive.CONECTANDO) }

        val keyPreview = BuildConfig.MESTRE_IA_GEMINI_KEY.take(8) + "..."
        val url = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent?key=${BuildConfig.MESTRE_IA_GEMINI_KEY}"
        android.util.Log.i("GeminiLive", "╔══ INICIANDO SESSÃO ══════════════════")
        android.util.Log.i("GeminiLive", "║  Modelo: ${BuildConfig.GEMINI_LIVE_MODEL}")
        android.util.Log.i("GeminiLive", "║  Voz: ${BuildConfig.GEMINI_LIVE_VOICE}")
        android.util.Log.i("GeminiLive", "║  Chave: $keyPreview")
        android.util.Log.i("GeminiLive", "║  Conectando ao WebSocket...")

        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                android.util.Log.i("GeminiLive", "║  WebSocket ABERTO (HTTP ${response.code})")
                val setup = buildSetupMessage()
                android.util.Log.i("GeminiLive", "║  Enviando setup (${setup.length} chars) — aguardando setupComplete...")
                // Log do setup em chunks para ver o JSON completo no Logcat
                setup.chunked(800).forEachIndexed { i, chunk ->
                    android.util.Log.i("GeminiLive", "║  SETUP[$i]: $chunk")
                }
                ws.send(setup.encodeUtf8())
                // NÃO enviar mais nada aqui — aguardar setupComplete no onMessage
            }

            override fun onMessage(ws: WebSocket, text: String) {
                val temAudio = text.contains("audio/pcm") && text.contains("\"data\"")
                val ehResumption = text.contains("sessionResumptionUpdate")
                if (!temAudio && !ehResumption) {
                    android.util.Log.i("GeminiLive", "◄ MSG texto (${text.length} chars): ${text.take(300)}")
                } else if (ehResumption) {
                    android.util.Log.d("GeminiLive", "◄ sessionResumptionUpdate (suprimido)")
                }
                processarMensagemServidor(text)
            }

            override fun onMessage(ws: WebSocket, bytes: ByteString) {
                val text = bytes.utf8()
                val temAudio = text.contains("audio/pcm") && text.contains("\"data\"")
                val ehResumption = text.contains("sessionResumptionUpdate")
                if (!temAudio && !ehResumption) {
                    android.util.Log.i("GeminiLive", "◄ MSG binário (${bytes.size} bytes): ${text.take(300)}")
                } else if (ehResumption) {
                    android.util.Log.d("GeminiLive", "◄ sessionResumptionUpdate (suprimido)")
                }
                processarMensagemServidor(text)
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                val httpCode = response?.code ?: -1
                val body = response?.body?.string()?.take(300) ?: "sem body"
                android.util.Log.e("GeminiLive", "╔══ FALHA WEBSOCKET ══════════════════")
                android.util.Log.e("GeminiLive", "║  Erro: ${t.javaClass.simpleName}: ${t.message}")
                android.util.Log.e("GeminiLive", "║  HTTP: $httpCode")
                android.util.Log.e("GeminiLive", "║  Body: $body")
                android.util.Log.e("GeminiLive", "╚═════════════════════════════════════")
                encerrar()
                mainHandler.post { onEstado(EstadoLive.ERRO) }
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                android.util.Log.w("GeminiLive", "WebSocket FECHADO: code=$code reason=$reason")
                encerrar()
                // Fechamento inesperado (não foi o usuário que encerrou) → reconectar
                if (code != 1000) {
                    android.util.Log.i("GeminiLive", "Fechamento inesperado (code=$code) — reconectando...")
                    reconectarAutomaticamente("fechado")
                } else {
                    mainHandler.post { onEstado(EstadoLive.OCIOSO) }
                }
            }
        })
    }

    private fun processarMensagemServidor(json: String) {
        try {
            val obj = JSONObject(json)

            // Session resumption — salva token para reconexão transparente
            if (obj.has("sessionResumptionUpdate")) {
                val update = obj.getJSONObject("sessionResumptionUpdate")
                val token = update.optString("newHandle", "")
                if (token.isNotBlank()) {
                    sessionResumptionToken = token
                    android.util.Log.d("GeminiLive", "◄ sessionResumptionToken atualizado (${token.take(20)}...)")
                }
                return
            }

            // setupComplete — servidor confirmou o setup, agora podemos enviar mensagens
            if (obj.has("setupComplete")) {
                android.util.Log.i("GeminiLive", "║  setupComplete recebido — enviando contexto e saudação")
                val ws = webSocket ?: return

                val contextoFicha = contextoFichaParaSaudacao
                val foiReconexao = reconectandoApos.isNotBlank()
                reconectandoApos = ""
                val perguntaInterrompidaAgora = perguntaInterrompida
                perguntaInterrompida = null

                // Se usou session resumption, o modelo já tem todo o contexto — não reinjetar nada
                val usouResumption = sessionResumptionToken != null && foiReconexao
                if (!usouResumption) {
                    // Primeira abertura ou reconexão sem token — injeta contexto da ficha
                    val ctxMsg = JSONObject().apply {
                        put("clientContent", JSONObject().apply {
                            put("turns", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("role", "user")
                                    put("parts", JSONArray().apply {
                                        put(JSONObject().apply { put("text", "Contexto atual da ficha: $contextoFicha") })
                                    })
                                })
                            })
                            put("turnComplete", false)
                        })
                    }
                    android.util.Log.i("GeminiLive", "║  Enviando contexto da ficha (${contextoFicha.length} chars)...")
                    ws.send(ctxMsg.toString().encodeUtf8())

                    // Reinjetar histórico só se não tiver token de resumption
                    val resumo = buildResumoHistorico()
                    if (resumo.isNotBlank()) {
                        val histMsg = JSONObject().apply {
                            put("clientContent", JSONObject().apply {
                                put("turns", JSONArray().apply {
                                    put(JSONObject().apply {
                                        put("role", "user")
                                        put("parts", JSONArray().apply {
                                            put(JSONObject().apply { put("text", resumo) })
                                        })
                                    })
                                })
                                put("turnComplete", false)
                            })
                        }
                        android.util.Log.i("GeminiLive", "║  Reinjetando histórico (${historicoTurnos.size} turnos)...")
                        ws.send(histMsg.toString().encodeUtf8())
                    }
                } else {
                    android.util.Log.i("GeminiLive", "║  Session resumption ativa — contexto preservado, sem reinjeção")
                }

                val saudacaoPrompt = when {
                    usouResumption && perguntaInterrompidaAgora != null ->
                        "Conexão restaurada. Responda a pergunta que estava processando: \"$perguntaInterrompidaAgora\""
                    usouResumption ->
                        "Conexão restaurada. Continue a conversa naturalmente sem se apresentar novamente. Máximo 1 frase."
                    perguntaInterrompidaAgora != null ->
                        "A conexão caiu enquanto você estava processando a pergunta: \"$perguntaInterrompidaAgora\". Avise brevemente que a conexão foi restaurada e responda essa pergunta agora."
                    foiReconexao && historicoTurnos.isNotEmpty() ->
                        "A sessão foi reconectada automaticamente. Avise brevemente o jogador que a conexão foi renovada e que você lembra do que estávamos conversando. Máximo 1 frase em português."
                    foiReconexao ->
                        "A sessão foi reconectada automaticamente. Avise brevemente o jogador. Máximo 1 frase em português."
                    contextoFicha.contains("Sem nome") || contextoFicha.length < 20 ->
                        "Apresente-se brevemente como Mestre IA de GURPS e pergunte como pode ajudar. Máximo 2 frases em português."
                    else ->
                        "Cumprimente o jogador mencionando o personagem pelo nome e pergunte como pode ajudar. Máximo 2 frases em português."
                }

                val saudacaoMsg = JSONObject().apply {
                    put("realtimeInput", JSONObject().apply {
                        put("text", saudacaoPrompt)
                    })
                }
                android.util.Log.i("GeminiLive", "║  Enviando saudação (realtimeInput.text)...")
                ws.send(saudacaoMsg.toString().encodeUtf8())

                sessaoAtiva = true
                iniciarCaptura()
                android.util.Log.i("GeminiLive", "╚══ SESSÃO ATIVA — aguardando fala do usuário")
                mainHandler.post { onEstado(EstadoLive.OUVINDO) }
                return
            }

            if (obj.has("goAway")) {
                val goAway = obj.getJSONObject("goAway")
                val timeLeft = goAway.optString("timeLeft", "")
                val timeLeftSecs = timeLeft.trimEnd('s').toLongOrNull() ?: 0L
                android.util.Log.w("GeminiLive", "GoAway recebido — timeLeft=${timeLeft.ifBlank { "não informado" }}, reconectando preventivamente...")
                encerrar()
                // Reconecta com atraso menor se tiver tempo sobrando (reconexão preventiva)
                val delayMs = if (timeLeftSecs > 5L) 500L else 1500L
                reconectarAutomaticamenteComDelay("goAway", delayMs)
                return
            }

            // Erro explícito da API retornado como JSON
            if (obj.has("error")) {
                val err = obj.getJSONObject("error")
                android.util.Log.e("GeminiLive", "╔══ ERRO DA API ══════════════════════")
                android.util.Log.e("GeminiLive", "║  code: ${err.optInt("code")}")
                android.util.Log.e("GeminiLive", "║  status: ${err.optString("status")}")
                android.util.Log.e("GeminiLive", "║  message: ${err.optString("message")}")
                android.util.Log.e("GeminiLive", "╚═════════════════════════════════════")
                encerrar()
                mainHandler.post { onEstado(EstadoLive.ERRO) }
                return
            }

            if (obj.has("toolCall")) {
                val calls = obj.getJSONObject("toolCall").getJSONArray("functionCalls")
                val respostas = JSONArray()
                for (i in 0 until calls.length()) {
                    val call = calls.getJSONObject(i)
                    val id = call.getString("id")
                    val nome = call.getString("name")
                    val args = call.optJSONObject("args") ?: JSONObject()
                    android.util.Log.i("GeminiLive", "╔══ 🔧 TOOL CALL ══════════════════════")
                    android.util.Log.i("GeminiLive", "║  Ferramenta: $nome")
                    android.util.Log.i("GeminiLive", "║  Args: ${args.toString().take(200)}")
                    // Feedback visual imediato — evita silêncio durante RAG (pode demorar ~10s)
                    val labelFerramenta = when (nome) {
                        "consultarManual"                  -> "📖 Consultando o Códex..."
                        "buscarCatalogo"                   -> "🔍 Buscando no catálogo..."
                        "editarFicha"                      -> "✏️ Editando a ficha..."
                        "trilhaDeMagias"                   -> "🗺️ Calculando trilha de magias..."
                        "lerFicha"                         -> "📋 Lendo a ficha..."
                        "forjador_buscar_racas"            -> "🧬 Buscando raças/metacaracterísticas..."
                        "forjador_aplicar_modelo_racial"   -> "🧬 Aplicando modelo racial..."
                        else                               -> "⚙️ Processando..."
                    }
                    mainHandler.post { onRespostaMestre(labelFerramenta) }
                    val t0 = System.currentTimeMillis()
                    val resultado = onToolCall(nome, args)
                    val ms = System.currentTimeMillis() - t0
                    android.util.Log.i("GeminiLive", "║  Resultado (${ms}ms): ${resultado.toString().take(300)}")
                    android.util.Log.i("GeminiLive", "╚══════════════════════════════════════")
                    respostas.put(JSONObject().apply {
                        put("id", id)
                        put("name", nome)
                        put("response", resultado)
                    })
                }
                // camelCase conforme spec Gemini Live
                val toolResp = JSONObject().apply {
                    put("toolResponse", JSONObject().apply {
                        put("functionResponses", respostas)
                    })
                }
                val toolRespStr = toolResp.toString()
                val ws = webSocket
                if (ws != null) {
                    android.util.Log.i("GeminiLive", "► Enviando toolResponse ao servidor...")
                    ws.send(toolRespStr.encodeUtf8())
                } else {
                    // Conexão caiu enquanto o RAG processava — registra pergunta interrompida
                    // NÃO tenta reenviar toolResponse (a nova sessão não conhece o toolCall original)
                    android.util.Log.w("GeminiLive", "⚠ Conexão caiu durante RAG — pergunta será reinjetada na reconexão")
                    if (ultimaPerguntaUsuario.isNotBlank()) {
                        perguntaInterrompida = ultimaPerguntaUsuario
                    }
                }
                return
            }

            if (obj.has("serverContent")) {
                val content = obj.getJSONObject("serverContent")

                val modelTurn = content.optJSONObject("modelTurn")
                if (modelTurn != null) {
                    val parts = modelTurn.optJSONArray("parts") ?: return
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        if (part.has("inlineData")) {
                            val mime = part.getJSONObject("inlineData").getString("mimeType")
                            if (mime.contains("audio")) {
                                if (!turnoTemAudio) {
                                    // Primeiro chunk do turno: bloqueia mic, limpa fila anterior
                                    turnoTemAudio = true
                                    modeloFalando = true
                                    limparFilaAudio()
                                    mainHandler.post { onEstado(EstadoLive.FALANDO) }
                                    android.util.Log.i("GeminiLive", "♪ Áudio iniciado — mic bloqueado")
                                }
                                val audioB64 = part.getJSONObject("inlineData").getString("data")
                                val bytes = Base64.decode(audioB64, Base64.DEFAULT)
                                reproduzirAudio(bytes)
                            }
                        }
                        if (part.has("text")) {
                            // Ignora parts de raciocínio interno (thought=true)
                            if (part.optBoolean("thought", false)) {
                                android.util.Log.d("GeminiLive", "✎ pensamento ignorado: \"${part.getString("text").take(80)}\"")
                                continue
                            }
                            val t = part.getString("text")
                            android.util.Log.i("GeminiLive", "✎ texto turno: \"${t.take(120)}\"")
                            if (t.isNotBlank()) {
                                pendingTextoFallback += t + " "
                            }
                        }
                    }
                }

                // outputTranscription: chega como objeto {"text":"palavra"} fragmentado por palavra
                val outputTranscriptRaw = content.opt("outputTranscription")
                if (outputTranscriptRaw != null) {
                    val fragmento = when (outputTranscriptRaw) {
                        is JSONObject -> outputTranscriptRaw.optString("text", "")
                        is String -> outputTranscriptRaw
                        else -> ""
                    }
                    if (fragmento.isNotBlank()) {
                        android.util.Log.d("GeminiLive", "✎ frag transcrição: \"$fragmento\"")
                        pendingTextoFallback += fragmento
                    }
                }

                // interrupted=true: modelo foi cortado, descarta texto parcial
                if (content.optBoolean("interrupted")) {
                    android.util.Log.i("GeminiLive", "⚡ Turno interrompido — descartando texto parcial")
                    pendingTextoFallback = ""
                    turnoTemAudio = false
                }

                if (content.optBoolean("turnComplete")) {
                    // Loga tokens do turno (usageMetadata vem no mesmo JSON que turnComplete)
                    val usage = obj.optJSONObject("usageMetadata")
                    if (usage != null) {
                        val prompt   = usage.optInt("promptTokenCount")
                        val response = usage.optInt("responseTokenCount")
                        val total    = usage.optInt("totalTokenCount")
                        android.util.Log.i("GeminiLive", "📊 Tokens — prompt: $prompt | resposta: $response | total: $total")
                    }
                    android.util.Log.i("GeminiLive", "✓ Turno completo — voltando a ouvir")
                    val fallback = pendingTextoFallback.trim()
                    if (fallback.isNotBlank()) {
                        android.util.Log.i("GeminiLive", "✎ Fallback chat: \"${fallback.take(150)}\"")
                        mainHandler.post { onRespostaMestre(fallback) }
                    }
                    // Salva turno no histórico (máx 5 turnos)
                    if (ultimaPerguntaUsuario.isNotBlank() && fallback.isNotBlank()) {
                        if (historicoTurnos.size >= 5) historicoTurnos.removeFirst()
                        historicoTurnos.addLast(TurnoResumido(
                            usuario = ultimaPerguntaUsuario.take(200),
                            mestre  = fallback.take(300)
                        ))
                        ultimaPerguntaUsuario = ""
                    }
                    pendingTextoFallback = ""
                    turnoTemAudio = false
                    modeloFalando = false
                    mainHandler.post { onEstado(EstadoLive.OUVINDO) }
                }

                val inputTranscriptRaw = content.opt("inputTranscription")
                val inputTranscript = when (inputTranscriptRaw) {
                    is JSONObject -> inputTranscriptRaw.optString("text", "")
                    is String -> inputTranscriptRaw
                    else -> ""
                }
                if (inputTranscript.isNotBlank()) {
                    android.util.Log.i("GeminiLive", "✎ Transcrição usuário: \"${inputTranscript.take(100)}\"")
                    ultimaPerguntaUsuario = inputTranscript
                    mainHandler.post { onTranscricaoUsuario(inputTranscript) }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("GeminiLive", "Erro ao processar mensagem: ${e.message} | json=${json.take(200)}")
        }
    }

    private fun iniciarCaptura() {
        val bufferSize = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
            bufferSize * 2
        ).also { it.startRecording() }

        // Buffer mínimo — write() bloqueia naturalmente na taxa correta (24kHz)
        // Buffer grande causava acúmulo e reprodução acelerada
        val trackBufSize = AudioTrack.getMinBufferSize(
            24000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        audioTrack = AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(),
            AudioFormat.Builder()
                .setSampleRate(24000)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
            trackBufSize,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        ).also { it.play() }

        capturaJob = scope.launch {
            val buffer = ByteArray(3200) // ~100ms de áudio a 16kHz
            while (isActive && sessaoAtiva) {
                val lidos = audioRecord?.read(buffer, 0, buffer.size) ?: break
                if (lidos > 0) {
                    // Não envia microfone enquanto modelo está falando — evita auto-interrupção
                    if (modeloFalando) continue
                    val b64 = Base64.encodeToString(buffer.copyOf(lidos), Base64.NO_WRAP)
                    // Formato correto conforme doc: realtimeInput.audio com data+mimeType
                    val msg = JSONObject().apply {
                        put("realtimeInput", JSONObject().apply {
                            put("audio", JSONObject().apply {
                                put("data", b64)
                                put("mimeType", "audio/pcm;rate=16000")
                            })
                        })
                    }
                    webSocket?.send(msg.toString().encodeUtf8())
                }
            }
        }

        // Keepalive: envia áudio silencioso a cada 20s para manter WebSocket vivo
        keepAliveJob = scope.launch {
            val silencio = ByteArray(3200) // 100ms de zeros = silêncio PCM
            val b64silencio = Base64.encodeToString(silencio, Base64.NO_WRAP)
            while (isActive && sessaoAtiva) {
                kotlinx.coroutines.delay(20_000)
                if (!sessaoAtiva) break
                try {
                    val ping = JSONObject().apply {
                        put("realtimeInput", JSONObject().apply {
                            put("audio", JSONObject().apply {
                                put("data", b64silencio)
                                put("mimeType", "audio/pcm;rate=16000")
                            })
                        })
                    }
                    webSocket?.send(ping.toString().encodeUtf8())
                    android.util.Log.d("GeminiLive", "♥ keepalive enviado")
                } catch (e: Exception) {
                    android.util.Log.w("GeminiLive", "keepalive falhou: ${e.message}")
                }
            }
        }

        // Coroutine de reprodução num thread dedicado (não IO pool)
        // O write() bloqueia até o HW consumir os dados — garante ritmo natural
        val track = audioTrack
        reproducaoJob = scope.launch(Dispatchers.Default) {
            for (pcm in audioChannel) {
                if (!isActive) break
                // write() em MODE_STREAM bloqueia até o hardware consumir
                // Garante que reproduzimos na taxa real de 24kHz sem pular frames
                var offset = 0
                while (offset < pcm.size && isActive) {
                    val written = track?.write(pcm, offset, pcm.size - offset) ?: break
                    if (written <= 0) break
                    offset += written
                }
            }
        }
    }

    private fun limparFilaAudio() {
        // Para o AudioTrack imediatamente e descarta buffer interno
        audioTrack?.pause()
        audioTrack?.flush()
        audioTrack?.play()
        // Descarta chunks pendentes no channel
        var descartados = 0
        while (audioChannel.tryReceive().isSuccess) { descartados++ }
        android.util.Log.d("GeminiLive", "♪ Novo turno: $descartados chunks descartados, AudioTrack resetado")
    }

    private fun reproduzirAudio(pcm: ByteArray) {
        // Enfileira o chunk — a coroutine de reprodução consome em ordem, sem concorrência
        audioChannel.trySend(pcm)
    }

    private fun reconectarAutomaticamente(motivo: String) {
        reconectarAutomaticamenteComDelay(motivo, 1500L)
    }

    private fun reconectarAutomaticamenteComDelay(motivo: String, delayMs: Long) {
        reconectandoApos = motivo
        mainHandler.post { onEstado(EstadoLive.CONECTANDO) }
        val runnable = Runnable {
            reconexaoPendente = null
            android.util.Log.i("GeminiLive", "♻ Reconectando após $motivo (token=${if (sessionResumptionToken != null) "✓" else "✗"}, histórico: ${historicoTurnos.size} turnos)...")
            iniciarSessao(contextoFichaParaSaudacao)
        }
        reconexaoPendente = runnable
        mainHandler.postDelayed(runnable, delayMs)
    }

    private fun buildResumoHistorico(): String {
        if (historicoTurnos.isEmpty()) return ""
        val sb = StringBuilder("Resumo da conversa anterior (antes da reconexão):\n")
        historicoTurnos.forEachIndexed { i, t ->
            sb.append("${i + 1}. Jogador: \"${t.usuario}\"\n   Mestre: \"${t.mestre}\"\n")
        }
        return sb.toString()
    }

    fun encerrar() {
        // Cancela reconexão automática pendente — encerramento manual é intencional
        reconexaoPendente?.let { mainHandler.removeCallbacks(it) }
        reconexaoPendente = null
        reconectandoApos = ""
        perguntaInterrompida = null
        sessionResumptionToken = null // encerramento manual limpa o token
        sessaoAtiva = false
        capturaJob?.cancel()
        keepAliveJob?.cancel()
        reproducaoJob?.cancel()
        audioChannel.close()
        audioChannel = Channel(capacity = 200) // recria para próxima sessão
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
        webSocket?.close(1000, "Sessão encerrada pelo usuário")
        webSocket = null
    }
}
