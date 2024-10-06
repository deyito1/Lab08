package com.example.lab08

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.lab08.Task

class TaskViewModel(private val dao: TaskDao) : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    init {
        viewModelScope.launch {
            _tasks.value = dao.getAllTasks()
        }
    }

    fun addTask(description: String) {
        val newTask = Task(description = description)
        viewModelScope.launch {
            dao.insertTask(newTask)
            _tasks.value = dao.getAllTasks()
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            dao.updateTask(updatedTask)
            _tasks.value = dao.getAllTasks()
        }
    }

    fun deleteAllTasks() {
        viewModelScope.launch {
            dao.deleteAllTasks()
            _tasks.value = emptyList()
        }
    }

    fun editTask(task: Task, newDescription: String) {
        viewModelScope.launch {
            val updatedTask = task.copy(description = newDescription)
            dao.updateTask(updatedTask)
            _tasks.value = dao.getAllTasks()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            dao.deleteTask(task)
            _tasks.value = dao.getAllTasks()
        }
    }

    fun searchTasks(query: String) {
        viewModelScope.launch {
            _tasks.value = dao.searchTasks("%$query%")
        }
    }

    fun getCompletedTasks() {
        viewModelScope.launch {
            _tasks.value = _tasks.value.filter { it.isCompleted }
        }
    }

    fun getPendingTasks() {
        viewModelScope.launch {
            _tasks.value = _tasks.value.filter { !it.isCompleted }
        }
    }

    fun getAllTasks() {
        viewModelScope.launch {
            _tasks.value = dao.getAllTasks()
        }
    }
}
