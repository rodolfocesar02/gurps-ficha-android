#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import re
import unicodedata
from dataclasses import dataclass
from difflib import SequenceMatcher
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple

STOP = {"de", "do", "da", "dos", "das", "e", "ou", "a", "o"}


def norm_text(value: str) -> str:
    txt = unicodedata.normalize("NFD", str(value))
    txt = "".join(ch for ch in txt if unicodedata.category(ch) != "Mn")
    txt = txt.replace("†", " ").replace("*", " ")
    txt = re.sub(r"\([^)]*\)", " ", txt)
    txt = txt.replace("/NT", " ").replace("/nt", " ").replace("/", " ")
    txt = re.sub(r"[^a-zA-Z0-9 ]+", " ", txt)
    txt = re.sub(r"\s+", " ", txt).strip().lower()
    return txt


def tokens(value: str) -> List[str]:
    out: List[str] = []
    for t in norm_text(value).split():
        if t in STOP:
            continue
        if len(t) > 4 and t.endswith("s"):
            t = t[:-1]
        out.append(t)
    return out


def score_name(a: str, b: str) -> float:
    ta = tokens(a)
    tb = tokens(b)
    sa = " ".join(ta)
    sb = " ".join(tb)
    if not sa or not sb:
        return 0.0
    ratio = SequenceMatcher(None, sa, sb).ratio()
    set_a, set_b = set(ta), set(tb)
    jac = len(set_a & set_b) / max(1, len(set_a | set_b))
    return ratio * 0.7 + jac * 0.3


@dataclass
class Donor:
    source: str
    nome: str
    descricao: str
    pre_req: str = "-"
    predef: str = "-"
    mods: str = ""


def infer_tipo(pericia: Dict[str, Any]) -> Dict[str, Any]:
    attr = str(pericia.get("atributoBase") or "IQ").strip().upper() or "IQ"
    diff = str(pericia.get("dificuldadeFixa") or "M").strip().upper() or "M"
    return {
        "attributeMode": "fixed",
        "attributeOptions": [attr],
        "difficultyMode": "fixed",
        "difficulty": diff,
    }


def generic_description(nome: str) -> str:
    n = norm_text(nome)
    if any(k in n for k in ["espada", "faca", "sabre", "rapieira", "lanca", "mangual", "machado", "bastao", "chicote", "arco", "arma"]):
        return f"Treinamento no uso de {nome} em combate, incluindo manuseio, ataques e defesas compatíveis com a arma."
    if "traje" in n:
        return f"Habilidade de vestir, operar e se movimentar com {nome}, reduzindo erros de operação em campo."
    if "operacao de aparelhos eletronicos" in n:
        return f"Uso técnico de {nome}, cobrindo procedimentos operacionais, configuração e rotina segura de trabalho."
    if "conserto de equipamentos eletronicos" in n:
        return f"Diagnóstico e reparo ligados a {nome}, incluindo manutenção corretiva e ajustes de funcionamento."
    if "instrumento musical" in n:
        return "Execução e domínio técnico de instrumento musical, com foco em desempenho e interpretação."
    if "cavalgar" in n:
        return "Perícia para montar e controlar montarias em deslocamento, manobras e situações de risco."
    return f"Conhecimento prático de {nome}, aplicado em testes da perícia conforme o contexto de jogo."


