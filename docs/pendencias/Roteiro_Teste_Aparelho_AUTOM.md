# Roteiro de teste no aparelho — fase AUTOM-4

> Versões **2.5-CATALOGO** (T1..T9, já validados) e **2.7-RESISTENCIA**
> (versionCode 106, T10..T15). Branch `GURPS-Saga`.
> Cobre V-3, V-4, V-5, REACAO-1/2/3, STB-1, STB-2, NOTA-2, OPCAO-1 e V-8.
> Gate: 1.178 testes, 0 falhas, build OK nas duas variantes.
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

## T8 — Todo número diz de onde veio (NOTA-2)

Precisa de uma ficha com **Mestre de Armas** e uma **arma** equipada, e de um
**escudo** selecionado no Bloqueio.

1. Aba **Rolagem**, card **Dano**, com a arma escolhida.
2. ✅ Abaixo do dano aparece uma linha pequena: `+1/dado Mestre de Armas`.
3. ✅ Diz **/dado**, não só `+1` — numa arma de 3d o ganho real é +3, e "+1"
   sozinho seria mentira.
4. Troque a fonte de dano para **Dano ST** (sem arma).
5. ✅ A linha **some** — o Mestre de Armas não vale para dano de ST puro.
6. Toque no botão **BLOQUEIO** para abrir a configuração.
7. ✅ Aparece `Somado à base:` e a lista, com o escudo **pelo nome**
   (ex.: `+3 (Escudo Grande +2, Reflexos em Combate +1)`).
8. Digite um bônus manual de `+1` e escreva a nota "poção do Mestre".
9. ✅ Ao reabrir, a nota aparece na lista no lugar da palavra "Manual".
10. ✅ **A soma da lista bate com o número do card.** Se o card mostra 10 e a
    base é 7, a lista tem que somar 3. Explicação que não bate é pior que
    explicação nenhuma — anote e me avise.
11. Numa ficha **sem** escudo, sem vantagem de defesa e sem bônus manual:
12. ✅ O bloco `Somado à base:` **não aparece** no diálogo.

## T9 — Os traços novos do catálogo (REACAO-3, OPCAO-1, V-8)

São 16 traços novos. Não precisa testar todos — estes quatro cobrem os riscos.

**A Aparência é o teste mais importante do lote**, porque foi onde quase entrou
um bônus no lugar de uma penalidade.

1. Adicione **Aparência** como **vantagem**, escolhendo **12 pts** (Elegante).
2. Aba **Rolagem**, card **Teste de Reação**.
3. ✅ Aparecem **duas** caixinhas da Aparência: `+2 — de quem pode enxergar você`
   e `+2 — adicional, de quem se sente atraído pelo seu sexo`.
4. ✅ Marcando as duas, o total sobe **+4** — é o número do livro para Elegante.
5. Troque para **20 pts** (Lindo).
6. ✅ Agora as parcelas são `+2` e `+6`, total **+8**.
7. Remova e adicione **Aparência** como **desvantagem**, com **−16** (Hediondo).
8. ✅ 🔴 A caixinha tem que ser **−4**, penalidade. Se aparecer bônus positivo,
   a correção da colisão de id falhou — **anote e me avise na hora**.

9. Adicione **Reconhecimento Social** nível 3.
10. ✅ O card de Reação mostra `+3` direto no total, **sem caixinha** — é o único
    modificador de reação incondicional do lote.

11. Adicione **Daltonismo** e a perícia **Química/NT**.
12. ✅ No card da perícia aparece a nota `-1 Daltonismo`, nas abas **Perícias** e
    **Rolagem**.
13. ✅ O NH mostrado já vem com o −1 aplicado.

14. Adicione **Invisibilidade** e a perícia **Furtividade**.
15. ✅ Ao rolar Furtividade aparece a caixinha
    `Invisibilidade +9 — sem carregar nada, quando importa não ser visto`.
16. ✅ Com ela **desmarcada** o NH não muda — +9 é grande demais para valer
    sempre.

---

## O que NÃO precisa testar (e por quê)

- **RD natural, Voo/Natação e Vulnerabilidade** — descartados nesta fase, não há
  código novo. Motivo registrado em `Revisao_Abas_e_Navegacao.md`.
- **Nota de bônus em Perícias** — já existe desde o NOTA-1 e já foi validada em
  27/07.
- **ST Braçal no combate tático** — de propósito, o combate continua usando a ST
  do corpo. Lá a escolha teria de ser por ataque, não um botão da ficha.
- **Condicional de atributo** ("+1 HT para ver se sobrevive", 13 traços) — não
  foi declarado. A caixinha só existe para perícia e defesa; a rolagem de
  atributo é um toque direto, sem diálogo onde ela caiba.

---

# FASE 2 — versão 2.7-RESISTENCIA (28/07, gate 1246/0)

Cobre os 8 lotes da fila nova: DX-BRACAL, MARCOS-1, RESIST-1, MAO-1, V-9,
RESIST-2 e TETO-HT.

> Os T1..T9 já foram validados. **Comece daqui.**

## Preparo

Na mesma ficha de teste, acrescente:

| Traço | Para quê |
|---|---|
| **DX Braçal** +3 em dois braços | T10 |
| **Duro de Matar** 2 e **Boa Forma** (15 pts) | T11 e T12 |
| **Ambidestria** | T13 |
| **Empatia** e a perícia **Adivinhação** | T14 |
| **Magro** (desvantagem) | T14 |

## T10 — DX Braçal (a dívida que fechou)

1. Aba **Rolagem**, logo abaixo dos atributos.
2. ✅ Aparece uma segunda caixinha:
   `DX Braçal +3 (braços agem como DX 13; não vale para combate)`.
3. Marque.
4. ✅ O **DX** passa de 10 para **13**.
5. ✅ 🔴 **O NH da Faca NÃO muda.** Continua 12. Este é o teste que importa: o
   livro proíbe a DX Braçal de ajudar perícia de combate. Se o NH subir, avise.
6. ✅ O **ST** e os outros atributos não mudam.
7. ✅ A caixinha do **ST Braçal** continua funcionando junto, independente.

## T11 — Testes exigidos pela queda de PV

Com **PV 10** na ficha.

1. Baixe o PV de 10 para **9** (um toque no menos).
2. ✅ **Nada acontece.** Arranhão não pede teste.
3. Edite o PV direto para **5**.
4. ✅ Aparece um card avisando **Ferimento grave — não cair**, com `HT` e a
   explicação de quanto se perdeu.
5. ✅ Se você tiver **Boa Forma 15 pts**, o alvo vem **+2**, e a linha embaixo
   diz de onde veio.
6. Toque no teste.
7. ✅ Rola 3d6 normalmente e o card some.
8. Baixe o PV para **0**.
9. ✅ Aparece **Manter a consciência**.
10. Baixe para **−10**.
11. ✅ Aparece **Evitar a morte (−1× PV)**, com o bônus do **Duro de Matar**.
12. ✅ 🔴 **O Duro de Matar NÃO aparece no teste de consciência**, e o Difícil de
    Subjugar não aparece no de morte. São vantagens diferentes.
13. **Cure** o personagem (suba o PV).
14. ✅ **Nenhum** teste novo aparece. Curar nunca dispara nada.
15. Toque em **Dispensar** com um teste na tela.
16. ✅ O card some sem rolar.

## T12 — Estado de PV e PF

1. Deixe o PV em **3** (de 10).
2. ✅ Aparece `Cambaleante — Deslocamento e Esquiva pela metade`.
3. Baixe o **PF** para **3** (de 10).
4. ✅ Aparece também `Cansado — ST e DX caem pela metade`.
5. ✅ O PF **não** pede rolagem — só avisa. É a diferença: PV rola, PF informa.
6. Volte os dois ao máximo.
7. ✅ Os avisos somem.

## T13 — Botão "Reação e Resistência" e a mão inábil

1. Aba **Rolagem**: entre *Perícias* e *Rolagem Livre* há um botão novo,
   **Reação e Resistência**.
2. ✅ O card de **Teste de Reação** e o de **Autocontrole** **não estão mais** no
   fim da tela — mudaram para dentro do diálogo.
3. Abra o botão.
4. ✅ Lá dentro estão: Reação, Autocontrole, e os grupos **Corpo**, **Mente**.
5. ✅ Em Corpo: manter consciência, evitar a morte, doença, veneno, esforço.
6. ✅ Em Mente: Verificação de Pânico e resistir a Intimidação, saindo da
   **Vontade** (não do HT).
7. ✅ A Verificação de Pânico diz que **NÃO é disparada por dano**.
8. ✅ Com **Boa Forma**, todos os de Corpo sobem +2 e os de Mente **não**.
9. Feche e olhe o card de **Ataque**.
10. ✅ Há uma caixinha `Mão hábil`.
11. Marque-a.
12. ✅ Com **Ambidestria** na ficha, o texto vira
    `Mão inábil — sem penalidade (Ambidestria)` e **não mostra −4**.
13. Remova a Ambidestria e marque de novo.
14. ✅ Agora diz `Mão inábil (-4)`, e rolar o ataque aplica o −4.

## T14 — Declarações novas e o teto de HT

1. Com **Empatia** e **Adivinhação** na ficha, abra **Perícias**.
2. ✅ O card mostra a nota `+3 Empatia`.
3. Adicione **Natação** e a desvantagem **Gordo**.
4. ✅ Nota `+3 Gordo` na Natação — e `-2 Gordo` no Disfarce.
5. Com **Flexibilidade**, adicione **Arte Erótica**.
6. ✅ Nota `+3 Flexibilidade` (era o furo corrigido; antes só Escalada e Fuga
   tinham).
7. Adicione **Magro** e deixe a **HT em 15**.
8. ✅ No topo da aba **Traços** aparece um aviso vermelho:
   `⚠ Magro limita a HT em 14, e a ficha está com 15.`
9. ✅ 🔴 **A ficha continua funcionando.** É aviso, não bloqueio.
10. Baixe a HT para 14.
11. ✅ O aviso some.

## T15 — A trava do Abascanto

1. Adicione **Aptidão Mágica**.
2. Tente adicionar **Abascanto (Resistência à Magia)**.
3. ✅ 🔴 O app **recusa**, com a mensagem explicando o motivo e citando a p.85.
4. Remova a Aptidão Mágica e adicione o Abascanto.
5. ✅ Agora entra normalmente.
6. Tente adicionar a Aptidão Mágica de volta.
7. ✅ Recusa também — a trava vale **nos dois sentidos**.
8. Com o Abascanto nível 3, abra **Reação e Resistência**.
9. ✅ Aparece um card `Resistência à Magia 3`, dizendo que o mago sofre −3 e que
   você deve informar ao Mestre.
10. ✅ E um teste novo no grupo **Sobrenatural**: resistir a elixir mágico.


---

# Roteiro — Lotes de 29/07/2026 (versoes 3.9 a 4.5)

## T-T · Os dez Talentos (TAL-1)

- **T-T1** — Compre **Artifice nivel 2** (20 pts). Abra Pericias e confira que
  **Engenharia/NT, Mecanica/NT, Alvenaria, Carpintaria, Armeiro/NT,
  Eletricista/NT, Maquinista/NT, Ferreiro/NT e Conserto de Equipamento
  Eletronico/NT** subiram **+2**. Antes deste lote nao subiam nada.
