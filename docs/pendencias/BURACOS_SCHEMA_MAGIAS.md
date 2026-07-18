# Buracos de SCHEMA da mecânica das magias (auditoria LIMPEZA-4)

Auditoria PROATIVA das **84 magias que o motor executa**: onde a regra do livro tem número
concreto que a curadoria teve de jogar na `notas` (texto morto) porque **não havia campo**.

**Por que existe:** 5 vezes na sessão de 17/jul um campo faltante só apareceu quando o jogo
saiu errado no aparelho (Géiser 15d, Escudo sem BD, Aumentar Força inerte, +2 vazando pro arco,
jogador sem escolher energia). Em todas a curadoria já sabia — faltava CAMPO. Esta lista é o
inverso: achar os próximos ANTES de virarem bug.

**26 buracos** (13 de impacto ALTO = o motor hoje aplica a magia ERRADO).

## 🔴 ALTO — o motor produz número/efeito ERRADO hoje

### Terror / Pânico / Medo / Êxtase / Atordoamento Mental / Deturpar / Quietude
- **Regra sem campo:** Todas são "Resistível com VONTADE" (R-Vont), não com HT. O enum `condicaoResistencia` só aceita HT | HT-3 | HT_por_pv — não existe valor para Vontade. Terror inclusive está gravado como condicaoResistencia:"HT" quando o livro diz Vontade; as outras ficaram SEM campo (só menção na nota "R-Vont").
- **Campo sugerido:** `condicaoResistencia: adicionar valores "Vontade" e "Vontade-3" ao enum (ex.: condicaoResistencia: "Vontade")`

### Chuva de Ácido / Chuva de Fogo / Chuva de Pedras / Nuvem de Fogo / Nuvem de Faíscas / Tempestade de Faíscas / Géiser (respingo) / Mau Cheiro
- **Regra sem campo:** O dano é POR SEGUNDO enquanto a criatura estiver na área, por toda a duração (1 min = até 60 ticks): "1d-1 de dano por ácido por segundo a todos dentro dela"; "a nuvem inflige 1 ponto de dano por segundo para cada ponto de energia"; Mau Cheiro é 1d por teste de HT "uma vez por minuto". Não existe campo de tick — o motor aplica o dano UMA única vez.
- **Campo sugerido:** `danoPorSegundo: Boolean + danoIntervaloSegundos: Int (1 para as chuvas/nuvens, 60 para Mau Cheiro)`

### Bola de Fogo Explosiva / Relampago Explosivo / Bola de Relampagos / Concussão / Explodir
- **Regra sem campo:** "O alvo e qualquer um mais próximo do alvo que um metro recebe dano total. Os mais afastados dividem o dano em três vezes a distância em metros (arredondado para baixo)." O schema só tem entrega:"area" — nenhum campo de decaimento explosivo, então todos na área levam dano CHEIO.
- **Campo sugerido:** `explosaoDivisorPorMetro: Int (=3) ou explosao: Boolean (aplica divisor 3×distância além de 1m)`

### Toque Candente / Desidratar / Geladura / Enfraquecer / Espantar Zumbi / Desintegrar / Explodir / Fender / Toque Chocante / Morte Candente / Morte Putrefata
- **Regra sem campo:** Teto de energia por magia: Toque Candente "Custo: 1 a 3"; Desidratar "1 para 1d-1, até 3"; Desintegrar "Custo: 1 a 4"; Enfraquecer/Explodir "Custo: 2 a 6"; Olhar de Relâmpago "1 a 4". Projéteis têm teto implícito ("até a Aptidão Mágica por segundo, por 3 segundos" = 3×AM; os de custo dobrado = 6×AM). Não existe campo de máximo — com o seletor de energia o jogador pode despejar 10 num Toque Candente e sair 10d.
- **Campo sugerido:** `energiaMaxima: Int, e energiaMaxMultiploAM: Int (3 ou 6) para os projéteis`

### Desintegrar / Fender / Enfraquecer / Explodir / Espantar Zumbi
- **Regra sem campo:** Restrição de alvo válido com efeito mecânico: "Essa mágica afeta apenas objetos inanimados" (Desintegrar), "funciona apenas em itens inanimados" (Enfraquecer), "Faz buracos em objetos inanimados, paredes" (Fender); Espantar Zumbi "Causa 1d de lesão em qualquer coisa na área que foi animada usando a mágica Zumbi". Sem campo, o motor deixa aplicar 4d de Desintegrar num NPC vivo e 1d de Espantar Zumbi num humano.
- **Campo sugerido:** `alvoValido: String ("objeto" | "morto_vivo_zumbi" | "vivo")`

