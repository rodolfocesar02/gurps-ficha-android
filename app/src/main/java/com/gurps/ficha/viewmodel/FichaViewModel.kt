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
import com.gurps.ficha.domain.rules.CharacterRules
import com.gurps.ficha.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    var buscaArmaEquipamento by mutableStateOf("")
        private set
    var filtroTipoArmaEquipamento by mutableStateOf<String?>(null) // "corpo_a_corpo" | "distancia" | "armas_de_fogo" | null
        private set
    var buscaEscudoEquipamento by mutableStateOf("")
        private set
    var buscaArmaduraEquipamento by mutableStateOf("")
        private set
    var filtroNtArmaduraEquipamento by mutableStateOf<Int?>(null)
        private set
    var filtroLocalArmaduraEquipamento by mutableStateOf<String?>(null)
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

    val magiasFiltradas: List<MagiaDefinicao>
        get() = dataRepository.filtrarMagias(buscaMagia, filtroEscolaMagia, filtroClasseMagia)

    val armasEquipamentosFiltradas: List<ArmaCatalogoItem>
        get() = dataRepository.filtrarArmasCatalogo(
            busca = buscaArmaEquipamento,
            tipoCombate = filtroTipoArmaEquipamento,
            stMaximo = personagem.forca
        )

    val escudosEquipamentosFiltrados: List<EscudoCatalogoItem>
        get() = dataRepository.filtrarEscudosCatalogo(
            busca = buscaEscudoEquipamento,
            stMaximo = personagem.forca
        )

    val armadurasEquipamentosFiltradas: List<ArmaduraCatalogoItem>
        get() = dataRepository.filtrarArmadurasCatalogo(
            busca = buscaArmaduraEquipamento,
            nt = filtroNtArmaduraEquipamento,
            localFiltro = filtroLocalArmaduraEquipamento
        )

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

    fun atualizarBuscaArmaEquipamento(busca: String) {
        buscaArmaEquipamento = busca
    }

    fun atualizarFiltroTipoArmaEquipamento(tipo: String?) {
        filtroTipoArmaEquipamento = tipo
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

    // === VANTAGENS ===

    fun adicionarVantagem(definicao: VantagemDefinicao, nivel: Int = 1, custoEscolhido: Int = 0, descricao: String = "") {
        // Verifica duplicatas
        if (personagem.vantagens.any { it.definicaoId == definicao.id && it.descricao == descricao }) {
            return // Ja existe
        }
        val vantagem = dataRepository.criarVantagemSelecionada(definicao, nivel, custoEscolhido, descricao)
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
            lista[index] = vantagem
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

    // === EQUIPAMENTOS ===

    fun adicionarEquipamento(equipamento: Equipamento) {
        val lista = personagem.equipamentos.toMutableList()
        lista.add(equipamento)
        personagem = personagem.copy(equipamentos = lista)
        ajustarEscudoSelecionadoAutomatico()
    }

    fun adicionarEquipamentoArma(arma: ArmaCatalogoItem) {
        val equipamento = Equipamento(
            nome = arma.nome,
            peso = arma.pesoBaseKg ?: 0f,
            custo = arma.custoBase ?: 0f,
            quantidade = 1,
            notas = arma.grupo,
            tipo = if (arma.nome.contains("escudo", ignoreCase = true)) TipoEquipamento.ESCUDO else TipoEquipamento.ARMA,
            bonusDefesa = 0,
            armaCatalogoId = arma.id,
            armaTipoCombate = arma.tipoCombate,
            armaDanoRaw = arma.danoRaw,
            armaStMinimo = arma.stMinimo
        )
        adicionarEquipamento(equipamento)
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
        val notas = buildString {
            append("Local: ${armadura.local}; RD: ${armadura.rd}")
            if (armadura.observacoes.isNotBlank()) append("; Obs: ${armadura.observacoes}")
            if (componentesTexto.isNotBlank()) append("; Componentes: $componentesTexto")
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
            val notas = "Local: $localSel; RD: $rdLocal"
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
                val precisaRetentativaRede = primeiraTentativa.statusCode == null
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
                        detalhe = mensagemErroEnvio(
                            statusCode = resultadoFinal.statusCode,
                            erroBruto = resultadoFinal.error
                        )
                    )
                }
            }
        }
    }

    private fun mensagemErroEnvio(statusCode: Int?, erroBruto: String?): String {
        return when {
            statusCode == 401 -> "chave de acesso inválida (401)"
            statusCode == 400 -> "canal de envio não definido (400)"
            statusCode == 500 -> "servidor não configurado corretamente (500)"
            statusCode == 502 -> "falha ao publicar no Discord (502)"
            statusCode != null -> "erro HTTP $statusCode"
            else -> "falha de internet/timeout ao conectar no servidor"
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

    val nivelAptidaoMagica: Int
        get() = personagem.vantagens
            .filter { it.definicaoId.equals("aptidao_magica", ignoreCase = true) }
            .maxOfOrNull { it.nivel }
            ?: 0

    val temAptidaoMagica: Boolean
        get() = nivelAptidaoMagica > 0

    // === COMBATE - DEFESAS ATIVAS ===

    fun atualizarBonusManualEsquiva(bonus: Int) {
        val defesas = personagem.defesasAtivas.copy(bonusManualEsquiva = bonus)
        personagem = personagem.copy(defesasAtivas = defesas)
    }

    fun atualizarPericiaApara(periciaId: String?) {
        val defesas = personagem.defesasAtivas.copy(periciaAparaId = periciaId)
        personagem = personagem.copy(defesasAtivas = defesas)
    }

    fun atualizarBonusManualApara(bonus: Int) {
        val defesas = personagem.defesasAtivas.copy(bonusManualApara = bonus)
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
        val defesas = personagem.defesasAtivas.copy(bonusManualBloqueio = bonus)
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
}


