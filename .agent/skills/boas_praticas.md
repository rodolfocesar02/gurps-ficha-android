---
description: Regras técnicas sobre tamanhos de arquivos, modularização de UI e arquitetura para a Ficha GURPS.
---
# Boas Práticas - Refatoração Ficha GURPS

## 1. Regra de Limite (1000 Linhas)
- O projeto identificou enormes dívidas técnicas devido ao acúmulo de código.
- A regra principal deste projeto é: **ABSOLUTAMENTE NENHUM ARQUIVO NOVO DEVE ULTRAPASSAR 1000 LINHAS.**
- Se o projeto for escalonar e uma Aba exigir mais de 1000 linhas, você DEVE dividi-la em componentes (excesso visual vai para `components/`, excesso lógico vai para `useCases/` ou `helpers/`).

## 2. Princípio da Responsabilidade Única
- Uma classe de "Data Repository" lida com carregar JSON/Room, buscar e salvar dados brutos. Ela NÃO formata texto para visualização UI.
- Uma `ViewModel` expõe fluxos de UI (Flows) e não deve ter hardcode de regras matemáticas longas que possam ser terceirizadas para Engines/Calculadoras independentes.

## 3. UI Jetpack Compose
- Componentize exaustivamente as Abas. 
- Use os pacotes correspondentes. Em `ui/features/` separe cada "Tab" como seu próprio ecossistema.
- Use `Modifiers` de forma encadeada clara e consistente para não deixar as colunas e hierarquias ilegíveis.
