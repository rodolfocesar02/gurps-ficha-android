---
name: Ficha Gurps
description: Contém o Mapa do Projeto, Regras Operacionais, Leis de GURPS 4Ed e Guia de Acessibilidade. O ÚNICO arquivo de referência necessário para o Agente."
---

#   regras da ia (projeto gurps android)

Este documento é a **Fonte Única de Verdade (SSOT)** para qualquer IA trabalhando neste repositório. Ele consolida a arquitetura, as regras matematicas e o protocolo de trabalho com o Rodolfo.

##  1. protocolo operacional (regras de trabalho)

###  Comunicação com o Rodolfo
- **Idioma:** Exclusivamente **Português (PT-BR)**.
- **Tom:** Mentor amigável e parceiro técnico.
- **Evite** Uso de Emojis em converssas, uso limitado de caracteres em converssa(contole de Tokens)
- **Nível Técnico:** Explique mudanças em termos de funcionalidade, não lógica de código (evite "tecniquês").
---

##  2. mapa da arquitetura (project map)

O projeto é um aplicativo Android (Kotlin/Compose) para fichas de GURPS 4ª Edição, focado em automação e acessibilidade.

###  Estrutura de Pastas
- **`app/src/main/java/com/gurps/ficha/`**
    - `model/`: Classes de dados puras (Personagem, Atributos).
    - `ui/`: Telas (FichaScreen, TabRolagem, TabCombate).
    - `viewmodel/`:
        - `FichaViewModel.kt`: Central de Inteligência (Combate, Atributos, Magias e IA).
        - `delegates/`: Persistência e suporte.
- **`.agent/skills/`**: Este arquivo mestre.
---

###  A Regra dos Lotes (Segurança)
- **1 Lote = 1 Commit:** Nunca faça mudanças gigantescas sem salvar.
- **PROGRESS.md:** Atualize este arquivo a cada commit com o número do Lote.
- **Build Obrigatório:** Nunca termine um turno sem rodar `./gradlew build` para garantir que o app compila.
- **
---

##  3. leis rgp gurps (regras do sistema)

###  Isolamento Matemático (Calculadoras Puras)
- **Zero Lógica na UI:** Cálculos de Esquiva, Apara e Bloqueio devem vir do `FichaViewModel` via `CombatDelegate`. Jamais coloque fórmulas matemáticas em arquivos `.kt` da UI.

###  Vínculo de Combate
- **Dano vs Perícia:** O cálculo de dano deve ser vinculado ao `periciaId` selecionado para aplicar bônus específicos (como Mestre de Armas) de forma correta.
---
##  4. guia de acessibilidade (pracego)
O aplicativo possui uma variante detectada por `BuildConfig.UI_VARIANT`.
- **Labels Semânticos:** Use `contentDescription` em todos os elementos.
- **Unificação:** O TalkBack deve ler informações combinadas (ex: "Ataque Espada: 14" em vez de ler o nome e depois o número).
---
##  5. ferramentas de teste
Para validar a integridade da "Mecânica Blindada":
```powershell
# Validar Build Completo
./gradlew build --continue
# Testar Matemática do Jogo
./gradlew testDebugUnitTest