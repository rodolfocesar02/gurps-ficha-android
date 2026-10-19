#!/usr/bin/env node
'use strict';
/**
 * O custo que DEPENDE DA CONDIÇÃO — as magias do lote 2 (as "fixas com condição", as de custo por unidade/ponto e as
 * de ÁREA), escritas em forma compacta e gravadas em `custo-por-condicao-gerado.json`.
 *
 *   node scripts/magias/gerar-custo-por-condicao.js            # grava os dois arquivos
 *   node scripts/magias/gerar-custo-por-condicao.js --conferir # só confere se o que está gravado é o que sai daqui
 *
 * Os 18 do lote 1 ficam à mão em `custo-por-condicao.json`. Aqui mora a ESTRUTURA da conta (o que o jogador informa e
 * como sai o PF); o texto do livro continua vindo do `chunks.jsonl`, e a guarda (`custo-condicao.js`) confere cada
 * número da conta contra o texto de custo da entrada. Cada magia traz CASOS tirados do texto do livro, escritos aqui
 * a mão (`[condições, operar, manter]`) para o motor de dois lados (Mesa e app) provar que dá o mesmo número.
 *
 * Decisões dele (19/out/2026): sempre arredondar PARA CIMA; "dobre o custo" vale para operar e manter.
 */
const fs = require('fs');
const path = require('path');

const SAIDA = path.join(__dirname, 'custo-por-condicao-gerado.json');
const CASOS = path.join(__dirname, '..', '..', 'app', 'src', 'test', 'resources', 'custo_por_condicao_casos_gerados.json');

// ------------------------------------------------------------------------------------------------ entradas
const E = {
  /** Um número com unidade (kg, litros, m...). `max` é obrigatório: o servidor recusa o que não tem teto. */
  num: (id, rotulo, unidade, padrao, max, passo = 1, min = 0) => ({ id, tipo: 'numero', rotulo, unidade, min, max, passo, padrao }),
  kg: (padrao = 1, max = 100000, rotulo = 'Peso') => ({ id: 'peso', tipo: 'numero', rotulo, unidade: 'kg', min: 0, max, passo: 0.5, padrao }),
  int: (id, rotulo, min, max, padrao, unidade) => Object.assign({ id, tipo: 'inteiro', rotulo, min, max, padrao }, unidade ? { unidade } : {}),
  marca: (id, rotulo) => ({ id, tipo: 'marca', rotulo }),
  esc: (id, rotulo, padrao, opcoes) => ({ id, tipo: 'escolha', rotulo, padrao, opcoes: opcoes.map(([i, r, v]) => ({ id: i, rotulo: r, valor: v })) }),
};
// ------------------------------------------------------------------------------------------------ contas
/** ⌈qtd ÷ tam⌉, pelo menos 1 (a unidade começada paga). */
const un = (qtd, tam) => ['max', 1, ['cima', ['/', qtd, tam]]];
/** 1 + as marcas: "dobre" (uma), "triplique" (duas)... */
const mais1 = (...marcas) => ['+', 1, ...marcas];

const DEFS = [];
/** Uma magia: `tipo` 'fixo' (o custo-base do livro vale no caso-padrão) ou 'porUnidade'. */
function S(id, tipo, entradas, operar, manter, casos) {
  DEFS.push({ id, tipo, entradas, operar, manter, casos: casos || [] });
}

// ================================================================================================ 1. fixas com condição
S('atravessar_terra', 'fixo', [E.esc('material', 'Passar por', 'terra', [['terra', 'Terra', 1], ['pedra', 'Pedra (dobra o custo)', 2], ['metal', 'Metal (triplica o custo)', 3]])],
  ['*', 3, '$material'], ['*', 3, '$material'], [[{ material: 'terra' }, 3, 3], [{ material: 'pedra' }, 6, 6], [{ material: 'metal' }, 9, 9]]);
S('comunicacao', 'fixo', [E.int('adicionais', 'Pessoas adicionais na comunicação', 0, 20, 0)],
  ['*', 4, mais1('$adicionais')], ['*', 4, mais1('$adicionais')], [[{ adicionais: 0 }, 4, 4], [{ adicionais: 2 }, 12, 12]]);
S('telepatia', 'fixo', [E.int('adicionais', 'Pessoas adicionadas à conexão', 0, 20, 0)],
  ['*', 4, mais1('$adicionais')], ['*', 4, mais1('$adicionais')], [[{ adicionais: 0 }, 4, 4], [{ adicionais: 1 }, 8, 8]]);
S('controle_de_membro', 'fixo', [E.marca('uma_mao', 'Só sobre uma mão (ou parte análoga do corpo)')],
  ['-', 3, '$uma_mao'], ['-', 3, '$uma_mao'], [[{ uma_mao: false }, 3, 3], [{ uma_mao: true }, 2, 2]]);
S('convocar_espirito', 'fixo', [E.marca('local', 'Operada no local da morte'), E.marca('cadaver', 'Operada sobre o cadáver da pessoa')],
  ['cima', ['/', 20, ['*', mais1('$local'), mais1('$cadaver')]]], ['cima', ['/', 10, ['*', mais1('$local'), mais1('$cadaver')]]],
  [[{}, 20, 10], [{ local: true }, 10, 5], [{ local: true, cadaver: true }, 5, 3]]);
S('detectar_pontos_fracos', 'fixo', [E.int('raio', 'Raio do objeto grande (1 para um objeto comum)', 1, 50, 1, 'm'), E.marca('vivo', 'O objetivo é vivo')],
  ['*', '$raio', mais1('$vivo')], undefined, [[{}, 1, null], [{ raio: 3, vivo: true }, 6, null]]);
S('enclausuramento_arboreo', 'fixo', [E.esc('acao', 'Operar', 'criar', [['criar', 'Criar o enclausuramento', 8], ['reverter', 'Reverter um enclausuramento', 5]])],
  '$acao', undefined, [[{ acao: 'criar' }, 8, null], [{ acao: 'reverter' }, 5, null]]);
S('escriba_musical', 'fixo', [E.marca('irregular', 'Papel não regulado para escrita musical')],
  ['+', 3, '$irregular'], ['+', 1, '$irregular'], [[{}, 3, 1], [{ irregular: true }, 4, 2]]);
S('imunidade_a_acido', 'fixo', [E.esc('ameaca', 'Resistir a', 'comum', [['comum', 'Ácido comum', 2], ['essencial', 'Ácido Essencial', 6]])],
  '$ameaca', 'metade', [[{}, 2, 1], [{ ameaca: 'essencial' }, 6, 3]]);
S('imunidade_ao_fogo', 'fixo', [E.esc('calor', 'Resistir a', 'comum', [['comum', 'Fogo comum', 1], ['forno', 'Alto-forno ou vulcão (duplica o custo)', 2], ['estrela', 'Calor de uma estrela ou bomba nuclear (triplica)', 3]])],
  ['*', 2, '$calor'], ['*', 1, '$calor'], [[{}, 2, 1], [{ calor: 'forno' }, 4, 2], [{ calor: 'estrela' }, 6, 3]]);
S('imunidade_ao_frio', 'fixo', [E.esc('frio', 'Resistir a', 'comum', [['comum', 'Frio comum', 1], ['intenso', 'Frio de -20° ou mais (dobra o custo)', 2], ['absoluto', 'Frio do zero absoluto (triplica)', 3]])],
  ['*', 2, '$frio'], ['*', 1, '$frio'], [[{}, 2, 1], [{ frio: 'intenso' }, 4, 2], [{ frio: 'absoluto' }, 6, 3]]);
