# 🔨 PLANO DE MELHORIAS — FORJADOR (criação e edição de fichas)

> **Escrito em 2026-05-30** após leitura linha-a-linha dos arquivos do Forjador + modelo da
> ficha (`Personagem.kt`) + regras (`CharacterRules`, `CombatRules`, `MagicEngine`, `SkillEngine`)
> + catálogos do `DataRepository`.
>
> **Princípio herdado do Auditor (Lotes 318/325):** dar AUTONOMIA ao modelo via FERRAMENTAS que
> ele dirige em loop, não via prompt hardcoded. **Zero exemplos que criem viés direcionado.**
> Este é um PLANO — nada aqui foi implementado ainda. Discutir antes de codar.

---

## 0. A ANALOGIA (como o Claude trabalha → como o Forjador deveria)

Quando eu (Claude) edito código neste projeto, eu NÃO escrevo um arquivo inteiro de cabeça e
torço pra dar certo. Eu: **(1)** leio o estado real (`Read`), **(2)** consulto a referência
(`Grep`/catálogo), **(3)** faço a mudança cirúrgica (`Edit`), **(4)** confiro o resultado
(`Read`/build), **(5)** se quebrou, conserto. Loop fechado, baseado em FATO, não em suposição.

O Auditor ganhou esse modelo nos Lotes 325-328 (localizar→ler→julgar→responder). **O Forjador
está a meio caminho:** já tem `forjador_ler_ficha`, `forjador_buscar_catalogo`, `forjador_editar_ficha`
e read-back automático (ótimo!). Mas tem **três cegueiras** que o impedem de ser autônomo de verdade:

1. **Não enxerga o CUSTO REAL** que a própria ficha calcula (usa fórmulas paralelas e erradas).
2. **Não enxerga o RESULTADO CALCULADO** das suas edições (defesas, dano por ST, NH de magia, carga).
3. **Não tem ferramenta para EQUIPAMENTO de catálogo** (inventa armas/armaduras às cegas no JSON).

---

## 1. O QUE JÁ EXISTE E FUNCIONA BEM (não mexer)

- **Loop agêntico** (`MestreIAGeneratorUseCase`): iteração até estabilizar (estagnação/teto 30),
  read-back automático pós-edição, status ao vivo, âncora de "pedido real" anti-confusão.
- **`forjador_editar_ficha`**: aplica direto (sem JSON), idempotente por chave, remove última
  ocorrência (dedup seguro), barra magia sem pré-requisito (igual à tela), `forcar=true` narrativo.
- **`forjador_gps_magia`**: veredito determinístico + trilha ótima (Pathfinder). Excelente — o
  modelo NÃO calcula pré-requisito de cabeça, obedece o app. É o padrão-ouro a replicar.
- **`RegrasEspeciaisSchema`**: injeta o schema de metadados de traços de custo variável (Aliado,
  Garras, Inimigo...) quando o modelo busca — resolve a cegueira de custo desses traços.
- **`forjador_buscar_racas` / `forjador_aplicar_modelo_racial`**: aplica pacote racial inteiro.
- **Validação na integração JSON**: `gerarRelatorio` (OK/FUZZY/FALLBACK), dedup defensivo,
  `substituir` para edição de seção inteira.

---

## STATUS DE EXECUÇÃO — ✅ PLANO COMPLETO (2026-05-30)

Todos os lotes implementados e compilando (build verde a cada um). ⏳ pendente teste do usuário no app.

- ✅ **Lote A+F.** `forjador_ler_ficha(pontos)` lê `Personagem.pontosGastos`/`pontosRestantes` + quebra por categoria + aviso de limite de desvantagens. `validarBudget` usa custo real. `calcularPontosGastos` (Executor) REMOVIDO. BUG-1/BUG-2 corrigidos. Fonte de verdade única.
- ✅ **Lote C (read-back numérico).** Nova seção `forjador_ler_ficha(secao=derivados)`: defesas (Esquiva/Apara/Bloqueio), dano GdP/GeB, PV/PF/Vontade/Percepção, Velocidade/Deslocamento, carga calculada, Aptidão Mágica. Read-back automático pós-edição passou a incluir `derivados` quando atributos/equip/perícias/vantagens mudam.
- ✅ **Lote B (equipamento de catálogo).** `forjador_buscar_catalogo` aceita `arma`/`armadura`/`escudo` com stats REAIS (dano, RD, BD, ST mín, peso, custo, grupo). `forjador_editar_ficha(adicionar, equipamentos, <id>)` resolve do catálogo via `adicionarEquipamentoArma/Escudo/Armadura` (dano por ST automático, grupo p/ Mestre de Armas). Fallback: item genérico se não estiver no catálogo.
- ✅ **Lote D (GPS de pré-req de técnica).** `forjador_buscar_catalogo(tipo=tecnica)` agora dá VEREDITO determinístico (✓ PODE ADICIONAR com base / ⚠ FALTA base) via `tecnicaAtendePreRequisito` — o app é o juiz, análogo ao `forjador_gps_magia`.
- ✅ **Lote E (edição de secundários).** `forjador_editar_ficha(alterar, atributos, <pv|vontade|percepcao|deslocamento|velocidade>)` — mod incremental, custo entra automático em `pontosSecundarios`.

