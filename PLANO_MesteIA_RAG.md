# PLANO: Melhorias no Sistema RAG do Mestre IA
**Data:** 23 de Maio de 2026  
**Última revisão:** 23 de Maio de 2026  
**Autor:** Claude Sonnet 4.6  
**Objetivo:** Tornar o RAG rápido, preciso e escalável para 40+ livros de GURPS.

---

## STATUS GERAL DOS LOTES

| Lote | Melhoria | Status | Commit |
|---|---|---|---|
| 251 | Plano RAG + arquitetura inicial | ✅ FEITO | 3c3d43b |
| 252 | BM25-Kotlin + filtro por fonte + pool 500 | ✅ FEITO | db839d5 |
| 253 | LRU Cache de buscas FTS | ✅ FEITO | 3372e58 |
| 254 | Pocket RAG: compressão seletiva de contexto | ✅ FEITO | 2a551bd |
| 255 | Dicionário 90+ entradas + livrosPorCategoria | ✅ FEITO | 8e92543 |
| 256 | topic_index.json — índice de tópicos | ⚠️ FEITO MAS REVISAR | 8bef6dc |
| 257 | Prompt: raciocínio com lacunas (Falha Tipo 3) | 🔲 PENDENTE | — |
| 258 | Query rewriting pela IA antes do FTS | 🔲 PENDENTE | — |
| 259 | SQLite-vec: busca semântica híbrida | 🔲 PENDENTE | — |
| 260 | Rechunking: chunks menores via script Python | 🔲 PENDENTE | — |
| 261 | Pipeline offline: processar livros novos | 🔲 PENDENTE | — |
| 262 | Tabelas FTS por categoria (escala 15k+ chunks) | 🔲 PENDENTE | — |

---

## AVISO: O que foi feito de ERRADO e por quê

### Lote 256 — topic_index.json (⚠️ REVISAR)

O `topic_index.json` foi implementado como um conjunto de **casos hardcoded** para cenários específicos ("tiro subaquático", "queda", "fogo", etc.). Isso é o oposto do que o RAG deveria ser.

**O problema:** Um RAG serve para achar qualquer regra que o usuário perguntar — não só os 11 casos que alguém listou. "Cavar em solo de Marte", "colisão de veículo em gravidade baixa", "deslocamento com carga pesada numa tempestade" — nada disso está no topic_index.json e nunca vai estar, porque as combinações de cenário em GURPS são infinitas.

**O que deveria ser:** O motor de busca deve ser bom o suficiente para achar qualquer regra, não uma lista de exceções manuais.

**Decisão:** Manter o topic_index.json por enquanto (não quebra nada), mas a solução real é o Lote 259 (busca semântica) que elimina a necessidade de qualquer lista manual.

---

## Os 3 Tipos de Falha (status atual)

### Falha Tipo 1 — RAG não acha o chunk certo
O chunk com a resposta existe no banco, mas não entra no top-30 enviado à IA.

- **Causa raiz:** FTS4 com keyword matching puro. "tiro na piscina" não encontra "divisor de alcance subaquático" porque são palavras diferentes.
- **O que foi feito:** BM25-Kotlin (Lote 252), dicionário de sinônimos 90+ entradas (Lote 255). Melhora parcialmente — dicionário manual não cobre combinações novas.
- **Solução real:** Busca semântica (Lote 259) — entende que "piscina" e "subaquático" são semanticamente próximos sem lista manual.
- **Sinal no log:** `RAG OK: 30 chunks` mas nenhum da página correta.

### Falha Tipo 2 — Chunk está no contexto mas IA ignora
O chunk correto chega, mas o modelo não o usa — usa conhecimento de treinamento em vez do manual.

- **Causa raiz:** Contexto com 30 chunks misturados sem hierarquia clara. A IA não sabe qual chunk priorizar.
- **O que foi feito:** Pocket RAG com marcadores ★★★/★★/★ (Lote 254). Chunks de alta relevância chegam completos, os demais comprimidos.
- **Solução complementar:** Lote 257 (instrução no prompt para a IA citar a fonte antes de responder).
- **Sinal no log:** Página correta nas páginas recebidas, mas resposta errada.

### Falha Tipo 3 — IA tem a regra mas raciocina errado
A IA encontra "divida o alcance por 1.000" mas diz que 50m÷1000 = 4m alcançável.

- **Causa raiz:** Sem instrução explícita para mostrar cálculos passo a passo. E sem instrução de como agir quando a regra **não existe** no manual (risco de alucinação).
- **O que foi feito:** Nada ainda para Tipo 3.
- **Solução:** Lote 257 — prompt de raciocínio estruturado com protocolo para lacunas.
- **Sinal no log:** Resposta cita a página correta mas o cálculo/conclusão está errado. Ou: regra não existe e a IA inventa.

