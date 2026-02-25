# PROGRESS - GURPS Ficha Android

Status atual do projeto para retomada rápida e execução em lotes pequenos.

## Fonte oficial de regras
- Livro base usado para alinhamento da ficha:
  - `c:\Users\Rodolfo\Desktop\rpg\gurps\livros\GURPS 4ª Edição - Módulo Básico.pdf`
- Estrutura extraída (578 páginas) salva para referência técnica:
  - `gurps-ficha-android/build/gurps_book_outline.txt`

## Concluído (engenharia/base)
- Testes unitários iniciais de regras.
- Extração de regras para domínio.
- Persistência com Room + migração do legado.
- Refatoração de UI por tabs/dialogs.
- Modelo de `Personagem` padronizado para listas imutáveis.
- Confirmação antes de limpar magias ao perder Aptidão Mágica.
- Filtro de perícias para Apara/Bloqueio refinado.
- Correção de encoding/mojibake na UI.
- Higiene de repositório (`.gitignore`) e toolchain (Gradle wrapper em `8.10.2`).
- UX do campo numérico de pontos iniciais ajustada.
- Robustez de parsing dos JSONs:
  - decisão: manter estrutura atual por compatibilidade e evoluir incrementalmente (sem migração brusca agora).
  - `PericiaDefinicao` passou a aceitar chaves alternativas de `atributosPossiveis` (incluindo grafia legada com encoding incorreto).
  - `DataRepository` normaliza campos textuais ao carregar (trim/sanitização leve).
  - teste adicionado para parsing legado:
    - `app/src/test/java/com/gurps/ficha/model/PericiaJsonParsingTest.kt`
- Aba Equipamentos simplificada:
  - card `Resumo de Carga` removido por redundância com informações já exibidas em `Geral` e `Combate`.
  - arquivo: `app/src/main/java/com/gurps/ficha/ui/TabEquipamentos.kt`.
- Estrutura futura da aba Equipamentos:
  - placeholder discreto `Próximos Recursos` adicionado para expansão incremental sem refatoração de layout.
  - arquivo: `app/src/main/java/com/gurps/ficha/ui/TabEquipamentos.kt`.
- Lote 1 - Criação de Personagem (fechado por enquanto):
  - pacote 1: regras derivadas (deslocamento mínimo 1 e custo de Velocidade Básica por passos de 0.25).
  - pacote 2: tabela de dano GdP/GeB alinhada à tabela oficial na faixa ST 1-30.
  - pacote 3: revisão de deslocamento básico/velocidade com testes de borda, mantendo regra atual.
  - item de campanha (pontos iniciais/limite de desvantagens por tipo de campanha) mantido sem alteração por decisão do usuário.
- Pipeline de vantagens v3 (estrutura nova, sem substituir produção ainda):
  - schema final criado: `app/src/main/assets/vantagens.v3.schema.json`.
  - script de conversão da planilha criado: `scripts/convert_vantagens_v3.py`.
  - arquivo inicial gerado da planilha: `app/src/main/assets/vantagens.v3.json` (259 itens, validado no schema).
  - checkpoint de segurança criado antes da geração: `snapshots/checkpoint-20260224-005055`.
  - atualização: regenerado a partir de `vantagens_extraidas_fiel_v2.xlsx` (264 itens, validado no schema).
  - tags mínimas úteis aplicadas por heurística no pipeline: `combate`, `social`, `fisica`, `mental`, `magica`.
  - política para custos híbridos definida no conversor:
    - casos como `X ou Y/nível` ficam em `costKind = special`, preservando `options` e metadado de nível para tratamento explícito no código.
- Pipeline de desvantagens v2 (estruturado, baseado na planilha atualizada):
  - conversor criado: `scripts/convert_desvantagens_v2.py`.
  - schema criado: `app/src/main/assets/desvantagens.v2.schema.json`.
  - arquivo gerado: `app/src/main/assets/desvantagens.v2.json` (227 itens).
  - custos convertidos para tipos estruturados:
    - `fixed`: 157
    - `choice`: 38
    - `range`: 1
    - `perLevel`: 12
    - `special`: 19
  - normalização aplicada: todos os custos numéricos ficam negativos para desvantagens.
  - taxonomia manual do usuário aplicada nas tags de desvantagens (`combate/social/fisica/mental/magica`) com cobertura alta.
  - resultado após aplicação:
    - `combate`: 5
    - `social`: 13
    - `fisica`: 86
    - `mental`: 102
    - `magica`: 14
  - pendências de ID não encontrado da lista manual: `feicoes_estranhas`, `ressacas_terriveis`.
