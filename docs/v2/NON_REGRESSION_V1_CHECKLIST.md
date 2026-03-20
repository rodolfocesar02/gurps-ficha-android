# Checklist de Não-Regressão V1 (para cada release V2)

Objetivo: garantir que melhorias da V2 não quebrem a versão atual em uso.

## 1. Build mínimo obrigatório
- [ ] `./gradlew :app:assembleVisualDebug`
- [ ] `./gradlew :app:assemblePracegoDebug`

## 2. Testes críticos de regra
- [ ] `./gradlew :app:testVisualDebugUnitTest --tests com.gurps.ficha.regras_prerequisitos.PreRequisitoParserTest`
- [ ] `./gradlew :app:testVisualDebugUnitTest --tests com.gurps.ficha.domain.magias.NexusArcanoModoAlvoAdapterTest --tests com.gurps.ficha.domain.magias.NexusArcanoLoteFCanonicScenarioTest`
- [ ] `./gradlew :app:testVisualDebugUnitTest --tests nexus.arcano.NexusArcanoEngineLoteAGlobalTest --tests nexus.arcano.NexusArcanoEngineLoteBGlobalTest`

## 3. Verificação funcional manual (Visual + PraCego)
- [ ] Abrir ficha existente sem crash
- [ ] Salvar, carregar, exportar e importar JSON
- [ ] Adicionar/editar/remover: Vantagem, Desvantagem, Perícia, Técnica e Magia
- [ ] Validar pré-requisito de Técnica e Magia
- [ ] Validar aba Rolagem (atributo/perícia/técnica/magia/defesa)
- [ ] Validar rótulos essenciais no TalkBack (ações de editar/remover/abrir detalhes)

## 4. Higiene de texto (obrigatório)
- [ ] Varredura de artefatos (`Ã`, `Â`, `�`, `ï¿½`, `n?o`, `per?cia`, `pr?-requisito`)
- [ ] Correção aplicada na fonte canônica (JSON/UI) antes de gerar APK

## 5. Fechamento do lote
- [ ] Atualizar `PROGRESS.md` com evidências
- [ ] Commit objetivo por passo/lote (`lote-v2.x passo-y: ...`)
- [ ] Se aplicável, instalar versão visual no emulador e validar abertura
