# Revisão das Abas, Navegação e Crítica dos Planos de Automação

> **Para o agente de IA.** Dois assuntos que se cruzam:
> (1) crítica aos planos `Automações_Vantagens.md` / `Automações_Desvantagens.md`;
> (2) reorganização das abas.
>
> Estão juntos porque **a automação só é percebida pelo jogador se a tela
> mostrar o resultado** — automatizar um bônus que ninguém vê é trabalho perdido.
>
> Criado em 2026-07-27. Tudo aqui é PLANO — nada implementado.

---

# PARTE 1 — Crítica dos planos de automação

## C1. 🔴 Bônus manual vai DUPLICAR — SOLUÇÃO DEFINIDA: nota no bônus

`Personagem.kt:804-813` tem campos que o jogador preenche à mão:
```kotlin
var bonusManualEsquiva: Int = 0
var bonusManualApara: Int = 0
var bonusManualBloqueio: Int = 0
```

Quem tem *Reflexos em Combate* hoje quase certamente digitou `+1` ali, porque o
app não calculava. No dia em que a automação entrar, esse personagem fica com
**+2** — e sem nenhum aviso.

### ✅ Decisão do usuário (2026-07-27): o bônus manual vira NOTA

Em vez de o app adivinhar e perguntar "quer zerar?", **o próprio bônus manual
passa a carregar a explicação de por que existe**:

> Ao selecionar/alterar um bônus manual, abre um pop-up onde o jogador escreve
> uma breve descrição. A nota pode ser **editada** e **apagada**.

Por que isso é melhor que o diálogo de migração que eu tinha proposto:
- o app não precisa adivinhar a origem do número — quem sabe é o jogador;
- resolve o caso permanentemente, não só na migração: bônus manual de item,
  magia temporária ou decisão do Mestre passa a ser rastreável;
- casa com o princípio do C6 (todo número tem que dizer de onde veio).

**Comportamento:**
- bônus manual com valor ≠ 0 e **sem** nota ⇒ mostrar um indicador discreto
  convidando a anotar (não bloquear, não obrigar);
- a nota aparece junto do valor, no mesmo lugar em que o bônus é exibido;
- apagar a nota **não** zera o bônus, e vice-versa — são coisas separadas.

**Modelo de dados** (`Personagem.kt` — campos aditivos, fichas antigas
desserializam como `""`):
```kotlin
var notaBonusManualEsquiva: String = ""
var notaBonusManualApara: String = ""
var notaBonusManualBloqueio: String = ""
```

## C2. 🟠 A ordem dos lotes rende pouco

Conta honesta do plano de Vantagens: LOTE V-0 entrega **0** vantagens
automatizadas (só infraestrutura) e o V-1 entrega **~8 de 272 = 3%**.

Todo o resto depende do suporte a **bônus condicional**, empurrado para o LOTE
V-5. E condicional é a MAIORIA dos bônus do GURPS ("ao tentar parecer honesto",
"quando não quer ser visto", "se surpreender").

**Correção:** `condicao` e `escopo` precisam estar no DESENHO do V-0, mesmo sem
UI. O interpretador nasce sabendo que existem; a UI vem depois. Senão o V-0 é
reescrito no V-5.

## C3. 🟠 Adiei o mesmo problema duas vezes: escopo por membro

- `st_bracal` (+1 ST só dos braços) — adiado no plano de Vantagens
- `sem_um_dedo` (−1 DX daquela mão) — adiado no plano de Desvantagens

Adiar o mesmo problema em dois planos diferentes significa que ele **não é
exceção**: é uma dimensão que falta no formato. Resolver no desenho:
```json
{ "tipo": "atributo", "alvo": "DX", "valor": -1, "escopo": "mao_direita" }
```

## C4. 🟢 O caminho ponta a ponta JÁ FUNCIONA — conferido

`TraducaoFichaParaCombate.kt:60` faz `esquiva = p.defesasAtivas.calcularEsquiva(p)`
— e esse cálculo já passa pelo `TraitRuleRegistry`. Automação de defesa aparece
no combate sem trabalho extra.

**Confirmação do usuário sobre Mestre de Armas — verificado em 2026-07-27:**
o bônus de dano por dado percorre o caminho inteiro e chega nos dois destinos:

```
MestreDeArmasRule.getDamageBonusPerDie()
   └─> TraitRuleRegistry.getDamageBonusPerDie()      (Registry soma todas as vantagens)
        └─> Equipamento.danoCalculadoComSt()          (Personagem.kt:774)
             ├─> TraducaoFichaParaCombate.kt:92       ✅ chega no COMBATE
             └─> TabRolagem.kt:164                    ✅ chega na ROLAGEM
```

**Conclusão importante para os planos:** o padrão de automação está provado de
ponta a ponta para **defesa** e para **dano**. Não é teoria — há dois casos
funcionando. Copiar esse caminho em vez de inventar outro.

Ainda **não confirmado**: se o bônus de PERÍCIA (`getSkillModifiers`) chega ao
NH usado dentro do combate. Verificar antes do LOTE V-1.

## C5. 🟠 Os planos testam a camada errada

Regra do projeto: *"só acha bug no aparelho = testei a camada errada"*.

Os planos pedem teste unitário da regra. Isso prova que **a regra devolve +2**;
não prova que **o +2 chega na tela**. O caminho real é
`regra → Registry → Personagem.nhPericia → aba → combate`. Teste que cobre só o
primeiro elo passa verde com o app quebrado.

