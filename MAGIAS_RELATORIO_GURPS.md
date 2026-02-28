# Relatorio de Magias (Formato GURPS)

## Visao Geral dos Arquivos

- Arquivo 1: `app/src/main/assets/magias.json` -> **831** magias
- Arquivo 2: `app/src/main/assets/magias2versao.json` -> **831** magias
- IDs em comum: **831**
- So no `magias.json`: **0**
- So no `magias2versao.json`: **0**

## Campos Existentes

### `magias.json` (legado)
- atributoBase
- atributoEscolhaObrigatoria
- atributosPossiveis
- dificuldadeFixa
- dificuldadeVariavel
- exigeEspecializacao
- id
- nome
- pagina
- preDefinicoes
- texto

### `magias2versao.json` (normalizado)
- classe
- dificuldade
- duracao
- energia
- escola
- id
- nome
- pagina
- preRequisitos
- tempoOperacao

## Lista Completa de Magias (Ficha GURPS)

### Abençoar Plantas
- ID: abencoar_plantas
- Dificuldade: D
- Pagina: 161
- Classe: Área
- Escola(s): Plantas
- Duracao: 1 plant./ estação
- Energia: 1
- Tempo de Operacao: 5 min.
- Pre-requisitos: Curar Planta

### Acalmar Animal
- ID: acalmar_animal
- Dificuldade: D
- Pagina: 29
- Classe: Comum
- Escola(s): Animais
- Duracao: Perm.#
- Energia: 1 a 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Persuasão ou a vantagem Empatia com Animais

### Acelerar
- ID: acelerar
- Dificuldade: MD
- Pagina: 146
- Classe: Comum
- Escola(s): Deslocamento
- Duracao: 10 seg.
- Energia: 5#
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM1, Apressar, IQ 12+

### Acelerar Tempo
- ID: acelerar_tempo
- Dificuldade: MD
- Pagina: 86
- Classe: Área/R-Espacial
- Escola(s): Portal
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 2 seg.
- Pre-requisitos: AM2, IQ 13+, 2 mágicas de 10 escolas

### Acionar/NT
- ID: acionarnt
- Dificuldade: D
- Pagina: 180
- Classe: Comum
- Escola(s): Tecnológica
- Duracao: 10 min.
- Energia: Varia
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Criar Combustível, Objeto Dançante

### Adaga de Gelo
- ID: adaga_de_gelo
- Dificuldade: D
- Pagina: 188
- Classe: Projetil
- Escola(s): Água
- Duracao: Instant.
- Energia: 1 a AM#
- Tempo de Operacao: 1 a 3 seg.
- Pre-requisitos: Esfera de Gelo ou Jato de Água

### Adiar Mágica
- ID: adiar_magica
- Dificuldade: MD
- Pagina: 128
- Classe: Especial
- Escola(s): Metamágica
- Duracao: 1 hora
- Energia: Varia
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Retardo

### Adivinhação
- ID: adivinhacao
- Dificuldade: D
- Pagina: 108
- Classe: Informação
- Escola(s): Reconhecimento
- Duracao: Instant.
- Energia: 10
- Tempo de Operacao: 1 hora#
- Pre-requisitos: História, outras mágicas#

### Aerovisão
- ID: aerovisao
- Dificuldade: D
- Pagina: 24
- Classe: Comum
- Escola(s): Ar, Reconhecimento
- Duracao: 1 min.
- Energia: 1 por 1,5 km/M
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Moldar Ar

### Afetar Espíritos
- ID: afetar_espiritos
- Dificuldade: D
- Pagina: 151
- Classe: Comum
- Escola(s): Necromancia
- Duracao: 1 min.
- Energia: 04/02
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Solidificar

### Afiar
- ID: afiar
- Dificuldade: D
- Pagina: 118
- Classe: Comum
- Escola(s): Quebrar e Consertar
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 4 seg.
- Pre-requisitos: Consertar

### Agitar Animal
- ID: agitar_animal
- Dificuldade: D
- Pagina: 30
- Classe: Comum
- Escola(s): Animais
- Duracao: 1 hora#
- Energia: 1 a 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Vexação ou Empatia em Animais

### Agonizar
- ID: agonizar
- Dificuldade: D
- Pagina: 40
- Classe: Comum/R-HT
- Escola(s): Corpo
- Duracao: 1 min.
- Energia: 08/06
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, Sensibilizar

### Alarme
- ID: alarme
- Dificuldade: D
- Pagina: 100
- Classe: Comum
- Escola(s): Reconhecimento
- Duracao: 1 semana
- Energia: 1
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Hora Certa

### Alarme Florestal
- ID: alarme_florestal
- Dificuldade: D
- Pagina: 162
- Classe: Área
- Escola(s): Plantas
- Duracao: 10 horas
- Energia: 2#/I
- Tempo de Operacao: 1 seg.
- Pre-requisitos: 4 mágicas sobre Plantas ou Percepção do Perigo

### Alimento Essencial
- ID: alimento_essencial
- Dificuldade: MD
- Pagina: 79
- Classe: Comum
- Escola(s): Alimentos
- Duracao: Perm.
- Energia: 3/refeição#
- Tempo de Operacao: 30 seg.
- Pre-requisitos: 6 mágicas de Alimento incl. Criar Alimento

### Aliviar Enjoo
- ID: aliviar_enjoo
- Dificuldade: D
- Pagina: 90
- Classe: Comum/R-Mágica
- Escola(s): Cura
- Duracao: 10 min.
- Energia: 2
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Conceder Vitalidade

### Aliviar Loucura
- ID: aliviar_loucura
- Dificuldade: D
- Pagina: 92
- Classe: Comum/R-Mágica
- Escola(s): Cura, Mente
- Duracao: 10 min.
- Energia: 2
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Conceder Vitalidade, Sabedoria

### Aliviar Paralisia
- ID: aliviar_paralisia
- Dificuldade: D
- Pagina: 93
- Classe: Comum
- Escola(s): Cura
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Cessar Paralisia

### Aliviar Vício
- ID: aliviar_vicio
- Dificuldade: D
- Pagina: 92
- Classe: Comum
- Escola(s): Cura
- Duracao: 1 dia
- Energia: 6
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Neutralizar Veneno

### Alongar Membro
- ID: alongar_membro
- Dificuldade: D
- Pagina: 41
- Classe: Comum
- Escola(s): Corpo
- Duracao: 1 min.
- Energia: 02/02
- Tempo de Operacao: 5 seg.
- Pre-requisitos: AM3, Metamorfose

### Alongar Objeto
- ID: alongar_objeto
- Dificuldade: MD
- Pagina: 120
- Classe: Comum
- Escola(s): Quebrar e Consertar
- Duracao: 1 hora
- Energia: Varia
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM3, Transformar Objeto

### Alterar Corpo
- ID: alterar_corpo
- Dificuldade: D
- Pagina: 41
- Classe: Comum/R-HT
- Escola(s): Corpo
- Duracao: 1 hora
- Energia: 08/06
- Tempo de Operacao: 2 min.
- Pre-requisitos: Alterar Feições

### Alterar Feições
- ID: alterar_feicoes
- Dificuldade: D
- Pagina: 41
- Classe: Comum/R-HT
- Escola(s): Corpo
- Duracao: 1 hora
- Energia: 04/03
- Tempo de Operacao: 1 min.
- Pre-requisitos: Metamorfose ou Ilusão Perfeita, e 8 mágicas de Corpo

### Alterar Terreno
- ID: alterar_terreno
- Dificuldade: MD
- Pagina: 55
- Classe: Área
- Escola(s): Terra
- Duracao: 2d dias
- Energia: 1#
- Tempo de Operacao: 10 seg.
- Pre-requisitos: AM3, todas as 4 mágicas de Moldar Elemental

### Alterar Voz
- ID: alterar_voz
- Dificuldade: D
- Pagina: 41
- Classe: Comum/R-HT
- Escola(s): Corpo, Som
- Duracao: 1 hora
- Energia: 02/02
- Tempo de Operacao: 1 min.
- Pre-requisitos: 4 mágicas de Corpo, 4 mágicas de Som

### Alucinação
- ID: alucinacao
- Dificuldade: D
- Pagina: 140
- Classe: Comum/R-Vont
- Escola(s): Mente
- Duracao: 1 min.
- Energia: 04/02
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Loucura, Sugestão

### Alucinação Superior
- ID: alucinacao_superior
- Dificuldade: MD
- Pagina: 141
- Classe: Comum/R-Vont
- Escola(s): Mente
- Duracao: 1 min.
- Energia: 06/03
- Tempo de Operacao: 4 seg.
- Pre-requisitos: AM2, Alucinação

### Ambidestria
- ID: ambidestria
- Dificuldade: D
- Pagina: 39
- Classe: Comum
- Escola(s): Corpo
- Duracao: 1 min.
- Energia: 03/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Graça

### Amuleto
- ID: amuleto
- Dificuldade: D
- Pagina: 58
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 50/pt. RàM
- Tempo de Operacao: —
- Pre-requisitos: Talismã para a mágica apropriada

### Analisar Mágica
- ID: analisar_magica
- Dificuldade: D
- Pagina: 102
- Classe: Informação/R-Mágica
- Escola(s): Reconhecimento
- Duracao: Instant.
- Energia: 5
- Tempo de Operacao: 1 hora
- Pre-requisitos: Identificar Mágica

### Andar em Círculos
- ID: andar_em_circulos
- Dificuldade: D
- Pagina: 36
- Classe: Comum/R-HT
- Escola(s): Corpo
- Duracao: Instant.
- Energia: 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Trança-Pés

### Animar Máquina/NT
- ID: animar_maquinant
- Dificuldade: D
- Pagina: 177
- Classe: Comum/R-Vont
- Escola(s): Tecnológica
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: Varia
- Pre-requisitos: Controle de Máquina, Animação ou Animar Objeto

### Animar Objeto
- ID: animar_objeto
- Dificuldade: MD
- Pagina: 117
- Classe: Comum/R-Espec.
- Escola(s): Quebrar e Consertar
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM2, 3 mágicas Moldar

### Animar Planta
- ID: animar_planta
- Dificuldade: D
- Pagina: 164
- Classe: Comum
- Escola(s): Plantas
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 5 seg.
- Pre-requisitos: 7 mágicas de Planta

### Animar Sombra
- ID: animar_sombra
- Dificuldade: D
- Pagina: 154
- Classe: Comum/R-HT
- Escola(s): Necromancia
- Duracao: 10 seg.
- Energia: 04/04
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Espírito de Caveira, Moldar Trevas

### Animação
- ID: animacao
- Dificuldade: MD
- Pagina: 150
- Classe: Comum
- Escola(s): Necromancia
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Convocar Espírito

### Animação Suspensa
- ID: animacao_suspensa
- Dificuldade: D
- Pagina: 94
- Classe: Comum/R-HT
- Escola(s): Cura
- Duracao: Indef.#
- Energia: 6
- Tempo de Operacao: 30 seg.
- Pre-requisitos: Sono, 4 mágicas de Cura

### Anular Criação
- ID: anular_criacao
- Dificuldade: D
- Pagina: 99
- Classe: Comum/R-Mágica
- Escola(s): Ilusão
- Duracao: Instant.
- Energia: 1 a 3#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Controle de Criação

### Anular Ilusão
- ID: anular_ilusao
- Dificuldade: D
- Pagina: 97
- Classe: Comum/R-Mágica
- Escola(s): Ilusão
- Duracao: Instant.
- Energia: 1
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Controle de Ilusão

### Anular Mágica
- ID: anular_magica
- Dificuldade: D
- Pagina: 126
- Classe: Área/R-Mágica
- Escola(s): Metamágica
- Duracao: Perm.
- Energia: 3
- Tempo de Operacao: seg.=custo
- Pre-requisitos: Contramágica e 12 outras mágicas

### Anular Possessão
- ID: anular_possessao
- Dificuldade: D
- Pagina: 49
- Classe: Comum/R-Mágica
- Escola(s): Comunicação
- Duracao: Instant.
- Energia: 10
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Passageiro da Alma ou Possessão#

### Apanhar Mágica
- ID: apanhar_magica
- Dificuldade: MD
- Pagina: 123
- Classe: Bloqueio
- Escola(s): Metamágica
- Duracao: Instant.
- Energia: 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, DX 12+, Devolver Projetil

### Apanhar Projetil
- ID: apanhar_projetil
- Dificuldade: D
- Pagina: 168
- Classe: Bloqueio
- Escola(s): Proteção
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Desviar Projetil

### Aporte
- ID: aporte
- Dificuldade: D
- Pagina: 142
- Classe: Comum/R-Vont
- Escola(s): Deslocamento
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1

### Apressar
- ID: apressar
- Dificuldade: D
- Pagina: 142
- Classe: Comum
- Escola(s): Deslocamento
- Duracao: 1 min.
- Energia: 2/pt/M
- Tempo de Operacao: 2 seg.
- Pre-requisitos: —

### Apressar Fogo
- ID: apressar_fogo
- Dificuldade: D
- Pagina: 73
- Classe: Comum
- Escola(s): Fogo
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Retardar Fogo

### Aprisionar Alma
- ID: aprisionar_alma
- Dificuldade: MD
- Pagina: 154
- Classe: Comum
- Escola(s): Necromancia
- Duracao: Perm.
- Energia: 8
- Tempo de Operacao: 1 min.
- Pre-requisitos: AM1, 6 mágicas de Necromancia incl. Roubar Vitalidade

### Aprisionar Espírito
- ID: aprisionar_espirito
- Dificuldade: D
- Pagina: 157
- Classe: Especial
- Escola(s): Necromancia
- Duracao: 5 min.
- Energia: Varia
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, Aprisionar Alma, Espantar Espírito

### Aquavisão
- ID: aquavisao
- Dificuldade: D
- Pagina: 187
- Classe: Informação
- Escola(s): Água, Reconhecimento
- Duracao: 30 seg.
- Energia: 1/1#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Moldar Água

### Aquece
- ID: aquece
- Dificuldade: D
- Pagina: 195
- Classe: Área
- Escola(s): Clima, Ar
- Duracao: 1 hora
- Energia: 1/10/I
- Tempo de Operacao: 1 min.#
- Pre-requisitos: Calor, 4 mágicas do Ar

### Ar Essencial
- ID: ar_essencial
- Dificuldade: D
- Pagina: 26
- Classe: Área
- Escola(s): Ar
- Duracao: Perm.
- Energia: 2
- Tempo de Operacao: 3 seg.
- Pre-requisitos: 6 mágicas do Ar

### Arma Congelante
- ID: arma_congelante
- Dificuldade: D
- Pagina: 185
- Classe: Comum
- Escola(s): Água
- Duracao: 1 min.
- Energia: 03/01
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Criar Água

### Arma Dançante
- ID: arma_dancante
- Dificuldade: D
- Pagina: 63
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 1000/0,5 kg#
- Tempo de Operacao: —
- Pre-requisitos: Encantar, Objeto Dançante

### Arma Espiritual
- ID: arma_espiritual
- Dificuldade: MD
- Pagina: 64
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: —
- Pre-requisitos: Encantar, Convocar Espírito

### Arma Fantasmagórica
- ID: arma_fantasmagorica
- Dificuldade: D
- Pagina: 65
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 250/0,5 kg#
- Tempo de Operacao: —
- Pre-requisitos: Encantar, Solidificar

### Arma Flamejante
- ID: arma_flamejante
- Dificuldade: D
- Pagina: 75
- Classe: Comum
- Escola(s): Fogo
- Duracao: 1 min.
- Energia: 04/01
- Tempo de Operacao: 2 seg.
- Pre-requisitos: AM2, Calor

### Arma Graciosa
- ID: arma_graciosa
- Dificuldade: D
- Pagina: 63
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 150/0,5 kg
- Tempo de Operacao: —
- Pre-requisitos: Encantar, Aporte

### Arma Penetrante
- ID: arma_penetrante
- Dificuldade: D
- Pagina: 63
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: —
- Pre-requisitos: Encantar, Detectar Pontos Fracos

### Arma Protetora
- ID: arma_protetora
- Dificuldade: D
- Pagina: 64
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: —
- Pre-requisitos: Encantar, Objeto Dançante

### Arma de Relâmpago
- ID: arma_de_relampago
- Dificuldade: D
- Pagina: 198
- Classe: Comum
- Escola(s): Clima, Ar
- Duracao: 1 min.
- Energia: 04/01
- Tempo de Operacao: 2 seg.
- Pre-requisitos: AM2, Relâmpago

### Armadura
- ID: armadura
- Dificuldade: D
- Pagina: 167
- Classe: Comum
- Escola(s): Proteção
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Escudo

### Armadura Flamejante
- ID: armadura_flamejante
- Dificuldade: D
- Pagina: 75
- Classe: Comum
- Escola(s): Fogo
- Duracao: 1 min.
- Energia: 06/03
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, Imunidade ao Fogo, Jato de Chamas

### Armadura de Relâmpagos
- ID: armadura_de_relampagos
- Dificuldade: D
- Pagina: 198
- Classe: Comum
- Escola(s): Clima, Ar
- Duracao: 1 min.
- Energia: 07/04
- Tempo de Operacao: 1 seg.
- Pre-requisitos: 6 mágicas de Relâmpago incl. Imunidade a Relâmpagos

### Aromas do Passado
- ID: aromas_do_passado
- Dificuldade: D
- Pagina: 107
- Classe: Comum
- Escola(s): Reconhecimento, Alimentos
- Duracao: 1 min.
- Energia: 1/1#
- Tempo de Operacao: 10 seg.
- Pre-requisitos: AM2, História, Gerar Odor

### Arremessar Mágica
- ID: arremessar_magica
- Dificuldade: MD
- Pagina: 128
- Classe: Projetil/Especial
- Escola(s): Metamágica
- Duracao: Indef.#
- Energia: 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Retardo, Apanhar Mágica

### Atar
- ID: atar
- Dificuldade: D
- Pagina: 118
- Classe: Comum/R-DX
- Escola(s): Quebrar e Consertar
- Duracao: Perm.
- Energia: 3#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Nó

### Atear Fogo
- ID: atear_fogo
- Dificuldade: D
- Pagina: 72
- Classe: Comum
- Escola(s): Fogo
- Duracao: 1 seg.
- Energia: 1 a 4/I
- Tempo de Operacao: 1 seg.
- Pre-requisitos: nenhum

### Atordoamento
- ID: atordoamento
- Dificuldade: D
- Pagina: 37
- Classe: Comum/R-HT
- Escola(s): Corpo
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Dor

### Atordoamento Mental
- ID: atordoamento_mental
- Dificuldade: D
- Pagina: 135
- Classe: Comum/R-Vont
- Escola(s): Mente
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Torpor ou Atordoamento

### Atrair
- ID: atrair
- Dificuldade: D
- Pagina: 137
- Classe: Área/R-Vont
- Escola(s): Mente
- Duracao: 1 hora
- Energia: 1/I
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Controle de Emoção

### Atrapalhar
- ID: atrapalhar
- Dificuldade: D
- Pagina: 38
- Classe: Bloqueio/R-DX
- Escola(s): Corpo
- Duracao: Instant.
- Energia: 3
- Tempo de Operacao: nenhum
- Pre-requisitos: Inabilidade

### Atravessar Madeira
- ID: atravessar_madeira
- Dificuldade: D
- Pagina: 164
- Classe: Comum
- Escola(s): Plantas
- Duracao: 1 seg.
- Energia: 03/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Atravessar Plantas

### Atravessar Plantas
- ID: atravessar_plantas
- Dificuldade: D
- Pagina: 163
- Classe: Comum
- Escola(s): Plantas
- Duracao: 1 min.
- Energia: 03/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Ocultar Rastros, Moldar Planta

### Atravessar Terra
- ID: atravessar_terra
- Dificuldade: D
- Pagina: 52
- Classe: Comum
- Escola(s): Terra
- Duracao: 10 seg.
- Energia: 3/3#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: 4 mágicas da Terra

### Atravessar Água
- ID: atravessar_agua
- Dificuldade: D
- Pagina: 188
- Classe: Comum
- Escola(s): Água
- Duracao: 1 seg.
- Energia: 04/03
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM1, Moldar Água

### Atrofiar (Sentido)
- ID: atrofiar_sentido
- Dificuldade: D
- Pagina: 133
- Classe: Comum/R-HT
- Escola(s): Mente
- Duracao: 30 min.
- Energia: 1 a 3/M
- Tempo de Operacao: 1 seg.
- Pre-requisitos: —

### Atrofiar Membro
- ID: atrofiar_membro
- Dificuldade: D
- Pagina: 40
- Classe: Toque/R-HT
- Escola(s): Corpo
- Duracao: Perm.
- Energia: 5
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, Paralisar Membro

### Atrofiar Sentidos
- ID: atrofiar_sentidos
- Dificuldade: MD
- Pagina: 134
- Classe: Comum/R-HT
- Escola(s): Mente
- Duracao: 10 min.
- Energia: 2 a 10/M
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Quaisquer 2 mágicas Atrofiar

### Audição Remota
- ID: audicao_remota
- Dificuldade: D
- Pagina: 173
- Classe: Informação
- Escola(s): Som, Reconhecimento
- Duracao: 1 min.
- Energia: 04/02
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM1, 4 mágicas de Som, não pode ser Surdo ou Duro de Ouvido

### Aumentar
- ID: aumentar
- Dificuldade: MD
- Pagina: 42
- Classe: Comum
- Escola(s): Corpo
- Duracao: 1 hora
- Energia: 2/+1 MT/I
- Tempo de Operacao: 5 seg.
- Pre-requisitos: AM2, Alterar Corpo

### Aumentar Atributo
- ID: aumentar_atributo
- Dificuldade: D
- Pagina: 37
- Classe: Comum ou Bloqueio
- Escola(s): Corpo, Mente
- Duracao: Instant.
- Energia: 1 a 5
- Tempo de Operacao: nenhum
- Pre-requisitos: Varia

### Aumentar Carga
- ID: aumentar_carga
- Dificuldade: D
- Pagina: 143
- Classe: Comum/R-Especial
- Escola(s): Deslocamento
- Duracao: 10 min.
- Energia: 1/12,5 kg#
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Aporte

### Aumentar Objeto
- ID: aumentar_objeto
- Dificuldade: MD
- Pagina: 120
- Classe: Comum
- Escola(s): Quebrar e Consertar
- Duracao: 1 hora
- Energia: Varia
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Ampliar Objeto

### Aumentar Outro
- ID: aumentar_outro
- Dificuldade: MD
- Pagina: 43
- Classe: Comum/R-HT
- Escola(s): Corpo
- Duracao: 1 hora
- Energia: 2/+1 MT/I
- Tempo de Operacao: 10 seg.
- Pre-requisitos: AM3, Aumentar

### Aura
- ID: aura
- Dificuldade: D
- Pagina: 101
- Classe: Informação
- Escola(s): Reconhecimento
- Duracao: Instant.
- Energia: 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Detecção de Magia

### Aura Falsa
- ID: aura_falsa
- Dificuldade: D
- Pagina: 122
- Classe: Comum/Área/R-IØ#
- Escola(s): Metamágica
- Duracao: 10 horas
- Energia: 4/M
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Ocultar Mágica, Aura

### Autossuficiência
- ID: autossuficiencia
- Dificuldade: D
- Pagina: 96
- Classe: Área
- Escola(s): Ilusão
- Duracao: Varia
- Energia: 2
- Tempo de Operacao: Varia
- Pre-requisitos: Ilusão Simples

### Avaria/NT
- ID: avariant
- Dificuldade: D
- Pagina: 177
- Classe: Toque/R-HT
- Escola(s): Tecnológica
- Duracao: 1 min.
- Energia: 5
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, Pane

### Baliza
- ID: baliza
- Dificuldade: D
- Pagina: 83
- Classe: Área
- Escola(s): Portal, Deslocamento
- Duracao: 24 horas
- Energia: 10/M
- Tempo de Operacao: 30 seg.
- Pre-requisitos: Teleporte, Viagem no Tempo ou Trocar de Plano

### Banquete do Monge
- ID: banquete_do_monge
- Dificuldade: D
- Pagina: 79
- Classe: Comum
- Escola(s): Alimentos
- Duracao: 24 horas
- Energia: 6
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Banquete do Tolo, Imunidade à Dor

### Banquete do Tolo
- ID: banquete_do_tolo
- Dificuldade: D
- Pagina: 79
- Classe: Comum
- Escola(s): Alimentos
- Duracao: 1 dia
- Energia: 2 por refeição
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, Cozinhar, Tolice

### Barreira Astral
- ID: barreira_astral
- Dificuldade: D
- Pagina: 159
- Classe: Área
- Escola(s): Necromancia
- Duracao: 10 min.
- Energia: 4/2#
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Convocar Espírito, Repelir Espíritos

### Barulho
- ID: barulho
- Dificuldade: D
- Pagina: 173
- Classe: Área
- Escola(s): Som
- Duracao: 5 seg.
- Energia: 04/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Muralha de Silêncio

### Bloqueador de Impressões
- ID: bloqueador_de_impressoes
- Dificuldade: D
- Pagina: 60
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 20/0,5 kg
- Tempo de Operacao: —
- Pre-requisitos: Encantar, Localizador, Resguardar Área

### Bloquear
- ID: bloquear
- Dificuldade: D
- Pagina: 166
- Classe: Bloqueio
- Escola(s): Proteção
- Duracao: Instant.
- Energia: 1/BD+##
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1

### Boca Mágica
- ID: boca_magica
- Dificuldade: D
- Pagina: 104
- Classe: Comum
- Escola(s): Reconhecimento, Alimentos, Som
- Duracao: 1 min.
- Energia: 04/02
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Aporte, Paladar Remoto, Voz Amplificada

### Bola de Cristal
- ID: bola_de_cristal
- Dificuldade: D
- Pagina: 71
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 1000
- Tempo de Operacao: —
- Pre-requisitos: Encantar, Adivinhação (Cristalomancia)

### Bola de Fogo
- ID: bola_de_fogo
- Dificuldade: D
- Pagina: 74
- Classe: Projetil
- Escola(s): Fogo
- Duracao: Instant.
- Energia: 1 a AM#
- Tempo de Operacao: 1 a 3 seg.
- Pre-requisitos: AM1, Criar Fogo, Moldar Fogo

### Bola de Fogo Explosiva
- ID: bola_de_fogo_explosiva
- Dificuldade: D
- Pagina: 75
- Classe: Projetil
- Escola(s): Fogo
- Duracao: Instant.
- Energia: 2 a 2×AM#
- Tempo de Operacao: 1 a 3 seg.
- Pre-requisitos: Bola de Fogo

### Bola de Relâmpagos
- ID: bola_de_relampagos
- Dificuldade: D
- Pagina: 197
- Classe: Comum
- Escola(s): Clima, Ar
- Duracao: 1 min.
- Energia: 2 a 6/M
- Tempo de Operacao: 1 a 3 seg.
- Pre-requisitos: Aporte, Relâmpago

