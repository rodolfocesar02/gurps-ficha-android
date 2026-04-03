# 🐉 GURPS Ficha - Android App

[![Status: Refatorado](https://img.shields.io/badge/Status-Refatorado%202026-brightgreen.svg)]()
[![Platform: Android](https://img.shields.io/badge/Platform-Android%207.0+-blue.svg)]()
[![Kotlin: 1.9+](https://img.shields.io/badge/Kotlin-1.9+-purple.svg)]()

Um aplicativo de ficha editável e automatizada para **GURPS 4ª Edição**, totalmente em português brasileiro. Projetado para agilizar o combate e a exploração, com cálculos automáticos e integração com ferramentas modernas.

---

## 🔥 Funcionalidades Principais

### 🎲 Sistema de Rolagem & Combate
*   **Integração com Discord**: Envie rolagens de dados diretamente para o seu servidor (voz/texto) via API dedicada.
*   **Modo Alvo (VTT Nexus)**: Sincronização com tabuleiros virtuais para automação de dano, defesa e estados.
*   **Cálculo Dinâmico**: O app calcula automaticamente o **NH (Nível de Habilidade)** efetivo, considerando modificadores de carga e atributos.
*   **Progressão Infinita**: Suporte a custos de perícias, magias e técnicas (4 pontos por nível), sem limites arbitrários.

### 🧙 Magia & Habilidades
*   **Gestão de Energia**: Controle automático de gasto de PF (Pontos de Fadiga) com redução por nível alto de NH.
*   **Magia da Alma**: Sistema exclusivo de aspectos para campanhas místicas.
*   **Técnicas & Especializações**: Suporte completo a técnicas de combate e perícias especializadas.

### 🎒 Gestão de Personagem
*   **Carga Automática**: Ajuste em tempo real de **Deslocamento** e **Esquiva** baseado no peso dos equipamentos carregados.
*   **Atributos Secundários**: PV, Vontade, Percepção e PF calculados instantaneamente a partir dos primários.
*   **Persistência**: Salve e carregue múltiplas fichas localmente.

---

## 🛠️ Arquitetura & Tecnologia

Este projeto passou por uma grande refatoração em Abril de 2026 para garantir uma base de código sólida e modular:

*   **UI (Jetpack Compose)**: Interface moderna, dividida em componentes reutilizáveis.
*   **Domain (Kotlin)**: Regras de negócio isoladas (Ex: `MagiaEnergiaRules.kt`).
*   **Data (Repository Pattern)**: Gestão de arquivos JSON e persistência desacoplada.
*   **Variantes de Build**:
    *   **Visual**: Layout padrão rico em detalhes gráficos.
    *   **PraCego**: Layout otimizado para acessibilidade via TalkBack.

---

## 🚀 Como Compilar

1.  **Requisitos**: Android Studio (Hedgehog ou superior), Java 17+, Android SDK 34.
2.  **SDK**: Certifique-se de ter o Android SDK 24+ instalado.
3.  **Configuração Local**: Verifique o arquivo `local.properties` para as URLs da API de Rolagem.
4.  **Build**:
    ```bash
    ./gradlew assembleVisualDebug
    ```

---

## 📋 Status do Desenvolvimento (Refatoração 2026)

| Etapa | Módulo | Status | Descrição |
|---|---|---|---|
| 1 | `DataRepository` | ✅ Feito | Extração de lógica de JSON e redução de ~1000 linhas. |
| 2 | `NexusArcanoEngine` | ✅ Feito | Refatoração do motor Modo Alvo e regras de combate. |
| 3 | `FichaViewModel` | ✅ Feito | Divisão em subclasses para facilitar a manutenção. |
| 6 | `TraitDialogs` | ✅ Feito | Modularização de Vantagens e Peculiaridades. |
| 7 | `TabRolagem` & `Dialogs` | ✅ Feito | Reestruturação completa da interface de dados e rolagens. |

---

## 📄 Requisitos do Sistema
*   **Android 7.0 (API 24)** ou superior.
*   Conexão à internet (apenas para funções de Discord e Modo Alvo).

---
*Este projeto é desenvolvido para a comunidade brasileira de GURPS. Críticas e sugestões são bem-vindas!*
