package com.apartment.watertracker.feature.reports.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apartment.watertracker.core.ui.components.PrimaryScaffold
import com.apartment.watertracker.core.ui.components.PremiumCard
import com.apartment.watertracker.core.ui.components.GlassSurface
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun EntryDetailScreen(
    onBackClick: () -> Unit,
    viewModel: EntryDetailViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")

    PrimaryScaffold(
        title = "Entry Details",
        onBackClick = onBackClick
    ) { paddingValues ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        } else if (uiState.entry != null && uiState.vendor != null) {
            val entry = uiState.entry
            val vendor = uiState.vendor

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card: Vendor & Timestamp
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = vendor.supplierName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = entry.capturedAt.atZone(ZoneId.systemDefault()).format(formatter),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (entry.duplicateFlag) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "⚠️ Flagged as Duplicate",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // Delivery Data Card
                Text(
                    text = "Delivery Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                PremiumCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailRow(label = "Vehicle Number", value = entry.vehicleNumber ?: "Not Provided")
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        DetailRow(label = "Volume Delivered", value = "${entry.volumeLiters} Liters")
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        DetailRow(label = "Water Hardness", value = "${entry.hardnessPpm} PPM")
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        DetailRow(label = "Remarks", value = entry.remarks ?: "None")
                    }
                }

                // Audit Data Card
                Text(
                    text = "Audit Log",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                PremiumCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailRow(label = "Operator ID", value = entry.createdByUserId.take(8) + "...")
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        DetailRow(label = "GPS Location", value = "Lat: ${entry.latitude.format(4)}\nLng: ${entry.longitude.format(4)}")
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        DetailRow(label = "GPS Accuracy", value = "±${entry.gpsAccuracyMeters.toInt()} meters")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.5f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)
