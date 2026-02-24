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

## Em andamento
- Nenhum item ativo no momento.

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
- Validar Esquiva, Apara e Bloqueio contra fórmulas e modificadores do livro.
- Revisar impacto de carga e equipamentos nas defesas.
- Ajustar regras de situação especial que estiverem fora do escopo atual.
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
- Planejar Lote 2 (Vantagens/Desvantagens) em modo seguro:
  - não alterar `app/src/main/assets/vantagens.json` e demais `*.json` sem autorização explícita.
  - priorizar ajustes em regras/código/testes primeiro.

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
