# PLANO — Mecânica estruturada das magias (`mecanica`)

Transforma a **prosa** das 879 magias em **regra executável**, escola por escola, SEM tocar na
`descricao` (que é fiel ao livro). Cada magia ganha um objeto `mecanica` legível pela máquina.

## O schema `mecanica` (vale pras 879)
Campo em `MagiaDefinicao` (catálogo) → lido no combate via `DataRepository.getMagiaPorId(id).mecanica`
(cobre até magias já aprendidas). Modelo: `domain/magic/MagicMechanics.kt`.

`efeito` (conjunto FECHADO): `dano` | `condicao` | `buff` | `ambiente` | `controle` | `informacao` | `narrado`.

- **dano**: `danoPorEnergia` ("1d-1"), `energiaPorDado` (1 ou 2), **`danoFixo`** (MEC-1: não escala com
  a energia — Géiser é 3d SEMPRE; sem isto sairia 15d), `tipoDano` (quei/cont/projecao),
  `armadura` (null/"ignora"/"metal_rd_1"), `entrega` (projetil/toque/feixe/area), + condição embutida
  (`condicao`, `condicaoResistencia` "HT-3"/"HT_por_pv", `condicaoRaioM`).
  ⚠️ Dano em PONTOS ("1 ponto/seg") vira `"0d+N"` no `expandirDano` — `rolarDano` exige `<n>d` e
  devolveria **0** para um `"1"` pelado.
- **buff** (MEC-2 — os números): o modelo do livro é "N por NÍVEL, X de energia por nível, teto M".
  `niveis = buffEnergiaPorNivel > 0 ? min(energia / buffEnergiaPorNivel, buffMaxNiveis) : 1` (piso 1);
  cada campo abaixo é o valor POR NÍVEL, o motor multiplica:
  - `buffRd` (Pele de Crocodilo 4), `buffEsquiva` (Apressar 1), `buffAtributo` ("ST"/"DX"/"HT") +
    `buffAtributoValor` (NEGATIVO em Debilitar/Fragilidade/Inabilidade), `buffDeslocamento` (delta),
    `buffDeslocamentoFixo` (ABSOLUTO — Voo 10, Voo do Falcão 40), `buffPenalidadeAtacantes` (Nublar),
    `buffDanoArma` + **`buffArmaTipo`** ("cac"/"distancia" — sem isto o +2 do gume vaza pro arco).
  - O motor SÓ sabe aplicar esses. Bônus de perícia/reação/alcance/peso NÃO têm campo → `semNumero`.
- **ambiente/controle/informacao**: `notas` (o motor tagueia, o Mestre descreve). Desde o MEC-2 a
  `notas` CHEGA ao Narrador via `resumoEfeito` — antes era gravada e nunca lida.
- **narrado**: bespoke.

## Regra de ouro
O motor aplica o que é estruturável (dano exato, condição, buff numérico); o resto tem a `mecanica`
como TAG + `notas`, e o efeito fica narrado. `descricao` intocada.
**Meia-regra errada é PIOR que narrar** — na dúvida, `narrado`/`semNumero` com nota. Foi assim que os
74 buffs sem número ficaram honestos (metacaracterísticas, vantagens, imunidades, utilidade).

## Como o buff vive no motor (MEC-2)
`BuffAplicado` (deltas concretos) guardado na `MagiaAtivaNoCombate` **e** na lista `Combatente.buffs`.
O perfil efetivo é COMPUTADO da lista (não se muta e "desmuta" nada) → expirar/dissipar é um `remove`,
imune a drift e a reversão dupla. `heroiPerfil` é propriedade computada (ficha + buffs), então todo o
motor enxerga o buff sem tocar nos ~30 pontos de uso. NPC: `stEfetivo`/`htEfetivo`/`dxEfetivo`.
⚠️ `registrarSeMagiaAtiva` precisa checar `res.alvoResistiu` — `sucesso` só diz que a CONJURAÇÃO deu
certo; sem o check, um Debilitar resistido ainda aplicaria −3 ST.

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
