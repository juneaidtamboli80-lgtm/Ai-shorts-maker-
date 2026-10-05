package com.example.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import com.example.model.*
import com.example.ui.components.TimeFormatter
import com.example.ui.theme.*

@Composable
fun TrackingPanel(
    trackingObjects: List<TrackingObject>,
    currentTimeMs: Long,
    isTrackingSelectionActive: Boolean,
    isProcessingTracking: Boolean,
    trackingProgress: Float,
    onToggleTrackingSelectionMode: (Boolean) -> Unit,
    onStartFaceTracking: () -> Unit,
    onUpdateTrackingObject: (TrackingObject) -> Unit,
    onDeleteTrackingObject: (String) -> Unit,
    onRecalculateTracking: (TrackingObject) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTrackingId by remember { mutableStateOf<String?>(trackingObjects.firstOrNull()?.id) }
    val selectedTrack = trackingObjects.find { it.id == selectedTrackingId } ?: trackingObjects.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSurface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Action Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "OBJECT & FACE TRACKING",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // Auto Face Detect Button
            FilledTonalButton(
                onClick = onStartFaceTracking,
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = AccentSecondary.copy(alpha = 0.2f), contentColor = AccentSecondary),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Auto Face Track", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Selection Guide Banner
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isTrackingSelectionActive) AccentPrimaryVariant.copy(alpha = 0.5f) else DarkBg
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isTrackingSelectionActive) "👆 Drag on video preview to draw box" else "Select custom area to follow",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Attaches text, emojis, or blur censor to moving subject", color = TextSecondary, fontSize = 10.sp)
                }

                Button(
                    onClick = { onToggleTrackingSelectionMode(!isTrackingSelectionActive) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTrackingSelectionActive) ErrorRed else AccentPrimary
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(if (isTrackingSelectionActive) "Cancel" else "Draw Box", fontSize = 11.sp)
                }
            }
        }

        // Processing Progress Bar
        if (isProcessingTracking) {
            Column {
                LinearProgressIndicator(
                    progress = { trackingProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = AccentSecondary
                )
                Text(
                    "Calculating optical flow & motion keyframes (${(trackingProgress * 100).toInt()}%)...",
                    color = AccentSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Active Tracked Objects Chips
        if (trackingObjects.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                trackingObjects.forEach { track ->
                    val isSelected = track.id == selectedTrack?.id
                    InputChip(
                        selected = isSelected,
                        onClick = { selectedTrackingId = track.id },
                        label = { Text("🎯 ${track.label} (${track.boundingBoxes.size} kf)", fontSize = 11.sp) },
                        trailingIcon = {
                            IconButton(
                                onClick = { onDeleteTrackingObject(track.id) },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Delete", modifier = Modifier.size(12.dp))
                            }
                        },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = AccentPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        Divider(color = DarkSurfaceBorder)

        if (selectedTrack != null) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Attached Item Selector
                Text("ATTACHED ELEMENT TYPE", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TrackedAttachmentType.values().forEach { type ->
                        val isSelected = selectedTrack.attachmentType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { onUpdateTrackingObject(selectedTrack.copy(attachmentType = type)) },
                            label = { Text(type.label, fontSize = 11.sp) }
                        )
                    }
                }

                // Attached Content Edit
                if (selectedTrack.attachmentType == TrackedAttachmentType.TEXT) {
                    OutlinedTextField(
                        value = selectedTrack.attachedText,
                        onValueChange = { onUpdateTrackingObject(selectedTrack.copy(attachedText = it)) },
                        label = { Text("Pinned Label / Speaker Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else if (selectedTrack.attachmentType == TrackedAttachmentType.STICKER) {
                    val emojis = listOf("🔥", "⚡", "🎯", "⭐", "🚀", "👑", "👀", "💎")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        emojis.forEach { em ->
                            val isSelected = selectedTrack.attachedStickerEmoji == em
                            FilledTonalButton(
                                onClick = { onUpdateTrackingObject(selectedTrack.copy(attachedStickerEmoji = em)) },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isSelected) AccentPrimary else DarkSurfaceElevated
                                )
                            ) {
                                Text(em, fontSize = 16.sp)
                            }
                        }
                    }
                }

                // Tracking Sensitivity & Smoothing
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tracking Sensitivity", color = TextSecondary, fontSize = 12.sp)
                        Text("${(selectedTrack.sensitivity * 100).toInt()}%", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = selectedTrack.sensitivity,
                        onValueChange = { onUpdateTrackingObject(selectedTrack.copy(sensitivity = it)) },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = AccentPrimary, activeTrackColor = AccentPrimary)
                    )
                }

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Trajectory Smoothing", color = TextSecondary, fontSize = 12.sp)
                        Text("${(selectedTrack.smoothing * 100).toInt()}%", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = selectedTrack.smoothing,
                        onValueChange = { onUpdateTrackingObject(selectedTrack.copy(smoothing = it)) },
                        valueRange = 0.0f..0.9f,
                        colors = SliderDefaults.colors(thumbColor = AccentSecondary, activeTrackColor = AccentSecondary)
                    )
                }

                // Recalculate Button
                Button(
                    onClick = { onRecalculateTracking(selectedTrack) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = AccentSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Recalculate Motion Trajectory", color = TextPrimary, fontSize = 12.sp)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tap 'Draw Box' or 'Auto Face Track' to track any element.", color = TextMuted, fontSize = 12.sp)
            }
        }
    }
}
