# Planejamento: Mesa Virtual GURPS (VTT Integration)

Este documento é a "Fonte da Verdade" para o sistema de sincronização bi-direcional entre App e Console Web.

## 1. Arquitetura de Conexão (Identidade via Discord)
- **Login Zero-Burocracia:** O Discord ID é a chave primária.
- **Deep Link:** `gurpsapp://conectar?id={discord_id}&token={session_token}`.
- **Gatilho de Voz:** O servidor Railway detecta entrada no Canal de Voz para ativar o espelhamento automático.

---

## 2. Especificação Técnica (O Protocolo)
Para garantir latência próxima de zero (< 500ms):

- **Transporte:** WebSockets (Socket.io) para comunicação Full-Duplex.
- **Payload de Sincronia:**
    ```json
    {
      "type": "UPDATE_STATUS",
      "playerId": "123456",
      "data": { "pv": 12, "pf": 10, "rd_local": { "cabeca": 5 } },
      "timestamp": "ISO_DATE"
    }
    ```
- **Payload de Comando (Mestre):**
    ```json
    {
      "type": "REMOTE_COMMAND",
      "targetId": "123456",
      "command": "SUBTRACT_PV",
      "value": 5,
      "source": "NARRADOR"
    }
    ```

---

## 3. Console do Narrador (Web Emulator)
- **Hospedagem:** Sub-rota no servidor Railway (`/mesa`).
- **Visual:** Reutilização dos assets CSS/Imagens do app Android para fidelidade visual 1:1.
- **Controles de Mestre:**
    - Botão de Dano/Cura rápida.
    - Aplicar penalidades de Fadiga.
    - **Bônus de Próxima Jogada:** O mestre pode enviar um mod de `+2` ou `-2` que aparecerá no celular do jogador para o próximo teste.

---

## 4. Segurança e Integridade
- **Auth Token:** Tokens temporários gerados pelo Bot expiram em 12 horas (uma sessão de jogo).
- **Consistência:** Em caso de queda de internet, o App mantém o log local e sincroniza assim que o sinal voltar.

---

## 5. Cronograma de Lotes (Execução)

### Lote 1: Fundação (Deep Link e Manifesto) - OK
- Intent Filters configurados.
- MainActivity preparada para capturar tokens.

### Lote 2: Infraestrutura de Rede (Railway + WebSockets)
- Adicionar Socket.io ao `server.js`.
- Criar a rota de "Lobby" para identificar quem está na mesa.

### Lote 3: Espelho Visual (HTML/CSS Dashboard)
- Desenvolver a interface que imita o celular no navegador.
- Renderização dinâmica baseada no JSON da ficha.

### Lote 4: Sincronização e Comandos Bi-direcionais
- Implementar o envio de dano do PC para o Celular.
- Implementar a atualização de vida do Celular para o PC.

---
*Assinado: Antigravity (IA Mentor)*
