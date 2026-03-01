package com.gurps.ficha.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gurps.ficha.BuildConfig
import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.network.DiscordRollApiClient
import com.gurps.ficha.data.network.DiscordRollPayload
import com.gurps.ficha.data.network.DiscordVoiceChannel
import com.gurps.ficha.data.storage.FichaStorageRepository
import com.gurps.ficha.domain.roll.RollDispatchPolicy
import com.gurps.ficha.domain.rules.CharacterRules
import com.gurps.ficha.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Normalizer

enum class DefenseType { ESQUIVA, APARA, BLOQUEIO }

data class ActiveDefense(
    val type: DefenseType,
    val name: String,
    val baseValue: Int,
    val bonus: Int,
    val finalValue: Int
)

data class RollDispatchStatus(
    val enviado: Boolean,
    val detalhe: String? = null
)

@OptIn(FlowPreview::class)
class FichaViewModel(application: Application) : AndroidViewModel(application) {
    private val autoSaveRecuperacaoNome = "_autosave_recuperacao"

    var personagem by mutableStateOf(Personagem())
        private set

    var fichasSalvas by mutableStateOf<List<String>>(emptyList())
        private set

    var mostrarConfirmacaoLimpezaMagias by mutableStateOf(false)
        private set

    // Estado de busca e filtros
    var buscaVantagem by mutableStateOf("")
        private set
    var filtroTipoCustoVantagem by mutableStateOf<TipoCusto?>(null)
        private set

    var buscaDesvantagem by mutableStateOf("")
        private set
    var filtroTipoCustoDesvantagem by mutableStateOf<TipoCusto?>(null)
        private set

    var buscaPericia by mutableStateOf("")
        private set
    var filtroAtributoPericia by mutableStateOf<String?>(null)
        private set
    var filtroDificuldadePericia by mutableStateOf<String?>(null)
        private set

    var buscaMagia by mutableStateOf("")
        private set
    var filtroEscolaMagia by mutableStateOf<String?>(null)
        private set
    var filtroClasseMagia by mutableStateOf<String?>(null)
        private set
    var buscaTecnica by mutableStateOf("")
        private set
    var filtroFonteTecnica by mutableStateOf<String?>(null)
        private set

    var buscaArmaEquipamento by mutableStateOf("")
        private set
    var filtroTipoArmaEquipamento by mutableStateOf<String?>(null) // "corpo_a_corpo" | "distancia" | "armas_de_fogo" | null
        private set
    var filtroCategoriaArmaFogoEquipamento by mutableStateOf<String?>(null) // "pistolas_mm" | "rifles_espingardas" | "ultratech" | "pesadas" | null
        private set
    var buscaEscudoEquipamento by mutableStateOf("")
        private set
    var buscaArmaduraEquipamento by mutableStateOf("")
        private set
    var filtroNtArmaduraEquipamento by mutableStateOf<Int?>(null)
        private set
    var filtroLocalArmaduraEquipamento by mutableStateOf<String?>(null)
        private set
    var filtroTagArmaduraEquipamento by mutableStateOf<String?>(null)
        private set

    private val fichaStorage = FichaStorageRepository.getInstance(application)
    val dataRepository = DataRepository.getInstance(application)
    private val configPrefs = application.getSharedPreferences("gurps_config", Context.MODE_PRIVATE)
    private var personagemPendenteLimpezaMagias: Personagem? = null
    private val prefCanalDiscordId = "discord_canal_id"
    private val prefCanalDiscordNome = "discord_canal_nome"

    var canaisDiscord by mutableStateOf<List<DiscordVoiceChannel>>(emptyList())
        private set
    var canaisDiscordCarregando by mutableStateOf(false)
        private set
    var canaisDiscordErro by mutableStateOf<String?>(null)
        private set
    var canalDiscordSelecionadoId by mutableStateOf<String?>(null)
        private set
    var canalDiscordSelecionadoNome by mutableStateOf<String?>(null)
        private set

    // Listas filtradas
    val vantagensFiltradas: List<VantagemDefinicao>
        get() = dataRepository.filtrarVantagens(buscaVantagem, filtroTipoCustoVantagem, null)

    val desvantagensFiltradas: List<DesvantagemDefinicao>
        get() = dataRepository.filtrarDesvantagens(buscaDesvantagem, filtroTipoCustoDesvantagem)

    val periciasFiltradas: List<PericiaDefinicao>
        get() = dataRepository.filtrarPericias(buscaPericia, filtroAtributoPericia, filtroDificuldadePericia)

    val periciasSuplementaresArtesMarciais: List<PericiaSuplementarItem>
        get() = dataRepository.periciasSuplementares

    val magiasFiltradas: List<MagiaDefinicao>
        get() = dataRepository.filtrarMagias(buscaMagia, filtroEscolaMagia, filtroClasseMagia)

    val tecnicasCatalogo: List<TecnicaCatalogoItem>
        get() = dataRepository.tecnicasCatalogo

    val tecnicasFiltradas: List<TecnicaCatalogoItem>
        get() = dataRepository.filtrarTecnicasCatalogo(buscaTecnica, filtroFonteTecnica)

    val armasEquipamentosFiltradas: List<ArmaCatalogoItem>
        get() {
            val base = dataRepository.filtrarArmasCatalogo(
            busca = buscaArmaEquipamento,
            tipoCombate = filtroTipoArmaEquipamento,
            stMaximo = personagem.forca
        )
            val categoriaFiltro = filtroCategoriaArmaFogoEquipamento
            if (categoriaFiltro.isNullOrBlank()) return base
            return base.filter { arma ->
                arma.tipoCombate == "armas_de_fogo" &&
                    categoriaArmaFogoParaFiltro(arma) == categoriaFiltro
            }
        }

    val escudosEquipamentosFiltrados: List<EscudoCatalogoItem>
        get() = dataRepository.filtrarEscudosCatalogo(
            busca = buscaEscudoEquipamento,
            stMaximo = personagem.forca
        )

    val armadurasEquipamentosFiltradas: List<ArmaduraCatalogoItem>
        get() = dataRepository.filtrarArmadurasCatalogo(
            busca = buscaArmaduraEquipamento,
            nt = filtroNtArmaduraEquipamento,
            localFiltro = filtroLocalArmaduraEquipamento,
            tagFiltro = filtroTagArmaduraEquipamento
        )

    val tagsArmadurasEquipamentos: List<String>
        get() = dataRepository.armadurasCatalogo
            .asSequence()
            .flatMap { armadura ->
                sequenceOf(armadura.tags) + armadura.componentes.map { it.tags }
            }
            .flatMap { it.asSequence() }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { it.startsWith("local:", ignoreCase = true) }
            .filterNot { it.startsWith("local_exp:", ignoreCase = true) }
            .filterNot { it.startsWith("nt:", ignoreCase = true) }
            .filterNot { it.startsWith("tipo:", ignoreCase = true) }
            .distinct()
            .sorted()
            .toList()

    val errosCargaCatalogos: Map<String, String>
        get() = dataRepository.getCatalogLoadErrors()