---

## 2. 🐞 BUGS REAIS ENCONTRADOS NA LEITURA (corrigir antes de adicionar features)

Estes não são "melhorias", são **erros que fazem o Forjador mentir sobre pontos**. A ficha
(`Personagem.kt`) JÁ tem o cálculo correto e completo — o Forjador o ignora e recalcula errado.

### BUG-1 — `ForjadorToolExecutor.calcularPontosGastos()` (linha ~531) está INCOMPLETO
Soma só atributos+vantagens+desvantagens+perícias+magias. **Ignora:** técnicas, equipamentos
(não custam pts, ok), atributos secundários (modPV/modPF/Vontade/Percepção/Velocidade), qualidades,
peculiaridades, e o **custo do modelo racial**. Também usa `st/dx/iq/ht` (já com mod racial) e
subtrai 10 — dupla contagem do racial.
→ A tool `forjador_ler_ficha(secao=pontos)` reporta um número **errado** para o modelo decidir.

### BUG-2 — `MestreIAGeneratorUseCase.validarBudget()` (linha ~389) usa estimativa grosseira
`custoPericias = pericias.sumOf { it.nivel * 2 }` — **"nivel*2" não é o custo de perícia.** O custo
real depende da dificuldade e do atributo-base (`CharacterRules.calcularPontosParaNivel`). Uma
perícia Difícil em NH alto pode custar 8/12/16+ pts; o budget reportado pode estar muito abaixo
do real. Técnicas e secundários também ficam de fora.

### A CORREÇÃO DOS DOIS É A MESMA: usar a fonte de verdade
`Personagem.pontosGastos` e `Personagem.pontosRestantes` JÁ calculam TUDO certo (atributos +
secundários + vantagens + desvantagens + qualidades + peculiaridades + perícias + técnicas +
magias + racial). **Basta o Forjador LER esses campos** em vez de recalcular. Ver Proposta A.

---

## 3. AS LACUNAS DE AUTONOMIA (com a analogia das minhas ferramentas)

| Capacidade que EU tenho | Equivalente no Forjador hoje | Lacuna |
|---|---|---|
| Ler o estado real antes de agir | `forjador_ler_ficha` ✅ | Não lê **pontos reais**, nem **stats calculados** (defesa/dano/carga) |
| Consultar referência (grep) | `forjador_buscar_catalogo` (vant/desv/perícia/magia/técnica) | **Falta EQUIPAMENTO** (armas/armaduras/escudos existem no catálogo) |
| Edição cirúrgica | `forjador_editar_ficha` ✅ | Não cobre: atributos secundários, modificadores de vantagem, equipamento do catálogo |
| Conferir resultado | read-back automático ✅ | Read-back mostra texto, mas não os **números calculados** (ex: "a Esquiva ficou 9") |
| Seguir cadeia de dependência | `forjador_gps_magia` ✅ (padrão-ouro) | Não há equivalente para **pré-requisito de PERÍCIA/TÉCNICA/VANTAGEM** |

---

## 4. PROPOSTAS (ordenadas por valor/custo — discutir quais entram)

### ✅ PROPOSTA A — Forjador enxerga o CUSTO REAL (corrige BUG-1 e BUG-2) **[prioridade máxima]**
Trocar as duas fórmulas paralelas por leitura de `Personagem`:
- `forjador_ler_ficha(secao=pontos)` retorna `p.pontosGastos`, `p.pontosTotaisDisponiveis`,
  `p.pontosRestantes`, e a quebra por categoria (`pontosAtributos`, `pontosVantagens`, ...,
  `modeloRacial.custoTotal`). Tudo já existe como propriedade calculada.
- Adicionar aviso de **limite de desvantagens** (`p.desvantagensExcedemLimite`, `p.limiteDesvantagens`).
- `validarBudget` passa a usar `viewModel.personagem.pontosGastos` direto (após integrar o JSON),
  ou ser removido em favor do número real.
- **Custo:** baixo (reusa o que existe). **Risco:** baixo. **Ganho:** o Forjador para de mentir pontos.

