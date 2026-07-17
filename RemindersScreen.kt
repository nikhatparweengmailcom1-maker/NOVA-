package com.nova.assistant.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nova.assistant.domain.model.Reminder
import com.nova.assistant.domain.model.ReminderType
import com.nova.assistant.presentation.ui.theme.*
import com.nova.assistant.presentation.viewmodel.RemindersViewModel
import com.nova.assistant.util.DateTimeUtil

@Composable
fun RemindersScreen(
    onBack: () -> Unit,
    viewModel: RemindersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var newTitle by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf(System.currentTimeMillis() + 3600_000L) }

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
                Text("Reminders", style = MaterialTheme.typography.titleLarge, color = TextPrimary,
                    modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.clearCompleted() }) {
                    Icon(Icons.Default.DeleteSweep, "Clear done", tint = TextTertiary)
                }
            }

            if (uiState.reminders.isEmpty()) {
                // Empty state
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Alarm, null, tint = TextDisabled, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No reminders yet", style = MaterialTheme.typography.titleMedium, color = TextTertiary)
                        Text("Ask NOVA to set one for you!", style = MaterialTheme.typography.bodySmall, color = TextDisabled)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.reminders, key = { it.id }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onComplete = { viewModel.markCompleted(it) },
                            onDelete   = { viewModel.delete(it) }
                        )
                    }
                }
            }

            // Add reminder FAB area
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.BottomEnd) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.showAddDialog() },
                    containerColor = NovaCyan.copy(0.2f),
                    contentColor = NovaCyan,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Add Reminder")
                }
            }
        }

        // Add dialog
        if (uiState.showAddDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.hideAddDialog() },
                containerColor = BgCard,
                title = { Text("New Reminder", color = TextPrimary) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("Title", color = TextTertiary) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NovaCyan, unfocusedBorderColor = BorderPrimary,
                                focusedContainerColor = BgSurfaceVar, unfocusedContainerColor = BgSurfaceVar,
                                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Text(
                            "Scheduled: ${DateTimeUtil.formatDateTime(selectedTime)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = NovaCyan
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(30 to "+30m", 60 to "+1h", 120 to "+2h", 1440 to "+1d")
                                .forEach { (mins, label) ->
                                    OutlinedButton(
                                        onClick = { selectedTime = System.currentTimeMillis() + mins * 60_000L },
                                        border = BorderStroke(0.5.dp, BorderPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    }
                                }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newTitle.isNotBlank()) {
                                viewModel.createReminder(newTitle, selectedTime)
                                newTitle = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NovaCyan.copy(0.2f)),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Set", color = NovaCyan) }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideAddDialog() }) {
                        Text("Cancel", color = TextTertiary)
                    }
                }
            )
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: Reminder,
    onComplete: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val typeIcon = when (reminder.type) {
        ReminderType.ALARM  -> Icons.Default.Alarm
        ReminderType.TIMER  -> Icons.Default.Timer
        else                -> Icons.Default.Notifications
    }
    val typeColor = when (reminder.type) {
        ReminderType.ALARM  -> Warning
        ReminderType.TIMER  -> NovaPurple
        else                -> NovaCyan
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.5.dp, if (reminder.isCompleted) Divider else typeColor.copy(0.3f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(typeIcon, null, tint = if (reminder.isCompleted) TextDisabled else typeColor,
                modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    reminder.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (reminder.isCompleted) TextDisabled else TextPrimary
                )
                Text(
                    DateTimeUtil.formatDateTime(reminder.triggerAtMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (reminder.isCompleted) TextDisabled else typeColor
                )
            }
            if (!reminder.isCompleted) {
                IconButton(onClick = { onComplete(reminder.id) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Check, "Done", tint = Success, modifier = Modifier.size(18.dp))
                }
            }
            IconButton(onClick = { onDelete(reminder.id) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, "Delete", tint = TextDisabled, modifier = Modifier.size(18.dp))
            }
        }
    }
}