### Bola de Ácido
- ID: bola_de_acido
- Dificuldade: D
- Pagina: 191
- Classe: Projetil
- Escola(s): Água
- Duracao: Instant.
- Energia: 1 a AM#
- Tempo de Operacao: 1 a 3 seg.
- Pre-requisitos: AM2, Criar Ácido

### Bravura
- ID: bravura
- Dificuldade: D
- Pagina: 134
- Classe: Área/R-Vont-1
- Escola(s): Mente
- Duracao: 1 hora
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Medo

### Braço de Ferro
- ID: braco_de_ferro
- Dificuldade: D
- Pagina: 169
- Classe: Bloqueio
- Escola(s): Proteção
- Duracao: Instant.
- Energia: 1
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Imunidade à Dor, DX 11+

### Brilho
- ID: brilho
- Dificuldade: D
- Pagina: 112
- Classe: Área
- Escola(s): Luz e Trevas
- Duracao: Varia
- Energia: Varia
- Tempo de Operacao: Varia
- Pre-requisitos: Luz Contínua

### Bênção
- ID: bencao
- Dificuldade: D
- Pagina: 129
- Classe: Comum
- Escola(s): Metamágica
- Duracao: Especial
- Energia: Varia
- Tempo de Operacao: min.=custo
- Pre-requisitos: AM2, 2 mágicas de 10 escolas#

### Cadência
- ID: cadencia
- Dificuldade: D
- Pagina: 39
- Classe: Comum
- Escola(s): Corpo
- Duracao: 1 hora
- Energia: 05/03
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Apressar, Graça

### Cajado
- ID: cajado
- Dificuldade: D
- Pagina: 70
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 30
- Tempo de Operacao: —
- Pre-requisitos: Encantar

### Calor
- ID: calor
- Dificuldade: D
- Pagina: 74
- Classe: Comum
- Escola(s): Fogo
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 1 min.
- Pre-requisitos: Criar Fogo, Moldar Fogo

### Camada de Gelo
- ID: camada_de_gelo
- Dificuldade: D
- Pagina: 186
- Classe: Área
- Escola(s): Água
- Duracao: Perm.
- Energia: 3
- Tempo de Operacao: Varia
- Pre-requisitos: Geada

### Caminhar na Água
- ID: caminhar_na_agua
- Dificuldade: D
- Pagina: 186
- Classe: Comum
- Escola(s): Água
- Duracao: 1 min.
- Energia: 03/02
- Tempo de Operacao: 4 seg.
- Pre-requisitos: Moldar Água

### Caminhar nas Nuvens
- ID: caminhar_nas_nuvens
- Dificuldade: D
- Pagina: 148
- Classe: Comum
- Escola(s): Desloc., Clima
- Duracao: 1 hora
- Energia: 03/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Caminhar no Ar, Caminhar na Água

### Caminhar nas Paredes
- ID: caminhar_nas_paredes
- Dificuldade: D
- Pagina: 144
- Classe: Comum
- Escola(s): Deslocamento
- Duracao: 1 min.
- Energia: 1 por 25 kg/M#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Aporte

### Caminhar no Ar
- ID: caminhar_no_ar
- Dificuldade: D
- Pagina: 25
- Classe: Comum
- Escola(s): Ar
- Duracao: 1 min.
- Energia: 03/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Moldar Ar

### Carne para Gelo
- ID: carne_para_gelo
- Dificuldade: MD
- Pagina: 190
- Classe: Comum/R/HT
- Escola(s): Água
- Duracao: Perm.
- Energia: 12
- Tempo de Operacao: 2 seg.
- Pre-requisitos: AM1, Geladura, Corpo de Água

### Carne para Pedra
- ID: carne_para_pedra
- Dificuldade: D
- Pagina: 51
- Classe: Comum/R/HT
- Escola(s): Terra
- Duracao: Instant.
- Energia: 10#
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Terra para Pedra

### Cartógrafo
- ID: cartografo
- Dificuldade: D
- Pagina: 118
- Classe: Especial
- Escola(s): Quebrar e Consertar
- Duracao: 1 hora
- Energia: 04/02
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Entalhar, Medidas

### Cativar
- ID: cativar
- Dificuldade: D
- Pagina: 139
- Classe: Especial/R-Vont
- Escola(s): Mente
- Duracao: 1 hora
- Energia: 03/03
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Esquecimento, Torpor, Retardar

### Cavalgar
- ID: cavalgar
- Dificuldade: D
- Pagina: 31
- Classe: Comum
- Escola(s): Animais
- Duracao: 5 min.
- Energia: 02/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: 1 mágica de Controle#

### Cegar
- ID: cegar
- Dificuldade: D
- Pagina: 38
- Classe: Comum/R-HT
- Escola(s): Corpo
- Duracao: 10 seg.
- Energia: 04/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: 2 mágicas de Luz, Espasmo

### Cessar Corrente Elétrica
- ID: cessar_corrente_eletrica
- Dificuldade: D
- Pagina: 179
- Classe: Área
- Escola(s): Tecnológica
- Duracao: 1 min.
- Energia: 3/M
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM1, Localizar Corrente Elétrica

### Cessar Cura
- ID: cessar_cura
- Dificuldade: D
- Pagina: 153
- Classe: Comum
- Escola(s): Necromancia
- Duracao: Indef.#
- Energia: 10
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Retardar Cura

### Cessar Espasmo
- ID: cessar_espasmo
- Dificuldade: D
- Pagina: 35
- Classe: Comum
- Escola(s): Corpo, Cura
- Duracao: Instant.
- Energia: 1
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Espasmo ou Conceder Vitalidade

### Cessar Paralisia
- ID: cessar_paralisia
- Dificuldade: D
- Pagina: 93
- Classe: Comum
- Escola(s): Cura
- Duracao: Perm.
- Energia: 1 ou 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Cura Profunda, ou Cura Superficial e Paralisar Membro

### Cessar Sangramento
- ID: cessar_sangramento
- Dificuldade: D
- Pagina: 91
- Classe: Comum
- Escola(s): Cura
- Duracao: Perm.
- Energia: 1 ou 10
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Conceder Vitalidade

### Chama Essencial
- ID: chama_essencial
- Dificuldade: D
- Pagina: 75
- Classe: Área
- Escola(s): Fogo
- Duracao: 1 min.
- Energia: 3/2#
- Tempo de Operacao: 3 seg.
- Pre-requisitos: 6 mágicas do Fogo

### Chama Fantasmagórica
- ID: chama_fantasmagorica
- Dificuldade: D
- Pagina: 73
- Classe: Área
- Escola(s): Fogo, Ilusão
- Duracao: 1 min.
- Energia: 1/I
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Moldar Fogo ou Ilusão Simples

### Chave-Mestra
- ID: chavemestra
- Dificuldade: D
- Pagina: 144
- Classe: Comum/R-Tranca Mágica
- Escola(s): Deslocamento
- Duracao: Perm.
- Energia: 3
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Serralheiro ou Aporte e AM2

### Chicote de Relâmpago
- ID: chicote_de_relampago
- Dificuldade: D
- Pagina: 196
- Classe: Comum
- Escola(s): Clima, Ar
- Duracao: 10 seg.
- Energia: 1 por 2 m#
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Relâmpago

### Chuva
- ID: chuva
- Dificuldade: D
- Pagina: 195
- Classe: Área
- Escola(s): Clima, Ar, Água
- Duracao: 1 hora
- Energia: 1/10/I#
- Tempo de Operacao: 1 min.
- Pre-requisitos: Nuvens

### Chuva de Adagas de Gelo
- ID: chuva_de_adagas_de_gelo
- Dificuldade: D
- Pagina: 192
- Classe: Área
- Escola(s): Água
- Duracao: 1 min.
- Energia: 2/2#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, Granizo, Adaga de Gelo

### Chuva de Fogo
- ID: chuva_de_fogo
- Dificuldade: D
- Pagina: 74
- Classe: Área
- Escola(s): Fogo
- Duracao: 1 min.
- Energia: 1/I#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, Criar Fogo

### Chuva de Nozes
- ID: chuva_de_nozes
- Dificuldade: D
- Pagina: 165
- Classe: Área
- Escola(s): Plantas
- Duracao: 1 min.
- Energia: 1/10/I
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, 6 mágicas sobre Plantas incl. Moldar Planta

### Chuva de Pedras
- ID: chuva_de_pedras
- Dificuldade: D
- Pagina: 53
- Classe: Área
- Escola(s): Terra
- Duracao: 1 min.
- Energia: 1/I#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, Criar Terra

### Chuva de Ácido
- ID: chuva_de_acido
- Dificuldade: D
- Pagina: 191
- Classe: Área
- Escola(s): Água
- Duracao: 1 min.
- Energia: 03/03
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, Criar Água, Criar Ácido

### Cobertura Ilusória
- ID: cobertura_ilusoria
- Dificuldade: D
- Pagina: 96
- Classe: Comum
- Escola(s): Ilusão
- Duracao: 1 min.
- Energia: 1 a 2/M
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Ilusão Simples

### Coceira
- ID: coceira
- Dificuldade: D
- Pagina: 35
- Classe: Comum/R-HT
- Escola(s): Corpo
- Duracao: Coçar#
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: —

### Cola
- ID: cola
- Dificuldade: D
- Pagina: 142
- Classe: Área
- Escola(s): Deslocamento
- Duracao: 10 min.
- Energia: 3/I
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Apressar

### Comandar Espírito
- ID: comandar_espirito
- Dificuldade: D
- Pagina: 153
- Classe: Comum/R-Vont
- Escola(s): Necromancia
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Convocar Espírito, Espantar Espírito

### Comando
- ID: comando
- Dificuldade: D
- Pagina: 136
- Classe: Bloqueio/R-Vont
- Escola(s): Mente
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, Esquecimento

### Combustível Essencial/NT
- ID: combustivel_essencialnt
- Dificuldade: D
- Pagina: 179
- Classe: Comum
- Escola(s): Tecnológica
- Duracao: Perm.
- Energia: 2/litro
- Tempo de Operacao: 1 seg.
- Pre-requisitos: 6 máscaras de Energia

### Compartilhar Energia
- ID: compartilhar_energia
- Dificuldade: D
- Pagina: 89
- Classe: Comum
- Escola(s): Cura
- Duracao: Especial
- Energia: Varia
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Conceder Energia

### Compartilhar Vitalidade
- ID: compartilhar_vitalidade
- Dificuldade: D
- Pagina: 90
- Classe: Comum
- Escola(s): Cura
- Duracao: Perm.
- Energia: 0#
- Tempo de Operacao: 1 seg./PV
- Pre-requisitos: Conceder Vitalidade

### Compelir à Mentira
- ID: compelir_a_mentira
- Dificuldade: D
- Pagina: 137
- Classe: Comum/R-Vont
- Escola(s): Mente, Comunicação
- Duracao: 5 min.
- Energia: 04/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Controle de Emoção

### Compelir à Verdade
- ID: compelir_a_verdade
- Dificuldade: D
- Pagina: 47
- Classe: Informação/R-Vont
- Escola(s): Comunicação
- Duracao: 5 min.
- Energia: 04/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, Percepção de Veracidade

### Comunicação
- ID: comunicacao
- Dificuldade: MD
- Pagina: 48
- Classe: Comum
- Escola(s): Comunicação
- Duracao: 1 min.
- Energia: 04/04
- Tempo de Operacao: 4 seg.
- Pre-requisitos: Olho Mágico, Audição Remota, Vozes, Ilusão Simples

### Conceder Corrente Elétrica/NT
- ID: conceder_corrente_eletricant
- Dificuldade: D
- Pagina: 180
- Classe: Comum
- Escola(s): Tecnológica
- Duracao: Indef.
- Energia: Varia
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, Localizar Corrente Elétrica

### Conceder Energia
- ID: conceder_energia
- Dificuldade: D
- Pagina: 89
- Classe: Comum
- Escola(s): Cura
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1 ou a vantagem Empatia

### Conceder Idioma
- ID: conceder_idioma
- Dificuldade: D
- Pagina: 46
- Classe: Comum
- Escola(s): Comunicação
- Duracao: 1 min.
- Energia: 03/01
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Falar com Animais ou 3 máscaras de Comunicação e Empatia

### Conceder Mágica
- ID: conceder_magica
- Dificuldade: MD
- Pagina: 126
- Classe: Comum
- Escola(s): Metamágica
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM1, Conceder Perícia, 1 máscara em 6 escolas diferentes

### Conceder Perícia
- ID: conceder_pericia
- Dificuldade: D
- Pagina: 47
- Classe: Comum
- Escola(s): Comunicação
- Duracao: 1 min.
- Energia: 03/02
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Transmissão de Pensamento, IQ 11+

### Conceder Vitalidade
- ID: conceder_vitalidade
- Dificuldade: D
- Pagina: 89
- Classe: Comum
- Escola(s): Cura
- Duracao: 1 hora
- Energia: 1 por PV emprestado
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Conceder Energia

### Concussão
- ID: concussao
- Dificuldade: D
- Pagina: 26
- Classe: Projétil
- Escola(s): Ar, Som
- Duracao: Instant.
- Energia: 2 a 2x AM#
- Tempo de Operacao: 1 a 3 seg.
- Pre-requisitos: Moldar Ar, Estrondo

### Condensar Vapor
- ID: condensar_vapor
- Dificuldade: D
- Pagina: 189
- Classe: Área
- Escola(s): Água
- Duracao: Perm.
- Energia: 2#
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Frio ou Fever Água

### Condicionamento
- ID: condicionamento
- Dificuldade: MD
- Pagina: 140
- Classe: Comum/R-Vont
- Escola(s): Mente
- Duracao: Perm.
- Energia: 12
- Tempo de Operacao: 30 seg.
- Pre-requisitos: AM2, 10 máscaras de Controle da Mente

### Condicionamento Permanente
- ID: condicionamento_permanente
- Dificuldade: MD
- Pagina: 141
- Classe: Comum/R-Vont
- Escola(s): Mente
- Duracao: Perm.
- Energia: 30
- Tempo de Operacao: 1 min.
- Pre-requisitos: AM3, 15 máscaras de Controle da Mente incl. Condicionamento

### Conduzir Corrente Elétrica/NT
- ID: conduzir_corrente_eletricant
- Dificuldade: MD
- Pagina: 180
- Classe: Especial
- Escola(s): Tecnológica
- Duracao: 1 min.
- Energia: 0/1#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, Localizar Corrente Elétrica

### Conexão
- ID: conexao
- Dificuldade: D
- Pagina: 131
- Classe: Área
- Escola(s): Metamágica
- Duracao: Indef.#
- Energia: 8
- Tempo de Operacao: 4 horas
- Pre-requisitos: Retardo

### Conexão com Animal
- ID: conexao_com_animal
- Dificuldade: D
- Pagina: 30
- Classe: Comum
- Escola(s): Animais
- Duracao: Especial
- Energia: 3
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Convocar Animal

### Congelar
- ID: congelar
- Dificuldade: D
- Pagina: 185
- Classe: Comum
- Escola(s): Água
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Moldar Água

### Consertar
- ID: consertar
- Dificuldade: D
- Pagina: 118
- Classe: Comum
- Escola(s): Quebrar e Consertar
- Duracao: Perm.
- Energia: 2/2,5 kg#
- Tempo de Operacao: 1 seg./0,5 kg
- Pre-requisitos: AM2, Remendar

### Contrair Objeto
- ID: contrair_objeto
- Dificuldade: MD
- Pagina: 120
- Classe: Comum
- Escola(s): Quebrar e Consertar
- Duracao: 1 hora
- Energia: Varia
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM3, Transformar Objeto

### Contramágica
- ID: contramagica
- Dificuldade: D
- Pagina: 121
- Classe: Comum/R-Mágica
- Escola(s): Metamágica
- Duracao: Instant.
- Energia: Varia
- Tempo de Operacao: 5 seg.
- Pre-requisitos: AM1

### Controle de Animal
- ID: controle_de_animal
- Dificuldade: D
- Pagina: 30
- Classe: Comum/R-Vont
- Escola(s): Animais
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Acalmar Animal

### Controle de Criação
- ID: controle_de_criacao
- Dificuldade: D
- Pagina: 99
- Classe: Comum/R-Mágica
- Escola(s): Ilusão
- Duracao: Instant.
- Energia: 1
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Criar Animal ou Criar Servo

### Controle de Elemental
- ID: controle_de_elemental
- Dificuldade: D
- Pagina: 28
- Classe: Especial
- Escola(s): Ar, Fogo, Terra, Água
- Duracao: 1 min.
- Energia: Especial
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Convocar Elemental#

### Controle de Emoção
- ID: controle_de_emocao
- Dificuldade: D
- Pagina: 137
- Classe: Área/R-Vont
- Escola(s): Mente
- Duracao: 1 hora
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Lealdade ou Atordoamento Mental

### Controle de Híbrido
- ID: controle_de_hibrido
- Dificuldade: MD
- Pagina: 30
- Classe: Comum/R-Vont
- Escola(s): Animais
- Duracao: 1 min.
- Energia: 06/03
- Tempo de Operacao: 1 seg.
- Pre-requisitos: 2 máscaras de Controle#

### Controle de Ilusão
- ID: controle_de_ilusao
- Dificuldade: D
- Pagina: 97
- Classe: Comum/R-Mágica
- Escola(s): Ilusão
- Duracao: Perm.
- Energia: 1
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Ilusão Perfeita

### Controle de Membro
- ID: controle_de_membro
- Dificuldade: D
- Pagina: 40
- Classe: Comum/R-Vont
- Escola(s): Corpo
- Duracao: 5 seg.
- Energia: 3/3#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, 5 máscaras de Corpo incl. Espasmo

### Controle de Máquina/NT
- ID: controle_de_maquinant
- Dificuldade: D
- Pagina: 176
- Classe: Comum
- Escola(s): Tecnológica
- Duracao: 1 min.
- Energia: 06/03
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Identificar Função, Serralheiro, Relâmpago

### Controle de Pessoa
- ID: controle_de_pessoa
- Dificuldade: D
- Pagina: 49
- Classe: Comum/R-Vont
- Escola(s): Comunicação
- Duracao: 1 min.
- Energia: 06/03
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Passageiro da Alma ou Telepatia

### Controle de Planta
- ID: controle_de_planta
- Dificuldade: D
- Pagina: 164
- Classe: Comm/R-Vont
- Escola(s): Plantas
- Duracao: 1 min.
- Energia: 3/M
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Percepção de Planta

### Controle de Portal
- ID: controle_de_portal
- Dificuldade: D
- Pagina: 85
- Classe: Comm/R-Portal
- Escola(s): Portal
- Duracao: 1 min.
- Energia: 06/03
- Tempo de Operacao: 10 seg.
- Pre-requisitos: AM3, Localizar Portal

### Controle de Zumbi
- ID: controle_de_zumbi
- Dificuldade: D
- Pagina: 152
- Classe: Comm/R-Mágica
- Escola(s): Necromancia
- Duracao: Perm.
- Energia: 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Zumbi

### Conversar
- ID: conversar
- Dificuldade: D
- Pagina: 173
- Classe: Comm/R-Mágica
- Escola(s): Som
- Duracao: Indef.#
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, Deturpar, Silêncio

### Convocar Animal
- ID: convocar_animal
- Dificuldade: D
- Pagina: 30
- Classe: Comm
- Escola(s): Animais
- Duracao: 1 min.
- Energia: 3/2#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Acalmar Animal

### Convocar Demônio
- ID: convocar_demonio
- Dificuldade: D
- Pagina: 155
- Classe: Especial
- Escola(s): Necromancia
- Duracao: 1 hora#
- Energia: 20#
- Tempo de Operacao: 5 min.
- Pre-requisitos: AM1, 1 mágica em 10 escolas diferentes

### Convocar Elemental
- ID: convocar_elemental
- Dificuldade: D
- Pagina: 27
- Classe: Especial
- Escola(s): Ar, Fogo, Terra, Água
- Duracao: 1 hora
- Energia: 4#
- Tempo de Operacao: 30 seg.
- Pre-requisitos: AM1, #

### Convocar Espírito
- ID: convocar_espirito
- Dificuldade: D
- Pagina: 150
- Classe: Informação/R-Vont
- Escola(s): Necromancia
- Duracao: 1 min.
- Energia: 20/10#
- Tempo de Operacao: 5 min.
- Pre-requisitos: Visão da Morte, AM2

### Convocar Máquina/NT
- ID: convocar_maquinant
- Dificuldade: D
- Pagina: 176
- Classe: Comm
- Escola(s): Tecnológica
- Duracao: 1 min.
- Energia: 04/02
- Tempo de Operacao: 4 seg.
- Pre-requisitos: Controle de Máquina

### Convocar Sombra
- ID: convocar_sombra
- Dificuldade: MD
- Pagina: 102
- Classe: Informação/R-Vont
- Escola(s): Reconhecimento
- Duracao: 1 min.
- Energia: 50/20
- Tempo de Operacao: 10 min.
- Pre-requisitos: Convocar Espíritos ou Adivinhação

### Convocar Zumbi
- ID: convocar_zumbi
- Dificuldade: D
- Pagina: 153
- Classe: Especial
- Escola(s): Necromancia
- Duracao: 1 min.
- Energia: 5/2#
- Tempo de Operacao: 4 seg.
- Pre-requisitos: Zumbi

### Convocação Planar
- ID: convocacao_planar
- Dificuldade: D
- Pagina: 82
- Classe: Especial
- Escola(s): Portal
- Duracao: 1 hora
- Energia: 20#
- Tempo de Operacao: 5 min.
- Pre-requisitos: AM1, 1 mágica em 10 escolas diferentes

### Copiar
- ID: copiar
- Dificuldade: D
- Pagina: 116
- Classe: Comm
- Escola(s): Quebrar e Consertar
- Duracao: Perm.
- Energia: 2 mais 1/cópia
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Tingir, um idioma Com Sotaque

### Cores
- ID: cores
- Dificuldade: D
- Pagina: 110
- Classe: Comm
- Escola(s): Luz e Trevas
- Duracao: 1 min.
- Energia: 02/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Luz

### Cornucópia
- ID: cornucopia
- Dificuldade: D
- Pagina: 64
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 50× valor $#
- Tempo de Operacao: —
- Pre-requisitos: Encantar, 2 mágicas de Encantamento de Armas

### Corpo Etéreo
- ID: corpo_etereo
- Dificuldade: MD
- Pagina: 146
- Classe: Comum
- Escola(s): Deslocamento
- Duracao: 10 seg.
- Energia: 08/04
- Tempo de Operacao: 30 seg.
- Pre-requisitos: 6 mágicas de Deslocamento ou AM3 e Corpo de Ar

### Corpo de Ar
- ID: corpo_de_ar
- Dificuldade: D
- Pagina: 24
- Classe: Comm/R-HT
- Escola(s): Ar
- Duracao: 1 min.
- Energia: 04/01
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Moldar Ar

### Corpo de Chamas
- ID: corpo_de_chamas
- Dificuldade: MD
- Pagina: 76
- Classe: Comm/R-HT
- Escola(s): Fogo
- Duracao: 1 min.
- Energia: 12/04
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Sopro de Fogo

### Corpo de Gelo
- ID: corpo_de_gelo
- Dificuldade: MD
- Pagina: 189
- Classe: Comm/R-HT
- Escola(s): Água
- Duracao: 1 min.
- Energia: 07/03
- Tempo de Operacao: 5 seg.
- Pre-requisitos: AM2, Corpo de Água, Congelar

### Corpo de Lodo
- ID: corpo_de_lodo
- Dificuldade: D
- Pagina: 165
- Classe: Comm/R-HT
- Escola(s): Plantas
- Duracao: 1 min.
- Energia: 06/02
- Tempo de Operacao: 5 seg.
- Pre-requisitos: AM2, Forma de Planta, Moldar Água

### Corpo de Madeira
- ID: corpo_de_madeira
- Dificuldade: D
- Pagina: 165
- Classe: Comm/R-HT
- Escola(s): Plantas
- Duracao: 1 min.
- Energia: 07/03
- Tempo de Operacao: 5 seg.
- Pre-requisitos: AM2, Forma de Planta

### Corpo de Metal
- ID: corpo_de_metal
- Dificuldade: MD
- Pagina: 183
- Classe: Comm/R-HT
- Escola(s): Tecnológica
- Duracao: 1 min.
- Energia: 12/06
- Tempo de Operacao: 5 seg.
- Pre-requisitos: AM2, Moldar Metal

### Corpo de Pedra
- ID: corpo_de_pedra
- Dificuldade: MD
- Pagina: 54
- Classe: Comm/R-HT
- Escola(s): Terra
- Duracao: 1 min.
- Energia: 10/05
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Pedra para Carne

### Corpo de Plástico
- ID: corpo_de_plastico
- Dificuldade: D
- Pagina: 183
- Classe: Comum/R-HT
- Escola(s): Tecnológica
- Duracao: 1 min.
- Energia: 10/05
- Tempo de Operacao: 5 seg.
- Pre-requisitos: AM2, Moldar Plástico

### Corpo de Relâmpagos
- ID: corpo_de_relampagos
- Dificuldade: MD
- Pagina: 198
- Classe: Comum/R-HT
- Escola(s): Clima, Ar
- Duracao: 1 min.
- Energia: 12/04
- Tempo de Operacao: 5 seg.
- Pre-requisitos: AM2, Relâmpago

### Corpo de Sombra
- ID: corpo_de_sombra
- Dificuldade: D
- Pagina: 114
- Classe: Comum/R-HT
- Escola(s): Luz e Trevas
- Duracao: 1 min.
- Energia: 06/03
- Tempo de Operacao: 5 seg.
- Pre-requisitos: AM2, Moldar Trevas

### Corpo de Vento
- ID: corpo_de_vento
- Dificuldade: D
- Pagina: 27
- Classe: Comum/R-HT
- Escola(s): Ar
- Duracao: 1 min.
- Energia: 08/04
- Tempo de Operacao: 2 seg.
- Pre-requisitos: AM3, Corpo de Ar; Furacão, 1 mágica em 5 escolas diferentes#

### Corpo de Água
- ID: corpo_de_agua
- Dificuldade: D
- Pagina: 185
- Classe: Comm/R-HT
- Escola(s): Água
- Duracao: 1 min.
- Energia: 05/02
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Moldar Água

### Corpulência
- ID: corpulencia
- Dificuldade: MD
- Pagina: 43
- Classe: Comum/R-HT
- Escola(s): Corpo
- Duracao: 10 min.
- Energia: 06/06
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM2, Criar Terra, Criar Água, 4 mágicas de Corpo incl. Alterar Corpo

### Correnteza
- ID: correnteza
- Dificuldade: D
- Pagina: 194
- Classe: Especial/Área
- Escola(s): Clima, Água
- Duracao: 1 hora
- Energia: 1/50/I
- Tempo de Operacao: 1 min.
- Pre-requisitos: 6 mágicas da Água

### Corte de Cabelo
- ID: corte_de_cabelo
- Dificuldade: D
- Pagina: 39
- Classe: Comum/R-HT
- Escola(s): Corpo
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Enfraquecer, 2 mágicas de Corpo

