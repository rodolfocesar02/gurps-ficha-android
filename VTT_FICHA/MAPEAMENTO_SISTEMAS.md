# Mapeamento de Sistemas - VTT x Ficha

## Ficha Android (fonte canônica do personagem)
Responsável por:
1. Edição completa da ficha (Geral, Traços, Perícias, Técnicas, Magias, Equipamentos, Defesas).
2. Regras e cálculos canônicos da ficha.
3. Persistência local, exportação/importação de JSON interoperável.

Não muda nesta integração:
1. Fluxos e regras das abas acima.
2. Motor de cálculo existente.

## VTT (fonte canônica da sessão)
Responsável por:
1. Mapa/grid/token/linha de visão.
2. Voz e comunicação em sala.
3. Eventos em tempo real (movimento, chat, rolagem contextual no token).

## Fronteira de integração
1. Entrada no VTT ocorre pela nova aba `VTT` do app.
2. App envia identidade de personagem + snapshot da ficha para sessão.
3. VTT devolve ID de sessão/token e publica eventos para UI do jogador.
4. Resultado de rolagem no VTT deve ser compatível com os valores da ficha.

## Regras de coexistência
1. A ficha continua sendo o editor oficial de personagem.
2. O VTT é a interface de mesa (combate/voz/mapa/rolagem contextual por token).
3. Divergência de dados: resolução por contrato (timestamp + versão + origem).
