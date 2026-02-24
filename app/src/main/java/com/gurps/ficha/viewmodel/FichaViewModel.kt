package com.gurps.ficha.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.storage.FichaStorageRepository
import com.gurps.ficha.domain.rules.CharacterRules
import com.gurps.ficha.model.*
import kotlinx.coroutines.launch

enum class DefenseType { ESQUIVA, APARA, BLOQUEIO }

data class ActiveDefense(
    val type: DefenseType,
    val name: String,
    val baseValue: Int,
    val bonus: Int,
    val finalValue: Int
)

class FichaViewModel(application: Application) : AndroidViewModel(application) {

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

    private val fichaStorage = FichaStorageRepository.getInstance(application)
    val dataRepository = DataRepository.getInstance(application)
    private var personagemPendenteLimpezaMagias: Personagem? = null

    // Listas filtradas
    val vantagensFiltradas: List<VantagemDefinicao>
        get() = dataRepository.filtrarVantagens(buscaVantagem, filtroTipoCustoVantagem, null)

    val desvantagensFiltradas: List<DesvantagemDefinicao>
        get() = dataRepository.filtrarDesvantagens(buscaDesvantagem, filtroTipoCustoDesvantagem)

    val periciasFiltradas: List<PericiaDefinicao>
        get() = dataRepository.filtrarPericias(buscaPericia, filtroAtributoPericia, filtroDificuldadePericia)

    val magiasFiltradas: List<MagiaDefinicao>
        get() = dataRepository.filtrarMagias(buscaMagia, filtroEscolaMagia)

    val todasEscolasMagia: List<String>
        get() = dataRepository.magias
            .flatMap { it.escola ?: emptyList() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

    init {
        viewModelScope.launch {
            fichaStorage.migrarDeSharedPreferencesSeNecessario()
            carregarListaFichas()
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
        val magia = dataRepository.criarMagiaSelecionada(definicao, pontosGastos)
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
            lista[index] = magia
            personagem = personagem.copy(magias = lista)
        }
    }

    // === EQUIPAMENTOS ===

    fun adicionarEquipamento(equipamento: Equipamento) {
        val lista = personagem.equipamentos.toMutableList()
        lista.add(equipamento)
        personagem = personagem.copy(equipamentos = lista)
    }

    fun removerEquipamento(index: Int) {
        val lista = personagem.equipamentos.toMutableList()
        if (index in lista.indices) {
            lista.removeAt(index)
            personagem = personagem.copy(equipamentos = lista)
        }
    }

    fun atualizarEquipamento(index: Int, equipamento: Equipamento) {
        val lista = personagem.equipamentos.toMutableList()
        if (index in lista.indices) {
            lista[index] = equipamento
            personagem = personagem.copy(equipamentos = lista)
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
        fichasSalvas = fichaStorage.listarFichas()
    }

    // === UTILITARIOS ===

    val pesoTotal: Float get() = personagem.pesoTotal

    val custoTotalEquipamentos: Float get() = personagem.equipamentos.sumOf {
        (it.custo * it.quantidade).toDouble()
    }.toFloat()

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


