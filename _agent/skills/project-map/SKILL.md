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
- **[CharacterRules.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/domain/rules/CharacterRules.kt):** Cálculo de custos de PV, PF, Atributos e Vantagens Especiais (como **Ataque Inato**).
- **[PreRequisitoParser.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/regras_prerequisitos/PreRequisitoParser.kt):** Analisa strings complexas de pré-requisitos (ex: "AM1, IQ 12+, 5 magias de Fogo").

### 📱 4. UI Layout (`/ui`)
Componentes visuais organizados por abas (`Tab*.kt`) e diálogos (`Dialogs*.kt`).
- **[TabRolagem.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/ui/TabRolagem.kt):** Aba principal de combate. Gerencia ataques, defesas e danos.
- **[VantagemDialogs.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/ui/features/traits/VantagemDialogs.kt):** Configuração detalhada de vantagens (modificadores, metadados).
- **[TabVtt.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/ui/TabVtt.kt):** Integração massiva com mesas virtuais (Discord, etc).

### 🧠 5. ViewModel (`/viewmodel`)
- **[FichaViewModel.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/viewmodel/FichaViewModel.kt):** O controlador principal. Mantém o `FichaUIState` e coordena salvamento/carregamento.

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
> Se o usuário muda um valor na UI (`TabRolagem`), a ação é enviada para o `FichaViewModel`, que utiliza `CharacterRules` para validar/recalcular e atualiza o `Personagem` (Model), que por fim é persistido via `DataRepository`.
