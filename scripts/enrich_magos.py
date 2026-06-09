# -*- coding: utf-8 -*-
import json, os
os.chdir(os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets"))

def load(f):
    d = json.load(open(f, encoding="utf-8"))
    return d if isinstance(d, list) else d.get("items", [])

def ids(*fs):
    s = set()
    for f in fs:
        for i in load(f):
            if isinstance(i, dict) and i.get("id"): s.add(str(i["id"]))
    return s

VAN = ids("vantagens.v3.json", "vantagens_artes_marciais.v1.json")
DES = ids("desvantagens.v2.json")
EQ = ids("armas_corpo_a_corpo.v1.normalized.json", "armas_distancia.v1.normalized.json",
         "armas_fogo.v1.normalized.json", "armaduras.v2.json", "escudos.v1.json")

# Enriquecimento tematico por mago: (atributos, vantagens_extra, desvant_extra, equipamentos)
# aptidao_magica 2 e curiosidade JA estao; aqui ADICIONAMOS.
ENRICH = {
 "ilusionista":          ({"dx":11,"iq":14,"ht":10}, [{"id":"voz_melodiosa"}], [{"id":"reputacao","custoEscolhido":-5}], ["adaga","tunica","capa_leve"]),
 "invocador":            ({"iq":14,"ht":11},          [{"id":"empatia_com_espiritos"}], [{"id":"voto"}], ["bordao","adaga","tunica"]),
 "mago_de_batalha":      ({"st":11,"dx":12,"iq":13,"ht":12}, [{"id":"reflexos_em_combate"}], [{"id":"senso_do_dever"}], ["bordao","adaga","tunica"]),
 "cronomante":           {"atrib":{"iq":15,"ht":10}, "van":[{"id":"nocao_exata_do_tempo"}], "des":[{"id":"obsessao"}], "eq":["bordao","tunica"]},
 "astrologo":            {"atrib":{"iq":14,"ht":10}, "van":[{"id":"intuicao"}], "des":[{"id":"distraido" if "distraido" in DES else "obsessao"}], "eq":["bordao","tunica"]},
 "encantador":           {"atrib":{"iq":14,"ht":10}, "van":[{"id":"carisma","nivel":1},{"id":"voz_melodiosa"}], "des":[{"id":"luxuria"}], "eq":["adaga","tunica","capa_leve"]},
 "alquimista_de_guerra": {"atrib":{"dx":11,"iq":14,"ht":11}, "van":[{"id":"resistente"}], "des":[{"id":"obsessao"}], "eq":["adaga","tunica","capa_pesada"]},
 "necromante_branco":    {"atrib":{"iq":14,"ht":11}, "van":[{"id":"empatia_com_espiritos"}], "des":[{"id":"senso_do_dever"}], "eq":["bordao","adaga","tunica"]},
 "bruxa_do_pantano":     {"atrib":{"iq":14,"ht":11}, "van":[{"id":"empatia_com_plantas"}], "des":[{"id":"reputacao","custoEscolhido":-10}], "eq":["bordao","faca","tunica_de_pele"]},
 "magista_runico":       {"atrib":{"dx":11,"iq":14,"ht":10}, "van":[{"id":"versatil"}], "des":[{"id":"obsessao"}], "eq":["bordao","adaga","tunica"]},
}

def get(spec, i, default):
    """suporta tanto tupla quanto dict nos specs acima."""
    if isinstance(spec, dict):
        return spec.get(["atrib","van","des","eq"][i], default)
    return spec[i]

d = json.load(open("forjador_templates.json", encoding="utf-8"))
bad = 0
aplicados = 0
for t in d:
    if t["id"] not in ENRICH: continue
    spec = ENRICH[t["id"]]
    atrib = get(spec, 0, {"iq":14,"ht":10})
    van_extra = get(spec, 1, [])
    des_extra = get(spec, 2, [])
    eq = get(spec, 3, [])

    # valida extras
    for v in van_extra:
        if v["id"] not in VAN: print("RUIM V", t["id"], v["id"]); bad += 1
    for x in des_extra:
        if x["id"] not in DES: print("RUIM D", t["id"], x["id"]); bad += 1
    eq_valido = [e for e in eq if e in EQ]
    for e in eq:
        if e not in EQ: print("AVISO equip inexistente (ignorado):", t["id"], e)

    if bad: continue
    # aplica: atributos + adiciona vantagens/desvantagens sem duplicar + equip
    t["atributos"] = atrib
    vis_v = {v["id"] for v in t.get("vantagens", [])}
    for v in van_extra:
        if v["id"] not in vis_v: t.setdefault("vantagens", []).append(v); vis_v.add(v["id"])
    vis_d = {x["id"] for x in t.get("desvantagens", [])}
    for x in des_extra:
        if x["id"] not in vis_d: t.setdefault("desvantagens", []).append(x); vis_d.add(x["id"])
    t["equipamentos"] = eq_valido
    aplicados += 1

print("IDs invalidos:", bad)
if bad == 0:
    json.dump(d, open("forjador_templates.json", "w", encoding="utf-8"), ensure_ascii=False, indent=2)
    print("SALVO: enriquecidos %d magos" % aplicados)
else:
    print("NAO salvei")
