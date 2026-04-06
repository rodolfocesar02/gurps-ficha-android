---
name: project-map
description: Guia de arquitetura e responsabilidade de arquivos do repositório GURPS Ficha Android. Use para localizar lógica rapidamente sem ler todos os arquivos.
---

# 🗺️ Mapa do Projeto GURPS Ficha Android

Este documento é a "fonte da verdade" para agentes de IA entenderem a organização do código.

## 📂 Visão Geral da Árvore (Java/Kotlin)
O código fonte principal reside em `app/src/main/java/com/gurps/ficha/`.

### 🧩 1. Model (`/model`)
Contém as definições de dados e o estado do personagem.
- **[Personagem.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/model/Personagem.kt):** Define a classe principal `Personagem` e as listas de vantagens/perícias selecionadas.
- **[VantagemDefinicao.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/model/VantagemDefinicao.kt):** Modelos para os itens do catálogo (JSON).

### 📖 2. Data (`/data`)
Responsável pelo acesso a dados persistentes e assets.
- **[DataRepository.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/data/DataRepository.kt):** Ponto central de carregamento de todos os arquivos JSON da pasta `assets/`.

### ⚙️ 3. Domain & Rules (`/domain/rules` e `/regras_prerequisitos`)
A lógica do sistema GURPS 4ª Edição.
- **[CharacterRules.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/domain/rules/CharacterRules.kt):** Cálculo de custos gerais. Utiliza o `TraitRuleRegistry` para delegar cálculos complexos.
- **[CombatRules.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/domain/rules/CombatRules.kt):** Lógica pura de combate (Esquiva, Apara, Bloqueio, Dano). Inclui a regra de BD do Escudo.
- **[traits/](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/domain/rules/traits/):** Pasta com a nova arquitetura modular.
    - **TraitRule.kt:** Interface base para automação.
    - **TraitRuleRegistry.kt:** Mapeia IDs de vantagens para seus arquivos de regra.
    - **[Nome]Rule.kt:** Cada arquivo isola a lógica de uma vantagem específica (ex: `AtaqueInatoRule.kt`).
- **[PreRequisitoParser.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/regras_prerequisitos/PreRequisitoParser.kt):** Analisa strings complexas de pré-requisitos.

### 📱 4. UI Layout (`/ui`)
Componentes visuais organizados por abas (`Tab*.kt`) e diálogos (`Dialogs*.kt`).
- **[TabRolagem.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/ui/TabRolagem.kt):** Aba principal de combate. 
- **[RolagemSecondaryDialogs.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/ui/features/rolagem/RolagemSecondaryDialogs.kt):** Diálogos de edição de Esquiva, Apara e Bloqueio (Inclui notas explicativas de BD).
- **[VantagemDialogs.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/ui/features/traits/VantagemDialogs.kt):** Configuração detalhada de vantagens (modificadores, metadados).
- **[TabVtt.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/ui/TabVtt.kt):** Integração massiva com mesas virtuais (Discord, etc).

### 🧠 5. ViewModel & Delegates (`/viewmodel`)
- **[FichaViewModel.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/viewmodel/FichaViewModel.kt):** O controlador principal. Mantém o `FichaUIState`.
- **[FichaCombatDelegate.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/viewmodel/delegates/FichaCombatDelegate.kt):** Delegado que gerencia a lógica de exibição e cálculo de NH de combate (Ataque e Defesa).

---

## 🐍 Scripts de Automação (`/scripts`)
Scripts Python usados para manter a consistência dos dados (Assets).
- **`audit_active_jsons_v2.py`:** Valida a integridade dos catálogos JSON.
- **`fix_mojibake_project.py`:** Corrige erros de codificação de caracteres (UTF-8) em lote.
- **`convert_*.py`:** Conversores de dados entre diferentes versões de schemas JSON.

---

## 🤖 Contexto dos Agentes (`/_agent`)
Arquivos para memória e inteligência dos assistentes de codificação.
- **`skills/`**: Conjunto de instruções especializadas (como esta).
- **`context/`**: Persistência de decisões e progresso entre sessões de desenvolvimento.

---

> [!TIP]
> **Fluxo de Dados Típico:**
> Se o usuário muda um valor na UI (`TabRolagem`), a ação é enviada para o `FichaViewModel`, que utiliza delegates (como `FichaCombatDelegate`) e classes de regra (`CombatRules`, `CharacterRules`) para recalcular e atualizar o `Personagem` (Model).
