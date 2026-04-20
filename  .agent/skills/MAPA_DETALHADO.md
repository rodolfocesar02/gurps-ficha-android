# Mapa Detalhado: Arquivos e Funções do Projeto GURPS

Este arquivo serve como o mapa definitivo de engenharia para o projeto. Utilize-o para localizar lógicas específicas sem a necessidade de varredura completa do código.
---
## 1. ViewModels e Controle de Estado (O Cérebro)
*Onde a lógica de negócio e o estado da UI residem.*
- **`FichaViewModel.kt`**: O controlador central. Gerencia o personagem ativo, coordena salvamentos, e delega cálculos para os especialistas.
- **`FichaUIState.kt`**: Define os estados reativos da interface (carregando, sincronizando, erros).
- **`delegates/FichaPersistenceDelegate.kt`**: Especialista em salvar e carregar arquivos JSON do disco e gerenciar o Auto-Save.
---
## 2. Domain & Engines (As Regras do RPG)
*Onde a matemática do GURPS 4ª Edição acontece.*
- **`engine/SkillEngine.kt`**: Calcula NH (Nível de Habilidade), bônus de atributos e custos de XP para Perícias.
- **`engine/MagicEngine.kt`**: Gerencia o custo de mana, pré-requisitos e tempos de conjuração de Magias.
- **`rules/CharacterRules.kt`**: Define limites de atributos, PV (Pontos de Vida) e PF (Pontos de Fadiga).
- **`rules/CombatRules.kt`**: Onde moram as regras de defesa ativa, bônus de escudo (BD) e bônus de Mestre de Armas.
- **`rules/MagiaEnergiaRules.kt`**: Regras específicas para recuperação de energia e custos extras.
- **`motor modo alvo/src/NexusArcanoEngine.kt`**: O Maestro do sistema "Modo Alvo". Orquestra o parser, o avaliador de regras e o buscador de caminhos para liberar magias complexas.
- **`regras_prerequisitos/PreRequisitoChecker.kt`**: O motor legado/base que valida se um personagem pode ter certas magias ou perícias de forma isolada.
---
## 3. UI - Telas e Abas (O Corpo)
*Componentes visuais e interações do usuário.*
- **`FichaScreen.kt`**: O container principal que abriga o Scaffold e a navegação entre abas.
- **`TabRolagem.kt`**: O Hub de Combate Unificado. Gerencia rolagens de ataque, defesa e dano.
- **`TabGeral.kt`**: Exibe Atributos, PV, PF e informações básicas.
- **`TabCombate.kt`**: Visão detalhada de armas, defesas e equipamentos de proteção.
- **`TabPericias.kt` / `TabMagias.kt`**: Listagem e edição de habilidades do personagem.
- **`TabTracos.kt`**: Vantagens e Desvantagens.
- **`TabEquipamentos.kt`**: Gerenciamento de itens e peso.
---
## 4. Model (Os Dados)
*Representação dos objetos do mundo real.*
- **`Personagem.kt`**: O objeto raiz que contém tudo o que define o personagem.
- **`Atributo.kt` / `Vantagem.kt` / `Equipamento.kt`**: Classes de dados (Data Classes).
- **`PersonagemInterop.kt`**: Camada de tradução para importar/exportar JSONs entre versões diferentes.
---
## 5. Data, Network & Social (Conectividade)
*Persistência de dados, serviços externos e integração social.*
- **`DataRepository.kt`**: Ponto único de acesso aos catálogos oficiais e aos IDs de canais do Discord salvos.
- **`storage/FichaDatabase.kt`**: Configuração do SQLite (Room).
- **`network/DiscordRollApiClient.kt`**: O motor de envio. Transfere os resultados das rolagens para o Discord via Webhook.
- **`viewmodel/delegates/FichaSocialDelegate.kt`**: Gerencia a ativação/desativação do envio automático e as preferências de rede.
- **`network/MestreIAClient.kt`**: Interface de comunicação com o backend da IA.
---
## 6. Update e VTT (Recursos Extras)
*Sistema de atualização e integração virtual.*
- **`update/AppUpdateService.kt`**: Verifica novas versões no GitHub e gerencia o download.
- **`vtt/VttRollService.kt`**: Sincroniza dados com mesas virtuais (Foundry/Roll20).
---
## 7. Recursos e Catálogos (A Alma do RPG)
*Onde os dados estatísticos de GURPS residem (Pasta `assets/`).*
- **`pericias.json`**: Banco de dados completo de perícias, dificuldades e atributos base.
- **`magias.json`**: Lista oficial de magias, custos, tempos e classes.
- **`vantagens.json` / `desvantagens.json`**: Descrições e custos de pontos.
- **`equipamentos.json`**: Catálogo de armas, armaduras e itens gerais.
---
## 8. Variantes e Build (As Versões do App)
*Diferenças cruciais entre as variantes de compilação.*
- **Variante `Pracego`**: Focada em acessibilidade total e TalkBack. Possui labels extras e diálogos simplificados para cegos.
- **Variante `Visual`**: Focada na melhor estética visual, com cores vibrantes e layouts mais densos.
- **`BuildConfig.UI_VARIANT`**: A chave usada no código para decidir qual lógica de interface aplicar.
---
## 9. Endereços VIP (Busca Instantânea)
*Funções críticas que mais sofrem manutenção.*

- **Cálculo de Esquiva/Apara/Bloqueio**: `CombatRules.kt` -> `calcularDefesa()`.
- **Bônus de Mestre de Armas**: `CombatRules.kt` -> `calcularDanoArma()`.
- **Consumo de FP/HP**: `CharacterRules.kt` -> `processarCustoRecurso()`.
- **Trava de Salvamento**: `FichaPersistenceDelegate.kt` -> `estaCarregando`.
---
##  10. Scripts e Automação (Ferramentas de Suporte)
*Onde as ferramentas de manutenção de dados residem (Pasta `scripts/`).*

