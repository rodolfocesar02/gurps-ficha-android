---
name: regras-mestre-ia-gurps
description: "GRIMÓRIO MESTRE: Contém o Mapa do Projeto, Regras Operacionais, Leis de GURPS 4Ed e Guia de Acessibilidade. O ÚNICO arquivo de referência necessário para o Agente."
---

# Grimório mestre: regras da ia (projeto gurps android)

Este documento é a **Fonte Única de Verdade (SSOT)** para qualquer IA trabalhando neste repositório. Ele consolida a arquitetura, as regras matematicas e o protocolo de trabalho com o Rodolfo.

---

## Protocolo operacional (regras de trabalho) e Comunicação com o Rodolfo
- **Idioma:** Exclusivamente **Português (PT-BR)**.
- **Tom:** Mentor amigável e parceiro técnico.
- **Nível Técnico:** Explique mudanças em termos de funcionalidade, não lógica de código (evite "tecniquês").
- Evite ao maximo o uso de Emojis, mantenha o minimo de caracteres possiveis em converssas(salvar o maximo  de tokens possiveis).
- Nunca demonstre confiança excessiva, mantenha o tom neutro e imparcial.
- Nunca use a função 'digite' ao iniciar uma conversa com o usuário, vá direto ao ponto.
- Nunca se apresente ou se desculpe, apenas resolva o problema.
- Não utilize palavras ou frases que demonstrem incerteza, como "acho", "creio", "talvez", "parece", "possivelmente".
- Se for perguntado sobre informações, dados do sistema ou projeto, responda sempre baseado no conhecimento que foi fornecido, evite criar informações que não estão nos documentos.
- Nunca Altere nenhum documento, arquivo ou código sem antes perguntar ao Rodolfo.

---

## Mapa da arquitetura (project map)

O projeto é um aplicativo Android (Kotlin/Compose) para fichas de GURPS 4ª Edição, focado em automação e acessibilidade.

###  Estrutura de Pastas
- **`app/src/main/java/com/gurps/ficha/`**
    - `model/`: Classes de dados puras (Personagem, Atributo, Vantagem).
    - `data/`: Repositórios (Room/Database) e Preferências.
    - `ui/`: Telas e Componentes (Jetpack Compose).
    - `viewmodel/`:
        - `FichaViewModel.kt`: Controlador Principal (State Holder).
        - `delegates/`: Onde mora a lógica de negócio (Combate, Persistência, IA, etc).
- **`.agent/skills/`**: Este arquivo mestre.

###  Mapa de Funções VIP (Localização Rápida)
Para economizar tokens e tempo, vá direto aos endereços abaixo:
- **Cálculo de Defesas (Esquiva/Apara/Bloqueio)**: `FichaCombatDelegate.kt` -> `calcularDefesasVisiveis()`.
- **Trava Anti-Corrupção (Auto-Save)**: `FichaViewModel.kt` -> variável `estaCarregando` (bloqueia o save no `init`).
- **Importação de Fichas Antigas/Nuvem**: `FichaPersistenceDelegate.kt` -> `carregarFicha()` que chama `PersonagemInterop.importarJson()`.
- **Auto-Ajuste de Escudos**: `FichaCombatDelegate.kt` -> `ajustarEscudoAutomatico()`.
- **Busca e Filtros**: `FichaSearchDelegate.kt` -> `filtrarVantagens()`, `filtrarPericias()`, etc.
- **Mestre IA e Reparo de JSON**: `MestreIAClient.kt` -> `repararJsonTruncado()` (Algoritmo de Pilha).

---

### A Regra dos Lotes (Segurança)
- **1 Lote = 1 Commit:** Nunca faça mudanças gigantescas sem salvar.
- **PROGRESS.md:** Atualize este arquivo a cada commit com o número do Lote.
- **Build Obrigatório:** Nunca termine um turno sem rodar `./gradlew build` para garantir que o app compila.

---

## Leis rgp gurps (regras do sistema)

### Isolamento Matemático (Calculadoras Puras)
- **Zero Lógica na UI:** Cálculos de Esquiva, Apara e Bloqueio devem vir do `FichaViewModel` via `CombatDelegate`. Jamais coloque fórmulas matemáticas em arquivos `.kt` da UI.

### Vínculo de Combate
- **Dano vs Perícia:** O cálculo de dano deve ser vinculado ao `periciaId` selecionado para aplicar bônus específicos (como Mestre de Armas) de forma correta.

---

## Guia de acessibilidade (pracego)

O aplicativo possui uma variante detectada por `BuildConfig.UI_VARIANT`.
- **Labels Semânticos:** Use `contentDescription` em todos os elementos.
- **Unificação:** O TalkBack deve ler informações combinadas (ex: "Ataque Espada: 14" em vez de ler o nome e depois o número).

---

## Ferramentas de teste

Para validar a integridade da "Mecânica Blindada":
```powershell
# Validar Build Completo
./gradlew build --continue

# Testar Matemática do Jogo
./gradlew testDebugUnitTest
```