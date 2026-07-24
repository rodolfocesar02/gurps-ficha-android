# Plano — Mecanizar ao máximo as magias no combate

> Levantamento feito lendo a descrição de **todas as 879 magias** do `magias2versao.json`, escola por
> escola, com 9 agentes em paralelo (jul/2026). Cada magia foi classificada em **MECANIZAR** (tem efeito
> de combate real que o motor consegue executar, mas hoje está sub-mecanizada), **JÁ OK** (efeito de
> combate já numerado), ou **NÃO VALE** (interpretativa, fora de combate, criação de item, invocação,
> teleporte, detecção/sentido, controle mental fino — não force mecânica em magia narrativa).

## Placar geral

| | Qtd | % |
|---|---:|---:|
| **MECANIZAR** (candidatas novas) | **102** | 12% |
| JÁ OK (já numeradas) | 75 | 9% |
| NÃO VALE (narrativas/fora de combate) | 702 | 80% |
| **Total** | **879** | |

Por escola (ordenado por potencial de mecanização):

| Escola | Mecanizar | Já ok | Não vale | Total |
|--------|----------:|------:|---------:|------:|
| Corpo | 25 | 4 | 34 | 63 |
| Animais | 12 | 0 | 35 | 47 |
| Cura | 9 | 4 | 24 | 37 |
| Quebrar e Consertar | 8 | 0 | 25 | 33 |
| Água | 6 | 14 | 34 | 54 |
| Fogo | 5 | 10 | 17 | 32 |
| Clima | 5 | 9 | 14 | 28 |
| Deslocamento | 5 | 4 | 30 | 39 |
| Luz e Trevas | 5 | 3 | 23 | 31 |
| Plantas | 5 | 0 | 26 | 31 |
| Metamágica | 4 | 0 | 36 | 40 |
| Som | 3 | 3 | 17 | 23 |
| Tecnológica | 3 | 1 | 42 | 46 |
| Terra | 2 | 4 | 23 | 29 |
| Mente | 2 | 10 | 44 | 56 |
| Portal | 2 | 0 | 27 | 29 |
| Ar | 1 | 2 | 22 | 25 |
| Proteção | 0 | 4 | 24 | 28 |
| Necromancia | 0 | 3 | 37 | 40 |
| Ilusão | 0 | 0 | 20 | 20 |
| Reconhecimento | 0 | 0 | 41 | 41 |
| Comunicação | 0 | 0 | 32 | 32 |
| Alimentos | 0 | 0 | 18 | 18 |
| Encantamento | 0 | 0 | 57 | 57 |

## Leitura honesta do resultado

- **O motor já cobre bem o "dano duro".** As escolas elementais (Fogo, Água, Clima, Terra) têm a maioria
  do dano/feixe/projétil/zona **já numerada** — o que falta ali é pouco.
- **O ouro está em CORPO (25) e ANIMAIS (12).** Corpo é quase todo **buff/debuff de atributo** (Força,
  Graça, Vigor e seus opostos) que o motor **já sabe executar** (`buffAtributo`) — só falta ligar o número.
  Animais é **controle de alvo animal** (dominar/paralisar animal), que precisa de resistência por Vontade/IQ.
- **80% é legitimamente narrativo.** Reconhecimento (41), Comunicação (32), Encantamento (57), Alimentos
  (18) e boa parte de Necromancia/Portal são detecção, informação, criação de item, invocação, teleporte,
  viagem no tempo, controle mental fino — coisas que o motor **não modela e não deve fingir que modela**.

## O que o motor precisa GANHAR (ordenado por quanto desbloqueia)

O gargalo **não é** capacidade de dano — é que várias magias de controle/debuff esbarram em 4 lacunas do
vocabulário. Priorizadas por número de magias que destravam:

1. **`condicaoResistencia` por Vontade / DX / ST** (hoje só HT/HT-3). — *Desbloqueia ~30 magias.*
   Quase toda condição de controle resiste por Vontade (Controle de Animal, Dominar), DX (Trança-Pés) ou
   ST (Pés Plantados, Soterramento, escape do Toque Congelante). É um campo pequeno, alto retorno.
2. **`buffAtributo` já existe — só ligar os números.** — *~15 magias de Corpo, prioridade ALTA.*
   Força/Debilitar (ST), Graça/Inabilidade (DX), Vigor/Fragilidade (HT), Dor, Coceira, Estorvar.
3. **Condições NOVAS no enum.** — *~10 magias.* O motor tem `CAIDO`, `IMOBILIZADO`, `PARALISADO`,
   `CEGO`, `DORMINDO`, `AMEDRONTADO`, `SILENCIADO`, `ATORDOADO`. **Faltam:**
   `SURDO` (Ensurdecer, Estrondo), `NAUSEADO` (Nausear, Enjoo), `DESARMADO` (Espasmo — larga a arma),
   `DOMINADO` (Controle de Animal), `RETARDADO` (Retardar — perde metade das ações), `REMOVIDO`
   (Banir/Transportar no Tempo — sai do combate). *(Trança-Pés/Ensebar = `CAIDO`, já existe.)*
4. **`removeCondicao` — cura que LIMPA estado.** — *~6 magias de Cura, prioridade ALTA.*
   Cessar Sangramento, Despertar (anti-Sono/Atordoar), Neutralizar Veneno, Cessar Paralisia, Restaurar
   Visão. O motor conhece as condições; falta o efeito "remover".
5. **Zona de CONDIÇÃO** (estender o `ZonaDelegate`, hoje só zona de dano). — *~6 magias.*
   Cola/Ensebar/Entrelaçamento (imobilizar/derrubar quem pisa), Silêncio (silenciar quem entra),
   Muralha de Relâmpagos (dano ao tocar).
6. **`buffImunidade` a CONDIÇÃO** (hoje só a elemento de dano). — *~5 magias.*
   Imunidade ao Som (anula surdo/atordoar sônico), Visão Brilhante (anula cegueira por luz), Imunidade
   à Dor (anula atordoar/choque).
7. **`divisorArmadura`** (penetração) + **`buffResistenciaMagia`** (+RM). — *Metamágica, ~4 magias.*
8. **Camada de iluminação/visibilidade** — *o MAIOR lift, deixar por último.* Escuridão, Trevas,
   Invisibilidade plena, Jato de Luz. Invisibilidade sozinha já mapeia em `buffPenalidadeAtacantes`
   (existe) — o resto exige modelar luz/visão por área.

## Plano de lotes proposto (do mais barato/alto-retorno ao mais caro)

- **MAG-0 — Correções (barato):** ligar o que já é executável mas ficou rotulado errado — `proteger_animal`
  (buffBd), `imunidade_a_acido`/`imunidade_a_veneno` (buffImunidade), marcar `buffUmUnicoUso` nos "Aumentar
  Força/Destreza/Vitalidade", e alinhar `carne_para_pedra`/`soterramento` à já-existente Carne para Gelo.
