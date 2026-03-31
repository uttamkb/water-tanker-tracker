package com.apartment.watertracker.feature.admin.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apartment.watertracker.core.ui.components.PrimaryScaffold
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApartmentSwitchScreen(
    viewModel: ApartmentSwitchViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    LaunchedEffect(Unit) {
        viewModel.clearError()
    }

    PrimaryScaffold(title = "Apartments") { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text("Create apartment", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = uiState.newApartmentName,
                            onValueChange = viewModel::updateNewApartmentName,
                            label = { Text("Apartment name") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = viewModel::createApartment,
                            enabled = uiState.newApartmentName.isNotBlank() && !uiState.isSaving,
                        ) {
                            Text(text = if (uiState.isSaving) "Creating..." else "Create & switch")
                        }
                        uiState.errorMessage?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
            item {
                Text("Your apartments", style = MaterialTheme.typography.titleLarge)
            }
            items(uiState.apartments, key = { it.id }) { apartment ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(apartment.name, style = MaterialTheme.typography.titleMedium)
                        val expiry = apartment.subscriptionExpiresAt
                            ?.atZone(ZoneId.systemDefault())
                            ?.format(formatter)
                        Text(
                            text = "Status: ${apartment.subscriptionStatus} " +
                                (expiry?.let { "(until $it)" } ?: ""),
                            color = if (apartment.isSubscriptionActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { viewModel.switchApartment(apartment.id) },
                        ) {
                            Text("Switch to this apartment")
                        }
                        SubscriptionControls(
                            status = uiState.subscriptionStatus,
                            selectedExpiryMillis = uiState.subscriptionExpiryMillis,
                            onStatusChange = viewModel::updateSubscriptionStatus,
                            onExpiryChangeMillis = viewModel::updateSubscriptionExpiryMillis,
                            onSave = { viewModel.updateSubscription(apartment.id) },
                            isSaving = uiState.isSaving,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubscriptionControls(
    status: String,
    selectedExpiryMillis: Long?,
    onStatusChange: (String) -> Unit,
    onExpiryChangeMillis: (Long?) -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean,
) {
    @OptIn(ExperimentalMaterial3Api::class)
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedExpiryMillis,
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Subscription", style = MaterialTheme.typography.titleMedium)
        SingleChoiceSegmentedButtonRow {
            SegmentedButton(
                selected = status.uppercase() == "ACTIVE",
                onClick = { onStatusChange("ACTIVE") },
                shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
            ) { Text("Active") }
            SegmentedButton(
                selected = status.uppercase() != "ACTIVE",
                onClick = { onStatusChange("INACTIVE") },
                shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
            ) { Text("Inactive") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = selectedExpiryMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().toString() }
                    ?: "No expiry",
                onValueChange = {},
                enabled = false,
                label = { Text("Expiry date") },
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = { showDatePicker = true },
            ) {
                Text("Pick date")
            }
            Button(
                onClick = { onExpiryChangeMillis(null) },
                enabled = selectedExpiryMillis != null,
            ) { Text("Clear") }
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onSave,
            enabled = !isSaving,
        ) {
            Text(if (isSaving) "Saving…" else "Save subscription")
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        onExpiryChangeMillis(millis)
                        showDatePicker = false
                    },
                ) { Text("Set") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = datePickerState, showModeToggle = false)
        }
    }
}
