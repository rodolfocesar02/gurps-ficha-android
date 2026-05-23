# PLANO: Melhorias no Sistema RAG do Mestre IA
**Data:** 23 de Maio de 2026  
**Última revisão:** 23 de Maio de 2026  
**Autor:** Claude Sonnet 4.6  
**Objetivo:** RAG preciso, rápido e escalável para 40+ livros de GURPS.

---

## STATUS GERAL DOS LOTES

| Lote | Melhoria | Status | Commit |
|---|---|---|---|
| 251 | Plano RAG + arquitetura inicial | ✅ FEITO | 3c3d43b |
| 252 | BM25-Kotlin + filtro por fonte + pool 500 | ✅ FEITO | db839d5 |
| 253 | LRU Cache de buscas FTS | ✅ FEITO | 3372e58 |
| 254 | Pocket RAG: compressão seletiva de contexto | ✅ FEITO | 2a551bd |
| 255 | Dicionário 90+ entradas + livrosPorCategoria | ✅ FEITO | 8e92543 |
| 256 | topic_index.json — 11 tópicos iniciais | ✅ FEITO | 8bef6dc |
| 257 | Prompt: raciocínio com lacunas (Protocolo de Lacuna) | ✅ FEITO | a3c5415 |
| 258 | Query rewriting pela IA antes do FTS | ✅ FEITO | a3c5415 |
| 259 | SQLite-vec + busca semântica híbrida | ✅ FEITO | a291d6b |
| 260 | Scripts offline: gerar_embeddings.py | ✅ FEITO | a291d6b |
| 261 | Pipeline offline: processar_livro.py | ✅ FEITO | 6e6a7e0 |
| 262 | Tabelas FTS por categoria (preparatório) | ✅ PREPARADO | — |
| 263 | Velocidade + Feedback Visual + Thinking adaptativo | ✅ FEITO | 01c8ceb |
| 264 | Status no balão + Thinking sem iter final + topic_index expandido | ✅ FEITO | 4d91d4b |
| **265** | **topic_index gerado do índice do livro** | **🔲 PENDENTE** | — |
| **266** | **Rechunking: chunks menores via script Python** | **🔲 PENDENTE** | — |
| **267** | **Tabelas FTS por categoria (implementação real)** | **🔲 PENDENTE** | — |

---

## DIAGNÓSTICO ATUAL (pós-Lote 264)

### O que funciona bem
- BM25 + semântica acha a maioria das regras
- topic_index garante as 15 páginas mais críticas independente do BM25
- Thinking adaptativo: simples (~15s) vs complexas (~30-35s)
- Feedback visual granular (sem silêncio de 54s)
- Jailbreak e social engineering resistidos corretamente
- Parallel tool calls funcionando (3 async coroutines)
- Embedding cache (ConcurrentHashMap, thread-safe) evita chamadas repetidas

### Problema raiz ainda em aberto: BM25 Displacement
O BM25 com pool de 200 candidatos sofre de **displacement**: chunks de Artes Marciais e Gun Fu têm altíssima densidade de termos de combate ("ajoelhado", "penalidade", "tiro") e dominam o ranking mesmo quando não são a regra mais relevante.

**Exemplo confirmado:** p.549 (tabela de modificadores de tiro) ficava em rank #31 — 1 posição fora do top-30 — deslocada por 29+ chunks de Artes Marciais.

**Solução atual:** topic_index garante as páginas críticas conhecidas. Não escala para perguntas novas.

**Solução definitiva:** Lote 266 (rechunking) — chunks menores (~150 tokens) têm maior densidade por tema e sofrem menos displacement.

---

## Os 3 Tipos de Falha (status atual)

### Falha Tipo 1 — RAG não acha o chunk certo
- **Status:** PARCIALMENTE RESOLVIDO
- BM25 + semântica resolvem ~75% dos casos
- topic_index resolve os casos críticos conhecidos (15 tópicos)
- Displacement ainda ocorre para páginas fora do topic_index
- **Solução pendente:** Lote 265 (topic_index do índice do livro) + Lote 266 (rechunking)

