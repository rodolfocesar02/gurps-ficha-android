# PLANO — Card de detalhe das armas (fogo, distância e corpo a corpo)

> ✅ **EXECUTADO** em 03/08/2026, versão **5.9-ARMAS** (versionCode 138).
> ARMA-1 ✅ · ARMA-2 ✅ · ARMA-3 ✅ · ARMA-4 ✅ · ARMA-5 ✅.
> Gate: 1.783 testes, 0 falhas nas duas variantes, lint limpo.
> Roteiro de aparelho: `docs/pendencias/Roteiro_Teste_Aparelho_AUTOM.md`
> (blocos T-AR, T-AF, T-AD, T-AC, T-AI, T-MI, T-CF, T-AA).
>
> **Achados extras durante a execução**, que não estavam neste plano:
> - `"2.900".toIntOrNull()` = null → **57 de 124** alcances viravam
>   "desconhecido", e o aviso *"Fora de alcance"* nunca disparava para pistola.
> - Uma linha do catálogo tem os dois danos **colados sem barra**
>   (`"GeB+2 corteGdP+3 perf"`) — daí serem **29** e não 28 as armas com mais de
>   um modo.
> - *Glaive* e *Alabarda* têm `stMinimo.valor` **nulo** (`"13‡ / 12"`, uma ST por
>   modo) e apareciam sem ST nenhuma.
> - 43 armas carregam 45 flags de ST: as duas acima têm **† e ‡**.
>
> A decisão adiada da seção 4 (**qual modo da Katana entra na ficha**) continua
> em aberto — o card mostra os dois, a ficha ainda grava o primeiro.

Pedido de 2026-08-03: o card da lista continua com o básico, mas o **toque abre um card novo com
TUDO que o JSON tem**. Hoje o catálogo guarda 13 blocos de dados por arma de fogo e a tela mostra 4.

Catálogos envolvidos (150 armas):
`armas_fogo.v1.normalized.json` (62) · `armas_distancia.v1.normalized.json` (28) ·
`armas_corpo_a_corpo.v1.normalized.json` (60).

---

## 1. Diagnóstico — o que o JSON tem e o jogador não vê

### (a) Campos que o carregador NEM LÊ

`ArmaCatalogoItem` não tem esses campos, então o dado morre em
`CatalogLoaders.carregarArmasDistanciaDeArquivo` / `carregarArmasCorpoACorpoNormalizadas`:

| Campo no JSON | Quantas armas têm | O que é |
|---|---|---|
| `nt` | 148 de 150 | NT da arma |
| `cl` | 42 | Classe de Legalidade |
| `peso.municaoKg` | 67 | peso da munição (só o `armaKg` é lido) |
| `stMinimo.flags` | 45 (`dagger` 37, `double_dagger` 8) | os símbolos † / ‡ do livro |
| `custo.raw`, `peso.raw`, `alcanceDistancia.raw` | todas | o texto literal da tabela |

### (b) 🔴 Precisão com mira embutida — bug de REGRA, não de vitrine

`precisao.raw` traz `"6+1"`: 6 da arma **+1 de acessório embutido**. O carregador lê só
`precisao.valor` (= 6) e descarta o `+1`.

**12 armas de fogo** perdem isso:

| Arma | Prec no livro | Prec no app | Perda |
|---|---|---|---|
| Rifle de Atirador, .338 | `6+3` | 6 | −3 |
| Rifle de Atirador a Laser | `12+2` | 12 | −2 |
| Rifle Laser | `12+2` | 12 | −2 |
| Rifle de Gauss, 4 mm | `7+2` | 7 | −2 |
| ACI, 6,8 mm | `4+2` | 4 | −2 |
| ADP Gauss, 4 mm | `6+1` | 6 | −1 |
| Carabina de Assalto, 5,56 mm | `5+1` | 5 | −1 |
| Carabina Eletrolaser conjunto | `8+1` | 8 | −1 |
| (+ 4 outras) | | | −1 |

⚠️ Essa Precisão é a que alimenta o **Apontar/Mira** (`ApontarRules`, lote MIRA-4). O atirador está
apontando com até 3 pontos a menos, e o **teto do dobro da Prec** (MB p.373) sai errado por tabela.
É bug da aba **Rolagem**.

### (c) 🔴 28 das 60 armas corpo a corpo perdem o segundo modo de ataque

