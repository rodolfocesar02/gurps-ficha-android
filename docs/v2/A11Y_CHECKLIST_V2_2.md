# V2.2 - Checklist Acessibilidade (Visual + PraCego)

Objetivo: confirmar que a padronização visual não quebrou uso com leitor de tela e foco.

## 1. Ordem de foco e navegação
- [ ] Foco segue ordem lógica em `Geral`, `Tracos`, `Pericias`, `Tecnicas`, `Magias`, `Equip.`, `Defesas`, `Rolagem`.
- [ ] Em diálogos, foco inicia no título/pergunta e termina nas ações finais sem "saltos".
- [ ] Botões críticos são alcançáveis sem gesto complexo.

## 2. Rótulos e ações
- [ ] Itens de lista com ação de editar têm descrição clara para TalkBack.
- [ ] Botões de remover anunciam o tipo de item e nome.
- [ ] Campos com contexto (`PV`, `PF`, `NH`, `Nível`) anunciam valor atual.

## 3. Contraste e legibilidade
- [ ] Texto principal com contraste adequado no tema atual.
- [ ] Texto secundário permanece legível em telas pequenas.
- [ ] Mensagens de erro/estado não dependem só de cor.

## 4. Alvos de toque
- [ ] Ações principais e itens clicáveis respeitam área mínima de toque.
- [ ] Não há controles sobrepostos em telas compactas.

## 5. Fechamento
- [ ] Registrar evidência no `PROGRESS.md`.
- [ ] Só marcar lote concluído após validação no emulador visual e pracego.