**Correção:** cada categoria precisa de UM teste de ponta a ponta — montar
personagem com a vantagem e afirmar o valor final que a tela mostraria.

## C6. ✅ RESOLVIDO: nota discreta no card da perícia

Escalada pula de 12 para 14 e nada explica o porquê.

### Decisão do usuário (2026-07-27)

> Se houver bônus em perícia vindo de vantagem ou desvantagem, **dentro do card
> da perícia** — tanto na **aba Perícias** quanto na **aba Rolagem** — uma nota
> **pequena e discreta** dizendo de onde veio o bônus.

**Especificação:**
- aparece **só quando há bônus** — perícia sem bônus não ganha linha nenhuma;
- texto curto, no padrão `+2 Pendulear` ou, com mais de uma origem,
  `+3 (Pendulear +2, Reflexos +1)`;
- estilo discreto: `labelSmall`, cor `outline` — o NH continua sendo o
  protagonista visual do card;
- **os dois lugares usam o MESMO componente** — não duplicar a montagem do texto
  em dois arquivos;
- inclui o bônus manual e sua nota (C1), quando houver;
- TalkBack: a origem entra na descrição do card, não como elemento separado
  (senão vira ruído na navegação por toque).

**Onde:** componente único em `ui/features/traits/` ou `ui/features/rolagem/`,
consumido pelo card de perícia das duas abas. Ver mapa na PARTE 3.

Modelo a copiar: `SentidoRules` + `DialogoSentidos`, que já exibem os
componentes nomeados do cálculo.

## C7. 🟡 O maior ganho talvez não seja a ficha, e sim a IA

Não está em plano nenhum. Com os efeitos **declarados em JSON**, o Narrador da
Saga passa a poder **ler** a mecânica em vez de adivinhar pela prosa:
sabe que o personagem tem *Fobia (Altura), NA 12*, provoca a situação, pede o
teste e reage ao resultado. O Forjador ganha o mesmo.

Isso provavelmente vale mais que o "+2" na ficha. **Incluir `efeitos` no contexto
enviado à IA** (`MestreIAContextFilter`) deve ser um lote próprio, logo após o V-0.

## C8. ✅ DECIDIDO: automatizar todas as previstas nos planos

Minha sugestão era começar só pelas mais usadas. **O usuário decidiu aplicar
todas** — não há fichas salvas suficientes para dizer quais aparecem mais, então
priorizar por uso seria chutar.

**Escopo confirmado:** todas as vantagens e desvantagens classificadas como
automatizáveis nos dois planos. As de categoria "narrativa pura"
(`Automações_Vantagens.md` §6 e `Automações_Desvantagens.md` CAT-D8) continuam
**fora** — não por prioridade, mas porque não têm efeito que o app possa aplicar.

---

# PARTE 2 — Abas e navegação

## D1. Situação atual (medida em 2026-07-27)

Barra inferior: ícone fixo do **Mestre IA** à esquerda (toque = chat, segurar =
voz) + as abas:

`Geral` · `Traços` · `Perícias` · `Técnicas` · `Magia`* · `Equip.` · `Rolagem` · `Saga`*

\* condicionais: Magia só aparece com Aptidão Mágica; Saga depende de flag.

## D2. 🔴 Código morto: três telas que não são renderizadas

| Arquivo | Tamanho | Referências fora dele |
|---|---|---|
| `ui/TabVtt.kt` | **103 KB / 2.270 linhas** | **nenhuma** |
| `ui/TabCombate.kt` | 34 KB | **nenhuma** |
| `ui/TabNotas.kt` | 1 KB | **nenhuma** |

O roteamento (`FichaScreen.kt:465-473`) não cita nenhuma das três. São ~138 KB
de UI morta. Há ainda o ícone `R.drawable.tab_defesas` referenciado no
`FichaCustomNavigationBar` para uma aba **"Defesas" que não existe** — vestígio
de uma organização anterior.

**Decisão necessária (é sua, não minha):** o `TabCombate` foi abandonado ou
ficou pelo caminho? Ele tem 34 KB de tela pronta de defesas, armas e RD por
local. Ou revive, ou some — mas não pode ficar como está.

## D3. ✅ A divisão das abas está CORRETA — eu li errado

Eu tinha apontado como problema "duas atividades misturadas". **O usuário
explicou a intenção do desenho e ela faz sentido:**

| Grupo | Abas | Para quê |
|---|---|---|
| **Criação da ficha** | Geral, Traços, Perícias, Técnicas, Magia, Equip. | montar o personagem |
| **Rolagem** | Rolagem | ⚠️ **foi feita para jogar via DISCORD** — as rolagens são usadas dentro do canal de voz/texto |
| **Saga** | Saga | jogo **solo**, em desenvolvimento |

**Fato que muda tudo:** *"futuramente o app se dividirá em 2 apps"*. Ou seja, a
separação que eu propunha (modos Ficha/Jogo) **já está planejada em outro nível**
— vai virar dois aplicativos, não dois modos.

### Consequência para os planos

❌ **NÃO reorganizar as abas.** Decisão do usuário: *"por enquanto não vejo
necessidade de modificar isso"*.

✅ O que continua valendo da minha crítica: a `TabRolagem` tem **1.128 linhas** e
estoura o teto de 1.000 (regra R1). Mas o motivo de dividir passa a ser
**manutenção**, não reorganização de navegação — e a divisão tem que **preservar
a aba como ela é hoje** para o usuário. É refatoração invisível.

