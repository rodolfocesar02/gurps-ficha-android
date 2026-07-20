# Mapa das Regras de Combate — GURPS Artes Marciais

**Fonte:** `app/src/main/assets/chunks.jsonl`, livro `pt_artes_marciais` (GURPS 4ª ed. Artes Marciais, 264 páginas).
**Propósito:** inventário das regras de combate do Artes Marciais para planejar o que pode entrar no
combate do modo **Saga** — análogo ao audit do `Combate.md`. NÃO é implementação; é o mapa.
**Como foi feito:** lido o índice completo (headings h2–h6 de todas as páginas), a **Tabela de Técnicas**
(p258–262, lista TODAS as técnicas com pré-req/predefinido/dano/efeito) e o miolo do Capítulo 4 (Opções de
Combate, p109–113). Seções de que só li o título estão marcadas "(ler antes de implementar)".

> ⚠️ **AVISO DE VALIDADE (18/jul/2026).** Este mapa foi escrito quando o Saga usava **só faixas de
> distância**. A **grade de hexágonos existe desde então** (HEX-1..9 + VTT 2D), então todo item
> marcado 🔴 *"posicional/hexágono"* merece reavaliação — a justificativa caducou. Além disso, vários
> itens 🟢/🟡 **já foram implementados** nos lotes PONTE-1..4 e estão remarcados ⚪ abaixo.
> Consolidado em `docs/pendencias/PENDENCIAS.md`.

## Legenda de encaixe no Saga (modelo de FAIXAS, sem hexágono, NPC com dado limitado)
- 🟢 **FIT** — encaixa no modelo abstrato; codável com o que já existe.
- 🟡 **PARCIAL** — encaixa adaptado / precisa de dado novo (ex.: peso/qualidade de arma do NPC, perícia de luta do NPC).
- 🔴 **FORA** — não encaixa: posicional/hexágono, montaria, cinematográfico, ou opção de construção de personagem (não de resolução).
- ⚪ **JÁ FEITO** — já implementado no Saga (vindo do Módulo Básico / lotes 364–422).

> Observação estrutural: grande parte do Artes Marciais é **construção de personagem** (estilos p140–210,
> vantagens p42–53, perícias p54–62) e **cenário** (história p0–27, campanhas p235–250) — não são regras de
> resolução de combate. O coração das REGRAS está no **Capítulo 4 (p96–139)** e nas **Técnicas (p63–95)**.

---

## 1. TÉCNICAS (Cap. 3, p63–95; tabela completa p258–262)

Uma "técnica" é uma especialização de uma manobra existente, comprada acima de um valor predefinido (ex.:
"Chute" predefine em Briga/Caratê −2). No Saga **o herói não tem técnicas compradas**, então a maioria entra
(se entrar) como **opção tática** com o modificador fixo da técnica. ~110 técnicas, agrupadas por função:

### 1A. Golpes desarmados / variações de ataque — 🟡 PARCIAL
Modificam dano/acerto de um soco/chute. Encaixam como "modo de ataque desarmado" com o modificador fixo.
- **Chute** (GdP cont, +1 alcance, −2 acerto), **Chute Descendente**, **Pisão** (GdP+1), **Joelhada** (GdP),
  **Cotovelada** (GdP−1), **Soco de Martelo** (GdP−2), **Soco de Duas Mãos** (GdP+1/+1 por dado), **Uppercut**,
  **Golpe Exótico com as Mãos**, **Cabeçada** (GdP−1). Limitação: o Saga não modela localização fina nem
  "perna vs pé"; entrariam como bônus/penalidade de dano no ataque desarmado.

### 1B. Golpes a alvos específicos (olhos / pontos de pressão / atordoar) — 🔴/🟡
Dependem de localização precisa + efeito tipo Atribulação (Cegueira, Dor, Atordoamento, Surdez).
- **Dedada no Olho**, **Arranhão nos Olhos** (Cegueira), **Golpe nos Olhos**, **Telefone** (Surdez+Atordoa),
  **Pontos de Pressão***, **Tapa na Fuça** (Atordoa+Desarme). FORA enquanto não houver modelo de afflição/local
  fino para o herói desarmado; alguns (atordoar) seriam 🟡 se reduzidos a "teste → ATORDOADO".