- **T-T2** — Toque no NH de Engenharia e confira a **notinha de origem**: tem de
  dizer "Artifice +2".
- **T-T3 · a sobreposicao** — Some **Habilidade Matematica nivel 3**. Engenharia
  tem de ir para **+5**, e a notinha tem de mostrar **as duas linhas**. O livro
  permite passar de +4 so nesse caso.
- **T-T4** — No card de **Reacao**, tem de aparecer uma caixinha do Talento, com a
  plateia certa ("de qualquer pessoa para quem ele trabalha"). Marcada, soma +2.
- **T-T5** — Compre **Agente Cativante** (ela nao existia antes). A caixinha de
  reacao dele tem de dizer que o bonus **some** se ele estiver manipulando.
- **T-T6 · as tres caixinhas novas** — Compre **Toque Sensivel**, **Venturoso** e
  **Versatil**. Abra **qualquer** pericia: as tres caixinhas tem de aparecer la
  (+4, +1, +1). Confira em duas pericias bem diferentes, ex. Escalada e Cirurgia.
- **T-T7 · o que NAO pode acontecer** — Com o Venturoso comprado, abra
  **Esquiva/Apara/Bloqueio** e o card de **Reacao**: a caixinha dele **nao pode**
  aparecer em nenhum deles. O livro fala de "testes de habilidade".
- **T-T8** — Compre **Visao Hiperespectral**: **Observacao, Pericia Forense/NT,
  Revistar e Rastreamento** tem de subir **+3**. Antes subia nada.

## T-L · Luz da cena e deslocamentos (LUZ-1 / DESL-1)

- **T-L1** — Na Rolagem, o card **"Luz da cena"** comeca em *Boa luz* com **0**.
- **T-L2 · o exemplo do livro** — Compre **Visao Noturna 4** e leve a luz ate
  **-7**: o card tem de mostrar **-3** e a conta *"Quase nada de luz -7 + Visao
  Noturna 4 -> -3"*.
- **T-L3 · o que NAO pode acontecer** — Leve a luz ate **-10 (Escuridao total)**:
  tem de mostrar **-10**, e a linha dizer que a Visao Noturna **nao vale aqui**. Se
  mostrar -6, e bug.
- **T-L4** — Compre **Visao no Escuro**: em **-10** tem de virar **0**.
- **T-L5** — Com a luz em -5, role um **ataque** e uma **defesa**: os dois tem de
  sair com o -5 no log.
- **T-L6** — Compre **Voo**: na aba Geral aparece **"Voando"** = Velocidade Basica
  x 2. Com DX 13/HT 10 (Velocidade 5,75) tem de dar **11**, nao 12.
- **T-L7** — Compre **Super Escalada 3**: aparece **"Escalando"** = Deslocamento
  + 3.

## T-M · Golpe Rapido e apara repetida (MESTRE-1)

- **T-M1** — Selecione um ataque **corpo a corpo** e segure o NH: a caixinha
  **"Golpe Rapido"** tem de estar la, dizendo **-6**.
- **T-M2** — Selecione **Arcos** e segure o NH: a caixinha do Golpe Rapido **nao
  pode** aparecer.
- **T-M3** — Compre **Treinado por um Mestre**: o rotulo tem de virar **-3** e
  citar a vantagem.
- **T-M4** — O painel **"Apara no do turno"** tem de aparecer (se a ficha tiver
  Apara) e comecar em **1** com **0**.
- **T-M5 · os quatro degraus** — Leve para a **4a apara**. Sem vantagem e com arma
  comum: **-12**. Com Treinado por um Mestre: **-6**. Com rapieira e sem a
  vantagem: **-6**. Com **rapieira + Treinado por um Mestre**: **-3**.
- **T-M6** — Com a apara em 4, role a **Esquiva**: ela **nao pode** levar a
  penalidade. So a Apara leva.

## T-S · Sorte (SORTE-1)

- **T-S1** — Sem a vantagem, o painel da Sorte **nao aparece**.
- **T-S2** — Compre **Sorte (15 pts)**. Antes de rolar qualquer coisa, o botao diz
  para rolar algo primeiro.
- **T-S3 · o sinal certo** — Role uma **pericia** e toque **"Usar Sorte"**. O log
  tem de mostrar as tres jogadas e ficar com a **MENOR**. Se ficar com a maior, e
  bug.
- **T-S4 · o sinal invertido** — Role **Dano** e use a Sorte: ai tem de ficar com
  a **MAIOR**.
- **T-S5** — Depois de usar, o botao desabilita e diz **quantos minutos faltam**
  (60 para a Sorte comum).
- **T-S6** — Troque para **Sorte Impossivel (60 pts)**: o relogio tem de virar
  **10 min**.

## T-A · Apontar, Precisao e Telescopica (MIRA-3)

- **T-A1** — Com **Arcos**, segure o NH: a caixinha **"Apontei 1 turno"** tem de
  aparecer com a Precisao do arco (o Arco Longo tem Prec 3).
- **T-A2** — Marcada, todos os 13 numeros da lista sobem **+3**.
- **T-A3** — Compre **Visao Telescopica 2** e ponha o alvo a **100 m (-10)**: sem
  Apontar ela cancela **2**; com Apontar, **4** — e o rotulo diz *"Precisao +3 e
  Telescopica +4"*.
- **T-A4 · sobra nao vira bonus** — Ponha o alvo a **3 m (-1)** com Visao
  Telescopica 2: ela tem de cancelar **1**, nao 2.
- **T-A5** — Numa **espada**, a caixinha do Apontar **nao pode** aparecer.

## T-V · Visualizacao (VIS-1)

- **T-V1** — Sem a vantagem, o botao **nao aparece**.
- **T-V2** — Compre **Visualizacao** e abra: tem de trazer o aviso de que **nao
  vale em combate**.
- **T-V3 · os tres arredondamentos** — Role o IQ ate tirar **margem 7**. Os tres
  botoes tem de mostrar **+7 / +3 / +2**.
- **T-V4 · o piso que existe** — Com **margem 1**, o botao *Parecido* tem de dar
  **+1** (a conta daria 0, mas o livro garante o minimo).
- **T-V5 · o piso que NAO existe** — Com **margem 2**, o botao *Muito diferente*
  tem de dar **0**, e explicar que aqui nao ha minimo.
- **T-V6** — Guarde o bonus: ele tem de ficar **a vista** na Rolagem, com a conta,
  ate voce tocar em **Limpar**.

## T-I · Talento Instintivo (TI-1)

- **T-I1** — Sem a vantagem, o botao **nao aparece**.
- **T-I2** — Compre **Talento Instintivo nivel 2**: o botao diz **"2 de 2 nesta
  sessao"**.
- **T-I3** — Abra: a lista tem de trazer **so pericias que o personagem nao tem**,
  em ordem alfabetica, com campo de busca.
- **T-I4** — Cada linha rola o **atributo da pericia**: Arrombamento pela DX,
  Programacao pela IQ, Natacao pela HT.
- **T-I5** — Compre **Escalada** na ficha e reabra: **Escalada nao pode** aparecer
  na lista.
- **T-I6** — Role duas vezes: o contador chega a **0 de 2** e a lista para de
  aceitar toque. **"Nova sessao"** devolve os dois usos.

---

# Roteiro — Desvantagens, versoes 4.8 a 5.0 (30/07/2026)

Cobre os **sete lotes** do plano de desvantagens. Nada disso foi validado no
aparelho ainda.

> **Ficha de teste sugerida.** Quase tudo aqui depende de comprar a desvantagem.
> Vale montar **uma ficha descartavel** com DX 10, IQ 10, HT 10 e ir comprando o
> que cada bloco pede, em vez de sujar a ficha de jogo.

## T-P · Pares proibidos (D-PAR)

- **T-P1 · o mais grave** — Compre **Paralisia Frente ao Combate**. Agora tente
  comprar **Reflexos em Combate**: tem de **recusar**, citando a **p.153**.
- **T-P2 · a volta** — Numa ficha nova, compre **Reflexos em Combate** primeiro e
  tente a **Paralisia**. Tem de recusar igual — a ordem nao pode importar.
- **T-P3 · os dois lados desvantagem** — Compre **Oblivio** e tente **Pouca
  Empatia**. Tem de recusar (e o primeiro par em que as DUAS sao desvantagem).
- **T-P4 · o que o livro PERMITE** — Compre **Suscetibilidade a Magia** e depois
  **Aptidao Magica**. Tem de **deixar** — o livro autoriza as duas na mesma frase.
  Ja o **Abascanto** tem de ser recusado.
- **T-P5 · ficha antiga** — Se voce tiver alguma ficha salva com um par proibido,
  abra ela. Tem de **abrir normalmente**, sem perder traco nenhum. A trava vale
  para ADICIONAR, nunca para ficha ja salva.

## T-E · Espelhos (D-ESPELHO)

Todos no botao **Reacao e Resistencia**.

- **T-E1 · Fora de Forma** — Compre **Fora de Forma (-5)**: **todos** os testes de
  Corpo caem **1**. Troque para **-15**: caem **2**. Os de Mente **nao mudam**.
- **T-E2 · o contraste** — Compre **Facil de Matar 3**. So **"Evitar a morte"**
  pode cair (para 7). Veneno, doenca, esforco e consciencia ficam em **10**.
- **T-E3 · Suscetivel** — Compre **Suscetivel nivel 2**. So **doenca e veneno**
  caem para 8, e a linha tem de trazer o aviso *"Confirme com o Mestre"*.
- **T-E4 · Temor** — Compre **Temor 3** com IQ 12. **Verificacao de Panico** e
  **Resistir a Intimidacao** caem para **9**. Veneno continua 10.
- **T-E5 · o piso de 3** — Compre **Temor 20**. Os dois testes de mente tem de
  parar em **3**, nunca abaixo. *(Este era o bug: "Resistir a Intimidacao"
  descia para numero negativo.)*
- **T-E6 · o card que mentia** — Compre **Suscetibilidade a Magia 3**. O card tem
  de sair em **vermelho**, dizendo *"O mago ganha +3 (...) e voce sofre -3"* — e
  NAO a frase da Resistencia.
- **T-E7** — Troque para **Abascanto 4**: o card volta ao texto normal,
  *"O mago sofre -4"*, sem vermelho.

## T-LI · Listas de pericia e reacao (D-LISTA)

- **T-LI1 · Timidez** — Compre **Timidez (-5)**: Labia, Diplomacia, Atuacao e as
  outras 12 caem **1**. Troque para **-10** (caem 2) e **-20** (caem **4**).
- **T-LI2 · Gagueira** — Compre: **Atuacao, Canto, Diplomacia, Labia, Oratoria e
  Sex Appeal** caem 2. ⚠️ **Politica NAO pode cair** — o livro nao poe Politica
  do lado da desvantagem, so do lado da Voz Melodiosa.
- **T-LI3 · Pouca Empatia** — Compre: 16 pericias caem 3, entre elas
  **Criminologia/NT** (confira essa, e a que quase ficou muda).
- **T-LI4 · Insensivel** — Pedagogia cai **3 direto**. E no diálogo de pericias
  tem de aparecer **caixinha** de *+1 Interrogatorio* e *+1 Intimidacao* "com
  ameaca ou tortura" — bonus dentro de uma desvantagem.
- **T-LI5 · Mao Fraca nivel 2** — **Todas** as pericias de arma de **corpo a
  corpo** caem **4**, mais Escalada e Acrobacia. ⚠️ **Arco e Besta NAO podem
  cair**.
