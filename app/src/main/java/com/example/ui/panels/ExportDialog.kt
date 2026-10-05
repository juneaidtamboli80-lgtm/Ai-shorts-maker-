package com.example.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.ui.components.TimeFormatter
import com.example.ui.theme.*
import java.io.File

@Composable
fun ExportDialog(
    projectDurationMs: Long,
    currentAspectRatio: AspectRatio,
    isExporting: Boolean,
    exportProgress: Float,
    exportStatusMessage: String,
    exportedFile: File?,
    onStartExport: (ExportConfig) -> Unit,
    onCancelExport: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedResolution by remember { mutableStateOf(ExportResolution.RES_1080P) }
    var selectedFps by remember { mutableStateOf(ExportFps.FPS_30) }
    var selectedCodec by remember { mutableStateOf(VideoCodec.H264) }
    var selectedQuality by remember { mutableStateOf(ExportQuality.BALANCED) }

    val estimatedSizeMb = remember(projectDurationMs, selectedQuality, selectedFps) {
        val sec = projectDurationMs / 1000f
        val bitrate = selectedQuality.bitrateMbps * (if (selectedFps == ExportFps.FPS_60) 1.25f else 1f)
        (sec * bitrate / 8f).coerceAtLeast(0.5f)
    }

    Dialog(onDismissRequest = { if (!isExporting) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.IosShare, contentDescription = null, tint = AccentPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Video", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }

                    if (!isExporting) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }
                }

                Divider(color = DarkSurfaceBorder)

                if (exportedFile != null && !isExporting) {
                    // Export Success State
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(48.dp))
                        Text("Export Complete!", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Saved: ${exportedFile.name}", color = TextSecondary, fontSize = 12.sp)
                        Text("File size: ${String.format("%.1f", exportedFile.length() / (1024f * 1024f))} MB", color = AccentSecondary, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
                        ) {
                            Text("Done")
                        }
                    }
                } else if (isExporting) {
                    // Export in Progress State
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            progress = { exportProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = AccentPrimary,
                            trackColor = DarkSurfaceElevated
                        )

                        Text(
                            "${(exportProgress * 100).toInt()}%",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )

                        Text(
                            exportStatusMessage,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        OutlinedButton(
                            onClick = onCancelExport,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                        ) {
                            Text("Cancel Export")
                        }
                    }
                } else {
                    // Configuration Options State
                    // Resolution
                    Text("RESOLUTION", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExportResolution.values().forEach { res ->
                            val isSelected = selectedResolution == res
                            FilledTonalButton(
                                onClick = { selectedResolution = res },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isSelected) AccentPrimary else DarkSurfaceElevated,
                                    contentColor = if (isSelected) Color.White else TextPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text(res.label.take(5), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Framerate
                    Text("FRAME RATE", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExportFps.values().forEach { fps ->
                            val isSelected = selectedFps == fps
                            FilledTonalButton(
                                onClick = { selectedFps = fps },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isSelected) AccentSecondary else DarkSurfaceElevated,
                                    contentColor = if (isSelected) Color.Black else TextPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("${fps.fps} FPS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Codec
                    Text("ENCODER CODEC", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VideoCodec.values().forEach { codec ->
                            val isSelected = selectedCodec == codec
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCodec = codec },
                                label = { Text(codec.label, fontSize = 11.sp) }
                            )
                        }
                    }

                    // File Size Estimate Summary
                    Surface(
                        color = DarkBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Estimated Size:", color = TextSecondary, fontSize = 12.sp)
                            Text("~${String.format("%.1f", estimatedSizeMb)} MB (${TimeFormatter.formatTimecode(projectDurationMs)})", color = AccentSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Export Start Button
                    Button(
                        onClick = {
                            val config = ExportConfig(
                                resolution = selectedResolution,
                                fps = selectedFps,
                                codec = selectedCodec,
                                quality = selectedQuality,
                                targetAspectRatio = currentAspectRatio
                            )
                            onStartExport(config)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Export (MP4)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
