#!/usr/bin/env python3
import argparse
import json
import re
import unicodedata
from pathlib import Path
from typing import Dict, List, Optional, Set


IGNORAR_TERMOS_EXATOS = {
    "st", "dx", "ht", "iq", "per", "von", "vontade", "aparar", "bloquear"
}


def norm(value: str) -> str:
    text = unicodedata.normalize("NFD", value or "")
    text = "".join(ch for ch in text if unicodedata.category(ch) != "Mn")
    text = text.lower().replace("-", " ")
    text = re.sub(r"[^a-z0-9\s/+_]", " ", text)
    text = re.sub(r"\s+", " ", text).strip()
    return text


def pericia_terms(pericia: Dict) -> Set[str]:
    base: Set[str] = set()
    nome = norm(pericia.get("nome", ""))
    if nome:
        base.add(nome)

    esp = norm(pericia.get("especializacao", ""))
    if esp:
        base.add(esp)
        if nome:
            base.add(f"{nome} {esp}")
            base.add(f"{nome} ({esp})")

    if "carate" in nome:
        base.add("karate")
    if "judo" in nome:
        base.add("judô")
    if "luta greco romana" in nome:
        base.update({"luta greco romana", "luta-greco romana", "wrestling"})
    if "armas de fogo" in nome:
        base.update({"armas de fogo", "arma de fogo"})
    if "arcos" in nome:
        base.add("arco")

    return {norm(t) for t in base if norm(t)}


def pericia_has_any(pericia: Dict, keys: List[str]) -> bool:
    terms = pericia_terms(pericia)
    return any(any(k in t for k in keys) for t in terms)


def pericia_is_unarmed(pericia: Dict) -> bool:
    return pericia_has_any(pericia, ["briga", "boxe", "carate", "karate", "judo", "sumo", "luta greco romana"])


def pericia_is_melee(pericia: Dict) -> bool:
    keys = [
        "adaga", "espada", "maca", "machado", "chicote", "kusari", "lanca",
        "bastao", "capa", "jitte", "sai", "mangual", "arma de haste", "faca"
    ]
    return pericia_is_unarmed(pericia) or pericia_has_any(pericia, keys)


def pericia_is_fencing(pericia: Dict) -> bool:
    pid = (pericia.get("id", "") or "").strip().lower()
    return pericia_has_any(pericia, ["esgrima"]) or pid in {"adaga_de_esgrima", "rapieira", "sabre"}


def pericia_is_ranged(pericia: Dict) -> bool:
    keys = [
        "armas de fogo", "armas de feixe", "arco", "besta", "funda",
        "arma de arremesso", "arremessador de lanca", "canhoneiro",
        "artilharia", "arma de longo alcance"
    ]
    return pericia_has_any(pericia, keys)


def pericia_is_quick_draw(pericia: Dict) -> bool:
    return pericia_has_any(pericia, ["sacar rapido"])


def pericia_is_combate(pericia: Dict) -> bool:
    return pericia_is_melee(pericia) or pericia_is_ranged(pericia) or pericia_has_any(pericia, ["escudo"])


def termo_eh_nao_avaliavel(term: str, tecnicas_nomes: Set[str]) -> bool:
    if not term or term in IGNORAR_TERMOS_EXATOS:
        return True
    if term in tecnicas_nomes:
        return True
    if len(term) >= 6 and any(term in nome or nome in term for nome in tecnicas_nomes):
        return True
    if term.startswith("tecnica "):
        return True
    if "consulte pag" in term or "consulte pg" in term:
        return True
    if "habitos detestaveis" in term:
        return True
    if "pericia pre requisito" in term:
        return True
    if "outra pericia" in term:
        return True
    return False


def pericia_match_term(pericia: Dict, term_raw: str, tecnicas_nomes: Set[str]) -> Optional[bool]:
    term = norm(term_raw)
    if termo_eh_nao_avaliavel(term, tecnicas_nomes):
        return None

    if "armas de fogo" in term and "pistola" in term:
        # No app, "Armas de Fogo/NT" pode receber especializacao livre.
        # Para auditoria de catalogo, considera coerente se existe a base "Armas de Fogo".
        return pericia_has_any(pericia, ["armas de fogo", "arma de fogo"])
    if "qualquer pericia de combate" in term:
        return pericia_is_combate(pericia)
    if "qualquer pericia" in term and "tiro" in term:
        return pericia_is_ranged(pericia)
    if "qualquer pericia de tiro adequada" in term:
        return pericia_is_ranged(pericia)
    if "qualquer pericia de tiro" in term:
        return pericia_is_ranged(pericia)
    if "arma de longo alcance" in term:
        return pericia_is_ranged(pericia)
    if "qualquer pericia de sacar rapido" in term:
        return pericia_is_quick_draw(pericia)
    if "agarrar desarmado" in term:
        return pericia_is_unarmed(pericia)
    if "combate desarmado" in term:
        return pericia_is_unarmed(pericia)
    if "qualquer pericia com arma de esgrima" in term or "arma de esgrima" in term:
        return pericia_is_fencing(pericia)
    if "arma de combate corpo a corpo" in term:
        return pericia_is_melee(pericia)
    if "qualquer pericia com arma" in term:
        return pericia_is_melee(pericia) or pericia_is_ranged(pericia)
    if "defesa ativa" in term or "bloquear ou aparar" in term:
        return pericia_is_melee(pericia) or pericia_is_unarmed(pericia) or pericia_has_any(pericia, ["escudo"])
    if "apropriada" in term and "arma" in term:
        return pericia_is_melee(pericia) or pericia_is_ranged(pericia)

    terms = pericia_terms(pericia)
    return any((pt in term or term in pt) for pt in terms)


