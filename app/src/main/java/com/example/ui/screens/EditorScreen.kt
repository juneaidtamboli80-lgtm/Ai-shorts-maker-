package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatio
import com.example.ui.components.TimeFormatter
import com.example.ui.components.TimelineView
import com.example.ui.components.VideoPlayerView
import com.example.ui.panels.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.EditorViewModel

enum class EditorToolTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    EDIT("Edit", Icons.Default.ContentCut),
    CAPTIONS("Captions", Icons.Default.Subtitles),
    TEXT("Text", Icons.Default.TextFields),
    TRACKING("Tracking", Icons.Default.CenterFocusStrong),
    KEYFRAMES("Keyframes", Icons.Default.Diamond),
    EFFECTS("Effects", Icons.Default.AutoFixHigh),
    AUDIO("Audio", Icons.Default.Audiotrack),
    SHORTS("AI Shorts", Icons.Default.Bolt),
    STICKERS("Stickers", Icons.Default.EmojiEmotions)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    projectId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // BackHandler so user returns to home seamlessly
    BackHandler {
        onNavigateBack()
    }

    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }

    val project by viewModel.project.collectAsState()
    val captions by viewModel.captions.collectAsState()
    val textLayers by viewModel.textLayers.collectAsState()
    val stickerLayers by viewModel.stickerLayers.collectAsState()
    val trackingObjects by viewModel.trackingObjects.collectAsState()
    val audioTracks by viewModel.audioTracks.collectAsState()
    val effects by viewModel.effects.collectAsState()

    val isPlaying by viewModel.playerEngine.isPlaying.collectAsState()
    val currentPositionMs by viewModel.playerEngine.currentPositionMs.collectAsState()
    val durationMs by viewModel.playerEngine.durationMs.collectAsState()

    val isTrackingSelectionActive by viewModel.isTrackingSelectionActive.collectAsState()
    val isProcessingTracking by viewModel.isProcessingTracking.collectAsState()
    val trackingProgress by viewModel.trackingProgress.collectAsState()

    val isGeneratingAiCaptions by viewModel.isGeneratingAiCaptions.collectAsState()
    val aiCaptionsProgress by viewModel.aiCaptionsProgress.collectAsState()

    val isGeneratingShorts by viewModel.isGeneratingShorts.collectAsState()
    val shortsProgress by viewModel.shortsProgress.collectAsState()
    val suggestedShorts by viewModel.suggestedShorts.collectAsState()

    val isExporting by viewModel.isExporting.collectAsState()
    val exportProgress by viewModel.exportProgress.collectAsState()
    val exportStatusMessage by viewModel.exportStatusMessage.collectAsState()
    val exportedFile by viewModel.exportedFile.collectAsState()

    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()

    var activeTab by remember { mutableStateOf(EditorToolTab.CAPTIONS) }
    var showExportDialog by remember { mutableStateOf(false) }

    val effectiveDurationMs = remember(project, durationMs) {
        maxOf(project?.durationMs ?: 0L, durationMs, 1000L)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            project?.name ?: "Video Editor",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            color = TextPrimary
                        )
                        Text(
                            "${TimeFormatter.formatTimecode(currentPositionMs)} • ${project?.aspectRatio?.label?.take(4) ?: "9:16"}",
                            fontSize = 11.sp,
                            color = AccentSecondary,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    // Undo
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = canUndo
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = if (canUndo) TextPrimary else TextMuted)
                    }
                    // Redo
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = canRedo
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo", tint = if (canRedo) TextPrimary else TextMuted)
                    }
                    // Export Button
                    Button(
                        onClick = { showExportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 8.dp).height(32.dp)
                    ) {
                        Icon(Icons.Default.IosShare, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        containerColor = DarkBg
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. VIDEO PREVIEW CANVAS & CONTROLS (Top 36% Height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.38f)
                    .background(Color.Black)
            ) {
                if (project != null) {
                    VideoPlayerView(
                        playerEngine = viewModel.playerEngine,
                        videoUri = project!!.videoUri,
                        aspectRatio = project!!.aspectRatio,
                        currentTimeMs = currentPositionMs,
                        captions = captions,
                        textLayers = textLayers,
                        stickerLayers = stickerLayers,
                        trackingObjects = trackingObjects,
                        effects = effects,
                        isTrackingSelectionMode = isTrackingSelectionActive,
                        onTrackingAreaSelected = { l, t, r, b ->
                            viewModel.startTrackingSelectedBox(l, t, r, b)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Floating Playback Control Bar at Bottom of Video Preview
                Surface(
                    color = DarkBg.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Frame -1
                        IconButton(
                            onClick = { viewModel.playerEngine.stepBackwardFrames(1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.FastRewind, contentDescription = "-1 Frame", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }

                        // Jump -1s
                        IconButton(
                            onClick = { viewModel.playerEngine.seekTo(currentPositionMs - 1000L) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = "-1s", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }

                        // Big Glowing Play / Pause
                        Surface(
                            color = AccentPrimary,
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.playerEngine.togglePlayPause() },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Jump +1s
                        IconButton(
                            onClick = { viewModel.playerEngine.seekTo(currentPositionMs + 1000L) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Forward10, contentDescription = "+1s", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }

                        // Frame +1
                        IconButton(
                            onClick = { viewModel.playerEngine.stepForwardFrames(1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.FastForward, contentDescription = "+1 Frame", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // 2. MULTI-TRACK TIMELINE (Middle 24% Height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.24f)
                    .background(TimelineTrackBg)
            ) {
                TimelineView(
                    currentTimeMs = currentPositionMs,
                    durationMs = effectiveDurationMs,
                    trimStartMs = project?.trimStartMs ?: 0L,
                    trimEndMs = project?.trimEndMs ?: effectiveDurationMs,
                    captions = captions,
                    textLayers = textLayers,
                    stickerLayers = stickerLayers,
                    trackingObjects = trackingObjects,
                    audioTracks = audioTracks,
                    onSeek = { viewModel.playerEngine.seekTo(it) },
                    onSelectCaption = { activeTab = EditorToolTab.CAPTIONS },
                    onSelectTextLayer = { activeTab = EditorToolTab.TEXT },
                    onSelectStickerLayer = { activeTab = EditorToolTab.STICKERS },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 3. TOOL SELECTOR TAB BAR
            ScrollableTabRow(
                selectedTabIndex = activeTab.ordinal,
                containerColor = DarkSurfaceElevated,
                contentColor = TextPrimary,
                edgePadding = 8.dp,
                divider = {}
            ) {
                EditorToolTab.values().forEach { tab ->
                    val isSelected = activeTab == tab
                    Tab(
                        selected = isSelected,
                        onClick = { activeTab = tab },
                        icon = {
                            Icon(
                                tab.icon,
                                contentDescription = tab.label,
                                modifier = Modifier.size(18.dp),
                                tint = if (isSelected) AccentPrimary else TextSecondary
                            )
                        },
                        text = {
                            Text(
                                tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                        }
                    )
                }
            }

            // 4. ACTIVE TOOL INSPECTOR DRAWER (Bottom 38% Height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.38f)
                    .background(DarkSurface)
            ) {
                when (activeTab) {
                    EditorToolTab.EDIT -> {
                        EditTrimPanel(
                            durationMs = effectiveDurationMs,
                            trimStartMs = project?.trimStartMs ?: 0L,
                            trimEndMs = project?.trimEndMs ?: effectiveDurationMs,
                            aspectRatio = project?.aspectRatio ?: AspectRatio.RATIO_9_16,
                            speed = project?.speed ?: 1.0f,
                            currentTimeMs = currentPositionMs,
                            onUpdateTrim = { s, e -> viewModel.updateTrim(s, e) },
                            onUpdateAspectRatio = { viewModel.updateAspectRatio(it) },
                            onUpdateSpeed = { viewModel.updateSpeed(it) },
                            onSplitAtPlayhead = { viewModel.splitAtPlayhead() },
                            onSeekToTime = { viewModel.playerEngine.seekTo(it) }
                        )
                    }
                    EditorToolTab.CAPTIONS -> {
                        CaptionsPanel(
                            captions = captions,
                            currentTimeMs = currentPositionMs,
                            isGeneratingAiCaptions = isGeneratingAiCaptions,
                            aiProgress = aiCaptionsProgress,
                            onGenerateAiCaptions = { viewModel.generateAiCaptions(it) },
                            onAddCaption = { s, e, t -> viewModel.addCaption(s, e, t) },
                            onUpdateCaptionText = { c, t -> viewModel.updateCaptionText(c, t) },
                            onUpdateCaptionTiming = { c, s, e -> viewModel.updateCaptionTiming(c, s, e) },
                            onDeleteCaption = { viewModel.deleteCaption(it) },
                            onSplitCaption = { c, t -> viewModel.splitCaption(c, t) },
                            onMergeCaptions = { a, b -> viewModel.mergeCaptions(a, b) },
                            onUpdateGlobalSubtitleStyle = { viewModel.updateGlobalSubtitleStyle(it) },
                            onSeekToTime = { viewModel.playerEngine.seekTo(it) }
                        )
                    }
                    EditorToolTab.TEXT -> {
                        TextLayersPanel(
                            textLayers = textLayers,
                            trackingObjects = trackingObjects,
                            currentTimeMs = currentPositionMs,
                            onAddTextLayer = { viewModel.addTextLayer(it) },
                            onUpdateTextLayer = { viewModel.updateTextLayer(it) },
                            onDeleteTextLayer = { viewModel.deleteTextLayer(it) }
                        )
                    }
                    EditorToolTab.TRACKING -> {
                        TrackingPanel(
                            trackingObjects = trackingObjects,
                            currentTimeMs = currentPositionMs,
                            isTrackingSelectionActive = isTrackingSelectionActive,
                            isProcessingTracking = isProcessingTracking,
                            trackingProgress = trackingProgress,
                            onToggleTrackingSelectionMode = { viewModel.setTrackingSelectionMode(it) },
                            onStartFaceTracking = { viewModel.startFaceTracking() },
                            onUpdateTrackingObject = { viewModel.updateTrackingObject(it) },
                            onDeleteTrackingObject = { viewModel.deleteTrackingObject(it) },
                            onRecalculateTracking = { viewModel.recalculateTracking(it) }
                        )
                    }
                    EditorToolTab.KEYFRAMES -> {
                        KeyframesPanel(
                            selectedLayer = textLayers.firstOrNull(),
                            currentTimeMs = currentPositionMs,
                            onAddOrUpdateKeyframe = { kf ->
                                val layer = textLayers.firstOrNull()
                                if (layer != null) {
                                    val updated = (layer.keyframes.filterNot { it.timeMs == kf.timeMs } + kf).sortedBy { it.timeMs }
                                    viewModel.updateTextLayer(layer.copy(keyframes = updated))
                                }
                            },
                            onDeleteKeyframe = { timeMs ->
                                val layer = textLayers.firstOrNull()
                                if (layer != null) {
                                    val updated = layer.keyframes.filterNot { it.timeMs == timeMs }
                                    viewModel.updateTextLayer(layer.copy(keyframes = updated))
                                }
                            },
                            onSeekToTime = { viewModel.playerEngine.seekTo(it) }
                        )
                    }
                    EditorToolTab.EFFECTS -> {
                        EffectsPanel(
                            effects = effects,
                            onUpdateEffects = { viewModel.updateEffects(it) }
                        )
                    }
                    EditorToolTab.AUDIO -> {
                        AudioPanel(
                            originalVolume = project?.volume ?: 1.0f,
                            isOriginalMuted = project?.isMuted ?: false,
                            audioTracks = audioTracks,
                            onUpdateOriginalAudio = { v, m -> viewModel.updateOriginalAudio(v, m) },
                            onAddAudioTrack = { viewModel.addAudioTrack(it) },
                            onDeleteAudioTrack = { viewModel.deleteAudioTrack(it) }
                        )
                    }
                    EditorToolTab.SHORTS -> {
                        AutoShortsPanel(
                            durationMs = effectiveDurationMs,
                            isGeneratingShorts = isGeneratingShorts,
                            progress = shortsProgress,
                            suggestedShorts = suggestedShorts,
                            onGenerateShorts = { dur, st -> viewModel.generateShorts(dur, st) },
                            onApplyShortClip = { viewModel.applyShortClip(it) }
                        )
                    }
                    EditorToolTab.STICKERS -> {
                        StickersPanel(
                            stickerLayers = stickerLayers,
                            currentTimeMs = currentPositionMs,
                            onAddSticker = { viewModel.addStickerLayer(it) },
                            onUpdateSticker = { viewModel.updateStickerLayer(it) },
                            onDeleteSticker = { viewModel.deleteStickerLayer(it) }
                        )
                    }
                }
            }
        }
    }

    // Export Dialog Overlay
    if (showExportDialog) {
        ExportDialog(
            projectDurationMs = (project?.trimEndMs ?: effectiveDurationMs) - (project?.trimStartMs ?: 0L),
            currentAspectRatio = project?.aspectRatio ?: AspectRatio.RATIO_9_16,
            isExporting = isExporting,
            exportProgress = exportProgress,
            exportStatusMessage = exportStatusMessage,
            exportedFile = exportedFile,
            onStartExport = { viewModel.startExport(it) },
            onCancelExport = { viewModel.cancelExport() },
            onDismiss = {
                showExportDialog = false
            }
        )
    }
}
