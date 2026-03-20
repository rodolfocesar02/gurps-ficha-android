# GURPS Ficha Android - Relatorio de Progresso e Regras de Ouro

**Ultima Atualizacao:** 2026-03-20
**Versao Atual:** v1.4.3 (Build 10)
**Status do Projeto:** STAVEL - LANCADO

---

## Estado Atual (v1.4.3)
- **Restauracao de Pericias Raciais**: Mecanica de bonus sinalizado (+1, -2) totalmente operacional e desvinculada da ficha principal.
- **Acessibilidade (TalkBack)**: Sistema de acessorios e rotulos semanticos integrados para usuarios cegos.
- **Sincronizacao Cloud**: update.json atualizado para apontar para a build v1.4.3 (Build 10).
- **Estabilidade**: Build via Gradle passando sem erros de compilacao ou conflitos de icones.

---

## REGRAS OPERACIONAIS DE OURO (NAO QUEBRAR)

### 1. Build e Lancamento
- **Versionamento**: Sempre elevar o versionCode no app/build.gradle (atualmente 10) e sincronizar o versionName (atualmente 1.4.3) no update.json.
- **APKs de Lote**: Gerar sempre os dois sabores: visual (para videntes) e pracego (com otimizacoes para TalkBack).
- **Limpeza de Build**: Se o Gradle travar a pasta build, use ./gradlew clean --no-daemon ou mate os processos Java da IDE.

### 2. Integridade de Dados (JSON)
- **Auditoria**: Nunca alterar um arquivo .json nos assets sem rodar ./gradlew validateActiveJsonAssets.
- **Encoding**: Manter arquivos em UTF-8. Evitar caracteres especiais quebrados (mojibake).

### 3. Mecanica de Pericia Racial (A Regra do Mestre)
- **Nao Bloqueio**: O seletor de pericia na Personalizacao de Raca deve ser INDEPENDENTE. Ele nunca deve impedir a selecao de uma pericia porque ela ja existe na ficha.
- **Bonus Sinalizado**: Os bonus raciais devem ser salvos como nivelRelativo (ex: +1, -2). 
- **Calculo de Custo**: O custo em pontos deve ser calculado automaticamente baseado na dificuldade (F, M, D, MD) e no nivel desejado.

### 4. Acessibilidade (PraCego)
- **Labels Semanticos**: Todo novo botao de acao ou icone deve conter contentDescription explicativo.
- **Traversal**: Manter a ordem de leitura logica para o TalkBack (Cima para Baixo, Esquerda para Direita).

### 5. Padrao de Interface (UI)
- **Primary Buttons**: Sempre usar a cor primaria para botoes de acao principal (Adicionar/Salvar).
- **Densidade**: Dialogos devem respeitar o espacamento DialogContentSpacing para nao ficarem apertados.
- **Confirmacao**: Acoes destrutivas (Excluir) devem pedir confirmacao antes de apagar.

---

## Pendencias Proximas (Backlog)
- [ ] Validar a aplicacao de bonus raciais negativos no calculo final do NH (Nivel de Habilidade) dentro da aba de Pericias.
- [ ] Revisao de bonus de atributos secundarios (HP, Per, Von) na personalizacao racial.
- [ ] Testar a importacao de modelos raciais antigos para compatibilidade com a Build 10.

---

## Localizacao de Builds Recentes
- **Pasta:** app/build/outputs/apk/visual/release
- **Pasta:** app/build/outputs/apk/pracego/release

---
*Nota: Este arquivo foi limpo radicalmente em 20/03/2026 para remover logs obsoletos de 2024/2025.*
