# O que o app automatiza — mapa completo por aba

> **Para que serve este documento**
>
> 1. **Mostrar a um jogador novo** o que a ficha faz sozinha, sem ele precisar
>    abrir o Módulo Básico.
> 2. **Servir de mapa para nós dois** — o que já existe, o que não existe, e onde
>    cada regra mora. Sem isto, a próxima automação corre o risco de refazer o
>    que já está pronto.
>
> Levantado a partir do **código**, não de memória. Toda regra citada tem arquivo
> e página do livro no índice do fim.

---

## Em uma frase

É uma ficha de GURPS 4ª edição que **faz as contas do livro** — não só guarda
números. O jogador diz o que aconteceu; o app diz o que a regra manda, com a
página.

E há **duas variantes** do mesmo app: a **visual** e a **pracego**, para quem usa
leitor de tela. Não é a mesma tela com fonte maior — é outra entrada para as
mesmas regras.

---

## Os catálogos que vêm dentro

Tudo offline, sem consulta ao livro na mesa:

| catálogo | itens |
|---|---:|
| Magias | **879** |
| Perícias | **281** (+ 40 de artes marciais) |
| Vantagens | **272** |
| Desvantagens | **218** |
| Modificadores (ampliações e limitações) | **218** |
| Armas | **150** (60 corpo a corpo, 28 à distância, 62 de fogo) |
| Técnicas | **118** |
| Armaduras | **72** |
| Poderes | **44** |
| Escudos | **7** |

Mais o **Módulo Básico inteiro em texto** dentro do app, usado pelo Mestre de
Regras (ver *Saga*).

---

# Aba **Geral** — quem é o personagem

O cadastro, e as contas que saem dele.

**O que se preenche:** nome, jogador, campanha, aparência, história, notas.

**O que o app calcula sozinho:**

- **Atributos secundários** a partir dos primários: PV, PF, Vontade, Percepção,
  Velocidade Básica, Deslocamento Básico.
- **Dano GdP e GeB** pela ST — a tabela da p.16 já resolvida.
- **Todos os deslocamentos**: correndo, nadando, voando, rastejando, saltando.
  Toque no valor para ver a lista inteira.
- **Custo em pontos** de tudo: atributos, vantagens, desvantagens, perícias,
  técnicas, magias, peculiaridades — com o **total gasto** e o que sobra.
- **Teto de atributo** e teto de nível de traço, avisando quando passa do que a
  campanha permite.

**Botões:**

| botão | o que faz |
|---|---|
| **Definir Base de Atributos** | fixa a base a partir da qual os custos são contados |
| **Histórico de Alterações** | tudo que mudou na ficha, com data — dá para exportar em texto |
| **Apagar Histórico** | limpa o registro |

---

# Aba **Traços** — vantagens, desvantagens, qualidades, peculiaridades

**Botões:** *Adicionar Vantagem*, *Adicionar Desvantagem*, *Adicionar
Qualidade*, *Adicionar Peculiaridade*, *Configurar Poderes*.

**O que o app faz sozinho:**

- **Busca no catálogo** com filtro, e o custo já calculado por nível.
- **Modificadores** (ampliações e limitações): o app aplica a porcentagem e
  mostra o custo final.
- **Incompatibilidade entre traços** — avisa quando o personagem pega dois que o
  livro não deixa conviver.
- **Teto de nível** por traço.
- **Número de Autocontrole** das desvantagens: vira teste rolável na aba Rolagem.
- **Poderes** com os seus modificadores próprios.

⚠️ Vantagens e desvantagens **não ficam só na lista**: elas entram nas contas das
outras abas. *Reflexos em Combate* aparece no Bloqueio; *Sorte* vira botão;
*Talento Instintivo* abre um diálogo; *Visão Noturna* desconta a penalidade de
luz sozinha.

---

# Aba **Perícias** — o que ele sabe fazer

**Botões:** *Adicionar Perícia*, *Criar Perícia* (para o que não está no livro).

**O que o app faz sozinho:**

- **NH** a partir do atributo base, da dificuldade e dos pontos gastos.
- **Perícias suplementares** (as que dão bônus a outras).
- **Modificador de equipamento** (p.346): sem ferramenta, improvisada, boa,
  superior — com a coluna dupla para perícia tecnológica.
- **Familiaridade cultural** e a penalidade de não a ter.
- **Predefinições**: o NH que ele tem numa perícia que **não** comprou.

---

# Aba **Técnicas** — os golpes treinados

**Botão:** *Adicionar Técnica*.

Calcula o NH da técnica a partir da **perícia base**, respeitando o teto que cada
técnica tem, e avisa quando a perícia base não está definida.

---

# Aba **Magia** — as 879 magias

