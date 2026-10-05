package com.example.data.database

import androidx.room.TypeConverter
import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject

class Converters {

    @TypeConverter
    fun fromAspectRatio(aspectRatio: AspectRatio): String = aspectRatio.name

    @TypeConverter
    fun toAspectRatio(value: String): AspectRatio = try {
        AspectRatio.valueOf(value)
    } catch (e: Exception) {
        AspectRatio.RATIO_9_16
    }

    @TypeConverter
    fun fromTransitionType(transitionType: TransitionType): String = transitionType.name

    @TypeConverter
    fun toTransitionType(value: String): TransitionType = try {
        TransitionType.valueOf(value)
    } catch (e: Exception) {
        TransitionType.NONE
    }

    @TypeConverter
    fun fromLayerAnimationType(anim: LayerAnimationType): String = anim.name

    @TypeConverter
    fun toLayerAnimationType(value: String): LayerAnimationType = try {
        LayerAnimationType.valueOf(value)
    } catch (e: Exception) {
        LayerAnimationType.NONE
    }

    @TypeConverter
    fun fromTrackedAttachmentType(type: TrackedAttachmentType): String = type.name

    @TypeConverter
    fun toTrackedAttachmentType(value: String): TrackedAttachmentType = try {
        TrackedAttachmentType.valueOf(value)
    } catch (e: Exception) {
        TrackedAttachmentType.TEXT
    }

