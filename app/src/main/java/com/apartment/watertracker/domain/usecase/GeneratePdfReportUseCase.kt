package com.apartment.watertracker.domain.usecase

import android.content.Context
import com.apartment.watertracker.feature.reports.presentation.ReportEntryDetail
import com.apartment.watertracker.feature.reports.presentation.VendorMonthlySummary
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class GeneratePdfReportUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun execute(
        monthLabel: String,
        totalTankers: Int,
        totalVolumeLiters: Long,
        vendorSummaries: List<VendorMonthlySummary>,
        dailyEntries: Map<String, List<ReportEntryDetail>>
    ): File? {
        val fileName = "WaterTracker_Report_${monthLabel.replace(" ", "_")}.pdf"
        val file = File(context.cacheDir, fileName)

        try {
            val writer = PdfWriter(file)
            val pdf = PdfDocument(writer)
            val document = Document(pdf)

            // 1. Header
            val header = Paragraph("WaterTracker Monthly Report")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20f)
                .setBold()
            document.add(header)

            val subHeader = Paragraph("Month: $monthLabel")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(14f)
            document.add(subHeader)
            document.add(Paragraph("\n"))

            // 2. High-Level Summary
            document.add(Paragraph("Executive Summary").setBold().setFontSize(16f))
            document.add(Paragraph("Total Tankers Received: $totalTankers"))
            document.add(Paragraph("Total Volume: ${totalVolumeLiters / 1000}k Liters"))
            val totalSpend = vendorSummaries.sumOf { it.totalSpend }
            document.add(Paragraph("Estimated Monthly Spend: Rs. ${String.format(java.util.Locale.US, "%.0f", totalSpend)}"))
            document.add(Paragraph("\n"))

            // 3. Vendor Breakdown
            document.add(Paragraph("Vendor Breakdown").setBold().setFontSize(16f))
            val vendorTable = Table(UnitValue.createPercentArray(floatArrayOf(40f, 20f, 20f, 20f)))
                .useAllAvailableWidth()

            vendorTable.addHeaderCell(Cell().add(Paragraph("Vendor Name").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY))
            vendorTable.addHeaderCell(Cell().add(Paragraph("Tankers").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY))
            vendorTable.addHeaderCell(Cell().add(Paragraph("Volume (L)").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY))
            vendorTable.addHeaderCell(Cell().add(Paragraph("Est. Cost").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY))

            vendorSummaries.forEach { summary ->
                vendorTable.addCell(Cell().add(Paragraph(summary.vendorName)))
                vendorTable.addCell(Cell().add(Paragraph(summary.tankerCount.toString())))
                vendorTable.addCell(Cell().add(Paragraph(summary.totalVolumeLiters.toString())))
                vendorTable.addCell(Cell().add(Paragraph("Rs. ${String.format(java.util.Locale.US, "%.0f", summary.totalSpend)}")))
            }
            document.add(vendorTable)
            document.add(Paragraph("\n"))

            // 4. Daily Log
            document.add(Paragraph("Detailed Daily Log").setBold().setFontSize(16f))
            
            dailyEntries.forEach { (date, entries) ->
                document.add(Paragraph(date).setBold().setFontSize(12f).setBackgroundColor(ColorConstants.LIGHT_GRAY))
                
                val dailyTable = Table(UnitValue.createPercentArray(floatArrayOf(20f, 30f, 25f, 25f)))
                    .useAllAvailableWidth()
                    
                dailyTable.addHeaderCell(Cell().add(Paragraph("Time").setBold()))
                dailyTable.addHeaderCell(Cell().add(Paragraph("Vendor").setBold()))
                dailyTable.addHeaderCell(Cell().add(Paragraph("Volume").setBold()))
                dailyTable.addHeaderCell(Cell().add(Paragraph("Hardness/Status").setBold()))

                entries.forEach { entry ->
                    dailyTable.addCell(Cell().add(Paragraph(entry.timeString)))
                    dailyTable.addCell(Cell().add(Paragraph(entry.vendorName)))
                    dailyTable.addCell(Cell().add(Paragraph("${entry.volume} L")))
                    
                    val statusText = if (entry.isDuplicate) "DUPLICATE" else "${entry.hardness} PPM"
                    val statusCell = Cell().add(Paragraph(statusText))
                    if (entry.isDuplicate) {
                        statusCell.setFontColor(ColorConstants.RED)
                    }
                    dailyTable.addCell(statusCell)
                }
                document.add(dailyTable)
                document.add(Paragraph("\n"))
            }

            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
