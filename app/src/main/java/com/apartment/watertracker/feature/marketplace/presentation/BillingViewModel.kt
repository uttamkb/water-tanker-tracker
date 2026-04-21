package com.apartment.watertracker.feature.marketplace.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apartment.watertracker.domain.model.Invoice
import com.apartment.watertracker.domain.repository.InvoiceRepository
import com.apartment.watertracker.domain.repository.VendorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import android.app.Activity
import com.razorpay.Checkout
import org.json.JSONObject
import com.apartment.watertracker.core.payment.PaymentEventBus
import com.apartment.watertracker.core.payment.PaymentResult

data class BillingUiState(
    val invoices: List<Invoice> = emptyList(),
    val isLoading: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val successMessage: String = "",
    val invoiceToMarkOffline: Invoice? = null
)

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val vendorRepository: VendorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BillingUiState())
    val uiState: StateFlow<BillingUiState> = _uiState.asStateFlow()

    init {
        loadInvoices()
        
        viewModelScope.launch {
            PaymentEventBus.paymentResults.collect { result ->
                when (result) {
                    is PaymentResult.Success -> {
                        markAsPaid(result.invoiceId, result.paymentId)
                        _uiState.update { 
                            it.copy(
                                showSuccessDialog = true, 
                                successMessage = "Payment Successful! Txn ID: ${result.paymentId}"
                            ) 
                        }
                    }
                    is PaymentResult.Error -> {
                        _uiState.update { 
                            it.copy(
                                showSuccessDialog = true, 
                                successMessage = "Payment Failed: ${result.description}"
                            ) 
                        }
                    }
                }
            }
        }
    }

    private fun loadInvoices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            invoiceRepository.observeInvoicesForApartment().collect { invoices ->
                _uiState.update { it.copy(invoices = invoices, isLoading = false) }
            }
        }
    }

    // Auto-generation stub. In reality, a Cloud Function triggers this on the 1st of every month.
    // For this MVP, we let the Admin click a button to "Generate current month invoices".
    fun generateMonthlyInvoices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
            
            // Get all active vendors
            val vendors = vendorRepository.observeVendors().first()
            vendors.forEach { vendor ->
                // Stub: Generating an invoice for each vendor.
                invoiceRepository.generateDraftInvoice(vendor.id, vendor.supplierName, currentMonth)
            }
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    showSuccessDialog = true,
                    successMessage = "Successfully generated invoices for $currentMonth"
                )
            }
        }
    }

    fun markAsPaid(invoiceId: String, transactionId: String) {
        viewModelScope.launch {
            invoiceRepository.markInvoiceAsPaid(invoiceId, transactionId)
        }
    }

    fun initiatePayment(invoice: Invoice, activity: Activity) {
        PaymentEventBus.currentInvoiceId = invoice.id
        val checkout = Checkout()
        
        // IMPORTANT: Use your actual Razorpay Key ID here!
        checkout.setKeyID("rzp_test_mock_key_12345") 
        
        try {
            val options = JSONObject()
            options.put("name", "WaterTracker")
            options.put("description", "Invoice ${invoice.billingMonth}")
            options.put("currency", "INR")
            // Amount is expected in paise (1 INR = 100 paise)
            options.put("amount", (invoice.totalAmount * 100).toInt())
            
            val prefill = JSONObject()
            prefill.put("email", "admin@watertracker.in")
            prefill.put("contact", "9999999999")
            options.put("prefill", prefill)

            checkout.open(activity, options)
        } catch (e: Exception) {
            e.printStackTrace()
            PaymentEventBus.emitError(0, e.message ?: "Unknown error setting up Razorpay")
        }
    }

    fun initiateOfflinePayment(invoice: Invoice) {
        _uiState.update { it.copy(invoiceToMarkOffline = invoice) }
    }

    fun cancelOfflinePayment() {
        _uiState.update { it.copy(invoiceToMarkOffline = null) }
    }

    fun submitOfflinePayment(referenceNumber: String) {
        val invoice = _uiState.value.invoiceToMarkOffline ?: return
        val finalRef = if (referenceNumber.isBlank()) "OFFLINE_PAYMENT" else "OFFLINE: $referenceNumber"
        
        viewModelScope.launch {
            invoiceRepository.markInvoiceAsPaid(invoice.id, finalRef)
            _uiState.update { 
                it.copy(
                    invoiceToMarkOffline = null,
                    showSuccessDialog = true,
                    successMessage = "Invoice marked as paid offline."
                )
            }
        }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(showSuccessDialog = false) }
    }
}
