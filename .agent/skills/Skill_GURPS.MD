---
name: skill-gurps-4e-regras-mestras
description: Compêndio mestre de regras de GURPS 4ª Edição (Módulo Básico - Personagens e Campanhas), em PT-BR (tradução Devir). Cobre criação de ficha, atributos, características secundárias/sociais, vantagens, desvantagens, modificadores, perícias, técnicas, magia, equipamento, carga, e o sistema completo de combate (manobras, defesas ativas, dano, ferimentos, localização de acerto). Consulte este arquivo para qualquer dúvida de regra de GURPS 4e.
---

# 🎲 SKILL GURPS — Compêndio Mestre de Regras (4ª Edição)

> **Escopo:** GURPS 4e — Módulo Básico (*Basic Set: Characters* + *Campaigns*), tradução
> brasileira Devir. Este documento é a referência de regras para o "Mestre IA" (Auditor e
> Forjador) deste app. Terminologia em PT-BR oficial. Números e tabelas conferidos contra o
> `livros de gurps/modulo_basico_text.txt`.
>
> **Convenção:** `3d` = rolagem de 3 dados de 6 lados (3d6). Páginas citadas referem-se ao
> Módulo Básico. Quando uma regra tem variações por gênero de campanha, está marcado.

---

## ÍNDICE