### Falha Tipo 2 — Chunk está no contexto mas IA ignora
- **Status:** PARCIALMENTE RESOLVIDO
- Pocket RAG com ★★★/★★/★ melhora priorização
- **Solução pendente:** nenhuma nova — Pocket RAG é o estado da arte para este app

### Falha Tipo 3 — IA tem a regra mas raciocina errado
- **Status:** RESOLVIDO (Lote 257)
- Protocolo de Lacuna obrigatório no prompt
- Protocolo de Cálculo obrigatório quando há fórmula

---

## ONDA 3 — Pendentes

### Lote 265 — topic_index gerado do Índice do Livro
**Status: 🔲 PENDENTE**  
**Resolve: Falha Tipo 1 — displacement de páginas fora dos 15 tópicos hardcoded**

**O problema:**
O topic_index atual tem 15 tópicos criados manualmente. Existem centenas de regras no Módulo Básico que podem sofrer displacement no BM25. Não é viável criar entradas manualmente para cada uma.

**A solução:**
O `indice.md` e o `glossario.md` já existem no repositório — são o índice remissivo e glossário do próprio livro GURPS. Os autores do livro mapearam cada termo → páginas exatas.

**Script Python a criar:** `scripts/gerar_topic_index.py`
```
[Input]
  indice.md    → "Ataques à distância, 548" → { termo, [páginas] }
  glossario.md → "posição: perfil corporal... Pág. 364" → { termo, definição, página }

[Processamento]
  Para cada entrada do índice:
    - keywords = [termo] + variações (plural, sem acento, sinônimos do glossário)
    - require_all = [palavra_principal]
    - fallback_any = pares de palavras do termo
    - pages = [{ source_id: "pt_modulo_basico", pages: [N, N+1] }]

[Output]
  topic_index.json com 200-400 entradas cobrindo todo o MB
```

**Impacto esperado:** cobertura de ~80-90% das regras do MB contra displacement. Custo: 0 (sem API, sem modelo — só parsing de texto).

**Limitação:** cobre só o Módulo Básico. Pyramid e livros extras ainda dependem do topic_index manual.

---

### Lote 266 — Rechunking: Chunks Menores (~150 tokens)
**Status: 🔲 PENDENTE**  
**Resolve: Falha Tipo 1 definitivamente — a raiz do displacement**

**O problema raiz:**
Chunks atuais têm média de ~834 palavras (confirmado via simulação BM25). Um chunk de página inteira contém 5-10 regras diferentes. Quando o BM25 calcula o score, todos os termos do chunk contribuem — não só os relevantes para a pergunta. Isso causa displacement.

**A solução:**
Rechunkar por seção/parágrafo (~150 tokens = ~100 palavras). 1 chunk = 1 regra/tabela específica. Score BM25 fica concentrado no tema certo.

**Script:** `scripts/rechunkar.py`
```python
def rechunkar_por_secao(texto, max_tokens=200, overlap_tokens=20):
    # Quebra em fronteiras de header (##, ###) ou parágrafo duplo
    # Preserva tabelas inteiras (não quebra no meio de | col | col |)
    # Mantém overlap para preservar contexto
```

**Resultado estimado:**
- 1.197 chunks atuais → ~6.000-8.000 chunks menores
- p.549 passa a ter 3-4 chunks específicos: "tabela modificadores tiro", "alvo ajoelhado -2", "cobertura -2"
- Cada chunk tem densidade máxima no seu tema → displacement praticamente eliminado

**Custo:** reprocessar embeddings (1 chamada API por chunk novo). ~6.000 chamadas ao Gemini embedding → ~$0.90 total. Não muda nada no app.

---