S('lealdade', 'fixo', [E.esc('relacao', 'O objetivo', 'conhece', [['conhece', 'Conhece o operador', 1], ['desconhece', 'Não conhece o operador (dobra o custo)', 2], ['inimigo', 'É inimigo do operador (triplica)', 3]])],
  ['*', 2, '$relacao'], ['*', 2, '$relacao'], [[{}, 2, 2], [{ relacao: 'desconhece' }, 4, 4], [{ relacao: 'inimigo' }, 6, 6]]);
S('manipular', 'porUnidade', [E.kg(5, 100000, 'Peso manipulado')],
  ['*', 4, un('$peso', 5)], ['*', 3, un('$peso', 5)], [[{ peso: 5 }, 4, 3], [{ peso: 12 }, 12, 9]]);
S('metamorfose_superior', 'fixo', [E.int('pontos', 'Pontos adicionais de limite de modelo', 0, 400, 0)],
  ['+', 20, ['cima', ['/', '$pontos', 5]]], 'metade', [[{}, 20, 10], [{ pontos: 5 }, 21, 11], [{ pontos: 12 }, 23, 12]]);
S('moldar_planta', 'fixo', [E.marca('morta', 'O material não está mais vivo (dobra o custo)')],
  ['*', 3, mais1('$morta')], ['*', 1, mais1('$morta')], [[{}, 3, 1], [{ morta: true }, 6, 2]]);
S('muralha_magica', 'porUnidade', [E.int('metros', 'Comprimento da muralha', 1, 40, 1, 'm')],
  ['*', 2, '$metros'], 'igual', [[{ metros: 1 }, 2, 2], [{ metros: 5 }, 10, 10]]);
S('passo_leve', 'fixo', [E.marca('vegetacao', 'Apagar também os vestígios na vegetação (conhece Ocultar Rastros)')],
  ['+', 4, '$vegetacao'], ['+', 1, '$vegetacao'], [[{}, 4, 1], [{ vegetacao: true }, 5, 2]]);
S('pedra_para_carne', 'fixo', [E.marca('metal', 'Restaurar uma pessoa transformada em metal (dobra o custo)')],
  ['*', 10, mais1('$metal')], undefined, [[{}, 10, null], [{ metal: true }, 20, null]]);
S('projeteis_flamejantes', 'fixo', [E.marca('duro', 'Projéteis de pedra ou metal (dobra o custo)')],
  ['*', 4, mais1('$duro')], ['*', 2, mais1('$duro')], [[{}, 4, 2], [{ duro: true }, 8, 4]]);
S('soterramento', 'fixo', [E.esc('acao', 'Operar', 'soterrar', [['soterrar', 'Soterrar', 10], ['reverter', 'Reverter um soterramento', 6]])],
  '$acao', undefined, [[{}, 10, null], [{ acao: 'reverter' }, 6, null]]);
S('visao_microscopica', 'fixo', [E.esc('duplicacoes', 'Duplicações do custo de operação', 'nenhuma', [['nenhuma', 'Nenhuma', 1], ['uma', '1 duplicação (ampliação ×10)', 2], ['duas', '2 duplicações (ampliação ×100)', 4], ['tres', '3 duplicações (ampliação ×1.000)', 8]])],
  ['*', 4, '$duplicacoes'], ['*', 2, '$duplicacoes'], [[{}, 4, 2], [{ duplicacoes: 'uma' }, 8, 4], [{ duplicacoes: 'tres' }, 32, 16]]);

// ================================================================================================ 2. por unidade, por ponto
/** "N por ponto (máximo M)": o jogador diz quantos pontos; `manter` é 'igual', 'metade' ou undefined (o livro não diz). */
function ponto(id, rotulo, mult, manter, max = 5, extra = {}) {
  const cm = (op) => (manter === 'igual' ? op : manter === 'metade' ? Math.ceil(op / 2) : null);
  S(id, 'porUnidade', [E.int('pontos', rotulo, 1, max, 1)], mult === 1 ? '$pontos' : ['*', mult, '$pontos'], manter,
    [[{ pontos: 1 }, mult, cm(mult)], [{ pontos: max }, mult * max, cm(mult * max)]].concat(extra.casos || []));
}
ponto('forca', 'Pontos de aumento de ST', 2, 'igual');
ponto('graca', 'Pontos de aumento de DX', 4, 'igual');
ponto('sabedoria', 'Pontos de aumento de IQ', 4, 'igual');
ponto('vigor', 'Pontos de aumento de HT', 2, 'igual');
ponto('fragilidade', 'Pontos de decréscimo de HT', 2, 'igual');
ponto('liberdade', 'Pontos de bônus', 2, 'igual');
ponto('enfraquecer_vontade', 'Pontos de redução de Vontade', 2, 'metade');
ponto('fortalecer_vontade', 'Pontos de aumento de Vontade', 1, 'metade');
ponto('debilitar', 'Pontos de redução de ST', 1, 'metade');
ponto('prontidao', 'Pontos de bônus', 2, 'metade');
ponto('sentido_agucado_audicao_agucada', 'Pontos de bônus', 1, 'metade');
ponto('sentido_agucado_visao_agucada', 'Pontos de bônus', 1, 'metade');
ponto('sentido_agucado_olfato_paladar_agucado', 'Pontos de bônus', 1, 'metade');
ponto('embriaguez', 'Pontos de IQ e DX perdidos', 1, 'metade');
ponto('persuasao', 'Bônus a adicionar ao teste', 2, 'igual');
ponto('vexacao', 'Penalidade subtraída do teste', 2, 'igual');
ponto('robustez', 'Pontos de RD', 1, undefined);
ponto('bloquear', 'Pontos de BD', 1, undefined);
ponto('aumentar', 'Pontos de MT a mais', 2, 'igual', 10);
ponto('aumentar_outro', 'Pontos de MT a mais', 2, 'igual', 10);
ponto('encolher', 'Pontos de MT a menos', 2, 'igual', 10);
ponto('encolher_outro', 'Pontos de MT a menos', 2, 'igual', 10);
ponto('olhos_do_falcao', 'Níveis de visão telescópica', 2, 'metade', 10);
S('fender', 'porUnidade', [E.int('dados', 'Dados de dano a infligir ao objeto', 1, 20, 1)], '$dados', undefined, [[{ dados: 1 }, 1, null], [{ dados: 6 }, 6, null]]);