    val escudosEquipados: List<Equipamento>
        get() = personagem.equipamentos
            .filter { it.tipo == TipoEquipamento.ESCUDO }
            .sortedBy { it.nome.lowercase() }

    val todasEscolasMagia: List<String>
        get() = dataRepository.magias
            .flatMap { it.escola ?: emptyList() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

    val todasClassesMagia: List<String>
        get() = dataRepository.magias
            .mapNotNull { dataRepository.agruparClasseMagia(it.classe) }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

    init {
        canalDiscordSelecionadoId = configPrefs.getString(prefCanalDiscordId, null)
        canalDiscordSelecionadoNome = configPrefs.getString(prefCanalDiscordNome, null)

        viewModelScope.launch {
            fichaStorage.migrarDeSharedPreferencesSeNecessario()
            restaurarAutoSaveSeExistir()
            carregarListaFichas()
        }

        viewModelScope.launch {
            snapshotFlow { personagem.toJson() }
                .distinctUntilChanged()
                .debounce(600)
                .collect { json ->
                    fichaStorage.salvarFicha(autoSaveRecuperacaoNome, json)
                }
        }
    }

    // === FILTROS ===

    fun atualizarBuscaVantagem(busca: String) {
        buscaVantagem = busca
    }

    fun atualizarFiltroTipoCustoVantagem(tipo: TipoCusto?) {
        filtroTipoCustoVantagem = tipo
    }

    fun atualizarBuscaDesvantagem(busca: String) {
        buscaDesvantagem = busca
    }

    fun atualizarFiltroTipoCustoDesvantagem(tipo: TipoCusto?) {
        filtroTipoCustoDesvantagem = tipo
    }

    fun atualizarBuscaPericia(busca: String) {
        buscaPericia = busca
    }

    fun atualizarFiltroAtributoPericia(atributo: String?) {
        filtroAtributoPericia = atributo
    }

    fun atualizarFiltroDificuldadePericia(dificuldade: String?) {
        filtroDificuldadePericia = dificuldade
    }

    fun atualizarBuscaMagia(busca: String) {
        buscaMagia = busca
    }

    fun atualizarFiltroEscolaMagia(escola: String?) {
        filtroEscolaMagia = escola
    }

    fun atualizarFiltroClasseMagia(classe: String?) {
        filtroClasseMagia = classe
    }

    fun atualizarBuscaTecnica(busca: String) {
        buscaTecnica = busca
    }

    fun atualizarFiltroFonteTecnica(fonte: String?) {
        filtroFonteTecnica = fonte
    }

    fun atualizarBuscaArmaEquipamento(busca: String) {
        buscaArmaEquipamento = busca
    }

    fun atualizarFiltroTipoArmaEquipamento(tipo: String?) {
        filtroTipoArmaEquipamento = tipo
        if (tipo != "armas_de_fogo") {
            filtroCategoriaArmaFogoEquipamento = null
        }
    }

    fun atualizarFiltroCategoriaArmaFogoEquipamento(categoria: String?) {
        filtroCategoriaArmaFogoEquipamento = categoria
    }

    fun atualizarBuscaEscudoEquipamento(busca: String) {
        buscaEscudoEquipamento = busca
    }

    fun atualizarBuscaArmaduraEquipamento(busca: String) {
        buscaArmaduraEquipamento = busca
    }

    fun atualizarFiltroNtArmaduraEquipamento(nt: Int?) {
        filtroNtArmaduraEquipamento = nt
    }

    fun atualizarFiltroLocalArmaduraEquipamento(local: String?) {
        filtroLocalArmaduraEquipamento = local
    }

    fun atualizarFiltroTagArmaduraEquipamento(tag: String?) {
        filtroTagArmaduraEquipamento = tag?.trim()?.takeIf { it.isNotBlank() }
    }

    fun limparFiltrosArmaduraEquipamento() {
        buscaArmaduraEquipamento = ""
        filtroNtArmaduraEquipamento = null
        filtroLocalArmaduraEquipamento = null
        filtroTagArmaduraEquipamento = null
    }

    // === INFORMACOES BASICAS ===

    fun atualizarNome(nome: String) {
        personagem = personagem.copy(nome = nome)
    }

    fun atualizarJogador(jogador: String) {
        personagem = personagem.copy(jogador = jogador)
    }

    fun atualizarCampanha(campanha: String) {
        personagem = personagem.copy(campanha = campanha)
    }

    fun atualizarPontosIniciais(pontos: Int) {
        personagem = personagem.copy(pontosIniciais = pontos.coerceIn(0, 1000))
    }

    fun atualizarLimiteDesvantagens(limite: Int) {
        personagem = personagem.copy(limiteDesvantagens = limite.coerceIn(-200, 0))
    }

    // === ATRIBUTOS PRIMARIOS ===

    fun atualizarForca(valor: Int) {
        personagem = personagem.copy(forca = valor.coerceIn(1, 30))
    }

    fun atualizarDestreza(valor: Int) {
        personagem = personagem.copy(destreza = valor.coerceIn(1, 30))
    }

    fun atualizarInteligencia(valor: Int) {
        personagem = personagem.copy(inteligencia = valor.coerceIn(1, 30))
    }

    fun atualizarVitalidade(valor: Int) {
        personagem = personagem.copy(vitalidade = valor.coerceIn(1, 30))
    }

    fun definirBasesAtributosPrimarios(
        forcaBase: Int,
        destrezaBase: Int,
        inteligenciaBase: Int,
        vitalidadeBase: Int
    ) {
        val novaForcaBase = forcaBase.coerceIn(1, 30)
        val novaDestrezaBase = destrezaBase.coerceIn(1, 30)
        val novaInteligenciaBase = inteligenciaBase.coerceIn(1, 30)
        val novaVitalidadeBase = vitalidadeBase.coerceIn(1, 30)

        personagem = personagem.copy(
            forcaBase = novaForcaBase,
            destrezaBase = novaDestrezaBase,
            inteligenciaBase = novaInteligenciaBase,
            vitalidadeBase = novaVitalidadeBase,
            forca = novaForcaBase,
            destreza = novaDestrezaBase,
            inteligencia = novaInteligenciaBase,
            vitalidade = novaVitalidadeBase
        )
    }

    // === MODIFICADORES SECUNDARIOS ===

    fun atualizarModPontosVida(valor: Int) {
        personagem = personagem.copy(modPontosVida = valor.coerceIn(-20, 20))
    }

    fun atualizarModVontade(valor: Int) {
        personagem = personagem.copy(modVontade = valor.coerceIn(-20, 20))
    }

    fun atualizarModPercepcao(valor: Int) {
        personagem = personagem.copy(modPercepcao = valor.coerceIn(-20, 20))
    }

    fun atualizarModPontosFadiga(valor: Int) {
        personagem = personagem.copy(modPontosFadiga = valor.coerceIn(-20, 20))
    }

    fun atualizarModVelocidadeBasica(valor: Float) {
        val valorNormalizado = CharacterRules
            .calcularPassosVelocidadeBasica(valor.coerceIn(-5f, 5f)) * 0.25f
        personagem = personagem.copy(modVelocidadeBasica = valorNormalizado)
    }

    fun atualizarModDeslocamentoBasico(valor: Int) {
        personagem = personagem.copy(modDeslocamentoBasico = valor.coerceIn(-10, 10))
    }

    fun atualizarPontosVidaRolagemAtual(valor: Int?) {
        val maxPvRolagem = (personagem.pontosVida.coerceAtLeast(0) * 5).coerceAtLeast(0)
        personagem = personagem.copy(
            pontosVidaRolagemAtual = valor?.coerceIn(0, maxPvRolagem)
        )
    }

    fun atualizarPontosFadigaRolagemAtual(valor: Int?) {
        personagem = personagem.copy(
            pontosFadigaRolagemAtual = valor?.coerceAtLeast(0)
        )
    }

    // === VANTAGENS ===

    fun adicionarVantagem(definicao: VantagemDefinicao, nivel: Int = 1, custoEscolhido: Int = 0, descricao: String = "") {
        // Verifica duplicatas
        if (personagem.vantagens.any { it.definicaoId == definicao.id && it.descricao == descricao }) {
            return // Ja existe
        }
        val nivelNormalizado = normalizarNivelVantagem(definicao.id, nivel)
        val vantagem = dataRepository.criarVantagemSelecionada(definicao, nivelNormalizado, custoEscolhido, descricao)
        val lista = personagem.vantagens.toMutableList()
        lista.add(vantagem)
        atualizarVantagensComConfirmacao(lista)
    }

    fun removerVantagem(index: Int) {
        val lista = personagem.vantagens.toMutableList()
        if (index in lista.indices) {
            lista.removeAt(index)
            atualizarVantagensComConfirmacao(lista)
        }
    }

    fun atualizarVantagem(index: Int, vantagem: VantagemSelecionada) {
        val lista = personagem.vantagens.toMutableList()
        if (index in lista.indices) {
            val nivelNormalizado = normalizarNivelVantagem(vantagem.definicaoId, vantagem.nivel)
            lista[index] = vantagem.copy(nivel = nivelNormalizado)
            atualizarVantagensComConfirmacao(lista)
        }
    }

    // === DESVANTAGENS ===

    fun adicionarDesvantagem(
        definicao: DesvantagemDefinicao,
        nivel: Int = 1,
        custoEscolhido: Int = 0,
        descricao: String = "",
        autocontrole: Int? = null
    ) {
        // Verifica duplicatas
        if (personagem.desvantagens.any { it.definicaoId == definicao.id && it.descricao == descricao }) {
            return // Ja existe
        }
        val desvantagem = dataRepository.criarDesvantagemSelecionada(definicao, nivel, custoEscolhido, descricao, autocontrole)
        val lista = personagem.desvantagens.toMutableList()
        lista.add(desvantagem)
        personagem = personagem.copy(desvantagens = lista)
    }

    fun removerDesvantagem(index: Int) {
        val lista = personagem.desvantagens.toMutableList()
        if (index in lista.indices) {
            lista.removeAt(index)
            personagem = personagem.copy(desvantagens = lista)
        }
    }

    fun atualizarDesvantagem(index: Int, desvantagem: DesvantagemSelecionada) {
        val lista = personagem.desvantagens.toMutableList()
        if (index in lista.indices) {
            lista[index] = desvantagem
            personagem = personagem.copy(desvantagens = lista)
        }
    }

    // === PECULIARIDADES ===

    fun adicionarQualidade(qualidade: String) {
        if (personagem.qualidades.size >= 5) return // Maximo 5
        if (personagem.qualidades.contains(qualidade)) return // Duplicata
        val lista = personagem.qualidades.toMutableList()
        lista.add(qualidade)
        personagem = personagem.copy(qualidades = lista)
    }

    fun removerQualidade(index: Int) {
        val lista = personagem.qualidades.toMutableList()
        if (index in lista.indices) {
            lista.removeAt(index)
            personagem = personagem.copy(qualidades = lista)
        }
    }

    fun adicionarPeculiaridade(peculiaridade: String) {
        if (personagem.peculiaridades.size >= 5) return // Maximo 5
        if (personagem.peculiaridades.contains(peculiaridade)) return // Duplicata
        val lista = personagem.peculiaridades.toMutableList()
        lista.add(peculiaridade)
        personagem = personagem.copy(peculiaridades = lista)
    }

    fun removerPeculiaridade(index: Int) {
        val lista = personagem.peculiaridades.toMutableList()
        if (index in lista.indices) {
            lista.removeAt(index)
            personagem = personagem.copy(peculiaridades = lista)
        }
    }

    // === PERICIAS ===

    fun adicionarPericia(
        definicao: PericiaDefinicao,
        pontosGastos: Int = 1,
        especializacao: String = "",
        atributoEscolhido: AtributoBase? = null,
        dificuldadeEscolhida: Dificuldade? = null
    ) {
        // Verifica duplicatas (mesmo id e especializacao)
        if (personagem.pericias.any { it.definicaoId == definicao.id && it.especializacao == especializacao }) {
            return // Ja existe
        }
        val pericia = dataRepository.criarPericiaSelecionada(definicao, pontosGastos, especializacao, atributoEscolhido, dificuldadeEscolhida)
        val lista = personagem.pericias.toMutableList()
        lista.add(pericia)
        personagem = personagem.copy(pericias = lista)
    }

    fun adicionarPericiaCustomizada(pericia: PericiaSelecionada) {
        val lista = personagem.pericias.toMutableList()
        lista.add(pericia)
        personagem = personagem.copy(pericias = lista)
    }

    fun removerPericia(index: Int) {
        val lista = personagem.pericias.toMutableList()
        if (index in lista.indices) {
            lista.removeAt(index)
            personagem = personagem.copy(pericias = lista)
        }
    }

    fun atualizarPericia(index: Int, pericia: PericiaSelecionada) {
        val lista = personagem.pericias.toMutableList()
        if (index in lista.indices) {
            lista[index] = pericia
            personagem = personagem.copy(pericias = lista)
        }
    }

    // === MAGIAS ===

    fun adicionarMagia(
        definicao: MagiaDefinicao,
        pontosGastos: Int = 1
    ) {
        if (personagem.magias.any { it.definicaoId == definicao.id }) {
            return
        }
        val magia = dataRepository.criarMagiaSelecionada(definicao, pontosGastos.coerceAtLeast(1))
        val lista = personagem.magias.toMutableList()
        lista.add(magia)
        personagem = personagem.copy(magias = lista)
    }

    fun removerMagia(index: Int) {
        val lista = personagem.magias.toMutableList()
        if (index in lista.indices) {
            lista.removeAt(index)
            personagem = personagem.copy(magias = lista)
        }
    }

    fun atualizarMagia(index: Int, magia: MagiaSelecionada) {
        val lista = personagem.magias.toMutableList()
        if (index in lista.indices) {
            lista[index] = magia.copy(pontosGastos = magia.pontosGastos.coerceAtLeast(1))
            personagem = personagem.copy(magias = lista)
        }
    }

    // === TECNICAS ===

    fun adicionarTecnica(
        definicao: TecnicaCatalogoItem,
        pontosGastos: Int = 1
    ) {
        if (personagem.tecnicas.any { it.definicaoId == definicao.id }) {
            return
        }
        val tecnica = dataRepository.criarTecnicaSelecionada(definicao, pontosGastos)
        val lista = personagem.tecnicas.toMutableList()
        lista.add(tecnica)
        personagem = personagem.copy(tecnicas = lista)
    }

    fun removerTecnica(index: Int) {
        val lista = personagem.tecnicas.toMutableList()
        if (index in lista.indices) {
            lista.removeAt(index)
            personagem = personagem.copy(tecnicas = lista)
        }
    }

    fun atualizarTecnica(index: Int, tecnica: TecnicaSelecionada) {
        val lista = personagem.tecnicas.toMutableList()
        if (index in lista.indices) {
            lista[index] = tecnica.copy(pontosGastos = tecnica.pontosGastos.coerceAtLeast(1))
            personagem = personagem.copy(tecnicas = lista)
        }
    }

    // === EQUIPAMENTOS ===

    fun adicionarEquipamento(equipamento: Equipamento) {
        val lista = personagem.equipamentos.toMutableList()
        lista.add(equipamento)
        personagem = personagem.copy(equipamentos = lista)
        ajustarEscudoSelecionadoAutomatico()
    }

    fun adicionarEquipamentoArma(arma: ArmaCatalogoItem) {
        val notasArma = buildString {
            if (!arma.aparar.isNullOrBlank()) {
                if (isNotBlank()) append("\n")
                append("Aparar: ${arma.aparar} (${explicarAparar(arma.aparar)})")
            }
            val observacoes = observacoesArmaFormatadas(arma)
            if (observacoes.isNotBlank()) {
                if (isNotBlank()) append("\n")
                append(observacoes)
            }
        }
        val equipamento = Equipamento(
            nome = arma.nome,
            peso = arma.pesoBaseKg ?: 0f,
            custo = arma.custoBase ?: 0f,
            quantidade = 1,
            notas = notasArma,
            tipo = if (arma.nome.contains("escudo", ignoreCase = true)) TipoEquipamento.ESCUDO else TipoEquipamento.ARMA,
            bonusDefesa = 0,
            armaCatalogoId = arma.id,
            armaTipoCombate = arma.tipoCombate,
            armaDanoRaw = arma.danoRaw,
            armaStMinimo = arma.stMinimo
        )
        adicionarEquipamento(equipamento)
    }

    private fun explicarAparar(valor: String): String {
        val v = valor.trim().uppercase()
        return when {
            v == "NÃO" || v == "NAO" -> "Nao pode aparar"
            v.endsWith("E") -> "Arma de esgrima"
            v.endsWith("D") -> "Arma desbalanceada"
            v == "0" -> "Sem modificador"
            v.startsWith("+") || v.startsWith("-") -> "Modificador na aparada"
            else -> "Valor de aparar"
        }
    }

    private fun observacoesArmaFormatadas(arma: ArmaCatalogoItem): String {
        if (arma.tipoCombate != "corpo_a_corpo" && arma.tipoCombate != "distancia" && arma.tipoCombate != "armas_de_fogo") {
            return ""
        }
        val refs = extrairReferenciasObservacoes(arma.observacoes)
        if (arma.tipoCombate == "armas_de_fogo") {
            val classe = classificarArmaDeFogo(arma)
            val linhas = mutableListOf<String>()
            if (classe == ClasseArmaFogo.ULTRATECH) {
                linhas.add("Todas as armas de feixe incluem sistemas eletronicos das armas inteligentes (pag. 278).")
            }
            val mapa = when (classe) {
                ClasseArmaFogo.PISTOLA_MM -> OBS_ARMA_FOGO_PISTOLA_MM
                ClasseArmaFogo.RIFLE_ESPINGARDA -> OBS_ARMA_FOGO_RIFLE
                ClasseArmaFogo.ULTRATECH -> OBS_ARMA_FOGO_ULTRATECH
                ClasseArmaFogo.PESADA -> OBS_ARMA_FOGO_PESADA
            }
            refs.mapNotNull { ref -> mapa[ref]?.let { "[$ref] $it" } }.forEach { linhas.add(it) }
            return linhas.joinToString("\n")
        }

        if (refs.isEmpty()) return ""
        val mapa = if (arma.tipoCombate == "distancia") OBS_ARMA_DISTANCIA else OBS_ARMA_CORPO_A_CORPO
        return refs.mapNotNull { ref -> mapa[ref]?.let { "[$ref] $it" } }.joinToString("\n")
    }

    private fun extrairReferenciasObservacoes(observacoes: String): List<Int> {
        if (observacoes.isBlank() || !observacoes.contains("[")) return emptyList()
        return Regex("\\d+")
            .findAll(observacoes)
            .mapNotNull { it.value.toIntOrNull() }
            .distinct()
            .toList()
    }

    private enum class ClasseArmaFogo {
        PISTOLA_MM,
        RIFLE_ESPINGARDA,
        ULTRATECH,
        PESADA
    }

    private fun classificarArmaDeFogo(arma: ArmaCatalogoItem): ClasseArmaFogo {
        val grupo = arma.grupo.lowercase()
        val nome = arma.nome.lowercase()
        if (
            grupo.contains("feixe") ||
            nome.contains("laser") ||
            nome.contains("eletrolaser") ||
            nome.contains("ionico") ||
            nome.contains("iônico")
        ) return ClasseArmaFogo.ULTRATECH

        if (
            grupo.contains("artilharia") ||
            grupo.contains("canhoneiro") ||
            grupo.contains("lancador") ||
            grupo.contains("lançador") ||
            grupo.contains("ala")
        ) return ClasseArmaFogo.PESADA

        if (grupo.contains("rifle")) return ClasseArmaFogo.RIFLE_ESPINGARDA

        return ClasseArmaFogo.PISTOLA_MM
    }

    private fun categoriaArmaFogoParaFiltro(arma: ArmaCatalogoItem): String {
        return when (classificarArmaDeFogo(arma)) {
            ClasseArmaFogo.PISTOLA_MM -> "pistolas_mm"
            ClasseArmaFogo.RIFLE_ESPINGARDA -> "rifles_espingardas"
            ClasseArmaFogo.ULTRATECH -> "ultratech"
            ClasseArmaFogo.PESADA -> "pesadas"
        }
    }

    fun observacoesArmaPorEquipamento(equipamento: Equipamento): String {
        val porId = equipamento.armaCatalogoId
            ?.takeIf { it.isNotBlank() }
            ?.let { id -> dataRepository.armasCatalogo.firstOrNull { it.id == id } }
        if (porId != null) return observacoesArmaFormatadas(porId)

        val nomeBase = equipamento.nome.substringBefore(" (").trim()
        if (nomeBase.isBlank()) return ""
        val nomeBaseNorm = normalizarChaveTexto(nomeBase)

        val porNome = dataRepository.armasCatalogo.firstOrNull { arma ->
            val tipoOk = equipamento.armaTipoCombate.isNullOrBlank() ||
                arma.tipoCombate.equals(equipamento.armaTipoCombate, ignoreCase = true)
            val danoOk = equipamento.armaDanoRaw.isNullOrBlank() ||
                arma.danoRaw.equals(equipamento.armaDanoRaw, ignoreCase = true)
            val nomeOk = normalizarChaveTexto(arma.nome) == nomeBaseNorm
            tipoOk && danoOk && nomeOk
        } ?: dataRepository.armasCatalogo.firstOrNull { arma ->
            normalizarChaveTexto(arma.nome) == nomeBaseNorm
        }

        return if (porNome != null) observacoesArmaFormatadas(porNome) else ""
    }

    private fun normalizarChaveTexto(valor: String): String {
        val semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return semAcento
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun adicionarEquipamentoEscudo(escudo: EscudoCatalogoItem) {
        val equipamento = Equipamento(
            nome = escudo.nome,
            peso = escudo.pesoKg ?: 0f,
            custo = escudo.custo ?: 0f,
            quantidade = 1,
            notas = escudo.observacoes,
            tipo = TipoEquipamento.ESCUDO,
            bonusDefesa = escudo.db
        )
        adicionarEquipamento(equipamento)
    }

    fun adicionarEquipamentoArmadura(armadura: ArmaduraCatalogoItem) {
        val componentesTexto = if (armadura.componentes.isEmpty()) {
            ""
        } else {
            armadura.componentes.joinToString(" | ") { c ->
                val custo = c.custoBase?.let { "$$it" } ?: "—"
                val peso = c.pesoKg?.let { "${it}kg" } ?: "—"
                "${c.local} RD ${c.rd} Custo $custo Peso $peso"
            }
        }
        val observacoes = montarObservacoesArmadura(armadura)
        val notas = buildString {
            append("Local: ${armadura.local}; RD: ${armadura.rd}")
            if (observacoes.isNotBlank()) append("\n$observacoes")
            if (componentesTexto.isNotBlank()) append("\nComponentes: $componentesTexto")
        }
        val equipamento = Equipamento(
            nome = armadura.nome,
            peso = armadura.pesoBaseKg ?: 0f,
            custo = armadura.custoBase ?: 0f,
            quantidade = 1,
            notas = notas,
            tipo = TipoEquipamento.ARMADURA,
            armaduraLocal = armadura.local,
            armaduraRd = armadura.rd
        )
        adicionarEquipamento(equipamento)
    }

    fun adicionarEquipamentoArmaduraComSelecao(armadura: ArmaduraCatalogoItem, locaisSelecionados: List<String>) {
        val selecionadosNorm = locaisSelecionados.map { it.trim() }.filter { it.isNotBlank() }
        val locaisFinais = if (selecionadosNorm.isEmpty()) listOf(armadura.local) else selecionadosNorm
        val custoBase = armadura.custoBase ?: 0f
        val pesoBase = armadura.pesoBaseKg ?: 0f
        val possuiComponentes = armadura.componentes.isNotEmpty()
        val divisor = locaisFinais.size.coerceAtLeast(1).toFloat()

        locaisFinais.forEach { localSel ->
            val componente = armadura.componentes.firstOrNull { it.local.equals(localSel, ignoreCase = true) }
            val custoLocal = when {
                componente?.custoBase != null -> componente.custoBase
                possuiComponentes -> custoBase
                else -> (custoBase / divisor)
            }
            val pesoLocal = when {
                componente?.pesoKg != null -> componente.pesoKg
                possuiComponentes -> pesoBase
                else -> (pesoBase / divisor)
            }
            val rdLocal = componente?.rd ?: armadura.rd
            val observacoes = montarObservacoesArmadura(armadura)
            val notas = buildString {
                append("Local: $localSel; RD: $rdLocal")
                if (observacoes.isNotBlank()) append("\n$observacoes")
            }
            val equipamento = Equipamento(
                nome = "${armadura.nome} ($localSel)",
                peso = pesoLocal,
                custo = custoLocal,
                quantidade = 1,
                notas = notas,
                tipo = TipoEquipamento.ARMADURA,
                armaduraLocal = localSel,
                armaduraRd = rdLocal
            )
            adicionarEquipamento(equipamento)
        }
    }

    private fun montarObservacoesArmadura(armadura: ArmaduraCatalogoItem): String {
        val refs = Regex("\\[(\\d+)]")
            .findAll(armadura.observacoes)
            .mapNotNull { it.groupValues.getOrNull(1)?.toIntOrNull() }
            .toList()
        var detalhes = armadura.observacoesDetalhadas
            .map { it.trim() }
            .filter { it.isNotBlank() }
        if (detalhes.isEmpty()) return ""

        val linhas = mutableListOf<String>()
        val primeira = detalhes.firstOrNull()
        if (primeira != null && primeira.contains("NT7+", ignoreCase = true)) {
            linhas.add(primeira)
            detalhes = detalhes.drop(1)
        }

        if (refs.isEmpty()) {
            linhas.addAll(detalhes)
            return linhas.joinToString("\n")
        }

        refs.zip(detalhes).forEach { (ref, texto) ->
            linhas.add("[$ref] $texto")
        }
        if (detalhes.size > refs.size) {
            linhas.addAll(detalhes.drop(refs.size))
        }
        return linhas.joinToString("\n")
    }

    fun removerEquipamento(index: Int) {
        val lista = personagem.equipamentos.toMutableList()
        if (index in lista.indices) {
            lista.removeAt(index)
            personagem = personagem.copy(equipamentos = lista)
            ajustarEscudoSelecionadoAutomatico()
        }
    }

    fun atualizarEquipamento(index: Int, equipamento: Equipamento) {
        val lista = personagem.equipamentos.toMutableList()
        if (index in lista.indices) {
            lista[index] = equipamento
            personagem = personagem.copy(equipamentos = lista)
            ajustarEscudoSelecionadoAutomatico()
        }
    }

    // === DESCRICAO ===

    fun atualizarAparencia(aparencia: String) {
        personagem = personagem.copy(aparencia = aparencia)
    }

    fun atualizarHistorico(historico: String) {
        personagem = personagem.copy(historico = historico)
    }

    fun atualizarNotas(notas: String) {
        personagem = personagem.copy(notas = notas)
    }

    // === PERSISTENCIA ===

    fun salvarFicha(nomeArquivo: String = personagem.nome.ifBlank { "Sem_Nome" }) {
        viewModelScope.launch {
            fichaStorage.salvarFicha(nomeArquivo, personagem.toJson())
            carregarListaFichas()
        }
    }

    fun carregarFicha(nomeArquivo: String) {
        viewModelScope.launch {
            val json = fichaStorage.carregarFicha(nomeArquivo)
            if (json != null) {
                personagem = Personagem.fromJson(json)
            }
        }
    }

    fun excluirFicha(nomeArquivo: String) {
        viewModelScope.launch {
            fichaStorage.excluirFicha(nomeArquivo)
            carregarListaFichas()
        }
    }

    fun novaFicha() {
        personagem = Personagem()
        personagemPendenteLimpezaMagias = null
        mostrarConfirmacaoLimpezaMagias = false
    }

    fun exportarFichaJsonCompativel(): String {
        return personagem.toJson()
    }

    fun exportarFichaJsonVersionada(): String {
        return PersonagemInterop.exportarJson(
            personagem = personagem,
            appVersion = BuildConfig.VERSION_NAME,
            uiVariant = BuildConfig.UI_VARIANT
        )
    }

    fun importarFichaJson(json: String): String? {
        return try {
            val resultado = PersonagemInterop.importarJson(json)
            personagem = resultado.personagem
            personagemPendenteLimpezaMagias = null
            mostrarConfirmacaoLimpezaMagias = false
            resultado.aviso?.let { "Ficha importada com sucesso. $it" }
        } catch (_: UnsupportedOperationException) {
            "Versao de arquivo nao suportada por esta versao do app."
        } catch (_: Exception) {
            "Arquivo de ficha invalido ou corrompido."
        }
    }

    private suspend fun carregarListaFichas() {
        fichasSalvas = fichaStorage
            .listarFichas()
            .filterNot { it == autoSaveRecuperacaoNome }
    }

    private suspend fun restaurarAutoSaveSeExistir() {
        val json = fichaStorage.carregarFicha(autoSaveRecuperacaoNome) ?: return
        runCatching {
            personagem = Personagem.fromJson(json)
        }
    }

    // === UTILITARIOS ===

    val pesoTotal: Float get() = personagem.pesoTotal

    val custoTotalEquipamentos: Float get() = personagem.equipamentos.sumOf {
        (it.custo * it.quantidade).toDouble()
    }.toFloat()

    fun calcularDanoArmaComSt(danoRaw: String?): String {
        val raw = danoRaw?.trim().orEmpty()
        if (raw.isBlank()) return ""
        return CharacterRules.resolverDanoPorSt(raw, personagem.forca)
    }

    suspend fun enviarRolagemDiscord(payload: DiscordRollPayload): RollDispatchStatus {
        return withContext(Dispatchers.IO) {
            val primeiraTentativa = DiscordRollApiClient.postRoll(
                baseUrl = BuildConfig.DISCORD_ROLL_API_BASE_URL,
                apiKey = BuildConfig.DISCORD_ROLL_API_KEY,
                payload = payload
            )
            if (primeiraTentativa.ok) {
                RollDispatchStatus(enviado = true)
            } else {
                // Retentativa unica somente para falha de rede/timeout (sem resposta HTTP)
                val precisaRetentativaRede = RollDispatchPolicy.deveRetentar(primeiraTentativa.statusCode)
                val resultadoFinal = if (precisaRetentativaRede) {
                    DiscordRollApiClient.postRoll(
                        baseUrl = BuildConfig.DISCORD_ROLL_API_BASE_URL,
                        apiKey = BuildConfig.DISCORD_ROLL_API_KEY,
                        payload = payload
                    )
                } else {
                    primeiraTentativa
                }

                if (resultadoFinal.ok) {
                    RollDispatchStatus(enviado = true)
                } else {
                    RollDispatchStatus(
                        enviado = false,
                        detalhe = RollDispatchPolicy.mensagemErro(
                            statusCode = resultadoFinal.statusCode,
                            erroBruto = resultadoFinal.error
                        )
                    )
                }
            }
        }
    }

    fun atualizarCanaisDiscord() {
        viewModelScope.launch {
            canaisDiscordCarregando = true
            canaisDiscordErro = null
            val resultado = withContext(Dispatchers.IO) {
                DiscordRollApiClient.fetchVoiceChannels(
                    baseUrl = BuildConfig.DISCORD_ROLL_API_BASE_URL,
                    apiKey = BuildConfig.DISCORD_ROLL_API_KEY
                )
            }
            canaisDiscordCarregando = false

            if (!resultado.ok) {
                canaisDiscordErro = resultado.error ?: "erro_ao_carregar_canais"
                return@launch
            }

            canaisDiscord = resultado.channels
            val selecionadoAtual = canalDiscordSelecionadoId
            if (!selecionadoAtual.isNullOrBlank()) {
                val canal = resultado.channels.firstOrNull { it.id == selecionadoAtual }
                if (canal != null) {
                    canalDiscordSelecionadoNome = "${canal.guildName} / ${canal.name}"
                    configPrefs.edit()
                        .putString(prefCanalDiscordNome, canalDiscordSelecionadoNome)
                        .apply()
                }
            }
        }
    }

    fun selecionarCanalDiscord(canal: DiscordVoiceChannel?) {
        canalDiscordSelecionadoId = canal?.id
        canalDiscordSelecionadoNome = canal?.let { "${it.guildName} / ${it.name}" }
        configPrefs.edit()
            .putString(prefCanalDiscordId, canalDiscordSelecionadoId)
            .putString(prefCanalDiscordNome, canalDiscordSelecionadoNome)
            .apply()
    }

    val nivelCarga: Int get() = personagem.nivelCarga

    val deslocamentoAtual: Int get() = personagem.deslocamentoAtual

    val esquivaAtual: Int get() = (personagem.esquiva - personagem.nivelCarga).coerceAtLeast(1)

    // === VALIDACAO ===

    val desvantagensExcedemLimite: Boolean
        get() = personagem.desvantagensExcedemLimite

    val pontosDesvantagens: Int
        get() = personagem.pontosDesvantagens

    val limiteDesvantagens: Int
        get() = personagem.limiteDesvantagens

    fun vantagemJaAdicionada(id: String, descricao: String = ""): Boolean {
        return personagem.vantagens.any { it.definicaoId == id && it.descricao == descricao }
    }

    fun desvantagemJaAdicionada(id: String, descricao: String = ""): Boolean {
        return personagem.desvantagens.any { it.definicaoId == id && it.descricao == descricao }
    }

    fun periciaJaAdicionada(id: String, especializacao: String = ""): Boolean {
        return personagem.pericias.any { it.definicaoId == id && it.especializacao == especializacao }
    }

    fun magiaJaAdicionada(id: String): Boolean {
        return personagem.magias.any { it.definicaoId == id }
    }

    fun tecnicaJaAdicionada(id: String): Boolean {
        return personagem.tecnicas.any { it.definicaoId == id }
    }

    val nivelAptidaoMagica: Int
        get() = personagem.vantagens
            .filter { it.definicaoId.equals("aptidao_magica", ignoreCase = true) }
            .maxOfOrNull { it.nivel }
            ?: 0

    val temAptidaoMagica: Boolean
        get() = nivelAptidaoMagica > 0

    val nivelAptidaoAstral: Int
        get() = personagem.vantagens
            .filter { it.definicaoId.equals("aptidao_astral", ignoreCase = true) }
            .maxOfOrNull { (it.nivel - 1).coerceAtLeast(0) }
            ?: 0

    val temAptidaoAstral: Boolean
        get() = personagem.vantagens.any { it.definicaoId.equals("aptidao_astral", ignoreCase = true) }

    // === COMBATE - DEFESAS ATIVAS ===

    fun atualizarBonusManualEsquiva(bonus: Int) {
        val defesas = personagem.defesasAtivas.copy(bonusManualEsquiva = bonus.coerceIn(-20, 20))
        personagem = personagem.copy(defesasAtivas = defesas)
    }

    fun atualizarPericiaApara(periciaId: String?) {
        val defesas = personagem.defesasAtivas.copy(periciaAparaId = periciaId)
        personagem = personagem.copy(defesasAtivas = defesas)
    }

    fun atualizarBonusManualApara(bonus: Int) {
        val defesas = personagem.defesasAtivas.copy(bonusManualApara = bonus.coerceIn(-20, 20))
        personagem = personagem.copy(defesasAtivas = defesas)
    }

    fun atualizarPericiaBloqueio(periciaId: String?) {
        val defesas = personagem.defesasAtivas.copy(periciaBloqueioId = periciaId)
        personagem = personagem.copy(defesasAtivas = defesas)
        ajustarEscudoSelecionadoAutomatico()
    }

    fun atualizarEscudoBloqueio(escudoNome: String?) {
        val defesas = personagem.defesasAtivas.copy(escudoSelecionadoNome = escudoNome)
        personagem = personagem.copy(defesasAtivas = defesas)
    }

    fun atualizarBonusManualBloqueio(bonus: Int) {
        val defesas = personagem.defesasAtivas.copy(bonusManualBloqueio = bonus.coerceIn(-20, 20))
        personagem = personagem.copy(defesasAtivas = defesas)
    }

    // Valores calculados de defesas
    val esquivaCalculada: Int get() = personagem.defesasAtivas.calcularEsquiva(personagem)
    val aparaCalculada: Int? get() = personagem.defesasAtivas.calcularApara(personagem)
    val bloqueioCalculado: Int? get() = personagem.defesasAtivas.calcularBloqueio(personagem)

    // Estado derivado para a UI
    val defesasAtivasVisiveis: List<ActiveDefense> get() {
        val lista = mutableListOf<ActiveDefense>()
        
        // Esquiva
        lista.add(ActiveDefense(
            type = DefenseType.ESQUIVA,
            name = "Esquiva",
            baseValue = personagem.defesasAtivas.getEsquivaBase(personagem),
            bonus = personagem.defesasAtivas.bonusManualEsquiva,
            finalValue = esquivaCalculada
        ))
        
        // Apara
        aparaCalculada?.let { finalVal ->
            personagem.defesasAtivas.getAparaBase(personagem)?.let { baseVal ->
                lista.add(ActiveDefense(
                    type = DefenseType.APARA,
                    name = "Apara",
                    baseValue = baseVal,
                    bonus = personagem.defesasAtivas.bonusManualApara,
                    finalValue = finalVal
                ))
            }
        }
        
        // Bloqueio
        bloqueioCalculado?.let { finalVal ->
            personagem.defesasAtivas.getBloqueioBase(personagem)?.let { baseVal ->
                val db = personagem.defesasAtivas.getBonusEscudo(personagem)
                lista.add(ActiveDefense(
                    type = DefenseType.BLOQUEIO,
                    name = "Bloqueio",
                    baseValue = baseVal + db,
                    bonus = personagem.defesasAtivas.bonusManualBloqueio,
                    finalValue = finalVal
                ))
            }
        }
        return lista
    }

    fun atualizarBonusDefesa(type: DefenseType, bonus: Int) {
        when(type) {
            DefenseType.ESQUIVA -> atualizarBonusManualEsquiva(bonus)
            DefenseType.APARA -> atualizarBonusManualApara(bonus)
            DefenseType.BLOQUEIO -> atualizarBonusManualBloqueio(bonus)
        }
    }

    // Perícias para o dropdown de Apara (Combate exceto Escudo)
    val periciasParaApara: List<PericiaSelecionada> get() {
        return personagem.pericias.filter { pericia ->
            val idNormalizado = pericia.definicaoId.trim().lowercase()
            PERICIAS_COMBATE.contains(idNormalizado) && idNormalizado != "escudo"
        }
    }

    // Perícias para o dropdown de Bloqueio (Somente Escudo)
    val periciasParaBloqueio: List<PericiaSelecionada> get() {
        return personagem.pericias.filter { pericia ->
            pericia.definicaoId.trim().equals("escudo", ignoreCase = true)
        }
    }
    fun confirmarLimpezaMagiasAoPerderAptidao() {
        val pendente = personagemPendenteLimpezaMagias ?: return
        personagem = pendente.copy(magias = emptyList())
        personagemPendenteLimpezaMagias = null
        mostrarConfirmacaoLimpezaMagias = false
    }

    fun cancelarLimpezaMagiasAoPerderAptidao() {
        personagemPendenteLimpezaMagias = null
        mostrarConfirmacaoLimpezaMagias = false
    }

    private fun ajustarEscudoSelecionadoAutomatico() {
        if (personagem.defesasAtivas.periciaBloqueioId.isNullOrBlank()) return
        val escudos = escudosEquipados
        if (escudos.isEmpty()) {
            val def = personagem.defesasAtivas.copy(escudoSelecionadoNome = null)
            personagem = personagem.copy(defesasAtivas = def)
            return
        }
        val atual = personagem.defesasAtivas.escudoSelecionadoNome
        val existeAtual = atual?.let { nomeSel ->
            escudos.any { it.nome.equals(nomeSel.trim(), ignoreCase = true) }
        } == true
        if (existeAtual) return
        val melhor = escudos.maxByOrNull { it.bonusDefesa }?.nome ?: escudos.first().nome
        val def = personagem.defesasAtivas.copy(escudoSelecionadoNome = melhor)
        personagem = personagem.copy(defesasAtivas = def)
    }

    private fun atualizarVantagensComConfirmacao(vantagensAtualizadas: List<VantagemSelecionada>) {
        if (deveConfirmarLimpezaMagias(vantagensAtualizadas)) {
            personagemPendenteLimpezaMagias = personagem.copy(vantagens = vantagensAtualizadas)
            mostrarConfirmacaoLimpezaMagias = true
            return
        }
        personagem = personagem.copy(vantagens = vantagensAtualizadas)
    }

    private fun deveConfirmarLimpezaMagias(vantagensAtualizadas: List<VantagemSelecionada>): Boolean {
        if (personagem.magias.isEmpty()) return false
        val nivelAptidaoAposMudanca = vantagensAtualizadas
            .filter { it.definicaoId.equals("aptidao_magica", ignoreCase = true) }
            .maxOfOrNull { it.nivel }
            ?: 0
        return nivelAptidaoAposMudanca <= 0
    }

    private fun normalizarNivelVantagem(definicaoId: String, nivel: Int): Int {
        if (definicaoId.equals("aptidao_astral", ignoreCase = true)) {
            return nivel.coerceIn(1, 4)
        }
        return nivel.coerceAtLeast(1)
    }

    companion object {
        private val OBS_ARMA_CORPO_A_CORPO = mapOf(
            1 to "Pode ser de arremesso. Veja Tabela de Armas Motoras de Combate a Distancia (pag. 275).",
            2 to "Pode ficar presa; veja Picaretas (pag. 406).",
            3 to "Briga aumenta dano sem armas; Garras e Carate aumentam dano de socos/chutes; Boxe aumenta dano por soco.",
            4 to "Se fracassar em um chute, precisa passar em teste de DX para nao cair.",
            5 to "Em fracasso de HT, a vitima fica atordoada enquanto houver contato e por mais (20-HT) segundos. Depois testa HT-3 para recuperar.",
            6 to "Aparar manguais sofre -4 e armas de esgrima (E) nao apararam. Bloquear manguais sofre -2. No nunchaku, redutores pela metade.",
            7 to "Lamina de energia. Exige manobra Preparar para ativar/desativar. Lamina inquebravel e danifica armas/corpo ao aparar/bloquear. Celula extra: $100, 0,25 kg, 300s.",
            8 to "Corda para estrangular; veja Garrote (pag. 406).",
            9 to "Dano maior quando usada montado; veja Armas de Cavalaria (pag. 397).",
            10 to "O cabo da arma pode ser usado como soco ingles em combate corporal.",
            11 to "Muito barulhento. Funciona 2 horas com 2,5 l de gasolina.",
            12 to "Especifique alcance maximo (ate 7 m) na compra. Custo/peso por metro. ST 5 +1 por metro. Veja Chicotes (pag. 405)."
        )

        private val OBS_ARMA_DISTANCIA = mapOf(
            1 to "Ataque de acompanhamento para dopar/envenenar se o dano ultrapassar a RD. Efeito depende do veneno (pag. 437).",
            2 to "Exige duas maos para preparar, mas apenas uma para atacar.",
            3 to "Municao: flecha/virote $2; dardo/bolinha $0,1; pedra de funda e gratuita.",
            4 to "Pode enredar ou apanhar o alvo (pag. 410).",
            5 to "Sarilho/pole para recarregar besta de ST alta. Permite recarregar arma com ST ate +4 da sua em 20 manobras Preparar.",
            6 to "Rede nao tem 1/2D. Distancia Max: (ST/2 + NH/5) rede grande; (ST + NH/5) rede de combate.",
            7 to "Pode disparar pedras (NT0) ou balas de chumbo (NT2). Bala de chumbo: +1 dano e dobra distancia.",
            8 to "Preparado: exige manobra Preparar e teste de ST para disparar; remover projetil causa metade do dano de entrada."
        )

        private val OBS_ARMA_FOGO_PISTOLA_MM = mapOf(
            1 to "Inclui sistemas eletronicos das armas inteligentes (veja o quadro).",
            2 to "Os foguetes demoram um pouco para acelerar. Divida o dano por 3 a 1-2 metros e por 2 a 3-10 metros.",
            3 to "A versao civil de uma arma semiautomatica tem CdT 3, -25% no custo e recebe um bonus de +1 na CL."
        )

        private val OBS_ARMA_FOGO_RIFLE = mapOf(
            1 to "A versao civil de uma arma semiautomatica tem CdT 3, -25% no custo e um bonus de +1 na CL.",
            2 to "Se o dano ultrapassar a RD, o dardo injeta uma droga ou veneno como ataque de acompanhamento. No caso de dardo tranquilizador, faca um teste de HT-3; um fracasso deixa o alvo inconsciente por uma quantidade de minutos igual a margem pela qual o teste falhou.",
            3 to "Inclui os sistemas eletronicos das armas inteligentes (pag. 278).",
            4 to "Inclui um lancador de granadas completo de 25 mm (pag. 281)."
        )

        private val OBS_ARMA_FOGO_ULTRATECH = mapOf(
            1 to "A arma precisa de atmosfera para funcionar. Ela nao produz nenhum efeito em atmosferas rarefeitas ou no vacuo.",
            2 to "O dano por queimadura recebe o modificador de dano de Sobretensao (pag. 108). Alem disso, mesmo quando nenhum dano penetre, o alvo deve obter sucesso em um teste de HT-4 mais metade da RD do local atingido (devido ao divisor de armadura). No caso de fracasso, o choque eletrico deixa o alvo atordoado. O alvo pode fazer novo teste de HT a cada turno sob a mesma penalidade (mas sem o bonus de RD) para se recuperar.",
            3 to "Fumaca, nevoa, chuva, nuvens etc. concedem ao alvo uma RD adicional igual a penalidade de visibilidade. Exemplo: se a chuva impuser -1 a cada 100 metros, um laser percorrendo 2.000 metros de chuva deve superar RD adicional de 20.",
            4 to "O dano por queimadura recebe modificador de dano de Sobretensao (pag. 108).",
            5 to "Em aventuras com superciencia, um onidisparador custa o dobro, mas tem regulagem para atordoamento: o dano se torna HT-3(3) at para pistola e HT-6(3) at para rifle. Um fracasso em teste de HT deixa a vitima inconsciente por uma quantidade de minutos igual a margem de erro."
        )

        private val OBS_ARMA_FOGO_PESADA = mapOf(
            1 to "Tem uma distancia minima: 10 metros no caso de um LG de 40 mm, 30 metros no caso de um MTA de 115 mm e 200 metros no caso de um MAS de 70 mm.",
            2 to "Contra-disparo de risco: 1d ponto de dano por queimadura em qualquer pessoa que se encontre atras do atirador a uma distancia de ate 15 metros (30 no caso da MTA).",
            3 to "Ataque Guiado (pag. 412). O Canhoneiro usa Artilharia (Missil Guiado) para atacar. 1/2D e igual a velocidade do projetil (m/s). O peso se refere ao lancador vazio/um projetil.",
            4 to "Ataque Teleguiado (Visao Hiperespectral) (pag. 413) com NH 10 do projetil. O atirador faz teste de Bombardeiro (Missil Guiado) para apontar. Em sucesso, o missil recebe bonus de Prec. 1/2D e igual a velocidade (m/s) do projetil. O peso se refere ao lancador vazio/um projetil.",
            5 to "Um tripe destacavel pesa mais 22 kg.",
            6 to "Pode ser anexada a parte inferior do cano de qualquer rifle ou carabina de NT7+. Utilize a Magnitude do Rifle.",
            7 to "O dano nao e reduzido pela metade na distancia de 1/2D, mas perdera seu divisor de armadura que e de (10).",
            8 to "Embutido na ACI de NT9 (pag. 279). Utilize a Magnitude da ACI. Possui sistemas eletronicos das armas inteligentes (pag. 278)."
        )
    }
}


