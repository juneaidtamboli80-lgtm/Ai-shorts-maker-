package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.EditorViewModel
import com.example.ui.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val editorViewModel: EditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var currentProjectId by remember { mutableStateOf<String?>(null) }

                AnimatedContent(
                    targetState = currentProjectId,
                    transitionSpec = {
                        if (targetState != null) {
                            slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                        }
                    },
                    label = "ScreenTransition",
                    modifier = Modifier.fillMaxSize()
                ) { projectId ->
                    if (projectId != null) {
                        EditorScreen(
                            viewModel = editorViewModel,
                            projectId = projectId,
                            onNavigateBack = {
                                currentProjectId = null
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        HomeScreen(
                            viewModel = homeViewModel,
                            onOpenProject = { id ->
                                currentProjectId = id
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
