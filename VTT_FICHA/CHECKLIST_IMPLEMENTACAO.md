# Checklist de Implementação - VTT + Ficha

## Fase A - Preparação
1. [ ] Confirmar URL base do backend VTT para dev/homolog/prod.
2. [ ] Confirmar política de autenticação da sala (roomKey/token).
3. [ ] Congelar versão do schema de interoperabilidade da ficha.

## Fase B - Aba VTT (sem tocar abas atuais)
1. [ ] Criar nova aba `VTT` na navegação do app.
2. [ ] Garantir que abas atuais permaneçam inalteradas.
3. [ ] Implementar shell de carregamento (estado desconectado/conectando/conectado).

## Fase C - Sessão e token
1. [ ] Join de sala com `playerId` e `character snapshot`.
2. [ ] Receber/associar `tokenId` da sessão.
3. [ ] Reentrada com recuperação de sessão/token.

## Fase D - Fluxo de rolagem por token
1. [ ] Clique no token próprio abre painel de ações.
2. [ ] Clique em token alvo abre painel de ataque/defesa contextual.
3. [ ] Resultado aparece em chat + balão no token.

## Fase E - Voz e acessibilidade
1. [ ] Conectar/desconectar voz na aba VTT.
2. [ ] Leitura acessível de eventos importantes.
3. [ ] Teste de foco e navegação TalkBack.

## Fase F - Não regressão
1. [ ] Testar edição completa da ficha sem usar a aba VTT.
2. [ ] Testar import/export JSON igual ao comportamento atual.
3. [ ] Testar build visual e pracego.
