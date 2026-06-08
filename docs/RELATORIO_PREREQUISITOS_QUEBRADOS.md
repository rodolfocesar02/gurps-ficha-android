# Relatório — Pré-requisitos de magia quebrados

> Gerado em 2026-06-03 por auditoria de satisfatibilidade (testa todas as 873 magias:
> monta uma ficha que CUMPRE os requisitos e verifica se o motor LIBERA).
> 32 magias não liberam mesmo cumprindo. Agrupadas por causa.

Regras do parser que o motor entende (referência):
- Número em **dígito** (`4`, não "quatro").
- Sempre **"de X"** (nunca "X" sozinho, "da escola X" nem "com X").
- **Sem vírgula** antes de "e"/"ou" (`, e` / `, ou` quebra a precedência).
- Numa rota com magia + contagem, **magia vem antes** ("Convocar Elemental e 4 de Ar").
- Pra forçar magia obrigatória + escolha, **repetir** a obrigatória nas duas rotas.
- "N magias quaisquer" → o motor JÁ entende (Lote 335).
- "N magias em X escolas diferentes" → o motor entende (NÃO mexer).

---

## GRUPO A — Erro de digitação no nome (não bate com o catálogo)

| Magia | Texto atual | Correção sugerida |
|-------|-------------|-------------------|
| `alterar_terreno` | `AM3, Moldar Ar, Modar Terra, Moldar Agua, Moldar Fogo` | corrigir **"Modar"→"Moldar"**: `AM3 e Moldar Ar e Moldar Terra e Moldar Água e Moldar Fogo` (conferir nomes exatos das 4 magias Moldar no catálogo) |
| `curar_doencas` | `Cura Profunda, Aliviar Enjooos` | `Cura Profunda e Aliviar Enjoo` (nome real: "Aliviar Enjoo", id `aliviar_enjoo`) |

## GRUPO B — Metamorfose: "Criatura" vs "Criaturas" (singular/plural não bate)

O catálogo usa nomes específicos. Conferir o nome EXATO de cada magia base e replicar.

| Magia | Texto atual | Observação |
|-------|-------------|------------|
| `metamorfosear_outro_animal_ar` | `AM2, Metamorfose (Criatura do Ar)` | conferir nome real da magia "Metamorfose ..." no catálogo |
| `metamorfosear_outro_animal_mar` | `AM2, Metamorfose (Criatura do Mar)` | idem |
| `metamorfosear_outro_animal_terra` | `AM2, Metamorfose (Criatura do Terra)` | "do Terra" → provavelmente "da Terra" |
| `metamorfose_permanente_animal_ar` | `AM3, Metamorfose (Criatura do Ar)` | idem |
| `metamorfose_permanente_animal_mar` | `AM3, Metamorfose (Criatura do Mar)` | idem |
| `metamorfose_permanente_animal_terra` | `AM3, Metamorfose (Criatura do Terra)` | idem |
| `metamorfose_parcial_animal_ar` | `AM3, Metamorfosear Outro (Criaturas do Ar), Alterar Corpo` | conferir se "Metamorfosear Outro (...)" bate com o nome real |
| `metamorfose_parcial_animal_mar` | `AM3, Metamorfosear Outro (Criaturas do Mar), Alterar Corpo` | idem |
| `metamorfose_parcial_animal_terra` | `AM3, Metamorfosear Outro (Criaturas da Terra), Alterar Corpo` | idem |
| `transformar_corpo` | `3 formas de Metamorfose, Alterar Corpo` | "3 formas de Metamorfose" = 3 magias de Metamorfose? Reescrever como contagem |

> ⚠️ É a área que o Rodolfo já anotou como "magias de metamorfose têm pré-requisitos complicados".
> Precisa primeiro LISTAR os nomes exatos das magias Metamorfose/Metamorfosear no catálogo.

## GRUPO C — "de cada um dos N elementos" (igual detectar_pontos_fracos)

O motor entende o formato `1 mágica de cada um dos quatro escola (ar,terra,fogo,agua)`.
Reescrever nesse padrão.

