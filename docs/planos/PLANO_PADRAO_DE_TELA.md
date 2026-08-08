# PLANO — Um padrão de tela para o app (começando por vantagens e desvantagens)

> ✅ **EXECUTADO** em 03/08/2026, versão **6.1-PADRAO** (versionCode 140).
> LAYOUT-1 ✅ · LAYOUT-1b ✅ · LAYOUT-2 ✅ · LAYOUT-3 ✅ · LAYOUT-4 ✅.
> Gate: 1.830 testes, 0 falhas nas duas variantes, lint limpo.
> Skill em `.claude/skills/padrao-de-tela/SKILL.md`.
> Roteiro de aparelho: blocos **T-PD, T-DL, T-TC, T-BT, T-PO, T-AA3**.
>
> **O que mudou em relação ao previsto:**
> - A lista de **poderes já na ficha** tem lápis e lixeira na linha e não cabia no
>   `AppSelectionRow`. Em vez de deixá-la fora, o componente ganhou o bloco
>   `acoes` — a exceção apareceu no mesmo dia em que o padrão nasceu.
> - Os diálogos de configurar × editar **não** viraram um composable único: o
>   miolo compartilhado (`TracoFormComuns.kt`) resolveu as quatro divergências sem
>   fundir dois arquivos de 700 e 380 linhas, que estourariam o teto de 1000.
> - O teste do LAYOUT-2 me obrigou a corrigir duas coisas nele mesmo: a regex
>   aninhada estourou a pilha, e o filtro de comentário acusava o KDoc que
>   **explica** a regra.

Pedido de 2026-08-03: *"os diálogos seguirem padrões… o mesmo tamanho de cards…
podemos criar um padrão de formatos, texturas, fontes, tamanho de textos? e
quando for criar ou modificar algum desses itens, você já ver o padrão?"*

Escopo desta rodada: **vantagens e desvantagens**. Perícias, técnicas, magias e
equipamentos ficam para depois — mas o padrão já nasce pronto para eles.

---

## 1. Diagnóstico — por que as telas divergiram

### 1.1 O padrão existe e quase ninguém usa

`ui/UiStandards.kt` já tem `UiTokens` (espaçamentos, padding de card, elevação) e
os componentes `AppListItemCard`, `StandardDialogColumn`, `appCardColors()`.

**Nenhum dos seis diálogos de seleção usa `AppListItemCard`.** Todos escrevem à
mão a mesma coisa:

```kotlin
Card(
    modifier = Modifier.fillMaxWidth().clickable { … },
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), …)
```

Seis cópias do mesmo bloco. Cada uma foi ajustada uma vez e nunca as outras — é
daí que vem a diferença de tamanho que você notou. E repare: o padding escrito à
mão é **8/6**, enquanto o `UiTokens` diz **12/10**. O padrão está no arquivo e a
tela está fora dele.

### 1.2 O que está diferente hoje, item por item

| | Vantagem | Desvantagem | Perícia | Técnica | Arma | Armadura |
|---|---|---|---|---|---|---|
| Cor do título | padrão | padrão | padrão | padrão | **primary** | **primary** |
| Contador | "275 vantagens encontradas" | **nenhum** | "281 perícias encontradas" | "118 técnicas encontradas" | **nenhum** | **"Resultados: 72"** |
| Filtros | `FilterChip` | `FilterChip` | **texto simples** | `FilterChip` | **texto simples** | **texto simples** |
| Linhas do card | 2 | 2 | 1 | 1 | 3–4 | 3 |
| Info secundária | abaixo | abaixo | **à direita** | **à direita** | abaixo | abaixo |
| Cores do corpo | `onSurfaceVariant` | `onSurfaceVariant` | `onSurfaceVariant` | `onSurfaceVariant` | **tertiary + primary** | **tudo primary** |

### 1.3 🔴 O caso do Abafador de Mana — adicionar × editar

Você mandou as duas telas. As diferenças são reais e cada uma tem uma causa:

**São dois arquivos diferentes**: `VantagemDialogs.kt` (adicionar, 699 linhas) e
`VantagemEditarDialog.kt` (editar, 383 linhas). Nasceram como cópia e seguiram
caminhos separados.

