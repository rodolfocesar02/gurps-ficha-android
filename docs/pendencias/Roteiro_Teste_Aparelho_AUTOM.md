# Roteiro de teste no aparelho — fase AUTOM-4

> Versão **2.2-AUTOM** (versionCode 101), branch `GURPS-Saga`.
> Cobre os lotes V-3, V-4, V-5 e REACAO-1. Gate: 1.140 testes, 0 falhas,
> build OK nas duas variantes.
>
> Marque o que passou. O que falhar, anote **o que apareceu na tela**, não só
> "quebrou" — o número errado diz onde está o defeito.

---

## Preparo (uma ficha só serve para tudo)

Crie ou abra uma ficha e adicione estas vantagens em **Traços**:

| Vantagem | Serve para testar |
|---|---|
| **Carisma** (nível 2) | Teste de Reação — modificador que soma sempre |
| **Voz Melodiosa** | Teste de Reação — modificador **condicional** |
| **Rosto Sincero** | Bônus condicional em perícia |
| **Silêncio** (nível 1) | Bônus condicional com **duas** condições diferentes |
| **ST Braçal** (nível 2) | Atributo com escopo — **não** pode somar no ST geral |

E uma perícia qualquer: **Dissimulação** e **Furtividade**.

---

## T1 — Atributos continuam certos, e o app não trava (V-3/V-4)

Este lote não mostra nada novo na tela. O que ele pode quebrar é o **cálculo
de sempre**, então o teste é de "nada mudou".

1. Abra a ficha na aba **Geral**.
2. ✅ ST, DX, IQ, HT, Vontade, Percepção, PV, PF e Velocidade Básica aparecem
   normalmente, com os valores de antes.
3. ✅ A ficha abre **na hora**. Se demorar, congelar ou fechar sozinha, é a
   trava de recursão falhando — anote e me avise.
4. ✅ Com **ST Braçal 2** na ficha, o **ST geral continua o mesmo**
   (ex.: ST 10 continua 10). ST Braçal vale só para os braços; se o ST subir
   para 12, está somando errado.

## T2 — Bônus condicional na hora de rolar (V-5)

1. Vá para a aba **Rolagem**.
2. Toque em **Dissimulação**.
3. ✅ Antes de rolar aparece uma caixinha:
   `Rosto Sincero +1 — para parecer inocente`.
4. ✅ Com a caixinha **desmarcada**, o total rola sem o +1.
5. ✅ Marcando a caixinha, o **+1 entra no total** exibido.
6. Toque em **Furtividade**.
7. ✅ Aparecem **duas** caixinhas do Silêncio (`imóvel +2` e `em movimento +1`)
   — são situações diferentes, cada uma com a sua.
8. Toque numa perícia **sem** bônus condicional (ex.: Escalada, se não tiver
   Pendulear).
9. ✅ **Nenhuma** caixinha aparece — perícia sem condição não ganha painel.

## T3 — Teste de Reação (REACAO-1)

1. Ainda na aba **Rolagem**, role até o **fim**.
2. ✅ Existe um card **Teste de Reação** mostrando `+2` (do Carisma nível 2).
3. ✅ Logo abaixo, a notinha diz de onde veio: `Carisma +2`.
4. ✅ E a tabela: `3d6: 6- péssima · 7-9 ruim · 10-12 neutra · 13-15 boa · 16+ excelente`.
5. ✅ **Voz Melodiosa NÃO entra no +2.** É condicional (só de quem pode ouvir a
   voz) — somar sempre daria bônus contra surdos e contra máquinas.
6. Toque no card.
7. ✅ Rola 3d6 e o resultado aparece **com o modificador aplicado**.
8. Remova o Carisma e a Voz Melodiosa da ficha.
9. ✅ O card de Reação **some por completo** — ficha sem traço social não mostra
   painel vazio.

## T4 — O painel de Autocontrole continua certo (regressão do AUTOM-3)

Foi corrigido em 27/07 e vale reconferir, porque o painel de Reação foi colocado
logo antes dele.

1. Adicione uma desvantagem com autocontrole (ex.: **Avareza**, NA 12).
2. Vá ao fim da aba **Rolagem**.
3. ✅ Reação aparece **acima** de Autocontrole, os dois lado a lado sem se
   sobrepor.
4. ✅ O `NA 12` aparece **na horizontal**, numa linha só — não quebrado letra
   por letra na vertical (era o defeito corrigido).

## T5 — Variante Pra Cego (TalkBack)

Só se você for testar a variante de acessibilidade.

1. Ligue o TalkBack e abra a aba **Rolagem**.
2. ✅ O card de Reação é anunciado como
   *"Rolar teste de reação. Modificador mais 2. Carisma mais 2"* — o número e a
   origem vêm juntos, não em elementos separados.
3. ✅ Cada caixinha de bônus condicional é anunciada com a **condição por
   extenso**, não só o número.

---

## O que NÃO precisa testar (e por quê)

- **RD natural, Voo/Natação e Vulnerabilidade** — descartados nesta fase, não há
  código novo. Motivo registrado em `Revisao_Abas_e_Navegacao.md`.
- **Nota de bônus em Armas e Itens** — é o lote NOTA-2, ainda não feito. Em
  **Perícias** a nota já existe desde o NOTA-1 e já foi validada.
