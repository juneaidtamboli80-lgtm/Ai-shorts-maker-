package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.ProjectEntity
import com.example.data.repository.ProjectRepository
import com.example.data.sample.SampleVideoProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProjectRepository(application)

    val projects: StateFlow<List<ProjectEntity>> = repository.getAllProjectsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createProjectFromUri(uri: Uri, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val project = repository.createProjectFromUri(uri)
            onCreated(project.id)
        }
    }

    fun createSampleProject(templateIndex: Int, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val project = repository.createSampleProject(templateIndex)
            onCreated(project.id)
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
        }
    }

    fun duplicateProject(projectId: String, onDuplicated: (String) -> Unit = {}) {
        viewModelScope.launch {
            val duplicated = repository.duplicateProject(projectId)
            if (duplicated != null) {
                onDuplicated(duplicated.id)
            }
        }
    }

    fun renameProject(project: ProjectEntity, newName: String) {
        viewModelScope.launch {
            repository.updateProject(project.copy(name = newName))
        }
    }
}