- **MAG-1 ✅ CONCLUÍDO (24/jul)** — Buffs/debuffs de atributo de CORPO: Força, Graça, Vigor, Debilitar,
  Inabilidade, Fragilidade, Aumentar Força/Destreza/Vitalidade (um-uso), Estorvar, Reflexos (11 magias).
  Campos já existiam (`buffAtributo`/`buffBd`/`buffEsquiva`/`buffDeslocamento`), zero motor novo. Gate 969/0.
  *(Dor, Coceira, Magreza — duração especial "1 turno"/"até coçar" — ficaram para tratamento próprio.)*
- **MAG-2 ✅ CONCLUÍDO (24/jul)** — Buffs de dano de arma elementais: Arma Flamejante/Congelante/de
  Relâmpago + as 3 versões de projétil (`buffDanoArma=2` + `buffArmaTipo` cac/distancia). 6 magias, zero
  motor novo. Gate 971/0.
- **MAG-3 — `condicaoResistencia` Vontade/DX/ST:** o campo novo do motor. Destrava Animais (dominar/
  controlar), Trança-Pés, Pés Plantados, Soterramento, Carne para Pedra, e várias de Mente já-ok ganham
  base sólida. ~30 magias passam a resistir certo.
- **MAG-4 — Condições novas:** SURDO, NAUSEADO, DESARMADO, DOMINADO, RETARDADO, REMOVIDO + as magias que
  as usam (Ensurdecer/Estrondo, Nausear/Enjoo, Espasmo, Controle de Animal, Retardar, Banir).
- **MAG-5 — Cura que limpa (`removeCondicao`):** Cessar Sangramento, Despertar, Neutralizar Veneno, Cessar
  Paralisia, Restaurar Visão.
- **MAG-6 — Zonas de condição:** estender `ZonaDelegate` — Cola, Ensebar, Entrelaçamento, Silêncio, Teia de
  Aranha, Muralha de Relâmpagos.
- **MAG-7 — Penetração e anti-magia:** `divisorArmadura` (Mágica Penetrante), `buffResistenciaMagia`
  (Resistência à Magia), `buffImunidade` a condição (Imunidade ao Som/à Dor, Visão Brilhante).
- **MAG-8 (grande, opcional) — Iluminação/visibilidade:** Invisibilidade (barato, `buffPenalidadeAtacantes`),
  depois Escuridão/Trevas/Jato de Luz (caro: exige camada de luz por área).

## O que foi descartado — e por quê (categorias honestas)

- **Criação de item / Encantamento (57):** a escola inteira é forja de item mágico, fora de combate (você
  excluiu de propósito).
- **Detecção / sentido / informação (~120):** Reconhecimento (41) e Comunicação (32) são 100% disto, mais
  os "Localizar/Detectar/Ver" espalhados. O motor não modela testes de Sentido/Percepção.
- **Invocação / criação de criatura (~30):** Criar Guerreiro/Animal/Servo, Zumbi, Convocar/Criar Elemental,
  Espírito de Caveira. Exigiria um subsistema de aliados invocados que não existe.
- **Teleporte / planar / tempo (~25):** toda a escola de Portal. Fora do que um combate tático modela.
- **Controle mental fino (~20):** Sugestão, Comando, Escravizar, Possessão, Condicionamento — "forçar uma
  ação específica" não cabe no vocabulário de condição.
- **Metacaracterísticas / metamorfose (~20):** Corpo de Fogo/Água/Pedra/Metal, Metamorfose Animal — trocam
  o stat-block inteiro; não é buff de um campo.
- **Ambiente / clima / utilidade (~80):** criar/moldar/purificar água/ar/terra/fogo, clima de longo prazo,
  comida, conserto.
- **Save-or-die (~3):** Decapitação, Evisceração, Murchar Planta — matar por um teste não tem vocabulário
  (e seria perigoso mecanizar sem cuidado).
- **Dano a OBJETO (~6):** Desintegrar, Fender, Explodir, Enfraquecer, Fragmentar — só valem quando/se o
  motor passar a mirar objetos destrutíveis (`alvoValido="objeto"` já existe; o alvo é que não).

---

# Apêndice — listagem completa por escola

Legenda: **MEC** = mecanizar · **OK** = já numerada · **—** = não vale. Campos propostos usam o vocabulário
de `MagiaMecanica`.

## Corpo (25 MEC / 4 OK / 34 —)

**Mecanizar / Já ok:**

