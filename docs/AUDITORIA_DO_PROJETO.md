# Auditoria do projeto — o que um dev sênior veria

*03 de agosto de 2026. Escrito para quem não programa: cada achado tem o que é, por
que importa, e uma analogia. Nada aqui é opinião de estilo — tudo foi medido no
código, e o comando que mediu está junto quando ajuda.*

---

## Antes de tudo: o projeto está bem

Não é elogio de cortesia. Estas quatro coisas são **raras** em projeto de uma
pessoa só, e é por causa delas que a auditoria abaixo é curta:

1. **1.830 testes automáticos**, e do tipo certo — varredura sobre o catálogo real
   e invariante ("isto nunca pode acontecer"), não só "testei um caso".
2. **As regras do livro estão separadas da tela.** `domain/rules/` é matemática
   pura, sem Android. É por isso que dá para testar tanta coisa.
3. **As chaves de IA não estão no repositório.** Ficam em `local.properties`, que
   o `.gitignore` bloqueia. Confirmado: `git ls-files` não devolve nenhuma.
4. **O banco de dados tem migração explícita.** Havia um `fallbackToDestructive`
   — que **apagava as fichas** a cada atualização — e ele foi trocado por
   migrações escritas à mão (24→25→26).

Uma equipe que herdasse este projeto conseguiria trabalhar nele. Isso é o teste
real.

---

# 1. Segurança

*Contexto que muda tudo: uso doméstico, no máximo 10 pessoas conhecidas. Isso
rebaixa quase todo risco — mas não os dois primeiros.*

## 🔴 1.1 As chaves de IA viajam dentro do aplicativo

**O que é.** As chaves de Gemini, DeepSeek, OpenRouter, NVIDIA e OpenAI saem do
`local.properties` e entram no app pelo `BuildConfig`:

```
buildConfigField("String", "MESTRE_IA_GEMINI_KEY", "\"$geminiKey\"")
```

Isso as grava **em texto puro dentro do arquivo instalado**. Quem tiver o aplicativo
no celular consegue extraí-las com ferramenta gratuita, em minutos.

**A analogia.** É como guardar a chave de casa num cofre (o `local.properties`,
que não vai para o GitHub — isso está certo) e depois **colar uma cópia dela atrás
do porta-retrato que você dá de presente**. O cofre continua fechado; a cópia é
que anda por aí.

**Por que importa mesmo com 10 pessoas.** O risco não é o seu grupo. É o celular
perdido, o aparelho emprestado, o APK que alguém repassa. Chave de IA extraída
**gera cobrança na sua conta** — e o teto é o que o provedor deixar gastar.

**O que fazer.** Existe um caminho que o projeto **já tem meio construído**: você
já tem um servidor próprio no Railway (o da API do Discord). O padrão certo é o
app falar só com o **seu** servidor, e o servidor guardar as chaves de IA. Aí a
chave nunca sai da sua máquina.

⚠️ Não é trabalho de uma tarde. É o único item desta auditoria que eu classifico
como **médio prazo obrigatório**, não "quando der".

## ⚠️ 1.2 O app aceita conexão sem criptografia

**O que é.** No `AndroidManifest.xml`:

```
android:usesCleartextTraffic="true"
```

Isso libera tráfego **HTTP** (sem cadeado), não só HTTPS.

**A analogia.** HTTPS é carta em envelope lacrado; HTTP é cartão-postal — quem
estiver no caminho lê. Numa rede Wi-Fi de casa isso é quase irrelevante; numa rede
de hotel, de bar ou de convenção, não é.

**O detalhe que me fez não marcar como grave:** todas as URLs de verdade do projeto
**já são HTTPS**. O único HTTP é `http://10.0.2.2`, que é o endereço do seu próprio
computador visto de dentro do emulador — desenvolvimento puro.

**O que fazer.** Mudar para `false` e deixar uma exceção só para `10.0.2.2` na
variante de depuração. É uma alteração pequena, e o app não deve nem notar.

## ✅ 1.3 O backup para a nuvem está configurado certo

Olhei porque `android:allowBackup="true"` costuma ser um vazamento clássico. Aqui
não é:

```xml
<cloud-backup>
    <include domain="sharedpref" path="."/>
</cloud-backup>
```

Só as **preferências** sobem (qual canal do Discord você escolheu, etc.). As
**fichas** e o banco de dados **não** vão para a nuvem do Google. Está certo, e
foi decisão deliberada de alguém.

## ⚠️ 1.4 Qualquer aplicativo pode mandar um arquivo para o app