- **`audit_active_jsons_v2.py`**: A ferramenta mais importante. Verifica falhas de integridade em todos os catálogos JSON.
- **`generate_pericias_v2_rules_map.py`**: Mapeia as regras complexas de perícias para o motor do jogo.
- **`fix_mojibake_project.py`**: Corrige erros de codificação de texto (acentuação corrompida).
- **`cleanup_assets_text.py`**: Normaliza textos e limpa resíduos de OCR de PDFs.
- **Série `convert_*.py`**: Scripts de transformação usados para migrar dados brutos para o formato do App.
---
##  12. Mestre Digital IA (A Inteligência do App)
*Onde a "mágica" de conversar, criar histórias e gerar fichas acontece.*

- **`network/MestreIAClient.kt`**: O mensageiro. Faz a chamada técnica para os servidores (Gemini, OpenRouter, DeepSeek) e extrai o JSON da resposta da IA processando chamadas de Tools.
- **`network/MestreIATools.kt`**: Catálogo de Ferramentas da IA. Fornece os esquemas validados (Schemas nativo Gemini / padrão OpenAI) de "Functions" que permitem à IA "Preencher Ficha" ou executar a "Busca de Regra".
- **`domain/MestreIARagEngine.kt`**: O motor RAG (Retrieval-Augmented Generation). Injeta as lógicas de pesquisa de regras baseada nas invocações (Tools) da IA.
- **`domain/MestreIAUseCase.kt`**: O tradutor. Orquestra as chamadas da IA e usa **Busca Fuzzy** (similaridade) para mapear decisões da IA para as perícias/magias reais do banco de dados local.
- **`ui/DialogsMestreIA.kt`**: A interface de chat. Onde o usuário digita as perguntas e vê as sugestões da IA.
- **`viewmodel/delegates/FichaIADelegate.kt`**: O gerente. Controla o histórico das conversas e a execução de **[AÇÕES]** instantâneas sugeridas pela IA.

**Recursos de IA suportados:**
1.  **Geração de Personagem**: Fluxo completo de transformar descrição em atributos e habilidades usando Schemas estruturados (Function Calling).
2.  **Consulente de Regras & RAG**: Tira dúvidas de GURPS enviando chamadas nativas de ferrametas (Tool Calls) para pesquisar no banco.
3.  **Comandos Diretos**: Processamento de tags como `[ATRIBUTO: ST 12]` enviadas pela IA.
---
##  13. Fluxo de Manutenção Sugerido
1.  **Modificar Lógica**: ViewModel -> Engines -> Model.
2.  **Modificar Dados**: Assets (.json) -> Rodar Script de Auditoria.
3.  **Compilar**: Escolher Variante -> Build APK.
---
##  14. Laboratório de Testes (Qualidade)
*Onde as regras de RPG são validadas automaticamente (Pasta `app/src/test/`).*

- **`rules/RulesLayerTest.kt`**: Onde as regras de Combate e Atributos são estressadas para garantir que 1+1 sempre seja 2.
- **`domain/magias/NexusArcanoLoteFCanonicScenarioTest.kt`**: Suíte de testes de "Cenário Ouro" (Desejo) que valida a progressão incremental de metas.
- **`domain/magias/NexusArcano*Test.kt`**: Uma suíte massiva de testes para o motor de magias (Nexus Arcano). Garante que pré-requisitos e custos de mana nunca quebrem.
- **`PersonagemRulesTest.kt`**: Valida a criação de personagens e limites de pontos.
- **`vtt/VttBridgeCodecStressTest.kt`**: Testa a robustez da conexão com o Foundry/VTT.
---
##  15. Infraestrutura Android (Manifesto e Configs)
*A base técnica que permite o app rodar no telefone.*

- **`AndroidManifest.xml`**: Onde as permissões de Internet (para IA/Discord) e Acesso a Arquivos são declaradas.
- **`build.gradle(.kts)`**: Define a versão do app (como a V1.4.5), as bibliotecas usadas e as variantes (Visual vs Pracego).
##  16. Detalhamento: Motor Nexus Arcano (Complexidade de Magia)
*Onde a resolução de dependências pesadas do GURPS 4E reside (Pasta `motor modo alvo/src/`).*

- **`NexusArcanoEngine.kt`**: Interface principal e orquestrador.
- **`ArcanoModels.kt`**: Dados básicos (Metas, Snapshots para a UI e Estados de Evolução).
- **`NexusArcanoHeuristics.kt`**: O especialista em regras (Avalia quantas magias de que escola o personagem tem).
- **`NexusArcanoParser.kt`**: Interpretador de texto bruto dos caminhos de magia (Separa perícias de magias).
- **`NexusArcanoPathfinder.kt`**: Algoritmo de Busca (DFS) para encontrar o caminho mais curto/lógico para uma magia alvo.
- **`NexusArcanoStrings.kt`**: Formatação de textos para a interface (Mensagens de erro e trilhas de aprendizado).
- **`domain/magias/NexusArcanoModoAlvoAdapter.kt`**: Faz o "de-para" entre o motor puramente lógico e a lista reativa que o usuário vê na tela.

---
> [!TIP]
> **DICA PARA O AGENTE**: Antes de finalizar qualquer mudança em regras de combate ou magias, **RODE OS TESTS** (Especialmente o `NexusArcanoLoteFCanonicScenarioTest.kt`). Se um teste do Nexus Arcano falhar, sua alteração causou um bug de regressão na resolução de dependências!