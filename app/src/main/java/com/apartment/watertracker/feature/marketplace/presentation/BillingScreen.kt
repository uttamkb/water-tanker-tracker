package com.apartment.watertracker.feature.marketplace.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apartment.watertracker.core.ui.components.PremiumCard
import com.apartment.watertracker.core.ui.components.PrimaryScaffold
import com.apartment.watertracker.domain.model.Invoice
import com.apartment.watertracker.domain.model.InvoiceStatus
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@Composable
fun BillingScreen(
    onBackClick: () -> Unit,
    viewModel: BillingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val formatter = DateTimeFormatter.ofPattern("dd MMM, yyyy")
    val activityContext = androidx.activity.compose.LocalActivity.current

    LaunchedEffect(Unit) {
        viewModel.shareFileEvent.collect { file ->
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Invoice PDF"))
        }
    }

    if (uiState.showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDialog() },
            title = { Text("Success") },
            text = { Text(uiState.successMessage) },
            confirmButton = {
                Button(onClick = { viewModel.dismissDialog() }) {
                    Text("OK")
                }
            }
        )
    }

    if (uiState.invoiceToMarkOffline != null) {
        var referenceInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.cancelOfflinePayment() },
            title = { Text("Mark Paid Offline") },
            text = {
                Column {
                    Text(
                        "Are you sure you want to mark ${uiState.invoiceToMarkOffline.vendorName}'s invoice as paid outside the app?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = referenceInput,
                        onValueChange = { referenceInput = it },
                        label = { Text("Bank UTR / Cheque No. (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.submitOfflinePayment(referenceInput) }) {
                    Text("Confirm Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelOfflinePayment() }) {
                    Text("Cancel")
                }
            }
        )
    }

    PrimaryScaffold(
        title = "Vendor Billing",
        onBackClick = onBackClick
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PremiumCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Auto-Invoicing",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Generate monthly statements based on the actual deliveries tracked in the system. (Stub logic for MVP)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = viewModel::generateMonthlyInvoices,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (uiState.isLoading) "Generating..." else "Generate Current Month Invoices")
                        }
                    }
                }
            }

            if (uiState.invoices.isNotEmpty()) {
                item {
                    Text(
                        text = "Invoices & Ledgers",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                items(uiState.invoices) { invoice ->
                    InvoiceItem(
                        invoice = invoice,
                        formatter = formatter,
                        onPayClick = { 
                            activityContext?.let { activity ->
                                viewModel.initiatePayment(invoice, activity)
                            }
                        },
                        onMarkPaidOffline = {
                            viewModel.initiateOfflinePayment(invoice)
                        },
                        onShareClick = {
                            viewModel.shareInvoice(invoice)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun InvoiceItem(
    invoice: Invoice,
    formatter: DateTimeFormatter,
    onPayClick: () -> Unit,
    onMarkPaidOffline: () -> Unit,
    onShareClick: () -> Unit
) {
    PremiumCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = invoice.vendorName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "₹${invoice.totalAmount.toInt()}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
            }

            Text(
                text = "Billing Month: ${invoice.billingMonth} | Due: ${invoice.dueDate.atZone(ZoneId.systemDefault()).format(formatter)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Deliveries", style = MaterialTheme.typography.labelSmall)
                    Text("${invoice.deliveryCount}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Total Volume", style = MaterialTheme.typography.labelSmall)
                    Text("${invoice.totalLiters}L", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                    Text("Status", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = invoice.status.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (invoice.status == InvoiceStatus.PAID) 
                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }

            if (invoice.status != InvoiceStatus.PAID) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onPayClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Pay via App")
                    }
                    Button(
                        onClick = onMarkPaidOffline,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("Mark Paid (Offline)")
                    }
                }
            } else if (invoice.paymentReference != null) {
                Text(
                    text = "Transaction ID: ${invoice.paymentReference}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
