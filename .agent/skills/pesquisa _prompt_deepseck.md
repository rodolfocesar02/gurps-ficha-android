O que as pesquisas dizem — aplicado ao Forjador
1. Estrutura do system prompt para DeepSeek V4 Flash
Princípio central descoberto: DeepSeek "liga a diretiva ao conteúdo mais próximo". Isso significa que a instrução deve vir depois do contexto que ela referencia — não antes. Hoje o nosso prompt faz o oposto: as Regras de Ouro citam as ferramentas antes de o modelo ter lido a descrição delas.

Ordem recomendada pela pesquisa:


1. Papel / identidade (quem é o modelo)
2. Contexto estático (o que ele tem disponível — ferramentas, catálogo)
3. Instrução / como usar o contexto
4. Restrições e regras
5. Formato de saída
Isso confirma exatamente a reordenação que mapeamos: Ferramentas antes das Regras de Ouro.

2. Tamanho ideal do system prompt
A pesquisa recomenda 200–800 tokens para o núcleo do prompt, com contexto variável injetado dinamicamente. O nosso PROMPT do Forjador tem ~2500 tokens só de instrução fixa, antes do catálogo. Isso é alto.

O que tirar sem perder função:

Seção "O QUE NÃO EXISTE EM GURPS" (~200 tokens) — nunca é usada no fluxo agêntico. Remove ou vai para o fim.
Exemplos de arquétipos (Ladrão/Guerreiro/Mago) dentro dessa seção — são contexto de treinamento, não instrução operacional.
3. DeepSeek responde melhor a framing positivo
A pesquisa é direta: "explicit negative constraints ('don't hallucinate') had no measurable effect". O modelo responde melhor ao que deve fazer, não ao que não deve.

No nosso prompt temos vários blocos negativos:

"NUNCA invente IDs"
"NÃO use sufixos descritivos"
"NUNCA adicione o mesmo item duas vezes"
O recomendado é converter para positivo:

"Use APENAS IDs do catálogo abaixo"
"Use o nome exato do catálogo, sem sufixos"
"Cada item entra UMA única vez — verifique o read-back antes de re-adicionar"
4. Para tool calling agêntico — padrão PLAN→EXECUTE→VERIFY→REPORT
A pesquisa descreve o padrão ideal para agentes com tools:

PLAN — lista subtarefas em ordem de dependência
EXECUTE — executa verificando cada etapa
VERIFY — checa se o objetivo foi atingido
REPORT — resume o que foi feito
Nosso Forjador já faz isso implicitamente (os 9 pilares + read-back + fechamento), mas o prompt não nomeia o padrão com clareza — o modelo improvisa a ordem. Tornar isso explícito reduz desvios.

5. Cache — dado importante de custo
O V4 Flash cacheia o system prompt a $0.028/M tokens (90% mais barato). A condição é que o prompt seja byte-idêntico entre chamadas. Hoje gerarPromptComCatalogo injeta o catálogo dinâmico no meio da string — qualquer mudança no catálogo quebra o prefixo de cache e paga preço cheio.

Fix simples: mover o catálogo dinâmico para o fim do prompt. O prefixo fixo (PROMPT + budget) fica cacheável; só o sufixo (catálogo) varia.

Resumo — o que muda nos nossos prompts
Problema	O que a pesquisa confirma	Ação
Ferramentas citadas antes de serem definidas	Instrução deve vir depois do contexto	Reordenar: ferramentas sobem
Protocolo GPS iterativo desatualizado	Padrão PLAN→EXECUTE claro reduz loops	Reescrever seção de magias
Framing negativo ("NUNCA", "NÃO")	Positivo tem impacto mensurável, negativo não	Converter restrições para positivo
Seção "O que não existe em GURPS"	Prompt enxuto 200-800 tokens recomendado	Remover ou mover para o fim
Catálogo dinâmico no meio do prompt	Prefixo fixo = cache; sufixo variável = sem cache	Mover catálogo para o fim
Quer que eu comece pelas correções nos prompts já? Posso fazer tudo num lote só: reordenação + protocolo de magias + conversão de negativos + catálogo no fim.

Sources:

DeepSeek V4 Prompting Techniques — Lightrains
DeepSeek V4 Prompt Engineering: What Actually Works in Production — Lightrains
DeepSeek V4 AI Agents: Function Calling, MCP & Agentic Guide — Lushbinary
System Prompt Design Best Practices — LLM Guide
Prompt Engineering for AI Agents: 2026 Guide — Inflectra
Advanced Prompt Engineering 2026 — Lushbinary