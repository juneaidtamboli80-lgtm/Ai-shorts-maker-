package com.example.ui.components

import android.graphics.SurfaceTexture
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine.KeyframeInterpolator
import com.example.engine.VideoPlayerEngine
import com.example.model.*
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.AccentSecondary
import com.example.ui.theme.DarkBg
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalTextApi::class)
@Composable
fun VideoPlayerView(
    playerEngine: VideoPlayerEngine,
    videoUri: String,
    aspectRatio: AspectRatio,
    currentTimeMs: Long,
    captions: List<CaptionSegment>,
    textLayers: List<TextLayer>,
    stickerLayers: List<StickerLayer>,
    trackingObjects: List<TrackingObject>,
    effects: VideoEffects,
    isTrackingSelectionMode: Boolean = false,
    onTrackingAreaSelected: (normLeft: Float, normTop: Float, normRight: Float, normBottom: Float) -> Unit = { _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    var dragStartOffset by remember { mutableStateOf<Offset?>(null) }
    var dragCurrentOffset by remember { mutableStateOf<Offset?>(null) }

    BoxWithConstraints(
        modifier = modifier
            .background(DarkBg)
            .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        val containerWidth = maxWidth
        val containerHeight = maxHeight
        val targetRatio = if (aspectRatio.ratioH > 0) aspectRatio.ratio else 9f / 16f

        // Calculate aspect-ratio fitted box inside available constraints
        val containerAspect = containerWidth.value / containerHeight.value
        val (videoWidthDp, videoHeightDp) = if (containerAspect > targetRatio) {
            // Container is wider than target ratio: fit to height
            (containerHeight * targetRatio) to containerHeight
        } else {
            // Container is taller than target ratio: fit to width
            containerWidth to (containerWidth / targetRatio)
        }

        Box(
            modifier = Modifier
                .size(videoWidthDp, videoHeightDp)
                .background(Color.Black)
                .clip(RoundedCornerShape(8.dp))
        ) {
            // 1. Android TextureView for hardware video playback
            AndroidView(
                factory = { ctx ->
                    TextureView(ctx).apply {
                        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                            override fun onSurfaceTextureAvailable(st: SurfaceTexture, w: Int, h: Int) {
                                val surface = Surface(st)
                                playerEngine.setSurface(surface)
                            }
                            override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, w: Int, h: Int) {}
                            override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                                playerEngine.setSurface(null)
                                return true
                            }
                            override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // 2. Video Color Grading Filter Overlay
            if (effects.isGrayscale || effects.brightness != 0f || effects.contrast != 1f || effects.saturation != 1f || effects.vignette > 0f) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (effects.isGrayscale) {
                        drawRect(
                            color = Color(0x60808080),
                            blendMode = BlendMode.Color
                        )
                    }
                    if (effects.brightness > 0f) {
                        drawRect(
                            color = Color.White.copy(alpha = effects.brightness.coerceIn(0f, 0.4f)),
                            blendMode = BlendMode.Screen
                        )
                    } else if (effects.brightness < 0f) {
                        drawRect(
                            color = Color.Black.copy(alpha = (-effects.brightness).coerceIn(0f, 0.6f)),
                            blendMode = BlendMode.Darken
                        )
                    }
                    if (effects.vignette > 0.05f) {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = effects.vignette.coerceIn(0f, 0.85f))),
                                center = center,
                                radius = size.maxDimension * 0.6f
                            )
                        )
                    }
                }
            }

            // 3. Interactive Graphics Layer (Captions, Text Layers, Stickers, Tracking, Safe Guides)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isTrackingSelectionMode) {
                        if (isTrackingSelectionMode) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    dragStartOffset = offset
                                    dragCurrentOffset = offset
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    dragCurrentOffset = change.position
                                },
                                onDragEnd = {
                                    val start = dragStartOffset
                                    val curr = dragCurrentOffset
                                    if (start != null && curr != null && size.width > 0 && size.height > 0) {
                                        val left = (min(start.x, curr.x) / size.width).coerceIn(0f, 1f)
                                        val top = (min(start.y, curr.y) / size.height).coerceIn(0f, 1f)
                                        val right = (max(start.x, curr.x) / size.width).coerceIn(0f, 1f)
                                        val bottom = (max(start.y, curr.y) / size.height).coerceIn(0f, 1f)
                                        if (right - left > 0.05f && bottom - top > 0.05f) {
                                            onTrackingAreaSelected(left, top, right, bottom)
                                        }
                                    }
                                    dragStartOffset = null
                                    dragCurrentOffset = null
                                }
                            )
                        }
                    }
            ) {
                val canvasW = size.width
                val canvasH = size.height

                // Render Tracking Object Bounding Boxes & Attachments
                trackingObjects.forEach { track ->
                    if (currentTimeMs in track.startMs..track.endMs) {
                        val box = track.getInterpolatedBox(currentTimeMs)
                        if (box != null) {
                            val boxRect = Rect(
                                offset = Offset(box.normLeft * canvasW, box.normTop * canvasH),
                                size = Size(box.width * canvasW, box.height * canvasH)
                            )

                            // Draw tracking box bounding frame
                            drawRoundRect(
                                color = if (track.isFace) AccentSecondary else AccentPrimary,
                                topLeft = boxRect.topLeft,
                                size = boxRect.size,
                                cornerRadius = CornerRadius(8f, 8f),
                                style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)))
                            )

                            // Render attached items (Text, Sticker, Blur Censor, Spotlight)
                            val anchorX = (box.centerX + track.offsetX) * canvasW
                            val anchorY = (box.centerY + track.offsetY) * canvasH

                            when (track.attachmentType) {
                                TrackedAttachmentType.TEXT -> {
                                    val measured = textMeasurer.measure(
                                        text = AnnotatedString(track.attachedText),
                                        style = TextStyle(
                                            color = Color.White,
                                            fontSize = (canvasW * 0.045f).sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                    drawRoundRect(
                                        color = Color.Black.copy(alpha = 0.75f),
                                        topLeft = Offset(anchorX - measured.size.width / 2f - 12f, anchorY - measured.size.height / 2f - 6f),
                                        size = Size(measured.size.width + 24f, measured.size.height + 12f),
                                        cornerRadius = CornerRadius(6f, 6f)
                                    )
                                    drawText(
                                        textMeasurer = textMeasurer,
                                        text = track.attachedText,
                                        topLeft = Offset(anchorX - measured.size.width / 2f, anchorY - measured.size.height / 2f),
                                        style = TextStyle(
                                            color = Color.White,
                                            fontSize = (canvasW * 0.045f).sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                TrackedAttachmentType.STICKER -> {
                                    val emojiMeasured = textMeasurer.measure(
                                        text = AnnotatedString(track.attachedStickerEmoji),
                                        style = TextStyle(fontSize = (canvasW * 0.08f).sp)
                                    )
                                    drawText(
                                        textMeasurer = textMeasurer,
                                        text = track.attachedStickerEmoji,
                                        topLeft = Offset(anchorX - emojiMeasured.size.width / 2f, anchorY - emojiMeasured.size.height / 2f),
                                        style = TextStyle(fontSize = (canvasW * 0.08f).sp)
                                    )
                                }
                                TrackedAttachmentType.BLUR_CENSOR -> {
                                    drawRoundRect(
                                        color = Color(0xDD202020),
                                        topLeft = boxRect.topLeft,
                                        size = boxRect.size,
                                        cornerRadius = CornerRadius(8f, 8f)
                                    )
                                }
                                TrackedAttachmentType.ARROW_POINTER -> {
                                    val arrowMeasured = textMeasurer.measure(
                                        text = AnnotatedString("🎯"),
                                        style = TextStyle(fontSize = (canvasW * 0.07f).sp)
                                    )
                                    drawText(
                                        textMeasurer = textMeasurer,
                                        text = "🎯",
                                        topLeft = Offset(anchorX - arrowMeasured.size.width / 2f, anchorY - arrowMeasured.size.height / 2f),
                                        style = TextStyle(fontSize = (canvasW * 0.07f).sp)
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
                }

                // Render Text Layers with Keyframe Interpolation
                textLayers.forEach { layer ->
                    if (currentTimeMs in layer.startMs..layer.endMs) {
                        val transform = KeyframeInterpolator.interpolate(
                            keyframes = layer.keyframes,
                            timeMs = currentTimeMs,
                            defaultPosX = layer.posX,
                            defaultPosY = layer.posY,
                            defaultScale = layer.scale,
                            defaultRotation = layer.rotationDeg,
                            defaultOpacity = layer.opacity
                        )

                        val x = transform.posX * canvasW
                        val y = transform.posY * canvasH
                        val fontSize = (layer.fontSizeSp * (canvasW / 380f) * transform.scale).sp
                        val textColor = try {
                            Color(android.graphics.Color.parseColor(layer.colorHex)).copy(alpha = transform.opacity)
                        } catch (e: Exception) {
                            Color.White.copy(alpha = transform.opacity)
                        }

                        val measured = textMeasurer.measure(
                            text = AnnotatedString(layer.text),
                            style = TextStyle(
                                color = textColor,
                                fontSize = fontSize,
                                fontWeight = if (layer.isBold) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        )

                        rotate(degrees = transform.rotationDeg, pivot = Offset(x, y)) {
                            // Background box
                            if (layer.backgroundOpacity > 0f) {
                                val bgColor = try {
                                    Color(android.graphics.Color.parseColor(layer.backgroundColorHex)).copy(alpha = layer.backgroundOpacity * transform.opacity)
                                } catch (e: Exception) {
                                    Color.Black.copy(alpha = 0.5f)
                                }
                                drawRoundRect(
                                    color = bgColor,
                                    topLeft = Offset(x - measured.size.width / 2f - 16f, y - measured.size.height / 2f - 8f),
                                    size = Size(measured.size.width + 32f, measured.size.height + 16f),
                                    cornerRadius = CornerRadius(8f, 8f)
                                )
                            }

                            drawText(
                                textMeasurer = textMeasurer,
                                text = layer.text,
                                topLeft = Offset(x - measured.size.width / 2f, y - measured.size.height / 2f),
                                style = TextStyle(
                                    color = textColor,
                                    fontSize = fontSize,
                                    fontWeight = if (layer.isBold) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }

                // Render Sticker Layers with Keyframe Interpolation
                stickerLayers.forEach { sticker ->
                    if (currentTimeMs in sticker.startMs..sticker.endMs) {
                        val transform = KeyframeInterpolator.interpolate(
                            keyframes = sticker.keyframes,
                            timeMs = currentTimeMs,
                            defaultPosX = sticker.posX,
                            defaultPosY = sticker.posY,
                            defaultScale = sticker.scale,
                            defaultRotation = sticker.rotationDeg,
                            defaultOpacity = sticker.opacity
                        )

                        val x = transform.posX * canvasW
                        val y = transform.posY * canvasH
                        val stickerFontSize = (canvasW * 0.12f * transform.scale).sp

                        val emojiMeasured = textMeasurer.measure(
                            text = AnnotatedString(sticker.emojiOrText),
                            style = TextStyle(fontSize = stickerFontSize)
                        )

                        rotate(degrees = transform.rotationDeg, pivot = Offset(x, y)) {
                            drawText(
                                textMeasurer = textMeasurer,
                                text = sticker.emojiOrText,
                                topLeft = Offset(x - emojiMeasured.size.width / 2f, y - emojiMeasured.size.height / 2f),
                                style = TextStyle(fontSize = stickerFontSize)
                            )
                        }
                    }
                }

                // Render Subtitles with Word-by-Word Karaoke Highlighting
                val activeCaption = captions.find { it.containsTime(currentTimeMs) }
                if (activeCaption != null) {
                    val style = activeCaption.style
                    val posY = canvasH * style.positionYRatio
                    val baseFontSize = (style.fontSizeSp * (canvasW / 380f)).sp

                    val baseTextColor = try {
                        Color(android.graphics.Color.parseColor(style.textColorHex))
                    } catch (e: Exception) {
                        Color.White
                    }
                    val activeWordColor = try {
                        Color(android.graphics.Color.parseColor(style.activeWordColorHex))
                    } catch (e: Exception) {
                        Color.Yellow
                    }
                    val bgColor = try {
                        Color(android.graphics.Color.parseColor(style.backgroundColorHex)).copy(alpha = style.backgroundOpacity)
                    } catch (e: Exception) {
                        Color.Black.copy(alpha = 0.5f)
                    }

                    if (activeCaption.words.isNotEmpty()) {
                        // Word-by-word annotated string with active word styling
                        val builder = AnnotatedString.Builder()
                        activeCaption.words.forEachIndexed { idx, w ->
                            val isWordActive = currentTimeMs in w.startMs..w.endMs
                            val spanStyle = SpanStyle(
                                color = if (isWordActive) activeWordColor else baseTextColor,
                                fontSize = if (isWordActive) (baseFontSize.value * style.activeWordScale).sp else baseFontSize,
                                fontWeight = if (isWordActive || style.isBold) FontWeight.ExtraBold else FontWeight.Medium
                            )
                            builder.pushStyle(spanStyle)
                            builder.append("${w.word} ")
                            builder.pop()
                        }
                        val annotatedText = builder.toAnnotatedString()
                        val measured = textMeasurer.measure(
                            text = annotatedText,
                            style = TextStyle(textAlign = TextAlign.Center)
                        )

                        if (style.hasBackground) {
                            drawRoundRect(
                                color = bgColor,
                                topLeft = Offset(canvasW / 2f - measured.size.width / 2f - 20f, posY - measured.size.height / 2f - 10f),
                                size = Size(measured.size.width + 40f, measured.size.height + 20f),
                                cornerRadius = CornerRadius(12f, 12f)
                            )
                        }

                        drawText(
                            textMeasurer = textMeasurer,
                            text = annotatedText,
                            topLeft = Offset(canvasW / 2f - measured.size.width / 2f, posY - measured.size.height / 2f)
                        )
                    } else {
                        // Standard subtitle
                        val measured = textMeasurer.measure(
                            text = AnnotatedString(activeCaption.text),
                            style = TextStyle(
                                color = baseTextColor,
                                fontSize = baseFontSize,
                                fontWeight = if (style.isBold) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        )

                        if (style.hasBackground) {
                            drawRoundRect(
                                color = bgColor,
                                topLeft = Offset(canvasW / 2f - measured.size.width / 2f - 20f, posY - measured.size.height / 2f - 10f),
                                size = Size(measured.size.width + 40f, measured.size.height + 20f),
                                cornerRadius = CornerRadius(12f, 12f)
                            )
                        }

                        drawText(
                            textMeasurer = textMeasurer,
                            text = activeCaption.text,
                            topLeft = Offset(canvasW / 2f - measured.size.width / 2f, posY - measured.size.height / 2f),
                            style = TextStyle(
                                color = baseTextColor,
                                fontSize = baseFontSize,
                                fontWeight = if (style.isBold) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }

                // Active User Drag Selection Box (Tracking Setup)
                if (isTrackingSelectionMode && dragStartOffset != null && dragCurrentOffset != null) {
                    val s = dragStartOffset!!
                    val c = dragCurrentOffset!!
                    val left = min(s.x, c.x)
                    val top = min(s.y, c.y)
                    val width = max(s.x, c.x) - left
                    val height = max(s.y, c.y) - top

                    drawRoundRect(
                        color = AccentSecondary.copy(alpha = 0.25f),
                        topLeft = Offset(left, top),
                        size = Size(width, height),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                    drawRoundRect(
                        color = AccentSecondary,
                        topLeft = Offset(left, top),
                        size = Size(width, height),
                        cornerRadius = CornerRadius(8f, 8f),
                        style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f)))
                    )
                }
            }
        }
    }
}
