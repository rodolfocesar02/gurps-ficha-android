# PLANO — Mecânica estruturada das magias (`mecanica`)

Transforma a **prosa** das 879 magias em **regra executável**, escola por escola, SEM tocar na
`descricao` (que é fiel ao livro). Cada magia ganha um objeto `mecanica` legível pela máquina.

## O schema `mecanica` (vale pras 879)
Campo em `MagiaDefinicao` (catálogo) → lido no combate via `DataRepository.getMagiaPorId(id).mecanica`
(cobre até magias já aprendidas). Modelo: `domain/magic/MagicMechanics.kt`.

`efeito` (conjunto FECHADO): `dano` | `condicao` | `buff` | `ambiente` | `controle` | `informacao` | `narrado`.

- **dano**: `danoPorEnergia` ("1d-1"), `energiaPorDado` (1 ou 2), `tipoDano` (quei/cont/projecao),
  `armadura` (null/"ignora"/"metal_rd_1"), `entrega` (projetil/toque/feixe/area), + condição embutida
  (`condicao`, `condicaoResistencia` "HT-3"/"HT_por_pv", `condicaoRaioM`).
- **buff**: `buffRotulo` + `buffDanoArma` (bônus numérico simples) — rastreado como magia ativa.
- **ambiente/controle/informacao**: `notas` (o motor tagueia, o Mestre descreve).
- **narrado**: bespoke.

## Regra de ouro
O motor aplica o que é estruturável (dano exato, condição, buff numérico); o resto tem a `mecanica`
como TAG + `notas`, e o efeito fica narrado. `descricao` intocada.

## Progresso por escola
- **Ar (49 magias)** — em curso:
  - ✅ **AR-1**: schema + modelo + parsing do catálogo + fiação no combate + handler de `dano` (dado
    exato escalado por energia, tipo, "ignora armadura", condição embutida atordoar). Curadas as 6 de
    DANO: Relâmpago, Toque Chocante, Concussão, Olhar de Relâmpago, Relâmpago Explosivo, Chicote de
    Relâmpago. +7 testes.
  - ⬜ **AR-2**: buffs (Corpo de Ar, Caminhar no Ar, Arma de Relâmpago +2, Respirar Ar, Imunidade a
    Relâmpagos, Projéteis de Relâmpago) + ambiente (Criar Ar, Nuvens, Muralha de Vento/Relâmpagos,
    Furacão, Tempestade, Chuva, Neve, Nuvem/Tempestade de Faíscas) + controle (Turbilhão) +
    informação (Localizar Ar, Previsão do Tempo, Aerovisão) + as demais (~43). Handlers de `buff`
    (rastrear ativo + bônus) e `condicao` standalone.
- Demais 23 escolas: depois de Ar validada no aparelho.
