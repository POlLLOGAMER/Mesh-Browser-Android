package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.CachedPage
import com.example.data.database.MeshLog
import com.example.data.repository.PeerNode
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.viewmodel.MeshViewModel
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeshBrowserScreen(
    viewModel: MeshViewModel,
    modifier: Modifier = Modifier
) {
    val simulatedNetwork by viewModel.simulatedNetworkState.collectAsStateWithLifecycle()
    val shareWifiEnabled by viewModel.shareWifiEnabled.collectAsStateWithLifecycle()
    val shareMobileDataEnabled by viewModel.shareMobileDataEnabled.collectAsStateWithLifecycle()
    val isSharingWifiActive by viewModel.isSharingWifiActive.collectAsStateWithLifecycle()
    val isSharingMobileDataActive by viewModel.isSharingMobileDataActive.collectAsStateWithLifecycle()
    val encryptionOn by viewModel.isEncryptionEnforced.collectAsStateWithLifecycle()
    val networkRole by viewModel.meshNetworkRole.collectAsStateWithLifecycle()
    val peersList by viewModel.peersList.collectAsStateWithLifecycle()

    val currentUrl by viewModel.currentUrl.collectAsStateWithLifecycle()
    val currentPage by viewModel.currentPage.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val lowConnectivityOpt by viewModel.lowConnectivityOptimization.collectAsStateWithLifecycle()
    val showImages by viewModel.showImages.collectAsStateWithLifecycle()

    val cachedPages by viewModel.cachedPages.collectAsStateWithLifecycle()
    val isPageBookmarked by viewModel.isCurrentPageBookmarked.collectAsStateWithLifecycle()
    val routerLogs by viewModel.routerLogs.collectAsStateWithLifecycle()
    val forumMessages by viewModel.forumMessages.collectAsStateWithLifecycle()
    val routingPath by viewModel.currentRoutingPath.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Browser, 1: Mesh Console, 2: Community & Logs
    var urlFieldText by remember { mutableStateOf(currentUrl) }
    var forumInputText by remember { mutableStateOf("") }
    var forumAuthorText by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Sync address bar text with viewModel url state
    LaunchedEffect(currentUrl) {
        urlFieldText = currentUrl
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (simulatedNetwork != "OFFLINE") Color(0xFF22C55E) else Color(0xFFEF4444))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Mesh Browser",
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.5).sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF22C55E))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Mesh Active",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Quick network state pills
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (simulatedNetwork) {
                            "WIFI" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            "MOBILE_DATA" -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                            else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                        },
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = when (simulatedNetwork) {
                                    "WIFI" -> Icons.Default.Wifi
                                    "MOBILE_DATA" -> Icons.Default.SignalCellular4Bar
                                    else -> Icons.Default.WifiOff
                                },
                                contentDescription = "Conexión",
                                modifier = Modifier.size(14.dp),
                                tint = when (simulatedNetwork) {
                                    "WIFI" -> CyberGreen
                                    "MOBILE_DATA" -> CyberCyan
                                    else -> Color(0xFFFF5252)
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (simulatedNetwork) {
                                    "WIFI" -> "Wi-Fi"
                                    "MOBILE_DATA" -> "Celular"
                                    else -> "Malla (Offline)"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (simulatedNetwork) {
                                    "WIFI" -> CyberGreen
                                    "MOBILE_DATA" -> CyberCyan
                                    else -> Color(0xFFFF8A80)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            // Material 3 bottom navigation bar
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.Language, contentDescription = "Tab Navegador") },
                    label = { Text("Navegador") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = CyberGreen,
                        indicatorColor = CyberGreen
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.Hub, contentDescription = "Tab Consola Malla") },
                    label = { Text("Red Malla") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = CyberGreen,
                        indicatorColor = CyberGreen
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.Forum, contentDescription = "Tab Foros y Bitácora") },
                    label = { Text("Social y Logs") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = CyberGreen,
                        indicatorColor = CyberGreen
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab contents
            Box(modifier = Modifier.weight(1f)) {
                when (activeTab) {
                    0 -> BrowserTabContent(
                        urlFieldText = urlFieldText,
                        onUrlFieldTextChange = { urlFieldText = it },
                        currentPage = currentPage,
                        isLoading = isLoading,
                        lowConnectivityOpt = lowConnectivityOpt,
                        showImages = showImages,
                        isCurrentPageBookmarked = isPageBookmarked,
                        routingPath = routingPath,
                        encryptionOn = encryptionOn,
                        onNavigate = {
                            keyboardController?.hide()
                            viewModel.navigateToUrl(urlFieldText)
                        },
                        onToggleBookmark = { viewModel.toggleBookmark() },
                        onToggleLowConnectivity = { viewModel.toggleLowConnectivityOptimization() },
                        onToggleImages = { viewModel.toggleImages() }
                    )
                    1 -> MeshConsoleTabContent(
                        simulatedNetwork = simulatedNetwork,
                        onSetNetworkState = { viewModel.setSimulatedNetworkState(it) },
                        shareWifiEnabled = shareWifiEnabled,
                        onSetShareWifi = { viewModel.setShareWifiEnabled(it) },
                        shareMobileDataEnabled = shareMobileDataEnabled,
                        onSetShareMobileData = { viewModel.setShareMobileDataEnabled(it) },
                        isSharingWifiActive = isSharingWifiActive,
                        isSharingMobileDataActive = isSharingMobileDataActive,
                        networkRole = networkRole,
                        encryptionOn = encryptionOn,
                        onToggleEncryption = { viewModel.setEncryptionEnforced(it) },
                        localKeyPublic = viewModel.localPublicKeyStr,
                        localKeyPrivate = viewModel.localPrivateKeyStr,
                        peersList = peersList
                    )
                    2 -> CommunityTabContent(
                        forumMessages = forumMessages,
                        forumAuthorText = forumAuthorText,
                        onForumAuthorChange = { forumAuthorText = it },
                        forumInputText = forumInputText,
                        onForumInputChange = { forumInputText = it },
                        onSendForumMessage = {
                            viewModel.postForumMessage(forumAuthorText, forumInputText)
                            forumInputText = ""
                        },
                        cachedPages = cachedPages,
                        onLoadPage = { cached ->
                            activeTab = 0
                            viewModel.navigateToUrl(cached.url)
                        },
                        onDeletePage = { viewModel.deletePage(it) },
                        routerLogs = routerLogs,
                        onClearLogs = { viewModel.clearAllLogs() }
                    )
                }
            }
        }
    }
}

