# -*- coding: utf-8 -*-
import json, os, unicodedata
os.chdir(os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets"))

def norm(s):
    return unicodedata.normalize("NFD", str(s)).encode("ascii", "ignore").decode().lower()

def load(f):
    d = json.load(open(f, encoding="utf-8"))
    return d if isinstance(d, list) else d.get("items", [])

mags = load("magias2versao.json")
MAG_IDS = set(str(m["id"]) for m in mags if isinstance(m, dict) and m.get("id"))
# mapa nome-normalizado -> id (pra sugerir correcao)
NOME2ID = {}
for m in mags:
    if isinstance(m, dict) and m.get("id"):
        NOME2ID[norm(m.get("nome", ""))] = m["id"]
        NOME2ID[norm(m["id"])] = m["id"]

magos = {
 "ilusionista": ["ilusao_simples","ilusao_complexa","ilusao_perfeita","alucinacao","alucinacao_superior","anular_ilusao","controle_de_ilusao","detectar_ilusao","cobertura_ilusoria","disfarce_ilusorio","aura_falsa","memoria_falsa","esquecimento","esquecimento_permanente","doppelganger","imitar_voz","alterar_voz","alterar_feicoes","invisibilidade","escamotear_informacao"],
 "invocador": ["convocar_elemental_ar","convocar_elemental_fogo","convocar_elemental_terra","convocar_elemental_agua","convocar_animal_terra","convocar_animal_ar","convocar_animal_mar","convocar_espirito","convocar_sombra","convocar_demonio","convocar_zumbi","convocacao_planar","criar_elemental_ar","criar_elemental_fogo","criar_elemental_terra","criar_elemental_agua","criar_guerreiro","criar_montaria","criar_servo","criar_animal"],
 "mago_de_batalha": ["bola_de_fogo","bola_de_fogo_explosiva","bola_de_relampagos","jato_de_chamas","jato_de_acido","jato_de_ar","jato_de_luz","chicote_de_relampago","flecha_magica","flecha_magica_rapida","arma_flamejante","arma_de_relampago","arma_penetrante","armadura","armadura_flamejante","armadura_de_relampagos","escudo","escudo_antiprojeteis","fortificar","bravura"],
 "cronomante": ["acelerar_tempo","acelerar","apressar","cadencia","marcha_acelerada","marcha_lenta","intervalo","adiar_magica","baliza_viagem_no_tempo","criar_portal_viagem_no_tempo","hora_certa","historia","historia_antiga","ecos_do_passado","imagens_do_passado","aromas_do_passado","interromper_envelhecimento","envelhecer","memorizar","jornada_rapida"],
 "astrologo": ["adivinhacao","aura","bola_de_cristal","historia","historia_antiga","ecos_do_passado","imagens_do_passado","aromas_do_passado","hora_certa","localizador","localizar_magia","localizar_portal","guia","aerovisao","aquavisao","geovisao","metalovisao","audicao_remota","leitura_da_mente","premonicao"],
 "encantador": ["encantar","encantamento_temporario","cativar","fascinar","feitico","enfeiticar","controle_de_emocao","controle_de_pessoa","comando","condicionamento","condicionamento_permanente","escravizar","juramento","lealdade","compelir_a_verdade","compelir_a_mentira","eloquencia","loquacidade","fortalecer_vontade","enfraquecer_vontade"],
 "alquimista_de_guerra": ["acido_essencial","bola_de_acido","jato_de_acido","chuva_de_acido","criar_acido","agua_essencial","chama_essencial","ar_essencial","alimento_essencial","gema_de_energia","conceder_energia","compartilhar_energia","energizacao","arma_flamejante","arma_congelante","arma_penetrante","fortificar","fortalecer","cura_superficial","cura_profunda"],
 "necromante_branco": ["afetar_espiritos","espantar_espirito","convocar_espirito","comandar_espirito","aprisionar_espirito","expulsar","fantasma","espectro","descanso_final","cura_superficial","cura_profunda","cura_superior","curar_doencas","cessar_sangramento","cessar_paralisia","aliviar_loucura","compartilhar_vitalidade","conceder_vitalidade","fortalecer","bencao"],
 "bruxa_do_pantano": ["agua_podre","envenenar_alimento","mau_cheiro","gerar_odor","desidratar","doenca","maldicao","maleficio","medo","loucura","atrofiar_membro","atrofiar_visao","atrofiar_sentidos","controle_de_planta","criar_planta","curar_planta","falar_com_plantas","enclausuramento_arboreo","corpo_de_lodo","jato_de_lama"],
 "magista_runico": ["amuleto","marca_mistica","boca_magica","alarme","alarme_florestal","guardamagica","escudo_antimagica","contramagica","anular_magica","identificar_magica","deteccao_de_magia","localizar_magia","analisar_magica","conceder_magica","manter_magica","arremessar_magica","apanhar_magica","deslocar_magica","escudo_protetor","guardachuva"],
}

print("=" * 60)
total_ok = total_bad = 0
fixes = {}
for nome, lista in magos.items():
    ok, bad = [], []
    for mid in lista:
        if mid in MAG_IDS:
            ok.append(mid)
        else:
            # tenta achar por nome normalizado
            sug = NOME2ID.get(norm(mid))
            if sug:
                fixes[mid] = sug
                ok.append(sug)
                bad.append("%s -> (corrigido) %s" % (mid, sug))
            else:
                bad.append(mid)
    total_ok += len([m for m in lista if m in MAG_IDS or norm(m) in NOME2ID])
    print("\n[%s] %d magias: %d OK, %d quebradas" % (nome, len(lista),
          len([m for m in lista if m in MAG_IDS or norm(m) in NOME2ID]),
          len([m for m in lista if m not in MAG_IDS and norm(m) not in NOME2ID])))
    quebradas = [m for m in lista if m not in MAG_IDS and norm(m) not in NOME2ID]
    if quebradas:
        print("   QUEBRADAS (nao existem):", quebradas)
    corrigidas = [b for b in bad if "->" in b]
    if corrigidas:
        print("   corrigidas por nome:", corrigidas)

print("\n" + "=" * 60)
print("Magias com ID exato no catalogo: verificado acima por mago.")
