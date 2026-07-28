# Automações de Desvantagens

> **Para o agente de IA.** Fonte de verdade para automatizar as 221
> desvantagens de `assets/desvantagens.v2.json`.
>
> **LEIA PRIMEIRO `Automações_Vantagens.md`.** Este arquivo é o irmão dele e
> **não repete** o que já está lá: mecanismo do `TraitRule`, regras de
> engenharia (teto de 1.000 linhas, mapa de pastas), formato do campo `efeitos`
> e a decisão da arquitetura híbrida. Aqui só está o que é **específico de
> desvantagem**.
>
> Criado em 2026-07-27.
---

# ✅ STATUS FINAL — 28 de Julho de 2026 (ler primeiro)

**Este plano está CUMPRIDO.** Números reais, medidos no catálogo:

| Categoria | Previsto | Situação real |
|---|---|---|
| CAT-D1 Autocontrole | 35 | ✅ FEITO (D-1). São **30**, não 35 — contei pela palavra na descrição em vez do marcador `*` do custo |
| CAT-D2 Penalidade | ~35 | ✅ triado um a um (V-8): dos candidatos brutos sobraram poucos. `gordo`, `acima_do_peso`, `caracteristicas_distintas`, `veracidade`, `daltonismo` declarados |
| CAT-D3 Reação negativa | ~23 | ✅ **9 declarados** (REACAO-3/4). Os outros não têm número por rolagem — ver descartes |
| CAT-D4 Perda de sentido | 3 | ✅ `SentidoRules` já tratava |
| CAT-D5 Deslocamento | 3 | ❌ descartado — declarar duplicaria o cálculo existente |
| CAT-D6 Vulnerabilidade | 1 | ❌ pertence ao `InjuryRules` |
| CAT-D7 Custo especial | 15 | ✅ já implementadas |
| CAT-D8 Interpretação pura | ~124 | fora do escopo por definição |

> ⚠️ **A estimativa de ~23 em CAT-D3 era alta.** A classificação foi por
> palavra-chave; lendo as descrições, a maioria menciona reação sem dar número
> por rolagem ("as pessoas o evitam"). Isso vale para todas as contagens "~"
> deste plano — foram chutes de varredura, não leitura.

## Descartes de CAT-D3, com motivo

| id | Por quê |
|---|---|
| `reputacao` | quatro componentes (modificador, público, frequência) **e uma rolagem** para ver se reconheceram. Não é dado |
| `viciado_em_trabalho` | "+1 no início, depois −1 ou −2" — depende do tempo de convivência, que o app não acompanha |
| `deficiencias_menores` | "o Mestre **pode** impor −1". Discricionário, não regra |
| `vicio` | custo Variável, e a penalidade é de abstinência |

## Armadilhas achadas na leitura (valem para o próximo lote)

- `facil_de_decifrar`: o "+4 em Empatia" é para **quem lê** o personagem, não
  para ele. Declarar daria o bônus à pessoa errada.
- `amigavel` e `senso_do_dever` dão bônus **positivo** sendo desvantagens. Não é
  engano: o GURPS cobra em outro lugar.
- `sem_um_dedo`: escopo por membro, mesmo impasse do `st_bracal`.

## Única pendência

A mesma do plano de vantagens: **condicional de atributo** sem UI onde caiba.

---

---

## 0. O que muda em relação às vantagens

Desvantagem não é "vantagem com sinal trocado". Quatro diferenças mudam o plano:

| Diferença | Consequência |
|---|---|
| **Autocontrole** — 35 desvantagens exigem um teste periódico contra 6/9/12/15 | É a maior automação deste arquivo, e ela é uma **ação** (rolar), não um bônus passivo. Ver §2 |
| **O Registry hoje ignora desvantagens** | `TraitRuleRegistry` só varre `personagem.vantagens`. Sem corrigir isso, NENHUMA automação de desvantagem funciona. Ver §1 |
| **A maioria é interpretação de papel** | ~124 de 221 sem efeito numérico. A proporção de "não automatizar" é maior que nas vantagens |
| **Custo já tem bug conhecido** | `CharacterRules.kt:290` documenta a "DUPLA APLICAÇÃO de autocontrole". Ver §5 |

---