| id | nome | veredito | efeito | prio | mecânica proposta | por quê |
|---|---|---|---|---|---|---|
| forca | Força | MEC | buff ST | ALTA | buffAtributo=ST, buffAtributoValor=1, buffEnergiaPorNivel=2, buffMaxNiveis=5 | +ST afeta dano/Carga/PV; 1 min. |
| graca | Graça | MEC | buff DX | ALTA | buffAtributo=DX, +1, buffEnergiaPorNivel=4, buffMaxNiveis=5 | +DX melhora ataque, defesa e mira. Núcleo de combate. |
| debilitar | Debilitar | MEC | debuff ST | ALTA | buffAtributo=ST, -1, buffEnergiaPorNivel=1, buffMaxNiveis=5 | -ST reduz dano do inimigo; R-HT, 1 min. |
| inabilidade | Inabilidade | MEC | debuff DX | ALTA | buffAtributo=DX, -1, buffEnergiaPorNivel=1, buffMaxNiveis=5 | -DX limpo; base de Debilitar/Estorvar. |
| vigor | Vigor | MEC | buff HT | MEDIA | buffAtributo=HT, +1, buffEnergiaPorNivel=2, buffMaxNiveis=5 | +HT: Vel Básica, PF, resistências. |
| fragilidade | Fragilidade | MEC | debuff HT | MEDIA | buffAtributo=HT, -1, buffEnergiaPorNivel=2, buffMaxNiveis=5 | -HT; R-HT. |
| aumentar_forca | Aumentar Força | MEC | buff ST 1 uso | MEDIA | buffAtributo=ST, +1, buffEnergiaPorNivel=1, buffMaxNiveis=5, buffUmUnicoUso=true | +ST p/ um golpe; uso único auto-encerra. |
| aumentar_destreza | Aumentar Destreza | MEC | buff DX 1 uso | MEDIA | buffAtributo=DX, +1/energia, máx5, buffUmUnicoUso=true | +DX p/ um teste. |
| aumentar_vitalidade | Aumentar Vitalidade | MEC | buff HT 1 uso | BAIXA | buffAtributo=HT, +1/energia, máx5, buffUmUnicoUso=true | +HT p/ um teste. |
| dor | Dor | MEC | debuff DX 1 turno | MEDIA | buffAtributo=DX, -3, buffMaxNiveis=1, dur=1 turno, R-HT | -3 DX no próximo turno. |
| coceira | Coceira | MEC | debuff DX | BAIXA | buffAtributo=DX, -2 (até coçar) | -2 DX curto; fim narrado. |
| estorvar | Estorvar | MEC | debuff Desloc/Esquiva | MEDIA | buffDeslocamento=-1, buffEsquiva=-1 por energia (1-4), R-HT | reduz mobilidade e defesa. |
| magreza | Magreza | MEC | debuff DX | BAIXA | buffAtributo=DX, -2 (+1d PF) | roupa folgada; situacional. |
| reflexos | Reflexos | MEC | +defesas | BAIXA | buffEsquiva=1, buffBd=1 | +1 em todas as defesas ativas. |
| espasmo | Espasmo | MEC | desarme | MEDIA | condicao=DESARMADO (nova), condicaoResistencia=HT | contrai a mão e faz largar a arma. |
| trancapes | Trança-Pés | MEC | derrubar | MEDIA | condicao=CAIDO, condicaoResistencia=DX | alvo cai; resiste DX (não HT). |
| pes_plantados | Pés Plantados | MEC | imobilizar | MEDIA | condicao=IMOBILIZADO, buffEsquiva=metade, -2 melee, condicaoResistencia=ST | pés presos; re-teste -5/turno. |
| paralisar_membro | Paralisar Membro | MEC | paralisia parcial | MEDIA | condicao=PARALISADO (membro), R-HT, dur=60s | braço paralisado impede atacar/segurar. |
| agonizar | Agonizar | MEC | incapacitar | MEDIA | condicao=PARALISADO, R-HT, dur=60s | indefeso por 1 min. |
| vomitar | Vomitar | MEC | incapacitar | MEDIA | condicao=PARALISADO/incapacitado, R-HT, dur=(25-HT)s | vomita e fica incapacitado. |
| nausear | Nausear | MEC | náusea | BAIXA | condicao=NAUSEADO (nova), R-HT, dur=10s | -2 testes/-1 defesas. |
| cocegas | Cócegas | MEC | incapacitar (riso) | BAIXA | condicao=PARALISADO, dur=60s (R-Vontade → narrar) | indefeso rindo. |
| sede | Sede | MEC | dano fixo | BAIXA | danoFixo=1 PV (+4 PF) | desidratação. |
| imunidade_a_dor | Imunidade à Dor | MEC | imune atordoar/choque | BAIXA | buff imune-atordoado + ignora-choque (novos) | não atordoa, sem penal. de choque. |
| transmogrificacao | Transmogrificação | MEC | tira de combate | BAIXA | condicao=incapacitado, dur longa (R-Vontade → narrar) | vira objeto inerte. |
| atordoamento | Atordoamento | OK | atordoado | — | — | condicao=ATORDOADO, R-HT. |
| cegar | Cegar | OK | cego | — | — | condicao=CEGO 10s. |
| emudecer | Emudecer | OK | silenciado | — | — | condicao=SILENCIADO 10s. |
| paralisia_total | Paralisia Total | OK | paralisado | — | — | condicao=PARALISADO 1 min, toque -5. |

**Não vale (34):** alongar_membro, alterar_corpo, alterar_feicoes, alterar_voz, ambidestria, andar_em_circulos, atrapalhar (fumble não modelado), atrofiar_membro, aumentar_inteligencia (IQ), aumentar / aumentar_outro (MT multiplicativo), cadencia, cessar_espasmo, controle_de_membro (fantoche, R-Vont), corpulencia, corte_de_cabelo, crescimento_capilar, decapitacao (save-or-die), encolher / encolher_outro (MT), enfraquecer_sangue, ensurdecer (surdez não existia — ver MAG-4), equilibrio, escalada, fome, perfume, prender_a_respiracao, provocar_dormencia, provocar_esterilidade, sensibilizar, toque, toque_mortal (fica em Necromancia), transformar_corpo, transformar_outro, aumentar_inteligencia.

## Cura (9 MEC / 4 OK / 24 —)

**Mecanizar / Já ok:**

| id | nome | veredito | efeito | prio | mecânica proposta | por quê |
|---|---|---|---|---|---|---|
| cessar_sangramento | Cessar Sangramento | MEC | limpa sangramento +1PV | MEDIA | removeCondicao=SANGRANDO + curaFixa=1 | muito usada em combate. |
| despertar | Despertar | MEC | acorda/desatordoa (área) | MEDIA | removeCondicao=[ATORDOADO,DORMINDO], condicaoRaioM, teste HT | anti-Sono/Atordoamento. |
| neutralizacao_instantanea_de_veneno | Neutralização Instantânea de Veneno | MEC | remove veneno | MEDIA | removeCondicao=veneno / interrompe danoPorTurno(veneno) | corta dano recorrente na hora. |
| neutralizar_veneno | Neutralizar Veneno | MEC | remove veneno (30s) | MEDIA | idem, +teste Venefício | interrompe dano por veneno. |
| cessar_paralisia | Cessar Paralisia | MEC | remove paralisia | MEDIA | removeCondicao=PARALISADO | anti-Paralisar. |
| imunidade_a_veneno | Imunidade a Veneno | MEC | imune a veneno | MEDIA | buffImunidade=veneno | encaixe limpo (veneno é elementoDano). |
| conceder_vitalidade | Conceder Vitalidade | MEC | PV temporário | MEDIA | curaPvPorEnergia=1 (temporário, some em 1h) | HP temporário em combate. |
| compartilhar_vitalidade | Compartilhar Vitalidade | MEC | cura transferindo dano | BAIXA | cura PV do alvo = dano ao operador | mecânica invertida. |
| restaurar_visao | Restaurar Visão | MEC | remove cegueira | BAIXA | removeCondicao=CEGO (perda mágica resiste) | anti-Cegueira. |
| cura_superficial | Cura Superficial | OK | cura ≤3 PV | — | — | curaPvPorEnergia=1, curaMaxPv=3. |
| cura_profunda | Cura Profunda | OK | cura ≤8 PV | — | — | curaPvPorEnergia=2, curaMaxPv=8. |
| cura_superior | Cura Superior | OK | cura total | — | — | curaTotal. |
| sono_coletivo | Sono Coletivo | OK | dormir (área) | — | — | condicao=DORMINDO, área, R-HT. |

**Não vale (24):** aliviar_enjoo, aliviar_loucura, aliviar_paralisia (crônica), aliviar_vicio, animacao_suspensa, compartilhar_energia (PF), conceder_energia (PF), curar_doencas, descanso_final, imunidade_a_doencas, interromper_envelhecimento, leitura_do_corpo, purificar, recuperar_energia, regeneracao / regeneracao_instantanea, rejuvenescer, remover_infeccao, ressurreicao, restauracao / restauracao_instantanea, restaurar_audicao, restaurar_fala, restaurar_memoria.
## Animais (12 MEC / 0 OK / 35 —)