// ---- por peso, volume, área, distância (a unidade começada paga: ⌈qtd ÷ tamanho⌉)
S('caminhar_nas_paredes', 'porUnidade', [E.kg(25, 100000, 'Peso do objetivo')], ['max', 2, ['cima', ['/', '$peso', 25]]], 'metade', [[{ peso: 25 }, 2, 1], [{ peso: 100 }, 4, 2], [{ peso: 110 }, 5, 3]]);
S('levitacao', 'porUnidade', [E.kg(40, 100000, 'Peso do objetivo')], ['max', 2, ['cima', ['/', '$peso', 40]]], 'metade', [[{ peso: 40 }, 2, 1], [{ peso: 100 }, 3, 2]]);
S('retardar_queda', 'porUnidade', [E.kg(25, 100000, 'Peso do objetivo')], un('$peso', 25), 'metade', [[{ peso: 25 }, 1, 1], [{ peso: 60 }, 3, 2]]);
S('remendar', 'porUnidade', [E.kg(5, 100000, 'Peso do objetivo')], ['max', 2, ['cima', ['/', '$peso', 5]]], 'metade', [[{ peso: 5 }, 2, 1], [{ peso: 20 }, 4, 2]]);
S('endurecer', 'porUnidade', [E.kg(0.5, 100000, 'Peso do objetivo')], ['max', 2, ['cima', ['/', '$peso', 0.5]]], 'metade', [[{ peso: 0.5 }, 2, 1], [{ peso: 3 }, 6, 3]]);
S('estragar', 'porUnidade', [E.kg(0.5, 100000, 'Peso do material arruinado')], ['*', 2, un('$peso', 0.5)], 'igual', [[{ peso: 1 }, 4, 4], [{ peso: 0.2 }, 2, 2]]);
S('ocultar_objeto', 'porUnidade', [E.kg(0.5, 100000, 'Peso do objeto')], un('$peso', 0.5), 'igual', [[{ peso: 2 }, 4, 4], [{ peso: 0.5 }, 1, 1]]);
S('faca_alada', 'porUnidade', [E.kg(0.5, 100000, 'Peso da arma'), E.marca('nao_arremessada', 'Arma que normalmente não é arremessada (dobra o custo)')],
  ['*', un('$peso', 0.5), mais1('$nao_arremessada')], undefined, [[{ peso: 1 }, 2, null], [{ peso: 1, nao_arremessada: true }, 4, null]]);
S('maturar', 'porUnidade', [E.kg(0.5, 100000, 'Peso do alimento')], un('$peso', 0.5), 'nenhum', [[{ peso: 1 }, 2, 0], [{ peso: 0.2 }, 1, 0]]);
S('purificar_o_alimento', 'porUnidade', [E.kg(0.5, 100000, 'Peso do material')], un('$peso', 0.5), undefined, [[{ peso: 1.5 }, 3, null]]);
S('combustivel_essencialnt', 'porUnidade', [E.kg(0.5, 100000, 'Peso do combustível')], un('$peso', 0.5), undefined, [[{ peso: 2 }, 4, null]]);
S('purificar_o_combustivelnt', 'porUnidade', [E.kg(1, 100000, 'Peso do material')], ['max', 1, ['cima', '$peso']], undefined, [[{ peso: 0.3 }, 1, null], [{ peso: 7 }, 7, null]]);
S('preservar_combustivelnt', 'porUnidade', [E.kg(0.5, 100000, 'Peso do combustível')], ['*', 4, un('$peso', 0.5)], 'metade', [[{ peso: 0.5 }, 4, 2], [{ peso: 1.5 }, 12, 6]]);
S('preservar_alimento', 'porUnidade', [E.kg(0.5, 100000, 'Peso do alimento')], ['*', 2, un('$peso', 0.5)], ['max', 1, ['cima', '$peso']], [[{ peso: 1 }, 4, 1], [{ peso: 2.5 }, 10, 3]]);
S('consertar', 'porUnidade', [E.kg(2.5, 100000, 'Peso do objetivo'), E.marca('moveis', 'Objeto com partes móveis (custo mínimo 6)')],
  ['max', ['*', 6, '$moveis'], ['*', 2, un('$peso', 2.5)]], undefined, [[{ peso: 5 }, 4, null], [{ peso: 5, moveis: true }, 6, null], [{ peso: 20, moveis: true }, 16, null]]);
S('criar_objeto', 'porUnidade', [E.kg(2.5, 100000, 'Peso do objeto criado')], ['*', 2, un('$peso', 2.5)], undefined, [[{ peso: 2.5 }, 2, null], [{ peso: 6 }, 6, null]]);
S('duplicar', 'porUnidade', [E.kg(2.5, 100000, 'Peso do objeto criado')], ['*', 3, un('$peso', 2.5)], undefined, [[{ peso: 2.5 }, 3, null], [{ peso: 6 }, 9, null]]);
S('aumentar_carga', 'porUnidade', [E.kg(12.5, 100000, 'Peso original'), E.esc('fator', 'Carga', 'dobrar', [['dobrar', 'Dobrar', 1], ['triplicar', 'Triplicar (o dobro do custo)', 2], ['quadruplicar', 'Quadruplicar (o dobro novamente)', 4]])],
  ['*', un('$peso', 12.5), '$fator'], 'metade', [[{}, 1, 1], [{ peso: 30, fator: 'triplicar' }, 6, 3], [{ peso: 30, fator: 'quadruplicar' }, 12, 6]]);
S('tapete_voador', 'porUnidade', [E.num('area', 'Superfície', 'm²', 0.1, 50, 0.1)], un('$area', 0.1), 'metade', [[{ area: 0.1 }, 1, 1], [{ area: 0.5 }, 5, 3]]);
S('pentagrama', 'porUnidade', [E.num('area', 'Área protegida', 'm²', 1, 9.9, 0.1)], ['max', 10, ['cima', ['/', '$area', 0.1]]], undefined, [[{ area: 0.5 }, 10, null], [{ area: 2 }, 20, null]]);
S('criar_porta', 'porUnidade', [E.num('area', 'Tamanho da porta', 'm²', 3, 100, 0.5)], ['*', 2, un('$area', 3)], undefined, [[{ area: 2 }, 2, null], [{ area: 7 }, 6, null]]);
S('madeira_essencial', 'porUnidade', [E.num('volume', 'Volume', 'm³', 0.75, 20, 0.25)], ['*', 8, un('$volume', 0.75)], undefined, [[{ volume: 0.75 }, 8, null], [{ volume: 2 }, 24, null]]);
S('terra_essencial', 'porUnidade', [E.num('volume', 'Volume', 'm³', 0.75, 20, 0.25)], ['*', 8, un('$volume', 0.75)], undefined, [[{ volume: 0.75 }, 8, null], [{ volume: 1.6 }, 24, null]]);
S('terra_para_pedra', 'porUnidade', [E.num('volume', 'Volume de terra', 'm³', 0.75, 30, 0.25)], ['*', 3, un('$volume', 0.75)], undefined, [[{ volume: 0.75 }, 3, null], [{ volume: 2 }, 9, null]]);
S('pedra_para_terra', 'porUnidade', [E.num('volume', 'Volume de pedra', 'm³', 0.75, 15, 0.25)], ['*', 6, un('$volume', 0.75)], undefined, [[{ volume: 0.75 }, 6, null], [{ volume: 2 }, 18, null]]);
S('terra_para_agua', 'porUnidade', [E.num('volume', 'Volume de terra transformada em lama', 'm³', 0.75, 60, 0.25), E.marca('agua', 'Transformar a terra em água (duplica o custo)')],
  ['*', un('$volume', 0.75), mais1('$agua')], undefined, [[{ volume: 0.75 }, 1, null], [{ volume: 2, agua: true }, 6, null]]);
S('criar_terra', 'porUnidade', [E.num('volume', 'Volume', 'm³', 0.75, 30, 0.25), E.esc('origem', 'Criar', 'nada', [['nada', 'Terra do nada', 2], ['lama', 'Solidificar lama em terra', 1]])],
  ['*', '$origem', un('$volume', 0.75)], undefined, [[{ volume: 0.75 }, 2, null], [{ volume: 2, origem: 'lama' }, 3, null], [{ volume: 2 }, 6, null]]);
