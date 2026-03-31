package com.apartment.watertracker.feature.admin.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllApartmentsScreen(
    viewModel: AllApartmentsViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    LaunchedEffect(Unit) { viewModel.clearError() }

    PrimaryScaffold(title = "All Apartments (Owner)") { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            uiState.errorMessage?.let { msg ->
                item {
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            items(uiState.apartments, key = { it.id }) { apartment ->
                var status = remember { mutableStateOf(apartment.subscriptionStatus) }
                var expiryMillis = remember { mutableStateOf(apartment.subscriptionExpiresAt?.toEpochMilli()) }
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = expiryMillis.value,
                )
                var showDatePicker = remember { mutableStateOf(false) }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(apartment.name, style = MaterialTheme.typography.titleMedium)
                        val expiryLabel = expiryMillis.value?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).format(formatter)
                        } ?: "No expiry"
                        Text(
                            text = "Status: ${status.value} • Expires: $expiryLabel",
                            color = if (apartment.isSubscriptionActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        SingleChoiceSegmentedButtonRow {
                            SegmentedButton(
                                selected = status.value.uppercase() == "ACTIVE",
                                onClick = { status.value = "ACTIVE" },
                                shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
                            ) { Text("Active") }
                            SegmentedButton(
                                selected = status.value.uppercase() != "ACTIVE",
                                onClick = { status.value = "INACTIVE" },
                                shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
                            ) { Text("Inactive") }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = expiryLabel,
                                onValueChange = {},
                                enabled = false,
                                label = { Text("Expiry date") },
                                modifier = Modifier.weight(1f),
                            )
                            Button(onClick = { showDatePicker.value = true }) { Text("Pick date") }
                            Button(onClick = { expiryMillis.value = null }) { Text("Clear") }
                        }
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                viewModel.updateSubscription(
                                    apartmentId = apartment.id,
                                    status = status.value,
                                    expiresMillis = expiryMillis.value,
                                )
                            },
                            enabled = !uiState.isSaving,
                        ) {
                            Text(if (uiState.isSaving) "Saving…" else "Save")
                        }
                    }
                }

                if (showDatePicker.value) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker.value = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    expiryMillis.value = datePickerState.selectedDateMillis
                                    showDatePicker.value = false
                                },
                            ) { Text("Set") }
                        },
                        dismissButton = { TextButton(onClick = { showDatePicker.value = false }) { Text("Cancel") } },
                    ) {
                        DatePicker(state = datePickerState, showModeToggle = false)
                    }
                }
            }
        }
    }
}
