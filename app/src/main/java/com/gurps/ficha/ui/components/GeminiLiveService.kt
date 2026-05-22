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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
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
Você é o Mestre IA de GURPS — um mestre de campanha experiente, sábio e com personalidade própria.
Fale sempre em português brasileiro, de forma natural e conversacional.
Seu nome é Mestre.

REGRAS DE COMPORTAMENTO — FICHA:
- Antes de modificar a ficha, SEMPRE chame obterFicha para verificar pontos disponíveis
- Confirme o que fez depois de executar — ex: "Pronto, adicionei X, ficam Y pontos restantes"
- Se o usuário pedir algo impossível (sem pontos suficientes), explique e sugira alternativas, se insistir faça oque foi pedido
- Após modificar a ficha, confirme o novo saldo de pontos em voz

REGRAS DE COMPORTAMENTO — DÚVIDAS DE REGRAS:
- Para QUALQUER dúvida de regra, use consultarManual ANTES de responder — nunca invente
- FIDELIDADE EXCLUSIVA AO CÓDEX: use SOMENTE o que estiver nos chunks retornados por consultarManual
- Se a regra não estiver no Códex, diga: "Não localizei essa regra nos manuais disponíveis"
- Você pode chamar consultarManual múltiplas vezes com termos diferentes para investigar
- Use termos técnicos de GURPS nas buscas: "ST", "DX", "penalidade", "modificador", nome exato das regras

PROTOCOLO OBRIGATÓRIO DE CÁLCULO (quando a regra envolver número ou fórmula):
1. Cite a regra: "Segundo [Livro, Pág]..."
2. Identifique os valores: "O alcance da arma é X, o divisor é Y..."
3. Calcule em voz alta: "Então X dividido por Y é igual a Z..."
4. Conclua: "Portanto, o alcance efetivo é Z metros"
NUNCA dê resultado sem explicar o cálculo. NUNCA confunda stat da arma com distância cênica.

ESTILO DE VOZ:
- Fale enquanto pensa — não fique em silêncio enquanto processa ferramentas
- Respostas curtas e diretas são melhores que longas
- Personalidade: sábio, justo, levemente dramático
- Nunca invente regras — se não encontrar, diga claramente