---

## ONDA 1 — Rápido, sem mudar arquitetura

### Melhoria 1A — FTS4 → FTS5 com BM25 nativo
**Status: ✅ IMPLEMENTADO PARCIALMENTE (BM25 em Kotlin, não FTS5 nativo)**

O BM25 foi implementado em Kotlin sobre os resultados do FTS4 (Lote 252) porque Room 2.6.1 não tem suporte a `@Fts5`. FTS5 real requer Room 2.7-alpha, que foi considerado arriscado para produção. O resultado é matematicamente equivalente, mas mais lento que o BM25 nativo do SQLite.

**Pendente:** Quando Room 2.7 sair em versão estável, migrar para FTS5 nativo e remover o loop BM25 em Kotlin.

---

### Melhoria 1B — Filtro por source_id antes do FTS
**Status: ✅ FEITO (Lote 255)**

`livrosPorCategoria` detecta o cenário e filtra os source_ids relevantes. A busca não vai em todos os livros ao mesmo tempo quando o cenário é claro.

---

### Melhoria 1C — Chunking menor ao processar livros novos
**Status: 🔲 PENDENTE (Lote 260)**

Os chunks atuais têm média de ~5.949 chars — muito grandes. Deveria ser 400-800 chars.

**O que fazer:** Script Python externo que reprocessa o `chunks.jsonl`, quebrando cada chunk grande em sub-chunks de ~600 chars com overlap de ~100 chars. Não mexe no app.

```python
def rechunk(texto, max_chars=600, overlap=100):
    chunks = []
    inicio = 0
    while inicio < len(texto):
        fim = min(inicio + max_chars, len(texto))
        if fim < len(texto):
            fim = texto.rfind('. ', inicio, fim) + 1 or fim
        chunks.append(texto[inicio:fim])
        inicio = fim - overlap
    return chunks
```

**Impacto:** Chunks de 5.949 chars → ~8-10 sub-chunks de 600 chars. Modelo recebe parágrafos específicos, não seções inteiras. Scoring BM25 mais preciso (menos ruído interno no chunk).

---

## ONDA 2 — Muda a qualidade de resposta

### Melhoria 2A — SQLite-vec: busca semântica
**Status: 🔲 PENDENTE (Lote 259)**  
**Esta é a solução real para a Falha Tipo 1.**

Veja a seção completa abaixo.

---

### Melhoria 2B — Cache LRU de buscas
**Status: ✅ FEITO (Lote 253)**

LRU cache de 20 entradas no `MestreIARepository`. Perguntas repetidas na mesma sessão não refazem a busca FTS.

---

### Melhoria 2C — Compressão seletiva de contexto (Pocket RAG)
**Status: ✅ FEITO (Lote 254)**

Chunks ★★★ (score ≥ 8.0): texto completo. Chunks ★★ e ★: comprimidos às sentenças relevantes. Reduz contexto de ~35KB para ~12-18KB.

---

### Melhoria 2D — Índice de tópicos
**Status: ⚠️ FEITO MAS COM RESSALVA (Lote 256)**

Implementado como lista de 11 casos hardcoded. Funciona para os casos mapeados, mas não escala. A solução real é o Lote 259 (semântica). Manter por enquanto — não quebra nada e cobre os casos mais críticos enquanto o Lote 259 não está pronto.

---

### Lote 257 — Prompt: Raciocínio com Lacunas (NOVO)
**Status: 🔲 PENDENTE**  
**Resolve: Falha Tipo 3 + alucinação quando regra não existe**

**O problema:**
Quando o usuário pergunta algo que não tem regra específica no manual (ex: "cavar em solo de Marte"), o modelo tem duas opções ruins:
1. Inventar uma regra (alucinação)
2. Dizer "não sei" (inútil)

A opção correta é uma terceira: **compor uma resposta com as regras existentes, deixando claro que é uma interpretação**.

Quando o usuário pergunta algo que tem regra e envolve cálculo (ex: "alcance do revólver na água"), o modelo às vezes encontra a regra mas erra o cálculo.

**O que fazer:** Adicionar no prompt do sistema dois protocolos obrigatórios:

**Protocolo A — Cálculo:**
```
QUANDO a resposta envolver fórmula, divisor ou modificador:
1. Cite a regra: "[Fonte, Pág. X]: [texto exato]"
2. Identifique os valores: "Valores: alcance=Xm, divisor=1000"
3. Faça o cálculo explícito: "Cálculo: X ÷ 1000 = Y"
4. Dê a conclusão: "Resultado: Y metros de alcance efetivo"
NUNCA dê conclusão sem mostrar o cálculo.
```