- Integração de leitura de vantagens v3 no app:
  - `DataRepository` agora carrega `app/src/main/assets/vantagens.v3.json` e converte para o modelo legado em memória.
  - fallback legado removido: vantagens agora vêm somente de `app/src/main/assets/vantagens.v3.json`.
  - parser de `vantagens.v3.json` reforçado (leitura por `JsonParser` item a item) para evitar lista vazia em runtime por incompatibilidade de parse tipado.
  - `aptidao_magica` e `elo_mental` passam como `POR_NIVEL` no app (configuração por nível), mantendo custo híbrido no domínio (`5 + 10/nível`).
  - validação executada com sucesso: `.\gradlew.bat :app:compileDebugKotlin testDebugUnitTest --no-daemon`.
- Busca de vantagens por tags (v3):
  - `VantagemDefinicao` passou a carregar `tags`.
  - filtro de vantagens passou a aceitar tag opcional.
  - diálogo `Selecionar Vantagem` recebeu chips: `Todas`, `combate`, `social`, `fisica`, `mental`, `magica`.
  - validação executada com sucesso: `.\gradlew.bat :app:compileDebugKotlin testDebugUnitTest --no-daemon`.
- Varredura e normalização de custos híbridos em `vantagens.v3.json`:
  - conversor (`scripts/convert_vantagens_v3.py`) atualizado para tratar opções por nível com 2+ opções (ex.: `3, 5, ou 8/nível`) como `special`.
  - override canônico aplicado para `apetrechos` (`5/apetrecho`).
  - arquivo `app/src/main/assets/vantagens.v3.json` regenerado da planilha `vantagens_extraidas_fiel_v2.xlsx` (264 itens).
- Lote 3 (Perícias) - pacote 1 iniciado:
  - correção da tabela de bônus por pontos para pontos intermediários (especialmente `3 pontos`) em `CharacterRules.calcularBonusPorDificuldade`.
  - cobertura de testes ampliada em `RulesLayerTest` para os degraus `1, 2, 3, 4, 8, 12` nas quatro dificuldades.
  - validação executada com sucesso: `.\gradlew.bat :app:compileDebugKotlin testDebugUnitTest --no-daemon`.
- Lote 3 (Perícias) - pacote 2 iniciado:
  - `PericiaDefinicao` atualizado para aceitar variações de chave de `preDefinicoes` (`preDefinições` e forma legada com encoding incorreto).
  - testes de parsing ampliados em `PericiaJsonParsingTest` cobrindo `preDefinicoes` com chaves acentuada/legada.
  - validação executada com sucesso: `.\gradlew.bat :app:compileDebugKotlin testDebugUnitTest --no-daemon`.
- Lote 3 (Perícias) - pacote 3 iniciado:
  - exibição de NH relativo adicionada na lista da aba Perícias (`Nível X (ATR+Y)`), alinhando com os diálogos de criação/edição.
  - validação executada com sucesso: `.\gradlew.bat :app:compileDebugKotlin testDebugUnitTest --no-daemon`.
- Lote 3 (Perícias) - fechado nesta etapa.
- Lote 4 (Magia) - pacote 1 iniciado:
  - filtro de magias ampliado para combinar `escola` + `classe` no repositório.
  - UI de seleção de magias passou a ter chips de filtro por `classe`.
  - exibição de NH nos diálogos de magia ajustada para base `IQ+AM` e relativo consistente.
  - exibição da dificuldade de magia ajustada para usar a sigla real (`F/M/D/MD`) em vez de simplificação `D/MD`.
  - pré-requisitos de magia mantidos sem alteração (decisão explícita do usuário).
  - validação executada com sucesso: `.\gradlew.bat :app:compileDebugKotlin testDebugUnitTest --no-daemon`.
