# Capítulo 14 — Lesões, Enfermidades e Fadiga (MB p.418+)

> **Para o agente de IA.** Inventário do capítulo 14 do Módulo Básico, separado
> pelo que serve à **aba Rolagem** e pelo que seria só da Saga.
>
> Criado em 28/07/2026, a pedido do usuário: *"preciso que analise o Capítulo
> Quatorze — Lesões, Enfermidades e Fadiga, a partir da pág. 418, como podemos
> automatizar esses elementos na ficha"*.
>
> **Nada aqui está implementado**, exceto o que a coluna "hoje" marca como ✅.

---

## ⛔ O filtro, antes de tudo

Vale a regra de escopo do projeto (ver `Automações_Vantagens.md` §11):

> **A aba Rolagem é o alvo.** É por onde o usuário joga, via Discord. Se a
> entrega só aparece com o combate tático aberto, **não fazer**.

O Capítulo 14 é o caso mais misturado do livro inteiro — metade dele é efeito de
turno de combate, metade é estado que dura dias. Por isso este documento separa
os dois **antes** de propor qualquer lote.

| Natureza | Onde vive | Serve à Rolagem? |
|---|---|---|
| Efeito de **turno** (choque, atordoamento, knockback) | `domain/combat/` | ❌ não |
| **Estado que dura** (cambaleante, cansado, sangrando, doente, envenenado) | ficha | ✅ sim |
| **Recuperação** (cura natural, descanso, tratamento) | ficha | ✅ sim |
| **Testes de resistir** (consciência, morte, doença, veneno) | ficha | ✅ **já feito** |

---

## 1. O que JÁ está pronto (não refazer)

Conferido no código em 28/07, não no documento — a lição do `Combate.md`, que
listava como pendente coisa que já existia há lotes.

| Item | Onde | Lote |
|---|---|---|
| Testes de manter consciência, evitar a morte, resistir a doença e veneno | `ResistenciaRules` + botão "Reação e Resistência" | RESIST-1 |
| Marcos de PV: ferimento grave, 0 PV, múltiplos negativos, morte automática | `MarcosDeVidaRules` | MARCOS-1 |
| Estados Cambaleante, Caído, Cansado, Exausto, Desmaiado | `MarcosDeVidaRules.estadosDe` | MARCOS-1 |
| Sangramento **dentro do combate** | `CombatModels.sangramentoAtivo` | PONTE-2 |
| Choque (−1 por PV perdido) | motor de combate | — |
| Bônus de Boa Forma, Difícil de Subjugar, Duro de Matar nos testes de HT | `ResistenciaRules` | RESIST-1 |

**Boa parte do capítulo já foi coberta de lado**, pelos lotes de traços. O que
sobra é menor do que parece.

## 2. O que falta e SERVE à aba Rolagem

### 2.1 Sangramento fora do combate 🟢 alto valor

O motor de combate já modela sangramento (`PONTE-2`), mas a **ficha** não sabe
que o personagem está sangrando. Numa sessão por Discord, quem sangrou numa cena
continua sangrando na próxima e ninguém acompanha.

- MB p.420: teste de HT por intervalo; falha custa PV.
- Já existe `Personagem.sagaSangrando` e `sagaSangramentoIntervaloSeg` — campos
  criados para a Saga, mas **a aba Rolagem não os mostra**.
- **Entrega:** o estado aparece no painel de marcos, com o teste montado.

⚠️ Conferir se os campos existentes servem, em vez de criar outros.

### 2.2 Recuperação: cura natural e descanso 🟢 alto valor

- **PF:** ~1 a cada 10 minutos de descanso.
- **PV:** teste de HT ao fim de cada dia; sucesso recupera 1 PV. *Recuperação
  Acelerada* melhora isso — e a vantagem **já está declarada no catálogo** como
  condicional, esperando exatamente por este lote.
- **Entrega:** um botão de "descansar" que oferece o teste e ajusta o PV/PF.

⚠️ **Não automatizar a passagem de tempo.** Quem controla o relógio é o Mestre.
O app oferece o teste quando o jogador diz que descansou.

### 2.3 Doença e veneno como ESTADO 🟡 médio

Hoje só existe o teste de resistir. Falta o estado que fica: "envenenado, −2 em
tudo, testa HT a cada hora".

⚠️ Depende de o catálogo ter os venenos e doenças com números — **conferir
antes**; se não tiver, é curadoria de dado, não código.

### 2.4 Membros aleijados 🟡 médio

MB p.421-422. O braço aleijado muda o que a ficha pode fazer. Encosta no
`escopo` por membro dos traços (`st_bracal`, `sem_um_dedo`), que já existe no
modelo mas nunca foi usado para nada.

### 2.5 Fome, sede, frio, calor 🔴 baixo valor por enquanto

Perda de PF/PV ao longo de dias. É contabilidade de campanha longa; numa sessão
por Discord o Mestre narra. **Não priorizar.**

## 3. O que NÃO fazer — é Saga pura

| Item | Por quê |
|---|---|
| **Choque** (−1 por PV perdido no próximo turno) | só existe se há turno |
| **Atordoamento e knockdown** como estado de combate | idem — o *teste* já existe no MARCOS-1; o estado que dura um turno é do motor |
| **Knockback** | precisa de direção na grade |
| Disparo automático dos testes ao aplicar dano | descartado em 28/07 — ver `Automações_Vantagens.md` §11.10 |

## 4. Ordem sugerida

| # | Lote | Custo | Valor |
|---|---|---|---|
| 1 | **CAP14-1** — sangramento visível na ficha (§2.1) | baixo | alto |
| 2 | **CAP14-2** — descanso e cura natural (§2.2) | médio, **toca UI** | alto |
| 3 | **CAP14-3** — doença e veneno como estado (§2.3) | médio | médio |
| 4 | **CAP14-4** — membros aleijados (§2.4) | alto | médio |

> ⚠️ Antes de abrir o 1, **medir** `TabRolagem.kt` (estava em 984 linhas em
> 28/07) e `PainelMarcosDeVida.kt`. O teto do projeto é 1.000 e o painel de
> marcos é o destino natural do sangramento.

## 5. O que este documento NÃO cobriu

Honestidade sobre o método: o inventário saiu do **compêndio**
(`.agent/skills/Skill_GURPS.MD`, §20) e do código, **não de uma leitura página a
página do capítulo 14 no livro**. O compêndio é um resumo conferido, mas resumo.

Antes de abrir o CAP14-3 ou o CAP14-4, **ler as páginas 418-443 no PDF** — foi
lendo o texto original que apareceram as armadilhas dos outros lotes (o "ou" das
vantagens de peso, o bônus do Fácil de Decifrar que é para o outro).