**Protocolo B — Lacuna (regra não existe):**
```
QUANDO não houver regra específica no Códex para o cenário exato:
1. Declare explicitamente: "Não há regra específica para [cenário] no material disponível."
2. Identifique as regras aplicáveis: "Regras relacionadas encontradas: [lista]"
3. Componha uma interpretação: "Aplicando [Regra X] + [Regra Y] ao cenário:"
4. Marque como interpretação: "⚠️ Interpretação RAG — verifique com o Mestre."
NUNCA invente regras. NUNCA diga apenas "não sei".
```

**Arquivo a modificar:** `MestreIAPromptsAuditor.kt` (ou equivalente de prompt do sistema)

---

### Lote 258 — Query Rewriting pela IA (NOVO)
**Status: 🔲 PENDENTE**  
**Resolve: Falha Tipo 1 — gap semântico entre linguagem do jogador e terminologia do manual**

**O problema:**
O usuário digita linguagem natural. O FTS4/FTS5 precisa de termos técnicos do GURPS. O dicionário de sinônimos (90+ entradas) compensa parcialmente, mas não cobre combinações novas.

**O que fazer:**
Antes da busca FTS, usar a própria IA (chamada leve, sem contexto RAG) para reformular a pergunta em termos técnicos do GURPS:

```
Iteração 0 (nova, antes do FTS):
  Input: "posso usar minha arma debaixo d'água?"
  Prompt: "Reescreva esta pergunta em 5-8 termos técnicos do sistema GURPS 4ª ed. separados por vírgula. Apenas os termos, sem explicação."
  Output da IA: "tiro subaquático, penalidade alcance, arma distância, ambiente aquático, divisor alcance"

  → Esses termos vão para o FTS em vez (ou além) dos termos originais
```

**Custo:** 1 chamada extra ao modelo (pequena, sem contexto RAG). Mas elimina a necessidade de manter dicionário manualmente.

**Condição de ativação:** Só quando a busca FTS retornar menos de 5 chunks (sinal de que os termos originais não acharam nada).

---

## ONDA 2 — Busca Semântica

### Lote 259 — SQLite-vec: Busca Semântica Híbrida (NOVO)
**Status: 🔲 PENDENTE**  
**Resolve: Falha Tipo 1 definitivamente — sem dicionário manual, sem casos hardcoded**

**O que é:**
SQLite-vec é uma extensão SQLite que adiciona busca vetorial KNN diretamente no banco. Você gera embeddings (vetores numéricos que representam o significado do texto) **offline no PC** com Python, salva junto com os chunks, e o app faz busca semântica em <50ms sem nenhum modelo rodando.

**Por que isso resolve o problema raiz:**
"piscina" e "subaquático" têm vetores parecidos porque ambas as palavras aparecem em contextos similares nos textos de treinamento do modelo de embedding. A distância vetorial encontra a página correta mesmo que nenhuma keyword coincida.

**Fluxo completo:**
```
[PC — ao processar livro novo]
  Script Python:
  1. Lê chunks.jsonl
  2. Gera embedding de 384 dims para cada chunk via sentence-transformers (all-MiniLM-L6-v2, ~80MB)
  3. Salva no chunks.jsonl: { ..., "embedding": [0.12, -0.34, ...384 floats] }

[App — na importação do chunks.jsonl]
  FichaDatabase:
  4. Cria tabela vec_chunks(chunk_id TEXT, embedding BLOB)
  5. Insere vetores junto com os chunks de texto

[Em tempo de busca]
  Arquitetura híbrida FTS + semântica:
  
  a) FTS5/FTS4 → pool de 200 candidatos por keyword (~10ms)
  b) Embedding da query via API Gemini Flash Lite → vetor da pergunta (~200ms)
  c) SQLite-vec → reranqueia os 200 por similaridade cosseno (~30ms)
  d) Top-30 chunks: híbrido keyword + semântica combinados
```

**Embedding da query no app:**
Usar API Gemini Flash Lite (já disponível via `BuildConfig.MESTRE_IA_LITE_1_URL`) para gerar o embedding da pergunta. 1 chamada HTTP de ~200ms — muito mais leve que rodar modelo local.

**Dependência Android:**
```kotlin
// build.gradle.kts
implementation("io.github.asg017:sqlite-vec-android:0.1.3")
```

**Impacto esperado:**
- "cavar em solo de Marte" → acha "trabalho físico ST", "ambiente hostil", "gravidade reduzida" automaticamente
- Elimina necessidade de dicionário de sinônimos manual e topic_index hardcoded
- Escala para 40 livros sem degradação de precisão
- Latência total adicionada: ~230ms (embedding via API)

