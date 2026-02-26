# PROGRESS2 - Escala, Cenario de Uso e Implementacoes

Atualizado em: 2026-02-26
Status geral: `OFF (por enquanto)`
Objetivo: registrar quando cada pacote de implementacoes faz sentido, por escala de uso.

## Decisao Atual
- Projeto em uso caseiro (grupo pequeno).
- As implementacoes de hardening e escala ficam pausadas por enquanto.
- So aplicar o minimo necessario quando for preparar release externa.

## Cenarios de Uso e Prioridades

### Cenario 1 - Uso caseiro
Perfil:
- 5 a 20 pessoas.
- Trafego baixo.
- Uso regional/local.

Prioridade:
- UX fluida.
- Estabilidade funcional.
- Backup/persistencia confiavel.
- Correcao rapida de bugs.

Pode ficar para depois:
- Refatoracao arquitetural grande.
- Observabilidade avancada.
- Hardening completo de seguranca.

Minimo recomendado:
- HTTPS em producao.
- HTTP apenas em debug/local.
- Chaves fora do repositorio.

Status para este projeto: `ATIVO AGORA`

### Cenario 2 - Comunidade pequena
Perfil:
- 20 a 200 pessoas.
- Trafego baixo a medio.
- Uso regional/nacional.

Prioridade:
- Separacao clara debug/release.
- Tratamento de erro melhor no app e backend.
- Testes de fluxo critico.
- Primeiro ciclo de refatoracao (arquivos muito grandes).

Necessario iniciar:
- Planejamento de migrations de banco.
- Melhor estrutura de rede/log.

Status para este projeto: `OFF`

### Cenario 3 - Produto em crescimento
Perfil:
- 200 a 5.000 pessoas.
- Trafego medio a alto.
- Uso nacional.

Prioridade:
- Arquitetura por camadas.
- Observabilidade de producao.
- Cliente de rede robusto (timeout/retry/erros padronizados).
- Cobertura de testes ampliada.

Necessario:
- Processo de release mais rigoroso.
- Higiene de repositorio e CI mais forte.

Status para este projeto: `OFF`

### Cenario 4 - Escala ampla (multi-regiao/mundial)
Perfil:
- 5.000+ pessoas.
- Trafego alto.
- Uso multi-regiao/global.

Prioridade:
- Seguranca forte (sem segredo sensivel no cliente).
- Operacao escalavel de backend.
- Monitoramento e resposta a incidente.
- Estrategia de disponibilidade por regiao.

Necessario:
- Maturidade de SRE/DevOps.
- Versionamento de API e governanca tecnica.

Status para este projeto: `OFF`

## Mapeamento dos Lotes (PROGRESS.md) por Cenario
- Lote 8 (seguranca de rede): obrigatorio a partir do Cenario 2; minimo parcial no Cenario 1 para release.
- Lote 9 (refatoracao arquitetura): recomendado a partir do Cenario 2; forte no Cenario 3.
- Lote 10 (performance local): recomendado do Cenario 2 em diante.
- Lote 11 (rede/log/observabilidade): recomendado no Cenario 2; essencial no Cenario 3.
- Lote 12 (banco/testes/higiene): recomendado no Cenario 2; essencial no Cenario 3.

## Regra de Ativacao (quando tirar do OFF)
Ativar este plano quando ocorrer ao menos 1 dos gatilhos:
1. Base de usuarios passar de ~20 pessoas.
2. Uso em mais de uma mesa/grupo de forma continua.
3. Publicacao para publico externo (alem de grupo privado).
4. Incidentes frequentes de rede, dados ou regressao.

## Nota Pratica
- Enquanto estiver em `OFF`, manter foco em entregas de jogabilidade e usabilidade.
- Evitar refatoracao grande sem necessidade real de escala.
