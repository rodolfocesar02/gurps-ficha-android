# Acompanhamento do Projeto da Ficha GURPS (Para Rodolfo)

**Última Atualização:** 03 de Abril de 2026
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

## Próximos Passos (A Refatoração)
Já mapeamos 6 partes do projeto que estão muito grandes e vamos focar nelas *uma etapa de cada vez*, apenas quando você autorizar:

*   **[Feito] Etapa 6:** Componentizar os botões e janelas de Traços (o `TraitDialogs.kt`), para que adicionar Vantagens ou Desvantagens não custe tanta lentidão visual ou peso no arquivo.
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

* [Feito] Lote 1.2: Extração de Loaders Json (DataRepository)   | `(Commit: 7c52e26)`
* [Feito] Lote 2.1: Separação de Peças do Nexus (Modelos)       | `(Commit: a6992f1)`
* [Feito] Lote 2.2: O Cérebro A* (Planejador de Caminho)        | `(Commit: 113b540)`
* [Feito] Lote 2.3: O Motor de Diagnóstico (Raio-X)             | `(Commit: 105949a)`
* [Feito] Lote 2.4: Limpeza final (Helpers & Parser)            | `(Commit: aab9ff2)`
* [Feito] Lote 3: Modularização do FichaViewModel               | `(Commit: 20414fd)`
* [Feito] Lote 4: Padronização UTF-8 e Motor Modo Alvo          | `(Commit: 0912746)`
* [Feito] Lote 6: Modularização do TraitDialogs                 | `(Commit: 3d186e9)`
* [Feito] Lote 6.1: Correção de Seleção e Regras Especiais      | `(Commit: 0e9aee5)`
* [Feito] Lote 7: Refatoração da UI de Rolagem (TabRolagem.kt)  | `(Commit: 042cd2d)`
* [Feito] Lote 7.1: Correções de Discord, UI de Ataque e Mojibake  | `(Commit: pending)`

> A `TabRolagem.kt` foi modernizada com layout vertical e espaçamento uniforme.
> O limite de 12 pontos para perícias e magias foi removido, permitindo progressão infinita.
> O catálogo de perícias foi sincronizado, restaurando 41 perícias (como Dissimulação e Arco) e organizado em ordem alfabética.

> O `TraitDialogs.kt` foi reduzido com sucesso de +3000 linhas para apenas 1499 linhas, finalizando a Etapa 6!
> O `DataRepository.kt` foi refatorado com sucesso de 1934 linhas para em torno de ~800 linhas, finalizando a Etapa 1!
> O `NexusArcanoEngine.kt` foi reduzido com sucesso de +2000 linhas para apenas 938 linhas, finalizando a Etapa 2!
> O `FichaViewModel.kt` foi reduzido com sucesso de +2000 linhas para ~915 linhas, finalizando a Etapa 3!
> O `TabRolagem.kt` foi reduzido com sucesso de +1400 linhas para ~720 linhas, finalizando a Etapa 7!
> Corrigido erro 400 no Discord (envio de channelId), removidas armas da seleção de Ataque e limpo todo "mojibake" de encoding.