"""
Mapa de toque da silhueta do botão PV — Lote PV-1a.

Este arquivo é a FONTE do mapa. Ele mede a arte de verdade (`silhueta1.png`),
aplica as regras de recorte e desenha o resultado por cima da arte para
conferência humana. Os números que ele imprime são os mesmos que vão para o
Kotlin — se alguém mudar a arte, roda de novo e compara.

⚠️ O mapa NÃO é um monte de polígonos desenhados no olho. São ~12 números
(linhas de corte + dois limites de braço) aplicados sobre a máscara do corpo.
É por isso que dá para conferir: o mesmo cálculo roda aqui e no app.

    python mapa_silhueta.py
"""
from PIL import Image
import numpy as np
from collections import deque
import hashlib
import io
import os
import sys

sys.stdout.reconfigure(encoding="utf-8")
AQUI = os.path.dirname(os.path.abspath(__file__))


def carregar_corpo(nome="silhueta1.png"):
    """A máscara do corpo: interior + traço. O fundo é transparente, então o
    interior também é — a única saída é inundar de fora para dentro."""
    a = np.array(Image.open(os.path.join(AQUI, nome)).convert("RGBA"))
    rgb = a[..., :3].astype(int).sum(axis=2)
    tinta = (a[..., 3] > 128) & (rgb < 350)
    H, W = tinta.shape
    livre = ~tinta
    fora = np.zeros_like(livre)
    q = deque()
    for x in range(W):
        for y in (0, H - 1):
            if livre[y, x] and not fora[y, x]:
                fora[y, x] = True
                q.append((y, x))
    for y in range(H):
        for x in (0, W - 1):
            if livre[y, x] and not fora[y, x]:
                fora[y, x] = True
                q.append((y, x))
    while q:
        y, x = q.popleft()
        for dy, dx in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            ny, nx = y + dy, x + dx
            if 0 <= ny < H and 0 <= nx < W and livre[ny, nx] and not fora[ny, nx]:
                fora[ny, nx] = True
                q.append((ny, nx))
    return ~fora


# ── As linhas de corte, todas medidas na arte (591 × 1555) ────────────────
TESTA = 82        # sobrancelhas em y 85 — acima disso é crânio
QUEIXO = 192      # ⚠️ NÃO é o ponto mais estreito (176): neste desenho o
                  # maxilar tem quase a largura do pescoço, e cortar no mínimo
                  # jogava a BOCA (y 168) para dentro do pescoço.
PESCOCO_FIM = 244  # ⚠️ os ombros abrem em 235, mas cortar ali deixava o pescoço
                  # com 44,7 dp de altura — abaixo do mínimo de toque de 48.
AXILA = 448        # primeira linha em que o braço se separa do tronco
PUNHO = 758        # o braço volta a alargar: começa a mão
VIRILHA_TOPO = 690
VIRILHA_FIM = 789  # a silhueta se parte em duas pernas
MAO_FIM = 884
TORNOZELO = 1387
MEIO = 295         # divisor entre as duas pernas

# ⚠️ LADO É O DO PERSONAGEM, NÃO O DA TELA.
# A figura está de frente para quem olha, então o que aparece à ESQUERDA da
# imagem é o lado DIREITO dele. Decisão do usuário: a ficha registra "braço
# esquerdo" quando for o braço esquerdo DELE.
OLHOS = {"OLHO_D": (251, 278, 99, 110), "OLHO_E": (312, 339, 99, 110)}
FOLGA_OLHO = 16    # o olho é minúsculo; a área de toque cresce em volta dele

VITAIS = (295, 380, 105, 92)    # elipse: coração e pulmões, pela frente
VIRILHA_EL = (295, 748, 118, 68)


def _elipse(x, y, e):
    cx, cy, rx, ry = e
    return ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2 <= 1.0


def limite_lado_direito(y):
    """Onde o braço DIREITO dele termina e o tronco começa — é o que aparece à
    esquerda da imagem.
    Entre o ombro e a axila os dois estão colados no desenho, então a fronteira
    é uma reta; abaixo da axila ela segue o vão real entre os dois."""
    if y < AXILA:
        return 175 + (156 - 175) * (y - PESCOCO_FIM) / (AXILA - PESCOCO_FIM)
    return 156 + (110 - 156) * (y - AXILA) / (829 - AXILA)


def limite_lado_esquerdo(y):
    """O braço ESQUERDO dele, que aparece à direita da imagem."""
    if y < AXILA:
        return 416 + (433 - 416) * (y - PESCOCO_FIM) / (AXILA - PESCOCO_FIM)
    return 433 + (478 - 433) * (y - AXILA) / (829 - AXILA)


def regiao_geo(y, x):
    """A regra inteira, só geometria — é ela que decide o TOQUE.

    ⚠️ De propósito não olha a máscara do corpo. As regiões cobrem o retângulo
    inteiro da tela, então um toque logo ao lado da mão ainda seleciona a mão.
    A mão tem só 44 dp de largura desenhada; exigir acerto no traço deixaria o
    alvo abaixo do mínimo de 48 dp que o app cobra de qualquer botão.
    """
    if y < TESTA:
        return "CRANIO"
    if y < QUEIXO:
        for k, (x0, x1, y0, y1) in OLHOS.items():
            if x0 - FOLGA_OLHO <= x <= x1 + FOLGA_OLHO and y0 - FOLGA_OLHO <= y <= y1 + FOLGA_OLHO:
                return k
        return "ROSTO"
    if y < PESCOCO_FIM:
        return "PESCOCO"
    if y < VIRILHA_FIM:
        if x < limite_lado_direito(y):
            return "MAO_D" if y >= PUNHO else "BRACO_D"
        if x > limite_lado_esquerdo(y):
            return "MAO_E" if y >= PUNHO else "BRACO_E"
        if y >= VIRILHA_TOPO and _elipse(x, y, VIRILHA_EL):
            return "VIRILHA"
        if _elipse(x, y, VITAIS):
            return "VITAIS"
        return "TRONCO"
    if y < MAO_FIM:
        yy = min(y, 829)
        if x < limite_lado_direito(yy):
            return "MAO_D"
        if x > limite_lado_esquerdo(yy):
            return "MAO_E"
    lado = "D" if x < MEIO else "E"
    return ("PE_" if y >= TORNOZELO else "PERNA_") + lado


