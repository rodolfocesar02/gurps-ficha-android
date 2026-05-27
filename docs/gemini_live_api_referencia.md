# Biblioteca de Referência — Gemini Live API
**Projeto:** GURPS Ficha Android — Mestre IA  
**Modelo em uso:** `models/gemini-2.5-flash-native-audio-preview-12-2025`  
**Última atualização:** 2026-05-27  
**Fonte:** https://ai.google.dev/gemini-api/docs/live-api

---

## Modelos Disponíveis para Live API

| Modelo | ID Completo | Notas |
|--------|-------------|-------|
| **Gemini 2.5 Flash Native Audio** ✅ EM USO | `models/gemini-2.5-flash-native-audio-preview-12-2025` | Flagship Live API. Áudio nativo bidirecional. Baixa latência. |
| Gemini 3.1 Flash Live Preview | `models/gemini-3.1-flash-live-preview` | Não suporta async function calling. |

**URL WebSocket:**
```
wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent?key=API_KEY
```

---

## Estrutura da Sessão (Fluxo de Mensagens)

```
Cliente → setup{}           → Servidor
Cliente ← setupComplete{}   ← Servidor
Cliente → clientContent{}   → Servidor  (contexto inicial, histórico)
Cliente → realtimeInput{}   → Servidor  (voz PCM, texto)
Cliente ← serverContent{}  ← Servidor  (áudio, transcrição, texto)
Cliente ← toolCall{}        ← Servidor  (pedido de function call)
Cliente → toolResponse{}    → Servidor  (resultado da function call)
Cliente ← sessionResumptionUpdate{} ← Servidor (token de reconexão)
```

---

## 1. Setup — BidiGenerateContentSetup

Enviado como **primeira mensagem** da sessão.

```json
{
  "setup": {
    "model": "models/gemini-2.5-flash-native-audio-preview-12-2025",
    "generationConfig": { ... },
    "systemInstruction": { "parts": [{ "text": "..." }] },
    "tools": [ { "function_declarations": [ ... ] } ],
    "outputAudioTranscription": {},
    "inputAudioTranscription": {},
    "sessionResumptionConfig": { "handle": "TOKEN" }
  }
}
```

### Campos do setup

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| `model` | string | Sim | ID do modelo no formato `models/{nome}` |
| `generationConfig` | objeto | Não | Configura modalidades de resposta, voz, temperatura |
| `systemInstruction` | objeto | Não | System prompt. Formato: `{parts: [{text: "..."}]}` |
| `tools` | array | Não | Lista de ferramentas disponíveis para o modelo |
| `outputAudioTranscription` | objeto | Não | `{}` vazio ativa transcrição do áudio do modelo |
| `inputAudioTranscription` | objeto | Não | `{}` vazio ativa transcrição da voz do usuário |
| `sessionResumptionConfig` | objeto | Não | `{handle: "TOKEN"}` — reconecta sessão existente |
| `contextWindowCompression` | objeto | Não | Compressão automática do contexto (evitar usar em native-audio — comportamento não documentado) |
| `realtimeInputConfig` | objeto | Não | Configura VAD e input de áudio em tempo real |
| `proactivity` | objeto | Não | Configura proatividade do modelo |

### generationConfig

```json
"generationConfig": {
  "responseModalities": ["AUDIO"],
  "speechConfig": {
    "voiceConfig": {
      "prebuiltVoiceConfig": {
        "voiceName": "Charon"
      }
    }
  }
}
```

| Campo | Valores | Descrição |
|-------|---------|-----------|
| `responseModalities` | `["AUDIO"]`, `["TEXT"]`, `["AUDIO","TEXT"]` | Modalidade de resposta do modelo |
| `voiceName` | `Charon`, `Sadaltager`, `Gacrux`, `Puck`, `Kore`, `Fenrir`, `Aoede` | Voz de saída. Projeto usa rotação entre 3. |

---

## 2. Function Declarations — Ferramentas

Declaradas dentro do `setup.tools`. Cada ferramenta tem um `name`, `description`, `parameters` e opcionalmente `behavior`.

```json
{
  "function_declarations": [
    {
      "name": "consultarManual",
      "description": "Busca regras no Códex de GURPS...",
      "behavior": "NON_BLOCKING",
      "parameters": {
        "type": "object",
        "properties": {
          "termos": { "type": "string", "description": "..." }
        },
        "required": ["termos"]
      }
    }
  ]
}
```

### Campos da FunctionDeclaration

| Campo | Tipo | Obrigatório | Valores | Descrição |
|-------|------|-------------|---------|-----------|
| `name` | string | Sim | qualquer | Nome da função. Deve ser único. |
| `description` | string | Sim | qualquer | Descrição do que a função faz. O modelo usa isso para decidir quando chamar. |
| `parameters` | objeto | Sim* | JSON Schema | Parâmetros da função. Gemini Live exige o campo mesmo que vazio `{"type":"object","properties":{}}` |
| `behavior` | enum | Não | ver abaixo | Define se a função é síncrona ou assíncrona |

### behavior — Enum