Isso também facilita a futura separação em 2 apps: com a lógica de rolagem
isolada num delegate, mover a aba Rolagem para outro app fica mais barato.

## D4. Respostas do usuário às incoerências que apontei

**Técnicas continua aba própria — minha crítica estava errada.**
Explicação do usuário: diferente da Magia, que depende de **uma** vantagem
(Aptidão Mágica), as técnicas podem derivar de **muitas perícias** e às vezes
também exigem vantagem. Não há uma condição única que permita escondê-la.
❌ Não esconder, ❌ não fundir com Perícias.

**Notas / histórico / aparência: ✅ JÁ ESTAVA CORRETO — nada a fazer.**
> "deveriam estar presentes apenas na Aba Geral, e mais nenhuma outra, se houver
> resquícios pode retirar das outras abas"

Verificado em 2026-07-27 sobre toda a pasta `ui/`: os três campos são editados
**só** em `TabGeral.kt:335-352`. Não há resquício em aba nenhuma.

Meu levantamento anterior apontava a `TabRolagem` por engano — lá "historico" é
o log de ROLAGENS da sessão, não a história do personagem. Detalhes e a lição
sobre o termo ambíguo estão no LOTE UI-3 (PARTE 3).

**8 abas na barra:** fica como está — decorre da divisão explicada em D3 e será
resolvido pela futura separação em 2 apps.

## D5. ✅ DECISÕES DO USUÁRIO (2026-07-27) — fecham a PARTE 2

| # | Decisão | Status |
|---|---|---|
| **D5.1** | **Apagar os órfãos** (`TabVtt`, `TabCombate`, `TabNotas`) — desde que não gere bug. **Fazer duplo check antes** | ✅ APROVADO com verificação |
| **D5.2** | **NÃO** fundir Técnicas com Perícias | ❌ REJEITADO |
| **D5.3** | **NÃO** esconder nenhuma aba | ❌ REJEITADO |
| **D3** | **NÃO** reorganizar as abas (2 apps no futuro) | ❌ REJEITADO |
| **D4** | Notas/histórico/aparência **só na aba Geral** | ✅ APROVADO |

**O `TabCombate.kt` (34 KB) vai ser apagado**, não revivido. Fica registrado
aqui que ele existiu, com tela de defesas/armas/RD por local — se um dia fizer
falta, o git tem o histórico.

### Protocolo do duplo check antes de apagar (D5.1)

Executar as DUAS verificações e só apagar se ambas voltarem vazias:

```bash
# 1) referências no código-fonte (Kotlin)
grep -rn "TabVtt\|TabCombate\|TabNotas" app/src --include=*.kt | grep -v "/TabVtt.kt\|/TabCombate.kt\|/TabNotas.kt"

# 2) referências fora do Kotlin (XML, navegação, testes, proguard)
grep -rn "TabVtt\|TabCombate\|TabNotas" app/src --include=*.xml --include=*.pro --include=*.kts
```

Depois de apagar: **build nas 2 variantes + suíte de testes completa** antes de
considerar o lote fechado. Se o build quebrar, o arquivo não era órfão.

⚠️ Apagar também o `"Defesas" -> R.drawable.tab_defesas` do
`FichaCustomNavigationBar.kt` (aba que não existe) e o drawable, se ninguém mais
o usar.

## D6. ✅ Onde cada automação aparece — DEFINIDO

| O que a automação produz | Onde aparece | Decisão |
|---|---|---|
| Bônus de perícia | card da perícia, na **aba Perícias E na aba Rolagem**, com nota discreta de origem | usuário, C6 |
| Bônus de defesa | onde as defesas já estão hoje (Rolagem) | mantido |
| Bônus manual + sua nota | junto do valor, com pop-up de editar/apagar | usuário, C1 |
| **Autocontrole (35 desvantagens)** | **no FIM da aba Rolagem**, e **só se a ficha tiver desvantagem com NA**. Sem nenhuma ⇒ **não aparece nada** | usuário, D6 |

A regra do autocontrole ("só aparece se houver") é a mesma lógica já usada pela
aba Magia, que só existe com Aptidão Mágica. Consistente com o app.

**A `TabRolagem` continua sendo o destino de quase tudo** — e já tem 1.128
linhas. Dividi-la deixa de ser opcional: sem isso, cada automação a engorda mais.
Mas a divisão é **invisível para o usuário** (D3): a aba continua idêntica na
tela, só o código muda de lugar.

---

# PARTE 3 — Onde cada melhoria age (mapa de arquivos)

> Respeita as regras R1 (teto de 1.000 linhas) e R2 (mapa de pastas) de
> `Automações_Vantagens.md` §0.5. **CRIAR** = arquivo/pasta que ainda não existe.

## LOTE M-1 — Nota no bônus manual (decisão C1)

Deixou de ser "migração" e virou **funcionalidade permanente**: todo bônus
manual pode carregar uma nota explicando de onde veio.

| Onde | O que muda |
|---|---|
| `model/Personagem.kt` (1.164 linhas ⚠️) | **+3 linhas**: `notaBonusManualEsquiva/Apara/Bloqueio: String = ""`. Campos aditivos — ficha antiga desserializa como `""`. Nada além disso aqui (R1) |
| `ui/features/traits/NotaBonusManualDialog.kt` **CRIAR** | pop-up de escrever/editar/apagar a nota |
| onde o bônus manual é editado hoje (`FichaCombatDelegate` + a tela que o expõe) | ponto de entrada do pop-up + indicador discreto quando há bônus sem nota |
| `app/src/test/java/com/gurps/ficha/model/NotaBonusManualTest.kt` **CRIAR** | ficha antiga sem o campo carrega OK; apagar nota não zera o bônus; zerar bônus não apaga a nota |