### ✅ PROPOSTA B — Ferramenta de EQUIPAMENTO de catálogo **[alto valor]**
Hoje o Forjador inventa `{"nome":"Espada Longa","dano":"1d+1 corte",...}` no JSON — dano,
peso, ST mínimo e custo **chutados**. Mas `armasCatalogo`/`armadurasCatalogo`/`escudosCatalogo`
têm os stats REAIS. Propor:
- Estender `forjador_buscar_catalogo` com tipos novos: `arma`, `armadura`, `escudo` (ou tool
  dedicada `forjador_buscar_equipamento`). Retorna nome, dano real, ST mín, alcance, peso, custo,
  grupo (p/ Mestre de Armas), periciaId.
- `forjador_editar_ficha(adicionar, equipamentos, <nome do catálogo>)` resolve os stats do
  catálogo automaticamente (em vez de o modelo preencher campo a campo).
- **Por quê importa:** dano por ST é calculado pela ficha (`Equipamento.danoCalculadoComSt` →
  `CharacterRules.resolverDanoPorSt`). Se a arma vier do catálogo com `armaDanoRaw="GdP+2 corte"`,
  a ficha calcula o dano final sozinha. Hoje o modelo escreve "1d+1" fixo e quebra esse automático.
- **Custo:** médio. **Risco:** baixo (aditivo). **Ganho:** equipamento correto + integra com automações.

### ✅ PROPOSTA C — Forjador enxerga os STATS CALCULADOS (read-back numérico) **[alto valor]**
Dar ao Forjador uma leitura do que a ficha CALCULOU, não só do que foi inserido. Nova seção em
`forjador_ler_ficha(secao=combate)` ou `secao=derivados`:
- Esquiva/Apara/Bloqueio (via `DefesasAtivas` — já calcula com perícia, escudo, vantagens).
- Dano GdP/GeB do ST atual, PV, PF, Vontade, Percepção, Velocidade, Deslocamento.
- Nível de carga e deslocamento com carga (`nivelCarga`, `deslocamentoAtual`).
- NH calculado de cada perícia/magia (já vem em `lerFicha` parcial — expandir).
- **Por quê:** o Forjador poderia validar "criei um guerreiro, a Esquiva ficou 8, baixa demais"
  e ajustar — fechando o loop como eu faço com build. Hoje ele cria no escuro.
- **Custo:** baixo-médio. **Risco:** baixo. **Ganho:** auto-correção baseada em resultado real.

### 🟡 PROPOSTA D — GPS de pré-requisito para PERÍCIA / TÉCNICA / VANTAGEM
O `forjador_gps_magia` é o padrão-ouro: o app calcula a cadeia, o modelo obedece. Técnicas já
têm pré-requisito (`tecnicaAtendePreRequisito` no ViewModel) e o executor já barra técnica sem
perícia-base. Vantagens/perícias podem ter pré-requisitos também. Propor um veredito análogo
("PODE ADICIONAR / FALTA X") para esses tipos, para o modelo não tatear.
- **Custo:** médio (perícia/vantagem têm pré-req menos estruturado que magia). **Risco:** médio.
- **Ganho:** menos tentativa-e-erro; consistência com o fluxo de magia.

### 🟡 PROPOSTA E — Editar atributos SECUNDÁRIOS e MODIFICADORES de vantagem
`forjador_editar_ficha` cobre ST/DX/IQ/HT, PF, nome, história, pontosIniciais. **Não cobre:**
modPV, modVontade, modPercepção, modVelocidade, modDeslocamento (todos editáveis via ViewModel),
nem ajustar modificadores de uma vantagem já na ficha. Aditivo, completa a cobertura de edição.
- **Custo:** baixo. **Risco:** baixo.

### 🟡 PROPOSTA F — Consolidar o cálculo de custo (DRY / anti-divergência)
Hoje há TRÊS lugares somando pontos: `Personagem.pontosGastos` (correto), `Executor.calcularPontosGastos`
(errado, BUG-1), `UseCase.validarBudget` (errado, BUG-2). Depois de A, **remover os dois últimos**
e deixar só a fonte de verdade. Reduz superfície de bug futuro.

---

## 5. O QUE **NÃO** FAZER (lições do Auditor)

