package com.example.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
fun TextLayersPanel(
    textLayers: List<TextLayer>,
    trackingObjects: List<TrackingObject>,
    currentTimeMs: Long,
    onAddTextLayer: (TextLayer) -> Unit,
    onUpdateTextLayer: (TextLayer) -> Unit,
    onDeleteTextLayer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedLayerId by remember { mutableStateOf<String?>(textLayers.firstOrNull()?.id) }
    val selectedLayer = textLayers.find { it.id == selectedLayerId } ?: textLayers.firstOrNull()

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
                text = "TEXT LAYERS (${textLayers.size})",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Button(
                onClick = {
                    val newLayer = TextLayer(
                        projectId = "",
                        text = "Sample Title",
                        startMs = currentTimeMs,
                        endMs = currentTimeMs + 4000L,
                        posX = 0.5f,
                        posY = 0.4f,
                        fontSizeSp = 24f,
                        colorHex = "#FFFFFF"
                    )
                    onAddTextLayer(newLayer)
                    selectedLayerId = newLayer.id
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ New Text Layer", fontSize = 11.sp)
            }
        }

        // Layer selection chips
        if (textLayers.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                textLayers.forEach { layer ->
                    val isSelected = layer.id == selectedLayer?.id
                    InputChip(
                        selected = isSelected,
                        onClick = { selectedLayerId = layer.id },
                        label = { Text("🔤 ${layer.text.take(12)}", fontSize = 11.sp) },
                        trailingIcon = {
                            IconButton(
                                onClick = { onDeleteTextLayer(layer.id) },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Delete", modifier = Modifier.size(12.dp))
                            }
                        },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = AccentPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = DarkSurfaceElevated,
                            labelColor = TextPrimary
                        )
                    )
                }
            }
        }

        Divider(color = DarkSurfaceBorder)

        if (selectedLayer != null) {
            // Selected Layer Editor
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Text Input
                OutlinedTextField(
                    value = selectedLayer.text,
                    onValueChange = { onUpdateTextLayer(selectedLayer.copy(text = it)) },
                    label = { Text("Layer Text Content") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkBg,
                        unfocusedContainerColor = DarkBg,
                        focusedBorderColor = AccentPrimary,
                        unfocusedBorderColor = DarkSurfaceBorder
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                // Color Presets
                Text("TEXT COLOR", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                val colors = listOf("#FFFFFF", "#FDE047", "#38BDF8", "#EC4899", "#34D399", "#EF4444", "#A855F7")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colors.forEach { hex ->
                        val isSelected = selectedLayer.colorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(android.graphics.Color.parseColor(hex)), CircleShape)
                                .clickable { onUpdateTextLayer(selectedLayer.copy(colorHex = hex)) }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Font Size & Position X / Y Sliders
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Font Size", color = TextSecondary, fontSize = 12.sp)
                        Text("${selectedLayer.fontSizeSp.toInt()} sp", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = selectedLayer.fontSizeSp,
                        onValueChange = { onUpdateTextLayer(selectedLayer.copy(fontSizeSp = it)) },
                        valueRange = 12f..56f,
                        colors = SliderDefaults.colors(thumbColor = AccentPrimary, activeTrackColor = AccentPrimary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Horizontal X: ${(selectedLayer.posX * 100).toInt()}%", color = TextSecondary, fontSize = 11.sp)
                        Slider(
                            value = selectedLayer.posX,
                            onValueChange = { onUpdateTextLayer(selectedLayer.copy(posX = it)) },
                            valueRange = 0.1f..0.9f
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Vertical Y: ${(selectedLayer.posY * 100).toInt()}%", color = TextSecondary, fontSize = 11.sp)
                        Slider(
                            value = selectedLayer.posY,
                            onValueChange = { onUpdateTextLayer(selectedLayer.copy(posY = it)) },
                            valueRange = 0.1f..0.9f
                        )
                    }
                }

                // Scale & Rotation Sliders
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Scale: ${String.format("%.1f", selectedLayer.scale)}x", color = TextSecondary, fontSize = 11.sp)
                        Slider(
                            value = selectedLayer.scale,
                            onValueChange = { onUpdateTextLayer(selectedLayer.copy(scale = it)) },
                            valueRange = 0.5f..3.0f
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Rotation: ${selectedLayer.rotationDeg.toInt()}°", color = TextSecondary, fontSize = 11.sp)
                        Slider(
                            value = selectedLayer.rotationDeg,
                            onValueChange = { onUpdateTextLayer(selectedLayer.copy(rotationDeg = it)) },
                            valueRange = -180f..180f
                        )
                    }
                }

                // Animation Type Presets
                Text("ENTER ANIMATION", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LayerAnimationType.values().forEach { anim ->
                        val isSelected = selectedLayer.animation == anim
                        FilterChip(
                            selected = isSelected,
                            onClick = { onUpdateTextLayer(selectedLayer.copy(animation = anim)) },
                            label = { Text(anim.label, fontSize = 11.sp) }
                        )
                    }
                }

                // Add Keyframe Shortcut at Current Playhead
                Button(
                    onClick = {
                        val newKeyframe = Keyframe(
                            timeMs = currentTimeMs,
                            posX = selectedLayer.posX,
                            posY = selectedLayer.posY,
                            scale = selectedLayer.scale,
                            rotationDeg = selectedLayer.rotationDeg,
                            opacity = selectedLayer.opacity
                        )
                        val updatedKeyframes = (selectedLayer.keyframes + newKeyframe).distinctBy { it.timeMs }.sortedBy { it.timeMs }
                        onUpdateTextLayer(selectedLayer.copy(keyframes = updatedKeyframes))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
                ) {
                    Icon(Icons.Default.Diamond, contentDescription = null, tint = KeyframeDiamond, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Keyframe at ${TimeFormatter.formatTimecode(currentTimeMs)}", color = TextPrimary, fontSize = 12.sp)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No text layer selected. Tap '+ New Text Layer' above.", color = TextMuted, fontSize = 12.sp)
            }
        }
    }
}