**Mecanizar:**

| id | nome | veredito | efeito | prio | mecânica proposta | por quê |
|---|---|---|---|---|---|---|
| teia_de_aranha | Teia de Aranha | MEC | projétil imobiliza | ALTA | entrega=projetil, condicao=IMOBILIZADO, condicaoEscapeAtributo=ST (fio ST10/RD3), sofre 3x fogo | acerto DX-4/Ataque Inato prende o alvo. |
| controle_de_animal_terra | Controle de Animal (Terra) | MEC | domina animal | MEDIA | condicao=DOMINADO, condicaoResistencia=Vontade, alvo=animal (naoAfeta sapiente IQ6+) | neutraliza/vira o animal inimigo. |
| controle_de_animal_ar | Controle de Animal (Ar) | MEC | domina animal | MEDIA | idem, alvo aéreo | idem. |
| controle_de_animal_mar | Controle de Animal (Mar) | MEC | domina animal | MEDIA | idem, alvo aquático | idem. |
| controle_de_hibrido | Controle de Híbrido | MEC | domina híbrido | BAIXA | condicao=DOMINADO, R-Vontade, alvo=híbrido | híbridos raros. |
| dominar_animal_terra | Dominar Animal (Terra) | MEC | imobiliza animal | MEDIA | condicao=PARALISADO, condicaoResistencia=IQ, alvo=animal | prende o animal parado. |
| dominar_animal_ar | Dominar Animal (Ar) | MEC | imobiliza animal | MEDIA | idem | idem. |
| dominar_animal_mar | Dominar Animal (Mar) | MEC | imobiliza animal | MEDIA | idem | idem. |
| proteger_animal | Proteger Animal | MEC | RD5 + BD+3 | BAIXA | buffBd=3 (buffRd=5 já executa) | motor TEM buffBd — só ligar. |
| metamorfose_parcial_animal_ar | Metamorfose Parcial (Ar) | MEC | membro vira arma animal | BAIXA | por sub-forma: Garras→buffDanoArma=1d+1 corte; Boca→1d+2 perf; Pele Croc→buffRd=4; Asas→voo | menu de 5 efeitos; split por sub-forma. |
| metamorfose_parcial_animal_terra | Metamorfose Parcial (Terra) | MEC | idem | BAIXA | mesmos campos por sub-forma | idem. |
| metamorfose_parcial_animal_mar | Metamorfose Parcial (Mar) | MEC | idem | BAIXA | mesmos campos por sub-forma | idem. |

**Não vale (35):** acalmar_animal / agitar_animal (reação), cavalgar_criaturas (ar/mar/terra), conexao_com_animal (ar/mar/terra), convocar_animal (ar/mar/terra — invocação), falar_com_animais (ar/mar/terra), localizar_animais, metamorfose_animal (ar/mar/terra — troca stat-block), metamorfose_permanente (ar/mar/terra), metamorfose_superior, metamorfosear_outro (ar/mar/terra), passageiro_interno (ar/mar/terra — sensorial), passo_leve, possessao_de_animais / possessao_permanente_de_animais, repelir_animal (ar/mar/terra), repelir_hibridos.

## Plantas (5 MEC / 0 OK / 26 —)

**Mecanizar:**

| id | nome | veredito | efeito | prio | mecânica proposta | por quê |
|---|---|---|---|---|---|---|
| entrelacamento | Entrelaçamento | MEC | área prende | MEDIA | zona: Desloc=1/2, buffEsquiva=-2; falha ao sair → condicao=IMOBILIZADO, condicaoEscapeAtributo=ST | é o "Emaranhar"; controle de área. |
| nuvem_de_polen | Nuvem de Pólen | MEC | área -2 DX | MEDIA | condicaoResistencia=HT, buffAtributo=DX -2, condicaoRaioM, dur (na nuvem +3d turnos) | -2 DX em área é debuff forte. |
| enclausuramento_arboreo | Enclausuramento Arbóreo | MEC | árvore engole alvo | MEDIA | condicao=PARALISADO, R-HT, exige árvore; indefinido até cortar | neutraliza 1 alvo; operação 3s. |
| controle_de_planta | Controle de Planta | MEC | domina planta móvel | BAIXA | condicao=DOMINADO, R-Vontade, alvo=planta | vs planta que se move (rara). |
| forma_de_planta_em_outro | Forma de Planta em Outro | MEC | inimigo vira planta | BAIXA | condicao=PARALISADO, R-Vontade | neutralize limpo, mas operação 30s (quase inutilizável em luta). |

**Não vale (26):** abencoar_plantas, alarme_florestal, animar_planta (cria NPC), atravessar_madeira, atravessar_plantas, chuva_de_nozes (-1 NH genérico), corpo_de_lodo, corpo_de_madeira, crescimento_de_plantas, criar_planta, curar_planta, falar_com_plantas, florescer, forma_de_planta (auto), identificar_planta, localizar_planta, madeira_essencial, moldar_planta (dano a objeto), murchar_planta (save-or-die), ocultar, ocultar_rastros, pegadas_falsas, percepcao_de_planta, rejuvenescer_planta, secar_plantas, visao_das_plantas.

## Portal (2 MEC / 0 OK / 27 —)

**Mecanizar:**

| id | nome | veredito | efeito | prio | mecânica proposta | por quê |
|---|---|---|---|---|---|---|
| viagem_planar_para_outro | Viagem Planar para Outro | MEC | bane inimigo | MEDIA | condicao=REMOVIDO (sai do combate), condicaoResistencia=Vontade+1 | é o "Banir"; operação 5s. |
| transportar_outro_no_tempo | Transportar Outro no Tempo | MEC | tira do combate | MEDIA | condicao=REMOVIDO, condicaoResistencia=Vontade+1 | remoção decisiva; operação 1s. |

**Não vale (27):** acelerar_tempo, baliza (planar/tempo/teleporte), controle_de_portal, convocacao_planar, criar_porta, criar_portal (teleporte/tempo/planar), defasar / defasar_outro (defesa), desviar_teleporte, espiar_portal, intervalo, jornada_rapida, localizar_portal, ocultar_objeto, rastrear_teleporte, retardar_tempo, santuario, suspender_tempo, translocacao_no_tempo / translocar_outro_no_tempo (defesa), viagem_no_tempo, viagem_planar, visita_planar.
## Água (6 MEC / 14 OK / 34 —)

**Mecanizar:**