S('criar_acido', 'porUnidade', [E.num('litros', 'Ácido criado', 'litros', 1, 1000)], un('$litros', 1), undefined, [[{ litros: 1 }, 1, null], [{ litros: 6 }, 6, null]]);
S('acido_essencial', 'porUnidade', [E.num('litros', 'Ácido', 'litros', 1, 1000)], ['*', 2, un('$litros', 1)], undefined, [[{ litros: 3 }, 6, null]]);
S('criar_agua', 'porUnidade', [E.num('litros', 'Água criada', 'litros', 4, 1000)], ['*', 2, un('$litros', 4)], undefined, [[{ litros: 4 }, 2, null], [{ litros: 9 }, 6, null]]);
S('criar_gelo', 'porUnidade', [E.num('litros', 'Gelo criado', 'litros', 4, 1000)], ['*', 2, un('$litros', 4)], undefined, [[{ litros: 4 }, 2, null], [{ litros: 10 }, 6, null]]);
S('agua_essencial', 'porUnidade', [E.num('litros', 'Água criada', 'litros', 4, 1000)], ['*', 3, un('$litros', 4)], undefined, [[{ litros: 4 }, 3, null], [{ litros: 9 }, 9, null]]);
S('agua_para_vinho', 'porUnidade', [E.num('litros', 'Água a transformar', 'litros', 1, 1000), E.esc('bebida', 'Bebida', 'vinho', [['cerveja', 'Cerveja (metade do custo)', 0.5], ['vinho', 'Vinho', 1], ['destilada', 'Bebida destilada (o dobro do custo)', 2]])],
  ['max', 1, ['cima', ['*', '$litros', '$bebida']]], undefined, [[{ litros: 10, bebida: 'cerveja' }, 5, null], [{ litros: 10 }, 10, null], [{ litros: 10, bebida: 'destilada' }, 20, null]]);
S('criar_combustivelnt', 'porUnidade', [E.kg(0.5, 100000, 'Peso do combustível'), E.int('nt', 'NT do combustível', 1, 12, 5)],
  ['*', '$nt', un('$peso', 0.5)], undefined, [[{ peso: 0.5, nt: 5 }, 5, null], [{ peso: 1, nt: 5 }, 10, null]]);
S('toque_congelante', 'porUnidade', [E.num('cm', 'Espessura do gelo', 'cm', 0.5, 50, 0.5)], ['*', 2, un('$cm', 0.5)], undefined, [[{ cm: 0.5 }, 2, null], [{ cm: 2 }, 8, null]]);
S('curar_radiacao', 'porUnidade', [E.num('rads', 'Radiação removida', 'rads', 10, 2000, 10)], ['max', 5, ['cima', ['/', '$rads', 10]]], undefined, [[{ rads: 30 }, 5, null], [{ rads: 80 }, 8, null]]);
S('extinguir_radiacao', 'porUnidade', [E.num('rads', 'Radiação por hora', 'rads/h', 10, 2000, 10)], un('$rads', 10), undefined, [[{ rads: 10 }, 1, null], [{ rads: 55 }, 6, null]]);
S('irradiar', 'porUnidade', [E.num('rads', 'Radiação por hora', 'rads/h', 10, 2000, 10)], un('$rads', 10), 'metade', [[{ rads: 10 }, 1, 1], [{ rads: 55 }, 6, 3]]);
S('mensagem', 'porUnidade', [E.int('segundos', 'Duração da mensagem', 1, 600, 15, 's')], un('$segundos', 15), undefined, [[{ segundos: 15 }, 1, null], [{ segundos: 40 }, 3, null]]);
S('translocacao_no_tempo', 'porUnidade', [E.int('segundos', 'Lapso pretendido', 1, 99, 1, 's')], '$segundos', undefined, [[{ segundos: 1 }, 1, null], [{ segundos: 5 }, 5, null]]);
S('translocar_outro_no_tempo', 'porUnidade', [E.int('segundos', 'Deslocamento pretendido', 1, 99, 1, 's')], '$segundos', undefined, [[{ segundos: 1 }, 1, null], [{ segundos: 5 }, 5, null]]);
S('puxar', 'porUnidade', [E.int('st', 'ST de "atração"', 1, 200, 2)], un('$st', 2), undefined, [[{ st: 2 }, 1, null], [{ st: 5 }, 3, null]]);
S('repelir', 'porUnidade', [E.int('st', 'ST de "repulsão"', 1, 200, 2)], un('$st', 2), undefined, [[{ st: 2 }, 1, null], [{ st: 7 }, 4, null]]);
S('reter', 'porUnidade', [E.int('metros', 'Projeção anulada', 1, 99, 1, 'm')], '$metros', undefined, [[{ metros: 4 }, 4, null]]);
S('retardar_mensagem', 'porUnidade', [E.int('frases', 'Frases simples', 1, 33, 1)], ['*', 3, '$frases'], 'nenhum', [[{ frases: 1 }, 3, 0], [{ frases: 4 }, 12, 0]]);
S('aquavisao', 'porUnidade', [E.num('metros', 'Profundidade', 'm', 20, 200, 10)], un('$metros', 20), 'igual', [[{ metros: 20 }, 1, 1], [{ metros: 50 }, 3, 3], [{ metros: 200 }, 10, 10]]);
S('geovisao', 'porUnidade', [E.num('metros', 'Profundidade', 'm', 10, 500, 10)], ['*', 2, un('$metros', 10)], 'igual', [[{ metros: 10 }, 2, 2], [{ metros: 35 }, 8, 8]]);
S('metalovisao', 'porUnidade', [E.num('metros', 'Profundidade', 'm', 5, 50, 5)], ['*', 2, un('$metros', 5)], 'igual', [[{ metros: 5 }, 2, 2], [{ metros: 50 }, 20, 20]]);
S('plastivisao', 'porUnidade', [E.num('metros', 'Profundidade', 'm', 5, 50, 5)], ['*', 2, un('$metros', 5)], 'igual', [[{ metros: 5 }, 2, 2], [{ metros: 50 }, 20, 20]]);
S('visao_das_plantas', 'porUnidade', [E.num('metros', 'Distância', 'm', 10, 100, 10)], un('$metros', 10), 'igual', [[{ metros: 10 }, 1, 1], [{ metros: 100 }, 10, 10]]);
S('aerovisao', 'porUnidade', [E.num('km', 'Distância', 'km', 1.5, 500, 0.5)], un('$km', 1.5), 'metade', [[{ km: 1.5 }, 1, 1], [{ km: 5 }, 4, 2]]);
S('muralha_absoluta', 'porUnidade', [E.int('metros', 'Comprimento', 1, 24, 1, 'm')], ['*', 4, '$metros'], 'igual', [[{ metros: 1 }, 4, 4], [{ metros: 5 }, 20, 20]]);
S('muralha_de_forca', 'porUnidade', [E.int('metros', 'Comprimento', 1, 49, 1, 'm')], ['*', 2, '$metros'], 'igual', [[{ metros: 1 }, 2, 2], [{ metros: 5 }, 10, 10]]);

