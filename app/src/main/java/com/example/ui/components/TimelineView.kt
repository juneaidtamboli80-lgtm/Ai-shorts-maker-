package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalTextApi::class)
@Composable
fun TimelineView(
    currentTimeMs: Long,
    durationMs: Long,
    trimStartMs: Long,
    trimEndMs: Long,
    captions: List<CaptionSegment>,
    textLayers: List<TextLayer>,
    stickerLayers: List<StickerLayer>,
    trackingObjects: List<TrackingObject>,
    audioTracks: List<AudioTrack>,
    selectedTrackId: String? = null,
    onSeek: (Long) -> Unit,
    onSelectCaption: (CaptionSegment) -> Unit = {},
    onSelectTextLayer: (TextLayer) -> Unit = {},
    onSelectStickerLayer: (StickerLayer) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var zoomScale by remember { mutableStateOf(1.0f) } // 0.5f to 4.0f
    val scrollState = rememberScrollState()
    val textMeasurer = rememberTextMeasurer()

    // Base pixels per second
    val basePixelsPerSecond = 80f * zoomScale
    val totalSeconds = (durationMs / 1000f).coerceAtLeast(1f)
    val timelineContentWidthDp = (totalSeconds * basePixelsPerSecond).dp.coerceAtLeast(400.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TimelineTrackBg)
    ) {
        // Zoom and Quick Action Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "TIMELINE",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                TimeDisplayChip(currentMs = currentTimeMs, totalMs = durationMs)
            }

            // Zoom In / Out Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { zoomScale = (zoomScale - 0.25f).coerceAtLeast(0.5f) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
                Text(
                    text = "${(zoomScale * 100).toInt()}%",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                IconButton(
                    onClick = { zoomScale = (zoomScale + 0.25f).coerceAtMost(4.0f) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }

        Divider(color = DarkSurfaceBorder, thickness = 1.dp)

        // Scrollable Multi-Track Canvas & Playhead
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .horizontalScroll(scrollState)
        ) {
            val contentWidthPx = (totalSeconds * basePixelsPerSecond)

            // Timeline Tracks Canvas
            Canvas(
                modifier = Modifier
                    .width(timelineContentWidthDp + 200.dp)
                    .fillMaxHeight()
                    .pointerInput(durationMs, zoomScale) {
                        detectTapGestures { offset ->
                            val clickedTimeMs = ((offset.x / basePixelsPerSecond) * 1000L)
                                .toLong()
                                .coerceIn(0L, durationMs)
                            onSeek(clickedTimeMs)
                        }
                    }
                    .pointerInput(durationMs, zoomScale) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val draggedTimeMs = ((change.position.x / basePixelsPerSecond) * 1000L)
                                .toLong()
                                .coerceIn(0L, durationMs)
                            onSeek(draggedTimeMs)
                        }
                    }
            ) {
                val canvasH = size.height
                val timeToX = { ms: Long -> (ms / 1000f) * basePixelsPerSecond }

                // 1. Time Ruler (Top 24dp)
                val rulerHeight = 24.dp.toPx()
                drawRect(
                    color = DarkSurfaceElevated,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, rulerHeight)
                )

                val stepSec = if (zoomScale > 2f) 1 else if (zoomScale > 1f) 2 else 5
                val totalSecInt = (durationMs / 1000).toInt()
                for (sec in 0..totalSecInt step stepSec) {
                    val x = sec * basePixelsPerSecond
                    drawLine(
                        color = TextMuted,
                        start = Offset(x, rulerHeight - 8f),
                        end = Offset(x, rulerHeight),
                        strokeWidth = 2f
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "${sec}s",
                        topLeft = Offset(x + 4f, 4f),
                        style = TextStyle(color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    )
                }

                var currentTrackY = rulerHeight + 4f
                val trackHeight = 28.dp.toPx()
                val trackSpacing = 4.dp.toPx()

                // 2. Video Main Track (with Trim handles)
                val videoStartX = timeToX(0L)
                val videoEndX = timeToX(durationMs)
                drawRoundRect(
                    color = TimelineVideoTrack,
                    topLeft = Offset(videoStartX, currentTrackY),
                    size = Size(videoEndX - videoStartX, trackHeight),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                // Draw filmstrip ticks
                val tickStep = 24f
                var tickX = videoStartX + 8f
                while (tickX < videoEndX - 8f) {
                    drawRect(
                        color = Color.Black.copy(alpha = 0.35f),
                        topLeft = Offset(tickX, currentTrackY + 4f),
                        size = Size(10f, trackHeight - 8f)
                    )
                    tickX += tickStep
                }
                drawText(
                    textMeasurer = textMeasurer,
                    text = "🎬 Video Clip",
                    topLeft = Offset(videoStartX + 12f, currentTrackY + 6f),
                    style = TextStyle(color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                )

                // Trim dim overlay
                if (trimStartMs > 0L) {
                    drawRect(
                        color = Color.Black.copy(alpha = 0.6f),
                        topLeft = Offset(0f, currentTrackY),
                        size = Size(timeToX(trimStartMs), trackHeight)
                    )
                }
                if (trimEndMs < durationMs) {
                    drawRect(
                        color = Color.Black.copy(alpha = 0.6f),
                        topLeft = Offset(timeToX(trimEndMs), currentTrackY),
                        size = Size(videoEndX - timeToX(trimEndMs), trackHeight)
                    )
                }

                currentTrackY += trackHeight + trackSpacing

                // 3. Subtitles Track
                if (captions.isNotEmpty()) {
                    drawRoundRect(
                        color = DarkSurface.copy(alpha = 0.5f),
                        topLeft = Offset(0f, currentTrackY),
                        size = Size(videoEndX, trackHeight),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    captions.forEach { cap ->
                        val startX = timeToX(cap.startMs)
                        val endX = timeToX(cap.endMs)
                        val capWidth = (endX - startX).coerceAtLeast(16f)

                        drawRoundRect(
                            color = TimelineSubtitleTrack,
                            topLeft = Offset(startX, currentTrackY),
                            size = Size(capWidth, trackHeight),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        drawRoundRect(
                            color = AccentSecondary.copy(alpha = 0.8f),
                            topLeft = Offset(startX, currentTrackY),
                            size = Size(capWidth, trackHeight),
                            cornerRadius = CornerRadius(6f, 6f),
                            style = Stroke(width = 1.5f)
                        )
                        drawText(
                            textMeasurer = textMeasurer,
                            text = "💬 ${cap.text.take(18)}...",
                            topLeft = Offset(startX + 6f, currentTrackY + 6f),
                            style = TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        )
                    }
                    currentTrackY += trackHeight + trackSpacing
                }

                // 4. Text Layers Track
                if (textLayers.isNotEmpty()) {
                    textLayers.forEach { layer ->
                        val startX = timeToX(layer.startMs)
                        val endX = timeToX(layer.endMs)
                        val layerW = (endX - startX).coerceAtLeast(16f)

                        drawRoundRect(
                            color = TimelineTextTrack,
                            topLeft = Offset(startX, currentTrackY),
                            size = Size(layerW, trackHeight),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        drawText(
                            textMeasurer = textMeasurer,
                            text = "🔤 ${layer.text.take(14)}",
                            topLeft = Offset(startX + 6f, currentTrackY + 6f),
                            style = TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        )

                        // Render keyframe diamonds along the text layer
                        layer.keyframes.forEach { kf ->
                            val kfX = timeToX(kf.timeMs)
                            drawKeyframeDiamond(kfX, currentTrackY + trackHeight / 2f)
                        }
                    }
                    currentTrackY += trackHeight + trackSpacing
                }

                // 5. Sticker / Tracking Track
                if (stickerLayers.isNotEmpty() || trackingObjects.isNotEmpty()) {
                    stickerLayers.forEach { sticker ->
                        val startX = timeToX(sticker.startMs)
                        val endX = timeToX(sticker.endMs)
                        drawRoundRect(
                            color = Color(0xFF7C2D12),
                            topLeft = Offset(startX, currentTrackY),
                            size = Size((endX - startX).coerceAtLeast(16f), trackHeight),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        drawText(
                            textMeasurer = textMeasurer,
                            text = "🎨 ${sticker.emojiOrText}",
                            topLeft = Offset(startX + 6f, currentTrackY + 6f),
                            style = TextStyle(color = Color.White, fontSize = 10.sp)
                        )
                    }
                    trackingObjects.forEach { track ->
                        val startX = timeToX(track.startMs)
                        val endX = timeToX(track.endMs)
                        drawRoundRect(
                            color = Color(0xFF1E3A8A),
                            topLeft = Offset(startX, currentTrackY),
                            size = Size((endX - startX).coerceAtLeast(16f), trackHeight),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        drawText(
                            textMeasurer = textMeasurer,
                            text = "🎯 ${track.label}",
                            topLeft = Offset(startX + 6f, currentTrackY + 6f),
                            style = TextStyle(color = Color.White, fontSize = 10.sp)
                        )
                    }
                    currentTrackY += trackHeight + trackSpacing
                }

                // 6. Audio Waveform Track
                drawRoundRect(
                    color = TimelineAudioTrack,
                    topLeft = Offset(0f, currentTrackY),
                    size = Size(videoEndX, trackHeight),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                // Draw decorative waveform bars
                var audioBarX = 0f
                var bIdx = 0
                while (audioBarX < videoEndX) {
                    val barH = (4f + abs(Math.sin(bIdx * 0.4)).toFloat() * (trackHeight - 8f))
                    drawLine(
                        color = Color(0xFF34D399),
                        start = Offset(audioBarX, currentTrackY + (trackHeight - barH) / 2f),
                        end = Offset(audioBarX, currentTrackY + (trackHeight + barH) / 2f),
                        strokeWidth = 2f
                    )
                    audioBarX += 6f
                    bIdx++
                }
                drawText(
                    textMeasurer = textMeasurer,
                    text = "🎵 Audio Track",
                    topLeft = Offset(8f, currentTrackY + 6f),
                    style = TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                )

                // 7. Red Playhead Vertical Needle & Scrubber Head
                val playheadX = timeToX(currentTimeMs)
                // Playhead top triangle marker
                val path = Path().apply {
                    moveTo(playheadX - 8f, 0f)
                    lineTo(playheadX + 8f, 0f)
                    lineTo(playheadX + 8f, 12f)
                    lineTo(playheadX, 20f)
                    lineTo(playheadX - 8f, 12f)
                    close()
                }
                drawPath(path, color = TimelinePlayhead, style = Fill)
                // Playhead vertical line through all tracks
                drawLine(
                    color = TimelinePlayhead,
                    start = Offset(playheadX, 0f),
                    end = Offset(playheadX, canvasH),
                    strokeWidth = 2.5f
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawKeyframeDiamond(centerX: Float, centerY: Float) {
    val size = 7f
    val path = Path().apply {
        moveTo(centerX, centerY - size)
        lineTo(centerX + size, centerY)
        lineTo(centerX, centerY + size)
        lineTo(centerX - size, centerY)
        close()
    }
    drawPath(path, color = KeyframeDiamond, style = Fill)
    drawPath(path, color = Color.Black, style = Stroke(width = 1.5f))
}