| Valor | Descrição | Suporte |
|-------|-----------|---------|
| *(ausente)* | **Síncrono (padrão):** modelo para de falar e aguarda o resultado da tool antes de continuar | Todos os modelos |
| `NON_BLOCKING` | **Assíncrono:** modelo **continua falando** enquanto a tool processa em paralelo | Gemini 2.5 Flash Live ✅ / Gemini 3.1 ❌ / native-audio-preview: não documentado, testando |

**No projeto — quais ferramentas são NON_BLOCKING:**
- `buscarCatalogo` — NON_BLOCKING (busca lenta, ~3s)
- `consultarManual` — NON_BLOCKING (RAG pesado, ~3-4s)
- Demais tools (`lerFicha`, `editarFicha`, etc.) — síncronas (rápidas, < 500ms)

---

## 3. toolCall — Servidor pede execução de ferramenta

Mensagem que o **servidor envia** quando o modelo quer chamar uma ferramenta.

```json
{
  "toolCall": {
    "functionCalls": [
      {
        "id": "fc_abc123",
        "name": "consultarManual",
        "args": {
          "termos": "Reflexos em Combate",
          "livro": "Módulo Básico"
        }
      }
    ]
  }
}
```

### Campos do toolCall

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `toolCall.functionCalls` | array | Lista de funções que o modelo quer chamar (pode ser múltiplas ao mesmo tempo) |
| `functionCalls[].id` | string | ID único do call — **deve ser ecoado** no toolResponse para o servidor associar |
| `functionCalls[].name` | string | Nome da função a executar |
| `functionCalls[].args` | objeto | Argumentos passados pelo modelo |

**Importante:** o servidor **não envia** `turnComplete` junto com `toolCall` — o turno só completa após o `toolResponse`.

---

## 4. toolResponse — Cliente responde com resultado

Mensagem que o **cliente envia** com o resultado da execução da ferramenta.

```json
{
  "toolResponse": {
    "functionResponses": [
      {
        "id": "fc_abc123",
        "name": "consultarManual",
        "response": {
          "scheduling": "SILENT",
          "encontrado": true,
          "regras": "..."
        }
      }
    ]
  }
}
```

### Campos do toolResponse

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `toolResponse.functionResponses` | array | Lista de respostas (uma por `functionCall`) |
| `functionResponses[].id` | string | **Obrigatório:** mesmo `id` recebido no `toolCall` |
| `functionResponses[].name` | string | Nome da função |
| `functionResponses[].response` | objeto | Resultado da execução. Campos livres + `scheduling` |
| `functionResponses[].response.scheduling` | enum | **Só para NON_BLOCKING.** Ver abaixo. |

### scheduling — Enum (só para tools NON_BLOCKING)

| Valor | Comportamento | Quando usar |
|-------|---------------|-------------|
| `INTERRUPT` | Modelo **para imediatamente** o que está falando e fala sobre o resultado | Resultado urgente / crítico — ex: alarme, erro fatal |
| `WHEN_IDLE` | Modelo **termina a frase atual** e então incorpora o resultado gerando **nova resposta** | Resultado que merece resposta dedicada — ⚠️ causa segundo turno de áudio |
| `SILENT` | Modelo **incorpora o resultado internamente** sem gerar novo turno de fala — usa o dado para completar a resposta em andamento | ✅ **Recomendado para RAG/catálogo** — evita duplo turno |

**Decisão no projeto:**
- `buscarCatalogo` → `SILENT` (dados de suporte, não merecem resposta separada)
- `consultarManual` → `SILENT` (RAG: modelo usa os chunks para completar a resposta)
- Se mudar para `WHEN_IDLE`: causa corte + recomeço (59 chunks descartados — testado em 27/05/2026)

---

## 5. realtimeInput — Envio de mídia em tempo real

Usado para enviar **áudio do microfone**, texto ou vídeo durante a sessão.

```json
{ "realtimeInput": { "audio": { "data": "BASE64_PCM", "mimeType": "audio/pcm;rate=16000" } } }
{ "realtimeInput": { "text": "mensagem de texto" } }
```

| Campo | Valores | Descrição |
|-------|---------|-----------|
| `audio.mimeType` | `audio/pcm;rate=16000` | PCM 16-bit mono 16kHz — formato do microfone |
| `audio.data` | base64 | Chunk de áudio codificado em base64 |
| `text` | string | Texto enviado como se fosse fala do usuário. Útil para saudação inicial. |

**No projeto:** microfone envia chunks de 3200 bytes (~100ms). KeepAlive envia silêncio (zeros) a cada 20s.

---

## 6. serverContent — Resposta do modelo

Mensagem que o **servidor envia** com áudio, transcrições e sinalizadores de turno.

```json
{
  "serverContent": {
    "modelTurn": {
      "parts": [
        { "inlineData": { "mimeType": "audio/pcm;rate=24000", "data": "BASE64" } },
        { "text": "texto da resposta" }
      ]
    },
    "outputTranscription": { "text": "fragmento da transcrição" },
    "inputTranscription": { "text": "fragmento da voz do usuário" },
    "generationComplete": true,
    "turnComplete": true,
    "interrupted": true
  }
}
```