- Lote 4 (Magia) - pacote 2 iniciado:
  - regra de custo mínimo de magia reforçada (mínimo `1 ponto`) no repositório/viewmodel e no total da ficha.
  - teste adicionado cobrindo normalização de custo mínimo em `PersonagemRulesTest`.
  - validação executada com sucesso: `.\gradlew.bat :app:compileDebugKotlin testDebugUnitTest --no-daemon`.
- Lote 4 (Magia) - pacote 3 iniciado:
  - classes de magia agrupadas para filtro por tags de domínio:
    - `Bloqueio` (família `Bloq./Bloqueio...`)
    - `Comum` (família `Com./Comm/Comum...`)
    - `Encantamento` (família `Encant...`)
    - `Especial`, `Informação`, `Projétil`, `Toque`, `Área` (demais famílias definidas pelo usuário)
  - robustez de agrupamento reforçada para variações de OCR/acentuação.
  - total de classes no filtro reduzido para 8 categorias agrupadas.
  - validação executada com sucesso: `.\gradlew.bat :app:compileDebugKotlin testDebugUnitTest --no-daemon`.
- Lote 4 (Magia) - pacote 4 iniciado:
  - checklist funcional automatizado para cenários de NH com e sem Aptidão Mágica adicionado em `PersonagemRulesTest`.
  - cobertura de cenário real: dificuldade D com 1 ponto sem AM e dificuldade M com 8 pontos com/sem AM.
  - validação executada com sucesso: `.\gradlew.bat :app:compileDebugKotlin testDebugUnitTest --no-daemon`.
- Lote 4 (Magia) - fechado nesta etapa.
- Lote 5 (Combate) - pacote 1 iniciado:
  - testes de borda adicionados para Esquiva/Apara/Bloqueio na camada de regras (mínimo de Esquiva, arredondamento floor de NH/2 + 3).
  - testes de modelo ampliados para cenário sem perícia configurada (retorno `null`) e validação de DB somente para equipamento do tipo `ESCUDO`.
  - validação executada com sucesso: `.\gradlew.bat :app:compileDebugKotlin testDebugUnitTest --no-daemon`.
- Lote 5 (Combate) - pacote 2 iniciado:
  - robustez no cálculo de DB de escudo: seleção por nome agora ignora caixa (`upper/lower`) e espaços extras, mantendo filtro estrito por tipo `ESCUDO`.
  - teste adicionado para cenário com nome normalizado e item `GERAL` homônimo (continua ignorado).
  - validação executada com sucesso: `.\gradlew.bat :app:compileDebugKotlin testDebugUnitTest --no-daemon`.
- Lote 5 (Combate) - pacote 3 iniciado:
  - teste comparativo de regra de ficha adicionado para confirmar comportamento de carga nas defesas ativas:
    - carga impacta `Esquiva` conforme esperado;
    - `Apara` e `Bloqueio` permanecem estáveis para a mesma perícia/escudo (sem regra tática de turno).
  - validação executada com sucesso: `.\gradlew.bat :app:compileDebugKotlin testDebugUnitTest --no-daemon`.
- Lote 5 (Combate) - pacote 4 iniciado:
  - testes finais de borda adicionados na camada de regras:
    - comparação de `Esquiva` com carga `0` vs `5`;
    - confirmação de `floor(NH/2)+3` para NH ímpar/par em `Apara/Bloqueio`;
    - confirmação de soma linear de DB no `Bloqueio` (`+0/+1/+2`).
  - validação executada com sucesso: `.\gradlew.bat :app:compileDebugKotlin testDebugUnitTest --no-daemon`.
- Lote 5 (Combate) - fechado nesta etapa.
- Tabela comparativa app x livro (defesas ativas):
  - Esquiva:
    - Livro: `floor(Velocidade Básica + 3)` com penalidade por carga.
    - App: `CombatRules.calcularEsquivaBase/esquiva` usa base `personagem.esquiva` e subtrai `nivelCarga` (mínimo `1`).
    - Status: aderente ao escopo atual da ficha.
  - Apara:
    - Livro: `3 + floor(NH/2)` (sem regras táticas nesta fase).
    - App: `CombatRules.calcularAparaBase` aplica `floor(NH/2)+3`; bônus manual separado.
    - Status: aderente ao escopo atual da ficha.
  - Bloqueio:
    - Livro: `3 + floor(NH/2)` + DB do escudo.
    - App: `CombatRules.calcularBloqueioBase` + DB por equipamento `ESCUDO` selecionado + bônus manual.
    - Status: aderente ao escopo atual da ficha.
