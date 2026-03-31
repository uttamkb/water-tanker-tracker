package com.apartment.watertracker.feature.scan.presentation

import android.content.Context
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

@Composable
fun CameraQrScannerView(
    modifier: Modifier = Modifier,
    scanningEnabled: Boolean,
    onQrDetected: (String) -> Unit,
    torchEnabled: Boolean,
    onTorchAvailabilityChanged: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build(),
        )
    }
    var boundCamera: Camera? by remember { mutableStateOf(null) }

    AndroidView(
        modifier = modifier,
        factory = { previewView },
    )

    LaunchedEffect(scanningEnabled, lifecycleOwner) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()

        if (!scanningEnabled) return@LaunchedEffect

        val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(
                    cameraExecutor,
                    QrScannerAnalyzer(
                        barcodeScanner = barcodeScanner,
                        onQrDetected = onQrDetected,
                    ),
                )
            }

        val camera = cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageAnalysis,
        )
        boundCamera = camera
        onTorchAvailabilityChanged(camera.cameraInfo.hasFlashUnit())
        runCatching { camera.cameraControl.enableTorch(torchEnabled) }
    }

    DisposableEffect(Unit) {
        onDispose {
            barcodeScanner.close()
            cameraExecutor.shutdown()
            runCatching {
                val provider = ProcessCameraProvider.getInstance(context).get()
                provider.unbindAll()
            }
        }
    }

    LaunchedEffect(torchEnabled) {
        boundCamera?.cameraControl?.enableTorch(torchEnabled)
    }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCancellableCoroutine { continuation ->
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener(
            {
                continuation.resume(future.get())
            },
            ContextCompat.getMainExecutor(this),
        )
    }
