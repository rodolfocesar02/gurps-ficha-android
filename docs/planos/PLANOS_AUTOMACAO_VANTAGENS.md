# Plano de Automação de Vantagens e Desvantagens

Este documento lista as vantagens que contêm mecânicas identificáveis e propõe um plano de automação para cada uma delas no código da Ficha GURPS.

## Abafador de Mana (`abafador_de_mana`)
**Resumo mecânico**: O personagem anula a energia mágica (“mana”) que existe ao seu redor, dificultando ou impossibilitando que outros personagens façam mágicas. Ele não é capaz de fazer mágicas, nem pode ter qualquer nív...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Abascanto (Resistência à Magia) (`abascanto_resistencia_a_magia`)
**Resumo mecânico**: O personagem tem mais chance de não ser afetado pela magia. O nível de Resistência à Magia dele é subtraído do nível de habilidade de qualquer outro personagem que faça uma mágica nele. Por exemplo, s...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## adaptabilidade_cultural (`adaptabilidade_cultural`)
**Resumo mecânico**: O personagem está familiarizado com uma grande variedade de culturas. Quando interage com elas, ele nunca sofre uma penalidade de -3 devido ao “desconhecimento cultural” descrito na seção Cultura (pág...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Aderência (`aderencia`)
**Resumo mecânico**: O personagem é capaz de andar ou rastejar em muros e tetos, podendo interromper o percurso a qualquer momento, pois adere à superfície sem nenhum risco de queda. Nenhum desses feitos exige um teste de...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Ambidestria (`ambidestria`)
**Resumo mecânico**: O personagem é capaz de lutar e manusear igualmente bem com qualquer uma das mãos e nunca sofre a penalidade de -4 na DX por estar usando a mão inábil (pág. 14). Lembre-se que isso não permite que o p...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Ampliador de Mana (`ampliador_de_mana`)
**Resumo mecânico**: O personagem irradia energia mágica, ou “mana”. Cada nível de Ampliador de Mana (dois, no máximo) aumenta em um ponto o nível de mana local, mas apenas para ele e pessoas ou objetos com os quais estiv...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Aparência (`aparencia`)
**Resumo mecânico**: A aparência é, principalmente, um “efeito especial” — você pode escolher a aparência que quiser para seu personagem. No mínimo, anote a cor de pele, dos cabelos e dos olhos (e outras características a...
**Plano de Automação:**
- **Reação**: Adicionar na engine de reações (criar `ReactionRuleRegistry` ou injetar em `CharacterRules.kt`) para aplicar o modificador de reação automaticamente.
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).

## Apetrechos (`apetrechos`)
**Resumo mecânico**: O personagem parece ter sempre em mão exatamente o que precisa. Para cada nível da vantagem, uma vez a cada sessão de jogo, ele pode sacar uma pequena peça de equipamento que, por alguma razão, estari...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Arrebatador (`arrebatador`)
**Resumo mecânico**: O personagem tem o poder de encontrar quase qualquer item pequeno que desejar num mundo alternativo e “arrebatá-lo”, fazendo-o atravessar dimensões. Os objetos arrebatados não provêm do mundo do perso...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Atirador (Arqueiro Heroico) (`atirador_arqueiro_heroico`)
**Resumo mecânico**: Você é capaz de realizar feitos extraordinários com qualquer arma que utilize a perícia Arco.

Esta vantagem representa uma habilidade cinematográfica que permite disparar com precisão e velocidade mu...
**Plano de Automação:**
- **Perícias (NH)**: Implementar uma regra customizada em `TraitRuleRegistry.kt` que afete o NH de usos específicos (ex: certas mágicas ou ataques).
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Artífice (`artifice`)
**Resumo mecânico**: Talento que concede bônus em perícias de conserto e engenharia. Custa 10 pontos/nível....
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Artista Talentoso (`artista_talentoso`)
**Resumo mecânico**: Talento para perícias artísticas (Artista, Costura, etc.). Custa 5 pontos/nível....
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Ataque Adicional (`ataque_adicional`)
**Resumo mecânico**: O personagem é capaz de atacar mais de uma vez por turno. Em GURPS, via de regra, qualquer personagem pode realizar um ataque por turno, independente de quantos membros ele possui. Cada nível de Ataqu...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Ataque Constritivo (`ataque_constritivo`)
**Resumo mecânico**: A musculatura do personagem foi aperfeiçoada para esmagar seus oponentes, seja com um “abraço de urso” ou esmagando-o como uma jiboia. Para utilizar esta habilidade, o personagem primeiro precisa agar...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Atirador (`atirador`)
**Resumo mecânico**: O personagem é capaz de dar tiros extraordinariamente precisos sem mirar. Quando estiver usando qualquer arma que utilize as perícias Armas de Feixe, Armas de Fogo, Canhoneiro ou Projetor de Líquidos,...
**Plano de Automação:**
- **Perícias (NH)**: Implementar uma regra customizada em `TraitRuleRegistry.kt` que afete o NH de usos específicos (ex: certas mágicas ou ataques).

## Atribulação (`atribulacao`)
**Resumo mecânico**: O personagem possui um ataque que provoca um efeito nocivo sem causar dano: cegueira, paralisia, fraqueza, etc. Essa talvez seja uma arma de feixe de alta tecnologia, um spray químico, um ataque visua...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Audição Aguçada (`audicao_agucada`)
**Resumo mecânico**: Bônus de +1 por nível em testes de audição. Custa 2 pontos/nível....
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Audição Discriminatória (`audicao_discriminatoria`)
**Resumo mecânico**: O personagem tem uma capacidade sobre-humana para distinguir sons. Ele sempre consegue identificar as pessoas pela voz e é capaz de reconhecer cada máquina por seu “som típico”. No caso de um sucesso ...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Audição Parabólica (`audicao_parabolica`)
**Resumo mecânico**: O personagem é capaz de dar um “zoom” num som ou numa área em particular e filtrar os sons que lhe interessam do ruído de fundo. Cada nível de Audição Parabólica dobra a distância dentro da qual o per...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Audição Subsônica (`audicao_subsonica`)
**Resumo mecânico**: O personagem é capaz de ouvir sons de baixa frequência (abaixo de 40 Hz), como o retumbar de trovões distantes, a vibração inicial de terremotos e o estampido produzido pelo estouro de um bando de ani...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Boa Forma (`boa_forma`)
**Resumo mecânico**: O personagem tem um sistema cardiovascular mais saudável do que indica seu valor de HT. Esta vantagem pode ser comprada em dois níveis: Boa Forma: O personagem recebe um bônus de +1 em todos os testes...
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).

