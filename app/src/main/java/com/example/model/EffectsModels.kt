package com.example.model

enum class TransitionType(val label: String) {
    NONE("None"),
    FADE_BLACK("Fade to Black"),
    DISSOLVE("Cross Dissolve"),
    SLIDE_LEFT("Slide Left"),
    ZOOM_IN("Zoom In"),
    WIPE_RIGHT("Wipe Right")
}

data class VideoEffects(
    val brightness: Float = 0f,       // -1.0 to 1.0 (default 0)
    val contrast: Float = 1.0f,       // 0.0 to 2.0 (default 1)
    val saturation: Float = 1.0f,     // 0.0 to 2.0 (default 1)
    val exposure: Float = 0f,         // -1.0 to 1.0 (default 0)
    val temperature: Float = 0f,      // -1.0 (cool blue) to 1.0 (warm orange)
    val blurRadius: Float = 0f,       // 0.0 to 25.0
    val sharpen: Float = 0f,          // 0.0 to 1.0
    val vignette: Float = 0f,         // 0.0 to 1.0
    val isGrayscale: Boolean = false,
    val speedMultiplier: Float = 1.0f // 0.25x to 4.0x
)

enum class FilterPreset(val label: String, val effects: VideoEffects) {
    ORIGINAL("Original", VideoEffects()),
    CINEMATIC("Cinematic", VideoEffects(contrast = 1.15f, saturation = 0.9f, temperature = 0.15f, vignette = 0.35f)),
    VIBRANT("Vibrant", VideoEffects(brightness = 0.05f, contrast = 1.2f, saturation = 1.35f, temperature = 0.05f)),
    WARM_SUNSET("Sunset Warm", VideoEffects(brightness = 0.05f, contrast = 1.1f, saturation = 1.1f, temperature = 0.45f, vignette = 0.2f)),
    COOL_CYAN("Cool Modern", VideoEffects(contrast = 1.1f, saturation = 1.05f, temperature = -0.35f)),
    VINTAGE_FILM("Vintage 90s", VideoEffects(brightness = 0.08f, contrast = 0.95f, saturation = 0.8f, temperature = 0.25f, vignette = 0.45f)),
    NOIR_MONOCHROME("Film Noir", VideoEffects(contrast = 1.35f, isGrayscale = true, vignette = 0.5f)),
    DRAMATIC("Dramatic", VideoEffects(contrast = 1.3f, saturation = 0.85f, exposure = -0.1f, vignette = 0.4f))
}