### 1C. Chaves, imobilizações e estrangulamentos — ⚪ **FEITO (Lote PONTE-1)**
> Chave de Membro e Mata-Leão herói↔NPC implementados; o NPC agressivo parte para a chave quando já agarrou.
O Saga já tem Agarrar/Imobilizar/Estrangular/Desvencilhar (lotes 386/411/412/422). Estas adicionam **chaves
por Disputa Rápida → dano/dor**:
- **Chave de Braço/Pulso/Cotovelo**, **Chave de Cabeça**, **Chave de Dedo**, **Chave de Perna**, **Mata-Leão**
  (DR), **Tesoura**, **Triângulo**, **Torção de Pescoço/Coluna/Membro** (GeB cont), **Bate-Estaca*** (cinematográfico).
  Encaixe: como manobras extras quando há alvo AGARRADO, resolvendo DR de ST → dano/incapacitação. ⚠️ Precisam
  de localização do membro agarrado (parcial no modelo atual).

### 1D. Arremessos e derrubadas — 🟡 PARCIAL
- **Arremesso do Judô**, **Arremesso com Perna**, **Arremesso de Sacrifício**, **Rasteira** (Nocaute),
  **Cotovelada em Queda**, **Joelhada em Queda**, **Cabeçada**. Encaixe: ataque → Nocaute/Atordoamento +
  CAIDO. O Saga já tem Derrubar/Encontrão; estes seriam variações com perícia de luta (dado do NPC limita).

### 1E. Técnicas defensivas — 🟡 PARCIAL
- **Aparar Agressivo** (apara + dano), **Obstruir**, **Defesa de Linha Baixa**, **Evasão**, **Queda**,   **Rolar com o Golpe***, **Levantar Acrobático**, **Defesa Cronometrada***, **Defesa contra Duas Armas***.
  Encaixe: modificadores de defesa ativa; alguns já cobertos por Esquiva Acrobática/Recuo do Saga.

### 1F. Técnicas com armas — 🟡/🔴
- **Desarmar** (Desarme), **Reter Arma**, **Conservar a Arma**, **Golpe Rodopiante** (Finta), **Golpe Para   Trás**, **Enganchar**, **Envolver**, **Estalar** (chicote), **Quebrar Arma***, **Contra-Ataque**, **Fintar**
  (já existe). Encaixe parcial: Desarmar e Reter precisam do modelo de "arma na mão" (agora existe `confiscado`,
  lote 421); o resto é posicional/por tipo de arma.

### 1G. Montado / à distância especial — 🔴 FORA
- **Cavalgar em Combate**, **Arquearia Montada**, **Tiro Montado**, **Treinamento de Cavalaria**,
  **Combate Corporal à Distância**. Sem montaria/posicionamento no Saga.

### 1H. Cinematográficas (*) — 🔴 FORA (campanha realista)
- **Golpe Letal**, **Chute Letal**, **Dedada Letal no Olho**, **Voadora Aérea**, **Ataque Giratório**,
  **Estocada Aérea**, **Desarme Grandioso**, **Ataque Acumulado**, **Golpe em Ponto de Pressão**, etc.
  (~25 técnicas com *). Fora do realismo do Saga.

### 1I. Descontraídas (†) — 🔴 FORA (cômicas)
- **Ataque de Bafo de Onça**, **Cuecão Atômico Aéreo**, **Dedo Molhado na Orelha**, **Esfregão na Cabeça**, etc.

> A **Tabela de Técnicas (p258–262)** é a lista canônica completa (dificuldade, pré-requisito, predefinido,
> máximo, dano, página) — usar como índice mestre ao implementar qualquer técnica.

---

## 2. CAPÍTULO 4 — COMBATE (p96–139): as REGRAS de resolução (núcleo)

### 2.1 Manobras de Combate Expandidas (p97–108) — (ler antes de implementar, salvo onde notado)
- **Aguardar / Interromper Acertos / Aguardar Encadeado** (p97) — interromper o turno do oponente. 🟡 (Saga tem Aguardar/Interromper Investida, lote 399).
- **Apontar** (p98) — ⚪ JÁ FEITO (lote 392/395).
- **Ataque Dedicado** (p98) — +acerto OU +dano abrindo mão de defesa parcial. ⚪ **FEITO (Lote PONTE-4)**.
- **Ataque Defensivo** (p98) — −dano por +defesa. ⚪ **FEITO (Lote PONTE-4)**.
- **Ataque Total** (p99) — opções Determinado/Duplo/Fintar/Forte ⚪ JÁ FEITO; **nova opção "Longo"** (+1 alcance) 🟡.
- **Avançar e Atacar** (p100) — ⚪ JÁ FEITO (Mover e Atacar, lote 378).
- **Lidando com Oponentes em Carga** (p101): Aparar, **Obstrução**, **Manter à Distância**, **Movimento Acrobático** — 🟡/🔴 (parte posicional).
- **Movimentos acrobáticos** (Derrapagem, Deslizar, Evadir, Girar, Piruetas, Tic-Tacs, Salto/Mergulho, p102–103) — 🔴 posicional.
- **Detectando Fintas** (p104) — 🟡.
- **Mudança de Posição / Posturas** (p104–105): De Pé, Ajoelhado, Sentado, Rastejando, Deitado de Costas/Bruços — ⚪ parcialmente feito (posturas no Saga, lote 416); detalhes de "lutar de cada postura" 🟡.
- **Preparar** (p105–108): **Empunhadura Defensiva/Invertida**, **Sacar Rápido em Série**, **Quem Saca Primeiro?**, **Preparar Rápido Armas Próximas**, **Alternar a Perícia com Arma** — 🟡 (Saga tem Preparar/sacar, lotes 374/406; o resto é detalhe).