## Braços Adicionais (`bracos_adicionais`)
**Resumo mecânico**: Em GURPS, qualquer membro com o qual o personagem é capaz de manipular objetos é classificado como um braço, independente de sua localização ou aparência. Um braço normal consegue dar um golpe que cau...
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Cabeça Adicional (`cabeca_adicional`)
**Resumo mecânico**: O personagem tem mais de uma cabeça, com orelhas, olhos, boca, etc., completamente funcionais. Cada Cabeça Adicional apresenta uma Boca Adicional (pág. 45) e um nível de Rastreamento Ampliado (pág. 81...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Camaleão (`camaleao`)
**Resumo mecânico**: O personagem é capaz de mudar seu aspecto físico para se camuflar. Em qualquer situação onde ele não queira ser visto, ele recebe um bônus de +2 em Furtividade para cada nível da vantagem enquanto per...
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Camaleão Social (`camaleao_social`)
**Resumo mecânico**: O personagem sabe exatamente quando e o que dizer a pessoas consideradas socialmente superiores a ele. Isso o isenta de penalidades de reação impostos pelas diferenças em Hierarquia ou Status. Em situ...
**Plano de Automação:**
- **Reação**: Adicionar na engine de reações (criar `ReactionRuleRegistry` ou injetar em `CharacterRules.kt`) para aplicar o modificador de reação automaticamente.
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).

## Caminhar no Ar (`caminhar_no_ar`)
**Resumo mecânico**: Ar, fumaça e outros gases são como terra sólida sob os pés do personagem — o que lhe permite andar e subir e descer "escadas invisíveis" com o mesmo Deslocamento que ele tem em terra. Essa habilidade ...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Caminhar sobre Líquidos (`caminhar_sobre_liquidos`)
**Resumo mecânico**: O personagem é capaz de caminhar sobre a superfície de qualquer líquido como se fosse chão sólido. Ele se move com o mesmo Deslocamento que tem em terra. No entanto, isso não o protege de qualquer dan...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Canalização (`canalizacao`)
**Resumo mecânico**: O personagem pode se tornar um condutor para o mundo espiritual, permitindo que os seres que habitam esse lugar falem através dele. Para isso, ele precisa entrar em transe, o que exige um minuto de co...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Carga Útil (`carga_util`)
**Resumo mecânico**: O personagem é capaz de transportar carga ou passageiros dentro de seu próprio corpo! Essa pode ser uma característica superficial (ex.: uma “bolsa de carne” implantada cirurgicamente ou uma bolsa nat...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Carisma (`carisma`)
**Resumo mecânico**: O personagem tem uma aptidão natural para impressionar e liderar outras pessoas. Qualquer um pode conseguir um carisma ilusório por meio da boa aparência, boas maneiras e inteligência, mas o carisma r...
**Plano de Automação:**
- **Reação**: Adicionar na engine de reações (criar `ReactionRuleRegistry` ou injetar em `CharacterRules.kt`) para aplicar o modificador de reação automaticamente.
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Clarissenciência (`clarissenciencia`)
**Resumo mecânico**: O personagem pode projetar seus sentidos a um ponto a até 10 m. Requer concentração e teste de IQ. Custa 50 pontos. Limitações: Clariaudiência (-30%), Clarissima (-60%), Clarividência (-10%), PES (-10...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Clericato (`clericato`)
**Resumo mecânico**: O personagem foi ordenado sacerdote de alguma religião reconhecida. Um clérigo tem alguns poderes e privilégios que um leigo não tem, entre os quais estão a autoridade para presidir casamentos, funera...
**Plano de Automação:**
- **Reação**: Adicionar na engine de reações (criar `ReactionRuleRegistry` ou injetar em `CharacterRules.kt`) para aplicar o modificador de reação automaticamente.
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).

## Companheiro Animal (`companheiro_animal`)
**Resumo mecânico**: Talento para perícias relacionadas a animais. Custa 5 pontos/nível....
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Controle da Mente (`controle_da_mente`)
**Resumo mecânico**: O personagem é capaz de dominar mentalmente pessoas que ele consegue ver ou tocar. Para usar esta habilidade, o personagem tem que se concentrar durante um segundo e, em seguida, fazer uma Disputa Ráp...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Controle do Metabolismo (`controle_do_metabolismo`)
**Resumo mecânico**: O personagem é capaz de controlar normalmente funções biológicas involuntárias, como pulsação, fluxo sanguíneo, digestão e respiração.

Cada nível desta vantagem concede um bônus de +1 em qualquer tes...
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Controle Térmico (`controle_termico`)
**Resumo mecânico**: O personagem é capaz de alterar a temperatura ambiente.

O aquecimento ou esfriamento está limitado a 10 °C por nível e ocorre a uma taxa de 1 °C por nível a cada segundo de concentração.

O personage...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Crescimento (`crescimento`)
**Resumo mecânico**: O personagem é capaz de crescer realmente crescer muito! Na medida em que o personagem cresce, sua ST tem de crescer também (senão ele cairia devido ao próprio peso). O equipamento do personagem não m...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Cura (`cura`)
**Resumo mecânico**: O personagem é capaz de curar outras pessoas — ele precisa manter contato físico com o alvo. Para ativar o poder, ele precisa se concentrar por um segundo e obter um sucesso num teste de IQ. O teste s...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Curandeiro (`curandeiro`)
**Resumo mecânico**: Talento para perícias médicas. Custa 10 pontos/nível....
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Dedos Verdes (`dedos_verdes`)
**Resumo mecânico**: Talento para perícias de botânica e cultivo. Custa 5 pontos/nível....
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Defesas Ampliadas: Aparar Ampliado (`defesas_ampliadas_aparar_ampliado`)
**Resumo mecânico**: Bônus de +1 no Aparar. Pode ser para mãos nuas (5 pts), uma perícia específica (5 pts) ou todas as manobras Aparar (10 pts)....
**Plano de Automação:**
- **Combate**: Criar regra que sobrescreva `getDodgeModifier`, `getParryModifier` ou `getBlockModifier` no `TraitRuleRegistry.kt`.

## Defesas Ampliadas: Bloqueio Ampliado (`defesas_ampliadas_bloqueio_ampliado`)
**Resumo mecânico**: O personagem recebe um bônus de +1 no valor de seu Bloqueio quando usar a perícia Capa ou Escudo. O personagem tem que se especializar em uma defesa específica de Bloqueio. 5 pontos....
**Plano de Automação:**
- **Combate**: Criar regra que sobrescreva `getDodgeModifier`, `getParryModifier` ou `getBlockModifier` no `TraitRuleRegistry.kt`.

## Defesas Ampliadas: Esquiva Ampliada (`defesas_ampliadas_esquiva_ampliada`)
**Resumo mecânico**: O personagem recebe um bônus de +1 no valor de sua Esquiva. 15 pontos....
**Plano de Automação:**
- **Combate**: Criar regra que sobrescreva `getDodgeModifier`, `getParryModifier` ou `getBlockModifier` no `TraitRuleRegistry.kt`.

## Deslocamento Ampliado (`deslocamento_ampliado`)
**Resumo mecânico**: O personagem é realmente muito rápido! Cada nível de Deslocamento Ampliado dobra sua velocidade máxima em um determinado ambiente: Ar, Solo, Espaço ou Água. Também é possível adquirir meio nível de De...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Destemor (`destemor`)
**Resumo mecânico**: Não é fácil assustar nem intimidar o personagem! Adicione o nível de Destemor do personagem ao atributo Vontade toda vez que ele for fazer uma Verificação de Pânico ou precisar resistir à perícia Inti...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Destreza Manual Elevada (`destreza_manual_elevada`)
**Resumo mecânico**: O personagem possui uma habilidade motora notável. Cada nível (até no máximo 4) concede um bônus de +1 na DX para tarefas que exigem um toque delicado. Isso inclui todos os testes de DX das perícias A...
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Deslocamento Aquático (`deslocamento_aquatico`)
**Resumo mecânico**: O Deslocamento aquático normalmente é igual ao Deslocamento Básico/5, arredondado para baixo. É possível aumentar o Deslocamento aquático diretamente a um custo de 5 pontos de personagem por metro/seg...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Difícil de Subjugar (`dificil_de_subjugar`)
**Resumo mecânico**: O personagem é difícil de nocautear. Cada nível em Difícil de Subjugar concede um bônus de +1 nos testes de HT feitos para verificar se o personagem evita a inconsciência — seja por ferimentos, drogas...
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Dobra (`dobra`)
**Resumo mecânico**: O personagem tem a habilidade de se teleportar, viajando de um ponto a outro sem se deslocar pelo espaço entre eles. Para isso, ele precisa enxergar seu destino com os próprios olhos, vê-lo remotament...
**Plano de Automação:**
- **Atributos**: Ajustar os cálculos base de PV, PF ou atributos secundários no `CharacterRules.kt` (interceptando a vantagem para somar ao total base).

## Dominação (`dominacao`)
**Resumo mecânico**: O personagem é capaz de “infectar” outras pessoas com uma condição sobrenatural — vampirismo, licantropia, etc. — e exercer controle absoluto sobre elas. Essa característica é apropriada apenas a sere...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Duplicação (`duplicacao`)
**Resumo mecânico**: O personagem é capaz de dividir seu corpo em duas ou mais Duplicatas, sendo que cada uma delas possui todos os poderes e conhecimentos do original. As duplicatas não possuem cópias do equipamento, a m...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Durabilidade Sobrenatural (`durabilidade_sobrenatural`)
**Resumo mecânico**: Da mesma forma que um vampiro ou um assassino psicótico de um filme de terror, o personagem é capaz de “ignorar” a maioria dos ferimentos.

As lesões consomem PV, como sempre, e o personagem pode ser ...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Duro de Matar (`duro_de_matar`)
**Resumo mecânico**: É incrivelmente difícil matar o personagem.

Cada nível nesta vantagem concede um bônus de +1 nos testes de HT feitos para ver se o personagem consegue sobreviver quando seu número de Pontos de Vida é...
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).
- **Atributos**: Ajustar os cálculos base de PV, PF ou atributos secundários no `CharacterRules.kt` (interceptando a vantagem para somar ao total base).
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## DX Braçal (`dx_bracal`)
**Resumo mecânico**: Um ou mais braços do personagem têm uma DX adicional em relação ao restante do corpo.