O loader lê `modos.first()` e para. `dano.modos` tem dois elementos em 28 armas:

| Arma | Modo 1 | Modo 2 (perdido) |
|---|---|---|
| Katana | `GeB+1 corte`, alcance `1, 2` | `GdP+1 perf`, alcance `1` |
| Espada Larga | `GeB+1 corte` | `GdP+1 cont` |
| Espada Bastarda | `GeB+1 corte`, alcance `1, 2` | `GdP+1 cont`, alcance `2` |
| Sabre de Cavalaria | `GeB+1 corte` | `GdP+1 perf` |
| Facão | `GeB-2 corte` | `GdP perf` |
| Porrete | `GeB+1 cont` | `GdP+1 cont` |
| … | | mais 22 |

Não é card faltando: é **um ataque inteiro** que o personagem tem no livro e não tem no app.

### (d) Campos lidos, salvos na ficha e mesmo assim invisíveis

Precisão, 1/2D, Máximo, alcance por múltiplo de ST, CdT, Tiros, Magnitude, Recuo, duas mãos e
alcance corpo-a-corpo já existem em `Equipamento` desde o Lote 371. São consumidos **só** por
`TraducaoFichaParaCombate` (combate da Saga). Nenhum aparece em nenhum card.

---

## 2. Onde o código está hoje

| Arquivo | Linhas | Papel |
|---|---|---|
| `domain/loaders/CatalogLoaders.kt` | 1134 | carrega TODOS os catálogos — **já acima do teto de 1000** |
| `model/ArmaCatalogoItem.kt` | 54 | modelo do item do catálogo |
| `model/Personagem.kt` | 1170 | `Equipamento` (linha 797) — **acima do teto** |
| `viewmodel/delegates/FichaEquipmentDelegate.kt` | 385 | copia catálogo → ficha; rodapés `[n]` |
| `ui/TabEquipamentos.kt` | 801 | lista, `ArmaItemSelecao`, cards do inventário |

Nenhum desses arquivos pode engordar. Todo código novo nasce em arquivo novo.

---

## 3. Os lotes

### ARMA-1 — alargar o dado (sem UI)

- Novo `domain/loaders/ArmasCatalogLoader.kt`: as três funções de arma saem do `CatalogLoaders.kt`
  (que cai de 1134 para dentro do teto). O `CatalogLoaders` fica só com o encaminhamento.
- `ArmaCatalogoItem` ganha `nt`, `cl`, `municaoKg`, `custoRaw`/`pesoRaw`/`alcanceRaw`, `stFlags`,
  **`precisaoAcessorio`** (o `+1` do `"6+1"`) e **`modos: List<ModoDeArma>`**.
- `precisaoTotal = precisao + precisaoAcessorio` passa a ser o que vai para `Equipamento.armaPrecisao`
  — é o que conserta o Apontar.

**Teste (varredura, não caso isolado):** para cada uma das 150 armas do asset real, todo campo
presente no JSON tem de chegar ao modelo. Asserção pontual não serve aqui — foi exatamente uma
leitura parcial (`modos.first()`, `precisao.valor`) que criou os furos (b) e (c).
Teste dedicado para as 12 armas com `+N` na Prec e para as 28 com 2 modos.

### ARMA-2 — a ficha técnica (sem UI)

`domain/rules/FichaTecnicaDaArma.kt` — função pura que devolve as linhas prontas:
`(rótulo, valor, explicação)`. Sem Compose, então dá para testar de verdade.

É onde o jargão vira português:

| Cru | Vira |
|---|---|
| `Tiros 80(3)` | 80 tiros, 3 turnos para recarregar |
| `Mag −3` | −3 para atacar em espaço apertado e para ocultar |
| `Rec 2` | 1 acerto extra a cada 2 pontos de margem |
| `CL 2` | licença militar |
| `ST 9 †` | usa as duas mãos |
| `4d(3) pa-` | divisor de armadura 3 |

E é onde o valor é calculado **com a ST da ficha**: Arco Longo `×15/×20` com ST 11 → **165/220 m**;
`GdP+2 perf` com ST 11 → **1d+3**. Hoje o app guarda o `×15` e nunca faz a conta.

Regra: campo ausente aparece como **—**, nunca como 0. Zero é um dado; "não sei" é outro.

### ARMA-3 — o card de detalhe na seleção

