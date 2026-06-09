# -*- coding: utf-8 -*-
import json, os, unicodedata
os.chdir(os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets"))

def norm(s):
    return unicodedata.normalize("NFD", str(s)).encode("ascii", "ignore").decode().lower().strip()

def load(f):
    d = json.load(open(f, encoding="utf-8"))
    return d if isinstance(d, list) else d.get("items", [])

per = load("pericias.json") + load("pericias_artes_marciais.v1.json")
PER_IDS = set(str(p["id"]) for p in per if isinstance(p, dict) and p.get("id"))
N2ID = {}
for p in per:
    if isinstance(p, dict) and p.get("id"):
        N2ID[norm(p.get("nome", ""))] = p["id"]
        N2ID.setdefault(norm(p["id"]), p["id"])

# mapeamento manual dos casos que o auto-resolver erra/perde
MANUAL = {
 "armaria": "armeiro_nt",
 "conhecimento de espiritos": "conhecimento_oculto_conhecimento_espiritual",
 "detectar mentiras": "deteccao_de_mentiras",
 "simbologia oculta": "desenho_de_simbolos",
 "historia antiga": "historia",
 "astrologia": None,        # nao existe -> remove (astronomia cobre)
 "historia militar": None,  # nao existe -> remove
 "trato social": "trato_social",   # evita pegar 'trato_social_dojo'
}

def resolver(nome):
    n = norm(nome)
    if n in MANUAL: return MANUAL[n]
    if n in N2ID: return N2ID[n]
    base = {norm(p["id"]) for p in per if isinstance(p, dict)}
    if n + "_nt" in base:
        for p in per:
            if norm(p["id"]) == n + "_nt": return p["id"]
    for p in per:
        if not isinstance(p, dict): continue
        pn = norm(p.get("nome", "")); pb = pn.split("/")[0].split("(")[0].strip()
        if pb == n or pn.startswith(n + "/") or pn.startswith(n + " ("):
            return p["id"]
    for p in per:
        if isinstance(p, dict) and n and n in norm(p.get("nome", "")):
            return p["id"]
    return None

magos = {
 "ilusionista": (["Taumatologia","Ocultismo","Psicologia","Dissimulacao","Labia","Atuacao","Disfarce","Observacao","Linguistica","Escrita"],
                 ["Diplomacia","Trato Social","Detectar Mentiras","Prestidigitacao","Pesquisa","Historia"]),
 "invocador": (["Taumatologia","Ocultismo","Conhecimento de Espiritos","Ritual Religioso","Exorcismo","Meditacao","Simbologia Oculta","Historia","Linguistica","Escrita"],
               ["Diplomacia","Psicologia","Pesquisa","Teologia","Sobrevivencia"]),
 "mago_de_batalha": (["Taumatologia","Estrategia","Tatica","Lideranca","Escudo","Primeiros Socorros","Observacao","Ocultismo","Cavalgar"],
                     ["Intimidacao","Sobrevivencia","Armaria","Soldado","Historia Militar"]),
 "cronomante": (["Taumatologia","Matematica","Astronomia","Fisica","Historia","Historia Antiga","Pesquisa","Linguistica","Escrita","Meditacao"],
                ["Ocultismo","Filosofia","Arqueologia","Simbologia Oculta","Observacao"]),
 "astrologo": (["Astronomia","Astrologia","Taumatologia","Ocultismo","Matematica","Historia","Historia Antiga","Simbologia Oculta","Pesquisa","Linguistica"],
               ["Cartografia","Navegacao","Observacao","Escrita","Filosofia"]),
 "encantador": (["Taumatologia","Psicologia","Diplomacia","Labia","Trato Social","Detectar Mentiras","Lideranca","Simbologia Oculta","Linguistica","Escrita"],
                ["Atuacao","Politica","Pesquisa","Ocultismo","Administracao"]),
 "alquimista_de_guerra": (["Alquimia","Quimica","Taumatologia","Ocultismo","Armaria","Explosivos","Engenharia","Fisica","Pesquisa","Escrita"],
                          ["Primeiros Socorros","Medicina","Veneficio","Ferreiro","Matematica"]),
 "necromante_branco": (["Taumatologia","Ocultismo","Teologia","Ritual Religioso","Exorcismo","Medicina","Primeiros Socorros","Psicologia","Historia","Linguistica"],
                       ["Filosofia","Pesquisa","Diagnose","Simbologia Oculta","Meditacao"]),
 "bruxa_do_pantano": (["Alquimia","Conhecimento das Ervas","Medicina Alternativa","Ocultismo","Taumatologia","Naturalista","Sobrevivencia","Adestramento de Animais","Meteorologia","Veneficio"],
                      ["Rastreamento","Furtividade","Diagnose","Pesca","Navegacao"]),
 "magista_runico": (["Taumatologia","Simbologia Oculta","Linguistica","Escrita","Historia","Historia Antiga","Pesquisa","Ocultismo","Arquitetura","Cartografia"],
                    ["Alquimia","Matematica","Arqueologia","Criptografia","Filosofia"]),
}

# monta pericias por mago (4 pts principais, 2 pts secundarias), sem duplicar id
pericias_por_mago = {}
for key, (prin, sec) in magos.items():
    vistos = set()
    lst = []
    for nome in prin:
        rid = resolver(nome)
        if rid and rid not in vistos and rid in PER_IDS:
            lst.append({"id": rid, "pts": 4}); vistos.add(rid)
    for nome in sec:
        rid = resolver(nome)
        if rid and rid not in vistos and rid in PER_IDS:
            lst.append({"id": rid, "pts": 2}); vistos.add(rid)
    pericias_por_mago[key] = lst

# valida
bad = 0
for key, lst in pericias_por_mago.items():
    for p in lst:
        if p["id"] not in PER_IDS:
            print("RUIM", key, p["id"]); bad += 1
print("IDs invalidos:", bad)
print("pericias por mago:", {k: len(v) for k, v in pericias_por_mago.items()})

if bad == 0:
    f = "forjador_templates.json"
    d = json.load(open(f, encoding="utf-8"))
    aplicados = 0
    for t in d:
        if t["id"] in pericias_por_mago:
            t["pericias"] = pericias_por_mago[t["id"]]  # substitui as base pelas tematicas
            aplicados += 1
    json.dump(d, open(f, "w", encoding="utf-8"), ensure_ascii=False, indent=2)
    print("SALVO: pericias aplicadas em %d magos" % aplicados)
else:
    print("NAO salvei")