### Cozinhar
- ID: cozinhar
- Dificuldade: D
- Pagina: 78
- Classe: Comum
- Escola(s): Alimentos
- Duracao: Instant.
- Energia: 1 por refeição
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Testar Alimento, Criar Fogo

### Crescimento Capilar
- ID: crescimento_capilar
- Dificuldade: D
- Pagina: 39
- Classe: Comum/R-HT
- Escola(s): Corpo
- Duracao: 5 seg.
- Energia: 01/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: 5 mágicas de Corpo

### Crescimento de Plantas
- ID: crescimento_de_plantas
- Dificuldade: D
- Pagina: 162
- Classe: Área
- Escola(s): Plantas
- Duracao: 1 min.
- Energia: 03/02
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Curar Plantas

### Criar Alimento
- ID: criar_alimento
- Dificuldade: D
- Pagina: 79
- Classe: Comum
- Escola(s): Alimentos
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: 30 seg.
- Pre-requisitos: Cozinhar, Localizar Alimento

### Criar Animal
- ID: criar_animal
- Dificuldade: D
- Pagina: 98
- Classe: Comum
- Escola(s): Ilusão
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: se->custo
- Pre-requisitos: Criar Água, Criar Objeto, IQ 12+

### Criar Ar
- ID: criar_ar
- Dificuldade: D
- Pagina: 23
- Classe: Área
- Escola(s): Ar
- Duracao: 5 seg.#
- Energia: 1
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Purificar o Ar ou Localizar Ar

### Criar Combustível/NT
- ID: criar_combustivelnt
- Dificuldade: D
- Pagina: 179
- Classe: Comum
- Escola(s): Tecnológica
- Duracao: Perm.
- Energia: 1/0,5 kg
- Tempo de Operacao: 30 seg.
- Pre-requisitos: Localizar Combustível, 2 mágicas de transmutação

### Criar Elemental
- ID: criar_elemental
- Dificuldade: D
- Pagina: 28
- Classe: Especial
- Escola(s): Ar, Fogo, Terra, Água
- Duracao: Perm.
- Energia: Especial
- Tempo de Operacao: Especial
- Pre-requisitos: AM2, Controle de Elemental#

### Criar Fogo
- ID: criar_fogo
- Dificuldade: D
- Pagina: 72
- Classe: Área
- Escola(s): Fogo
- Duracao: 1 min.
- Energia: 2/M
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Atear Fogo ou Localizar Fogo

### Criar Gelo
- ID: criar_gelo
- Dificuldade: D
- Pagina: 188
- Classe: Comum
- Escola(s): Água
- Duracao: Perm.
- Energia: 2/4 litros
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Congelar

### Criar Guerreiro
- ID: criar_guerreiro
- Dificuldade: D
- Pagina: 98
- Classe: Comum
- Escola(s): Ilusão
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 4 seg.
- Pre-requisitos: Criar Servo

### Criar Montaria
- ID: criar_montaria
- Dificuldade: D
- Pagina: 99
- Classe: Comum
- Escola(s): Ilusão
- Duracao: 1 hora
- Energia: Varia
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM3, Criar Animal

### Criar Nascente
- ID: criar_nascente
- Dificuldade: D
- Pagina: 190
- Classe: Comum
- Escola(s): Água
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: 1 min.
- Pre-requisitos: Secar Nascente, Moldar Água

### Criar Objeto
- ID: criar_objeto
- Dificuldade: MD
- Pagina: 98
- Classe: Comum
- Escola(s): Ilusão
- Duracao: Indef.#
- Energia: 2/2,5 kg
- Tempo de Operacao: se->custo
- Pre-requisitos: AM2, Criar Terra, Ilusão Perfeita

### Criar Planta
- ID: criar_planta
- Dificuldade: D
- Pagina: 163
- Classe: Área
- Escola(s): Plantas
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: se->custo
- Pre-requisitos: AM1, Crescimento de Plantas

### Criar Porta
- ID: criar_porta
- Dificuldade: D
- Pagina: 84
- Classe: Comum
- Escola(s): Portal
- Duracao: 10 seg.
- Energia: 2/1 m²#
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Teleporte, qualquer uma mágica Atravessar

### Criar Portal
- ID: criar_portal
- Dificuldade: MD
- Pagina: 85
- Classe: Comum
- Escola(s): Portal
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: Varia
- Pre-requisitos: Controle de Portal ou Teleporte, Viagem no Tempo ou Trocar de Plano

### Criar Servo
- ID: criar_servo
- Dificuldade: D
- Pagina: 98
- Classe: Comum
- Escola(s): Ilusão
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM3, IQ 12+, Criar Objeto

### Criar Terra
- ID: criar_terra
- Dificuldade: D
- Pagina: 51
- Classe: Comum
- Escola(s): Terra
- Duracao: Perm.
- Energia: 2/0,75 m³
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Terra para Pedra

### Criar Vapor
- ID: criar_vapor
- Dificuldade: D
- Pagina: 190
- Classe: Área
- Escola(s): Água
- Duracao: 5 min.#
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Ferver Água

### Criar Ácido
- ID: criar_acido
- Dificuldade: D
- Pagina: 190
- Classe: Comum
- Escola(s): Água
- Duracao: Perm.
- Energia: 1/4 litros
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Criar Água, Criar Terra

### Criar Água
- ID: criar_agua
- Dificuldade: D
- Pagina: 184
- Classe: Comum
- Escola(s): Água
- Duracao: Perm.
- Energia: 2/4 litros
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Purificar a Água

### Criação Inspirada
- ID: criacao_inspirada
- Dificuldade: MD
- Pagina: 115
- Classe: Comum
- Escola(s): Quebrar e Consertar
- Duracao: Perm.
- Energia: 5/dia
- Tempo de Operacao: Varia
- Pre-requisitos: —

### Criptografar
- ID: criptografar
- Dificuldade: D
- Pagina: 135
- Classe: Comum/R-Especial
- Escola(s): Mente
- Duracao: 1 semana
- Energia: Varia
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Torpor

### Cura Profunda
- ID: cura_profunda
- Dificuldade: MD
- Pagina: 91
- Classe: Comum
- Escola(s): Cura
- Duracao: Perm.
- Energia: 1 a 4
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, Cura Superficial

### Cura Superficial
- ID: cura_superficial
- Dificuldade: D
- Pagina: 91
- Classe: Comum
- Escola(s): Cura
- Duracao: Perm.
- Energia: 1 a 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Conceder Vitalidade

### Cura Superior
- ID: cura_superior
- Dificuldade: MD
- Pagina: 91
- Classe: Comum
- Escola(s): Cura
- Duracao: Perm.
- Energia: 20
- Tempo de Operacao: 1 min.
- Pre-requisitos: AM3, Cura Profunda

### Curar Doenças
- ID: curar_doencas
- Dificuldade: D
- Pagina: 91
- Classe: Comum
- Escola(s): Cura
- Duracao: Instant.
- Energia: 4
- Tempo de Operacao: 10 min.
- Pre-requisitos: Cura Profunda, Aliviar Enjooos

### Curar Planta
- ID: curar_planta
- Dificuldade: D
- Pagina: 161
- Classe: Área
- Escola(s): Plantas
- Duracao: Perm.
- Energia: 3
- Tempo de Operacao: 1 min.
- Pre-requisitos: Identificar Planta

### Curar Radiação
- ID: curar_radiacao
- Dificuldade: MD
- Pagina: 182
- Classe: Comum
- Escola(s): Tecnológica, Cura
- Duracao: Perm.
- Energia: 1/10 rads#
- Tempo de Operacao: 30 seg.
- Pre-requisitos: Imunidade à Radiação, Cura Profunda

### Cócegas
- ID: cocegas
- Dificuldade: D
- Pagina: 36
- Classe: Comum/R-Vont
- Escola(s): Corpo
- Duracao: 1 min.
- Energia: 05/05
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Espasmo

### Debilitar
- ID: debilitar
- Dificuldade: D
- Pagina: 36
- Classe: Comum/R-HT
- Escola(s): Corpo
- Duracao: 1 min.
- Energia: 1 por ST/M
- Tempo de Operacao: 1 seg.
- Pre-requisitos: —

### Decapitação
- ID: decapitacao
- Dificuldade: MD
- Pagina: 42
- Classe: Comum/R-HT+2
- Escola(s): Corpo
- Duracao: Indef.
- Energia: 4
- Tempo de Operacao: 2 seg.
- Pre-requisitos: AM2, Alterar Corpo

### Defasar
- ID: defasar
- Dificuldade: D
- Pagina: 83
- Classe: Bloqueio
- Escola(s): Portal
- Duracao: Instant.
- Energia: 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM3, Viagem Planar ou Corpo Etéreo

### Defasar Outro
- ID: defasar_outro
- Dificuldade: MD
- Pagina: 83
- Classe: Bloqueio
- Escola(s): Portal
- Duracao: Instant.
- Energia: 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Defasar

### Denominar
- ID: denominar
- Dificuldade: D
- Pagina: 68
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 200 ou 400#
- Tempo de Operacao: —
- Pre-requisitos: Encantar

### Desatar
- ID: desatar
- Dificuldade: D
- Pagina: 145
- Classe: Comum/R-Especial
- Escola(s): Deslocamento
- Duracao: Instant.
- Energia: 3 ou 6#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Serralheiro

### Descanso Final
- ID: descanso_final
- Dificuldade: D
- Pagina: 89
- Classe: Comum
- Escola(s): Cura, Necromancia
- Duracao: Perm.
- Energia: 20
- Tempo de Operacao: 10 min.#
- Pre-requisitos: AM1 ou Empatia com Espíritos

### Descongelar
- ID: descongelar
- Dificuldade: D
- Pagina: 186
- Classe: Área
- Escola(s): Água
- Duracao: Perm.#
- Energia: 2#
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Calor ou Congelar

### Desejo
- ID: desejo
- Dificuldade: MD
- Pagina: 61
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Especial
- Energia: 250
- Tempo de Operacao: —
- Pre-requisitos: Pequeno Desejo, 1 mágica em 15 escolas

### Desejo Superior
- ID: desejo_superior
- Dificuldade: MD
- Pagina: 62
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Especial
- Energia: 2000
- Tempo de Operacao: —
- Pre-requisitos: AM3, Desejo, (DX + IQ):30+

### Desidratar
- ID: desidratar
- Dificuldade: D
- Pagina: 188
- Classe: Comum/R-HT
- Escola(s): Água
- Duracao: Perm.
- Energia: 1 a 3
- Tempo de Operacao: 2 seg.
- Pre-requisitos: 5 mágicas da Água incl. Dissipar Água

### Desintegrar
- ID: desintegrar
- Dificuldade: MD
- Pagina: 120
- Classe: Comum
- Escola(s): Quebrar e Consertar
- Duracao: Perm.
- Energia: 1 a 4
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, Fragmentar, Estragar, Terra para Ar, Dissipar Ar e Dissipar Água

### Deslizar
- ID: deslizar
- Dificuldade: D
- Pagina: 145
- Classe: Comum/R-Vont
- Escola(s): Deslocamento
- Duracao: 1 min.
- Energia: 02/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Aporte, Ensebar

### Deslocar Mágica
- ID: deslocar_magica
- Dificuldade: D
- Pagina: 124
- Classe: Comum/R-Mágica
- Escola(s): Metamágica
- Duracao: Varia
- Energia: Varia
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Suspender Mágica

### Deslocar Terreno
- ID: deslocar_terreno
- Dificuldade: MD
- Pagina: 55
- Classe: Área/R-Especial
- Escola(s): Terra
- Duracao: 1 hora
- Energia: 10/08
- Tempo de Operacao: 1 min.
- Pre-requisitos: Alterar Terreno, Ocultar Objeto

### Desorientar
- ID: desorientar
- Dificuldade: D
- Pagina: 135
- Classe: Área/R-Vont
- Escola(s): Mente
- Duracao: Indef.#
- Energia: 1
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Tolice

### Despertar
- ID: despertar
- Dificuldade: D
- Pagina: 90
- Classe: Área
- Escola(s): Cura
- Duracao: Instant.
- Energia: 1
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Conceder Vitalidade

### Despertar Computador/NT
- ID: despertar_computadornt
- Dificuldade: D
- Pagina: 178
- Classe: Comum
- Escola(s): Tecnológica
- Duracao: 1 hora
- Energia: Varia
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Animação, Sabedoria

### Despertar Espírito do Ofício
- ID: despertar_espirito_do_oficio
- Dificuldade: D
- Pagina: 115
- Classe: Comum
- Escola(s): Quebrar e Consertar, Necromancia
- Duracao: Perm.
- Energia: 1/min.
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Criação Inspirada, Percepção de Espíritos

### Destilar
- ID: destilar
- Dificuldade: D
- Pagina: 79
- Classe: Comum
- Escola(s): Alimentos, Água
- Duracao: Perm.
- Energia: 1/litro
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Maturar, Dissipar Água

### Desviar
- ID: desviar
- Dificuldade: D
- Pagina: 67
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: —
- Pre-requisitos: Encantar

### Desviar Energia
- ID: desviar_energia
- Dificuldade: D
- Pagina: 73
- Classe: Bloqueio
- Escola(s): Fogo
- Duracao: Instant.
- Energia: 1
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, Moldar Fogo

### Desviar Projétil
- ID: desviar_projetil
- Dificuldade: D
- Pagina: 143
- Classe: Bloqueio
- Escola(s): Desloc., Proteção
- Duracao: Instant.
- Energia: 1
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Aporte

### Desviar Teleporte
- ID: desviar_teleporte
- Dificuldade: MD
- Pagina: 84
- Classe: Bloq./R-Mágica
- Escola(s): Portal, Deslocamento
- Duracao: Instant.
- Energia: Varia
- Tempo de Operacao: 2 seg.
- Pre-requisitos: AM3, Rastrear Teleporte

### Desvitalizar Ar
- ID: desvitalizar_ar
- Dificuldade: D
- Pagina: 25
- Classe: Área
- Escola(s): Ar
- Duracao: Varia
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Dissipar Ar

### Detectar Ilusão
- ID: detectar_ilusao
- Dificuldade: D
- Pagina: 97
- Classe: Informação
- Escola(s): Ilusão
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Ilusão Simples

### Detectar Pontos Fracos
- ID: detectar_pontos_fracos
- Dificuldade: D
- Pagina: 116
- Classe: Informação
- Escola(s): Quebrar e Consertar
- Duracao: Instant.
- Energia: 1#
- Tempo de Operacao: 2 seg.
- Pre-requisitos: 1 mágica de cada um dos quatro elementos

### Detecção de Magia
- ID: deteccao_de_magia
- Dificuldade: D
- Pagina: 101
- Classe: Comum
- Escola(s): Reconhecimento
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 5 seg.
- Pre-requisitos: AM1

### Detecção de Veneno
- ID: deteccao_de_veneno
- Dificuldade: D
- Pagina: 166
- Classe: Área/Informação
- Escola(s): Proteção, Cura
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Percepção do Perigo ou Testar Alimento

### Deteriorar
- ID: deteriorar
- Dificuldade: D
- Pagina: 77
- Classe: Comum
- Escola(s): Alimentos
- Duracao: Perm.
- Energia: 1/refeição
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Testar Alimento

### Deturpar
- ID: deturpar
- Dificuldade: D
- Pagina: 172
- Classe: Comum/R-Vont
- Escola(s): Som
- Duracao: 1 min.
- Energia: 04/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Vozes

### Devolver Projétil
- ID: devolver_projetil
- Dificuldade: D
- Pagina: 168
- Classe: Bloqueio
- Escola(s): Proteção
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Apanhar Projétil

### Diagrama/NT
- ID: diagramant
- Dificuldade: D
- Pagina: 177
- Classe: Informação
- Escola(s): Tecnológica, Reconhecimento
- Duracao: 1 min.
- Energia: 5/M#
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Identificar Função, História

### Disfarce Ilusório
- ID: disfarce_ilusorio
- Dificuldade: D
- Pagina: 96
- Classe: Comum
- Escola(s): Ilusão
- Duracao: Varia
- Energia: 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Ilusão Simples

### Dissipar Ar
- ID: dissipar_ar
- Dificuldade: D
- Pagina: 24
- Classe: Área
- Escola(s): Ar
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Criar Ar

### Dissipar Água
- ID: dissipar_agua
- Dificuldade: D
- Pagina: 185
- Classe: Área
- Escola(s): Água
- Duracao: Perm.
- Energia: 3/I
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Criar Água

### Dom da Escrita
- ID: dom_da_escrita
- Dificuldade: MD
- Pagina: 46
- Classe: Comum
- Escola(s): Comunicação
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Requisitar Idioma, 3 idiomas Com Sotaque

### Dom das Línguas
- ID: dom_das_linguas
- Dificuldade: MD
- Pagina: 46
- Classe: Comum
- Escola(s): Comunicação
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Requisitar Idioma, 3 idiomas Com Sotaque

### Dominar Animal
- ID: dominar_animal
- Dificuldade: D
- Pagina: 30
- Classe: Comum/Bloqueio/R-IQ
- Escola(s): Animais
- Duracao: Indef.
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Acalmar Animal

### Domo Absoluto
- ID: domo_absoluto
- Dificuldade: D
- Pagina: 170
- Classe: Área
- Escola(s): Proteção
- Duracao: 1 min.
- Energia: 06/04
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, Domo de Força, Escudo Antimágica

### Domo Atmosférico
- ID: domo_atmosferico
- Dificuldade: D
- Pagina: 169
- Classe: Área
- Escola(s): Proteção, Ar
- Duracao: 6 horas
- Energia: 4/M
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Purificar o Ar, Domo Climático

### Domo Climático
- ID: domo_climatico
- Dificuldade: D
- Pagina: 169
- Classe: Área
- Escola(s): Proteção, Clima
- Duracao: 6 horas
- Energia: 03/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: 2 máscaras de cada um dos 4 elementos

### Domo de Força
- ID: domo_de_forca
- Dificuldade: D
- Pagina: 170
- Classe: Área
- Escola(s): Proteção
- Duracao: 10 min.
- Energia: 03/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Domo Climático, Aporte

### Doppelgänger
- ID: doppelganger
- Dificuldade: MD
- Pagina: 62
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.#
- Energia: 1000
- Tempo de Operacao: —
- Pre-requisitos: AM3, Golem, História, Escravizar

### Dor
- ID: dor
- Dificuldade: D
- Pagina: 36
- Classe: Comum/R-HT
- Escola(s): Corpo
- Duracao: 1 seg.
- Energia: 2
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Espasmo

### Drenar Aptidão Mágica
- ID: drenar_aptidao_magica
- Dificuldade: MD
- Pagina: 130
- Classe: Comum/ R-Vont+AM
- Escola(s): Metamágica
- Duracao: Perm.
- Energia: 30
- Tempo de Operacao: 10 min.
- Pre-requisitos: AM3, Suspender Aptidão Mágica

### Drenar Mana
- ID: drenar_mana
- Dificuldade: MD
- Pagina: 127
- Classe: Área
- Escola(s): Metamágica
- Duracao: Perm.
- Energia: 10
- Tempo de Operacao: 1 hora
- Pre-requisitos: Anular Mágica, Suspender Mana

### Duplicar
- ID: duplicar
- Dificuldade: MD
- Pagina: 98
- Classe: Comum
- Escola(s): Ilusão
- Duracao: Indef.#
- Energia: 3/2,5 kg
- Tempo de Operacao: 5 seg.=custo
- Pre-requisitos: Criar Objeto, Copiar

### Ecos do Passado
- ID: ecos_do_passado
- Dificuldade: D
- Pagina: 107
- Classe: Comum
- Escola(s): Reconhecimento, Som
- Duracao: Perm.
- Energia: 2/2#
- Tempo de Operacao: 10 seg.
- Pre-requisitos: AM2, História, Vozes

### Efigie
- ID: efigie
- Dificuldade: MD
- Pagina: 71
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 1000
- Tempo de Operacao: —
- Pre-requisitos: Encantar

### Eliminar
- ID: eliminar
- Dificuldade: D
- Pagina: 62
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 100
- Tempo de Operacao: —
- Pre-requisitos: Encantar

### Eliminar Odor
- ID: eliminar_odor
- Dificuldade: D
- Pagina: 24
- Classe: Comum
- Escola(s): Ar
- Duracao: 1 hora
- Energia: 02/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Purificar o Ar

### Eloquência
- ID: eloquencia
- Dificuldade: D
- Pagina: 174
- Classe: Comum
- Escola(s): Som
- Duracao: 1 min.
- Energia: 03/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Vozes, Controle de Emoção

### Embriaguez
- ID: embriaguez
- Dificuldade: D
- Pagina: 136
- Classe: Comum/R-Vont
- Escola(s): Mente
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Tolice, Inabilidade

### Emudecer
- ID: emudecer
- Dificuldade: D
- Pagina: 38
- Classe: Comum/R-HT
- Escola(s): Corpo
- Duracao: 10 seg.
- Energia: 03/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Espasmo

### Encantamento Temporário
- ID: encantamento_temporario
- Dificuldade: D
- Pagina: 56
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Indef.#
- Energia: Varia
- Tempo de Operacao: —
- Pre-requisitos: Encantar

### Encantar
- ID: encantar
- Dificuldade: MD
- Pagina: 56
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: Varia
- Pre-requisitos: AM2, 1 mágica em 10 escolas diferentes

### Enclausuramento Arbóreo
- ID: enclausuramento_arboreo
- Dificuldade: D
- Pagina: 165
- Classe: Comum/R-HT
- Escola(s): Plantas
- Duracao: Indef.#
- Energia: 8#
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM2, Atravessar Madeira

### Encobrir
- ID: encobrir
- Dificuldade: D
- Pagina: 61
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 50#
- Tempo de Operacao: —
- Pre-requisitos: Encantar, Criar Objeto, Reduzir o Peso

### Encolher
- ID: encolher
- Dificuldade: MD
- Pagina: 42
- Classe: Comum
- Escola(s): Corpo
- Duracao: 1 hora
- Energia: 2/-1 MT/I
- Tempo de Operacao: 5 seg.
- Pre-requisitos: AM2, Alterar Corpo

### Encolher Objeto
- ID: encolher_objeto
- Dificuldade: MD
- Pagina: 120
- Classe: Comum
- Escola(s): Quebrar e Consertar
- Duracao: 1 hora
- Energia: Varia
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Contrair Objeto

### Encolher Outro
- ID: encolher_outro
- Dificuldade: MD
- Pagina: 42
- Classe: Comum/R-HT
- Escola(s): Corpo
- Duracao: 1 hora
- Energia: 2/-1 MT/I
- Tempo de Operacao: 10 seg.
- Pre-requisitos: AM3, Encolher

### Endurecer
- ID: endurecer
- Dificuldade: D
- Pagina: 117
- Classe: Comum/R-Especial
- Escola(s): Quebrar e Consertar
- Duracao: 10 min.
- Energia: 1 por 0,5 kg/M#
- Tempo de Operacao: 2 seg./0,5 kg Remendar
- Pre-requisitos: —

### Energização
- ID: energizacao
- Dificuldade: D
- Pagina: 57
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: —
- Pre-requisitos: Encantar, Recuperar Energia

### Enfeitiçar
- ID: enfeiticar
- Dificuldade: MD
- Pagina: 60
- Classe: Encant./R-Especial
- Escola(s): Encantamento
- Duracao: Perm.#
- Energia: 200x custo da mágica
- Tempo de Operacao: —
- Pre-requisitos: Malefício

### Enfraquecer
- ID: enfraquecer
- Dificuldade: D
- Pagina: 116
- Classe: Comum
- Escola(s): Quebrar e Consertar
- Duracao: Perm.
- Energia: 2 a 6
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Detectar Pontos Fracos

### Enfraquecer Sangue
- ID: enfraquecer_sangue
- Dificuldade: D
- Pagina: 40
- Classe: Comum/R-HT
- Escola(s): Corpo, Necromancia
- Duracao: 1 dia
- Energia: 09/05
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Enjoo ou Roubar Vitalidade

### Enfraquecer Vontade
- ID: enfraquecer_vontade
- Dificuldade: D
- Pagina: 136
- Classe: Comum/R-Vont
- Escola(s): Mente
- Duracao: 1 min.
- Energia: 2/pt/M
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, Tolice

### Enjoo
- ID: enjoo
- Dificuldade: D
- Pagina: 138
- Classe: Comum/R-HT
- Escola(s): Mente, Corpo
- Duracao: 1 min.
- Energia: 03/03
- Tempo de Operacao: 4 seg.
- Pre-requisitos: Embriaguez ou Pestilência

### Ensebar
- ID: ensebar
- Dificuldade: D
- Pagina: 142
- Classe: Área
- Escola(s): Deslocamento
- Duracao: 10 min.
- Energia: 3/I
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Apressar

### Ensurdecer
- ID: ensurdecer
- Dificuldade: D
- Pagina: 38
- Classe: Comum/R-HT
- Escola(s): Corpo
- Duracao: 10 seg.
- Energia: 03/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: 2 mágicas de Som, Espasmo

### Entalhar
- ID: entalhar
- Dificuldade: D
- Pagina: 97
- Classe: Área/R-Vont
- Escola(s): Ilusão, Quebrar e Consertar
- Duracao: 1 min.
- Energia: 2/I
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Ilusão Simples, Copiar

### Entrelaçamento
- ID: entrelacamento
- Dificuldade: D
- Pagina: 162
- Classe: Área
- Escola(s): Plantas
- Duracao: 1 min.
- Energia: 1 ou 2#/M
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Crescimento de Plantas

### Envelhecer
- ID: envelhecer
- Dificuldade: MD
- Pagina: 154
- Classe: Comum/R-HT
- Escola(s): Necromancia
- Duracao: Perm.
- Energia: 10 a 50
- Tempo de Operacao: 1 min.
- Pre-requisitos: Rejuvenescere, ou 6 mágicas de Necromancia

### Envenenar Alimento
- ID: envenenar_alimento
- Dificuldade: D
- Pagina: 78
- Classe: Comum
- Escola(s): Alimentos
- Duracao: Perm.
- Energia: 3 por refeição
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Purificar o Alimento, Deteriorar

### Equilíbrio
- ID: equilibrio
- Dificuldade: D
- Pagina: 39
- Classe: Comum
- Escola(s): Corpo
- Duracao: 1 min.
- Energia: 05/03
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Graça

### Escalada
- ID: escalada
- Dificuldade: D
- Pagina: 35
- Classe: Comum
- Escola(s): Corpo
- Duracao: 1 min.
- Energia: 1 a 3/I
- Tempo de Operacao: 1 seg.
- Pre-requisitos: —

### Escamotear Informação
- ID: escamotear_informacao
- Dificuldade: D
- Pagina: 123
- Classe: Comum/R-Especial
- Escola(s): Metamágica
- Duracao: 10 horas
- Energia: 04/02
- Tempo de Operacao: 10 seg.
- Pre-requisitos: AM2, Percepção de Observação, Ilusão Simples