- **NÃO** colocar exemplos concretos de build no prompt (ex: "para um guerreiro use ST 14, Espada
  Longa NH 16..."). Isso vira cola e enviesa toda criação para o mesmo arquétipo. O prompt já tem
  alguns ("LADRÃO → ...", "GUERREIRO → ST 13-15...") — **candidatos a revisar/remover** se virarem viés.
  Preferir REGRAS CATEGORIAIS + ferramentas que o modelo consulta.
- **NÃO** fazer o modelo recalcular o que a ficha já calcula (custo, NH, dano, defesa). Sempre LER
  o número do app — é a verdade (mesma filosofia do `forjador_gps_magia`).
- **NÃO** quebrar o read-back automático nem a âncora de "pedido real" — são proteções que funcionam.

---

## 6. ORDEM SUGERIDA DE EXECUÇÃO (se aprovado)

1. **Lote 1 — Proposta A + F** (custo real + remover fórmulas erradas). Pré-requisito de tudo:
   o Forjador precisa enxergar pontos certos antes de qualquer coisa. Baixo risco, alto retorno.
2. **Lote 2 — Proposta B** (equipamento de catálogo). Tangível para o usuário, integra automações.
3. **Lote 3 — Proposta C** (read-back numérico). Fecha o loop de auto-correção.
4. **Lote 4 — Propostas D + E** (GPS de pré-req não-magia + edição secundária). Refinamento.

Cada lote: build obrigatório verde **antes** de commit; testar criar UMA ficha e editar UMA ficha;
medir no log se o Forjador passou a citar pontos/stats reais. Sem exemplos hardcoded.

---

## 7. ARQUIVOS QUE CADA PROPOSTA TOCA (referência rápida)

| Proposta | Arquivos |
|---|---|
| A (custo real) | `ForjadorToolExecutor.lerFicha` (secao=pontos), `MestreIAGeneratorUseCase.validarBudget` |
| B (equipamento) | `ForjadorTools` (schema), `ForjadorToolExecutor.buscarCatalogo`/`editarFicha`, usa `DataRepository.armasCatalogo/armadurasCatalogo/escudosCatalogo` |
| C (stats calc) | `ForjadorToolExecutor.lerFicha` (nova seção), usa `DefesasAtivas`/`Personagem` (cálculos já prontos) |
| D (GPS pré-req) | `ForjadorTools` + `ForjadorToolExecutor` (novo veredito), usa `tecnicaAtendePreRequisito`/`PreRequisitoChecker` |
| E (edição extra) | `ForjadorTools` + `ForjadorToolExecutor.editarFicha`, métodos `atualizarMod*` do ViewModel |
| F (DRY) | remove `Executor.calcularPontosGastos` e `UseCase.validarBudget` legados |

Fonte de verdade do custo/stat: **`model/Personagem.kt`** (propriedades calculadas) +
**`domain/rules/CharacterRules.kt`** + **`model/...DefesasAtivas`**. Nunca recalcular fora daí.

---

## 8. LOTE 329 — B-COMPLETO (criação incremental) + correções pós-teste

**B-completo (feito):** a criação deixou de depender do JSON final + botão INTEGRAR.
O Forjador monta a ficha AO VIVO via `forjador_editar_ficha` (igual ao modo análise),
aplicando bloco a bloco na ordem dos 9 pilares. Se a conexão cai no meio, o que já
foi aplicado permanece. Ao iniciar, se há ficha não-vazia → `autoSaveIA()` + `novaFicha()`.
Arquivos: `MestreIAGeneratorUseCase` (salvar+zerar; síntese vira [FECHAMENTO] sem JSON),
`MestreIAPromptsForjador.PROMPT` (seção MODO INCREMENTAL), `FichaIADelegate` (modo geracao
não cria `fichaGeradaPendente`). Teto de pesquisa 4→12 + aviso de orçamento por rodada.

**Correções pós-teste (feito), a partir do log + comparação com fichas humanas:**
- BUG aparência: `editarFicha` não aceitava `aparencia`/`notas` → perdia o texto. Corrigido.
- BUG Riqueza: id existe em vantagens E desvantagens (escala única); o executor sempre
  preferia a vantagem (+10). Corrigido p/ respeitar a seção pedida + retornar custo real.
- custoEscolhido: `buscar_catalogo` agora mostra opções de escala (ESCOLHA/POR_NIVEL/
  VARIAVEL) e o prompt ensina a passar `custo=N`/`nivel=N`. Antes ignorava (tudo nível 1,
  custo-base) — fichas humanas usam muito (Riqueza, Status, Mestre de Armas).
- Anti-desperdício: prompt instrui não repetir busca já feita e aplicar antes de pesquisar mais.
- Log do budget no início da criação.

### PENDÊNCIAS conhecidas (lotes futuros)
- **MOJIBAKE — NÃO reproduzido na fonte (provável falso positivo).** O "histÃ³rias"/
  "incÃªndio" aparecia só na cópia vista pela IA assistente; no editor do usuário e no
  logcat REAL o texto está correto → é artefato de transferência/encoding na leitura,
  não bug do app. NÃO investigar por ora. Só reabrir SE surgir um bug de encoding
  observado no próprio app (ficha exibindo caractere errado na tela do usuário).
- **Equipamentos podem não ser aplicados** se o loop encerrar (2/2 sem avanço) antes do
  pilar 8. O anti-desperdício deve aliviar; reconfirmar em teste antes de mexer mais.
- **Arquétipos hardcoded** (LADRÃO/GUERREIRO/MAGO no PROMPT) citam custo e contradizem a
  regra "o app calcula custo". Candidatos a remover — usuário fará teste A/B antes.