### 2.2 Opções de Combate (p109–113) — LIDO EM DETALHE
- **Caneladas** (chutar com a canela) — 🟡 detalhe de dano desarmado.
- **Combinações** (p109/80) — sequência fixa de ataques (caso especial de Golpe Rápido); falha cancela o resto (+3 defesa ao alvo). 🟡 (Saga tem Golpe Rápido, lote 408).
- **Ataque Telegráfico** (p109) — **+4 para acertar, mas +2 à defesa do alvo** (oposto do Enganoso). ⚪ **FEITO (Lote PONTE-3)** — inclusive a regra de que o crítico usa NH−4 mas nunca vira falha crítica.
- **Balanço vs. Ponta / Peso da Arma / Comprimento da Arma** (p110) — modificadores finos em "quem golpeia primeiro"/Aguardar. 🔴 precisa de peso/comprimento/ST da arma (não modelado p/ NPC).
- **Corte com a Ponta** (p111) — converter perfuração em corte (−2 dano). 🟡 modo de ataque alternativo da arma.
- **Empunhadura Defensiva** (p111) — +1 aparar frontal / −1 lateral; gasta Preparar. 🟡 (Saga não modela arco lateral).
- **Empunhadura Invertida** (p112) — empunhadura "picador de gelo"; muda alcance/dano. 🔴 detalhe por arma.
- **Empurrões com Armas / Encontrões com Armas Longas** (p112) — empurrar/derrubar com arma. 🟡 (Saga tem Empurrão/Encontrão, lotes 409/410).
- **Golpe com o Pomo** (p113) — atacar com o cabo (GdP cont). 🟡 modo de ataque alternativo.
- **Golpeando Escudos** (p113) — atacar/destruir escudo/capa (usa Golpes Visando a Arma). 🟡 (Saga não modela durabilidade de objeto — relacionado ao deferido "Aparando Armas Pesadas").
- **O Que É um Ataque Enganoso?** (p111) — texto de cor; mecânica ⚪ JÁ FEITA (lote 401).

### 2.3 Combate Corporal / Luta Agarrada Profunda (p114–119) — (ler antes de implementar)
- **Combate Corporal**, **Armas Longas em Combate Corporal**, **Combate Corporal e Morfologia** (braços/dentes/pernas extras), **Como Fica o Agarrão com Uma Mão**, **Defesa de Queda**, **Dor no Combate Corporal** (chaves infligem Dor; Dor e Desvencilhamento), **Segurar e Bater!** (Agarrar Total e Golpear, "Beijar a Parede", Mais Ações Depois de Agarrar), **Arremessos a partir de Chaves**, **Empurrando Pessoas Por Aí**. 🟢/🟡 — **extensão natural da luta agarrada NPC↔herói já feita (lote 422)**; é a área de maior sinergia.

### 2.4 Preparações para Ataque à Distância (p119–121) — 🟡/🔴
- **Arcos de Tiro Rápido**, **Tiro Engenhoso**. Detalhe de munição/arco (Saga deferiu munição, decisão 366).

### 2.5 Opções de Defesa (p121–123) — 🟡 (várias FIT)
- **Aparar com Armas de Duas Mãos**, **Aparar com Armas de Esgrima** ⚪ (esgrima já tratada, lote 375/389), **Aparar com Armas Desbalanceadas** ⚪ (lote 398/406), **Aparar com Pernas ou Pés**, **Aparar Cruzado**, **Bloqueios Múltiplos**, **Defesa Durante o Agarrão** ⚪ (−4 preso, lote 422), **Limitando Esquivas**, **Opções de Retirada** ⚪ (Retirada, lote 389), **Realismo Severo para Lutadores Desarmados**. 🟡 — alguns codáveis (bloqueios múltiplos, aparar cruzado).