### Esconder
- ID: esconder
- Dificuldade: D
- Pagina: 113
- Classe: Comum
- Escola(s): Luz e Trevas
- Duracao: 1 hora
- Energia: 1 a 5/I
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Nublar ou Esquecimento

### Escravizar
- ID: escravizar
- Dificuldade: MD
- Pagina: 141
- Classe: Comum/R-Vont
- Escola(s): Mente
- Duracao: Perm.
- Energia: 30
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Subjugar, Telepatia

### Escriba
- ID: escriba
- Dificuldade: D
- Pagina: 174
- Classe: Comum
- Escola(s): Som
- Duracao: 1 min.
- Energia: 03/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Vozes, Objeto Dançante, 1 idioma Com Sotaque

### Escriba Musical
- ID: escriba_musical
- Dificuldade: D
- Pagina: 174
- Classe: Comum
- Escola(s): Som
- Duracao: 1 min.
- Energia: 3/1#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Escriba

### Escudo
- ID: escudo
- Dificuldade: D
- Pagina: 167
- Classe: Comum
- Escola(s): Proteção
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2

### Escudo Antimágica
- ID: escudo_antimagica
- Dificuldade: D
- Pagina: 124
- Classe: Área
- Escola(s): Metamágica
- Duracao: 1 min.
- Energia: 03/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, Resguardar, Resistência à Magia

### Escudo Antiprojéteis
- ID: escudo_antiprojeteis
- Dificuldade: D
- Pagina: 168
- Classe: Comum
- Escola(s): Proteção
- Duracao: 1 min.
- Energia: 05/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Aporte ou Escudo

### Escudo Antiteleporte
- ID: escudo_antiteleporte
- Dificuldade: D
- Pagina: 170
- Classe: Área
- Escola(s): Proteção, Portal
- Duracao: 1 hora
- Energia: 1/I#
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Sentinela mais Escudo Antimágica ou Teleporte

### Escudo Dançante
- ID: escudo_dancante
- Dificuldade: D
- Pagina: 67
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 250/0,5 kg#
- Tempo de Operacao: —
- Pre-requisitos: Encantar, Objeto Dançante

### Escudo Protetor
- ID: escudo_protetor
- Dificuldade: D
- Pagina: 67
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: —
- Pre-requisitos: Encantar, Graça

### Escurecer
- ID: escurecer
- Dificuldade: D
- Pagina: 112
- Classe: Área
- Escola(s): Luz e Trevas
- Duracao: Varia
- Energia: Varia
- Tempo de Operacao: Varia
- Pre-requisitos: Luz Contínua

### Escuridão
- ID: escuridao
- Dificuldade: D
- Pagina: 112
- Classe: Área
- Escola(s): Luz e Trevas
- Duracao: 1 min.
- Energia: 02/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Trevas

### Esfera de Gelo
- ID: esfera_de_gelo
- Dificuldade: D
- Pagina: 186
- Classe: Projétil
- Escola(s): Água
- Duracao: Instant.
- Energia: 1 a AM#
- Tempo de Operacao: 1 a 3 seg.
- Pre-requisitos: Moldar Água

### Espada Fiel
- ID: espada_fiel
- Dificuldade: D
- Pagina: 63
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 750/0,5 kg#
- Tempo de Operacao: —
- Pre-requisitos: Encantar, Aporte

### Espantar Espírito
- ID: espantar_espirito
- Dificuldade: D
- Pagina: 151
- Classe: Comum/R-Vont
- Escola(s): Necromancia
- Duracao: 10 seg.
- Energia: 4/2#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Medo, Percepção de Espíritos

### Espantar Zumbi
- ID: espantar_zumbi
- Dificuldade: D
- Pagina: 152
- Classe: Área
- Escola(s): Necromancia
- Duracao: 1 dia
- Energia: 5
- Tempo de Operacao: 4 seg.
- Pre-requisitos: Zumbi#

### Espasmo
- ID: espasmo
- Dificuldade: D
- Pagina: 35
- Classe: Comum/R-HT
- Escola(s): Corpo
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Coceira

### Espectro
- ID: espectro
- Dificuldade: MD
- Pagina: 160
- Classe: Encantamento/ R-HT
- Escola(s): Necromancia, Encantamento
- Duracao: Perm.
- Energia: 250 ou 500#
- Tempo de Operacao: Varia
- Pre-requisitos: AM3, IQ 13+, Encantar, Interromper Envelhecimento, Aprisionar Alma

### Espectro de Metal
- ID: espectro_de_metal
- Dificuldade: D
- Pagina: 54
- Classe: Comum/R-HT
- Escola(s): Terra
- Duracao: 1 min.
- Energia: 07/04
- Tempo de Operacao: 2 seg.
- Pre-requisitos: AM2, Atravessar Terra

### Espelho
- ID: espelho
- Dificuldade: D
- Pagina: 112
- Classe: Comum
- Escola(s): Luz e Trevas
- Duracao: 1 min.
- Energia: 02/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Cores

### Espiar Portal
- ID: espiar_portal
- Dificuldade: D
- Pagina: 85
- Classe: Comum
- Escola(s): Portal
- Duracao: 1 min.
- Energia: 04/04
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Localizar Portal

### Espírito de Caveira
- ID: espirito_de_caveira
- Dificuldade: D
- Pagina: 151
- Classe: Comum
- Escola(s): Necromancia
- Duracao: 24 horas
- Energia: 20
- Tempo de Operacao: 1 seg.
- Pre-requisitos: 4 outras mágicas Necromântica

### Esquecimento
- ID: esquecimento
- Dificuldade: D
- Pagina: 135
- Classe: Comum/R-Vont ou perícia
- Escola(s): Mente
- Duracao: 1 hora
- Energia: 03/03
- Tempo de Operacao: 10 seg.
- Pre-requisitos: AM1, Tolice

### Esquecimento Permanente
- ID: esquecimento_permanente
- Dificuldade: MD
- Pagina: 138
- Classe: Comum/R-Vont ou perícia
- Escola(s): Mente
- Duracao: Perm.
- Energia: 15
- Tempo de Operacao: 1 hora
- Pre-requisitos: AM2, Esquecimento, IQ 13+

### Estorvar
- ID: estorvar
- Dificuldade: D
- Pagina: 36
- Classe: Comum
- Escola(s): Corpo, Deslocamento
- Duracao: 1 min.
- Energia: 1 a 4/I
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Apressar ou Inabilidade

### Estragar
- ID: estragar
- Dificuldade: D
- Pagina: 118
- Classe: Comum
- Escola(s): Quebrar e Consertar
- Duracao: 1 min.#
- Energia: 2 por 0,5 kg/I
- Tempo de Operacao: 5 seg./0,5 kg
- Pre-requisitos: AM1, Enfraquecer, Deteriorar

### Estrondo
- ID: estrondo
- Dificuldade: D
- Pagina: 171
- Classe: Comum
- Escola(s): Som
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Som

### Estupidez
- ID: estupidez
- Dificuldade: MD
- Pagina: 137
- Classe: Comum/R-Vont
- Escola(s): Mente
- Duracao: 1 min.
- Energia: 08/04
- Tempo de Operacao: 5 seg.
- Pre-requisitos: AM2, Esquecimento

### Evisceração
- ID: evisceracao
- Dificuldade: MD
- Pagina: 154
- Classe: Comum/ R-HT ou IQ
- Escola(s): Necromancia
- Duracao: Instant.
- Energia: 10
- Tempo de Operacao: 5 seg.
- Pre-requisitos: AM3, Aporte, Roubar Vitalidade

### Evitar
- ID: evitar
- Dificuldade: D
- Pagina: 140
- Classe: Área
- Escola(s): Mente
- Duracao: 1 hora
- Energia: 03/03
- Tempo de Operacao: 1 min.
- Pre-requisitos: Esconder, Medo, Esquecimento

### Explodir
- ID: explodir
- Dificuldade: MD
- Pagina: 118
- Classe: Comum
- Escola(s): Quebrar e Consertar
- Duracao: Instant.
- Energia: 2 a 6
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, Fragmentar, Aporte

### Expulsar
- ID: expulsar
- Dificuldade: D
- Pagina: 156
- Classe: Especial/R-Vont
- Escola(s): Necromancia
- Duracao: Instant.
- Energia: Varia
- Tempo de Operacao: 5 seg.
- Pre-requisitos: AM1, 1 mágica em 10 escolas diferentes

### Extinguir Fogo
- ID: extinguir_fogo
- Dificuldade: D
- Pagina: 72
- Classe: Comum
- Escola(s): Fogo
- Duracao: Perm.
- Energia: 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Atear Fogo

### Extinguir Radiação
- ID: extinguir_radiacao
- Dificuldade: MD
- Pagina: 181
- Classe: Comum
- Escola(s): Tecnológica
- Duracao: Perm.
- Energia: 1/10 rad/h
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, Extinguir Fogo, Terra para Ar, Irradiar

### Extrair Corrente Elétrica/NT
- ID: extrair_corrente_eletricant
- Dificuldade: MD
- Pagina: 180
- Classe: Especial
- Escola(s): Tecnológica
- Duracao: 1 min.
- Energia: 0/1#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Roubar Corrente Elétrica, 2 mágicas em 10 escolas diferentes

### Faca Alada
- ID: faca_alada
- Dificuldade: D
- Pagina: 145
- Classe: Projétil
- Escola(s): Deslocamento
- Duracao: Instant.
- Energia: 1 por 0,5 kg#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Poltergeist

### Falar com Animais
- ID: falar_com_animais
- Dificuldade: D
- Pagina: 31
- Classe: Comum
- Escola(s): Animais
- Duracao: 1 min.
- Energia: 04/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Convocar Animais

### Falar com Máquinas/NT
- ID: falar_com_maquinasnt
- Dificuldade: D
- Pagina: 176
- Classe: Comum
- Escola(s): Tecnológica, Comunicação
- Duracao: 1 min.
- Energia: 05/03
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Convocar Máquina

### Falar com Plantas
- ID: falar_com_plantas
- Dificuldade: D
- Pagina: 164
- Classe: Comum
- Escola(s): Plantas
- Duracao: 1 min.
- Energia: 03/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, Percepção de Planta

### Fantasma
- ID: fantasma
- Dificuldade: MD
- Pagina: 97
- Classe: Área
- Escola(s): Ilusão
- Duracao: 1 min.
- Energia: 5/M#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, Ilusão Perfeita, Estorvar, Aporte

### Fascinar
- ID: fascinar
- Dificuldade: D
- Pagina: 135
- Classe: Comum ou Bloqueio/R-Vont
- Escola(s): Mente
- Duracao: Indef.#
- Energia: 4
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Torpor

### Feitiço
- ID: feitico
- Dificuldade: D
- Pagina: 57
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 200
- Tempo de Operacao: —
- Pre-requisitos: Encantar

### Fender
- ID: fender
- Dificuldade: MD
- Pagina: 117
- Classe: Comum
- Escola(s): Quebrar e Consertar
- Duracao: Instant.
- Energia: 1 por dado
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, Fragmentar

### Ferver Água
- ID: ferver_agua
- Dificuldade: D
- Pagina: 189
- Classe: Comum
- Escola(s): Água
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Moldar Água, Calor

### Flecha Mágica
- ID: flecha_magica
- Dificuldade: D
- Pagina: 65
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 30× custo da mágica
- Tempo de Operacao: —
- Pre-requisitos: Pedra Mágica

### Flecha Mágica Rápida
- ID: flecha_magica_rapida
- Dificuldade: D
- Pagina: 66
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: —
- Pre-requisitos: Velocidade, Flecha Mágica

### Flecha Mágica Vazia
- ID: flecha_magica_vazia
- Dificuldade: MD
- Pagina: 66
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 30× capacita.#
- Tempo de Operacao: —
- Pre-requisitos: Flecha Mágica

### Florescer
- ID: florescer
- Dificuldade: D
- Pagina: 162
- Classe: Área
- Escola(s): Plantas
- Duracao: 1 hora
- Energia: 2
- Tempo de Operacao: 5 min.
- Pre-requisitos: Crescimento de Plantas

### Fome
- ID: fome
- Dificuldade: D
- Pagina: 38
- Classe: Comum/R-HT
- Escola(s): Corpo, Alimentos
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 5 seg.
- Pre-requisitos: AM1, Debilitar, Deteriorar

### Forma de Planta
- ID: forma_de_planta
- Dificuldade: D
- Pagina: 164
- Classe: Especial
- Escola(s): Plantas
- Duracao: 1 hora
- Energia: 05/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, 6 mágicas sobre Plantas

### Forma de Planta em Outro
- ID: forma_de_planta_em_outro
- Dificuldade: D
- Pagina: 165
- Classe: Especial/R-Vont
- Escola(s): Plantas
- Duracao: 1 hora
- Energia: 05/02
- Tempo de Operacao: 30 seg.
- Pre-requisitos: AM2, Forma de Planta

### Fortalecer
- ID: fortalecer
- Dificuldade: D
- Pagina: 119
- Classe: Comum
- Escola(s): Quebrar e Consertar
- Duracao: 1 hora
- Energia: Varia
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Resistência a Danos

### Fortalecer Vontade
- ID: fortalecer_vontade
- Dificuldade: D
- Pagina: 136
- Classe: Comum
- Escola(s): Mente
- Duracao: 1 min.
- Energia: 1/ponto/M
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, 6 mágicas da Mente

### Fortificar
- ID: fortificar
- Dificuldade: D
- Pagina: 66
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: —
- Pre-requisitos: Encantar

### Força
- ID: forca
- Dificuldade: D
- Pagina: 37
- Classe: Comum
- Escola(s): Corpo
- Duracao: 1 min.
- Energia: 2 por ST+/I
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Conceder Energia

### Fragilidade
- ID: fragilidade
- Dificuldade: D
- Pagina: 37
- Classe: Comum/R-HT
- Escola(s): Corpo
- Duracao: 1 min.
- Energia: 2 po HT-/I#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Conceder Energia

### Fragmentar
- ID: fragmentar
- Dificuldade: MD
- Pagina: 116
- Classe: Comum
- Escola(s): Quebrar e Consertar
- Duracao: Instant.
- Energia: 1 a 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, Enfraquecer

### Frescor
- ID: frescor
- Dificuldade: D
- Pagina: 187
- Classe: Comum
- Escola(s): Água, Proteção
- Duracao: 1 hora
- Energia: 02/01
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Frio

### Frio
- ID: frio
- Dificuldade: D
- Pagina: 74
- Classe: Comum
- Escola(s): Fogo
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 1 min.
- Pre-requisitos: Calor

### Fumaça
- ID: fumaca
- Dificuldade: D
- Pagina: 73
- Classe: Área
- Escola(s): Fogo
- Duracao: 5 min.#
- Energia: 1/M
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Moldar Fogo, Extinguir Fogo

### Furacão
- ID: furacao
- Dificuldade: D
- Pagina: 25
- Classe: Área
- Escola(s): Ar
- Duracao: 1 min.#
- Energia: 2/M
- Tempo de Operacao: Instant.#
- Pre-requisitos: Moldar Ar

### Furtividade Mágica
- ID: furtividade_magica
- Dificuldade: D
- Pagina: 172
- Classe: Comum
- Escola(s): Som
- Duracao: 1 min.
- Energia: 03/02
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Quietude

### Fusão com Arma
- ID: fusao_com_arma
- Dificuldade: MD
- Pagina: 119
- Classe: Comum/R-HT#
- Escola(s): Quebrar e Consertar
- Duracao: 1 min.
- Energia: 08/04
- Tempo de Operacao: 5 seg.
- Pre-requisitos: AM2, Aporte, 6 mágicas de Quebrar e Consertar incl. Remodelar

### Fúria
- ID: furia
- Dificuldade: D
- Pagina: 134
- Classe: Comum/R-Vont
- Escola(s): Mente
- Duracao: 10 min.#
- Energia: 03/02
- Tempo de Operacao: 4 seg.
- Pre-requisitos: Bravura

### Geada
- ID: geada
- Dificuldade: D
- Pagina: 193
- Classe: Área
- Escola(s): Clima, Água
- Duracao: Indef.
- Energia: 1
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Criar Água ou Frio

### Geladura
- ID: geladura
- Dificuldade: D
- Pagina: 189
- Classe: Comum/R-HT
- Escola(s): Água
- Duracao: Perm.
- Energia: 1 a 3
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Geada, Congelar

### Gema de Energia
- ID: gema_de_energia
- Dificuldade: D
- Pagina: 69
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 20
- Tempo de Operacao: —
- Pre-requisitos: Encantar

### Gema de Energia de Uma Só Escola
- ID: gema_de_energia_de_uma_so_escola
- Dificuldade: D
- Pagina: 70
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Indef.
- Energia: 12
- Tempo de Operacao: —
- Pre-requisitos: Encantar

### Geovisão
- ID: geovisao
- Dificuldade: D
- Pagina: 51
- Classe: Comum
- Escola(s): Terra, Reconhecimento
- Duracao: 30 seg.
- Energia: 2/10 m#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Moldar Terra

### Gerar Odor
- ID: gerar_odor
- Dificuldade: D
- Pagina: 24
- Classe: Área
- Escola(s): Ar
- Duracao: 1 hora
- Energia: 1
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Eliminar Odor

### Gira-Lâminas
- ID: giralaminas
- Dificuldade: D
- Pagina: 168
- Classe: Comum/ R-Especial
- Escola(s): Proteção
- Duracao: 1 min.
- Energia: 02/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Escudo ou Girar Lâmina

### Girar Lâmina
- ID: girar_lamina
- Dificuldade: D
- Pagina: 167
- Classe: Bloqueio/R-DX
- Escola(s): Proteção
- Duracao: Instant.
- Energia: 1
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Aporte ou Espasmo

### Golem
- ID: golem
- Dificuldade: MD
- Pagina: 59
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: Varia
- Pre-requisitos: Encantar, Moldar Terra, Animar#

### Golpe Distante
- ID: golpe_distante
- Dificuldade: D
- Pagina: 144
- Classe: Comum
- Escola(s): Deslocamento
- Duracao: 5 seg.
- Energia: 03/03
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM2, Aporte

### Granizo
- ID: granizo
- Dificuldade: D
- Pagina: 195
- Classe: Área
- Escola(s): Clima, Água
- Duracao: 1 min.
- Energia: 1/5/I#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Neve

### Graça
- ID: graca
- Dificuldade: D
- Pagina: 37
- Classe: Comum
- Escola(s): Corpo
- Duracao: 1 min.
- Energia: 4 por DX+/I
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Inabilidade

### Guarda-Chuva
- ID: guardachuva
- Dificuldade: D
- Pagina: 185
- Classe: Comum
- Escola(s): Água, Proteção
- Duracao: 10 min.
- Energia: 01/01
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Moldar Água ou Escudo

### Guarda-Mágica
- ID: guardamagica
- Dificuldade: MD
- Pagina: 127
- Classe: Comum/ R-Especial
- Escola(s): Metamágica
- Duracao: 10 horas
- Energia: 1 a 3/I#
- Tempo de Operacao: Varia
- Pre-requisitos: Anular Mágica

### Guia
- ID: guia
- Dificuldade: D
- Pagina: 105
- Classe: Informação
- Escola(s): Reconhecimento
- Duracao: Instant.
- Energia: 4
- Tempo de Operacao: 10 seg.
- Pre-requisitos: AM1, IQ 12+, 2 mágicas Localizar

### Géiser
- ID: geiser
- Dificuldade: MD
- Pagina: 190
- Classe: Área
- Escola(s): Água
- Duracao: 1 seg.
- Energia: 05/02
- Tempo de Operacao: 5 seg.
- Pre-requisitos: 6 mágicas da Água incl. Criar Nascente e quaisquer 4 mágicas da Terra ou Fogo

### História
- ID: historia
- Dificuldade: D
- Pagina: 106
- Classe: Informação
- Escola(s): Reconhecimento
- Duracao: Instant.
- Energia: Varia
- Tempo de Operacao: min.=custo
- Pre-requisitos: Rastrear

### História Antiga
- ID: historia_antiga
- Dificuldade: D
- Pagina: 106
- Classe: Informação
- Escola(s): Reconhecimento
- Duracao: Instant.
- Energia: Varia
- Tempo de Operacao: seg.=custo
- Pre-requisitos: História

### Homúnculo
- ID: homunculo
- Dificuldade: D
- Pagina: 70
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 800
- Tempo de Operacao: —
- Pre-requisitos: Encantar, Transmissão de Pensamento

### Hora Certa
- ID: hora_certa
- Dificuldade: D
- Pagina: 100
- Classe: Informação
- Escola(s): Reconhecimento
- Duracao: Instant.
- Energia: 1
- Tempo de Operacao: 1 seg.
- Pre-requisitos: —

### Identificar Função/NT
- ID: identificar_funcaont
- Dificuldade: D
- Pagina: 176
- Classe: Informação/ R-Mágica
- Escola(s): Tecnológica
- Duracao: Instant.
- Energia: 8
- Tempo de Operacao: 10 min.
- Pre-requisitos: Localizar Máquina

### Identificar Metal
- ID: identificar_metal
- Dificuldade: D
- Pagina: 182
- Classe: Informação
- Escola(s): Tecnológica
- Duracao: Instant.
- Energia: 1
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Localizar Terra

### Identificar Mágica
- ID: identificar_magica
- Dificuldade: D
- Pagina: 102
- Classe: Informação
- Escola(s): Reconhecimento
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Detecção de Magia

### Identificar Planta
- ID: identificar_planta
- Dificuldade: D
- Pagina: 161
- Classe: Informação
- Escola(s): Plantas
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Localizar Planta

### Identificar Plástico
- ID: identificar_plastico
- Dificuldade: D
- Pagina: 182
- Classe: Informação
- Escola(s): Tecnológica
- Duracao: Instant.
- Energia: 1
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Localizar Plástico

### Ilusão Complexa
- ID: ilusao_complexa
- Dificuldade: D
- Pagina: 96
- Classe: Área
- Escola(s): Ilusão
- Duracao: 1 min.
- Energia: 2/M
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Som, Ilusão Simples

### Ilusão Perfeita
- ID: ilusao_perfeita
- Dificuldade: D
- Pagina: 96
- Classe: Área
- Escola(s): Ilusão
- Duracao: 1 min.
- Energia: 3/M#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, Ilusão Complexa

### Ilusão Simples
- ID: ilusao_simples
- Dificuldade: D
- Pagina: 95
- Classe: Área
- Escola(s): Ilusão
- Duracao: 1 min.
- Energia: 1/M
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Não pode ser cego, IQ 11+

### Imagens do Passado
- ID: imagens_do_passado
- Dificuldade: D
- Pagina: 107
- Classe: Comum
- Escola(s): Reconhecimento, Luz e Trevas
- Duracao: 1 min.
- Energia: 3/3#
- Tempo de Operacao: 10 seg.
- Pre-requisitos: AM2, História, Ilusão Simples

### Imitar Voz
- ID: imitar_voz
- Dificuldade: D
- Pagina: 172
- Classe: Comum/R-HT
- Escola(s): Som
- Duracao: 1 min.
- Energia: 03/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Vozes

### Imunidade a Encantamento
- ID: imunidade_a_encantamento
- Dificuldade: D
- Pagina: 58
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: —
- Pre-requisitos: Qualquer Encantamento de Limitação

### Imunidade a Relâmpagos
- ID: imunidade_a_relampagos
- Dificuldade: D
- Pagina: 196
- Classe: Comum
- Escola(s): Clima, Ar, Proteção
- Duracao: 1 min.
- Energia: 02/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: 6 mágicas do Ar

### Imunidade a Veneno
- ID: imunidade_a_veneno
- Dificuldade: D
- Pagina: 91
- Classe: Comum
- Escola(s): Cura, Proteção
- Duracao: 1 hora
- Energia: 04/03
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Vigor

### Imunidade a Ácido
- ID: imunidade_a_acido
- Dificuldade: D
- Pagina: 190
- Classe: Comum
- Escola(s): Água, Proteção
- Duracao: 1 min.
- Energia: 2/M#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Criar Ácido

### Imunidade ao Fogo
- ID: imunidade_ao_fogo
- Dificuldade: D
- Pagina: 74
- Classe: Comum
- Escola(s): Fogo
- Duracao: 1 min.
- Energia: 2/1#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Resistência ao Fogo

### Imunidade ao Frio
- ID: imunidade_ao_frio
- Dificuldade: D
- Pagina: 74
- Classe: Comum
- Escola(s): Fogo
- Duracao: 1 min.
- Energia: 02/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Calor

### Imunidade ao Som
- ID: imunidade_ao_som
- Dificuldade: D
- Pagina: 173
- Classe: Comum
- Escola(s): Som, Proteção
- Duracao: 1 min.
- Energia: 02/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: 4 mágicas de Som

### Imunidade à Doenças
- ID: imunidade_a_doencas
- Dificuldade: D
- Pagina: 90
- Classe: Comum
- Escola(s): Cura, Proteção
- Duracao: 1 hora
- Energia: 04/03
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Remover Infecção ou Vigor

### Imunidade à Dor
- ID: imunidade_a_dor
- Dificuldade: D
- Pagina: 38
- Classe: Comum
- Escola(s): Corpo
- Duracao: 1 min.
- Energia: 04/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, Dor

### Imunidade à Pressão
- ID: imunidade_a_pressao
- Dificuldade: D
- Pagina: 169
- Classe: Comum
- Escola(s): Proteção
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Domo Climático

### Imunidade à Radiação
- ID: imunidade_a_radiacao
- Dificuldade: D
- Pagina: 182
- Classe: Comum
- Escola(s): Tecnológica, Proteção
- Duracao: 1 min.
- Energia: Varia#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: 3 mágicas de Radiação

### Imunidade à Sujeira
- ID: imunidade_a_sujeira
- Dificuldade: D
- Pagina: 116
- Classe: Comum
- Escola(s): Quebrar e Consertar
- Duracao: 10 min.
- Energia: 02/01
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Limpar

### Imunidade à Água
- ID: imunidade_a_agua
- Dificuldade: D
- Pagina: 186
- Classe: Comum
- Escola(s): Água, Proteção
- Duracao: 1 min.
- Energia: 02/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Guarda-Chuva ou Moldar Água e Dissipar Água

### Inabilidade
- ID: inabilidade
- Dificuldade: D
- Pagina: 36
- Classe: Comum/R-HT
- Escola(s): Corpo
- Duracao: 1 min.
- Energia: 1 a 5/M
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Espasmos

### Infravisão
- ID: infravisao
- Dificuldade: D
- Pagina: 111
- Classe: Comum
- Escola(s): Luz e Trevas
- Duracao: 1 min.
- Energia: 03/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Visão Aguda ou 5 mágicas de Luz

### Iniciativa
- ID: iniciativa
- Dificuldade: D
- Pagina: 97
- Classe: Área
- Escola(s): Ilusão
- Duracao: Indef.#
- Energia: Varia
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Auto-Suficiência, Sabedoria

