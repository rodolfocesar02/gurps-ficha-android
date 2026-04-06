---
name: project-map
description: Guia de arquitetura e responsabilidade de arquivos do repositório GURPS Ficha Android. Use para localizar lógica rapidamente sem ler todos os arquivos.
---

# 🗺️ Mapa do Projeto GURPS Ficha Android

Este documento é a "fonte da verdade" para agentes de IA entenderem a organização do código. **Atualizado em Abril de 2026.**

## 📂 Visão Geral da Árvore (Java/Kotlin)
O código fonte principal reside em `app/src/main/java/com/gurps/ficha/`.

### 🧩 1. Model (`/model`)
Contém as definições de dados e o estado do personagem.
- **[Personagem.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/model/Personagem.kt):** Define a classe principal `Personagem` e as listas de vantagens/perícias selecionadas. Inclui cálculos de base de dano (ST).
- **[VantagemDefinicao.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/model/VantagemDefinicao.kt):** Modelos para os itens do catálogo (JSON).

### 📖 2. Data (`/data`)
Responsável pelo acesso a dados persistentes e assets.
- **[DataRepository.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/data/DataRepository.kt):** Ponto central de carregamento de todos os arquivos JSON da pasta `assets/`.

### ⚙️ 3. Domain & Rules (`/domain/rules`)
A lógica "pesada" do sistema GURPS 4ª Edição.
- **[CombatRules.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/domain/rules/CombatRules.kt):** Lógica pura de combate (Esquiva, Apara, Bloqueio). Centraliza a regra de Bônus de Defesa (BD) do Escudo.
- **[traits/](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/domain/rules/traits/):** Arquitetura Modular de Automação. 
    - **MestreDeArmasRule.kt:** Exemplo de automação complexa (Dano vinculado ao NH da perícia e ataque selecionado).
    - **StrikersRule.kt / TeethRule.kt:** Automação de modificadores em perícias específicas baseado no corpo do personagem.
- **[PreRequisitoParser.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/regras_prerequisitos/PreRequisitoParser.kt):** Analisa strings complexas de pré-requisitos.

### 📱 4. UI Layout (`/ui`)
Componentes visuais e Fluxo de Telas.
- **[TabRolagem.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/ui/TabRolagem.kt):** **O Hub Unificado de Combate.** Gerencia Atributos, Status (PV/PF), Defesas Ativas e Combate (Ataque/Dano).
- **[features/rolagem/](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/ui/features/rolagem/):** Pasta modular da interface de rolagem.
    - **RolagemComponents.kt:** Contém os "QuickRollPanels" (Atributos, Defesas, Cabeçalhos PraCego).
    - **Primary/SecondaryDialogs:** Diálogos de configuração detalhada de ataques e defesas.

### 🧠 5. ViewModel & Delegates (`/viewmodel`)
- **[FichaViewModel.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/viewmodel/FichaViewModel.kt):** Controlador principal (State Holder).
- **[delegates/FichaCombatDelegate.kt](file:///c:/Users/Rodolfo/Desktop/ficha%20gurps/ficha-gurps/gurps_app/gurps-ficha-android/app/src/main/java/com/gurps/ficha/viewmodel/delegates/FichaCombatDelegate.kt):** Gerencia a lógica de exibição de NH de Defesa e Ataque, aplicando bônus de vantagens e itens automaticamente.

---

## ♿ Acessibilidade (TalkBack / PraCego)
O projeto utiliza um sistema de variante detectado pelo `BuildConfig.UI_VARIANT`.
- Quando `UI_VARIANT == "pracego"`, injetamos `SectionHeaderPraCego` na UI para navegação por cabeçalhos.
- Rótulos semânticos são unificados para evitar "poluição" auditiva (ex: uma única frase para o botão e o valor).

---

## 🤖 Contexto dos Agentes (`/.agent`)
Arquivos para memória e inteligência dos assistentes.
- **`skills/`**: Instruções especializadas (como esta).
- **`flows/`**: Workflow de automação de commits e deploys.

---

> [!IMPORTANT]
> **Aba de Defesa:** Não existe mais como aba separada. Tudo o que antes era "TabDefesa" agora reside dentro da "TabRolagem" em painéis compactos e adaptativos.
