package com.example.model

enum class ShortStylePreset(val label: String, val subtitleStyle: SubtitleStyle) {
    VIRAL_TIKTOK("Viral TikTok", SubtitleStyle(
        textColorHex = "#FFFFFF",
        activeWordColorHex = "#FACC15",
        backgroundColorHex = "#000000",
        backgroundOpacity = 0.7f,
        animation = CaptionAnimation.SCALE_ACTIVE
    )),
    YOUTUBE_SHORTS("Clean Shorts", SubtitleStyle(
        textColorHex = "#38BDF8",
        activeWordColorHex = "#FFFFFF",
        backgroundColorHex = "#0F172A",
        backgroundOpacity = 0.85f,
        animation = CaptionAnimation.POP
    )),
    PODCAST_CLIPS("Podcast Highlight", SubtitleStyle(
        textColorHex = "#FFFFFF",
        activeWordColorHex = "#EC4899",
        backgroundColorHex = "#18181B",
        backgroundOpacity = 0.6f,
        animation = CaptionAnimation.KARAOKE_HIGHLIGHT
    )),
    MINIMAL_LUXURY("Minimalist", SubtitleStyle(
        textColorHex = "#F3F4F6",
        activeWordColorHex = "#A78BFA",
        hasBackground = false,
        hasStroke = true,
        animation = CaptionAnimation.FADE
    ))
}

data class SpeechInterval(
    val startMs: Long,
    val endMs: Long,
    val confidence: Float = 0.9f,
    val summary: String = ""
)

data class SilenceInterval(
    val startMs: Long,
    val endMs: Long,
    val durationMs: Long = endMs - startMs
)

data class FaceDetectionEvent(
    val timeMs: Long,
    val normCenterX: Float,
    val normCenterY: Float,
    val width: Float,
    val height: Float,
    val isPrimarySpeaker: Boolean = true
)

data class ShortClipSuggestion(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val hookSummary: String,
    val startMs: Long,
    val endMs: Long,
    val scoreOutOf100: Int,
    val viralityFactors: List<String> = emptyList(),
    val suggestedAspectRatio: AspectRatio = AspectRatio.RATIO_9_16,
    val suggestedStyle: ShortStylePreset = ShortStylePreset.VIRAL_TIKTOK
) {
    val durationSec: Int get() = ((endMs - startMs) / 1000).toInt()
}

data class AiAnalysisResult(
    val spokenLanguage: String = "English (auto-detected)",
    val speechIntervals: List<SpeechInterval> = emptyList(),
    val silenceIntervals: List<SilenceInterval> = emptyList(),
    val faceDetections: List<FaceDetectionEvent> = emptyList(),
    val suggestedShorts: List<ShortClipSuggestion> = emptyList(),
    val transcriptSummary: String = ""
)
