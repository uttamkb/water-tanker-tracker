package com.apartment.watertracker.feature.scan.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FlashlightOff
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apartment.watertracker.core.ui.components.PrimaryScaffold

@Composable
fun ScanScreen(
    onVendorScanned: (String) -> Unit,
    viewModel: ScanViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val hasCameraPermission = context.hasCameraPermission()
    var torchAvailable by remember { mutableStateOf(false) }
    var torchOn by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (!granted) {
            viewModel.clearScanError()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(uiState.pendingVendorId) {
        uiState.pendingVendorId?.let { vendorId ->
            viewModel.consumeNavigation()
            onVendorScanned(vendorId)
        }
    }

    PrimaryScaffold(title = "Scan Vendor QR") { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Text(
                    text = "Scan the printed QR at the supply point.",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            item {
                Text(
                    text = "After scan, vendor is verified and entry form opens automatically.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .padding(10.dp),
                    ) {
                        if (hasCameraPermission) {
                            CameraQrScannerView(
                                modifier = Modifier.fillMaxSize(),
                                scanningEnabled = !uiState.isResolvingScan && uiState.pendingVendorId == null,
                                onQrDetected = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.onQrScanned(it)
                                },
                                torchEnabled = torchOn,
                                onTorchAvailabilityChanged = { available ->
                                    torchAvailable = available
                                    if (!available) torchOn = false
                                },
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                                    Text(text = "Allow Camera Permission")
                                }
                            }
                        }
                    }
                }
            }
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    ),
                ) {
                    Text(
                        text = when {
                        uiState.isResolvingScan -> "Resolving scanned QR..."
                        uiState.lastScannedQrValue != null -> "Last scanned QR: ${uiState.lastScannedQrValue}"
                        else -> "Scanner ready"
                        },
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            if (uiState.isResolvingScan) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        ),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 3.dp,
                            )
                            Column {
                                Text("Opening entry form…", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "Hold steady, we’ll auto-open when resolved.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
            uiState.scanError?.let { errorMessage ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Button(onClick = viewModel::clearScanError) {
                                Text("Retry scan")
                            }
                        }
                    }
                }
            }
            item {
                Text(
                    text = "Manual fallback",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            item {
                OutlinedTextField(
                    value = uiState.manualQrInput,
                    onValueChange = viewModel::updateManualQrInput,
                    label = { Text("Enter QR text") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = viewModel::submitManualQr) {
                        Text("Submit QR")
                    }
                    if (torchAvailable) {
                        IconButton(onClick = { torchOn = !torchOn }) {
                            Icon(
                                imageVector = if (torchOn) Icons.Outlined.FlashlightOff else Icons.Outlined.FlashlightOn,
                                contentDescription = "Toggle flashlight",
                            )
                        }
                    }
                }
            }
            item { HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.4f)) }
            items(uiState.vendors, key = { it.id }) { vendor ->
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.onQrScanned(vendor.qrValue) },
                ) {
                    Text(text = "Use ${vendor.supplierName}")
                }
            }
        }
    }
}

private fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA,
    ) == PackageManager.PERMISSION_GRANTED