// ---- por refeição, por dia, por alvo, por pontos de personagem
const refeicoes = (id, mult, manter) => S(id, 'porUnidade', [E.int('refeicoes', 'Refeições', 1, 99, 1)], mult === 1 ? '$refeicoes' : ['*', mult, '$refeicoes'], manter,
  [[{ refeicoes: 1 }, mult, manter === 'nenhum' ? 0 : null], [{ refeicoes: 5 }, 5 * mult, manter === 'nenhum' ? 0 : null]]);
refeicoes('cozinhar', 1);
refeicoes('temperar', 2);
refeicoes('envenenar_alimento', 3);
refeicoes('banquete_do_tolo', 2, 'nenhum');
S('deteriorar', 'porUnidade', [E.int('refeicoes', 'Refeições destruídas', 1, 99, 1)], '$refeicoes', undefined, [[{ refeicoes: 3 }, 3, null]]);
S('alimento_essencial', 'porUnidade', [E.int('refeicoes', 'Refeições', 1, 33, 1), E.esc('origem', 'Substância inicial', 'organica', [['organica', 'Material orgânico', 3], ['inedivel', 'Totalmente não comestível', 5]])],
  ['*', '$refeicoes', '$origem'], undefined, [[{ refeicoes: 4 }, 12, null], [{ refeicoes: 2, origem: 'inedivel' }, 10, null]]);
S('criar_alimento', 'porUnidade', [E.int('refeicoes', 'Refeições', 1, 33, 1), E.esc('origem', 'Criar a partir de', 'organico', [['organico', 'Material orgânico', 2], ['inorganico', 'Material inorgânico', 3], ['nada', 'Do nada', 4]])],
  ['*', '$refeicoes', '$origem'], undefined, [[{ refeicoes: 3 }, 6, null], [{ refeicoes: 3, origem: 'inorganico' }, 9, null], [{ refeicoes: 3, origem: 'nada' }, 12, null]]);
S('criacao_inspirada', 'porUnidade', [E.int('dias', 'Dias gastos na criação do objeto', 1, 19, 1)], ['*', 5, '$dias'], undefined, [[{ dias: 1 }, 5, null], [{ dias: 4 }, 20, null]]);
S('criar_nascente', 'porUnidade', [E.int('aumento', 'Aumento da produção (em unidades de 4 litros por hora)', 1, 19, 1)], ['*', 5, '$aumento'], undefined, [[{ aumento: 2 }, 10, null]]);
S('secar_nascente', 'porUnidade', [E.int('reducao', 'Redução da produção (em unidades de 4 litros por hora)', 1, 33, 1)], ['*', 3, '$reducao'], undefined, [[{ reducao: 4 }, 12, null]]);
S('protecao_superior', 'porUnidade', [E.int('alvos', 'Objetivos protegidos', 1, 99, 1)], ['max', 4, '$alvos'], undefined, [[{ alvos: 2 }, 4, null], [{ alvos: 9 }, 9, null]]);
S('prender_espirito', 'porUnidade', [E.int('pontos', 'Pontos de personagem do espírito', 3, 297, 90)], ['max', 30, ['cima', ['/', '$pontos', 3]]], undefined, [[{ pontos: 90 }, 30, null], [{ pontos: 150 }, 50, null]]);
['forca', 'graca', 'vigor', 'sabedoria', 'pericia'].forEach((x) => S('roubar_' + x, 'porUnidade', [E.int('pontos', 'Pontos de personagem roubados', 1, 99, 10)], ['max', 10, '$pontos'], undefined, [[{ pontos: 4 }, 10, null], [{ pontos: 25 }, 25, null]]));
S('convocacao_planar', 'porUnidade', [E.int('pontos', 'Pontos de personagem da entidade', 10, 990, 200)], ['max', 20, ['cima', ['/', '$pontos', 10]]], 'nenhum', [[{ pontos: 200 }, 20, 0], [{ pontos: 400 }, 40, 0]]);
S('convocar_demonio', 'porUnidade', [E.int('pontos', 'Pontos de personagem do demônio', 10, 990, 200)], ['max', 20, ['cima', ['/', '$pontos', 10]]], undefined, [[{ pontos: 200 }, 20, null], [{ pontos: 400 }, 40, null]]);
['ar', 'fogo', 'terra', 'agua'].forEach((x) => {
  S('controle_de_elemental_' + x, 'porUnidade', [E.int('pontos', 'Pontos de personagem do elemental', 10, 990, 40)], ['max', 1, ['cima', ['/', '$pontos', 10]]], 'metade', [[{ pontos: 40 }, 4, 2], [{ pontos: 75 }, 8, 4]]);
  S('criar_elemental_' + x, 'porUnidade', [E.int('pontos', 'Pontos de personagem do elemental', 10, 990, 40)], ['max', 4, ['cima', ['/', '$pontos', 10]]], undefined, [[{ pontos: 40 }, 4, null], [{ pontos: 120 }, 12, null]]);
});
S('memoria_falsa', 'porUnidade', [E.esc('duracao', 'Duração', 'hora', [['hora', 'Uma hora', 0], ['dia', 'Um dia inteiro', 1]])],
  ['max', 3, ['*', 8, '$duracao']], ['max', 3, ['*', 4, '$duracao']], [[{}, 3, 3], [{ duracao: 'dia' }, 8, 4]]);
S('prehistoria', 'porUnidade', [E.esc('periodo', 'História a ver', 'k1', [['k1', '1.000 anos', 3], ['k10', '10.000 anos', 5], ['k100', '100.000 anos', 8], ['mais', 'Períodos mais longos', 10]])],
  '$periodo', undefined, [[{}, 3, null], [{ periodo: 'k10' }, 5, null], [{ periodo: 'mais' }, 10, null]]);
const clima = (id) => S(id, 'porUnidade', [E.int('dias', 'Duração da previsão', 1, 49, 1, 'dias'), E.esc('local', 'Local', 'geral', [['geral', 'Na área local', 1], ['fora', 'Fora da área (duplica o custo)', 2], ['continente', 'Em outro continente (quadruplica)', 4]])],
  ['*', 2, ['*', '$dias', '$local']], undefined, [[{ dias: 3 }, 6, null], [{ dias: 3, local: 'fora' }, 12, null], [{ dias: 3, local: 'continente' }, 24, null]]);
clima('previsao_do_tempo');
clima('prever_movimento_da_terra');
S('sono_curativo', 'porUnidade', [E.esc('recuperacao', 'Velocidade de recuperação', 'normal', [['normal', 'Normal', 6], ['dobrada', 'Dobrada', 10]])],
  '$recuperacao', undefined, [[{}, 6, null], [{ recuperacao: 'dobrada' }, 10, null]]);

// ================================================================================================ 3. ÁREA (Magia p.11)
// "O custo real para operá-la é igual ao custo básico multiplicado pelo raio da área de efeito em metros (mínimo de
// 1 m)... Algumas têm uma fração como custo básico... É necessário gastar, no mínimo, um ponto de energia." O app já
// cobra assim na Saga (`MagicCost.custoAreaPorRaio`); aqui a aba Rolagem e a Mesa passam a perguntar o raio. Só entram
// as entradas do livro que escrevem "Custo BÁSICO" (o rótulo do livro é o que marca a regra) e são de classe Área.
const AREA_NOME_RAIO = 'Raio da área';
const EP = 1e-9;

/**
 * Uma magia de área: `basico` (número ou expressão), `manter` ('igual' | 'metade' | 'nenhum' | número do livro | undefined),
 * `minCusto` (o custo mínimo da magia), `raioMin` (o raio mínimo da magia, em metros), `extras` (entradas além do raio) e
 * `fator` (expressão que multiplica o total: "dobre o custo básico").
 */
