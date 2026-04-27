package com.apartment.watertracker.feature.entries.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.apartment.watertracker.core.ui.components.PremiumCard
import com.apartment.watertracker.core.ui.components.PrimaryScaffold

/**
 * Dedicated Gate Entry Screen for the Security Guard Persona.
 * Adheres strictly to the "Day 0" Playbook Journey.
 */
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apartment.watertracker.feature.scan.presentation.CameraQrScannerView

@Composable
fun GateEntryScreen(
    onBackClick: () -> Unit,
    viewModel: GateEntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.entrySaved) {
        if (uiState.entrySaved) {
            // Navigation or snackbar can be triggered here
            viewModel.resetState()
        }
    }

    PrimaryScaffold(
        title = "Gate Entry",
        onBackClick = onBackClick
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // 1. Prominent 'Scan QR' button
            if (uiState.vendor == null && !uiState.isScanning) {
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { viewModel.startScanning() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Scan Tanker QR", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            // Real Camera Integration
            if (uiState.isScanning) {
                PremiumCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CameraQrScannerView(
                            modifier = Modifier.fillMaxSize(),
                            scanningEnabled = true,
                            onQrDetected = { viewModel.onQrScanned(it) },
                            torchEnabled = false
                        )
                        
                        Text(
                            text = "Align QR Code within the frame",
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
            }

            // 2. The Entry Form (Vendor, Capacity, TDS)
            uiState.vendor?.let { vendor ->
                PremiumCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Verify Tanker Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        
                        OutlinedTextField(
                            value = vendor.supplierName,
                            onValueChange = {},
                            label = { Text("Vendor") },
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = "${vendor.defaultCapacityLiters} Liters",
                            onValueChange = {},
                            label = { Text("Standard Capacity") },
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Mandatory TDS Level Input
                        OutlinedTextField(
                            value = uiState.tdsLevel,
                            onValueChange = { viewModel.updateTdsLevel(it) },
                            label = { Text("TDS Level (PPM) - Mandatory*") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = uiState.tdsLevel.isBlank() && uiState.isSaving,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 3. 'Save Entry' button
                        Button(
                            onClick = { viewModel.saveEntry() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = uiState.gpsLocation != null && !uiState.isSaving
                        ) {
                            Text(if (uiState.isSaving) "Saving..." else "Save Entry & Log GPS")
                        }

                        if (uiState.gpsLocation != null) {
                            Text(
                                text = "📍 GPS Verified: ${uiState.gpsLocation!!.accuracyMeters}m precision", 
                                style = MaterialTheme.typography.labelSmall, 
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else if (uiState.isCapturingLocation) {
                            Text("⏳ Capturing GPS location...", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // 5. 'Possible Duplicate' UI Alert
        if (uiState.showDuplicateAlert) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDuplicateAlert() },
                title = { Text("Possible Duplicate Detected") },
                text = { 
                    Text("A tanker from ${uiState.vendor?.supplierName} was already scanned at ${uiState.previousEntryTime}. Are you sure this is a new physical delivery?", 
                    style = MaterialTheme.typography.bodyMedium) 
                },
                confirmButton = {
                    Button(onClick = { viewModel.saveEntry(forceSave = true) }) {
                        Text("Confirm & Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDuplicateAlert() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
