package com.example.model

enum class TrackedAttachmentType(val label: String) {
    TEXT("Text Layer"),
    STICKER("Sticker"),
    BLUR_CENSOR("Blur Box (Censor)"),
    SPOTLIGHT("Spotlight Highlight"),
    ARROW_POINTER("Tracking Arrow")
}

data class BoundingBoxKeyframe(
    val timeMs: Long,
    val normLeft: Float,   // 0.0 to 1.0 normalized coordinates
    val normTop: Float,
    val normRight: Float,
    val normBottom: Float,
    val confidence: Float = 0.95f
) {
    val centerX: Float get() = (normLeft + normRight) / 2f
    val centerY: Float get() = (normTop + normBottom) / 2f
    val width: Float get() = (normRight - normLeft).coerceAtLeast(0.01f)
    val height: Float get() = (normBottom - normTop).coerceAtLeast(0.01f)
}

data class TrackingObject(
    val id: String = java.util.UUID.randomUUID().toString(),
    val projectId: String,
    val label: String = "Tracked Object",
    val isFace: Boolean = false,
    val startMs: Long = 0L,
    val endMs: Long = 5000L,
    val boundingBoxes: List<BoundingBoxKeyframe> = emptyList(),
    val sensitivity: Float = 0.7f,
    val smoothing: Float = 0.3f,
    val offsetX: Float = 0f,
    val offsetY: Float = -0.05f, // Position text slightly above tracked object
    val attachmentType: TrackedAttachmentType = TrackedAttachmentType.TEXT,
    val attachedText: String = "Target 🎯",
    val attachedStickerEmoji: String = "🔥"
) {
    fun getInterpolatedBox(timeMs: Long): BoundingBoxKeyframe? {
        if (boundingBoxes.isEmpty()) return null
        if (timeMs <= boundingBoxes.first().timeMs) return boundingBoxes.first()
        if (timeMs >= boundingBoxes.last().timeMs) return boundingBoxes.last()

        val index = boundingBoxes.indexOfFirst { it.timeMs >= timeMs }
        if (index == 0) return boundingBoxes[0]
        if (index == -1) return boundingBoxes.last()

        val prev = boundingBoxes[index - 1]
        val next = boundingBoxes[index]
        val fraction = ((timeMs - prev.timeMs).toFloat() / (next.timeMs - prev.timeMs).toFloat())
            .coerceIn(0f, 1f)

        return BoundingBoxKeyframe(
            timeMs = timeMs,
            normLeft = prev.normLeft + (next.normLeft - prev.normLeft) * fraction,
            normTop = prev.normTop + (next.normTop - prev.normTop) * fraction,
            normRight = prev.normRight + (next.normRight - prev.normRight) * fraction,
            normBottom = prev.normBottom + (next.normBottom - prev.normBottom) * fraction,
            confidence = (prev.confidence + next.confidence) / 2f
        )
    }
}