1. [Filosofia e mecânica central](#1-filosofia-e-mecânica-central)
2. [Testes de sucesso, margens e críticos](#2-testes-de-sucesso-margens-e-críticos)
3. [Disputas (regular e rápida)](#3-disputas)
4. [Atributos primários](#4-atributos-primários)
5. [Características secundárias](#5-características-secundárias)
6. [Dano básico (GdP / GeB)](#6-dano-básico-gdp--geb)
7. [Modificador de Tamanho (MT)](#7-modificador-de-tamanho-mt)
8. [Características sociais e de campanha](#8-características-sociais-e-de-campanha)
9. [Aparência e idade](#9-aparência-e-idade)
10. [Criação de personagem — orçamento de pontos](#10-criação-de-personagem)
11. [Vantagens](#11-vantagens)
12. [Desvantagens](#12-desvantagens)
13. [Modificadores (ampliações e limitações)](#13-modificadores)
14. [Perícias](#14-perícias)
15. [Técnicas](#15-técnicas)
16. [Magia](#16-magia)
17. [Riqueza e equipamento](#17-riqueza-e-equipamento)
18. [Carga e deslocamento](#18-carga-e-deslocamento)
19. [Combate — sistema completo](#19-combate)
20. [Dano, ferimentos e recuperação](#20-dano-ferimentos-e-recuperação)
21. [Tabelas de referência rápida](#21-tabelas-de-referência-rápida)
22. [Glossário de abreviações PT-BR](#22-glossário-de-abreviações)

---

## 1. Filosofia e mecânica central

GURPS = **Generic Universal RolePlaying System**. Sistema genérico baseado em pontos, escala
3d6 ("role-under"), aplicável a qualquer cenário (medieval, moderno, futurista, super-heróis,
horror etc.).

**Núcleo do sistema:**
- Todo personagem é definido por **pontos de personagem**. Campanhas típicas: 100–200 pts.
- Tudo se resolve rolando **3d** e comparando com um **Nível de Habilidade (NH)** alvo
  (atributo ou perícia). **Rolar ≤ NH = sucesso.**
- A curva de 3d6 é em sino: média 10,5; resultados extremos (3, 18) são raros (~0,46% cada).
- **Quanto mais baixo o resultado, melhor.** Margem de Sucesso (MdS) = NH − resultado.

**Faixa de competência (referência rápida de NH):**
| NH | Significado |
|----|-------------|
| 6 ou menos | Incompetente / só sob calma total |
| 8–9 | Amador / iniciante |
| 10–11 | Competente (NH típico de profissional médio) |
| 12–13 | Bom / profissional experiente |
| 14–15 | Excelente / veterano |
| 16–18 | Mestre |
| 20+ | Lendário / sobre-humano |

**Probabilidade de sucesso com 3d (acumulada):**
| NH | % sucesso | NH | % sucesso |
|----|-----------|----|-----------|
| 3 | 0,46% | 11 | 62,50% |
| 4 | 1,85% | 12 | 74,07% |
| 5 | 4,63% | 13 | 83,80% |
| 6 | 9,26% | 14 | 90,74% |
| 7 | 16,20% | 15 | 95,37% |
| 8 | 25,93% | 16 | 98,15% |
| 9 | 37,50% | 17 | 99,54% |
| 10 | 50,00% | 18 | 100%* |

\* 17 e 18 **sempre falham**, independentemente do NH (ninguém acerta 100% das vezes).

---

## 2. Testes de sucesso, margens e críticos

### Tipos de teste
- **Teste de atributo:** role 3d ≤ atributo (eventualmente ± modificadores).
- **Teste de perícia:** role 3d ≤ NH da perícia (já inclui bônus/penalidades de equipamento e
  situação).
- **Teste de resistência (TR):** rolagem para resistir a um efeito (veneno, medo, magia etc.),
  geralmente contra HT, Vontade ou um atributo específico.

### NH básico vs. NH efetivo
- **NH básico:** o valor não modificado (mede competência em condições típicas de aventura —
  tensão, consequências reais).
- **NH efetivo:** depois de aplicar todos os modificadores (situação, distância, iluminação,
  pressa, equipamento, ferimentos etc.).

### Modificadores de tarefa (escala padrão)
| Dificuldade        | Mod | | Dificuldade          | Mod |
|--------------------|-----|-|----------------------|-----|
| Automática         | +10 | | Difícil              | −2  |
| Trivial            | +8  | | Muito difícil        | −4  |
| Muito fácil        | +6  | | Penosa               | −6  |
| Fácil              | +4  | | Quase impossível     | −8  |
| Fácil de fazer     | +2  | | Impossível (limite)  | −10 |
| Padrão             | 0   | |                      |     |

### Margens
- **Margem de Sucesso (MdS)** = NH − resultado. Importa em disputas, perícias graduadas
  (medicina, criação artística) e dano de combate.
- **Margem de Falha (MdF)** = resultado − NH.

### Acertos e falhas críticas
**Acerto crítico (sucesso espetacular):**
- **3 ou 4** → sempre crítico.
- **5** → crítico se NH efetivo ≥ 15.
- **6** → crítico se NH efetivo ≥ 16.

**Falha crítica (desastre):**
- **18** → sempre falha crítica.
- **17** → falha crítica se NH efetivo ≤ 15; se NH ≥ 16, é só falha comum.
- Qualquer resultado que falhe por **10 ou mais** (resultado ≥ NH + 10) → falha crítica.

Em combate, críticos usam a **Tabela de Acerto Crítico** e **Tabela de Falha Crítica** (efeitos
dramáticos: dano dobrado, ignora defesa, derruba arma, acerta a si mesmo etc.).

---

## 3. Disputas

### Disputa Regular
Ambos os lados fazem um teste contra seu NH. Resultados possíveis:
- Um obtém sucesso, o outro falha → quem teve sucesso vence.
- Ambos falham → impasse (geralmente nada acontece; repete-se).
- Ambos têm sucesso → vence quem tiver **maior MdS**; empate de MdS = impasse.

### Disputa Rápida
Cada lado faz **um** teste; compara-se a **Margem de Sucesso**. Vence a maior MdS. Empate =
impasse (status quo). Usada para Braço de Ferro (ST), Dissimulação vs. Observação, Furtividade
vs. Sentidos etc. (uma falha conta como MdS negativa para fins de comparação quando a regra
pede).

---

## 4. Atributos primários

Quatro atributos, todos com **valor inicial 10** (humano mediano). Custo é por nível acima/abaixo
de 10:

| Atributo        | Sigla| Mede                                            | Custo/nível |
|---------------- |------|------------------------------------------------ |-------------|
| **Força**       | ST   | Massa muscular, capacidade de carga, dano físico| **10 pts**  |
| **Destreza**    | DX   | Agilidade, coordenação, reflexos                | **20 pts**  |
| **Inteligência**| IQ   | Raciocínio, percepção, vontade, aprendizado     | **20 pts**  |
| **Vitalidade**  | HT   | Saúde, energia, resistência física              | **10 pts**  |

- Reduzir um atributo abaixo de 10 **devolve pontos** (mesma tabela, valor negativo).
- DX e IQ são caros porque governam a maioria das perícias (DX → perícias físicas; IQ → perícias
  mentais).
- ST afeta PV e dano; HT afeta PF, Velocidade Básica e testes de resistência físicos.
- Faixa humana realista: 1 a 20 (8 = fraco/doente; 14 = atleta de elite). Acima de 20 entra em
  território sobre-humano (heróis, monstros).

---

## 5. Características secundárias

Derivadas dos atributos primários. Cada uma pode ser comprada/reduzida separadamente.

| Característica      |Sigla| Fórmula base                      | Custo |
|---------------------|-----|-----------------------------------|-------|
| **Pontos de Vida**  |PV   | = ST                              | **±2 pts por PV** |
| **Vontade**         |—    | = IQ                              | **±5 pts por nível** |
| **Percepção**       |Per  | = IQ                              | **±5 pts por nível** |
| **Pontos de Fadiga**|PF   | = HT                              | **±3 pts por PF** |
| **Veloc. Básica**   |—    | = (HT + DX) / 4                   | **±5 pts por 0,25** |
| **Desloc. Básico**  |—    | = Veloc. Básica (arred. p/ baixo) | **±5 pts por nível** |

**Detalhamento:**
- **PV** — capacidade de absorver dano antes de cair. Para fantasia/mundano humano, costuma-se
  limitar a ±30% da ST.
- **Vontade** — resiste a medo, intimidação, tortura, magia mental, distração. Testes de
  autocontrole de desvantagens usam Vontade implicitamente.
- **Percepção (Per)** — todos os testes de sentidos (Visão, Audição, Olfato, etc.) partem de Per.
- **PF** — "combustível": esforço extra, correr, segurar a respiração, lançar magias, manobras
  especiais. Chega a 0 = exausto.
- **Velocidade Básica** — ordem de iniciativa no combate e base da Esquiva. Não arredonda (ex.:
  5,25). Frações de 0,25 custam 5 pts cada.
- **Deslocamento Básico** — quantos metros (ou jardas) o personagem anda por turno sem carga.
  É a Velocidade Básica truncada (5,75 → 5).

### Esquiva (defesa ativa derivada)
**Esquiva = Velocidade Básica + 3** (arredondado para baixo). Reduzida por carga. É uma das três
defesas ativas (ver §19).

---

## 6. Dano básico (GdP / GeB)

A ST determina o **dano básico** desarmado/corpo-a-corpo, em duas colunas (siglas oficiais Devir):
- **GdP (Golpe de Ponta / *thrust*)** — socos, estocadas, golpes de impulso.
- **GeB (Golpe em Balanço / *swing*)** — golpes em arco, mais fortes (alavanca).

| ST | GdP  | GeB  | | ST | GdP  | GeB  |
|----|------|------|-|----|------|------|
| 1 | 1d-6 | 1d-5 | | 11 | 1d-1 | 1d+1 |
| 2 | 1d-6 | 1d-5 | | 12 | 1d-1 | 1d+2 |
| 3 | 1d-5 | 1d-4 | | 13 | 1d   | 2d-1 |
| 4 | 1d-5 | 1d-4 | | 14 | 1d   | 2d   |
| 5 | 1d-4 | 1d-3 | | 15 | 1d+1 | 2d+1 |
| 6 | 1d-4 | 1d-3 | | 16 | 1d+1 | 2d+2 |
| 7 | 1d-3 | 1d-2 | | 17 | 1d+2 | 3d-1 |
| 8 | 1d-3 | 1d-2 | | 18 | 1d+2 | 3d   |
| 9 | 1d-2 | 1d-1 | | 19 | 2d-1 | 3d+1 |
| 10 | 1d-2| 1d   | | 20 | 2d-1 | 3d+2 |

**Continuação para ST alta (sobre-humano):**
| ST | GdP  | GeB  | | ST | GdP  | GeB  |
|----|------|------|-|----|------|------|
| 21 | 2d   | 4d-1 | | 35 | 4d-1 | 6d+1 |
| 22 | 2d   | 4d   | | 40 | 4d+1 | 7d-1 |
| 25 | 2d+2 | 5d-1 | | 45 | 5d   | 7d+1 |
| 27 | 3d-1 | 5d+1 | | 50 | 5d+2 | 8d-1 |
| 30 | 3d   | 5d+2 | | 60 | 7d-1 | 9d   |
| 33 | 3d+2 | 6d   | | 70 | 8d   | 10d  |

Armas adicionam um modificador ao dano básico (ex.: espada larga = GeB+2 corte; faca = GdP-1
perfurante).

### Tipos de dano e modificador de ferimento
Após a RD reduzir o dano, o que penetra é multiplicado pelo modificador do tipo:

| Tipo (PT-BR)           | Sigla | Multiplicador de ferimento |
|------------------------|-------|----------------------------|
| Contusão               | cont  | ×1                         |
| Corte                  | cort  | ×1,5                       |
| Perfurante pequena     | perf- | ×0,5                       |
| Perfurante             | perf  | ×1                         |
| Perfurante grande      | perf+ | ×1,5                       |
| Perfurante enorme      | perf++ | ×2                        |
| Perfuração             | Perf  | ×2                         |
| Queimadura             | qmd   | ×1                         |
| Corrosão               | cor   | ×1                         |
| Toxina                 | tox   | ×1 (ignora RD, salvo regra)|

> Ex.: 8 de dano corte que penetra 6 após RD → 6 × 1,5 = **9 PV de ferimento**.

---

## 7. Modificador de Tamanho (MT)

Mede o tamanho físico relativo a um humano (MT 0). Afeta a chance de ser **acertado/percebido**:
um alvo grande recebe seu MT como bônus para os atacantes e é mais fácil de notar; um alvo
pequeno dá penalidade aos atacantes.

| MT | Comprimento aprox. | Exemplo                      |
|----|--------------------|------------------------------|
| −4 | 30 cm              | gato pequeno, rato grande    |
| −3 | 60 cm              | cão pequeno                  |
| −2 | 1 m                | criança, cão grande          |
| −1 | 1,5 m              | humano baixo / criança grande|
| 0  | ~1,8 m             | **humano adulto padrão**     |
| +1 | 2–3 m              | cavalo, urso                 |
| +2 | 3–4,5 m            | carro, elefante              |
| +3 | 4,5–6 m            | caminhão pequeno             |
| +4 | 6–9 m              | ônibus, T-rex                |

O MT também interage com a regra de **Velocidade/Distância** (mesma tabela linear que mede
distância e velocidade em metros → modificador).

### Tabela de Velocidade/Distância (resumo)
|Distância(m)| Mod | Distância (m) | Mod |
|------------|-----|---------------|-----|
| 2          | 0   | 20            | −6  |
| 3          | −1  | 30            | −7  |
| 5          | −2  | 50            | −8  |
| 7          | −3  | 70            | −9  |
| 10         | −4  | 100           | −10 |
| 15         | −5  | 150           | −11 |

Usada para penalidade de alcance de ataques à distância e para perceber/atingir alvos longe.

---

## 8. Características sociais e de campanha

### Nível Tecnológico (NT / TL)
Define a tecnologia disponível. NT do personagem ≠ NT da campanha gera bônus/penalidade.

| NT   | Era                                          |
|------|-----------------------------------------------|
| 0    | Idade da Pedra                                |
| 1    | Bronze                                        |
| 2    | Ferro / Antiguidade Clássica                  |
| 3    | Medieval                                      |
| 4    | Renascença / Pólvora inicial                  |
| 5    | Revolução Industrial                          |
| 6    | Mecanizada (início séc. XX, I/II Guerra)      |
| 7    | Nuclear (pós-guerra, anos 1980)               |
| 8    | Digital (atual)                               |
| 9    | Microtecnologia / cibernética inicial         |
| 10–12| Futuro próximo a avançado                     |
| ^    | Ultratecnologia (ficção científica dura/mole) |

### Status social
Posição na hierarquia social. **5 pts por nível** (de −2 a +8 típico). Status alto exige custo de
vida proporcional. Riqueza alta concede Status grátis (1 nível por faixa de riqueza acima de
Confortável, até certo limite).

### Riqueza
Multiplicador da renda/recursos iniciais ($) em relação à média do NT:

| Nível | Multiplic. da riqueza inicial | Custo   |
|-------|-------------------------------|---------|
| Falido (Dead Broke) | $0              | −25 pts |
| Pobre (Poor)        | ×1/5            | −15 pts |
| Batalhador (Struggling) | ×1/2        | −10 pts |
| Médio (Average)     | ×1              | 0 pts   |
| Confortável (Comfortable) | ×2        | 10 pts  |
| Rico (Wealthy)      | ×5              | 20 pts  |
| Muito Rico (Very Wealthy) | ×20       | 30 pts  |
| Podre de Rico (Filthy Rich) | ×100    | 50 pts  |
| Multimilionário | ×1.000+             | 75+ pts |

(Riqueza inicial padrão NT8 ≈ $20.000.)

### Outras características sociais (custo/nível)
- **Posto/Patente Militar (Rank):** 5 pts/nível. Autoridade dentro de uma organização.
- **Posto Eclesiástico:** 5 pts/nível.
- **Reputação:** variável (+/− por faixa: quão conhecido, por quem, reação). Pode ser vantagem
  (boa fama) ou desvantagem (má fama).
- **Aliados (Ally / Aliados):** custo conforme poder e frequência de aparição.
- **Contatos (Contacts):** fontes de informação/favores; custo por habilidade, confiabilidade e
  frequência.
- **Grupo de Contatos:** rede inteira.
- **Patrono (Patron):** organização/pessoa poderosa que ajuda. Custo por poder e frequência.
- **Aliança/Favores, Dependentes (Dependents):** desvantagem (NPC que você deve proteger).
- **Inimigos (Enemies):** desvantagem (caçadores/rivais ativos).
- **Identidades, Idiomas, Cultura Familiar:** ver Idiomas abaixo.

### Idiomas e cultura
- **Idioma nativo:** grátis (Falado e Escrito em nível Nativo).
- Comprar idioma adicional: **Falado** e **Escrito** separadamente, em níveis: Nenhum / Sotaque
  Carregado (1 pt) / Sotaque (2 pts) / Nativo (3 pts) por modalidade. Falado + Escrito Nativo = 6
  pts (ou 3 se a língua não tem escrita).
- **Familiaridade Cultural:** 1ª cultura nativa grátis; adicionais 1 pt cada. Desconhecer uma
  cultura dá −3 em perícias sociais relevantes.

---

## 9. Aparência e idade

### Aparência
Vantagem/desvantagem que dá modificador de reação:

| Nível | Mod reação | Custo |
|-------|-----------|-------|
| Horrendo | −4 | −24 |
| Monstruoso | −5 a −... | −20 |
| Repugnante | −4 | −16 |
| Feio | −2 | −8 |
| Sem Atrativos | −1 | −4 |
| Comum | 0 | 0 |
| Atraente | +1 | 4 |
| Bonito/Bela | +4 (do sexo atraído) | 12 |
| Muito Bonito/Bela | +6 | 16 |
| Lindo/Deslumbrante | +8 | 20 |

Modificadores: "Universal" (afeta todos), "Anormal", "Off-the-Shelf Looks" etc.

### Idade e envelhecimento
- Definir idade é grátis. A partir de certas faixas (50, 70, 90 anos no NT moderno) o personagem
  faz **testes de envelhecimento** de HT periodicamente.
- **Imaturidade/Juventude:** personagens jovens podem ter ST/atributos reduzidos pela idade.
- Vantagens relacionadas: **Longevidade**, **Não Envelhece (Unaging)**.

---

## 10. Criação de personagem

### Passo a passo
1. **Conceito** — quem é o personagem, papel na campanha, gênero do cenário.
2. **Orçamento de pontos** — o Mestre define (ex.: 100, 150, 200, 250). Define também o **limite
   de desvantagens** (ex.: −50) e o **limite de peculiaridades** (geralmente 5).
3. **Atributos** — comprar ST/DX/IQ/HT.
4. **Características secundárias** — ajustar PV, PF, Vontade, Per, Velocidade, Deslocamento.
5. **Características sociais** — NT, Riqueza, Status, Reputação, Posto.
6. **Vantagens** — gastar pontos.
7. **Desvantagens** — ganhar pontos de volta (até o limite negociado).
8. **Peculiaridades (Quirks)** — até 5 traços de personalidade menores, −1 pt cada (devolvem
   pontos).
9. **Perícias e técnicas** — gastar o restante.
10. **Magia/poderes** (se aplicável).
11. **Equipamento** — comprar com a riqueza inicial.
12. **Toques finais** — nome, história, aparência, deslocamento final com carga.

### Orçamento (matemática)
```
Pontos gastos (atributos + vantagens + perícias + ...) 
   = Pontos iniciais + Pontos de desvantagens + Peculiaridades
```
A ficha deve **fechar exatamente** no total (ou abaixo, sobrando pontos não gastos).

### Limite de desvantagens
Padrão recomendado: o jogador só pode "ganhar" de volta até um teto (ex.: −40 ou −50 pts) em
desvantagens, mais até −5 em peculiaridades. Isso evita personagens carregados de defeitos só
para comprar poder.

### Autocontrole (para desvantagens mentais)
Muitas desvantagens mentais têm **número de autocontrole** (NA) que modifica o custo:
| Frequência | NA | Multiplicador de custo |
|------------|----|------------------------|
| Quase o tempo todo | 6 ou menos | ×2 |
| Frequentemente | 9 ou menos | ×1,5 |
| Às vezes | 12 ou menos | ×1 |
| Raramente | 15 ou menos | ×0,5 |
Para resistir ao impulso, role 3d ≤ NA.

---

## 11. Vantagens

Vantagens são traços que custam pontos. Lista das mais comuns do Módulo Básico (custo entre
parênteses; "/nível" = por nível):

### Físicas / corporais
- **Ambidestria (5):** sem penalidade de mão ruim.
- **Reflexos em Combate (15):** +1 em todas as defesas ativas, +2 Rapidez de Cálculo no combate,
  nunca paralisa de surpresa, +6 em testes de medo/pânico.
- **Esquadrinhar Rápido (Combat Reflexes inclui parte)** — ver acima.
- **Visão Aguçada (2/nível):** +1 Per visual/nível.
- **Audição/Olfato/Tato/Paladar Aguçado (2/nível cada).**
- **Visão no Escuro (Dark Vision) (25):** enxerga sem luz.
- **Visão Noturna (Night Vision) (1/nível):** reduz penalidade de pouca luz.
- **Visão Telescópica/Microscópica.**
- **Fôlego Adicional (Extra Effort/Breath-Holding) etc.**
- **Recuperação Acelerada (Rapid Healing) (5) / Recuperação Muito Acelerada (15).**
- **Resistência a Doenças/Veneno (Resistant) (custo por escopo e grau, +3 ou +8 em TR ou imune).**
- **Imunidade a Doença (10) / a Veneno (15).**
- **Não Respira (20) / Vácuo / Pressão.**
- **Metabolismo Reduzido / Não Precisa Dormir (Doesn't Sleep) (20) / Comer (Doesn't Eat or
  Drink) (10).**
- **Fôlego (Extra Fatigue) → comprar PF.**
- **Membros Extras (Extra Arms) (10/braço).**
- **Garras (Claws) (várias: Garras Afiadas 5, Garras Longas, etc.).**
- **Mordida/Presas (Teeth: Sharp 1, Fangs 2).**
- **Cauda Preênsil / Membro Preênsil.**
- **Flexibilidade (5) / Dupla Articulação.**
- **Elasticidade, Forma Alongada (Stretching).**

### Movimento
- **Salto (Super Jump) (10/nível).**
- **Aderência (Clinging) (20).**
- **Escalar Paredes / Andar na Água (Walk on Liquid).**
- **Voo (Flight) (40; modificadores: Asas −25%, etc.).**
- **Velocidade Aumentada (Enhanced Move) (20/nível — dobra deslocamento por nível).**
- **Deslizar (Slithering), Nadar (Amphibious 10 / Aquático).**
- **Teletransporte (Warp) (100 base).**

### Combate / ofensivas
- **Ataque Inato (Innate Attack):** compra de ataque (queimadura, corrosão, perfurante etc.).
  Custo por ponto de dano conforme tipo (ex.: queimadura 5/dado-base equivalente; ver tabela de
  Innate Attack).
- **Aparar Descalço / Striking ST / Lifting ST.**
- **ST de Golpe (Striking ST) (5/nível):** + dano sem +PV/carga.
- **ST de Carga (Lifting ST) (3/nível):** + carga/erguer sem +dano.
- **Ataque Adicional (Extra Attack) (25/ataque).**
- **Não Sente Dor (High Pain Threshold) (10):** ignora choque, +3 em TR de dor/atordoamento.
- **Rapidez (Altered Time Rate) (100/nível):** manobras extras por turno.
- **Armadura/RD (Damage Resistance) (5/nível de RD; modificadores reduzem).**
- **Refletir/Absorver dano (Reflection, Damage Absorption).**

### Mentais / sociais
- **Talento (Talent) (5, 10 ou 15/nível):** +1/nível num grupo de perícias correlatas
  (ex.: Talento Matemático, Curandeiro, Bardo) e bônus de reação relacionada.
- **Memória Eidética (5) / Memória Fotográfica (10).**
- **Cálculo Rápido / Calculadora Mental (Lightning Calculator 2 / Intuitive Mathematician 5).**
- **Vontade de Ferro (não é vantagem; comprar Vontade).**
- **Destemido (Fearlessness) (2/nível):** + em testes de medo.
- **Sangue-Frio (Unfazeable) (15).**
- **Empatia (15) / Empatia Sensitiva (5) / Detectar Mentiras.**
- **Voz (Voice) (10):** +2 reação e perícias vocais (Atuação, Diplomacia, Canto etc.).
- **Carisma (Charisma) (5/nível):** + reação.
- **Sorte (Luck) (15) / Muita Sorte (30) / Sorte Excepcional (60):** rerrolar 1×/hora (ou /30min,
  /10min) e ficar com o melhor.
- **Bênção/Destino (Blessed) (10).**
- **Sentido de Perigo (Danger Sense) (15).**
- **Intuição (15).**
- **Servos / Aliados / Patrono / Contatos / Reputação positiva** (ver §8).
- **Riqueza, Status, Posto** (ver §8).
- **Independência Financeira / Renda Independente (Independent Income).**
- **Permissão Legal / Imunidade Legal / Imunidade Diplomática.**
- **Status Zero (Social Chameleon, Cultural Adaptability 10).**

### Exóticas / sobrenaturais
- **Aptidão Mágica (Magery) (5 p/ Magery 0 + 10/nível):** pré-requisito para lançar magias
  (sistema padrão). Magery 0 = pode aprender; cada nível dá +1 em todas as magias e melhora
  alcance/percepção mágica.
- **Aptidão Sacerdotal (Power Investiture) (10/nível):** para magia clerical.
- **Aptidão Psiônica (vários poderes psi).**
- **Sentidos Especiais:** Radar/Sonar, Visão de Calor, Detecção (Detect), Visão Penetrante.
- **Regeneração (Regeneration) (custo por velocidade).**
- **Insubstancialidade (Insubstantiality) (80).**
- **Invisibilidade (Invisibility) (40).**
- **Imortalidade (Unkillable) / Não Envelhece (Unaging 15).**
- **Mudar de Forma (Morph, Alternate Form).**
- **Possessão, Telepatia (Mind Control), Telecinese (TK).**
- **Modular Abilities, Imbuir, Afinidade Elemental.**

> A lista completa do Módulo Básico tem ~150 vantagens. Cada uma pode ser alterada por
> **ampliações** e **limitações** (§13). Para casos específicos, consultar o Codex/livros.

---

## 12. Desvantagens

Devolvem pontos. Lista das mais comuns (custo negativo entre parênteses):

### Físicas
- **Cego (−50) / Visão Parcial / Caolho (Olho Furado −15) / Daltônico (−10) / Míope (−25 com
  óculos −10).**
- **Surdo (−20) / Audição Reduzida (−10) / Mudo (−25).**
- **Lento (Reduced Move/Decreased Speed) / Cocho (Lame: Pés Tortos −10, Aleijado −20, Cadeira de
  Rodas −30).**
- **ST/DX/IQ/HT baixos** (comprar atributos abaixo de 10 já devolve pontos).
- **PV/PF reduzidos.**
- **Doença Crônica / Achaque (Fragile, Brittle, Flimsy).**
- **Hemofilia (−30) / Albinismo / Sensibilidade à Luz.**
- **Anão / Gigantismo (Dwarfism −15 / Gigantism 0–...).**
- **Magreza/Sobrepeso/Obesidade/Gordo (Skinny −5, Overweight −1, Fat −3, Very Fat −5).**
- **Idade Avançada / Senilidade.**
- **Mutilado (membros faltando): Um Braço (−20), Sem Braços (−40), Uma Perna (−25), etc.**
- **Dependência (Dependency: precisa de substância/condição p/ sobreviver).**
- **Vulnerabilidade (Vulnerability: dano extra de certo tipo).**
- **Fraqueza (Weakness: perde PV sob certa condição).**
- **Restrição Alimentar / Dieta Restrita.**
- **Não Recupera Fadiga / Sono Pesado / Insônia (−10/−15).**
- **Eletrossensibilidade / Pirofobia física, etc.**

### Mentais (muitas com autocontrole — ver §10)
- **Código de Honra (Code of Honor) (−5 a −15):** regras pessoais inquebráveis.
- **Senso de Dever (Sense of Duty) (−2 a −20):** lealdade obrigatória a grupo/causa.
- **Voto (Vow) (−1 a −15):** promessa solene.
- **Honestidade (Honesty) (−10*):** não pode infringir a lei; NA.
- **Pacifismo (várias gradações: Não Mata −15, Auto-Defesa Apenas, Total −30).**
- **Compaixão (Charitable) / Generosidade (Selfless).**
- **Curiosidade (Curious) (−5*) / Impulsivo (Impulsiveness) (−10*) / Imprudência (Overconfidence)
  (−5*).**
- **Covardia (Cowardice) (−10*) / Medo (Fearfulness) (−2/nível) / Fobias (Phobias) (várias).**
- **Fúria/Sangue-Quente (Bad Temper) (−10*) / Brigão (Bully) (−10*) / Sadismo (−15*).**
- **Ganância (Greed) (−15*) / Cobiça / Avareza (Miserliness) (−10*) / Glutão (Gluttony) (−5*).**
- **Luxúria (Lecherousness) (−15*) / Vício (Addiction: custo por substância).**
- **Honra / Truthfulness (Sinceridade −5*) / Gabolice / Exibido.**
- **Delírios (Delusions) (−5/−10/−15) / Paranoia (−10) / Esquizofrenia (−10).**
- **Megalomania / Fixação (Obsession) / Mania.**
- **Intolerância (Intolerance) / Fanatismo (Fanaticism −15).**
- **Senso de Inferioridade / Baixa Autoestima (Low Self-Image).**
- **Distração/Sonhador (Absent-Mindedness −15) / Lerdeza Mental (Slow Riser).**
- **Tabu Mental / Bloqueio Mental.**
- **Dever (ver Senso de Dever) / Dependentes (Dependents) / Inimigos (Enemies).**
- **Má Reputação (ver §8) / Estigma Social (Social Stigma).**
- **Hábitos Pessoais Detestáveis (−5 a −15) / Falta de Higiene.**
- **Mau Gênio, Teimosia (Stubbornness −5), Distraído, Atrapalhado (Klutz −5 / Total −15).**
- **Azar (Unluckiness −10) / Maldição.**

### Sociais
- **Pobreza/Falido (ver Riqueza §8).**
- **Status Social Negativo.**
- **Estigma Social (Outsider −20, Cidadão de Segunda Classe −5, Mercadoria/Valuable Property,
  Criminoso, Minoria/Selvagem).**
- **Analfabetismo (Illiteracy: Semi-Alfabetizado −5, Analfabeto −10).**
- **Idioma nativo ruim / Mudo.**
- **Identidade Secreta / Identidade Falsa.**
- **Inimigos, Dependentes, Compromisso/Obrigação.**

### Peculiaridades (Quirks) — −1 pt cada (máx. 5)
Traços menores de personalidade/hábito que não afetam mecânica significativamente: "Sempre
educado", "Odeia gatos", "Coleciona moedas antigas", "Fala consigo mesmo", "Nunca usa a cor
vermelha". Dão sabor e devolvem 1 ponto cada.

> O `*` indica desvantagem com **número de autocontrole** — o custo listado é para NA 12
> (frequência "às vezes"); ajuste pelo multiplicador da §10.

---

## 13. Modificadores (ampliações e limitações)

Quase toda vantagem/desvantagem/poder pode ser customizado:
- **Ampliação (Enhancement):** aumenta o custo (+% sobre o valor base). Ex.: *Área de Efeito*
  +50%/+100%, *Penetra Armadura (Cosmic)*, *Alcance Aumentado*.
- **Limitação (Limitation):** reduz o custo (−% sobre o valor base). Ex.: *Acessível somente...*,
  *Inseguro (Unreliable)*, *Cansativo (Costs Fatigue −5%/PF)*, *Preparação Necessária*,
  *Concentração*, *Tato (Touch-only) −20%*.

**Cálculo:** some todos os % do modificador; aplique o total ao custo base; arredonde. O total não
pode reduzir o custo abaixo de 20% do base (limite "−80%") na maioria dos casos.

Exemplos clássicos:
- *Ataque Inato* de queimadura com *Cansativo (Costs 1 FP)* e *Concentração*.
- *Voo* com *Asas (−25%)* (precisa bater asas, vulnerável se imobilizar a asa).
- *RD* com *Limitado (apenas energia) −40%*.

---

## 14. Perícias

Perícias representam conhecimento/treino aprendido. Cada uma tem:
- **Atributo regente** (ST, DX, IQ ou HT) — base do NH.
- **Nível de dificuldade:** Fácil (F), Média (M), Difícil (D), Muito Difícil (MD).
- **Especialização** (algumas exigem, ex.: Armas de Fogo/Pistola).

### Tabela de Custo das Perícias
Pontos investidos → NH relativo ao atributo regente:

| NH desejado | Fácil | Média | Difícil | Muito Difícil |
|-------------|-------|-------|---------|---------------|
| Atributo−3 | — | — | — | 1 |
| Atributo−2 | — | — | 1 | 2 |
| Atributo−1 | — | 1 | 2 | 4 |
| Atributo+0 | 1 | 2 | 4 | 8 |
| Atributo+1 | 2 | 4 | 8 | 12 |
| Atributo+2 | 4 | 8 | 12 | 16 |
| Atributo+3 | 8 | 12 | 16 | 20 |
| Atributo+4 | 12 | 16 | 20 | 24 |
| Atributo+5 | 16 | 20 | 24 | 28 |
| **+1 adicional** | **+4** | **+4** | **+4** | **+4** |

> Regra prática: o **1º ponto** te dá (F: atributo, M: atributo−1, D: atributo−2, MD:
> atributo−3); o **2º ponto** = +1 nível; o **4º ponto** = +2; o **8º** = +3; depois **+4 pts por
> nível**.

### Valores predefinidos (defaults)
Perícias não compradas podem ser tentadas com um **valor predefinido** — geralmente
atributo−penalidade (ex.: muitas perícias usam DX−5 ou IQ−6 sem treino). Uma perícia também pode
ter default baseado em **outra perícia** relacionada. Você pode comprar a perícia "a partir do
default" pagando só a diferença.

### Aumentos gratuitos
Aumentar o **atributo regente** eleva automaticamente o NH de **todas** as perícias que o usam.
Por isso DX/IQ altos são tão valiosos.

### Especialização e familiaridade
- **Especialização obrigatória:** ex.: Cirurgia (cada espécie), Piloto (cada tipo de veículo),
  Armas de Fogo (Pistola, Rifle, etc.). Especializações da mesma perícia têm default entre si
  (penalidade pequena, −2 a −4).
- **Familiaridade:** usar variante desconhecida (modelo de arma, dialeto, terreno) dá −1 a −2 até
  o personagem se familiarizar (algumas horas de uso).

### Aprendizado por estudo/treino
Fora de jogo: ~200h de estudo = 1 ponto (regra opcional de progressão). Em jogo, o Mestre concede
pontos de personagem como recompensa, que o jogador gasta para melhorar perícias.

### Perícias-chave por atributo (amostra representativa)
**Baseadas em DX (físicas):** Acrobacia (D), Arremesso (M), Briga/Boxe/Karatê (F-D), Escudo (F),
Esgrima/Espadas Curtas/Espada Larga/Machado-Maça/Lança/Bastão (M-D, *Maças/Bastão D*), Faca (M
→ *na verdade E*), Furtividade (M), Pilotagem (M-D), Cavalgar (M), Natação (F), Escapismo (D),
Prestidigitação (D), Arco (M), Bestas (E), Armas de Fogo/Pistola (E).

**Baseadas em IQ (mentais/sociais):** Conhecimentos Gerais (várias *Áreas*), Diplomacia (D),
Liderança (M), Intimidação (M), Atuação (M), Fast-Talk/Lábia (M), Comércio/Mercador (M),
Furto/Punga (D), Arrombamento (M), Disfarce (M), Cartografia, Navegação (M-D), Estratégia/Tática
(D/H), Primeiros Socorros (E), Diagnose (D), Eletrônica/Mecânica (M-D, especializadas),
Ocultismo (M), Alquimia (MD), Teologia (D), História (D), Direito (D), Idiomas, Pesquisa (M),
Escrita (M), Naturalismo/Biologia/Química/Física (MD as ciências), Computadores/Operação (E).

**Baseadas em IQ — Magia:** todas as magias são perícias **Muito Difíceis** baseadas em IQ (ver
§16).

**Baseadas em HT:** Resistência/Hiking (Caminhada M), Corrida (H), Mergulho, Sexo, Cantar (E),
Bebida (E), Tolerância a venenos.

**Baseadas em ST:** Levantamento de Peso (E), Escalada (usa DX ou ST), poucas.

**Baseadas em Per/Vontade:** Observação (Per/E ish), Rastreamento, Meditação (Vontade/H),
Exorcismo (Vontade), Autodomínio.

> O catálogo do app contém a lista completa traduzida; este resumo dá a estrutura.

---

## 15. Técnicas

Técnicas são manobras/aplicações especializadas de uma perícia, com um **valor predefinido
negativo** que você "compra de volta".

- Toda técnica tem um **default** = (perícia regente − penalidade). Ex.: *Decapitação* tem default
  Espada Larga−5.
- Comprar a técnica reduz/elimina essa penalidade, **sem** subir a perícia inteira.
- Técnicas são **Médias** ou **Difíceis**:

| Tipo | Custo |
|------|-------|
| **Média** | 1 ponto = +1 acima do default; depois **1 ponto por nível** adicional. |
| **Difícil** | 2 pontos = +1 acima do default; depois **1 ponto por nível** adicional. |

- Há um **teto**: a técnica não pode superar o NH da perícia regente (ou da perícia + um valor
  máximo definido na descrição da técnica).
- Exemplos: *Ataque na Cabeça*, *Desarmar*, *Finta Enganadora*, *Golpe Duplo*, *Off-Hand Weapon
  Training* (treino com a mão ruim), *Cavalgada e Disparo*, *Retención* etc.

---

## 16. Magia (sistema padrão de feitiços)

O Módulo Básico usa o **sistema de magia como perícias** (cada magia é uma perícia individual).

### Pré-requisitos
- **Aptidão Mágica (Magery)** geralmente exigida. **Magery 0 (5 pts)** = pode aprender magias;
  cada nível de **Magery (+10 pts/nível)** dá **+1 em todas as magias** e melhora a percepção
  mágica e o alcance.
- Muitas magias têm **pré-requisitos** (outras magias e/ou Magery mínimo). Formam-se "árvores"
  por **Colégio (College)**.

### Magias são perícias IQ/Muito Difíceis
- NH da magia = comprado pela tabela de perícias **MD** (§14), somado ao nível de **Magery**.
- Ex.: Magery 2, IQ 13, 1 ponto em "Bola de Fogo" (MD) → NH = (IQ−3) + Magery = 10 + 2 = **12**.

### Lançamento
1. **Concentração:** a maioria exige 1 segundo (1 turno) de concentração; magias maiores, mais
   tempo. Concentrar = manobra Concentrar-se; sofrer dano exige TR de Vontade para não perder.
2. **Teste de NH** da magia ao fim da concentração. Sucesso = funciona; falha = falha (e gasta
   parte da energia); falha crítica = consulta tabela de **Falhas Críticas de Magia** (efeitos
   caóticos).
3. **Custo de energia:** cada magia lista um custo em **PF** (ou PV se faltar PF) para lançar e
   um custo de **manutenção** (por minuto, se contínua). NH alto reduz custo:
   - **NH 15+:** custo de energia **−1**.
   - **NH 20+:** custo de energia **−2** (e assim por diante, −1 a cada +5 de NH).
   - O custo mínimo é geralmente 1 (algumas chegam a 0 com NH altíssimo).
4. **Tempo de lançamento / duração / alcance** — listados em cada magia.

### Classes de magia
- **Regular:** mira um alvo à vista; pode ser resistida (TR).
- **Área:** afeta um raio (custo escala com o raio em metros).
- **Missível (Missile):** acumula energia em turnos de concentração e é "arremessada" (usa
  Arremesso Inato / Bola de Fogo etc.); rola para acertar.
- **Bloqueio (Blocking):** defensiva, lançada como defesa ativa (1 turno).
- **Melee:** entregue por toque/ataque corpo-a-corpo.
- **Informativa, Especial, Encantamento (Enchantment — itens mágicos permanentes).**

### Colégios (Colleges) — agrupamentos temáticos
Ar, Água, Fogo, Terra, Animal, Corpo, Comunicação & Empatia, Necromancia (Morte),
Encantamento, Comida, Cura (Healing), Conhecimento, Luz & Trevas, Criação (Making & Breaking),
Mente (Mind Control), Movimento, Plantas, Proteção & Aviso, Som, Portais (Gate), Meta-Magia,
Tecnomancia, Clima, etc.

> **Nota do projeto:** o Rodolfo marcou que **as magias de Metamorfose têm pré-requisitos
> complicados e precisam de correção** (memória feita em 02/06/2026). Atenção ao tratar essa
> árvore.

### Sistemas alternativos
Existem (em suplementos) outros sistemas: **Magia Ritual (Path/Book, Ritual Path Magic)**,
**Sintônica/Syntactic**, **baseada em poderes (Powers)**. O padrão do app é o sistema de
feitiços-perícia acima.

---

## 17. Riqueza e equipamento

- **Riqueza inicial ($):** definida pelo nível de Riqueza (§8) × média do NT da campanha.
- **Custo de vida:** cada mês o personagem paga uma manutenção conforme seu Status. Não pagar
  baixa o Status / gera problemas.
- **Equipamento:** tem **custo ($)**, **peso (kg/lb)**, **NT mínimo** e, para armas/armaduras,
  estatísticas de combate.

### Armas corpo-a-corpo (estatísticas típicas)
Cada arma lista: **dano** (ex.: GeB+2 corte), **alcance (C/Reach)**, **aparar** (modificador para
Aparar), **custo**, **peso**, **ST mínima** (penalidade se ST insuficiente), perícia usada.

### Armas à distância
Listam: **dano**, **precisão (Acc)**, **alcance (1/2D e Máx)**, **cadência de tiro (RoF)**,
**tiros (Shots)**, **ST mínima**, **bulk**, **recuo (Rcl)**.

### Armaduras
Listam **RD (Redução de Dano)** por localização, **custo**, **peso**, **NT**, e penalidade de DX
se mal ajustada. RD subtrai-se do dano antes do multiplicador de tipo.

### Escudos
Dão **bônus de Defesa (DB)** que soma a Aparar/Bloquear/Esquiva quando o atacante vem pela frente,
e podem ser usados ativamente para **Bloquear** (perícia Escudo).

---

## 18. Carga e deslocamento

A capacidade de carga depende de **ST**, via **Base de Carga (BC)**.

**Base de Carga (BC)** = **(ST × ST) / 10 quilos** (peso máximo erguido sobre a cabeça com uma mão
em 1 segundo). Humano médio ST 10 → BC = 10 kg. É o valor que define os níveis de carga:

| Nível de carga | Peso máximo | Mult. Deslocamento | Penalidade Esquiva |
|----------------|-------------|--------------------|--------------------|
| **Nenhuma (0)** | até BC | ×1 (Desloc. total) | 0 |
| **Carga Leve (1)** | até 2×BC | ×0,8 | −1 |
| **Carga Média (2)** | até 3×BC | ×0,6 | −2 |
| **Carga Pesada (3)** | até 6×BC | ×0,4 | −3 |
| **Carga Muito Pesada (4)** | até 10×BC | ×0,2 | −4 |

- O **número do nível (0–4)** é o que se soma/subtrai quando uma regra manda usar o "nível de
  carga" (ex.: penaliza Escalada, Furtividade e Natação).
- **Deslocamento com carga** = Deslocamento Básico × multiplicador (ignore frações, mín. 1).
- **Esquiva com carga** = Esquiva − penalidade de carga.
- A carga também penaliza muitas perícias físicas (Furtividade, Acrobacia etc.).

**Movimento em combate** (1 turno ≈ 1 segundo):
- **Andar:** até o Deslocamento e atacar (manobra Mover e Atacar tem penalidades).
- **Passo (Step):** 1 metro (ou Deslocamento/10, mín. 1) que acompanha quase toda manobra.
- **Correr (manobra Mover):** todo o deslocamento; correr vários turnos pode usar a perícia
  Corrida e gastar PF.

---

## 19. Combate

### Escala e sequência
- **Turno = 1 segundo.** Cada personagem age na ordem de **Velocidade Básica** (maior primeiro;
  empate desempata por maior DX, depois disputa).
- Em cada turno o personagem escolhe **uma manobra**.

### Manobras (a escolha do turno)
| Manobra | O que faz | Defesa permitida |
|---------|-----------|------------------|
| **Mover (Move)** | Desloca-se até o Deslocamento; nada de ataque. | Qualquer |
| **Atacar (Attack)** | Um ataque corpo-a-corpo ou à distância + Passo. | Qualquer |
| **Mover e Atacar (Move and Attack)** | Move e ataca com penalidade (corpo-a-corpo −4; à distância limita a NH 9). | Só Esquiva (sem bônus) |
| **Ataque Total (All-Out Attack)** | +bônus ofensivo (variantes: Determinado +4 NH, Forte +2 dano/+1 dado, Duplo 2 ataques, Telegrafado). **Sem defesa ativa neste turno.** | Nenhuma |
| **Defesa Total (All-Out Defense)** | +2 numa defesa (Determinada) OU duas defesas diferentes (Dupla) OU +Deslocamento recuando. | Defesa reforçada |
| **Aguardar (Wait)** | Prepara para reagir a um gatilho (ataque de oportunidade). | Qualquer |
| **Apontar (Aim)** | Mira arma à distância → ganha o bônus de Precisão (Acc) no próximo Atacar. | Qualquer (mas perde a mira se atacado e falhar Vontade) |
| **Avaliar (Evaluate)** | +1 (até +3) no próximo ataque corpo-a-corpo contra aquele alvo. | Qualquer |
| **Concentrar-se (Concentrate)** | 1+ turno focado (magia, perícia mental, poder). | Qualquer (mas pode perder o foco) |
| **Preparar (Ready)** | Saca/recarrega/prepara arma, levanta objeto, muda de empunhadura. | Qualquer |
| **Mudar de Postura (Change Posture)** | Em pé ↔ ajoelhado ↔ sentado ↔ deitado ↔ rastejando. | Qualquer |
| **Não Fazer Nada (Do Nothing)** | Atordoado/inconsciente; ou recupera de atordoamento. | Penalizada se atordoado (−4) |

### Modificadores de postura
| Postura | Ataque CaC | Ataque à dist. | Defesa | Ser atacado CaC / à dist. |
|---------|-----------|----------------|--------|---------------------------|
| Em pé | 0 | 0 | normal | 0 / 0 |
| Agachado/Ajoelhado | −2 | −2 | −2 | 0 / −2 |
| Deitado | −4 | −2 | −3 | −4 (CaC inimigo +4) / −2 |

### Defesas Ativas
Ao ser atacado (e o atacante tendo sucesso na rolagem de acerto), o defensor pode tentar **uma**
defesa ativa (role 3d ≤ valor da defesa):

| Defesa | Valor | Contra |
|--------|-------|--------|
| **Esquiva (Dodge)** | Velocidade Básica + 3 (−carga) | Quase tudo (inclusive à distância) |
| **Aparar (Parry)** | (NH da arma/desarmado ÷ 2) + 3, arred. p/ baixo | Ataques corpo-a-corpo |
| **Bloquear (Block)** | (NH de Escudo ÷ 2) + 3 | Corpo-a-corpo e alguns à distância |

- **Reflexos em Combate** dá **+1** em todas as defesas. **Escudo** soma seu **DB** às três.
- **Recuar (Retreat):** ceder 1 passo para trás na primeira defesa contra um inimigo dá **+3 à
  Esquiva** ou **+1 a Aparar/Bloquear**.
- **Aparar repetido** com a mesma arma no mesmo turno acumula **−4** por aparada extra (−2 p/
  esgrima/armas balanceadas).
- **Aparar arma pesada/desarmado vs. arma:** desarmar/aparar ataque armado descalço é arriscado.
- **Finta (Feint):** Disputa Rápida da sua perícia de ataque vs. a melhor defesa/DX do alvo; sua
  MdS vira penalidade na defesa dele no próximo turno.
- **Defesa contra à distância:** só Esquiva (ou Bloquear); Aparar não vale (salvo aparar mágica/
  habilidade especial). Não dá para esquivar do que não se vê.

### Sequência de um ataque corpo-a-corpo
1. Atacante escolhe manobra de ataque, declara alvo (e local, se mirar parte específica).
2. **Rolagem de acerto:** 3d ≤ NH efetivo (perícia ± postura, local, escuridão, etc.).
3. Se acertou e não houve falha crítica, o defensor tenta **defesa ativa**.
4. Se a defesa falhar (ou não houve), o ataque conecta → **rola dano**.
5. Aplica-se **RD** da armadura; o que penetra × **modificador de tipo** = **PV de ferimento**.
6. Aplicam-se efeitos: **choque**, possível **atordoamento**, perda de membro, knockdown etc.

### Localização de acerto (ataque mirado)
Mirar uma parte específica dá penalidade, mas pode causar efeitos extras:

| Local | Penalidade | Efeito notável |
|-------|-----------|----------------|
| Crânio | −7 | Dano ×4 (após RD), risco de atordoar/knockout |
| Olho | −9 | Como crânio; perfurante/imp entra fácil |
| Rosto | −5 | Knockdown em contusão; perfurante pode atingir olho |
| Pescoço | −5 | Corte ×2, contusão ×1,5 (estrangular/decapitar) |
| Tronco/Torso | 0 | Padrão |
| Vitais (órgãos) | −3 | Perfurante/impalante ×3 |
| Virilha | −3 | Como vitais p/ humanos; +atordoamento |
| Braço/Perna | −2 | Limite de dano = PV/2 (excedente "vaza"); pode aleijar |
| Mão/Pé | −4 | Limite de dano = PV/3; pode aleijar |

### Ataques à distância — modificadores principais
- **Precisão (Acc):** bônus ganho ao **Apontar** (1 turno).
- **Alcance:** penalidade pela distância (tabela Velocidade/Distância, §7); além de **1/2D** o
  dano cai pela metade; além do **Máx** não alcança.
- **Cadência de Tiro (RoF):** vários tiros dão bônus de acerto (+1 por faixa de tiros: 4–8 = +1, 9–12 = +2 etc.).
- **Recuo (Rcl):** quantos pontos de MdS são necessários por tiro extra que acerta numa rajada.
- **Cobertura, alvo em movimento, pouca luz** etc. modificam.

---

## 20. Dano, ferimentos e recuperação

### Efeitos do ferimento
- **Choque (Shock):** ao levar dano num turno, sofre penalidade de **−1 por PV perdido** (máx
  −4) em DX/IQ no **próximo** turno. *Não Sente Dor (High Pain Threshold)* ignora.
- **Knockdown e Atordoamento:** dano grande (≥ PV/2 num golpe), acerto em crânio/rosto/virilha, ou
  dano que reduz a 0 PV → teste de **HT** (ou Vontade). Falha = cai e fica **atordoado** (−4 nas
  defesas, só "Não Fazer Nada" até se recuperar com novo teste de HT a cada turno).
- **Knockback (recuo físico):** contusão forte empurra o alvo.

### Marcos de PV (humano padrão)
| PV atual | Estado |
|----------|--------|
| > 1/3 do PV máx | Normal |
| ≤ 1/3 do PV máx | **Cambaleante:** Deslocamento e Esquiva pela metade (a cada turno de esforço, teste de HT p/ não cair). |
| 0 ou menos | A cada turno faz teste de **HT** para continuar consciente/agindo; falha = desmaia. |
| −1×PV máx | Teste de **HT** ou morre. Repete a cada múltiplo: −2×PV, −3×PV, −4×PV (cada um exige novo teste). |
| −5×PV | **Morte automática.** |
| −10×PV | Corpo totalmente destruído. |

> Ex.: humano de PV 10 → cambaleante a 3 PV; risco de inconsciência a 0; testes de morte a −10,
> −20, −30, −40; morte automática a −50.

### Fadiga (PF)
- Esforço extra, correr, segurar a respiração, lançar magia, calor → perde PF.
- **≤ 1/3 do PF:** "Cansado" — ST e DX caem pela metade (até recuperar acima de 1/3).
- **0 PF:** quase desmaiando; qualquer esforço extra tira PV.
- **−1×PF:** desmaia.

### Recuperação
- **PF:** recupera ~1 PF a cada 10 minutos de descanso (sentado/parado). Sono recupera mais.
- **PV (cura natural):** ao fim de cada dia de descanso, teste de **HT**: sucesso recupera **1 PV**
  (ou mais com *Recuperação Acelerada*). Cuidados médicos (perícia **Primeiros Socorros**,
  **Diagnose**, **Cirurgia**) ajudam a estancar, estabilizar e dar bônus.
- **Membros aleijados:** se o dano que aleijou não excedeu o limite letal, o membro pode se
  recuperar em semanas; senão, é permanente (mutilação).
- **Magia/Tecnologia** (Curar, regeneração, autodoc NT alto) acelera tudo.

### Condições e perigos diversos (resumo)
Sufocamento/afogamento (TR de HT por turno sem ar), veneno (TR HT, dano cíclico), doença,
fome/sede/frio/calor (perda de PF/PV ao longo do tempo), queda (dano por altura), fogo, ácido,
eletricidade, radiação, explosão (dano por raio), atordoamento por luz/som — todos com regras
específicas no capítulo de Aventuras/Campanhas.

---

## 21. Tabelas de referência rápida

### Custos resumidos (criação)
| Item | Custo |
|------|-------|
| ST | 10/nível | 
| DX | 20/nível |
| IQ | 20/nível |
| HT | 10/nível |
| PV | 2/ponto |
| PF | 3/ponto |
| Vontade | 5/nível |
| Percepção | 5/nível |
| Velocidade Básica | 5 por 0,25 |
| Deslocamento Básico | 5/nível |
| Peculiaridade | −1 (cada, máx 5) |

### Fórmulas essenciais
```
PV               = ST            (modificável ±2/PV)
PF               = HT            (modificável ±3/PF)
Vontade          = IQ           (modificável ±5/nível)
Percepção        = IQ           (modificável ±5/nível)
Velocidade Básica = (HT + DX) / 4
Deslocamento Básico = trunc(Velocidade Básica)
Esquiva          = trunc(Velocidade Básica) + 3   (− carga)
Aparar           = trunc(NH_arma / 2) + 3
Bloquear         = trunc(NH_escudo / 2) + 3
Base de Carga    = (ST × ST) / 10  (kg)
Dano             = GdP/GeB pela ST + modificador da arma
Ferimento        = (dano − RD) × multiplicador de tipo
```

### Defesas ativas — fórmula rápida
- Esquiva = Vel.Básica + 3 (−carga, +1 se Reflexos em Combate, +DB escudo).
- Aparar = NH/2 + 3 (arred. baixo).
- Bloquear = Escudo/2 + 3 (arred. baixo).
- Recuar: +3 Esquiva / +1 Aparar-Bloquear (1×/inimigo/turno).

### Críticos (memorizar)
- Acerto crítico: **3–4 sempre**; **5** se NH≥15; **6** se NH≥16.
- Falha crítica: **18 sempre**; **17** se NH≤15; **falhar por 10+** sempre.

---

## 22. Glossário de abreviações

| PT-BR | Sigla | EN original |
|-------|-------|-------------|
| Força | ST | Strength |
| Destreza | DX | Dexterity |
| Inteligência | IQ | Intelligence |
| Vitalidade | HT | Health |
| Pontos de Vida | PV | HP (Hit Points) |
| Pontos de Fadiga | PF | FP (Fatigue Points) |
| Percepção | Per | Per |
| Nível de Habilidade | NH | SL (Skill Level) |
| Margem de Sucesso | MdS | MoS (Margin of Success) |
| Margem de Falha | MdF | MoF (Margin of Failure) |
| Nível Tecnológico | NT | TL (Tech Level) |
| Modificador de Tamanho | MT | SM (Size Modifier) |
| Redução de Dano | RD | DR (Damage Resistance) |
| Golpe de Ponta | GdP | thr (thrust) |
| Golpe em Balanço | GeB | sw (swing) |
| Base de Carga | BC | BL (Basic Lift) |
| Número de Autocontrole | NA | CR (Self-Control Rating) |
| Bônus de Defesa (escudo) | DB | DB (Defense Bonus) |
| Aptidão Mágica | — | Magery |
| Pontos de Personagem | — | CP (Character Points) |
| Teste de Resistência | TR | Resistance roll |

**Tipos de dano:** cont (contusão/cr), cort (corte/cut), perf (perfurante/pi), perf-/perf+/perf++
(pi-/pi+/pi++), imp (impalante/imp), qmd (queimadura/burn), cor (corrosão/cor), tox (toxina/tox).

**Dificuldade de perícia:** F (Fácil/E), M (Média/A), D (Difícil/H), MD (Muito Difícil/VH).

---

### Notas de fidelidade
- Todos os valores aqui são do **Módulo Básico 4e** (tradução Devir). Suplementos (*Martial Arts*,
  *Powers*, *Magic*, *High-Tech*, *Ultra-Tech*, *Thaumatology* etc.) expandem/alteram regras
  específicas — quando o catálogo do app trouxer conteúdo desses livros, este compêndio é o ponto
  de partida, não a palavra final.
- Unidades: o Módulo Básico brasileiro usa **metros e quilos**; o original usa jardas/libras (1
  jarda ≈ 1 m, 1 lb ≈ 0,5 kg). As fórmulas funcionam igual em ambas as escalas.
- Em caso de conflito entre este resumo e o texto-fonte `livros de gurps/modulo_basico_text.txt`,
  **o texto-fonte prevalece** — este arquivo é um índice de alta densidade, não um substituto do
  livro.
