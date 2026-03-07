# Manual de Uso - GURPS Ficha Android

Este manual explica como preencher a ficha por aba e quais automacoes o app ja faz.

## 1. Antes de comecar
- Abra o app e preencha o nome da ficha no topo.
- A barra de pontos mostra:
  - `Pontos Iniciais`
  - `Gastos`
  - `Restantes`
- O menu (`...`) permite:
  - Nova ficha
  - Salvar / Carregar / Excluir ficha
  - Exportar JSON compativel
  - Exportar JSON versionado
  - Importar JSON

## 2. Abas da navegacao
Quando o personagem tem `Aptidao Magica`, aparece a aba `Magia`.
Sem `Aptidao Magica`, as abas ficam: Geral, Tracos, Pericias, Tecnicas, Equip., Defesas, Rolagem.

## 3. Aba Geral
Use para preencher base do personagem.

### O que preencher
- Dados basicos (nome, idade, aparencia, etc.)
- Atributos primarios: ST, DX, IQ, HT
- Secundarios customizaveis (PV, PF, Vontade, Percepcao, Velocidade/Deslocamento)

### Automacoes
- Recalculo de derivados ao alterar atributos
- Dano `GdP/GeB` automatico por ST
- Resumo detalhado de pontos por categoria

## 4. Aba Tracos (Vantagens, Desvantagens, Qualidades, Peculiaridades)

### O que preencher
- Adicione vantagens/desvantagens pelos botoes de cada secao
- Ajuste nivel/custo quando houver variacao
- Adicione qualidades e peculiaridades por texto

### Automacoes
- Custo final por nivel
- Regras especiais de custo/autocontrole
- Validacoes de limites por item
- Cards padronizados por item com editar/remover

## 5. Aba Pericias

### O que preencher
- Adicionar pericia do catalogo
- Ou criar pericia customizada
- Definir pontos gastos

### Automacoes
- NH calculado automaticamente
- Nivel relativo ao atributo base
- Filtros por atributo e dificuldade na selecao
- Resumo de total de pericias e pontos

## 6. Aba Tecnicas

### O que preencher
- Escolha tecnica no catalogo
- Selecione pericia base compativel
- Ajuste nivel acima do predefinido

### Automacoes
- Valida pre-requisito da tecnica antes de salvar
- Filtra pericias base coerentes com o pre-requisito
- Calcula custo e NH automaticamente
- Aplica limites de tecnica (ex.: predefinido +X)
- Regra adaptativa Visual/PraCego:
  - Visual: ajuste por swipe vertical
  - PraCego: botoes `+/-` com rotulos TalkBack

## 7. Aba Magia

### O que preencher
- Adicionar magia
- Ajustar pontos
- Preencher campos especiais quando exigido (especializacao, alvo de encantamento)

### Automacoes
- NH de magia calculado com IQ + Aptidao Magica
- Verificacao de pre-requisitos antes de adicionar
- Opcao de adicao forcada (quando habilitada no dialogo)
- Filtros por classe e escola
- Cards padronizados de lista

### Modo Alvo (assistente de progressao)
- Ative `Modo Alvo` e marque uma magia como `Alvo`
- O app:
  - Reaproveita magias ja aprendidas
  - Mostra proximas recomendadas para destravar a cadeia
  - Indica bloqueios de pre-requisito e progresso
- Ha manual automatico no primeiro uso, com opcao `Nao mostrar mais`

## 8. Aba Equipamentos

### O que preencher
- Adicionar item manual
- Adicionar arma por catalogo
- Adicionar escudo por catalogo
- Adicionar armadura com selecao de local

### Automacoes
- Peso total e custo total automaticos
- Carga aplicada no personagem
- Calculo de dano de arma com ST quando aplicavel
- Observacoes de catalogo exibidas automaticamente
- Aviso quando algum catalogo nao carregar
- Cards padronizados por item com editar/remover

## 9. Aba Defesas (Combate)

### O que preencher
- Configurar Apara (pericia base)
- Configurar Bloqueio (pericia de escudo + escudo equipado)
- Ajustar bonus manuais se necessario

### Automacoes
- Esquiva calculada automaticamente
- Apara calculada por NH da pericia
- Bloqueio calculado por NH + DB de escudo
- Atualizacao imediata quando pericia/equipamento muda

## 10. Aba Rolagem
Use para testes rapidos durante o jogo.

### O que pode rolar
- Atributos
- Pericias
- Tecnicas
- Magias
- Defesas

### Automacoes
- Valor alvo puxado automaticamente da ficha atual
- Historico de rolagens na sessao
- Rotulos de acessibilidade com valor atual (ex.: `Rolar ST 12`)
- Integracao com envio de rolagem (quando configurada)

## 11. Diferencas Visual x PraCego

### Visual
- Mais foco em gesto/swipe para ajuste de niveis em dialogs
- Interface otimizada para leitura rapida visual

### PraCego
- Botoes `+/-` mantidos nos dialogs relevantes
- Rotulos TalkBack nos controles principais
- Textos de apoio e resumo acessivel no Modo Alvo

## 12. Fluxo recomendado de preenchimento
1. Preencha `Geral` (atributos e base do personagem).
2. Adicione `Tracos`.
3. Adicione `Pericias`.
4. Adicione `Tecnicas`.
5. Se houver Aptidao Magica, adicione `Magias` (use Modo Alvo para cadeias longas).
6. Configure `Equipamentos`.
7. Ajuste `Defesas`.
8. Use `Rolagem` para jogar.

## 13. Dicas rapidas
- Se uma tecnica ou magia nao entra, abra o dialogo e leia a linha de pre-requisito.
- Em Magia, prefira seguir as recomendacoes do Modo Alvo para chegar mais rapido na magia final.
- Salve a ficha com frequencia e exporte JSON para backup.