def regiao(corpo, y, x):
    """A regra para PINTAR o destaque: mesma geometria, recortada no corpo."""
    return regiao_geo(y, x) if corpo[y, x] else None


CORES = {
    "CRANIO": (150, 110, 200), "ROSTO": (120, 150, 220),
    "OLHO_E": (230, 90, 90), "OLHO_D": (230, 150, 60),
    "PESCOCO": (90, 180, 190), "TRONCO": (90, 160, 110),
    "VITAIS": (220, 70, 120), "VIRILHA": (200, 170, 60),
    "BRACO_E": (80, 120, 200), "BRACO_D": (150, 180, 240),
    "MAO_E": (190, 80, 170), "MAO_D": (240, 150, 220),
    "PERNA_E": (70, 160, 180), "PERNA_D": (140, 210, 220),
    "PE_E": (180, 120, 70), "PE_D": (230, 190, 130),
}

# Os três recortes, conferidos por casamento de traço (100% de coincidência).
RECORTES = {
    "cabeca": (156, 0, 435, 258),
    "tronco": (0, 269, 591, 911),
    "pernas": (40, 908, 551, 1555),
}


DESTINO_MASCARA = os.path.join(
    AQUI, "..", "..", "..", "app", "src", "main", "assets", "silhueta_corpo_mascara.txt")


def exportar_mascara(corpo, arte="silhueta1.png"):
    """Grava a máscara do corpo para o TESTE do app poder conferir o mapa.

    ⚠️ O teste roda no ambiente do Android, que **não tem AWT** — ele não
    consegue abrir um PNG. Sem este arquivo, os testes que valem de verdade (o
    lado esquerdo/direito, a ordem anatômica) simplesmente não existiriam.

    O cabeçalho leva o **sha256 da arte**. O teste confere esse hash contra o PNG
    que o app usa: se alguém trocar o desenho e esquecer de rodar esta
    ferramenta, o teste reprova dizendo exatamente isso — em vez de continuar
    verde conferindo um corpo que não existe mais.
    """
    H, W = corpo.shape
    sha = hashlib.sha256(io.open(os.path.join(AQUI, arte), "rb").read()).hexdigest()
    linhas = [
        "# mascara do corpo da silhueta -- GERADA por docs/arte/silhueta/mapa_silhueta.py",
        "# NAO EDITE A MAO. Se a arte mudar, rode a ferramenta de novo.",
        f"arte={arte}",
        f"sha256={sha}",
        f"largura={W}",
        f"altura={H}",
    ]
    for y in range(H):
        faixas, ini = [], None
        for x in range(W):
            if corpo[y, x] and ini is None:
                ini = x
            elif not corpo[y, x] and ini is not None:
                faixas.append(f"{ini}-{x-1}"); ini = None
        if ini is not None:
            faixas.append(f"{ini}-{W-1}")
        if faixas:
            linhas.append(f"{y}:" + ",".join(faixas))
    destino = os.path.normpath(DESTINO_MASCARA)
    io.open(destino, "w", encoding="utf-8", newline=chr(10)).write(chr(10).join(linhas) + chr(10))
    print(f"máscara -> {destino}  ({len(linhas)-6} linhas, sha {sha[:12]}…)")


def main():
    corpo = carregar_corpo()
    H, W = corpo.shape
    exportar_mascara(corpo)
    mapa = np.full((H, W, 3), 255, np.uint8)
    conta = {}
    for y in range(H):
        for x in range(W):
            r = regiao(corpo, y, x)
            if r:
                mapa[y, x] = CORES[r]
                conta[r] = conta.get(r, 0) + 1

    base = Image.open(os.path.join(AQUI, "silhueta1.png")).convert("RGBA")
    fundo = Image.new("RGBA", (W, H), (255, 255, 255, 255))
    fundo.alpha_composite(base)
    over = Image.blend(fundo.convert("RGB"), Image.fromarray(mapa), 0.55)
    over = Image.alpha_composite(over.convert("RGBA"), base).convert("RGB")
    over.resize((W // 3, H // 3), Image.LANCZOS).save(os.path.join(AQUI, "_conferencia_corpo.png"))
    over.crop((150, 0, 440, 300)).resize((580, 600), Image.LANCZOS).save(
        os.path.join(AQUI, "_conferencia_cabeca.png"))
    over.crop((0, 250, 591, 930)).resize((520, 598), Image.LANCZOS).save(
        os.path.join(AQUI, "_conferencia_tronco.png"))

    for k in sorted(conta, key=lambda k: -conta[k]):
        print(f"   {k:9s} {conta[k]:7d} px")
    falta = [k for k in CORES if k not in conta]
    print("\nregiões sem pixel:", falta if falta else "nenhuma")


if __name__ == "__main__":
    main()