// ==========================================
// TAB 1: BROWSER CONTENT
// ==========================================
@Composable
fun BrowserTabContent(
    urlFieldText: String,
    onUrlFieldTextChange: (String) -> Unit,
    currentPage: com.example.data.repository.LocalWebPage,
    isLoading: Boolean,
    lowConnectivityOpt: Boolean,
    showImages: Boolean,
    isCurrentPageBookmarked: Boolean,
    routingPath: List<String>,
    encryptionOn: Boolean,
    onNavigate: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleLowConnectivity: () -> Unit,
    onToggleImages: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Address Bar with modern minimal styling
        OutlinedTextField(
            value = urlFieldText,
            onValueChange = onUrlFieldTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("browser_address_input"),
            placeholder = { Text("Ingresa dirección web (ej: wikipedia.org)...", color = Color.Gray, fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = CyberGreen
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.testTag("bookmark_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (isCurrentPageBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Bookmark site",
                        tint = if (isCurrentPageBookmarked) CyberGreen else Color.Gray
                    )
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = { onNavigate() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberGreen,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                cursorColor = CyberGreen
            ),
            shape = RoundedCornerShape(28.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Popular offline mesh nodes quick links
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val suggestions = listOf(
                "mesh.news" to "Noticias",
                "wikipedia.org" to "Wiki",
                "local.forum" to "Foro Vecinos",
                "clima.node" to "Clima local"
            )
            suggestions.forEach { (url, label) ->
                SuggestionChip(
                    onClick = {
                        onUrlFieldTextChange(url)
                        onNavigate()
                    },
                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    border = BorderStroke(1.dp, CyberGreen.copy(alpha = 0.5f)),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        labelColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Technical optimize compression metrics control bar
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Compression switch
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.OfflineBolt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = CyberGreen
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Filtro Conectividad Baja",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Switch 1: Offline text compress filter
                    FilterChip(
                        selected = lowConnectivityOpt,
                        onClick = onToggleLowConnectivity,
                        label = { Text("Comprimir", fontSize = 10.sp) },
                        leadingIcon = if (lowConnectivityOpt) {
                            { Icon(Icons.Default.Check, "Activo", modifier = Modifier.size(12.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberGreen.copy(alpha = 0.3f),
                            selectedLabelColor = CyberGreen
                        )
                    )

                    // Switch 2: Show images or placeholder
                    FilterChip(
                        selected = showImages,
                        onClick = onToggleImages,
                        label = { Text("Fotos", fontSize = 10.sp) },
                        leadingIcon = if (showImages) {
                            { Icon(Icons.Default.Image, "Fotos ON", modifier = Modifier.size(12.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan.copy(alpha = 0.3f),
                            selectedLabelColor = CyberCyan
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Primary Webpage Canvas View
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = CyberGreen, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Ruteando paquetes a través de la malla local...",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Encriptación: ${if (encryptionOn) "🔒 256-bit P2P" else "🔓 En claro"}",
                            fontSize = 11.sp,
                            color = CyberGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Web Header Metadata
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CyberGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = currentPage.category.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CyberGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            // Telemetry data compression ratio
                            val savingPercent = if (currentPage.sizeBytesOriginal > 0) {
                                ((currentPage.sizeBytesOriginal - currentPage.sizeBytesCompressed).toFloat() / currentPage.sizeBytesOriginal * 100).toInt()
                            } else 0

                            Text(
                                text = "Ahorro: $savingPercent% (~${(currentPage.sizeBytesOriginal - currentPage.sizeBytesCompressed)/1024} KB)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Unified Page Title
                        Text(
                            text = currentPage.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 24.sp
                        )

                        Text(
                            text = "Source: ${currentPage.url}",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                        )

                        // Simplified body view
                        Text(
                            text = currentPage.textContent,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Justify
                        )

                        // Simulated compressed picture box representation
                        if (currentPage.hasImages) {
                            Spacer(modifier = Modifier.height(16.dp))
                            if (showImages) {
                                // Simulate loaded compressed image mockup
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.DarkGray.copy(alpha = 0.3f))
                                        .border(2.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.CloudQueue,
                                            contentDescription = null,
                                            tint = CyberCyan,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            "Foto Comprimida (Vía Gateway Proxy)",
                                            fontSize = 11.sp,
                                            color = CyberCyan,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            "Peso descodificado: 11 KB",
                                            fontSize = 9.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                }
                            } else {
                                // Classic Opera Mini Compressed Outline Box
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.background)
                                        .border(
                                            border = BorderStroke(1.dp, CyberGreen.copy(alpha = 0.4f)),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onToggleImages() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 14.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.HideImage,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "[Imagen en caché bloqueada de 145 KB]",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Gray
                                            )
                                            Text(
                                                text = "Toca para recargar imagen comprimida (+12 KB)",
                                                fontSize = 9.sp,
                                                color = CyberGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Hop-routing Path Visualization panel (Under browser as required!)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            border = BorderStroke(1.dp, CyberGreen.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LinearScale,
                        contentDescription = "Ruta de Saltos",
                        modifier = Modifier.size(14.dp),
                        tint = CyberGreen
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ruta Física del Paquete en la Malla",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberGreen
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Draw structured hop blocks
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    routingPath.forEachIndexed { index, name ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (index == 0) CyberGreen else CyberCyan)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "H${index + 1}",
                                        color = Color.Black,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (index < routingPath.size - 1) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (encryptionOn) "──🔒──>" else "─────>",
                                    fontSize = 9.sp,
                                    color = if (encryptionOn) CyberGreen else Color.Gray,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 2: MESH NETWORK SHARING CONTROLS
// ==========================================
@Composable
fun MeshConsoleTabContent(
    simulatedNetwork: String,
    onSetNetworkState: (String) -> Unit,
    shareWifiEnabled: Boolean,
    onSetShareWifi: (Boolean) -> Unit,
    shareMobileDataEnabled: Boolean,
    onSetShareMobileData: (Boolean) -> Unit,
    isSharingWifiActive: Boolean,
    isSharingMobileDataActive: Boolean,
    networkRole: String,
    encryptionOn: Boolean,
    onToggleEncryption: (Boolean) -> Unit,
    localKeyPublic: String,
    localKeyPrivate: String,
    peersList: List<PeerNode>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Section A: Simulated device physical connection mode (Allows testing easily!)
        Text(
            text = "Simulador de Enlace de Entrada",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = CyberGreen
        )
        Text(
            text = "Cambia el estado de red de tu cel para probar cómo actúa automáticamente la malla:",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val netModes = listOf(
                "WIFI" to "Conectado a Wifi",
                "MOBILE_DATA" to "Conectado a Celular",
                "OFFLINE" to "Modo Malla (Offline)"
            )
            netModes.forEach { (mode, title) ->
                val isSel = simulatedNetwork == mode
                Button(
                    onClick = { onSetNetworkState(mode) },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSel) CyberGreen else MaterialTheme.colorScheme.surface,
                        contentColor = if (isSel) Color.White else MaterialTheme.colorScheme.onBackground
                    ),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = if (!isSel) BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer) else null
                ) {
                    Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Static visual badge showing current actual role
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSharingWifiActive || isSharingMobileDataActive) {
                                CyberGreen.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSharingWifiActive || isSharingMobileDataActive) Icons.Default.PortableWifiOff else Icons.Default.CellTower,
                        contentDescription = null,
                        tint = if (isSharingWifiActive || isSharingMobileDataActive) CyberGreen else Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Rol de Red:",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = networkRole,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyberGreen,
                        fontSize = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION B: THE REQUIRED SHARING SEPARATE BUTTONS (Toggles)
        Text(
            text = "Parámetros de Retransmisión",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = CyberGreen
        )
        Text(
            text = "Si activas ambos, la app inteligentemente retransmitirá según tu red activa en el momento:",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Switch 1: Compartir Wi-Fi (Tethering / Relay)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(
                width = 1.dp,
                color = if (isSharingWifiActive) CyberGreen.copy(alpha = 0.5f) else Color.Transparent
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSharingWifiActive) CyberGreen.copy(alpha = 0.1f) else Color.DarkGray.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = if (isSharingWifiActive) CyberGreen else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Compartir Conexión Wi-Fi",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isSharingWifiActive) "Activo (Comparte banda ancha)" else "Inactivo / Apagado",
                            fontSize = 10.sp,
                            color = if (isSharingWifiActive) CyberGreen else Color.Gray
                        )
                    }
                }

                Switch(
                    checked = shareWifiEnabled,
                    onCheckedChange = onSetShareWifi,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = CyberGreen
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Switch 2: Compartir Datos Móviles (Datalink Help neighboring users)
        // **IMPORTANT**: This is the separate MOBILE DATA sharing button as explicitly requested!
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(
                width = 1.dp,
                color = if (isSharingMobileDataActive) CyberCyan.copy(alpha = 0.5f) else Color.Transparent
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSharingMobileDataActive) CyberCyan.copy(alpha = 0.1f) else Color.DarkGray.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SignalCellularAlt,
                            contentDescription = null,
                            tint = if (isSharingMobileDataActive) CyberCyan else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Compartir Datos Celulares",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFF9100).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "DATOS",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9100),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isSharingMobileDataActive) "Activo (Ayudando a usuarios cercanos)" else "Inactivo (Protegiendo tu saldo)",
                            fontSize = 10.sp,
                            color = if (isSharingMobileDataActive) CyberCyan else Color.Gray
                        )
                    }
                }

                Switch(
                    checked = shareMobileDataEnabled,
                    onCheckedChange = onSetShareMobileData,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = CyberCyan
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION C: INTERACTIVE GRAPHICAL NODES DRAWING CANVAS
        Text(
            text = "Mapa de Enlace Local",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = CyberGreen
        )
        Text(
            text = "Señal de malla encriptada punto-a-punto activa a tu alrededor:",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "Radar lines")
            val pulseRatio by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3500, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "Radar pulses"
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width * 0.5f, size.height * 0.5f)
                val baseRadius = size.height * 0.45f

                // Draw radar circles
                drawCircle(
                    color = CyberGreen.copy(alpha = 0.08f * (1f - pulseRatio)),
                    radius = baseRadius * pulseRatio,
                    center = center,
                    style = Stroke(width = 3f)
                )

                // Draw secondary pulse
                val secondPulse = (pulseRatio + 0.5f) % 1.0f
                drawCircle(
                    color = CyberCyan.copy(alpha = 0.08f * (1f - secondPulse)),
                    radius = baseRadius * secondPulse,
                    center = center,
                    style = Stroke(width = 3f)
                )

                // Central radar guidelines
                drawCircle(
                    color = Color.LightGray.copy(alpha = 0.25f),
                    radius = baseRadius * 0.6f,
                    center = center,
                    style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                )

                // Draw links from central device to peers
                val peerPositions = listOf(
                    Offset(size.width * 0.2f, size.height * 0.25f), // node_alpha
                    Offset(size.width * 0.8f, size.height * 0.35f), // node_beta
                    Offset(size.width * 0.3f, size.height * 0.75f), // node_gamma
                    Offset(size.width * 0.85f, size.height * 0.75f) // node_delta
                )

                peerPositions.forEachIndexed { idx, pos ->
                    drawLine(
                        color = if (encryptionOn) CyberGreen.copy(alpha = 0.25f) else Color.Gray.copy(alpha = 0.25f),
                        start = center,
                        end = pos,
                        strokeWidth = 6f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), (pulseRatio * 40f))
                    )

                    // Moving encryption key dots along connection path
                    val dotT = (pulseRatio + (idx * 0.25f)) % 1f
                    val dotPos = Offset(
                        center.x + (pos.x - center.x) * dotT,
                        center.y + (pos.y - center.y) * dotT
                    )
                    drawCircle(
                        color = if (encryptionOn) CyberGreen else CyberCyan,
                        radius = 12f,
                        center = dotPos
                    )
                }

                // Draw central device node
                drawCircle(
                    color = CyberGreen,
                    radius = 33f,
                    center = center
                )
                drawCircle(
                    color = Color.White,
                    radius = 24f,
                    center = center
                )
                drawCircle(
                    color = CyberGreen,
                    radius = 15f,
                    center = center
                )

                // Draw peer outer nodes
                peerPositions.forEach { pos ->
                    drawCircle(
                        color = CyberCyan.copy(alpha = 0.12f),
                        radius = 48f,
                        center = pos
                    )
                    drawCircle(
                        color = CyberCyan,
                        radius = 18f,
                        center = pos
                    )
                }
            }

            // Custom dynamic overlays to explain parts of design
            Text(
                "Mi Celular (H1)",
                color = CyberGreen,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 15.dp)
                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )

            Text(
                "Nodo Alfa (Gateway)",
                color = CyberCyan,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 10.dp, y = 14.dp)
                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )

            Text(
                "Punto Medio (Relay)",
                color = CyberCyan,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-30).dp, y = 35.dp)
                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION D: ENCRYPTED SECURITY DATA KEYS (P2P Handshake)
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Https,
                            contentDescription = "Seguridad P2P",
                            modifier = Modifier.size(16.dp),
                            tint = CyberGreen
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Seguridad de Tráfico P2P",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Switch(
                        checked = encryptionOn,
                        onCheckedChange = onToggleEncryption,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CyberGreen
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Llave Pública del Dispositivo (Curve25519):",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = localKeyPublic,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CyberGreen
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Llave Privada Efímera de Sesión (Protegida):",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = localKeyPrivate.take(15) + "••••••••••••••••••••••••••••",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION E: PEERS IN RANGE DETAILS
        Text(
            text = "Dispositivos Cercanos en Rango",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = CyberGreen
        )

        Spacer(modifier = Modifier.height(8.dp))

        peersList.forEach { peer ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(CyberCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = peer.name.take(1),
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = peer.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "IP: ${peer.address}  •  Danza: ${peer.distanceMeters}m",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = CircleShape,
                            color = when (peer.role) {
                                "Gateway" -> CyberGreen.copy(alpha = 0.15f)
                                "Relay" -> CyberCyan.copy(alpha = 0.15f)
                                else -> Color.Gray.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = peer.role,
                                fontSize = 8.sp,
                                color = when (peer.role) {
                                    "Gateway" -> CyberGreen
                                    "Relay" -> CyberCyan
                                    else -> Color.White
                                },
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Relay: ${(peer.sharedBytesCount / 1024)} KB",
                            fontSize = 9.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 3: COMMUNITY CHAT & ROUTER LOGS
// ==========================================
@Composable
fun CommunityTabContent(
    forumMessages: List<com.example.ui.viewmodel.ForumMessage>,
    forumAuthorText: String,
    onForumAuthorChange: (String) -> Unit,
    forumInputText: String,
    onForumInputChange: (String) -> Unit,
    onSendForumMessage: () -> Unit,
    cachedPages: List<CachedPage>,
    onLoadPage: (CachedPage) -> Unit,
    onDeletePage: (CachedPage) -> Unit,
    routerLogs: List<MeshLog>,
    onClearLogs: () -> Unit
) {
    var logsOrForumByChoice by remember { mutableIntStateOf(0) } // 0: Chat Foro Gossip, 1: Biblioteca Offline, 2: Logs Router
    val listState = rememberLazyListState()

    // Scroll chat bottom on news
    LaunchedEffect(forumMessages.size) {
        if (logsOrForumByChoice == 0 && forumMessages.isNotEmpty()) {
            listState.animateScrollToItem(forumMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Toggle Buttons list
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val sections = listOf(
                "Foro Mesh" to 0,
                "Biblioteca Offline" to 1,
                "Logs Enrutador" to 2
            )
            sections.forEach { (title, id) ->
                val active = logsOrForumByChoice == id
                Button(
                    onClick = { logsOrForumByChoice = id },
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (active) CyberGreen else MaterialTheme.colorScheme.surface,
                        contentColor = if (active) Color.White else MaterialTheme.colorScheme.onBackground
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (logsOrForumByChoice) {
            0 -> {
                // SECTION A: CHAT FORUM GOSSIP
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Foro P2P de Chisme Vecino",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyberGreen
                    )
                    Text(
                        text = "Vibrando de forma autónoma en el dial local de red sin requerir internet tradicional.",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(forumMessages) { msg ->
                            val isMe = msg.author.lowercase() == "tú" || msg.author.lowercase() == "yo"
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = msg.author,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isMe) CyberGreen else CyberCyan
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = msg.timeLabel,
                                        fontSize = 8.sp,
                                        color = Color.Gray
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 10.dp,
                                                topEnd = 10.dp,
                                                bottomStart = if (isMe) 10.dp else 0.dp,
                                                bottomEnd = if (isMe) 0.dp else 10.dp
                                            )
                                        )
                                        .background(
                                            if (isMe) CyberGreen.copy(alpha = 0.12f) else CyberCyan.copy(
                                                alpha = 0.12f
                                            )
                                        )
                                        .border(
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = if (isMe) CyberGreen.copy(alpha = 0.3f) else CyberCyan.copy(
                                                    alpha = 0.3f
                                                )
                                            ),
                                            shape = RoundedCornerShape(
                                                topStart = 10.dp,
                                                topEnd = 10.dp,
                                                bottomStart = if (isMe) 10.dp else 0.dp,
                                                bottomEnd = if (isMe) 0.dp else 10.dp
                                            )
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = msg.content,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Input Form
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = forumAuthorText,
                            onValueChange = onForumAuthorChange,
                            modifier = Modifier.width(85.dp).testTag("author_input"),
                            placeholder = { Text("Alias", fontSize = 11.sp, color = Color.Gray) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberGreen,
                                cursorColor = CyberGreen
                            )
                        )

                        OutlinedTextField(
                            value = forumInputText,
                            onValueChange = onForumInputChange,
                            placeholder = { Text("Escribe algo...", fontSize = 11.sp, color = Color.Gray) },
                            modifier = Modifier.weight(1f).testTag("message_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { onSendForumMessage() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberGreen,
                                cursorColor = CyberGreen
                            )
                        )

                        IconButton(
                            onClick = onSendForumMessage,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyberGreen)
                                .testTag("send_message_btn")
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Enviar mensaje",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
            1 -> {
                // SECTION B: READ OFFLINE PAGES LIBRARY
                Text(
                    text = "Biblioteca Desconectada",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CyberGreen
                )
                Text(
                    text = "Navega de forma 100% offline con snapshots encriptados guardados localmente.",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (cachedPages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.DarkGray.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.LibraryBooks,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Biblioteca vacía por ahora",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                "Visita páginas en el Navegador para autocachear",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(cachedPages) { page ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onLoadPage(page) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Default.OfflineShare,
                                            contentDescription = null,
                                            tint = CyberGreen,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = page.title,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Lectura Offline: ${page.url}",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                            Text(
                                                text = "Tamaño Comprimido: ${page.savedBytes / 1024} KB (Original: ${page.originalBytes / 1024} KB)",
                                                fontSize = 9.sp,
                                                color = CyberCyan
                                            )
                                        }
                                    }

                                    IconButton(onClick = { onDeletePage(page) }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Borrar",
                                            tint = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // SECTION C: ENRUTADOR LOGS (Packet Sniffer simulation)
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Bitácora del Enrutador Malla",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyberGreen
                            )
                            Text(
                                text = "Lector de paquetes de transporte en tiempo real.",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = onClearLogs,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color(0xFFFF5252)
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Limpiar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (routerLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color.DarkGray.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Sin logs de red por ahora. Navega un poco...", color = Color.Gray, fontSize = 11.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black)
                                .border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        ) {
                            items(routerLogs) { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = when (log.type) {
                                            "WIFI_SHARE" -> " [WIFI] "
                                            "DATA_SHARE" -> " [DATA] "
                                            "P2P_ENCRYPT" -> "[E2EE] "
                                            "PEER_CONNECT" -> " [CONN] "
                                            else -> " [ROUT] "
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 8.sp,
                                        color = when (log.type) {
                                            "WIFI_SHARE" -> CyberGreen
                                            "DATA_SHARE" -> CyberCyan
                                            "P2P_ENCRYPT" -> Color(0xFFFFEB3B)
                                            "PEER_CONNECT" -> Color(0xFFE040FB)
                                            else -> Color.White
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = log.message,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.LightGray,
                                        lineHeight = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
