package com.apartment.watertracker.domain.usecase

import android.content.Context
import com.apartment.watertracker.domain.model.Invoice
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.UnitValue
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class GenerateInvoicePdfUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    fun execute(invoice: Invoice): File {
        val fileName = "Invoice_${invoice.vendorName.replace(" ", "_")}_${invoice.billingMonth}.pdf"
        val file = File(context.cacheDir, fileName)
        
        val writer = PdfWriter(file)
        val pdf = PdfDocument(writer)
        val document = Document(pdf)

        // Header
        document.add(Paragraph("WATER TRACKER INVOICE").setBold().setFontSize(20f))
        document.add(Paragraph("Generated on: ${java.time.Instant.now().atZone(ZoneId.systemDefault()).format(formatter)}"))
        document.add(Paragraph("\n"))

        // Invoice Info
        document.add(Paragraph("Invoice ID: ${invoice.id}"))
        document.add(Paragraph("Billing Month: ${invoice.billingMonth}"))
        document.add(Paragraph("Due Date: ${invoice.dueDate.atZone(ZoneId.systemDefault()).format(formatter)}"))
        document.add(Paragraph("\n"))

        // Details Table
        val table = Table(UnitValue.createPointArray(floatArrayOf(200f, 200f)))
        table.width = UnitValue.createPercentValue(100f)

        table.addCell("Vendor Name")
        table.addCell(invoice.vendorName)

        table.addCell("Total Deliveries")
        table.addCell(invoice.deliveryCount.toString())

        table.addCell("Total Volume")
        table.addCell("${invoice.totalLiters} Liters")

        table.addCell("Total Amount Due")
        table.addCell("INR ${invoice.totalAmount}")

        document.add(table)

        // Footer
        document.add(Paragraph("\n"))
        document.add(Paragraph("Please settle the payment before the due date.").setItalic())
        
        document.close()
        return file
    }
}
