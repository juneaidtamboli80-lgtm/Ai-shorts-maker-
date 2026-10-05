package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Represents an individual video clip/segment on the timeline.
 */
data class VideoTimelineSegment(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "Clip",
    val startMs: Long,
    val endMs: Long,
    val thumbnailCount: Int = 4,
    val colorHex: String = "#1E293B",
    val isSelected: Boolean = false,
    val speed: Float = 1.0f,
    val transitionIn: String? = null
) {
    val durationMs: Long get() = (endMs - startMs).coerceAtLeast(100L)
}

/**
 * A horizontal scrollable timeline UI component for visualizing and navigating video segments.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun VideoSegmentTimeline(
    segments: List<VideoTimelineSegment>,
    currentTimeMs: Long,
    totalDurationMs: Long,
    selectedSegmentId: String? = null,
    onSeekToTime: (Long) -> Unit,
    onSelectSegment: (VideoTimelineSegment) -> Unit = {},
    onTrimSegment: (segmentId: String, newStartMs: Long, newEndMs: Long) -> Unit = { _, _, _ -> },
    onSplitSegment: (segmentId: String, splitTimeMs: Long) -> Unit = { _, _ -> },
    onDeleteSegment: (segmentId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var zoomScale by remember { mutableStateOf(1.0f) } // 0.5f to 4.0f
    val scrollState = rememberScrollState()
    val textMeasurer = rememberTextMeasurer()

    val pixelsPerSecond = 90f * zoomScale
    val totalSeconds = (totalDurationMs / 1000f).coerceAtLeast(1f)
    val totalWidthDp = (totalSeconds * pixelsPerSecond).dp.coerceAtLeast(400.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface)
    ) {
        // 1. Timeline Header & Navigation Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = AccentPrimary,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ViewTimeline, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
                Text(
                    "VIDEO SEGMENTS (${segments.size})",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                TimeDisplayChip(currentMs = currentTimeMs, totalMs = totalDurationMs)
            }

            // Zoom & Quick Step Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { zoomScale = (zoomScale - 0.25f).coerceAtLeast(0.5f) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }

                Text(
                    "${(zoomScale * 100).toInt()}%",
                    color = AccentSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
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

        // 2. Horizontal Scrollable Multi-Layer Timeline Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .horizontalScroll(scrollState)
        ) {
            val contentWidthPx = totalSeconds * pixelsPerSecond

            // Layer A: Ruler & Background Track Grid
            Canvas(
                modifier = Modifier
                    .width(totalWidthDp + 200.dp)
                    .fillMaxHeight()
                    .pointerInput(totalDurationMs, zoomScale) {
                        detectTapGestures { offset ->
                            val clickedMs = ((offset.x / pixelsPerSecond) * 1000L).toLong().coerceIn(0L, totalDurationMs)
                            onSeekToTime(clickedMs)
                        }
                    }
                    .pointerInput(totalDurationMs, zoomScale) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val draggedMs = ((change.position.x / pixelsPerSecond) * 1000L).toLong().coerceIn(0L, totalDurationMs)
                            onSeekToTime(draggedMs)
                        }
                    }
            ) {
                val canvasH = size.height
                val rulerHeight = 22.dp.toPx()

                // Draw Timecode Ruler Header
                drawRect(
                    color = DarkSurfaceElevated,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, rulerHeight)
                )

                val stepSec = if (zoomScale > 2f) 1 else if (zoomScale > 1f) 2 else 5
                val totalSecInt = (totalDurationMs / 1000).toInt()
                for (sec in 0..totalSecInt step stepSec) {
                    val x = sec * pixelsPerSecond
                    // Ruler tick line
                    drawLine(
                        color = TextMuted,
                        start = Offset(x, rulerHeight - 6f),
                        end = Offset(x, rulerHeight),
                        strokeWidth = 1.5f
                    )
                    // Sub-ticks
                    val halfX = x + (pixelsPerSecond * stepSec / 2f)
                    if (halfX < size.width) {
                        drawLine(
                            color = DarkSurfaceBorder,
                            start = Offset(halfX, rulerHeight - 3f),
                            end = Offset(halfX, rulerHeight),
                            strokeWidth = 1f
                        )
                    }
                    // Timestamp label
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "${sec}s",
                        topLeft = Offset(x + 4f, 3f),
                        style = TextStyle(color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    )
                }

                // Waveform Sub-track Background (Bottom 24dp)
                val waveTrackY = canvasH - 24.dp.toPx()
                drawRect(
                    color = TimelineAudioTrack.copy(alpha = 0.5f),
                    topLeft = Offset(0f, waveTrackY),
                    size = Size(contentWidthPx, 24.dp.toPx())
                )
                // Procedural audio waveform bars
                var waveX = 0f
                var barIdx = 0
                while (waveX < contentWidthPx) {
                    val barH = (3f + abs(Math.sin(barIdx * 0.35)).toFloat() * 16f)
                    drawLine(
                        color = Color(0xFF34D399),
                        start = Offset(waveX, waveTrackY + (24.dp.toPx() - barH) / 2f),
                        end = Offset(waveX, waveTrackY + (24.dp.toPx() + barH) / 2f),
                        strokeWidth = 1.5f
                    )
                    waveX += 4f
                    barIdx++
                }
            }

            // Layer B: Visual Segment Cards with Filmstrip & Handles
            Row(
                modifier = Modifier
                    .padding(top = 26.dp, bottom = 28.dp)
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                segments.forEachIndexed { index, segment ->
                    val segmentWidthDp = ((segment.durationMs / 1000f) * pixelsPerSecond).dp.coerceAtLeast(60.dp)
                    val isSelected = segment.id == selectedSegmentId
                    val isCurrentPlayheadInside = currentTimeMs in segment.startMs..segment.endMs

                    Box(
                        modifier = Modifier
                            .width(segmentWidthDp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        if (isSelected) AccentPrimaryVariant else Color(0xFF1E293B),
                                        if (isSelected) AccentPrimary else Color(0xFF0F172A)
                                    )
                                )
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) AccentSecondary else if (isCurrentPlayheadInside) AccentPrimary else DarkSurfaceBorder,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                onSelectSegment(segment)
                                onSeekToTime(segment.startMs)
                            }
                    ) {
                        // Filmstrip Thumbnails Representation
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val frames = max(1, (segmentWidthDp.value / 40).toInt())
                            for (f in 0 until frames) {
                                Surface(
                                    color = Color.Black.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Movie,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.25f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Top Title & Duration Badge
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.75f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "#${index + 1} ${segment.title}",
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    maxLines = 1
                                )
                            }

                            Surface(
                                color = if (isSelected) AccentSecondary else Color.Black.copy(alpha = 0.75f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = TimeFormatter.formatDuration(segment.durationMs),
                                    color = if (isSelected) Color.Black else TextSecondary,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Left Trim Handle (Drag to adjust start)
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .width(12.dp)
                                .fillMaxHeight()
                                .background(if (isSelected) AccentSecondary.copy(alpha = 0.5f) else Color.Transparent)
                                .pointerInput(segment.id) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        val deltaMs = ((dragAmount.x / pixelsPerSecond) * 1000L).toLong()
                                        val newStart = (segment.startMs + deltaMs).coerceIn(0L, segment.endMs - 300L)
                                        onTrimSegment(segment.id, newStart, segment.endMs)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(20.dp)
                                    .background(if (isSelected) Color.White else TextMuted, RoundedCornerShape(2.dp))
                            )
                        }

                        // Right Trim Handle (Drag to adjust end)
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .width(12.dp)
                                .fillMaxHeight()
                                .background(if (isSelected) AccentSecondary.copy(alpha = 0.5f) else Color.Transparent)
                                .pointerInput(segment.id) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        val deltaMs = ((dragAmount.x / pixelsPerSecond) * 1000L).toLong()
                                        val newEnd = (segment.endMs + deltaMs).coerceAtLeast(segment.startMs + 300L)
                                        onTrimSegment(segment.id, segment.startMs, newEnd)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(20.dp)
                                    .background(if (isSelected) Color.White else TextMuted, RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            }

            // Layer C: Red Draggable Playhead Overlay
            Canvas(
                modifier = Modifier
                    .width(totalWidthDp + 200.dp)
                    .fillMaxHeight()
            ) {
                val playheadX = (currentTimeMs / 1000f) * pixelsPerSecond
                val canvasH = size.height

                // Playhead top triangle marker
                val path = Path().apply {
                    moveTo(playheadX - 7f, 0f)
                    lineTo(playheadX + 7f, 0f)
                    lineTo(playheadX + 7f, 10f)
                    lineTo(playheadX, 16f)
                    lineTo(playheadX - 7f, 10f)
                    close()
                }
                drawPath(path, color = TimelinePlayhead, style = Fill)

                // Vertical needle line
                drawLine(
                    color = TimelinePlayhead,
                    start = Offset(playheadX, 0f),
                    end = Offset(playheadX, canvasH),
                    strokeWidth = 2.5f
                )
            }
        }

        // 3. Selected Segment Quick Action Footer Bar
        if (selectedSegmentId != null) {
            val currentSelected = segments.find { it.id == selectedSegmentId }
            if (currentSelected != null) {
                Surface(
                    color = DarkSurfaceElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Selected: #${segments.indexOf(currentSelected) + 1} (${TimeFormatter.formatDuration(currentSelected.durationMs)})",
                            color = AccentSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Split at Playhead
                            OutlinedButton(
                                onClick = { onSplitSegment(currentSelected.id, currentTimeMs) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCut, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Split", fontSize = 10.sp)
                            }

                            // Delete Segment
                            IconButton(
                                onClick = { onDeleteSegment(currentSelected.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