**O que é.** A tela principal está declarada como `exported="true"` com um filtro
que aceita `application/octet-stream` — ou seja, **qualquer arquivo**. Isso foi
proposital e é o que faz a ficha abrir direto do WhatsApp.

**O risco real.** Não é invasão. É **travamento**: um arquivo que não seja a ficha
esperada pode derrubar o app na hora de interpretar.

**A analogia.** É uma caixa de correio na porta, sem tampa. Ninguém entra por ela —
mas qualquer um enfia o que quiser, e você tem de aguentar o que chegar.

**O que fazer.** Curto prazo: garantir que a leitura do arquivo esteja dentro de um
"amortecedor" (`try/catch`) que mostre *"este arquivo não é uma ficha válida"* em
vez de fechar o app. Vale conferir com um arquivo qualquer — uma foto renomeada
para `.json`, por exemplo.

---

# 2. Riscos de quebra (bugs esperando acontecer)

## 🔴 2.1 Onze arquivos passaram do teto que você mesmo definiu

O projeto adotou **1.000 linhas por arquivo**. Medido hoje:

| Arquivo | Linhas | Quanto passou |
|---|---|---|
| `domain/combat/CombatSession.kt` | **3.180** | **3,2×** |
| `viewmodel/delegates/SagaCombatController.kt` | **2.121** | 2,1× |
| `ui/saga/CombatUi.kt` | 1.612 | 1,6× |
| `domain/MestreIAGeneratorUseCase.kt` | 1.525 | 1,5× |
| `ui/components/GeminiLiveService.kt` | 1.359 | 1,4× |
| `ui/TabRolagem.kt` | 1.350 | 1,4× |
| `model/Personagem.kt` | 1.174 | 1,2× |
| `domain/loaders/CatalogLoaders.kt` | 1.065 | 1,1× |
| `viewmodel/FichaViewModel.kt` | 1.047 | 1,0× |
| `ui/features/rolagem/RolagemComponents.kt` | 1.018 | 1,0× |
| `ui/FichaScreen.kt` | 1.004 | 1,0× |

**Por que o teto existe.** Não é frescura de organização. Arquivo grande é onde o
bug se esconde: você conserta uma coisa numa ponta e quebra outra na outra ponta,
porque ninguém consegue ter as 3.180 linhas na cabeça ao mesmo tempo.

**A analogia.** É a diferença entre uma oficina com bancadas separadas e uma sala
com tudo numa mesa só. Na mesa única você acha o martelo — mas derruba o parafuso.

⚠️ **Os dois primeiros são os do combate da Saga**, e eles já têm uma regra
combinada: *"o motor não cresce; mecânica nova vai em delegate"*. A regra está
sendo respeitada — o problema é o que **já estava lá** antes dela.

## ⚠️ 2.2 O maior arquivo do combate não tem teste do próprio

`SagaCombatController.kt` tem **2.121 linhas** e a pasta de testes dele tem
**dois** arquivos, nenhum sobre o controller.

**Por que importa.** Todo o resto do combate é testado (`CombatSession`,
`CombatResolver`, os hexágonos, os subsistemas). O controller é a peça que
**junta** tudo — e é exatamente onde a "fiação" costuma quebrar.

**Isso já aconteceu no seu projeto, duas vezes**, e é por isso que eu insisto:
- a **trava de pares** de traços: 17 testes de regra verdes, e o delegate não
  consultava a trava;
- o **teto do catálogo** na edição de vantagem: a regra certa, e a tela passando
  `null`.

**A analogia.** Você testou o motor, testou as rodas, testou o freio. Não testou
se o pedal está ligado no freio.

## ⚠️ 2.3 Quarenta e um pontos onde o app pode fechar sozinho

O código tem **41** usos de `!!`. Em Kotlin isso quer dizer *"eu garanto que aqui
não está vazio"* — e se estiver, **o app fecha na hora**, sem aviso.

**A analogia.** É atravessar sem olhar dizendo "não vem carro". Quase sempre não
vem. O problema é a vez que vier.

Não é para caçar os 41. É para tratá-los quando aparecerem no caminho de outro
trabalho — e nunca escrever um novo.

## ⚠️ 2.4 Noventa e uma cores escritas na mão

`Color(0xFF...)` aparece **91 vezes** na interface. Cor escrita assim **não sabe
se está no modo claro ou escuro**.

**Isso já te mordeu**: o *"✓ Requisitos Atendidos"* das mágicas era um verde escuro
cravado, e sumia no fundo noturno. Você achou olhando a tela; um teste não pegaria.

**A analogia.** É pintar a parede com a lata na mão em vez de pedir "a cor da
casa". Quando a casa muda de cor, aquela parede fica sozinha.