function area(id, { basico, manter, minCusto, raioMin = 1, extras = [], fator = null, casos = null }) {
  const entradas = [E.int('raio', AREA_NOME_RAIO, raioMin, 100, raioMin, 'm')].concat(extras);
  let total = ['*', basico, '$raio'];
  if (fator) total = ['*', total, fator];
  const operar = ['max', minCusto || 1, total];
  let m;
  if (manter === 'igual' || manter === 'metade' || manter === 'nenhum') m = manter;
  else if (typeof manter === 'number') m = ['max', 1, ['*', manter, '$raio']];
  S(id, 'inalterado', entradas, operar, m, casos || casosDeArea({ basico, manter, minCusto, raioMin }));
}

/** Os casos de uma área simples (básico numérico): raio mínimo e raio mínimo + 4, contados à parte do motor. */
function casosDeArea({ basico, manter, minCusto, raioMin }) {
  return [raioMin, raioMin + 4].map((raio) => {
    const op = Math.max(minCusto || 1, Math.ceil(basico * raio - EP));
    let mt = null;
    if (manter === 'igual') mt = op;
    else if (manter === 'metade') mt = Math.ceil(op / 2);
    else if (manter === 'nenhum') mt = 0;
    else if (typeof manter === 'number') mt = Math.max(1, Math.ceil(manter * raio - EP));
    return [{ raio }, op, mt];
  });
}

// ---- as de área que o livro escreve com uma CONDIÇÃO a mais (escolhas, "dobre o custo básico"...)
const dobro = (id, rotulo) => ({ extras: [E.marca(id, rotulo)], fator: mais1('$' + id) });
const AREA_MANUAL = {
  alarme_florestal: () => area('alarme_florestal', { basico: 0.5, minCusto: 2, manter: 'igual' }),
  alterar_terreno: () => area('alterar_terreno', { basico: 1, minCusto: 15, manter: 'nenhum' }),
  autossuficiencia: () => area('autossuficiencia', { basico: 2, manter: 'nenhum' }),
  chama_essencial: () => area('chama_essencial', { basico: 3, manter: 2 }),
  chuva_de_adagas_de_gelo: () => area('chuva_de_adagas_de_gelo', Object.assign({ basico: 1, minCusto: 2, casos: [[{ raio: 1 }, 2, null], [{ raio: 4 }, 4, null], [{ raio: 4, dobro: true }, 8, null]] }, dobro('dobro', 'Dobrar o custo básico (1d de dano por segundo)'))),
  chuva_de_fogo: () => area('chuva_de_fogo', Object.assign({ basico: 1, minCusto: 2, manter: 'igual', casos: [[{ raio: 1 }, 2, 2], [{ raio: 4 }, 4, 4], [{ raio: 4, dobro: true }, 8, 8]] }, dobro('dobro', 'Dobrar o custo básico (2d-2 de dano por segundo)'))),
  chuva_de_pedras: () => area('chuva_de_pedras', Object.assign({ basico: 1, minCusto: 2, manter: 'igual', casos: [[{ raio: 1 }, 2, 2], [{ raio: 4 }, 4, 4], [{ raio: 4, dobro: true }, 8, 8]] }, dobro('dobro', 'Dobrar o custo básico (2d-2 de dano por segundo)'))),
  condensar_vapor: () => area('condensar_vapor', { basico: 1, minCusto: 2 }),
  conexao: () => area('conexao', { basico: 8, manter: 'nenhum' }),
  criar_fogo: () => area('criar_fogo', { basico: 2, manter: 'metade' }),
  criar_planta: () => area('criar_planta', {
    basico: '$planta', extras: [E.esc('planta', 'Criar', 'gramineas', [['gramineas', 'Gramíneas', 4], ['arbustos', 'Arbustos', 8], ['arvores', 'Árvores', 15]])],
    casos: [[{ raio: 1 }, 4, null], [{ raio: 2, planta: 'arbustos' }, 16, null], [{ raio: 3, planta: 'arvores' }, 45, null]]
  }),
  descongelar: () => area('descongelar', { basico: 1, minCusto: 2, manter: 'igual' }),
  dissipar_agua: () => area('dissipar_agua', { basico: 3, manter: 'igual' }),
  entalhar: () => area('entalhar', { basico: 1, minCusto: 2, manter: 'igual' }),
  entrelacamento: () => area('entrelacamento', {
    basico: '$cobertura', manter: 'metade', extras: [E.esc('cobertura', 'Cobertura', 'grama', [['grama', 'Grama', 1], ['arbustos', 'Arbustos e árvores', 2]])],
    casos: [[{ raio: 1 }, 1, 1], [{ raio: 3, cobertura: 'arbustos' }, 6, 3]]
  }),
  escudo_antiteleporte: () => area('escudo_antiteleporte', { basico: 1 / 3, raioMin: 3, manter: 'igual' }),
  escurecer: () => area('escurecer', {
    basico: '$luz', raioMin: 2, extras: [E.esc('luz', 'Luz', 'luar', [['tochas', 'Tochas (custo básico 1/2)', 0.5], ['luar', 'Luar (custo básico 1)', 1], ['estrelas', 'Luz das estrelas (custo básico 3/2)', 1.5], ['total', 'Escuridão total (custo básico 2)', 2]])],
    casos: [[{ raio: 2, luz: 'tochas' }, 1, null], [{ raio: 3, luz: 'tochas' }, 2, null], [{ raio: 4, luz: 'estrelas' }, 6, null], [{ raio: 5, luz: 'total' }, 10, null]]
  }),
  fantasma: () => area('fantasma', {
    basico: '$forca', manter: 'metade', extras: [E.esc('forca', 'Fantasma com', 'normal', [['normal', 'ST normal', 5], ['dobro', 'O dobro da ST normal (custo básico 9)', 9]])],
    casos: [[{ raio: 1 }, 5, 3], [{ raio: 2, forca: 'dobro' }, 18, 9]]
  }),
  ilusao_perfeita: () => area('ilusao_perfeita', Object.assign({ basico: 3, manter: 'metade', casos: [[{ raio: 1 }, 3, 2], [{ raio: 2, toque: true }, 12, 6]] }, dobro('toque', 'Enganar também o sentido do toque (custo duplo)'))),
  purificar_a_terra: () => area('purificar_a_terra', Object.assign({ basico: 2, casos: [[{ raio: 1 }, 2, null], [{ raio: 3, pobre: true }, 12, null]] }, dobro('pobre', 'Solo pobre, como planície rochosa ou deserto arenoso (dobra o custo)'))),
  moldar_trevas: () => area('moldar_trevas', {
    basico: '$sombras', manter: 'igual', extras: [E.esc('sombras', 'Sombras', 'tridimensionais', [['tridimensionais', 'Tridimensionais', 2], ['bidimensionais', 'Bidimensionais (custo básico 1)', 1]])],
    casos: [[{ raio: 2 }, 4, 4], [{ raio: 3, sombras: 'bidimensionais' }, 3, 3]]
  }),
  neblina_mistica: () => area('neblina_mistica', { basico: 1, manter: 'igual' }),
  // Terremoto: a intensidade multiplica o custo básico (Leve ×2, Grave ×4), escrita na descrição da entrada
  terremoto: () => area('terremoto', {
    basico: 2, manter: 'igual', extras: [E.esc('intensidade', 'Intensidade', 'demonstracao', [['demonstracao', 'Apenas uma demonstração (custo básico)', 1], ['leve', 'Leve (custo básico ×2)', 2], ['grave', 'Grave (custo básico ×4)', 4]])],
    fator: '$intensidade', casos: [[{ raio: 3 }, 6, 6], [{ raio: 3, intensidade: 'leve' }, 12, 12], [{ raio: 3, intensidade: 'grave' }, 24, 24]]
  }),
  ocultar: () => area('ocultar', {
    basico: '$terreno', manter: 'igual', extras: [E.esc('terreno', 'Terreno', 'floresta', [['floresta', 'Floresta, selva ou debaixo d\'água', 1], ['planicie', 'Planícies ou savana', 2], ['tundra', 'Tundra', 3]])],
    casos: [[{ raio: 3 }, 3, 3], [{ raio: 3, terreno: 'planicie' }, 6, 6], [{ raio: 2, terreno: 'tundra' }, 6, 6]]
  }),
  percepcao_de_espiritos: () => area('percepcao_de_espiritos', { basico: 0.5, minCusto: 1 }),
  percepcao_de_vida: () => area('percepcao_de_vida', { basico: 0.5, minCusto: 1 }),
  percepcao_de_inimigos: () => area('percepcao_de_inimigos', { basico: 1, minCusto: 2 }),
  testar_carga: () => area('testar_carga', { basico: 1, minCusto: 2 }),
  percepcao_de_observacao: () => S('percepcao_de_observacao', 'inalterado', [E.int('raio', AREA_NOME_RAIO, 1, 100, 1, 'm'), E.marca('ser', 'Operada em um ser (custo 3)')],
    ['max', ['*', 3, '$ser'], ['*', ['-', 1, '$ser'], '$raio']], 'metade', [[{ raio: 1 }, 1, 1], [{ raio: 5 }, 5, 3], [{ ser: true }, 3, 2]]),
  resistencia_ao_fogo: () => area('resistencia_ao_fogo', Object.assign({ basico: 3, manter: 'igual', casos: [[{ raio: 1 }, 3, 3], [{ raio: 2, dobro: true }, 12, 12]] }, dobro('dobro', 'Custo dobrado (-10 a tentativas mágicas de acender fogueiras)'))),
  sono_coletivo: () => area('sono_coletivo', { basico: 3, raioMin: 2 }),
  sugestao_coletiva: () => area('sugestao_coletiva', { basico: 4, manter: 2, raioMin: 2 }),
  torpor_coletivo: () => area('torpor_coletivo', { basico: 2, manter: 1, raioMin: 2 }),
  zumbis_em_massa: () => area('zumbis_em_massa', { basico: 7, raioMin: 2 }),
  abencoar_plantas: () => area('abencoar_plantas', { basico: 1 }),
  secar_plantas: () => area('secar_plantas', { basico: 1 }),
  curar_planta: () => area('curar_planta', { basico: 3 }),
  tempestade_de_faiscas: () => area('tempestade_de_faiscas', {
    basico: '$relampago', manter: 'metade', extras: [E.esc('relampago', 'Relâmpago', 'r1', [['r1', 'Relâmpago 1d-1', 2], ['r2', 'Relâmpago 2d-2', 4], ['r3', 'Relâmpago 3d-3', 6]])],
    casos: [[{ raio: 1 }, 2, 1], [{ raio: 2, relampago: 'r3' }, 12, 6]]
  }),
  // a faixa de energia que o jogador escolhe × o raio
  muralha_de_luz: () => faixaArea('muralha_de_luz', 1, 3, 'igual'),
  muralha_de_relampagos: () => faixaArea('muralha_de_relampagos', 2, 6, 'igual'),
  nuvem_de_faiscas: () => faixaArea('nuvem_de_faiscas', 1, 5, 'igual'),
  nuvem_de_fogo: () => faixaArea('nuvem_de_fogo', 1, 5, 'igual'),
};
function faixaArea(id, de, ate, manter) {
  area(id, {
    basico: '$energia', manter, extras: [E.int('energia', 'Energia no custo básico', de, ate, de)],
    casos: [[{ raio: 1 }, de, de], [{ raio: 3, energia: ate }, ate * 3, ate * 3]]
  });
}
// o tempo: as fracionárias do clima ("1/10 (mínimo 1)") têm o mesmo molde
const CLIMA = [['aquece', 0.1, 'Aplicações do custo básico (cada uma aumenta a temperatura em 2° C)'], ['resfriar', 0.1, 'Aplicações do custo básico (cada uma reduz a temperatura em 20° C)'],
  ['neve', 1 / 15, 'Aplicações (cada 1/15 a mais por metro de raio aumenta a neve em 2,5 cm por hora)']];
