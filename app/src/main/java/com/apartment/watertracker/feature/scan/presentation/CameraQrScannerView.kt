package com.apartment.watertracker.feature.scan.presentation

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executors
import kotlin.coroutines.resume

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

    // Root layout holding both the camera preview and the overlay
    val containerView = remember {
        FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
    }

    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val overlayView = remember {
        QrScannerOverlayView(context).apply {
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
    }

    LaunchedEffect(Unit) {
        if (containerView.childCount == 0) {
            containerView.addView(previewView)
            containerView.addView(overlayView)
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
        factory = { containerView },
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

/**
 * A custom View that draws a semi-transparent dark background with a clear square hole in the middle
 * to indicate the scanning area to the user.
 */
private class QrScannerOverlayView(context: Context) : View(context) {
    private val backgroundPaint = Paint().apply {
        color = Color.parseColor("#99000000") // 60% black
        style = Paint.Style.FILL
    }

    private val transparentPaint = Paint().apply {
        color = Color.TRANSPARENT
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        isAntiAlias = true
    }

    private val borderPaint = Paint().apply {
        color = Color.parseColor("#1976D2") // Primary blue
        style = Paint.Style.STROKE
        strokeWidth = 6f * resources.displayMetrics.density
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }

    // Preallocate RectF objects to avoid creating them during onDraw (prevents garbage collection stutter)
    private val transparentRect = RectF()
    private val topLeftArc = RectF()
    private val topRightArc = RectF()
    private val bottomLeftArc = RectF()
    private val bottomRightArc = RectF()
    
    private val cornerRadius = 16f * resources.displayMetrics.density
    private val borderLength = 32f * resources.displayMetrics.density

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // Draw the dark background overlay over the entire view
        canvas.drawRect(0f, 0f, w, h, backgroundPaint)

        // Calculate the square scanning area (70% of width, centered)
        val boxSize = w * 0.7f
        val left = (w - boxSize) / 2
        val top = (h - boxSize) / 2
        val right = left + boxSize
        val bottom = top + boxSize
        
        transparentRect.set(left, top, right, bottom)
        topLeftArc.set(left, top, left + cornerRadius * 2, top + cornerRadius * 2)
        topRightArc.set(right - cornerRadius * 2, top, right, top + cornerRadius * 2)
        bottomLeftArc.set(left, bottom - cornerRadius * 2, left + cornerRadius * 2, bottom)
        bottomRightArc.set(right - cornerRadius * 2, bottom - cornerRadius * 2, right, bottom)

        // "Punch a hole" through the dark background
        canvas.drawRoundRect(transparentRect, cornerRadius, cornerRadius, transparentPaint)

        // Draw the 4 stylized corner borders
        
        // Top-Left
        canvas.drawLine(left, top + cornerRadius, left, top + borderLength, borderPaint)
        canvas.drawLine(left + cornerRadius, top, left + borderLength, top, borderPaint)
        canvas.drawArc(topLeftArc, 180f, 90f, false, borderPaint)

        // Top-Right
        canvas.drawLine(right, top + cornerRadius, right, top + borderLength, borderPaint)
        canvas.drawLine(right - cornerRadius, top, right - borderLength, top, borderPaint)
        canvas.drawArc(topRightArc, 270f, 90f, false, borderPaint)

        // Bottom-Left
        canvas.drawLine(left, bottom - cornerRadius, left, bottom - borderLength, borderPaint)
        canvas.drawLine(left + cornerRadius, bottom, left + borderLength, bottom, borderPaint)
        canvas.drawArc(bottomLeftArc, 90f, 90f, false, borderPaint)

        // Bottom-Right
        canvas.drawLine(right, bottom - cornerRadius, right, bottom - borderLength, borderPaint)
        canvas.drawLine(right - cornerRadius, bottom, right - borderLength, bottom, borderPaint)
        canvas.drawArc(bottomRightArc, 0f, 90f, false, borderPaint)
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