| O que você viu | Causa no código |
|---|---|
| No **adicionar** o Nível aparece só como `1`, sem `−` e `+` | Os botões só existem no ramo `if (isPraCegoVariant)`. Na variante **visual** o nível muda por **arrastar o dedo** (`detectVerticalDragGestures`) — um gesto sem nenhuma dica na tela |
| Rótulo do campo de texto muda | Três variantes no código: `"Descrição/Especializações"`, `"Descrição/Especialização"` e `"Descrição"` |
| *"Nenhum modificador aplicado."* só aparece ao adicionar | A linha existe só no `VantagemDialogs` |
| `Adicionar` × `Salvar` | Legítimo — é a única diferença que deve continuar |

### 1.4 🔴 E um bug de regra escondido na mesma tela

Ao **adicionar**, o teto do nível vem do catálogo:

```kotlin
TetoDeNivelDoTraco.de(definicao.id, definicao.max)     // VantagemDialogs
```

Ao **editar**, não:

```kotlin
TetoDeNivelDoTraco.de(vantagem.definicaoId, null)      // VantagemEditarDialog
```

**16 vantagens têm teto no catálogo** — Artífice, Curandeiro, Explorador e os
outros Talentos param em **4**; Espinhos em 3; Experiência-G em 10. Com `null`, a
edição cai no teto geral de **20**.

⚠️ É o **T-LI6 pela metade**: você validou o teto no seletor de adicionar, e a
edição ficou de fora. Quem já tem o Artífice na ficha ainda consegue levá-lo a 20
abrindo o lápis.

### 1.5 🔴 Os botões — o pedaço mais bagunçado

Medido no código de `ui/`:

| O que | Quanto |
|---|---|
| `TextButton` | **216** |
| `Button` puro | **72** |
| `IconButton` | **62** |
| `OutlinedButton` | **32** |
| `PrimaryActionButton` (o padrão do projeto) | **15** |
| `FilledTonalButton` / `ElevatedButton` | 4 e 4 |

São **sete** tipos de botão convivendo, e o componente que existe justamente para
padronizar é o **menos usado de todos**.

**Altura.** Escritas à mão: **32, 36, 42, 48, 50 e 56 dp**. Seis alturas para a
mesma coisa.

⚠️ E `UiTokens.TouchMinHeight = 48.dp` está declarado e é usado **zero vezes** —
o token do tamanho mínimo de toque existe e nunca foi aplicado. Os botões de
**32 e 36 dp** estão abaixo do mínimo de acessibilidade, num app que tem variante
para cegos.

**Espaçamento entre botões.** Nenhum lugar usa `spacedBy`: todos usam
`Arrangement.End` (24×), `Center` (6×) ou `SpaceBetween` (4×). O espaço entre
Cancelar e Salvar é o que o Material der — e varia com o tamanho do texto.

**Respiro interno.** Dez combinações diferentes de `contentPadding` escritas à
mão: `8/2`, `4/0`, `10/0`, `6/0`, `2/0`, `0/0`, `10/4`, só vertical…

**Tamanho do texto dentro do botão.** Seis estilos, incluindo `displaySmall` e
`headlineMedium` — tamanhos de manchete dentro de um botão.

**Cor.** Nove lugares forçam `ButtonDefaults.buttonColors(...)` na mão, fora do
tema.

### 1.6 Coisas menores, mas visíveis

- `definicao.tipoCusto.name.lowercase()` põe **`por_nivel`** na tela — nome de
  enum do código vazando para o jogador. Deveria ser *"por nível"*.
- A lista de **desvantagens não tem contador**, e a de armaduras chama de
  *"Resultados"* em vez de *"encontradas"*.
- Na lista de armaduras aparece **"Local: crnio"** — acento comido, e o
  `corrigirTextoQuebrado` do `TabEquipamentos` não pega esse caso.

---

## 2. Os lotes

### LAYOUT-1 — o padrão em código

`ui/UiStandards.kt` cresce (hoje 151 linhas) com os tokens que faltam, e ganha um
arquivo irmão `ui/AppSelectionUi.kt` com os componentes compartilhados:

**Tokens novos** (`UiTokens`):
- `TituloDialogo`, `SubtituloDialogo`, `NomeDoItem`, `DetalheDoItem` — os quatro
  estilos de texto, para ninguém mais escolher `titleMedium` no olho.
- `CardListaPaddingH/V` — um valor só, e o `8/6` escrito à mão morre.
- `ContadorPrefixo` — o formato do contador em um lugar.

**Componentes** (`AppSelectionUi.kt`, arquivo novo):

| Componente | O que resolve |
|---|---|
| `AppSelectionDialog` | cabeçalho, busca, chips, contador e botão Fechar — a moldura inteira, igual para todos |
| `AppSelectionRow` | a linha: nome à esquerda, detalhe abaixo **ou** à direita, um padding só |
| `AppFilterChips` | chips de verdade em todos, no lugar do texto simples de perícias/armas |
| `AppContador` | `"N vantagens encontradas"` com o plural certo |

⚠️ **Ninguém é obrigado a caber.** A ficha da arma tem 4 linhas e a magia tem o
aviso vermelho — o `AppSelectionRow` recebe um bloco livre para o extra, em vez de
proibir. Padrão que não acomoda a exceção vira gambiarra na primeira exceção.

### LAYOUT-1b — os botões

Arquivo irmão `ui/AppButtons.kt`, com **quatro papéis** em vez de sete tipos:

| Componente | Quando | Aparência |
|---|---|---|
| `AppBotaoPrincipal` | a ação que a tela existe para fazer (Adicionar, Salvar, Rolar) | preenchido, cor do tema |
| `AppBotaoSecundario` | alternativa legítima (Cancelar, Voltar) | contornado |
| `AppBotaoDiscreto` | ação de saída ou apoio (Fechar, Limpar filtros) | só texto |
| `AppBotaoIcone` | lápis, lixeira, `−`/`+` | ícone, com rótulo obrigatório |

**Os cinco números que passam a ser um só:**

| | Valor | Por quê |
|---|---|---|
| `BotaoAltura` | **48.dp** | é o `TouchMinHeight` que já existia e nunca foi usado; abaixo disso o dedo erra, e o app tem variante para cegos |
| `BotaoEspacamento` | **8.dp** | o espaço entre Cancelar e Salvar deixa de depender do tamanho do texto |
| `BotaoPaddingInterno` | **16/8** | um respiro só, no lugar das dez combinações à mão |
| `BotaoMargemDaBorda` | **12.dp** | a distância entre a fileira de botões e a borda do diálogo — hoje cada tela escolhe a sua |
| `BotaoTextoEstilo` | `labelLarge` | nada de `displaySmall` dentro de botão |

**Cor sai do tema, sempre.** As nove chamadas a `ButtonDefaults.buttonColors(...)`
na mão viram uma exceção só onde houver motivo escrito — ação destrutiva em
vermelho, por exemplo. Cor escolhida no olho quebra o modo escuro sem avisar.

`AppFileiraDeBotoes` monta a linha inteira: alinhamento à direita, o espaçamento
padrão entre eles e a margem da borda. Uma chamada, em vez de um `Row` diferente
em cada diálogo.

⚠️ **O `−`/`+` do nível é caso à parte.** Ele é um controle, não uma ação: fica no
`AppBotaoIcone` com altura de toque cheia e rótulo de acessibilidade — é o mesmo
componente que conserta o gesto invisível do LAYOUT-3.

⚠️ **Migração por lote, não de uma vez.** São 400+ botões. O LAYOUT-1b cria os
componentes e migra **só vantagens e desvantagens**; o resto entra na lista de
exceções com data, como os diálogos.

### LAYOUT-2 — a skill, para o padrão não ser esquecido

Foi a sua pergunta principal: *"quando for criar ou modificar, você já ver o
padrão?"*. Sim — vira uma **skill de projeto**:

```
.claude/skills/padrao-de-tela/SKILL.md
```