CLIMA.forEach(([id, frac, rot]) => {
  AREA_MANUAL[id] = () => area(id, {
    basico: frac, minCusto: 1, manter: 'igual', extras: [E.int('aplicacoes', rot, 1, 20, 1)], fator: '$aplicacoes',
    casos: [[{ raio: 10 }, Math.max(1, Math.ceil(frac * 10 - EP)), Math.max(1, Math.ceil(frac * 10 - EP))], [{ raio: 20, aplicacoes: 2 }, Math.max(1, Math.ceil(frac * 20 * 2 - EP)), Math.max(1, Math.ceil(frac * 20 * 2 - EP))]]
  });
});
[['nuvens', 1 / 20], ['vento', 1 / 50], ['tempestade', 1 / 50], ['correnteza', 1 / 50], ['mare', 1 / 30], ['ondas', 1 / 60]].forEach(([id, frac]) => {
  AREA_MANUAL[id] = () => area(id, { basico: frac, minCusto: 1, manter: 'igual' });
});
AREA_MANUAL.chuva = () => area('chuva', Object.assign({ basico: 0.1, minCusto: 1, manter: 'igual', casos: [[{ raio: 10 }, 1, 1], [{ raio: 30, inadequada: true }, 6, 6]] }, dobro('inadequada', 'Fazer chover em deserto, ou evitar a chuva em área chuvosa ou pantanosa (dobra o custo)')));
AREA_MANUAL.chuva_de_nozes = () => area('chuva_de_nozes', Object.assign({ basico: 0.1, manter: 'igual', casos: [[{ raio: 10 }, 1, 1], [{ raio: 30, dobro: true }, 6, 6]] }, dobro('dobro', 'Dobrar o custo para dobrar o efeito (-2 de penalidade no NH e visibilidade -1 por 4 metros)')));
AREA_MANUAL.granizo = () => area('granizo', {
  basico: '$tipo', minCusto: 1, manter: 'igual', extras: [E.esc('tipo', 'Granizo', 'comum', [['comum', 'Comum (custo básico 1/5)', 0.2], ['prejudicial', 'Prejudicial (custo básico 1)', 1]])],
  casos: [[{ raio: 10 }, 2, 2], [{ raio: 3, tipo: 'prejudicial' }, 3, 3]]
});

