package com.example.data.repository

import com.example.data.database.CachedPage
import com.example.data.database.MeshDao
import com.example.data.database.MeshLog
import kotlinx.coroutines.flow.Flow
import java.util.UUID

data class PeerNode(
    val id: String,
    val name: String,
    val role: String, // "Gateway" (comparte internet principal), "Relay" (repite señal), "Leaf" (solo recibe)
    val connectionType: String, // "WIFI", "MOBILE_DATA", "MESH_ONLY"
    val distanceMeters: Float,
    val signalPercentage: Int,
    val isEncrypted: Boolean = true,
    val sharedBytesCount: Long = 0,
    val address: String = "192.168.43." + (2..254).random()
)

class MeshRepository(private val meshDao: MeshDao) {

    // Cached pages handling
    val allCachedPages: Flow<List<CachedPage>> = meshDao.getAllCachedPages()
    val bookmarkedPages: Flow<List<CachedPage>> = meshDao.getBookmarkedPages()

    suspend fun getPage(url: String): CachedPage? = meshDao.getPageByUrl(url)

    suspend fun savePage(page: CachedPage) {
        meshDao.insertPage(page)
        // Add log entry
        val savedPercent = if (page.originalBytes > 0) {
            ((page.originalBytes - page.savedBytes).toFloat() / page.originalBytes * 100).toInt()
        } else 0
        
        insertLog(
            MeshLog(
                type = "ROUTING",
                message = "Pág. guardada en caché local: ${page.title} (${page.savedBytes / 1024} KB). ¡Ahorro del $savedPercent% vía compresión de malla!",
                bytes = page.savedBytes
            )
        )
    }

    suspend fun deletePage(page: CachedPage) = meshDao.deletePage(page)

    suspend fun toggleBookmark(url: String, isBookmarked: Boolean) {
        meshDao.updateBookmarkStatus(url, isBookmarked)
    }

    // Mesh logs
    val logsFlow: Flow<List<MeshLog>> = meshDao.getAllLogs()

    suspend fun insertLog(log: MeshLog) {
        meshDao.insertLog(log)
    }

    suspend fun clearLogs() {
        meshDao.clearLogs()
    }

    // Generate static nodes for local network visual simulation
    fun getInitialSimulatedPeers(): List<PeerNode> {
        return listOf(
            PeerNode(
                id = "node_alpha",
                name = "Enlace Wifi (Gateway)",
                role = "Gateway",
                connectionType = "WIFI",
                distanceMeters = 3.5f,
                signalPercentage = 94,
                isEncrypted = true,
                sharedBytesCount = 458920
            ),
            PeerNode(
                id = "node_beta",
                name = "Punto Medio (Relay)",
                role = "Relay",
                connectionType = "MESH_ONLY",
                distanceMeters = 7.2f,
                signalPercentage = 78,
                isEncrypted = true,
                sharedBytesCount = 210400
            ),
            PeerNode(
                id = "node_gamma",
                name = "Móvil Vecino (Relay-Datalink)",
                role = "Relay",
                connectionType = "MOBILE_DATA",
                distanceMeters = 5.1f,
                signalPercentage = 85,
                isEncrypted = true,
                sharedBytesCount = 89040
            ),
            PeerNode(
                id = "node_delta",
                name = "Receptor Mesh",
                role = "Leaf",
                connectionType = "MESH_ONLY",
                distanceMeters = 11.4f,
                signalPercentage = 46,
                isEncrypted = true,
                sharedBytesCount = 5740
            )
        )
    }
}