### Arma Flamejante / Arma Congelante / Arma de Relampago / Projéteis Flamejantes / Projéteis Congelantes / Projeteis de Relampago
- **Regra sem campo:** O +2 entra DEPOIS da RD e dos modificadores de ferimento: "causa mais 2 pontos de dano após a penetração da armadura e os modificadores de ferimento". `buffDanoArma: 2` provavelmente é somado ao dano bruto (antes da RD). Contra RD alta o resultado muda: dano 4 vs RD 5 → correto = 0 penetra + 2 = 2 de lesão; motor = 4+2=6 − 5 = 1. Número errado em toda luta contra armadura.
- **Campo sugerido:** `buffDanoAposRd: Boolean (aplica o bônus depois de RD e do multiplicador de ferimento)`

### Jato de Som
- **Regra sem campo:** "deve fazer um teste contra seu HT MENOS o custo de energia da mágica ou ficará atordoado. A RD atribui um bônus de +1 ao HT efetivo do alvo para cada cinco pontos de RD." O enum de resistência não tem "HT menos energia gasta" nem bônus de RD — está gravado só como "HT", então uma magia de 4 de energia rola HT cheio (4 pontos mais fácil de resistir que o correto).
- **Campo sugerido:** `condicaoResistencia: "HT_menos_energia" + condicaoRdBonusPor: Int (=5, +1 ao HT efetivo a cada 5 de RD)`

### Jato de Areia / Jato de Lama / Jato de Neve / Jato de Vapor / Sopro de Vapor
- **Regra sem campo:** Duração da cegueira escalada pela energia: "cada ponto de energia na mágica o cega por um segundo"; falha crítica no HT = "cego por 1d segundos POR PONTO"; sucesso simples = não vê por 1 segundo; sucesso decisivo = nada. Depois de voltar a ver: "-3 a todas as perícias de combate por mais 1d segundos". O schema tem `condicao: cego` mas nenhuma duração por energia nem os degraus de margem — o efeito PRINCIPAL destas magias fica no texto morto.
- **Campo sugerido:** `condicaoDuracaoSegPorEnergia: Int (=1) + condicaoDuracaoCriticoDados: String ("1d por energia") + condicaoRiderPenalidade / condicaoRiderDuracao (-3 combate por 1d seg)`

### Adaga de Gelo / Bola de Fogo / Bola de Ácido / Esfera de Gelo / Projétil de Pedra / Relampago / Relampago Explosivo / Raio Solar / Bola de Fogo Explosiva
- **Regra sem campo:** Estatísticas de projétil concretas que ficaram 100% na nota: "1/2D 30, Max 60, Prec 3" (Adaga), "1/2D 25, Max 50, Prec 1" (Bola de Fogo), "1/2D 75, Max 150, Prec 2" (Raio Solar), "1/2D 50, Max 100, Prec 3" (Relâmpago), etc. Sem campos, o motor não aplica meio-dano além do 1/2D, não limita no Max e não dá o bônus de Precisão na mirada — dano e chance de acerto saem errados a distância no grid tático.
- **Campo sugerido:** `alcanceMeioDanoM: Int, alcanceMaximoM: Int, precisao: Int`

### Toque Candente
- **Regra sem campo:** "Armadura não protege, mas RD natural sim." O campo `armadura: "ignora"` ignora TODA a RD, inclusive a natural — contra criaturas com couro/escamas (RD natural) o motor entrega dano cheio quando o livro manda a RD natural absorver.
- **Campo sugerido:** `armadura: adicionar valor "ignora_armadura_nao_natural" ao enum`

### Morte Candente / Morte Putrefata
- **Regra sem campo:** Ciclo por turno com teste: "Toda vez, a vítima deve fazer um teste de HT; em uma falha (crítica ou não), ele recebe 1d-1 de dano... em um sucesso decisivo, a mágica está quebrada." Morte Putrefata ainda tem "6 pontos em uma falha crítica". O schema modela como dano único (danoFixo) — não há tick por turno, não há teste de HT recorrente, e o sucesso decisivo não quebra a magia.
- **Campo sugerido:** `danoPorTurnoComTeste: String ("HT") + danoCriticoFixo: Int (=6) + terminaEmSucessoDecisivo: Boolean`

