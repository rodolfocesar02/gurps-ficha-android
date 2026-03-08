#!/usr/bin/env python3
import json
from pathlib import Path


def is_accepted_frontmatter(item: dict) -> bool:
    page_number = int(item.get("page_number", 0))
    layout = (item.get("layout") or "").strip().lower()
    used_ocr = bool(item.get("used_ocr"))
    text_length = int(item.get("text_length", 0))
    # Regra operacional: capas e páginas de abertura (sumário/editorial)
    # costumam ser OCR fraco, layout "vazio" e pouco texto útil.
    return page_number <= 2 and layout == "vazio" and used_ocr and text_length < 220


def main() -> int:
    root = Path(__file__).resolve().parents[2]
    detailed_path = root / "AGENTE GURPS" / "sources" / "processed" / "reports" / "paginas_suspeitas_detalhado.json"
    out_json = root / "AGENTE GURPS" / "sources" / "processed" / "reports" / "suspeitas_operacionais_report.json"
    out_md = root / "AGENTE GURPS" / "sources" / "processed" / "reports" / "suspeitas_operacionais_report.md"

    if not detailed_path.exists():
        raise FileNotFoundError(f"Arquivo ausente: {detailed_path}")

    with detailed_path.open("r", encoding="utf-8") as f:
        data = json.load(f)

    itens = data.get("itens", [])
    accepted = []
    actionable = []

    for item in itens:
        row = {
            "page_id": item.get("page_id"),
            "source_id": item.get("source_id"),
            "source_title": item.get("source_title"),
            "page_number": int(item.get("page_number", 0)),
            "layout": item.get("layout"),
            "used_ocr": bool(item.get("used_ocr")),
            "text_length": int(item.get("text_length", 0)),
        }
        if is_accepted_frontmatter(row):
            row["classificacao"] = "aceitas_contexto"
            row["motivo"] = "pagina de abertura/capa com OCR residual"
            accepted.append(row)
        else:
            row["classificacao"] = "acionaveis"
            row["motivo"] = "fora da regra de aceite para abertura"
            actionable.append(row)

    accepted.sort(key=lambda x: (x["source_id"], x["page_number"]))
    actionable.sort(key=lambda x: (x["source_id"], x["page_number"]))

    result = {
        "total_suspeitas_entrada": len(itens),
        "aceitas_contexto": len(accepted),
        "acionaveis": len(actionable),
        "criterio_aceite": "page_number<=2 and layout='vazio' and used_ocr=true and text_length<220",
        "aceitas_contexto_itens": accepted,
        "acionaveis_itens": actionable,
    }

    out_json.parent.mkdir(parents=True, exist_ok=True)
    with out_json.open("w", encoding="utf-8", newline="\n") as f:
        json.dump(result, f, ensure_ascii=False, indent=2)

    with out_md.open("w", encoding="utf-8", newline="\n") as f:
        f.write("# Relatório de Suspeitas Operacionais\n\n")
        f.write(f"- Total de entrada: **{result['total_suspeitas_entrada']}**\n")
        f.write(f"- Aceitas por contexto (capa/sumário): **{result['aceitas_contexto']}**\n")
        f.write(f"- Acionáveis: **{result['acionaveis']}**\n")
        f.write(f"- Critério: `{result['criterio_aceite']}`\n\n")

        f.write("## Aceitas por Contexto\n\n")
        if accepted:
            f.write("| source_id | página | layout | OCR | tamanho | motivo |\n")
            f.write("|---|---:|---|---|---:|---|\n")
            for item in accepted:
                f.write(
                    f"| {item['source_id']} | {item['page_number']} | {item['layout']} | "
                    f"{'sim' if item['used_ocr'] else 'nao'} | {item['text_length']} | {item['motivo']} |\n"
                )
        else:
            f.write("_Nenhuma._\n")

        f.write("\n## Acionáveis\n\n")
        if actionable:
            f.write("| source_id | página | layout | OCR | tamanho | motivo |\n")
            f.write("|---|---:|---|---|---:|---|\n")
            for item in actionable:
                f.write(
                    f"| {item['source_id']} | {item['page_number']} | {item['layout']} | "
                    f"{'sim' if item['used_ocr'] else 'nao'} | {item['text_length']} | {item['motivo']} |\n"
                )
        else:
            f.write("_Nenhuma._\n")

    print(f"OK: relatório operacional gerado em {out_json}")
    print(
        json.dumps(
            {
                "total_suspeitas_entrada": result["total_suspeitas_entrada"],
                "aceitas_contexto": result["aceitas_contexto"],
                "acionaveis": result["acionaveis"],
            },
            ensure_ascii=False,
        )
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