Esse valor se aplica somente às ações feitas com os braços ou mãos em questão — ele não afeta a Velocidade Básica...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Elasticidade (`elasticidade`)
**Resumo mecânico**: O personagem é capaz de esticar o próprio corpo em qualquer direção.

Cada nível de Elasticidade permite que o personagem aumente seu MT efetivo em +1 para qualquer parte do corpo sem fazer o mesmo co...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Empatia (`empatia`)
**Resumo mecânico**: O personagem tem uma “sensibilidade” para as outras pessoas.

Quando conhece ou encontra alguém depois de uma longa separação, ele pode pedir ao Mestre que faça um teste de IQ em seu lugar.

O Mestre ...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Encolhimento (`encolhimento`)
**Resumo mecânico**: O personagem é capaz de encolher à vontade.

Cada nível desta vantagem permite ao personagem alterar seu modificador de tamanho em -1, à razão de -1 MT por segundo.

O personagem volta ao tamanho norm...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Equilíbrio Perfeito (`equilibrio_perfeito`)
**Resumo mecânico**: Em condições normais, o personagem sempre consegue manter o equilíbrio, independente de quão estreita é a superfície sobre a qual ele está caminhando.

Isso permite ao personagem caminhar sobre uma co...
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).

## Escorregadio (`escorregadio`)
**Resumo mecânico**: O personagem é difícil de segurar.

Ele pode ser viscoso, ter moléculas muito lisas ou estar cercado por um campo de força que evita fricção.

Cada nível nesta característica, até no máximo cinco, con...
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Escudo Mental (`escudo_mental`)
**Resumo mecânico**: O personagem possui um “escudo” que o avisa ou defende de ataques mentais.

