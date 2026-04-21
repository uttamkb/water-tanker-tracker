package com.apartment.watertracker.feature.entries.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apartment.watertracker.core.ui.components.PrimaryScaffold
import com.apartment.watertracker.core.ui.components.PremiumCard
import com.apartment.watertracker.core.ui.components.StarRating
import com.apartment.watertracker.core.notifications.NotificationHelper
import androidx.compose.material3.SnackbarHostState
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
    val snackbarHostState = remember { SnackbarHostState() }
    var isAdvancedQualityExpanded by remember { mutableStateOf(false) }
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

    LaunchedEffect(uiState.locationError) {
        uiState.locationError?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    PrimaryScaffold(
        title = "New Tanker Entry",
        onBackClick = onBackClick,
        snackbarHostState = snackbarHostState
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PremiumCard(
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Vendor: ${uiState.vendor?.supplierName ?: "Loading..."}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Date/Time: ${
                            uiState.capturedAt.atZone(ZoneId.systemDefault()).format(formatter)
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                value = uiState.tdsInput,
                onValueChange = viewModel::updateTds,
                label = { Text(text = "TDS Level (PPM)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            PremiumCard(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isAdvancedQualityExpanded = !isAdvancedQualityExpanded }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Advanced Quality Metrics (Optional)",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = if (isAdvancedQualityExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = isAdvancedQualityExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.hardnessInput,
                                onValueChange = viewModel::updateHardness,
                                label = { Text(text = "Water Hardness (PPM)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = uiState.phInput,
                                onValueChange = viewModel::updatePh,
                                label = { Text(text = "pH Level") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = uiState.volumeInput,
                onValueChange = viewModel::updateVolume,
                label = { Text(text = "Volume (Liters)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
            
            PremiumCard(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Vendor Rating (Optional)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text("Water Quality", style = MaterialTheme.typography.bodyMedium)
                        StarRating(
                            rating = uiState.qualityRating,
                            onRatingChange = viewModel::updateQualityRating
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text("Timeliness", style = MaterialTheme.typography.bodyMedium)
                        StarRating(
                            rating = uiState.timelinessRating,
                            onRatingChange = viewModel::updateTimelinessRating
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text("Vehicle Hygiene", style = MaterialTheme.typography.bodyMedium)
                        StarRating(
                            rating = uiState.hygieneRating,
                            onRatingChange = viewModel::updateHygieneRating
                        )
                    }
                }
            }

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
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                onClick = { viewModel.saveEntry() },
                enabled = uiState.location != null && !uiState.isSaving,
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
