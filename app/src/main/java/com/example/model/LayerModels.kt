package com.example.model

enum class LayerAnimationType(val label: String) {
    NONE("None"),
    FADE_IN("Fade In"),
    SLIDE_LEFT("Slide In Left"),
    SLIDE_UP("Slide In Up"),
    ZOOM_IN("Zoom In"),
    BOUNCE("Bounce"),
    SPIN("Spin In"),
    PULSE("Pulse")
}

data class Keyframe(
    val timeMs: Long,
    val posX: Float = 0.5f,       // Normalized 0.0 .. 1.0 (screen center is 0.5)
    val posY: Float = 0.5f,
    val scale: Float = 1.0f,
    val rotationDeg: Float = 0f,
    val opacity: Float = 1.0f
)

data class TextLayer(
    val id: String = java.util.UUID.randomUUID().toString(),
    val projectId: String,
    val text: String = "Your Text Here",
    val startMs: Long = 0L,
    val endMs: Long = 5000L,
    val posX: Float = 0.5f,
    val posY: Float = 0.5f,
    val scale: Float = 1.0f,
    val rotationDeg: Float = 0f,
    val opacity: Float = 1.0f,
    val fontFamily: String = "SansSerif",
    val fontSizeSp: Float = 24f,
    val isBold: Boolean = true,
    val isItalic: Boolean = false,
    val colorHex: String = "#FFFFFF",
    val strokeColorHex: String = "#000000",
    val strokeWidth: Float = 2f,
    val hasStroke: Boolean = false,
    val shadowColorHex: String = "#000000",
    val shadowRadius: Float = 4f,
    val backgroundColorHex: String = "#000000",
    val backgroundOpacity: Float = 0f,
    val animation: LayerAnimationType = LayerAnimationType.NONE,
    val keyframes: List<Keyframe> = emptyList(),
    val attachedTrackingObjectId: String? = null
)

data class StickerLayer(
    val id: String = java.util.UUID.randomUUID().toString(),
    val projectId: String,
    val stickerType: String = "FIRE", // FIRE, SPARKLE, ARROW, HEART, EMOJI, BADGE, LOGO
    val emojiOrText: String = "🔥",
    val customImageUri: String? = null,
    val startMs: Long = 0L,
    val endMs: Long = 5000L,
    val posX: Float = 0.5f,
    val posY: Float = 0.35f,
    val scale: Float = 1.0f,
    val rotationDeg: Float = 0f,
    val opacity: Float = 1.0f,
    val keyframes: List<Keyframe> = emptyList(),
    val attachedTrackingObjectId: String? = null
)
