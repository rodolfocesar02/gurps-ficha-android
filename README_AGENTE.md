# 🤖 Diretrizes para Novos Agentes (Ficha GURPS)

**ATENÇÃO AGENTE:** Este projeto possui uma cultura de desenvolvimento rígida focada em segurança e acessibilidade. Leia este documento e as skills em `.agent/skills/` antes de tocar no código.

## 🌟 O Projeto
Este é um aplicativo de Ficha de RPG para **GURPS 4ª Edição**, desenvolvido em Kotlin/Jetpack Compose.
O autor (**Rodolfo**) é o mentor do projeto, mas não é programador de profissão. **Comunique-se em Português Simples e Direto.**

---

## 🏛️ Estrutura das Abas (Fluxo do Usuário)
A ficha é dividida em abas lógicas. Importante: A aba de Defesa foi fundida à de Rolagem.
- **GERAL:** Atributos (ST, DX, IQ, HT) e Status (PV/PF).
- **TRAÇOS:** Vantagens e Desvantagens (Arquitetura modular em `TraitsRule`).
- **PERÍCIAS:** Perícias e Técnicas.
- **MAGIAS:** Motor Arcano (`NexusArcanoEngine.kt`) com pré-requisitos automáticos.
- **EQUIPAMENTOS:** Gestão de peso, custo e itens.
- **ROLAGEM:** **O Hub Unificado de Combate.** Centraliza Atributos, Defesas Ativas, Ataques e Dano.

---

## 🛡️ Regras Operacionais (Cultura Antigravity)
Todo Agente deve seguir o sistema de segurança do Rodolfo:
1. **Sistema de Lotes:** Divida tarefas em Lotes (ex: Lote 40, 41). Cada lote concluído **PRECISA** de um commit Git com o número do lote.
2. **Registro de Progresso:** Atualize o `PROGRESS.md` a cada commit com o hash e o que foi feito.
3. **Variante PraCego:** Toda mudança na UI deve suportar o modo acessibilidade (`isPraCegoVariant`). Verifique os rótulos de TalkBack!
4. **Fonte da Verdade:** SEMPRE consulte o `.agent/skills/Project_map_SKILL.md` para entender a arquitetura antes de sugerir mudanças.


---

## 📉 Dívida Técnica (Arquivos em Refatoração)
Limpamos muito o projeto! **TabRolagem, FichaViewModel e DataRepository** já estão abaixo da meta de 1000 linhas. As "três grandes" que ainda faltam são:
- `TabVtt.kt` (**2270 linhas**) - Integração Discord.
- `VantagemDialogs.kt` (**1534 linhas**) - Sendo modularizado para `TraitsRule`.
- `NexusArcanoEngine.kt` (**1070 linhas**) - Motor central de magias.

**Meta Estrutural:** Nenhum arquivo novo ou refatorado deve exceder **1000 linhas**. Se passar disso, extraia lógicas para componentes ou delegados.

---
**Objetivo Final:** Manter o código estável, modular e acessível para que o Rodolfo possa testar com confiança em cada atualização! ⚔️🛡️