### Insignificância
- ID: insignificancia
- Dificuldade: D
- Pagina: 48
- Classe: Comum/R-Espec.
- Escola(s): Comunicação
- Duracao: 1 hora
- Energia: 04/04
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Persuasão, Evitar

### Interromper Envelhecimento
- ID: interromper_envelhecimento
- Dificuldade: MD
- Pagina: 94
- Classe: Comum
- Escola(s): Cura
- Duracao: 1 mês
- Energia: 20
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, 5 mágicas de Cura

### Intervalo
- ID: intervalo
- Dificuldade: MD
- Pagina: 87
- Classe: Área
- Escola(s): Portal
- Duracao: Instant.#
- Energia: 5
- Tempo de Operacao: 5 min.
- Pre-requisitos: AM3, Acelerar Tempo

### Invisibilidade
- ID: invisibilidade
- Dificuldade: D
- Pagina: 114
- Classe: Comum
- Escola(s): Luz e Trevas
- Duracao: 1 min.
- Energia: 05/03
- Tempo de Operacao: 3 seg.
- Pre-requisitos: 6 mágicas de Luz incl. Nublar

### Irradiar
- ID: irradiar
- Dificuldade: D
- Pagina: 181
- Classe: Área
- Escola(s): Tecnológica
- Duracao: 1 hora
- Energia: 1/10 rads/h/M
- Tempo de Operacao: 1 seg.
- Pre-requisitos: 2 mágicas da Terra, 2 mágicas do Fogo

### Jato de Ar
- ID: jato_de_ar
- Dificuldade: D
- Pagina: 24
- Classe: Comum
- Escola(s): Ar
- Duracao: Instant.
- Energia: 03/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Moldar Ar

### Jato de Areia
- ID: jato_de_areia
- Dificuldade: D
- Pagina: 52
- Classe: Comum
- Escola(s): Terra
- Duracao: Instant.
- Energia: 03/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Criar Terra

### Jato de Chamas
- ID: jato_de_chamas
- Dificuldade: D
- Pagina: 73
- Classe: Comum
- Escola(s): Fogo
- Duracao: Instant.
- Energia: 03/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Criar Fogo, Moldar Fogo

### Jato de Lama
- ID: jato_de_lama
- Dificuldade: D
- Pagina: 52
- Classe: Comum
- Escola(s): Terra, Água
- Duracao: Instant.
- Energia: 1 a 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Jato de Areia e Criar Água ou Criar Terra e Jato de Água

### Jato de Luz
- ID: jato_de_luz
- Dificuldade: D
- Pagina: 112
- Classe: Comum
- Escola(s): Luz e Trevas
- Duracao: 1 min.
- Energia: 02/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Luz Contínua ou Moldar Luz

### Jato de Neve
- ID: jato_de_neve
- Dificuldade: D
- Pagina: 189
- Classe: Comum
- Escola(s): Água
- Duracao: Instant.
- Energia: 1 a 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Jato de Água, Congelar

### Jato de Radiação
- ID: jato_de_radiacao
- Dificuldade: D
- Pagina: 182
- Classe: Comum
- Escola(s): Tecnológica
- Duracao: Instant.
- Energia: 03/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Irradiar, Resistência à Radiação

### Jato de Som
- ID: jato_de_som
- Dificuldade: D
- Pagina: 173
- Classe: Comum
- Escola(s): Som
- Duracao: Instant.
- Energia: 04/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Voz Ampliada

### Jato de Vapor
- ID: jato_de_vapor
- Dificuldade: D
- Pagina: 191
- Classe: Comum
- Escola(s): Água
- Duracao: Instant.
- Energia: 1 a 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Jato de Água, Ferver Água

### Jato de Ácido
- ID: jato_de_acido
- Dificuldade: D
- Pagina: 192
- Classe: Comum
- Escola(s): Água
- Duracao: 1 seg.
- Energia: 1 a 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, Jato de Água, Criar Ácido

### Jato de Água
- ID: jato_de_agua
- Dificuldade: D
- Pagina: 187
- Classe: Comum
- Escola(s): Água
- Duracao: Instant.
- Energia: 1 a 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Moldar Água

### Jornada Rápida
- ID: jornada_rapida
- Dificuldade: MD
- Pagina: 82
- Classe: Especial
- Escola(s): Portal, Deslocamento
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 5 seg.
- Pre-requisitos: AM3, Teleporte ou Viagem no Tempo

### Juramento
- ID: juramento
- Dificuldade: D
- Pagina: 138
- Classe: Comum/R-Espec.
- Escola(s): Mente
- Duracao: Perm.
- Energia: 4
- Tempo de Operacao: 1 min.
- Pre-requisitos: AM1, Controle de Emoção

### Lampejo
- ID: lampejo
- Dificuldade: D
- Pagina: 112
- Classe: Comum
- Escola(s): Luz e Trevas
- Duracao: Instant.
- Energia: 4
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Luz Contínua

### Lealdade
- ID: lealdade
- Dificuldade: D
- Pagina: 136
- Classe: Comum/R-Vont
- Escola(s): Mente
- Duracao: 1 hora
- Energia: 2/2#
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Bravura, 2 outras mágicas de Controle da Mente

### Leitura da Mente
- ID: leitura_da_mente
- Dificuldade: D
- Pagina: 46
- Classe: Comum/R-Vont
- Escola(s): Comunicação
- Duracao: 1 min.
- Energia: 04/02
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Percepção de Veracidade ou Requisitar Idioma

### Leitura do Corpo
- ID: leitura_do_corpo
- Dificuldade: D
- Pagina: 88
- Classe: Informação/R-Vont
- Escola(s): Cura
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 30 seg.
- Pre-requisitos: Percepção de Vida ou Despertar

### Levitação
- ID: levitacao
- Dificuldade: D
- Pagina: 143
- Classe: Comum/R-ST ou Vont
- Escola(s): Deslocamento
- Duracao: 1 min.
- Energia: 1 por 40 kg/M#
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Aporte

### Liberdade
- ID: liberdade
- Dificuldade: D
- Pagina: 148
- Classe: Comum
- Escola(s): Deslocamento, Proteção
- Duracao: 1 min.
- Energia: 2/pt/I
- Tempo de Operacao: 1 seg.
- Pre-requisitos: 3 mágicas de Corpo, 3 mágicas de Deslocamento, 2 mágicas de Proteção

### Lich
- ID: lich
- Dificuldade: MD
- Pagina: 159
- Classe: Encantamento
- Escola(s): Necromancia, Encantamento
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: Varia
- Pre-requisitos: AM3, IQ 13+, Encantar, Aprisionar Alma, Zumbi

### Limite
- ID: limite
- Dificuldade: D
- Pagina: 68
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 200
- Tempo de Operacao: —
- Pre-requisitos: Encantar

### Limpar
- ID: limpar
- Dificuldade: D
- Pagina: 116
- Classe: Área
- Escola(s): Quebrar e Consertar
- Duracao: Perm.
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Restaurar

### Limpar a Caça
- ID: limpar_a_caca
- Dificuldade: D
- Pagina: 78
- Classe: Comum
- Escola(s): Alimentos
- Duracao: Perm.
- Energia: 2
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Purificar o Alimento

### Localizador
- ID: localizador
- Dificuldade: D
- Pagina: 105
- Classe: Informação
- Escola(s): Reconhecimento
- Duracao: Instant.
- Energia: 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, IQ 12+, 2 mágicas Localizar

### Localizar Alimento
- ID: localizar_alimento
- Dificuldade: D
- Pagina: 77
- Classe: Informação
- Escola(s): Alimentos
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: —

### Localizar Animais
- ID: localizar_animais
- Dificuldade: D
- Pagina: 32
- Classe: Informação
- Escola(s): Animais
- Duracao: Instant.
- Energia: 3#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Localizador ou Convocar Animal e 2 mágicas Localizar

### Localizar Ar
- ID: localizar_ar
- Dificuldade: D
- Pagina: 23
- Classe: Informação
- Escola(s): Ar
- Duracao: Instant.
- Energia: 1
- Tempo de Operacao: 1 seg.
- Pre-requisitos: —

### Localizar Combustível/NT
- ID: localizar_combustivelnt
- Dificuldade: D
- Pagina: 179
- Classe: Informação
- Escola(s): Tecnológica
- Duracao: Instant.
- Energia: 3
- Tempo de Operacao: 10 seg.
- Pre-requisitos: —

### Localizar Corrente Elétrica/NT
- ID: localizar_corrente_eletricant
- Dificuldade: D
- Pagina: 179
- Classe: Informação
- Escola(s): Tecnológica
- Duracao: Instant.
- Energia: 3
- Tempo de Operacao: 10 seg.
- Pre-requisitos: —

### Localizar Costa
- ID: localizar_costa
- Dificuldade: D
- Pagina: 184
- Classe: Informação
- Escola(s): Água
- Duracao: Instant.
- Energia: 3
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Localizar Água

### Localizar Fogo
- ID: localizar_fogo
- Dificuldade: D
- Pagina: 72
- Classe: Informação
- Escola(s): Fogo
- Duracao: Instant.
- Energia: 1
- Tempo de Operacao: 1 seg.
- Pre-requisitos: —

### Localizar Magia
- ID: localizar_magia
- Dificuldade: D
- Pagina: 102
- Classe: Informação
- Escola(s): Reconhecimento, Metamágica
- Duracao: Instant.
- Energia: 6
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Detecção de Magia

### Localizar Máquina/NT
- ID: localizar_maquinant
- Dificuldade: D
- Pagina: 175
- Classe: Informação
- Escola(s): Tecnológica
- Duracao: Instant.
- Energia: 3
- Tempo de Operacao: 10 seg.
- Pre-requisitos: —

### Localizar Passagem
- ID: localizar_passagem
- Dificuldade: D
- Pagina: 51
- Classe: Informação
- Escola(s): Terra
- Duracao: Instant.
- Energia: 3
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Localizar Terra

### Localizar Planta
- ID: localizar_planta
- Dificuldade: D
- Pagina: 161
- Classe: Informação
- Escola(s): Plantas
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: —

### Localizar Plástico
- ID: localizar_plastico
- Dificuldade: D
- Pagina: 182
- Classe: Informação
- Escola(s): Tecnológica
- Duracao: Instant.
- Energia: 3
- Tempo de Operacao: 10 seg.
- Pre-requisitos: —

### Localizar Portal
- ID: localizar_portal
- Dificuldade: D
- Pagina: 85
- Classe: Informação
- Escola(s): Portal
- Duracao: Instant.
- Energia: 3
- Tempo de Operacao: 10 seg.
- Pre-requisitos: AM2, Localizar Mágica, 1 mágica em 10 escolas diferentes

### Localizar Radiação
- ID: localizar_radiacao
- Dificuldade: D
- Pagina: 181
- Classe: Comum
- Escola(s): Tecnológica
- Duracao: Instant.
- Energia: 3
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Ver Radiação

### Localizar Terra
- ID: localizar_terra
- Dificuldade: D
- Pagina: 50
- Classe: Informação
- Escola(s): Terra
- Duracao: Instant.
- Energia: 3
- Tempo de Operacao: 10 seg.
- Pre-requisitos: —

### Localizar Água
- ID: localizar_agua
- Dificuldade: D
- Pagina: 184
- Classe: Informação
- Escola(s): Água
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: —

### Loquacidade
- ID: loquacidade
- Dificuldade: D
- Pagina: 141
- Classe: Comum/R-Vont
- Escola(s): Mente
- Duracao: 5 min.
- Energia: 02/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Sugestão

### Loucura
- ID: loucura
- Dificuldade: D
- Pagina: 136
- Classe: Comum/R-Vont-2
- Escola(s): Mente
- Duracao: 1 min.
- Energia: 04/02
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Esquecimento ou Embriaguez

### Loucura Permanente
- ID: loucura_permanente
- Dificuldade: MD
- Pagina: 139
- Classe: Comum/R-Vont-2
- Escola(s): Mente
- Duracao: Perm.
- Energia: 20
- Tempo de Operacao: 10 min.
- Pre-requisitos: AM2, Loucura, IQ 13+

### Luz
- ID: luz
- Dificuldade: D
- Pagina: 110
- Classe: Comum
- Escola(s): Luz e Trevas
- Duracao: 1 min.
- Energia: 01/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: —

### Luz Contínua
- ID: luz_continua
- Dificuldade: D
- Pagina: 110
- Classe: Comum
- Escola(s): Luz e Trevas
- Duracao: Varia
- Energia: Varia
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Luz

### Luz Mágica
- ID: luz_magica
- Dificuldade: D
- Pagina: 113
- Classe: Comum
- Escola(s): Luz e Trevas
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Visão de Mágica, Luz

### Luz Mágica Contínua
- ID: luz_magica_continua
- Dificuldade: D
- Pagina: 113
- Classe: Comum
- Escola(s): Luz e Trevas
- Duracao: Varia
- Energia: Varia
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Luz Mágica, Luz Contínua

### Luz Solar
- ID: luz_solar
- Dificuldade: D
- Pagina: 114
- Classe: Área
- Escola(s): Luz e Trevas
- Duracao: 1 min.
- Energia: 02/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, Cores, Brilho

### Luz Solar Contínua
- ID: luz_solar_continua
- Dificuldade: D
- Pagina: 114
- Classe: Área
- Escola(s): Luz e Trevas
- Duracao: Varia
- Energia: 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Luz Solar

### Madeira Essencial
- ID: madeira_essencial
- Dificuldade: D
- Pagina: 164
- Classe: Comum
- Escola(s): Plantas
- Duracao: Perm.
- Energia: 8
- Tempo de Operacao: 30 seg.
- Pre-requisitos: 6 mágicas sobre Plantas

### Magreza
- ID: magreza
- Dificuldade: MD
- Pagina: 43
- Classe: Comum/R-HT
- Escola(s): Corpo
- Duracao: 10 min.
- Energia: 06/06
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM2, Terra para Ar, Dissipar Água, 4 mágicas de Corpo incl. Fome

### Maldição
- ID: maldicao
- Dificuldade: D
- Pagina: 129
- Classe: Comum
- Escola(s): Metamágica
- Duracao: Especial
- Energia: Varia
- Tempo de Operacao: Varia
- Pre-requisitos: AM2, 2 mágicas em 10 escolas diferentes#

### Malefício
- ID: maleficio
- Dificuldade: MD
- Pagina: 60
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Indef.#
- Energia: 250
- Tempo de Operacao: —
- Pre-requisitos: Encantar, Localizador

### Manipular
- ID: manipular
- Dificuldade: D
- Pagina: 145
- Classe: Comum
- Escola(s): Deslocamento
- Duracao: 1 min.
- Energia: 4/3#
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Serralheiro

### Manter Mágica
- ID: manter_magica
- Dificuldade: MD
- Pagina: 128
- Classe: Especial
- Escola(s): Metamágica
- Duracao: Indef.#
- Energia: Varia
- Tempo de Operacao: 2 seg.#
- Pre-requisitos: Conexão

### Marca Mística
- ID: marca_mistica
- Dificuldade: D
- Pagina: 119
- Classe: Comum/R-Espec.
- Escola(s): Quebrar e Consertar
- Duracao: Indef.#
- Energia: 3
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Tingir, Rastrear

### Marcha Acelerada
- ID: marcha_acelerada
- Dificuldade: D
- Pagina: 144
- Classe: Comum
- Escola(s): Deslocamento
- Duracao: 1 dia de marcha
- Energia: 4#
- Tempo de Operacao: 1 min.
- Pre-requisitos: AM1, Apressar

### Marcha Lenta
- ID: marcha_lenta
- Dificuldade: D
- Pagina: 143
- Classe: Comum/R-ST
- Escola(s): Deslocamento
- Duracao: 1 dia de marcha
- Energia: 3
- Tempo de Operacao: 1 min.
- Pre-requisitos: AM1, Inabilidade ou Debilitar

### Maré
- ID: mare
- Dificuldade: D
- Pagina: 194
- Classe: Especial/Área
- Escola(s): Clima, Água
- Duracao: 1 hora
- Energia: 1/30/I
- Tempo de Operacao: 1 min.
- Pre-requisitos: 8 mágicas da Água

### Materializar
- ID: materializar
- Dificuldade: D
- Pagina: 150
- Classe: Especial
- Escola(s): Necromancia
- Duracao: 1 min.
- Energia: 05/05
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Convocar Espírito

### Maturar
- ID: maturar
- Dificuldade: D
- Pagina: 78
- Classe: Comum
- Escola(s): Alimentos
- Duracao: Perm.
- Energia: 1 por 0,5 kg
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Deteriorar ou Temperar

### Mau Cheiro
- ID: mau_cheiro
- Dificuldade: D
- Pagina: 24
- Classe: Área
- Escola(s): Ar
- Duracao: 5 min.
- Energia: 1
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Purificar o Ar

### Medidas
- ID: medidas
- Dificuldade: D
- Pagina: 100
- Classe: Área/Informação
- Escola(s): Reconhecimento
- Duracao: Instant.
- Energia: 1
- Tempo de Operacao: 1 seg.
- Pre-requisitos: —

### Medo
- ID: medo
- Dificuldade: D
- Pagina: 134
- Classe: Área/R-Vont
- Escola(s): Mente
- Duracao: 10 min.
- Energia: 1
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Percepção de Emoção ou Empatia

### Memorizar
- ID: memorizar
- Dificuldade: D
- Pagina: 105
- Classe: Comum
- Escola(s): Reconhecimento, Mente
- Duracao: 1 dia#
- Energia: 3
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Sabedoria ou 6 mágicas de Reconhecimento

### Memória Falsa
- ID: memoria_falsa
- Dificuldade: D
- Pagina: 139
- Classe: Comum/R-Vont
- Escola(s): Mente
- Duracao: Varia
- Energia: Varia
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Esquecimento, outras 6 mágicas de Controle da Mente

### Mensagem
- ID: mensagem
- Dificuldade: D
- Pagina: 174
- Classe: Comum/R-Mágica
- Escola(s): Som, Comunicação
- Duracao: Varia
- Energia: 1/15 seg.
- Tempo de Operacao: Varia
- Pre-requisitos: Voz Amplificada, Localizador

### Metalovisão
- ID: metalovisao
- Dificuldade: D
- Pagina: 183
- Classe: Comum
- Escola(s): Tecnológica, Reconhecimento
- Duracao: 30 seg.
- Energia: 2/5 m/I
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Moldar Metal

### Metamorfose
- ID: metamorfose
- Dificuldade: MD
- Pagina: 32
- Classe: Especial
- Escola(s): Animais
- Duracao: 1 hora
- Energia: Varia
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM1, 6 outras mágicas

### Metamorfose Parcial
- ID: metamorfose_parcial
- Dificuldade: MD
- Pagina: 34
- Classe: Comum/R-Vont
- Escola(s): Animais
- Duracao: 1 hora
- Energia: Varia
- Tempo de Operacao: 10 seg.
- Pre-requisitos: AM3, Metamorfosear Outro, Alterar Corpo

### Metamorfose Permanente
- ID: metamorfose_permanente
- Dificuldade: MD
- Pagina: 33
- Classe: Comum
- Escola(s): Animais
- Duracao: Indef.
- Energia: Varia
- Tempo de Operacao: 1 min.
- Pre-requisitos: AM3, Metamorfose

### Metamorfose Superior
- ID: metamorfose_superior
- Dificuldade: MD
- Pagina: 34
- Classe: Especial
- Escola(s): Animais
- Duracao: 1 min.
- Energia: 20/M#
- Tempo de Operacao: 5 seg.
- Pre-requisitos: AM3, Alterar Corpo, 4 Metamorfose, 10 outras mágicas

### Metamorfosear Outro
- ID: metamorfosear_outro
- Dificuldade: MD
- Pagina: 33
- Classe: Especial/R-Vont
- Escola(s): Animais
- Duracao: 1 hora
- Energia: Varia
- Tempo de Operacao: 30 seg.
- Pre-requisitos: AM2, Metamorfose para aquela forma

### Mira Rápida
- ID: mira_rapida
- Dificuldade: D
- Pagina: 65
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: —
- Pre-requisitos: Encantar, Graça

### Moldar Ar
- ID: moldar_ar
- Dificuldade: D
- Pagina: 24
- Classe: Comum
- Escola(s): Ar
- Duracao: 1 min.
- Energia: 1 a 10#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Criar Ar

### Moldar Fogo
- ID: moldar_fogo
- Dificuldade: D
- Pagina: 72
- Classe: Área
- Escola(s): Fogo
- Duracao: 1 min.
- Energia: 2/M
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Atear Fogo

### Moldar Luz
- ID: moldar_luz
- Dificuldade: D
- Pagina: 111
- Classe: Comum
- Escola(s): Luz e Trevas
- Duracao: 1 min.
- Energia: 02/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Luz

### Moldar Metal
- ID: moldar_metal
- Dificuldade: D
- Pagina: 182
- Classe: Comum
- Escola(s): Tecnológica
- Duracao: 1 min.
- Energia: 6/M#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, Moldar Terra ou 6 mágicas Tecnológicas

### Moldar Planta
- ID: moldar_planta
- Dificuldade: D
- Pagina: 161
- Classe: Comum
- Escola(s): Plantas
- Duracao: 1 min.
- Energia: 3/1#
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Identificar Planta

### Moldar Plástico
- ID: moldar_plastico
- Dificuldade: D
- Pagina: 183
- Classe: Comum
- Escola(s): Tecnológica
- Duracao: 1 min.
- Energia: 06/03
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, Moldar Planta ou 6 mágicas Tecnológicas

### Moldar Terra
- ID: moldar_terra
- Dificuldade: D
- Pagina: 50
- Classe: Comum
- Escola(s): Terra
- Duracao: 1 min.
- Energia: 1/0,75 m³/h
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Localizar Terra

### Moldar Trevas
- ID: moldar_trevas
- Dificuldade: D
- Pagina: 113
- Classe: Área
- Escola(s): Luz e Trevas
- Duracao: 1 min.
- Energia: 2/I#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Trevas

### Moldar Água
- ID: moldar_agua
- Dificuldade: D
- Pagina: 185
- Classe: Comum
- Escola(s): Água
- Duracao: 1 min.
- Energia: 1/1#
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Criar Água

### Morte Candente
- ID: morte_candente
- Dificuldade: MD
- Pagina: 76
- Classe: Toque/R-HT
- Escola(s): Fogo, Necromancia
- Duracao: 1 seg.
- Energia: 03/02
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM2, Calor, Enjoo,

### Morte Putrefata
- ID: morte_putrefata
- Dificuldade: MD
- Pagina: 154
- Classe: Comum/R-HT
- Escola(s): Necromancia
- Duracao: 1 seg.
- Energia: 03/02
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM2, Enjoo, Pestilência

### Muralha Absoluta
- ID: muralha_absoluta
- Dificuldade: D
- Pagina: 170
- Classe: Comum
- Escola(s): Proteção
- Duracao: 1 min.
- Energia: 4/metro/I
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Domo Absoluto, Muralha Mágica

### Muralha Mágica
- ID: muralha_magica
- Dificuldade: D
- Pagina: 124
- Classe: Comum/R-Mágica
- Escola(s): Metamágica
- Duracao: 1 min.
- Energia: 2/2#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Escudo Antimágica

### Muralha de Força
- ID: muralha_de_forca
- Dificuldade: D
- Pagina: 170
- Classe: Comum
- Escola(s): Proteção
- Duracao: 10 min.
- Energia: 2/metro/I
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Domo de Força

### Muralha de Luz
- ID: muralha_de_luz
- Dificuldade: D
- Pagina: 113
- Classe: Área
- Escola(s): Luz e Trevas
- Duracao: 1 min.
- Energia: 1 a 3/I
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Luz Contínua

### Muralha de Relâmpagos
- ID: muralha_de_relampagos
- Dificuldade: D
- Pagina: 197
- Classe: Comum
- Escola(s): Clima, Ar
- Duracao: 1 min.
- Energia: 2 a 6/I
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Relâmpago

### Muralha de Silêncio
- ID: muralha_de_silencio
- Dificuldade: D
- Pagina: 172
- Classe: Área
- Escola(s): Som
- Duracao: 1 min.
- Energia: 02/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Silêncio

### Muralha de Vento
- ID: muralha_de_vento
- Dificuldade: D
- Pagina: 25
- Classe: Área
- Escola(s): Ar
- Duracao: 1 min.
- Energia: 2/M
- Tempo de Operacao: Instant.#
- Pre-requisitos: Moldar Ar

### Muralha de Vidro
- ID: muralha_de_vidro
- Dificuldade: D
- Pagina: 103
- Classe: Comum
- Escola(s): Reconhecimento
- Duracao: 1 min.
- Energia: 04/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: 5 mágicas de Reconhecimento ou Geovisão

### Murchar Planta
- ID: murchar_planta
- Dificuldade: D
- Pagina: 163
- Classe: Área/R-HT
- Escola(s): Plantas
- Duracao: Perm.
- Energia: 2
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Secar Planta

### Mágica Penetrante
- ID: magica_penetrante
- Dificuldade: D
- Pagina: 123
- Classe: Comum
- Escola(s): Metamágica
- Duracao: Varia
- Energia: Varia
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Retardo, Detectar Pontos Fracos

### Mão Mágica
- ID: mao_magica
- Dificuldade: D
- Pagina: 104
- Classe: Comum
- Escola(s): Reconhecimento, Deslocamento
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Manipular, Tato Remoto

### Nadar
- ID: nadar
- Dificuldade: D
- Pagina: 147
- Classe: Comum
- Escola(s): Água, Deslocamento
- Duracao: 1 min.
- Energia: 06/03
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Moldar Água, Levitação

### Nariz Mágico
- ID: nariz_magico
- Dificuldade: D
- Pagina: 104
- Classe: Comum
- Escola(s): Reconhecimento, Alimentos
- Duracao: 1 min.
- Energia: 03/02
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Aporte, Paladar Remoto

### Nausear
- ID: nausear
- Dificuldade: D
- Pagina: 38
- Classe: Comum/R-HT
- Escola(s): Corpo
- Duracao: 10 seg.
- Energia: 2/I
- Tempo de Operacao: 1 seg.
- Pre-requisitos: 2 mágicas de Corpo incl. Perfume

### Neblina Mística
- ID: neblina_mistica
- Dificuldade: D
- Pagina: 168
- Classe: Área
- Escola(s): Proteção
- Duracao: 10 horas
- Energia: 1/I
- Tempo de Operacao: 5 min.
- Pre-requisitos: AM1 e Sentinela ou Escudo

### Neutralizar Veneno
- ID: neutralizar_veneno
- Dificuldade: D
- Pagina: 92
- Classe: Comum
- Escola(s): Cura
- Duracao: Perm.
- Energia: 5
- Tempo de Operacao: 30 seg.
- Pre-requisitos: Curar Doenças ou AM3 e Testar Alimento

