# Mapa de Foco - PraCego (V2.4)

Objetivo: manter uma ordem previsível de navegação TalkBack nos fluxos principais.

## 1) Menu principal da ficha
Ordem de foco:
1. Nova Ficha
2. Salvar Ficha
3. Carregar Ficha
4. Exportar JSON Compatível
5. Exportar JSON Versionado
6. Importar Ficha (JSON)
7. Atualizar app
8. Fechar

Implementação:
- `Modifier.pracegoTraversal(...)` aplicado em `DialogsCommon.kt`.

## 2) Aba Perícias
Ordem de foco inicial:
1. Adicionar Perícia
2. Criar Perícia
3. Perícias Suplementares

Implementação:
- `Modifier.pracegoTraversal(...)` aplicado em `TabPericias.kt`.

## 3) Aba Técnicas
Ordem de foco inicial:
1. Adicionar Técnica

Implementação:
- `Modifier.pracegoTraversal(...)` aplicado em `TabTecnicas.kt`.

## 4) Aba Magias
Ordem de foco inicial:
1. Adicionar Magia

Implementação:
- `Modifier.pracegoTraversal(...)` aplicado em `TabMagias.kt`.

## Observações
- O mapa foca os pontos de entrada de fluxo e o menu principal.
- Próximo refinamento futuro: estender `traversalIndex` para diálogos de seleção longos.