def extrair_termos_principais(prereq_raw: str) -> List[str]:
    principal = (prereq_raw or "").split(";")[0]
    principal_n = norm(principal)
    if not principal_n:
        return []

    termos = [
        t.strip()
        for t in principal_n.replace(" ou ", ",").replace(" e ", ",").split(",")
        if t.strip()
    ]

    # remove prefixos comuns que só poluem matching literal
    limpa = []
    for t in termos:
        t2 = re.sub(r"^(pre requisito|prerequisito)\s*", "", t).strip()
        t2 = re.sub(r"^(ou|e)\s+", "", t2).strip()
        if t2:
            limpa.append(t2)
    return limpa


def main() -> None:
    parser = argparse.ArgumentParser(description="Valida coerencia de prerequisito das tecnicas.")
    parser.add_argument("--assets-dir", default="app/src/main/assets")
    parser.add_argument("--report-out", default="scripts/reports/tecnicas_prerequisitos_report.json")
    args = parser.parse_args()

    assets = Path(args.assets_dir)
    tecs = json.loads((assets / "tecnicas.v1.json").read_text(encoding="utf-8"))["items"]
    pericias = json.loads((assets / "pericias.json").read_text(encoding="utf-8"))
    pericias_supl = json.loads((assets / "pericias_artes_marciais.v1.json").read_text(encoding="utf-8"))["items"]
    skills = [{"id": p.get("id", ""), "nome": p.get("nome", ""), "especializacao": p.get("especializacao", "")} for p in (pericias + pericias_supl)]

    tecnicas_nomes = {
        norm(t.get("nome", ""))
        for t in tecs
        if norm(t.get("nome", ""))
    }

    incoerentes = []
    revisao_manual = []

    for t in tecs:
        raw = (t.get("preRequisitoRaw") or "").strip()
        if not raw or raw == "-":
            continue

        termos = extrair_termos_principais(raw)
        if not termos:
            continue

        # Técnica é incoerente se existir termo avaliável e nenhum skill casar com os termos avaliáveis.
        has_evaluable_term = False
        matched_skills = []
        for skill in skills:
            evals = [pericia_match_term(skill, termo, tecnicas_nomes) for termo in termos]
            evals_filtrados = [e for e in evals if e is not None]
            if evals_filtrados:
                has_evaluable_term = True
            if evals_filtrados and any(evals_filtrados):
                matched_skills.append(skill.get("nome", ""))

        tecnicas_citadas = [termo for termo in termos if termo in tecnicas_nomes]
        termos_avaliaveis = [termo for termo in termos if not termo_eh_nao_avaliavel(termo, tecnicas_nomes)]

        if tecnicas_citadas and not termos_avaliaveis:
            revisao_manual.append(
                {
                    "tecnica": t.get("nome", ""),
                    "preRequisitoRaw": raw,
                    "termosAvaliados": termos,
                    "status": "depende_de_pre_requisito_outra_tecnica",
                }
            )
            continue

        if has_evaluable_term and not matched_skills:
            incoerentes.append(
                {
                    "tecnica": t.get("nome", ""),
                    "preRequisitoRaw": raw,
                    "termosAvaliados": termos,
                    "status": "sem_pericia_compativel_no_catalogo",
                }
            )

    report = {
        "summary": {
            "totalTecnicas": len(tecs),
            "incoerentes": len(incoerentes),
            "revisaoManual": len(revisao_manual),
        },
        "incoerentes": incoerentes,
        "revisaoManual": revisao_manual,
    }

    report_path = Path(args.report_out)
    report_path.parent.mkdir(parents=True, exist_ok=True)
    report_path.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    print("=== Validacao de Pre-requisitos de Tecnicas ===")
    print(f"Tecnicas totais: {len(tecs)}")
    print(f"Incoerentes: {len(incoerentes)}")
    print(f"Revisao manual: {len(revisao_manual)}")
    print(f"Relatorio: {report_path}")
    for item in incoerentes[:120]:
        print(f"- {item['tecnica']}: {item['preRequisitoRaw']}")


if __name__ == "__main__":
    main()
