# Acompanhamento do Projeto da Ficha GURPS (Para Rodolfo)

**Última Atualização:** 04 de Abril de 2026 (20:15)
**Status Atual:** Em estruturação e organização para IAs.

---

## O Que Estamos Fazendo Agora? (Abril 2026)
O aplicativo cresceu bastante! Por causa disso, alguns arquivos ficaram enormes e "pesados" (com mais de 1000 linhas). Nosso objetivo atual é organizar a casa para que o app não quebre e fique mais fácil de dar manutenção.

Acabamos de implementar **As Novas Regras para Agentes Virtuais (IAs)**.
Eu, como Inteligência Artificial, deixei instruções de ouro em uma pasta especial chama `.agent/skills/` para que **qualquer outra IA que trabalhar com você no futuro saiba o que fazer e como te tratar:**

1. **Falar Simples:** Qualquer IA tem a obrigação de falar com você em um português normal. Nada de termos técnicos complicados. Se for preciso explicar o que foi feito, que seja em linguagem do dia a dia.
2. **Entender de Abas:** O agente novo vai ler o mapa `README_AGENTE.md` logo de cara, sabendo que sua ficha é dividida em Geral, Traços, Perícias, Magias, Equipamentos, Defesas e Rolagem, sem você precisar repetir tudo.
3. **Sempre Testar (Construção do App):** Proibimos qualquer IA de dizer que "terminou" o trabalho sem antes rodar um teste do sistema (um comando chamado `./gradlew build`), que garante que o aplicativo vai abrir no seu emulador sem travar.

---

## Lembretes Fixos do Seu Projeto

### 1. Ferramentas Acessíveis
Nós sempre cuidamos para que toda versão lançada tenha a versão **Visual** (Normal) e a versão **PraCego** (Com botão e navegação de acessibilidade para cegas por meio do programa TalkBack de celular). O emulador costuma focar na visual para seu teste rápido.

### 2. Consistência de Dados
Os dados das magias, vantagens e perícias moram em arquivos de texto (tipo planilhas, chamados de arquivos **JSON**). IAs que forem alterar algo lá não podem apagar aspas ou colchetes sem cuidado.

---

## 🕒 Registro de Lotes e Commits (Rede de Segurança)
*Todo Agente é obrigado a quebrar tarefas maiores em "Lotes Curtos" isolados de um arquivo por vez, efetuando o Commit no final para gerar um Ponto de Retorno seguro para o usuário. Cada nova "Aba" ganha também sua própria pasta.*

> Lista de Lotes Realizados a partir de Abril de 2026:

* [Feito] Lote 1.2: Extração de Loaders Json (DataRepository)       | `(Commit: 7c52e26)`
* [Feito] Lote 2.1: Separação de Peças do Nexus (Modelos)           | `(Commit: a6992f1)`
* [Feito] Lote 2.2: O Cérebro A* (Planejador de Caminho)            | `(Commit: 113b540)`
* [Feito] Lote 2.3: O Motor de Diagnóstico (Raio-X)                 | `(Commit: 105949a)`
* [Feito] Lote 2.4: Limpeza final (Helpers & Parser)                | `(Commit: aab9ff2)`
* [Feito] Lote 3: Modularização do FichaViewModel                   | `(Commit: 20414fd)`
* [Feito] Lote 4: Padronização UTF-8 e Motor Modo Alvo              | `(Commit: 0912746)`
* [Feito] Lote 6: Modularização do TraitDialogs                     | `(Commit: 3d186e9)`
* [Feito] Lote 6.1: Correção de Seleção e Regras Especiais          | `(Commit: 0e9aee5)`
* [Feito] Lote 7: Refatoração da UI de Rolagem (TabRolagem.kt)      | `(Commit: 042cd2d)`
* [Feito] Lote 7.1: Correções de Discord, UI de Ataque e Mojibake   | `(Commit: 6c3f773)`
* [Feito] Lote 7.2: Refatoração do RolagemDialogs.kt (Fragmentação) | `(Commit: c6a800c)`
* [Feito] Lote 8: Atualização Estética dos Ícones das Abas          | `(Commit: 1cec435)`
* [Feito] Lote 9: Interface de Navegação RPGística (Ultra-Premium)  | `(Commit: 9f13f43)`
* [Feito] Lote 10: Melhoria do Ataque Inato e Skill Project Map     | `(Commit: e1c1a1b)`
* [Feito] Lote 11: Modernização da UI do Mestre IA (ChatGPT)        | `(Commit: e42cce6)`
# Acompanhamento do Projeto da Ficha GURPS (Para Rodolfo)

**Última Atualização:** 04 de Abril de 2026 (20:15)
**Status Atual:** Em estruturação e organização para IAs.

---

## O Que Estamos Fazendo Agora? (Abril 2026)
O aplicativo cresceu bastante! Por causa disso, alguns arquivos ficaram enormes e "pesados" (com mais de 1000 linhas). Nosso objetivo atual é organizar a casa para que o app não quebre e fique mais fácil de dar manutenção.

Acabamos de implementar **As Novas Regras para Agentes Virtuais (IAs)**.
Eu, como Inteligência Artificial, deixei instruções de ouro em uma pasta especial chama `.agent/skills/` para que **qualquer outra IA que trabalhar com você no futuro saiba o que fazer e como te tratar:**