## ℹ️ 2.5 Quatro travas de tela em potencial

`runBlocking` aparece 4 vezes. Ele **para tudo** até a resposta chegar. Em três dos
casos é operação local (instantânea). O quarto está no `GeminiLiveTools`, no
caminho da **voz**, chamando a busca no manual — e essa pode demorar.

**A analogia.** É atender o telefone e ficar mudo enquanto procura o papel: quem
está do outro lado acha que a ligação caiu.

---

# 3. Código duplicado que pode conflitar

## 🔴 3.1 Doze maneiras diferentes de comparar dois nomes

Esta é a **duplicação que mais dói** no seu projeto, e a única que eu classifico
como grave.

Doze arquivos escrevem, cada um do seu jeito, a rotina de "tirar acento, deixar
minúsculo e comparar":

```
DataRepository · TokenImageStore · SkillEngine · CatalogFilters · TextNormalizer
CatalogLoaders · RacaCatalogo · MestreIAGeneratorUseCase · AlcanceDoAtaque
MestreDeArmasRule · ForjadorToolExecutor · Personagem
```

E elas **não fazem a mesma coisa**. Comparando as versões:

| Arquivo | Troca hífen por espaço? | Tira pontuação? | Junta espaços? |
|---|---|---|---|
| `FichaSkillDelegate` | **sim** | não | não |
| `FichaMagicDelegate` | não | **sim** | **sim** |
| `FichaEquipmentDelegate` | não | **sim** | não |

**Por que isso é perigoso — e não só feio.** Quando duas rotinas discordam, a busca
**falha em silêncio**: o app procura "Maça/Machado", não acha, e **não avisa nada**.
Só sobra o efeito — o bônus que não apareceu, a arma que não casou.

**A analogia.** É ter doze pessoas atendendo o telefone da mesma empresa, cada uma
anotando o nome do cliente de um jeito. Todas trabalham. Mas quando alguém procura
o cadastro, metade não aparece — e ninguém recebe erro, só o silêncio.

⚠️ **Isso já aconteceu três vezes com você**, e a fonte foi sempre a mesma:
- `canhoneiro_nt` fora da lista de perícias à distância (o diálogo abria errado);
- `artilheiro_nt`, um id que não existia em catálogo nenhum;
- ids de perícia divergindo entre os dois catálogos, na unificação.

**O que fazer.** Existe **um** arquivo certo para isso — `domain/filters/TextNormalizer.kt`
— e os outros onze reescreveram. Fazer todos apontarem para ele é um lote médio, e
o teste que impede a volta é o mesmo formato do `PadraoDeTelaTest`: ler o
código-fonte e reprovar quem escrever a sua própria versão.

## ⚠️ 3.2 Nomes repetidos dentro dos catálogos

| Catálogo | Nome repetido |
|---|---|
| Perícias | `Arcos` ×2, `Luta Greco-Romana` ×2, `Mímica/Pantomima` ×2, `Operação de Computadores/NT` ×2 |
| Armas corpo a corpo | `Naginata` ×3, `Katana` ×2, `Espada Bastarda` ×2, `Bordão` ×2 |
| Armas de fogo | `Pistola Auto., 9 mm` ×3, `MM, 9 mm` ×2 |

**Alguns são legítimos** — a Katana aparece duas vezes porque duas perícias
diferentes a usam. **Outros não**: `Operação de Computadores/NT` tem dois ids para
a mesma coisa, e `Arcos` também.

**Por que importa.** Toda vez que o app procura **por nome** em vez de por id, ele
pega o primeiro e ignora o resto — silenciosamente.

**A analogia.** Dois moradores com o mesmo nome no mesmo prédio. A correspondência
chega; só não dá para saber para qual dos dois.

## ℹ️ 3.3 Doze armas marcadas como "confira isto"

Os catálogos de arma trazem um campo `reviewFlags`, e **12 itens** estão marcados:

- `nt_nao_numerico` — Espada de Energia, Chicote Monofio
- `st_minimo_nao_numerico` — Glaive, Alabarda *(o livro dá uma ST por modo)*
- `grupo_suspeito_possivel_desalinhamento` — Bordão Curto, Terçado
- `linha_deslocada_corrigida_ARMA7` — as 3 que eu consertei
- `linha_suspeita_sem_fonte_ARMA7` — a Carabina de Assalto, que **não** consertei
  porque nenhuma linha do livro batia com ela

Isso é **bom sinal**, não ruim: significa que o catálogo sabe onde ele mesmo é
duvidoso. O que falta é uma tela ou script que **mostre** essa lista, para as
dúvidas não morrerem dentro do JSON.

