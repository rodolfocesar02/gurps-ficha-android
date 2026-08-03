# PLANO — Atirador, Arqueiro Heroico, as perícias de tiro e a Magnitude

> ✅ **EXECUTADO** em 03/08/2026, versão **6.0-ATIRADOR** (versionCode 139).
> ARMA-6 ✅ · ARMA-7 ✅ · ARMA-8 ✅ · ARMA-9 ✅.
> Gate: 1.822 testes, 0 falhas nas duas variantes, lint limpo.
> Roteiro de aparelho: blocos **T-CN, T-MG, T-DC, T-AT, T-AH**.
>
> **O que mudou em relação ao previsto:**
> - A linha torta era **muito** pior que "Magnitude positiva": a planilha
>   escorregou uma coluna inteira a partir da CdT, e o Rifle de Atirador .338
>   ficou com **ST 41** — invisível na lista, que filtra por ST. Corrigido com a
>   linha do livro (p.280) nas três armas, mais o Escorpião (campo Tiros).
> - A **Carabina de Assalto 5,56 mm** também parece deslocada, mas nenhuma linha
>   do livro bate campo a campo com ela. **Não foi corrigida** — levou
>   `reviewFlag` em vez de palpite.
> - O ARMA-6 cresceu: além do `canhoneiro_nt`, entraram 8 perícias que existiam
>   no catálogo e nenhuma lista pegava. Os 14 ids órfãos **ficaram**, por serem
>   nomes que fichas antigas podem ter gravado.
> - ARMA-8 e ARMA-9 saíram no mesmo commit: são a mesma família de regra, num
>   arquivo só, e separá-los deixaria metade da tabela sem par.

Pedido de 2026-08-03, depois da conferência da vantagem **Atirador**. Quatro lotes:
**ARMA-6** (perícias), **ARMA-7** (Magnitude), **ARMA-8** (Atirador) e **ARMA-9** (Arqueiro Heroico).

Todos caem na **aba Rolagem** — nada aqui depende do combate da Saga.

---

## 0. ⚠️ O que você pediu e NÃO precisa ser feito

> *"Canhoneiro/NT, artilheiro_nt (pegue no chunk's as informações, coloque ela no json
> correto de pericias)"*

**A perícia Canhoneiro/NT já está no catálogo, completa e correta.** Conferido em
`pericias.v3.json`:

| Campo | Valor no catálogo | Livro (p.187 e p.303) |
|---|---|---|
| id | `canhoneiro_nt` | — |
| nome | `Canhoneiro/NT` | ✅ |
| atributo | `DX` | ✅ |
| dificuldade | `F` (Fácil) | ✅ |
| predefinido | `DX-4` | ✅ |
| descrição | texto integral, com as especializações | ✅ |

Não falta nada no JSON. **O erro é uma linha de Kotlin**: a lista
`PERICIAS_COMBATE_DISTANCIA` ([PericiasDeCombate.kt:100](../../app/src/main/java/com/gurps/ficha/model/PericiasDeCombate.kt))
tem `"artilheiro_nt"` — um id que **não existe em catálogo nenhum** — e não tem
`"canhoneiro_nt"`.

Extrair o livro de novo seria reescrever o que já está certo, e ainda arriscaria
mudar o id — que é o que as fichas salvas guardam.

---

## Lote ARMA-6 — as perícias de tiro que a lista não reconhece

### 🔴 O defeito

`Canhoneiro/NT` não entra em `PERICIAS_COMBATE_DISTANCIA`. Consequência: quem
ataca com um canhão de tanque abre o diálogo **em modo corpo a corpo** — sem
linha de distância, sem 1/2D, sem Máx, sem Apontar, e com *Golpe Rápido*
oferecido. É a mesma classe do bug de 03/08, mas o ARMA-5 não o pega: aquele lote
corrigiu a **precedência** entre arma e perícia, não uma perícia que a lista nunca
reconheceu.

⚠️ E dói mais aqui do que parece: **Canhoneiro é uma das quatro perícias que a
vantagem Atirador cobre**. Sem esta correção, o ARMA-8 nasceria capenga.

### Os 14 ids mortos

Estão na lista e não existem em `pericias.v3.json`:

```
sopro · lancador_de_lancas · projetor_de_pressao · artilheiro_nt · bolas
arma_de_fogo_pistola · arma_de_fogo_fuzil · arma_de_fogo_espingarda
arma_de_fogo_submetralhadora · facas_de_arremesso · shuriken
pericia_de_arma_de_fogo · pericia_de_arco · pericia_de_besta
```

⚠️ **Não vou apagar todos sem olhar.** Alguns podem ser ids de **perícias raciais
ou do bestiário** (o `periciaEhADistancia` remove o prefixo `racial_`), e apagar
um deles quebraria um NPC em silêncio. O lote confere cada um contra
`racas.v1.json`, `bestiario.v1.json` e `pericias_artes_marciais.v1.json` antes de
mexer; o que sobrar morto sai com o motivo escrito no commit.

