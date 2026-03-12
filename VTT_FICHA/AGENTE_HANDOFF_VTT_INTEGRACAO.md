# AGENTE HANDOFF - Integração VTT + Ficha GURPS

Este arquivo é o contexto oficial para qualquer agente que for implementar a integração entre o app da ficha e o VTT.

## 1) Objetivo do projeto
Integrar o VTT ao app Android da ficha sem quebrar o que já funciona.

Meta funcional:
1. Manter as abas atuais da ficha como editor canônico.
2. Criar uma nova aba `VTT` para sessão de jogo (mapa, token, voz e rolagens contextuais).
3. Não alterar regras existentes de cálculo da ficha durante a integração.

## 2) Projetos envolvidos
1. Ficha Android (este repositório):
   - caminho: `gurps_app/gurps-ficha-android`
2. VTT externo:
   - caminho: `C:\Users\Rodolfo\Desktop\VTT_audio_video_ e ficha de gurps!`

## 3) Estado atual da ficha (o que NÃO mexer)
As abas abaixo estão estáveis e devem permanecer intactas:
1. Geral
2. Traços
3. Perícias
4. Técnicas
5. Magias
6. Equipamentos
7. Defesas

Somente a experiência de mesa deve migrar para a aba `VTT`.

## 4) Navegação atual da ficha
Arquivo principal:
1. `app/src/main/java/com/gurps/ficha/ui/FichaScreen.kt`

Hoje existe a aba `Rolagem` como última aba.
Plano:
1. Adicionar aba `VTT`.
2. Preservar as demais abas.
3. Não remover recursos de edição.

## 5) Contratos obrigatórios de integração
Base documental:
1. `VTT_FICHA/INTEGRACAO_CONTRATOS.md`
2. `VTT_FICHA/MAPEAMENTO_SISTEMAS.md`
3. `PROGRESS_VTT+FICHA.md`

Resumo dos contratos:
1. Envelope de personagem com `schema: gurps.personagem`.
2. Sessão com `sessionId`, `playerId`, `tokenId`.
3. Eventos WS mínimos:
   - `estado_inicial`
   - `cenario_atualizado`
   - `resultado_chat`
   - `audio_atualizado`
   - `audio_moderacao_atualizado`

## 6) Regras de coexistência (obrigatórias)
1. Ficha continua fonte canônica de atributos/perícias/magias/equipamentos.
2. VTT continua fonte canônica de estado de sessão (posição, voz, presença).
3. Qualquer conflito deve seguir prioridade definida em `INTEGRACAO_CONTRATOS.md`.
4. Não introduzir lógica paralela de cálculo que diverge do app sem flag explícita.

## 7) Sequência de implementação autorizada
Seguir os lotes em:
1. `PROGRESS_VTT+FICHA.md`

Ordem:
1. `VTT.1` contratos
2. `VTT.2` aba VTT (shell)
3. `VTT.3` sessão e presença
4. `VTT.4` rolagem por token
5. `VTT.5` voz e acessibilidade
6. `VTT.6` hardening e não regressão

## 8) Checklist antes de qualquer commit de integração
1. Nenhuma regressão nas abas atuais.
2. Import/export de ficha permanece funcionando.
3. Build compila nas variantes atuais.
4. Fluxo VTT só depende de novos componentes da aba `VTT` + camada de contrato.

## 9) Arquivos de referência úteis
Ficha:
1. `app/src/main/java/com/gurps/ficha/ui/FichaScreen.kt`
2. `FORMATO_INTEROPERABILIDADE_IMPORT_EXPORT.schema.json`

VTT externo:
1. `backend/index.js`
2. `frontend/src/utils/gurpsInterop.ts`
3. `frontend/src/utils/socketConfig.ts`
4. `ESPECIFICACAO_API.md`

## 10) Restrições operacionais
1. Não refatorar massa de UI da ficha junto com integração VTT.
2. Não alterar dados canônicos de catálogo durante lotes de integração.
3. Não misturar escopo de agente IA e escopo de integração VTT nesta trilha.

## 11) Entrega esperada do agente integrador
1. PR/commit por passo.
2. Atualização do `PROGRESS_VTT+FICHA.md` a cada passo concluído.
3. Evidência de validação mínima por passo.
