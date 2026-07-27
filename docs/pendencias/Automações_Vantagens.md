# Automações de Vantagens

> **Para o agente de IA.** Este arquivo é a fonte de verdade para automatizar as
> 272 vantagens de `assets/vantagens.v3.json`. Leia-o ANTES de implementar
> qualquer automação de vantagem. Ele descreve o mecanismo existente, os ganchos
> que faltam criar, e a especificação de cada vantagem.
>
> Criado em 2026-07-27. Escopo: só VANTAGENS. Desvantagens virão em arquivo
> irmão (`Automações_Desvantagens.md`) depois.

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