## 1. BLOQUEIO ESTRUTURAL: o Registry não enxerga desvantagens

⚠️ **Conferido no código em 2026-07-27. Nada deste arquivo funciona antes disto.**

Todos os agregadores do `TraitRuleRegistry` fazem:
```kotlin
personagem.vantagens.forEach { selection ->      // <-- SÓ vantagens
    val rule = rules[selection.definicaoId]
    ...
}
```
`personagem.desvantagens` **nunca é lido**. Uma `TraitRule` registrada com o id
de uma desvantagem simplesmente nunca é chamada — falha silenciosa, sem erro.

### LOTE D-0 (primeiro de todos) — fazer o Registry varrer os dois

```kotlin
// TraitRuleRegistry.kt — em cada agregador
private fun todosOsTracos(p: Personagem): List<TracoSelecionado> =
    p.vantagens + p.desvantagens     // exige um supertipo comum, ver abaixo
```

**Obstáculo:** `VantagemSelecionada` (`Personagem.kt:384`) e
`DesvantagemSelecionada` (`Personagem.kt:469`) são data classes distintas, mas
**os campos que as regras usam são idênticos** — `definicaoId`, `nome`,
`custoBase`, `nivel`, `custoEscolhido`, `descricao`. Isso torna a saída (A)
barata. A interface `TraitRule` hoje recebe `VantagemSelecionada`.
Duas saídas:

- **(A) Interface comum** — criar `interface TracoSelecionado` com o que as
  regras usam (`definicaoId`, `nivel`, `metadados`) e as duas classes a
  implementam. `TraitRule` passa a receber `TracoSelecionado`.
  Mexe na assinatura das 11 regras existentes. **Recomendada** — é a correta.
- **(B) Agregadores duplicados** (`getSkillBonusDesvantagem`, ...).
  Não mexe no existente, mas duplica todo o Registry. **Rejeitada.**

Ir de (A). Como muda assinatura pública, é lote sozinho, com o build das 2
variantes e a suíte inteira verde antes de qualquer regra nova.

Arquivo: `domain/rules/traits/TracoSelecionado.kt` **CRIAR**
(regra R2 de `Automações_Vantagens.md` §0.5).

---

## 2. AUTOCONTROLE — a automação principal (35 desvantagens)

### 2.1 O que é, e o que existe hoje

Desvantagem mental com número de autocontrole (NA): 6, 9, 12 ou 15. Quando a
situação surge, o jogador rola 3d6 contra o NA; falhou, cede à desvantagem.

Hoje o app:
- ✅ **guarda** o NA (`DesvantagemSelecionada.autocontrole`, chips na UI);
- ✅ **usa** para o custo (multiplicador 0,5×/1×/1,5×/2×);
- ❌ **não rola nada.** Não existe teste de autocontrole em lugar nenhum
  (conferido: nenhuma menção em `TabRolagem.kt` nem em `ui/features/rolagem/`).

### 2.2 O que automatizar

Uma seção na aba Rolagem listando **as desvantagens com NA do personagem**, cada
uma rolável em um toque — igual ao `DialogoSentidos` fez com os sentidos.

Espelhar `DialogoSentidos.kt` porque ele já resolveu os mesmos problemas:
mostrar o valor efetivo, explicar de onde vem, rolar pelo caminho normal
(`executarRolagem` → log → Discord) e ter rótulo grande + TalkBack na variante
PraCego.

### 2.3 Arquivos (seguindo R2)

| Arquivo | Pasta | Papel |
|---|---|---|
| `AutocontroleRules.kt` | `domain/rules/` **CRIAR** | Kotlin puro: lista as desvantagens com NA, devolve NA efetivo e os componentes ("Fobia (Altura), NA 12"). Testável sem Android |
| `DialogoAutocontrole.kt` | `ui/features/rolagem/` **CRIAR** | o diálogo, ao lado do `DialogoSentidos.kt` |
| `AutocontroleRulesTest.kt` | `app/src/test/java/com/gurps/ficha/domain/rules/` | invariantes do §2.5 |

⚠️ **Não** colocar a lógica no `TabRolagem.kt` — ele já tem 1.128 linhas
(regra R1). Ele só ganha o ponto de entrada.

### 2.4 Regras de GURPS a respeitar