    companion object {
        fun serializeWords(words: List<WordTiming>): String {
            val array = JSONArray()
            words.forEach { w ->
                val obj = JSONObject()
                obj.put("word", w.word)
                obj.put("startMs", w.startMs)
                obj.put("endMs", w.endMs)
                array.put(obj)
            }
            return array.toString()
        }

        fun deserializeWords(json: String?): List<WordTiming> {
            if (json.isNullOrEmpty()) return emptyList()
            val list = mutableListOf<WordTiming>()
            try {
                val array = JSONArray(json)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        WordTiming(
                            word = obj.getString("word"),
                            startMs = obj.getLong("startMs"),
                            endMs = obj.getLong("endMs")
                        )
                    )
                }
            } catch (e: Exception) {
                // Return empty on parse fail
            }
            return list
        }

        fun serializeSubtitleStyle(style: SubtitleStyle): String {
            val obj = JSONObject()
            obj.put("fontFamilyName", style.fontFamilyName)
            obj.put("fontSizeSp", style.fontSizeSp)
            obj.put("isBold", style.isBold)
            obj.put("isItalic", style.isItalic)
            obj.put("isUppercase", style.isUppercase)
            obj.put("alignment", style.alignment.name)
            obj.put("textColorHex", style.textColorHex)
            obj.put("activeWordColorHex", style.activeWordColorHex)
            obj.put("backgroundColorHex", style.backgroundColorHex)
            obj.put("backgroundOpacity", style.backgroundOpacity)
            obj.put("hasBackground", style.hasBackground)
            obj.put("strokeColorHex", style.strokeColorHex)
            obj.put("strokeWidth", style.strokeWidth)
            obj.put("hasStroke", style.hasStroke)
            obj.put("shadowColorHex", style.shadowColorHex)
            obj.put("shadowRadius", style.shadowRadius)
            obj.put("positionYRatio", style.positionYRatio)
            obj.put("letterSpacingSp", style.letterSpacingSp)
            obj.put("lineSpacingMultiplier", style.lineSpacingMultiplier)
            obj.put("maxCharsPerLine", style.maxCharsPerLine)
            obj.put("animation", style.animation.name)
            obj.put("activeWordScale", style.activeWordScale)
            return obj.toString()
        }

        fun deserializeSubtitleStyle(json: String?): SubtitleStyle {
            if (json.isNullOrEmpty()) return SubtitleStyle()
            return try {
                val obj = JSONObject(json)
                SubtitleStyle(
                    fontFamilyName = obj.optString("fontFamilyName", "SansSerif"),
                    fontSizeSp = obj.optDouble("fontSizeSp", 22.0).toFloat(),
                    isBold = obj.optBoolean("isBold", true),
                    isItalic = obj.optBoolean("isItalic", false),
                    isUppercase = obj.optBoolean("isUppercase", false),
                    alignment = try {
                        SubtitleAlignment.valueOf(obj.optString("alignment", "CENTER"))
                    } catch (e: Exception) {
                        SubtitleAlignment.CENTER
                    },
                    textColorHex = obj.optString("textColorHex", "#FFFFFF"),
                    activeWordColorHex = obj.optString("activeWordColorHex", "#FDE047"),
                    backgroundColorHex = obj.optString("backgroundColorHex", "#000000"),
                    backgroundOpacity = obj.optDouble("backgroundOpacity", 0.5).toFloat(),
                    hasBackground = obj.optBoolean("hasBackground", true),
                    strokeColorHex = obj.optString("strokeColorHex", "#000000"),
                    strokeWidth = obj.optDouble("strokeWidth", 3.0).toFloat(),
                    hasStroke = obj.optBoolean("hasStroke", true),
                    shadowColorHex = obj.optString("shadowColorHex", "#000000"),
                    shadowRadius = obj.optDouble("shadowRadius", 4.0).toFloat(),
                    positionYRatio = obj.optDouble("positionYRatio", 0.82).toFloat(),
                    letterSpacingSp = obj.optDouble("letterSpacingSp", 0.5).toFloat(),
                    lineSpacingMultiplier = obj.optDouble("lineSpacingMultiplier", 1.1).toFloat(),
                    maxCharsPerLine = obj.optInt("maxCharsPerLine", 28),
                    animation = try {
                        CaptionAnimation.valueOf(obj.optString("animation", "SCALE_ACTIVE"))
                    } catch (e: Exception) {
                        CaptionAnimation.SCALE_ACTIVE
                    },
                    activeWordScale = obj.optDouble("activeWordScale", 1.25).toFloat()
                )
            } catch (e: Exception) {
                SubtitleStyle()
            }
        }

        fun serializeKeyframes(keyframes: List<Keyframe>): String {
            val array = JSONArray()
            keyframes.forEach { k ->
                val obj = JSONObject()
                obj.put("timeMs", k.timeMs)
                obj.put("posX", k.posX)
                obj.put("posY", k.posY)
                obj.put("scale", k.scale)
                obj.put("rotationDeg", k.rotationDeg)
                obj.put("opacity", k.opacity)
                array.put(obj)
            }
            return array.toString()
        }

        fun deserializeKeyframes(json: String?): List<Keyframe> {
            if (json.isNullOrEmpty()) return emptyList()
            val list = mutableListOf<Keyframe>()
            try {
                val array = JSONArray(json)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        Keyframe(
                            timeMs = obj.getLong("timeMs"),
                            posX = obj.optDouble("posX", 0.5).toFloat(),
                            posY = obj.optDouble("posY", 0.5).toFloat(),
                            scale = obj.optDouble("scale", 1.0).toFloat(),
                            rotationDeg = obj.optDouble("rotationDeg", 0.0).toFloat(),
                            opacity = obj.optDouble("opacity", 1.0).toFloat()
                        )
                    )
                }
            } catch (e: Exception) {
                // Return empty
            }
            return list
        }

        fun serializeBoundingBoxes(boxes: List<BoundingBoxKeyframe>): String {
            val array = JSONArray()
            boxes.forEach { b ->
                val obj = JSONObject()
                obj.put("timeMs", b.timeMs)
                obj.put("normLeft", b.normLeft)
                obj.put("normTop", b.normTop)
                obj.put("normRight", b.normRight)
                obj.put("normBottom", b.normBottom)
                obj.put("confidence", b.confidence)
                array.put(obj)
            }
            return array.toString()
        }

        fun deserializeBoundingBoxes(json: String?): List<BoundingBoxKeyframe> {
            if (json.isNullOrEmpty()) return emptyList()
            val list = mutableListOf<BoundingBoxKeyframe>()
            try {
                val array = JSONArray(json)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        BoundingBoxKeyframe(
                            timeMs = obj.getLong("timeMs"),
                            normLeft = obj.optDouble("normLeft", 0.3).toFloat(),
                            normTop = obj.optDouble("normTop", 0.3).toFloat(),
                            normRight = obj.optDouble("normRight", 0.7).toFloat(),
                            normBottom = obj.optDouble("normBottom", 0.7).toFloat(),
                            confidence = obj.optDouble("confidence", 0.95).toFloat()
                        )
                    )
                }
            } catch (e: Exception) {
                // Return empty
            }
            return list
        }

        fun serializeEffects(effects: VideoEffects): String {
            val obj = JSONObject()
            obj.put("brightness", effects.brightness)
            obj.put("contrast", effects.contrast)
            obj.put("saturation", effects.saturation)
            obj.put("exposure", effects.exposure)
            obj.put("temperature", effects.temperature)
            obj.put("blurRadius", effects.blurRadius)
            obj.put("sharpen", effects.sharpen)
            obj.put("vignette", effects.vignette)
            obj.put("isGrayscale", effects.isGrayscale)
            obj.put("speedMultiplier", effects.speedMultiplier)
            return obj.toString()
        }

        fun deserializeEffects(json: String?): VideoEffects {
            if (json.isNullOrEmpty()) return VideoEffects()
            return try {
                val obj = JSONObject(json)
                VideoEffects(
                    brightness = obj.optDouble("brightness", 0.0).toFloat(),
                    contrast = obj.optDouble("contrast", 1.0).toFloat(),
                    saturation = obj.optDouble("saturation", 1.0).toFloat(),
                    exposure = obj.optDouble("exposure", 0.0).toFloat(),
                    temperature = obj.optDouble("temperature", 0.0).toFloat(),
                    blurRadius = obj.optDouble("blurRadius", 0.0).toFloat(),
                    sharpen = obj.optDouble("sharpen", 0.0).toFloat(),
                    vignette = obj.optDouble("vignette", 0.0).toFloat(),
                    isGrayscale = obj.optBoolean("isGrayscale", false),
                    speedMultiplier = obj.optDouble("speedMultiplier", 1.0).toFloat()
                )
            } catch (e: Exception) {
                VideoEffects()
            }
        }
    }
}
