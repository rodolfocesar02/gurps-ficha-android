# Planejamento: Mestre IA com Voz Bidirecional (Gemini Live API)

**Data:** Maio de 2026  
**Status:** Planejamento — NÃO implementado ainda  
**Objetivo:** Substituir o ciclo STT→Texto→TTS por uma conversa de voz natural e contínua

---

## Decisões de Design (Confirmadas)

| Decisão | Escolha |
|---------|---------|
| Voz | **Charon** (masculina, grave) |
| Histórico | **Opção B** — voz integrada ao chat de texto. Transcrições salvas no `ChatHistoryDao` |
| Ferramentas | **Mesmas do Forjador atual** — Gemini Live é nova interface, não nova lógica |
| Raciocínio por baixo | Gemini (não DeepSeek/MiMo) — mas ferramentas Kotlin continuam iguais |
| Ativar/desativar | `BuildConfig.VOZ_BIDIRECIONAL_HABILITADA` — false por padrão |
| GIF animado no ícone | **Fase 5 (futuro)** — animação do ícone do Mestre IA durante conversa por voz |

---

## O Que Queremos

O usuário fala com o Mestre IA como se fosse uma ligação:

> "Ei Mestre, adiciona Reflexos em Combate na minha ficha"  
> *Mestre IA (enquanto verifica os pontos por baixo):* "Claro, Rodolfo! Vamos ver aqui... você tem 15 pontos sobrando, dá pra colocar sim. Fica com 10 pontos restantes. É só isso ou quer mais alguma coisa?"

O modelo **fala enquanto pensa**, chama as ferramentas do Forjador por baixo, e mantém a personalidade do Mestre durante tudo.

---

## Por Que o Sistema Atual Não Chega Lá

O sistema atual faz:
```
Usuário fala → SpeechRecognizer (Android) → texto → modelo de texto → texto → TTS nativo → áudio
```

São 3 sistemas diferentes colados. Resultado:
- Voz robótica (TTS nativo Android = nota 3/10)
- Sem personalidade na fala
- Modelo não "pensa em voz alta" enquanto usa ferramentas
- Latência perceptível em cada etapa

---

## A Solução: Gemini Live API

Uma única conexão WebSocket que recebe áudio, raciocina, chama ferramentas e responde em áudio de forma contínua.

```
Microfone (PCM 16kHz) → WebSocket → Gemini 3.1 Flash Live → Áudio (PCM 24kHz) → Alto-falante
                                           ↕
                                    Tool calling (Forjador)
```

---

## Detalhes Técnicos

### Conexão
- **Protocolo:** WebSocket persistente
- **Endpoint:**
  ```
  wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent?key=API_KEY
  ```
- **Modelo recomendado:** `gemini-2.5-flash-native-audio-preview-12-2025`
- **Autenticação:** Mesma chave Google/Gemini já usada no app

### Formato de Áudio
| Direção | Formato | Sample Rate | Canais |
|---------|---------|-------------|--------|
| Entrada (microfone) | PCM 16-bit | 16.000 Hz | Mono |
| Saída (alto-falante) | PCM 16-bit | 24.000 Hz | Mono |

### Voz Escolhida
**Charon** — voz masculina, grave e sóbria. Testada em PT-BR em 22/05/2026.  
Vozes disponíveis na chave `mestre.ia.gemini1.key`: `Fenrir`, `Puck`, `Charon`, `Kore`, `Aoede`, `Zephyr`, `Schedar`, `Leda`, `Orus`, `Erinome`, `Iapetus`, `Callirrhoe`  
Clonagem de voz: **não disponível** nessa API (exigiria ElevenLabs pago).

### Chave API
Usar `mestre.ia.gemini1.key` do `local.properties` → `BuildConfig.MESTRE_IA_GEMINI_KEY`  
Modelos TTS confirmados disponíveis: `gemini-2.5-flash-preview-tts`, `gemini-2.5-pro-preview-tts`  
Modelo Live confirmado: `gemini-2.5-flash-native-audio-latest` (suporta `bidiGenerateContent`)

### BuildConfig Flag
```kotlin
// build.gradle.kts — controla se o botão de voz bidirecional aparece no app
buildConfigField("Boolean", "VOZ_BIDIRECIONAL_HABILITADA", "false")
// Para gerar APK de teste: mudar para "true"
```