| Magia | Texto atual | Correção sugerida |
|-------|-------------|-------------------|
| `domo_climatico` | `2 mágicas de cada um dos 4 elementos` | `2 mágicas de cada um dos quatro escola (ar,terra,fogo,agua)` |
| `reconstruirnt` | `AM3, Consertar, Criar Objeto e 3 magicas de cada escola Ar, Fogo, Terra e Agua` | `AM3 e Consertar e Criar Objeto e 3 mágicas de cada um dos quatro escola (ar,fogo,terra,agua)` |

## GRUPO D — Nome-base / sub-escola (conferir nome exato)

| Magia | Texto atual | Observação |
|-------|-------------|------------|
| `conexao_com_animal_agua` | `Convocar Animal (Criaturas da Água)` | conferir: catálogo tem "Convocar Animal (Criaturas do Mar)" — NÃO "da Água". Provável nome errado. |

## GRUPO E — O motor NÃO valida (idioma / "Varia" / tema-vantagem)

Estes pedem coisas fora do sistema de contagem de magia. Opções: (a) remover a parte
não-validável do texto, deixando só as magias; (b) aceitar que não é checada.

### E1 — Idiomas ("X idioma(s) Com Sotaque")
| Magia | Texto atual |
|-------|-------------|
| `copiar` | `Tingir, um idioma Com Sotaque` |
| `dom_da_escrita` | `Requisitar Idioma, 3 idiomas Com Sotaque` |
| `dom_das_linguas` | `Requisitar Idioma, 3 idiomas Com Sotaque` |
| `escriba` | `Vozes, Objeto Dançante, 1 idioma Com Sotaque` |
| `pergaminho_magico` | `AM1, 1 idioma com Sotaque` |

### E2 — "Varia" (pré-requisito variável, sem regra fixa)
| Magia | Texto atual |
|-------|-------------|
| `aumentar_atributo` | `Varia` |
| `roubar_atributo` | `Varia` |

### E3 — Tema/vantagem que vira "vantagem fake" (conferir nome ou aceitar)
| Magia | Texto atual | Observação |
|-------|-------------|------------|
| `armadura_de_relampagos` | `6 magicas de Relampagos, incl. Imunidade a Relampagos` | "de Relâmpagos" = tema (conta por nome). "incl." pode atrapalhar. |
| `cornucopia` | `Encantar, 2 mágicas de Encantamento de Armas` | "Encantamento de Armas" — escola? tema? conferir |
| `criar_combustivelnt` | `Localizar Combustível, 2 mágicas de transmutação` | "transmutação" não é escola — conferir |
| `iniciativa` | `Auto-Suficiência, Sabedoria` | "Auto-Suficiência" — é magia? vantagem? (id `autossuficiencia` existe) — conferir acento/nome |
| `jato_de_radiacao` | `Irradiar, Resistência à Radiação` | "Resistência à Radiação" = vantagem? magia? |
| `jato_de_som` | `Voz Ampliada` | "Voz Ampliada" — conferir se é magia do catálogo |
| `luz_magica` | `Visão de Mágica, Luz` | "Visão de Mágica" — conferir nome real (id `visao_de_magia`?) |
| `olho_magico` | `Aporte, Visão Aguçada` | "Visão Aguçada" = vantagem (não validada) |
| `paladar_remoto` | `AM1, sem Anosmia, Localizar Alimento, Localizar Ar` | "sem Anosmia" = condição de desvantagem |
| `visao_brilhante` | `Sentido Aguçado (Visão Aguçada) ou 5 mágicas de Luz e Trevas, não ter Desvantagem Cegueira` | mistura vantagem + contagem + condição |

---

## Próximos passos sugeridos
1. **Grupo A e C**: correções mecânicas seguras — aplicar já.
2. **Grupo B (metamorfose)**: primeiro listar nomes exatos no catálogo, depois corrigir em lote.
3. **Grupo D**: 1 magia, conferir nome.
4. **Grupo E**: decisão de produto — remover texto não-validável ou aceitar não-checado.

> NOTA: ~18 magias com "N magias em N escolas diferentes" apareceram na auditoria como
> falso-positivo (o teste não montou escolas distintas suficientes) — elas FUNCIONAM no app.
> Não estão neste relatório.
