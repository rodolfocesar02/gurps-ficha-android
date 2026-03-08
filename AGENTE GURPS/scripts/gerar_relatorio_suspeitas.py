#!/usr/bin/env python3
import json
from pathlib import Path


def main() -> int:
    root = Path(__file__).resolve().parents[2]
    pages_path = root / "AGENTE GURPS" / "sources" / "processed" / "pages.jsonl"
    main_report_path = root / "AGENTE GURPS" / "sources" / "processed" / "reports" / "ingestao_inicial_report.json"
    out_json = root / "AGENTE GURPS" / "sources" / "processed" / "reports" / "paginas_suspeitas_detalhado.json"
    out_md = root / "AGENTE GURPS" / "sources" / "processed" / "reports" / "paginas_suspeitas_detalhado.md"

    if not pages_path.exists():
        raise FileNotFoundError(f"Arquivo ausente: {pages_path}")
    if not main_report_path.exists():
        raise FileNotFoundError(f"Arquivo ausente: {main_report_path}")

    with main_report_path.open("r", encoding="utf-8") as f:
        main_report = json.load(f)
    baseline = int(main_report.get("paginas_suspeitas", 0))

    suspeitas = []
    with pages_path.open("r", encoding="utf-8") as f:
        for line in f:
            row = json.loads(line)
            text_len = len((row.get("text") or "").strip())
            if text_len < 180:
                suspeitas.append(
                    {
                        "source_id": row.get("source_id"),
                        "source_title": row.get("source_title"),
                        "page_number": row.get("page_number"),
                        "layout": row.get("layout"),
                        "used_ocr": bool(row.get("used_ocr")),
                        "text_length": text_len,
                        "page_id": row.get("page_id"),
                    }
                )

    suspeitas.sort(key=lambda x: (x["source_id"], x["page_number"]))
    result = {
        "total_suspeitas_detalhado": len(suspeitas),
        "baseline_relatorio_principal": baseline,
        "match_baseline": len(suspeitas) == baseline,
        "itens": suspeitas,
    }

    out_json.parent.mkdir(parents=True, exist_ok=True)
    with out_json.open("w", encoding="utf-8", newline="\n") as f:
        json.dump(result, f, ensure_ascii=False, indent=2)

    with out_md.open("w", encoding="utf-8", newline="\n") as f:
        f.write("# Páginas Suspeitas - Detalhado\n\n")
        f.write(f"- Total no detalhado: **{len(suspeitas)}**\n")
        f.write(f"- Baseline no relatório principal: **{baseline}**\n")
        f.write(f"- Match baseline: **{result['match_baseline']}**\n\n")
        f.write("| source_id | página | layout | OCR | tamanho |\n")
        f.write("|---|---:|---|---|---:|\n")
        for item in suspeitas:
            f.write(
                f"| {item['source_id']} | {item['page_number']} | {item['layout']} | "
                f"{'sim' if item['used_ocr'] else 'nao'} | {item['text_length']} |\n"
            )

    print(f"OK: relatório gerado em {out_json}")
    print(f"suspeitas={len(suspeitas)} baseline={baseline} match={result['match_baseline']}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