/** As de área "simples": `N [para operar]. [M para manter | Mesmo custo para manter | Metade ... | Não pode ser mantida]`. */
function areaSimples(texto) {
  const t = texto.replace(/\s+/g, ' ').trim();
  const m = /^(\d+)(?:\/(\d+))?\s*(?:para operar)?\s*[.;]?\s*(.*)$/i.exec(t);
  if (!m) return null;
  const basico = m[2] ? Number(m[1]) / Number(m[2]) : Number(m[1]);
  let resto = m[3];
  let manter;
  let r;
  if ((r = /^(?:e )?(\d+) para manter\.?(.*)$/i.exec(resto))) { manter = Number(r[1]); resto = r[2]; }
  else if ((r = /^(?:o )?mesmo custo para manter\.?(.*)$/i.exec(resto))) { manter = 'igual'; resto = r[1]; }
  else if ((r = /^metade(?: disso)? para manter\.?(.*)$/i.exec(resto))) { manter = 'metade'; resto = r[1]; }
  else if ((r = /^n[ãa]o pode ser mantida[^.]*\.?(.*)$/i.exec(resto))) { manter = 'nenhum'; resto = r[1]; }
  let raioMin = 1;
  let minCusto;
  const rm = /raio m[ií]nimo (?:de |é de )?(\d+) metros?/i.exec(resto);
  if (rm) { raioMin = Number(rm[1]); resto = resto.replace(rm[0], ''); }
  const mc = /(?:custo )?m[ií]nimo (?:de )?(\d+)/i.exec(resto);
  if (mc) { minCusto = Number(mc[1]); resto = resto.replace(mc[0], ''); }
  if (/[0-9]|dobr|tripl|multiplic/i.test(resto.replace(/[.\s()]/g, ' '))) return null;   // ainda tem condição: à mão
  return { basico, manter, raioMin, minCusto };
}

// ================================================================================================ a saída
/** O texto de custo e o RÓTULO do livro ("Custo básico" ou "Custo") de cada entrada, lidos do `chunks.jsonl`. */
function textosEComRotulo(catalogo) {
  const L = require('./localizador');
  const RAIZ = path.resolve(__dirname, '..', '..');
  const livro = L.carregarLivro(path.join(RAIZ, 'app', 'src', 'main', 'assets', 'chunks.jsonl'));
  const manual = L.lerLocalizacaoManual(path.join(__dirname, 'magias-localizacao-manual.json'));
  const indice = L.construirIndice(livro, catalogo, { manual });
  const saida = new Map();
  catalogo.forEach((m) => {
    const x = indice.magias[m.id];
    if (!x || !x.entrada || x.entrada.formato !== 'bloco') return;
    const e = L.camposDaEntrada(livro, x.entrada);
    const pos = x.entrada.campos && x.entrada.campos.custo;
    const linha = pos && livro.paginas.get(pos.pagina) ? String(livro.paginas.get(pos.pagina)[pos.linha] || '') : '';
    if (e.custo) saida.set(m.id, { texto: String(e.custo).replace(/\*\*/g, '').replace(/\s+/g, ' ').trim(), basico: /\*\*Custo b[áa]sico/i.test(linha) || /^Custo b[áa]sico/i.test(linha.replace(/\*\*/g, '')) });
  });
  return saida;
}

function montar() {
  const RAIZ = path.resolve(__dirname, '..', '..');
  const catalogo = JSON.parse(fs.readFileSync(path.join(RAIZ, 'app', 'src', 'main', 'assets', 'magias2versao.json'), 'utf8'));
  const info = textosEComRotulo(catalogo);
  const relatorio = { semRotuloBasico: [], areaAFazer: [], duplicadas: [] };

  // área escrita à mão: confere o rótulo do livro
  Object.keys(AREA_MANUAL).forEach((id) => {
    if (typeof AREA_MANUAL[id] !== 'function') return;
    const i = info.get(id);
    if (!i || !i.basico) relatorio.semRotuloBasico.push(id + ' (' + (i ? 'rótulo "Custo"' : 'sem entrada') + ')');
    AREA_MANUAL[id]();
  });
  // área simples, pelo texto do livro
  // as do lote 1 (à mão) ficam como estão; o que o catálogo já traz do próprio gerador NÃO conta (senão o gerador não repetiria)
  const lote1 = new Set(Object.keys(JSON.parse(fs.readFileSync(path.join(__dirname, 'custo-por-condicao.json'), 'utf8'))));
  const ja = new Set(DEFS.map((d) => d.id));
  catalogo.forEach((m) => {
    if (!/Área/.test(m.classe || '') || ja.has(m.id) || lote1.has(m.id)) return;
    const i = info.get(m.id);
    if (!i || !i.basico) return;
    const r = areaSimples(i.texto);
    if (r) { area(m.id, r); ja.add(m.id); }
    else relatorio.areaAFazer.push(m.id + ': ' + i.texto.slice(0, 120));
  });

  const vistos = new Set();
  DEFS.forEach((d) => { if (vistos.has(d.id)) relatorio.duplicadas.push(d.id); vistos.add(d.id); });
  return { catalogo, info, relatorio };
}

function paraJson() {
  const specs = {};
  const casos = [];
  DEFS.forEach((d) => {
    const s = { tipo: d.tipo, entradas: d.entradas, operar: d.operar };
    if (d.manter !== undefined) s.manter = d.manter;
    specs[d.id] = s;
    const tipoManter = d.manter === undefined ? null : (typeof d.manter === 'string' ? d.manter : 'conta');
    d.casos.forEach(([condicoes, operar, manter]) => casos.push({ id: d.id, condicoes, operar, manter, manterTipo: manter === null ? null : tipoManter }));
  });
  const leia = ['Gerado por scripts/magias/gerar-custo-por-condicao.js: NÃO edite à mão (edite o gerador e rode de novo).'];
  return {
    specs: Object.assign({ _leia: leia }, specs),
    casos: { _leia: ['Casos do lote 2, escritos à mão no gerador a partir do TEXTO DO LIVRO (não da conta). O app e a Mesa rodam os mesmos.'], casos }
  };
}

function main() {
  const { relatorio } = montar();
  const out = paraJson();
  const texto = JSON.stringify(out.specs, null, 1) + '\n';
  const textoCasos = JSON.stringify(out.casos, null, 1) + '\n';
  if (process.argv.includes('--conferir')) {
    const igual = fs.existsSync(SAIDA) && fs.readFileSync(SAIDA, 'utf8').replace(/\r\n/g, '\n') === texto
      && fs.existsSync(CASOS) && fs.readFileSync(CASOS, 'utf8').replace(/\r\n/g, '\n') === textoCasos;
    console.log(igual ? 'ok: o gravado é o que o gerador produz' : 'DIFERENTE: rode node scripts/magias/gerar-custo-por-condicao.js');
    process.exit(igual ? 0 : 1);
  }
  if (relatorio.duplicadas.length) { console.error('Magias definidas duas vezes: ' + relatorio.duplicadas.join(', ')); process.exit(1); }
  fs.writeFileSync(SAIDA, texto);
  fs.writeFileSync(CASOS, textoCasos);
  console.log('specs: ' + DEFS.length + ' | casos: ' + out.casos.casos.length);
  if (relatorio.semRotuloBasico.length) console.log('⚠️ à mão, mas o livro NÃO escreve "Custo básico": ' + relatorio.semRotuloBasico.join('; '));
  if (relatorio.areaAFazer.length) console.log('Área ainda à mão (' + relatorio.areaAFazer.length + '):\n  ' + relatorio.areaAFazer.join('\n  '));
}

if (require.main === module) main();
module.exports = { DEFS, montar, paraJson, SAIDA, CASOS };
