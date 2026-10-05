package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CaptionDao {
    @Query("SELECT * FROM captions WHERE projectId = :projectId ORDER BY startMs ASC")
    fun getCaptionsForProjectFlow(projectId: String): Flow<List<CaptionEntity>>

    @Query("SELECT * FROM captions WHERE projectId = :projectId ORDER BY startMs ASC")
    suspend fun getCaptionsForProject(projectId: String): List<CaptionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaption(caption: CaptionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaptions(captions: List<CaptionEntity>)

    @Update
    suspend fun updateCaption(caption: CaptionEntity)

    @Delete
    suspend fun deleteCaption(caption: CaptionEntity)

    @Query("DELETE FROM captions WHERE projectId = :projectId")
    suspend fun deleteAllCaptionsForProject(projectId: String)

    @Query("DELETE FROM captions WHERE id = :id")
    suspend fun deleteCaptionById(id: String)
}
