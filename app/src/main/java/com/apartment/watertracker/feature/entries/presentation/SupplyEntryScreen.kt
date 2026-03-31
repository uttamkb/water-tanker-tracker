package com.apartment.watertracker.feature.entries.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apartment.watertracker.core.ui.components.PrimaryScaffold
import com.apartment.watertracker.core.notifications.NotificationHelper
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SupplyEntryScreen(
    onEntrySaved: () -> Unit,
    onBackClick: (() -> Unit)? = null,
    viewModel: SupplyEntryViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            viewModel.recaptureLocation()
        } else {
            viewModel.onLocationPermissionDenied()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(uiState.entrySaved) {
        if (uiState.entrySaved) {
            viewModel.consumeEntrySaved()
            onEntrySaved()
        }
    }

    LaunchedEffect(Unit) {
        if (!context.hasLocationPermission()) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    LaunchedEffect(uiState.duplicateWarning) {
        val warning = uiState.duplicateWarning ?: return@LaunchedEffect
        NotificationHelper.showDuplicateWarning(
            context = context,
            title = "Possible duplicate tanker entry",
            message = "Previous entry at ${warning.previousCapturedAt}.",
        )
    }

    PrimaryScaffold(
        title = "New Tanker Entry",
        onBackClick = onBackClick
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Vendor: ${uiState.vendor?.supplierName ?: "Loading..."}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "Date/Time: ${
                            uiState.capturedAt.atZone(ZoneId.systemDefault()).format(formatter)
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = when {
                            uiState.location != null -> "GPS: ${uiState.location.label} (${uiState.location.accuracyMeters} m)"
                            uiState.isCapturingLocation -> "GPS: capturing..."
                            uiState.locationError != null -> "GPS: ${uiState.locationError}"
                            else -> "GPS: waiting..."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (uiState.location != null) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
            OutlinedTextField(
                value = uiState.hardnessInput,
                onValueChange = viewModel::updateHardness,
                label = { Text(text = "Water Hardness") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.vehicleNumber,
                onValueChange = viewModel::updateVehicleNumber,
                label = { Text(text = "Vehicle Number (Optional)") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.remarks,
                onValueChange = viewModel::updateRemarks,
                label = { Text(text = "Remarks (Optional)") },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Photo is optional and currently skipped to control storage cost.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = viewModel::recaptureLocation,
            ) {
                Text(text = "Recapture GPS")
            }
            if (!context.hasLocationPermission()) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
                    },
                ) {
                    Text(text = "Allow Location Permission")
                }
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.saveEntry() },
                enabled = uiState.hardnessInput.isNotBlank() && uiState.location != null && !uiState.isSaving,
            ) {
                Text(text = if (uiState.isSaving) "Saving..." else "Save Entry")
            }
        }

        val duplicateWarning = uiState.duplicateWarning
        if (duplicateWarning != null) {
            AlertDialog(
                onDismissRequest = viewModel::dismissDuplicateWarning,
                title = { Text(text = "Possible Duplicate Entry") },
                text = {
                    Text(
                        text = buildString {
                            append("Previous entry was recorded at ${duplicateWarning.previousCapturedAt}.")
                            duplicateWarning.previousVehicleNumber?.let {
                                append(" Vehicle: $it.")
                            }
                        },
                    )
                },
                confirmButton = {
                    Button(onClick = { viewModel.saveEntry(forceSave = true) }) {
                        Text(text = "Save Anyway")
                    }
                },
                dismissButton = {
                    Button(onClick = viewModel::dismissDuplicateWarning) {
                        Text(text = "Cancel")
                    }
                },
            )
        }
    }
}

private fun Context.hasLocationPermission(): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}