1. **Falar Simples:** Qualquer IA tem a obrigação de falar com você em um português normal. Nada de termos técnicos complicados. Se for preciso explicar o que foi feito, que seja em linguagem do dia a dia.
2. **Entender de Abas:** O agente novo vai ler o mapa `README_AGENTE.md` logo de cara, sabendo que sua ficha é dividida em Geral, Traços, Perícias, Magias, Equipamentos, Defesas e Rolagem, sem você precisar repetir tudo.
3. **Sempre Testar (Construção do App):** Proibimos qualquer IA de dizer que "terminou" o trabalho sem antes rodar um teste do sistema (um comando chamado `./gradlew build`), que garante que o aplicativo vai abrir no seu emulador sem travar.

---

## Lembretes Fixos do Seu Projeto

### 1. Ferramentas Acessíveis
Nós sempre cuidamos para que toda versão lançada tenha a versão **Visual** (Normal) e a versão **PraCego** (Com botão e navegação de acessibilidade para cegas por meio do programa TalkBack de celular). O emulador costuma focar na visual para seu teste rápido.

### 2. Consistência de Dados
Os dados das magias, vantagens e perícias moram em arquivos de texto (tipo planilhas, chamados de arquivos **JSON**). IAs que forem alterar algo lá não podem apagar aspas ou colchetes sem cuidado.

---

## 🕒 Registro de Lotes e Commits (Rede de Segurança)
*Todo Agente é obrigado a quebrar tarefas maiores em "Lotes Curtos" isolados de um arquivo por vez, efetuando o Commit no final para gerar um Ponto de Retorno seguro para o usuário. Cada nova "Aba" ganha também sua própria pasta.*

> Lista de Lotes Realizados a partir de Abril de 2026:

* [Feito] Lote 1.2: Extração de Loaders Json (DataRepository)       | `(Commit: 7c52e26)`
* [Feito] Lote 2.1: Separação de Peças do Nexus (Modelos)           | `(Commit: a6992f1)`
* [Feito] Lote 2.2: O Cérebro A* (Planejador de Caminho)            | `(Commit: 113b540)`
* [Feito] Lote 2.3: O Motor de Diagnóstico (Raio-X)                 | `(Commit: 105949a)`
* [Feito] Lote 2.4: Limpeza final (Helpers & Parser)                | `(Commit: aab9ff2)`
* [Feito] Lote 3: Modularização do FichaViewModel                   | `(Commit: 20414fd)`
* [Feito] Lote 4: Padronização UTF-8 e Motor Modo Alvo              | `(Commit: 0912746)`
* [Feito] Lote 6: Modularização do TraitDialogs                     | `(Commit: 3d186e9)`
* [Feito] Lote 6.1: Correção de Seleção e Regras Especiais          | `(Commit: 09aee5)`
* [Feito] Lote 7: Refatoração da UI de Rolagem (TabRolagem.kt)      | `(Commit: 042cd2d)`
* [Feito] Lote 7.1: Correções de Discord, UI de Ataque e Mojibake   | `(Commit: 6c3f773)`
* [Feito] Lote 7.2: Refatoração do RolagemDialogs.kt (Fragmentação) | `(Commit: c6a800c)`
* [Feito] Lote 8: Atualização Estética dos Ícones das Abas          | `(Commit: 1cec435)`
* [Feito] Lote 9: Interface de Navegação RPGística (Ultra-Premium)  | `(Commit: 9f13f43)`
* [Feito] Lote 10: Melhoria do Ataque Inato e Skill Project Map     | `(Commit: e1c1a1b)`
* [Feito] Lote 11: Modernização da UI do Mestre IA (ChatGPT)        | `(Commit: e42cce6)`
* [Feito] Lote 12: Card de dano adaptativo e soma automática de ST. | `(Commit: 783c295)`
* [Feito] Lote 13: Fluxo de Mestre IA com confirmação e análise consultiva. | `(Commit: a104632)`
* [Feito] Lote 14: Mestre IA Interativo (Antigravity-style) com botões de ação e tom inquisitivo. | `(Commit: a104632)`
* [Feito] Lote 15: Restauração das Defesas Ativas na Aba de Rolagem | `(Commit: 5c7c369)`
* [Feito] Lote 16: Modularização da FichaViewModel e Protótipo de Sugestões Clicáveis (Mestre IA) | `(Commit: c279b87)`
* [Feito] Lote 17: Automação da vantagem Golpeadores (Strikers) | `(Commit: 65ec4ef)`
* [Feito] Lote 18: Inteligência de NH e Aparar para Golpeadores/Ataque Inato | `(Commit: 687b8d9)`
* [Feito] Lote 19: Arquitetura Modular de Vantagens e Documentação de IA | `(Commit: 6132f9b)`
* [Feito] Lote 20: Automação da Vantagem Dentes (GdP-1 e Tipos) | `(Commit: 5fd613d)`
* [Feito] Lote 21: Persistência de Seleção de Ataque/Dano na Sessão | `(Commit: 217eaf4)`
* [Feito] Lote 22: Ajuste Vantagem Dentes e Nomeclatura de Dano PT-BR | `(Commit: ada9efd)`
* [Feito] Lote 23: Limpeza de Unicode Mojibake (\uXXXX) no Código Fonte | `(Commit: 33c771f)`
* [Feito] Lote 24: Automação de Garras, Cascos e Flexibilidade (+3 Perícias) | `(Commit: d4a5e2f)`
