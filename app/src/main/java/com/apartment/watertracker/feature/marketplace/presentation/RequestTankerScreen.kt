package com.apartment.watertracker.feature.marketplace.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apartment.watertracker.core.ui.components.PremiumCard
import com.apartment.watertracker.core.ui.components.PrimaryScaffold
import com.apartment.watertracker.domain.model.RequestUrgency
import com.apartment.watertracker.domain.model.TankerRequest
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun RequestTankerScreen(
    onBackClick: () -> Unit,
    onViewBids: (String) -> Unit,
    viewModel: RequestTankerViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }
    val formatter = DateTimeFormatter.ofPattern("dd MMM, hh:mm a")

    LaunchedEffect(uiState.showSuccess) {
        if (uiState.showSuccess) {
            snackbarHostState.showSnackbar("Tanker request broadcasted to vendors!")
            viewModel.dismissSuccess()
        }
    }

    PrimaryScaffold(
        title = "Request Tanker",
        onBackClick = onBackClick,
        snackbarHostState = snackbarHostState
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                PremiumCard(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Broadcast New Requirement",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        OutlinedTextField(
                            value = uiState.quantityLiters,
                            onValueChange = viewModel::updateQuantity,
                            label = { Text("Quantity (Liters)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Text("Urgency", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RequestUrgency.values().forEach { urgency ->
                                FilterChip(
                                    selected = uiState.urgency == urgency,
                                    onClick = { viewModel.updateUrgency(urgency) },
                                    label = { Text(urgency.name) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = uiState.notes,
                            onValueChange = viewModel::updateNotes,
                            label = { Text("Additional Notes (e.g. Gate 2)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = viewModel::submitRequest,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSaving && uiState.quantityLiters.isNotBlank()
                        ) {
                            Text(if (uiState.isSaving) "Broadcasting..." else "Broadcast Request")
                        }
                    }
                }
            }

            if (uiState.myRequests.isNotEmpty()) {
                item {
                    Text(
                        text = "Your Recent Requests",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(uiState.myRequests) { request ->
                    RequestItem(request, formatter, onClick = { onViewBids(request.id) })
                }
            }
        }
    }
}

@Composable
private fun RequestItem(request: TankerRequest, formatter: DateTimeFormatter, onClick: () -> Unit) {
    PremiumCard(onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${request.quantityLiters} Liters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = request.status.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (request.status == com.apartment.watertracker.domain.model.RequestStatus.OPEN) 
                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
            
            Text(
                text = "Requested: ${request.createdAt.atZone(ZoneId.systemDefault()).format(formatter)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (request.notes != null) {
                Text(
                    text = request.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
            
            Text(
                text = "${request.bidsCount} Vendor Bids Received",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
