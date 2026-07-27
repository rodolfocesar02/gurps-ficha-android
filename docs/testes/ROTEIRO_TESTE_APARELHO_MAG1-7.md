# Roteiro de teste no aparelho — Lotes MAG-1..7 (32 magias mecanizadas)

> Tudo abaixo roda **DENTRO do combate** (a tela de combate, onde o motor manda nos números).
> Fora de combate a magia é rolada mas o efeito é narrado — ver `PLANO_PONTE_NARRATIVA_COMBATE.md`.
>
> **Boa notícia:** a mecânica vem do CATÁLOGO, não da cópia na ficha. Fichas antigas já pegam as
> magias novas — não precisa recriar personagem.

## Preparação (uma vez só)

1. Ficha de teste: um **mago** com **Aptidão Mágica 2+** e NH alto nas magias (16+ evita ficar
   testando falha de conjuração em vez do efeito).
2. Dê ao mago as magias da lista abaixo (as que quiser testar).
3. Leve **uma arma corpo a corpo e uma arma de projétil** (arco/besta) — o MAG-2 precisa das duas.
4. Mana da cena: **normal** (mana baixa/nenhuma bloqueia a conjuração — é regra, não bug).
5. Inimigo de teste: algo com **armadura (RD 4+)** para o MAG-7, e um **conjurador** para o MAG-6.

**Onde olhar o resultado:** o **log do combate** (o feed) diz o número aplicado, e o **status do
combatente** mostra a condição. Confira também a ficha (PF gasto).

---

## MAG-1 — Buffs e debuffs de atributo (11 magias)

| # | Conjure | Energia | O que TEM que acontecer | Seria bug se… |
|---|---------|--------:|--------------------------|----------------|
| 1.1 | **Força** em si mesmo | 4 | +2 ST → seu **dano com arma sobe** no próximo golpe | o dano não mudar |
| 1.2 | **Graça** em si mesmo | 8 | +2 DX → **acerta mais** e **defende melhor** | NH/defesa iguais |
| 1.3 | **Vigor** em si mesmo | 4 | +2 HT (aguenta mais choque/atordoamento) | — |
| 1.4 | **Debilitar** num inimigo | 3 | −3 ST → **o dano DELE cai** nos golpes seguintes | ele bater igual |
| 1.5 | **Inabilidade** num inimigo | 4 | −4 DX → ele **erra mais e defende pior** | — |
| 1.6 | **Fragilidade** num inimigo | 4 | −2 HT | — |
| 1.7 | **Estorvar** num inimigo | 3 | −3 Deslocamento **e** −3 Esquiva | só um dos dois mudar |
| 1.8 | **Reflexos** em si | 5 | **+1 em TODAS as defesas** (esquiva, aparar, bloquear) | +1 só na esquiva |
| 1.9 | **Aumentar Força** em si | 3 | +3 ST **para UM golpe só** — depois **some sozinho** | ficar permanente |

⚠️ **Teste-chave (1.4/1.5/1.6/1.7):** essas são **resistidas com HT**. Se o inimigo tem HT alto, ele
vai resistir às vezes — o log diz *"RESISTE"*. Isso é **regra funcionando**, não bug. Repita 2-3 vezes.

⚠️ **Teste do teto (opcional):** conjure **Debilitar com 20 de energia** — tem que travar em **−5 ST**
(teto do livro), não −20.

---

## MAG-2 — Armas encantadas (6 magias)

| # | Conjure | O que TEM que acontecer | Seria bug se… |
|---|---------|--------------------------|----------------|
| 2.1 | **Arma Flamejante** e golpeie **corpo a corpo** | **+2 de dano** depois de furar a armadura | dano igual |
| 2.2 | Com Arma Flamejante ativa, **atire de arco** | **NÃO ganha o +2** (é só corpo a corpo) | o arco ganhar +2 |
| 2.3 | **Projéteis Flamejantes** e **atire de arco** | **+2 de dano** no tiro | tiro sem bônus |
| 2.4 | Com Projéteis ativos, golpeie **corpo a corpo** | **NÃO ganha o +2** | o golpe ganhar +2 |
| 2.5 | **Arma Congelante** / **Arma de Relâmpago** | mesmo comportamento (+2 CaC) | — |

⚠️ **O 2.2 e o 2.4 são o teste que importa** — provam que o bônus não vaza entre arma de mão e arco.

---

## MAG-3 — Controle que paralisa (5 magias)

| # | Conjure num inimigo | O que TEM que acontecer | Seria bug se… |
|---|---------------------|--------------------------|----------------|
| 3.1 | **Agonizar** (R-HT) | Se não resistir: fica **PARALISADO por 1 min** — no turno dele **só "não fazer nada"** | ele continuar atacando |
| 3.2 | **Cócegas** (R-Vontade) | Idem, 1 min | — |
| 3.3 | **Carne para Pedra** (R-HT) | **PARALISADO indefinido** (não expira sozinho) | sair sozinho em 1 min |
| 3.4 | **Soterramento** / **Enclausuramento Arbóreo** | Idem | — |

