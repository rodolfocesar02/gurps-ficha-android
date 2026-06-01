"""
Gera amostras de áudio de todas as vozes disponíveis no Gemini 2.5 Live.
Frase: "Olá Rodolfo, o que posso te ajudar hoje?"

Uso:
  pip install google-generativeai
  python gerar_amostras.py --key SUA_API_KEY

Os arquivos .wav são salvos nesta pasta.
"""

import argparse
import base64
import json
import sys
import wave
import urllib.request
import urllib.error
import os

# Força UTF-8 no stdout do Windows
if sys.stdout.encoding != "utf-8":
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")

VOZES = [
    "Zephyr", "Autonoe", "Kore", "Orus", "Umbriel", "Callirrhoe",
    "Erinome", "Iapetus", "Laomedeia", "Schedar", "Achird", "Sadachbia",
    "Puck", "Fenrir", "Aoede", "Enceladus", "Algieba", "Algenib",
    "Achernar", "Gacrux", "Zubenelgenubi", "Sadaltager", "Charon",
    "Leda", "Despina", "Rasalgethi", "Alnilam", "Pulcherrima",
    "Vindemiatrix", "Sulafat",
]

FRASE = "Olá Rodolfo, o que posso te ajudar hoje?"
MODELO = "gemini-2.5-flash-preview-tts"
SAMPLE_RATE = 24000


def pcm_para_wav(pcm: bytes, caminho: str):
    with wave.open(caminho, "wb") as wf:
        wf.setnchannels(1)
        wf.setsampwidth(2)  # 16-bit
        wf.setframerate(SAMPLE_RATE)
        wf.writeframes(pcm)


def gerar_voz(voz: str, api_key: str) -> bytes | None:
    url = f"https://generativelanguage.googleapis.com/v1beta/models/{MODELO}:generateContent?key={api_key}"
    payload = {
        "contents": [{"parts": [{"text": FRASE}]}],
        "generationConfig": {
            "responseModalities": ["AUDIO"],
            "speechConfig": {
                "voiceConfig": {
                    "prebuiltVoiceConfig": {"voiceName": voz}
                }
            },
        },
    }
    data = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(url, data=data, headers={"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            body = json.loads(resp.read())
        audio_b64 = (
            body["candidates"][0]["content"]["parts"][0]["inlineData"]["data"]
        )
        return base64.b64decode(audio_b64)
    except urllib.error.HTTPError as e:
        print(f"  ✗ HTTP {e.code}: {e.read().decode()[:200]}")
        return None
    except Exception as e:
        print(f"  ✗ Erro: {e}")
        return None


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--key", required=True, help="Gemini API key")
    args = parser.parse_args()

    pasta = os.path.dirname(os.path.abspath(__file__))
    print(f"Gerando {len(VOZES)} amostras de voz...\n")

    ok = 0
    for voz in VOZES:
        print(f"[{voz}]", end=" ", flush=True)
        pcm = gerar_voz(voz, args.key)
        if pcm:
            caminho = os.path.join(pasta, f"{voz}.wav")
            pcm_para_wav(pcm, caminho)
            print(f"✓ salvo ({len(pcm):,} bytes)")
            ok += 1
        import time; time.sleep(2.0)

    print(f"\n✅ {ok}/{len(VOZES)} vozes geradas com sucesso.")
    print(f"Arquivos em: {pasta}")


if __name__ == "__main__":
    main()
