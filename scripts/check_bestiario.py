# -*- coding: utf-8 -*-
# Lote 363 (Saga B6): valida assets/bestiario.v1.json.
# Regras: IDs únicos; campos obrigatórios; dano no formato NdX±Y; tipo de dano PT-BR válido;
# stats numéricos positivos; agressividade/moral em 0-10; pelo menos 1 ataque por criatura.
# Saída: lista de erros + código de saída != 0 se houver erro (padrão dos checks do projeto).
import json, os, re, sys

os.chdir(os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets"))

TIPOS_VALIDOS = {"cont", "corte", "pi-", "pi", "pi+", "pi++", "perf"}
DANO_RE = re.compile(r"^\d+d([+-]\d+)?$")
OBRIGATORIOS = ["id", "nome", "st", "dx", "iq", "ht", "pv", "ataques"]

def main():
    data = json.load(open("bestiario.v1.json", encoding="utf-8"))
    criaturas = data.get("criaturas", [])
    erros = []
    vistos = set()

    if not criaturas:
        erros.append("bestiario vazio (sem 'criaturas').")

    for i, c in enumerate(criaturas):
        rotulo = c.get("id") or f"#{i}"
        for campo in OBRIGATORIOS:
            if campo not in c:
                erros.append(f"[{rotulo}] falta o campo obrigatorio '{campo}'.")

        cid = c.get("id", "")
        if cid in vistos:
            erros.append(f"[{rotulo}] id duplicado.")
        vistos.add(cid)

        for s in ("st", "dx", "iq", "ht", "pv"):
            v = c.get(s)
            if isinstance(v, int) and v <= 0:
                erros.append(f"[{rotulo}] {s} deve ser positivo (tem {v}).")

        for campo in ("agressividade", "moral"):
            v = c.get(campo, 5)
            if not (isinstance(v, int) and 0 <= v <= 10):
                erros.append(f"[{rotulo}] {campo} deve estar entre 0 e 10 (tem {v}).")

        ataques = c.get("ataques", [])
        if not ataques:
            erros.append(f"[{rotulo}] precisa de pelo menos 1 ataque.")
        for a in ataques:
            dano = str(a.get("dano", ""))
            if not DANO_RE.match(dano):
                erros.append(f"[{rotulo}] dano invalido '{dano}' (esperado NdX, NdX+Y ou NdX-Y).")
            tipo = a.get("tipo", "")
            if tipo not in TIPOS_VALIDOS:
                erros.append(f"[{rotulo}] tipo de dano invalido '{tipo}' (validos: {sorted(TIPOS_VALIDOS)}).")
            alc = a.get("alcanceMetros", 1)
            if not (isinstance(alc, int) and alc >= 1):
                erros.append(f"[{rotulo}] alcanceMetros invalido '{alc}'.")

    print("=" * 60)
    print(f"Bestiario: {len(criaturas)} criaturas | IDs unicos: {len(vistos)}")
    if erros:
        print(f"ERROS ({len(erros)}):")
        for e in erros:
            print("  - " + e)
        sys.exit(1)
    print("OK: zero erros.")
    sys.exit(0)

if __name__ == "__main__":
    main()
