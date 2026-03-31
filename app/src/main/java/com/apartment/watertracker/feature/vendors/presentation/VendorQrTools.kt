package com.apartment.watertracker.feature.vendors.presentation

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.content.FileProvider
import androidx.print.PrintHelper
import com.apartment.watertracker.domain.model.Vendor
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream

fun generateVendorQrBitmap(vendor: Vendor, sizePx: Int = 900): Bitmap {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(
        vendor.qrValue,
        BarcodeFormat.QR_CODE,
        sizePx,
        sizePx,
        mapOf(EncodeHintType.MARGIN to 1),
    )

    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    for (x in 0 until sizePx) {
        for (y in 0 until sizePx) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
        }
    }
    return bitmap
}

fun shareVendorQr(context: Context, vendor: Vendor) {
    val qrBitmap = generateVendorQrPrintBitmap(vendor)
    val file = saveBitmapToCache(context, qrBitmap, "${vendor.id}-qr.png")
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "${vendor.supplierName} QR")
        putExtra(Intent.EXTRA_TEXT, "Vendor QR for ${vendor.supplierName}")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(shareIntent, "Share vendor QR"))
}

fun printVendorQr(context: Context, vendor: Vendor) {
    val printBitmap = generateVendorQrPrintBitmap(vendor)
    PrintHelper(context).apply {
        scaleMode = PrintHelper.SCALE_MODE_FIT
        colorMode = PrintHelper.COLOR_MODE_MONOCHROME
    }.printBitmap("${vendor.supplierName} QR", printBitmap)
}

private fun generateVendorQrPrintBitmap(vendor: Vendor): Bitmap {
    val canvasWidth = 1200
    val canvasHeight = 1600
    val qrSize = 900
    val qrBitmap = generateVendorQrBitmap(vendor, qrSize)
    val output = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    canvas.drawColor(Color.WHITE)

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 54f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 32f
        textAlign = Paint.Align.CENTER
    }

    canvas.drawText(vendor.supplierName, canvasWidth / 2f, 120f, titlePaint)
    canvas.drawBitmap(qrBitmap, (canvasWidth - qrSize) / 2f, 220f, null)
    canvas.drawText("Scan to open tanker entry", canvasWidth / 2f, 1180f, bodyPaint)
    canvas.drawText("Apartment: ${vendor.apartmentId}", canvasWidth / 2f, 1250f, bodyPaint)
    canvas.drawText("Vendor ID: ${vendor.id.take(8)}", canvasWidth / 2f, 1320f, bodyPaint)
    canvas.drawText(vendor.phoneNumber, canvasWidth / 2f, 1390f, bodyPaint)

    return output
}

private fun saveBitmapToCache(context: Context, bitmap: Bitmap, fileName: String): File {
    val sharedDir = File(context.cacheDir, "shared").apply { mkdirs() }
    val file = File(sharedDir, fileName)
    FileOutputStream(file).use { output ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    }
    return file
}