### Campos do serverContent

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `modelTurn.parts[].inlineData` | objeto | Chunk de áudio PCM 24kHz em base64 |
| `modelTurn.parts[].text` | string | Texto da resposta (fallback quando sem áudio) |
| `modelTurn.parts[].thought` | boolean | `true` = raciocínio interno — **ignorar, não exibir** |
| `outputTranscription.text` | string | Fragmento da transcrição do que o modelo **falou** — preferir sobre `text` parts |
| `inputTranscription.text` | string | Fragmento da transcrição da **voz do usuário** — chega em streaming antes do `turnComplete` |
| `generationComplete` | boolean | Modelo terminou de **gerar** — congela contagem de bytes de áudio real |
| `turnComplete` | boolean | Turno completo — pode chegar **após** `generationComplete` com silêncio extra |
| `interrupted` | boolean | Usuário interrompeu o modelo — descartar áudio e texto parciais |

**Ordem típica de chegada:**
1. Vários `modelTurn.parts` com chunks de áudio (streaming)
2. `outputTranscription.text` em fragmentos (streaming)  
3. `generationComplete: true`
4. (silêncio de cauda)
5. `turnComplete: true` com `usageMetadata`

---

## 7. Session Resumption — Reconexão sem perda de contexto

```json
{ "sessionResumptionUpdate": { "newHandle": "TOKEN_OPACO", "resumable": true, "lastConsumedClientMessageIndex": 42 } }
```

| Campo | Descrição |
|-------|-----------|
| `newHandle` | Token opaco — salvar e usar no próximo `setup.sessionResumptionConfig.handle` |
| `resumable` | `false` = contexto foi compactado pelo servidor — reconexão não recupera contexto completo |
| `lastConsumedClientMessageIndex` | Índice da última mensagem processada pelo servidor |

**Para reconectar:** passar `handle` no `setup`:
```json
{ "setup": { "sessionResumptionConfig": { "handle": "TOKEN_SALVO" } } }
```

---

## 8. goAway — Servidor avisa que vai encerrar

```json
{ "goAway": { "timeLeft": "5s" } }
```

Modelo tem `timeLeft` segundos antes de encerrar. Reconectar imediatamente com `sessionResumptionToken` se disponível.

---

## 9. usageMetadata — Contagem de tokens

Chega junto com `turnComplete`.

```json
{ "usageMetadata": { "promptTokenCount": 5094, "responseTokenCount": 117, "totalTokenCount": 5211 } }
```

| Campo | Descrição |
|-------|-----------|
| `promptTokenCount` | Tokens no contexto atual (system prompt + histórico + ficha). Se cair > 500 → compressão de contexto |
| `responseTokenCount` | Tokens gerados na resposta. Se = 1 com 0 bytes de áudio → bug `<ctrl46>` |
| `totalTokenCount` | Total do turno |

---

## 10. Bug `<ctrl46>` — Referência

**O que é:** modelo emite tokens de controle `<ctrl46>` em `outputTranscription` em vez de gerar áudio PCM. Resultado: resposta silenciosa, `responseTokenCount=1`, sessão encerra logo depois.

**Quando ocorre:** após múltiplas tool calls em sequência dentro da mesma sessão. Observado em `tc=3` (3ª tool call) em 26/05/2026.

**Mitigação implementada:**
1. `buscarCatalogo` e `consultarManual` declarados como `NON_BLOCKING` — modelo fala enquanto tool processa, reduz ciclos silêncio→fala
2. `scheduling=SILENT` nas respostas — evita segundo turno de áudio
3. Detector no código: log `🚨 <ctrl46> DETECTADO: N tokens | tc=X` quando encontrado em `outputTranscription`
4. Contador `toolCallCount` por sessão — correlaciona com o momento do bug

**Detector no GeminiLiveService.kt:**
```kotlin
if (fragmento.contains("<ctrl46>")) {
    val ctrl46Count = fragmento.split("<ctrl46>").size - 1
    android.util.Log.e("GeminiLive", "🚨 <ctrl46> DETECTADO: $ctrl46Count tokens | tc=$toolCallCount na sessão")
}
```

---

## Resumo: o que está ativo vs desativado no projeto

| Feature | Status | Motivo |
|---------|--------|--------|
| `responseModalities: AUDIO` | ✅ Ativo | Modelo de áudio nativo |
| `outputAudioTranscription` | ✅ Ativo | Texto do que o mestre falou |
| `inputAudioTranscription` | ✅ Ativo | Texto do que o usuário falou |
| `sessionResumptionConfig` | ✅ Ativo | Reconexão sem perda de contexto |
| `behavior: NON_BLOCKING` | ✅ Ativo | `buscarCatalogo` + `consultarManual` |
| `scheduling: SILENT` | ✅ Ativo | Evita duplo turno de áudio |
| `contextWindowCompression` | ❌ Desativado | Comportamento não documentado no native-audio-preview |
| `realtimeInputConfig` | ❌ Desativado | Idem |
| `proactivity` | ❌ Desativado | Idem |
| `transparent resumption` | ❌ Desativado | Não suportado no 2.5 preview |
