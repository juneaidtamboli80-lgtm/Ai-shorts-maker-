package com.example.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StickerLayer
import com.example.ui.theme.*

@Composable
fun StickersPanel(
    stickerLayers: List<StickerLayer>,
    currentTimeMs: Long,
    onAddSticker: (StickerLayer) -> Unit,
    onUpdateSticker: (StickerLayer) -> Unit,
    onDeleteSticker: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val stickers = listOf(
        "🔥" to "Fire", "⚡" to "Lightning", "🎯" to "Bullseye", "⭐" to "Star",
        "🚀" to "Rocket", "👑" to "Crown", "💯" to "100", "💥" to "Boom",
        "❤️" to "Heart", "💡" to "Idea", "🎬" to "Slate", "🍿" to "Popcorn",
        "💎" to "Diamond", "🏆" to "Trophy", "👀" to "Eyes", "🚨" to "Alert",
        "📈" to "Trending", "🎧" to "Music", "✨" to "Sparkle", "🤙" to "Vibe"
    )

    var selectedStickerId by remember { mutableStateOf<String?>(stickerLayers.firstOrNull()?.id) }
    val selectedSticker = stickerLayers.find { it.id == selectedStickerId } ?: stickerLayers.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSurface)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("ADD STICKERS & BADGES", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

        // Sticker Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier.height(130.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(stickers) { (emoji, label) ->
                Surface(
                    color = DarkBg,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable {
                            val newSticker = StickerLayer(
                                projectId = "",
                                stickerType = "EMOJI",
                                emojiOrText = emoji,
                                startMs = currentTimeMs,
                                endMs = currentTimeMs + 4000L,
                                posX = 0.5f,
                                posY = 0.35f,
                                scale = 1.0f
                            )
                            onAddSticker(newSticker)
                            selectedStickerId = newSticker.id
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(emoji, fontSize = 24.sp)
                    }
                }
            }
        }

        Divider(color = DarkSurfaceBorder)

        if (selectedSticker != null) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ACTIVE STICKER (${selectedSticker.emojiOrText})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    IconButton(onClick = { onDeleteSticker(selectedSticker.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                    }
                }

                // Scale
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Scale", color = TextSecondary, fontSize = 11.sp)
                        Text("${String.format("%.1f", selectedSticker.scale)}x", color = TextPrimary)
                    }
                    Slider(
                        value = selectedSticker.scale,
                        onValueChange = { onUpdateSticker(selectedSticker.copy(scale = it)) },
                        valueRange = 0.4f..3.0f,
                        colors = SliderDefaults.colors(thumbColor = AccentPrimary, activeTrackColor = AccentPrimary)
                    )
                }

                // Rotation
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Rotation", color = TextSecondary, fontSize = 11.sp)
                        Text("${selectedSticker.rotationDeg.toInt()}°", color = TextPrimary)
                    }
                    Slider(
                        value = selectedSticker.rotationDeg,
                        onValueChange = { onUpdateSticker(selectedSticker.copy(rotationDeg = it)) },
                        valueRange = -180f..180f,
                        colors = SliderDefaults.colors(thumbColor = AccentSecondary, activeTrackColor = AccentSecondary)
                    )
                }
            }
        }
    }
}