| id | nome | veredito | efeito | prio | mecânica proposta | por quê |
|---|---|---|---|---|---|---|
| arma_congelante | Arma Congelante | MEC | buff dano arma | ALTA | buffDanoArma=2 (CaC, 1 min) | +2 após RD; buffDanoArma existe. |
| projeteis_congelantes | Projéteis Congelantes | MEC | buff dano arma | MEDIA | buffDanoArma=2, buffArmaTipo=projétil | idem p/ arma de projétil. |
| imunidade_a_acido | Imunidade a Ácido | MEC | imune ácido | MEDIA | buffImunidade=acido | ácido é elementoDano; encaixe limpo. |
| criar_vapor | Criar Vapor | MEC | zona de vapor | MEDIA | zonaPersistente, zonaIntervaloSeg=1, elementoDano=fogo(escaldante), danoPorTurnoExpr≈1d-1 | nuvem de vapor = hex de fogo. |
| dissipar_agua | Dissipar Água | MEC | dano | MEDIA | entrega=toque/projetil, dano≈Desidratar, armadura=ignora, R-HT; vs aquático 1d/energia sem resistência | perfil de dano vs ser vivo. |
| sopro_de_radiacao | Sopro de Radiação | MEC | feixe radiação | MEDIA | entrega=feixe, feixePenalidadeDx=4, elementoDano=radiacao, danoPorEnergia=(de Jato de Radiação) | feixe executável; efeito de rads exige tratamento próprio. |

**Já ok (14):** adaga_de_gelo, bola_de_acido, carne_para_gelo (condicao R-HT), chuva_de_acido, desidratar, esfera_de_gelo, geiser, geladura, jato_de_acido, jato_de_neve (cega R-HT), jato_de_vapor, sopro_de_acido, sopro_de_vapor, toque_congelante.

**Não vale (34):** acido_essencial, agua_essencial, agua_podre (ingestão), aquavisao, atravessar_agua, camada_de_gelo (zona de penalidade), caminhar_na_agua, chuva_de_adagas_de_gelo, condensar_vapor, congelar, controle_de_elemental_agua, convocar_elemental_agua, corpo_de_agua, corpo_de_gelo, criar_acido, criar_agua, criar_elemental_agua, criar_gelo, criar_nascente, descongelar, ferver_agua, frescor, guardachuva, imunidade_a_agua ("água" não é elemento), localizar_agua, localizar_costa, moldar_agua, nadar, purificar_a_agua, respirar_agua, sapatos_de_neve, secar_nascente, sorvedouro, jato_de_agua (só empurrão).

## Fogo (5 MEC / 10 OK / 17 —)

**Mecanizar:**

| id | nome | veredito | efeito | prio | mecânica proposta | por quê |
|---|---|---|---|---|---|---|
| arma_flamejante | Arma Flamejante | MEC | buff dano arma | MEDIA | buffDanoArma=2, buffArmaTipo=fogo, entrega=CaC | +2 de dano de fogo, hoje só rótulo. |
| projeteis_flamejantes | Projéteis Flamejantes | MEC | buff dano arma | MEDIA | buffDanoArma=2, buffArmaTipo=fogo, entrega=projetil | idem p/ projétil. |
| criar_fogo | Criar Fogo | MEC | zona de fogo | MEDIA | zonaPersistente, zonaIntervaloSeg=1, danoPorTurnoExpr≈1d-1, entrega=area | fogo real que queima quem fica dentro. |
| chama_essencial | Chama Essencial | MEC | zona de fogo | BAIXA | zonaPersistente=fogo, +1 dano (+2 a aquáticos) | zona de dano, hoje "ambiente". |
| armadura_flamejante | Armadura Flamejante | MEC | imune fogo (fatia) | BAIXA | buffImunidade=fogo | dano reativo ao atacante fica narrado. |

**Já ok (10):** bola_de_fogo, bola_de_fogo_explosiva, chuva_de_fogo, imunidade_ao_fogo, imunidade_ao_frio, jato_de_chamas (feixe P9), morte_candente (recorrente), nuvem_de_fogo, sopro_congelante (feixe), toque_candente.

**Não vale (17):** apressar_fogo, atear_fogo (só objeto), calor, chama_fantasmagorica (ilusória), controle_de_elemental_fogo, convocar_elemental_fogo, corpo_de_chamas, criar_elemental_fogo, desviar_energia (defesa), extinguir_fogo, frio, fumaca (visão), localizar_fogo, moldar_fogo, resistencia_ao_fogo (anti-ignição), retardar_fogo, tepidez.

## Ar (1 MEC / 2 OK / 22 —)

**Mecanizar:**

| id | nome | veredito | efeito | prio | mecânica proposta | por quê |
|---|---|---|---|---|---|---|
| concussao | Concussão | MEC | projétil explosivo + stun | ALTA | entrega=projetil, danoPorEnergia=1d por 2, tipoDano=cont, explosaoDivisorPorMetro=3, alcanceMeioDano=20, alcanceMaximo=40; condicao=ATORDOADO, condicaoResistencia=HT-3, condicaoRaioM=10 | dano explosivo COM atordoamento em área de 10m — a condição está sem campo hoje. |

**Já ok (2):** dissipar_ar (1d-2, HT anula), mau_cheiro (nuvem tóxica 1d por HT).

**Não vale (22):** aerovisao, ar_essencial, caminhar_no_ar, controle_de_elemental_ar, convocar_elemental_ar, corpo_de_ar, corpo_de_vento, criar_ar, criar_elemental_ar, desvitalizar_ar (sufocamento), eliminar_odor, furacao (controle amplo), gerar_odor, jato_de_ar (só empurrão), localizar_ar, moldar_ar, muralha_de_vento, purificar_o_ar, respirar_ar, tempestade_de_areia, terra_para_ar, turbilhao (relocação).

## Clima (5 MEC / 9 OK / 14 —)

**Mecanizar:**

| id | nome | veredito | efeito | prio | mecânica proposta | por quê |
|---|---|---|---|---|---|---|
| arma_de_relampago | Arma de Relâmpago | MEC | buff dano arma | MEDIA | buffDanoArma=2, buffArmaTipo=eletricidade, entrega=CaC | +2 numa arma metálica de mão. |
| projeteis_de_relampago | Projéteis de Relâmpago | MEC | buff dano arma | MEDIA | buffDanoArma=2, buffArmaTipo=eletricidade, entrega=projetil | idem p/ projétil metálico. |
| granizo | Granizo | MEC | zona de impacto | MEDIA | zonaPersistente=impacto(cont), danoPorTurnoExpr=1d-2, zonaIntervaloSeg=1 | versão prejudicial: 1d-2/seg. |
| muralha_de_relampagos | Muralha de Relâmpagos | MEC | barreira de dano | MEDIA | zonaPersistente=eletricidade, dano=1d ao tocar, entrega=area (bloqueia deslocamento) | fere quem toca (1d). |
| armadura_de_relampagos | Armadura de Relâmpagos | MEC | imune eletricidade (fatia) | BAIXA | buffImunidade=eletricidade | dano reativo fica narrado. |

