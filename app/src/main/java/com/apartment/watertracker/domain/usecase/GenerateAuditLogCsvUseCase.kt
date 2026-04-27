package com.apartment.watertracker.domain.usecase

import android.content.Context
import com.apartment.watertracker.domain.model.SupplyEntry
import com.apartment.watertracker.domain.repository.VendorRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.File
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class GenerateAuditLogCsvUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vendorRepository: VendorRepository
) {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    suspend fun execute(entries: List<SupplyEntry>, month: String): File {
        val fileName = "Water_Audit_Log_$month.csv"
        val file = File(context.cacheDir, fileName)
        val vendors = vendorRepository.observeVendors().first()
        
        file.bufferedWriter().use { writer ->
            // CSV Header
            writer.write("ID,Timestamp,Vendor,Volume (L),TDS (PPM),pH,Hardness,Vehicle No,Operator,Duplicate,Latitude,Longitude\n")
            
            entries.forEach { entry ->
                val vendorName = vendors.find { it.id == entry.vendorId }?.supplierName ?: "Unknown"
                val timestamp = entry.capturedAt.atZone(ZoneId.systemDefault()).format(dateTimeFormatter)
                
                val line = buildString {
                    append("${entry.id},")
                    append("$timestamp,")
                    append("${vendorName.replace(",", " ")},")
                    append("${entry.volumeLiters},")
                    append("${entry.tdsPpm ?: "N/A"},")
                    append("${entry.phLevel ?: "N/A"},")
                    append("${entry.hardnessPpm},")
                    append("${entry.vehicleNumber ?: "N/A"},")
                    append("${entry.createdByUserId},")
                    append("${entry.duplicateFlag},")
                    append("${entry.latitude},")
                    append("${entry.longitude}")
                }
                writer.write("$line\n")
            }
        }
        return file
    }
}
