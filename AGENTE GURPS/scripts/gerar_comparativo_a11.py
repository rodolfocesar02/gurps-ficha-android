#!/usr/bin/env python3
import json
from pathlib import Path


def as_int(value):
    try:
        return int(value)
    except Exception:
        return 0


def main() -> int:
    root = Path(__file__).resolve().parents[2]
    reports_dir = root / "AGENTE GURPS" / "sources" / "processed" / "reports"
    baseline_path = reports_dir / "ingestao_baseline_pre_a11.json"
    current_path = reports_dir / "ingestao_inicial_report.json"
    out_json = reports_dir / "a11_comparativo_report.json"
    out_md = reports_dir / "a11_comparativo_report.md"

    if not baseline_path.exists():
        raise FileNotFoundError(f"Baseline ausente: {baseline_path}")
    if not current_path.exists():
        raise FileNotFoundError(f"Relatorio atual ausente: {current_path}")

    with baseline_path.open("r", encoding="utf-8") as f:
        baseline = json.load(f)
    with current_path.open("r", encoding="utf-8") as f:
        current = json.load(f)

    base_sources = {s.get("id"): s for s in baseline.get("sources", [])}
    cur_sources = {s.get("id"): s for s in current.get("sources", [])}
    all_ids = sorted(set(base_sources.keys()) | set(cur_sources.keys()))

    by_source = []
    for source_id in all_ids:
        b = base_sources.get(source_id, {})
        c = cur_sources.get(source_id, {})
        pages_before = as_int(b.get("paginas_lidas"))
        pages_after = as_int(c.get("paginas_lidas"))
        suspects_before = as_int(b.get("suspeitas"))
        suspects_after = as_int(c.get("suspeitas"))
        by_source.append(
            {
                "source_id": source_id,
                "titulo": c.get("titulo") or b.get("titulo") or "",
                "paginas_before": pages_before,
                "paginas_after": pages_after,
                "delta_paginas": pages_after - pages_before,
                "suspeitas_before": suspects_before,
                "suspeitas_after": suspects_after,
                "delta_suspeitas": suspects_after - suspects_before,
            }
        )

    by_source.sort(key=lambda x: x["delta_paginas"], reverse=True)

    result = {
        "baseline": {
            "pdfs_processados": as_int(baseline.get("pdfs_processados")),
            "paginas_processadas": as_int(baseline.get("paginas_processadas")),
            "chunks_gerados": as_int(baseline.get("chunks_gerados")),
            "paginas_suspeitas": as_int(baseline.get("paginas_suspeitas")),
        },
        "atual": {
            "pdfs_processados": as_int(current.get("pdfs_processados")),
            "paginas_processadas": as_int(current.get("paginas_processadas")),
            "chunks_gerados": as_int(current.get("chunks_gerados")),
            "paginas_suspeitas": as_int(current.get("paginas_suspeitas")),
        },
        "delta_global": {
            "paginas_processadas": as_int(current.get("paginas_processadas")) - as_int(baseline.get("paginas_processadas")),
            "chunks_gerados": as_int(current.get("chunks_gerados")) - as_int(baseline.get("chunks_gerados")),
            "paginas_suspeitas": as_int(current.get("paginas_suspeitas")) - as_int(baseline.get("paginas_suspeitas")),
        },
        "comparativo_por_fonte": by_source,
    }

    with out_json.open("w", encoding="utf-8", newline="\n") as f:
        json.dump(result, f, ensure_ascii=False, indent=2)

    with out_md.open("w", encoding="utf-8", newline="\n") as f:
        f.write("# A11 - Comparativo Antes/Depois\n\n")
        f.write("## Global\n\n")
        f.write(f"- Páginas: {result['baseline']['paginas_processadas']} -> {result['atual']['paginas_processadas']} (delta {result['delta_global']['paginas_processadas']})\n")
        f.write(f"- Chunks: {result['baseline']['chunks_gerados']} -> {result['atual']['chunks_gerados']} (delta {result['delta_global']['chunks_gerados']})\n")
        f.write(f"- Suspeitas: {result['baseline']['paginas_suspeitas']} -> {result['atual']['paginas_suspeitas']} (delta {result['delta_global']['paginas_suspeitas']})\n\n")
        f.write("## Cobertura por Fonte\n\n")
        f.write("| source_id | páginas antes | páginas depois | delta |\n")
        f.write("|---|---:|---:|---:|\n")
        for row in by_source:
            f.write(f"| {row['source_id']} | {row['paginas_before']} | {row['paginas_after']} | {row['delta_paginas']} |\n")

    print(f"OK: comparativo gerado em {out_json}")
    print(
        json.dumps(
            {
                "paginas_before": result["baseline"]["paginas_processadas"],
                "paginas_after": result["atual"]["paginas_processadas"],
                "chunks_before": result["baseline"]["chunks_gerados"],
                "chunks_after": result["atual"]["chunks_gerados"],
            },
            ensure_ascii=False,
        )
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
