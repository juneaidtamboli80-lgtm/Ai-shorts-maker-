package com.example.ui.panels

import androidx.compose.foundation.background
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
import com.example.ui.theme.*

@Composable
fun AudioPanel(
    originalVolume: Float,
    isOriginalMuted: Boolean,
    audioTracks: List<AudioTrack>,
    onUpdateOriginalAudio: (volume: Float, isMuted: Boolean) -> Unit,
    onAddAudioTrack: (AudioTrack) -> Unit,
    onDeleteAudioTrack: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val builtInTracks = listOf(
        BuiltInMusicTrack("bg_1", "⚡ Neon Pulse", "Veloce Beats", 30000L, MusicPresetCategory.BEATS, 128),
        BuiltInMusicTrack("bg_2", "🌌 Epic Horizon", "Cinema Scores", 45000L, MusicPresetCategory.CINEMATIC, 95),
        BuiltInMusicTrack("bg_3", "☕ Midnight Coffee", "Lo-Fi Lounge", 35000L, MusicPresetCategory.LOFI, 85),
        BuiltInMusicTrack("bg_4", "🎹 Deep Reflection", "Ambient Aura", 40000L, MusicPresetCategory.AMBIENT, 70)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSurface)
            .padding(14.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Original Video Audio Control
        Text("ORIGINAL VIDEO AUDIO", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkBg),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isOriginalMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = if (isOriginalMuted) ErrorRed else AccentSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isOriginalMuted) "Muted" else "Volume ${(originalVolume * 100).toInt()}%", color = TextPrimary, fontSize = 13.sp)
                    }

                    Switch(
                        checked = !isOriginalMuted,
                        onCheckedChange = { onUpdateOriginalAudio(originalVolume, !it) }
                    )
                }

                if (!isOriginalMuted) {
                    Slider(
                        value = originalVolume,
                        onValueChange = { onUpdateOriginalAudio(it, false) },
                        valueRange = 0.0f..2.0f,
                        colors = SliderDefaults.colors(thumbColor = AccentSecondary, activeTrackColor = AccentSecondary)
                    )
                }
            }
        }

        Divider(color = DarkSurfaceBorder)

        // Royalty-Free Background Music Library
        Text("ROYALTY-FREE MUSIC TRACKS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

        builtInTracks.forEach { track ->
            val isAdded = audioTracks.any { it.title == track.title }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isAdded) TimelineAudioTrack.copy(alpha = 0.4f) else DarkSurfaceElevated
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(track.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${track.artist} • ${track.bpm} BPM • ${track.category.label}", color = TextSecondary, fontSize = 11.sp)
                    }

                    if (isAdded) {
                        OutlinedButton(
                            onClick = {
                                val added = audioTracks.find { it.title == track.title }
                                if (added != null) onDeleteAudioTrack(added.id)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                        ) {
                            Text("Remove", fontSize = 11.sp)
                        }
                    } else {
                        Button(
                            onClick = {
                                onAddAudioTrack(
                                    AudioTrack(
                                        projectId = "",
                                        title = track.title,
                                        durationMs = track.durationMs,
                                        volume = 0.6f
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Track", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