Adicione o nível de Escudo Mental do personagem à sua IQ ou Vontade sempre que ele for resistir a uma vantagem com a limitaç...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Espinhos (`espinhos`)
**Resumo mecânico**: O personagem tem espinhos afiados, como os de um porco-espinho ou de uma equidna, localizados em partes estratégicas do corpo.

Trata-se de uma arma defensiva com a função de desencorajar possíveis ag...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Expectativa de Vida Ampliada (`expectativa_de_vida_ampliada`)
**Resumo mecânico**: Um indivíduo com ciclo de vida médio atinge a maturidade aos 18 anos de idade e os efeitos do envelhecimento (pág. 444) aos 50, acelerando aos 70 e 90.

Cada nível de Expectativa de Vida Ampliada dobr...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Explorador (`explorador`)
**Resumo mecânico**: Talento para perícias de sobrevivência e exploração. Custa 10 pontos/nível....
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Fala Subsônica (`fala_subsonica`)
**Resumo mecânico**: O personagem é capaz de falar utilizando sons de frequência extremamente baixa.

Esta característica abrange Audição Subsônica.

A Fala Subsônica é lenta, funcionando com metade da velocidade usual.

...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Falar com Animais (`falar_com_animais`)
**Resumo mecânico**: O personagem é capaz de falar com os animais.

A qualidade das informações que ele recebe depende da IQ da criatura e da decisão do Mestre em relação ao que o animal tem a dizer.

Insetos e outras cri...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Familiaridade Cultural (`familiaridade_cultural`)
**Resumo mecânico**: O personagem está familiarizado com culturas diferentes da sua e não sofre a penalidade de -3 em perícias com componente cultural. Custa 1 ponto por cultura da mesma raça (ou raça semelhante) ou 2 pon...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Forma de Sombras (`forma_de_sombras`)
**Resumo mecânico**: O personagem é capaz de se transformar numa sombra bi-dimensional.

Isso permite que ele deslize pelo solo e pelas paredes, e até mesmo através das rachaduras mais estreitas, qualquer coisa com largur...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Habilidade Matemática (`habilidade_matematica`)
**Resumo mecânico**: Talento para perícias matemáticas e científicas. Custa 10 pontos/nível....
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Habilidade Musical (`habilidade_musical`)
**Resumo mecânico**: Talento para perícias musicais. Custa 5 pontos/nível....
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Hierarquia Honorária (`hierarquia_honoraria`)
**Resumo mecânico**: Título honorário sem autoridade efetiva, mas com benefícios sociais. Custa 1 ponto por nível. (pág. 29)...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Hipoalgia (Alto Limiar de Dor) (`hipoalgia_alto_limiar_de_dor`)
**Resumo mecânico**: O personagem é tão suscetível a dano quanto qualquer outra pessoa, mas não sente a dor com a mesma intensidade.

Ele nunca sofre as penalidades relacionadas ao choque quando é ferido.

Além disso, ele...
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Iluminado (`iluminado`)
**Resumo mecânico**: O personagem é um “Illuminatus” no sentido original da palavra — ele é iluminado.

Ele sabe intuitivamente o que está acontecendo ao seu redor.

O personagem é capaz de reconhecer outros Illuminati im...
**Plano de Automação:**
- **Reação**: Adicionar na engine de reações (criar `ReactionRuleRegistry` ou injetar em `CharacterRules.kt`) para aplicar o modificador de reação automaticamente.
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).

## Impossível de Matar (`impossivel_de_matar`)
**Resumo mecânico**: O personagem não morre facilmente. Nível 1: sobrevive até -10×PV (50 pts). Nível 2: vira esqueleto indestrutível (100 pts). Nível 3: vira fantasma (150 pts). Limitações: Calcanhar de Aquiles (-10% a -...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Infravisão (`infravisao`)
**Resumo mecânico**: O personagem consegue enxergar frequências de infravermelho, o que lhe permite detectar variações de temperatura.

Com esta vantagem, um personagem pode lutar sem sofrer penalidades mesmo sob escuridã...
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).

## Insubstancialidade (`insubstancialidade`)
**Resumo mecânico**: O personagem é capaz de se tornar intangível e atravessar objetos sólidos como se eles não existissem. Nesse estado, a gravidade não afeta o personagem e ele é capaz de se mover em qualquer direção co...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Interposição (`interposicao`)
**Resumo mecânico**: O personagem é capaz de atravessar uma determinada matéria sólida como se ela não existisse.

Ele não abre uma passagem atrás dele.

Seus observadores simplesmente o veem “derreter” na superfície e de...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Investidura de Poder (`investidura_de_poder`)
**Resumo mecânico**: Uma divindade — deus, lorde demoníaco, grande espírito, poder cósmico etc. — investiu o personagem de poderes para operar mágicas “clericais”.

Adicione o nível de Investidura de Poder à IQ do persona...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Invisibilidade (`invisibilidade`)
**Resumo mecânico**: O personagem é invisível.

Ao contrário da maioria das vantagens, esta vantagem está “sempre ativa”, a menos que o jogador compre uma ampliação especial.

O personagem faz barulho, deixa pegadas e exa...
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).

## Lamentável (`lamentavel`)
**Resumo mecânico**: Alguma coisa na aparência do personagem faz com que as pessoas sintam pena e vontade de cuidar dele. Ele recebe um bônus de +3 em todos os testes de reação feitos por aqueles que o considerarem fraco,...
**Plano de Automação:**
- **Reação**: Adicionar na engine de reações (criar `ReactionRuleRegistry` ou injetar em `CharacterRules.kt`) para aplicar o modificador de reação automaticamente.
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).

## Leitura da Mente (`leitura_da_mente`)
**Resumo mecânico**: O personagem pode bisbilhotar os pensamentos superficiais de outras pessoas.

Ele precisa ser capaz de ver ou tocar o alvo e tem que se concentrar durante um segundo.

Depois disso, deve fazer uma Dis...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Língua Ferina (`lingua_ferina`)
**Resumo mecânico**: O personagem é capaz de usar réplicas espirituosas para atordoar seus oponentes durante um combate.

Isso não exige uma manobra de combate, pois a fala é uma ação livre (pág. 363).

Faça uma Disputa R...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Membrana Nictitante (`membrana_nictitante`)
**Resumo mecânico**: O personagem tem lentes transparentes sobre os olhos que ele é capaz de abrir e fechar como uma pálpebra.