---

# 4. O plano: curto, médio e longo prazo

## Curto prazo — dias, e cada um cabe num lote

| # | O quê | Por quê agora |
|---|---|---|
| 1 | **Desligar o tráfego sem criptografia** (`usesCleartextTraffic=false`, exceção só para o emulador) | Uma linha, risco quase zero de quebrar |
| 2 | **Amortecedor no arquivo que chega de fora** — arquivo inválido mostra recado, não fecha o app | O app já aceita qualquer arquivo do WhatsApp |
| 3 | **Tirar o `runBlocking` do caminho da voz** | É o único que pode travar a tela de verdade |
| 4 | **Migrar as 91 cores cravadas** — pelo menos as de `ui/features/` e `ui/*.kt` | O verde das mágicas já provou o estrago; o `PadraoDeTelaTest` pode passar a reprovar cor nova |
| 5 | **Decidir os nomes duplicados de catálogo** — `Operação de Computadores/NT` e `Arcos` | São 4 decisões suas, de 5 minutos cada; eu não devo escolher sozinho |

## Médio prazo — semanas

| # | O quê | Por quê |
|---|---|---|
| 6 | 🔴 **Tirar as chaves de IA de dentro do app** — o servidor Railway passa a intermediar | É o único risco de **custo real** desta auditoria |
| 7 | 🔴 **Uma normalização de texto só** (`TextNormalizer`) + teste que reprova quem criar outra | É a causa-raiz de três bugs que você já viveu |
| 8 | **Testes de fiação no `SagaCombatController`** — não do controller inteiro, só das pontes | As duas quebras que você achou no aparelho eram de fiação, não de regra |
| 9 | **Terminar o padrão de tela** — sobram 11 arquivos na dívida datada | A lista já existe e não pode crescer calada |
| 10 | **Uma tela (ou script) para os `reviewFlags`** | Dúvida registrada e invisível é dúvida esquecida |

## Longo prazo — meses, e só se o projeto crescer

| # | O quê | Quando vale |
|---|---|---|
| 11 | **Quebrar o `CombatSession` (3.180 linhas)** em subsistemas, como já se faz com os delegates | Quando o combate voltar a ser a frente principal. Antes disso, é mexer no que está funcionando |
| 12 | **Separar o app da Saga do app da ficha** | Já está no seu plano. As duas metades têm ritmos diferentes, e hoje um bug numa atrapalha a outra |
| 13 | **Um `CHANGELOG` para o jogador** — o `PROGRESS.md` é para você, não para quem usa | Quando houver mais gente na mesa que fora dela |
| 14 | **Assinatura e distribuição do app** | Só se sair do círculo de 10 pessoas |

---

# 5. O que eu **não** recomendo mexer

Um sênior honesto também diz onde **não** tocar. Estas quatro coisas parecem
candidatas a "melhoria" e não são:

1. **A arquitetura híbrida de traços** (JSON para o simples, Kotlin para o
   complexo, e a classe vencendo o JSON). Parece inconsistência; é a decisão certa,
   e tem validador (`validar_efeitos.py`) impedindo a confusão.

2. **Os 14 ids "mortos" nas listas de perícia de combate.** Parecem lixo. São
   nomes que **fichas antigas podem ter gravado** — apagar faria uma ficha perder o
   ataque em silêncio. Carregar um id a mais é mais barato que isso.

3. **Os catálogos JSON em vez de banco de dados.** Para 879 magias e 281 perícias
   que **não mudam em tempo de execução**, arquivo é mais simples, mais rápido de
   testar e mais fácil de conferir contra o livro.

4. **O `PROGRESS.md` de 5.700 linhas.** Parece um arquivo que precisa de faxina.
   É o oposto: é o histórico que permite responder "por que isto está assim?" —
   e várias correções desta semana saíram de lá.

---

# 6. Resumo em uma tela

**Se você fizer só três coisas**, faça estas, nesta ordem:

1. 🔴 **Tirar as chaves de IA de dentro do aplicativo.** É o único item que pode
   custar dinheiro de verdade.
2. 🔴 **Unificar a comparação de nomes.** É a causa-raiz de três bugs que você já
   caçou à mão, e vai causar o quarto.
3. ⚠️ **Testar a fiação do controller de combate.** É o maior arquivo sem rede, e
   o histórico mostra que é exatamente ali que quebra.

O resto é manutenção saudável, e o projeto está em condições de recebê-la — o que,
sinceramente, é mais do que a maioria dos projetos desse tamanho consegue dizer.