### 2.6 Ataques Múltiplos / Combates Mais Rápidos (p126–128) — 🟡
- **Combates Mais Rápidos**, **Ataques Múltiplos** (interação Golpe Rápido × Combinações × Ataque com Duas Armas), **Golpe Rápido** ⚪ (lote 408), **Fintas e Ataques Múltiplos**, **Vários Alvos**. 🟡 (Saga não modela múltiplos alvos por turno simultâneos).

### 2.7 Regras Cinematográficas (p129–133) — 🔴 FORA (campanha realista)
- **Defesas Chambara**, **Proezas Cinematográficas** (Passos Leves, Salto Voador, Escalada do Lagarto),
  **Jogos Mentais** (Disputa de Vontades, Concentração, Medo, Fingindo) — 🟡 só a parte de Medo/Vontade,
  **Regras Cinematográficas de Combate**, **Tempo de Bala**.

### 2.8 Combate de Torneio (p134–135) — 🔴 narrativo (domínio do Narrador)
- Métodos de Disputa Rápida/Detalhado, tipos de competição, combate judicial/justas.

### 2.9 Lesões Realistas (p136–139) — 🟡 (alta sinergia com o motor de dano do Saga)
- **Lesões Parciais** (Braço/Perna/Mão/Pé/Tronco), **Desmembramento Extremo**, **Sangramento Grave**,
  **Enfaixando Ferimentos Graves**, **Lesões Duradouras e Permanentes**, **Tabelas de Ferimentos** (Crânio,
  Órgãos Vitais, Veias/Artérias, Pescoço). 🟡 — o Saga já tem localização+multiplicadores+choque+desmaio;
  **Sangramento** e **incapacitação de membro** seriam as adições de maior valor (e o usuário já perguntou sobre sangramento no teste de batalha — item 5). ⚠️ ler as tabelas antes de codar.

---

## 3. VANTAGENS / PERÍCIAS DE COMBATE (p42–62) — 🔴 construção de personagem (não-resolução)
Relevantes ao combate, mas são da FICHA, não do motor: Treinado por um Mestre, Mestre de Armas, ST de Golpe,
Aparar/Bloquear/Esquiva Ampliados, Reflexos de Combate, Golpeadores, Ataque Adicional, Poderes do Chi;
perícias novas (Pontos de Pressão, Golpe Poderoso, Arte/Esporte de Combate, etc.). Entram via Forjador/ficha,
não como regra de turno. ⚪ A ficha já suporta vantagens/perícias; o efeito em combate é caso a caso.

## 4. ARMAS E EQUIPAMENTOS (p211–234) — 🟡/🔴
- **Armas de Qualidade** (barata/fina/altíssima) — 🟡 precisa de campo de qualidade no Equipamento (não existe).
- **Ataques Pouco Ortodoxos** (arma de 2 mãos usada com 1; arma corpo-a-corpo arremessada) — 🟡.
- **Aparando Manguais** (Módulo 6) — 🔴 (relacionado ao deferido "Aparando Armas Pesadas": peso de arma).
- **Armas Combinadas / Ocultas / Disfarçadas**, tabelas de armas, equipamento de treino — 🔴 dado/catálogo.

---

## 5. RECOMENDAÇÃO (maior valor × menor custo para o Saga)
Ordem sugerida se algum dia abrir uma "Fase Artes Marciais" no combate do Saga:
1. 🟢 **Ataque Telegráfico** (p109) — par do Enganoso, trivial (+4 acerto / +2 defesa do alvo).
2. 🟢/🟡 **Luta agarrada profunda** (p114–119) — estende o agarrão NPC↔herói do lote 422: chaves (Chave de
   Braço/Mata-Leão → DR de dano), "Segurar e Bater", arremessos a partir de chave. Maior sinergia.
3. 🟡 **Sangramento Grave + incapacitação de membro** (p136–138) — completa a parte de "lesões realistas" que
   o usuário perguntou no teste de batalha; encaixa no motor de dano/localização existente.
4. 🟡 **Ataque Dedicado / Ataque Defensivo** (p98) — duas manobras novas simples (troca acerto↔defesa↔dano).
5. 🟡 **Opções de defesa codáveis** (p121–123): Bloqueios Múltiplos, Aparar Cruzado.

**Fora de escopo (não codável no modelo de faixas):** tudo posicional/hexágono (movimentos acrobáticos,
comprimento/arco de arma, vários alvos simultâneos), montaria, cinematográfico (*), descontraído (†), e o que
depende de dado que o NPC não tem (peso/qualidade/comprimento da arma).

> ⚠️ Este mapa é o ÍNDICE. Antes de implementar QUALQUER item, ler a página indicada no chunk e a Tabela de
> Técnicas (p258–262) — várias regras têm exceções que não cabem aqui (igual ao protocolo do `Combate.md`).
