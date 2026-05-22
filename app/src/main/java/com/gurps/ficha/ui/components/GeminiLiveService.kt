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

class GeminiLiveService(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.IO)
    private var webSocket: WebSocket? = null
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var capturaJob: Job? = null
    private var reproducaoJob: Job? = null
    // Recriado a cada sessão; máximo 200 chunks (~20s de buffer)
    private var audioChannel = Channel<ByteArray>(capacity = 200)
    private var sessaoAtiva = false

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
- consultarManual(termos): busca regras no Códex de GURPS. Use ANTES de responder qualquer dúvida de regra.

FLUXO OBRIGATÓRIO PARA EDITAR A FICHA:
1. SEMPRE buscar com buscarCatalogo primeiro para obter o ID correto
2. Então chamar editarFicha com o ID/nome retornado
3. Confirmar em voz o que foi feito e quantos pontos restam

REGRAS DE COMPORTAMENTO — DÚVIDAS DE REGRAS:
- Para QUALQUER dúvida de regra, use consultarManual ANTES de responder — nunca invente
- FIDELIDADE EXCLUSIVA AO CÓDEX: use SOMENTE o que estiver nos chunks retornados por consultarManual
- Se a regra não estiver no Códex, diga: "Não localizei essa regra nos manuais disponíveis"
- Use termos técnicos de GURPS nas buscas: "ST", "DX", "penalidade", "modificador", nome exato das regras