### Lote 267 — Tabelas FTS por Categoria (implementação real)
**Status: 🔲 PENDENTE — só quando atingir 5.000+ chunks**

`livrosPorCategoria` e `buscarRegrasPorFonte` já existem. Implementar tabelas separadas quando o banco ultrapassar 5.000 chunks:

```
manual_chunks_core      → Módulo Básico
manual_chunks_combat    → GunFu + Artes Marciais
manual_chunks_magic     → Magia
manual_chunks_sci       → Ultra-Tech + Bio-Tech
manual_chunks_social    → Social Engineering
```

---

## Ordem de Implementação Recomendada

| Prioridade | Lote | Melhoria | Falha Resolvida | Complexidade | Custo |
|---|---|---|---|---|---|
| **1** | 265 | topic_index do índice do livro | Tipo 1 — displacement conhecido | Baixa — script Python | $0 |
| **2** | 266 | Rechunking ~150 tokens | Tipo 1 — raiz do displacement | Média — script + re-embed | ~$0.90 |
| **3** | 267 | Tabelas FTS por categoria | Escala 5k+ chunks | Alta | $0 |

---

## O que NÃO fazer

| Abordagem | Motivo |
|---|---|
| Mais entradas manuais no topic_index | Não escala. Lote 265 automatiza isso |
| Mais entradas no dicionário de sinônimos | Lote 266 (rechunking) elimina a necessidade |
| Modelo de embedding rodando no device | Gerar offline no PC é melhor — sem latência, sem bateria |
| CAG (contexto completo do livro) | 300k tokens por chamada — custo proibitivo |
| GraphRAG / Knowledge Graph | Semanas de trabalho, ganho marginal sobre o atual |
| Fine-tuning | Ensina estilo, não fatos — máximo 75% fidelidade |
| ColBERT / ColPali | Sem modelo em português, +200MB APK |

---

## Arquitetura Atual (pós-Lote 264)

```
[Pergunta do usuário]
    ↓
MestreIAPlanner → detecta complexidade, categorias, termos-chave
    ↓
MestreIAGraphEngine:
  1. FTS4 → pool 200 candidatos por keyword
  2. BM25-Kotlin → reranqueia os 200
  3. MestreIASemanticEngine → reranqueia top-50 por cosseno (embedding Gemini)
  4. MestreIATopicIndex → injeta páginas garantidas (15 tópicos, ~30 páginas)
  5. Pocket RAG → comprime chunks ★ e ★★, mantém ★★★ completos
  6. Top-30 chunks formatados → contexto da IA
    ↓
MestreIAUseCase:
  - isComplexo? → Thinking ON (máx 3 iters) : Thinking OFF (máx 2 iters)
  - isUltimaIteracao? → Thinking OFF (economiza ~15s)
  - IA faz tool calls → GraphEngine busca → contexto acumulado
    ↓
Resposta final com Protocolo de Lacuna + Protocolo de Cálculo obrigatórios
```

---

## Referências

| Tecnologia | Status | Uso |
|---|---|---|
| SQLite FTS4 + BM25-Kotlin | ✅ Ativo | Busca keyword, pool 200 |
| gemini-embedding-001 (3072 dims) | ✅ Ativo | Embedding da query + reranking semântico |
| Pocket RAG (compressão seletiva) | ✅ Ativo | Reduz contexto 35KB → 12-18KB |
| topic_index.json (15 tópicos) | ✅ Ativo | Garante páginas críticas |
| Protocolo de Lacuna | ✅ Ativo | Previne alucinação |
| Query Rewriting (Gemini Flash Lite) | ✅ Ativo | Ativa quando FTS retorna < 5 chunks |
| SQLite FTS5 + BM25 nativo | 🔲 Futuro | Quando Room 2.7 stable |

---

*Última revisão: 23/05/2026. Próxima revisão: ao implementar Lote 265 ou ao atingir 3.000+ chunks.*
