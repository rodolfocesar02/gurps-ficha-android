# Pariedade Ficha APP x VTT (sem Aba Rolagem)

Lista completa das diferenças identificáveis a partir dos arquivos do app Android + estado atual do VTT (prints/erros). Se algo aqui já tiver sido corrigido depois do último teste, marque como resolvido.

1. App possui `limiteDesvantagens` configurável no personagem; VTT não expõe o ajuste na UI.
2. App calcula `pontosAtributos` via `CharacterRules.calcularPontosAtributos`; VTT não espelha a mesma função/resultado.
3. App calcula `pontosSecundarios` com mods de PV/PF/Per/Velocidade/Deslocamento; VTT não usa o mesmo cálculo.
4. App soma `pontosVantagens` pelo `custoFinal`; VTT mostra custo zerado para vantagens adicionadas.
5. App soma `pontosDesvantagens` pelo `custoFinal`; VTT não desconta corretamente em pontos.
6. App soma `pontosQualidades` (1 por qualidade); VTT não contabiliza ou não exibe.
7. App soma `pontosPeculiaridades` como -1 cada; VTT não aplica a regra negativa.
8. App soma `pontosPericias` por `pontosGastos`; VTT não soma igual.
9. App soma `pontosTecnicas` com `coerceAtLeast(0)`; VTT não trata negativos igual.
10. App soma `pontosMagias` com `coerceAtLeast(1)`; VTT permite 0.
11. App exibe alerta quando desvantagens excedem limite; VTT não bloqueia nem avisa.
12. App normaliza JSON antigo garantindo listas (magias/qualidades/tecnicas); VTT falha com payloads incompletos.
13. App tem parser robusto de custo de vantagem (fixo/por_nivel/variavel/?) ; VTT não parseia todos formatos.
14. App trata custos com “/n” em vantagens; VTT não aplica custo por nível corretamente.
15. App trata custos com múltiplos números em vantagens; VTT escolhe errado o custo final.
16. App trata custo com “?” e texto extra; VTT não limpa corretamente.
17. App calcula `custoFinal` de vantagem considerando `custoBase`, `custoEscolhido`, `nivel`; VTT não.
18. App trata autocontrole de desvantagens (`*`); VTT não calcula ajuste de autocontrole.
19. App trata custos negativos de desvantagens (múltiplos formatos); VTT não aplica.
20. App calcula `custoFinal` de desvantagem com `nivel`, `custoBase`, `custoEscolhido`; VTT não.
21. App mantém `preRequisitoRaw` em itens suplementares; VTT perde a string original.
22. App mostra `preRequisito` da perícia (v2) no detalhe; VTT não exibe.
23. App mostra `preRequisito` da técnica; VTT não exibe.
24. App mostra `preRequisito` da magia; VTT não exibe.
25. App tem `PericiaV2PreRequisitoRegra` acoplado ao catálogo; VTT não processa.
26. App tem `PreRequisitoChecker` com lógica de AND/OR/Nível; VTT não usa igual.
27. App tem `PreRequisitoParser` com tokens específicos; VTT não suporta todos tokens.
28. App suporta pré-requisito por atributo mínimo; VTT não valida.
29. App suporta pré-requisito por vantagem específica; VTT não valida.
30. App suporta pré-requisito por perícia com NH mínimo; VTT não valida.
31. App suporta pré-requisito por magia; VTT não valida.
32. App suporta pré-requisito por técnica; VTT não valida.
33. App suporta pré-requisito “OU” entre itens; VTT trata como “E”.
34. App suporta pré-requisito “E” em sequência; VTT não mostra erro quando falta.
35. App tem `MagiaEnergiaRules` com redução de custo por NH; VTT não aplica.
36. App aplica redução por NH em energia de magia; VTT não.
37. App ajusta custo final de magia em rolagem/consumo; VTT não.
38. App mantém `nivelAptidaoMagica` no cálculo de NH de magia; VTT não soma.
39. App fixa atributo base IQ para magia; VTT permite mudar indevidamente.
40. App calcula NH de magia igual perícia (IQ + AM + pontos); VTT difere.
41. App tem `NexusArcanoModoAlvoAdapter` para modo alvo; VTT não integra.
42. App exibe seletor “Modo Alvo” nas magias; VTT incompleto.
43. App mantém `magiasFiltradasCache` para desempenho; VTT trava com grandes listas.
44. App pré-aquece catálogo de magias; VTT não, causando lentidão.
45. App filtra magias por escola/classe/busca; VTT não replica filtros.
46. App filtra perícias por busca; VTT lista sem filtros equivalentes.
47. App filtra vantagens por tags; VTT tags não funcionam igual.
48. App filtra desvantagens por tags; VTT tags não funcionam igual.
49. App usa tags: combate, social, fisica, mental, magica; VTT não aplica corretamente.
50. App mostra custo total em vantagens/desvantagens por item; VTT não.
51. App mostra descrição de vantagem em diálogo; VTT não exibe consistentemente.
52. App mostra descrição de desvantagem em diálogo; VTT não exibe.
53. App mostra descrição de perícia em diálogo; VTT não exibe.
54. App mostra descrição de técnica em diálogo; VTT não exibe.
55. App mostra descrição de magia em diálogo; VTT não exibe.
56. App mostra especialização de perícia; VTT não permite editar.
57. App exige especialização quando `exigeEspecializacao`; VTT permite salvar vazio.
58. App bloqueia adicionar perícia duplicada sem especialização; VTT permite duplicar.
59. App mostra preview de NH no editor de perícia; VTT não mostra.
60. App permite ajuste de pontos com presets (botões +/–) por tabela; VTT não segue.
61. App calcula NH conforme tabela GURPS (F/M/D/MD); VTT não aplica mesma tabela.
62. App limita pontos por regra (se habilitado) na perícia; VTT não limita igual.
63. App exibe atributo base e dificuldade no card de perícia; VTT não.
64. App exibe NH relativo (ex.: DX-1); VTT não.
65. App mostra nível e custo final na lista; VTT mostra 0.
66. App permite editar perícia já adicionada; VTT quebra ao editar.
67. App permite remover perícia; VTT remove mas não recalcula.
68. App mantém `periciaBaseDefinicaoId` e `especializacao` em técnica; VTT não.
69. App valida se técnica está ligada à perícia base; VTT não bloqueia.
70. App calcula NH de técnica usando NH da perícia base + ajuste; VTT não.
71. App aplica `limiteMaximoRelativo` em técnica; VTT ignora.
72. App exibe `preRequisitoExibicao` da técnica; VTT não.
73. App separa técnicas por perícia base; VTT lista tudo.
74. App exibe nível da técnica (target) quando disponível; VTT não.
75. App bloqueia rolagem de técnica sem NH; VTT não indica indisponível.
76. App trata técnicas com custo mínimo; VTT não.
77. App possui catálogo de técnicas suplementares; VTT não integra.
78. App possui pericias suplementares (Artes Marciais/GunFu); VTT não integra.
79. App possui catalogos suplementares (vantagens/pericias) com ids; VTT não integra.
80. App exibe custos de equipamentos (custo base e total); VTT não.
81. App calcula peso total e carga; VTT não.
82. App calcula nível de carga e deslocamento; VTT não.
83. App calcula dano básico por ST; VTT não.
84. App calcula dano por arma com ST e tipo; VTT não.
85. App calcula ataque com arma e NH; VTT não.
86. App tem cálculo de bloqueio com escudo; VTT não.
87. App calcula `Apara` e `Bloqueio` com NH/2 + 3; VTT não.
88. App permite selecionar perícia de aparar/bloqueio; VTT não.
89. App mantém `periciaAparaId` e `periciaBloqueioId` no personagem; VTT não persiste.
90. App possui lista de perícias de combate para apara/bloqueio; VTT não usa.
91. App tem campos PV/PF atuais vs máximos; VTT não mantém.
92. App possui bônus manuais de aparar/bloqueio; VTT não.
93. App permite ajustar modificadores secundários; VTT não.
94. App exibe resumo de pontos por aba (Tracos/Pericias/Magias); VTT inconsistente.
95. App exibe resumo de itens e pontos em Traços; VTT incompleto.
96. App exibe resumo de perícias (total/pontos); VTT incompleto.
97. App exibe resumo de magias (total/aptidão/pontos); VTT incompleto.
98. App exibe resumo de técnicas (total/pontos); VTT incompleto.
99. App exibe resumo de equipamentos (itens/peso/custo); VTT incompleto.
100. App mantém campos de identificação (nome/jogador/campanha); VTT não salva corretamente.
101. App salva ficha persistente (Room/DAO); VTT não persiste entre recargas.
102. App carrega ficha automaticamente ao abrir; VTT abre vazio.
103. App exporta ficha JSON no formato do app; VTT exporta formato divergente.
104. App importa ficha JSON e normaliza; VTT falha com chaves antigas.
105. App mantém compatibilidade com versões antigas (magias/qualidades/tecnicas); VTT não.
106. App mantém `personagemInterop` para compatibilidade; VTT não usa.
107. App mantém `aviso` no JSON; VTT ignora.
108. App registra `pontosIniciais`; VTT não valida limites.
109. App atualiza `pontosRestantes` em tempo real; VTT não.
110. App limita pontos em vantagens/desvantagens conforme regra; VTT não.
111. App mostra custo por nível (Nível X) em vantagens; VTT não.
112. App mostra custo por nível em desvantagens; VTT não.
113. App permite editar nível de vantagem/desvantagem; VTT não.
114. App permite escolher custo (variável/escolha); VTT não.
115. App mostra seletor de custo quando necessário; VTT não.
116. App exibe marcador de autocontrole em desvantagens; VTT não.
117. App aplica custo por autocontrole (12/9/6/3); VTT não.
118. App permite editar autocontrole; VTT não.
119. App evita duplicar vantagens sem níveis; VTT permite duplicado.
120. App filtra vantagens por texto e tag; VTT filtra parcialmente.
121. App filtra desvantagens por texto e tag; VTT filtra parcialmente.
122. App possui campo de busca com debounce; VTT não.
123. App exibe listas com paginação virtual; VTT trava ao renderizar tudo.
124. App tem acessibilidade (contentDescription) em botões; VTT não.
125. App tem mensagens de erro amigáveis; VTT mostra overlay de erro JS.
126. App usa tipografia e layout otimizado para mobile; VTT não.
127. App tem suporte de tema para variante “pra cego”; VTT não.
128. App tem fluxos de confirmação ao sair da ficha; VTT não.
129. App tem validação antes de salvar; VTT salva mesmo inválido.
130. App impede fechar diálogo com dados inválidos; VTT fecha sem salvar.
131. App tem campo “Notas” por ficha; VTT não exibe.
132. App possui aba “Equipamentos” com catálogo e itens; VTT sem catálogo real.
133. App possui lista de armas de fogo/corpo-a-corpo/armaduras; VTT não integra.
134. App permite escolher armadura por local; VTT não.
135. App calcula RD por local; VTT não.
136. App calcula bônus de defesa por escudo; VTT não.
137. App calcula peso e custo por item; VTT não.
138. App permite marcar equipamento equipado; VTT não.
139. App organiza equipamentos por tipo; VTT não.
140. App permite remover equipamento; VTT remove sem recalcular carga.
141. App calcula deslocamento base por HT/DX; VTT não.
142. App calcula velocidade base; VTT não.
143. App calcula percepção/vontade base; VTT não.
144. App atualiza PV/PF com mods; VTT não.
145. App exibe PV/PF atuais e máximos; VTT não.
146. App permite alterar pontos atuais; VTT não.
147. App tem regras de dano (corte, perfuração, contusão) em combate; VTT não.
148. App calcula dano por arma e ST; VTT não.
149. App exibe “Resumo de pontos” na aba Geral; VTT parcial.
150. App exibe “Gastos” e “Restantes”; VTT não.
151. App atualiza “Gastos” ao alterar fichas; VTT não.
152. App tem categoria “Qualidades” e “Peculiaridades”; VTT apresenta mas não salva.
153. App permite adicionar qualidade com texto livre; VTT não.
154. App permite adicionar peculiaridade com texto livre; VTT não.
155. App mantém ordem e edição de qualidades/peculiaridades; VTT não.
156. App utiliza `DataRepository` com normalização de nomes; VTT não.
157. App remove acentuação para busca; VTT não.
158. App aplica normalização para comparar duplicados; VTT não.
159. App tem `tecnicasNomesNormalizados` para filtro; VTT não.
160. App tem cache por chave para magias filtradas; VTT não.
161. App mostra classes/escolas no filtro de magias; VTT não.
162. App aplica filtro “classe” na magia; VTT não.
163. App aplica filtro “escola”; VTT não.
164. App exibe energia/tempo/duração de magia; VTT não.
165. App trata magia “Talismã” com vínculo; VTT não.
166. App mostra custo de energia ajustado por NH; VTT não.
167. App aplica redução de energia ao lançar; VTT não.
168. App salva `pontosFadigaRolagemAtual` na rolagem; VTT não (sem rolagem).
169. App mantém `modificadoresPericia` por item; VTT não.
170. App mantém `modificadoresMagia` por item; VTT não.
171. App mantém `modificadoresTecnica` por item; VTT não.
172. App usa `RollDispatchPolicy` para rolagens; VTT não (sem rolagem).
173. App tem `ContextLabel` para ações; VTT não.
174. App integra “Modo Alvo” com magias específicas; VTT não.
175. App mantém “Aptidão Mágica” (vantagem) refletindo magias; VTT não.
176. App recalcula NH de magias quando Aptidão muda; VTT não.
177. App recalcula NH de perícias quando atributo base muda; VTT não.
178. App recalcula NH de técnicas quando perícia base muda; VTT não.
179. App impede pontos negativos em técnicas; VTT não.
180. App permite “Pontos gastos” em perícia sem limite 12 (alteração pedida); VTT mantém limite antigo.
181. App mantém campos `preRequisitoRaw` para vantagens/desvantagens suplementares; VTT não.
182. App mostra `preRequisito` no diálogo de item; VTT não.
183. App usa `PericiaSelecionada.calcularNivel` com atributo/dificuldade; VTT não usa função equivalente.
184. App usa `MagiaSelecionada.calcularNivel` com aptidão e atributo; VTT não.
185. App calcula nível de técnica por `TecnicaSelecionada.calcularNivel`; VTT não.
186. App mantém `limiteMaximoRelativo` em técnica; VTT não.
187. App suporta técnica com `limiteKind` (NENHUM/RELATIVO/ABSOLUTO); VTT não.
188. App exibe “perícia base” na técnica; VTT não.
189. App exibe “nível relativo” para perícia; VTT não.
190. App permite escolher atributo base alternativo em perícia custom; VTT não.
191. App permite criar perícia custom; VTT não.
192. App permite criar técnica custom; VTT não.
193. App permite criar magia custom; VTT não.
194. App possui catálogos suplementares (Gun Fu/Artes Marciais); VTT não.
195. App tem regras específicas hardcoded no ViewModel para magias; VTT não.
196. App tem regras específicas hardcoded para vantagens; VTT não.
197. App calcula `custoFinal` com coerção e arredondamento; VTT não.
198. App converte custos string para int com regex; VTT não.
199. App mantém `custoEscolhido` para custo variável; VTT não.
200. App aplica custo por nível para vantagens/desvantagens; VTT não.
201. App permite editar nome do personagem e salva; VTT perde ao fechar.
202. App tem UI de confirmação ao excluir item; VTT não.
203. App mostra rótulos e campos na ordem específica; VTT layout diferente.
204. App usa abas com ícones/hex; VTT usa tabs simples.
205. App mantém foco e teclado adequado; VTT não.
206. App tem validação de campo vazio em diálogos; VTT não.
207. App tem textos de ajuda e placeholders; VTT não.
208. App mostra custos no formato “X pts”; VTT mostra número cru.
209. App mostra nível no formato “Nível X”; VTT não.
210. App mostra “Pontos gastos” com stepper; VTT não.
211. App tem seletor de especialização com autocomplete; VTT não.
212. App mostra lista de perícias com atributo/dificuldade; VTT não.
213. App mostra “preRequisito” somente se não vazio; VTT não.
214. App trata `descricao` vazia como “Sem descrição disponível”; VTT quebra.
215. App possui normalização de `descricao` com fallback; VTT não.
216. App possui `DataRepository.filtrarMagias` com index; VTT filtra em UI (lento).
217. App possui `DataRepository` normalizando IDs; VTT não.
218. App organiza listas por nome; VTT não ordena.
219. App usa acentos corretamente na busca; VTT não.
220. App armazena `campanha` no personagem; VTT não.
221. App armazena `jogador` no personagem; VTT não.
222. App armazena `notas`; VTT não.
223. App armazena `pontosIniciais`; VTT não valida.
224. App atualiza `pontosRestantes` = iniciais - gastos; VTT não.
225. App mostra “Gastos” e “Restantes” no topo; VTT não.
226. App possui “resumo por aba” sempre visível; VTT incompleto.
227. App possui botões de ação consistentes (Salvar/Editar); VTT fluxos quebrados.
228. App salva automático ao sair; VTT perde se não clicar salvar.
229. App possui persistência local via Room; VTT não tem persistência local equivalente.
230. App possui import/export com compatibilidade; VTT não.
231. App mantém chaves JSON exatas do app; VTT muda nomes.
232. App mantém campos `descricao` nos itens selecionados; VTT não.
233. App mantém `especializacao` nos itens; VTT não.
234. App mantém `pontosGastos` em cada item; VTT não.
235. App mantém `id`/`definicaoId`; VTT perde.
236. App mantém `nivel` para vantagens/desvantagens; VTT não.
237. App mantém `autocontrole` para desvantagens; VTT não.
238. App mantém `custoBase` e `custoEscolhido`; VTT não.
239. App mantém `tipoCusto` (“POR_NIVEL”, etc.); VTT não.
240. App mantém `preRequisitoRaw` em vantagens/desvantagens; VTT não.
241. App mantém `classe` e `escola` em magia; VTT não.
242. App mantém `energia`, `duracao`, `tempoOperacao`; VTT não.
243. App mantém `alcance` e `resistencia` quando existirem; VTT não.
244. App mantém `pagina` de referência; VTT não.
245. App mantém `especializacao` obrigatória em certas perícias; VTT não.
246. App mantém `limiteDesvantagens` com regra desabilitável; VTT não expõe toggle.
247. App possui variantes “visual” e “pra cego”; VTT não.
248. App possui elementos de acessibilidade nas listas; VTT não.
249. App possui `Dialog` para descrição; VTT não.
250. App possui ação “Remover imagem” no perfil; VTT não.
251. App permite definir imagem do token; VTT não na ficha.
252. App mantém relação ficha->token; VTT não.
253. App sincroniza ficha com VTT via payload; VTT não.
254. App usa `PersonagemInterop` para mapear campos; VTT não.
255. App define `pontosVidaBase`, `pontosFadigaBase`; VTT não.
256. App usa `velocidadeBasica` com cálculo; VTT não.
257. App usa `deslocamentoBasico` com cálculo; VTT não.
258. App mantém `modVelocidadeBasica` e `modDeslocamentoBasico`; VTT não.
259. App atualiza `vitalidadeBase`, `destrezaBase` etc; VTT não.
260. App calcula `percepcao` e `vontade` com mods; VTT não.
261. App usa `Caracteristicas` derivadas; VTT não.
262. App trata `carga` por tabelas; VTT não.
263. App inclui arma/armadura/escudo catálogos com `custoBase`, `peso`; VTT não.
264. App permite equipar/desquipar e ajustar RD; VTT não.
265. App calcula `pesoTotal` e `custoTotal` de inventário; VTT não.
266. App separa “Equipamentos” e “Defesas”; VTT não implementa “Defesas”.
267. App tem aba “Defesas” com cálculo automático; VTT ausente.
268. App permite ajustar `bonusManualApara`, `bonusManualBloqueio`, `bonusManualEsquiva`; VTT não.
269. App calcula `esquiva` com velocidade + 3; VTT não.
270. App mantém `desvantagensExcedemLimite`; VTT não.
271. App permite editar `pontosIniciais`; VTT não.
272. App tem fluxo de “Salvar ficha” visível; VTT não confirma.
273. App mostra “Resumo de Traços” com totais; VTT não.
274. App mostra “Resumo de Perícias” com total/pontos; VTT não.
275. App mostra “Resumo de Magias” com aptidão; VTT não.
276. App mostra “Resumo de Técnicas”; VTT não.
277. App mostra “Resumo de Equipamentos”; VTT não.
278. App mostra custo em moedas (quando aplicável); VTT não.
279. App trata equipamentos com quantidade; VTT não.
280. App mostra peso por item; VTT não.
281. App permite editar quantidade; VTT não.
282. App mostra `tipo` do equipamento; VTT não.
283. App permite filtrar equipamentos por tipo; VTT não.
284. App permite remover em lote; VTT não.
285. App permite exportar ficha PDF; VTT não.
286. App mantém `notas` por item; VTT não.
287. App mantém `definicaoId` e `nome` separados; VTT mistura.
288. App usa cálculo de `custoFinal` com `tipoCusto`; VTT ignora `tipoCusto`.
289. App mantém `custoEscolhido` para VARIAVEL/ESCOLHA; VTT não.
290. App mostra mensagens de erro específicas (ex.: “Sem perícias”); VTT não.
291. App bloqueia adicionar item sem nome; VTT não.
292. App mostra `preRequisito` apenas quando existe; VTT quebra ao renderizar `undefined`.
293. App trata `descricao` null como string vazia; VTT lança erro.
294. App trata `preRequisitoRaw` ausente com fallback; VTT lança erro.
295. App trata `especializacao` nula; VTT lança erro.
296. App mostra `NH` em pericias/magias/tecnicas; VTT não.
297. App mostra `NH` de técnica baseado na perícia base; VTT não.
298. App mantém `bonusManualApara/Bloqueio/Esquiva`; VTT não.
299. App calcula `esquiva` com `velocidadeBasica` arredondada; VTT não.
300. App exibe `titulo` e `labels` consistentes; VTT layout divergente.
301. App inclui abas “Geral/Traços/Perícias/Técnicas/Magias/Equip./Defesas/Notas”; VTT não tem todas operacionais.
302. App salva ficha por personagem; VTT perde ao trocar personagem.
303. App tem mecanismo de `DataRepository` central; VTT espalha estado.
304. App tem normalização de catálogo (ids únicos); VTT não.
305. App respeita ordem alfabética de catálogo; VTT não.
306. App usa placeholders “Ex.: Aragon”; VTT não.
307. App possui validação de `nome` obrigatório; VTT não.
308. App possui logs internos de erro controlados; VTT mostra overlay.
309. App integra `CatalogosSuplementares` (vantagens/pericias adicionais); VTT não.
310. App usa `MagiaEnergiaRules.reducaoPorNh`; VTT não.
311. App usa `CharacterRules.calcularPontosSecundarios`; VTT não.
312. App usa `CombatRules` para dano/defesa; VTT não.
313. App possui `NexusArcanoModoAlvoAdapter` para magias; VTT não.
314. App atualiza campos de ficha quando muda atributo base; VTT não.
315. App atualiza pontos gastos em tempo real; VTT não.
316. App impede inconsistências ao editar item; VTT não.
317. App mantém `preRequisitoExibicao` para técnica; VTT não.
318. App permite editar pontos de técnica; VTT não.
319. App limita técnica por NH da perícia base; VTT não.
320. App inclui `periciaBaseNome` em técnica; VTT não.
321. App inclui `limiteMaximoRelativo` em técnica; VTT não.
322. App inclui `limiteKind` (relativo/absoluto); VTT não.
323. App exibe “Pontos gastos” e “NH” no editor de técnica; VTT não.
324. App usa `PericiaDefinicao` com atributo/dificuldade; VTT não mostra na seleção.
325. App usa `PericiaSelecionada` com `atributoBase` e `dificuldade`; VTT não.
326. App usa `MagiaDefinicao` com `classe`/`escola`; VTT não filtra.
327. App possui regra para `aptidao_m` influenciar magias; VTT não.
328. App tem regra de `pontosGastos` mínimos para magias; VTT não.
329. App trata `pontosGastos` mínimos de técnicas; VTT não.
330. App exibe barra de PV/PF; VTT não.
331. App permite ajuste manual de PV/PF; VTT não.
332. App tem regra de `pontosFadiga` para magia; VTT não.
333. App exibe aviso quando `desvantagensExcedemLimite`; VTT não.
334. App separa `qualidades` e `peculiaridades` com pontos; VTT não.
335. App possui UI de edição inline com modais; VTT quebra.
336. App mantém estado por personagem; VTT não.
337. App permite exportar/importar ficha e reabrir sem perda; VTT perde dados.
338. App mantém `tokenImage` na ficha; VTT não.
339. App mantém `tokenColor` quando não há imagem; VTT não.
340. App sincroniza ficha e token no VTT; VTT não.
341. App salva dados no device; VTT não persiste no navegador.
342. App possui controle de versão de ficha; VTT não.
343. App trata `pontosRestantes` negativos sem crash; VTT pode quebrar.
344. App trata `pontosIniciais` quando vazio; VTT não.
345. App mostra mensagens de “Sem descrição disponível” para campos vazios; VTT não.
346. App suporta filtro por “classe” em magias; VTT não.
347. App suporta filtro por “escola” em magias; VTT não.
348. App suporta busca por substring em magias; VTT não.
349. App suporta busca por substring em pericias; VTT não.
350. App suporta busca por substring em vantagens/desvantagens; VTT não.
351. App aplica normalização (sem acento) ao buscar; VTT não.
352. App evita travar UI em listas grandes com cache; VTT não.
353. App mostra `pagina` da magia/perícia para referência; VTT não.
354. App mostra `preRequisito` como texto legível; VTT não.
355. App mostra `preRequisito` apenas quando não “-”; VTT não.
356. App mantém `contextLabel` por ação; VTT não.
357. App mantém `target` por técnica/magia/perícia; VTT não.
358. App mostra `target` em UI; VTT não.
359. App calcula `target` automaticamente; VTT não.
360. App trata “Especialização” de perícia como parte do nome; VTT não.
361. App cria label de perícia com especialização; VTT não.
362. App usa `periciaLabel` em VTT; VTT não no frontend.
363. App tem regra de seleção de `periciaAparaId` e `periciaBloqueioId`; VTT não.
364. App calcula `esquiva` com arredondamento; VTT não.
365. App mantém `bonusDefesa` em armaduras; VTT não.
366. App mantém `armaduraLocal` e `RD` por local; VTT não.
367. App mantém `peso` e `quantidade` de armaduras; VTT não.
368. App integra escudos com `db` e `stMinimo`; VTT não.
369. App integra armas com `dano` e `alcance`; VTT não.
370. App integra armas de fogo com `cadencia`, `alcance`, `recuo`; VTT não.
371. App integra armas corpo a corpo com `alcance` e `aparar`; VTT não.
372. App possui catálogo de armas suplementares; VTT não.
373. App possui catálogo de armaduras suplementares; VTT não.
374. App possui catálogo de escudos suplementares; VTT não.
375. App mantém `custoBase` e `pesoBase` em itens; VTT não.
376. App permite editar custo/peso manual; VTT não.
377. App possui `Notas` com texto livre; VTT não.
378. App possui `Aparencia`/`Campanha`/`Jogador`; VTT não.
379. App mantém `pontosIniciais` e recalcula pontos restantes; VTT não.
380. App possui `modPontosVida` e `modPontosFadiga`; VTT não.
381. App tem `modPercepcao` e `modVontade`; VTT não.
382. App permite editar `modVelocidadeBasica` e `modDeslocamentoBasico`; VTT não.
383. App calcula `velocidadeBasica` e `deslocamento` automaticamente; VTT não.
384. App usa `CharacterRules` para dano básico; VTT não.
385. App usa `CombatRules` para dano por arma; VTT não.
386. App usa `MagiaEnergiaRules` para custo por NH; VTT não.
387. App mantém `dataRepository` com todos catálogos; VTT não carrega todos.
388. App resolve `definicaoId` em catálogos; VTT falha em IDs.
389. App mantém `especializacao` em técnicas/perícias; VTT perde.
390. App exibe resumo “Total Traços” com pontos; VTT não.
391. App exibe “Total Perícias” e “Média NH”; VTT não.
392. App exibe “Total Magias” e “Aptidão Mágica”; VTT não.
393. App exibe “Total Técnicas”; VTT não.
394. App exibe “Peso total” em equipamentos; VTT não.
395. App exibe “Custo total” em equipamentos; VTT não.
396. App lista “Qualidades” e “Peculiaridades” separadas; VTT não salva.
397. App mantém `pontosGastos` por item com validação; VTT não.
398. App permite editar `pontosGastos` e recalcular NH; VTT não.
399. App mostra `NH` atualizado ao editar; VTT não.
400. App mantém consistência após salvar/reabrir; VTT perde ao fechar.

