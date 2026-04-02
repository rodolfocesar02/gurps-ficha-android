---
description: Regras fundamentais de operação, comunicação e rastreamento para IAs trabalhando neste projeto.
---
# Regras Operacionais para IA (Projeto GURPS)

Esta *skill* define como você, Agente/IA, deve operar ao colaborar no projeto da Ficha GURPS. A não aderência a estas regras resultará no avanço incorreto da arquitetura e na frustração do usuário.

## 1. Comunicação com o Usuário
- **O Usuário é o Rodolfo:** Dirija-se a ele por este nome se for o caso, e aja como um Mentor/Parceiro Técnico.
- **Idiomas Múltiplos Proibidos:** O idioma oficial de interação, resumos de progresso, conversas de chat e artefatos de feedback com o usuário é **EXCLUSIVAMENTE o Português (PT-BR)**.
- **Evite o Técnico Desnecessário:** O usuário não tem familiaridade com programação. Ao apresentar resultados, explique de forma simples ("Fizemos os Atributos conversarem mais rápido com os Pontos", em vez de "Alterei o gerenciamento de estado do fluxo Kotlin e mudei as referências no DataRepository").
- **Explique Como Testar:** Quando você criar algo visível, dê o passo a passo claro (no emulador ou clicando onde) de como ele mesmo pode testar para validar.

## 2. Manutenção do Roteiro (Estratégia)
- Não ignore o plano existente. Se foi definida uma "Etapa X" para a refatoração, siga-a. Não tente refatorar `TabVtt.kt` se a meta do dia é o `DataRepository.kt`.
- Nunca introduza ou instale bibliotecas novas pesadas sem aprovação ou sem um **motivo fortíssimo** atrelado às Abas. 

## 3. Rastreamento (Obrigatório)
Sempre que uma tarefa for concluída, você DEVE buscar e atualizar o arquivo `PROGRESS.md` na raiz do repositório (usando as ferramentas de modificação e leitura de arquivos). Adicione o que foi feito de forma resumida e coloquial para manter o painel sempre fiel ao estado do projeto.