Isso permite que ele enxergue normalmente sob a água e protege os olhos da areia, de substânc...
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Memória Fotográfica (`memoria_fotografica`)
**Resumo mecânico**: O personagem tem uma memória excepcionalmente boa.

Qualquer pessoa pode fazer um teste de IQ para tentar relembrar eventos passados.

Quanto melhor o resultado do teste, mais verdadeira será a lembra...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Mente Segmentada (`mente_segmentada`)
**Resumo mecânico**: A coordenação mental do personagem efetivamente concede a ele mais de uma mente.

Cada uma dessas mentes ou “compartimentos” funciona de modo independente e com capacidade total.

As segmentações da m...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Mente Segmentada (Controles) (`mente_segmentada_controles`)
**Resumo mecânico**: Cada nível compra um conjunto de controles.

Esses controles permitem que o operador realize suas próprias manobras físicas ou mentais usando as habilidades do veículo, como Ataque Inato ou Radar, con...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Mente Segmentada (Controles Dedicados) (`mente_segmentada_controles_dedicados`)
**Resumo mecânico**: Cada conjunto de controles executa uma tarefa específica.

Exemplo: “artilharia da retaguarda”.

O indivíduo que estiver tripulando o veículo não pode fazer mais nada.

Custo: 10 pontos por nível....
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Metabolismo Impoluto (`metabolismo_impoluto`)
**Resumo mecânico**: Corpo produz detritos mínimos, sem mau hálito, transpiração excessiva. Penalidade de -1 para rastreamento por faro, +1 em reações em ambientes fechados. 1 ponto. (pág. 101)...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Noção do Perigo (`nocao_do_perigo`)
**Resumo mecânico**: O personagem sente perigo iminente. Custa 15 pontos. Limitação: PES (-10%)....
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Noção Tridimensional do Espaço (`nocao_tridimensional_do_espaco`)
**Resumo mecânico**: Bônus do Senso de Direção, +1 em Pilotagem e +2 em Acrobacia, Queda Livre e Navegação (Espaço). Custa 10 pontos....
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## NT Alto (`nt_alto`)
**Resumo mecânico**: O NT pessoal do personagem é maior que o NT da campanha. Ele pode começar a aventura com perícias relacionadas a equipamentos de NT menor ou igual ao seu NT pessoal. Isso é mais útil se ele também tiv...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Obscurecer (`obscurecer`)
**Resumo mecânico**: O personagem produz um efeito que efetivamente “bloqueia” um determinado sentido, fazendo com que seja difícil detectá-lo e tudo ao redor dele.

É necessário especificar o sentido afetado.

Isso pode ...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Obstinado (`obstinado`)
**Resumo mecânico**: O personagem realmente sabe como se concentrar.

Ele recebe um bônus de +3 nos testes de habilidade para qualquer tarefa mental extensa na qual precise se concentrar, independente de todas as outras a...
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).

## Oflato Discriminatório (`oflato_discriminatorio`)
**Resumo mecânico**: O olfato do personagem é muito mais aguçado do que o olfato normal dos seres humanos e registra odores diferentes para praticamente tudo que ele encontrar.

Isso permite que o personagem reconheça pes...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Ótima Forma (`otima_forma`)
**Resumo mecânico**: Ótima Forma: O personagem recebe um bônus de +2 em todos os testes de HT (para manter a consciência, evitar a morte, resistir a doenças e venenos, etc.). Isso não melhora o valor da HT nem das perícia...
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).

## Padrão de Tempo Alterado (`padrao_de_tempo_alterado`)
**Resumo mecânico**: O ritmo de percepção do tempo do personagem é maior do que o de um ser humano normal.

O primeiro nível desta vantagem permite que a percepção do tempo do personagem seja duas vezes mais veloz que o n...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Paladar Discriminatório (`paladar_discriminatorio`)
**Resumo mecânico**: Este talento funciona da mesma maneira que Olfato Discriminatório (pág. 74), mas amplia o sentido do paladar, não permitindo rastreamento.

Para analisar uma substância, o personagem deve ingerir uma ...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Paladar/Olfato Apurado (`paladar_olfato_apurado`)
**Resumo mecânico**: Sentido Aguçado para paladar e olfato; +1 por nível em testes. Custa 2 pontos/nível. (pág. 88)...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Pele Elástica (`pele_elastica`)
**Resumo mecânico**: O personagem é capaz de alterar sua pele e feições, mas não as vestimentas ou maquiagem, para imitar outros membros de sua própria raça ou de outra raça muito semelhante.

O processo leva 10 segundos ...
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).

## Pendulear (`pendulear`)
**Resumo mecânico**: O personagem pode se locomover pendurado, com bônus de +2 em Escalada. Custa 5 pontos....
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).

## Pernas Adicionais (`pernas_adicionais`)
**Resumo mecânico**: Se o personagem é capaz de andar com um determinado membro, mas não consegue utilizá-lo para manipular objetos, este membro é considerado uma perna para o sistema GURPS.

Se quiser pernas que se dobra...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Perspicácia Comercial (`perspicacia_comercial`)
**Resumo mecânico**: Talento para perícias comerciais. Custa 10 pontos/nível....
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Por Dentro da Moda (`por_dentro_da_moda`)
**Resumo mecânico**: O visual do personagem está sempre um passo à frente da maioria das pessoas. Ele tem a capacidade de criar moda a partir de materiais baratos e comuns. O personagem recebe um bônus de +1 nos testes de...
**Plano de Automação:**
- **Reação**: Adicionar na engine de reações (criar `ReactionRuleRegistry` ou injetar em `CharacterRules.kt`) para aplicar o modificador de reação automaticamente.
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).

## Possessão (`possessao`)
**Resumo mecânico**: O personagem pode transferir sua consciência para outro corpo. Custa 100 pontos. Ampliações: Assimilação (+10%), Crônico (+20%), Telecontrole (+50%), Troca Mental (+10%). Limitações: Digital (-40%), E...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Pouco Sono (`pouco_sono`)
**Resumo mecânico**: O personagem precisa de menos sono que a maioria das pessoas.

Um humano comum necessita de 8 horas de sono por noite.

Cada nível desta vantagem, até no máximo quatro níveis, permite subtrair uma hor...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Prender a Respiração (`prender_a_respiracao`)
**Resumo mecânico**: O personagem é perito em prender o fôlego.

