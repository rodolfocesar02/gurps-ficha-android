# PLANO: Melhorias no Sistema RAG do Mestre IA
**Data:** 23 de Maio de 2026
**Autor:** Claude Sonnet 4.6 — leitura direta do código + pesquisa web maio/2026
**Objetivo:** Tornar o RAG rápido, preciso e escalável para 40+ livros de GURPS.

---

## Contexto: O que foi analisado

O agente leu na íntegra os seguintes arquivos antes de escrever este plano:
- `MestreIAGraphEngine.kt` — motor de scoring e busca
- `MestreIAPlanner.kt` — extração de termos e sub-queries
- `MestreIAQueryEngine.kt` — geração de queries FTS
- `MestreIARepository.kt` — sincronização e acesso ao banco
- `ManualChunkDao.kt` — queries SQL do banco de chunks
- `FichaDatabase.kt` — importação e schema do banco
- `MestreIAUseCase.kt` — loop de iterações e orquestração
- `chunks.jsonl` (primeiras 30 linhas) — formato e tamanho dos chunks

---

## Diagnóstico: Números Reais do Sistema Atual

| Métrica | Valor Atual | Problema |
|---|---|---|
| Total de chunks | 1.196 | Só 5 livros. Com 40 livros → ~15.000 chunks |
| Tamanho médio por chunk | **5.949 chars** | Enorme. Deveria ser 400-800 chars |
| Pool FTS (candidatos) | **1.500 chunks** | Com 15k chunks, scoring vai travar |
| Chunks enviados ao modelo | 30 | Fixo, independente da qualidade |
| Contexto RAG máximo | **35 KB** | Muito ruído, pouca hierarquia |
| Termos por query FTS | Até 20 | Corta sinônimos relevantes |
| Dicionário de sinônimos | **24 entradas** | Impossível cobrir 40 livros manualmente |
| Iterações por resposta | Até 3 | 3 chamadas ao modelo = 3x a latência |
| Algoritmo de ranking | Scoring manual em Kotlin | Lento, impreciso, não-padrão |
| Banco FTS | **FTS4** | Versão antiga, sem BM25 nativo |

**Problema central:** O sistema foi construído para 1.200 chunks. Com 40 livros (estimativa: 15.000-20.000 chunks), tudo que funciona "razoável" hoje vai quebrar — lentidão, pool explodindo, dicionário impossível de manter.

---

## Os 3 Tipos de Falha (ainda ativos)

### Falha Tipo 1 — RAG não acha o chunk certo
O chunk com a resposta existe no banco, mas não entra no top-30 enviado à IA.
- **Causa:** FTS4 com keyword matching puro. "tiro na piscina" não encontra "divisor de alcance subaquático".
- **Sinal no log:** `RAG OK: 30 chunks` mas nenhum da página correta.

### Falha Tipo 2 — Chunk está no contexto mas IA ignora
O chunk correto chega, mas o modelo não o usa — ele usa conhecimento de treinamento.
- **Causa:** 30 chunks = ~35KB de texto misturado sem hierarquia clara. A IA não sabe qual chunk priorizar.
- **Sinal no log:** Página correta nas páginas recebidas, mas resposta errada.

### Falha Tipo 3 — IA tem a regra mas raciocina errado
A IA encontra "divida o alcance por 1.000" mas diz que 50m÷1000 = 4m alcançável.
- **Causa:** Sem instrução explícita para mostrar os cálculos passo a passo antes da conclusão.
- **Sinal no log:** Resposta cita a página correta mas o cálculo/conclusão está errado.

---

## PROPOSTA CONCRETA — Ponta a Ponta

As melhorias estão divididas em 3 ondas por impacto e complexidade.
Cada uma pode ser implementada independentemente.

---

## ONDA 1 — Rápido, sem mudar arquitetura
### (Impacto imediato, baixo risco, 1-3 dias)

---

