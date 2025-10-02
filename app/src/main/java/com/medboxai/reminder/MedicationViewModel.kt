package com.medboxai.reminder

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.medboxai.reminder.data.Medication
import com.medboxai.reminder.data.MedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MedicationViewModel(private val repository: MedicationRepository) : ViewModel() {
    private val formatter = DateTimeFormatter.ofPattern("hh:mm a")

    private val _uiState = MutableStateFlow(MedicationUiState())
    val uiState: StateFlow<MedicationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeMedications().collect { medications ->
                _uiState.update { state ->
                    state.copy(
                        medications = medications.map { it.toUi(formatter) }
                    )
                }
            }
        }
    }

    fun showAddDialog(show: Boolean) {
        _uiState.update { it.copy(isAddDialogVisible = show) }
    }

    fun saveMedication(
        id: Long = Medication.UNSAVED_ID,
        name: String,
        dosage: String,
        instructions: String,
        time: LocalTime,
        isEnabled: Boolean
    ) {
        viewModelScope.launch {
            repository.upsertMedication(id, name, dosage, instructions, time, isEnabled)
        }
    }

    fun toggleMedication(id: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleMedication(id, isEnabled)
        }
    }

    fun deleteMedication(id: Long) {
        viewModelScope.launch {
            repository.deleteMedication(id)
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(MedicationViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return MedicationViewModel(MedicationRepository.create(context.applicationContext)) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}

data class MedicationUiState(
    val medications: List<MedicationItem> = emptyList(),
    val isAddDialogVisible: Boolean = false
)

data class MedicationItem(
    val id: Long,
    val name: String,
    val dosage: String,
    val instructions: String,
    val timeLabel: String,
    val isEnabled: Boolean,
    val nextDoseLabel: String
)

private fun Medication.toUi(formatter: DateTimeFormatter): MedicationItem {
    val nextDose = nextDoseDateTime()
    return MedicationItem(
        id = id,
        name = name,
        dosage = dosage,
        instructions = instructions,
        timeLabel = time.format(formatter),
        isEnabled = isEnabled,
        nextDoseLabel = nextDose.format(DateTimeFormatter.ofPattern("EEE, MMM d 'at' hh:mm a"))
    )
}
