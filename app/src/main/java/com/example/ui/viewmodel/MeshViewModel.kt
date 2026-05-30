package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.CachedPage
import com.example.data.database.MeshLog
import com.example.data.repository.LocalWebPage
import com.example.data.repository.MeshRepository
import com.example.data.repository.PeerNode
import com.example.data.repository.WebCatalog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MeshViewModel(private val repository: MeshRepository) : ViewModel() {

    // --- Connection Settings ---
    // User can manually simulate connection types to test behavioral gates!
    private val _simulatedNetworkState = MutableStateFlow("WIFI") // "WIFI", "MOBILE_DATA", "OFFLINE"
    val simulatedNetworkState: StateFlow<String> = _simulatedNetworkState.asStateFlow()

    // Master configs (User Toggles)
    private val _shareWifiEnabled = MutableStateFlow(true)
    val shareWifiEnabled: StateFlow<Boolean> = _shareWifiEnabled.asStateFlow()

    private val _shareMobileDataEnabled = MutableStateFlow(false)
    val shareMobileDataEnabled: StateFlow<Boolean> = _shareMobileDataEnabled.asStateFlow()

    // Cryptography state
    private val _isEncryptionEnforced = MutableStateFlow(true)
    val isEncryptionEnforced: StateFlow<Boolean> = _isEncryptionEnforced.asStateFlow()

    val localPublicKeyStr = "0x7F2C A91E CE82 5D4B 9FE3 BB10 4D8A 099E CDFA"
    val localPrivateKeyStr = "0x5E80 18A2 EE45 CB6C BD32 23AA BD40 1EFA C910"

    // --- Dynamic Calculated states based on current connection & Toggles ---
    val isSharingWifiActive: StateFlow<Boolean> = combine(
        _simulatedNetworkState,
        _shareWifiEnabled
    ) { network, enabled ->
        network == "WIFI" && enabled
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isSharingMobileDataActive: StateFlow<Boolean> = combine(
        _simulatedNetworkState,
        _shareMobileDataEnabled
    ) { network, enabled ->
        network == "MOBILE_DATA" && enabled
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val meshNetworkRole: StateFlow<String> = combine(
        _simulatedNetworkState,
        isSharingWifiActive,
        isSharingMobileDataActive
    ) { network, wifiActive, dataActive ->
        when {
            network == "WIFI" && wifiActive -> "Gateway (Wi-Fi Compartido)"
            network == "MOBILE_DATA" && dataActive -> "Gateway (Datos Compartidos)"
            network == "OFFLINE" -> "Receptor Mesh"
            else -> "Relay Pasivo (Malla Local)"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Gateway (Wi-Fi Compartido)")

    // --- Peers Simulation & Active Malla Nodes ---
    private val _peersList = MutableStateFlow(repository.getInitialSimulatedPeers())
    val peersList: StateFlow<List<PeerNode>> = _peersList.asStateFlow()

    // --- Browser engine & States ---
    private val _currentUrl = MutableStateFlow("mesh.news")
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private val _currentPage = MutableStateFlow(WebCatalog.getPage("mesh.news")!!)
    val currentPage: StateFlow<LocalWebPage> = _currentPage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _lowConnectivityOptimization = MutableStateFlow(true)
    val lowConnectivityOptimization: StateFlow<Boolean> = _lowConnectivityOptimization.asStateFlow()

    private val _showImages = MutableStateFlow(false) // Toggle to compress images/load outline only
    val showImages: StateFlow<Boolean> = _showImages.asStateFlow()

    // Dynamic database feeds
    val cachedPages: StateFlow<List<CachedPage>> = repository.allCachedPages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedPages: StateFlow<List<CachedPage>> = repository.bookmarkedPages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isCurrentPageBookmarked: StateFlow<Boolean> = combine(
        _currentUrl,
        bookmarkedPages
    ) { url, bookmarks ->
        bookmarks.any { it.url == url }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val routerLogs: StateFlow<List<MeshLog>> = repository.logsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulated forum messages that stay in state for dynamic communication
    private val _forumMessages = MutableStateFlow(
        listOf(
            ForumMessage("Sara_P", "Hola, ¿alguien sabe si hay señal celular subiendo por el mirador? Uso este foro mesh porque allá arriba no hay cobertura 4G.", "hace 5 min"),
            ForumMessage("Carlos_M8", "Ofrezco mi enlace de Wi-Fi de alta velocidad en la esquina de la cafetería de 8:00 AM a 6:00 PM. ¡Activen compartir!", "hace 10 min"),
            ForumMessage("Eduardo_G", "Probando la red desde el sótano del edificio de ingenierías. ¡Me llega internet de rebote! Increíble.", "hace 12 min")
        )
    )
    val forumMessages: StateFlow<List<ForumMessage>> = _forumMessages.asStateFlow()

    // Simulated routing paths
    private val _currentRoutingPath = MutableStateFlow<List<String>>(emptyList())
    val currentRoutingPath: StateFlow<List<String>> = _currentRoutingPath.asStateFlow()

    init {
        // Seed initial friendly start up log
        viewModelScope.launch {
            repository.insertLog(
                MeshLog(
                    type = "WIFI_SHARE",
                    message = "Mesh Browser iniciado. Nodo Local: ${UUID.randomUUID().toString().substring(0,6).uppercase()}."
                )
            )
            repository.insertLog(
                MeshLog(
                    type = "P2P_ENCRYPT",
                    message = "Llaves de encriptación Efímeras generadas con Curve25519 (P2P activa en 256 bits)."
                )
            )
            triggerNetworkStateLogs(_simulatedNetworkState.value)
            calculateRoutingPath(_simulatedNetworkState.value)
        }
    }

    // --- Connection switches ---
    fun setSimulatedNetworkState(state: String) {
        viewModelScope.launch {
            _simulatedNetworkState.value = state
            triggerNetworkStateLogs(state)
            calculateRoutingPath(state)
        }
    }

    fun setShareWifiEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _shareWifiEnabled.value = enabled
            val stateName = if (enabled) "ACTIVADA (Esperando conexión Wifi)" else "DESACTIVADA"
            repository.insertLog(
                MeshLog(
                    type = "WIFI_SHARE",
                    message = "Compartir Conexión Wi-Fi ha sido $stateName por el usuario."
                )
            )
            triggerNetworkStateLogs(_simulatedNetworkState.value)
        }
    }

    fun setShareMobileDataEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _shareMobileDataEnabled.value = enabled
            val stateName = if (enabled) "ACTIVADA. Ayudarás a otros usuarios cercanos compartiendo tus datos cuando estés conectado al celular." else "DESACTIVADA"
            repository.insertLog(
                MeshLog(
                    type = "DATA_SHARE",
                    message = "Compartir Datos Móviles ha sido $stateName por el usuario."
                )
            )
            triggerNetworkStateLogs(_simulatedNetworkState.value)
        }
    }

    fun setEncryptionEnforced(enabled: Boolean) {
        viewModelScope.launch {
            _isEncryptionEnforced.value = enabled
            val messageText = if (enabled) {
                "Encriptación Extremo-a-Extremo Forzada. Todo paso intermedio viaja seguro bajo AES-GCM-256."
            } else {
                "⚠️ Encriptación Extremo-a-Extremo relajada. Los paquetes viajan legibles para nodos intermediarios."
            }
            repository.insertLog(MeshLog(type = "P2P_ENCRYPT", message = messageText))
        }
    }

    fun toggleImages() {
        _showImages.value = !_showImages.value
    }

    fun toggleLowConnectivityOptimization() {
        _lowConnectivityOptimization.value = !_lowConnectivityOptimization.value
    }

    private suspend fun triggerNetworkStateLogs(state: String) {
        val currentWifiShare = _shareWifiEnabled.value
        val currentDataShare = _shareMobileDataEnabled.value

        when (state) {
            "WIFI" -> {
                repository.insertLog(
                    MeshLog(
                        type = "WIFI_SHARE",
                        message = "Conexión Wi-Fi activa detectada."
                    )
                )
                if (currentWifiShare) {
                    repository.insertLog(
                        MeshLog(
                            type = "WIFI_SHARE",
                            message = "Compartiendo conexión Wi-Fi con radio de malla local (~5-8m). Rol: Gateway Principal."
                        )
                    )
                } else {
                    repository.insertLog(
                        MeshLog(
                            type = "WIFI_SHARE",
                            message = "Wi-Fi activo pero compartir deshabilitado voluntariamente por el usuario."
                        )
                    )
                }
            }
            "MOBILE_DATA" -> {
                repository.insertLog(
                    MeshLog(
                        type = "DATA_SHARE",
                        message = "Señal celular de Datos Móviles activa detectada."
                    )
                )
                if (currentDataShare) {
                    repository.insertLog(
                        MeshLog(
                            type = "DATA_SHARE",
                            message = "¡Gracias! Compartiendo datos móviles de forma controlada para aliviar zonas de baja cobertura."
                        )
                    )
                } else {
                    repository.insertLog(
                        MeshLog(
                            type = "DATA_SHARE",
                            message = "Datos móviles activos. No se comparte internet (Protegiendo tu saldo de telefonía)."
                        )
                    )
                }
            }
            "OFFLINE" -> {
                repository.insertLog(
                    MeshLog(
                        type = "ROUTING",
                        message = "Teléfono completamente sin internet centralizado. Modo Malla Receptor activado."
                    )
                )
                repository.insertLog(
                    MeshLog(
                        type = "PEER_CONNECT",
                        message = "Escaneando canales de malla inalámbricos locales... Nodos encontrados: Enlace Wifi, Punto Medio, Móvil Vecino."
                    )
                )
            }
        }
    }

    private fun calculateRoutingPath(networkState: String) {
        _currentRoutingPath.value = when (networkState) {
            "WIFI" -> listOf("Mi Dispositivo", "Enlace Sólido de Internet (Wi-Fi)")
            "MOBILE_DATA" -> listOf("Mi Dispositivo", "Torre Celular 4G/5G")
            "OFFLINE" -> listOf("Mi Navegador", "Punto Medio (Relay - 7m)", "Enlace Wifi (Gateway - 10m)")
            else -> listOf("Mi Dispositivo")
        }
    }

    // --- Web Navigation ---
    fun navigateToUrl(urlInput: String) {
        val cleanUrl = urlInput.trim().lowercase()
        if (cleanUrl.isEmpty()) return

        _currentUrl.value = cleanUrl
        _isLoading.value = true

        viewModelScope.launch {
            // Simulated mesh travel delay (2-hop latency in mesh adds around 1 sec)
            val baseDelay = when (_simulatedNetworkState.value) {
                "WIFI" -> 300L
                "MOBILE_DATA" -> 600L
                "OFFLINE" -> 1500L // 2 hops dynamic routing handshake latency
                else -> 1000L
            }
            delay(baseDelay)

            // Dynamic logs representing P2P exchange
            if (_simulatedNetworkState.value == "OFFLINE") {
                repository.insertLog(
                    MeshLog(
                        type = "PEER_CONNECT",
                        message = "Enviando paquete de búsqueda comprimido para: $cleanUrl"
                    )
                )
                delay(200)
                if (_isEncryptionEnforced.value) {
                    repository.insertLog(
                        MeshLog(
                            type = "P2P_ENCRYPT",
                            message = "Túnel de encriptación ECDH establecido con Nodo 'Punto Medio' (Relay)."
                        )
                    )
                }
                repository.insertLog(
                    MeshLog(
                        type = "ROUTING",
                        message = "Ruta establecida: Mi Dispositivo ──[Encriptado]──> Punto Medio ──[Encriptado]──> Enlace Wifi (Gateway) ──> Internet"
                    )
                )
            } else {
                repository.insertLog(
                    MeshLog(
                        type = "ROUTING",
                        message = "Carga de página directa para $cleanUrl como Nodo Gateway."
                    )
                )
            }

            // Fetch page
            val foundPage = WebCatalog.getPage(cleanUrl) ?: WebCatalog.makeDynamicPlaceholder(cleanUrl)
            _currentPage.value = foundPage

            // Simulated connection telemetry
            val bytesTransferred = if (_lowConnectivityOptimization.value) {
                foundPage.sizeBytesCompressed
            } else {
                foundPage.sizeBytesOriginal
            }

            // Save Snapshot in DB as Cached item to simulate Room local persistence
            repository.savePage(
                CachedPage(
                    url = foundPage.url,
                    title = foundPage.title,
                    content = foundPage.textContent,
                    originalBytes = foundPage.sizeBytesOriginal,
                    savedBytes = bytesTransferred,
                    isBookmark = false
                )
            )

            // Randomize sharing simulation increases byte counters on peer list!
            _peersList.value = _peersList.value.map { peer ->
                if (peer.id == "node_alpha" || peer.id == "node_beta") {
                    peer.copy(sharedBytesCount = peer.sharedBytesCount + bytesTransferred + (500..2000).random())
                } else {
                    peer
                }
            }

            _isLoading.value = false
        }
    }

    // --- Dynamic Community Gossip Chat ---
    fun postForumMessage(authorName: String, text: String) {
        if (text.trim().isEmpty()) return
        
        viewModelScope.launch {
            val simpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val messageTime = "hoy, " + simpleDateFormat.format(Date())

            val userMessage = ForumMessage(
                author = authorName.ifBlank { "Tú" },
                content = text,
                timeLabel = messageTime
            )

            _forumMessages.value = _forumMessages.value + userMessage
            repository.insertLog(
                MeshLog(
                    type = "ROUTING",
                    message = "Difundiendo mensaje P2P en canal Gossip Vecino: '${text.take(20)}...'"
                )
            )

            // Simulate immediate multi-hop chat echo/reply from an interesting peer!
            delay(1500)
            val simulatedReplies = listOf(
                Pair("Carlos_M8", "¡Recibidos tus datos por aquí de una! Conexión encriptada estable con tu nodo."),
                Pair("Sara_P", "Qué chido ver más gente unida a la red acá en el mirador. Sí funciona de maravilla."),
                Pair("MeshRelay_Bot", "Mensaje propagado con éxito en 3 saltos dinámicos. Latencia: 42ms.")
            ).random()

            val peerReply = ForumMessage(
                author = simulatedReplies.first,
                content = simulatedReplies.second,
                timeLabel = "hace un instante"
            )

            _forumMessages.value = _forumMessages.value + peerReply
            repository.insertLog(
                MeshLog(
                    type = "PEER_CONNECT",
                    message = "Paquete Gossip recibido de @${simulatedReplies.first}: '${simulatedReplies.second.take(20)}...'"
                )
            )
        }
    }

    // --- Bookmark operations ---
    fun toggleBookmark() {
        viewModelScope.launch {
            val targetUrl = _currentUrl.value
            val isBookmarkedNow = isCurrentPageBookmarked.value
            repository.toggleBookmark(targetUrl, !isBookmarkedNow)
            
            repository.insertLog(
                MeshLog(
                    type = "ROUTING",
                    message = if (!isBookmarkedNow) "Marcador guardado: $targetUrl para lectura 100% desconectada." else "Marcador removido: $targetUrl"
                )
            )
        }
    }

    fun deletePage(page: CachedPage) {
        viewModelScope.launch {
            repository.deletePage(page)
            repository.insertLog(MeshLog(type = "ROUTING", message = "Página purgada del caché local."))
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }
}

data class ForumMessage(
    val author: String,
    val content: String,
    val timeLabel: String
)

class MeshViewModelFactory(private val repository: MeshRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MeshViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MeshViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