### Também falta

- `arma_de_arremesso` (**Arma de Arremesso**) — a lista pega `arremesso`, que é
  outra perícia.
- `arte_do_arremesso` — perícia cinematográfica; **decidir**, não presumir.

### Teste

- Varredura: **todo id da lista existe** em algum catálogo, ou está numa lista de
  exceções com justificativa escrita.
- Varredura ao contrário: toda perícia do catálogo cujo nome contenha
  *fogo, feixe, canhoneiro, artilharia, projetor, arco, besta, arremesso, funda,
  zarabatana* **está** na lista — ou está numa lista de exceções.
- `Canhoneiro/NT (Canhão)` → `ehADistancia` verdadeiro.

*(É a varredura que faltava: nenhum teste hoje compara a lista com o catálogo, e
por isso 14 ids mortos passaram anos verdes.)*

---

## Lote ARMA-7 — a Magnitude no Avançar e Atacar

### A regra, literal (MB p.366)

> O personagem ataca como descrito na manobra Ataque, mas sofre uma penalidade.
> Se estiver realizando um Ataque **à distância**, a penalidade é de **-2 ou igual
> à Magnitude da arma, o que for pior**. Se estiver realizando um Ataque **corpo a
> corpo**, a penalidade é de **-4** e o nível de habilidade ajustado **não pode ser
> maior que 9**.

### O que entra na tela

Uma caixinha **abaixo do Apontar**, no diálogo *Onde acertar*:

```
☐ Avançar e Atacar: −6 (Magnitude da arma, pior que o −2 básico)
```

O rótulo diz **de onde veio o número** — se foi o −2 do padrão ou a Magnitude da
arma. Sem isso, o jogador vê −6 e não tem como conferir.

⚠️ **A caixinha do Apontar e a de Avançar e Atacar se excluem.** Não existe apontar
por três segundos *enquanto se corre*; marcar uma tem de zerar a outra, e o
rótulo tem de dizer isso. Deixar as duas marcadas somaria um bônus que a regra não
permite.

⚠️ **Em corpo a corpo o teto é NH 9**, e teto não é penalidade: um espadachim
NH 20 vai a **9**, não a 16. É a parte que mais escapa.

### 🔴 O dado torto que descobri

Três armas de fogo têm **Magnitude positiva** no catálogo:

| Arma | `magnitude.raw` | `valor` | `cdt.raw` |
|---|---|---|---|
| Rifle de Atirador, .338 | `10↑` | **10** | *(vazio)* |
| ACI, 6,8 mm | `10↑` | **10** | *(vazio)* |
| Rifle de Gauss, 4 mm | `10↑` | **10** | *(vazio)* |

A CdT dessas três está **vazia** e a Magnitude ficou com um `10↑` — a linha da
planilha saiu **deslocada uma coluna**. Magnitude no livro é sempre ≤ 0.

Se eu automatizar sem guarda, essas três dariam **+10 no ataque enquanto se corre**.

**Regra do lote: Magnitude só penaliza.** O valor entra como
`min(magnitude, 0)`, e quando o catálogo traz positivo a tela **avisa que o dado
está suspeito** em vez de somar. Duas armas (`Sob-Tambor 40 mm` e
`Integrado, 25 mm`) trazem `—` e caem no **−2 básico**, que é o certo: sem
Magnitude cadastrada, vale o padrão.

### O segundo uso da Magnitude (p.271)

> Ela também serve como uma penalidade sobre o NH em **Ocultamento** quando ele
> estiver tentando esconder a arma.

Isso é na perícia **Ocultamento**, não no ataque, e precisa saber *qual arma* está
sendo escondida. Fica para um lote próprio — misturar as duas coisas numa caixinha
só confundiria.

### Teste

- `−2` quando a arma não tem Magnitude, e quando a Magnitude é `−1`.
- `−6` quando a Magnitude é `−6` (o pior dos dois).
- Corpo a corpo: `−4` **e** teto de 9 — inclusive num NH 20.
- 🔴 Magnitude positiva **nunca** vira bônus; nas três armas tortas o resultado é
  o `−2` básico, com aviso.
- Varredura: para as 90 armas à distância, `penalidade ≤ −2` sempre.
- Apontar e Avançar e Atacar **não** podem valer juntos.

---

## Lote ARMA-8 — a vantagem Atirador (25 pontos, cinematográfica)

Hoje ela está no catálogo (`atirador`, 25 pts) com a descrição inteira e
**nenhuma automação**: sem `efeitos`, sem `specialRule`, sem regra Kotlin.

### O que dá para automatizar — e por que só agora

