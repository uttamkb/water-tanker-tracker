package com.apartment.watertracker.feature.marketplace.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apartment.watertracker.core.ui.components.PremiumCard
import com.apartment.watertracker.core.ui.components.PrimaryScaffold
import com.apartment.watertracker.domain.model.Bid
import com.apartment.watertracker.domain.model.BidStatus
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun BidsScreen(
    onBackClick: () -> Unit,
    viewModel: BidsViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val formatter = DateTimeFormatter.ofPattern("dd MMM, hh:mm a")

    PrimaryScaffold(
        title = "Vendor Bids",
        onBackClick = onBackClick
    ) { paddingValues ->
        if (uiState.bids.isEmpty() && !uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No bids received yet.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.bids) { bid ->
                    BidItem(
                        bid = bid,
                        formatter = formatter,
                        onAccept = { viewModel.acceptBid(bid.id) },
                        onReject = { viewModel.rejectBid(bid.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BidItem(
    bid: Bid,
    formatter: DateTimeFormatter,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    PremiumCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = bid.vendorName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₹${bid.price.toInt()}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
            }

            Text(
                text = "ETA: ${bid.estimatedArrival.atZone(ZoneId.systemDefault()).format(formatter)}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (bid.notes != null) {
                Text(
                    text = bid.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (bid.status == BidStatus.PENDING) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Accept")
                    }
                    Button(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Decline")
                    }
                }
            } else {
                Text(
                    text = "Status: ${bid.status.name}",
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (bid.status == BidStatus.ACCEPTED) 
                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
