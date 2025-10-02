package com.medboxai.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medboxai.reminder.notifications.ReminderReceiver
import com.medboxai.reminder.ui.theme.MedBoxTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        setContent {
            MedBoxTheme {
                val context = LocalContext.current
                val viewModel: MedicationViewModel = viewModel(
                    factory = MedicationViewModel.provideFactory(context)
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                MedicationScreen(
                    state = uiState,
                    onAddMedication = { name, dosage, instructions, time, isEnabled ->
                        viewModel.saveMedication(
                            name = name,
                            dosage = dosage,
                            instructions = instructions,
                            time = time,
                            isEnabled = isEnabled
                        )
                    },
                    onToggleMedication = viewModel::toggleMedication,
                    onDeleteMedication = viewModel::deleteMedication,
                    onShowAddDialog = viewModel::showAddDialog
                )
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ReminderReceiver.CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_description)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationScreen(
    state: MedicationUiState,
    onAddMedication: (String, String, String, LocalTime, Boolean) -> Unit,
    onToggleMedication: (Long, Boolean) -> Unit,
    onDeleteMedication: (Long) -> Unit,
    onShowAddDialog: (Boolean) -> Unit
) {
    val showDialog = state.isAddDialogVisible

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = stringResource(R.string.app_name)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onShowAddDialog(true) }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (state.medications.isEmpty()) {
                Text(
                    text = stringResource(R.string.empty_state_message),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.medications) { item ->
                        MedicationCard(
                            item = item,
                            onToggle = { enabled -> onToggleMedication(item.id, enabled) },
                            onDelete = { onDeleteMedication(item.id) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddMedicationDialog(
            onDismiss = { onShowAddDialog(false) },
            onConfirm = { name, dosage, instructions, time, isEnabled ->
                onAddMedication(name, dosage, instructions, time, isEnabled)
                onShowAddDialog(false)
            }
        )
    }
}

@Composable
fun MedicationCard(
    item: MedicationItem,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.name, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (item.dosage.isNotBlank()) {
                        Text(text = item.dosage, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (item.instructions.isNotBlank()) {
                        Text(text = item.instructions, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.card_next_dose, item.nextDoseLabel),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                    Switch(checked = item.isEnabled, onCheckedChange = onToggle)
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.card_daily_time, item.timeLabel),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun AddMedicationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, LocalTime, Boolean) -> Unit
) {
    val nameState = remember { mutableStateOf(TextFieldValue()) }
    val dosageState = remember { mutableStateOf(TextFieldValue()) }
    val instructionsState = remember { mutableStateOf(TextFieldValue()) }
    val timeState = remember { mutableStateOf(LocalTime.now().withSecond(0).withNano(0)) }
    val enabledState = remember { mutableStateOf(true) }

    val context = LocalContext.current
    val formatter = remember { DateTimeFormatter.ofPattern("hh:mm a") }
    val timePickerDialog = rememberTimePickerDialog(
        context = context,
        timeState = timeState
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.dialog_add_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(
                    value = nameState.value,
                    onValueChange = { nameState.value = it },
                    label = { Text(stringResource(R.string.dialog_field_medication_name)) },
                    singleLine = true
                )
                TextField(
                    value = dosageState.value,
                    onValueChange = { dosageState.value = it },
                    label = { Text(stringResource(R.string.dialog_field_dosage)) },
                    singleLine = true
                )
                TextField(
                    value = instructionsState.value,
                    onValueChange = { instructionsState.value = it },
                    label = { Text(stringResource(R.string.dialog_field_instructions)) },
                    singleLine = false
                )
                Button(onClick = { timePickerDialog.show() }) {
                    Text(text = stringResource(R.string.dialog_field_time, timeState.value.format(formatter)))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.dialog_field_enable_label))
                    Switch(checked = enabledState.value, onCheckedChange = { enabledState.value = it })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (nameState.value.text.isBlank()) {
                    return@TextButton
                }
                onConfirm(
                    nameState.value.text.trim(),
                    dosageState.value.text.trim(),
                    instructionsState.value.text.trim(),
                    timeState.value,
                    enabledState.value
                )
            }) {
                Text(text = stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun rememberTimePickerDialog(
    context: android.content.Context,
    timeState: MutableState<LocalTime>
): android.app.TimePickerDialog {
    val currentTime = timeState.value
    return remember(context, currentTime) {
        android.app.TimePickerDialog(
            context,
            { _, hour, minute -> timeState.value = LocalTime.of(hour, minute) },
            currentTime.hour,
            currentTime.minute,
            false
        )
    }
}

@Preview
@Composable
private fun MedicationCardPreview() {
    MedBoxTheme {
        MedicationCard(
            item = MedicationItem(
                id = 1,
                name = "Amoxicillin",
                dosage = "500mg",
                instructions = "Take with food",
                timeLabel = "08:00 AM",
                isEnabled = true,
                nextDoseLabel = "Mon, Jan 1 at 08:00 AM"
            ),
            onToggle = {},
            onDelete = {}
        )
    }
}