⚠️ Confira no **status do inimigo** que aparece a condição, e que no turno dele o motor **não deixa**
ele atacar.

---

## MAG-4 — Cura que limpa condição (3 magias)

| # | Situação | Conjure | O que TEM que acontecer |
|---|----------|---------|--------------------------|
| 4.1 | Você **sangrando** (leve corte/perfuração de um inimigo) | **Cessar Sangramento** | o **SANGRANDO some** e você **recupera 1 PV** |
| 4.2 | Você (ou aliado) **paralisado** | **Cessar Paralisia** | o **PARALISADO some** |
| 4.3 | Você **cego** (leve um Jato de Areia / Lampejo) | **Restaurar Visão** | o **CEGO some** |

⚠️ Sem alvo escolhido, essas curam **você mesmo** (automagia) — é o comportamento certo.
⚠️ No 4.1, confira que o sangramento **parou de tirar PV** nos turnos seguintes.

---

## MAG-5 — Náusea e Banimento (3 magias)

| # | Conjure | O que TEM que acontecer | Seria bug se… |
|---|---------|--------------------------|----------------|
| 5.1 | **Nausear** num inimigo (R-HT) | −2 DX nele (erra mais, defende pior) | — |
| 5.2 | **Viagem Planar para Outro** (Banir) no **único** inimigo | Ele **some do combate** e a luta **encerra em VITÓRIA** | o combate continuar com ele lá |
| 5.3 | **Banir** com **2+ inimigos** | O banido sai; **a luta segue** com os outros | encerrar cedo demais |
| 5.4 | **Transportar Outro no Tempo** | Igual ao Banir | — |

⚠️ **Este é o teste mais importante do MAG-5** — mexi no coração do motor (quem conta como "vivo").
Confira que o banido **não age mais**, **não é alvo** e **não aparece na iniciativa**.

---

## MAG-6 — Silêncio (anti-conjurador)

| # | Passo | O que TEM que acontecer |
|---|-------|--------------------------|
| 6.1 | Coloque um **inimigo conjurador** na cena | ele conjura normalmente contra você |
| 6.2 | Conjure **Silêncio** (área) pegando ele | ele fica **SILENCIADO** |
| 6.3 | Deixe o turno dele chegar | ele **NÃO consegue conjurar** — o log diz *"está silenciado e não consegue conjurar"* |

---

## MAG-7 — Mágica Penetrante (fura armadura)

| # | Passo | O que TEM que acontecer |
|---|-------|--------------------------|
| 7.1 | Contra um inimigo **de armadura pesada (RD 6+)**, conjure uma magia de dano **normal** | anote o dano (a RD come boa parte) |
| 7.2 | Conjure **Mágica Penetrante** com **3 de energia** | o log avisa que a **próxima** magia fura armadura (÷5); **não causa dano** |
| 7.3 | Agora conjure a **mesma magia de dano** | o log mostra **"RD 6→1"** e o **dano é bem maior** |
| 7.4 | Conjure a magia de dano **de novo** (sem repetir a Penetrante) | volta ao **normal** (o efeito valia para UMA magia) |

⚠️ O 7.4 é o teste que importa: o divisor **não pode ficar grudado**.

---

## Regressão — o que eu mexi no motor (confira que NÃO quebrou)

Estes lotes mudaram peças internas e **nunca foram testados no aparelho**. Não precisa de magia nova:
basta jogar um combate completo e conferir que continua igual ao que você já conhece.

| # | O que testar | Por que |
|---|--------------|---------|
| R.1 | **Um combate normal do começo ao fim** (ataque, defesa, dano, morte do inimigo, vitória) | Os lotes MOTOR-1..5 moveram zonas, dano mágico, ataque à distância e efeitos ativos para arquivos novos |
| R.2 | **Magias antigas que já funcionavam**: Bola de Fogo, Relâmpago, Jato de Chamas, Sono, Cegar, Escudo, Armadura | O `imporCondicaoMagica` foi reescrito (MAG-5) — Sono/Cegar/Medo/Silêncio têm que continuar iguais |
| R.3 | **Zona persistente**: Chuva de Fogo / Nuvem de Faíscas | Zonas saíram para o `ZonaDelegate` (MOTOR-1) |
| R.4 | **Magia que fere a cada turno**: Morte Candente | Foi para o `EfeitosMagicosDelegate` (MOTOR-4) |
| R.5 | **Buff com duração**: Escudo — conjure e veja **expirar** no fim, revertendo o bônus | Manutenção/expiração saíram para o delegate |
| R.6 | **Projétil e feixe**: Bola de Fogo (projétil), Jato de Chamas (feixe) | Saíram para o `AtaqueMagicoResolver` (MOTOR-3) |

---

## Como me reportar

Para cada item, basta: **"3.1 ok"** / **"3.1 quebrado: o inimigo continuou atacando"**.
Se der erro, mande **a linha do log do combate** — é onde o motor diz o que aplicou.