### Primeira Mensagem (Setup)
```json
{
  "setup": {
    "model": "gemini-2.5-flash-native-audio-latest",
    "generationConfig": {
      "responseModalities": ["AUDIO"],
      "speechConfig": {
        "voiceConfig": { "presetVoice": "Charon" }
      },
      "contextWindowCompression": {
        "maxTokens": 25000,
        "slidingWindowSize": 8000
      }
    },
    "systemInstruction": { "parts": [{ "text": "..." }] },
    "tools": [{ "function_declarations": [...] }]
  }
}
```

### Tool Calling
O modelo sinaliza uma chamada de ferramenta:
```json
{
  "toolCall": {
    "functionCalls": [{
      "id": "call_123",
      "name": "adicionarVantagem",
      "args": { "nome": "Reflexos em Combate", "nivel": 1 }
    }]
  }
}
```

O app executa e responde:
```json
{
  "toolResponse": {
    "functionResponses": [{
      "id": "call_123",
      "name": "adicionarVantagem",
      "response": { "sucesso": true, "pontosRestantes": 10 }
    }]
  }
}
```

**Importante:** O modelo pode continuar falando enquanto a ferramenta executa (não bloqueante por padrão). Para fazer ele esperar o resultado antes de continuar, adicionar `"scheduling": "WHEN_IDLE"`.

### Limitações Críticas
| Limitação | Valor |
|-----------|-------|
| Duração de sessão | ~15 minutos (depois disso reconectar) |
| Timeout WebSocket | ~10 minutos sem atividade |
| Context window | 128k tokens (áudio = ~25 tokens/segundo) |
| Latência de resposta | ~500-2000ms |

O servidor avisa antes de desconectar com uma mensagem `GoAway` — o app deve reconectar e resumir o contexto.

---

## Custo Estimado

| Uso diário | Custo/mês |
|-----------|-----------|
| 10 min/dia | ~$7 |
| 30 min/dia | ~$20 |
| 1 hora/dia | ~$40 |

**Comparação:** OpenAI GPT-Realtime-2 custaria ~$35-140/mês para o mesmo uso.

---

## SDK: Firebase AI Logic vs WebSocket Direto

### Firebase AI Logic (recomendado)
```kotlin
dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.13.0"))
    implementation("com.google.firebase:firebase-ai")
}
```
- Abstrai reconexão automática
- Suporte nativo a AudioRecord/AudioTrack
- Tool calling integrado
- Ainda em developer preview

### WebSocket Direto (OkHttp)
- Mais controle, menos dependências
- Código mais verboso
- Você gerencia reconexão e buffering

**Decisão:** Começar com Firebase AI Logic. Se tiver limitações, migrar para WebSocket direto.

---

## Ferramentas do Forjador a Expor

Mapeamento direto para métodos reais do `FichaViewModel`:

| Ferramenta (Gemini Live) | Método Real no ViewModel | Observação |
|-----------|-----------|-----------|
| `obterFicha` | `viewModel.personagem` | Retorna nome, pontos, vantagens, perícias, etc. |
| `obterPontosRestantes` | `viewModel.personagem.pontosRestantes` | Calculado em `Personagem.kt:178` |
| `adicionarVantagem` | `viewModel.adicionarVantagem(def, nivel, custo)` | Precisa buscar `VantagemDefinicao` no catálogo antes |
| `removerVantagem` | `viewModel.removerVantagem(index)` | Requer índice da lista |
| `adicionarDesvantagem` | `viewModel.adicionarDesvantagem(def, nivel, custo)` | Mesmo padrão da vantagem |
| `adicionarPericia` | `viewModel.adicionarPericia(def, pts, esp)` | Precisa buscar `PericiaDefinicao` no catálogo |
| `removerPericia` | `viewModel.removerPericia(index)` | Requer índice |
| `adicionarEquipamento` | `viewModel.adicionarEquipamento(e)` | Recebe objeto `Equipamento` |
| `consultarManual` | Sistema RAG existente (`MestreIAUseCase`) | Reutiliza busca FTS já implementada |

