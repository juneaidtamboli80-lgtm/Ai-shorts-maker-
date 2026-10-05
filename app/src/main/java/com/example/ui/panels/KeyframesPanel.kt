package com.example.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Keyframe
import com.example.model.TextLayer
import com.example.ui.components.TimeFormatter
import com.example.ui.theme.*

@Composable
fun KeyframesPanel(
    selectedLayer: TextLayer?,
    currentTimeMs: Long,
    onAddOrUpdateKeyframe: (Keyframe) -> Unit,
    onDeleteKeyframe: (Long) -> Unit,
    onSeekToTime: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedLayer == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DarkSurface),
            contentAlignment = Alignment.Center
        ) {
            Text("Select a text or sticker layer to animate with keyframes.", color = TextMuted, fontSize = 12.sp)
        }
        return
    }

    val keyframes = selectedLayer.keyframes.sortedBy { it.timeMs }
    val activeKeyframe = keyframes.find { kotlin.math.abs(it.timeMs - currentTimeMs) < 150L }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSurface)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header with Jump Controls & Add Keyframe Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "KEYFRAME ANIMATION (${keyframes.size})",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Jump Prev
                IconButton(
                    onClick = {
                        val prev = keyframes.filter { it.timeMs < currentTimeMs - 50L }.lastOrNull()
                        if (prev != null) onSeekToTime(prev.timeMs)
                    },
                    enabled = keyframes.any { it.timeMs < currentTimeMs - 50L },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Prev Keyframe", tint = KeyframeDiamond)
                }

                // Jump Next
                IconButton(
                    onClick = {
                        val next = keyframes.filter { it.timeMs > currentTimeMs + 50L }.firstOrNull()
                        if (next != null) onSeekToTime(next.timeMs)
                    },
                    enabled = keyframes.any { it.timeMs > currentTimeMs + 50L },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next Keyframe", tint = KeyframeDiamond)
                }
            }
        }

        // Add / Remove Keyframe Button
        if (activeKeyframe != null) {
            Button(
                onClick = { onDeleteKeyframe(activeKeyframe.timeMs) },
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delete Keyframe at ${TimeFormatter.formatTimecode(currentTimeMs)}", fontSize = 12.sp)
            }
        } else {
            Button(
                onClick = {
                    val kf = Keyframe(
                        timeMs = currentTimeMs,
                        posX = selectedLayer.posX,
                        posY = selectedLayer.posY,
                        scale = selectedLayer.scale,
                        rotationDeg = selectedLayer.rotationDeg,
                        opacity = selectedLayer.opacity
                    )
                    onAddOrUpdateKeyframe(kf)
                },
                colors = ButtonDefaults.buttonColors(containerColor = KeyframeDiamond),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Diamond, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Place Keyframe at ${TimeFormatter.formatTimecode(currentTimeMs)}", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Divider(color = DarkSurfaceBorder)

        // Keyframe List & Sliders
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (activeKeyframe != null) {
                Text("EDIT ACTIVE KEYFRAME PROPERTIES", color = KeyframeDiamond, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Scale", color = TextSecondary, fontSize = 11.sp)
                        Text("${String.format("%.2f", activeKeyframe.scale)}x", color = TextPrimary)
                    }
                    Slider(
                        value = activeKeyframe.scale,
                        onValueChange = { onAddOrUpdateKeyframe(activeKeyframe.copy(scale = it)) },
                        valueRange = 0.2f..3.0f,
                        colors = SliderDefaults.colors(thumbColor = KeyframeDiamond, activeTrackColor = KeyframeDiamond)
                    )
                }

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Rotation", color = TextSecondary, fontSize = 11.sp)
                        Text("${activeKeyframe.rotationDeg.toInt()}°", color = TextPrimary)
                    }
                    Slider(
                        value = activeKeyframe.rotationDeg,
                        onValueChange = { onAddOrUpdateKeyframe(activeKeyframe.copy(rotationDeg = it)) },
                        valueRange = -180f..180f,
                        colors = SliderDefaults.colors(thumbColor = KeyframeDiamond, activeTrackColor = KeyframeDiamond)
                    )
                }

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Opacity", color = TextSecondary, fontSize = 11.sp)
                        Text("${(activeKeyframe.opacity * 100).toInt()}%", color = TextPrimary)
                    }
                    Slider(
                        value = activeKeyframe.opacity,
                        onValueChange = { onAddOrUpdateKeyframe(activeKeyframe.copy(opacity = it)) },
                        valueRange = 0.0f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = KeyframeDiamond, activeTrackColor = KeyframeDiamond)
                    )
                }
            } else {
                Text("ALL KEYFRAMES IN LAYER", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                keyframes.forEach { kf ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkBg),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSeekToTime(kf.timeMs) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Diamond, contentDescription = null, tint = KeyframeDiamond, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(TimeFormatter.formatTimecode(kf.timeMs), color = TextPrimary, fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                            }
                            Text("Scale: ${String.format("%.1f", kf.scale)}x • Rot: ${kf.rotationDeg.toInt()}°", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