### Neutralização Instantânea de Veneno
- ID: neutralizacao_instantanea_de_veneno
- Dificuldade: D
- Pagina: 92
- Classe: Comum
- Escola(s): Cura
- Duracao: Instant.
- Energia: 8
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, Neutralizar Veneno

### Neve
- ID: neve
- Dificuldade: D
- Pagina: 195
- Classe: Área
- Escola(s): Clima, Ar, Água
- Duracao: 1 hora
- Energia: 1/15#/I
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Nuvens, Geada

### Nevoeiro
- ID: nevoeiro
- Dificuldade: D
- Pagina: 193
- Classe: Área
- Escola(s): Clima, Água
- Duracao: 1 min.
- Energia: 2/M
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Moldar Água

### Nublar
- ID: nublar
- Dificuldade: D
- Pagina: 113
- Classe: Comum
- Escola(s): Luz e Trevas
- Duracao: 1 min.
- Energia: 1 a 5/I
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Trevas ou Escuridão

### Nuvem de Faíscas
- ID: nuvem_de_faiscas
- Dificuldade: D
- Pagina: 196
- Classe: Área
- Escola(s): Clima, Ar
- Duracao: 10 seg.
- Energia: 1 a 5/I
- Tempo de Operacao: 1 a 5 seg.
- Pre-requisitos: Moldar Ar, Relâmpago

### Nuvem de Fogo
- ID: nuvem_de_fogo
- Dificuldade: D
- Pagina: 75
- Classe: Área
- Escola(s): Fogo
- Duracao: 10 seg.
- Energia: 1 a 5/I
- Tempo de Operacao: 1 a 5 seg.
- Pre-requisitos: Moldar Ar, Bola de Fogo

### Nuvem de Pólen
- ID: nuvem_de_polen
- Dificuldade: D
- Pagina: 162
- Classe: Área/R-HT
- Escola(s): Plantas
- Duracao: 5 min.#
- Energia: 1
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Moldar Planta

### Nuvens
- ID: nuvens
- Dificuldade: D
- Pagina: 194
- Classe: Área
- Escola(s): Clima, Ar
- Duracao: 10 min.
- Energia: 1/20/I
- Tempo de Operacao: 10 seg.
- Pre-requisitos: 2 mágicas da Água, 2 mágicas do Ar

### Nó
- ID: no
- Dificuldade: D
- Pagina: 117
- Classe: Comum
- Escola(s): Quebrar e Consertar
- Duracao: Indef.#
- Energia: 2
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Endurecer

### Objeto Dançante
- ID: objeto_dancante
- Dificuldade: D
- Pagina: 144
- Classe: Comum
- Escola(s): Deslocamento
- Duracao: 1 hora
- Energia: 04/02
- Tempo de Operacao: 10 seg.
- Pre-requisitos: AM2, Aporte

### Ocultar
- ID: ocultar
- Dificuldade: D
- Pagina: 162
- Classe: Área
- Escola(s): Plantas
- Duracao: 1 min.
- Energia: Varia#
- Tempo de Operacao: 4 seg.
- Pre-requisitos: Crescimento de Plantas

### Ocultar Emoção
- ID: ocultar_emocao
- Dificuldade: D
- Pagina: 45
- Classe: Comum
- Escola(s): Comunicação
- Duracao: 1 hora
- Energia: 02/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Percepção de Emoção

### Ocultar Magia
- ID: ocultar_magia
- Dificuldade: D
- Pagina: 122
- Classe: Comum
- Escola(s): Metamágica
- Duracao: 10 horas
- Energia: 1 a 5/I#
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Detecção de Magia

### Ocultar Objeto
- ID: ocultar_objeto
- Dificuldade: D
- Pagina: 86
- Classe: Comum
- Escola(s): Portal
- Duracao: 1 hora
- Energia: 1/0,5 kg/I
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Encobrir, Teleporte

### Ocultar Pensamentos
- ID: ocultar_pensamentos
- Dificuldade: D
- Pagina: 46
- Classe: Comum
- Escola(s): Comunicação
- Duracao: 10 min.
- Energia: 03/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Percepção de Veracidade ou Ocultar Emoção

### Ocultar Rastros
- ID: ocultar_rastros
- Dificuldade: D
- Pagina: 162
- Classe: Comum
- Escola(s): Plantas
- Duracao: 1 min.
- Energia: 02/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Curar Planta

### Olhar de Relâmpago
- ID: olhar_de_relampago
- Dificuldade: MD
- Pagina: 163
- Classe: Comum
- Escola(s): Clima, Ar
- Duracao: 1 min.
- Energia: 1 a 4
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Relâmpago, Imunidade a Relâmpagos

### Olho Mágico
- ID: olho_magico
- Dificuldade: D
- Pagina: 104
- Classe: Comum
- Escola(s): Reconhecimento
- Duracao: 1 min.
- Energia: 04/02
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Aporte, Visão Aguçada

### Olho Mágico Invisível
- ID: olho_magico_invisivel
- Dificuldade: D
- Pagina: 104
- Classe: Comum
- Escola(s): Reconhecimento
- Duracao: 1 min.
- Energia: 05/03
- Tempo de Operacao: 4 seg.
- Pre-requisitos: Olho Mágico, Invisibilidade

### Olhos do Falcão
- ID: olhos_do_falcao
- Dificuldade: D
- Pagina: 111
- Classe: Comum
- Escola(s): Luz e Trevas
- Duracao: 1 min.
- Energia: 2/nível/M#
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Visão Aguçada ou 5 mágicas de Luz; não pode ter Cegueira ou Disopia

### Ondas
- ID: ondas
- Dificuldade: D
- Pagina: 194
- Classe: Especial/Área
- Escola(s): Clima, Água
- Duracao: 1 hora
- Energia: 1/60/I
- Tempo de Operacao: 1 min.
- Pre-requisitos: Moldar Água

### Orientação
- ID: orientacao
- Dificuldade: D
- Pagina: 105
- Classe: Informação
- Escola(s): Reconhecimento
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1

### Ouvido Mágico
- ID: ouvido_magico
- Dificuldade: D
- Pagina: 173
- Classe: Comum
- Escola(s): Som
- Duracao: 1 min.
- Energia: 04/03
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Aporte, Audição Remota

### Ouvido Mágico Invisível
- ID: ouvido_magico_invisivel
- Dificuldade: D
- Pagina: 173
- Classe: Comum
- Escola(s): Som
- Duracao: 1 min.
- Energia: 05/03
- Tempo de Operacao: 4 seg.
- Pre-requisitos: Ouvido Mágico, Invisibilidade

### Paladar Remoto
- ID: paladar_remoto
- Dificuldade: D
- Pagina: 104
- Classe: Comum
- Escola(s): Alimentos, Reconhecimento
- Duracao: 1 min.
- Energia: 03/01
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM1, sem Anosmia, Localizar Alimento, Localizar Ar

### Pane/NT
- ID: panent
- Dificuldade: D
- Pagina: 176
- Classe: Comum/R-HT
- Escola(s): Tecnológica
- Duracao: Instant.
- Energia: 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Controle de Máquina

### Paralisar Membro
- ID: paralisar_membro
- Dificuldade: D
- Pagina: 40
- Classe: Toque/R-HT
- Escola(s): Corpo
- Duracao: 1 min.
- Energia: 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, 5 mágicas do Corpo incl. Inabilidade

### Paralisia Total
- ID: paralisia_total
- Dificuldade: D
- Pagina: 40
- Classe: Toque/R-HT
- Escola(s): Corpo
- Duracao: 1 min.
- Energia: 5
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Paralisar Membro

### Passageiro Interno
- ID: passageiro_interno
- Dificuldade: D
- Pagina: 31
- Classe: Comum
- Escola(s): Animais
- Duracao: 1 min.
- Energia: 04/01
- Tempo de Operacao: 3 seg.
- Pre-requisitos: 2 mágicas de Controle#

### Passageiro da Alma
- ID: passageiro_da_alma
- Dificuldade: D
- Pagina: 49
- Classe: Comum/R-Vont
- Escola(s): Comunicação
- Duracao: 1 min.
- Energia: 05/02
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Leitura da Mente

### Passo Leve
- ID: passo_leve
- Dificuldade: D
- Pagina: 144
- Classe: Comum
- Escola(s): Deslocamento
- Duracao: 10 min.
- Energia: 4/1#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Aporte, Moldar Terra

### Pedra Mágica
- ID: pedra_magica
- Dificuldade: D
- Pagina: 57
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Varia
- Energia: 20x custo da mágica
- Tempo de Operacao: —
- Pre-requisitos: Encantar, Retardar

### Pedra da Alma
- ID: pedra_da_alma
- Dificuldade: MD
- Pagina: 159
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 500
- Tempo de Operacao: —
- Pre-requisitos: AM3, Encantar, Aprisionar Alma

### Pedra de Mana
- ID: pedra_de_mana
- Dificuldade: MD
- Pagina: 57
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Indef.
- Energia: 5
- Tempo de Operacao: —
- Pre-requisitos: Encantar

### Pedra para Carne
- ID: pedra_para_carne
- Dificuldade: D
- Pagina: 52
- Classe: Comum
- Escola(s): Terra
- Duracao: Instant.
- Energia: 10
- Tempo de Operacao: 5 seg.
- Pre-requisitos: AM2, Pedra para Terra, Carne para Pedra

### Pedra para Terra
- ID: pedra_para_terra
- Dificuldade: D
- Pagina: 51
- Classe: Comum
- Escola(s): Terra
- Duracao: Perm.
- Energia: 6/0,75 m³
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Terra para Pedra ou 4 mágicas da Terra

### Pegadas Falsas
- ID: pegadas_falsas
- Dificuldade: D
- Pagina: 163
- Classe: Comum/R-Vont
- Escola(s): Plantas
- Duracao: 1 min.
- Energia: 02/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Moldar Planta, Moldar Terra

### Pentagrama
- ID: pentagrama
- Dificuldade: D
- Pagina: 125
- Classe: Especial
- Escola(s): Metamágica
- Duracao: Perm.
- Energia: 1/0,1 m²#
- Tempo de Operacao: 1/0,1 m²
- Pre-requisitos: Escudo Antimágica

### Pequeno Desejo
- ID: pequeno_desejo
- Dificuldade: MD
- Pagina: 61
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Especial
- Energia: 180
- Tempo de Operacao: —
- Pre-requisitos: Encantar

### Percepção de Emoção
- ID: percepcao_de_emocao
- Dificuldade: D
- Pagina: 45
- Classe: Comum
- Escola(s): Comunicação
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Percepção de Inimigos

### Percepção de Espíritos
- ID: percepcao_de_espiritos
- Dificuldade: D
- Pagina: 150
- Classe: Informação/Área
- Escola(s): Necromancia
- Duracao: Instant.
- Energia: 1#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Visão da Morte, ou Percepção de Vida e AM1

### Percepção de Inimigos
- ID: percepcao_de_inimigos
- Dificuldade: D
- Pagina: 45
- Classe: Informação/Área
- Escola(s): Comunicação
- Duracao: Instant.
- Energia: 2#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: —

### Percepção de Magia
- ID: percepcao_de_magia
- Dificuldade: D
- Pagina: 101
- Classe: Informação
- Escola(s): Reconhecimento
- Duracao: 1 min.
- Energia: 03/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Detecção de Magia

### Percepção de Mana
- ID: percepcao_de_mana
- Dificuldade: D
- Pagina: 101
- Classe: Informação
- Escola(s): Reconhecimento
- Duracao: Instant.
- Energia: 3
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Detecção de Magia

### Percepção de Observação
- ID: percepcao_de_observacao
- Dificuldade: D
- Pagina: 166
- Classe: Área
- Escola(s): Proteção
- Duracao: 1 hora
- Energia: 1/M#
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Percepção do Perigo ou Resguardar

### Percepção de Planta
- ID: percepcao_de_planta
- Dificuldade: D
- Pagina: 162
- Classe: Comum/R-Ocultar Rastros
- Escola(s): Plantas
- Duracao: 1 min.
- Energia: 03/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Alarme Florestal, Ocultar Rastros

### Percepção de Veracidade
- ID: percepcao_de_veracidade
- Dificuldade: D
- Pagina: 47
- Classe: Informação/R-Vont
- Escola(s): Comunicação
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Percepção de Emoção

### Percepção de Vida
- ID: percepcao_de_vida
- Dificuldade: D
- Pagina: 45
- Classe: Informação/Área
- Escola(s): Comunicação
- Duracao: Instant.
- Energia: 1#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: —

### Percepção do Perigo
- ID: percepcao_do_perigo
- Dificuldade: D
- Pagina: 166
- Classe: Informação
- Escola(s): Proteção
- Duracao: Instant.
- Energia: 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Percepção de Inimigos ou Noção do Perigo

### Perfume
- ID: perfume
- Dificuldade: D
- Pagina: 35
- Classe: Comum/R-HT
- Escola(s): Corpo
- Duracao: 10 min.
- Energia: 02/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Gerar Odor

### Pergaminho Mágico
- ID: pergaminho_magico
- Dificuldade: D
- Pagina: 57
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Varia
- Energia: Especial
- Tempo de Operacao: dias=custo
- Pre-requisitos: AM1, 1 idioma com Sotaque

### Persuasão
- ID: persuasao
- Dificuldade: D
- Pagina: 48
- Classe: Comum/R-Vont
- Escola(s): Comunicação
- Duracao: 1 min.
- Energia: 2xbónus#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Percepção de Emoção

### Pesadelo
- ID: pesadelo
- Dificuldade: D
- Pagina: 139
- Classe: Comum/R-Vont
- Escola(s): Mente
- Duracao: 1 hora
- Energia: 6
- Tempo de Operacao: 1 min.
- Pre-requisitos: AM2, Visão da Morte, Medo, Sono

### Pestilência
- ID: pestilencia
- Dificuldade: D
- Pagina: 152
- Classe: Comum
- Escola(s): Necromancia
- Duracao: Perm.
- Energia: 6
- Tempo de Operacao: 30 seg.
- Pre-requisitos: AM1, Roubar Vitalidade, Deteriorar

### Petrificação Parcial
- ID: petrificacao_parcial
- Dificuldade: MD
- Pagina: 52
- Classe: Comum/R-HT
- Escola(s): Terra
- Duracao: Perm.
- Energia: 12
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM2, Carne para Pedra

### Plastivisão
- ID: plastivisao
- Dificuldade: D
- Pagina: 183
- Classe: Comum
- Escola(s): Tecnológica, Reconhecimento
- Duracao: 30 seg.
- Energia: 2/5 m/I
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Moldar Plástico

### Poltergeist
- ID: poltergeist
- Dificuldade: D
- Pagina: 144
- Classe: Projetil/R-HT
- Escola(s): Deslocamento
- Duracao: Instant.
- Energia: 1 ou 2#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Aporte

### Possessão
- ID: possessao
- Dificuldade: MD
- Pagina: 49
- Classe: Comum/R-Vont
- Escola(s): Comunicação
- Duracao: 1 min.
- Energia: 10/04
- Tempo de Operacao: 1 min.
- Pre-requisitos: AM1, e Controle de Pessoa ou Possessão de Animais

### Possessão Permanente
- ID: possessao_permanente
- Dificuldade: MD
- Pagina: 49
- Classe: Comum/R-Vont
- Escola(s): Comunicação
- Duracao: Indef.
- Energia: 30
- Tempo de Operacao: 5 min.
- Pre-requisitos: AM3, Possessão

### Possessão Permanente de Animais
- ID: possessao_permanente_de_animais
- Dificuldade: MD
- Pagina: 31
- Classe: Comum/R-Vont
- Escola(s): Animais
- Duracao: Indef.
- Energia: 20
- Tempo de Operacao: 1 min.
- Pre-requisitos: AM2, Possessão de Animais

### Possessão Permanente de Máquina/NT
- ID: possessao_permanente_de_maquinant
- Dificuldade: MD
- Pagina: 176
- Classe: Comum/R-Vont
- Escola(s): Tecnológica
- Duracao: Indef.#
- Energia: 30
- Tempo de Operacao: 5 min.
- Pre-requisitos: AM3, Possessão de Máquina

### Possessão de Animais
- ID: possessao_de_animais
- Dificuldade: D
- Pagina: 31
- Classe: Comum/R-Vont
- Escola(s): Animais
- Duracao: 1 min.
- Energia: 06/02
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Passageiro Interno ou Possessão

### Possessão de Máquina/NT
- ID: possessao_de_maquinant
- Dificuldade: D
- Pagina: 176
- Classe: Comum/R-Vont
- Escola(s): Tecnológica
- Duracao: 1 min.
- Energia: 06/02
- Tempo de Operacao: 30 seg.
- Pre-requisitos: Controle de Máquina, Passageiro Interno ou Passageiro da Alma

### Precisão
- ID: precisao
- Dificuldade: D
- Pagina: 66
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: —
- Pre-requisitos: Encantar e 5 mágicas do Ar

### Prender Espírito
- ID: prender_espirito
- Dificuldade: MD
- Pagina: 153
- Classe: Comum/R-Vont
- Escola(s): Necromancia
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: 5 min.
- Pre-requisitos: Comandar Espírito, Aprisionar Alma

### Prender a Respiração
- ID: prender_a_respiracao
- Dificuldade: D
- Pagina: 89
- Classe: Comum
- Escola(s): Corpo
- Duracao: 1 min.
- Energia: 04/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, Vigor

### Presença
- ID: presenca
- Dificuldade: D
- Pagina: 48
- Classe: Comum/R-Espec.
- Escola(s): Comunicação
- Duracao: 1 hora
- Energia: 04/04
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Persuasão, Atrair

### Preservar Alimento
- ID: preservar_alimento
- Dificuldade: D
- Pagina: 77
- Classe: Comum
- Escola(s): Alimentos
- Duracao: 1 semana
- Energia: Especial
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Deteriorar

### Preservar Combustível/NT
- ID: preservar_combustivelnt
- Dificuldade: D
- Pagina: 179
- Classe: Comum
- Escola(s): Tecnológica
- Duracao: 1 semana
- Energia: 4/0,5 kg/M
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Testar Combustível

### Prever Movimento da Terra
- ID: prever_movimento_da_terra
- Dificuldade: D
- Pagina: 51
- Classe: Informação
- Escola(s): Terra
- Duracao: Instant.
- Energia: 2 por dia#
- Tempo de Operacao: Varia
- Pre-requisitos: 4 mágicas da Terra

### Previsão do Tempo
- ID: previsao_do_tempo
- Dificuldade: D
- Pagina: 193
- Classe: Informação
- Escola(s): Clima, Ar
- Duracao: Instant.
- Energia: Varia
- Tempo de Operacao: 5 seg.#
- Pre-requisitos: 4 mágicas do Ar

### Projeção da Mente
- ID: projecao_da_mente
- Dificuldade: D
- Pagina: 105
- Classe: Comum
- Escola(s): Reconhecimento
- Duracao: 1 min.
- Energia: 04/02
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Percepção de Espíritos, 4 mágicas de Reconhecimento

### Projeção de Sonho
- ID: projecao_de_sonho
- Dificuldade: D
- Pagina: 46
- Classe: Comum
- Escola(s): Comunicação, Mente
- Duracao: 1 min.
- Energia: 03/03
- Tempo de Operacao: 1 min.
- Pre-requisitos: Transmissão de Sonho

### Projéteis Congelantes
- ID: projeteis_congelantes
- Dificuldade: D
- Pagina: 186
- Classe: Comum
- Escola(s): Água
- Duracao: 1 min.
- Energia: 04/02
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Arma Congelante

### Projéteis Flamejantes
- ID: projeteis_flamejantes
- Dificuldade: D
- Pagina: 75
- Classe: Comum
- Escola(s): Fogo
- Duracao: 1 min.
- Energia: 4/2#
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Arma Flamejante

### Projéteis de Relâmpago
- ID: projeteis_de_relampago
- Dificuldade: D
- Pagina: 198
- Classe: Comum
- Escola(s): Clima, Ar
- Duracao: 1 min.
- Energia: 4/2#
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Arma de Relâmpago

### Projétil de Pedra
- ID: projetil_de_pedra
- Dificuldade: D
- Pagina: 52
- Classe: Projetil
- Escola(s): Terra
- Duracao: Instant.
- Energia: 1 a AM
- Tempo de Operacao: 1 a 3 seg.
- Pre-requisitos: Criar Terra

### Prontidão
- ID: prontidao
- Dificuldade: MD
- Pagina: 133
- Classe: Comum
- Escola(s): Mente
- Duracao: 10 min.
- Energia: 2 a 10/M
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Quaisquer 2 mágicas de Sentido Aguçado

### Proteger Animal
- ID: proteger_animal
- Dificuldade: D
- Pagina: 32
- Classe: Área
- Escola(s): Animais, Proteção
- Duracao: 1 min.
- Energia: 1/I
- Tempo de Operacao: 1 min.
- Pre-requisitos: Armadura, Sentinela, 3 mágicas sobre Animais

### Proteção
- ID: protecao
- Dificuldade: D
- Pagina: 122
- Classe: Bloq./R-Mágica
- Escola(s): Metamágica
- Duracao: Instant.
- Energia: 2 ou 3#
- Tempo de Operacao: nenhum
- Pre-requisitos: AM1

### Proteção Superior
- ID: protecao_superior
- Dificuldade: D
- Pagina: 122
- Classe: Bloq./R-Mágica
- Escola(s): Metamágica
- Duracao: Instant.
- Energia: 1 por alvo#
- Tempo de Operacao: nenhum
- Pre-requisitos: AM2, Proteção

### Provocar Dormência
- ID: provocar_dormencia
- Dificuldade: D
- Pagina: 40
- Classe: Comum/R-HT
- Escola(s): Corpo
- Duracao: 10 seg.
- Energia: 03/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Imunidade à Dor

### Provocar Esterilidade
- ID: provocar_esterilidade
- Dificuldade: D
- Pagina: 41
- Classe: Comum/R-HT
- Escola(s): Corpo, Necromancia
- Duracao: Perm.
- Energia: 5
- Tempo de Operacao: 30 seg.
- Pre-requisitos: AM1, Roubar Vitalidade, Deteriorar

### Pré-História
- ID: prehistoria
- Dificuldade: D
- Pagina: 106
- Classe: Informação
- Escola(s): Reconhecimento
- Duracao: Instant.
- Energia: Varia
- Tempo de Operacao: hora=custo
- Pre-requisitos: História Antiga

### Pujança
- ID: pujanca
- Dificuldade: D
- Pagina: 65
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: —
- Pre-requisitos: Encantar, 5 mágicas da Terra

### Purificar
- ID: purificar
- Dificuldade: D
- Pagina: 94
- Classe: Comum/R-Espec.
- Escola(s): Cura
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Cura Superficial, Purificar a Terra

### Purificar a Terra
- ID: purificar_a_terra
- Dificuldade: D
- Pagina: 54
- Classe: Área
- Escola(s): Terra, Plantas
- Duracao: Perm.
- Energia: 2#
- Tempo de Operacao: 30 seg.
- Pre-requisitos: Criar Terra, Crescimento de Plantas

### Purificar a Água
- ID: purificar_a_agua
- Dificuldade: D
- Pagina: 184
- Classe: Especial
- Escola(s): Água
- Duracao: Perm.
- Energia: 1/4 litros
- Tempo de Operacao: 5–10 seg./ 4 litros#
- Pre-requisitos: Localizar Água

### Purificar o Alimento
- ID: purificar_o_alimento
- Dificuldade: D
- Pagina: 78
- Classe: Comum
- Escola(s): Alimentos
- Duracao: Perm.
- Energia: 1 por 0,5 kg
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Deteriorar

### Purificar o Ar
- ID: purificar_o_ar
- Dificuldade: D
- Pagina: 23
- Classe: Área
- Escola(s): Ar
- Duracao: Instant.
- Energia: 1
- Tempo de Operacao: 1 seg.
- Pre-requisitos: —

### Purificar o Combustível/NT
- ID: purificar_o_combustivelnt
- Dificuldade: D
- Pagina: 179
- Classe: Comum
- Escola(s): Tecnológica
- Duracao: Instant.
- Energia: 1#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Purificar a Água ou Deteriorar

### Puxar
- ID: puxar
- Dificuldade: D
- Pagina: 146
- Classe: Comum
- Escola(s): Deslocamento
- Duracao: 1 min.
- Energia: 1/2 ST/I
- Tempo de Operacao: 5 seg.
- Pre-requisitos: AM2, 4 mágicas de Deslocamento incl. Levitação

### Pânico
- ID: panico
- Dificuldade: D
- Pagina: 134
- Classe: Área/R-Vont
- Escola(s): Mente
- Duracao: 1 min.
- Energia: 04/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Medo

### Pés Plantados
- ID: pes_plantados
- Dificuldade: D
- Pagina: 36
- Classe: Comum/R-ST
- Escola(s): Corpo
- Duracao: 1 min.#
- Energia: 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Estorvar

### Quietude
- ID: quietude
- Dificuldade: D
- Pagina: 172
- Classe: Comum/R-Vont
- Escola(s): Som
- Duracao: 10 seg.#
- Energia: 02/01
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Silêncio

### Raio Solar
- ID: raio_solar
- Dificuldade: D
- Pagina: 114
- Classe: Projetil
- Escola(s): Luz e Trevas
- Duracao: Instant.
- Energia: 1 a AM#
- Tempo de Operacao: 1 a 3 seg.
- Pre-requisitos: 6 mágicas de Luz incl. Luz Solar

### Rastrear
- ID: rastrear
- Dificuldade: D
- Pagina: 106
- Classe: Comum
- Escola(s): Reconhecimento
- Duracao: Hora
- Energia: 03/01
- Tempo de Operacao: 1 min.
- Pre-requisitos: Localizador

### Rastrear Teleporte
- ID: rastrear_teleporte
- Dificuldade: D
- Pagina: 84
- Classe: Informação/ R-Mágica
- Escola(s): Portal, Deslocamento
- Duracao: Instant.
- Energia: 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Teleporte, Viagem no Tempo, ou Viagem Planar

### Recarregar Gema de Energia
- ID: recarregar_gema_de_energia
- Dificuldade: MD
- Pagina: 126
- Classe: Comum
- Escola(s): Metamágica
- Duracao: Perm.
- Energia: 3/ponto
- Tempo de Operacao: 10 min.
- Pre-requisitos: AM3, Gema de Energia, Conceder Energia

### Reconstruir Mágica
- ID: reconstruir_magica
- Dificuldade: D
- Pagina: 106
- Classe: Informação
- Escola(s): Reconhecimento
- Duracao: Instant.
- Energia: 3#
- Tempo de Operacao: 10 seg.
- Pre-requisitos: AM2, História, Identificar Mágica

### Reconstruir/NT
- ID: reconstruirnt
- Dificuldade: D
- Pagina: 177
- Classe: Comum
- Escola(s): Tecnológica, Quebrar e Consertar
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: Varia
- Pre-requisitos: AM3, Consertar, Criar Objeto, 3 mágicas de cada elemento#