Cada nível desta vantagem dobra o período que ele consegue permanecer sem respirar.

Consulte Prender o Fôlego (pág. 355).

Humanos normais não podem adquir...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Propósito Maior (`proposito_maior`)
**Resumo mecânico**: O personagem é compelido a superar seus limites em busca de um objetivo específico.

O jogador deve especificar exatamente esse objetivo, da mesma forma que faria ao adquirir a desvantagem Código de H...
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).

## Psicometria (`psicometria`)
**Resumo mecânico**: O personagem é capaz de sentir a história de um lugar ou de um objeto inanimado, incluindo sua utilização, a personalidade do usuário e acontecimentos marcantes relacionados a ele.

Este poder costuma...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Queda de Gato (`queda_de_gato`)
**Resumo mecânico**: O personagem subtrai automaticamente 5 metros de qualquer queda.

Considere esse efeito como um sucesso automático em um teste de Acrobacia.

Além disso, um sucesso em um teste de DX reduz o dano de q...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Rastreamento Ampliado (`rastreamento_ampliado`)
**Resumo mecânico**: O personagem é capaz de “rastrear” mais de um alvo.

Isso pode ocorrer por meio de uma estrutura sensorial embutida ou olhos capazes de girar independentemente um do outro, como os de um camaleão.

Um...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Reconhecimento Social (`reconhecimento_social`)
**Resumo mecânico**: O personagem é membro de uma classe, raça, sexo ou outro grupo muito respeitado pela sociedade.

Para ser considerada uma vantagem, essa condição precisa ser óbvia para qualquer pessoa que conheça o p...
**Plano de Automação:**
- **Reação**: Adicionar na engine de reações (criar `ReactionRuleRegistry` ou injetar em `CharacterRules.kt`) para aplicar o modificador de reação automaticamente.
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).

## Recuperação Acelerada (`recuperacao_acelerada`)
**Resumo mecânico**: Bônus de +5 em HT para recuperar PV e lesões. Custa 5 pts (HT 10+). Muito Acelerada: cura 2 PV por sucesso, custa 15 pts (HT 12+)....
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).

## Reflexos em Combate (`reflexos_em_combate`)
**Resumo mecânico**: O personagem tem reações fantásticas e raramente fica surpreso por mais do que alguns segundos.

Ele recebe:

- Bônus de +1 em todas as jogadas de defesa ativa (v. Defendendo, pág. 374).
- Bônus de +1...
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).

## Reivindicar Hospitalidade (`reivindicar_hospitalidade`)
**Resumo mecânico**: O personagem pertence a um grupo social que incentiva seus membros a ajudarem uns aos outros.

Quando está longe de casa, ele pode recorrer a outros membros desse grupo para obter alimento, abrigo e a...
**Plano de Automação:**
- **Reação**: Adicionar na engine de reações (criar `ReactionRuleRegistry` ou injetar em `CharacterRules.kt`) para aplicar o modificador de reação automaticamente.
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).

## Renda Própria (`renda_propria`)
**Resumo mecânico**: Fonte de renda que não exige trabalho; 1% dos recursos iniciais por nível (máx. 20%). Custa 1 ponto/nível. (pág. 26)...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Resistência a Dano (`resistencia_a_dano`)
**Resumo mecânico**: O próprio corpo do personagem possui um valor de Resistência a Dano (RD).

Subtraia esse valor do dano causado por qualquer ataque físico ou de energia depois de aplicar a RD de armaduras artificiais,...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Resistência à Pressão (`resistencia_a_pressao`)
**Resumo mecânico**: Todo personagem possui uma pressão nativa. Para a maioria dos seres humanos, essa pressão corresponde a 1 atmosfera, equivalente à pressão atmosférica da Terra.

Possuir uma pressão nativa diferente d...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Restaurar Membros (`restaurar_membros`)
**Resumo mecânico**: O personagem é capaz de regenerar membros e órgãos perdidos.

Partes menores do corpo, como orelhas, dedos das mãos ou dos pés, garras, pontas de tentáculos e estruturas semelhantes, regeneram-se em 1...
**Plano de Automação:**
- **Atributos**: Ajustar os cálculos base de PV, PF ou atributos secundários no `CharacterRules.kt` (interceptando a vantagem para somar ao total base).

## Retenção (`retencao`)
**Resumo mecânico**: O personagem possui um ataque capaz de manter o alvo preso no lugar.

Ao adquirir a vantagem, o jogador deve definir a aparência e o funcionamento do efeito.

Exemplos incluem:

- Vinhas que se enrola...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Riqueza (`riqueza`)
**Resumo mecânico**: Riqueza acima da média é vantagem, abaixo é desvantagem. Níveis: Falido (-25), Pobre (-15), Batalhador (-10), Médio (0), Confortável (10), Rico (20), Muito Rico (30), Podre de Rico (50),...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Rosto Sincero (`rosto_sincero`)
**Resumo mecânico**: Parece honesto/inofensivo; não é revistado sem razão, +1 em Dissimulação para "parecer inocente". 1 ponto. (pág. 101)...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Saltador (`saltador`)
**Resumo mecânico**: O personagem é capaz de viajar no tempo ou para mundos paralelos simplesmente desejando realizar um salto.

Ao adquirir a vantagem, é necessário escolher um dos tipos:

- Saltador (Tempo).
- Saltador ...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Senso de Direção (`senso_de_direcao`)
**Resumo mecânico**: O personagem possui um excelente senso de orientação e localização.

Ele sempre sabe onde fica o norte e consegue refazer qualquer trajeto percorrido nos últimos 30 dias, independentemente de quão com...
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).

## Sentido de Monitoramento (`sentido_de_monitoramento`)
**Resumo mecânico**: O personagem é capaz de emitir energia, analisar seu reflexo e construir uma imagem tridimensional dos arredores.

Esse sentido permite determinar tamanho, forma e posição dos objetos sem depender de ...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Sentido Protegido (`sentido_protegido`)
**Resumo mecânico**: Um dos sentidos de longa distância do personagem possui proteção especial contra sobrecargas sensoriais.

Esse sentido adapta-se rapidamente a estímulos extremos, permitindo que o personagem continue ...
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Serendipidade (`serendipidade`)
**Resumo mecânico**: O personagem possui uma extraordinária tendência a estar no lugar certo na hora certa.

