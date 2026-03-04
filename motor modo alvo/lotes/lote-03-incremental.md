# Lote 3 - Estado Incremental

Objetivo
- Evitar recomputação total após cada adição de magia.

Etapas
1. Persistir snapshot de estado por alvo.
2. Recalcular apenas chaves afetadas pela última magia adicionada.
3. Invalidar cache somente quando AM/IQ/magias mudarem.
4. Medir tempo por rodada e registrar métricas.

Critério de pronto
- Recomendação em tempo curto e estável por interação.
