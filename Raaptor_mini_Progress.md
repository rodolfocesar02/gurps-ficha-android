# Raaptor_mini_Progress

**Data:** 01/03/2026

Este documento foi gerado para resumir o estado atual do projeto *GURPS Ficha Android* ("Raaptor mini"), descrevendo o escopo, arquitetura, lotes de trabalho, pontos de melhoria, saúde do projeto e áreas críticas a evitar.

---

## O que é o projeto?
Uma ficha de RPG para o sistema GURPS 4ª Edição, completamente em português, editável e automatizada. A interface é organizada em abas que são preenchidas conforme o jogador escolhe atributos, vantagens, desvantagens, perícias, técnicas etc.; a aba final **Rolagem** consolida a ficha e realiza jogadas automáticas apropriadas para envio no canal do Discord.

O projeto roda em duas variantes:

- **Visual**: layout gráfico convencional.
- **PRACEGO**: versão 100% acessível para leitores de tela (TalkBack), com controles rotulados, fluxo linear e espaçamentos adaptados.

A base de regras é o livro *GURPS 4ª Edição - Módulo Básico* acompanhado de suplementos que incrementam funcionalidades (armas, magias, artes marciais, técnicas etc.).

---

## Lotes e pontos de melhoria

### Lote 1 – Revisão de JSON e catálogos
- **Melhorias já entregues:** consolidação de armaduras, normalização de observações, suporta diversos formatos de notas.
- **Próximos passos:** migrar outros catálogos legados (armas, escudos) para o novo padrão, implementar validação de esquema JSON para evitar assets corrompidos.

### Lote 14 – Import/Export JSON
- **Melhorias entregues:** envelope versionado, compatibilidade retroativa, validação de versão e erros claros.
- **Sugestões:** adicionar botão de ver versão atual da ficha antes de exportar, permitir comparação visual ao importar.

### Lote 15 – Perícias, Técnicas e automação de cálculos
- **Melhorias entregues:** separação da aba Técnicas, regras de custo automático, pré-requisitos refinados, parser de requisitos complexo.
- **Áreas de automação ainda possíveis:** cálculo de NH e modificadores diretamente na ficha, geração de sugestões de técnicas/perícias baseado em atributos atuais, integração com personagens já salvos.

### Dados e normalização textual
- Auditoria e normalização de mojibake concluída.
- **Melhoria sugerida:** adicionar verificação automática de alias em tempo de execução para evitar erros de digitação do usuário ou do script; incluir revisão de idiomas (caso haja traduções futuras).

### Acessibilidade
- Versão PRACEGO avançada com rotulação e fluxo linear.
- **Melhoria:** testes com múltiplos leitores de tela e depuração de foco de elemento; adicionar guias de navegação por teclado para no emulador e para o Discord.

### Saúde do projeto (percentual)
- **Compilação e testes:** 100% verdes nas últimas execuções.
- **Suporte a dados antigos:** compatibilidade retroativa mantida em cada bloco.
- **Qualidade de código:** bom uso de Kotlin/Gradle, porém há áreas com código longo (ex.: `DataRepository.kt`) que poderiam ser refatoradas em módulos menores.
- **Porcentagem estimada:** **85%** (saudável, mas com espaço para refatoração e completeness de regras).

### Automação e regras
- Scripts Python robustos para conversão, validação e normalização.
- **Próximas automações:** gerar mapas de regras V2 automaticamente a partir de planilhas XLSX, testes de carga para assets grandes, CI que execute builds de emulador e testes de acessibilidade.

### Insights das regras básicas (PDF)
A leitura do `Módulo Básico` revela padrões que podem guiar melhorias na interface e na automação:

- **Perícias e Técnicas** são agrupadas por NT, com opções de especialização, familiaridade e custo pré-definido. O app já automatiza o custo por tabela, mas falta:
  - interface de escolha de perícia que mostre atributos dominantes e NT dificuldades diretamente;
  - mecanismo para sugestão de nível com base em atributos atuais e importação de valores pré-definidos do PDF;
  - agrupamento de perícias relacionadas (agrupadas por tema) para escolha mais rápida.
- **Tabelas de custo** aparecem em várias partes do livro; ter um componente reutilizável que calcule automaticamente pontos e recalcula conforme o jogador altera nível/dificuldade ajudaria a acelerar o preenchimento.
- **Valores pré-definidos** e regras de compra (consultas de custo ao comprar ou aprimorar perícias/magias/técnicas) devem ser extraídas do catálogo e exibidas ao animar a seleção do usuário.
- **Regras especiais** como grotescas (armazenar duplos, técnicas de combate rápidas) indicam necessidade de campos extras nos formulários.
- A interface deve permitir **comparação visual** de custos antes e depois de alterações, para decisões mais rápidas.
- **Botões de compra/improviso** poderiam ser agrupados em um painel único, com atalhos de teclado/talkback para acelerar uso por PRACEGO.

Essas observações derivadas do conteúdo justiçam a priorização de algumas melhorias descritas nos lotes acima e inspiram novas automações.

---

## Pontos críticos / áreas para não mexer ainda

1. **Modelo de dados de personagens:** mudanças podem invalidar fichas salvas; alterar apenas com migrador e validação extensiva.
2. **Compatibilidade PRACEGO:** qualquer mudança no layout deve ter revisão de rotulação; evitar reestruturações drásticas sem testar com TalkBack.
3. **Import/Export JSON:** não alterar o envelope legado sem manter fallback; a perda de compatibilidade afetaria usuários existentes.
4. **Processos de normalização de texto em massa:** manipulações in-place devem ser usadas com cuidado para não introduzir alias conflitantes.

---

## Considerações finais
O projeto está bem encaminhado, com blocos entregues em ritmo constante e sem erros nas compilações. Investir em testes automatizados (unitários e UI) e manter um ciclo contínuo de auditoria textual e de acessibilidade aumentará a robustez. A prioridade deve ser finalizar os catálogos restantes e polir a experiência de entrada de dados para tornar o preenchimento da ficha mais intuitivo.

Com atenção aos pontos críticos, o código atual serve de base sólida para expansão futura e suporte a regras adicionais do GURPS.
