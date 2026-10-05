package com.example.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FilterPreset
import com.example.model.VideoEffects
import com.example.ui.theme.*

@Composable
fun EffectsPanel(
    effects: VideoEffects,
    onUpdateEffects: (VideoEffects) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSurface)
            .padding(14.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Presets Header & Reset Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "COLOR & CINEMATIC FILTERS",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            TextButton(onClick = { onUpdateEffects(VideoEffects()) }) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = AccentSecondary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset All", fontSize = 11.sp, color = AccentSecondary)
            }
        }

        // Filter Preset Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterPreset.values().forEach { preset ->
                FilledTonalButton(
                    onClick = { onUpdateEffects(preset.effects) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = DarkSurfaceElevated,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(preset.label, fontSize = 11.sp)
                }
            }
        }

        Divider(color = DarkSurfaceBorder)

        // Manual Tuning Sliders
        Text("MANUAL ADJUSTMENTS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

        // Brightness
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Brightness", color = TextSecondary, fontSize = 11.sp)
                Text("${(effects.brightness * 100).toInt()}%", color = TextPrimary)
            }
            Slider(
                value = effects.brightness,
                onValueChange = { onUpdateEffects(effects.copy(brightness = it)) },
                valueRange = -0.5f..0.5f,
                colors = SliderDefaults.colors(thumbColor = AccentPrimary, activeTrackColor = AccentPrimary)
            )
        }

        // Contrast
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Contrast", color = TextSecondary, fontSize = 11.sp)
                Text("${String.format("%.2f", effects.contrast)}x", color = TextPrimary)
            }
            Slider(
                value = effects.contrast,
                onValueChange = { onUpdateEffects(effects.copy(contrast = it)) },
                valueRange = 0.5f..2.0f,
                colors = SliderDefaults.colors(thumbColor = AccentSecondary, activeTrackColor = AccentSecondary)
            )
        }

        // Saturation
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Saturation", color = TextSecondary, fontSize = 11.sp)
                Text("${(effects.saturation * 100).toInt()}%", color = TextPrimary)
            }
            Slider(
                value = effects.saturation,
                onValueChange = { onUpdateEffects(effects.copy(saturation = it)) },
                valueRange = 0.0f..2.0f,
                colors = SliderDefaults.colors(thumbColor = AccentTertiary, activeTrackColor = AccentTertiary)
            )
        }

        // Vignette
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Vignette Edge Blur", color = TextSecondary, fontSize = 11.sp)
                Text("${(effects.vignette * 100).toInt()}%", color = TextPrimary)
            }
            Slider(
                value = effects.vignette,
                onValueChange = { onUpdateEffects(effects.copy(vignette = it)) },
                valueRange = 0.0f..0.8f,
                colors = SliderDefaults.colors(thumbColor = AccentPrimary, activeTrackColor = AccentPrimary)
            )
        }

        // Grayscale Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Black & White (Grayscale)", color = TextPrimary, fontSize = 13.sp)
            Switch(
                checked = effects.isGrayscale,
                onCheckedChange = { onUpdateEffects(effects.copy(isGrayscale = it)) },
                colors = SwitchDefaults.colors(checkedThumbColor = AccentPrimary)
            )
        }
    }
}