- **T-LI6 · o teto** — Ponha Mao Fraca no nivel **5**: a penalidade tem de parar
  em **-6** (o livro trava em 3 niveis).
- **T-LI7 · reacao** — Compre **Mau Cheiro**: -2 fixo no Teste de Reacao. Compre
  **Megalomania**: tem de virar **duas caixinhas**, +2 e -2, e nao somar sozinho.

## T-NA · Numero de Autocontrole (D-NA)

- **T-NA1 · Covardia** — Compre **Covardia com NA 9**. Em *Reacao e Resistencia*,
  a **Verificacao de Panico** continua no valor normal, e aparece uma
  **caixinha** *"Covardia -3 — quando houver risco de dano fisico"*. Marque: o
  numero cai 3. Desmarque: volta.
- **T-NA2 · o NA manda** — Troque o NA para **6**: a caixinha vira **-4**. Para
  **15**: vira **-1**. *(NA baixo e PIOR — e o contrario do resto do GURPS.)*
- **T-NA3 · Xenofilia** — Compre **Xenofilia NA 12**: caixinha de **+2** no
  Panico. E a unica tabela do livro que sobe.
- **T-NA4 · as duas juntas** — Com Covardia e Xenofilia na mesma ficha: **duas
  caixinhas separadas**, e o alvo base sem mexer. Elas nao podem se anular.
- **T-NA5 · Fobias** — Compre **Fobias NA 9**. No diálogo de pericias tem de
  aparecer caixinha de **-3 em qualquer pericia**, "enquanto a causa do medo
  persistir".
- **T-NA6 · sem NA** — Se der para cadastrar a Covardia **sem** numero de
  autocontrole, a caixinha tem de sumir (nao pode chutar -4).

## T-ES · Interruptor de estado (D-ESTADO)

O painel novo fica **logo abaixo do ST/DX Bracal**, na aba Rolagem.

- **T-ES1 · so aparece com a desvantagem** — Ficha sem nenhuma das nove: o painel
  **nao existe**. Compre **Dor Cronica**: aparece uma linha.
- **T-ES2 · o ciclo** — Toque na linha da Dor Cronica **quatro vezes**. Tem de
  passar por **Suave (-2) → Grave (-4) → Excruciante (-6) → desligado**, e o
  rotulo dizer o grau e o numero em cada parada.
- **T-ES3 · DX e IQ caem juntos** — Com a Dor Cronica em **Grave**, os numeros de
  **DX e IQ** no painel de atributos tem de estar **4 mais baixos**.
- **T-ES4 · o terceiro alvo** — Ainda em Grave, abra **Reacao e Resistencia**: o
  **Autocontrole** de qualquer outra desvantagem tem de estar **-4**.
- **T-ES5 · Lunatico** — Compre e ligue: so a **Vontade** cai 2. ⚠️ **IQ NAO pode
  cair** (e Percepcao tambem nao).
- **T-ES6 · Sangue Frio** — Ligue no grau 3: **DX -3**, e a linha de resumo tem de
  dizer **"Desloc. -3"**. *(⚠️ O botao `Desloc.` da aba Geral ainda NAO desconta
  isso — esta anotado como pendencia.)*
- **T-ES7 · Enjoo e as pericias** — Ligue **Enjoo / Vomitando**: role qualquer
  pericia. O NH tem de sair **5 mais baixo**.
- **T-ES8 · dois ao mesmo tempo** — Ligue **Dor Cronica Suave** e
  **Supersensitivo grau 2**: a DX tem de cair **4** (2 + 2), e o resumo nomear
  os **dois**.
- **T-ES9 · a notinha** — Com qualquer coisa ligada, a linha de resumo em vermelho
  tem de nomear o traco, o grau e o total. Sem nada ligado, ela **some**.
- **T-ES10 · TalkBack** — Passe o leitor pelas linhas: cada uma tem de anunciar o
  grau atual, **o que o proximo toque faz** e **quando** o estado vale.

## T-MI · Mira e mao (D-MIRA)

- **T-MI1 · Zarolho a distancia** — Compre **Zarolho**, segure o NH de um **arco**
  para abrir a Mira. Tem de aparecer, em vermelho, *"Zarolho -3 a distancia —
  Apontar cancela"*, e o NH cair 3.
- **T-MI2 · Apontar cancela** — Marque **Apontei**: a linha vira *"Zarolho -1 —
  Apontar cancelou o -3"*. ⚠️ O **-1 continua** — nao pode zerar.
- **T-MI3 · corpo a corpo** — Abra a Mira de uma **espada**: tem de dizer
  *"Zarolho -1 em combate e coordenacao mao-olho"*, e nunca -4.
- **T-MI4 · Assassino Relutante** — Compre **Pacifismo** na opcao de **-5 pontos**.
  Na Mira aparece a caixinha *"Ataque letal contra uma pessoa que eu consigo
  ver"*. Marque: NH cai **4**, e aparece a segunda caixinha do rosto.
- **T-MI5 · sem ver o rosto** — Desmarque *"Consigo ver o rosto"*: a penalidade
  tem de virar **-2**.
- **T-MI6 · o Apontar bloqueado** — Com o ataque letal marcado, a caixinha
  **Apontei** tem de ficar **apagada e nao clicavel**, dizendo que o Assassino
  Relutante nao pode Apontar.
- **T-MI7 · 🔴 a combinacao** — Ficha com **Zarolho E Assassino Relutante**,
  arco, ataque letal marcado: o **-3 do Zarolho fica inteiro** (porque ele nao
  pode Apontar) **e** o -4 do Pacifismo entra por cima. NH total **-7**.
- **T-MI8 · outras variantes do Pacifismo** — Troque para **-10, -15 ou -30**: a
  caixinha do ataque letal **nao pode aparecer**.
- **T-MI9 · Sem Um Dedo** — Compre na opcao de **-2**: no bloco de Ataque aparece
  uma segunda caixinha *"E esta a mao sem um dedo (-1)"*. Marque: NH cai 1.
- **T-MI10 · o polegar** — Troque para a opcao de **-5**: a caixinha tem de dizer
  *"a mao sem o polegar (-5)"* e descontar **5**.
- **T-MI11 · soma com a mao inabil** — Marque **mao inabil** E **a mao sem o
  polegar**: -4 e -5 tem de **somar**, dando **-9**.
- **T-MI12 · Invertebrado** — Compre e abra o botao **Desloc.**: os pesos-limite
  da tabela de carga tem de cair para **1/4**, e o cabecalho avisar que a BC de
  **empurrar** continua inteira.

## T-CR · Completamente Desastrado (D-CRIT)

- **T-CR1** — Compre **Completamente Desastrado**. Role uma pericia de **DX**
  (Espada Curta, Furtividade) ate **falhar por pouco**. O resultado tem de sair
  **"Falha Critica!"**, com a frase *"Completamente Desastrado: todo fracasso em
  DX e critico (MB p.133)"* colada.
- **T-CR2 · vai para o Discord** — Confira que a mesma frase aparece na mensagem
  enviada ao canal — e o lembrete para o Mestre.
- **T-CR3 · ⚠️ so DX** — Role uma pericia de **IQ** (Teologia, Alquimia) e falhe:
  tem de sair **"Falha"** comum, sem aviso nenhum.
- **T-CR4 · ⚠️ sucesso nao muda** — Role e **passe**: tem de sair "Sucesso"
  normal. E um **3 ou 4** tem de continuar "Sucesso Critico".
- **T-CR5 · sem ruido** — Falhe com um **18**: tem de sair "Falha Critica!"
  **sem** a frase do Desastrado — nesse caso o numero ja explica sozinho.
- **T-CR6 · o nivel barato** — Troque para **Desastrado** (-5 pontos): **nada**
  pode mudar. So o de -15 tem numero.
- **T-CR7 · o atributo** — Role o **DX** direto no painel de atributos e falhe:
  tem de virar falha critica tambem.
- **T-CR8 · pericia com especializacao** — Se tiver **Faca (Arremesso)** ou
  parecida, role e falhe: tem de pegar igual. *(Era a armadilha do lote — o
  rotulo com parenteses.)*

---

# Roteiro — Perícias, versões 5.1 e 5.2 (30/07/2026)

Cobre o **P-CRUZ** (furos da conferência cruzada), o **P-EQUIP** (seletor de
equipamento), o **P-CULT** (cultura estrangeira) e o **P-SIT** (situações da
perícia). Nada foi validado no aparelho.

> **Ficha de teste sugerida.** Uma ficha descartável com DX/IQ/HT 10 e as
> perícias que cada bloco pede — quase tudo aqui aparece no **diálogo de
> Perícias** da aba Rolagem.

## T-PC · Os furos que o P-CRUZ fechou

- **T-PC1 · o maior** — Compre **Noção Tridimensional do Espaço** e tenha
  **Percepção do Corpo** e **Navegação/NT** na ficha. As duas tem de mostrar
  **+3**. *(Antes ficavam em zero: quem pagava 10 pontos perdia os bônus que o
  traço de 5 pontos da.)*
- **T-PC2 · especializacao** — Com a mesma vantagem, **Acrobacia Aérea** tem de
  mostrar **+2**. Confira que **Acrobacia** (a base) continua **+2** também.
- **T-PC3 · Deslumbrar** — Compre **Pouca Empatia** e adicione
  **Deslumbrar (Persuadir)**: tem de aparecer **−3**. *(Antes só a base
  "Deslumbrar" pegava.)*
- **T-PC4 · Carisma** — Compre **Carisma 2**: **Diplomacia, Intimidação, Lábia,
  Manha, Sex Appeal e Trato-Social** tem de subir **+2** cada. E as quatro
  antigas (Adivinhação, Liderança, Mendicância, Oratória) continuam **+2**.
