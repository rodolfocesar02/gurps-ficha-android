# Roteiro de teste no aparelho — fase AUTOM-4

> Versão **2.3-BRACAL** (versionCode 102), branch `GURPS-Saga`.
> Cobre V-3, V-4, V-5, REACAO-1 e as correções de 28/07 (REACAO-2, STB-1,
> STB-2). Gate: 1.156 testes, 0 falhas, build OK nas duas variantes.
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
| **ST Braçal** +2 em **dois braços** | Custo por nível, e o bônus de dano (T6 e T7) |

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

## T3 — Teste de Reação (REACAO-1 e REACAO-2)

1. Ainda na aba **Rolagem**, role até o **fim**.
2. ✅ Existe um card **Teste de Reação** mostrando `+2` (do Carisma nível 2).
3. ✅ Logo abaixo, a notinha diz de onde veio: `Carisma +2`.
4. ✅ A linha da tabela `3d6: 6- péssima...` **não existe mais**, e o card ficou
   mais baixo.
5. ✅ A **Voz Melodiosa** aparece como **caixinha**:
   `Voz Melodiosa +2 — de quem pode ouvir sua voz`.
6. ✅ Com a caixinha **desmarcada**, o número no canto continua `+2`.
7. ✅ **Marcando** a caixinha, o número vira `+4`.
8. ✅ Marcar a caixinha **NÃO** dispara a rolagem — só o cabeçalho do card rola.
9. Toque no cabeçalho.
10. ✅ Rola 3d6 e o resultado sai com o modificador aplicado.
11. Remova o Carisma **e** a Voz Melodiosa da ficha.
12. ✅ O card de Reação **some por completo**.

## T4 — O painel de Autocontrole continua certo (regressão do AUTOM-3)

Foi corrigido em 27/07 e vale reconferir, porque o painel de Reação foi colocado
logo antes dele.

1. Adicione uma desvantagem com autocontrole (ex.: **Avareza**, NA 12).
2. Vá ao fim da aba **Rolagem**.
3. ✅ Reação aparece **acima** de Autocontrole, os dois sem se sobrepor.
4. ✅ O `NA 12` aparece **na horizontal**, numa linha só — não quebrado letra
   por letra na vertical.
5. ✅ A linha `NA 12: costuma resistir. Role 3d6...` **não existe mais** — o
   card virou uma linha só por desvantagem.

## T5 — Variante Pra Cego (TalkBack)

Só se você for testar a variante de acessibilidade.

1. Ligue o TalkBack e abra a aba **Rolagem**.
2. ✅ O card de Reação é anunciado como
   *"Rolar teste de reação. Modificador mais 2. Carisma mais 2"* — o número e a
   origem vêm juntos, não em elementos separados.
3. ✅ Cada caixinha de bônus condicional é anunciada com a **condição por
   extenso**, não só o número.

## T6 — O custo do ST Braçal (STB-1)

Este era o erro de regra: o app cobrava 3, 5 ou 8 pontos **uma vez**, quando no
livro esse é o preço de **cada +1**.

1. Vá em **Traços** → adicionar vantagem → busque **ST Braçal**.
2. ✅ O diálogo **não** mostra mais três botões soltos de "3 pts / 5 pts /
   8 pts". Mostra **Braços beneficiados** (`Um braço — 3 pts por +1`,
   `Dois braços — 5 pts por +1`, `Três braços — 8 pts por +1`) e, abaixo,
   **Níveis** com `-` e `+`.
3. Escolha **Dois braços** e suba os níveis até **+4**.
4. ✅ O custo no topo mostra **20 pts** (é o exemplo do próprio livro, p.89) e a
   conta aparece embaixo: `5 × 4 = 20 pts`.
5. Adicione e volte para a lista de Traços.
6. ✅ A vantagem continua valendo **20 pts** na lista.
7. Abra-a para **editar**.
8. ✅ Ela reabre já com **Dois braços** e **+4** marcados — não volta ao padrão.
9. Repita o teste com **DX Braçal**: dois braços, +3 → deve dar **48 pts**
   (16 × 3), e só existem as opções de **um e dois** braços.

## T7 — O bônus de dano do ST Braçal (STB-2)

Com a ST Braçal +4 em dois braços da etapa anterior, numa ficha de **ST 10**.

1. Vá para a aba **Rolagem**.
2. ✅ Logo **abaixo da linha de atributos** aparece uma caixinha pequena e
   discreta: `ST Braçal +4 (braços agem como ST 14)`.
3. Com ela **desmarcada**:
   - ✅ o **ST** mostra `10`;
   - ✅ **Dano** mostra o dano de ST 10 (GdP `1d-2`, GeB `1d`).
4. **Marque** a caixinha:
   - ✅ o **ST** passa a mostrar `14`;
   - ✅ os **outros atributos não mudam** (DX, IQ, HT, VON, PER seguem 10);
   - ✅ o **Dano** passa para o de ST 14 (GdP `1d`, GeB `2d`);
   - ✅ se houver **arma** equipada, o dano dela sobe junto (a Faca `1d-3 corte`
     vira `2d-3 corte`).
5. ✅ **PV e PF NÃO mudam** em nenhum dos dois estados. É o ponto da regra: a ST
   Braçal não dá Pontos de Vida.
6. Remova a ST Braçal da ficha.
7. ✅ A caixinha **some por completo**.

---

## O que NÃO precisa testar (e por quê)

- **RD natural, Voo/Natação e Vulnerabilidade** — descartados nesta fase, não há
  código novo. Motivo registrado em `Revisao_Abas_e_Navegacao.md`.
- **Nota de bônus em Armas e Itens** — é o lote NOTA-2, ainda não feito. Em
  **Perícias** a nota já existe desde o NOTA-1 e já foi validada.
- **ST Braçal no combate tático** — de propósito, o combate continua usando a ST
  do corpo. Lá a escolha teria de ser por ataque, não um botão da ficha.