⚠️ Localizar antes onde o bônus manual é editado — não está mapeado ainda.
Provavelmente na aba Rolagem (onde as defesas aparecem).

## LOTE UI-1 — ✅ CONCLUÍDO em 2026-07-27

**Executado.** Resultado real:

| Ação | Arquivo | Resultado |
|---|---|---|
| migrado | `mensagemBloqueioPendente` → `domain/rules/MensagensDefesa.kt` | vira `MensagensDefesa.bloqueioPendente` |
| migrado | teste → `test/.../domain/rules/MensagensDefesaTest.kt` | 4 testes preservados |
| apagado | `ui/TabVtt.kt` | −103 KB |
| apagado | `ui/TabCombate.kt` | −34 KB |
| apagado | `ui/TabNotas.kt` | −1 KB |
| apagado | `test/.../ui/TabCombateStateTest.kt` | substituído pelo novo |
| apagado | `res/drawable/tab_defesas.png` | ícone de aba inexistente |
| editado | `FichaCustomNavigationBar.kt` | −1 linha (`"Defesas" ->`) |

**Gate:** compilação nas 2 variantes ✅ · suíte **1.000 testes, 0 falhas** ✅ ·
`assembleVisualDebug` (valida remoção do recurso) ✅

Backup dos arquivos apagados: scratchpad da sessão + histórico do git.

⚠️ `MensagensDefesa.bloqueioPendente` ficou **sem chamador em produção** — o
único era a tela removida. Está pronto para a aba Rolagem usar quando o aviso
de "por que não posso bloquear" for adicionado lá.

### Especificação original (mantida para referência)

| Onde | Ação |
|---|---|
| `ui/TabVtt.kt` (103 KB / 2.270 linhas) | **APAGAR** — duplo check limpo ✅ |
| `ui/TabNotas.kt` (1 KB) | **APAGAR** — duplo check limpo ✅. A função dele (notas) fica na `TabGeral` (D4) |
| `ui/TabCombate.kt` (34 KB) | ⚠️ **APAGAR SÓ DEPOIS de salvar `mensagemBloqueioPendente`** — ver duplo check abaixo |
| `domain/rules/` | recebe a função salva do `TabCombate` |
| `app/src/test/.../ui/TabCombateStateTest.kt` | ajustar import + renomear |
| `ui/components/FichaCustomNavigationBar.kt` | remover a linha `"Defesas" -> R.drawable.tab_defesas` |
| `res/drawable/tab_defesas.*` | apagar **se** nenhum outro ponto usar |

### ⚠️ DUPLO CHECK JÁ EXECUTADO (2026-07-27) — e achou um problema

| Arquivo | XML/gradle | Kotlin | Veredito |
|---|---|---|---|
| `TabVtt.kt` | limpo | limpo | ✅ **pode apagar** |
| `TabNotas.kt` | limpo | limpo | ✅ **pode apagar** |
| `TabCombate.kt` | limpo | ⚠️ **NÃO está limpo** | ❌ **NÃO apagar direto** |

**O que foi encontrado:** `TabCombate.kt:45` define
```kotlin
internal fun mensagemBloqueioPendente(temPericiaEscudo: Boolean, temEscudoEquipado: Boolean): String
```
e existe um teste ATIVO que a usa:
`app/src/test/java/com/gurps/ficha/ui/TabCombateStateTest.kt`.

Ou seja: a TELA é morta, mas o arquivo carrega **uma função de regra viva e
testada** — a mensagem que orienta o jogador quando não dá para bloquear
("adicione perícia de Escudo… e equipe ao menos um escudo").

**Procedimento correto para o LOTE UI-1:**
1. **mover** `mensagemBloqueioPendente` para um arquivo de regra —
   sugestão: `domain/rules/CombatRules.kt` (já existe e trata defesas) ou
   `domain/rules/MensagensDefesa.kt` **CRIAR** se o `CombatRules` ficar grande;
2. atualizar o `import` do `TabCombateStateTest.kt` (e renomeá-lo, já que
   `TabCombate` não existirá mais);
3. **só então** apagar `TabCombate.kt`;
4. build nas 2 variantes + suíte completa.

Isto é exatamente o que o duplo check existe para pegar. Sem ele, o lote teria
quebrado a suíte de testes.

**Depois de apagar:** build nas 2 variantes + suíte completa. Se quebrar, o
arquivo não era órfão — reverter e investigar.

## LOTE UI-3 — ❌ CANCELADO: não havia nada a fazer (2026-07-27)

**O problema não existia. Eu diagnostiquei errado.**

Meu levantamento original rodou `grep "historico\|aparencia"` sobre as abas e
achou os dois termos em `TabGeral.kt` **e** `TabRolagem.kt` — daí a conclusão de
que estavam "espalhados". Era **falso positivo**: na Rolagem, todas as
ocorrências de "historico" são de `HistoricoRolagemItem` / `HistoricoRolagemPanel`
— o **log de rolagens da sessão**, que não tem relação com o campo `historico`
(a história de vida do personagem).

Verificação feita sobre TODA a pasta `ui/`, não só as abas:

