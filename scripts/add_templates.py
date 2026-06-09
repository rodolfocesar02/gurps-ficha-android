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

novos = [
{"id":"assassino_sombras","nome":"Assassino das Sombras","descricao":"Matador furtivo que ataca pelas sombras com laminas e venenos.","tags":["combate","furtividade","assassino","matador","ninja","espiao","sicario","sombras"],"pontosBase":150,"racaId":None,"atributos":{"st":11,"dx":14,"iq":11,"ht":11},"vantagens":[{"id":"reflexos_em_combate"},{"id":"ambidestria"}],"desvantagens":[{"id":"inimigos"},{"id":"codigo_de_honra"}],"pericias":[{"id":"faca","pts":4},{"id":"furtividade","pts":8},{"id":"disfarce_nt","pts":2},{"id":"farmacia_nt","pts":2},{"id":"arrombamento_nt","pts":2},{"id":"observacao","pts":2},{"id":"acrobacia","pts":2}],"variacoes":["Versao ninja: adicione pericias de escalada e disfarce","Versao envenenador: foque em Farmacia e venenos","Troque Faca por Espada Curta para combate mais aberto"]},
{"id":"espadachim_duelista","nome":"Espadachim Duelista","descricao":"Lutador agil e elegante, mestre da rapieira e do duelo.","tags":["combate","espadachim","duelista","esgrima","mosqueteiro","swashbuckler","rapieira","aventureiro"],"pontosBase":150,"racaId":None,"atributos":{"st":11,"dx":15,"iq":10,"ht":11},"vantagens":[{"id":"reflexos_em_combate"},{"id":"sorte"}],"desvantagens":[{"id":"codigo_de_honra"},{"id":"orgulhoso"}],"pericias":[{"id":"rapieira","pts":8},{"id":"adaga_de_esgrima","pts":4},{"id":"acrobacia","pts":2},{"id":"briga","pts":2},{"id":"intimidacao","pts":2},{"id":"trato_social","pts":2}],"variacoes":["Versao mosqueteiro: adicione Armas de Fogo e Cavalgar","Versao pirata: adicione Manejo de Barcos","Adicione Ambidestria para luta com duas laminas"]},
{"id":"cavaleiro_montado","nome":"Cavaleiro Montado","descricao":"Guerreiro de elite a cavalo, com lanca e armadura pesada.","tags":["combate","cavaleiro","montado","lanceiro","nobre","guerreiro","justa","honra"],"pontosBase":150,"racaId":None,"atributos":{"st":13,"dx":13,"iq":10,"ht":12},"vantagens":[{"id":"reflexos_em_combate"},{"id":"status"}],"desvantagens":[{"id":"codigo_de_honra"},{"id":"senso_do_dever"}],"pericias":[{"id":"lanca","pts":4},{"id":"espada_de_lamina_larga","pts":4},{"id":"escudo","pts":2},{"id":"cavalgar","pts":4},{"id":"tatica","pts":2},{"id":"lideranca","pts":2}],"variacoes":["Troque Lanca por Lanca de Justa para torneios","Versao general: foque em Tatica e Lideranca","Adicione Riqueza para equipamento melhor"]},
{"id":"artifice_engenhoso","nome":"Artifice Engenhoso","descricao":"Inventor e construtor de engenhocas, conserta e cria equipamentos.","tags":["tecnico","artifice","inventor","engenheiro","construtor","gadgeteer","mecanico","intelectual"],"pontosBase":150,"racaId":None,"atributos":{"st":10,"dx":12,"iq":14,"ht":10},"vantagens":[{"id":"artifice"}],"desvantagens":[{"id":"curiosidade"}],"pericias":[{"id":"mecanica_nt","pts":4},{"id":"engenharia_nt","pts":4},{"id":"armadilhas_nt","pts":2},{"id":"ferreiro_nt","pts":2},{"id":"conserto_de_equipamento_eletronico_nt","pts":2}],"variacoes":["Versao alquimista: adicione Quimica e Farmacia","Versao armeiro: foque em Ferreiro e armas","Adicione Riqueza para oficina melhor"]},
{"id":"cacador_mortos_vivos","nome":"Cacador de Mortos-Vivos","descricao":"Especialista em rastrear e destruir mortos-vivos e criaturas das trevas.","tags":["combate","cacador","mortos-vivos","exorcista","cacador de monstros","religioso","trevas","undead"],"pontosBase":150,"racaId":None,"atributos":{"st":12,"dx":13,"iq":11,"ht":12},"vantagens":[{"id":"reflexos_em_combate"},{"id":"nocao_do_perigo"}],"desvantagens":[{"id":"voto"},{"id":"senso_do_dever"}],"pericias":[{"id":"espada_de_lamina_larga","pts":4},{"id":"besta","pts":2},{"id":"ocultismo","pts":4},{"id":"exorcismo","pts":2},{"id":"teologia","pts":2},{"id":"rastreamento","pts":2},{"id":"primeiros_socorros_nt","pts":1}],"variacoes":["Versao sacerdote-guerreiro: adicione magias sagradas","Versao cacador secular: foque em armas e Ocultismo","Adicione Conhecimento Oculto para tipos especificos"]},
{"id":"detetive_investigador","nome":"Detetive Investigador","descricao":"Investigador perspicaz que resolve crimes com logica e observacao. Moderno (NT8).","tags":["moderno","detetive","investigador","policial","inspetor","criminologia","noir","misterio"],"pontosBase":150,"racaId":None,"atributos":{"st":10,"dx":11,"iq":13,"ht":10},"vantagens":[{"id":"intuicao"}],"desvantagens":[{"id":"curiosidade"}],"pericias":[{"id":"criminologia_nt","pts":4},{"id":"observacao","pts":4},{"id":"interrogatorio","pts":2},{"id":"pesquisa_nt","pts":2},{"id":"armas_de_fogo_nt","pts":2,"esp":"Pistola"},{"id":"psicologia","pts":2},{"id":"furtividade","pts":1}],"variacoes":["Versao policial: adicione Direito e autoridade legal","Versao noir/durao: foque em Briga e Intimidacao","Versao forense: adicione Diagnose e Quimica"]},
{"id":"soldado_moderno","nome":"Soldado Moderno","descricao":"Combatente profissional treinado com armas de fogo e taticas. Moderno (NT8).","tags":["moderno","soldado","militar","combatente","atirador","tropa","veterano","guerra"],"pontosBase":150,"racaId":None,"atributos":{"st":12,"dx":13,"iq":10,"ht":12},"vantagens":[{"id":"reflexos_em_combate"}],"desvantagens":[{"id":"senso_do_dever"}],"pericias":[{"id":"armas_de_fogo_nt","pts":8,"esp":"Rifle"},{"id":"briga","pts":2},{"id":"tatica","pts":2},{"id":"sobrevivencia","pts":2},{"id":"primeiros_socorros_nt","pts":2},{"id":"conducao_nt","pts":1},{"id":"intimidacao","pts":1}],"variacoes":["Versao atirador de elite: foque em Rifle e Furtividade","Versao oficial: adicione Lideranca e Tatica","Versao mercenario: adicione mais armas e Demolicao"]},
{"id":"medico_doutor","nome":"Medico","descricao":"Profissional da saude que diagnostica e trata ferimentos e doencas. Moderno (NT8).","tags":["moderno","medico","doutor","cirurgiao","paramedico","saude","hospital","cientista"],"pontosBase":150,"racaId":None,"atributos":{"st":10,"dx":11,"iq":14,"ht":10},"vantagens":[{"id":"status"}],"desvantagens":[{"id":"senso_do_dever"}],"pericias":[{"id":"medicina_nt","pts":8},{"id":"diagnose_nt","pts":4},{"id":"primeiros_socorros_nt","pts":2},{"id":"farmacia_nt","pts":2},{"id":"psicologia","pts":2}],"variacoes":["Versao cirurgiao: foque em Cirurgia e Medicina","Versao paramedico de campo: adicione Conducao","Versao pesquisador: adicione Quimica e Biologia"]},
{"id":"cientista_pesquisador","nome":"Cientista Pesquisador","descricao":"Mente brilhante dedicada a pesquisa e ao conhecimento. Moderno (NT8+).","tags":["moderno","cientista","pesquisador","academico","fisico","quimico","professor","intelectual","sci-fi"],"pontosBase":150,"racaId":None,"atributos":{"st":9,"dx":10,"iq":15,"ht":10},"vantagens":[{"id":"talento_instintivo"}],"desvantagens":[{"id":"curiosidade"}],"pericias":[{"id":"fisica_nt","pts":4},{"id":"quimica_nt","pts":4},{"id":"pesquisa_nt","pts":2},{"id":"operacao_de_computadores_nt","pts":2},{"id":"programacao_de_computadores_nt","pts":2},{"id":"engenharia_nt","pts":2}],"variacoes":["Versao hacker: foque em Computadores e Hacking","Versao inventor: adicione Engenharia e Mecanica","Versao biologo: adicione Biologia e Medicina"]},
{"id":"investigador_horror","nome":"Investigador do Oculto","descricao":"Pesquisador do sobrenatural que enfrenta horrores alem da compreensao. Horror moderno (NT8).","tags":["horror","ocultismo","investigador","cacador","sobrenatural","paranormal","misterio","cthulhu","oculto"],"pontosBase":150,"racaId":None,"atributos":{"st":10,"dx":11,"iq":13,"ht":11},"vantagens":[{"id":"nocao_do_perigo"}],"desvantagens":[{"id":"curiosidade"},{"id":"fobias"}],"pericias":[{"id":"ocultismo","pts":4},{"id":"pesquisa_nt","pts":2},{"id":"observacao","pts":2},{"id":"armas_de_fogo_nt","pts":2,"esp":"Pistola"},{"id":"psicologia","pts":2},{"id":"conhecimento_oculto","pts":2},{"id":"furtividade","pts":1}],"variacoes":["Versao academico: foque em Pesquisa e idiomas mortos","Versao cacador armado: adicione mais armas e Tatica","Versao medium: adicione poderes psiquicos"]},
]

bad = 0
for t in novos:
    for v in t["vantagens"]:
        if v["id"] not in VAN: print("RUIM V", t["id"], v["id"]); bad += 1
    for x in t["desvantagens"]:
        if x["id"] not in DES: print("RUIM D", t["id"], x["id"]); bad += 1
    for p in t["pericias"]:
        if p["id"] not in PER and p["id"] not in TEC: print("RUIM P", t["id"], p["id"]); bad += 1

print("IDs invalidos:", bad)
if bad == 0:
    f = "forjador_templates.json"
    d = json.load(open(f, encoding="utf-8"))
    existentes = {t["id"] for t in d}
    add = [t for t in novos if t["id"] not in existentes]
    d.extend(add)
    json.dump(d, open(f, "w", encoding="utf-8"), ensure_ascii=False, indent=2)
    print("SALVO: +%d templates novos. Total agora: %d" % (len(add), len(d)))
else:
    print("NAO salvei - corrija os IDs acima")
