package com.example.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.ui.components.TimeFormatter
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptionsPanel(
    captions: List<CaptionSegment>,
    currentTimeMs: Long,
    isGeneratingAiCaptions: Boolean,
    aiProgress: Float,
    onGenerateAiCaptions: (language: String) -> Unit,
    onAddCaption: (startMs: Long, endMs: Long, text: String) -> Unit,
    onUpdateCaptionText: (CaptionSegment, String) -> Unit,
    onUpdateCaptionTiming: (CaptionSegment, Long, Long) -> Unit,
    onDeleteCaption: (String) -> Unit,
    onSplitCaption: (CaptionSegment, Long) -> Unit,
    onMergeCaptions: (CaptionSegment, CaptionSegment) -> Unit,
    onUpdateGlobalSubtitleStyle: (SubtitleStyle) -> Unit,
    onSeekToTime: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Segment List & Search, 1: Subtitle Styling
    var searchQuery by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf("English") }
    var showLanguageMenu by remember { mutableStateOf(false) }

    // Dialog states for Add / Edit Timestamp Overlay
    var showAddOverlayDialog by remember { mutableStateOf(false) }
    var editingCaptionTiming by remember { mutableStateOf<CaptionSegment?>(null) }
    var editingWordTimingCaption by remember { mutableStateOf<CaptionSegment?>(null) }

    val languages = listOf("English", "Spanish", "French", "German", "Japanese", "Portuguese", "Hindi", "Chinese")
    val filteredCaptions = remember(captions, searchQuery) {
        if (searchQuery.isBlank()) captions else captions.filter { it.text.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface)
    ) {
        // Tab Header: Segments vs Style Inspector
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurfaceElevated,
            contentColor = TextPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Captions & Overlays (${captions.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Styles & Animation", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        if (selectedTab == 0) {
            // SEGMENTS & AUTO-GENERATION TAB
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // AI Generate & Add Overlay Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkBg),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("✨ AI Auto Captions & Timestamps", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Word-level timing synchronization", color = TextSecondary, fontSize = 11.sp)
                            }

                            // Language Selector
                            Box {
                                OutlinedButton(
                                    onClick = { showLanguageMenu = true },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(selectedLanguage, fontSize = 11.sp, color = AccentSecondary)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                                DropdownMenu(
                                    expanded = showLanguageMenu,
                                    onDismissRequest = { showLanguageMenu = false }
                                ) {
                                    languages.forEach { lang ->
                                        DropdownMenuItem(
                                            text = { Text(lang) },
                                            onClick = {
                                                selectedLanguage = lang
                                                showLanguageMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isGeneratingAiCaptions) {
                            Column {
                                LinearProgressIndicator(
                                    progress = { aiProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = AccentPrimary,
                                    trackColor = DarkSurfaceElevated
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Analyzing speech cadence & word timestamps (${(aiProgress * 100).toInt()}%)...",
                                    color = AccentSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onGenerateAiCaptions(selectedLanguage) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                                    modifier = Modifier.weight(1.2f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Auto Transcribe", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { showAddOverlayDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.AddComment, contentDescription = null, tint = AccentSecondary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ Add Overlay", fontSize = 11.sp, color = TextPrimary)
                                }
                            }
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search text overlays & timestamps...", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkBg,
                        unfocusedContainerColor = DarkBg,
                        focusedBorderColor = AccentPrimary,
                        unfocusedBorderColor = DarkSurfaceBorder
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                // Caption Segments List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(filteredCaptions) { index, caption ->
                        val isActive = currentTimeMs in caption.startMs..caption.endMs

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isActive) TimelineSubtitleTrack.copy(alpha = 0.5f) else DarkSurfaceElevated
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isActive) 1.5.dp else 0.dp,
                                    color = if (isActive) AccentSecondary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Header: Timestamp Chip, Seek button & Actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = if (isActive) AccentSecondary else DarkBg,
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.clickable { onSeekToTime(caption.startMs) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.PlayCircle,
                                                contentDescription = "Seek",
                                                tint = if (isActive) Color.Black else AccentSecondary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                "${TimeFormatter.formatTimecode(caption.startMs)} → ${TimeFormatter.formatTimecode(caption.endMs)}",
                                                color = if (isActive) Color.Black else TextPrimary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Edit Exact Timestamps Button
                                        IconButton(
                                            onClick = { editingCaptionTiming = caption },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(Icons.Default.AccessTime, contentDescription = "Adjust Timestamps", tint = TextSecondary, modifier = Modifier.size(14.dp))
                                        }

                                        // Word Timings Inspector
                                        IconButton(
                                            onClick = { editingWordTimingCaption = caption },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(Icons.Default.GraphicEq, contentDescription = "Word Timings", tint = AccentTertiary, modifier = Modifier.size(14.dp))
                                        }

                                        // Split button
                                        IconButton(
                                            onClick = { onSplitCaption(caption, (caption.startMs + caption.endMs) / 2L) },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(Icons.Default.CallSplit, contentDescription = "Split", tint = TextMuted, modifier = Modifier.size(14.dp))
                                        }

                                        // Merge with Next button
                                        if (index < filteredCaptions.size - 1) {
                                            IconButton(
                                                onClick = { onMergeCaptions(caption, filteredCaptions[index + 1]) },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(Icons.Default.Merge, contentDescription = "Merge Next", tint = TextMuted, modifier = Modifier.size(14.dp))
                                            }
                                        }

                                        // Delete button
                                        IconButton(
                                            onClick = { onDeleteCaption(caption.id) },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }

                                // Editable Text Field
                                var textInput by remember(caption.text) { mutableStateOf(caption.text) }
                                OutlinedTextField(
                                    value = textInput,
                                    onValueChange = {
                                        textInput = it
                                        onUpdateCaptionText(caption, it)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = DarkBg,
                                        unfocusedContainerColor = DarkBg,
                                        focusedBorderColor = AccentSecondary,
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = TextPrimary),
                                    shape = RoundedCornerShape(6.dp)
                                )

                                // Word-by-Word Active Chips
                                if (caption.words.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        caption.words.forEach { w ->
                                            val isWordActive = currentTimeMs in w.startMs..w.endMs
                                            Surface(
                                                color = if (isWordActive) AccentSecondary else DarkBg,
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.clickable { onSeekToTime(w.startMs) }
                                            ) {
                                                Text(
                                                    text = w.word,
                                                    color = if (isWordActive) Color.Black else TextSecondary,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isWordActive) FontWeight.Bold else FontWeight.Normal,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Fine-Nudge Timestamp Quick Adjusters
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { onUpdateCaptionTiming(caption, (caption.startMs - 100L).coerceAtLeast(0L), caption.endMs) },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                        modifier = Modifier.height(24.dp).weight(1f)
                                    ) {
                                        Text("-0.1s In", fontSize = 9.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { onUpdateCaptionTiming(caption, caption.startMs + 100L, caption.endMs) },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                        modifier = Modifier.height(24.dp).weight(1f)
                                    ) {
                                        Text("+0.1s In", fontSize = 9.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { onUpdateCaptionTiming(caption, caption.startMs, (caption.endMs - 100L).coerceAtLeast(caption.startMs + 200L)) },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                        modifier = Modifier.height(24.dp).weight(1f)
                                    ) {
                                        Text("-0.1s Out", fontSize = 9.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { onUpdateCaptionTiming(caption, caption.startMs, caption.endMs + 100L) },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                        modifier = Modifier.height(24.dp).weight(1f)
                                    ) {
                                        Text("+0.1s Out", fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // SUBTITLE STYLES & ANIMATION TAB
            val currentStyle = captions.firstOrNull()?.style ?: SubtitleStyle()
            SubtitleStyleInspector(
                currentStyle = currentStyle,
                onUpdateStyle = onUpdateGlobalSubtitleStyle,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
    }

    // 1. ADD NEW CAPTION / TEXT OVERLAY AT TIMESTAMPS DIALOG
    if (showAddOverlayDialog) {
        var newText by remember { mutableStateOf("") }
        var startMs by remember { mutableStateOf(currentTimeMs) }
        var endMs by remember { mutableStateOf(currentTimeMs + 3000L) }

        Dialog(onDismissRequest = { showAddOverlayDialog = false }) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = DarkSurface,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Add Caption Overlay", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    OutlinedTextField(
                        value = newText,
                        onValueChange = { newText = it },
                        label = { Text("Overlay Text") },
                        placeholder = { Text("Type caption text...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentPrimary,
                            unfocusedBorderColor = DarkSurfaceBorder
                        )
                    )

                    // Start Timestamp Control
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Start Time: ${TimeFormatter.formatTimecode(startMs)}", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            TextButton(
                                onClick = { startMs = currentTimeMs },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("Snap to Playhead", fontSize = 10.sp, color = AccentSecondary)
                            }
                        }
                        Slider(
                            value = startMs.toFloat(),
                            onValueChange = { startMs = it.toLong().coerceAtMost(endMs - 300L) },
                            valueRange = 0f..(endMs + 10000L).toFloat(),
                            colors = SliderDefaults.colors(thumbColor = AccentPrimary, activeTrackColor = AccentPrimary)
                        )
                    }

                    // End Timestamp Control
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("End Time: ${TimeFormatter.formatTimecode(endMs)}", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            TextButton(
                                onClick = { endMs = currentTimeMs.coerceAtLeast(startMs + 300L) },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("Snap to Playhead", fontSize = 10.sp, color = AccentSecondary)
                            }
                        }
                        Slider(
                            value = endMs.toFloat(),
                            onValueChange = { endMs = it.toLong().coerceAtLeast(startMs + 300L) },
                            valueRange = startMs.toFloat()..(startMs + 20000L).toFloat(),
                            colors = SliderDefaults.colors(thumbColor = AccentSecondary, activeTrackColor = AccentSecondary)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddOverlayDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (newText.isNotBlank()) {
                                    onAddCaption(startMs, endMs, newText.trim())
                                }
                                showAddOverlayDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Add Overlay")
                        }
                    }
                }
            }
        }
    }

    // 2. FINE-TUNE TIMINGS MODAL FOR EXISTING CAPTION
    if (editingCaptionTiming != null) {
        val target = editingCaptionTiming!!
        var startMs by remember(target) { mutableStateOf(target.startMs) }
        var endMs by remember(target) { mutableStateOf(target.endMs) }

        Dialog(onDismissRequest = { editingCaptionTiming = null }) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = DarkSurface,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Fine-Tune Timestamps", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("\"${target.text}\"", color = AccentSecondary, fontSize = 12.sp, maxLines = 2)

                    // Start Time
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Start Time: ${TimeFormatter.formatTimecode(startMs)}", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                OutlinedButton(onClick = { startMs = (startMs - 200L).coerceAtLeast(0L) }, contentPadding = PaddingValues(horizontal = 4.dp), modifier = Modifier.height(24.dp)) {
                                    Text("-0.2s", fontSize = 9.sp)
                                }
                                OutlinedButton(onClick = { startMs = (startMs + 200L).coerceAtMost(endMs - 200L) }, contentPadding = PaddingValues(horizontal = 4.dp), modifier = Modifier.height(24.dp)) {
                                    Text("+0.2s", fontSize = 9.sp)
                                }
                            }
                        }
                    }

                    // End Time
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("End Time: ${TimeFormatter.formatTimecode(endMs)}", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                OutlinedButton(onClick = { endMs = (endMs - 200L).coerceAtLeast(startMs + 200L) }, contentPadding = PaddingValues(horizontal = 4.dp), modifier = Modifier.height(24.dp)) {
                                    Text("-0.2s", fontSize = 9.sp)
                                }
                                OutlinedButton(onClick = { endMs = endMs + 200L }, contentPadding = PaddingValues(horizontal = 4.dp), modifier = Modifier.height(24.dp)) {
                                    Text("+0.2s", fontSize = 9.sp)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = { editingCaptionTiming = null }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                onUpdateCaptionTiming(target, startMs, endMs)
                                editingCaptionTiming = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save Timings")
                        }
                    }
                }
            }
        }
    }

    // 3. WORD-BY-WORD TIMINGS INSPECTOR MODAL
    if (editingWordTimingCaption != null) {
        val target = editingWordTimingCaption!!
        Dialog(onDismissRequest = { editingWordTimingCaption = null }) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = DarkSurface,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Word-Level Timing Breakdown", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Tap any word to preview and align karaoke sync:", color = TextSecondary, fontSize = 11.sp)

                    LazyColumn(
                        modifier = Modifier.height(220.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(target.words) { wIdx, wordTiming ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkBg),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth().clickable { onSeekToTime(wordTiming.startMs) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Surface(color = AccentPrimary, shape = CircleShape, modifier = Modifier.size(18.dp)) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("${wIdx + 1}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Text(wordTiming.word, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }

                                    Text(
                                        "${TimeFormatter.formatTimecode(wordTiming.startMs)} → ${TimeFormatter.formatTimecode(wordTiming.endMs)}",
                                        color = AccentSecondary,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { editingWordTimingCaption = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@Composable
fun SubtitleStyleInspector(
    currentStyle: SubtitleStyle,
    onUpdateStyle: (SubtitleStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier.verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Animation Type Presets
        Text("WORD-BY-WORD ANIMATION", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CaptionAnimation.values().forEach { anim ->
                val isSelected = currentStyle.animation == anim
                FilterChip(
                    selected = isSelected,
                    onClick = { onUpdateStyle(currentStyle.copy(animation = anim)) },
                    label = { Text(anim.label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Divider(color = DarkSurfaceBorder)

        // Color Palettes
        Text("TEXT & ACTIVE WORD COLOR", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        val colorPairs = listOf(
            "#FFFFFF" to "#FDE047" to "Classic Gold",
            "#FFFFFF" to "#38BDF8" to "Electric Cyan",
            "#FFFFFF" to "#EC4899" to "Viral Pink",
            "#F3F4F6" to "#34D399" to "Emerald Glow",
            "#FDE047" to "#EF4444" to "Red Flame"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            colorPairs.forEach { pair ->
                val (textColor, activeColor) = pair.first
                val label = pair.second
                val isSelected = currentStyle.activeWordColorHex == activeColor

                OutlinedButton(
                    onClick = {
                        onUpdateStyle(currentStyle.copy(textColorHex = textColor, activeWordColorHex = activeColor))
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) AccentPrimaryVariant else DarkBg
                    )
                ) {
                    Box(modifier = Modifier.size(14.dp).background(Color(android.graphics.Color.parseColor(textColor)), CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.size(14.dp).background(Color(android.graphics.Color.parseColor(activeColor)), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(label, fontSize = 11.sp, color = TextPrimary)
                }
            }
        }

        // Font Size & Vertical Position Slider
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Font Size", color = TextSecondary, fontSize = 12.sp)
                Text("${currentStyle.fontSizeSp.toInt()} sp", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = currentStyle.fontSizeSp,
                onValueChange = { onUpdateStyle(currentStyle.copy(fontSizeSp = it)) },
                valueRange = 14f..40f,
                colors = SliderDefaults.colors(thumbColor = AccentPrimary, activeTrackColor = AccentPrimary)
            )
        }

        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Vertical Position", color = TextSecondary, fontSize = 12.sp)
                Text("${(currentStyle.positionYRatio * 100).toInt()}%", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = currentStyle.positionYRatio,
                onValueChange = { onUpdateStyle(currentStyle.copy(positionYRatio = it)) },
                valueRange = 0.2f..0.92f,
                colors = SliderDefaults.colors(thumbColor = AccentSecondary, activeTrackColor = AccentSecondary)
            )
        }

        // Background Opacity
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Background Box Opacity", color = TextSecondary, fontSize = 12.sp)
                Text("${(currentStyle.backgroundOpacity * 100).toInt()}%", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = currentStyle.backgroundOpacity,
                onValueChange = { onUpdateStyle(currentStyle.copy(backgroundOpacity = it, hasBackground = it > 0.05f)) },
                valueRange = 0.0f..1.0f,
                colors = SliderDefaults.colors(thumbColor = AccentTertiary, activeTrackColor = AccentTertiary)
            )
        }

        // Active Word Pop Scale
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Active Word Scale Pop", color = TextSecondary, fontSize = 12.sp)
                Text("${String.format("%.2f", currentStyle.activeWordScale)}x", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = currentStyle.activeWordScale,
                onValueChange = { onUpdateStyle(currentStyle.copy(activeWordScale = it)) },
                valueRange = 1.0f..1.6f,
                colors = SliderDefaults.colors(thumbColor = AccentPrimary, activeTrackColor = AccentPrimary)
            )
        }
    }
}