- Lote 6 (Equipamento e economia) - pacote 1 iniciado:
  - conversor inicial criado: `scripts/convert_armas_cc_v1_raw.py`.
  - fonte OCR importada com sucesso para JSON bruto versionado:
    - entrada: `Tabela_de_Armas_de_Combate_Corpo_a_Corpo.xlsx` (usuário).
    - saída: `app/src/main/assets/armas_corpo_a_corpo.v1.raw.json` (`61` itens).
  - estratégia aplicada: preservar campos textuais do livro (`raw`) e adicionar normalizações básicas por modo (`/`) para custo/peso/alcance/aparar.
  - sem integração de UI nesta etapa (apenas pipeline/base de dados para evolução incremental).
- Lote 6 (Equipamento e economia) - pacote 2 iniciado:
  - normalização v1 criada a partir do `raw`:
    - script: `scripts/normalize_armas_cc_v1.py`
    - saída: `app/src/main/assets/armas_corpo_a_corpo.v1.normalized.json`
  - estrutura normalizada inclui:
    - `dano` parseado (`base` + `tipo`),
    - `stMinimo` com flags (`dagger/double_dagger`),
    - `modos[]` alinhando `alcance/aparar/custo/peso` por índice de modo.
  - revisão assistida habilitada por `reviewFlags` para linhas com risco de OCR/desambiguação.
  - status atual da normalização:
    - itens totais: `61`
    - itens com `reviewFlags`: `32`
  - sem integração de UI nesta etapa (pipeline de dados evoluído incrementalmente).
- Lote 7 (Evolução e manutenção da ficha) - pacote 1 iniciado:
  - teste de persistência round-trip (`toJson` -> `fromJson`) adicionado em `PersonagemRulesTest`.
  - cenário coberto: ficha com alterações combinadas (atributos, vantagens, desvantagens, perícias, magias, equipamentos e defesas ativas).
  - validações do teste:
    - preservação estrutural dos dados persistidos;
    - consistência dos cálculos derivados após restauração (`nivelCarga`, `deslocamentoAtual`, defesas ativas, pontos gastos/restantes).
  - validação executada com sucesso: `.\gradlew.bat :app:compileDebugKotlin testDebugUnitTest --no-daemon`.
- Lote 6 (Equipamento e economia) - pacote 3 iniciado:
  - armas à distância: planilha `tabela de armas a distancia.xlsx` analisada e detectada com apenas cabeçalho (sem linhas de dados).
  - fallback aplicado com extração draft via PDF OCR (`armas a distancia.pdf`):
    - script: `scripts/extract_armas_distancia_v1_from_pdf.py`
    - saída: `app/src/main/assets/armas_distancia.v1.raw.json` (`18` itens extraídos da tabela da pág. 276).
  - arquivo de revisão gerado no mesmo padrão:
    - `app/src/main/assets/armas_distancia.v1.review_flags.json`
  - observação: integração no app permanece pausada nesta etapa; dados de distância ficam como base de pipeline para refinamento.
- Lote 6 (Equipamento e economia) - pacote 4 iniciado:
  - normalização das armas à distância gerada no mesmo padrão de pipeline:
    - script: `scripts/normalize_armas_distancia_v1.py`
    - saída: `app/src/main/assets/armas_distancia.v1.normalized.json`
  - arquivo de revisão atualizado a partir do normalizado:
    - `app/src/main/assets/armas_distancia.v1.review_flags.json`
  - status atual:
    - itens totais: `18`
    - itens com `reviewFlags`: `1` (`st_minimo_nao_numerico`).
  - observação: extração atual cobre a tabela da página 276 (OCR). Demais tabelas de armas à distância permanecem para expansão incremental.
