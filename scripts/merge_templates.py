# -*- coding: utf-8 -*-
import json, os
os.chdir(os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets"))

def load(f):
    d = json.load(open(f, encoding="utf-8"))
    return d if isinstance(d, list) else d.get("items", [])

def ids(*files):
    s = set()
    for f in files:
        for i in load(f):
            if isinstance(i, dict) and i.get("id"):
                s.add(str(i["id"]))
    return s

VAN = ids("vantagens.v3.json", "vantagens_artes_marciais.v1.json")
DES = ids("desvantagens.v2.json")
PER = ids("pericias.json", "pericias_artes_marciais.v1.json")
TEC = ids("tecnicas.v1.json")
MAG = ids("magias2versao.json")
EQUIP = ids("armas_corpo_a_corpo.v1.normalized.json", "armas_distancia.v1.normalized.json",
            "armas_fogo.v1.normalized.json", "armaduras.v2.json", "escudos.v1.json")

# --- correcoes de IDs do templates2 (Rodolfo) ---
FIX_VAN = {"voz": "voz_penetrante"}
FIX_DES = {"ganancia": "avareza"}
FIX_PER = {"machado_de_duas_maos": "macamachado_de_duas_maos",
           "navegacao": "navegacao_nt", "jogos": "jogos_de_azar"}
FIX_EQUIP = {"machado_de_duas_maos": "macamachado_de_duas_maos"}
REMOVE_VAN = {"intimidante"}  # nao existe vantagem equivalente; template ja tem pericia intimidacao

def corrigir(t):
    # vantagens
    novv = []
    for v in t.get("vantagens", []):
        vid = FIX_VAN.get(v["id"], v["id"])
        if vid in REMOVE_VAN:
            continue
        v["id"] = vid
        novv.append(v)
    t["vantagens"] = novv
    # desvantagens
    for x in t.get("desvantagens", []):
        x["id"] = FIX_DES.get(x["id"], x["id"])
    # pericias
    for p in t.get("pericias", []):
        p["id"] = FIX_PER.get(p["id"], p["id"])
    # equipamentos: corrige conhecidos, remove os que nao existem (sao so sugestao)
    if "equipamentos" in t:
        t["equipamentos"] = [FIX_EQUIP.get(e, e) for e in t["equipamentos"]
                             if FIX_EQUIP.get(e, e) in EQUIP]
    return t

def validar(t, origem):
    miss = []
    for v in t.get("vantagens", []):
        if v["id"] not in VAN: miss.append("V:" + v["id"])
    for x in t.get("desvantagens", []):
        if x["id"] not in DES: miss.append("D:" + x["id"])
    for p in t.get("pericias", []):
        if p["id"] not in PER and p["id"] not in TEC: miss.append("P:" + p["id"])
    for m in t.get("magias", []):
        if m not in MAG: miss.append("M:" + m)
    if miss:
        print("  [%s] %s: %s" % (origem, t["id"], miss))
    return len(miss)

# --- carrega os 3 conjuntos ---
base = json.load(open("forjador_templates.json", encoding="utf-8"))  # ja tem 30 (20 orig + 10 meus)

txt = open("forjador_templates2.json", encoding="utf-8").read().rstrip()
if txt.endswith(","): txt = txt[:-1]
if not txt.endswith("]"): txt += "]"
rodolfo = json.loads(txt)
rodolfo = [corrigir(t) for t in rodolfo]

print("Validando templates2 (Rodolfo) apos correcao:")
bad = sum(validar(t, "R") for t in rodolfo)
print("  IDs quebrados restantes:", bad)

if bad == 0:
    existentes = {t["id"] for t in base}
    add = [t for t in rodolfo if t["id"] not in existentes]
    base.extend(add)
    # validacao final do conjunto inteiro
    print("Validacao final do conjunto completo (%d templates):" % len(base))
    bad_total = sum(validar(t, "ALL") for t in base)
    allids = [t["id"] for t in base]
    dups = sorted(set(x for x in allids if allids.count(x) > 1))
    print("  IDs quebrados:", bad_total, "| duplicados:", dups or "nenhum")
    if bad_total == 0 and not dups:
        json.dump(base, open("forjador_templates.json", "w", encoding="utf-8"),
                  ensure_ascii=False, indent=2)
        print("SALVO: forjador_templates.json com %d templates (+%d do Rodolfo)" % (len(base), len(add)))
    else:
        print("NAO salvei - ha problemas no conjunto final")
else:
    print("NAO mesclei - corrija os IDs do templates2 acima")
