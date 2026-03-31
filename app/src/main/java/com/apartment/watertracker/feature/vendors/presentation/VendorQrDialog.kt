package com.apartment.watertracker.feature.vendors.presentation

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.apartment.watertracker.domain.model.Vendor

@Composable
fun VendorQrDialog(
    vendor: Vendor,
    onShareClick: (Vendor) -> Unit,
    onPrintClick: (Vendor) -> Unit,
    onDismiss: () -> Unit,
) {
    val qrBitmap: Bitmap = remember(vendor.id, vendor.qrValue) {
        generateVendorQrBitmap(vendor)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "${vendor.supplierName} QR") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "${vendor.supplierName} QR",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                )
                Text(text = "Printable vendor QR for apartment-scoped scanning.")
                Text(text = vendor.qrValue)
            }
        },
        confirmButton = {
            Button(onClick = { onShareClick(vendor) }) {
                Text(text = "Share")
            }
        },
        dismissButton = {
            Column(
                modifier = Modifier.padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = { onPrintClick(vendor) }) {
                    Text(text = "Print")
                }
                Button(onClick = onDismiss) {
                    Text(text = "Close")
                }
            }
        },
    )
}
