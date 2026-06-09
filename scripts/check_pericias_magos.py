# -*- coding: utf-8 -*-
import json, os, unicodedata
os.chdir(os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets"))

def norm(s):
    return unicodedata.normalize("NFD", str(s)).encode("ascii", "ignore").decode().lower().strip()

def load(f):
    d = json.load(open(f, encoding="utf-8"))
    return d if isinstance(d, list) else d.get("items", [])

per = load("pericias.json") + load("pericias_artes_marciais.v1.json")
# mapa nome-normalizado -> id  e  id-normalizado -> id
N2ID = {}
for p in per:
    if isinstance(p, dict) and p.get("id"):
        N2ID[norm(p.get("nome", ""))] = p["id"]
        N2ID.setdefault(norm(p["id"]), p["id"])

def resolver(nome):
    """Tenta achar o ID de uma pericia pelo nome. Retorna id ou None."""
    n = norm(nome)
    # remove sufixo /NT que o nome do usuario nao tem
    if n in N2ID: return N2ID[n]
    # tenta com _nt
    if n + "_nt" in {norm(p["id"]) for p in per if isinstance(p, dict)}:
        for p in per:
            if norm(p["id"]) == n + "_nt": return p["id"]
    # tenta match parcial (nome do catalogo comeca com o termo, ignorando /NT e (esp))
    cands = []
    for p in per:
        if not isinstance(p, dict): continue
        pn = norm(p.get("nome", ""))
        pn_base = pn.split("/")[0].split("(")[0].strip()
        if pn_base == n or pn.startswith(n + "/") or pn.startswith(n + " ("):
            cands.append(p["id"])
    if cands: return cands[0]
    # match por conteudo (termo dentro do nome)
    for p in per:
        if not isinstance(p, dict): continue
        if n and n in norm(p.get("nome", "")):
            cands.append(p["id"])
    return cands[0] if cands else None

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

resultado = {}
faltando_global = set()
for key, (prin, sec) in magos.items():
    out = []
    miss = []
    for nome in prin + sec:
        rid = resolver(nome)
        if rid:
            out.append((nome, rid))
        else:
            miss.append(nome)
            faltando_global.add(nome)
    resultado[key] = (out, miss)
    print("\n[%s]" % key)
    print("  OK (%d): %s" % (len(out), ", ".join(r[1] for r in out)))
    if miss:
        print("  NAO ACHADAS (%d): %s" % (len(miss), miss))

print("\n" + "=" * 60)
print("PERICIAS que nao existem no catalogo (unicas):")
for n in sorted(faltando_global):
    print("  -", n)