### Melhoria 1A — FTS4 → FTS5 com BM25 nativo
**Resolve:** Falha Tipo 1 (ranking ruim) + Velocidade
**Complexidade:** Baixa — 2 arquivos (`ManualChunkDao.kt` + `FichaDatabase.kt`)
**Referência:** SQLite FTS5 documentation — [sqlite.org/fts5.html](https://www.sqlite.org/fts5.html#bm25_ranking_function) — pesquisa maio/2026

**O problema hoje:**
O banco usa `@Fts4` (versão antiga do SQLite). O scoring é feito em Kotlin: loop por 1.500 chunks, calculando pesos manualmente (raridade, proximidade, AND-bonus). Isso é lento e matematicamente inferior ao padrão da indústria.

**A solução:**
FTS5 tem a função `bm25()` nativa — ela faz o ranking diretamente no SQLite antes de retornar os resultados. BM25 considera frequência do termo no documento *e* frequência no corpus inteiro (TF-IDF melhorado). O banco já devolve os chunks ordenados pelo melhor ranking, sem loop em Kotlin.

**Antes (FTS4):**
```sql
-- Busca sem ordem, retorna 1.500 candidatos para Kotlin ordenar
SELECT * FROM manual_chunks WHERE search_text MATCH :query LIMIT 1500
```

**Depois (FTS5 + BM25):**
```sql
-- Banco já retorna os 200 melhores, ordenados matematicamente
SELECT *, bm25(manual_chunks) as score
FROM manual_chunks WHERE search_text MATCH :query
ORDER BY score LIMIT 200
```

**Impacto esperado:**
- Velocidade: **5-8x mais rápido** (SQLite C nativo vs. loop Kotlin)
- Precisão: melhor ranking (BM25 é o padrão acadêmico de IR)
- Escala: com 15.000 chunks, ainda funciona rápido
- Pool reduz de 1.500 → 200 (menos ruído para o scoring residual)

**O que muda no código:**
1. `FichaDatabase.kt`: Trocar `@Fts4` por `@Fts5` na entidade `ManualChunkEntity`
2. `ManualChunkDao.kt`: Atualizar query para usar `ORDER BY bm25(manual_chunks)`
3. `MestreIAGraphEngine.kt`: Simplificar ou remover o loop de scoring manual (BM25 já fez o trabalho)
4. Incrementar versão do banco para forçar re-importação dos chunks

---

### Melhoria 1B — Filtro por `source_id` antes do FTS
**Resolve:** Falha Tipo 1 (pool genérico demais) + Velocidade
**Complexidade:** Baixa — `MestreIAPlanner.kt` + `ManualChunkDao.kt`
**Referência:** Análise do código — o campo `source_id` já existe em todos os chunks mas nunca é usado como filtro

**O problema hoje:**
Toda query busca em todos os livros ao mesmo tempo. Uma pergunta sobre "tiro com pistola" busca nos chunks de Magia, Artes Marciais, Módulo Básico, GunFu, Pyramid — retornando ~1.000+ candidatos irrelevantes.

**A solução:**
O `MestreIAPlanner` já detecta categorias (`temTiroDistância`, `temMagia`, `temCombate`, etc.). Usar esses detectores para filtrar `source_id` antes do FTS.

**Mapa de categoria → livros relevantes:**
```kotlin
val livrosPorCategoria = mapOf(
    "tiro"    to listOf("pt_modulo_basico", "pt_gun_fu", "pt_pyramid_26_underwater"),
    "magia"   to listOf("pt_modulo_basico", "pt_magia"),
    "combate" to listOf("pt_modulo_basico", "pt_artes_marciais", "pt_gun_fu"),
    "pericia" to listOf("pt_modulo_basico"),
    "raca"    to listOf("pt_modulo_basico")
    // expandir ao adicionar livros novos
)
```

**Query com filtro:**
```sql
SELECT *, bm25(manual_chunks) as score
FROM manual_chunks
WHERE search_text MATCH :query
  AND source_id IN (:livrosRelevantes)
ORDER BY score LIMIT 100
```

**Impacto esperado:**
- Pool de candidatos cai de ~1.000 para ~80-150 chunks (só livros relevantes)
- Velocidade: adicional de 3-5x sobre a Melhoria 1A
- Precisão: menos ruído de livros irrelevantes no contexto

---

### Melhoria 1C — Chunking menor ao processar livros novos
**Resolve:** Falha Tipo 2 (chunks grandes = ruído no contexto) + Escala
**Complexidade:** Baixa — script Python externo, não mexe no app
**Referência:** Análise do chunks.jsonl — média atual de 5.949 chars/chunk é 7-14x maior que o recomendado

**O problema hoje:**
Os chunks atuais têm média de **5.949 chars** — isso equivale a ~1.500 palavras, ou seja, seções inteiras do manual numa única entrada. Quando o scoring seleciona esse chunk, o modelo recebe uma página inteira de texto misturado, dificultando encontrar a regra específica.

**O padrão da indústria:** 400-800 chars por chunk (1-2 parágrafos). Isso permite:
- Scoring mais preciso (chunk tem menos "ruído" interno)
- Modelo consegue identificar a regra no chunk com mais facilidade
- Mais chunks distintos no top-30 (mais cobertura de páginas)

**A solução:**
Reprocessar o `chunks.jsonl` com um script Python ao adicionar livros novos, quebrando cada chunk grande em sub-chunks de ~600 chars com overlap de ~100 chars (para não perder contexto entre chunks adjacentes).

```python
# Script de reprocessamento (rodar no PC, não no app)
def rechunk(texto, max_chars=600, overlap=100):
    chunks = []
    inicio = 0
    while inicio < len(texto):
        fim = min(inicio + max_chars, len(texto))
        # Quebra no final de frase mais próximo
        if fim < len(texto):
            fim = texto.rfind('. ', inicio, fim) + 1 or fim
        chunks.append(texto[inicio:fim])
        inicio = fim - overlap
    return chunks
```

**Impacto esperado:**
- Chunks de 5.949 chars → ~8-10 sub-chunks de 600 chars cada
- Total atual: 1.196 chunks → ~10.000-12.000 chunks (ainda manejável com FTS5)
- Precisão: modelo recebe parágrafos específicos, não seções inteiras
- Contexto mais denso: 30 chunks de 600 chars = 18KB (vs. 30 chunks de 5.949 = 178KB)

---

## ONDA 2 — Médio prazo, muda a pipeline
### (Alto impacto em escala, 1-2 semanas)

---

### Melhoria 2A — SQLite-vec: busca semântica sem modelo no app
**Resolve:** Falha Tipo 1 definitivamente ("piscina" → "subaquático" sem dicionário manual)
**Complexidade:** Média — extensão SQLite (.so) + script Python offline + nova tabela
**Referência:** SQLite-vec GitHub — [github.com/asg017/sqlite-vec](https://github.com/asg017/sqlite-vec) — versão 0.1.2+ com suporte oficial Android, pesquisa maio/2026

**O problema hoje:**
FTS4/FTS5 é busca por keyword. "tiro na piscina" não encontra "divisor de alcance subaquático" porque as palavras são diferentes. O dicionário de sinônimos atual (24 entradas) tenta compensar, mas é impossível manter manualmente para 40 livros.

**A solução:**
SQLite-vec é uma extensão SQLite que adiciona busca vetorial KNN diretamente no banco. Você gera os embeddings **offline no PC** com Python, salva os vetores no `chunks.jsonl`, e o app importa junto com o texto. A busca semântica acontece no SQLite em <50ms, sem nenhum modelo rodando no app.

**Fluxo completo:**
```
[PC — offline ao processar livro novo]
  1. Script Python lê o PDF/TXT do livro
  2. Quebra em chunks de 600 chars
  3. Gera embedding para cada chunk via sentence-transformers (all-MiniLM-L6-v2)
  4. Salva no chunks.jsonl: { "text": "...", "embedding": [0.12, -0.34, ...384 dims] }

[App Android — na importação]
  5. FichaDatabase importa chunks.jsonl
  6. Cria tabela vec_chunks via SQLite-vec
  7. Insere vetores: INSERT INTO vec_chunks(chunk_id, embedding) VALUES (?, ?)

[Em tempo de busca — <50ms]
  Arquitetura híbrida:
  FTS5 + BM25 → top-200 candidatos por keyword (~10ms)
      ↓
  SQLite-vec → reranqueia os 200 por similaridade semântica (~40ms)
      ↓
  Top-30 chunks realmente relevantes (keyword + semântica combinados)
```

**Para gerar embedding da query no app (sem modelo pesado):**
Usar a API Gemini Flash Lite (já disponível via `BuildConfig.MESTRE_IA_LITE_1_URL`) para gerar o embedding da pergunta — 1 chamada HTTP de ~200ms, muito mais leve que rodar modelo local.

**Impacto esperado:**
- "tiro na piscina" → encontra "divisor de alcance subaquático" (busca semântica real)
- Elimina necessidade de manter dicionário de sinônimos manualmente
- Escala para 40 livros sem degradação de precisão
- Latência total: ~250ms (FTS5: 10ms + SQLite-vec: 40ms + embedding via API: 200ms)

---

### Melhoria 2B — Cache LRU de buscas
**Resolve:** Lentidão em perguntas repetidas ou variações próximas
**Complexidade:** Baixa-Média — `MestreIARepository.kt`, ~30 linhas de código
**Referência:** Padrão de engenharia — sem referência externa específica

**O problema hoje:**
Cada pergunta refaz toda a pipeline FTS + scoring do zero, mesmo que seja uma variação de pergunta anterior já feita na sessão.

**A solução:**
`LruCache<String, List<MestreIAChunk>>` no `MestreIARepository`. Chave = hash dos primeiros 60 chars normalizados da query. Cache de 50 entradas = ~50 últimas buscas distintas em memória.

```kotlin
// Em MestreIARepository.kt
private val cacheChunks = LruCache<String, List<MestreIAChunk>>(50)

fun buscarNoCodexDireto(query: String): List<MestreIAChunk> {
    val chave = query.lowercase().trim().take(60).hashCode().toString()
    cacheChunks.get(chave)?.let { return it }  // Cache hit: instantâneo
    val resultado = /* busca FTS normal */
    cacheChunks.put(chave, resultado)
    return resultado
}
```

**Impacto esperado:**
- Perguntas repetidas na mesma sessão: instantâneas (0ms vs. 500ms)
- Útil especialmente durante as iterações do loop (iteração 1 busca, iteração 2 reutiliza)
- Memória: ~50 buscas × 30 chunks × 1KB/chunk ≈ 1,5MB RAM

---

### Melhoria 2C — Compressão seletiva de contexto (Pocket RAG)
**Resolve:** Falha Tipo 2 (35KB de ruído → ~20KB denso) + Redução de tokens e latência
**Complexidade:** Média — `MestreIAGraphEngine.kt`, método `formatarParaIA()`
**Referência:** Paper **Pocket RAG** (arXiv 2602.13229, janeiro 2026) — pesquisa maio/2026
Resultado comprovado: **14.2s → 3.7s por resposta**, redução de 31-42% de tokens sem perda de acurácia

**O problema hoje:**
Os 30 chunks selecionados são enviados integralmente ao modelo — ~35KB de texto. Boa parte é contexto de parágrafo irrelevante para a pergunta específica. O modelo recebe ruído junto com a informação relevante.

**A solução:**
Antes de montar o contexto final, extrair apenas as **sentenças relevantes** de cada chunk, não o chunk inteiro.

```kotlin
// Em MestreIAGraphEngine.kt — método formatarParaIA()
fun comprimirChunk(texto: String, termos: List<String>, maxSentencas: Int = 3): String {
    val sentencas = texto.split(Regex("[.!?]\\s+"))
    val relevantes = sentencas.filter { sentenca ->
        termos.any { termo -> sentenca.contains(termo, ignoreCase = true) }
    }
    // Mantém até 3 sentenças relevantes + contexto
    return relevantes.take(maxSentencas).joinToString(". ") + "."
}
```

**Resultado:**
- Chunk de 5.949 chars → ~300-600 chars (só as sentenças que contêm a regra)
- Contexto total: de 35KB → ~12-18KB
- Menos tokens = resposta mais rápida e mais barata na API
- Modelo foca no que importa, não no ruído

---

### Melhoria 2D — Índice de tópicos pré-computado por livro
**Resolve:** Falha Tipo 1 para casos específicos (páginas críticas sempre entram)
**Complexidade:** Média — script Python offline + novo asset `topic_index.json` + `MestreIAPlanner.kt`
**Referência:** Extensão da análise do código — o sistema de "fonte garantida" já existe hardcoded, este plano o generaliza

**O problema hoje:**
O sistema já tem "fontes garantidas" (ex: `pt_pyramid_26_underwater` entra quando detecta tiro aquático). Mas é hardcoded em Kotlin — impossível manter para 40 livros.

**A solução:**
Um arquivo `topic_index.json` gerado automaticamente pelo script Python ao processar cada livro. Mapeia tópicos → páginas exatas onde a regra está:

```json
{
  "tiro_subaquatico": [
    { "source": "pt_pyramid_26_underwater", "pages": [5, 7, 8] },
    { "source": "pt_gun_fu", "pages": [14, 27] }
  ],
  "queda_dano": [
    { "source": "pt_modulo_basico", "pages": [398, 399, 432] }
  ],
  "dano_fogo": [
    { "source": "pt_modulo_basico", "pages": [433, 434] }
  ]
}
```

O `MestreIAPlanner` consulta esse índice e garante que as páginas críticas **sempre entram no contexto**, substituindo o sistema hardcoded atual.

---

## ONDA 3 — Escala para 40 livros
### (Necessário quando ultrapassar 10 livros, semanas-meses)

---

### Melhoria 3A — Pipeline offline de processamento de livros
**Resolve:** Escalabilidade — como incorporar 40 livros sem esforço manual
**Complexidade:** Alta — script Python completo externo
**Referência:** Arquitetura derivada das boas práticas de RAG + análise do chunks.jsonl atual

**O problema hoje:**
Adicionar um livro novo = editar o `chunks.jsonl` manualmente. Com 40 livros, inviável.

**A solução — script `processar_livro.py`:**
```
[Input]  PDF do livro GURPS
    ↓
1. Extração de texto (PyMuPDF / pdfplumber)
2. Detecção de estrutura (capítulos, seções, tabelas)
3. Chunking inteligente: 400-800 chars, quebra em fronteira de parágrafo
4. Geração de embeddings offline (sentence-transformers all-MiniLM-L6-v2)
5. Detecção de tópicos → atualiza topic_index.json automaticamente
6. Geração de source_id padronizado (ex: pt_ultra_tech, pt_social_engineering)
7. Append no chunks.jsonl (não sobrescreve, só adiciona)
    ↓
[Output] chunks.jsonl atualizado + topic_index.json atualizado
         Pronto para incluir no próximo build do app
```

**Com esse script:** adicionar um livro novo = 1 comando Python, 5-10 minutos no PC.

---

### Melhoria 3B — Tabelas FTS5 separadas por categoria de livro
**Resolve:** Velocidade com 15.000+ chunks
**Complexidade:** Alta — refatoração de `FichaDatabase.kt` e `ManualChunkDao.kt`
**Referência:** Arquitetura de particionamento de dados

**A solução:**
Uma tabela FTS5 por categoria, com ~2.000-3.000 chunks máximo cada:

```
manual_chunks_core      → Módulo Básico
manual_chunks_combat    → GunFu + Artes Marciais + Tactical Shooting
manual_chunks_magic     → Magia + Thaumatology
manual_chunks_sci       → Ultra-Tech + Bio-Tech
manual_chunks_social    → Social Engineering + Mass Combat
```

O `MestreIAGraphEngine` busca só nas tabelas relevantes detectadas pelo `MestreIAPlanner`. Cada tabela opera em <5ms.

---

## Ordem de Implementação Recomendada

| Prioridade | Melhoria | Falha Resolvida | Complexidade | Estimativa |
|---|---|---|---|---|
| **1** | 1A — FTS4 → FTS5 + BM25 | Velocidade + Tipo 1 | Baixa | 1 dia |
| **2** | 1B — Filtro por source_id | Velocidade + Tipo 1 | Baixa | 0,5 dia |
| **3** | 2C — Compressão de contexto | Tipo 2 + Tokens | Média | 2 dias |
| **4** | 1C — Chunking menor (script Python) | Tipo 1 + Escala | Baixa | 1 dia |
| **5** | 2B — Cache LRU | Velocidade sessão | Baixa | 0,5 dia |
| **6** | 2D — Índice de tópicos | Tipo 1 (casos críticos) | Média | 3 dias |
| **7** | 2A — SQLite-vec semântico | Tipo 1 definitivo | Média | 1 semana |
| **8** | 3A — Pipeline offline de livros | Escalabilidade | Alta | 2 semanas |
| **9** | 3B — Tabelas por categoria | Escala 15k+ chunks | Alta | 2 semanas |

---

## Ganhos Esperados (Acumulados por Onda)

### Após Onda 1 (dias 1-3):
- Velocidade de busca: **5-10x mais rápida**
- Latência por resposta: de ~6-8s → ~2-3s
- Precisão: melhor ranking (BM25 > scoring manual em Kotlin)
- Pronto para escalar até ~5.000 chunks

### Após Onda 2 (semanas 1-2):
- Tokens enviados ao modelo: **-35% a -42%** (compressão Pocket RAG)
- Busca semântica real: "piscina" → "subaquático" sem dicionário manual
- Latência por resposta: de ~3s → ~1,5s
- Custo de API: redução proporcional à redução de tokens
- Pronto para ~10.000 chunks (8-10 livros)

### Após Onda 3 (meses):
- Adicionar qualquer livro novo: 1 comando Python, sem tocar no app
- Pronto para **40+ livros / 20.000+ chunks**
- Busca: <500ms mesmo com corpus completo
- Precisão: híbrida keyword + semântica

---

## Referências da Pesquisa (maio/2026)

| Tecnologia | Fonte | Relevância |
|---|---|---|
| SQLite FTS5 + BM25 | [sqlite.org/fts5.html](https://www.sqlite.org/fts5.html#bm25_ranking_function) | Base da Melhoria 1A |
| SQLite-vec (busca vetorial Android) | [github.com/asg017/sqlite-vec](https://github.com/asg017/sqlite-vec) | Base da Melhoria 2A |
| Pocket RAG (arXiv 2602.13229) | [arxiv.org/abs/2602.13229](https://arxiv.org/abs/2602.13229) | Base da Melhoria 2C — 14s→3,7s comprovado |
| MobileRAG (arXiv 2507.01079) | [arxiv.org/abs/2507.01079](https://arxiv.org/abs/2507.01079) | EcoVector + SCR: -40% bateria, +41% velocidade |
| EmbeddingGemma (Google, 2025) | [ai.google.dev/gemma/docs/embeddinggemma](https://ai.google.dev/gemma/docs/embeddinggemma) | Alternativa para embeddings on-device |
| all-MiniLM-L6-V2 (ONNX) | [ProAndroidDev](https://proandroiddev.com/from-python-to-android-hf-sentence-transformers-embeddings-1ecea0ce94d8) | Embeddings offline para Melhoria 2A |
| ObjectBox Vector DB Android | [objectbox.io/vector-database](https://objectbox.io/the-on-device-vector-database-for-android-and-java/) | Alternativa ao SQLite-vec |
| Querylight (BM25 Kotlin MP) | [github.com/jillesvangurp/querylight](https://github.com/jillesvangurp/querylight) | Alternativa Kotlin pura ao FTS5 |
| LiteRT-LM (Google AI Edge) | [ai.google.dev/edge/litert-lm](https://ai.google.dev/edge/litert-lm/overview) | RAG on-device com Gemma (high-end devices) |
| Gemini Nano AICore Android 14+ | [developer.android.com/ai/gemini-nano](https://developer.android.com/ai/gemini-nano) | Embeddings system-level (Early Access) |

---

## O que NÃO implementar (e por quê)

| Tecnologia | Motivo para não usar agora |
|---|---|
| LiteRT-LM + Gemma-3n on-device | Requer Pixel 9 / Galaxy S25+. Qualidade muito inferior ao DeepSeek para raciocínio complexo de GURPS |
| Gemini Nano AICore | Early Access, dispositivos limitados, qualidade abaixo do DeepSeek |
| EmbeddingGemma (308M) rodando no app | Modelo pesado, 1-2s de latência por embedding — gerar offline é melhor |
| Chroma / LanceDB mobile | Sem bindings Android nativos oficiais hoje |

---

*Plano elaborado com base em leitura direta do código do projeto + pesquisa web realizada em 23/05/2026.*
*Próxima revisão recomendada: ao atingir 10 livros importados ou 5.000 chunks.*