**Fluxo de busca no catálogo:** antes de chamar `adicionarVantagem`, o `GeminiLiveTools.kt` precisa buscar a `VantagemDefinicao` pelo nome no `DataRepository` — o mesmo que o Forjador texto já faz.

## System Prompt do Mestre IA (Voz)

```
Você é o Mestre IA de GURPS — um mestre de campanha experiente, sábio e com personalidade própria.
Fale sempre em português brasileiro, de forma natural e conversacional.
Seu nome é Mestre. O personagem atual se chama [NOME_PERSONAGEM].

REGRAS DE COMPORTAMENTO:
- Fale enquanto pensa — não fique em silêncio enquanto processa
- Antes de modificar a ficha, SEMPRE verifique os pontos disponíveis primeiro
- Confirme o que fez depois de executar — ex: "Pronto, adicionei X, ficam Y pontos"
- Se o usuário pedir algo impossível (sem pontos), explique e sugira alternativas
- Para dúvidas de regras, consulte o manual antes de responder
- Seja direto e objetivo — respostas curtas são melhores que longas
- Mantenha personalidade consistente: sábio, justo, levemente dramático

NUNCA:
- Invente regras que não existem no manual
- Modifique a ficha sem confirmar o resultado
- Fique mais de 3 segundos em silêncio
```

---

## O Que Precisa Ser Feito (Ordem de Implementação)

### Fase 1 — Prova de Conceito
- [ ] Criar `GeminiLiveService.kt` com conexão WebSocket básica
- [ ] Capturar áudio com `AudioRecord` (16kHz PCM)
- [ ] Reproduzir áudio com `AudioTrack` (24kHz PCM)
- [ ] Setup da sessão com system prompt do Mestre IA
- [ ] Testar conversa simples sem ferramentas

### Fase 2 — Ferramentas
- [ ] Expor ferramentas do Forjador no protocolo Gemini Live
- [ ] Implementar handler de `toolCall` → executa → envia `toolResponse`
- [ ] Testar: "adiciona Reflexos em Combate" → verifica pontos → adiciona → confirma

### Fase 3 — UX
- [ ] Botão dedicado no app para entrar no "Modo Conversa"
- [ ] Indicador visual de estado (ouvindo / pensando / falando)
- [ ] Reconexão automática ao timeout
- [ ] Parar a fala ao tocar na tela

### Fase 4 — Qualidade
- [ ] Ajustar system prompt para personalidade do Mestre
- [ ] Testar PT-BR com voz Charon
- [ ] Compressão de contexto para sessões longas

### Fase 5 — UX Avançada (Futuro)
- [ ] GIF animado no ícone do Mestre IA durante conversa por voz (ouvindo / pensando / falando)
- [ ] Microfone dentro do dialog (Fase 2 do sistema de voz atual)

---

## Arquivos que Serão Criados/Modificados

| Arquivo | Ação |
|---------|------|
| `ui/components/GeminiLiveService.kt` | NOVO — gerencia WebSocket + áudio |
| `ui/components/GeminiLiveTools.kt` | NOVO — ponte entre Gemini Live e Forjador |
| `ui/DialogsMestreIA.kt` | MODIFICAR — botão "Modo Conversa" |
| `ui/FichaScreen.kt` | MODIFICAR — estado do modo conversa |
| `AndroidManifest.xml` | JÁ TEM — RECORD_AUDIO já declarado |

---

## O Que NÃO Muda

- O sistema atual de texto (chat digitado) continua igual
- O VozMestreIA.kt (segurar ícone) continua como fallback
- Os modelos de texto (DeepSeek, Gemini Flash, etc.) continuam para o chat
- O Forjador e toda a lógica de integração na ficha continuam iguais

O Gemini Live é um **modo adicional**, não uma substituição.

---

## Referências

- [Gemini Live API Overview](https://ai.google.dev/gemini-api/docs/live-api)
- [Tool use with Live API](https://ai.google.dev/gemini-api/docs/live-api/tools)
- [Firebase AI Logic - Live API](https://firebase.google.com/docs/ai-logic/live-api)
- [Android Developers - Gemini Live](https://developer.android.com/ai/gemini/live)
- [Session management](https://ai.google.dev/gemini-api/docs/live-session)
- [Live API Best Practices](https://ai.google.dev/gemini-api/docs/live-api/best-practices)