`ui/features/equipamento/CardDetalheArma.kt` (pasta nova — `TabEquipamentos.kt` está em 801/1000).

- A lista continua igual: nome, ST, tipo, dano, custo, peso.
- O toque abre o detalhe. **Adicionar ao inventário** e **Voltar** ficam dentro dele.
- ⚠️ Muda o fluxo: hoje um toque já adiciona; passa a exigir dois. É o que foi pedido, e é o certo —
  hoje dá para adicionar uma arma sem descobrir que ela pesa 7,3 kg.
- `pracego`: cada linha precisa de `contentDescription` da linha inteira. Tabela lida célula a
  célula pelo TalkBack não serve para nada.

### ARMA-4 — o mesmo card no inventário

O detalhe também abre a partir da arma já equipada, ao lado do lápis de Editar. Monta a partir do
`Equipamento` (não do catálogo), então **ficha antiga funciona**: os campos que não existiam quando
ela foi salva aparecem como —.

---

### ARMA-5 — mira acoplada e o conflito arma × perícia (aba Rolagem)

Pedido de 2026-08-03, junto com o print do diálogo "Onde acertar".

**(a) Caixinha de mira acoplada.** É o `+N` da seção 1(b): `Prec 6+1` quer dizer arma 6 **+1 de mira
embutida**. Vira uma caixinha aninhada sob o Apontar, ao lado de "arma firmada", e **só aparece quando
`precisaoAcessorio > 0`** — arma sem mira não pode oferecer o bônus. Depende do ARMA-1.

**(b) 🔴 O diálogo abriu em modo corpo a corpo com perícia de pistola.** No print, o cabeçalho diz
`Ataque Armas de Fogo/NT (pistola)` e mesmo assim aparece **Golpe Rápido** (opção de corpo a corpo,
MB p.371) e **não** aparecem a linha de distância nem o Apontar.

O caminho: `TabRolagem` chama `AlcanceDoAtaque.ehADistancia(armaDaFonteDeDano, periciaDoAtaque)`, e a
regra diz *"arma na mão manda"* (`AlcanceDoAtaque.kt:62`):

```
if (tipoEhADistancia(arma.armaTipoCombate)) return true
if (!arma.armaTipoCombate.isNullOrBlank()) return false   // <- corpo a corpo cala a perícia
```

A perícia `armas_de_fogo_nt_pistola` casa com `PERICIAS_COMBATE_DISTANCIA` — conferido contra
`pericias.v3.json`, o id é `armas_de_fogo_nt` e o teste de prefixo pega a especialização. Logo, o
único caminho que produz o sintoma é a **"Arma / Fonte de dano" estar apontando para uma arma de
corpo a corpo** enquanto o ataque escolhido é a pistola.

⚠️ A regra foi escrita para o caso oposto (faca de arremesso empunhada com a perícia Faca) e nunca
previu a combinação incoerente. Hoje ela **escolhe em silêncio** — e o jogador perde a distância, o
1/2D, o Máx e o Apontar inteiro sem nenhum aviso.

Correção proposta: quando perícia e arma discordam, o app **não escolhe calado**. Segue a perícia
(que é o ataque que o jogador tocou) e escreve a divergência na tela: *"o ataque é de pistola mas a
fonte de dano é a Adaga — confira a arma"*. Mesmo princípio do `AvisoDeAlcance`: silêncio é resposta
ambígua.

**Teste:** matriz perícia × arma (fogo/arco/corpo a corpo/nenhuma) contra o valor esperado de
`ehADistancia`, mais o caso incoerente exigindo o aviso. Os testes de hoje só cobrem os pares
coerentes — foi por isso que passaram verdes com o defeito em pé.

---

## 4. Fora de escopo (decisão adiada)

**Qual modo da Katana entra na ficha?** Os lotes acima só **mostram** os dois. Adicionar vira uma
pergunta: entram duas armas no inventário, ou uma arma com dois ataques? Isso mexe no combate e na
Rolagem — lote próprio, depois de o card estar rodando no aparelho.

---

## 5. Gate por lote

Build nas 2 variantes (`visual` e `pracego`), `===EXIT=0===` conferido, lint limpo, `PROGRESS.md`
anexado (nunca reescrito), commit + push. ARMA-3 e ARMA-4 tocam UI → **param para teste no aparelho**.