```bash
grep -rn "atualizarHistorico\|atualizarAparencia\|atualizarNotas\|p\.aparencia\|p\.historico\b" \
    app/src/main/java/com/gurps/ficha/ui --include=*.kt | grep -v historicoLog
```
Resultado: **5 ocorrências, todas em `TabGeral.kt:335-352`**.

✅ **Notas, histórico e aparência já estão exclusivamente na aba Geral** — que é
exatamente o que a decisão D4 pedia. Nada a remover, nada a mover.

⚠️ **Lição para os próximos lotes deste plano:** `historico` é um termo
ambíguo no projeto. Existem três coisas diferentes:
| Termo | O que é | Onde |
|---|---|---|
| `personagem.historico` | história de vida (texto livre) | `TabGeral` |
| `HistoricoRolagemItem` | log de rolagens da sessão | `TabRolagem` |
| `historicoLog` / `FichaHistoryDelegate` | histórico de alterações da ficha | delegate próprio |

Antes de agir sobre "histórico", conferir **qual** dos três.

## LOTE UI-2 — ✅ CONCLUÍDO em 2026-07-27 (parcial, por decisão)

`TabRolagem.kt`: **1.128 → 990 linhas.** Abaixo do teto de 1.000.

| Extraído | Para | Linhas |
|---|---|---|
| overlay dos dados 3D + textos de resultado | `ui/features/rolagem/OverlayDados3D.kt` | −103 |
| painel de modificador global (PraCego) | `ui/features/rolagem/PainelModificadorGlobal.kt` | −35 |

