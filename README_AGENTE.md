# Diretrizes para Novos Agentes (GURPS VTT e Ficha)

**ATENÇÃO AGENTE:** Leia este documento inteiramente ANTES de fazer qualquer alteração no código.

## Contexto do Projeto
Este projeto é um aplicativo de Ficha de RPG e VTT para o sistema **GURPS**, desenvolvido primordialmente em Android (Kotlin via Jetpack Compose/Views) com potencial integração em React no front-end VTT (conforme documentado nos arquivos da pasta).
O autor (Rodolfo) iniciou este projeto com forte apoio de IA. **Rodolfo NÃO É UM PROGRAMADOR EXPERIENTE e NÃO FLUI EM INGLÊS.**
Sua principal responsabilidade ao assumir qualquer parte do projeto é agir como Senior Developer, comunicando-se EXCLUSIVAMENTE em Português simples (sem jargões inúteis) e sendo meticuloso para **NÃO QUEBRAR O BUILD**.

## Estrutura da Ficha (As Abas)
O projeto funciona em torno de várias "Abas" principais. Se você for alterar a interface ou o comportamento, saiba o escopo de cada uma:
*   **GERAL:** Atributos Primários (ST, DX, IQ, HT), Secundários, Características Derivadas e um resumo numérico e de pontos de todas as outras abas.
*   **TRAÇOS:** Vantagens, Desvantagens, Qualidades, Peculiaridades, Modelo Racial, e resumo de pontos gastos nestes itens. Principal motor visual em `TraitDialogs.kt`.
*   **PERÍCIAS:** Sistema de perícias padrão, técnicas de combate, criação de perícias customizadas e um resumo.
*   **MAGIAS:** Catálogo de magias com **Motor Modo Alvo** (`NexusArcanoEngine.kt`). Este modo rastreia pré-requisitos, facilitando a navegação na árvore de habilidades arcanas e somando resumos de pontos.
*   **EQUIPAMENTOS:** Gerenciador de armas de fogo, combate corpo-a-corpo, armaduras, criação de itens genéricos, escudos, e calculador de Carga (peso) e Dinheiro do personagem.
*   **DEFESAS:** Controla estatísticas reativas: Esquiva, Apara (Block/Parry) e Bloqueio.
*   **ROLAGEM:** Aba de fim de formulário (`TabRolagem.kt`). Funciona como um painel agregador (Dashboard) onde os dados do personagem e armas ganham vida permitindo "rolar" contra as estatísticas, somando modificadores pontuais positivos e negativos da mesa.

## 6 Arquivos Problemáticos (Dívida Técnica Atual)
Estes são arquivos massivos que estamos num processo de Refatoração por Etapas. Você não deve mexer na estrutura core deles a não ser que faça parte de uma etapa aprovada pelo Rodolfo:
1. `DataRepository.kt` (>1900 linhas)
2. `NexusArcanoEngine.kt` (>2000 linhas)
3. `FichaViewModel.kt` (>2000 linhas)
4. `TabVtt.kt` (>2200 linhas)
5. `TabRolagem.kt` (>3000 linhas)
6. `TraitDialogs.kt` (>3000 linhas)

## Instruções Padrão
Sempre carregue e honre as *skills* e políticas definidas na pasta `.agent/skills/`:
1. Mantenha os rastreador de progresso (`PROGRESS.md`) sempre atualizado.
2. Certifique-se de testar seu código com o `./gradlew build` para evitar entregar código não compilável ao Rodolfo.
3. Se um arquivo estiver passando de 1000 linhas na sua refatoração, pare. Crie um novo componente ou extraia lógicas. O limite estrutural alvo para novos arquivos é <= 1000 linhas.
