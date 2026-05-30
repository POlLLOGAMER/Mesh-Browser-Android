package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_pages")
data class CachedPage(
    @PrimaryKey val url: String,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isBookmark: Boolean = false,
    val originalBytes: Long = 0,
    val savedBytes: Long = 0
)

@Entity(tableName = "mesh_logs")
data class MeshLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // "WIFI_SHARE", "DATA_SHARE", "P2P_ENCRYPT", "ROUTING", "PEER_CONNECT"
    val message: String,
    val sender: String = "",
    val recipient: String = "",
    val bytes: Long = 0
)
