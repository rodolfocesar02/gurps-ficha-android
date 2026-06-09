# -*- coding: utf-8 -*-
import json, os
os.chdir(os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets"))

def load(f):
    d = json.load(open(f, encoding="utf-8"))
    return d if isinstance(d, list) else d.get("items", [])

def ids(*files):
    s = set()
    for f in files:
        for i in load(f):
            if isinstance(i, dict) and i.get("id"):
                s.add(str(i["id"]))
    return s

VAN = ids("vantagens.v3.json", "vantagens_artes_marciais.v1.json")
DES = ids("desvantagens.v2.json")
PER = ids("pericias.json", "pericias_artes_marciais.v1.json")
TEC = ids("tecnicas.v1.json")
MAG = ids("magias2versao.json")

# listas de magias (premonicao e doenca removidas - nao existem no catalogo)
M = {
 "ilusionista": ["ilusao_simples","ilusao_complexa","ilusao_perfeita","alucinacao","alucinacao_superior","anular_ilusao","controle_de_ilusao","detectar_ilusao","cobertura_ilusoria","disfarce_ilusorio","aura_falsa","memoria_falsa","esquecimento","esquecimento_permanente","doppelganger","imitar_voz","alterar_voz","alterar_feicoes","invisibilidade","escamotear_informacao"],
 "invocador": ["convocar_elemental_ar","convocar_elemental_fogo","convocar_elemental_terra","convocar_elemental_agua","convocar_animal_terra","convocar_animal_ar","convocar_animal_mar","convocar_espirito","convocar_sombra","convocar_demonio","convocar_zumbi","convocacao_planar","criar_elemental_ar","criar_elemental_fogo","criar_elemental_terra","criar_elemental_agua","criar_guerreiro","criar_montaria","criar_servo","criar_animal"],
 "mago_de_batalha": ["bola_de_fogo","bola_de_fogo_explosiva","bola_de_relampagos","jato_de_chamas","jato_de_acido","jato_de_ar","jato_de_luz","chicote_de_relampago","flecha_magica","flecha_magica_rapida","arma_flamejante","arma_de_relampago","arma_penetrante","armadura","armadura_flamejante","armadura_de_relampagos","escudo","escudo_antiprojeteis","fortificar","bravura"],
 "cronomante": ["acelerar_tempo","acelerar","apressar","cadencia","marcha_acelerada","marcha_lenta","intervalo","adiar_magica","baliza_viagem_no_tempo","criar_portal_viagem_no_tempo","hora_certa","historia","historia_antiga","ecos_do_passado","imagens_do_passado","aromas_do_passado","interromper_envelhecimento","envelhecer","memorizar","jornada_rapida"],
 "astrologo": ["adivinhacao","aura","bola_de_cristal","historia","historia_antiga","ecos_do_passado","imagens_do_passado","aromas_do_passado","hora_certa","localizador","localizar_magia","localizar_portal","guia","aerovisao","aquavisao","geovisao","metalovisao","audicao_remota","leitura_da_mente"],
 "encantador": ["encantar","encantamento_temporario","cativar","fascinar","feitico","enfeiticar","controle_de_emocao","controle_de_pessoa","comando","condicionamento","condicionamento_permanente","escravizar","juramento","lealdade","compelir_a_verdade","compelir_a_mentira","eloquencia","loquacidade","fortalecer_vontade","enfraquecer_vontade"],
 "alquimista_de_guerra": ["acido_essencial","bola_de_acido","jato_de_acido","chuva_de_acido","criar_acido","agua_essencial","chama_essencial","ar_essencial","alimento_essencial","gema_de_energia","conceder_energia","compartilhar_energia","energizacao","arma_flamejante","arma_congelante","arma_penetrante","fortificar","fortalecer","cura_superficial","cura_profunda"],
 "necromante_branco": ["afetar_espiritos","espantar_espirito","convocar_espirito","comandar_espirito","aprisionar_espirito","expulsar","fantasma","espectro","descanso_final","cura_superficial","cura_profunda","cura_superior","curar_doencas","cessar_sangramento","cessar_paralisia","aliviar_loucura","compartilhar_vitalidade","conceder_vitalidade","fortalecer","bencao"],
 "bruxa_do_pantano": ["agua_podre","envenenar_alimento","mau_cheiro","gerar_odor","desidratar","maldicao","maleficio","medo","loucura","atrofiar_membro","atrofiar_visao","atrofiar_sentidos","controle_de_planta","criar_planta","curar_planta","falar_com_plantas","enclausuramento_arboreo","corpo_de_lodo","jato_de_lama"],
 "magista_runico": ["amuleto","marca_mistica","boca_magica","alarme","alarme_florestal","guardamagica","escudo_antimagica","contramagica","anular_magica","identificar_magica","deteccao_de_magia","localizar_magia","analisar_magica","conceder_magica","manter_magica","arremessar_magica","apanhar_magica","deslocar_magica","escudo_protetor","guardachuva"],
}

# metadados (atributos IQ-foco + vantagem aptidao_magica + pericias base)
META = {
 "ilusionista":         ("Ilusionista", "Mestre das ilusoes, alucinacoes e enganos mentais.", ["magia","ilusao","ilusionista","enganador","mentalista"]),
 "invocador":           ("Invocador", "Conjura elementais, animais, espiritos e criaturas de outros planos.", ["magia","invocador","conjurador","convocacao","elementais"]),
 "mago_de_batalha":     ("Mago de Batalha", "Especialista em magia ofensiva e protecao no campo de batalha.", ["magia","combate","mago de guerra","ofensivo","destruidor"]),
 "cronomante":          ("Cronomante", "Manipula o tempo: acelera, atrasa e enxerga o passado.", ["magia","tempo","cronomante","temporal"]),
 "astrologo":           ("Astrologo", "Le os astros, adivinha e enxerga a distancia.", ["magia","adivinhacao","astrologo","vidente","oraculo"]),
 "encantador":          ("Encantador", "Domina mentes e emocoes com feiticos de controle.", ["magia","encantamento","encantador","controle mental","sedutor"]),
 "alquimista_de_guerra":("Alquimista de Guerra", "Combina alquimia ofensiva com energia e cura de campo.", ["magia","alquimia","acido","energia","guerra"]),
 "necromante_branco":   ("Necromante Branco", "Lida com espiritos e mortos para curar e libertar, nao para o mal.", ["magia","necromancia","espiritos","cura","sagrado"]),
 "bruxa_do_pantano":    ("Bruxa do Pantano", "Usa venenos, pragas, plantas e maldicoes da natureza sombria.", ["magia","bruxa","pantano","veneno","plantas","maldicao"]),
 "magista_runico":      ("Magista Runico", "Inscreve runas de protecao, deteccao e anti-magia.", ["magia","runas","magista","protecao","anti-magia"]),
}

ATRIBS = {"st":10,"dx":10,"iq":14,"ht":10}
# vantagens/desvantagens/pericias base de um mago (validar antes)
VAN_BASE = "aptidao_magica"
PER_BASE = ["ocultismo","alquimia_nt","pesquisa_nt"]
DES_BASE = ["curiosidade","pobreza_falido"]

# resolve pericias base validas
per_base = [p for p in PER_BASE if p in PER]
des_base = [d for d in DES_BASE if d in DES]
van_ok = VAN_BASE in VAN

novos = []
for key, magias in M.items():
    nome, desc, tags = META[key]
    magias_validas = [m for m in magias if m in MAG]
    t = {
        "id": key, "nome": nome, "descricao": desc + " Mago (precisa de Aptidao Magica).",
        "tags": tags, "pontosBase": 150, "racaId": None,
        "atributos": {"iq": 14, "ht": 10},
        "vantagens": ([{"id": VAN_BASE, "nivel": 2}] if van_ok else []),
        "desvantagens": [{"id": d} for d in des_base[:1]],
        "pericias": [{"id": p, "pts": 2} for p in per_base],
        "magias": magias_validas,
        "variacoes": ["Versao iniciante: menos magias, mais Aptidao Magica",
                      "Versao mestre: adicione magias avancadas da mesma escola",
                      "Combine com outra escola para um mago hibrido"],
    }
    novos.append(t)

# valida
bad = 0
for t in novos:
    for v in t["vantagens"]:
        if v["id"] not in VAN: print("V", t["id"], v["id"]); bad += 1
    for x in t["desvantagens"]:
        if x["id"] not in DES: print("D", t["id"], x["id"]); bad += 1
    for p in t["pericias"]:
        if p["id"] not in PER and p["id"] not in TEC: print("P", t["id"], p["id"]); bad += 1
    for m in t["magias"]:
        if m not in MAG: print("M", t["id"], m); bad += 1

print("IDs invalidos nos magos:", bad)
print("magias por mago:", {t["id"]: len(t["magias"]) for t in novos})
if bad == 0:
    f = "forjador_templates.json"
    d = json.load(open(f, encoding="utf-8"))
    ex = {t["id"] for t in d}
    add = [t for t in novos if t["id"] not in ex]
    d.extend(add)
    json.dump(d, open(f, "w", encoding="utf-8"), ensure_ascii=False, indent=2)
    print("SALVO: +%d magos. Total: %d templates" % (len(add), len(d)))
else:
    print("NAO salvei")
