package com.nova.assistant.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nova.assistant.domain.model.Priority
import com.nova.assistant.domain.model.Todo
import com.nova.assistant.presentation.ui.theme.*
import com.nova.assistant.presentation.viewmodel.TodoViewModel

@Composable
fun TodoScreen(
    onBack: () -> Unit,
    viewModel: TodoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var newTask by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDeepSpace, BgSpace)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = TextSecondary)
                }
                Text("To-Do List", style = MaterialTheme.typography.titleLarge, color = TextPrimary,
                    modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.toggleShowCompleted() }) {
                    Icon(
                        if (uiState.showCompleted) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        "Toggle completed", tint = TextTertiary
                    )
                }
                IconButton(onClick = { viewModel.clearCompleted() }) {
                    Icon(Icons.Default.DeleteSweep, "Clear done", tint = TextTertiary)
                }
            }

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatBadge("Active", uiState.activeTodos.size, NovaCyan)
                StatBadge("Done", uiState.completedTodos.size, Success)
            }

            if (uiState.activeTodos.isEmpty() && (!uiState.showCompleted || uiState.completedTodos.isEmpty())) {
                Box(Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircleOutline, null, tint = TextDisabled, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No tasks yet", style = MaterialTheme.typography.titleMedium, color = TextTertiary)
                        Text("Ask NOVA to add one!", style = MaterialTheme.typography.bodySmall, color = TextDisabled)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(uiState.activeTodos, key = { it.id }) { todo ->
                        TodoItem(todo, onToggle = { viewModel.toggleCompleted(it) },
                            onDelete = { viewModel.delete(todo) })
                    }
                    if (uiState.showCompleted && uiState.completedTodos.isNotEmpty()) {
                        item {
                            Text("Completed", style = MaterialTheme.typography.labelMedium,
                                color = TextDisabled, modifier = Modifier.padding(vertical = 8.dp))
                        }
                        items(uiState.completedTodos, key = { "done_${it.id}" }) { todo ->
                            TodoItem(todo, onToggle = { viewModel.toggleCompleted(it) },
                                onDelete = { viewModel.delete(todo) })
                        }
                    }
                }
            }

            // Add bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newTask,
                    onValueChange = { newTask = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("New task…", color = TextDisabled) },
                    shape = RoundedCornerShape(28.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NovaCyan, unfocusedBorderColor = BorderPrimary,
                        focusedContainerColor = BgCard, unfocusedContainerColor = BgCard,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                    )
                )
                // Priority toggle
                IconButton(
                    onClick = {
                        selectedPriority = Priority.entries.let {
                            it[(it.indexOf(selectedPriority) + 1) % it.size]
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = BgCard)
                ) {
                    Icon(
                        Icons.Default.Flag, null,
                        tint = priorityColor(selectedPriority),
                        modifier = Modifier.size(20.dp)
                    )
                }
                FilledIconButton(
                    onClick = {
                        if (newTask.isNotBlank()) {
                            viewModel.addTodo(newTask, selectedPriority)
                            newTask = ""
                        }
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = NovaCyan.copy(0.15f)
                    )
                ) {
                    Icon(Icons.Default.Add, "Add task", tint = NovaCyan)
                }
            }
        }

        // Undo snackbar
        uiState.lastDeletedTodo?.let {
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                action = { TextButton(onClick = { viewModel.undoDelete() }) { Text("Undo", color = NovaCyan) } },
                containerColor = BgCard,
                contentColor = TextPrimary
            ) { Text("Task deleted") }
        }
    }
}

@Composable
private fun TodoItem(
    todo: Todo,
    onToggle: (String) -> Unit,
    onDelete: (Todo) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, if (todo.isCompleted) Divider else priorityColor(todo.priority).copy(0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { onToggle(todo.id) },
                colors = CheckboxDefaults.colors(
                    checkedColor = Success,
                    uncheckedColor = BorderPrimary,
                    checkmarkColor = BgDeepSpace
                )
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    todo.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (todo.isCompleted) TextDisabled else TextPrimary,
                    textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else null
                )
                if (todo.description.isNotBlank()) {
                    Text(todo.description, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                }
            }
            Icon(Icons.Default.Flag, null, tint = priorityColor(todo.priority), modifier = Modifier.size(14.dp))
            IconButton(onClick = { onDelete(todo) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, "Delete", tint = TextDisabled, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun StatBadge(label: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(count.toString(), style = MaterialTheme.typography.titleMedium, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
    }
}

private fun priorityColor(priority: Priority) = when (priority) {
    Priority.LOW      -> TextTertiary
    Priority.MEDIUM   -> Info
    Priority.HIGH     -> Warning
    Priority.CRITICAL -> Error
}