Cada nível de Serendipidade concede ao personagem o direito a uma coincidência favorável por sessão de jogo.

Es...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Silêncio (`silencio`)
**Resumo mecânico**: O personagem consegue se mover, respirar e realizar ações sem produzir ruídos perceptíveis.

Cada nível de Silêncio concede bônus em testes de Furtividade relacionados à detecção auditiva.

## Benefíc...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Sonda Mental (`sonda_mental`)
**Resumo mecânico**: O personagem é capaz de penetrar profundamente na mente de outra pessoa e forçá-la a revelar uma informação específica.

Ao contrário da Leitura da Mente, que apenas capta pensamentos, Sonda Mental pe...
**Plano de Automação:**
- **Perícias (NH)**: Implementar uma regra customizada em `TraitRuleRegistry.kt` que afete o NH de usos específicos (ex: certas mágicas ou ataques).

## ST Braçal (`st_bracal`)
**Resumo mecânico**: Um ou mais braços do personagem possuem uma Força superior à do restante do corpo.

Essa ST adicional se aplica apenas a ações realizadas com os braços ou mãos beneficiados.

Ela pode ser utilizada pa...
**Plano de Automação:**
- **Atributos**: Ajustar os cálculos base de PV, PF ou atributos secundários no `CharacterRules.kt` (interceptando a vantagem para somar ao total base).

## ST de Golpe (`st_de_golpe`)
**Resumo mecânico**: O personagem é capaz de aplicar golpes significativamente mais poderosos do que sua Força normal indicaria.

A ST de Golpe representa força especializada para causar dano em combate, sem aumentar a fo...
**Plano de Automação:**
- **Atributos**: Ajustar os cálculos base de PV, PF ou atributos secundários no `CharacterRules.kt` (interceptando a vantagem para somar ao total base).
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## ST de Levantamento (`st_de_levantamento`)
**Resumo mecânico**: O personagem possui uma capacidade extraordinária de erguer, empurrar e mover cargas muito superiores ao que sua massa corporal sugeriria.

Esta vantagem representa força especializada para movimentaç...
**Plano de Automação:**
- **Atributos**: Ajustar os cálculos base de PV, PF ou atributos secundários no `CharacterRules.kt` (interceptando a vantagem para somar ao total base).
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Status (`status`)
**Resumo mecânico**: Status representa a posição social do personagem dentro de sua cultura e sociedade.

Ele mede o lugar ocupado pelo personagem na hierarquia social, refletindo prestígio, influência, privilégios e pode...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Super Escalada (`super_escalada`)
**Resumo mecânico**: O personagem possui uma capacidade excepcional de escalar superfícies com rapidez e eficiência muito superiores às de uma pessoa comum.

Cada nível de Super Escalada aumenta a velocidade de deslocamen...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Super Salto (`super_salto`)
**Resumo mecânico**: O personagem é capaz de realizar saltos muito além dos limites normais de sua espécie.

Cada nível de Super Salto dobra tanto a distância horizontal quanto a altura máxima que ele consegue atingir ao ...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Super Sorte (`super_sorte`)
**Resumo mecânico**: O personagem não é apenas extremamente sortudo. Ele possui uma capacidade limitada de manipular diretamente a probabilidade e o destino.

Uma vez a cada hora de jogo, o jogador pode escolher o resulta...
**Plano de Automação:**
- **Perícias (NH)**: Implementar uma regra customizada em `TraitRuleRegistry.kt` que afete o NH de usos específicos (ex: certas mágicas ou ataques).

## Talento Instintivo (`talento_instintivo`)
**Resumo mecânico**: O personagem é capaz de fazer coisas que não sabe explicar. Uma vez a cada sessão de jogo por nível desta vantagem, ele pode tentar fazer um teste contra qualquer perícia, utilizando o valor do atribu...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Talento dos Pequeninos para Armas de Longa Distância (`talento_dos_pequeninos_para_armas_de_longa_distancia`)
**Resumo mecânico**: Arco, Arma de Arremesso (Dardo), Arma de Arremesso (Faca), Arma de Arremesso (Vara), Arremesso, e Funda. Bônus de reação: arqueiros e outros usuários de armas de longa distância. 5 pontos/nível. Apena...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Tato Apurado (`tato_apurado`)
**Resumo mecânico**: Bônus de +4 em tarefas que usam o tato. Custa 10 pontos....
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).

## Telecinese (`telecinese`)
**Resumo mecânico**: O personagem é capaz de manipular objetos à distância por meio de uma força invisível controlada conscientemente.

Essa força pode representar diversas origens, como:

- Psiquismo.
- Psicocinese.
- Ma...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Tolerância a Ferimentos(Difuso) (`tolerancia_a_ferimentos_difuso`)
**Resumo mecânico**: Difuso: O corpo do personagem é fluido ou fragmentado, composto por um enxame de entidades menores ou talvez de energia pura. Isso o torna imune a lesões incapacitantes e reduz o dano que ele sofre co...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Tolerância a Ferimentos(Não-Vivo) (`tolerancia_a_ferimentos_nao_vivo`)
**Resumo mecânico**: Não-Vivo: O corpo do personagem não é feito de carne viva. Ele sofre dano reduzido de ataques perfurantes e GdP, mas não é tão resistente quanto uma criatura Homogênea; v. Lesões em Alvos Difusos, Hom...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Tolerância a Ferimentos(Sem Cérebro) (`tolerancia_a_ferimentos_sem_cerebro`)
**Resumo mecânico**: Sem Cérebro: O cérebro do personagem — se ele tiver algum — está distribuído por todo o seu corpo ou não abriga sua consciência. Oponentes não podem usá-lo como alvo para causar dano adicional. Talvez...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Tolerância a Ferimentos(Sem Órgãos Vitais) (`tolerancia_a_ferimentos_sem_orgaos_vitais`)
**Resumo mecânico**: Sem Órgãos Vitais: O personagem não tem órgãos vitais (como, por exemplo, um coração ou um motor) que seus oponentes possam usar como alvo para causar dano adicional. Considere todos os golpes em “órg...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Tolerância à Temperatura (`tolerancia_a_temperatura`)
**Resumo mecânico**: O personagem possui uma resistência natural excepcional a temperaturas extremas.