### Lampejo
- **Regra sem campo:** Efeito em BANDAS de distância com resultados diferentes: dentro de 10m falha = 3 seg cego + 1 min com -3 na DX; 11 a 25m falha = 1 min com -3 na DX; 26m+ falha = 3 seg com -3 na DX. O schema só guardou condicaoRaioM: 10 — quem está a 20m recebe o mesmo tratamento de quem está a 5m, e a penalidade de -3 na DX (que atinge todas as perícias baseadas em DX) não existe como campo de condição.
- **Campo sugerido:** `condicaoBandasDistancia: lista de {ateM, condicao, duracaoSeg} + condicaoPenalidadeAtributo: String/Int ("DX", -3)`

### Toque Congelante
- **Regra sem campo:** A saída da paralisia não é teste de HT nem tempo: "não pode tomar nenhuma ação até que ele rompa o gelo com um teste de ST bem-sucedido com uma penalidade de -1 por cada 0,5cm de gelo" (2 de energia = 0,5cm), e o gelo pode ser quebrado de fora (RD 1 por 1cm, 0,5cm por ponto de dano básico). Também: 1d-1 por 0,5cm contra criaturas de fogo. Nada disso tem campo — `condicao: paralisado` sozinho vira paralisia genérica sem escape correto.
- **Campo sugerido:** `condicaoEscapeAtributo: String ("ST") + condicaoEscapePenalidadePorEnergia: Int (-1 a cada 2 de energia)`

## 🟡 MÉDIO — aplica incompleto, não errado

### Chuva de Fogo / Chuva de Pedras
- **Regra sem campo:** Escala em DEGRAU pelo custo, não linear: "Pelo dobro do custo básico, a chuva causa 2d-2 por segundo". `danoFixo: true` congela em 1d-1 e não há campo para o degrau de custo dobrado; o jogador que paga o dobro não recebe nada.
- **Campo sugerido:** `danoDegrauCustoDobrado: String ("2d-2")`

### Jato de Neve / Jato de Lama / Jato de Vapor / Jato de Ácido / Géiser
- **Regra sem campo:** Rider de projeção/derrubada com número: "Ele causa projeção em todos os alvos... podendo matar ou derrubar criaturas voadoras" (1d de projeção por ponto de energia, que não vira PV); Géiser: "são automaticamente empurrados para fora da área de efeito e devem fazer um teste de DX-5 para permanecerem em pé". O motor tático já tem Empurrão/Derrubar como manobras, mas a magia não consegue disparar nenhum dos dois.
- **Campo sugerido:** `empurraoDadosPorEnergia: String ("1d") + derrubaTeste: String ("DX-5")`

### Nuvem de Faíscas / Tempestade de Faíscas / Sono Coletivo
- **Regra sem campo:** "Raio mínimo de 2 metros" (as duas nuvens e Sono Coletivo). Nenhum campo de raio para magias de ÁREA — `condicaoRaioM` só existe do lado da condição, e as magias de dano em área (Chuva de Fogo, Nuvem de Fogo, Géiser) não têm nenhum campo de raio.
- **Campo sugerido:** `areaRaioMinimoM: Int + areaRaioM / areaRaioPorEnergia`

### Jato de Vapor / Sopro de Vapor / Dissipar Ar / Jato de Neve / Adaga de Gelo
- **Regra sem campo:** Dano condicional por TIPO de alvo: Jato de Vapor "causa o dobro de dano a criaturas de fogo ou gelo"; Dissipar Ar "quaisquer seres que possuam a metacaracterística Corpo de Ar sofrem 2d pontos de dano" (em vez de 1d-2); Jato de Neve "causa dano a criaturas de fogo"; Adaga de Gelo explicitamente "nenhum efeito extra em criaturas de fogo". Nenhum multiplicador condicional no schema.
- **Campo sugerido:** `danoVsTipoAlvo: {tipo: String, multiplicador: Double | danoSubstituto: String}`

### Afiar
- **Regra sem campo:** O bônus escala DOBRANDO/TRIPLICANDO o custo, não somando energia por nível: "Duplique-os para +2 de bônus ou triplique para +3", e o custo base varia por tamanho da lâmina (1 flecha, 2 faca/lança/machado, 3 espada de uma mão, 5 de duas mãos). O schema fixou buffDanoArma: 1 sem buffEnergiaPorNivel — não há como comprar +2/+3 nem calcular o custo pela arma. Além disso o bônus só vale para armas "de corte e perfuração" e `buffArmaTipo` só distingue cac/distancia.
- **Campo sugerido:** `buffCustoMultiplicadorPorNivel: Boolean + buffCustoPorTipoDeArma: Map + buffArmaTiposDeDano: List<String> ("cort","perf")`

