---
description: Regras arquiteturais e restrições absolutas para manipulação das mecânicas GURPS 4ª Edição neste projeto.
---
# ⚔️ Leis das Regras GURPS (Arquitetura)

Qualquer Inteligência Artificial lidando com as mecânicas deste repositório DEVE seguir estas leis para garantir a fidelidade ao sistema GURPS 4ª Edição.

## 1. Isolamento Matemático (Calculadoras Puras)
- **Zero Lógica na UI:** Fórmulas vitais (Ex: Esquiva = `Velocidade Básica + 3 + BD`) JAMAIS devem estar em arquivos `Tab*.kt`.
- **Domain Modules:** Use `CombatRules.kt` e `CharacterRules.kt` para matemática pura. A UI apenas exibe o número que o `FichaViewModel` (via Delegates) fornece.

## 2. A Lei do Vínculo de Combate (Contexto)
- **Dano vs Perícia:** Nunca calcule o dano de uma arma de forma isolada. O cálculo deve sempre receber o `periciaId` selecionado na UI. Isso evita que bônus de vantagens (como Mestre de Armas) "vazem" para armas ou pericias incompatíveis.
- **Defesas Ativas e BD:** O Bônus de Defesa (BD) do escudo deve estar integrado ao cálculo final de Esquiva, Apara e Bloqueio de forma reativa. Se o escudo for desequipado, o valor deve cair instantaneamente em todas as defesas.

## 3. Integridade e Anti-Hardcode
- **Proibido Buscar por Nome:** JAMAIS faça `if (vantagem.nome == "Vampirismo")`. Use sempre o `definicaoId` ou `traitId` (ex: `mestre_de_armas`). Isso garante que o app funcione mesmo se o nome da vantagem mudar no JSON.
- **Registro de Vantagens:** Todo bônus vindo de uma vantagem deve passar pelo `TraitRuleRegistry`. Não crie "gambiarras" ou exceções fora deste sistema modular.

## 4. Unificação de Estado (Single Source of Truth)
- Atributos impactam todo o resto. Se o `HT` muda, os Pontos de Vida (HP) e a Velocidade Básica devem ser recalculados pelo motor de regras e notificar a UI via `StateFlow`.
- **Aba de Rolagem:** É o Hub central de estado. Ela deve refletir o estado atual do `Personagem` em tempo real, sem variáveis "clonadas" localmente que fiquem dessincronizadas.

---
**Objetivo:** Garantir que o aplicativo seja uma representação matemática PERFEITA do manual do GURPS 4ª Edição.
