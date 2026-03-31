package com.apartment.watertracker.feature.scan.presentation

import android.annotation.SuppressLint
import android.media.Image
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

class QrScannerAnalyzer(
    private val barcodeScanner: BarcodeScanner,
    private val onQrDetected: (String) -> Unit,
) : ImageAnalysis.Analyzer {

    private val isProcessing = AtomicBoolean(false)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (!isProcessing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        val mediaImage: Image? = imageProxy.image
        if (mediaImage == null) {
            isProcessing.set(false)
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees,
        )

        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull { barcode -> barcode.format == Barcode.FORMAT_QR_CODE }
                    ?.rawValue
                    ?.takeIf { it.isNotBlank() }
                    ?.let(onQrDetected)
            }
            .addOnCompleteListener {
                isProcessing.set(false)
                imageProxy.close()
            }
    }
}
