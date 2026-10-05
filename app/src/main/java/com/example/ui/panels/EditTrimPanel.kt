package com.example.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatio
import com.example.ui.components.TimeFormatter
import com.example.ui.components.VideoSegmentTimeline
import com.example.ui.components.VideoTimelineSegment
import com.example.ui.theme.*

@Composable
fun EditTrimPanel(
    durationMs: Long,
    trimStartMs: Long,
    trimEndMs: Long,
    aspectRatio: AspectRatio,
    speed: Float,
    currentTimeMs: Long,
    onUpdateTrim: (startMs: Long, endMs: Long) -> Unit,
    onUpdateAspectRatio: (AspectRatio) -> Unit,
    onUpdateSpeed: (Float) -> Unit,
    onSplitAtPlayhead: () -> Unit,
    onSeekToTime: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val segments = remember(trimStartMs, trimEndMs, durationMs) {
        listOf(
            VideoTimelineSegment(
                id = "seg_main",
                title = "Main Clip",
                startMs = trimStartMs,
                endMs = trimEndMs
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Visual Interactive Segment Timeline
        VideoSegmentTimeline(
            segments = segments,
            currentTimeMs = currentTimeMs,
            totalDurationMs = durationMs,
            selectedSegmentId = "seg_main",
            onSeekToTime = onSeekToTime,
            onTrimSegment = { _, start, end ->
                onUpdateTrim(start, end)
            },
            onSplitSegment = { _, splitMs ->
                onSplitAtPlayhead()
            },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
        )

        // 1. Aspect Ratio Selector
        Text(
            text = "CANVAS ASPECT RATIO",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AspectRatio.values().forEach { ratio ->
                val isSelected = ratio == aspectRatio
                FilterChip(
                    selected = isSelected,
                    onClick = { onUpdateAspectRatio(ratio) },
                    label = { Text(ratio.label, fontSize = 12.sp) },
                    leadingIcon = {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = DarkSurfaceElevated,
                        labelColor = TextPrimary
                    )
                )
            }
        }

        Divider(color = DarkSurfaceBorder, thickness = 1.dp)

        // 2. Video Trimming Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TRIM VIDEO",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            TextButton(onClick = { onUpdateTrim(0L, durationMs) }) {
                Text("Reset Trim", color = AccentSecondary, fontSize = 12.sp)
            }
        }

        // Trim Start Slider & Fine Buttons
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Start Point:", color = TextSecondary, fontSize = 12.sp)
                Text(TimeFormatter.formatTimecode(trimStartMs), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Slider(
                value = trimStartMs.toFloat(),
                onValueChange = { onUpdateTrim(it.toLong().coerceAtMost(trimEndMs - 500L), trimEndMs) },
                valueRange = 0f..durationMs.toFloat(),
                colors = SliderDefaults.colors(thumbColor = AccentPrimary, activeTrackColor = AccentPrimary)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onUpdateTrim((trimStartMs - 500L).coerceAtLeast(0L), trimEndMs) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text("-0.5s", fontSize = 11.sp)
                }
                Button(
                    onClick = { onUpdateTrim(currentTimeMs.coerceAtMost(trimEndMs - 500L), trimEndMs) },
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text("Set to Playhead", fontSize = 11.sp, color = AccentSecondary)
                }
                OutlinedButton(
                    onClick = { onUpdateTrim((trimStartMs + 500L).coerceAtMost(trimEndMs - 500L), trimEndMs) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text("+0.5s", fontSize = 11.sp)
                }
            }
        }

        // Trim End Slider & Fine Buttons
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("End Point:", color = TextSecondary, fontSize = 12.sp)
                Text(TimeFormatter.formatTimecode(trimEndMs), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Slider(
                value = trimEndMs.toFloat(),
                onValueChange = { onUpdateTrim(trimStartMs, it.toLong().coerceAtLeast(trimStartMs + 500L)) },
                valueRange = 0f..durationMs.toFloat(),
                colors = SliderDefaults.colors(thumbColor = AccentSecondary, activeTrackColor = AccentSecondary)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onUpdateTrim(trimStartMs, (trimEndMs - 500L).coerceAtLeast(trimStartMs + 500L)) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text("-0.5s", fontSize = 11.sp)
                }
                Button(
                    onClick = { onUpdateTrim(trimStartMs, currentTimeMs.coerceAtLeast(trimStartMs + 500L)) },
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text("Set to Playhead", fontSize = 11.sp, color = AccentSecondary)
                }
                OutlinedButton(
                    onClick = { onUpdateTrim(trimStartMs, (trimEndMs + 500L).coerceAtMost(durationMs)) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text("+0.5s", fontSize = 11.sp)
                }
            }
        }

        Divider(color = DarkSurfaceBorder, thickness = 1.dp)

        // 3. Playback Speed
        Text(
            text = "PLAYBACK SPEED",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val speedOptions = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
            speedOptions.forEach { s ->
                val isSelected = speed == s
                FilledTonalButton(
                    onClick = { onUpdateSpeed(s) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isSelected) AccentPrimary else DarkSurfaceElevated,
                        contentColor = if (isSelected) Color.White else TextPrimary
                    )
                ) {
                    Text("${s}x", fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Divider(color = DarkSurfaceBorder, thickness = 1.dp)

        // 4. Split and Cut Action
        Button(
            onClick = onSplitAtPlayhead,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.ContentCut, contentDescription = null, tint = AccentTertiary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Split Clip at Playhead (${TimeFormatter.formatTimecode(currentTimeMs)})", color = TextPrimary)
        }
    }
}