**Já ok (9):** bola_de_relampagos, chicote_de_relampago, imunidade_a_relampagos, nuvem_de_faiscas, olhar_de_relampago (feixe P9), relampago, relampago_explosivo, tempestade_de_faiscas, toque_chocante.

**Não vale (14):** aquece, chuva, corpo_de_relampagos, correnteza, geada, mare, neve, nevoeiro (visão), nuvens, ondas, previsao_do_tempo, resfriar, tempestade ("o Mestre decide"), vento.

## Terra (2 MEC / 4 OK / 23 —)

**Mecanizar:**

| id | nome | veredito | efeito | prio | mecânica proposta | por quê |
|---|---|---|---|---|---|---|
| carne_para_pedra | Carne para Pedra | MEC | petrifica | MEDIA | condicao=PARALISADO, condicaoResistencia=HT, dur=permanente (até reverter) | gêmea de Carne para Gelo (já OK) — inconsistência a corrigir. |
| soterramento | Soterramento | MEC | engole o alvo | MEDIA | condicao=PARALISADO/preso, condicaoResistencia=HT, dur permanente (até resgate) | a terra engole e tira de combate. |

**Já ok (4):** chuva_de_pedras, jato_de_areia (cega R-HT), jato_de_lama (cega R-HT), projetil_de_pedra.

**Não vale (23):** alterar_terreno, atravessar_terra, controle_de_elemental_terra, convocar_elemental_terra, corpo_de_pedra, criar_elemental_terra, criar_terra, deslocar_terreno, espectro_de_metal, geovisao, localizar_passagem, localizar_terra, moldar_terra, pedra_para_carne, pedra_para_terra, petrificacao_parcial (DX indefinido), prever_movimento_da_terra, purificar_a_terra, terra_essencial, terra_para_agua, terra_para_pedra, terremoto, vulcao.
## Deslocamento (5 MEC / 4 OK / 30 —)

**Mecanizar:**

| id | nome | veredito | efeito | prio | mecânica proposta | por quê |
|---|---|---|---|---|---|---|
| poltergeist | Poltergeist | MEC | arremessa objeto | MEDIA | entrega=projetil, danoFixo=1d (≤5kg)/1d+1 (≤12,5kg), tipoDano=cont, alvo esquiva/bloqueia, 1/2D20 Máx60 | ataque à distância com dano por faixa de peso. |
| faca_alada | Faca Alada | MEC | arma voadora | MEDIA | entrega=projetil, danoFixo por arma (ST15), 1/2D20 Máx40, alvo defende | arremessa arma como ST 15. |
| cola | Cola | MEC | zona grudenta | MEDIA | zona: condicao=IMOBILIZADO, condicaoResistencia=HT | Prender/Grudar imobiliza quem pisa. |
| ensebar | Ensebar | MEC | zona escorregadia | MEDIA | zona: teste DX-2 ao mover → condicao=CAIDO; -3 defesas | Escorregão derruba; controle de área. |
| retardar | Retardar | MEC | reduz ações | MEDIA | condicao=RETARDADO (perde ~metade das ações), condicaoResistencia=HT, dur=10s | debuff que reduz ações do inimigo. |

**Já ok (4):** apressar (buffDeslocamento + buffEsquiva), golpe_distante (feixe P9), voo (buffDeslocamentoFixo=10), voo_do_falcao (=40).

**Não vale (30):** acelerar (ação extra), aporte, aumentar_carga, caminhar_nas_nuvens, caminhar_nas_paredes, chavemestra, corpo_etereo, desatar, deslizar, desviar_projetil (defesa), levitacao, liberdade, manipular, marcha_acelerada, marcha_lenta, objeto_dancante, passo_leve, puxar / repelir (zona de movimento forçado), reduzir_carga, reter, retardar_queda, saltar_nuvens, salto, serralheiro, tapete_voador, teleportar_outro, teleporte, translocacao / translocar_outro (defesa/teleporte).

## Luz e Trevas (5 MEC / 3 OK / 23 —)

**Mecanizar:**

| id | nome | veredito | efeito | prio | mecânica proposta | por quê |
|---|---|---|---|---|---|---|
| invisibilidade | Invisibilidade | MEC | não é visto | ALTA | buffPenalidadeAtacantes grande (~-10, ou por Visibilidade MB394) | hoje o Mestre aplica na mão; campo já existe. |
| escuridao | Escuridão | MEC | área cega | MEDIA | entrega=area + penalidade de acerto (~cego/-10) a todos dentro; Visão no Escuro anula | exige camada de iluminação (maior lift). |
| trevas | Trevas | MEC | penumbra unidirecional | MEDIA | entrega=area; penaliza só ataques que ENTRAM na área | penumbra unidirecional. |
| jato_de_luz | Jato de Luz | MEC | ofusca | BAIXA | condicao "ofuscado": -4 NH combate 1 turno, -1 resto; auto no acerto | acerto já P9; falta o ofuscamento. |
| visao_brilhante | Visão Brilhante | MEC | anti-cegueira por luz | BAIXA | buffImunidade a cegueira por luz (anula Lampejo/Raio Solar/Jato de Luz) | defesa contra condições de luz. |

**Já ok (3):** lampejo (condicaoBandas), nublar (buffPenalidadeAtacantes), raio_solar (projétil).

**Não vale (23):** brilho, cores, corpo_de_sombra, esconder (sentido), escurecer, espelho, infravisao, luz, luz_continua, luz_magica, luz_magica_continua, luz_solar, luz_solar_continua, moldar_luz, moldar_trevas, muralha_de_luz (LDV), olhos_do_falcao, remover_reflexo, remover_sombra, ver_o_invisivel, visao_microscopica (inverteria a regra), visao_nas_trevas, visao_noturna.

## Som (3 MEC / 3 OK / 17 —)

**Mecanizar:**

| id | nome | veredito | efeito | prio | mecânica proposta | por quê |
|---|---|---|---|---|---|---|
| estrondo | Estrondo | MEC | ensurdece em área | MEDIA | condicao=SURDO (nova), condicaoResistencia=HT, condicaoRaioM=3 | ataque de área que ensurdece. |
| silencio | Silêncio | MEC | trava conjurador | BAIXA | zona aplicando condicao=SILENCIADO a quem está dentro | anti-conjurador real, hoje ambiente. |
| imunidade_ao_som | Imunidade ao Som | MEC | imune a som | BAIXA | buffImunidade a som (anula Estrondo/Jato de Som) | defesa contra condições sonoras. |

**Já ok (3):** deturpar (silenciado), jato_de_som (feixe, atordoa — só stun, não dano), quietude (silenciado).

**Não vale (17):** audicao_remota, barulho (-3 IQ em área), conversar, eloquencia, escriba, escriba_musical, furtividade_magica, imitar_voz, mensagem, muralha_de_silencio, ouvido_magico, ouvido_magico_invisivel, retardar_mensagem, som, visao_sonora, voz_amplificada, vozes.