NUNCA:
- Responda dúvidas de regra sem consultar o manual primeiro
- Modifique a ficha sem confirmar o resultado depois
- Use conhecimento geral de IA sobre GURPS — use apenas o Códex
""".trimIndent()

    private fun buildSetupMessage(): String {
        val tools = JSONArray().apply {
            put(JSONObject().apply {
                put("function_declarations", JSONArray().apply {
                    put(buildFuncao("obterFicha", "Retorna o estado atual da ficha do personagem: nome, pontos restantes, vantagens, desvantagens e perícias", JSONObject()))
                    put(buildFuncao("obterPontosRestantes", "Retorna quantos pontos estão disponíveis para gastar", JSONObject()))
                    put(buildFuncao("adicionarVantagem", "Adiciona uma vantagem na ficha do personagem",
                        JSONObject().apply {
                            put("type", "object")
                            put("properties", JSONObject().apply {
                                put("nome", JSONObject().apply { put("type", "string"); put("description", "Nome exato da vantagem") })
                                put("nivel", JSONObject().apply { put("type", "integer"); put("description", "Nível da vantagem (padrão 1)") })
                            })
                            put("required", JSONArray().apply { put("nome") })
                        }
                    ))
                    put(buildFuncao("removerVantagem", "Remove uma vantagem da ficha pelo nome",
                        JSONObject().apply {
                            put("type", "object")
                            put("properties", JSONObject().apply {
                                put("nome", JSONObject().apply { put("type", "string"); put("description", "Nome da vantagem a remover") })
                            })
                            put("required", JSONArray().apply { put("nome") })
                        }
                    ))
                    put(buildFuncao("adicionarDesvantagem", "Adiciona uma desvantagem na ficha",
                        JSONObject().apply {
                            put("type", "object")
                            put("properties", JSONObject().apply {
                                put("nome", JSONObject().apply { put("type", "string"); put("description", "Nome exato da desvantagem") })
                                put("nivel", JSONObject().apply { put("type", "integer"); put("description", "Nível (padrão 1)") })
                            })
                            put("required", JSONArray().apply { put("nome") })
                        }
                    ))
                    put(buildFuncao("adicionarPericia", "Adiciona ou atualiza uma perícia na ficha",
                        JSONObject().apply {
                            put("type", "object")
                            put("properties", JSONObject().apply {
                                put("nome", JSONObject().apply { put("type", "string"); put("description", "Nome da perícia") })
                                put("pontos", JSONObject().apply { put("type", "integer"); put("description", "Pontos a investir") })
                            })
                            put("required", JSONArray().apply { put("nome"); put("pontos") })
                        }
                    ))
                    put(buildFuncao("removerPericia", "Remove uma perícia da ficha pelo nome",
                        JSONObject().apply {
                            put("type", "object")
                            put("properties", JSONObject().apply {
                                put("nome", JSONObject().apply { put("type", "string"); put("description", "Nome da perícia a remover") })
                            })
                            put("required", JSONArray().apply { put("nome") })
                        }
                    ))
                    put(buildFuncao("inspecionarPersonagem", "Inspeciona seções específicas da ficha (atributos, vantagens, desvantagens, pericias, equipamentos, tudo)",
                        JSONObject().apply {
                            put("type", "object")
                            put("properties", JSONObject().apply {
                                put("secao", JSONObject().apply { put("type", "string"); put("description", "Seção a inspecionar: atributos, vantagens, desvantagens, pericias, equipamentos, tudo") })
                            })
                            put("required", JSONArray().apply { put("secao") })
                        }
                    ))
                    put(buildFuncao("consultarManual", "Busca regras no Códex de GURPS usando o sistema RAG. SEMPRE use esta ferramenta antes de responder qualquer dúvida de regra.",
                        JSONObject().apply {
                            put("type", "object")
                            put("properties", JSONObject().apply {
                                put("termos", JSONObject().apply { put("type", "string"); put("description", "Termos técnicos de GURPS para buscar. Use nomes exatos de regras, habilidades ou mecânicas. Ex: 'queda dano velocidade hex', 'tiro subaquatico penalidade'") })
                            })
                            put("required", JSONArray().apply { put("termos") })
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
            if (params.length() > 0) put("parameters", params)
        }
    }

    fun iniciarSessao(contextoFicha: String) {
        if (sessaoAtiva) return
        mainHandler.post { onEstado(EstadoLive.CONECTANDO) }

        val keyPreview = BuildConfig.MESTRE_IA_GEMINI_KEY.take(8) + "..."
        val url = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent?key=${BuildConfig.MESTRE_IA_GEMINI_KEY}"
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
                android.util.Log.i("GeminiLive", "║  Enviando setup (${setup.length} chars)...")
                ws.send(setup)

                val ctxMsg = JSONObject().apply {
                    put("client_content", JSONObject().apply {
                        put("turns", JSONArray().apply {
                            put(JSONObject().apply {
                                put("role", "user")
                                put("parts", JSONArray().apply {
                                    put(JSONObject().apply { put("text", "Contexto atual da ficha: $contextoFicha") })
                                })
                            })
                        })
                        put("turn_complete", true)
                    })
                }
                android.util.Log.i("GeminiLive", "║  Enviando contexto da ficha (${contextoFicha.length} chars)...")
                ws.send(ctxMsg.toString())

                // Saudação inicial — confirma canal de áudio e apresenta o Mestre
                val saudacaoPrompt = if (contextoFicha.contains("Sem nome") || contextoFicha.length < 20)
                    "O jogador acabou de abrir o modo de voz. Diga uma saudação curta como Mestre IA de GURPS, apresente-se e pergunte como pode ajudar na ficha. Seja breve — máximo 2 frases."
                else
                    "O jogador acabou de abrir o modo de voz. Diga uma saudação curta mencionando o personagem pelo nome (se souber) e pergunte como pode ajudar. Seja breve — máximo 2 frases."

                val saudacaoMsg = JSONObject().apply {
                    put("client_content", JSONObject().apply {
                        put("turns", JSONArray().apply {
                            put(JSONObject().apply {
                                put("role", "user")
                                put("parts", JSONArray().apply {
                                    put(JSONObject().apply { put("text", saudacaoPrompt) })
                                })
                            })
                        })
                        put("turn_complete", true)
                    })
                }
                android.util.Log.i("GeminiLive", "║  Enviando saudação inicial...")
                ws.send(saudacaoMsg.toString())

                sessaoAtiva = true
                iniciarCaptura()
                android.util.Log.i("GeminiLive", "╚══ SESSÃO ATIVA — aguardando fala do usuário")
                mainHandler.post { onEstado(EstadoLive.OUVINDO) }
            }

            override fun onMessage(ws: WebSocket, text: String) {
                android.util.Log.d("GeminiLive", "◄ MSG servidor (${text.length} chars): ${text.take(120)}")
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
                    android.util.Log.i("GeminiLive", "◄ TOOL RESP: $nome | ${ms}ms | sucesso=${resultado.optBoolean("sucesso", resultado.has("regras"))} | ${resultado.toString().take(150)}")
                    respostas.put(JSONObject().apply {
                        put("id", id)
                        put("name", nome)
                        put("response", resultado)
                    })
                }
                val toolResp = JSONObject().apply {
                    put("tool_response", JSONObject().apply {
                        put("function_responses", respostas)
                    })
                }
                android.util.Log.i("GeminiLive", "► Enviando tool_response ao servidor...")
                webSocket?.send(toolResp.toString())
                return
            }

            if (obj.has("serverContent")) {
                val content = obj.getJSONObject("serverContent")

                val modelTurn = content.optJSONObject("modelTurn")
                if (modelTurn != null) {
                    mainHandler.post { onEstado(EstadoLive.FALANDO) }
                    val parts = modelTurn.optJSONArray("parts") ?: return
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        if (part.has("inlineData")) {
                            val mime = part.getJSONObject("inlineData").getString("mimeType")
                            if (mime.contains("audio")) {
                                val audioB64 = part.getJSONObject("inlineData").getString("data")
                                val bytes = Base64.decode(audioB64, Base64.DEFAULT)
                                android.util.Log.d("GeminiLive", "♪ Áudio recebido: ${bytes.size} bytes (${mime})")
                                reproduzirAudio(bytes)
                            }
                        }
                        if (part.has("text")) {
                            val texto = part.getString("text")
                            if (texto.isNotBlank()) {
                                android.util.Log.i("GeminiLive", "✎ Texto Mestre: \"${texto.take(100)}\"")
                                mainHandler.post { onRespostaMestre(texto) }
                            }
                        }
                    }
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

        audioTrack = AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(),
            AudioFormat.Builder()
                .setSampleRate(24000)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
            AudioTrack.getMinBufferSize(24000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT) * 2,
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
                        put("realtime_input", JSONObject().apply {
                            put("media_chunks", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("mime_type", "audio/pcm;rate=16000")
                                    put("data", b64)
                                })
                            })
                        })
                    }
                    webSocket?.send(msg.toString())
                }
            }
        }
    }

    private fun reproduzirAudio(pcm: ByteArray) {
        scope.launch(Dispatchers.Main) {
            audioTrack?.write(pcm, 0, pcm.size)
        }
    }

    fun encerrar() {
        sessaoAtiva = false
        capturaJob?.cancel()
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