**Ganho inesperado:** o cálculo do texto de resultado ("Sucesso por 3", "Falha
Crítica", "Dano: 9") estava **dentro do composable** e era intestável — só dava
para conferir rolando dados no aparelho. Virou função pura
(`textoDoResultado` / `anuncioDoResultado`) e ganhou **14 testes**
(`TextoDoResultadoTest`), incluindo o piso de dano em 1 e o fato de o
modificador global só valer na variante PraCego.

**Gate:** 2 variantes ✅ · **1.014 testes, 0 falhas** ✅ · `assembleVisualDebug` ✅

### O que NÃO foi feito, e por quê

O plano original previa também `FichaRolagemDelegate` + `PainelAtributos` +
`PainelCombate` + `PainelListas`. **Não foram feitos de propósito:**

- a maior parte do layout **já estava extraída** em `ui/features/rolagem/`
  (`AtributosQuickRollPanel`, `DefesasAtivasQuickRollPanel`,
  `AtaqueDanoQuickArea`, `HistoricoRolagemPanel`, ~30 diálogos). O que restava
  inline eram invólucros que precisariam de 14+ parâmetros para sair —
  trocaria 60 linhas de layout por 20 de assinatura, sem ganho real;
- mover as 12 funções de lógica para um delegate exigiria mover junto ~30
  variáveis de estado que elas capturam. Isso muda o ciclo de vida do estado
  (passaria a sobreviver a rotação de tela) — **mudança de comportamento** num
  lote que devia ser invisível.

O objetivo do lote (sair do teto + abrir espaço para as automações) foi
atingido. O delegate continua disponível se um dia o arquivo voltar a crescer.

⚠️ **Margem apertada:** 990/1.000. O `PainelAutocontrole` do LOTE D-1 entra com
1 linha (a chamada), mas qualquer coisa maior que isso exige extrair outro bloco
antes. Candidato natural: o invólucro "Atributos e Status" (linhas 556-614).

### Especificação original (mantida para referência)

**Este é o lote que destrava a automação**, porque tudo desemboca nela (§D6).

A divisão correta é **por responsabilidade**, não por tamanho. Hoje o arquivo
mistura duas coisas:

**(a) LÓGICA de rolagem** — linhas 290-503, 12 funções:
`registrarResultado`, `dispararTabelaCritica`, `executarRolagem`,
`finalizarRolagem`, `executarRolagemDano`, `finalizarRolagemDano`,
`finalizarRolagemPersonalizada`, `executarRolagemPersonalizada`,
`consumirEnergiaMagia`, `tratarCustoEnergiaAposRolagemMagia`,
`ajustarPvRolagemPorSwipe`, `ajustarPfRolagemPorSwipe`.

**(b) LAYOUT** — seções "Atributos e Status" (556), "Modificador Global" (638),
"Combate: Ataque e Dano" (682), Esquiva, Perícias, Magias, Rolagem Livre,
Histórico.

| Destino | O que vai |
|---|---|
| `viewmodel/delegates/FichaRolagemDelegate.kt` **CRIAR** | toda a lógica (a). É estado + coordenação → é papel de delegate, igual aos outros 14 |
| `ui/features/rolagem/PainelAtributos.kt` **CRIAR** | seção Atributos/PV/PF + swipe |
| `ui/features/rolagem/PainelCombate.kt` **CRIAR** | Esquiva + ataques + dano |
| `ui/features/rolagem/PainelListas.kt` **CRIAR** | Perícias, Magias, Rolagem Livre |
| `ui/TabRolagem.kt` (fica) | só monta os painéis. Alvo: **< 200 linhas** |

Os painéis vão em `ui/features/rolagem/` porque a pasta já existe e já tem
`RolagemComponents.kt`, `RolagemModels.kt`, `DialogoSentidos.kt`.

⚠️ Lote grande e 100% UI ⇒ **PARA para teste no aparelho**. Não juntar com
nenhuma automação.

## LOTE V-0 — Interpretador de efeitos (com C2 e C3 corrigidos)

| Onde | O que |
|---|---|
| `domain/rules/traits/TraitEffectModels.kt` **CRIAR** | `EfeitoDeclarado` (com `condicao` **e** `escopo` desde já), `enum TipoEfeito`, `enum Atributo`, `enum EscopoEfeito` |
| `domain/rules/traits/EfeitoInterpretador.kt` **CRIAR** | lê `efeitos` da definição e devolve `TraitRule` genérico |
| `domain/rules/traits/TraitRuleRegistry.kt` (existente, ~100 linhas) | o resolvedor de 2 linhas (Kotlin vence JSON) |
| `model/Personagem.kt` (1.164 linhas ⚠️) | **+1 linha**: campo `efeitos` em `VantagemDefinicao` |
| `assets/vantagens.v3.json` | nenhuma vantagem ainda — só o schema aceito |
| `scripts/validar_efeitos.py` **CRIAR** | falha o build se `alvo` não existir em `pericias.json` |
| `app/src/test/java/com/gurps/ficha/domain/rules/traits/` **CRIAR PASTA** | os 7 testes de `Automações_Vantagens.md` §10.5 |

## LOTE IA-1 — Efeitos no contexto da IA (crítica C7)

| Onde | O que |
|---|---|
| `domain/MestreIAContextFilter.kt` (existente) | incluir os `efeitos` declarados das vantagens/desvantagens do personagem na string enviada à IA |
| `domain/saga/NarradorTools.kt` (existente) | avaliar se o Narrador ganha uma tool para consultar o efeito mecânico de um traço |
| `app/src/test/java/com/gurps/ficha/domain/MestreIAContextFilterTest.kt` (existente) | o contexto passa a citar o efeito |

**Cuidado:** o contexto da IA tem limite de tamanho. Mandar 272 efeitos estoura.
Enviar **só os traços que o personagem tem** — nunca o catálogo inteiro.

## LOTE D-0 — Registry enxergar desvantagens

| Onde | O que |
|---|---|
| `domain/rules/traits/TracoSelecionado.kt` **CRIAR** | interface comum |
| `model/Personagem.kt` (⚠️) | **2 linhas**: `VantagemSelecionada` e `DesvantagemSelecionada` passam a implementar a interface |
| `domain/rules/traits/TraitRule.kt` (existente) | assinatura passa a receber `TracoSelecionado` |
| as 11 regras em `domain/rules/traits/` | ajuste de assinatura (mecânico) |
| `domain/rules/traits/TraitRuleRegistry.kt` | agregadores varrem `vantagens + desvantagens` |

## LOTE D-1 — Autocontrole (35 desvantagens)

| Onde | O que |
|---|---|
| `domain/rules/AutocontroleRules.kt` **CRIAR** | Kotlin puro: lista desvantagens com NA + componentes da "notinha" |
| `ui/features/rolagem/PainelAutocontrole.kt` **CRIAR** | a seção que vai no **FIM da aba Rolagem**. Renderiza **nada** se a ficha não tiver desvantagem com NA (decisão D6) |
| `ui/features/rolagem/DialogoAutocontrole.kt` **CRIAR** | o diálogo de rolar, ao lado do `DialogoSentidos.kt` |
| `ui/TabRolagem.kt` | **+1 linha** no fim: `PainelAutocontrole(viewModel)` |
| `app/src/test/java/com/gurps/ficha/domain/rules/AutocontroleRulesTest.kt` **CRIAR** | os 5 testes de `Automações_Desvantagens.md` §2.5 |

⚠️ Depende do UI-2: sem ele, o ponto de entrada cairia na `TabRolagem` inchada.

## LOTE NOTA-1 — A nota de origem no card da perícia (decisão C6)

| Onde | O que |
|---|---|
| `domain/rules/traits/TraitEffectModels.kt` (criado no V-0) | `EfeitoDeclarado` carrega a origem (`"Pendulear"`) além do valor |
| `domain/rules/traits/TraitRuleRegistry.kt` | agregador novo que devolve a **lista de origens**, não só o total somado |
| `ui/features/traits/OrigemDoBonus.kt` **CRIAR** | **componente ÚNICO** que monta e exibe o texto (`+3 (Pendulear +2, Reflexos +1)`), em `labelSmall`/`outline` |
| `ui/TabPericias.kt` | usa o componente no card da perícia |
| `ui/features/rolagem/PainelListas.kt` (criado no UI-2) | usa **o mesmo** componente no card da perícia da Rolagem |

⚠️ **Um componente só, dois consumidores.** Se o texto for montado em dois
lugares, eles divergem na primeira mudança de regra.

Modelo a copiar: `SentidoRules` + `DialogoSentidos`, que já exibem os
componentes nomeados do cálculo.

## Resumo: arquivos novos por pasta

### Arquivos NOVOS

```
domain/rules/
   AutocontroleRules.kt          (D-1)
domain/rules/traits/
   TraitEffectModels.kt          (V-0)
   EfeitoInterpretador.kt        (V-0)
   TracoSelecionado.kt           (D-0)
viewmodel/delegates/
   FichaRolagemDelegate.kt       (UI-2)
ui/features/traits/
   NotaBonusManualDialog.kt      (M-1)
   OrigemDoBonus.kt              (NOTA-1)
ui/features/rolagem/
   PainelAtributos.kt            (UI-2)
   PainelCombate.kt              (UI-2)
   PainelListas.kt               (UI-2)
   PainelAutocontrole.kt         (D-1)
   DialogoAutocontrole.kt        (D-1)
scripts/
   validar_efeitos.py            (V-0)
app/src/test/java/com/gurps/ficha/domain/rules/traits/   (PASTA NOVA)
```

**Nenhum arquivo solto.** Todas as pastas já existem, exceto a de teste.

### Arquivos APAGADOS (lote UI-1)

```
ui/TabVtt.kt          -103 KB / -2.270 linhas
ui/TabCombate.kt       -34 KB
ui/TabNotas.kt          -1 KB
res/drawable/tab_defesas.*   (se ninguém mais usar)
```

Saldo: o projeto **encolhe** ~138 KB e ganha 13 arquivos pequenos e focados.

---

# PARTE 4 — Ordem final (todas as decisões tomadas)

| # | Lote | Toca UI? | Por quê nesta posição |
|---|---|---|---|
| 1 | ✅ **UI-1** — apagar os 3 órfãos | não | **CONCLUÍDO 2026-07-27.** −138 KB, 1.000 testes verdes |
| 2 | ❌ **UI-3** — notas/histórico/aparência só na Geral | — | **CANCELADO** — já estava correto; meu diagnóstico era falso positivo |
| 3 | ✅ **UI-2** — dividir a `TabRolagem` | **sim** | **CONCLUÍDO 2026-07-27.** 1.128 → 990 linhas, +14 testes. ⏸ **AGUARDA TESTE NO APARELHO** |
| 4 | **M-1** — nota no bônus manual | **sim** | ⏭ **PRÓXIMO** (após o teste do UI-2). Resolve o risco da duplicação de forma definitiva |
| 5 | **V-0** — interpretador de efeitos, com `condicao` e `escopo` no desenho | não | o trilho de tudo (C2, C3) |
| 6 | **NOTA-1** — origem do bônus no card da perícia | **sim** | decisão C6. Depende do V-0 (a origem vem do efeito declarado) |
| 7 | **V-1** — declarar em JSON as vantagens simples | não | só dados |
| 8 | **IA-1** — `efeitos` no contexto da IA | não | provável maior retorno (C7) |
| 9 | **D-0** — Registry enxergar desvantagens | não | bloqueia todas as desvantagens |
| 10 | **D-1** — autocontrole no fim da Rolagem | **sim** | maior valor percebido no jogo. Só aparece se houver desvantagem com NA (D6) |
| 11+ | ver a LISTA COMPLETA abaixo | | |

**Abas:** ❌ não serão reorganizadas (decisão D3/D5.2/D5.3). O que muda é só o
que está por baixo.

---

## Lista completa dos lotes (fonte única — consolida os 3 planos)

Substitui as ordens parciais de `Automações_Vantagens.md` §7 e
`Automações_Desvantagens.md` §6. **Toda ordem que aparecer nos outros dois
arquivos é subordinada a esta.**

### FASE 0 — Limpeza ✅ CONCLUÍDA (27/07/2026)

| # | Lote | Status |
|---|---|---|
| 0.1 | **UI-1** — apagar as 3 telas mortas + salvar `MensagensDefesa` | ✅ feito |
| 0.2 | **UI-3** — notas/histórico só na Geral | ❌ cancelado (já estava certo) |
| 0.3 | **UI-2** — dividir `TabRolagem` (1.128 → 990) | ✅ feito e validado no aparelho |

### FASE 1 — Trilhos (nada visível ainda; destrava todo o resto)

| # | Lote | UI? | Depende de | O que entrega |
|---|---|---|---|---|
| 1.1 | ✅ **M-1** — nota no bônus manual | 🖐 | — | **FEITO 27/07** (e77029ac). ⏸ aguarda teste no aparelho |
| 1.2 | ✅ **V-0** — interpretador do campo `efeitos` | — | — | **FEITO 27/07** (c9686caa). 16 testes + validador |
| 1.3 | ✅ **D-0** — Registry enxergar desvantagens | — | — | **FEITO 27/07** (ec719e66). Interface `TracoSelecionado`, 13 arquivos migrados, 8 testes |

### FASE 2 — Primeiras automações de verdade

| # | Lote | UI? | Depende de | O que entrega |
|---|---|---|---|---|
| 2.1 | ✅ **V-1** — vantagens simples em JSON | — | 1.2 | **FEITO 27/07** (caf75184). 5 vantagens, 16 efeitos, zero Kotlin. ⏸ aguarda teste no aparelho |
| 2.2 | **D-2** — desvantagens simples em JSON | — | 1.2 + 1.3 | Gordo, Acima do Peso… mesmo interpretador |
| 2.3 | **NOTA-1** — origem do bônus no card da perícia | 🖐 | 1.2 | nota discreta nas abas Perícias e Rolagem (componente único) |
| 2.4 | **D-1** — autocontrole no fim da Rolagem | 🖐 | 1.3 | **35 desvantagens.** Maior valor percebido no jogo |
| 2.5 | **IA-1** — `efeitos` no contexto da IA | — | 2.1 | o Narrador passa a LER a mecânica em vez de adivinhar pela prosa |

### FASE 3 — Ganchos novos (o contrato do `TraitRule` cresce)

| # | Lote | UI? | Depende de | O que entrega |
|---|---|---|---|---|
| 3.1 | **V-2** — `reflexos_em_combate` | — | — | +1 nas 3 defesas + Sacar Rápido + Pânico. Gancho existente |
| 3.2 | **V-3** — GANCHO-A (bônus de atributo) | — | — | `AtributoBonusRules.kt`; `Personagem.kt` só ganha 1 linha por propriedade. Teste de recursão obrigatório |
| 3.3 | **V-4** — vantagens de atributo + **escopo por membro** | — | 3.2 | ST Braçal, DX Braçal, Crescimento. Resolve o `escopo` adiado duas vezes |
| 3.4 | **D-3** — bug da dupla aplicação de custo | — | — | `CharacterRules.kt:290`, já documentado no código. É custo, não efeito |

### FASE 4 — Avançado

| # | Lote | UI? | Depende de | O que entrega |
|---|---|---|---|---|
| 4.1 | **V-5** — bônus condicional | 🖐 | 1.2 | "aplicar +1 de Rosto Sincero?" na hora de rolar. **Destrava a maioria dos bônus do GURPS** |
| 4.2 | **REACAO-1** — GANCHO-D + UI de Teste de Reação | 🖐 | — | serve vantagens (Aparência, Carisma) E desvantagens (~23) — UI **compartilhada**, não duplicar |
| 4.3 | **V-6** — GANCHO-B (RD natural) | — | — | consumo em `domain/combat/subsistemas/` |
| 4.4 | **V-7 / D-5** — GANCHO-C (deslocamento) | — | — | Voo, Natação, Anfíbio + as 3 desvantagens de deslocamento |
| 4.5 | **D-6** — vulnerabilidade a dano | — | — | pertence ao `InjuryRules`, não ao `TraitRule` |
| 4.6 | **NOTA-2** — nota de bônus em ARMAS, PERÍCIAS e ITENS | 🖐 | 1.1 | pedido do usuário em 27/07 ao validar o M-1 — ver detalhe abaixo |

### Lote NOTA-2 — estender a nota de bônus (pedido em 27/07/2026)

O M-1 resolveu o bônus manual das **defesas** (Esquiva/Apara/Bloqueio). O
usuário validou no aparelho e pediu o mesmo tratamento nos outros lugares onde
existe número digitado à mão:

| Onde | O que precisa investigar antes |
|---|---|
| **Armas** | há bônus/ajuste manual de dano ou de NH por arma? Onde é editado (`TabEquipamentos` ou o diálogo de arma)? |
| **Perícias** | existe campo de bônus manual por perícia hoje, ou só pontos gastos? Se não existe, o lote pode ser *criar* o campo junto com a nota |
| **Itens/Equipamento** | peso, custo e RD podem ter ajuste manual — conferir quais |

**Reaproveitar:** `ui/features/rolagem/CampoNotaBonus.kt` já existe e é genérico
(recebe `nota` + `onNotaChange`). Não criar componente novo — só mover para uma
pasta mais neutra se passar a servir abas fora da Rolagem.

**Padrão a seguir (do M-1):**
- campos de nota **aditivos** no modelo, default `""` (ficha antiga não quebra);
- estender método existente com parâmetro de default em vez de criar método
  novo — foi assim que o `FichaViewModel.kt` não cresceu nenhuma linha;
- nota e valor **independentes**: apagar um não mexe no outro;
- teste cobrindo essa independência.

⚠️ Antes de começar, **medir** os arquivos alvo (regra R3): `TabEquipamentos.kt`
e `TabPericias.kt` ainda não foram medidos neste plano.

**Relação com o NOTA-1 (2.3):** são complementares. O NOTA-1 mostra a origem de
bônus **automáticos** (vindos de vantagem); o NOTA-2 cobre os **digitados à
mão**. Juntos, todo número da ficha passa a dizer de onde veio.

### Resumo

- **21 lotes**, 7 concluídos, 1 cancelado → **13 pendentes**
- FASE 0 e FASE 1 fechadas (27/07/2026). Próximo: **2.1 (V-1)** — declarar as vantagens simples em JSON
- 🖐 = toca UI ⇒ **PARA para teste no aparelho**: M-1, NOTA-1, D-1, V-5, REACAO-1 (**5 paradas**)
- Caminho crítico: **1.2 (V-0)** destrava 2.1, 2.2, 2.3 e 4.1 · **1.3 (D-0)** destrava tudo de desvantagem
- Se o objetivo for "ver automação funcionando o quanto antes": **1.2 → 2.1** (duas etapas)
- Se for "maior valor de jogo": **1.3 → 2.4** (autocontrole, 35 desvantagens)

---

# Decisões registradas (nada em aberto)

| Assunto | Decisão |
|---|---|
| Bônus manual (C1) | vira **nota editável/apagável** via pop-up, não diálogo de migração |
| Mestre de Armas (C4) | ✅ verificado — funciona ponta a ponta (combate + rolagem) |
| Origem do bônus (C6) | nota discreta no card da perícia, **nas abas Perícias e Rolagem** |
| Escopo (C8) | automatizar **todas** as previstas nos planos |
| Órfãos (D2/D5.1) | **apagar os três**, com duplo check |
| `TabCombate.kt` | **apagar** — não revive |
| Estrutura das abas (D3) | **não mexer** — app vai se dividir em 2 no futuro |
| Técnicas (D4) | **continua aba própria** — deriva de muitas perícias, não dá para condicionar |
| Notas/histórico/aparência (D4) | **só na aba Geral**; remover resquícios da Rolagem |
| Fundir Técnicas+Perícias (D5.2) | ❌ não |
| Esconder abas (D5.3) | ❌ não |
| Autocontrole (D6) | **fim da aba Rolagem**, só se houver desvantagem com NA |