## Mente (2 MEC / 10 OK / 44 —)

**Mecanizar:**

| id | nome | veredito | efeito | prio | mecânica proposta | por quê |
|---|---|---|---|---|---|---|
| embriaguez | Embriaguez | MEC | debuff DX | MEDIA | buffAtributo=DX, valor=-(energia), R-Vontade; -IQ narrado | perda de DX real; nº precisa de decisão (texto contraditório). |
| enjoo | Enjoo | MEC | meio deslocamento | MEDIA | buffDeslocamento=meio, R-HT, dur=60s | meio-deslocamento concreto; náusea narrada. |

**Já ok (10):** atordoamento_mental, extase (paralisado), medo / panico / terror (amedrontado), sono (dormindo), enfraquecer_vontade, fortalecer_vontade, sabedoria, tolice (IQ/Vontade P3-1).

**Não vale (44):** todas as ilusões mentais (alucinacao, alucinacao_superior), compulsões e controle mental fino (atrair, cativar, comando, condicionamento(+perm), escravizar, fascinar, subjugar, sugestao(+coletiva), travar_vontade), atrofiar/sentido/prontidao/retrovisao/sabedoria-de-sentido (Sentido/Percepção não modelados), bravura, furia, estupidez (IQ), emoção/social (controle_de_emocao, compelir_a_mentira, juramento, lealdade, loquacidade), memória (esquecimento(+perm), memoria_falsa), loucura(+perm), sono_curativo / sono_tranquilo / pesadelo, torpor(+coletivo), evitar, criptografar, desorientar, vigilia.

## Ilusão (0 MEC / 0 OK / 20 —)

**Não vale (20):** anular_criacao, anular_ilusao, autossuficiencia, cobertura_ilusoria, controle_de_criacao, controle_de_ilusao, criar_animal, criar_guerreiro, criar_montaria, criar_objeto, criar_servo (convocações), detectar_ilusao, disfarce_ilusorio, duplicar, entalhar, fantasma, ilusao_complexa, ilusao_perfeita, ilusao_simples, iniciativa. Ilusões só enganam (dano real dissipa), convocações exigiriam subsistema de invocação, e o resto é dispel/criação.
## Quebrar e Consertar (8 MEC / 0 OK / 25 —)

**Mecanizar:**

| id | nome | veredito | efeito | prio | mecânica proposta | por quê |
|---|---|---|---|---|---|---|
| afiar | Afiar | MEC | buff dano arma | MEDIA | buffDanoArma=+1 (esc. +2/+3), buffArmaTipo=corte/perfuração, buffMaxNiveis=3 | +1 a +3 no dano; mapeia direto. |
| endurecer | Endurecer | MEC | debuff DX/Desloc | MEDIA | buffAtributo=DX (neg), buffDeslocamento (neg), condicaoResistencia=ST+2 | endurece a roupa do inimigo: -1 DX/0,5kg. |
| atar | Atar | MEC | imobilizar | BAIXA | condicao=IMOBILIZADO, condicaoResistencia=DX, entrega=toque (exige corda) | amarra o alvo; depende de corda no cenário. |
| desintegrar | Desintegrar | MEC | dano a objeto | BAIXA | dano, energiaPorDado=1 (máx 4d), alvoValido=objeto, entrega=toque | só objeto; útil se o motor mirar itens. |
| enfraquecer | Enfraquecer | MEC | dano a objeto | BAIXA | dano, energiaPorDado=2, armadura=ignora, alvoValido=objeto | só item inanimado; RD não protege. |
| explodir | Explodir | MEC | dano a objeto + fragmentação | BAIXA | dano, energiaPorDado=2, alvoValido=objeto; estilhaça em área | anti-item com respingo anti-pessoal. |
| fender | Fender | MEC | dano a objeto | BAIXA | dano, energiaPorDado=1, tipoDano=perf extremo, alvoValido=objeto | buraco em objeto/parede. |
| fragmentar | Fragmentar | MEC | dano a objeto | BAIXA | dano, energiaPorDado=2, alvoValido=objeto | quebra objeto inanimado. |

**Não vale (25):** alongar_objeto, animar_objeto, aumentar_objeto, cartografo, consertar, contrair_objeto, copiar, criacao_inspirada, despertar_espirito_do_oficio, detectar_pontos_fracos, encolher_objeto, estragar, fortalecer (exclui armadura), fusao_com_arma, imunidade_a_sujeira, limpar, marca_mistica, no, remendar, remodelar, resistencia_a_choques (qualidade), restaurar, tingir, transformar_objeto, transparencia. *(Ratoeiras: Fortalecer e Resistência a Choques parecem buff de RD/arma mas a descrição exclui proteção de armadura/dano — NÃO mecanizar.)*

## Metamágica (4 MEC / 0 OK / 36 —)

**Mecanizar:**

| id | nome | veredito | efeito | prio | mecânica proposta | por quê |
|---|---|---|---|---|---|---|
| magica_penetrante | Mágica Penetrante | MEC | penetração de armadura | MEDIA | novo campo divisorArmadura (2/3/5/10/ignora) na próxima magia de dano | penetração é efeito forte; buffDanoArma é pós-RD (semântica diferente). |
| resistencia_a_magia | Resistência à Magia | MEC | +RM | MEDIA | novo buffResistenciaMagia (+2/energia disposto, máx 5); soma à resistência vs magias | defesa real vs ataque mágico inimigo. |
| escudo_antimagica | Escudo Antimágica | MEC | barreira anti-magia | BAIXA | zona anti-magia com Resistência (PV, -1 por magia que passa); não afeta projétil físico | defesa vs conjurador. |
| muralha_magica | Muralha Mágica | MEC | parede anti-magia | BAIXA | idem (bloqueia magias por um lado) | mesma lógica de Escudo Antimágica. |

**Não vale (36):** adiar_magica, anular_magica, apanhar_magica, arremessar_magica, aura_falsa, bencao (+global), conceder_magica, conexao, contramagica, deslocar_magica, drenar_aptidao_magica, drenar_mana, escamotear_informacao, guardamagica, maldicao (-global), manter_magica, ocultar_magia, pentagrama, protecao / protecao_superior (defesa reativa), recarregar_gema_de_energia, refletir, reflexo, remover_aura, remover_maldicao, resguardar / resguardar_area, restaurar_mana, retardo, roubar_magica, suspender_aptidao_magica, suspender_magia / suspender_magica, suspender_maldicao, suspender_mana, telemagica. *(Dissipar magia inimiga fino não é modelado.)*

## Tecnológica (3 MEC / 1 OK / 42 —)

**Mecanizar:**