- Lote 6 (Equipamento e economia) - pacote 5 iniciado:
  - integração de armas na aba `Equipamentos` implementada com dados automáticos:
    - catálogo combinado de armas corpo a corpo + distância carregado do `normalized` no `DataRepository`;
    - seleção por diálogo com busca textual, ordenação alfabética e filtro por tipo (`Todas`, `Corpo a corpo`, `Distância`);
    - regra de ST aplicada na seleção: só armas com `stMinimo <= ST` do personagem ficam disponíveis.
  - ao selecionar arma:
    - item é adicionado na ficha com `nome/peso/custo` preenchidos automaticamente;
    - dano da arma exibido no item com resolução baseada no ST atual (`GdP/GeB` -> valor calculado).
  - aba `Equipamentos` recebeu resumo de custo/peso na própria tela.
  - testes adicionados:
    - `RulesLayerTest`: resolução de dano por ST;
    - `PersonagemRulesTest`: equipamento de arma resolve dano com ST.
  - validações executadas com sucesso:
    - `.\gradlew.bat :app:compileDebugKotlin testDebugUnitTest --no-daemon`
    - `.\gradlew.bat :app:assembleDebug --no-daemon`
- Lote 6 (Equipamento e economia) - pacote 6 iniciado:
  - armas à distância regeneradas a partir da planilha atualizada do usuário:
    - entrada: `Tabela_de_Armas_a_Distancia.xlsx`
    - conversor novo: `scripts/convert_armas_distancia_v1_raw.py`
  - saídas atualizadas:
    - `app/src/main/assets/armas_distancia.v1.raw.json` (`28` itens)
    - `app/src/main/assets/armas_distancia.v1.normalized.json` (`28` itens)
    - `app/src/main/assets/armas_distancia.v1.review_flags.json` (`0` itens com flags)
  - observação: esta base substitui o draft anterior extraído do PDF, reduzindo inconsistências de OCR.
- Lote 6 (Equipamento e economia) - pacote 7 iniciado:
  - tabela de escudos integrada a partir da planilha do usuário:
    - script: `scripts/convert_escudos_v1.py`
    - saída: `app/src/main/assets/escudos.v1.json` (`7` itens)
  - aba `Equipamentos` recebeu seleção de escudos de tabela (`Adicionar Escudo`) com preenchimento automático de:
    - nome, custo, peso e `DB` (`bonusDefesa`) no equipamento.
  - integração com `Defesas Ativas` (Bloqueio):
    - aba `Combate` agora permite selecionar o escudo equipado para Bloqueio quando a perícia `Escudo` está ativa;
    - `DB` do escudo selecionado é somado automaticamente ao Bloqueio;
    - fallback automático: sem seleção explícita, usa o maior `DB` entre os escudos equipados.
  - testes e validação:
    - teste adicionado para fallback de `DB` no Bloqueio (`PersonagemRulesTest`);
    - `.\gradlew.bat :app:compileDebugKotlin testDebugUnitTest --no-daemon` com sucesso;
    - `.\gradlew.bat :app:assembleDebug --no-daemon` com sucesso.
- Desvantagens v2 integradas e forçadas:
  - fonte de desvantagens fixada em `app/src/main/assets/desvantagens.v2.json` (fallback legado removido).
  - diálogo `Selecionar Desvantagem` recebeu chips de tags: `Todas`, `combate`, `social`, `fisica`, `mental`, `magica`.
  - validação executada com sucesso: `.\gradlew.bat :app:compileDebugKotlin testDebugUnitTest --no-daemon`.
  - APK debug atualizado gerado: `.\gradlew.bat :app:assembleDebug --no-daemon`.

## Em andamento
- Nenhum item ativo no momento.

## Checklist do ponto salvo
- [x] Fonte de vantagens forçada para `app/src/main/assets/vantagens.v3.json`.
- [x] Busca por tags em vantagens (`combate`, `social`, `fisica`, `mental`, `magica`).
- [x] Correção de parse runtime do `v3` (lista não fica vazia).
- [x] Ajustes de custo híbrido: `aptidao_magica`, `elo_mental`, `dx_bracal`, `st_bracal`, `apetrechos`.
- [x] APK debug atualizado gerado para teste manual.
- [x] Fonte de desvantagens forçada para `app/src/main/assets/desvantagens.v2.json`.
- [x] Busca por tags em desvantagens (`combate`, `social`, `fisica`, `mental`, `magica`).

