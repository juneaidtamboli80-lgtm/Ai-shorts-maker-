package com.example.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UndoRedoManager<T>(private val maxHistory: Int = 30) {

    private val undoStack = ArrayDeque<T>()
    private val redoStack = ArrayDeque<T>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    fun pushState(currentState: T) {
        if (undoStack.size >= maxHistory) {
            undoStack.removeFirst()
        }
        undoStack.addLast(currentState)
        redoStack.clear()
        updateFlows()
    }

    fun undo(currentState: T): T? {
        if (undoStack.isEmpty()) return null
        val previousState = undoStack.removeLast()
        redoStack.addLast(currentState)
        updateFlows()
        return previousState
    }

    fun redo(currentState: T): T? {
        if (redoStack.isEmpty()) return null
        val nextState = redoStack.removeLast()
        undoStack.addLast(currentState)
        updateFlows()
        return nextState
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
        updateFlows()
    }

    private fun updateFlows() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }
}
