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