PROTOCOLO OBRIGATÓRIO DE CÁLCULO (quando a regra envolver número ou fórmula):
1. Cite a regra: "Segundo [Livro, Pág]..."
2. Identifique os valores: "O alcance da arma é X, o divisor é Y..."
3. Calcule em voz alta: "Então X dividido por Y é igual a Z..."
4. Conclua: "Portanto, o alcance efetivo é Z metros"
NUNCA dê resultado sem explicar o cálculo. NUNCA confunda stat da arma com distância cênica.

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
- Use conhecimento geral de IA sobre GURPS — use apenas o Códex
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
                        "Edita a ficha DIRETAMENTE: adiciona, remove ou altera qualquer item. Para atributos: operacao=alterar, secao=atributos, alvo=ST/DX/IQ/HT, valor=14. Para vantagens/desvantagens/pericias/tecnicas/magias/equipamentos: use o ID retornado por buscarCatalogo.",
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
                                    put("description", "ID/nome do item ou atributo (ST/DX/IQ/HT/forca/destreza/inteligencia/vitalidade)")
                                })
                                put("valor", JSONObject().apply {
                                    put("type", "string")
                                    put("description", "Atributo: '14'. Perícia: 'nivel=14;esp=Florestas'. Vantagem: 'nivel=3'. Técnica: 'nivel=4;periciaBase=<id>'. Magia: 'forcar=true' apenas se narrativo.")
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
                        "Busca regras no Códex de GURPS (RAG). SEMPRE use antes de responder qualquer dúvida de regra — nunca invente. Pode chamar múltiplas vezes com termos diferentes.",
                        JSONObject().apply {
                            put("type", "object")
                            put("properties", JSONObject().apply {
                                put("termos", JSONObject().apply {
                                    put("type", "string")
                                    put("description", "Termos técnicos de GURPS para buscar. Ex: 'queda dano velocidade hex', 'tiro subaquatico penalidade alcance'")
                                })
                            })
                            put("required", JSONArray().put("termos"))
                        }
                    ))
                })
            })
        }

        return JSONObject().apply {
            put("setup", JSONObject().apply {
                put("model", BuildConfig.GEMINI_LIVE_MODEL)
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

    private var contextoFichaParaSaudacao: String = ""

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
                android.util.Log.i("GeminiLive", "◄ MSG texto (${text.length} chars): ${text.take(300)}")
                processarMensagemServidor(text)
            }

            override fun onMessage(ws: WebSocket, bytes: ByteString) {
                val text = bytes.utf8()
                android.util.Log.i("GeminiLive", "◄ MSG binário (${bytes.size} bytes): ${text.take(300)}")
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
                mainHandler.post { onEstado(EstadoLive.OCIOSO) }
            }
        })
    }

    private fun processarMensagemServidor(json: String) {
        try {
            val obj = JSONObject(json)

            // setupComplete — servidor confirmou o setup, agora podemos enviar mensagens
            if (obj.has("setupComplete")) {
                android.util.Log.i("GeminiLive", "║  setupComplete recebido — enviando contexto e saudação")
                val ws = webSocket ?: return

                val contextoFicha = contextoFichaParaSaudacao
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
                        put("turnComplete", true)
                    })
                }
                android.util.Log.i("GeminiLive", "║  Enviando contexto da ficha (${contextoFicha.length} chars)...")
                ws.send(ctxMsg.toString().encodeUtf8())

                val saudacaoPrompt = if (contextoFicha.contains("Sem nome") || contextoFicha.length < 20)
                    "O jogador acabou de abrir o modo de voz. Diga uma saudação curta como Mestre IA de GURPS, apresente-se e pergunte como pode ajudar na ficha. Seja breve — máximo 2 frases."
                else
                    "O jogador acabou de abrir o modo de voz. Diga uma saudação curta mencionando o personagem pelo nome (se souber) e pergunte como pode ajudar. Seja breve — máximo 2 frases."

                val saudacaoMsg = JSONObject().apply {
                    put("clientContent", JSONObject().apply {
                        put("turns", JSONArray().apply {
                            put(JSONObject().apply {
                                put("role", "user")
                                put("parts", JSONArray().apply {
                                    put(JSONObject().apply { put("text", saudacaoPrompt) })
                                })
                            })
                        })
                        put("turnComplete", true)
                    })
                }
                android.util.Log.i("GeminiLive", "║  Enviando saudação inicial...")
                ws.send(saudacaoMsg.toString().encodeUtf8())

                sessaoAtiva = true
                iniciarCaptura()
                android.util.Log.i("GeminiLive", "╚══ SESSÃO ATIVA — aguardando fala do usuário")
                mainHandler.post { onEstado(EstadoLive.OUVINDO) }
                return
            }

            if (obj.has("goAway")) {
                android.util.Log.w("GeminiLive", "GoAway recebido — servidor encerrando sessão")
                encerrar()
                mainHandler.post { onEstado(EstadoLive.OCIOSO) }
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
                    android.util.Log.i("GeminiLive", "► TOOL CALL: $nome | args=${args.toString().take(100)}")
                    val t0 = System.currentTimeMillis()
                    val resultado = onToolCall(nome, args)
                    val ms = System.currentTimeMillis() - t0
                    android.util.Log.i("GeminiLive", "◄ TOOL RESP: $nome | ${ms}ms | ${resultado.toString().take(150)}")
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
                android.util.Log.i("GeminiLive", "► Enviando toolResponse ao servidor...")
                webSocket?.send(toolResp.toString().encodeUtf8())
                return
            }

            if (obj.has("serverContent")) {
                val content = obj.getJSONObject("serverContent")

                val modelTurn = content.optJSONObject("modelTurn")
                if (modelTurn != null) {
                    mainHandler.post { onEstado(EstadoLive.FALANDO) }
                    val parts = modelTurn.optJSONArray("parts") ?: return
                    var temAudio = false
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        if (part.has("inlineData")) {
                            val mime = part.getJSONObject("inlineData").getString("mimeType")
                            if (mime.contains("audio")) {
                                temAudio = true
                                val audioB64 = part.getJSONObject("inlineData").getString("data")
                                val bytes = Base64.decode(audioB64, Base64.DEFAULT)
                                android.util.Log.d("GeminiLive", "♪ Áudio recebido: ${bytes.size} bytes")
                                reproduzirAudio(bytes)
                            }
                        }
                        // Só exibe texto no chat se NÃO tiver áudio (evita pensamento interno em inglês)
                        if (!temAudio && part.has("text")) {
                            val texto = part.getString("text")
                            if (texto.isNotBlank()) {
                                android.util.Log.i("GeminiLive", "✎ Texto Mestre: \"${texto.take(100)}\"")
                                mainHandler.post { onRespostaMestre(texto) }
                            }
                        }
                    }
                }

                // Transcrição do que o Mestre falou (vem em PT-BR, usa para exibir no chat)
                val outputTranscript = content.optString("outputTranscription", "")
                if (outputTranscript.isNotBlank()) {
                    android.util.Log.i("GeminiLive", "✎ Transcrição Mestre: \"${outputTranscript.take(100)}\"")
                    mainHandler.post { onRespostaMestre(outputTranscript) }
                }

                if (content.optBoolean("turnComplete")) {
                    android.util.Log.i("GeminiLive", "✓ Turno completo — voltando a ouvir")
                    mainHandler.post { onEstado(EstadoLive.OUVINDO) }
                }

                val inputTranscript = content.optString("inputTranscription", "")
                if (inputTranscript.isNotBlank()) {
                    android.util.Log.i("GeminiLive", "✎ Transcrição usuário: \"${inputTranscript.take(100)}\"")
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

        // USAGE_MEDIA com buffer grande para streaming contínuo sem underrun
        val trackBufSize = maxOf(
            AudioTrack.getMinBufferSize(24000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT),
            24000 * 2 * 2  // 2 segundos de buffer (24kHz * 16bit * 2s)
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
                    val b64 = Base64.encodeToString(buffer.copyOf(lidos), Base64.NO_WRAP)
                    val msg = JSONObject().apply {
                        put("realtimeInput", JSONObject().apply {
                            put("mediaChunks", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("mimeType", "audio/pcm;rate=16000")
                                    put("data", b64)
                                })
                            })
                        })
                    }
                    webSocket?.send(msg.toString().encodeUtf8())
                }
            }
        }

        // Coroutine única de reprodução — garante acesso serial ao AudioTrack
        val track = audioTrack
        reproducaoJob = scope.launch {
            for (pcm in audioChannel) {
                track?.write(pcm, 0, pcm.size)
            }
        }
    }

    private fun reproduzirAudio(pcm: ByteArray) {
        // Enfileira o chunk — a coroutine de reprodução consome em ordem, sem concorrência
        audioChannel.trySend(pcm)
    }

    fun encerrar() {
        sessaoAtiva = false
        capturaJob?.cancel()
        audioChannel.close()
        audioChannel = Channel(capacity = 200) // recria para próxima sessão
        capturaJob?.cancel()
        reproducaoJob?.cancel()
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
