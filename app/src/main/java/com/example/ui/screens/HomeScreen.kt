package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ProjectEntity
import com.example.ui.components.TimeFormatter
import com.example.ui.theme.*
import com.example.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenProject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.projects.collectAsState()
    var projectToRename by remember { mutableStateOf<ProjectEntity?>(null) }
    var renameInput by remember { mutableStateOf("") }

    // Android Photo Picker contract (Zero-permission media selector)
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.createProjectFromUri(uri) { newProjectId ->
                onOpenProject(newProjectId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = AccentPrimary,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("VELOCE VIDEO AI", fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 0.5.sp, color = TextPrimary)
                            Text("Pro Mobile AI Video Studio", fontSize = 11.sp, color = AccentSecondary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        containerColor = DarkBg
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Create Project Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(DarkSurfaceElevated, AccentPrimaryVariant.copy(alpha = 0.35f))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(
                                "Create & Edit AI Videos",
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            )
                            Text(
                                "Auto speech captions, object & face tracking, keyframe animations, and 9:16 viral shorts generation.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Import Local Video Button
                                Button(
                                    onClick = {
                                        videoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.VideoFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Import Video", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                // Interactive Sample Demo Button
                                OutlinedButton(
                                    onClick = {
                                        viewModel.createSampleProject(templateIndex = 0) { demoId ->
                                            onOpenProject(demoId)
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentSecondary)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Demo Project", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Quick Starter Templates Section
            item {
                Text(
                    "POPULAR AI TEMPLATES",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                viewModel.createSampleProject(templateIndex = 0) { onOpenProject(it) }
                            }
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🎬 AI Shorts 9:16", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Karaoke captions & face tracking enabled", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                viewModel.createSampleProject(templateIndex = 1) { onOpenProject(it) }
                            }
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🎙️ Podcast Clip", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Active word scale pop & hook detection", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Recent Projects Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "RECENT PROJECTS (${projects.size})",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            if (projects.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.MovieCreation, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                            Text("No projects yet", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Tap 'Import Video' or 'Demo Project' to start editing.", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(projects, key = { it.id }) { project ->
                    var showMenu by remember { mutableStateOf(false) }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenProject(project.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                // Project Thumbnail / Aspect Badge
                                Surface(
                                    color = DarkSurfaceElevated,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(54.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            when (project.aspectRatio) {
                                                com.example.model.AspectRatio.RATIO_9_16 -> "9:16"
                                                com.example.model.AspectRatio.RATIO_16_9 -> "16:9"
                                                com.example.model.AspectRatio.RATIO_1_1 -> "1:1"
                                                else -> "VID"
                                            },
                                            color = AccentSecondary,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        project.name,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            TimeFormatter.formatDuration(project.durationMs),
                                            color = AccentPrimary,
                                            fontSize = 11.sp,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                        )
                                        Text("•", color = TextMuted, fontSize = 11.sp)
                                        Text(
                                            "${project.width}x${project.height}",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            // Menu options
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextMuted)
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    modifier = Modifier.background(DarkSurfaceElevated)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Open Project", color = TextPrimary) },
                                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = AccentSecondary) },
                                        onClick = {
                                            showMenu = false
                                            onOpenProject(project.id)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Rename", color = TextPrimary) },
                                        leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null, tint = TextSecondary) },
                                        onClick = {
                                            showMenu = false
                                            projectToRename = project
                                            renameInput = project.name
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Duplicate", color = TextPrimary) },
                                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = TextSecondary) },
                                        onClick = {
                                            showMenu = false
                                            viewModel.duplicateProject(project.id)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete", color = ErrorRed) },
                                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed) },
                                        onClick = {
                                            showMenu = false
                                            viewModel.deleteProject(project.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Rename Project Dialog
    if (projectToRename != null) {
        AlertDialog(
            onDismissRequest = { projectToRename = null },
            title = { Text("Rename Project", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("Project Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPrimary,
                        unfocusedBorderColor = DarkSurfaceBorder
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = projectToRename
                        if (p != null && renameInput.isNotBlank()) {
                            viewModel.renameProject(p, renameInput.trim())
                        }
                        projectToRename = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToRename = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}
