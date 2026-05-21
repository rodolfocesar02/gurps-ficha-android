---
name: regras-mestre-ia-gurps
description: "GRIMÓRIO MESTRE: Protocolo operacional, regras de trabalho e guia de acessibilidade para agentes que trabalham neste repositório. Leia este arquivo primeiro, depois consulte os documentos de referência listados abaixo."
---

# Grimório mestre: regras da ia (projeto gurps android)

Este documento define o **protocolo de trabalho** para qualquer agente neste repositório: como se comunicar, como commitar, como testar. Para entender o projeto em si, consulte os documentos de referência listados na seção abaixo.

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

## Documentos de Referência

Leia nesta ordem ao assumir o projeto:

1. **`MAPA_DETALHADO.md`** — Mapa completo de todos os 130+ arquivos do projeto: o que cada um faz, onde fica, e a tabela de endereços rápidos para funções críticas. Leia primeiro para se orientar.
2. **`ARQUITETURA_MESTRE_IA.md`** — Detalhamento técnico do sistema de IA (fluxo Auditor/Forjador, loop de tool-use, FTS, prompts, decisões de arquitetura). Leia quando for trabalhar no Mestre IA.
3. **`PROGRESS.md`** — Diário de lotes e commits desde o início do projeto. Consulte para entender o histórico de decisões e o que já foi feito. Nunca apague entradas — apenas adicione ao final.

---

### A Regra dos Lotes (Segurança)
- **1 Lote = 1 Commit:** Nunca faça mudanças gigantescas sem salvar.
- **PROGRESS.md:** Atualize este arquivo (nunca apague nada dentro, apenas adicione o novo item ao final) a cada commit com o número do Lote.
- **Build Obrigatório:** Nunca termine um turno sem rodar `./gradlew build` para garantir que o app compila.

## Jamais coloque fórmulas matemáticas em arquivos `.kt` da UI.

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