**Botão:** *Adicionar Magia*.

**O que o app faz sozinho:**

- **NH da magia** pela Aptidão Mágica e pelos pontos.
- **Pré-requisitos**: avisa quando falta uma magia exigida.
- **Custo de energia** — incluindo o custo **variável** (*"1 a 3 PF"*), que vira
  uma escolha antes da rolagem.
- **Manutenção**, duração e tempo de execução.
- **Descrição completa** da magia: toque no nome.

⚠️ A aba **só aparece** se o personagem tiver Aptidão Mágica. Quem não tem, não
vê tela vazia.

---

# Aba **Equip.** — armas, armaduras, escudos e o resto

**Botões:** *Adicionar Itens*, *Adicionar Arma*, *Adicionar Escudo*,
*Adicionar Armadura*.

## A ficha técnica de cada item

Tocar num item do catálogo **não o adiciona** — abre a **ficha técnica**, e o
botão de adicionar mora lá dentro. Dois toques em vez de um, para ninguém comprar
uma arma sem descobrir que ela pesa 7,3 kg e exige as duas mãos.

A ficha é dividida em três blocos, iguais para arma, armadura e escudo:

| bloco | o que traz |
|---|---|
| **No meio da jogada** | o que se consulta com o dado na mão — dano, alcance, RD, BD |
| **Na hora de comprar** | NT, peso, custo |
| **Observações do livro** | as notas de rodapé **por extenso**, não o número |

**Toda sigla vem traduzida.** `Tiros 80(3)` aparece como *"80 tiros, 3 turnos para
recarregar"*; `Mag −3` diz o que a Magnitude faz.

## O que o app calcula sozinho

- **Dano da arma com a ST do personagem** — `GeB+2` vira `1d+4`.
- **Alcance dos arcos**: `×15/×20` numa ST 11 vira **165/220 m**.
- **Precisão com mira acoplada**, contada à parte.
- **ST mínima**: quando a arma pesa mais do que o personagem aguenta, a linha
  aparece em vermelho dizendo o que custa — *"Falta ST 3: −3 no ataque e 1 PF a
  mais"*.
- **Qualidade da arma** (barata, boa, superior, altíssima): o bônus de dano entra
  na conta — e **só em lâmina**, como o livro manda.
- **Armadura por local**: uma peça de tronco+virilha pode entrar só num dos dois,
  com peso e custo divididos.
- **Sobrepor armaduras**: avisa quando o livro não permite, e cobra o **−1 na DX**
  da camada extra.
- **Peso total, custo total e nível de carga** — que mexem no Deslocamento e na
  Esquiva.

## Campos editáveis

Nome, peso, custo, quantidade, notas — e, conforme o tipo, **RD** (armadura) ou
**BD** (escudo). É por aí que entra a armadura mágica de +1 RD: o campo alimenta o
combate; a nota, não.

---

# Aba **Rolagem** — a mesa

É a aba que existe para ser usada **durante** o jogo. Tudo aqui rola dados de
verdade e já aplica os modificadores.

## O topo

**Cartão de atributos** com ST, DX, IQ, HT, Vontade, Percepção — toque em
qualquer um para rolar contra ele. E **PV / PF** em destaque.

⚠️ **Arrastar o dedo** sobre um atributo muda o modificador da rolagem. Na
variante pracego há botões para o mesmo.

### O botão **PV** — ferimento

O Mestre diz *"5 de corte no braço"*. O jogador toca numa **silhueta do corpo**
(16 regiões, com lado), digita o dano, escolhe o tipo — e o app faz o que ninguém
faz de cabeça:

- tira a **RD das armaduras que ele está vestindo** naquele local (e comprar não
  é vestir: cada peça tem a sua caixinha);
- aplica o **multiplicador de ferimento** do tipo **e** do local;
- respeita o **teto do membro** (um golpe no braço nunca causa mais que o
  necessário para incapacitá-lo) e avisa quanto foi desperdiçado;
- diz se o membro foi **incapacitado** ou **destruído**;
- calcula o **choque** do próximo turno;
- avisa o **teste de HT** contra queda e atordoamento, com o modificador do local;
- calcula o **trauma por impacto** quando a armadura flexível barra o golpe
  inteiro;
- calcula quanta **RD o ácido destruiu** na armadura.

**Doze tipos de dano**, a tabela completa da p.380: contusão, corte, perfuração,
as quatro subclasses de perfurante, queimadura, corrosão, toxina, fadiga e
atribulação. Fadiga desconta **PF**; atribulação não tira ponto nenhum — vira
teste de HT.

A conta aparece escrita: `10 − RD 5 = 5 × 1.0 = 5`.

### O botão **PF** — fadiga