- Sucesso = resistiu. Fracasso = cede. **Fracasso crítico = cede da pior forma
  possível** (usar `CriticoRules.classificar`, que já existe).
- O NA é fixo por desvantagem; **não** somar bônus de perícia nem de atributo.
- Vontade alta **não** ajuda no teste de autocontrole (erro comum — não
  implementar isso).
- Modificadores situacionais existem (o Mestre pode dar −2 se a tentação for
  grande). Oferecer campo de modificador manual no diálogo, como nas outras
  rolagens.

### 2.5 Testes obrigatórios

1. personagem sem desvantagem com NA ⇒ lista vazia (diálogo não quebra);
2. desvantagem com NA 12 ⇒ aparece com valor 12;
3. duas instâncias da mesma desvantagem (ex.: duas Fobias) ⇒ aparecem
   **separadas**, cada uma com seu NA e sua especialização;
4. desvantagem mental **sem** NA ⇒ **não** aparece na lista;
5. NA nulo/ausente ⇒ não quebra, não aparece.

### 2.6 As 35 (conferir a lista ao implementar)

Altruísmo, Atavismo por Estresse, Avareza, Briquento, Cleptomania, Cobiça,
Convulsões Pós-combate, Covardia, Credulidade, Curiosidade, Depressão Crônica,
Desatento, e mais ~23. **Gerar a lista definitiva com:**
```bash
python -c "import json;d=json.load(open('app/src/main/assets/desvantagens.v2.json',encoding='utf-8'));print([x['nome'] for x in d if 'autocontrole' in (x.get('descricao') or '').lower()])"
```
Não confiar nesta lista escrita à mão — ela envelhece; o comando não.

---

## 3. Classificação das 221

| Categoria | Qtd | Como automatizar |
|---|---|---|
| **CAT-D1** Autocontrole | 35 | §2 (o grande lote) |
| **CAT-D2** Penalidade em perícia/atributo | ~35 | JSON `efeitos` com `valor` negativo — o mesmo interpretador das vantagens |
| **CAT-D3** Modificador de reação negativo | ~23 | Precisa GANCHO-D + UI de Reação (ver `Automações_Vantagens.md` §2) |
| **CAT-D4** Perda de sentido | 3 | Cegueira, Surdez, Visão Restrita — `SentidoRules` **já trata** (bloqueia o sentido). Conferir antes de mexer |
| **CAT-D5** Deslocamento reduzido | 3 | GANCHO-C |
| **CAT-D6** Vulnerabilidade a dano | 1 | pertence ao motor de combate (`InjuryRules`), não ao `TraitRule` |
| **CAT-D7** Custo especial (`specialRule`) | 15 | já implementadas — ver §5 |
| **CAT-D8** Interpretação pura | ~124 | **NÃO AUTOMATIZAR** |

### 3.1 CAT-D2 — as com penalidade numérica explícita

Vão para JSON (`efeitos` com valor negativo). Casos achados na varredura:

| id | Efeito | Cuidado |
|---|---|---|
| `gordo` | −2 Disfarce | direto |
| `acima_do_peso` | −1 Disfarce | direto |
| `dorminhoco` | −1 IQ (condicional: ao acordar) | **condicional** ⇒ não soma sozinho |
| `sangue_frio` | −1 DX e ... | ler descrição completa |
| `supersensitivismo` | −1 DX e ... | ler descrição completa |
| `sem_um_dedo` | −1 DX **da mão** / −5 se for o polegar | ⚠️ **escopo por membro** — o campo `efeitos` não expressa isso hoje. Mesmo impasse de `st_bracal` nas vantagens. **Deixar PENDENTE** até o escopo existir |

⚠️ A varredura automática achou só 6 com penalidade numérica explícita, mas a
classificação por palavra-chave contou ~35 em CAT-D2. A diferença são
penalidades escritas em prosa ("sofre redutor no teste"). **Ao abrir o lote,
reler as ~35 uma a uma** — não confiar no número.

---

## 4. Duas armadilhas específicas de desvantagem

### 4.1 Penalidade dupla

