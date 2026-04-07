---
description: Regras obrigatórias de GitHub e versão. Todo trabalho deve ser salvo em lotes atômicos minúsculos.
---
# Regras de Segurança e Versionamento (Git)

## 1. Regra do Lote Curto (Um Arquivo Apenas)
- **NUNCA** mude de escopo no meio do caminho. Você deve pegar UM NOME de lote, UM NOME de aba ou UM ARQUIVO com mais de mil linhas, e o seu universo termina ali.
- Finalize esse lote 100% (código + testes) antes de pular para o próximo arquivo.

## 2. Regra das Pastas (Abas)
- Ao fatiar um arquivo monolítico de uma "Aba" da Ficha (Ex: Rolagem, VTT, etc.), crie ou utilize uma pasta da *feature* para ele, por exemplo, `ui/features/rolagem/` ou `ui/features/magias/`. Tudo que pertence àquela aba vai para lá. 

## 3. O Ponto de Retorno Segura O Usuário
- Sempre que você encerrar as alterações e testes daquele Lote Único, você é OBRIGADO a executar as ferramentas do sistema de versão (Terminal):
```bash
# Adicionar tudo que você mudou no lote
git add .
# Realizar o commit descritivo do lote (expondo o NOME dado ao Lote)
git commit -m "Lote 1.0: [Descricao Simples Do Lote]"
```
- E perguntar se o Rodolfo deseja o Push.

## 4. O Livro-Caixa no PROGRESS.md
- Vá obrigatoriamente para a ÚLTIMA LINHA do arquivo root `PROGRESS.md`.
- No final deste arquivo, existe (ou você deve criar) um registro com a data do lote, o número do lote, o seu resumo, e o **Hash do Commit** do Git para que o Rodolfo saiba onde voltar um passo atrás se um erro acontecer.
- Marque o Lote atual como `(Concluído e Commit efetuado)`.