### Recordar
- ID: recordar
- Dificuldade: D
- Pagina: 106
- Classe: Comum
- Escola(s): Reconhecimento, Mente
- Duracao: Dia#
- Energia: 4
- Tempo de Operacao: 10 seg.
- Pre-requisitos: AM2, Memorizar, Sabedoria

### Recordar Caminho
- ID: recordar_caminho
- Dificuldade: D
- Pagina: 107
- Classe: Comum
- Escola(s): Reconhecimento
- Duracao: Hora
- Energia: 03/01
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Orientação, Memorizar

### Recuperar Energia
- ID: recuperar_energia
- Dificuldade: D
- Pagina: 89
- Classe: Especial
- Escola(s): Cura
- Duracao: Especial
- Energia: nenhum
- Tempo de Operacao: Especial
- Pre-requisitos: AM1, Conceder Energia

### Reduzir Carga
- ID: reduzir_carga
- Dificuldade: D
- Pagina: 143
- Classe: Comum
- Escola(s): Deslocamento
- Duracao: 10 min.
- Energia: 3 ou 5/M#
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Aporte

### Reduzir o Peso
- ID: reduzir_o_peso
- Dificuldade: D
- Pagina: 67
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: —
- Pre-requisitos: Encantar

### Refletir
- ID: refletir
- Dificuldade: D
- Pagina: 122
- Classe: Bloq./R-Mágica
- Escola(s): Metamágica
- Duracao: Instant.
- Energia: 4 ou 6#
- Tempo de Operacao: nenhum
- Pre-requisitos: Proteção

### Refletir Olhar
- ID: refletir_olhar
- Dificuldade: MD
- Pagina: 168
- Classe: Bloqueio/R-Espec.
- Escola(s): Proteção
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Espelho

### Reflexo
- ID: reflexo
- Dificuldade: D
- Pagina: 132
- Classe: Especial
- Escola(s): Metamágica
- Duracao: Hora
- Energia: Varia
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Retardo, Proteção

### Reflexos
- ID: reflexos
- Dificuldade: D
- Pagina: 39
- Classe: Comum
- Escola(s): Corpo
- Duracao: 1 min.
- Energia: 05/03
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Graça, Apressar

### Regeneração
- ID: regeneracao
- Dificuldade: MD
- Pagina: 93
- Classe: Comum
- Escola(s): Cura
- Duracao: Perm.
- Energia: 20
- Tempo de Operacao: Especial#
- Pre-requisitos: AM2, Restauração

### Regeneração Instantânea
- ID: regeneracao_instantanea
- Dificuldade: MD
- Pagina: 93
- Classe: Comum
- Escola(s): Cura
- Duracao: Perm.
- Energia: 80
- Tempo de Operacao: Especial
- Pre-requisitos: AM3, Regeneração

### Regressão
- ID: regressao
- Dificuldade: D
- Pagina: 47
- Classe: Comum/R-Vont
- Escola(s): Comunicação
- Duracao: 1 seg.
- Energia: 5
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Sonda Mental, Transmissão de Pensamento

### Rejuvenescere
- ID: rejuvenescere
- Dificuldade: MD
- Pagina: 94
- Classe: Comum
- Escola(s): Cura
- Duracao: Especial
- Energia: 100
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM3, Interromper Envelhecimento

### Rejuvenescere Planta
- ID: rejuvenescere_planta
- Dificuldade: D
- Pagina: 163
- Classe: Comum
- Escola(s): Plantas
- Duracao: Perm.
- Energia: 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, Crescimento de Plantas

### Relâmpago
- ID: relampago
- Dificuldade: D
- Pagina: 196
- Classe: Projetil
- Escola(s): Clima, Ar
- Duracao: Instant.
- Energia: 1 a AM#
- Tempo de Operacao: 1 a 3 seg.
- Pre-requisitos: AM1, 6 mágicas do Ar

### Relâmpago Explosivo
- ID: relampago_explosivo
- Dificuldade: D
- Pagina: 196
- Classe: Projetil
- Escola(s): Clima, Ar
- Duracao: Instant.
- Energia: 2 a 2×AM#
- Tempo de Operacao: 1 a 3 seg.
- Pre-requisitos: Relâmpago

### Remendar
- ID: remendar
- Dificuldade: D
- Pagina: 116
- Classe: Comum
- Escola(s): Quebrar e Consertar
- Duracao: 10 min.
- Energia: 1 por 5 kg/M
- Tempo de Operacao: 4 seg./5 kg
- Pre-requisitos: Enfraquecer, Restaurar

### Remodelar
- ID: remodelar
- Dificuldade: D
- Pagina: 117
- Classe: Comum
- Escola(s): Quebrar e Consertar
- Duracao: 1 min.
- Energia: 06/03
- Tempo de Operacao: 10 seg.
- Pre-requisitos: AM1, Enfraquecer, Moldar Terra ou Moldar Planta

### Remover Aura
- ID: remover_aura
- Dificuldade: D
- Pagina: 127
- Classe: Comum/R-Vont#
- Escola(s): Metamágica
- Duracao: Perm.
- Energia: 5
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Anular Mágica, Aura

### Remover Encantamento
- ID: remover_encantamento
- Dificuldade: D
- Pagina: 58
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 100#
- Tempo de Operacao: Varia
- Pre-requisitos: Encantar

### Remover Infecção
- ID: remover_infeccao
- Dificuldade: D
- Pagina: 90
- Classe: Área
- Escola(s): Cura
- Duracao: Instant.
- Energia: 3
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Deteriorar, Limpar ou Curar Doenças

### Remover Maldição
- ID: remover_maldicao
- Dificuldade: D
- Pagina: 126
- Classe: Comum/R-Mágica
- Escola(s): Metamágica
- Duracao: Instant.
- Energia: 20
- Tempo de Operacao: 1 hora
- Pre-requisitos: AM2, Suspender Maldição ou 1 mágica de cada uma das 15 escolas

### Remover Reflexo
- ID: remover_reflexo
- Dificuldade: D
- Pagina: 113
- Classe: Comum/R-Vont
- Escola(s): Luz e Trevas
- Duracao: 1 min.
- Energia: 02/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Remover Sombra

### Remover Sombra
- ID: remover_sombra
- Dificuldade: D
- Pagina: 110
- Classe: Comum/R-Vont
- Escola(s): Luz e Trevas
- Duracao: 1 min.
- Energia: 02/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Luz

### Repelir
- ID: repelir
- Dificuldade: D
- Pagina: 147
- Classe: Comum
- Escola(s): Deslocamento
- Duracao: 1 min.
- Energia: 1/2 ST/I
- Tempo de Operacao: 5 seg.
- Pre-requisitos: AM2, 4 mágicas de Deslocamento incl. Levitação

### Repelir Animal
- ID: repelir_animal
- Dificuldade: D
- Pagina: 31
- Classe: Área/R-HT
- Escola(s): Animais
- Duracao: Hora
- Energia: Varia
- Tempo de Operacao: 10 seg.
- Pre-requisitos: 1 mágica de Controle#

### Repelir Espíritos
- ID: repelir_espiritos
- Dificuldade: D
- Pagina: 158
- Classe: Área/R-Vont
- Escola(s): Necromancia
- Duracao: Hora
- Energia: 4/M
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Expulsar, Espantar Espírito

### Repelir Híbridos
- ID: repelir_hibridos
- Dificuldade: MD
- Pagina: 31
- Classe: Área/R-HT
- Escola(s): Animais
- Duracao: 1 hora
- Energia: 06/03
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Controle de Híbrido

### Requisitar Idioma
- ID: requisitar_idioma
- Dificuldade: D
- Pagina: 46
- Classe: Comum
- Escola(s): Comunicação
- Duracao: 1 min.
- Energia: 03/01
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Conceder Idioma

### Requisitar Perícia
- ID: requisitar_pericia
- Dificuldade: D
- Pagina: 47
- Classe: Comum
- Escola(s): Comunicação
- Duracao: 1 min.
- Energia: 04/03
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Conceder Perícia

### Resfriar
- ID: resfriar
- Dificuldade: D
- Pagina: 195
- Classe: Área
- Escola(s): Clima, Ar
- Duracao: 1 hora
- Energia: 1/10/I
- Tempo de Operacao: 1 min.#
- Pre-requisitos: Frio, 4 mágicas do Ar

### Resguardar
- ID: resguardar
- Dificuldade: D
- Pagina: 121
- Classe: Comum
- Escola(s): Metamágica
- Duracao: 10 horas
- Energia: 03/01
- Tempo de Operacao: 5 seg.
- Pre-requisitos: AM1

### Resguardar Área
- ID: resguardar_area
- Dificuldade: D
- Pagina: 122
- Classe: Área
- Escola(s): Metamágica
- Duracao: 10 horas
- Energia: 03/02
- Tempo de Operacao: seg=custo
- Pre-requisitos: Resguardar

### Resistência a Choques
- ID: resistencia_a_choques
- Dificuldade: D
- Pagina: 118
- Classe: Comum
- Escola(s): Quebrar e Consertar
- Duracao: 1 hora
- Energia: 03/03
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Consertar, Fragmentar

### Resistência ao Fogo
- ID: resistencia_ao_fogo
- Dificuldade: D
- Pagina: 73
- Classe: Área
- Escola(s): Fogo
- Duracao: 1 dia
- Energia: 3#
- Tempo de Operacao: 5 min.
- Pre-requisitos: Extinguir Fogo

### Resistência à Magia
- ID: resistencia_a_magia
- Dificuldade: D
- Pagina: 123
- Classe: Com./R-Vont+AM
- Escola(s): Metamágica
- Duracao: 1 min.
- Energia: 1 a 5/I#
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM1, 1 mágica em 7 escolas diferentes

### Respirar Ar
- ID: respirar_ar
- Dificuldade: D
- Pagina: 26
- Classe: Comum
- Escola(s): Ar, Água
- Duracao: 1 min.
- Energia: 04/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Criar Água, Dissipar Ar

### Respiração
- ID: respiracao
- Dificuldade: D
- Pagina: 189
- Classe: Comum
- Escola(s): Água, Ar
- Duracao: 1 min.
- Energia: 04/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Criar Ar, Dissipar Água

### Ressurreição
- ID: ressurreicao
- Dificuldade: MD
- Pagina: 94
- Classe: Comum
- Escola(s): Cura, Necromancia
- Duracao: Perm.
- Energia: 300
- Tempo de Operacao: 2 horas
- Pre-requisitos: Regeneração Instantânea, Convocar Espírito

### Restaurar
- ID: restaurar
- Dificuldade: D
- Pagina: 116
- Classe: Comum
- Escola(s): Quebrar e Consertar
- Duracao: 10 min.
- Energia: 02/01
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Detectar Pontos Fracos ou Ilusão Simples

### Restaurar Audição
- ID: restaurar_audicao
- Dificuldade: D
- Pagina: 92
- Classe: Comum
- Escola(s): Cura
- Duracao: 1 hora
- Energia: Varia
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Cura Superficial, Audição Aguçada ou Ensurdecer

### Restaurar Fala
- ID: restaurar_fala
- Dificuldade: D
- Pagina: 93
- Classe: Comum
- Escola(s): Cura
- Duracao: 1 hora
- Energia: 05/03
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Cura Superficial, Voz Amplificada ou Emudecer

### Restaurar Mana
- ID: restaurar_mana
- Dificuldade: MD
- Pagina: 127
- Classe: Área
- Escola(s): Metamágica
- Duracao: Perm.
- Energia: 10
- Tempo de Operacao: 1 hora
- Pre-requisitos: Anular Mágica, Suspender Mana

### Restaurar Memória
- ID: restaurar_memoria
- Dificuldade: D
- Pagina: 92
- Classe: Comum
- Escola(s): Cura
- Duracao: Perm.
- Energia: 3
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Despertar, IQ 11+

### Restaurar Visão
- ID: restaurar_visao
- Dificuldade: D
- Pagina: 92
- Classe: Comum
- Escola(s): Cura
- Duracao: 1 hora
- Energia: Varia
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Cura Superficial, Visão Aguçada ou Cegar

### Restauração
- ID: restauracao
- Dificuldade: MD
- Pagina: 93
- Classe: Comum
- Escola(s): Cura
- Duracao: Perm.
- Energia: 15
- Tempo de Operacao: 1 min.#
- Pre-requisitos: Cura Profunda, ou quaisquer 2 de Aliviar Paralisia e as mágicas de Restaurar

### Restauração Instantânea
- ID: restauracao_instantanea
- Dificuldade: MD
- Pagina: 93
- Classe: Comum
- Escola(s): Cura
- Duracao: Perm.
- Energia: 50
- Tempo de Operacao: Especial
- Pre-requisitos: AM2, Restauração

### Retardar
- ID: retardar
- Dificuldade: D
- Pagina: 145
- Classe: Comum/R-HT
- Escola(s): Deslocamento
- Duracao: 10 seg.
- Energia: 05/04
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM1, Apressar, Estorvar

### Retardar Cura
- ID: retardar_cura
- Dificuldade: D
- Pagina: 153
- Classe: Comum/R-HT
- Escola(s): Necromancia
- Duracao: 1 dia
- Energia: 1 a 5/I
- Tempo de Operacao: 10 seg.
- Pre-requisitos: AM1, Fragilidade, Roubar Vitalidade

### Retardar Fogo
- ID: retardar_fogo
- Dificuldade: D
- Pagina: 73
- Classe: Comum
- Escola(s): Fogo
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Extinguir Fogo

### Retardar Mensagem
- ID: retardar_mensagem
- Dificuldade: D
- Pagina: 173
- Classe: Área
- Escola(s): Som
- Duracao: Indef.#
- Energia: 3#
- Tempo de Operacao: 4 seg.
- Pre-requisitos: AM1, Vozes, Percepção de Vida

### Retardar Queda
- ID: retardar_queda
- Dificuldade: D
- Pagina: 144
- Classe: Comum
- Escola(s): Deslocamento
- Duracao: 1 min.
- Energia: 1/25 kg/M
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Aporte

### Retardar Tempo
- ID: retardar_tempo
- Dificuldade: MD
- Pagina: 86
- Classe: Área/R-Espec.
- Escola(s): Portal
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 2 seg.
- Pre-requisitos: AM2, IQ 13+, 2 mágicas em 10 escolas diferentes

### Retardo
- ID: retardo
- Dificuldade: D
- Pagina: 130
- Classe: Comum
- Escola(s): Metamágica
- Duracao: 2 horas
- Energia: 03/03
- Tempo de Operacao: 10 seg.
- Pre-requisitos: AM3, 15 mágicas

### Reter
- ID: reter
- Dificuldade: D
- Pagina: 143
- Classe: Bloqueio
- Escola(s): Deslocamento
- Duracao: Instant.
- Energia: 1/m#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Aporte

### Retrovisão
- ID: retrovisao
- Dificuldade: D
- Pagina: 134
- Classe: Comum
- Escola(s): Mente
- Duracao: 1 min.
- Energia: 03/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Prontidão

### Revelar Posição
- ID: revelar_posicao
- Dificuldade: D
- Pagina: 101
- Classe: Informação
- Escola(s): Reconhecimento
- Duracao: Instant.
- Energia: 1
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Medidas

### Reverter Projéteis
- ID: reverter_projeteis
- Dificuldade: D
- Pagina: 168
- Classe: Comum
- Escola(s): Proteção
- Duracao: 1 min.
- Energia: 07/03
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Escudo Antiprojétil ou Domo de Força

### Robuste
- ID: robuste
- Dificuldade: D
- Pagina: 167
- Classe: Bloqueio
- Escola(s): Proteção
- Duracao: Instant.
- Energia: 1/RD+#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Bloquear

### Roubar Atributo
- ID: roubar_atributo
- Dificuldade: MD
- Pagina: 158
- Classe: Comum/R-Espec.
- Escola(s): Necromancia
- Duracao: 1 dia
- Energia: Varia
- Tempo de Operacao: 1 min.
- Pre-requisitos: Varia

### Roubar Beleza
- ID: roubar_beleza
- Dificuldade: MD
- Pagina: 159
- Classe: Comum
- Escola(s): Necromancia
- Duracao: 24 horas
- Energia: Varia
- Tempo de Operacao: 30 seg.
- Pre-requisitos: AM3, Alterar Feições, Roubar Vitalidade

### Roubar Corrente Elétrica/NT
- ID: roubar_corrente_eletricant
- Dificuldade: MD
- Pagina: 180
- Classe: Comum
- Escola(s): Tecnológica
- Duracao: 1 min.
- Energia: 0#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, Cura Superficial, Conduzir Corrente Elétrica

### Roubar Energia
- ID: roubar_energia
- Dificuldade: D
- Pagina: 150
- Classe: Comum
- Escola(s): Necromancia
- Duracao: Perm.
- Energia: Nenhum#
- Tempo de Operacao: 1 min./3 PF-
- Pre-requisitos: Cura Superficial

### Roubar Juventude
- ID: roubar_juventude
- Dificuldade: MD
- Pagina: 158
- Classe: Comum/R-HT
- Escola(s): Necromancia
- Duracao: Perm.
- Energia: 10 a 30
- Tempo de Operacao: 1 hora
- Pre-requisitos: Rejuvenesc, Envelhecer, Roubar Vitalidade

### Roubar Mágica
- ID: roubar_magica
- Dificuldade: MD
- Pagina: 127
- Classe: Comum/R-Espec.
- Escola(s): Metamágica
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Conceder Mágica, Proteção Superior

### Roubar Perícia
- ID: roubar_pericia
- Dificuldade: MD
- Pagina: 158
- Classe: Comum/R-Vont
- Escola(s): Necromancia
- Duracao: 24 horas
- Energia: Varia
- Tempo de Operacao: 1 min.
- Pre-requisitos: AM3, Requisitar Perícia, Torpor

### Roubar Vitalidade
- ID: roubar_vitalidade
- Dificuldade: D
- Pagina: 150
- Classe: Comum
- Escola(s): Necromancia
- Duracao: Perm.
- Energia: Nenhum#
- Tempo de Operacao: 1 min./3 PV-
- Pre-requisitos: Roubar Energia

### Rouxinol
- ID: rouxinol
- Dificuldade: D
- Pagina: 167
- Classe: Área
- Escola(s): Proteção
- Duracao: 10 horas
- Energia: 02/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Percepção do Perigo

### Rádio-Audição
- ID: radioaudicao
- Dificuldade: D
- Pagina: 181
- Classe: Comum
- Escola(s): Tecnológica
- Duracao: 1 min.
- Energia: 02/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Audição Aguçada

### Sabedoria
- ID: sabedoria
- Dificuldade: D
- Pagina: 135
- Classe: Comum
- Escola(s): Mente
- Duracao: 1 min.
- Energia: 4 por IQ+/I
- Tempo de Operacao: 1 seg.
- Pre-requisitos: 6 outras mágicas da Mente

### Sacar Rápido
- ID: sacar_rapido
- Dificuldade: D
- Pagina: 63
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 300/0,5 kg#
- Tempo de Operacao: —
- Pre-requisitos: Encantar, Aporte

### Saltar Nuvens
- ID: saltar_nuvens
- Dificuldade: MD
- Pagina: 148
- Classe: Comum
- Escola(s): Deslocamento, Clima
- Duracao: 1 seg./150 km#
- Energia: 7
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, Salto, Caminhar nas Nuvens

### Salto
- ID: salto
- Dificuldade: D
- Pagina: 143
- Classe: Comum
- Escola(s): Deslocamento
- Duracao: 1 min.
- Energia: 1 a 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Aporte

### Santuário
- ID: santuario
- Dificuldade: MD
- Pagina: 86
- Classe: Especial
- Escola(s): Portal
- Duracao: 1 hora
- Energia: 5/I
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Ocultar Objeto

### Sapatos de Neve
- ID: sapatos_de_neve
- Dificuldade: D
- Pagina: 186
- Classe: Comum
- Escola(s): Água
- Duracao: 1 min.
- Energia: 02/01
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Moldar Água

### Secar Nascente
- ID: secar_nascente
- Dificuldade: D
- Pagina: 188
- Classe: Comum
- Escola(s): Água
- Duracao: Perm.
- Energia: Varia#
- Tempo de Operacao: 1 min.
- Pre-requisitos: Dissipar Água, Moldar Terra

### Secar Plantas
- ID: secar_plantas
- Dificuldade: D
- Pagina: 162
- Classe: Área
- Escola(s): Plantas
- Duracao: 1 plant./estação
- Energia: 1
- Tempo de Operacao: 5 min.
- Pre-requisitos: Crescimento de Plantas

### Sede
- ID: sede
- Dificuldade: D
- Pagina: 38
- Classe: Comum/R-HT
- Escola(s): Corpo, Alimentos
- Duracao: Instant.
- Energia: 5
- Tempo de Operacao: 10 seg.
- Pre-requisitos: AM1, Debilitar, Dissipar Água

### Senha
- ID: senha
- Dificuldade: D
- Pagina: 68
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 400#
- Tempo de Operacao: —
- Pre-requisitos: Encantar

### Sensibilizar
- ID: sensibilizar
- Dificuldade: D
- Pagina: 39
- Classe: Comum/R-HT
- Escola(s): Corpo
- Duracao: 1 min.
- Energia: 03/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, Atordoamento

### Sentido Aguçado
- ID: sentido_agucado
- Dificuldade: D
- Pagina: 133
- Classe: Comum
- Escola(s): Mente
- Duracao: 30 min.
- Energia: 1 por +/M#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: —

### Sentinela
- ID: sentinela
- Dificuldade: D
- Pagina: 167
- Classe: Área
- Escola(s): Proteção
- Duracao: 10 horas
- Energia: 01/01
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Percepção do Perigo

### Serralheiro
- ID: serralheiro
- Dificuldade: D
- Pagina: 143
- Classe: Comum
- Escola(s): Deslocamento
- Duracao: 1 min.
- Energia: 02/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Aporte

### Silêncio
- ID: silencio
- Dificuldade: D
- Pagina: 171
- Classe: Área
- Escola(s): Som
- Duracao: 1 min.
- Energia: 02/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Som

### Simulacro
- ID: simulacro
- Dificuldade: MD
- Pagina: 61
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.#
- Energia: 2× golem
- Tempo de Operacao: —
- Pre-requisitos: AM3, Golem, Ilusão Perfeita, Disfarce Ilusório

### Sintonizar
- ID: sintonizar
- Dificuldade: D
- Pagina: 69
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 100
- Tempo de Operacao: —
- Pre-requisitos: Eliminar

### Solidificar
- ID: solidificar
- Dificuldade: D
- Pagina: 151
- Classe: Especial
- Escola(s): Necromancia
- Duracao: 1 min.
- Energia: 50/10
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Materializar

### Som
- ID: som
- Dificuldade: D
- Pagina: 171
- Classe: Comum
- Escola(s): Som
- Duracao: Varia
- Energia: Varia
- Tempo de Operacao: 1 seg.
- Pre-requisitos: —

### Sombrear
- ID: sombrear
- Dificuldade: D
- Pagina: 169
- Classe: Comum
- Escola(s): Proteção, Luz e Trevas
- Duracao: 1 hora
- Energia: 1/M
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Luz Contínua ou Escudo

### Sonda Mental
- ID: sonda_mental
- Dificuldade: MD
- Pagina: 46
- Classe: Comum/R-Vont
- Escola(s): Comunicação
- Duracao: Min.
- Energia: 06/03
- Tempo de Operacao: 1 min.
- Pre-requisitos: Leitura da Mente

### Sono
- ID: sono
- Dificuldade: D
- Pagina: 135
- Classe: Comum/R-HT
- Escola(s): Mente
- Duracao: Instant.
- Energia: 4
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Torpor

### Sono Coletivo
- ID: sono_coletivo
- Dificuldade: D
- Pagina: 37
- Classe: Área/R-HT
- Escola(s): Cura
- Duracao: 8 horas#
- Energia: 6 ou 10
- Tempo de Operacao: 30 seg.
- Pre-requisitos: Sono, IQ 13+

### Sono Curativo
- ID: sono_curativo
- Dificuldade: D
- Pagina: 94
- Classe: Comum/R-#
- Escola(s): Mente
- Duracao: 8 horas
- Energia: 4
- Tempo de Operacao: 30 seg.
- Pre-requisitos: AM2, Sono, Cura Superficial

### Sono Tranquilo
- ID: sono_tranquilo
- Dificuldade: D
- Pagina: 138
- Classe: Comum/R-Espec.
- Escola(s): Mente
- Duracao: ?
- Energia: 1 a 4
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Sono, Silêncio

### Sopro Congelante
- ID: sopro_congelante
- Dificuldade: MD
- Pagina: 192
- Classe: Comum
- Escola(s): Fogo
- Duracao: Instant.
- Energia: 1 a 4
- Tempo de Operacao: 2 seg.
- Pre-requisitos: AM1, Jato de Neve, Imunidade ao Frio

### Sopro de Fogo
- ID: sopro_de_fogo
- Dificuldade: MD
- Pagina: 76
- Classe: Comum
- Escola(s): Tecnológica
- Duracao: Instant.
- Energia: 1 a 4
- Tempo de Operacao: 2 seg.
- Pre-requisitos: AM1, Jato de Chamas, Imunidade ao Fogo

### Sopro de Radiação
- ID: sopro_de_radiacao
- Dificuldade: MD
- Pagina: 182
- Classe: Comum
- Escola(s): Água
- Duracao: Instant.
- Energia: 1 a 4
- Tempo de Operacao: 2 seg.
- Pre-requisitos: AM2, Jato de Radiação

### Sopro de Vapor
- ID: sopro_de_vapor
- Dificuldade: MD
- Pagina: 192
- Classe: Área
- Escola(s): Água
- Duracao: Instant.
- Energia: 2/M
- Tempo de Operacao: Varia
- Pre-requisitos: Moldar Água

### Sopro de Ácido
- ID: sopro_de_acido
- Dificuldade: MD
- Pagina: None
- Classe: Comum
- Escola(s): Água
- Duracao: 1 seg.
- Energia: 1 a 4
- Tempo de Operacao: 2 seg.
- Pre-requisitos: AM3, Jato de Ácido, Imunidade a Ácido

### Sorvedouro
- ID: sorvedouro
- Dificuldade: D
- Pagina: 187
- Classe: Área
- Escola(s): Água
- Duracao: 1min.#
- Energia: 10#
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM2, 5 mágicas da Terra

### Sotterramento
- ID: sotterramento
- Dificuldade: D
- Pagina: 53
- Classe: Comum/R-HT
- Escola(s): Mente
- Duracao: Perm.
- Energia: 06/03
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM1, Lealdade, 7 mágicas de Controle da Mente

### Subjugar
- ID: subjugar
- Dificuldade: D
- Pagina: 139
- Classe: Comum/R-Vont
- Escola(s): Mente
- Duracao: 1 min.
- Energia: 4
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, 5 mágicas de Corpo incl. Espasmo

### Sugestão
- ID: sugestao
- Dificuldade: D
- Pagina: 140
- Classe: Comum/R-Vont
- Escola(s): Mente
- Duracao: 30 seg.
- Energia: 04/03
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Controle de Emoção, Esquecimento