Marca de onde veio o cansaço (esforço, magia, calor, sede, sono), e o PF desce
sozinho. Desmarcando, sobe de volta. Avisa os **marcos**: 1/3 do PF, PF zero,
PF negativo.

## Os painéis da mesa

| painel | o que resolve |
|---|---|
| **Marcos de vida** | avisa quando a queda de PV cruza um limiar que exige teste |
| **Luz da cena** | escolhe a iluminação uma vez; o redutor entra em toda rolagem de visão e combate, já com a Visão Noturna descontada |
| **Apara do turno** | a 2ª apara custa −4, a 3ª −8 — o app conta |
| **Sorte** | rola mais duas vezes e escolhe o melhor resultado |
| **Estados temporários** | atordoado, caído, agarrado — e o que cada um custa |
| **Modificadores de combate** | a página p.547-549 virada em caixinhas |
| **Bônus condicionais** | os bônus que só valem em certa situação, para marcar nesta rolagem |
| **Autocontrole** | só aparece se houver desvantagem com Número de Autocontrole |

## As seções de rolagem

- **Combate: Ataque e Dano** — escolhe a arma, rola o ataque, rola o dano.
- **Defesas Ativas** — Esquiva, Bloqueio e Aparar, já com escudo, carga e
  vantagens somados.
- **Perícias** — toda perícia da ficha, rolável com um toque.
- **Magias** — idem, com o custo de energia cobrado.
- **Técnicas** — idem.
- **Reação e Resistência** — teste de reação (p.494) com os modificadores
  sociais, e os testes de resistência.
- **Rolagem Livre** — 3d6 contra o número que você quiser.
- **Histórico da Sessão** — tudo que foi rolado, com o resultado.

## Os diálogos

| diálogo | o que faz |
|---|---|
| **Mira** | tocar no NH do ataque abre a lista de locais do corpo, **cada um já com o NH reduzido** |
| **Talento Instintivo** | só as perícias que ele **não** tem, com o NH que a vantagem concede |
| **Visualização** | um minuto de concentração, teste de IQ, e o bônus sai da margem de sucesso |
| **Sentidos** | rola Visão, Audição, Olfato — cada um com as vantagens somadas |
| **Configurar ataque / dano** | ajusta o que o app não tem como saber |

---

# Aba **Saga** — jogar sozinho, com um Narrador

Uma campanha de GURPS conduzida por IA.

- **Criar campanha**: gênero, dificuldade, nível tecnológico, se há magia no
  mundo, conceito.
- **Narrador** que descreve a cena e responde às ações.
- **Combate tático em hexágonos**, com tokens e NPCs.
- **Falar com o Narrador durante o combate** — ação improvisada vira mecânica.
- **Mestre de Regras**: pergunte qualquer regra e ele responde **citando a página
  do Módulo Básico**, lendo o livro de verdade que está dentro do app.

---

# Como ver de onde veio cada número

Esta é a decisão de projeto que atravessa o app inteiro:

> **automação que não se explica é caixa preta — e num jogo de regras, quem não
> confere não confia.**

Por isso:

- Todo número calculado tem uma **notinha** dizendo de onde veio: a Esquiva que
  subiu por causa do escudo, o Bloqueio que ganhou +1 de Reflexos em Combate.
- Toda conta de ferimento aparece **escrita**, não só o resultado.
- Toda sigla do livro vem com a **tradução ao lado**.
- Todo aviso de regra traz a **página**.

É a diferença entre o extrato que mostra só o saldo e o que mostra as linhas. O
saldo é o que interessa, mas sem as linhas você não descobre o lançamento errado.

---

# O que o app **não** faz (ainda)

Registrado de propósito, para não procurarmos o que não existe:

| não automatizado | por quê |
|---|---|
| Multiplicador de **preço** por qualidade de arma | depende do NT da campanha, que a ficha não guarda |
| **Teste de quebra** ao aparar arma pesada (p.376) | a qualidade já está guardada; falta a rolagem |
| **Redutores de reação** por usar armadura (p.287) | tem exceção que é julgamento humano |
| **Vestir e tirar armadura** (p.287) | só importa em combate por turno |
| **Sangramento** | não modelado |
| **Custo de vida** mensal (p.266) | fora do escopo da mesa |
| Aplicar a **corrosão** na peça sozinho | o app não sabe qual peça o ácido atingiu |
| **Montaria**, regras especiais por arma, perigos de ambiente | adiados |

---

# Índice de regras — onde cada uma mora

Sessenta arquivos de regra pura, sem Android, todos com teste. Para achar rápido
quando formos mexer:

| regra | arquivo | MB |
|---|---|---|
| Alcance do ataque | `AlcanceDoAtaque` | p.271 |
| Apontar | `ApontarRules` | p.270, 364, 373 |
| Atirador | `AtiradorRules` | p.364 |
| Autocontrole | `AutocontroleRules` | p.120 |
| Avançar e atacar | `AvancarEAtacarRules` | p.366 |
| Sobrepor armaduras | `CamadasDeArmadura` | p.287 |
| Cartão de item | `CartaoDoItem` | — |
| Atributos e derivados | `CharacterRules` | p.102 |
| Cobertura da armadura | `CoberturaDaArmadura` | p.379 |
| Corrosão na armadura | `TraumaPorImpacto` | p.380 |
| Desastrado | `DesastradoRules` | p.133 |
| Deslocamentos | `DeslocamentosRules` | p.17, 19, 39, 91 |
| DX Braçal | `DxBracalRules` | p.56 |
| Estados temporários | `EstadosTemporarios` | p.137-158 |
| Fadiga | `FadigaRules` | p.426-427 |
| Familiaridade cultural | `FamiliaridadeCulturalRules` | p.24 |
| Ferimento por local | `FerimentoPorLocalRules` | p.399-400, 419-422 |
| Ficha técnica (forma) | `FichaDeEquipamento` | p.271 |
| Ficha técnica da arma | `FichaTecnicaDaArma` | p.270-272, 508 |
| Ficha técnica da armadura | `FichaTecnicaDaArmadura` | p.283-286 |
| Ficha técnica do escudo | `FichaTecnicaDoEscudo` | p.288, 484 |
| Fôlego | `FolegoRules` | p.356 |
| Golpe rápido e apara | `GolpeRapidoEAparaRules` | p.371, 377 |
| Iluminação | `IluminacaoRules` | p.394, 549 |
| Incompatibilidade de traços | `IncompatibilidadeDeTracos` | p.124-162 |
| Locais de ataque | `LocaisDeAtaque` | p.398-400 |
| Energia das magias | `MagiaEnergiaRules` | p.236-241 |
| Mão inábil | `MaoInabilRules` | p.14, 157 |
| Mapa da silhueta | `MapaDaSilhueta` | — |
| Marcos de vida | `MarcosDeVidaRules` | p.419 |
| Impedimentos da mira | `MiraImpedimentosRules` | p.153, 163 |
| Modificadores de combate | `ModificadoresDeCombate` | p.547-549 |
| Modificadores situacionais | `ModificadoresSituacionais` | p.346, 376 |
| Notas do escudo | `NotasDoEscudo` | p.288 |
| Origem dos números | `OrigemDosNumeros` | p.17, 375 |
| Piso de teste | `PisoDeTeste` | p.140, 159 |
| Qualidade da arma | `QualidadeDaArma` | p.275-276 |
| Qualidade do equipamento | `QualidadeDoEquipamento` | p.346 |
| Queda | `QuedaRules` | p.430-432 |
| Reação | `ReacaoRules` | p.494 |
| Resistência | `ResistenciaRules` | p.365, 419-424 |
| Rótulo acessível | `RotuloAcessivel` | — |
| Sentidos | `SentidoRules` | p.358 |
| Sorte | `SorteRules` | p.90-91 |
| ST Braçal | `StBracalRules` | p.89 |
| ST especializada | `StEspecializadaRules` | p.65, 88 |
| ST mínima da arma | `StMinimaDaArma` | p.271 |
| Velocidade e distância | `TabelaVelocidadeDistancia` | p.550-551 |
| Talento instintivo | `TalentoInstintivoRules` | p.92 |
| Tamanho do alvo | `TamanhoDoAlvoRules` | p.549-550 |
| Teto de atributo | `TetoDeAtributoRules` | p.19 |
| Teto de nível do traço | `TetoDeNivelDoTraco` | p.91, 151, 159 |
| Texto do catálogo | `TextoDoCatalogo` | — |
| Tipos de dano | `TiposDeDano` | p.43-44, 379-380 |
| Tiro contínuo | `TiroContinuoRules` | p.407-409 |
| Trauma por impacto | `TraumaPorImpacto` | p.380 |
| Visualização | `VisualizacaoRules` | p.99 |

Todas em `app/src/main/java/com/gurps/ficha/domain/rules/`.

---

## Uma nota sobre confiança

O app tem **mais de 2.100 testes automáticos**, e boa parte deles não confere um
caso: confere a **regra inteira**. O teto de dano num membro é conferido para
todo PV de 1 a 40; a tabela de tipos de dano é conferida nas doze entradas
contra quatro propriedades cada.

Isso existe porque a experiência do projeto foi clara: teste que só olha o caso
que o autor imaginou fica verde com o defeito em pé.
