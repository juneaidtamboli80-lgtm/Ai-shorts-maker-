package com.example.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.model.*
import com.example.ui.components.TimeFormatter
import com.example.ui.theme.*

@Composable
fun AutoShortsPanel(
    durationMs: Long,
    isGeneratingShorts: Boolean,
    progress: Float,
    suggestedShorts: List<ShortClipSuggestion>,
    onGenerateShorts: (targetDurationSec: Int, style: ShortStylePreset) -> Unit,
    onApplyShortClip: (ShortClipSuggestion) -> Unit,
    modifier: Modifier = Modifier
) {
    var targetSec by remember { mutableStateOf(30) }
    var selectedStyle by remember { mutableStateOf(ShortStylePreset.VIRAL_TIKTOK) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSurface)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // AI Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkBg),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("⚡ AI Shorts & Reels Generator", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Detects highest viral hooks & auto-crops to 9:16", color = TextSecondary, fontSize = 11.sp)
                    }
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentTertiary)
                }

                // Target Duration Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 30, 60).forEach { sec ->
                        val isSelected = targetSec == sec
                        FilledTonalButton(
                            onClick = { targetSec = sec },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isSelected) AccentPrimary else DarkSurfaceElevated,
                                contentColor = if (isSelected) Color.White else TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("${sec}s", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Caption Style Selector
                Text("CAPTION PRESET", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ShortStylePreset.values().forEach { st ->
                        val isSelected = selectedStyle == st
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedStyle = st },
                            label = { Text(st.label, fontSize = 11.sp) }
                        )
                    }
                }

                // Generate Button
                if (isGeneratingShorts) {
                    Column {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                            color = AccentTertiary
                        )
                        Text(
                            "Analyzing engagement peaks & speech cadence (${(progress * 100).toInt()}%)...",
                            color = AccentSecondary,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    Button(
                        onClick = { onGenerateShorts(targetSec, selectedStyle) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Find & Generate Viral Shorts", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Suggestions List
        Text("AI DETECTED MOMENTS (${suggestedShorts.size})", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(suggestedShorts) { short ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(short.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                            // Score Badge
                            Surface(
                                color = if (short.scoreOutOf100 > 90) SuccessGreen else WarningAmber,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "${short.scoreOutOf100}% Score",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(short.hookSummary, color = TextSecondary, fontSize = 11.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Duration: ${short.durationSec}s (${TimeFormatter.formatTimecode(short.startMs)} → ${TimeFormatter.formatTimecode(short.endMs)})",
                                color = AccentSecondary,
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )

                            Button(
                                onClick = { onApplyShortClip(short) },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Apply Short", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
