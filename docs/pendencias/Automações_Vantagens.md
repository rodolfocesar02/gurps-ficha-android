# Automações de Vantagens

> **Para o agente de IA.** Este arquivo é a fonte de verdade para automatizar as
> 272 vantagens de `assets/vantagens.v3.json`. Leia-o ANTES de implementar
> qualquer automação de vantagem. Ele descreve o mecanismo existente, os ganchos
> que faltam criar, e a especificação de cada vantagem.
>
> Criado em 2026-07-27. Escopo: só VANTAGENS. Desvantagens virão em arquivo
> irmão (`Automações_Desvantagens.md`) depois.
---

# ✅ STATUS FINAL — 28 de Julho de 2026 (ler primeiro)

**As categorias CAT-1..CAT-10 estão CUMPRIDAS.** Números reais, medidos no
catálogo. ⚠️ **O plano foi REABERTO em 28/07** com a fila do §11 — ideias do
usuário que pedem tela nova, não só declaração.


| Categoria | Previsto no plano | Situação real |
|---|---|---|
| CAT-1 Perícia | ~20 | ✅ **14/14 dos nomeados** declarados |
| CAT-2 Defesa | ~10 | ✅ 3 em Kotlin + Reflexos em Combate declarado |
| CAT-3 Atributo | ~15 | ✅ só existem 4 no catálogo, e **as 4 são escopadas** (ver abaixo) |
| CAT-4 RD | ~10 | ❌ descartado — é regra, não dado |
| CAT-5 Deslocamento | ~15 | ❌ descartado — já há cálculo próprio; declarar duplicaria |
| CAT-6 Reação | ~10 | ✅ **10 declarados** (REACAO-1/3/4) |
| CAT-7 Ataque/dano | ~6 | ✅ 4 em Kotlin; os 3 "verificar" foram lidos e não geram bônus |
| CAT-8 Sentidos | ~19 | ✅ `SentidoRules` já tratava |
| CAT-9 Custo especial | 30 | ✅ já implementadas |
| CAT-10 Narrativa | ~118 | fora do escopo por definição |

**Total: 42 traços com efeito declarado + 13 regras Kotlin.**

## O que foi LIDO e descartado, com motivo (não repetir)

| id | Por quê |
|---|---|
| `crescimento`, `encolhimento` | mudam o **Modificador de Tamanho**, que não é atributo do modelo. Precisa de um gancho de MT — é regra nova, não declaração |
| `durabilidade_sobrenatural` | "imune a choque, atordoamento e nocaute" é comportamento do **motor de combate**, como a Vulnerabilidade (D-6) |
| `neutralizar` | gera rolagem, sim — mas é **Disputa Rápida de Vontade contra um alvo**, e a aba Rolagem só rola contra número-alvo |
| `dominacao`, `restaurar_membros` | narrativa e tempo de recuperação; sem número por rolagem |
| `ambidestria` | o "−4 na DX" é a penalidade que ela **REMOVE**. Declarar daria −4 a quem comprou |
| `infravisao` (+2 Visão), sentidos em geral | já tratados pelo `SentidoRules` — declarar daria o bônus **em dobro** |
| `versatil` | 5 pts fixos com três especialidades; `porOpcao` não ajuda porque o custo é o mesmo |
| `abencoado` | `costKind: special` sem lista de `options` — o `porOpcao` precisa delas para o validador conferir |
| 13 traços de "teste pontual de atributo" | "+1 HT para ver se sobrevive" e afins. **Única pendência real** — ver abaixo |

## Única pendência

**Condicional de ATRIBUTO.** O interpretador sabe representá-los, mas a caixinha
de bônus condicional só existe para perícia e defesa: a rolagem de atributo é um
toque direto no número, sem diálogo onde a caixinha caiba. **Declarar hoje seria
efeito morto na ficha.** Precisa de decisão de UI antes.

Traços afetados: `hipoalgia`, `tolerancia_ao_alcool`, `facil_de_matar`, `enjoo`,
`enjoo_espacial`, `estomago_sensivel`, `fanatismo`, `sangue_frio`, `vicio`,
`dorminhoco`, `supersensitivismo`.