Ele consegue suportar calor e frio muito além dos limites normais de sua espécie sem sofrer os efeitos fisiológicos as...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Tolerância ao Álcool (`tolerancia_ao_alcool`)
**Resumo mecânico**: Digere álcool com eficiência; pode beber indefinidamente sem efeitos nocivos, +2 em testes de HT relacionados ao alcoolismo. 1 ponto. (pág. 101)...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Toque Sensível (`toque_sensivel`)
**Resumo mecânico**: O personagem possui terminações táteis extremamente desenvolvidas, capazes de perceber detalhes físicos imperceptíveis para a maioria das pessoas.

Suas mãos, dedos ou órgãos equivalentes conseguem de...
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).

## Treinado por um Mestre (`treinado_por_um_mestre`)
**Resumo mecânico**: O personagem recebeu treinamento de um verdadeiro mestre das artes marciais ou alcançou esse nível de habilidade por mérito próprio.

Seu domínio do combate ultrapassa os limites normais da capacidade...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Ultraflexibilidade das Juntas (`ultraflexibilidade_das_juntas`)
**Resumo mecânico**: 

## Ultraflexibilidade das Juntas

Funciona da mesma maneira que o caso anterior, mas um pouco melhor.

O personagem não é capaz de se esticar ou espremer de maneira anormal, mas qualquer parte do se...
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).

## Ultravisão (`ultravisao`)
**Resumo mecânico**: O personagem é capaz de enxergar a faixa ultravioleta (UV) do espectro eletromagnético.

Isso permite perceber cores, padrões, rastros e detalhes invisíveis para a visão humana comum.

A luz ultraviol...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Venturoso (`venturoso`)
**Resumo mecânico**: O personagem parece atrair a sorte justamente quando decide fazer algo extremamente arriscado.

Quanto maior o perigo voluntariamente assumido, maiores são as chances de que tudo dê certo.

Venturoso ...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.

## Versátil (`versatil`)
**Resumo mecânico**: O personagem possui uma mente excepcionalmente criativa e inventiva.

Ele encontra soluções originais para problemas, cria ideias inovadoras com facilidade e consegue enxergar possibilidades que passa...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Vida Extra (`vida_extra`)
**Resumo mecânico**: O personagem possui a extraordinária capacidade de retornar à vida após ser morto.

Embora possa parecer que ele foi definitivamente destruído, alguma característica sobrenatural, tecnológica, cósmica...
**Plano de Automação:**
- **Dano**: Implementar `getDamageBonusPerDie` ou estender as `AttackOptions` em `TraitRuleRegistry.kt` para adicionar esse modificador de dano nativo.
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Visão 360 graus (`visao_360_graus`)
**Resumo mecânico**: O personagem possui um campo de visão completo ao seu redor, permitindo observar todas as direções simultaneamente.

Essa capacidade pode ser resultado de olhos adicionais, visão composta semelhante à...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Visão Aguçada (`visao_agucada`)
**Resumo mecânico**: +1 por nível em testes de Visão. Custa 2 pontos/nível. (pág. 88)...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Visão Hiperespectral (`visao_hiperespectral`)
**Resumo mecânico**: O personagem possui um sistema visual extremamente avançado capaz de perceber simultaneamente uma ampla faixa do espectro eletromagnético.

Sua visão integra informações provenientes do infravermelho,...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Visão Microscópica (`visao_microscopica`)
**Resumo mecânico**: O personagem é capaz de enxergar detalhes extremamente pequenos sem a necessidade de lentes, lupas ou microscópios.

Sua visão possui ampliação natural suficiente para examinar estruturas invisíveis a...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Visão Noturna (`visao_noturna`)
**Resumo mecânico**: Os olhos do personagem se adaptam rapidamente a ambientes pouco iluminados, permitindo enxergar melhor do que a maioria das pessoas em condições de baixa luminosidade.

Essa vantagem representa uma ca...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Visão Penetrante (`visao_penetrante`)
**Resumo mecânico**: O personagem é capaz de enxergar através de objetos sólidos, permitindo observar o que existe além de barreiras físicas.

Essa habilidade é frequentemente conhecida como 'visão raio-x', embora possa s...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Visão Periférica (`visao_periferica`)
**Resumo mecânico**: O personagem possui um campo de visão excepcionalmente amplo, permitindo enxergar muito além dos limites normais da visão humana.

Ele consegue observar uma grande área ao seu redor sem precisar virar...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Visão Telescópica (`visao_telescopica`)
**Resumo mecânico**: O personagem é capaz de ampliar visualmente objetos distantes como se possuísse binóculos integrados aos próprios olhos.

Essa capacidade permite observar detalhes a grandes distâncias sem a necessida...
**Plano de Automação:**
- **Escalonamento**: O bônus deve ser multiplicado pelo nível atual da vantagem.

## Visualização (`visualizacao`)
**Resumo mecânico**: Ao se visualizar realizando uma tarefa, o personagem é capaz de aumentar suas chances de sucesso.

Quanto mais sua imagem mental for fiel às circunstâncias verdadeiras, maior será o bônus.

A visualiz...
**Plano de Automação:**
- **Perícias**: Registrar em `TraitRuleRegistry.kt` e implementar `getSkillModifiers` para retornar o bônus na(s) perícia(s) associada(s).

## Voz Melodiosa (`voz_melodiosa`)
**Resumo mecânico**: O personagem tem uma voz clara, atraente e ressonante.

Sua fala é agradável de ouvir e naturalmente persuasiva, tornando mais fácil influenciar, impressionar ou entreter outras pessoas.

## Benefício...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Voz Penetrante (`voz_penetrante`)
**Resumo mecânico**: Voz que se destaca; +3 em testes de Audição para ser ouvido, +1 em Intimidação se surpreender com um berro. 1 ponto. (pág. 101)...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.

## Xeno-adaptabilidade (`xeno_adaptabilidade`)
**Resumo mecânico**: O personagem está familiarizado com uma grande variedade de culturas. Quando interage com elas, ele nunca sofre uma penalidade de -3 devido ao “desconhecimento cultural” descrito na seção Cultura (pág...
**Plano de Automação:**
- **Mecânica Geral**: Criar uma classe baseada em `TraitRule` que aplique os modificadores fixos mencionados na descrição às jogadas pertinentes.