### Chicote de Relampago / Olhar de Relampago / Jato de Chamas / Jato de Ácido / Jato de Areia
- **Regra sem campo:** A energia compra ALCANCE, não dano: Chicote "1 para operar com dois metros de alcance (máximo de 8 metros)"; Olhar de Relâmpago "A distância é de 2 metros por ponto de energia"; Jato de Ácido/Chamas "A distância é igual ao número de dados"; Jato de Areia "alcance em metros = energia". Sem campo, o motor não sabe até onde a magia chega no grid tático (pode deixar acertar de 30m ou negar a 3m).
- **Campo sugerido:** `alcanceMPorEnergia: Int + alcanceMaximoM: Int`

### Chuva de Ácido / Chuva de Fogo / Chuva de Pedras
- **Regra sem campo:** Duas regras concretas de mitigação: "se menos de um segundo inteiro for gasto na área afetada, o dano será reduzido pela metade" e "um escudo com um BD de 2 ou superior pode ser mantido acima da cabeça para bloquear" (proteção automática, custa uma ação Preparar e despreparara o escudo). Também "a mágica só pode ser operada em áreas externas". Nenhum dos três tem campo.
- **Campo sugerido:** `danoMeioSeSaiDaArea: Boolean + bloqueioPorEscudoBdMinimo: Int (=2) + requerAoArLivre: Boolean`

### Bola de Ácido / Jato de Ácido / Sopro de Ácido / Chuva de Ácido
- **Regra sem campo:** São dano por ÁCIDO/corrosão (modificador de ferimento cor, que também degrada RD), mas o enum `tipoDano` não tem valor para corrosão — as quatro ficaram SEM tipoDano e a nota registra "dano por ácido (corrosão), sem tipo padrão". O motor cai num default e aplica o multiplicador de ferimento errado.
- **Campo sugerido:** `tipoDano: adicionar "cor" (corrosão) ao enum`

### Raio Solar
- **Regra sem campo:** Riders com número: "Um golpe nos olhos DUPLICA o dano e cega a vítima, a menos que ela faça um teste de HT com uma penalidade igual ao dano sofrido"; "Aumente o BD de um escudo contra essa mágica em 50%, arredondando para baixo, se ele for altamente polido". O schema só tem condicao:"cego"/condicaoResistencia:"HT" — nem a duplicação por local, nem o HT penalizado pelo dano.
- **Campo sugerido:** `condicaoResistencia: "HT_menos_dano" + danoMultiplicadorLocal: {olhos: 2}`

### Bola de Relampagos
- **Regra sem campo:** A bola é uma entidade persistente com velocidade: "se move em linha reta a uma velocidade máxima igual ao NH do operador dividida por 5 (arredondar para baixo)", atravessa obstáculos não metálicos, "explode sob o comando mental do operador ou em contato com um ser vivo ou objeto metálico" e "se a mágica expirar, a Bola desaparecerá sem explodir". O schema trata como área instantânea — no grid tático não existe o token que se move nem o gatilho de detonação.
- **Campo sugerido:** `entrega: "entidade_movel" + entidadeVelocidadeNhDiv: Int (=5) + entidadeGatilhoDetonacao: String`

## ⚪ BAIXO

### Enfraquecer
- **Regra sem campo:** "Nenhum operador pode usar esta mágica no mesmo objetivo mais de uma vez por hora." É um cooldown por par (operador, alvo) que o motor ignora completamente — dá para empilhar Enfraquecer todo turno.
- **Campo sugerido:** `cooldownPorAlvoSegundos: Int (=3600)`

### Quietude
- **Regra sem campo:** "Adicione um bônus de 3 ao NH de Furtividade do objetivo... ou subtraia 5 do teste de Audição de qualquer pessoa que esteja ouvindo." São dois números concretos de perícia sem nenhum campo (o schema só tem buffs de combate).
- **Campo sugerido:** `buffPericia: {pericia: String, valor: Int} (ex.: Furtividade +3)`

### Desidratar / Geladura
- **Regra sem campo:** "Afeta toda a vítima se for operada à distância; se o operador tocar o objetivo, o dano será limitado à parte tocada." A entrega está fixada como "toque", mas a mesma magia tem duas variantes com efeito diferente (corpo todo × membro), e dano limitado a um membro obedece ao teto de lesão do membro.
- **Campo sugerido:** `danoLocalizadoNoToque: Boolean`