### Sugestão Coletiva
- ID: sugestao_coletiva
- Dificuldade: D
- Pagina: 141
- Classe: Área/R-Vont
- Escola(s): Mente
- Duracao: 10 min.
- Energia: 4/2#
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Sugestão

### Suspender
- ID: suspender
- Dificuldade: D
- Pagina: 130
- Classe: Comum/R-Vont+AM
- Escola(s): Metamágica
- Duracao: 10 min.
- Energia: 12/12#
- Tempo de Operacao: 10 seg.
- Pre-requisitos: AM2, 2 mágicas em 10 escolas diferentes#

### Suspender Encantamento
- ID: suspender_encantamento
- Dificuldade: D
- Pagina: 58
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: 1 hora
- Energia: 25#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Encantar

### Suspender Magia
- ID: suspender_magia
- Dificuldade: D
- Pagina: 123
- Classe: Área/R-Mágica
- Escola(s): Metamágica
- Duracao: Hora
- Energia: 03/02
- Tempo de Operacao: seg.=custo
- Pre-requisitos: Suspender Mágica, 8 outras mágicas

### Suspender Maldição
- ID: suspender_maldicao
- Dificuldade: D
- Pagina: 125
- Classe: Comum/R-Mágica
- Escola(s): Metamágica
- Duracao: 1 min.
- Energia: 10/10
- Tempo de Operacao: 1 min.
- Pre-requisitos: AM1, 1 mágica em 12 escolas diferentes

### Suspender Mana
- ID: suspender_mana
- Dificuldade: MD
- Pagina: 125
- Classe: Área
- Escola(s): Metamágica
- Duracao: 10 min.
- Energia: 5
- Tempo de Operacao: 10 min.
- Pre-requisitos: Suspender Mágica, 1 mágica em 10 escolas diferentes

### Suspender Mágica
- ID: suspender_magica
- Dificuldade: D
- Pagina: 121
- Classe: Comum/R-Mágica
- Escola(s): Metamágica
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1

### Suspender Tempo
- ID: suspender_tempo
- Dificuldade: MD
- Pagina: 86
- Classe: Área/R-Espec.
- Escola(s): Portal
- Duracao: Varia
- Energia: 05/05
- Tempo de Operacao: 5 min.
- Pre-requisitos: AM3, Retardar Tempo

### Talismã
- ID: talisma
- Dificuldade: D
- Pagina: 58
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: 1 dia
- Energia: Varia
- Tempo de Operacao: —
- Pre-requisitos: Encantar, mágica oposta

### Tapete Voador
- ID: tapete_voador
- Dificuldade: MD
- Pagina: 146
- Classe: Comum
- Escola(s): Deslocamento
- Duracao: Perm.
- Energia: 1/0,1 m²/M
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Voo, ou AM2 e Caminhar no Ar

### Tato Remoto
- ID: tato_remoto
- Dificuldade: D
- Pagina: 100
- Classe: Comum
- Escola(s): Reconhecimento
- Duracao: 10 min.
- Energia: 03/01
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM1

### Teia de Aranha
- ID: teia_de_aranha
- Dificuldade: D
- Pagina: 32
- Classe: Projetil
- Escola(s): Animais
- Duracao: 1 min.
- Energia: 1/5 m#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, 2 mágicas sobre Animais

### Telemágica
- ID: telemagica
- Dificuldade: MD
- Pagina: 128
- Classe: Especial
- Escola(s): Metamágica
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 1 min.
- Pre-requisitos: AM3, Teleporte, Olho Mágico, 1 mágica em 10 escolas diferentes

### Telepatia
- ID: telepatia
- Dificuldade: MD
- Pagina: 47
- Classe: Comum
- Escola(s): Comunicação
- Duracao: 1 min.
- Energia: 4/4#
- Tempo de Operacao: 4 seg.
- Pre-requisitos: Transmissão de Pensamento

### Teleportar Outro
- ID: teleportar_outro
- Dificuldade: MD
- Pagina: 147
- Classe: Comum/R-Vont+1
- Escola(s): Deslocamento, Portal
- Duracao: 1 min.
- Energia: Varia
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM3, Teleporte

### Teleporte
- ID: teleporte
- Dificuldade: MD
- Pagina: 147
- Classe: Especial
- Escola(s): Deslocamento, Portal
- Duracao: Instant.
- Energia: Varia
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Voo do Falcão ou IQ 13+ e 1 mágica em 10 escolas diferentes

### Temperar
- ID: temperar
- Dificuldade: D
- Pagina: 77
- Classe: Comum
- Escola(s): Alimentos
- Duracao: Instant.
- Energia: 2/refeição
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Testar Alimento

### Tempestade
- ID: tempestade
- Dificuldade: D
- Pagina: 195
- Classe: Área
- Escola(s): Clima, Ar, Água
- Duracao: Perm.
- Energia: 1/50/I
- Tempo de Operacao: 1 min.
- Pre-requisitos: Chuva, Granizo

### Tempestade de Areia
- ID: tempestade_de_areia
- Dificuldade: D
- Pagina: 27
- Classe: Área
- Escola(s): Ar, Terra
- Duracao: 1 hora
- Energia: 3/M
- Tempo de Operacao: Instant.#
- Pre-requisitos: Furacão, Criar Terra

### Tempestade de Faíscas
- ID: tempestade_de_faiscas
- Dificuldade: D
- Pagina: 197
- Classe: Área
- Escola(s): Clima, Ar
- Duracao: 1 min.#
- Energia: 2, 4 ou 6/M
- Tempo de Operacao: Instant.#
- Pre-requisitos: Furacão, Relâmpago

### Tepidez
- ID: tepidez
- Dificuldade: D
- Pagina: 74
- Classe: Comum
- Escola(s): Fogo, Proteção
- Duracao: 1 min.#
- Energia: 02/01
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Calor

### Terra Essencial
- ID: terra_essencial
- Dificuldade: D
- Pagina: 53
- Classe: Comum
- Escola(s): Terra
- Duracao: 1 hora
- Energia: 8
- Tempo de Operacao: 30 seg.
- Pre-requisitos: 6 mágicas da Terra

### Terra para Ar
- ID: terra_para_ar
- Dificuldade: D
- Pagina: 25
- Classe: Comum
- Escola(s): Ar, Terra
- Duracao: Perm.
- Energia: 5/0,75 m³#
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Criar Ar, Moldar Terra

### Terra para Pedra
- ID: terra_para_pedra
- Dificuldade: D
- Pagina: 51
- Classe: Comum
- Escola(s): Terra
- Duracao: Perm.
- Energia: 3/0,75 m³#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, Moldar Terra

### Terra para Água
- ID: terra_para_agua
- Dificuldade: D
- Pagina: 52
- Classe: Comum
- Escola(s): Terra, Água
- Duracao: Perm.
- Energia: 1/0,75 m³#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, Criar Água, Moldar Terra

### Terremoto
- ID: terremoto
- Dificuldade: D
- Pagina: 54
- Classe: Área
- Escola(s): Terra
- Duracao: Perm.
- Energia: 2/I
- Tempo de Operacao: 30 seg.
- Pre-requisitos: AM2, 6 mágicas da Terra incl. Geovisão

### Terror
- ID: terror
- Dificuldade: D
- Pagina: 134
- Classe: Área/R-Vont
- Escola(s): Mente
- Duracao: 1 min.
- Energia: 4
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Medo

### Testar Alimento
- ID: testar_alimento
- Dificuldade: D
- Pagina: 77
- Classe: Informação
- Escola(s): Alimentos
- Duracao: Instant.
- Energia: 1 a 3#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: —

### Testar Carga
- ID: testar_carga
- Dificuldade: D
- Pagina: 101
- Classe: Área/Informação
- Escola(s): Reconhecimento
- Duracao: Instant.
- Energia: 2#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Medidas

### Testar Combustível/NT
- ID: testar_combustivelnt
- Dificuldade: D
- Pagina: 179
- Classe: Informação
- Escola(s): Tecnológica
- Duracao: Instant.
- Energia: Varia
- Tempo de Operacao: 1 seg.
- Pre-requisitos: —

### Tingir
- ID: tingir
- Dificuldade: D
- Pagina: 116
- Classe: Comum
- Escola(s): Quebrar e Consertar
- Duracao: Instant.
- Energia: Varia
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Restaurar, Cores

### Tolice
- ID: tolice
- Dificuldade: D
- Pagina: 134
- Classe: Comum/R-Vont
- Escola(s): Mente
- Duracao: 2d dias
- Energia: 1 por IQ-/M
- Tempo de Operacao: 1 seg.
- Pre-requisitos: IQ 12+

### Toque
- ID: toque
- Dificuldade: D
- Pagina: 35
- Classe: Comum
- Escola(s): Corpo
- Duracao: 1 min.
- Energia: 1
- Tempo de Operacao: 1 seg.
- Pre-requisitos: —

### Toque Candente
- ID: toque_candente
- Dificuldade: D
- Pagina: 76
- Classe: Toque
- Escola(s): Fogo
- Duracao: Instant.
- Energia: 1 a 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, 6 mágicas do Fogo incl. Calor

### Toque Chocante
- ID: toque_chocante
- Dificuldade: D
- Pagina: 196
- Classe: Toque
- Escola(s): Clima, Ar
- Duracao: Instant.
- Energia: 1 a 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Relâmpago

### Toque Congelante
- ID: toque_congelante
- Dificuldade: D
- Pagina: 188
- Classe: Toque
- Escola(s): Água
- Duracao: Instant.
- Energia: 2#
- Tempo de Operacao: 1 seg.#
- Pre-requisitos: AM1, 4 mágicas da Água

### Toque Mortal
- ID: toque_mortal
- Dificuldade: D
- Pagina: 41
- Classe: Toque
- Escola(s): Corpo
- Duracao: Perm.
- Energia: 1 a 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Atrofiar Membro

### Torpor
- ID: torpor
- Dificuldade: D
- Pagina: 134
- Classe: Comum/R-HT
- Escola(s): Mente
- Duracao: Instant.
- Energia: 03/02
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Tolice

### Torpor Coletivo
- ID: torpor_coletivo
- Dificuldade: D
- Pagina: 137
- Classe: Área/R-HT
- Escola(s): Mente
- Duracao: 1 min.
- Energia: 2/1#
- Tempo de Operacao: seg.=custo
- Pre-requisitos: Torpor, IQ 13+

### Tranca Mágica
- ID: tranca_magica
- Dificuldade: D
- Pagina: 166
- Classe: Comum
- Escola(s): Proteção
- Duracao: Instant.
- Energia: 03/02
- Tempo de Operacao: 4 seg.
- Pre-requisitos: AM1

### Transformar Corpo
- ID: transformar_corpo
- Dificuldade: D
- Pagina: 43
- Classe: Especial
- Escola(s): Corpo
- Duracao: Instant.
- Energia: Varia
- Tempo de Operacao: 1 min.
- Pre-requisitos: 3 formas de Metamorfose, Alterar Corpo

### Transformar Objeto
- ID: transformar_objeto
- Dificuldade: MD
- Pagina: 120
- Classe: Comum/R-Espec.
- Escola(s): Quebrar e Consertar
- Duracao: 1 hora
- Energia: Varia
- Tempo de Operacao: Varia
- Pre-requisitos: AM2, Remodelar, 4 mágicas Criar

### Transformar Outro
- ID: transformar_outro
- Dificuldade: D
- Pagina: 43
- Classe: Especial/R-Vont
- Escola(s): Corpo
- Duracao: 1 hora
- Energia: Varia
- Tempo de Operacao: 2 min.
- Pre-requisitos: Metamorfosear Outro, Transformar Corpo#

### Translocar Outro
- ID: translocar_outro
- Dificuldade: MD
- Pagina: 148
- Classe: Bloqueio
- Escola(s): Deslocamento, Portal
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Translocação

### Translocar Outro no Tempo
- ID: translocar_outro_no_tempo
- Dificuldade: MD
- Pagina: 81
- Classe: Bloqueio
- Escola(s): Portal
- Duracao: Instant.
- Energia: 1/sec.#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Translocação no Tempo

### Translocação
- ID: translocacao
- Dificuldade: D
- Pagina: 148
- Classe: Bloqueio
- Escola(s): Deslocamento, Portal
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Teleporte

### Translocação no Tempo
- ID: translocacao_no_tempo
- Dificuldade: D
- Pagina: 81
- Classe: Bloqueio
- Escola(s): Portal
- Duracao: Instant.
- Energia: 1/sec.#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Viagem no Tempo

### Transmissão Mental
- ID: transmissao_mental
- Dificuldade: D
- Pagina: 47
- Classe: Comum
- Escola(s): Comunicação
- Duracao: 1 min.
- Energia: 04/04
- Tempo de Operacao: 4 seg.
- Pre-requisitos: Leitura da Mente

### Transmissão de Sonho
- ID: transmissao_de_sonho
- Dificuldade: D
- Pagina: 45
- Classe: Comum/R-Vont
- Escola(s): Comunicação, Mente
- Duracao: 1 hora
- Energia: 3
- Tempo de Operacao: 1 min.
- Pre-requisitos: Visualização de Sonho ou Sono

### Transmogrificação
- ID: transmogrificacao
- Dificuldade: D
- Pagina: 43
- Classe: Comum/R-Vont
- Escola(s): Corpo
- Duracao: 1 hora
- Energia: 20/20
- Tempo de Operacao: 2 min.
- Pre-requisitos: AM3, Transformar Objeto, Carne para Pedra

### Transparência
- ID: transparencia
- Dificuldade: D
- Pagina: 119
- Classe: Comum
- Escola(s): Quebrar e Consertar
- Duracao: 1 min.
- Energia: 04/02
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Tingir, Pedra para Terra

### Transportar Outro no Tempo
- ID: transportar_outro_no_tempo
- Dificuldade: MD
- Pagina: 81
- Classe: Comum/R-Vont+1
- Escola(s): Portal
- Duracao: Instant.
- Energia: Varia
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Viagem no Tempo

### Trança-Pés
- ID: trancapes
- Dificuldade: D
- Pagina: 36
- Classe: Comum/R-DX
- Escola(s): Corpo
- Duracao: 6 horas
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Inabilidade

### Travar Vontade
- ID: travar_vontade
- Dificuldade: D
- Pagina: 138
- Classe: Área/R-(ST+Vont)/2
- Escola(s): Mente
- Duracao: 1 dia
- Energia: 3
- Tempo de Operacao: Varia
- Pre-requisitos: Controle de Emoção

### Trevas
- ID: trevas
- Dificuldade: D
- Pagina: 111
- Classe: Área
- Escola(s): Luz e Trevas
- Duracao: 1 min.
- Energia: 02/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Luz Contínua

### Trocar de Corpo
- ID: trocar_de_corpo
- Dificuldade: MD
- Pagina: 49
- Classe: Comum/R-Vont
- Escola(s): Comunicação
- Duracao: Perm.
- Energia: 120
- Tempo de Operacao: 1 hora
- Pre-requisitos: Possessão Permanente, Aprisionar Alma

### Turbilhão
- ID: turbilhao
- Dificuldade: D
- Pagina: 26
- Classe: Área/R-HT ou DX
- Escola(s): Ar, Deslocamento
- Duracao: 10 seg.
- Energia: 08/03
- Tempo de Operacao: 2 seg.
- Pre-requisitos: AM2, Corpo de Ar, Furacão

### Vazamento
- ID: vazamento
- Dificuldade: D
- Pagina: 61
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: 100
- Tempo de Operacao: —
- Pre-requisitos: Encobrir

### Velocidade
- ID: velocidade
- Dificuldade: D
- Pagina: 57
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: —
- Pre-requisitos: Encantar, Apressar

### Vento
- ID: vento
- Dificuldade: D
- Pagina: 195
- Classe: Especial/Área
- Escola(s): Clima, Ar
- Duracao: 1 hora
- Energia: 1/50/I
- Tempo de Operacao: 1 min.
- Pre-requisitos: Furacão

### Ver Localização
- ID: ver_localizacao
- Dificuldade: D
- Pagina: 103
- Classe: Informação
- Escola(s): Reconhecimento
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 10 seg.
- Pre-requisitos: AM1, Revelar Posição

### Ver Radiação
- ID: ver_radiacao
- Dificuldade: D
- Pagina: 181
- Classe: Comum
- Escola(s): Tecnológica
- Duracao: 1 min.
- Energia: 03/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: —

### Ver Receita
- ID: ver_receita
- Dificuldade: D
- Pagina: 78
- Classe: Informação/R-Espec.
- Escola(s): Alimentos, Reconhecimento
- Duracao: Dia#
- Energia: 3
- Tempo de Operacao: 15 seg.
- Pre-requisitos: Paladar Remoto, Temperar

### Ver Segredos
- ID: ver_segredos
- Dificuldade: D
- Pagina: 107
- Classe: Comum
- Escola(s): Reconhecimento
- Duracao: 1 min.
- Energia: 05/02
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Localizador, Aura

### Ver a Forma Real
- ID: ver_a_forma_real
- Dificuldade: D
- Pagina: 106
- Classe: Informação
- Escola(s): Reconhecimento
- Duracao: Instant.
- Energia: 2
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM1, qualquer mágica de metamorfose, mais Aura ou Detectar Ilusão

### Ver o Invisível
- ID: ver_o_invisivel
- Dificuldade: D
- Pagina: 113
- Classe: Comum
- Escola(s): Luz e Trevas
- Duracao: 1 min.
- Energia: 04/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Invisibilidade, ou Visão nas Trevas ou Infravisão

### Vestuário Ajustável
- ID: vestuario_ajustavel
- Dificuldade: D
- Pagina: 58
- Classe: Encantamento
- Escola(s): Encantamento
- Duracao: Perm.
- Energia: Varia
- Tempo de Operacao: —
- Pre-requisitos: Encantar, Remodelar

### Vexação
- ID: vexacao
- Dificuldade: D
- Pagina: 45
- Classe: Comum/R-Vont
- Escola(s): Comunicação
- Duracao: 1 min.
- Energia: 2xpenal.#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Percepção de Emoção

### Viagem Planar
- ID: viagem_planar
- Dificuldade: MD
- Pagina: 83
- Classe: Especial
- Escola(s): Portal
- Duracao: Instant.
- Energia: 20
- Tempo de Operacao: 5 seg.
- Pre-requisitos: Convocação Planar

### Viagem Planar para Outro
- ID: viagem_planar_para_outro
- Dificuldade: MD
- Pagina: 83
- Classe: Comum/R-Vont+1
- Escola(s): Portal
- Duracao: Instant.
- Energia: 20
- Tempo de Operacao: 5 seg.
- Pre-requisitos: AM3, Viagem Planar

### Viagem no Tempo
- ID: viagem_no_tempo
- Dificuldade: MD
- Pagina: 81
- Classe: Especial
- Escola(s): Portal
- Duracao: Instant.
- Energia: Varia
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM3, Teleporte

### Vigor
- ID: vigor
- Dificuldade: D
- Pagina: 37
- Classe: Comum
- Escola(s): Corpo
- Duracao: 1 min.
- Energia: 2 por HT+/I#
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Conceder Vitalidade ou Fragilidade

### Vigília
- ID: vigilia
- Dificuldade: MD
- Pagina: 138
- Classe: Comum
- Escola(s): Mente
- Duracao: 1 noite
- Energia: 6
- Tempo de Operacao: 1 seg.
- Pre-requisitos: AM2, Sono, Conceder Energia

### Visita Planar
- ID: visita_planar
- Dificuldade: MD
- Pagina: 82
- Classe: Especial
- Escola(s): Portal
- Duracao: 1 min.
- Energia: 04/02
- Tempo de Operacao: 30 seg.
- Pre-requisitos: AM2, Projeção da Mente, ou Convocação Planar

### Visualização de Sonho
- ID: visualizacao_de_sonho
- Dificuldade: D
- Pagina: 45
- Classe: Comum/R-Vont
- Escola(s): Comunicação
- Duracao: 1 hora
- Energia: 02/01
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Percepção de Veracidade ou Sono

### Visão Astral
- ID: visao_astral
- Dificuldade: MD
- Pagina: 105
- Classe: Comum
- Escola(s): Reconhecimento, Necromancia
- Duracao: 1 min.
- Energia: 04/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Percepção de Espíritos, Ver o Invisível

### Visão Brilhante
- ID: visao_brilhante
- Dificuldade: D
- Pagina: 111
- Classe: Comum
- Escola(s): Luz e Trevas
- Duracao: 1 min.
- Energia: 02/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Visão Aguçada ou 5 mágicas de Luz, não pode ter Cegueira

### Visão Magnética
- ID: visao_magnetica
- Dificuldade: D
- Pagina: 181
- Classe: Comum
- Escola(s): Tecnológica
- Duracao: 1 min.
- Energia: 02/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Visão Aguçada

### Visão Microscópica
- ID: visao_microscopica
- Dificuldade: D
- Pagina: 111
- Classe: Comum
- Escola(s): Luz e Trevas, Reconhecimento
- Duracao: 1 min.
- Energia: 4/2#
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Visão Aguçada ou 5 mágicas de Luz, não pode ter Cegueira ou Disopia

### Visão Noturna
- ID: visao_noturna
- Dificuldade: D
- Pagina: 111
- Classe: Comum
- Escola(s): Luz e Trevas
- Duracao: 1 min.
- Energia: 03/01
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Visão Aguçada ou 5 mágicas de Luz

### Visão Sonora
- ID: visao_sonora
- Dificuldade: D
- Pagina: 171
- Classe: Comum
- Escola(s): Som
- Duracao: 1 min.
- Energia: 05/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Audição Aguçada (mágica ou vantagem)

### Visão da Morte
- ID: visao_da_morte
- Dificuldade: D
- Pagina: 149
- Classe: Comum
- Escola(s): Necromancia
- Duracao: 1 seg.
- Energia: 2
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM1

### Visão das Plantas
- ID: visao_das_plantas
- Dificuldade: D
- Pagina: 162
- Classe: Comum
- Escola(s): Plantas, Reconhecimento
- Duracao: 30 seg.
- Energia: 1/10 m
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Moldar Planta

### Visão de Magia
- ID: visao_de_magia
- Dificuldade: D
- Pagina: 102
- Classe: Comum
- Escola(s): Reconhecimento
- Duracao: 1 min.
- Energia: 03/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Detecção de Magia

### Visão do Espectro
- ID: visao_do_espectro
- Dificuldade: MD
- Pagina: 181
- Classe: Comum
- Escola(s): Tecnológica
- Duracao: 1 min.
- Energia: 04/04
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Infravisão

### Visão nas Trevas
- ID: visao_nas_trevas
- Dificuldade: D
- Pagina: 111
- Classe: Comum
- Escola(s): Luz e Trevas
- Duracao: 1 min.
- Energia: 05/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Visão Noturna ou Infravisão

### Vomitar
- ID: vomitar
- Dificuldade: D
- Pagina: 38
- Classe: Comum/R-HT
- Escola(s): Corpo
- Duracao: Instant.
- Energia: 3
- Tempo de Operacao: 4 seg.
- Pre-requisitos: Nausear, Espasmo

### Voo
- ID: voo
- Dificuldade: MD
- Pagina: 145
- Classe: Comum
- Escola(s): Deslocamento
- Duracao: 1 min.
- Energia: 05/03
- Tempo de Operacao: 2 seg.
- Pre-requisitos: AM2, Levitação

### Voo do Falcão
- ID: voo_do_falcao
- Dificuldade: MD
- Pagina: 146
- Classe: Comum
- Escola(s): Deslocamento
- Duracao: 1 min.
- Energia: 08/04
- Tempo de Operacao: 3 seg.
- Pre-requisitos: Voo

### Voz Amplificada
- ID: voz_amplificada
- Dificuldade: D
- Pagina: 173
- Classe: Comum
- Escola(s): Som
- Duracao: 1 min.
- Energia: 03/01
- Tempo de Operacao: 2 seg.
- Pre-requisitos: Vozes, Estrondo

### Vozes
- ID: vozes
- Dificuldade: D
- Pagina: 172
- Classe: Comum
- Escola(s): Som
- Duracao: 1 min.
- Energia: 03/02
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Som

### Vulcão
- ID: vulcao
- Dificuldade: D
- Pagina: 54
- Classe: Comum
- Escola(s): Terra
- Duracao: 1 dia
- Energia: 15/10
- Tempo de Operacao: 1 hora#
- Pre-requisitos: Terremoto, 6 mágicas do Fogo

### Zumbi
- ID: zumbi
- Dificuldade: D
- Pagina: 151
- Classe: Comum
- Escola(s): Necromancia
- Duracao: Perm.
- Energia: 8
- Tempo de Operacao: 1 min.
- Pre-requisitos: Convocar Espírito, Conceder Vitalidade

### Zumbis em Massa
- ID: zumbis_em_massa
- Dificuldade: MD
- Pagina: 153
- Classe: Área
- Escola(s): Necromancia
- Duracao: Perm.
- Energia: 7
- Tempo de Operacao: Varia#
- Pre-requisitos: Zumbi, Carisma 2+

### Ácido Essencial
- ID: acido_essencial
- Dificuldade: MD
- Pagina: 192
- Classe: Comum
- Escola(s): Água
- Duracao: Perm.
- Energia: 2/litro
- Tempo de Operacao: 1 seg.
- Pre-requisitos: 6 mágicas de Ácido

### Água Essencial
- ID: agua_essencial
- Dificuldade: D
- Pagina: 189
- Classe: Comum
- Escola(s): Água
- Duracao: Perm.
- Energia: 3/4 litros
- Tempo de Operacao: 1 seg.
- Pre-requisitos: 6 mágicas da Água

### Água Podre
- ID: agua_podre
- Dificuldade: D
- Pagina: 185
- Classe: Área
- Escola(s): Água, Alimentos
- Duracao: Perm.
- Energia: 3
- Tempo de Operacao: 1 seg.
- Pre-requisitos: Purificar a Água, Deteriorar

### Água para Vinho
- ID: agua_para_vinho
- Dificuldade: D
- Pagina: 79
- Classe: Comum
- Escola(s): Alimentos
- Duracao: Perm.
- Energia: 4/litro#
- Tempo de Operacao: 10 seg.
- Pre-requisitos: Purificar a Água, Maturar

### Êxtase
- ID: extase
- Dificuldade: MD
- Pagina: 139
- Classe: Comum/R-Vont
- Escola(s): Mente
- Duracao: 10 seg.
- Energia: 6
- Tempo de Operacao: 3 seg.
- Pre-requisitos: AM2, Controle de Emoção

## Informacoes Extras do Arquivo Legado (`magias.json`)

Campos de regra adicionais presentes no legado:
- atributoBase
- atributosPossiveis
- atributoEscolhaObrigatoria
- dificuldadeFixa
- dificuldadeVariavel
- exigeEspecializacao
- preDefinicoes
- texto (bloco consolidado com Classe/Escola/Duracao/Energia/Tempo/Pre-requisitos)