401. App bloqueia adição de vantagem quando catálogo não carregou; VTT não bloqueia.
402. App bloqueia adição de desvantagem quando catálogo não carregou; VTT não bloqueia.
403. App exibe indicador de carregamento de catálogos; VTT não.
404. App atualiza UI após importação sem recarregar tela; VTT precisa refresh.
405. App registra `ultimaEdicao` da ficha; VTT não.
406. App aplica correção de custo para vantagens com “/n” e nível 0; VTT não.
407. App aplica correção para custos com “-” no texto; VTT não.
408. App ignora símbolos inválidos em custo; VTT quebra.
409. App aceita custos fracionários e arredonda; VTT não.
410. App trata custo base 0 com nível > 1 (mantém 0); VTT aplica errado.
411. App mantém `custoEscolhido` ao editar vantagem; VTT perde.
412. App mantém `custoEscolhido` ao editar desvantagem; VTT perde.
413. App permite editar descrição manual de vantagem escolhida; VTT não.
414. App permite editar descrição manual de desvantagem escolhida; VTT não.
415. App mantém `descricao` do catálogo como fallback; VTT não.
416. App mostra `pagina` do livro na descrição; VTT não.
417. App mostra `origem`/livro quando disponível; VTT não.
418. App aplica filtro “tipo de custo” em vantagens/desvantagens; VTT não.
419. App aplica filtro “classe” em magias; VTT não.
420. App aplica filtro “escola” em magias; VTT não.
421. App mantém busca de magias por nome sem acento; VTT não.
422. App evita duplicar magia já conhecida; VTT permite.
423. App evita duplicar técnica já adicionada; VTT permite.
424. App evita duplicar equipamento idêntico sem quantidade; VTT duplica.
425. App permite combinar quantidades de equipamento iguais; VTT não.
426. App calcula `pesoTotal` incluindo quantidade; VTT não.
427. App calcula `custoTotal` incluindo quantidade; VTT não.
428. App atualiza `carga` ao alterar equipamento; VTT não.
429. App atualiza `deslocamentoAtual` ao alterar carga; VTT não.
430. App atualiza `esquiva` ao alterar carga; VTT não.
431. App aplica `nivelCarga` no resumo; VTT não.
432. App exibe `cargaMax` calculada; VTT não.
433. App exibe `deslocamentoAtual` com arredondamento; VTT não.
434. App permite marcar arma ativa/primária; VTT não.
435. App permite selecionar defesa ativa padrão; VTT não.
436. App permite selecionar escudo ativo; VTT não.
437. App recalcula `bloqueio` ao trocar escudo; VTT não.
438. App recalcula `aparar` ao trocar arma; VTT não.
439. App permite configurar `bonusManualEsquiva`; VTT não.
440. App permite configurar `bonusManualApara`; VTT não.
441. App permite configurar `bonusManualBloqueio`; VTT não.
442. App mostra `Apara/Bloqueio/Esquiva` em aba Defesas; VTT não.
443. App usa `periciasDeCombate` para aparar/bloquear; VTT não.
444. App permite escolher perícia de combate para aparar; VTT não.
445. App permite escolher perícia de combate para bloquear; VTT não.
446. App usa cálculo de `bonusDefesa` por armadura; VTT não.
447. App mantém `armaduraLocal` e soma RD por local; VTT não.
448. App possui editor de armaduras por local; VTT não.
449. App possui editor de escudos com `db` e `stMinimo`; VTT não.
450. App possui editor de armas com dano/alcance; VTT não.
451. App suporta armas de fogo com cadência/recarga; VTT não.
452. App suporta armas com `STMin` e penalidades; VTT não.
453. App suporta armas com `alcance` e `peso`; VTT não.
454. App suporta equipamentos genéricos com custo livre; VTT não.
455. App suporta notas por equipamento; VTT não.
456. App permite importar/exportar somente equipamentos; VTT não.
457. App permite exportar ficha limpa (sem rolagem); VTT não.
458. App permite zerar ficha mantendo pontos iniciais; VTT não.
459. App permite duplicar ficha; VTT não.
460. App mantém histórico de fichas no armazenamento; VTT não.
461. App permite selecionar ficha recente; VTT não.
462. App tem validação de número em campos de pontos; VTT não.
463. App mostra erro se campo numérico inválido; VTT quebra.
464. App corrige automaticamente espaços extras em nomes; VTT não.
465. App remove caracteres inválidos em nomes; VTT não.
466. App garante `definicaoId` único ao importar; VTT não.
467. App mantém compatibilidade com JSON sem `preRequisitoRaw`; VTT não.
468. App mantém compatibilidade com JSON sem `magias`; VTT não.
469. App mantém compatibilidade com JSON sem `tecnicas`; VTT não.
470. App mantém compatibilidade com JSON sem `qualidades`; VTT não.
471. App mantém compatibilidade com JSON sem `peculiaridades`; VTT não.
472. App garante `pontosGastos` mínimo nas magias; VTT não.
473. App garante `pontosGastos` mínimo nas técnicas; VTT não.
474. App garante `pontosGastos` não negativos nas perícias; VTT não.
475. App valida especialização obrigatória antes de salvar; VTT não.
476. App valida nível de vantagem/desvantagem > 0; VTT não.
477. App valida custos variáveis antes de salvar; VTT não.
478. App valida se pré-requisitos estão atendidos e exibe alerta; VTT não.
479. App sinaliza magias bloqueadas por pré-requisito; VTT não.
480. App sinaliza técnicas bloqueadas por pré-requisito; VTT não.
481. App sinaliza perícias bloqueadas por pré-requisito; VTT não.
482. App mostra ícones/cores para status de pré-requisito; VTT não.
483. App exibe tooltip de pré-requisito; VTT não.
484. App permite selecionar “modo alvo” dentro da ficha; VTT não.
485. App mantém seleção de “modo alvo” por magia; VTT não.
486. App aplica regras de “modo alvo” nas ações; VTT não.
487. App possui controles de acessibilidade para leitores; VTT não.
488. App mantém layout por aba com seções colapsáveis; VTT não.
489. App mantém scroll automático para erro; VTT não.
490. App destaca campos inválidos; VTT não.
491. App possui reorganização de itens por arraste; VTT não.
492. App permite copiar item (duplicar) com ajustes; VTT não.
493. App permite colar listas de qualidades/peculiaridades; VTT não.
494. App permite exportar ficha com versão; VTT não.
495. App mantém `schemaVersion` no JSON; VTT não.
496. App atualiza `schemaVersion` ao salvar; VTT não.
497. App mostra `Restantes` em vermelho quando negativo; VTT não.
498. App impede salvar se campos críticos faltam; VTT não.
499. App impede fechar sem salvar (aviso); VTT não.
500. App preserva `pontosIniciais` ao importar/exportar; VTT não.
