# NOTE - NEXUS ARCANO

Status: em execu√ß√£o

## Escopo imediato
- Entregar um n√∫cleo que responda de forma determin√≠stica para `Desejo`.
- Garantir ordem obrigat√≥ria de trilha:
1. `Encantar`
2. `Pequeno Desejo`
3. `Desejo`
- S√≥ depois completar contador de escolas diferentes.

## Regra central (hard-first)
- Sempre resolver primeiro requisitos nomeados (cadeia obrigat√≥ria).
- Requisito por contagem (`N escolas`) nunca pode atropelar requisito nomeado pendente.

## Caso de refer√™ncia: Desejo
- Pr√© de `Desejo`: `Pequeno Desejo` + `1 m√°gica em 15 escolas`.
- Pr√© de `Pequeno Desejo`: `Encantar`.
- Pr√© de `Encantar`: `1 m√°gica em 10 outras escolas`.

### Ordem esperada no motor
1. Fechar chave `encantar_liberado`.
2. Fechar chave `pequeno_desejo_liberado`.
3. Fechar chave `escolas_15_ok`.
4. Fechar chave `desejo_liberado`.

## Contratos m√≠nimos do Lote 1
- `ArcanoEstadoPersonagem`
- `ArcanoChave`
- `ArcanoAcao`
- `ArcanoResultado`
- `NexusArcanoEngine.calcularEstadoAlvo(...)`

## Crit√©rios de aceita√ß√£o do Lote 1
1. Mesmo input gera mesma sa√≠da (ordem est√°vel).
2. N√£o sugerir magia j√° aprendida.
3. N√£o repetir escola enquanto houver escola nova v√°lida.
4. Se n√£o houver a√ß√£o imediata, devolver `motivoBloqueio` objetivo.

## D√≠vidas conhecidas
- Normaliza√ß√£o de nomes e escolas ainda depende do parser legado.
- Falta snapshot incremental por alvo (Lote 3).

## Pr√≥xima entrega pr√°tica
- Implementar avaliador hard-first real no arquivo:
`motor modo alvo/src/NexusArcanoEngine.kt`

## Progresso Lote 1 (implementado)
- N˙cleo funcional em `src/NexusArcanoEngine.kt`.
- Regra hard-first ativa: cadeia obrigatÛria antes de contador por escola.
- ExtraÁ„o de dependÍncias nomeadas por nome normalizado.
- ExtraÁ„o de regra `N m·gicas em X escolas` por regex normalizada.
- SaÌda com chaves ativas/faltantes e atÈ 3 prÛximas aÁıes.
- Filtro de aÁıes: sem magia j· aprendida e sem repetir escola quando houver nova.
- AtualizaÁ„o: extraÁ„o/validaÁ„o inicial de AM e IQ implementada; bloqueio numÈrico agora evita sugest„o falsa de escola.