def main() -> None:
    parser = argparse.ArgumentParser(description="Completa regras/descrições faltantes no pericias_v2_rules_map.json")
    parser.add_argument("--pericias", required=True, type=Path)
    parser.add_argument("--rules", required=True, type=Path)
    parser.add_argument("--suplementar", required=True, type=Path)
    parser.add_argument("--report", required=True, type=Path)
    args = parser.parse_args()

    pericias: List[Dict[str, Any]] = json.loads(args.pericias.read_text(encoding="utf-8"))
    rules_root = json.loads(args.rules.read_text(encoding="utf-8"))
    rules_items: List[Dict[str, Any]] = rules_root.get("items", [])
    sup_root = json.loads(args.suplementar.read_text(encoding="utf-8"))
    sup_items: List[Dict[str, Any]] = sup_root.get("items", [])

    by_id = {str(x.get("id")): x for x in rules_items if str(x.get("id", "")).strip()}
    by_norm_name: Dict[str, Donor] = {}
    donors: List[Donor] = []

    for r in rules_items:
        nome = str(r.get("nome", "")).strip()
        desc = str(r.get("descricao", "")).strip()
        if not nome or not desc:
            continue
        donor = Donor(
            source="rules",
            nome=nome,
            descricao=desc,
            pre_req=str((r.get("preRequisito") or {}).get("raw") or "-").strip() or "-",
            predef=str((r.get("preDefinido") or {}).get("raw") or "-").strip() or "-",
            mods=str((r.get("modificadores") or {}).get("raw") or "").strip(),
        )
        donors.append(donor)
        by_norm_name.setdefault(norm_text(nome), donor)

    for s in sup_items:
        nome = str(s.get("nome", "")).strip()
        desc = str(s.get("descricao", "")).strip()
        if not nome or not desc:
            continue
        donor = Donor(source="suplementar", nome=nome, descricao=desc)
        donors.append(donor)
        by_norm_name.setdefault(norm_text(nome), donor)

    appended: List[Dict[str, Any]] = []
    stats = {
        "total_pericias": len(pericias),
        "rules_before": len(rules_items),
        "missing_ids": 0,
        "added": 0,
        "source_exact": 0,
        "source_fuzzy": 0,
        "source_generic": 0,
        "still_missing_after": 0,
        "samples_generic": [],
    }

    for p in pericias:
        pid = str(p.get("id", "")).strip()
        nome = str(p.get("nome", "")).strip()
        if not pid or pid in by_id:
            continue

        stats["missing_ids"] += 1
        donor: Optional[Donor] = None

        n = norm_text(nome)
        if n in by_norm_name:
            donor = by_norm_name[n]
            stats["source_exact"] += 1
        else:
            best_score = 0.0
            best: Optional[Donor] = None
            for cand in donors:
                s = score_name(nome, cand.nome)
                if s > best_score:
                    best_score = s
                    best = cand
            if best is not None and best_score >= 0.72:
                donor = best
                stats["source_fuzzy"] += 1

        if donor is None:
            desc = generic_description(nome)
            pre_req = "-"
            predef = "-"
            mods = ""
            stats["source_generic"] += 1
            if len(stats["samples_generic"]) < 25:
                stats["samples_generic"].append({"id": pid, "nome": nome})
        else:
            desc = donor.descricao
            pre_req = donor.pre_req
            predef = donor.predef
            mods = donor.mods

        new_item = {
            "id": pid,
            "nome": nome,
            "tipo": infer_tipo(p),
            "preRequisito": {
                "raw": pre_req if pre_req else "-",
                "allowWithoutPrerequisite": True,
                "logic": {"and": []},
            },
            "preDefinido": {
                "raw": predef if predef else "-",
                "onZeroPoints": "-",
                "parsed": [],
            },
            "descricao": desc,
            "modificadores": {
                "raw": mods,
            },
        }

        rules_items.append(new_item)
        by_id[pid] = new_item
        appended.append(new_item)
        stats["added"] += 1

    # sort for deterministic output
    rules_items.sort(key=lambda x: norm_text(str(x.get("nome", ""))))
    rules_root["items"] = rules_items
    rules_root["totalItems"] = len(rules_items)

    # coverage after
    still = [p for p in pericias if str(p.get("id", "")).strip() not in by_id]
    stats["still_missing_after"] = len(still)

    args.rules.write_text(json.dumps(rules_root, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    args.report.parent.mkdir(parents=True, exist_ok=True)
    args.report.write_text(json.dumps(stats, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(stats, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
