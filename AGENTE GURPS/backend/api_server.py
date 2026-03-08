#!/usr/bin/env python3
from __future__ import annotations

from typing import Literal, Optional

from fastapi import FastAPI
from pydantic import BaseModel, Field

from rag_runtime import (
    answer_with_citations,
    build_low_confidence_answer,
    ensure_collection_ready,
    evaluate_evidence,
    load_settings,
    retrieve_context,
)


class AskRequest(BaseModel):
    question: str = Field(min_length=3, max_length=2000)
    mode: Literal["regras", "criacao", "lore"] = "regras"
    top_k: Optional[int] = Field(default=None, ge=1, le=20)


class SourceItem(BaseModel):
    source_id: str
    source_title: str
    page_number: int
    chunk_id: str
    score: float


class AskResponse(BaseModel):
    answer: str
    confidence: str
    mode: str
    sources: list[SourceItem]


app = FastAPI(title="AGENTE GURPS API", version="0.1.0")


@app.on_event("startup")
def startup_prepare_index():
    settings = load_settings()
    app.state.rag_status = ensure_collection_ready(settings)


@app.get("/health")
def health():
    settings = load_settings()
    rag_status = getattr(app.state, "rag_status", {})
    return {
        "status": "ok",
        "collection": settings.collection_name,
        "chroma_dir": str(settings.chroma_dir),
        "has_openai_key": bool(settings.openai_api_key),
        "rag_ready": bool(rag_status.get("ready", False)),
        "indexed_chunks": int(rag_status.get("count", 0)),
    }


@app.post("/ask", response_model=AskResponse)
def ask(payload: AskRequest):
    settings = load_settings()
    ctx = retrieve_context(settings, payload.question, payload.top_k)
    items = ctx["items"]
    evidence = evaluate_evidence(payload.question, items)

    confidence = "baixa"
    if evidence["enough"]:
        if evidence["best_score"] >= 0.55 or evidence["max_overlap"] >= 4:
            confidence = "alta"
        else:
            confidence = "media"

    if not evidence["enough"]:
        answer = build_low_confidence_answer(items)
    else:
        answer = answer_with_citations(settings, payload.question, payload.mode, items)
    sources = [
        SourceItem(
            source_id=item["source_id"],
            source_title=item["source_title"],
            page_number=item["page_number"],
            chunk_id=item["chunk_id"],
            score=item["score"],
        )
        for item in items
    ]

    return AskResponse(
        answer=answer,
        confidence=confidence,
        mode=payload.mode,
        sources=sources,
    )
