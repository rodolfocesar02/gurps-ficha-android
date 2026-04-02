---
description: Regras arquiteturais e restrições absolutas para manipulação das regras matemáticas e de estado do sistema GURPS.
---
# Isolamento do Sistema e Regras GURPS (Arquitetura)

Qualquer Inteligência Artificial lidando com a refatoração deste repositório DEVE se ater às três leis fundamentais do isolamento para a manutenção das mecânicas do de regras e do escalonamento a longo prazo:

## 1. Isolamento Matemático (Calculadoras Puras)
- **Não implemente cálculos brutos na UI:** Fórmulas vitais do sistema (Ex: Esquiva igual à `(Velocidade Básica) + 3`, ou Pontos para subir atributos) JAMAIS devem ser escritas dentro de "Abas" (Views/Fragments/Compose).
- **Domain Módules:** Use arquivos de classes e utilitários puros dentro de diretórios como `domain/` ou `calculators/` para efetuar Matemática do Sistema. Assim, a UI apenas requisita e recebe o número pronto.

## 2. Reatividade Centralizada
- No RPG, atributos impactam atributos. Se `HT` sobe, os Pontos de Vida (`HP`) obrigatoriamente mudam e sua pontuação da Ficha altera.
- Não devem haver variáveis soltas retidas localmente nas abas. Integre e force um padrão com `StateFlow` ou repositórios centralizados onde o valor, ao ser mudado em um lado, notifica o Maestro (`ViewModel`) que altera todo o reflexo matemático que as Abas apenas copiam. (Unidirectional Data Flow).

## 3. Integridade das Bases de Dados e Anti-Hardcode
- **Proibido "Chumbamento" (Hardcode):** Ao testar alguma vantagem (Por exemplo, tentar saber se a pessoa é vulnerável ao sol), o seu código **NÃO DEVE** fazer buscas como `if (vantagem.nome == "Vampirismo")`. Como o repositório crescerá com suporte multi-idioma ou outras variações de nomenclatura, você estragará a regra de jogo.
- Baseie sempre toda lógica matemática de bônus, debuffs ou ações por verificação em IDs, referências estáveis (`type`) atrelados aos JSONs centrais sob a validação do `$schema` de Interoperabilidade que já consta nos logs.