---

## ONDA 3 — Escala para 40 livros

### Lote 260 — Rechunking: Script Python de chunks menores
**Status: 🔲 PENDENTE**

Script Python externo. Não mexe no app. Reprocessa `chunks.jsonl` quebrando chunks de ~5.949 chars em sub-chunks de ~600 chars com overlap de 100 chars.

**Resultado:** 1.196 chunks → ~10.000-12.000 chunks menores. Scoring mais preciso, modelo recebe parágrafos específicos, não seções inteiras.

---

### Lote 261 — Pipeline offline: processar livros novos
**Status: 🔲 PENDENTE**

Script `processar_livro.py` completo:
```
[Input]  PDF do livro GURPS
    ↓
1. Extração de texto (PyMuPDF / pdfplumber)
2. Chunking inteligente: 400-800 chars, quebra em fronteira de parágrafo
3. Geração de embeddings offline (all-MiniLM-L6-v2)
4. Geração de source_id padronizado (pt_ultra_tech, pt_social_engineering...)
5. Append no chunks.jsonl
    ↓
[Output] chunks.jsonl atualizado, pronto para o próximo build do app
```

Adicionar livro novo = 1 comando Python, 5-10 minutos no PC, sem tocar no app.

---

### Lote 262 — Tabelas FTS por categoria (escala 15k+ chunks)
**Status: 🔲 PENDENTE**

Quando ultrapassar ~5.000 chunks, uma tabela FTS única começa a ficar lenta. Solução: tabelas separadas por categoria.

```
manual_chunks_core      → Módulo Básico
manual_chunks_combat    → GunFu + Artes Marciais + Tactical Shooting
manual_chunks_magic     → Magia + Thaumatology
manual_chunks_sci       → Ultra-Tech + Bio-Tech
manual_chunks_social    → Social Engineering + Mass Combat
```

O `MestreIAGraphEngine` busca só nas tabelas relevantes detectadas pelo `MestreIAPlanner`.

---

## Ordem de Implementação Recomendada (revisada)

| Prioridade | Lote | Melhoria | Falha Resolvida | Complexidade |
|---|---|---|---|---|
| **1** | 257 | Prompt: raciocínio com lacunas | Tipo 3 + alucinação | Baixa — só prompt |
| **2** | 258 | Query rewriting pela IA | Tipo 1 — keyword gap | Média |
| **3** | 259 | SQLite-vec semântico | Tipo 1 definitivo | Média |
| **4** | 260 | Rechunking via Python | Tipo 1 + Escala | Baixa — script externo |
| **5** | 261 | Pipeline offline de livros | Escalabilidade | Alta |
| **6** | 262 | Tabelas por categoria | Escala 15k+ chunks | Alta |

---

## Ganhos Esperados

### Após Lote 257 (rápido):
- Fim das alucinações: a IA para de inventar regras que não existem
- Fim dos erros de cálculo: protocolo obrigatório de mostrar passo a passo
- Respostas com lacuna viram interpretações úteis, não negativas vazias

### Após Lote 259 (semântica):
- "cavar em Marte", "gravidade X", "qualquer cenário novo" → acha regras relevantes sem lista manual
- Elimina dicionário de sinônimos e topic_index como muletas
- Escala para 40 livros sem degradação

### Após Lotes 260-262 (escala):
- Adicionar livro: 1 comando Python
- Pronto para 40+ livros / 20.000+ chunks
- Busca: <500ms mesmo com corpus completo

---

## Referências

| Tecnologia | Relevância |
|---|---|
| SQLite-vec (sqlite-vec-android) | Lote 259 — busca semântica no SQLite |
| all-MiniLM-L6-v2 (sentence-transformers) | Lote 259/261 — embeddings offline 384 dims |
| SQLite FTS5 + BM25 nativo | Lote 252 revisão — quando Room 2.7 stable sair |
| Pocket RAG (arXiv 2602.13229) | Lote 254 — compressão seletiva de contexto |

---

## O que NÃO fazer

| Abordagem | Motivo |
|---|---|
| Mais entradas no dicionário de sinônimos | Não escala. Qualquer combinação nova falha. |
| Mais casos no topic_index.json | Mesma razão. A solução é semântica, não lista. |
| Modelo de embedding rodando no app | Gerar offline no PC é melhor — sem latência, sem consumo de bateria |
| Gemini Nano / LiteRT on-device | Qualidade muito inferior ao DeepSeek para raciocínio complexo de GURPS |

---

*Última revisão: 23/05/2026. Próxima revisão: ao implementar Lote 259 ou ao atingir 5.000+ chunks.*