Várias desvantagens já têm o efeito embutido no **custo** e o jogador espera que
o app aplique o efeito **também na mecânica**. Aplicar os dois é correto; o erro
é aplicar a mesma penalidade duas vezes na mecânica — ex.: `SentidoRules` já
bloqueia o sentido em Cegueira. Se uma regra nova também subtrair, o personagem
é punido em dobro.

**Antes de automatizar qualquer CAT-D4, conferir `SentidoRules.kt`.**

### 4.2 Limite de desvantagens

`CharacterRules` já tem cálculo de limite de pontos em desvantagens. Automação
de efeito **não pode** mexer nisso. São coisas separadas: uma é orçamento de
criação, a outra é regra de jogo. Não misturar no mesmo lote.

---

## 5. CAT-D7 — as 15 com `specialRule` (manutenção, não automação nova)

`dependencia`, `dependentes`, `dever`, `dor_cronica`, `fobias`, `fragilidade`,
`fraqueza`, `inimigos`, `maldicao_divina`, `manutencao`, `reputacao`,
`sem_pernas`, `sonolento`, `vicio`, `vulnerabilidade`.

Já têm UI (`TraitSpecialRuleComponents.kt`) e cálculo em `CharacterRules`.

🐞 **BUG CONHECIDO, ainda aberto** — `CharacterRules.kt:290` documenta:
> "BUG da DUPLA APLICAÇÃO de autocontrole (universal p/ qualquer desvantagem com
> autocontrole 6 ou 15): o dialog calcula e PERSISTE em `custoEscolhido` o valor
> JÁ multiplicado pelo autocontrole (ex: Avareza -10 -> -5). Se o autocontrole
> fosse reaplicado sobre..."

**Lote próprio, antes de mexer em custo de desvantagem.** É custo, não efeito —
não confundir com o LOTE D-1 do autocontrole (§2), que é rolagem.

---

## 6. Ordem de implementação

0. **LOTE D-0** — Registry passa a varrer desvantagens (§1). **Bloqueia tudo.**
   Sem vantagem nem desvantagem nova; só o trilho + suíte verde.
1. **LOTE D-1** — Autocontrole: `AutocontroleRules` + `DialogoAutocontrole` (§2).
   Maior valor para o jogador. Toca UI ⇒ **PARA para teste no aparelho**.
2. **LOTE D-2** — CAT-D2 incondicionais em JSON (§3.1). Depende do LOTE V-0
   (interpretador) de `Automações_Vantagens.md`. Zero Kotlin novo.
3. **LOTE D-3** — bug da dupla aplicação de custo (§5).
4. **LOTE D-4+** — CAT-D3 (reação, junto com a UI compartilhada com vantagens),
   CAT-D5 (deslocamento), CAT-D6 (vulnerabilidade, no motor de combate).

**Checklist de fim de lote:** o mesmo de `Automações_Vantagens.md` §7.

---

## 7. Dependências entre os dois arquivos

```
Automações_Vantagens.md          Automações_Desvantagens.md
  LOTE V-0 (interpretador) ─────────► LOTE D-2 (usa o mesmo interpretador)
  GANCHO-D + UI de Reação ──────────► LOTE D-4 (CAT-D3 usa a mesma UI)
  LOTE D-0 (Registry) ◄───────────── bloqueia TODA automação de desvantagem
```

O `EfeitoInterpretador` e a UI de Reação são **compartilhados**. Não criar
versão separada para desvantagem — só garantir que leem
`desvantagens.v2.json` também (o campo `efeitos` vale para os dois catálogos).

---

## 8. Achados de dados

- ✅ Nenhuma desvantagem sem campo `nome` (diferente das vantagens, onde
  `adaptabilidade_cultural` está sem — ver `Automações_Vantagens.md` §9).
- ⚠️ **`Características Proibidas`** tem a tag malformada
  `"fisica,mental,social,supernatural"` — uma string só, em vez de 4 tags
  separadas. Efeito prático: essa desvantagem **não aparece** em nenhum dos
  filtros por tag da tela de seleção. Corrigir para
  `["fisica","mental","social","supernatural"]` no próximo lote de dados.
  Reconferir se surgiram outras:
  ```bash
  python -c "import json;d=json.load(open('app/src/main/assets/desvantagens.v2.json',encoding='utf-8'));print([x['nome'] for x in d if any(',' in t for t in (x.get('tags') or []))])"
  ```
