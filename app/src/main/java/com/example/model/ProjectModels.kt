package com.example.model

enum class AspectRatio(val label: String, val ratioW: Float, val ratioH: Float) {
    RATIO_9_16("9:16 (Shorts/Reels/TikTok)", 9f, 16f),
    RATIO_16_9("16:9 (Landscape YouTube)", 16f, 9f),
    RATIO_1_1("1:1 (Square Post)", 1f, 1f),
    RATIO_4_5("4:5 (Portrait Feed)", 4f, 5f),
    RATIO_ORIGINAL("Original Video", 0f, 0f);

    val ratio: Float
        get() = if (ratioH > 0f) ratioW / ratioH else 16f / 9f
}

enum class ExportResolution(val label: String, val width: Int, val height: Int) {
    RES_720P("720p HD", 720, 1280),
    RES_1080P("1080p Full HD", 1080, 1920),
    RES_4K("4K Ultra HD", 2160, 3840);

    fun getDimensionsForAspectRatio(aspectRatio: AspectRatio): Pair<Int, Int> {
        return when (aspectRatio) {
            AspectRatio.RATIO_9_16 -> width to height
            AspectRatio.RATIO_16_9 -> height to width
            AspectRatio.RATIO_1_1 -> minOf(width, height) to minOf(width, height)
            AspectRatio.RATIO_4_5 -> width to (width * 5 / 4)
            AspectRatio.RATIO_ORIGINAL -> width to height
        }
    }
}

enum class ExportFps(val fps: Int, val label: String) {
    FPS_24(24, "24 FPS (Cinematic)"),
    FPS_30(30, "30 FPS (Standard)"),
    FPS_60(60, "60 FPS (Smooth)");
}

enum class VideoCodec(val label: String, val mimeType: String) {
    H264("H.264 (AVC) - Universal", "video/avc"),
    H265("H.265 (HEVC) - Compact", "video/hevc");
}

enum class ExportQuality(val label: String, val bitrateMbps: Float) {
    ECONOMY("Economy (Fast)", 4f),
    BALANCED("Balanced (Recommended)", 8f),
    HIGH_QUALITY("High Quality (Crisp)", 16f),
    ULTRA("Ultra (Maximum Detail)", 25f);
}

data class ExportConfig(
    val resolution: ExportResolution = ExportResolution.RES_1080P,
    val fps: ExportFps = ExportFps.FPS_30,
    val codec: VideoCodec = VideoCodec.H264,
    val quality: ExportQuality = ExportQuality.BALANCED,
    val targetAspectRatio: AspectRatio = AspectRatio.RATIO_9_16,
    val includeWatermark: Boolean = false,
    val outputPath: String = ""
)

data class VideoInfo(
    val uri: String,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val fps: Float = 30f,
    val rotation: Int = 0,
    val fileSizeFormatted: String = ""
)
