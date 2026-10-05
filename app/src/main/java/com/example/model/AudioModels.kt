package com.example.model

enum class MusicPresetCategory(val label: String) {
    BEATS("Upbeat & Trendy"),
    CINEMATIC("Cinematic & Dramatic"),
    LOFI("Chill Lo-Fi"),
    AMBIENT("Calm Ambient")
}

data class BuiltInMusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val category: MusicPresetCategory,
    val bpm: Int = 120
)

data class AudioTrack(
    val id: String = java.util.UUID.randomUUID().toString(),
    val projectId: String,
    val title: String = "Background Music",
    val uri: String? = null,
    val startTimelineMs: Long = 0L,
    val clipOffsetMs: Long = 0L,
    val durationMs: Long = 10000L,
    val volume: Float = 0.8f,        // 0.0 to 2.0
    val fadeInMs: Long = 500L,
    val fadeOutMs: Long = 1000L,
    val isMuted: Boolean = false,
    val isOriginalAudio: Boolean = false,
    val isVoiceOver: Boolean = false
)
