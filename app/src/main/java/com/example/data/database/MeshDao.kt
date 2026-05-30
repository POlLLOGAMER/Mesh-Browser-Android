package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MeshDao {
    // Cached pages queries
    @Query("SELECT * FROM cached_pages ORDER BY timestamp DESC")
    fun getAllCachedPages(): Flow<List<CachedPage>>

    @Query("SELECT * FROM cached_pages WHERE isBookmark = 1 ORDER BY timestamp DESC")
    fun getBookmarkedPages(): Flow<List<CachedPage>>

    @Query("SELECT * FROM cached_pages WHERE url = :url LIMIT 1")
    suspend fun getPageByUrl(url: String): CachedPage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPage(page: CachedPage)

    @Delete
    suspend fun deletePage(page: CachedPage)

    @Query("UPDATE cached_pages SET isBookmark = :isBookmarked WHERE url = :url")
    suspend fun updateBookmarkStatus(url: String, isBookmarked: Boolean)

    // Mesh logs queries
    @Query("SELECT * FROM mesh_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllLogs(): Flow<List<MeshLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MeshLog)

    @Query("DELETE FROM mesh_logs")
    suspend fun clearLogs()
}
