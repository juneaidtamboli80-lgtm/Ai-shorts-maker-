package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.Converters
import com.example.data.database.ProjectEntity
import com.example.data.repository.ProjectRepository
import com.example.engine.*
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProjectRepository(application)
    val playerEngine = VideoPlayerEngine(application)
    private val trackingEngine = TrackingEngine(application)
    private val aiService: AiVideoService = GeminiAiVideoService(application)
    private val exportEngine = VideoExportEngine(application)
    private val undoRedoManager = UndoRedoManager<EditorSnapshotState>()

    data class EditorSnapshotState(
        val project: ProjectEntity,
        val captions: List<CaptionSegment>,
        val textLayers: List<TextLayer>,
        val stickerLayers: List<StickerLayer>,
        val trackingObjects: List<TrackingObject>,
        val effects: VideoEffects
    )

    private val _project = MutableStateFlow<ProjectEntity?>(null)
    val project: StateFlow<ProjectEntity?> = _project.asStateFlow()

    private val _captions = MutableStateFlow<List<CaptionSegment>>(emptyList())
    val captions: StateFlow<List<CaptionSegment>> = _captions.asStateFlow()

    private val _textLayers = MutableStateFlow<List<TextLayer>>(emptyList())
    val textLayers: StateFlow<List<TextLayer>> = _textLayers.asStateFlow()

    private val _stickerLayers = MutableStateFlow<List<StickerLayer>>(emptyList())
    val stickerLayers: StateFlow<List<StickerLayer>> = _stickerLayers.asStateFlow()

    private val _trackingObjects = MutableStateFlow<List<TrackingObject>>(emptyList())
    val trackingObjects: StateFlow<List<TrackingObject>> = _trackingObjects.asStateFlow()

    private val _audioTracks = MutableStateFlow<List<AudioTrack>>(emptyList())
    val audioTracks: StateFlow<List<AudioTrack>> = _audioTracks.asStateFlow()

    private val _effects = MutableStateFlow(VideoEffects())
    val effects: StateFlow<VideoEffects> = _effects.asStateFlow()

    // Interactive states
    private val _isTrackingSelectionActive = MutableStateFlow(false)
    val isTrackingSelectionActive: StateFlow<Boolean> = _isTrackingSelectionActive.asStateFlow()

    private val _isProcessingTracking = MutableStateFlow(false)
    val isProcessingTracking: StateFlow<Boolean> = _isProcessingTracking.asStateFlow()

    private val _trackingProgress = MutableStateFlow(0f)
    val trackingProgress: StateFlow<Float> = _trackingProgress.asStateFlow()

    private val _isGeneratingAiCaptions = MutableStateFlow(false)
    val isGeneratingAiCaptions: StateFlow<Boolean> = _isGeneratingAiCaptions.asStateFlow()

    private val _aiCaptionsProgress = MutableStateFlow(0f)
    val aiCaptionsProgress: StateFlow<Float> = _aiCaptionsProgress.asStateFlow()

    private val _isGeneratingShorts = MutableStateFlow(false)
    val isGeneratingShorts: StateFlow<Boolean> = _isGeneratingShorts.asStateFlow()

    private val _shortsProgress = MutableStateFlow(0f)
    val shortsProgress: StateFlow<Float> = _shortsProgress.asStateFlow()

    private val _suggestedShorts = MutableStateFlow<List<ShortClipSuggestion>>(emptyList())
    val suggestedShorts: StateFlow<List<ShortClipSuggestion>> = _suggestedShorts.asStateFlow()

    // Export states
    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _exportProgress = MutableStateFlow(0f)
    val exportProgress: StateFlow<Float> = _exportProgress.asStateFlow()

    private val _exportStatusMessage = MutableStateFlow("")
    val exportStatusMessage: StateFlow<String> = _exportStatusMessage.asStateFlow()

    private val _exportedFile = MutableStateFlow<File?>(null)
    val exportedFile: StateFlow<File?> = _exportedFile.asStateFlow()

    val canUndo: StateFlow<Boolean> = undoRedoManager.canUndo
    val canRedo: StateFlow<Boolean> = undoRedoManager.canRedo

    fun loadProject(projectId: String) {
        viewModelScope.launch {
            val proj = repository.getProjectById(projectId) ?: return@launch
            _project.value = proj
            _effects.value = Converters.deserializeEffects(proj.effectsJson)

            // Setup player
            playerEngine.initializePlayer(Uri.parse(proj.videoUri))
            playerEngine.setTrimBounds(proj.trimStartMs, proj.trimEndMs)
            playerEngine.setSpeed(proj.speed)
            playerEngine.setVolume(proj.volume, proj.isMuted)

            // Collect database streams
            launch {
                repository.getCaptionsFlow(projectId).collect { _captions.value = it }
            }
            launch {
                repository.getTextLayersFlow(projectId).collect { _textLayers.value = it }
            }
            launch {
                repository.getStickerLayersFlow(projectId).collect { _stickerLayers.value = it }
            }
            launch {
                repository.getTrackingObjectsFlow(projectId).collect { _trackingObjects.value = it }
            }
            launch {
                repository.getAudioTracksFlow(projectId).collect { _audioTracks.value = it }
            }
        }
    }

    private fun captureSnapshot() {
        val p = _project.value ?: return
        val snapshot = EditorSnapshotState(
            project = p,
            captions = _captions.value,
            textLayers = _textLayers.value,
            stickerLayers = _stickerLayers.value,
            trackingObjects = _trackingObjects.value,
            effects = _effects.value
        )
        undoRedoManager.pushState(snapshot)
    }

    fun undo() {
        val current = _project.value?.let {
            EditorSnapshotState(it, _captions.value, _textLayers.value, _stickerLayers.value, _trackingObjects.value, _effects.value)
        } ?: return

        val previous = undoRedoManager.undo(current) ?: return
        applySnapshot(previous)
    }

    fun redo() {
        val current = _project.value?.let {
            EditorSnapshotState(it, _captions.value, _textLayers.value, _stickerLayers.value, _trackingObjects.value, _effects.value)
        } ?: return

        val next = undoRedoManager.redo(current) ?: return
        applySnapshot(next)
    }

    private fun applySnapshot(snapshot: EditorSnapshotState) {
        viewModelScope.launch {
            _project.value = snapshot.project
            _captions.value = snapshot.captions
            _textLayers.value = snapshot.textLayers
            _stickerLayers.value = snapshot.stickerLayers
            _trackingObjects.value = snapshot.trackingObjects
            _effects.value = snapshot.effects

            repository.updateProject(snapshot.project)
            repository.replaceCaptions(snapshot.project.id, snapshot.captions)
        }
    }

    // Trim, Speed, Aspect Ratio
    fun updateTrim(startMs: Long, endMs: Long) {
        captureSnapshot()
        val current = _project.value ?: return
        val updated = current.copy(trimStartMs = startMs, trimEndMs = endMs)
        _project.value = updated
        playerEngine.setTrimBounds(startMs, endMs)
        viewModelScope.launch { repository.updateProject(updated) }
    }

    fun updateAspectRatio(ratio: AspectRatio) {
        captureSnapshot()
        val current = _project.value ?: return
        val updated = current.copy(aspectRatio = ratio)
        _project.value = updated
        viewModelScope.launch { repository.updateProject(updated) }
    }

    fun updateSpeed(speed: Float) {
        captureSnapshot()
        val current = _project.value ?: return
        val updated = current.copy(speed = speed)
        _project.value = updated
        playerEngine.setSpeed(speed)
        viewModelScope.launch { repository.updateProject(updated) }
    }

    fun splitAtPlayhead() {
        val current = _project.value ?: return
        val pos = playerEngine.currentPositionMs.value
        if (pos > current.trimStartMs && pos < current.trimEndMs) {
            updateTrim(current.trimStartMs, pos)
        }
    }

    // Captions
    fun generateAiCaptions(language: String = "English") {
        val current = _project.value ?: return
        viewModelScope.launch {
            captureSnapshot()
            _isGeneratingAiCaptions.value = true
            _aiCaptionsProgress.value = 0f
            try {
                val newCaptions = aiService.generateCaptions(
                    projectId = current.id,
                    videoDurationMs = current.durationMs,
                    language = language,
                    onProgress = { _aiCaptionsProgress.value = it }
                )
                repository.replaceCaptions(current.id, newCaptions)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isGeneratingAiCaptions.value = false
            }
        }
    }

    fun addCaption(startMs: Long, endMs: Long, text: String) {
        val current = _project.value ?: return
        captureSnapshot()
        viewModelScope.launch {
            val caption = CaptionSegment(
                projectId = current.id,
                startMs = startMs,
                endMs = endMs,
                text = text,
                style = _captions.value.firstOrNull()?.style ?: SubtitleStyle()
            )
            repository.saveCaption(caption)
        }
    }

    fun updateCaptionText(caption: CaptionSegment, newText: String) {
        viewModelScope.launch {
            val words = newText.split(" ").mapIndexed { idx, w ->
                val span = (caption.endMs - caption.startMs) / maxOf(1, newText.split(" ").size)
                WordTiming(w, caption.startMs + idx * span, caption.startMs + (idx + 1) * span)
            }
            repository.saveCaption(caption.copy(text = newText, words = words))
        }
    }

    fun updateCaptionTiming(caption: CaptionSegment, newStart: Long, newEnd: Long) {
        viewModelScope.launch {
            repository.saveCaption(caption.copy(startMs = newStart, endMs = newEnd))
        }
    }

    fun deleteCaption(captionId: String) {
        captureSnapshot()
        viewModelScope.launch {
            repository.deleteCaption(captionId)
        }
    }

    fun splitCaption(caption: CaptionSegment, splitTimeMs: Long) {
        if (splitTimeMs <= caption.startMs || splitTimeMs >= caption.endMs) return
        captureSnapshot()
        viewModelScope.launch {
            val words = caption.text.split(" ")
            val mid = words.size / 2
            val text1 = words.take(mid.coerceAtLeast(1)).joinToString(" ")
            val text2 = words.drop(mid.coerceAtLeast(1)).joinToString(" ")

            val cap1 = caption.copy(endMs = splitTimeMs, text = text1)
            val cap2 = CaptionSegment(
                projectId = caption.projectId,
                startMs = splitTimeMs,
                endMs = caption.endMs,
                text = text2,
                style = caption.style
            )
            repository.saveCaption(cap1)
            repository.saveCaption(cap2)
        }
    }

    fun mergeCaptions(first: CaptionSegment, second: CaptionSegment) {
        captureSnapshot()
        viewModelScope.launch {
            val merged = first.copy(
                endMs = second.endMs,
                text = "${first.text} ${second.text}"
            )
            repository.saveCaption(merged)
            repository.deleteCaption(second.id)
        }
    }

    fun updateGlobalSubtitleStyle(style: SubtitleStyle) {
        viewModelScope.launch {
            _captions.value.forEach { cap ->
                repository.saveCaption(cap.copy(style = style))
            }
        }
    }

    // Text Layers
    fun addTextLayer(layer: TextLayer) {
        val current = _project.value ?: return
        captureSnapshot()
        viewModelScope.launch {
            repository.saveTextLayer(layer.copy(projectId = current.id))
        }
    }

    fun updateTextLayer(layer: TextLayer) {
        viewModelScope.launch {
            repository.saveTextLayer(layer)
        }
    }

    fun deleteTextLayer(id: String) {
        captureSnapshot()
        viewModelScope.launch {
            repository.deleteTextLayer(id)
        }
    }

    // Sticker Layers
    fun addStickerLayer(layer: StickerLayer) {
        val current = _project.value ?: return
        captureSnapshot()
        viewModelScope.launch {
            repository.saveStickerLayer(layer.copy(projectId = current.id))
        }
    }

    fun updateStickerLayer(layer: StickerLayer) {
        viewModelScope.launch {
            repository.saveStickerLayer(layer)
        }
    }

    fun deleteStickerLayer(id: String) {
        captureSnapshot()
        viewModelScope.launch {
            repository.deleteStickerLayer(id)
        }
    }

    // Tracking
    fun setTrackingSelectionMode(active: Boolean) {
        _isTrackingSelectionActive.value = active
    }

    fun startTrackingSelectedBox(normLeft: Float, normTop: Float, normRight: Float, normBottom: Float) {
        val current = _project.value ?: return
        _isTrackingSelectionActive.value = false
        _isProcessingTracking.value = true
        _trackingProgress.value = 0f

        viewModelScope.launch {
            captureSnapshot()
            val initialBox = BoundingBoxKeyframe(
                timeMs = playerEngine.currentPositionMs.value,
                normLeft = normLeft,
                normTop = normTop,
                normRight = normRight,
                normBottom = normBottom
            )
            val keyframes = trackingEngine.trackObject(
                videoUri = Uri.parse(current.videoUri),
                initialBox = initialBox,
                startMs = playerEngine.currentPositionMs.value,
                endMs = current.durationMs,
                onProgress = { _trackingProgress.value = it }
            )

            val track = TrackingObject(
                projectId = current.id,
                label = "Tracked Subject",
                startMs = playerEngine.currentPositionMs.value,
                endMs = current.durationMs,
                boundingBoxes = keyframes
            )
            repository.saveTrackingObject(track)
            _isProcessingTracking.value = false
        }
    }

    fun startFaceTracking() {
        val current = _project.value ?: return
        _isProcessingTracking.value = true
        _trackingProgress.value = 0f

        viewModelScope.launch {
            captureSnapshot()
            val faceBox = BoundingBoxKeyframe(
                timeMs = 0L,
                normLeft = 0.35f,
                normTop = 0.25f,
                normRight = 0.65f,
                normBottom = 0.55f
            )
            val keyframes = trackingEngine.trackObject(
                videoUri = Uri.parse(current.videoUri),
                initialBox = faceBox,
                startMs = 0L,
                endMs = current.durationMs,
                isFace = true,
                onProgress = { _trackingProgress.value = it }
            )

            val track = TrackingObject(
                projectId = current.id,
                label = "Speaker Face",
                isFace = true,
                startMs = 0L,
                endMs = current.durationMs,
                boundingBoxes = keyframes,
                attachedText = "Speaker 🎙️"
            )
            repository.saveTrackingObject(track)
            _isProcessingTracking.value = false
        }
    }

    fun updateTrackingObject(track: TrackingObject) {
        viewModelScope.launch {
            repository.saveTrackingObject(track)
        }
    }

    fun deleteTrackingObject(id: String) {
        captureSnapshot()
        viewModelScope.launch {
            repository.deleteTrackingObject(id)
        }
    }

    fun recalculateTracking(track: TrackingObject) {
        val current = _project.value ?: return
        _isProcessingTracking.value = true
        _trackingProgress.value = 0f

        viewModelScope.launch {
            val initial = track.boundingBoxes.firstOrNull() ?: BoundingBoxKeyframe(track.startMs, 0.3f, 0.3f, 0.7f, 0.7f)
            val updatedKeyframes = trackingEngine.trackObject(
                videoUri = Uri.parse(current.videoUri),
                initialBox = initial,
                startMs = track.startMs,
                endMs = track.endMs,
                sensitivity = track.sensitivity,
                smoothing = track.smoothing,
                onProgress = { _trackingProgress.value = it }
            )
            repository.saveTrackingObject(track.copy(boundingBoxes = updatedKeyframes))
            _isProcessingTracking.value = false
        }
    }

    // Effects
    fun updateEffects(effects: VideoEffects) {
        _effects.value = effects
        val current = _project.value ?: return
        val updated = current.copy(effectsJson = Converters.serializeEffects(effects))
        _project.value = updated
        viewModelScope.launch { repository.updateProject(updated) }
    }

    // Audio
    fun updateOriginalAudio(volume: Float, isMuted: Boolean) {
        val current = _project.value ?: return
        val updated = current.copy(volume = volume, isMuted = isMuted)
        _project.value = updated
        playerEngine.setVolume(volume, isMuted)
        viewModelScope.launch { repository.updateProject(updated) }
    }

    fun addAudioTrack(track: AudioTrack) {
        val current = _project.value ?: return
        captureSnapshot()
        viewModelScope.launch {
            repository.saveAudioTrack(track.copy(projectId = current.id))
        }
    }

    fun deleteAudioTrack(id: String) {
        captureSnapshot()
        viewModelScope.launch {
            repository.deleteAudioTrack(id)
        }
    }

    // Auto Shorts
    fun generateShorts(targetDurationSec: Int, style: ShortStylePreset) {
        val current = _project.value ?: return
        _isGeneratingShorts.value = true
        _shortsProgress.value = 0f

        viewModelScope.launch {
            val suggestions = aiService.generateAutoShorts(
                videoDurationMs = current.durationMs,
                targetDurationSec = targetDurationSec,
                aspectRatio = AspectRatio.RATIO_9_16,
                stylePreset = style,
                onProgress = { _shortsProgress.value = it }
            )
            _suggestedShorts.value = suggestions
            _isGeneratingShorts.value = false
        }
    }

    fun applyShortClip(short: ShortClipSuggestion) {
        updateTrim(short.startMs, short.endMs)
        updateAspectRatio(short.suggestedAspectRatio)
        updateGlobalSubtitleStyle(short.suggestedStyle.subtitleStyle)
        playerEngine.seekTo(short.startMs)
    }

    // Export Video
    fun startExport(config: ExportConfig) {
        val current = _project.value ?: return
        _isExporting.value = true
        _exportProgress.value = 0f
        _exportStatusMessage.value = "Starting render..."
        _exportedFile.value = null

        viewModelScope.launch {
            try {
                val file = exportEngine.exportVideo(
                    project = current,
                    captions = _captions.value,
                    textLayers = _textLayers.value,
                    stickerLayers = _stickerLayers.value,
                    trackingObjects = _trackingObjects.value,
                    effects = _effects.value,
                    config = config,
                    onProgress = { p, msg ->
                        _exportProgress.value = p
                        _exportStatusMessage.value = msg
                    }
                )
                _exportedFile.value = file
            } catch (e: Exception) {
                _exportStatusMessage.value = "Export error: ${e.message}"
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun cancelExport() {
        exportEngine.cancelExport()
        _isExporting.value = false
    }

    override fun onCleared() {
        super.onCleared()
        playerEngine.release()
    }
}
