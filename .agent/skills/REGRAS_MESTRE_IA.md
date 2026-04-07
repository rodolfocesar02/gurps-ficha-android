---
name: regras-mestre-ia-gurps
description: "GRIMÓRIO MESTRE: Contém o Mapa do Projeto, Regras Operacionais, Leis de GURPS 4Ed e Guia de Acessibilidade. O ÚNICO arquivo de referência necessário para o Agente."
---

# 📜 grimório mestre: regras da ia (projeto gurps android)

Este documento é a **Fonte Única de Verdade (SSOT)** para qualquer IA trabalhando neste repositório. Ele consolida a arquitetura, as regras matematicas e o protocolo de trabalho com o Rodolfo.

---

## 🗺️ 1. mapa da arquitetura (project map)

O projeto é um aplicativo Android (Kotlin/Compose) para fichas de GURPS 4ª Edição, focado em automação e acessibilidade.

### 🏗️ Estrutura de Pastas
- **`app/src/main/java/com/gurps/ficha/`**
    - `model/`: Classes de dados puras (Personagem, Atributo, Vantagem).
    - `data/`: Repositórios (Room/Database) e Preferências.
    - `ui/`: Telas e Componentes (Jetpack Compose).
    - `viewmodel/`: Lógica reativa e Delegates para isolar cálculos.
- **`.agent/skills/`**: Este arquivo mestre.
- **`docs/update/`**: Sistema de metadados para o atualizador in-app.

### 🛡️ Hub de Combate Unificado
Toda a lógica de ataque e defesa reside agora na **`TabRolagem.kt`**. Não existem abas separadas para defesa; tudo é compacto e adaptativo (padding 2dp).

---

## 🛠️ 2. protocolo operacional (regras de trabalho)

### 👤 Comunicação com o Rodolfo
- **Idioma:** Exclusivamente **Português (PT-BR)**.
- **Tom:** Mentor amigável e parceiro técnico.
- **Nível Técnico:** Explique mudanças em termos de funcionalidade, não lógica de código (evite "tecniquês").

### 📦 A Regra dos Lotes (Segurança)
- **1 Lote = 1 Commit:** Nunca faça mudanças gigantescas sem salvar.
- **PROGRESS.md:** Atualize este arquivo a cada commit com o número do Lote.
- **Build Obrigatório:** Nunca termine um turno sem rodar `./gradlew build` para garantir que o app compila.

---

## ⚖️ 3. leis rgp gurps (regras do sistema)

### 🧩 Isolamento Matemático (Calculadoras Puras)
- **Zero Lógica na UI:** Cálculos de Esquiva, Apara e Bloqueio devem vir do `FichaViewModel` via `CombatDelegate`. Jamais coloque fórmulas matemáticas em arquivos `.kt` da UI.
- **Bônus de Defesa (BD):** O bônus do escudo deve ser integrado reativamente a todas as defesas ativas.

### 🔗 Vínculo de Combate
- **Dano vs Perícia:** O cálculo de dano deve ser vinculado ao `periciaId` selecionado para aplicar bônus específicos (como Mestre de Armas) de forma correta.

---

## ♿ 4. guia de acessibilidade (pracego)

O aplicativo possui uma variante detectada por `BuildConfig.UI_VARIANT`.
- **Labels Semânticos:** Use `contentDescription` em todos os elementos.
- **Unificação:** O TalkBack deve ler informações combinadas (ex: "Ataque Espada: 14" em vez de ler o nome e depois o número).

---

## 🧪 5. ferramentas de teste

Para validar a integridade da "Mecânica Blindada":
```powershell
# Validar Build Completo
./gradlew build --continue

# Testar Matemática do Jogo
./gradlew testDebugUnitTest
```

---
> [!IMPORTANT]
> **Sincro V22 (Blindagem):** O sistema de Auto-Save é bloqueado via trava `estaCarregando` durante o carregamento de fichas para evitar sobrescrita de dados vazios.