Conteúdo: qual componente usar para cada caso, os quatro estilos de texto e os
quatro papéis de botão com exemplo, os cinco números fixos (altura 48, espaço 8,
padding 16/8, margem 12, texto `labelLarge`), o que **nunca** fazer (`Card` cru
numa lista, `titleMedium` escolhido no olho, `enum.name` na tela, `ButtonColors`
na mão, altura de botão abaixo de 48) e a regra do diálogo de configurar × editar.

Ela dispara sozinha quando o trabalho toca `ui/`, então eu leio o padrão **antes**
de escrever a tela, não depois de você achar a diferença.

**Além da skill, um teste.** Skill é instrução, e instrução se esquece. O
`PadraoDeTelaTest` lê o código-fonte de `ui/` e reprova:
- `Card(` dentro de `LazyColumn` sem passar por `AppSelectionRow`;
- `padding(horizontal = 8.dp, vertical = 6.dp)` escrito à mão;
- `tipoCusto.name` chegando a um `Text`;
- botão com `.height(...)` **abaixo de 48.dp**;
- `ButtonDefaults.buttonColors(` fora da lista de exceções;
- `contentPadding = PaddingValues(` num botão que já podia usar o padrão.

*(É o mesmo tipo de teste de fiação que pegou a trava de pares: a regra pode estar
certa e a tela continuar errada.)*

### LAYOUT-3 — 🔴 um diálogo só para configurar e editar

`VantagemDialogs.ConfigurarVantagemDialog` e `VantagemEditarDialog` viram **um
composable** com um parâmetro `modo: Adicionar | Editar`. A diferença passa a ser
o que ela realmente é: o rótulo do botão.

Com isso, de graça:
- o `−` e o `+` aparecem **nas duas variantes** e nos dois modos (o arrastar
  continua, como atalho — mas deixa de ser a única forma);
- um rótulo só para o campo de texto;
- a linha *"Nenhum modificador aplicado."* nos dois;
- 🔴 **o teto do catálogo volta a valer na edição** — o composable único recebe a
  `VantagemDefinicao`, então não existe mais o ramo que passava `null`.

Mesma coisa do lado das desvantagens (`ConfigurarDesvantagemDialog` ×
`EditarDesvantagemDialog`).

⚠️ **Os diálogos de regra especial não entram na fusão.** Dor Crônica, Fobia,
Autocontrole e companhia têm corpo próprio em `TraitSpecialRuleComponents.kt`
(910 linhas) e continuam sendo chamados de dentro do composante unificado. Fundir
isso também seria trocar dois arquivos que divergem por um arquivo grande demais
para caber no teto de 1000 linhas.

### LAYOUT-4 — as listas de vantagem e desvantagem no padrão

As duas passam a usar `AppSelectionDialog` + `AppSelectionRow`. Junto:
- contador na desvantagem, que não tinha;
- `por_nivel` vira **"por nível"**, `fixo` vira **"custo fixo"**, `escolha` vira
  **"custo à escolha"**;
- o *"Atual: 0 pts"* da desvantagem vira o subtítulo padrão do cabeçalho.

---

## 3. Fora desta rodada

Perícias, técnicas, magias, poderes e equipamentos **não** são migrados agora —
você pediu vantagens e desvantagens primeiro. Mas o `AppSelectionDialog` já nasce
com o que eles precisam (detalhe à direita, linhas extras, aviso colorido), e a
migração de cada um vira um lote curto depois.

⚠️ Enquanto isso, o teste do LAYOUT-2 vai apontar esses arquivos como fora do
padrão. Eles entram numa **lista de exceções com data**, para a dívida ficar
visível em vez de esquecida.

---

## 4. Ordem e gate

1. **LAYOUT-1** (tokens e componentes de lista) — sem mudança visível ainda.
2. **LAYOUT-1b** (os quatro papéis de botão e os cinco números).
3. **LAYOUT-2** (skill + teste de padrão) — a rede que impede a volta.
4. **LAYOUT-3** (diálogo único) — é onde mora o bug do teto.
5. **LAYOUT-4** (as duas listas e seus botões).

Cada um: build nas 2 variantes, `===EXIT=0===`, lint limpo, `PROGRESS.md`
anexado, commit + push. LAYOUT-3 e LAYOUT-4 mexem em tela → **param para teste no
aparelho**, com bloco próprio no roteiro.