- **T-PC5 · Sensível** — Compre **Sensível** (o de 5 pontos, não a Empatia):
  **Detecção de Mentiras** e **Adivinhação** tem de mostrar **+1**, e
  **Psicologia** vira **caixinha** ("ao analisar alguém com quem dá para
  conversar").
- **T-PC6** — **Lamentável**: **+3** em Mendicância, e o **+3 de reação**
  continua no diálogo de Reação.
- **T-PC7** — **Equilíbrio Perfeito**: **+4** em Postura Imóvel, e os **+1** de
  Acrobacia, Escalada e Pilotagem continuam.
- **T-PC8** — **Pele Elástica** dá **+4** em Disfarce; **Memória Eidética** dá
  **+5** e **Memória Fotográfica** dá **+10** em **Leitura Dinâmica**.
- **T-PC9 · o acento** — Confira que a perícia aparece escrita **"Leitura
  Dinâmica"** (com o **â**) e **"Auto-Hipnose"** (com hífen). ⚠️ Se você tiver
  ficha antiga com essas duas, **confira se o bônus aparece** — o nome mudou, e
  ficha salva guarda o nome antigo.
- **T-PC10** — **Escorregadio 3**: em **Fuga** tem de aparecer **caixinha** de
  **+3** ("para se soltar de amarras, escapar de agarrão ou passar por abertura
  estreita"), e **não** um bônus fixo.

## T-EQ · Seletor de equipamento (P-EQUIP)

- **T-EQ1 · só aparece se precisar** — Ficha sem nenhuma das 32 perícias de
  equipamento: o seletor **não existe** no topo do diálogo. Adicione
  **Arrombamento/NT**: ele aparece.
- **T-EQ2 · o padrão não mexe em nada** — Ele começa em **"Básico"**, e nenhuma
  perícia pode ter número de equipamento nenhum.
- **T-EQ3 · o giro** — Toque cinco vezes: **Sem equipamento → Improvisado →
  Básico → Boa qualidade → Qualidade superior** e volta.
- **T-EQ4 · 🔴 a coluna dupla** — Ponha em **"Sem equipamento"** com
  **Cirurgia/NT** e **Alvenaria** na ficha. Cirurgia tem de mostrar **−10** e
  Alvenaria **−5**. *(É o dobro, e é a pegadinha da tabela.)*
- **T-EQ5 · o bônus é igual para as duas** — Ponha em **"Qualidade superior"**:
  as duas tem de mostrar **+2**.
- **T-EQ6 · ⚠️ não vaza** — Com o seletor em **"Sem equipamento"**, role
  **Lábia** ou **Escalada**: o NH **não pode** mudar. Lábia não piora por o
  personagem estar de mãos vazias.
- **T-EQ7 · a conta chega na rolagem** — Com "Improvisado", role
  **Arrombamento/NT** e confira no log que o modificador **−5** entrou.
- **T-EQ8 · TalkBack** — O seletor tem de anunciar o degrau atual **e o que o
  próximo toque faz**.

## T-CU · Cultura estrangeira (P-CULT)

- **T-CU1 · vale para todo mundo** — Ficha **sem** Familiaridade Cultural: em
  **Trato-Social** tem de aparecer a caixinha *"cultura estrangeira **−3**"*.
  ⚠️ Se ela só aparecer para quem tem a vantagem, está invertido.
- **T-CU2 · a vantagem muda o TEXTO, não o número** — Compre **Familiaridade
  Cultural**: a caixinha continua **−3**, e o texto passa a dizer *"você tem
  Familiaridade Cultural, confirme com o Mestre se ela cobre esta"*.
- **T-CU3 · as oito** — A caixinha tem de aparecer em **Trato-Social, Dança,
  Heráldica, Poesia, Oratória, Connoisseur, Mímica/Pantomima e Jogos de
  Entretenimento** — e em mais nenhuma.
- **T-CU4 · ⚠️ não são as sociais** — Em **Lábia** e **Diplomacia** a caixinha
  **não pode** aparecer. O livro não as inclui.

## T-SI · Situações da perícia (P-SIT)

- **T-SI1 · Punga** — Adicione **Punga**: duas caixinhas, **+5** ("a vítima está
  distraída") e **+10** ("dormindo ou bêbada"). Marque a de +10 e role: o log
  tem de mostrar o **+10**.
- **T-SI2 · as quatro de chi** — **Golpe Poderoso, Pontaria Zen, Salto Voador e
  Arqueiro Zen** tem de mostrar a **mesma** caixinha: **−10**, "usado
  instantaneamente, sem Concentrar".
- **T-SI3 · aparar** — **Boxe** e **Sumô** tem **duas** caixinhas (−2 chute, −3
  arma); **Briga** e **Luta Greco-Romana** tem **só uma** (−3 arma).
- **T-SI4 · Furtividade** — Duas caixinhas de **−5**: "sem esconderijo" e
  "movendo acima de Deslocamento 1".
- **T-SI5 · Adestramento de Animais** — Três caixinhas: **−5**, **−5** e
  **−10** ("animal que ataca seres humanos").
- **T-SI6 · Passos Leves** — Uma caixinha de **−8** (papel de arroz).
- **T-SI7 · ⚠️ a maioria não tem nada** — Abra **Escalada, Teologia, Natação,
  Diplomacia**: **nenhuma caixinha de situação**. Se o diálogo virar um paredão
  de caixas, alguma coisa vazou.
- **T-SI8 · 🔴 o índice das caixinhas** — Numa perícia com **várias** fontes ao
  mesmo tempo — o melhor caso é **Trato-Social** com **Timidez** na ficha, a
  **cultura estrangeira** e a **luz da cena** baixa — marque **só a do meio** e
  role. O número somado tem de ser **o daquela caixinha**, não o de outra.
  *(Era o risco real do lote: as caixinhas casam por posição.)*
- **T-SI9 · Mergulho soma as duas** — Com **Mergulho/NT**, ponha o seletor em
  **"Improvisado"** (−5) **e** marque a caixinha "aparelho que ele nunca usou"
  (−2). O total tem de ser **−7**. As duas medem coisas diferentes: qualidade e
  familiaridade.
- **T-SI10 · a caixinha não gruda** — Marque uma caixinha, **feche** o diálogo e
  abra de novo: ela tem de voltar **desmarcada**. A condição vale para aquela
  rolagem, não para sempre. ⚠️ O **seletor de equipamento**, ao contrário,
  **continua** no degrau que você deixou — ele é da sessão.

---

# Lotes ARMA-1 a ARMA-5 — a ficha técnica das armas

> Gate: **1.783 testes**, 0 falhas nas duas variantes (`visual` e `pracego`),
> lint limpo. Cobre ARMA-1 (catálogo alargado), ARMA-2 (ficha técnica),
> ARMA-3 (card na seleção), ARMA-4 (card no inventário) e ARMA-5 (mira acoplada
> e o conflito arma × perícia).
>
> ⚠️ **O gesto de adicionar arma mudou.** Antes, tocar na arma da lista já a
> jogava no inventário. Agora o toque **abre a ficha técnica**, e o botão
> *Adicionar ao inventário* fica dentro dela. São dois toques em vez de um.

## Preparo

Uma ficha com **ST 11** e estas armas no inventário (aba Equipamentos →
Adicionar Arma):

| Arma | Onde achar | Serve para testar |
|---|---|---|
| **Rifle de Atirador, .338** | Armas de Fogo → Rifles | mira acoplada (Prec 6+3) |
| **Revólver, .36** | Armas de Fogo → Pistolas | o Máx de milhar |
| **Arco Longo** | Distancia | alcance por múltiplo de ST |
| **Katana** | Corpo a corpo | os dois modos de ataque |
| **Adaga** | Corpo a corpo | o conflito arma × perícia |

E na aba Perícias: **Armas de Fogo/NT (Pistola)** e **Arcos**.

---

## T-AR · O card de detalhe na seleção (ARMA-3)

- **T-AR1 · o toque abre, não adiciona** — Equipamentos → **Adicionar Arma** →
  toque na **Katana**. ✅ Abre um card com a ficha técnica. ✅ A Katana **não**
  entrou no inventário ainda.
- **T-AR2 · o botão adiciona e fecha tudo** — No card, toque em **Adicionar ao
  inventário**. ✅ A Katana aparece na lista de Armas e os dois diálogos fecham.
- **T-AR3 · Fechar não adiciona** — Abra outra arma e toque em **Fechar**.
  ✅ Ela **não** entra no inventário.
- **T-AR4 · o card da lista não mudou** — A lista continua com nome, ST, tipo,
  dano, custo e peso. Nada de novo ali.

## T-AF · A ficha de uma arma de fogo

Abra o **Rifle de Atirador, .338**.

- **T-AF1 · 🔴 a mira acoplada aparece** — Na linha **Precisão**: ✅ o valor é
  **6 +3** e a explicação diz *"6 da arma e +3 da mira acoplada — só valem se
  você Apontar"*. *(Este +3 o app jogava fora desde sempre.)*
- **T-AF2 · o selo de CL** — No alto à direita: ✅ aparece o **CL** da arma com
  o nome certo. ⚠️ **CL 1 é militar, CL 2 é restrito** — se aparecer "CL 2 ·
  militar", está errado.
- **T-AF3 · Tiros vira frase** — ✅ A linha **Tiros** mostra o valor cru e
  embaixo *"N tiros, M turnos para recarregar"*.
- **T-AF4 · Recuo e Magnitude explicados** — ✅ **Recuo** diz a regra do tiro
  múltiplo; ✅ **Magnitude** diz que penaliza Avançar e Atacar e ocultar a arma.
- **T-AF5 · o peso da munição** — ✅ A linha **Peso** mostra **dois** números
  (`2,3 kg + 0,5 kg`) e diz que o segundo é a munição.
- **T-AF6 · 🔴 o Máx de milhar** — Abra o **Revólver, .36**: ✅ o alcance mostra
  **1300 m** de máximo. *(Antes o app não sabia o Máx de nenhuma pistola: o
  ponto de milhar quebrava a leitura, e o aviso "fora de alcance" nunca podia
  disparar.)*

## T-AD · A ficha de uma arma à distância

Abra o **Arco Longo** (com a ficha de **ST 11**).

- **T-AD1 · a conta feita** — ✅ Alcance mostra **×15/×20** e embaixo *"com a
  sua ST 11 → 165 / 220 m"*.
- **T-AD2 · muda com a ST** — Suba a ST para **14** na aba Geral e abra de novo:
  ✅ agora diz **210 / 280 m**.
- **T-AD3 · sem Recuo é travessão** — ✅ A linha **Recuo** mostra **—**, não
  **0**. Zero diria "esta arma não coiceia"; o livro simplesmente não cadastrou.
- **T-AD4 · duas mãos** — ✅ A linha **ST mínima** mostra **11 †** e explica
  *"usa as duas mãos"*.

## T-AC · A ficha de uma arma corpo a corpo

Abra a **Katana**.

- **T-AC1 · 🔴 os DOIS ataques** — ✅ Aparece a seção **Modos de ataque** com
  **GeB+1 corte** e **GdP+1 perf**. *(A estocada da Katana nunca existiu no app:
  o carregador lia só o primeiro modo e parava.)*
- **T-AC2 · o alcance de cada modo** — ✅ O corte diz **Alcance 1, 2**; a
  estocada diz **Alcance 1**.
- **T-AC3 · ⚠️ mesma arma, não custa de novo** — ✅ O 2º modo traz a linha
  *"mesma arma — não custa nem pesa de novo"*, e o **Custo** do item continua
  **$650** (um só).
- **T-AC4 · três modos** — Abra a **Alabarda**: ✅ **três** modos de ataque.
- **T-AC5 · 🔴 a ST que faltava** — Ainda na Alabarda: ✅ a linha **ST mínima**
  mostra **13‡ / 12** e explica que o livro dá uma ST por modo. *(Antes ficava
  só um travessão — a alabarda não tinha ST nenhuma na tela.)*
- **T-AC6 · o ‡ não é o †** — ✅ A explicação da Alabarda fala em ficar
  **despreparada** depois de atacar. Só o † não diz isso.
- **T-AC7 · arma de um modo só** — Abra o **Machado**: ✅ **não** aparece uma
  seção "Modos de ataque" com um item só.

## T-AI · O card a partir do inventário (ARMA-4)

- **T-AI1 · o toque no nome abre** — Na aba Equipamentos, em **Armas**, toque no
  **nome** da Katana: ✅ abre a mesma ficha técnica.
- **T-AI2 · só leitura** — ✅ **Não** existe botão de adicionar. A arma já está
  na ficha.
- **T-AI3 · o lápis continua editando** — ✅ Tocar no **lápis** ainda abre
  *Editar Equipamento*, como antes.
- **T-AI4 · arma fora do catálogo** — Crie uma arma à mão (Adicionar Itens, com
  dano preenchido) e abra: ✅ o card diz **"Arma fora do catálogo"** e explica
  que só mostra o que está gravado na ficha. Não pode abrir vazio nem travar.

## T-MI · A mira acoplada na Rolagem (ARMA-5)

- **T-MI1 · a caixinha aparece** — Aba Rolagem → ataque **Armas de Fogo/NT
  (Pistola)** → fonte de dano no **Rifle de Atirador** → **segure** o NH para
  abrir *Onde acertar* → toque no **Apontar**. ✅ Embaixo do Apontar aparecem
  **duas** caixinhas: *arma firmada* e *usando a mira acoplada: +3*.
- **T-MI2 · a mira já vem marcada** — ✅ Quem tem luneta está usando a luneta;
  desmarcar é a exceção.
- **T-MI3 · o número muda ao desmarcar** — ✅ Desmarcando a mira, todos os NH da
  lista caem **3 pontos**.
- **T-MI4 · ⚠️ some sem apontar** — Volte o Apontar para **nenhum turno**:
  ✅ as duas caixinhas **somem**. A Prec só existe apontando.
- **T-MI5 · arma sem mira não mostra a caixinha** — Troque a fonte de dano para
  o **Revólver, .36**: ✅ a caixinha de mira **não** aparece (só a de firmada).
- **T-MI6 · o teto do livro ainda vale** — Com o rifle, **3 turnos** + firmada +
  mira: ✅ o rótulo mostra as parcelas e, se cortar, diz **"teto de 12 (dobro da
  Prec, MB p.373)"**.

## T-CF · 🔴 O conflito arma × perícia (ARMA-5)

Este é o bug do print de 03/08.

- **T-CF1 · 🔴 o diálogo abre em modo à distância** — Ataque **Armas de Fogo/NT
  (Pistola)**, fonte de dano na **Adaga**, segure o NH. ✅ Aparecem a **linha de
  distância** e o **Apontar**. ✅ **Não** aparece o *Golpe Rápido* (que é opção
  de corpo a corpo). *(Antes abria exatamente ao contrário.)*
- **T-CF2 · o aviso está escrito** — ✅ Aparece em vermelho: *"O ataque é à
  distância, mas a fonte de dano é Adaga, que é de corpo a corpo — confira a
  arma."*
- **T-CF3 · a arma certa é encontrada** — Ainda com a Adaga na fonte de dano:
  ✅ o alcance mostrado é o **da pistola**, não "alcance não cadastrado".
- **T-CF4 · par coerente não avisa nada** — Ponha a fonte de dano no **Revólver**
  com a mesma perícia: ✅ **nenhum** aviso vermelho.
- **T-CF5 · corpo a corpo continua corpo a corpo** — Ataque de **Faca** com a
  Adaga: ✅ aparece o **Golpe Rápido**, ✅ **não** aparece linha de distância,
  ✅ nenhum aviso.

## T-AA · Acessibilidade (variante `pracego`)

- **T-AA1** — No card de detalhe, cada linha é lida **inteira** pelo TalkBack:
  rótulo, valor e explicação de uma vez. Não pode ler "Precisão", pausa,
  "6 mais 3".
- **T-AA2** — No inventário, o nome da arma anuncia *"toque para ver a ficha
  técnica completa"*.
- **T-AA3** — A caixinha da mira anuncia quanto ela soma e o que significa
  desmarcar.

---

# Lotes ARMA-6 a ARMA-9 — perícias de tiro, Magnitude e as vantagens de Atirador

> Gate: **1.822 testes**, 0 falhas nas duas variantes, lint limpo.
> ARMA-6 (perícias), ARMA-7 (Avançar e Atacar + conserto do catálogo),
> ARMA-8 (Atirador), ARMA-9 (Arqueiro Heroico).

## Preparo

Duas fichas, porque as vantagens são exclusivas por perícia:

- **Ficha A — "Pistoleiro"**: ST 11, vantagem **Atirador** (25 pts), perícias
  **Armas de Fogo/NT (Pistola)** e **Canhoneiro/NT (Canhão)**. No inventário:
  **Pistola Auto., 9 mm**, **Rifle de Atirador, .338** e uma **Adaga**.
- **Ficha B — "Arqueira"**: ST 11, vantagem **Atirador (Arqueiro Heroico)**
  (20 pts), perícia **Arcos**, com um **Arco Longo**.

---

## T-CN · 🔴 Canhoneiro deixou de abrir em corpo a corpo (ARMA-6)

- **T-CN1** — Ficha A, ataque **Canhoneiro/NT (Canhão)**, segure o NH.
  ✅ Aparece a **linha de distância** e o **Apontar**. ✅ **Não** aparece o
  *Golpe Rápido*. *(Antes abria como arma branca: sem distância, sem 1/2D, sem
  Máx, sem Apontar.)*
- **T-CN2** — Ainda em Canhoneiro: ✅ o card do ataque mostra o rótulo
  `alvo a Xm (−N)` quando você afasta o alvo.

## T-MG · A Magnitude no Avançar e Atacar (ARMA-7)

- **T-MG1 · a caixinha existe** — Ficha A, ataque de **Pistola**, segure o NH.
  ✅ Abaixo do Apontar há **Avançar e Atacar**, com o número já calculado.
- **T-MG2 · o rótulo diz de onde veio** — Com a **Pistola Auto., 9 mm**
  (Magnitude −2): ✅ o texto diz **−2** e explica que é o **padrão**.
- **T-MG3 · a arma pior manda** — Troque a fonte de dano para o **Rifle de
  Atirador, .338** (Magnitude −6): ✅ o texto passa a **−6** e diz
  *"Magnitude da arma, pior que o −2 básico"*.
- **T-MG4 · ⚠️ as duas caixinhas se excluem** — Marque **Apontar** (2 turnos) e
  depois **Avançar e Atacar**: ✅ o bônus do Apontar **some do total** e aparece
  o aviso *"Apontar desligado: não dá para acumular segundos de pontaria
  correndo"*.
- **T-MG5 · ⚠️ corpo a corpo tem TETO** — Numa perícia de arma branca com **NH
  alto (16+)**, marque *Avançar e Atacar*: ✅ o NH do torso cai para **9**, e o
  rótulo diz *"teto de 9 no corpo a corpo"*. Não é só −4.

## T-DC · 🔴 O catálogo consertado (ARMA-7)

- **T-DC1 · o rifle voltou a existir** — Ficha A (**ST 11**), Adicionar Arma →
  Armas de Fogo → busque **"Rifle de Atirador"**. ✅ Ele **aparece na lista**.
  *(Estava cadastrado com **ST 41** e era invisível para qualquer ficha normal —
  a linha da planilha tinha escorregado uma coluna inteira.)*
- **T-DC2 · a ficha técnica bate com o livro** — Abra o detalhe dele:
  ✅ **CdT 1**, ✅ **Tiros 4+1(3)**, ✅ **ST 11 †**, ✅ **Magnitude −6**,
  ✅ **Recuo 4**, ✅ selo **CL 3 · licenciado**.
- **T-DC3 · a mira continua lá** — ✅ **Precisão 6 +3** (o conserto mexeu só da
  CdT para a direita; a Prec não podia mudar).
- **T-DC4 · as outras duas** — Mesma conferência na **ACI, 6,8 mm** (CdT 15,
  Mag −5, CL 1) e no **Rifle de Gauss, 4 mm** (CdT 12, Mag −4, CL 2).

## T-AT · 🔴 A vantagem Atirador (ARMA-8)

- **T-AT1 · a Precisão sem apontar** — Ficha A, **Pistola** (Prec 2, uma mão,
  CdT 3), segure o NH **sem** marcar nada. ✅ Aparece a linha *"Atirador: +2 de
  Precisão sem precisar Apontar"* e o NH já vem **+2**.
- **T-AT2 · ⚠️ metade com duas mãos** — Troque para o **Rifle de Atirador**
  (Prec 6, duas mãos): ✅ o bônus é **+3** e o texto diz *"metade de 6, arma de
  duas mãos"*. **Arredondado para cima** — Prec 5 daria 3, não 2.
- **T-AT3 · metade também na automática** — Com uma arma de **CdT acima de 3**
  de uma mão só: ✅ ainda é metade, com o texto *"arma automática"*.
- **T-AT4 · apontando vale o cheio** — Marque o **Apontar**: ✅ a linha muda para
  *"apontando, vale o bônus cheio de Precisão"* e o número **não dobra**.
- **T-AT5 · 🔴 a troca do Avançar e Atacar** — Marque **Avançar e Atacar**:
  ✅ a penalidade da manobra **some** (o NH não cai) **e** o *+2 de Precisão
  também some*. O texto explica a troca. *(O livro diz "em vez de", não "além
  de" — se os dois valessem juntos, seria vantagem dobrada.)*
- **T-AT6 · ⚠️ não vale para arco** — Numa ficha com Atirador **e** um arco,
  ataque com **Arcos**: ✅ **nenhuma** linha de Atirador aparece. O livro exclui
  armas motoras de projétil.
- **T-AT7 · sem a vantagem nada muda** — Numa ficha **sem** Atirador: ✅ o
  diálogo é exatamente o de antes.

## T-AH · 🔴 Arqueiro Heroico (ARMA-9)

- **T-AH1 · Precisão inteira, mesmo com o arco de duas mãos** — Ficha B, ataque
  **Arcos** com o **Arco Longo** (Prec 3): ✅ *"Arqueiro Heroico: +3 de Precisão
  sem precisar Apontar"* — **+3**, não +2. Ele não leva a metade.
- **T-AH2 · 🔴 os segundos vêm um turno mais cedo** — Marque o **Apontar** com
  **1 turno**: ✅ o total soma **+1 a mais** do que a mesma arma somaria numa
  ficha sem a vantagem. Com **2 turnos**, **+2 a mais**.
  *(Regra geral: +1 só com 2 segundos. Arqueiro Heroico: +1 já com 1.)*
- **T-AH3 · não vale para besta nem funda** — ✅ Atacando com **Besta**,
  nenhuma linha de Arqueiro Heroico.
- **T-AH4 · não vale para arma de fogo** — ✅ Idem com **Armas de Fogo**.

## T-AA2 · Acessibilidade (variante `pracego`)

- **T-AA4** — A caixinha *Avançar e Atacar* anuncia quanto custa e, no corpo a
  corpo, avisa do teto de nove.
- **T-AA5** — A linha do Atirador é lida inteira, dizendo o bônus e o motivo da
  metade quando houver.

---

# Lotes LAYOUT-1 a LAYOUT-4 — o padrão de tela (vantagens e desvantagens)

> Gate: **1.830 testes**, 0 falhas nas duas variantes, lint limpo.
> LAYOUT-1 (tokens e componentes), LAYOUT-1b (botões), LAYOUT-2 (skill + teste de
> padrão), LAYOUT-3 (diálogo de configurar × editar), LAYOUT-4 (as duas listas).

## T-PD · O card das duas listas

- **T-PD1 · mesmo tamanho** — Abra *Adicionar Vantagem* e depois *Adicionar
  Desvantagem*: ✅ os cards têm a **mesma altura e o mesmo respiro**. O padding
  passou de `8/6` escrito à mão para o token `12/8`.
- **T-PD2 · o contador que faltava** — Em *Adicionar Desvantagem*: ✅ aparece
  **"N desvantagens encontradas"**. *(Era a única lista sem contador.)*
- **T-PD3 · 🔴 o `por_nivel` sumiu** — Em qualquer vantagem por nível (ex.:
  **Abafador de Mana**): ✅ o card diz **"por nível"**, não `por_nivel`.
  ✅ Confira também **"custo fixo"** (Ambidestria) e **"custo à escolha"**
  (Adaptação ao Terreno).
- **T-PD4 · o "Atual: N pts"** — Na desvantagem, ✅ ele aparece como subtítulo
  logo abaixo do título, no mesmo lugar do "ST do personagem" da tela de armas.
- **T-PD5 · já adicionada** — Uma vantagem que a ficha já tem: ✅ mostra
  **"Adicionada"** à direita e **não** abre ao tocar.

## T-DL · 🔴 Configurar × Editar — o caso do Abafador de Mana

Este é o defeito dos seus prints de 03/08. Use o **Abafador de Mana**.

- **T-DL1 · 🔴 o `−` e o `+` existem ao ADICIONAR** — Traços → Adicionar Vantagem
  → Abafador de Mana. ✅ O nível aparece como **`− 1 +`**, com botões visíveis.
  *(Antes, na variante visual, o nível só mudava **arrastando o dedo** — gesto que
  não tinha como ser descoberto.)*
- **T-DL2 · os botões funcionam** — ✅ Tocar no `+` sobe o nível e o custo
  acompanha; o `−` desce e trava em 1.
- **T-DL3 · o rótulo é o mesmo nos dois** — ✅ O campo de texto diz
  **"Descrição / Especialização"** ao adicionar **e** ao editar.
  *(Eram três grafias diferentes.)*
- **T-DL4 · a linha de modificadores existe nos dois** — ✅ Com nenhuma ampliação
  ou limitação, os dois diálogos mostram *"Nenhum modificador aplicado."*
- **T-DL5 · só o botão muda** — ✅ Ao adicionar o botão diz **Adicionar**; ao
  editar diz **Salvar**. É a única diferença que deve sobrar.

## T-TC · 🔴 O teto do catálogo na edição

- **T-TC1 · 🔴 o Artífice para em 4** — Adicione **Artífice** (Talento, máximo 4
  no livro). Suba até 4 e **salve**. Agora toque no **lápis** para editar:
  ✅ o `+` fica **desligado no 4** e aparece *"Máximo do livro para este traço: 4"*.
  *(Antes a edição perdia o teto do catálogo e deixava subir até **20**.)*
- **T-TC2 · outros dois** — Repita com **Curandeiro** (4) e **Espinhos** (3).
- **T-TC3 · Aptidão Mágica continua com o teto dela** — ✅ Para em **11**.
- **T-TC4 · vantagem sem teto no livro** — Uma vantagem por nível qualquer sem
  `max`: ✅ vai até **20**, como antes.

## T-BT · Os botões

- **T-BT1 · dá para acertar com o dedo** — Nos diálogos de vantagem e
  desvantagem: ✅ nenhum botão fica pequeno demais para tocar. O `−`/`+` do nível
  tem a área de toque cheia.
- **T-BT2 · espaço entre eles** — ✅ *Cancelar* e *Salvar* têm um espaço visível e
  **igual** entre si, e a mesma distância da borda do diálogo.
- **T-BT3 · tamanho do texto** — ✅ O texto dentro dos botões tem o mesmo tamanho
  em todos eles.
- **T-BT4 · cor** — ✅ *Salvar/Adicionar* é o botão preenchido; *Cancelar* é
  contornado; *Fechar* é só texto. Nenhum com cor fora do tema.

## T-PO · A lista de poderes

- **T-PO1** — Traços → Poderes: ✅ as linhas ficam no mesmo formato das outras, e
  o **lápis** e a **lixeira** continuam funcionando.
- **T-PO2** — ✅ Tocar no **corpo da linha** abre a edição do poder.

## T-AA3 · Acessibilidade (variante `pracego`)

- **T-AA6** — O `−` e o `+` anunciam *"Diminuir/Aumentar o nível"* e o nível
  atual. No máximo, o `+` avisa que já está no teto do livro.
- **T-AA7** — Cada linha das listas é lida inteira: nome, custo, tipo e página.
- **T-AA8** — O lápis e a lixeira dos poderes anunciam **o nome do poder**, não
  só "editar".

---

# Lote LAYOUT-5 — a tela de mágicas no padrão

- **T-MA1 · mesmo tamanho** — Abra *Adicionar Mágica* e depois *Adicionar
  Vantagem*: ✅ os cards têm a **mesma altura** e o nome do item o **mesmo
  tamanho de letra**. *(O nome da mágica era `bodyLarge`, maior que o das outras
  listas.)*
- **T-MA2 · contador** — ✅ Aparece **"N mágicas encontradas"**, que não existia.
- **T-MA3 · título** — ✅ O título tem a mesma cor dos outros diálogos, não mais
  o azul de destaque.
- **T-MA4 · 🔴 o verde no modo escuro** — Com o aparelho no **tema escuro**, ache
  uma mágica com **"✓ Requisitos Atendidos"**: ✅ o texto está **legível**.
  *(Era um verde escuro cravado no código, que sumia no fundo noturno.)*
- **T-MA5 · o "Falta:" continua vermelho** — ✅ E legível nos dois temas.
- **T-MA6 · o seletor de escola** — ✅ Continua funcionando e filtra a lista.
- **T-MA7 · Modo Alvo** — ✅ O interruptor aparece, e com ele ligado cada linha
  ganha o botão **OBJETIVO/ALVO** à direita. ✅ O card "Alvo: X" com o botão
  **Limpar** continua funcionando.
- **T-MA8 · o rodapé** — ✅ O **Fechar** está na mesma posição e distância da
  borda que nos outros diálogos.

---

# Lote LAYOUT-6 — perícias, técnicas e equipamentos no padrão

- **T-PE1 · perícia** — Abra *Adicionar Perícia* e depois *Adicionar Vantagem*:
  ✅ os cards têm a **mesma altura** e a **mesma cor de fundo**. ✅ O `DX/D`
  continua encostado na direita.
- **T-PE2 · especialização** — Uma perícia com `*` (ex.: **Armas de Fogo/NT**)
  que **já está na ficha**: ✅ continua **clicável** (dá para ter Pistola e
  Rifle), e mostra *"Já está na ficha"*.
- **T-TE1 · técnica** — *Adicionar Técnica*: ✅ mesmo card, e o
  `Gun Fu | Difícil` à direita.
- **T-TE2 · perícias suplementares** — ⚠️ O botão **"Detalhes"** não existe mais:
  ✅ tocar na **linha inteira** abre os detalhes.
- **T-EQ2a · os filtros de arma viraram botões** — *Adicionar Arma*: ✅ *Todas /
  Corpo a corpo / Distância / Armas de Fogo* agora são **chips**, com a marcação
  de qual está ativo. *(Eram texto solto e não pareciam clicáveis.)*
- **T-EQ2b · escudo** — ✅ Aparece **"N escudos encontrados"**, que não existia.
- **T-EQ2c · armadura** — ✅ O contador diz **"N armaduras encontradas"** (era
  *"Resultados: N"*), e o **Limpar filtros** está acima da lista, separado da
  contagem.
- **T-EQ2d · lado a lado** — Abra *Adicionar Arma*, feche, abra *Adicionar
  Perícia*: ✅ as duas telas parecem a mesma tela com conteúdo diferente.

---

# Lote LAYOUT-7 — o editor de equipamento

- **T-ED4a · mesma cara** — Equipamentos → Armas → toque no **lápis** de uma
  arma do catálogo: ✅ o editor abre em **tela cheia**, com o nome no alto e o
  subtítulo (`Corpo a corpo · Faca · NT 1`), igual ao card do seletor.
- **T-ED4b · a ficha do livro está lá** — ✅ Aparece a seção **"Ficha do livro"**
  com ST mínima, Alcance, Aparar, Peso, Custo e NT — **só leitura**, sem caixa
  de digitar.
- **T-ED4c · o que é seu continua editável** — ✅ Nome, peso, custo, quantidade,
  notas, e a seção **"Automação de combate (opcional)"** com Dano e ST Mín.
- **T-ED4d · salvar funciona** — Mude a **quantidade** para 3 e salve: ✅ o
  inventário mostra a mudança.
- **T-ED4e · item criado à mão** — *Adicionar Itens* → crie um item qualquer e
  edite: ✅ **não** aparece a seção "Ficha do livro" (ele não casa com o
  catálogo), e os campos funcionam normalmente.
- **T-ED4f · 🔴 o "C m" sumiu** — Abra a ficha técnica da **Adaga**: ✅ a linha
  de alcance diz **"C"**, não **"C m"**. ✅ Numa **Lança** (alcance `1, 2`)
  continua dizendo **"1, 2 m"**.
- **T-ED4g · botões** — ✅ *Cancelar* é contornado, *Salvar* é preenchido, com o
  mesmo espaço entre eles e a mesma distância da borda dos outros diálogos.

---

# Lotes MB-13, MB-2 e MB-8 — carga, rajada e enguiço

> Gate: 1.846 testes, 0 falhas nas duas variantes.

## Preparo

Uma ficha com uma **arma automática** no inventário — serve a **ACI, 6,8 mm**
(CdT 15, Recuo 2, NT 9) ou o **Rifle de Gauss, 4 mm** (CdT 12, Recuo 2, NT 10) —
e a perícia **Armas de Fogo/NT**. E uma mochila pesada para testar a carga.

## T-CG · 🔴 A carga aparece na Esquiva (MB-13)

- **T-CG1 · ficha leve não mostra nada** — Com pouco equipamento: ✅ a notinha da
  **Esquiva** **não** menciona carga. *(Carga zero não vira linha.)*
- **T-CG2 · 🔴 carregue peso** — Adicione equipamento até a aba Equipamentos
  acusar **Carga leve** (ou mais). Abra a notinha da **Esquiva**: ✅ aparece
  **"Carga leve −1"**, com o nome do livro.
- **T-CG3 · o número bate** — ✅ A Esquiva mostrada é a base **menos** o nível de
  carga. Antes o app já descontava; a diferença é que agora **diz** que descontou.
- **T-CG4 · ⚠️ só na Esquiva** — ✅ As notinhas de **Apara** e **Bloqueio** **não**
  mencionam carga. Elas saem do NH da perícia, que a carga não toca.

## T-RJ · Fogo contínuo (MB-2)

- **T-RJ1 · rajada com acerto** — Ataque com a **ACI** (CdT 15, Recuo 2) e acerte
  com folga. ✅ Abaixo do "Sucesso" aparece **"N tiros acertaram"** com a conta:
  *"margem X ÷ Recuo 2 = … , + 1 do acerto"*.
- **T-RJ2 · a conta confere** — Acertando **por 7** com Recuo 2: ✅ o texto diz
  **4 tiros**.
- **T-RJ3 · errou, nada acerta** — ✅ Numa falha, **não** aparece linha de tiros.
- **T-RJ4 · ⚠️ arma de tiro único não vira rajada** — Ataque com uma **pistola de
  CdT 1** ou com uma **espada**: ✅ **nenhuma** linha de rajada aparece.
- **T-RJ5 · o teto dos tiros disparados** — Numa arma de **CdT 3** com margem
  alta: ✅ o resultado para em **3**, e o texto diz *"limitado pelos 3 tiros
  disparados"*.

## T-MF · 🔴 Mau funcionamento (MB-8)

⚠️ **É regra opcional e nasce DESLIGADA.** Se não houver interruptor visível
ainda, este bloco fica para quando ele existir — anote e me avise.

- **T-MF1 · desligado, nada muda** — Com a regra desligada: ✅ nenhuma rolagem de
  ataque mostra enguiço, nem com 17 ou 18.
- **T-MF2 · 🔴 ligado, o 17 enguiça** — Com a regra ligada e uma arma de **NT 6+**
  (Mauf 17): role até sair **17 ou 18**. ✅ Aparece *"A arma enguiçou (saiu 17, o
  Mauf desta arma é 17)"* mais o resultado da tabela.
- **T-MF3 · ⚠️ enguiça mesmo tendo acertado** — Com **NH 20** e a regra ligada, um
  **17** deve enguiçar do mesmo jeito. ✅ É o dado **cru** que manda, não o NH.
- **T-MF4 · ⚠️ e a rajada some quando enguiça** — Nesse caso: ✅ **não** aparece
  linha de "N tiros acertaram". Arma travada não cospe tiro.
- **T-MF5 · arma antiga enguiça muito mais** — Com uma **Pistola de Pederneira**
  (NT 4 → Mauf 14): ✅ um **14** já enguiça. *(É para ser assim: pólvora primitiva
  falha o tempo todo.)*
- **T-MF6 · corpo a corpo nunca enguiça** — Ataque com **espada**: ✅ nenhuma
  linha de enguiço, em nenhum resultado.

---

# Lotes MB-3 e MB-5 — tamanho do alvo e prender o fôlego

> Gate: 1.860 testes, 0 falhas nas duas variantes.

## T-MT · 🔴 Modificador de Tamanho do alvo (MB-3)

- **T-MT1 · a linha existe** — Aba Rolagem → ataque **à distância** → segure o NH.
  ✅ Abaixo da linha de distância aparece **"Alvo"**, com `−` e `+` e o exemplo
  ("humano adulto").
- **T-MT2 · 🔴 alvo grande é mais FÁCIL** — Toque no `+` até chegar em **carro**
  (MT +2): ✅ todos os NH da lista **sobem 2**, e o texto diz *"mais fácil de
  acertar"*.
- **T-MT3 · alvo pequeno é mais difícil** — Vá para **gato** (MT −4): ✅ os NH
  **caem 4**, e o texto diz *"mais difícil"*.
- **T-MT4 · o humano não modifica** — De volta a **humano adulto**: ✅ o NH volta
  ao original e o texto diz *"sem modificador"*.
- **T-MT5 · ⚠️ não aparece no corpo a corpo** — Ataque com **espada**: ✅ a linha
  de Alvo **não** existe. É passo da lista do tiro, não da arma branca.
- **T-MT6 · soma com o resto** — Com o alvo em **carro (+2)** e a distância em
  **20 m (−6)**: ✅ o NH final reflete os dois.

## T-FO · Prender o fôlego (MB-5)

⚠️ **Este lote entregou só a regra**, sem tela — ela vai entrar no botão **PF**
do MB-6. Se ainda não houver onde ver na tela, este bloco espera esse lote.

- **T-FO1** — Com **HT 12** parado: ✅ **120 segundos (2 min)**.
- **T-FO2 · 🔴 a diferença é de dez vezes** — Mesma HT 12 **lutando**: ✅ apenas
  **12 segundos**. *(É a razão de a regra existir na tela: quem decora "HT×10"
  acha que tem dois minutos.)*
- **T-FO3** — Hiperventilando, parado, HT 12: ✅ **180 segundos**.
- **T-FO4** — O tempo curto aparece também em **turnos**: ✅ *"12 segundos
  (= 12 turnos)"*.

---

# Lotes MB-1 e MB-4 — os modificadores condicionais de combate

> Gate: 1.877 testes, 0 falhas nas duas variantes.

## T-MC · A lista aparece e soma

- **T-MC1 · corpo a corpo** — Ataque com **espada** → segure o NH. ✅ Aparecem os
  grupos **Manobra / Posição / Situação** com as caixinhas.
- **T-MC2 · à distância é outra lista** — Ataque com **arma de fogo**: ✅ os
  grupos são **Manobra / Situação do alvo**, com *alvo parcialmente exposto*,
  *cobertura leve*, *pessoa no caminho* e *alvo abaixado*.
- **T-MC3 · marcar muda o número** — Marque **Agachado (−2)**: ✅ todos os NH da
  lista caem 2.
- **T-MC4 · ⚠️ Ataque Total vale diferente nos dois** — ✅ No corpo a corpo é
  **+4**; à distância é **+1**. *(São números diferentes no livro — se os dois
  mostrarem o mesmo, é erro de transcrição.)*

## T-MC5 · 🔴 O teto de 9 do asterisco

- **T-MC5a** — Com um personagem de **NH alto (16+)**, marque **Golpe
  Desenfreado**, que tem `*`: ✅ o NH do torso cai para **9**, e aparece o aviso
  *"Teto de 9: um modificador com asterisco foi aplicado"*.
- **T-MC5b · ⚠️ o teto não SOBE ninguém** — Com um personagem de **NH 10**, o
  mesmo Golpe Desenfreado: ✅ o NH vira **5**, não 9. É teto, não piso.
- **T-MC5c · sem asterisco, sem aviso** — Marque só **Agarrado (−4)**: ✅ nenhum
  aviso de teto aparece.

## T-MC6 · Os que se repetem

- **T-MC6a · Avaliar** — Marque **Avaliar** e toque no `+` até 3: ✅ o bônus vai a
  **+3**. No quarto toque: ✅ continua **+3** (o livro trava aí).
- **T-MC6b · Choque** — Marque **Choque** e suba até 6: ✅ o redutor para em
  **−4**.
- **T-MC6c · pessoa no caminho** — À distância, marque **pessoa no caminho** e
  ponha **3**: ✅ o redutor é **−12** (sem teto no livro).
- **T-MC6d · o contador só aparece marcado** — ✅ Com a caixinha desmarcada, não
  há `−`/`+` na linha.

## T-MC7 · ⚠️ Nada está duplicado

Esta é a conferência mais importante do lote.

- ✅ A lista **não** traz *Avançar e Atacar*, *Golpe Rápido*, *mão inábil*,
  *pontos de impacto*, *escuridão*, *distância* nem *tamanho do alvo* — todos já
  têm o seu próprio lugar no diálogo. **Se algum aparecer duas vezes, o redutor
  está sendo aplicado em dobro** e o jogador não tem como perceber.

---

# Lote MB-9 — dano por queda e colisão

> Gate: 1.894 testes, 0 falhas nas duas variantes.

⚠️ **Este lote entregou só a regra**, sem tela. A queda vai aparecer dentro do
botão **PV** (MB-7). Até lá não há o que tocar no aparelho — este bloco fica
esperando, como o T-FO do MB-5.

## T-QD · O que conferir quando a tela existir

- **T-QD1 · 🔴 a tabela ganha da fórmula** — Personagem com **PV 12** caindo de
  **30 m**: ✅ a velocidade mostrada é **26 m/s**, não 25. *(O livro traz as duas
  coisas e elas discordam em 3 dos 15 pontos conferidos. Vale a página impressa —
  é o que o jogador confere no livro aberto na mesa.)*
- **T-QD2 · queda pequena não sai de graça** — **PV 10** caindo **1 m**: ✅
  **1d-2**, não "sem dano". *(Arredondar normalmente daria zero.)*
- **T-QD3 · quem cai mais alto se machuca mais** — Suba a altura degrau a degrau:
  ✅ o dano nunca diminui.
- **T-QD4 · ⚠️ o PV que conta é o de quem cai** — Dois personagens da mesma
  altura, um com **PV 8** e outro com **PV 20**: ✅ o de PV 20 leva **mais**
  dano. *(Contraintuitivo, mas é a regra: o PV mede massa e resistência
  estrutural.)*
- **T-QD5 · objeto pontudo** — Marque *caiu sobre objeto pontudo*: ✅ metade dos
  dados **e** o tipo muda para perfuração/corte.
- **T-QD6 · o aviso da Acrobacia aparece** — ✅ *"Um teste de Acrobacia
  bem-sucedido reduz o dano como se a queda fosse 5 m mais curta"*. Não é
  automático de propósito: depende do chão e do Mestre.

---

# Lotes MB-6 e MB-7 — os botões PF e PV

> ⚠️ **Meio validado (10/08/2026).** O bloco **T-FE** (botão PV) passou no
> aparelho. O bloco **T-PF** (botão PF, fadiga) **continua pendente** — não foi
> aberto no teste. Dentro do T-FE, os itens de **persistência** (T-FE4 e T-FE14,
> fechar e reabrir o app) também ficaram de fora.

> Gate: 1.960 testes, 0 falhas nas duas variantes.

> Este lote mexe no que a ficha **guarda**, não só no que ela calcula: são dois
> campos novos no personagem. Os campos são aditivos e a ficha antiga
> desserializa vazia — decisão do usuário: se alguma não abrir, é só fazer outra.

## T-PF · O botão PF (fadiga)

- **T-PF1 · a palavra virou botão** — Na aba Rolagem, toque em **PF**: ✅ abre o
  painel *Pontos de Fadiga*. *(Na variante PraCego, é o botão **Fadiga**.)*
- **T-PF2 · o deslize continua funcionando** — ✅ Arrastar o dedo no **número**
  ainda muda 1 PF por vez, sem abrir diálogo. Quem quer tirar 1 PF não passa por
  painel.
- **T-PF3 · marcar desce o PF** — Marque **Refeições perdidas** e ponha **3** →
  ✅ o topo mostra *"PF ficará em X"*, 3 abaixo do máximo. Salve: ✅ a ficha
  mostra o número novo.
- **T-PF4 · desmarcar devolve** — Reabra, desmarque, salve: ✅ o PF volta.
- **T-PF5 · 🔴 a origem muda a recuperação** — Com fome marcada, o rodapé diz
  *"⚠️ SÓ com um dia de descanso… com três refeições completas"*. Marque também
  **Esforço**: ✅ aparecem **duas linhas separadas**, não um total só. *(É a razão
  de o painel existir: descansar não cura fome.)*
- **T-PF6 · 🔴 o PF gasto fora do painel NÃO volta de graça** — A conferência mais
  importante. Com PF cheio, **lance uma magia** que custe 3 PF (ou baixe 3 no
  deslize). Agora abra o painel: ✅ a linha **Perda anotada à mão** já vem com
  **3**. Salve sem mexer em nada: ✅ o PF continua o mesmo. *(Se ele subir, o
  painel apagou a magia.)*
- **T-PF7 · sede severa tira PV junto** — Marque **⚠️ Menos de 1 litro no dia**
  com **2**: ✅ aparece o aviso de **2 PV a menos** e, ao salvar, o **PV** também
  cai.
- **T-PF8 · os avisos do livro** — Deixe o PF previsto em **0**: ✅ aparece o
  aviso de que cada PF a mais também custa 1 PV e exige teste de Vontade.
- **T-PF9 · o aviso de sono** — Marque **Sono perdido** com metade do PF máximo:
  ✅ aparece o teste de Vontade a cada 2 h. Com uma ficha que tenha **Dorminhoco**:
  ✅ a penalidade é **-3** em vez de -2.
- **T-PF10 · mostrar as regras** — Toque em **Mostrar as regras**: ✅ cada linha
  ganha a explicação com a página do livro.

## T-FE · O botão PV (ferimento por local)

- **T-FE1 · abre pela palavra** — Toque em **PV**: ✅ abre o painel *Ferimento*.
  *(PraCego: botão **Ferimento**.)*
- **T-FE2 · a conta aparece escrita** — Local **Torso**, tipo **Corte**, dano
  **10**, sem armadura: ✅ mostra **−15 PV** e a conta *"10 − RD 0 = 10 × 1.5 =
  15"*.
- **T-FE3 · 🔴 a RD entra ANTES do multiplicador** — Vista uma armadura de **RD
  4** no torso e repita: ✅ dá **9**, não 11. *(11 seria multiplicar antes de tirar
  a RD — o erro mais comum desta conta.)*
- **T-FE4 · 🔴 comprar não é vestir** — Desmarque a armadura na lista: ✅ a RD cai
  para 0 e o dano volta a 15. **Feche o app e abra de novo**: ✅ ela continua
  desmarcada.
- **T-FE5 · a peça certa para o local certo** — Troque o local para **Crânio**:
  ✅ a armadura de torso **some** da lista e aparece *"Nenhuma peça da ficha
  cobre Crânio"* (a não ser que haja elmo).
- **T-FE6 · ⚠️ os vitais são cobertos pela armadura do torso** — Local
  **Vitais**: ✅ a peça de torso **aparece** e a RD conta.
- **T-FE7 · 🔴 o crânio tem RD 2 de graça** — Local **Crânio**, dano **2**,
  perfurante, sem elmo: ✅ **0 PV**. O mesmo golpe no **Olho**: ✅ passa. *(O olho
  não herda a RD do crânio.)*
- **T-FE8 · 🔴 o membro tem teto — e o resto é desperdiçado** — Personagem de
  **PV 10**, local **Braço**, **9** de contusão: ✅ perde **6 PV** (não 9) e
  aparece o aviso dos pontos desperdiçados + *braço incapacitado*. *(É o exemplo
  do livro, MB p.421.)*
- **T-FE9 · ⚠️ com PV par confira o número** — Personagem de **PV 14**, braço,
  **11** de contusão: ✅ perde **8**, não 7. *(Este é o ponto onde arredondar "para
  cima" erra.)*
- **T-FE10 · decepamento** — Mesmo PV 10, braço, **20** de contusão: ✅ aparece
  **MEMBRO DESTRUÍDO**, e o PV perdido **continua 6**.
- **T-FE11 · atravessar um braço não mata** — Braço, **10** de **Perfuração**:
  ✅ o multiplicador usado é **×1**, não ×2. No **Torso**, o mesmo golpe: ✅ ×2.
- **T-FE12 · a virilha e o choque dobrado** — Local **Virilha**, **Contusão**:
  ✅ aparece a caixinha *Humanoide macho*. Marcada: ✅ o choque pode chegar a
  **−8**. Desmarcada: ✅ para em **−4**.
- **T-FE13 · o teste de nocaute com o modificador certo** — Golpe grave no
  **Crânio**: ✅ *"Teste de HT (-10)"*. No **Rosto**: ✅ **(-5)**.
- **T-FE14 · aplicar grava** — Toque em **Aplicar**: ✅ o PV da ficha cai. Feche e
  reabra o app: ✅ continua caído.
- **T-FE15 · os marcos de PV baixo** — Leve o PV abaixo de 1/3: ✅ aparece o aviso
  de Deslocamento e Esquiva pela metade. Abaixo de 0: ✅ o teste de HT por turno.

---

# Lote MB-7b — o off-by-one do combate tático

> Gate: 1.966 testes, 0 falhas nas duas variantes.

⚠️ **Este lote muda um número do combate da Saga**, não da aba Rolagem. Um golpe
que incapacita braço ou perna passa a tirar **1 PV a mais** do que tirava — o que
o livro sempre mandou.

- **T-7B1 · 🔴 o membro incapacitado com PV par** — Num combate, ataque o **braço**
  de um alvo com **PV 10** e cause dano suficiente para incapacitar: ✅ o log diz
  **6 PV**, não 5, e *"O membro fica inutilizado!"*.
- **T-7B2 · com PV ímpar nada muda** — Mesmo golpe num alvo de **PV 11**: ✅
  continua **6 PV**, igual a antes. *(É por isso que o erro passou despercebido:
  ele só aparece com PV par.)*
- **T-7B3 · extremidade** — Golpe na **mão** de um alvo pequeno (**PV 3**): ✅
  incapacita com **2 PV**, não 1.
- **T-7B4 · o resto do combate não mudou** — ✅ Torso, crânio e vitais continuam
  com os mesmos números de sempre. Só o teto de **braço, perna, mão e pé** mudou.

---

# Lote PV-1a — o mapa de toque da silhueta

> ✅ **VALIDADO NO APARELHO em 10/08/2026** — testado parte por parte do corpo,
> com danos variados.


> Gate: 1.982 testes, 0 falhas nas duas variantes.

⚠️ **Este lote não tem tela.** Ele entrega o mapa e a prova de que ele está
certo; a silhueta desenhada e o zoom entram no **PV-1b**. Este bloco espera esse
lote, como o T-FO do MB-5 esperou.

## T-SI · O que conferir quando a silhueta existir

- **T-SI1 · 🔴 o lado é o DELE** — Toque no braço que aparece à **direita** da
  tela: ✅ a ficha registra **braço esquerdo**. *(A figura está de frente para
  você. É o erro que não aparece em lugar nenhum até alguém perder o braço.)*
- **T-SI2 · a mão é fácil de acertar** — Toque **um pouco ao lado** da mão
  desenhada: ✅ ainda seleciona a mão. *(O toque não exige acerto no traço.)*
- **T-SI3 · nenhuma faixa morta** — Passe o dedo pela base do pescoço, entre a
  cabeça e o peito: ✅ sempre abre alguma tela, nunca fica sem resposta.
- **T-SI4 · a cabeça abre em quatro** — Toque na cabeça: ✅ o zoom mostra
  **crânio, rosto, olho esquerdo, olho direito e pescoço** — e nada chamado
  "boca", que não existe no livro.
- **T-SI5 · o queixo é rosto** — No zoom da cabeça, toque no **queixo** e na
  **boca**: ✅ os dois selecionam **rosto**, não pescoço.
- **T-SI6 · os vitais estão no peito** — No zoom do tronco: ✅ a região de
  **órgãos vitais** fica sobre o tórax, não sobre a barriga.
- **T-SI7 · a lista continua na variante pracego** — ✅ Na `pracego`, os
  quadradinhos de local continuam lá. Silhueta não se tateia.

---

# Lote PV-1b — a silhueta na tela

> ✅ **VALIDADO NO APARELHO em 10/08/2026** — testado parte por parte do corpo,
> com danos variados.


> Gate: 1.983 testes, 0 falhas nas duas variantes.

⚠️ **Só na variante visual.** Na `pracego` os quadradinhos continuam como eram —
confira que eles não sumiram (T-SI7).

Os itens **T-SI1 a T-SI7** do bloco do PV-1a valem agora. Além deles:

- **T-SI8 · o zoom não pisca** — Toque na cabeça: ✅ a imagem **cresce
  continuamente** até a cabeça preencher a tela. Não pode haver troca de imagem
  nem salto — é a mesma arte, só a janela se movendo.
- **T-SI9 · voltar** — Com o zoom aberto: ✅ o botão **Ver o corpo inteiro**
  desce de volta com a mesma animação.
- **T-SI10 · o realce cobre a parte certa** — Selecione o **braço esquerdo**:
  ✅ o vermelho pinta **só o braço**, seguindo o contorno do desenho — não um
  retângulo, e não invade o tronco.
- **T-SI11 · 🔴 realce e registro concordam** — Com o braço realçado, digite um
  dano e aplique: ✅ o texto do resultado diz **braço esquerdo**. *(Se pintar um
  e escrever outro, é o defeito mais grave possível aqui.)*
- **T-SI12 · o ombro na tela da cabeça não seleciona** — No zoom da cabeça, toque
  no cantinho do ombro que aparece embaixo: ✅ nada é selecionado.
- **T-SI13 · sem engasgo ao tocar** — Toque em várias partes seguidas: ✅ o
  realce acompanha sem travar.

---

# Lote PV-1c — tela cheia e grade simétrica

> ✅ **VALIDADO NO APARELHO em 10/08/2026** — testado parte por parte do corpo,
> com danos variados.


> Gate: 1.983 testes, 0 falhas nas duas variantes.

- **T-TC1 · 🔴 nada vaza** — Abra o PV e dê zoom no **tronco**: ✅ o desenho fica
  **dentro** da área dele. Nenhuma perna por cima dos botões, nenhuma mão por
  cima do título. Repita no zoom das **pernas**.
- **T-TC2 · tela cheia** — ✅ O diálogo ocupa a tela inteira, e o botão **Aplicar**
  não fica debaixo da barra de navegação do sistema.
- **T-TC3 · botões do mesmo tamanho** — ✅ Os sete tipos de dano têm **a mesma
  largura, a mesma altura e a mesma letra**. O último (*Perfuração ×2*) fica com
  a largura dos outros, não esticado pela fileira.
- **T-TC4 · celular pequeno** — Se tiver um aparelho de tela menor: ✅ a silhueta
  encolhe junto e o resto continua alcançável rolando.
- **T-TC5 · abre sem nada escolhido** — ✅ Ao abrir, nenhuma parte está realçada e
  o texto diz *"Nenhuma parte escolhida"*. O **Aplicar** fica apagado mesmo com
  dano digitado, até você tocar numa parte.
- **T-TC6 · ⚠️ o vazio no peito é regra, não defeito** — Selecione **Tronco**:
  ✅ há um vazio no meio do peito. É a região dos **órgãos vitais**, que é outro
  local. Toque nele: ✅ o rótulo passa a dizer *Órgãos vitais*.

---

# Lote PV-1d — ordem da tela e resultado fixo

> ✅ **VALIDADO NO APARELHO em 10/08/2026** — testado parte por parte do corpo,
> com danos variados.


> Gate: 1.983 testes, 0 falhas nas duas variantes.

- **T-OR1 · 🔴 o número aparece sem rolar** — Escolha uma parte, digite **10** de
  dano: ✅ o resultado aparece **logo acima dos botões**, sem precisar rolar nada.
- **T-OR2 · ele muda ao vivo** — Sem sair do lugar, troque o **tipo de dano**:
  ✅ o número muda na hora. Desmarque **Descontar RD**: ✅ muda de novo.
- **T-OR3 · a ordem** — ✅ De cima para baixo: **dano rolado**, silhueta, tipo de
  dano, RD da armadura.
- **T-OR4 · ⚠️ sem parte escolhida não há conta** — Abra o diálogo e digite um
  dano **sem tocar no corpo**: ✅ nenhum resultado aparece, o painel de armadura
  diz *"Escolha uma parte do corpo…"* e o **Aplicar** continua apagado.
  *(Antes ele calculava para o torso calado.)*
- **T-OR5 · os detalhes continuam** — Com um golpe forte no braço: ✅ role para
  baixo e encontre o choque, o teste de HT e o aviso do membro incapacitado.
