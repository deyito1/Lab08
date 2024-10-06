package com.example.lab08

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.Room
import kotlinx.coroutines.launch
import com.example.lab08.ui.theme.Lab08Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab08Theme {
                val db = Room.databaseBuilder(
                    applicationContext,
                    TaskDatabase::class.java,
                    "task_db"
                ).build()
                val taskDao = db.taskDao()
                val viewModel = TaskViewModel(taskDao)
                TaskScreen(viewModel)
            }
        }
    }
}

@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var newTaskDescription by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search tasks text field
        TextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.searchTasks(searchQuery)
            },
            label = { Text("Buscar tarea") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // New task text field
        TextField(
            value = newTaskDescription,
            onValueChange = { newTaskDescription = it },
            label = { Text("Nueva tarea") },
            modifier = Modifier.fillMaxWidth()
        )

        // Button to add new task
        OutlinedButton(
            onClick = {
                if (newTaskDescription.isNotEmpty()) {
                    viewModel.addTask(newTaskDescription)
                    newTaskDescription = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Agregar tarea")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Filter buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = { viewModel.getCompletedTasks() }) {
                Text("Completadas")
            }
            OutlinedButton(onClick = { viewModel.getPendingTasks() }) {
                Text("Pendientes")
            }
            OutlinedButton(onClick = { viewModel.getAllTasks() }) {
                Text("Todas")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Task list with buttons
        tasks.forEach { task ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = task.description)
                Row {
                    // Edit task button
                    OutlinedButton(onClick = {
                        isEditing = true
                        taskToEdit = task
                    }) {
                        Text("Editar")
                    }
                    Spacer(modifier = Modifier.width(4.dp))

                    // Toggle task completion button
                    OutlinedButton(onClick = { viewModel.toggleTaskCompletion(task) }) {
                        Text(if (task.isCompleted) "Completada" else "Pendiente")
                    }
                    Spacer(modifier = Modifier.width(4.dp))

                    // Delete task button
                    OutlinedButton(onClick = { viewModel.deleteTask(task) }) {
                        Text("Eliminar")
                    }
                }
            }
        }

        // Edit task dialog
        if (isEditing && taskToEdit != null) {
            EditTaskDialog(
                task = taskToEdit!!,
                onDismiss = { isEditing = false }
            ) { newDescription ->
                viewModel.editTask(taskToEdit!!, newDescription)
                isEditing = false
            }
        }

        // Delete all tasks button
        Button(
            onClick = { coroutineScope.launch { viewModel.deleteAllTasks() } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Eliminar todas las tareas")
        }
    }
}

@Composable
fun EditTaskDialog(task: Task, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var updatedDescription by remember { mutableStateOf(task.description) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar tarea") },
        text = {
            TextField(
                value = updatedDescription,
                onValueChange = { updatedDescription = it }
            )
        },
        confirmButton = {
            Button(onClick = { onSave(updatedDescription) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