| Benefício do livro | Dá? | Por quê |
|---|---|---|
| **Prec sem Apontar** (total com 1 mão e CdT 1–3; **metade, arredondada para cima**, com 2 mãos ou automática) | ✅ | Precisa saber *uma ou duas mãos* e *CdT* — os dois campos chegaram no **ARMA-1**. Antes dele não havia como calcular. |
| **Ignorar o Avançar e Atacar** (−2 ou Magnitude) | ✅ | Depende do ARMA-7. |
| Ignorar o **−2 de ataque súbito** (p.390) | ⚠️ | O modificador não existe na Rolagem. Entra **junto com a caixinha** que o cria, ou não entra. |
| Magnitude em **combate corporal** (p.391) | ❌ | Combate corporal só existe na Saga. |
| Atirar **montado** com o NH da arma | ❌ | O app não modela montaria na Rolagem. |
| Metade do predefinido de **técnicas de tiro rápido** | ⚠️ | Depende de a aba Técnicas modelar predefinido penalizado — **conferir antes de prometer**. |
| Metade das penalidades de **Sacar Rápido (Munição)** | ❌ | Não modelado. |

### ⚠️ A regra que é fácil errar

O livro diz, duas vezes: *"Tudo isso é **em vez de** receber o bônus da Prec"*.

Ou seja: com Atirador, **Avançar e Atacar e Prec grátis são exclusivos**. Quem
marca *Avançar e Atacar* ignora a penalidade **e perde** a Precisão automática.
Somar os dois seria dar duas vantagens por um preço.

O rótulo tem de dizer qual dos dois está valendo naquele momento.

### Onde mora

`domain/rules/AtiradorRules.kt` — arquivo novo, Kotlin puro. **Não** entra como
`efeitos` no JSON: nenhum dos benefícios é "+N numa perícia", que é o único
formato que o `EfeitoInterpretador` entende. É o mesmo caso da Mão Fraca.

O `ApontarRules` ganha só um encaminhador fino — o motor não engorda.

### Teste

- Pistola (1 mão, CdT 3, Prec 2): **+2 sem apontar**.
- Rifle (2 mãos, Prec 6): **+3 sem apontar** — metade arredondada **para cima**.
- Prec 5 com 2 mãos: **+3**, não +2. *(O arredondamento para cima é a pegadinha.)*
- Arma automática de uma mão (CdT `9!`): **metade**, porque automática também conta.
- 🔴 Apontando, o bônus é o **total** — Prec + apoio + mira + segundos —, e o
  teto do dobro da Prec continua valendo.
- 🔴 Marcar *Avançar e Atacar*: a penalidade some **e** a Prec grátis some.
- ⚠️ **Arma motora (arco, besta, funda) não recebe nada** — o livro exclui, e é o
  erro mais provável de quem lê rápido.
- Sem a vantagem, **tudo continua exatamente como hoje** (varredura sobre as 90
  armas à distância).

---

## Lote ARMA-9 — Atirador (Arqueiro Heroico), 20 pontos

Está no catálogo (`atirador_arqueiro_heroico`, 20 pts, p.45), também sem
automação. **Sim, há o que automatizar** — e as regras **não são as mesmas**:

| | Atirador | Arqueiro Heroico |
|---|---|---|
| Perícias | Armas de Feixe, Armas de Fogo, Canhoneiro, Projetor de Líquidos | **Arco** |
| Prec sem Apontar | total (1 mão) / **metade** (2 mãos) | **total**, sem metade |
| Segundos de Apontar | os normais (+1 com 2s, +2 com 3s+) | 🔴 **+1 com 1s, +2 com 2s+** |
| Avançar e Atacar | ignora, em vez da Prec | ignora a Magnitude, em vez da Prec |

🔴 **O terceiro item é o mais fácil de deixar passar**: o Arqueiro Heroico acumula
os segundos **um turno mais cedo** que a regra geral. Copiar a tabela do
`bonusPorTurnos` sem olhar daria a ele o mesmo ritmo de todo mundo.

Fora isso: Ataque Aéreo (−1) e Ataque Acrobático (−2) ignorados, Arcos de Tiro
Rápido com o −6 pela metade, ADA (Arco) predefinido em Arco-2 em vez de Arco-4, e
Sacar Rápido (Flecha) com as penalidades somadas e divididas. Os quatro últimos
dependem de modificadores que a Rolagem ainda não tem — ficam documentados como
**fora**, com o motivo, na lista de exceções do teste.

---

## Ordem e gate

1. **ARMA-6** (perícias) — pequeno, e o ARMA-8 depende dele.
2. **ARMA-7** (Magnitude) — a caixinha nova; o ARMA-8 depende dela.
3. **ARMA-8** (Atirador).
4. **ARMA-9** (Arqueiro Heroico).

Cada um: build nas 2 variantes, `===EXIT=0===` conferido, lint limpo, `PROGRESS.md`
anexado, commit + push. ARMA-7, 8 e 9 tocam UI → **param para teste no aparelho**,
com bloco próprio no `Roteiro_Teste_Aparelho_AUTOM.md`.
