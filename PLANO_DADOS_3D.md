# Plano de Implementação: Dados 3D Nativos (SceneView 3.0)

Este documento reflete a decisão final e irrevogável de construir a rolagem de dados em 3D de forma **100% Nativa e Declarativa**, utilizando o SceneView 3.0 e integrando ao Jetpack Compose. A ideia de WebView foi descartada.

## A Arquitetura Nativa Escolhida

A stack de tecnologias para esta implementação será:
- **UI:** Jetpack Compose (telas, botões, painéis sobrepostos).
- **Renderização 3D:** `io.github.sceneview:arsceneview:3.0.0` (ou `sceneview` puro). É a biblioteca declarativa moderna que envelopa o motor Filament do Google.
- **Modelos:** Arquivos `.glb` (dados já modelados com materiais PBR, importados no diretório `assets/models/`).
- **Física:** Como o SceneView renderiza mas não calcula colisões complexas (RigidBody Dynamics com fricção), usaremos uma biblioteca de física em Kotlin/Java (ex: `dyn4j` ou `JBullet`) rodando em uma coroutine de gameloop. O `Node` do SceneView copiará a posição do corpo físico a cada frame.
- **Áudio:** `SoundPool` ou `AudioTrack` disparado por eventos de colisão detectados no motor de física.

---

## Execução em Lotes (Isolados da Aba Saga)

Para não gerar conflito com o agente que trabalha na Aba Saga, tudo será construído em pacotes dedicados (`ui/features/dice3d`, `domain/dice3d`).

### Lote 1: Setup e Cena Estática
**Objetivo:** Mostrar o dado na tela do Compose.
1. Adicionar dependências do SceneView 3.0 no `build.gradle.kts`.
2. Incluir um arquivo `dado_d6.glb` na pasta de assets.
3. Criar o Composable `Dice3DScene`, instanciando a `<Scene>` declarativa do SceneView.
4. Carregar o modelo `.glb` como um `ModelNode` no centro da câmera, configurando a iluminação ambiente (HDRI) nativa do Filament.
*Teste de Build:* Uma tela isolada exibindo o dado 3D em alta resolução, permitindo girar com o dedo (manipulação básica de câmera).

### Lote 2: O Motor de Física e o Gameloop
**Objetivo:** Fazer o dado cair e quicar de forma realista.
1. Integrar um motor de física puro (ex: `dyn4j`).
2. Criar a classe `PhysicsWorld` que contém um chão (plano invisível) e paredes.
3. Envolver o dado em uma `Box` (caixa de colisão) do mesmo tamanho do modelo `.glb`.
4. Criar a Coroutine do Gameloop (rodando a 60 FPS): ela avança a física (gravidade, giro, quique) e atualiza o `position` e `rotation` do `ModelNode` no SceneView.
5. Criar a lógica que avalia qual face parou virada para cima (usando produto escalar do vetor Y contra as normais das faces).
*Teste de Build:* Um botão "Arremessar" na UI. Ao clicar, o dado aparece no alto, cai, quica nas paredes invisíveis e para. O Logcat imprime "Caiu no número: X".

### Lote 3: Som, Polimento e Lógica GURPS
**Objetivo:** A sensação tátil do arremesso e resolução das regras.
1. Carregar amostras de áudio (impacto em madeira/plástico) usando `SoundPool`.
2. Detectar eventos de colisão (`CollisionListener` na física). Quanto maior a força do impacto (velocidade), mais alto o volume do toque.
3. Instanciar 3 dados simultâneos (3d6) com forças iniciais aleatórias para que se espalhem.
4. Integrar o resultado matemático final na `CriticoRules.kt` para calcular Sucesso, Falha, Decisivo, etc.
*Teste de Build:* Ao rolar, os 3 dados quicam entre si e no chão, emitindo sons condizentes com a porrada, e a UI exibe o sumário (Ex: "12 vs 10 - Falha").

### Lote 4: Conexão com a Aba de Rolagem
**Objetivo:** Substituir a rolagem chata pela rolagem 3D.
1. Conectar a interface do `Dice3DScene` como um overlay na `TabRolagem.kt`.
2. Pegar os modificadores (-4 a +4) preenchidos na Aba.
3. Ao finalizar a rolagem e os dados pararem, enviar o payload via `DiscordRollApiClient`.
*Teste de Build:* Fluxo completo no celular. O usuário clica na perícia "Espadas", o overlay 3D aparece, os dados rolam fisicamente, o som toca, o resultado finaliza e aparece lá no servidor do Discord.
