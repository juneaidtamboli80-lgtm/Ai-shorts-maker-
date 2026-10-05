package com.example.engine

import com.example.model.Keyframe
import kotlin.math.sin

object KeyframeInterpolator {

    data class InterpolatedTransform(
        val posX: Float,
        val posY: Float,
        val scale: Float,
        val rotationDeg: Float,
        val opacity: Float
    )

    fun interpolate(
        keyframes: List<Keyframe>,
        timeMs: Long,
        defaultPosX: Float = 0.5f,
        defaultPosY: Float = 0.5f,
        defaultScale: Float = 1.0f,
        defaultRotation: Float = 0f,
        defaultOpacity: Float = 1.0f
    ): InterpolatedTransform {
        if (keyframes.isEmpty()) {
            return InterpolatedTransform(defaultPosX, defaultPosY, defaultScale, defaultRotation, defaultOpacity)
        }
        if (keyframes.size == 1) {
            val k = keyframes[0]
            return InterpolatedTransform(k.posX, k.posY, k.scale, k.rotationDeg, k.opacity)
        }

        val sorted = keyframes.sortedBy { it.timeMs }
        if (timeMs <= sorted.first().timeMs) {
            val first = sorted.first()
            return InterpolatedTransform(first.posX, first.posY, first.scale, first.rotationDeg, first.opacity)
        }
        if (timeMs >= sorted.last().timeMs) {
            val last = sorted.last()
            return InterpolatedTransform(last.posX, last.posY, last.scale, last.rotationDeg, last.opacity)
        }

        val nextIndex = sorted.indexOfFirst { it.timeMs >= timeMs }
        val prev = sorted[nextIndex - 1]
        val next = sorted[nextIndex]

        val rawProgress = (timeMs - prev.timeMs).toFloat() / (next.timeMs - prev.timeMs).toFloat()
        // Smooth ease in-out interpolation
        val easedProgress = smoothStep(rawProgress.coerceIn(0f, 1f))

        return InterpolatedTransform(
            posX = prev.posX + (next.posX - prev.posX) * easedProgress,
            posY = prev.posY + (next.posY - prev.posY) * easedProgress,
            scale = prev.scale + (next.scale - prev.scale) * easedProgress,
            rotationDeg = prev.rotationDeg + (next.rotationDeg - prev.rotationDeg) * easedProgress,
            opacity = prev.opacity + (next.opacity - prev.opacity) * easedProgress
        )
    }

    private fun smoothStep(t: Float): Float {
        return t * t * (3f - 2f * t)
    }
}