| id | nome | veredito | efeito | prio | mecânica proposta | por quê |
|---|---|---|---|---|---|---|
| jato_de_radiacao | Jato de Radiação | MEC | feixe de radiação | MEDIA | entrega=feixe (DX-4/Ataque Inato), elementoDano=radiacao; NÃO causa PV (regras de rad) | ataque com jogada de acerto (P9); rads exigem tratamento próprio. |
| corpo_de_metal | Corpo de Metal | MEC | RD + tolerância | BAIXA | buffRd (MB263) + buffImunidade (metabólico/veneno/sangramento) | transformação defensiva com RD real. |
| corpo_de_plastico | Corpo de Plástico | MEC | RD1 + imunidades | BAIXA | buffRd=1 + buffImunidade (metabólico) | RD 1 explícito + Tolerância. |

**Já ok (1):** sopro_de_fogo (feixe DX-2, 1d+1/energia, P9).

**Não vale (42):** todas as /NT de veículo/dispositivo/combustível/corrente elétrica (acionar, animar_maquina, avaria, cessar/conceder/conduzir/extrair/roubar_corrente, combustivel_essencial, controle/convocar/possessao_de_maquina, criar/preservar/purificar/testar_combustivel, despertar_computador, diagrama, falar_com_maquinas, identificar_funcao, pane, reconstruir), radiação fora do funil de PV (curar/extinguir/imunidade/irradiar/localizar/ver_radiacao), sentidos (metalovisao, plastivisao, radioaudicao, visao_do_espectro/magnetica), e identificar/localizar/moldar metal/plástico.

## Proteção (0 MEC / 4 OK / 24 —)

**Já ok (4):** armadura (buffRd), bloquear (buffBd 1 uso), escudo (buffBd — nota interna "narrado" está obsoleta), robustez (buffRd 1 uso).

**Não vale (24):** apanhar_projetil, braco_de_ferro, deteccao_de_veneno, devolver_projetil, domo_absoluto / domo_atmosferico / domo_climatico / domo_de_forca (barreiras de área), escudo_antiprojeteis, escudo_antiteleporte, giralaminas / girar_lamina (converte tipo de dano), imunidade_a_pressao, muralha_absoluta / muralha_de_forca, neblina_mistica, percepcao_de_observacao, percepcao_do_perigo, refletir_olhar, reverter_projeteis, rouxinol, sentinela, sombrear, tranca_magica. *(Defesas ativas mágicas — aparar/bloquear/refletir concedido por magia — não são modeladas.)*

## Necromancia (0 MEC / 3 OK / 37 —)

**Já ok (3):** afetar_espiritos (buffAfetaInsubstancial), morte_putrefata (dano recorrente, naoAfeta morto-vivo), visao_da_morte (atordoado).

**Não vale (37):** animacao, animar_sombra, aprisionar_alma / aprisionar_espirito, barreira_astral, cessar_cura, comandar_espirito, controle_de_zumbi, convocar_demonio / convocar_espirito / convocar_zumbi, envelhecer, espantar_espirito / espantar_zumbi, espectro, espirito_de_caveira, evisceracao (save-or-die), expulsar, lich, materializar, percepcao_de_espiritos, pestilencia, prender_espirito, repelir_espiritos, retardar_cura, roubar_forca / graca / vigor / sabedoria / beleza / energia / juventude / pericia / vitalidade (efeito duplo, fora de combate), solidificar, zumbi / zumbis_em_massa. *(Invocação, controle de espíritos/mortos-vivos, roubo de atributos, maldições.)*

## Reconhecimento (0 / 0 / 41 —)

**Não vale (41) — 100% detecção/sentido/informação:** adivinhacao, alarme, analisar_magica, aromas_do_passado, aura, boca_magica, convocar_sombra, deteccao_de_magia, ecos_do_passado, guia, historia, historia_antiga, hora_certa, identificar_magica, imagens_do_passado, localizador, localizar_magia, mao_magica, medidas, memorizar, muralha_de_vidro, nariz_magico, olho_magico, olho_magico_invisivel, orientacao, percepcao_de_magia, percepcao_de_mana, prehistoria, projecao_da_mente, rastrear, reconstruir_magica, recordar, recordar_caminho, revelar_posicao, tato_remoto, testar_carga, ver_a_forma_real, ver_localizacao, ver_segredos, visao_astral, visao_de_magia.

## Comunicação (0 / 0 / 32 —)

**Não vale (32) — telepatia/idioma/emoção/possessão social:** anular_possessao, compelir_a_verdade, comunicacao, conceder_idioma, conceder_pericia, controle_de_pessoa, dom_da_escrita, dom_das_linguas, Insignificancia, leitura_da_mente, ocultar_emocao, ocultar_pensamentos, passageiro_da_alma, percepcao_de_emocao, percepcao_de_inimigos, percepcao_de_veracidade, percepcao_de_vida, persuasao, possessao, possessao_permanente, presenca, projecao_de_sonho, regressao, requisitar_idioma, requisitar_pericia, sonda_mental, telepatia, transmissao_de_sonho, transmissao_mental, trocar_de_corpo, vexacao, visualizacao_de_sonho.

## Alimentos (0 / 0 / 18 —)

**Não vale (18) — criação/preparo/detecção de comida:** agua_para_vinho, alimento_essencial, banquete_do_monge, banquete_do_tolo, cozinhar, criar_alimento, destilar, deteriorar, envenenar_alimento, limpar_a_caca, localizar_alimento, maturar, paladar_remoto, preservar_alimento, purificar_o_alimento, temperar, testar_alimento, ver_receita.

## Encantamento (0 / 0 / 57 —)

**Não vale (57) — a escola inteira é criação de item mágico (fora de combate; excluída de propósito):** amuleto, arma_dancante, arma_espiritual, arma_fantasmagorica, arma_graciosa, arma_penetrante, arma_protetora, bloqueador_de_impressoes, bola_de_cristal, cajado, cornucopia, denominar, desejo, desejo_superior, desviar, doppelganger, efigie, eliminar, encantamento_temporario, encantar, encobrir, energizacao, enfeiticar, escudo_dancante, escudo_protetor, espada_fiel, feitico, flecha_magica, flecha_magica_rapida, flecha_magica_vazia, fortificar, gema_de_energia, gema_de_energia_de_uma_so_escola, golem, homunculo, imunidade_a_encantamento, limite, maleficio, mira_rapida, pedra_da_alma, pedra_de_mana, pedra_magica, pequeno_desejo, pergaminho_magico, precisao, pujanca, reduzir_o_peso, remover_encantamento, sacar_rapido, senha, simulacro, sintonizar, suspender_encantamento, talisma, vazamento, velocidade, vestuario_ajustavel.

---

*Levantamento gerado por 9 agentes em paralelo lendo `magias2versao.json` por escola. Os shards por escola
e as tabelas brutas de cada agente ficam no scratchpad da sessão. Próximo passo sugerido: começar por
**MAG-0/MAG-1** (correções + buffs de atributo de Corpo), que não exigem motor novo.*