## Ponto de salvamento
- Data: 2026-02-24
- Marco: Lote 5 (Combate) fechado nesta etapa com tabela comparativa `app x livro` para defesas ativas.
- Snapshot: `snapshots/checkpoint-20260224-151219` (`git-status`, `PROGRESS.md`, `vantagens.v3.json`, `desvantagens.v2.json`).
- Status de validação: `.\gradlew.bat :app:compileDebugKotlin testDebugUnitTest --no-daemon` executado com sucesso.
- APK debug atualizado gerado: `.\gradlew.bat assembleDebug --no-daemon` (`app/build/outputs/apk/debug/app-debug.apk`).

## Backlog de aderência ao livro (priorizado)

### Lote 1 - Criação de Personagem (cap. 1, p. 11+)
- Validar custo de atributos básicos e secundários com exemplos do livro.
- Revisar limites e regras de pontos iniciais/desvantagens por template de campanha.
- Conferir fórmulas derivadas: BC, dano GdP/GeB, velocidade, deslocamento, carga.
- Entregável: checklist de conformidade + ajustes pontuais em regras.

### Lote 2 - Vantagens e Desvantagens (cap. 2-3, p. 33+ / p. 120+)
- Revisar custo por nível, custo variável e custos por autocontrole.
- Tratar casos especiais de vantagens com regras próprias (ex.: Aptidão Mágica).
- Garantir bloqueios/alertas para combinações inválidas.
- Entregável: matriz de regras por tipo de custo + testes unitários cobrindo casos de borda.

### Lote 3 - Perícias (cap. 4, p. 168+)
- Conferir fórmula de NH por dificuldade/pontos (1,2,4,8,+4).
- Validar pré-definições, especializações obrigatórias e atributos alternativos.
- Ajustar cálculo e exibição de NH relativo em todos os diálogos.
- Entregável: bateria de testes de perícias (fácil/média/difícil/muito difícil).

### Lote 4 - Magia (cap. 5, p. 235+)
- Revisar integração IQ + Aptidão Mágica no NH de magia.
- Conferir filtros (escola/classe), pré-requisitos e custo em pontos.
- Validar comportamento ao ganhar/perder Aptidão Mágica em cenários reais.
- Entregável: checklist funcional de magia + testes de regressão.

### Lote 5 - Combate (cap. 11-14, p. 363+)
- Validar Esquiva, Apara e Bloqueio contra fórmulas-base da ficha (sem regras de turno/manobra da jogabilidade).
- Revisar impacto de carga e equipamentos nas defesas.
- Não implementar situações táticas de mesa (postura, manobra, retirada, múltiplas defesas por turno, atordoamento de combate etc.) nesta fase.
- Entregável: tabela comparativa app x livro para defesas ativas.

### Lote 6 - Equipamento e economia (cap. 8, p. 265+)
- Revisar custos/pesos e impacto em carga.
- Validar recursos iniciais e consistência com riqueza/NT quando aplicável.
- Entregável: critérios de validação para inventário e custos.

### Lote 7 - Evolução e manutenção de ficha (cap. 9, p. 291+)
- Revisar evolução de personagem (pontos ganhos/gastos).
- Garantir persistência consistente ao longo de alterações complexas.
- Entregável: roteiro de teste de campanha longa.

## Pendências priorizadas (próximos passos)
- Lote 6 (Equipamento e economia): iniciar revisão de custos/pesos e impacto em carga no escopo da ficha.
- Lote 2 (Vantagens/Desvantagens): pausado temporariamente até recebimento das tags finais de desvantagens.

## Decisões de escopo
- 2026-02-24: descartada a implementação de situações de jogabilidade de combate na ficha nesta etapa do projeto.

## Regra de execução
- Não alterar visual/textos sem necessidade de regra.
- Cada lote:
  1. levantar regra no livro,
  2. comparar com implementação atual,
  3. corrigir em mudanças pequenas,
  4. validar com build/testes,
  5. atualizar este `PROGRESS.md`.

## Validação padrão
- Compilar + testes unitários:
  - `.\gradlew.bat :app:compileDebugKotlin testDebugUnitTest --no-daemon`
- Gerar APK debug:
  - `.\gradlew.bat assembleDebug --no-daemon`
  - saída: `app/build/outputs/apk/debug/app-debug.apk`
