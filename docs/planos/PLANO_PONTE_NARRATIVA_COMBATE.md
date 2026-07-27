# Plano — PONTE narrativa ⇄ combate para magias (MAG-8)

## O buraco, em uma frase

O clássico *"antes de entrar na masmorra eu lanço Escudo e Força"* **gasta o PF mas não dá bônus
nenhum quando a luta começa**. A magia é rolada de verdade na narrativa, mas o efeito não atravessa a
porta do combate.

## Diagnóstico (o que eu verifiquei no código, não no chute)

Hoje existem **dois caminhos** de conjuração, e eles não se falam:

| | Dentro do combate | Fora de combate (Narrador) |
|---|---|---|
| Entrada | tela → `SagaCombatController.heroiConjurar` | tool `lancar_magia` → `FichaSagaDelegate.lancarMagia` |
| Rola NH/mana/custo/choque | ✅ | ✅ |
| Debita PF | ✅ | ✅ |
| **Lê o campo `mecanica`** | ✅ (dano, buff, condição, cura, divisor) | ❌ **não lê** |
| Efeito | aplicado pelo motor | **narrado** pela IA (recebe a descrição do livro) |

Duas travas confirmam o desenho atual (são intencionais, não bug):
- `lancar_magia` **recusa** se há combate aberto: *"a conjuração é feita pelo jogador na tela"*.
- O prompt do Narrador proíbe a IA de inventar número em combate.

E ao iniciar a luta, o herói é montado **só a partir da ficha** (`construirPerfilHeroi`) — não há
lugar de onde puxar "magias que eu já tinha ativas".

## O precedente que resolve isso (não vamos inventar padrão novo)

**O sangramento já atravessa essa fronteira.** Ele usa três campos `saga*` na ficha:

```
sagaSangrando, sagaSangramentoPenalidadeLocal, sagaSangramentoIntervaloSeg
```

O combate grava; a narrativa lê. E o `passarTempo(minutos)` **roda os testes do intervalo** e grava o
resultado de volta na ficha. Existe até relógio de campanha: `campanhas.tempoJogoMin`.

👉 **A ponte de magias deve copiar esse padrão**, não criar um mecanismo novo.

## Desenho proposto

### 1. Onde guardar (persistência)
Campo novo na ficha, no mesmo espírito dos `saga*`:

```
sagaMagiasAtivas: List<EfeitoMagicoAtivo>
```
com, por efeito: `magiaId`, `nome`, `energiaInvestida`, `alvo` (herói/aliado), o **`BuffAplicado`
calculado** (ou os dados para recalcular), e **`expiraEmMinutoDeJogo`** (carimbo do relógio, não
"segundos restantes" — ver item 3).

Persistir na ficha (como o sangramento) faz o efeito sobreviver a fechar o app. Exige migração de
banco (`FichaDatabase`), que o projeto já faz rotineiramente.

### 2. Fazer o `lancar_magia` APLICAR a mecânica
Hoje ele só rola e devolve texto. Passa a, no sucesso:
- **buff numérico** (`temBuffEstruturado`) → calcula com `MagicMechanics.calcularBuff` e guarda em
  `sagaMagiasAtivas`;
- **cura / cura que limpa** (`curaPvPorEnergia`, `removeCondicoes`) → aplica direto na ficha (PV,
  e limpar `sagaSangrando` no caso do Cessar Sangramento — encaixe natural com o precedente);
- **dano/condição em alvo** → **continua narrado** (fora de combate não existe "alvo" com ficha; o
  Narrador é quem sabe quem é o inimigo da cena). Honestidade: não inventar alvo.

⚠️ Regra de ouro mantida: **o que não tem número curado continua narrado**.

### 3. O relógio (a parte conceitualmente delicada)
Um buff de "1 minuto" conjurado na narrativa **precisa saber quanto tempo passou** até a luta.

- Guardar `expiraEmMinutoDeJogo = campanha.tempoJogoMin + duraçãoEmMinutos`.
- O `passarTempo(minutos)` **já é o lugar certo** para varrer e expirar o que venceu (ele já faz isso
  com sangramento).
- **Decisão de regra necessária:** magia de 1 minuto (60 turnos) é, na prática, *"conjurei e já entrei
  na luta"*. Proposta: se o Narrador avançar o relógio além da duração, o buff **cai antes da luta** —
  e o Narrador é avisado, para não prometer ao jogador um efeito que expirou.

### 4. Entrar no combate
No `SagaCombatController.iniciarCombate`, ao montar o herói:
- para cada efeito ainda válido em `sagaMagiasAtivas` → `heroi.buffs.add(buff)` **e**
  `registrarMagiaAtiva(...)` no `EfeitosMagicosDelegate`, para que **manutenção e expiração continuem
  correndo dentro da luta** (não vira buff eterno).
- o log do combate abre dizendo o que já estava ativo (*"Você entra na luta com Escudo (+2 BD)"*).

### 5. Sair do combate
Ao encerrar, o inverso: os efeitos **ainda ativos** voltam para `sagaMagiasAtivas` com o novo carimbo
de expiração — senão o buff sumiria ao fim da luta, que é o mesmo bug ao contrário.

## Onde o código vai morar (respeitando "o motor não cresce")

- **`EfeitosMagicosDelegate`** (já existe, MOTOR-4) — ganha `exportarAtivos()` / `importarAtivos()`.
  A lógica de ciclo de vida já mora lá.
- **`FichaSagaDelegate.lancarMagia`** — passa a chamar `MagicMechanics` (aplicar buff/cura).
- **`SagaCombatController.iniciarCombate` / encerramento** — a costura (é o lugar certo: controller).
- **`CombatSession`** — **nada**. A ponte não precisa tocar o motor.

## Lotes sugeridos

| Lote | Escopo | Risco |
|------|--------|-------|
| **PONTE-1** | Campo `sagaMagiasAtivas` na ficha + migração + `exportar/importarAtivos` no delegate (com teste) | baixo |
| **PONTE-2** | `lancar_magia` aplica **buff** e **cura/limpeza** fora de combate | médio |
| **PONTE-3** | Carregar no `iniciarCombate` e devolver ao encerrar (o coração da ponte) | médio |
| **PONTE-4** | Expiração pelo relógio no `passarTempo` + avisar o Narrador | médio |

## Deferidos honestos (não entram na ponte)

- **Dano/condição em alvo fora de combate** — sem ficha do inimigo, o motor não tem em quem aplicar;
  continua narrado (a IA já recebe a descrição fiel).
- **Buff em ALIADO** — o combate só modela o herói e os inimigos; aliado não é combatente hoje.
- **Magia mantida por horas** — depende do WorldTick (Fase C2), que não existe.

## Pré-requisito

**Validar o roteiro `docs/testes/ROTEIRO_TESTE_APARELHO_MAG1-7.md` primeiro.** A ponte assume que os
efeitos (buff/cura/condição) funcionam certo dentro do combate — se algo lá estiver torto, a ponte só
propagaria o erro para fora da luta.
