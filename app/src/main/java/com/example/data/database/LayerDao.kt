package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LayerDao {
    // Text layers
    @Query("SELECT * FROM text_layers WHERE projectId = :projectId ORDER BY startMs ASC")
    fun getTextLayersForProjectFlow(projectId: String): Flow<List<TextLayerEntity>>

    @Query("SELECT * FROM text_layers WHERE projectId = :projectId ORDER BY startMs ASC")
    suspend fun getTextLayersForProject(projectId: String): List<TextLayerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTextLayer(layer: TextLayerEntity)

    @Update
    suspend fun updateTextLayer(layer: TextLayerEntity)

    @Delete
    suspend fun deleteTextLayer(layer: TextLayerEntity)

    @Query("DELETE FROM text_layers WHERE id = :id")
    suspend fun deleteTextLayerById(id: String)

    // Sticker layers
    @Query("SELECT * FROM sticker_layers WHERE projectId = :projectId ORDER BY startMs ASC")
    fun getStickerLayersForProjectFlow(projectId: String): Flow<List<StickerLayerEntity>>

    @Query("SELECT * FROM sticker_layers WHERE projectId = :projectId ORDER BY startMs ASC")
    suspend fun getStickerLayersForProject(projectId: String): List<StickerLayerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStickerLayer(layer: StickerLayerEntity)

    @Update
    suspend fun updateStickerLayer(layer: StickerLayerEntity)

    @Delete
    suspend fun deleteStickerLayer(layer: StickerLayerEntity)

    @Query("DELETE FROM sticker_layers WHERE id = :id")
    suspend fun deleteStickerLayerById(id: String)

    // Tracking objects
    @Query("SELECT * FROM tracking_objects WHERE projectId = :projectId ORDER BY startMs ASC")
    fun getTrackingObjectsForProjectFlow(projectId: String): Flow<List<TrackingObjectEntity>>

    @Query("SELECT * FROM tracking_objects WHERE projectId = :projectId ORDER BY startMs ASC")
    suspend fun getTrackingObjectsForProject(projectId: String): List<TrackingObjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackingObject(obj: TrackingObjectEntity)

    @Update
    suspend fun updateTrackingObject(obj: TrackingObjectEntity)

    @Delete
    suspend fun deleteTrackingObject(obj: TrackingObjectEntity)

    @Query("DELETE FROM tracking_objects WHERE id = :id")
    suspend fun deleteTrackingObjectById(id: String)

    // Audio tracks
    @Query("SELECT * FROM audio_tracks WHERE projectId = :projectId ORDER BY startTimelineMs ASC")
    fun getAudioTracksForProjectFlow(projectId: String): Flow<List<AudioTrackEntity>>

    @Query("SELECT * FROM audio_tracks WHERE projectId = :projectId ORDER BY startTimelineMs ASC")
    suspend fun getAudioTracksForProject(projectId: String): List<AudioTrackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioTrack(track: AudioTrackEntity)

    @Update
    suspend fun updateAudioTrack(track: AudioTrackEntity)

    @Delete
    suspend fun deleteAudioTrack(track: AudioTrackEntity)

    @Query("DELETE FROM audio_tracks WHERE id = :id")
    suspend fun deleteAudioTrackById(id: String)
}