⚠️ **Corrigido em 28/07:** `duro_de_matar` e `dificil_de_subjugar` estavam nesta
lista e **saíram**. Eu os classifiquei como "sem onde aparecer" sem ler a
descrição até o fim — os dois nomeiam um **marco de PV exato** ("quando os PV
ficam menores que −1×PVInicial", "para evitar a inconsciência"), e o §11.1
mostrou que esse marco tem onde aparecer. São os dois primeiros clientes do
lote MARCOS-1.

---

---

## 0. O que "automatizar" significa aqui

O usuário adiciona a vantagem na ficha e o **efeito mecânico aparece sozinho**,
sem ele digitar bônus manualmente.

**Exemplo canônico já funcionando — `defesas_ampliadas_aparar_ampliado`:**
o jogador escolhe "Todas as manobras Aparar (10 pts)"; a partir daí o valor de
Aparar exibido na ficha e usado no combate já vem +1. Nada é digitado à mão.

Automatizar NÃO é:
- reescrever a descrição (isso é catálogo, não automação);
- criar um lembrete/aviso textual ("lembre-se de somar +1") — isso é o oposto
  do objetivo;
- inventar regra que o Módulo Básico não define.

---

## 0.5 REGRAS DE ENGENHARIA (obrigatórias — ler antes de criar arquivo)

Duas regras dadas pelo usuário em 2026-07-27. Valem para TODO lote deste plano.

### R1 — Nenhum arquivo deve passar de 1.000 linhas

Estado atual do projeto (medido em 2026-07-27) — arquivos que **já estouraram**:

| Arquivo | Linhas |
|---|---|
| `domain/combat/CombatSession.kt` | 3.178 |
| `ui/TabVtt.kt` | 2.270 |
| `viewmodel/delegates/SagaCombatController.kt` | 2.119 |
| `ui/saga/CombatUi.kt` | 1.610 |
| `domain/MestreIAGeneratorUseCase.kt` | 1.525 |
| `ui/components/GeminiLiveService.kt` | 1.359 |
| **`model/Personagem.kt`** | **1.164** ⚠️ |
| `ui/TabRolagem.kt` | 1.128 |
| `domain/loaders/CatalogLoaders.kt` | 1.069 |
| `viewmodel/FichaViewModel.kt` | 1.031 |

**Consequência direta para este plano:** o GANCHO-A (bônus de atributo) tem como
consumidor natural o `Personagem.kt` — que **já está com 1.164 linhas**.
❌ NÃO engordar o `Personagem.kt`.
✅ Criar `domain/rules/AtributoBonusRules.kt` com a lógica de soma, e no
`Personagem.kt` deixar só a chamada de uma linha:
```kotlin
val pontosVida: Int get() = st + modPontosVida + modeloRacial.modPontosVida +
    AtributoBonusRules.bonusDe(this, Atributo.PV)   // 1 linha, nada mais
```
Mesmo princípio já registrado para o motor de combate: o arquivo grande só ganha
um **encaminhador fino**; a mecânica nova mora fora.

**Ao encostar em 1.000 linhas, quebrar por RESPONSABILIDADE**, não por tamanho.
Errado: `VantagemDialogs2.kt`. Certo: separar o diálogo de configuração do
diálogo de seleção.

### R2 — Nada de arquivo solto: mapa de destino

Toda pasta abaixo já existe (verificado), exceto onde marcado **CRIAR**.

| O que vou criar | Onde vai | Por quê |
|---|---|---|
| Regra de vantagem (`PendulearRule.kt`, etc.) | `domain/rules/traits/` | é onde vivem as 11 regras atuais |
| Contrato dos ganchos novos (enum `Atributo`, `ModoDeslocamento`, `BonusPericia`) | `domain/rules/traits/TraitEffectModels.kt` **CRIAR** | um arquivo só para os tipos, não espalhar data class |
| Soma de bônus de atributo | `domain/rules/AtributoBonusRules.kt` **CRIAR** | mantém `Personagem.kt` magro (R1) |
| Interpretador do campo `efeitos` do JSON (se adotado — §10) | `domain/rules/traits/EfeitoInterpretador.kt` **CRIAR** | fica junto do resto das regras de traço |
| RD natural (GANCHO-B) | `domain/rules/traits/` para a regra; consumo em `domain/combat/subsistemas/` | a pasta `subsistemas/` já existe justamente para não inchar o `CombatSession.kt` |
| UI de Teste de Reação (GANCHO-D) | `ui/features/rolagem/DialogoReacao.kt` **CRIAR** | espelha o `DialogoSentidos.kt`, que já mora lá |
| Testes de qualquer regra de traço | `app/src/test/java/com/gurps/ficha/domain/rules/traits/` **CRIAR PASTA** | a pasta `domain/rules/` de teste existe, `traits/` ainda não |
| Script de validação/geração | `scripts/` | convenção do projeto (~40 scripts Python) |

⚠️ **Se a pasta `domain/rules/traits/` passar de ~25 arquivos**, subdividir por
categoria: `traits/pericias/`, `traits/defesas/`, `traits/atributos/`.
Lembrar que em Kotlin **subpasta = subpacote**: mover arquivo exige atualizar a
linha `package` dele e os `import` de quem o usa. Fazer isso num lote dedicado,
nunca junto com regra nova.

### R3 — Antes de criar QUALQUER arquivo, rodar

```bash
find app/src/main/java -name "*.kt" -exec wc -l {} + | sort -rn | head -12
```
Se o arquivo que eu ia editar aparecer nessa lista, **não edito: crio ao lado**.

---

## 1. Mecanismo existente (como uma automação se pluga)

### 1.1 A interface

`domain/rules/traits/TraitRule.kt` — cada vantagem automatizada implementa esta
interface e é registrada no `TraitRuleRegistry`.

Ganchos que **já existem**:

| Gancho | Devolve | Quem consome |
|---|---|---|
| `calculateCost` | custo em pontos (ou null = padrão) | `CharacterRules` |
| `getSkillModifiers` | `Map<nomePerícia, bônus>` | `Personagem.kt:582` |
| `getParryModifier` | Int (por perícia) | `Personagem.kt:849` |
| `getDodgeModifier` | Int | `Personagem.kt:827` |
| `getBlockModifier` | Int | `Personagem.kt:886` |
| `getAttackOptions` | opções de rolagem de ataque | aba Rolagem |
| `getDamageOptions` | fontes de dano | aba Rolagem |
| `getDefenseOptions` | defesas ativas extras | `FichaCombatDelegate` |
| `getDamageBonusPerDie` | Int (ex.: Mestre de Armas) | cálculo de dano |

### 1.2 O registro

`domain/rules/traits/TraitRuleRegistry.kt` — `init { register(MinhaRule()) }`.
O Registry expõe agregadores (`getSkillBonus`, `getParryBonus`, `getDodgeBonus`,
`getBlockBonus`, `getDamageBonusPerDie`) que **somam a contribuição de todas as
vantagens do personagem**. O `Personagem` chama esses agregadores nas suas
propriedades calculadas.

### 1.3 Onde ficam as escolhas do jogador

`VantagemSelecionada.metadados: Map<String,String>`. É onde a UI grava a escolha
(qual perícia, qual tipo de bônus, etc.). A regra lê de lá.

No exemplo do Aparar Ampliado:
```
metadados["tipo"]    = "global" | "especifica"
metadados["skillId"] = "briga" | "espada_larga" | "desarmado"
```

### 1.4 Regras já implementadas (11) — NÃO reimplementar

`ataque_inato`, `golpeadores`, `dentes`, `flexibilidade`, `garras`,
`defesas_ampliadas_aparar_ampliado`, `defesas_ampliadas_bloqueio_ampliado`,
`defesas_ampliadas_esquiva_ampliada`, `mestre_de_armas`, `telecomunicacao`,
`idioma`.

---

## 2. Ganchos que FALTAM criar

O contrato atual cobre perícia, defesa, ataque, dano e custo. **Não cobre** os
efeitos abaixo — a maioria das vantagens pendentes cai aqui. Cada gancho novo é
um lote próprio: criar o gancho + o consumidor + testes, e só então as regras que
o usam.

> **Princípio (vale o mesmo do motor de combate):** o `TraitRule` não deve
> engordar sem necessidade. Só criar gancho quando houver ≥3 vantagens reais que
> o usem. Efeito de vantagem única resolve-se dentro de um gancho existente.

### GANCHO-A — Bônus em atributo / característica secundária
```kotlin
fun getAttributeModifiers(
    personagem: Personagem,
    selection: VantagemSelecionada
): Map<Atributo, Int> = emptyMap()   // Atributo = enum ST/DX/IQ/HT/VONT/PER/PV/PF/VEL/DESL
```
**Consumidor:** `Personagem.kt:126-131` (as propriedades `pontosVida`,
`vontade`, `percepcao`, `pontosFadiga`, `velocidadeBasica`, `deslocamentoBasico`).
Hoje elas somam apenas `mod*` manual + `modeloRacial.mod*`; precisam somar
`TraitRuleRegistry.getAttributeBonus(personagem, atributo)`.

⚠️ **Risco de recursão:** `getAttributeModifiers` NÃO pode ler o atributo que
ela mesma modifica. Ex.: uma regra que leia `personagem.pontosVida` para
calcular bônus de PV entra em laço infinito. Testar isso.

### GANCHO-B — RD natural (armadura corporal)
```kotlin
fun getRdNatural(
    personagem: Personagem,
    selection: VantagemSelecionada
): Map<LocalAtaque, Int> = emptyMap()   // LocalAtaque já existe em domain/combat
```
**Consumidor:** o cálculo de RD por local na aba Combate e o
`HitLocationRules.aplicarDano` do motor de combate. Hoje a RD vem só das
armaduras equipadas.

### GANCHO-C — Modo de deslocamento
```kotlin
fun getModosDeslocamento(
    personagem: Personagem,
    selection: VantagemSelecionada
): List<ModoDeslocamento> = emptyList()  // NOVO data class: nome, valor, unidade
```
**Consumidor:** aba Geral (exibir "Voo 12", "Natação 5") e, no futuro, o combate
tático (movimento no grid).

### GANCHO-D — Modificador de reação
```kotlin
fun getReactionModifier(
    personagem: Personagem,
    selection: VantagemSelecionada,
    contexto: ContextoReacao?     // NOVO enum: GERAL, MESMO_SEXO, ANIMAIS, PLANTAS...
): Int = 0
```
**Consumidor:** **não existe ainda.** Precisa de uma tela/seção "Teste de
Reação" na aba Rolagem, igual ao `DialogoSentidos` foi para Sentidos.
Sem esse consumidor, o gancho é inútil — implementar juntos.

---

## 3. Classificação das 272 vantagens

| Categoria | Qtd aprox. | Automatizável? |
|---|---|---|
| **CAT-1** Bônus fixo em perícia nomeada | ~20 | Sim, gancho existente |
| **CAT-2** Bônus em defesa ativa | ~10 | Sim, gancho existente |
| **CAT-3** Bônus em atributo/secundária | ~15 | Precisa GANCHO-A |
| **CAT-4** RD / proteção natural | ~10 | Precisa GANCHO-B |
| **CAT-5** Modo de deslocamento | ~15 | Precisa GANCHO-C |
| **CAT-6** Modificador de reação | ~10 | Precisa GANCHO-D + UI |
| **CAT-7** Fonte de ataque/dano | ~6 | Sim, gancho existente |
| **CAT-8** Sentidos | ~19 | Parcial — ver §5 |
| **CAT-9** Custo especial (só cálculo) | 30 | Sim, `calculateCost` |
| **CAT-10** Narrativa pura | ~118 | **Não** — ver §6 |

---

## 4. Especificação por vantagem

> Formato de cada ficha. Ao implementar, seguir à risca.
> **Status:** `PRONTO` / `PENDENTE` / `BLOQUEADO (gancho X)` / `NÃO AUTOMATIZAR`

### 4.1 CAT-1 — Bônus fixo em perícia (gancho `getSkillModifiers`)

Estas são as mais baratas de implementar: uma regra de ~10 linhas cada, sem UI
nova. **Fazer todas num lote só.**

| id | Bônus | Observação de implementação |
|---|---|---|
| `pendulear` | +2 Escalada | fixo, sem escolha |
| `senso_de_direcao` | +3 Percepção do Corpo, +3 Navegação (Ar), (Terra) e (Mar) | ⚠️ **três especializações distintas** — ver "armadilha da especialização" abaixo |
| `voz_melodiosa` | +2 Arremedo (+ outras) | ler descrição completa antes |
| `ultravisao` | +2 Observação, +2 Perícia Forense, +2 Revistar | |
| `noção_tridimensional_do_espaco` (`nocao_tridimensional_do_espaco`) | +1 Pilotagem, +2 Acrobacia | |
| `rosto_sincero` | +1 Dissimulação | **condicional** ("para parecer honesto") — ver ⚠️ abaixo |
| `voz_penetrante` | +1 Intimidação | **condicional** ("se surpreender") |
| `camaleao` | +2 Furtividade **por nível** | usar `selection.nivel` |
| `silencio` | +6 ou +3 Furtividade **por nível**, conforme parado/movendo | condicional — ver ⚠️ |
| `invisibilidade` | +9 Furtividade | valor alto; conferir no livro antes |
| `recuperacao_acelerada` | +5 HT **para recuperar** | é teste específico, não HT geral → não usar GANCHO-A |
| `reflexos_em_combate` | +2 Verificação de Pânico, +1 defesas ativas | **híbrido**: parte CAT-1, parte CAT-2 |
| `carisma` | +1 por nível em reação e algumas perícias | híbrido com CAT-6 |
| `aparencia` | +N reação por nível (Elegante+3, Muito Elegante+4, Lindo+5) | CAT-6, não CAT-1 |

⚠️ **Bônus condicional.** Vários bônus só valem "em certas situações"
(`rosto_sincero`: só para parecer honesto). Aplicar sempre seria **errado** e
inflaria a ficha.

**Decisão de projeto:** bônus condicional NÃO entra no NH base da perícia.
Ele entra como **opção marcável no momento da rolagem** — a aba Rolagem já tem
diálogo de modificador. A regra devolve o bônus com a flag de condição, e a UI
oferece "aplicar bônus de Rosto Sincero? (+1)".
Isso exige um campo novo no retorno:
```kotlin
data class BonusPericia(val pericia: String, val valor: Int, val condicao: String?)
// condicao == null -> soma direto no NH; != null -> vira opção na rolagem
```
Enquanto esse suporte não existir, implementar **só os incondicionais** e deixar
os condicionais como PENDENTE.

⚠️ **A armadilha da especialização.** `senso_de_direcao` dá +3 em
**Navegação (Ar)**, **Navegação (Terra)** e **Navegação (Mar)** — três perícias
separadas no GURPS, não uma. Como `getSkillBonus` casa por NOME, devolver
`mapOf("Navegação" to 3)` **não pega nenhuma das três**. O retorno tem que
listar as especializações exatas como aparecem em `pericias.json`.

Antes de escrever qualquer regra de CAT-1, rodar:
```bash
python -c "import json;print([p['nome'] for p in json.load(open('app/src/main/assets/pericias.json',encoding='utf-8')) if 'Naveg' in p.get('nome','')])"
```
e usar a string exata devolvida. Vale para toda perícia com especialização
(Navegação, Armas de Fogo, Pilotagem, Ciência...).

### 4.2 CAT-2 — Bônus em defesa ativa

| id | Efeito | Status |
|---|---|---|
| `defesas_ampliadas_aparar_ampliado` | +1 Aparar (global ou perícia) | **PRONTO** — modelo a copiar |
| `defesas_ampliadas_bloqueio_ampliado` | +1 Bloqueio | **PRONTO** |
| `defesas_ampliadas_esquiva_ampliada` | +1 Esquiva | **PRONTO** |
| `reflexos_em_combate` | ver decomposição abaixo | PENDENTE — automatizar só as partes 1-3 |
| `durabilidade_sobrenatural` | defesa especial | ler o livro; provavelmente não é bônus numérico |

**Decomposição de `reflexos_em_combate`** — é o exemplo de vantagem que NÃO cabe
num gancho só. A descrição no catálogo tem 6 efeitos distintos:

| # | Efeito | Como automatizar |
|---|---|---|
| 1 | +1 em **todas** as defesas ativas | `getParryModifier` + `getDodgeModifier` + `getBlockModifier`, todos devolvendo 1 |
| 2 | +1 em Sacar Rápido | `getSkillModifiers` — conferir o nome exato da perícia |
| 3 | +2 em Verificação de Pânico | `getSkillModifiers` (ou teste próprio, se existir) |
| 4 | +6 em IQ para se recuperar de surpresa/atordoamento | teste pontual, não IQ global → **não** usar GANCHO-A. Pertence ao motor de combate (`InjuryRules.recuperarAtordoamento`) |
| 5 | nunca fica "paralisado" na surpresa | regra de combate, não bônus |
| 6 | **+1/+2 de iniciativa para o GRUPO INTEIRO** | ❌ **não automatizável** — a ficha é de um personagem só; o app não conhece o grupo |

O item 6 é a lição geral: **efeito que age sobre outros personagens não tem onde
morar na ficha individual.** Documentar e ignorar.

### 4.3 CAT-3 — Bônus em atributo (BLOQUEADO até GANCHO-A)

| id | Efeito |
|---|---|
| `st_bracal` | +1 ST **só para braços** — não é ST global; precisa de escopo |
| `dx_bracal` | +1 DX para um braço / +1 nos dois |
| `destreza_manual_elevada` | +1 DX **para tarefas manuais finas** |
| `crescimento` | +ST por nível, muda Modificador de Tamanho |
| `encolhimento` | inverso do Crescimento |

⚠️ Note que `st_bracal` e `dx_bracal` **não são** bônus de atributo global — são
por membro. GANCHO-A com `Map<Atributo,Int>` não expressa isso. Ou o gancho
ganha um escopo, ou essas viram caso especial. **Decidir antes de implementar.**

### 4.4 CAT-7 — Fonte de ataque/dano (gancho existente)

| id | Status |
|---|---|
| `ataque_inato`, `golpeadores`, `dentes`, `garras` | **PRONTO** |
| `neutralizar`, `dominacao`, `restaurar_membros` | PENDENTE — verificar se geram rolagem |

### 4.5 CAT-9 — Custo especial (30 vantagens, `specialRule` != null)

Estas já têm UI de configuração (`TraitSpecialRuleComponents.kt`) e cálculo em
`CharacterRules` (`calcularCustoAliado`, `calcularCustoInimigo`, ...).
**Verificar uma a uma se o cálculo está certo** — é manutenção, não automação
nova. Não confundir com efeito mecânico.

---

## 5. Sentidos — já existe infraestrutura

`domain/rules/SentidoRules.kt` + `ui/features/rolagem/DialogoSentidos.kt` já
tratam Visão/Audição/Olfato/Tato Aguçados, mapeando ids do catálogo e mostrando
os componentes do cálculo.

**Antes de criar regra nova para qualquer vantagem sensorial, conferir se
`SentidoRules` já a cobre.** As ~19 vantagens de sentido em parte já estão lá.
O que falta é o que NÃO é teste de Percepção (ex.: `visao_telescopica` dá
+1 Precisão de arma por nível → isso é CAT-7/combate, não sentido).

---

## 6. CAT-10 — Narrativa pura (NÃO AUTOMATIZAR)

~118 vantagens não têm efeito numérico que o app possa aplicar sozinho:
`status_social`, `riqueza`, `reputacao` (parte), `contatos`, `aliados`,
`antecedentes_incomuns`, `apetrechos`, `imunidade_legal`, `posses`, etc.

**Regra:** se o efeito depende de julgamento do Mestre ou de ficção
("o personagem conhece alguém que..."), **não automatizar**. O valor delas está
na descrição do catálogo — que já foi corrigida contra o livro.

**Exceção a considerar no futuro:** algumas dessas (Riqueza, Status) afetam
compra de equipamento e reação social. Se um dia houver economia/reação no app,
reavaliar.

---

## 7. Ordem de implementação sugerida

Do mais barato e seguro para o mais caro.
⚠️ A arquitetura híbrida (§10) mudou o começo: primeiro o trilho, depois os dados.

0. **LOTE V-0** — o interpretador de `efeitos` do JSON. Ver §10.4 e §10.5.
   Nenhuma vantagem automatizada; só a infraestrutura que os lotes seguintes usam.
1. **LOTE V-1** — CAT-1 incondicionais (~8 vantagens), declaradas **em JSON**
   (§10.2). Zero Kotlin novo. Teste: personagem com a vantagem tem o NH +N.
2. **LOTE V-2** — `reflexos_em_combate` (CAT-2). Gancho existente, 3 defesas.
3. **LOTE V-3** — GANCHO-A. Criar `domain/rules/AtributoBonusRules.kt` +
   `TraitEffectModels.kt` (enum `Atributo`). No `Personagem.kt` entra **só a
   chamada de uma linha** por propriedade (R1 — ele já tem 1.164 linhas).
   Teste obrigatório: recursão (regra que lê o atributo que ela modifica).
4. **LOTE V-4** — CAT-3 usando GANCHO-A (decidir antes a questão do escopo por
   membro).
5. **LOTE V-5** — suporte a bônus condicional (`BonusPericia.condicao`) + UI na
   rolagem. Desbloqueia os condicionais da CAT-1.
6. **LOTE V-6+** — GANCHO-B (RD), GANCHO-C (deslocamento), GANCHO-D (reação,
   junto com a UI de Teste de Reação).

**Cada lote:** regra + registro no Registry + teste unitário + build nas 2
variantes. Lote que toca UI PARA para teste no aparelho.

**Checklist de fim de lote** (R1/R2):
- [ ] nenhum arquivo que eu toquei passou de 1.000 linhas — conferir com o
      comando da R3
- [ ] todo arquivo novo está numa pasta do mapa da R2, nenhum solto
- [ ] o teste está na pasta espelho (`app/src/test/.../domain/rules/traits/`)
- [ ] `TraitRuleRegistry.init` tem a linha `register(...)` da regra nova

---

## 8. Como escrever uma regra nova (modelo)

```kotlin
package com.gurps.ficha.domain.rules.traits

/**
 * Pendulear (GURPS MB p.77): +2 em Escalada.
 * Bônus incondicional -> entra direto no NH da perícia.
 */
class PendulearRule : TraitRule {
    override val traitId = "pendulear"

    override fun getSkillModifiers(
        personagem: Personagem,
        selection: VantagemSelecionada
    ): Map<String, Int> = mapOf("Escalada" to 2)
}
```
Registrar em `TraitRuleRegistry.init`. Teste mínimo:
```kotlin
@Test fun `pendulear da mais 2 em escalada`() { ... }
```

⚠️ **Casamento de nome de perícia.** `getSkillBonus` compara pelo NOME da
perícia (`Personagem.kt:582`), não por id. Nome com acento/variação não casa.
Conferir o nome exato em `pericias.json` antes de escrever a regra — e, se
possível, migrar a comparação para id normalizado (dívida técnica registrada
aqui).

---

## 9. Achados de dados (corrigir em lote à parte)

- **`adaptabilidade_cultural` está SEM o campo `nome`** em `vantagens.v3.json`.
  Aparece em branco na lista. Verificar se há outros.
- Vários bônus estão só na prosa da descrição, não em campo estruturado.
  **RESOLVIDO:** o campo `efeitos` foi aprovado — ver §10. A prosa continua sendo
  a descrição fiel ao livro; o `efeitos` é a versão que a máquina lê. Os dois
  convivem e precisam concordar: ao declarar um efeito, conferir se o número bate
  com o que a descrição diz.

---

## 10. DECISÃO: arquitetura HÍBRIDA (definida pelo usuário em 2026-07-27)

**JSON para os simples, Kotlin para os complexos.** Não é opção — é a
arquitetura. Toda automação nova segue esta seção.

### 10.1 Critério de decisão (aplicar SEM exceção)

Vai para **JSON** se as três forem verdadeiras:
1. o efeito é um bônus **numérico fixo** sobre um alvo **nomeado**
   (perícia, defesa, atributo);
2. não depende de escolha do jogador (nada de `metadados`);
3. não precisa ler outra parte do personagem para decidir.

Vai para **Kotlin** se qualquer uma for verdadeira:
- exige escolha do jogador (ex.: Aparar Ampliado — qual perícia?);
- tem ramificação (`if`) ou tabela;
- calcula custo em pontos (`calculateCost`);
- gera opção de ataque/dano na aba Rolagem;
- lê outra parte da ficha (outras vantagens, equipamento, nível de outra coisa).

**Na dúvida, JSON.** É mais fácil promover um efeito de JSON para Kotlin depois
do que remover uma classe Kotlin que já foi escrita.

### 10.2 Formato do campo `efeitos` (novo, em `vantagens.v3.json`)

```json
{
  "id": "pendulear",
  "nome": "Pendulear",
  "efeitos": [
    { "tipo": "pericia", "alvo": "Escalada", "valor": 2 }
  ]
}
```

Campos de cada efeito:

| Campo | Obrigatório | Valores | Observação |
|---|---|---|---|
| `tipo` | sim | `pericia` \| `defesa` \| `atributo` | define qual gancho recebe |
| `alvo` | sim | nome da perícia / `esquiva`\|`aparar`\|`bloqueio` / `ST`\|`DX`\|`IQ`\|`HT`\|`VONT`\|`PER`\|`PV`\|`PF`\|`VEL`\|`DESL` | para `pericia`, tem que ser o nome EXATO de `pericias.json` (ver armadilha da especialização, §4.1) |
| `valor` | sim | inteiro (pode ser negativo) | |
| `porNivel` | não | bool, default `false` | se `true`, multiplica por `selection.nivel` |
| `condicao` | não | texto | se presente, **NÃO** entra no NH base — vira opção marcável na rolagem (§4.1). Enquanto o suporte não existir, o interpretador **ignora** o efeito e loga um aviso |

Exemplo com todos os campos (`camaleao`):
```json
"efeitos": [
  { "tipo": "pericia", "alvo": "Furtividade", "valor": 2, "porNivel": true,
    "condicao": "em situações onde não queira ser visto" }
]
```

Exemplo de vantagem com vários alvos (`senso_de_direcao`) — repare nas
especializações separadas:
```json
"efeitos": [
  { "tipo": "pericia", "alvo": "Percepção do Corpo", "valor": 3 },
  { "tipo": "pericia", "alvo": "Navegação (Ar)",    "valor": 3 },
  { "tipo": "pericia", "alvo": "Navegação (Terra)", "valor": 3 },
  { "tipo": "pericia", "alvo": "Navegação (Mar)",   "valor": 3 }
]
```

### 10.3 O ponto exato onde o interpretador se conecta

⚠️ **Este é o detalhe que faz o híbrido funcionar. Conferido no código em
2026-07-27.**

Os agregadores do `TraitRuleRegistry` (`getSkillBonus`, `getParryBonus`,
`getDodgeBonus`, `getBlockBonus`) fazem hoje:
```kotlin
personagem.vantagens.forEach { selection ->
    val rule = rules[selection.definicaoId]
    if (rule != null) { total += rule.getXxx(...) }   // <-- quem não está no mapa é IGNORADO
}
```
Ou seja: **vantagem sem regra Kotlin é silenciosamente ignorada**. O interpretador
entra fechando exatamente esse buraco:

```kotlin
// TraitRuleRegistry.kt — trocar a busca direta por um resolvedor
private fun resolver(traitId: String): TraitRule? =
    rules[traitId]                                   // 1º: regra Kotlin (tem prioridade)
        ?: EfeitoInterpretador.regraPara(traitId)    // 2º: efeitos declarados no JSON
```

**Precedência:** se a vantagem tiver regra Kotlin E campo `efeitos`, a **Kotlin
vence** e o JSON é ignorado. Isso permite migrar um caso de JSON para Kotlin sem
apagar o JSON. O interpretador deve **logar um aviso** quando detectar os dois,
para o caso não passar despercebido.

### 10.4 Arquivos do lote do interpretador

Seguindo R2 (§0.5):

| Arquivo | Pasta | O que faz |
|---|---|---|
| `EfeitoInterpretador.kt` | `domain/rules/traits/` **CRIAR** | lê `efeitos` da definição via `DataRepository.getVantagemPorId(id)` e devolve um `TraitRule` genérico |
| `TraitEffectModels.kt` | `domain/rules/traits/` **CRIAR** | `data class EfeitoDeclarado`, `enum TipoEfeito`, `enum Atributo` |
| `TraitRuleRegistry.kt` | (existente, ~100 linhas) | só o resolvedor de 2 linhas do §10.3 |
| `VantagemDefinicao` | `model/Personagem.kt` (existente) | **+1 campo** `efeitos: List<EfeitoDeclarado> = emptyList()`. Uma linha — respeita R1 |
| `EfeitoInterpretadorTest.kt` | `app/src/test/java/com/gurps/ficha/domain/rules/traits/` **CRIAR PASTA** | ver §10.5 |
| `validar_efeitos_vantagens.py` | `scripts/` **CRIAR** | valida o JSON antes de compilar (§10.6) |

`DesvantagemDefinicao` ganha o mesmo campo quando chegar a vez das desvantagens.

### 10.5 Testes obrigatórios do interpretador

Um erro aqui é pior que um erro numa regra Kotlin: afeta **todas** as vantagens
declarativas de uma vez.

1. efeito simples de perícia soma no NH;
2. `porNivel: true` multiplica pelo nível da vantagem selecionada;
3. `condicao` presente ⇒ **não** soma (e loga aviso);
4. `alvo` que não existe em `pericias.json` ⇒ não quebra o app, loga aviso;
5. vantagem com regra Kotlin **e** `efeitos` ⇒ vence a Kotlin, JSON ignorado;
6. vantagem sem `efeitos` ⇒ devolve zero, comportamento de hoje intacto;
7. **simulação:** carregar `vantagens.v3.json` de verdade e conferir que todo
   `alvo` de `tipo: "pericia"` casa com um nome real de `pericias.json`.
   (Invariante sobre o catálogo real, não sobre dado inventado.)

### 10.6 Validador de JSON (roda antes do build)

`scripts/validar_efeitos_vantagens.py` — falha com código 1 se:
- `tipo` fora do enum;
- `alvo` de perícia sem correspondente exato em `pericias.json`;
- `valor` não inteiro;
- vantagem com `efeitos` **e** regra Kotlin registrada (conflito do §10.3).

Motivo: erro de digitação no JSON não é pego pelo compilador. Sem esse
validador, `"Escalda"` em vez de `"Escalada"` vira bônus que simplesmente nunca
aplica — e ninguém percebe.

### 10.7 Impacto na ordem dos lotes (§7)

O LOTE V-1 muda: **antes** de escrever qualquer regra, construir o trilho.

- **LOTE V-0 (NOVO, primeiro de todos)** — interpretador + modelos + campo no
  `VantagemDefinicao` + resolvedor no Registry + validador + os 7 testes.
  Nenhuma vantagem automatizada ainda; só o trilho. Prova de que funciona:
  declarar `pendulear` no JSON e o teste mostrar Escalada +2.
- **LOTE V-1** — passa a ser só **dados**: declarar `efeitos` das ~8 vantagens
  incondicionais da CAT-1. Zero Kotlin novo.
- Os demais lotes seguem como em §7.

---

# 11. FILA NOVA — ideias do usuário (revisão de 28/07/2026)

> Origem: `docs/pendencias/Possiveis_Automações.md`, escrito pelo usuário ao
> reler as vantagens no Módulo Básico. **Isto reabre o plano**: o STATUS FINAL
> lá em cima continua valendo para as categorias CAT-1..CAT-10, mas as ideias
> abaixo pedem coisas que aquelas categorias não previam — telas novas e testes
> disparados por evento, não só bônus declarado.
>
> Nada aqui está implementado. Cada item traz **o que é**, **onde mora** (regra
> R2) e **o que trava**, para o lote poder ser aberto sem reler o livro.

## ⛔ REGRA DE ESCOPO (reafirmada pelo usuário em 28/07) — ler antes de tudo

> *"todas as implementações estão sendo pra beneficiar 1º as jogadas dentro da
> aba Rolagem... se por ventura beneficia a aba Saga ok, mantém; agora se for
> apenas pra aba Saga, analise e não faça"*

**O alvo é a aba ROLAGEM**, que é por onde o usuário joga via Discord. A Saga é
jogo solo, ainda em desenvolvimento, e o app vai se dividir em dois no futuro.

| Camada | Pastas | Serve a |
|---|---|---|
| **Ficha / Rolagem** | `ui/TabRolagem.kt`, `ui/features/rolagem/`, `domain/rules/` | ✅ **o alvo** — e envia para o Discord |
| **Combate tático** | `domain/combat/`, `ui/saga/`, `viewmodel/delegates/SagaCombatController.kt` | só a Saga |

**Como aplicar o filtro:** se a entrega só aparece com o combate aberto, **não
fazer**. Se aparece na aba Rolagem e *de brinde* também no combate, fazer.

⚠️ **Eu já errei isso nesta seção** — o lote 6 original ("disparo automático
dentro do combate") era Saga puro e foi **descartado**. Ver §11.10.

⚠️ Vale para o que já está pronto também: o **ST Braçal** (STB-2) foi feito só
para a aba Rolagem de propósito; o combate tático segue com a ST do corpo. Isso
não é lacuna — é o escopo certo.

## 11.0 O fio condutor: a ficha não tem onde pôr "teste de resistir"

Sete das ideias caem no mesmo buraco. O GURPS está cheio de testes que **não são
perícia nem atributo puro**: manter consciência, evitar a morte, resistir a
doença, veneno, magia, medo. Hoje a aba Rolagem só sabe rolar contra um NH ou
contra um atributo, e o Teste de Reação e o Autocontrole foram encaixados como
painéis soltos no fim da tela.

**A proposta do usuário resolve isso de uma vez:** um botão **"Reação e
Resistência"** na aba Rolagem, ao lado de *Perícias* e *Rolagem Livre*, que abre
um diálogo com todos os testes desse tipo que a ficha tiver.

Isso muda o desenho do que já existe — Reação e Autocontrole sairiam do fim da
tela e entrariam no diálogo — então **é o primeiro lote da fila**, e boa parte
dos outros depende dele.

### O que entra no botão

| Teste | Origem | Situação hoje |
|---|---|---|
| **Reação** | MB p.494 | ✅ pronto, é só mudar de lugar |
| **Autocontrole** | MB p.121 | ✅ pronto, é só mudar de lugar |
| **Resistência à Magia** (Abascanto) | MB p.85 | ❌ campo não existe — §11.2 |
| **Manter consciência** | MB p.419 | ❌ — §11.1 |
| **Evitar a morte** | MB p.423 | ❌ |
| **Resistir a doença / veneno** | MB p.442-443 | ❌ |
| **Verificação de Pânico** | MB p.360 | ❌ — §11.1 |

⚠️ **Antes de abrir o lote, varrer o Módulo Básico atrás dos outros tipos de
teste de resistência** — o usuário pediu isso explicitamente, e a lista acima
saiu das vantagens que ele leu, não de uma varredura do livro. É provável que
falte afogamento, exaustão, tontura e choque.

**Onde mora (R2):**

| Arquivo | Pasta | Papel |
|---|---|---|
| `ResistenciaRules.kt` | `domain/rules/` **CRIAR** | Kotlin puro: lista os testes que a ficha tem, com alvo e origem. Irmão de `AutocontroleRules` e `ReacaoRules` |
| `DialogoReacaoEResistencia.kt` | `ui/features/rolagem/` **CRIAR** | o diálogo, ao lado de `DialogoSentidos.kt` |
| `PainelReacao.kt`, `PainelAutocontrole.kt` | (existentes) | passam a ser chamados de dentro do diálogo |

⚠️ **R1**: `TabRolagem.kt` está em ~999 linhas. O botão novo cabe, mas o diálogo
**tem que nascer em arquivo próprio** — não há espaço para embutir.

## 11.1 Testes disparados pela queda de PV / PF — ✅ VIÁVEL (decisão do usuário, 28/07)

> *"vamos tratar quando tomar o dano do PV e quando diminuir ali a relação
> 'dano' entra em ação! o mesmo quando envolver PF"*

**A ideia funciona, e é melhor do que a versão anterior deste texto.** Eu havia
escrito que o disparo automático "só existe dentro do combate". Está errado: a
aba Rolagem **já tem** os controles de PV/PF (`PvPfQuickRollPanel`, com
`onAjustarPv` / `onEditPv` e `pontosVidaRolagemAtual` persistido na ficha).
**Baixar o PV na ficha É o evento de dano** — não precisa do combate.

O gatilho, então, é: *PV atual caiu de X para Y* ⇒ conferir quais marcos foram
cruzados ⇒ oferecer os testes correspondentes.

### Os marcos que EXIGEM teste (compêndio §20, MB p.419-423)

| Situação | Teste | Vantagem que soma |
|---|---|---|
| Perda ≥ **PV máx / 2** num golpe | HT — knockdown e atordoamento | `dificil_de_subjugar` (parte do atordoamento) |
| **PV ≤ 0** | HT a cada turno para continuar consciente | **`dificil_de_subjugar`**, `boa_forma` |
| **PV ≤ −1× PV máx** (e a cada múltiplo: −2×, −3×, −4×) | HT ou morre | **`duro_de_matar`**, `boa_forma` |
| PV ≤ −5× PV máx | morte automática — **sem teste** | — |

⚠️ **Correção de escopo:** `duro_de_matar` estava na minha lista de "13
condicionais de atributo sem onde aparecer". **Estava errado.** A descrição dele
é literal: *"+1 nos testes de HT feitos para ver se o personagem consegue
sobreviver quando seu número de Pontos de Vida é menor que −1×PVInicial"*. Isso
é exatamente o marco acima — tem onde aparecer, sim.

### Os marcos de PV que são ESTADO, não teste

| PV atual | Estado |
|---|---|
| ≤ 1/3 do máximo | **Cambaleante**: Deslocamento e Esquiva pela metade, e teste de HT a cada turno de esforço para não cair |

### O lado do PF: quase tudo é estado, não rolagem

| PF atual | Efeito |
|---|---|
| ≤ 1/3 do máximo | **Cansado**: ST e DX caem pela metade |
| 0 | qualquer esforço extra passa a custar **PV** |
| ≤ −1× PF máx | desmaia — **automático, sem teste** |

**Ou seja:** o PF não gera teste novo. O que ele pede é **aviso de estado** na
tela ("Cansado — ST e DX pela metade"), que hoje não existe. Vale fazer junto,
porque é a mesma leitura de marcos, mas é entrega diferente: *mostrar*, não
*rolar*.

`boa_forma` toca o PF por outro caminho: recupera PF no **dobro** da velocidade
(e o nível Ótima Forma perde PF pela **metade**). Isso é tempo de descanso, não
marco — fica para o §11.7.

### ⚠️ A Verificação de Pânico NÃO entra aqui

`destemor` foi listado junto com `dificil_de_subjugar` na versão anterior, como
se os dois disparassem com dano. **Não é o caso.** A Verificação de Pânico é
disparada por **situação assustadora** (horror, sobrenatural, cena chocante), não
por perder PV. Conferido no compêndio §14 e na descrição da vantagem.

`destemor` continua valendo — mas como teste **manual**, dentro do botão do
§11.0, onde o Mestre pede e o jogador toca.

### Como implementar sem virar bug de fluxo

O risco aqui é o app rolar sozinho e o jogador não entender de onde veio o
número — o mesmo defeito que a zona de dano invisível causou no TOK-9.

**Regra de desenho: o app OFERECE, não rola.** Ao cruzar um marco, aparece um
aviso com o teste já montado (alvo, bônus e origem), e o jogador toca. Assim ele
vê *por que* está rolando, e o Mestre pode dispensar.

**Onde mora (R2):**

| Arquivo | Pasta | Papel |
|---|---|---|
| `MarcosDeVidaRules.kt` | `domain/rules/` **CRIAR** | Kotlin puro: recebe PV antes/depois e PV máx, devolve os marcos cruzados e os testes exigidos, com bônus e origem. Testável sem Android |
| `AvisoDeMarco.kt` | `ui/features/rolagem/` **CRIAR** | o aviso na tela, com o botão de rolar |

**Testes obrigatórios** (a lição do V-1 — testar o caminho todo, não a ponta):

- cair de 10 para 4 num golpe (PV máx 10) cruza o marco de ferimento grave;
- cair de 4 para 3 **não** cruza nada de novo (já estava cambaleante);
- cair para 0 oferece o teste de consciência **com** o bônus de Difícil de
  Subjugar somado;
- cair para −10 (PV máx 10) oferece o teste de morte **com** Duro de Matar;
- **subir** o PV (cura) não dispara nada;
- editar o PV máximo não dispara nada;
- ficha sem as vantagens recebe o mesmo teste, só sem o bônus.

## 11.2 Campo novo: Resistência à Magia (Abascanto)

MB p.85. O nível é **subtraído do NH de quem lança magia** no personagem e
**somado** à resistência dele. Também vale para elixires (teste de HT + nível).

**Por que é campo e não bônus:** não modifica perícia nem atributo do personagem
— modifica o **teste de outra pessoa**. Não existe gancho para isso.

### ⛔ Filtro de escopo: esta vantagem se parte em duas

| Metade | Onde aparece | Fazer? |
|---|---|---|
| **O personagem resiste** — teste de HT + nível contra elixires, e o bônus para resistir a magias lançadas nele | aba Rolagem, dentro do botão do §11.0 | ✅ **sim** |
| **O mago inimigo perde NH** — o −N no NH de quem lança magia no personagem | só existe com um conjurador PdM agindo, ou seja, **dentro do combate** | ❌ **não** — Saga pura |

O lote **RESIST-2 entrega só a primeira metade**: o campo na ficha e o teste que
o jogador rola. O número fica visível para o Mestre aplicar no Discord, que é o
uso real.

Restrições do livro que a ficha deveria respeitar:

- **incompatível com Aptidão Mágica** — o personagem não consegue lançar magia;
- **não protege** de projéteis mágicos, armas mágicas nem adivinhação;
- não pode ser "desligada" para receber magia benéfica.

### ✅ Decisão do usuário (28/07): trava mútua e automática

> *"se houver uma dessas vantagem na ficha, bloqueia automático a outra, e
> vice-versa"*

Ou seja: com Abascanto na ficha, **Aptidão Mágica não pode ser adicionada**, e
com Aptidão Mágica na ficha, **Abascanto não pode**.

Eu havia levantado o risco de repetir o erro do `conhecimento_oculto`, que
bloqueava uma compra legítima. **Não é o mesmo caso, e a decisão está certa:**

- no `conhecimento_oculto` o pré-requisito era *"Antecedentes Incomuns **a
  critério do Mestre**"* — decisão de mesa, que o app não tem como conhecer;
- aqui o livro é categórico: *"Esta vantagem **não pode** ser combinada com
  Aptidão Mágica"* (MB p.85). Não há margem de mesa.

**Onde mora:** `FichaTraitDelegate.adicionarVantagem` já devolve
`Result.failure(Exception(...))` para vantagem duplicada, e a UI já mostra a
mensagem em Toast. A trava entra no mesmo ponto — **não criar caminho novo**.

A mensagem precisa dizer o porquê, não só "não pode":
> *"Abascanto não combina com Aptidão Mágica (MB p.85): quem resiste à magia não
> consegue lançá-la. Remova uma para adicionar a outra."*

⚠️ **A trava vale para adicionar, não para fichas já salvas.** Ficha antiga com
as duas não pode quebrar ao abrir — nesse caso, **avisar** e deixar o jogador
resolver. Bloquear a abertura seria perder a ficha.

⚠️ **Generalizar com cuidado.** Se um dia isto virar uma tabela de
incompatibilidades, cada par precisa da mesma checagem: o livro proíbe, ou é
"a critério do Mestre"? Só o primeiro caso vira trava.

## 11.3 Mão hábil / inábil no botão de Ataque

Ideia do usuário, e a mais barata de todas com retorno imediato:

> Um quadrado no botão de Ataque distinguindo **mão hábil** / **mão inábil**.
> Marcando inábil, −4 nos testes (MB p.14). Se o personagem tiver
> **Ambidestria**, o redutor não aparece, mas **o seletor continua funcionando**.

É o mesmo padrão do seletor de ST Braçal, já validado no aparelho em 28/07.

**Nota importante:** este é o caso em que declarar `ambidestria` como efeito
seria **errado** — o "−4 na DX" é a penalidade que ela **REMOVE**, não concede.
Ver a tabela de descartes no STATUS FINAL. A automação certa é esta: a penalidade
é da UI, e a vantagem a zera.

**Onde mora:** o seletor em `ui/features/rolagem/`, ao lado do card de Ataque; a
regra ("quanto vale a mão inábil para este personagem") em
`domain/rules/MaoInabilRules.kt` **CRIAR**, para ter teste.

## 11.4 Declaráveis JÁ, sem tela nova

Estas cabem no campo `efeitos` como ele está hoje. **Lote barato.**

| id | Efeito | Formato |
|---|---|---|
| `empatia` | +1 ou +3 em Detecção de Mentiras, Adivinhação e Psicologia | `porOpcao` (5 pts → +1, 15 pts → +3) |
| `magro` | −2 Disfarce/NT | declaração simples; ver ⚠️ abaixo |
| `muito_gordo` | −3 Disfarce/NT, +5 Natação | idem |
| `acima_do_peso` | falta **+1 Natação** | já declarado só o Disfarce |
| `gordo` | falta a parte de Natação | já declarado só o Disfarce |
| `flexibilidade` | falta **Arte Erótica +3** | a `FlexibilidadeRule` dá só Escalada e Fuga; o livro lista as três |
| `boa_forma` | +1 ou +2 em **todos** os testes de HT | ⚠️ precisa do §11.0 — não é perícia |

⚠️ **A armadilha do "ou" nas de peso.** O livro diz *"−N em Disfarce — **ou** em
Perseguição, se estiver tentando seguir alguém no meio da multidão"*. São duas
perícias **alternativas**, não somadas. Declarar as duas daria penalidade dobrada
a quem tem as duas na ficha. Ou declara só Disfarce (o caso comum), ou o formato
ganha "efeito alternativo" — **e não vale criar recurso por um caso**.
Recomendação: declarar Disfarce e registrar a Perseguição como sabida-e-omitida.

⚠️ **`magro` e `muito_gordo` também travam a HT** (máximo 14 e 13). Isso é regra
de **criação de personagem**, não bônus — vive no validador da ficha, não no
`efeitos`. **Decisão do usuário (28/07): fazer, mas por último** — virou o item
final da fila (§11.9). Fica separado da declaração dos bônus porque toca outro
arquivo e outra regra.

⚠️ **`flexibilidade` tem regra Kotlin**, então a correção é no `.kt`, não no
JSON — a Kotlin vence e o JSON seria ignorado em silêncio.

## 11.5 Precisam de gancho que não existe

| id | O que pede | Por que não cabe hoje |
|---|---|---|
| `dx_bracal` | o mesmo seletor do ST Braçal | 🔴 **LACUNA REAL** — ver §11.6 |
| `destreza_manual_elevada` | o bônus aparecer **dentro de cada perícia** que o livro lista (Arrombamento, Artista, Cirurgia, Costura, Habilidade com Nós, Joalheiro, Prestidigitação, Punga, Trabalhos em Couro) | hoje está declarado como **condicional genérico**. O livro dá a lista fechada — dá para declarar perícia a perícia, e fica melhor |
| `facilidade_para_idiomas` | baratear o custo dos idiomas comprados | mexe em **custo de outro traço**. O `IdiomaRule` existe, mas precisaria ler a ficha inteira, não só a própria seleção |
| `reputacao` | caixinhas como as da Voz Melodiosa | tem 4 componentes **e uma rolagem de reconhecimento** — o modificador só vale se reconhecerem o personagem |
| `status` | idem | é hierarquia social; o efeito em reação é ocasional e "a critério do Mestre" |
| `controle_do_metabolismo` | ver §11.7 | — |

### 11.6 🔴 LACUNA ACHADA: DX Braçal ficou pela metade

Conferido no código em 28/07: o Lote **STB-1** corrigiu o **custo** das duas (ST
e DX Braçal), mas o **STB-2** implementou o efeito só da **ST**. Quem compra DX
Braçal paga certo e **não recebe nada na tela**.

O que falta, espelhando o que já existe para ST:

- `DxBracalRules.kt` em `domain/rules/` (irmão de `StBracalRules.kt`);
- seletor abaixo do **DX** no painel de atributos;
- ⚠️ **e uma diferença de regra**: o livro diz que *"as perícias de combate
  dependem da DX corporal e **não se beneficiam** da destreza braçal"*. O seletor
  da DX Braçal **não pode** afetar o NH de ataque — ao contrário do ST Braçal,
  que afeta o dano. Isso precisa de teste próprio.

**Prioridade alta**: é dívida de um lote que o usuário já pagou em pontos.

## 11.7 O pedido grande: Capítulo 14 (MB p.418+)

> *"preciso que analise o Capítulo Quatorze — Lesões, Enfermidades e Fadiga, a
> partir da pág. 418, como podemos automatizar esses elementos na ficha"*

Isto **não é um lote**, é uma frente inteira — do tamanho do que foi a automação
de traços. Cobre sangramento, choque, inconsciência, morte, doença, veneno,
fadiga e recuperação.

Parte já existe no motor de combate (o Sangramento saiu no PONTE-2; o
`InjuryRules` aplica dano). O que o usuário quer é o outro lado: **a ficha** saber
desses estados fora do combate.

`controle_do_metabolismo` é a vantagem que puxa esse fio — dá +1/nível em testes
de HT de sangramento e de recuperação de doença e envenenamento.

**Recomendação honesta:** abrir documento próprio
(`docs/pendencias/Capitulo14_Lesoes_e_Fadiga.md`) com o mesmo método dos planos
de traços — inventário do capítulo, classificação do que já existe no motor, e só
então a fila. Encaixar isso aqui misturaria duas frentes.

⛔ **E aplicar o filtro de escopo já no inventário.** Boa parte do Capítulo 14 é
efeito de turno de combate (choque, atordoamento, knockback) e seria Saga pura.
O que interessa à aba Rolagem é o outro lado: **estados que duram** (cambaleante,
cansado, sangrando, doente, envenenado) e **recuperação** (cura natural, descanso,
tratamento). O documento tem que separar os dois antes de propor qualquer lote.

## 11.8 Já feitas, que estavam na lista do usuário

O usuário marcou como sugestão, sem saber se já existiam. Estas **já estão
prontas** — não reabrir:

| id | Onde |
|---|---|
| `camaleao_social` | declarado no REACAO-3 (+1 condicional) |
| `lamentavel` | declarado no REACAO-4 (+3 condicional) |
| `equilibrio_perfeito` | declarado no V-8 (+1 Acrobacia, Escalada, Pilotagem/NT) |
| `destreza_manual_elevada` | declarado, mas dá para melhorar — §11.5 |

## 11.9 Ordem sugerida

## ✅ FILA CUMPRIDA — 28 de Julho de 2026

Os oito lotes foram feitos em sequência. Todos entregam **na aba Rolagem**.

| # | Lote | Commit | Estado |
|---|---|---|---|
| 1 | **DX-BRACAL** | `07c73016` | ✅ ⏸ aguarda teste |
| 2 | **MARCOS-1** | `ff07c0d5` | ✅ ⏸ aguarda teste |
| 3 | **RESIST-1** | `21f78030` | ✅ ⏸ aguarda teste |
| 4 | **MAO-1** | `294118ef` | ✅ ⏸ aguarda teste |
| 5 | **V-9** | `7309e669` | ✅ ⏸ aguarda teste |
| 6 | **RESIST-2** | `3fc4e459` | ✅ ⏸ aguarda teste |
| 7 | **CAP14** | — | ✅ virou `docs/pendencias/Capitulo14_Lesoes_e_Fadiga.md` |
| 8 | **TETO-HT** | `9060397f` | ✅ ⏸ aguarda teste |

### Dois achados durante a execução

**A `FlexibilidadeRule` esquecia Arte Erótica.** O MB p.61 lista três perícias
(Escalada, Fuga, Arte Erótica) e a regra dava duas. Corrigido no `.kt` — como é
regra Kotlin, declarar no JSON seria ignorado em silêncio.

**🔴 Eu escrevi o id errado do Abascanto** no RESIST-1: `abascanto`, quando o
catálogo usa `abascanto_resistencia_a_magia`. Os onze testes daquele lote
passaram porque **inventavam o id** em vez de ler o catálogo — mesmo perfil do
bug do V-1. Um id errado não dá erro: simplesmente nunca casa, e a vantagem fica
sem efeito para sempre.

Criado o `IdsDeVantagemNoCatalogoTest`, que confronta **todo id usado em regra
Kotlin** com os catálogos reais. **Ao escrever regra nova que casa por id,
acrescente o id lá.**

## 11.10 ❌ DESCARTADO por escopo — o antigo lote 6

**"DANO-TESTE — Pânico e Inconsciência disparados por dano, dentro do combate."**

Eu havia proposto isto como a "metade cara" do §11.1: o motor de combate
dispararia os testes sozinho ao aplicar dano, em `domain/combat/InjuryRules`.

**É Saga pura** — só acontece com o combate tático aberto, e não aparece na aba
Rolagem. Pela regra de escopo do topo desta seção, **não fazer**.

E é redundante: o **MARCOS-1** já entrega o mesmo valor onde o usuário joga. Lá
o gatilho é a queda do PV **na própria ficha**, que funciona no Discord, no
papel, ou em qualquer mesa — sem depender do combate do app.

> Se um dia a Saga precisar disso, o `MarcosDeVidaRules` criado no MARCOS-1 já
> serve: é Kotlin puro, sem UI. O combate só precisaria chamá-lo. **Mas isso é
> trabalho da frente da Saga, não desta.**
