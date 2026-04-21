package com.apartment.watertracker.core.payment

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class PaymentResult {
    data class Success(val paymentId: String, val invoiceId: String) : PaymentResult()
    data class Error(val code: Int, val description: String, val invoiceId: String) : PaymentResult()
}

object PaymentEventBus {
    var currentInvoiceId: String? = null
    
    private val _paymentResults = MutableSharedFlow<PaymentResult>(extraBufferCapacity = 1)
    val paymentResults = _paymentResults.asSharedFlow()

    fun emitSuccess(paymentId: String) {
        currentInvoiceId?.let { invoiceId ->
            _paymentResults.tryEmit(PaymentResult.Success(paymentId, invoiceId))
            currentInvoiceId = null
        }
    }

    fun emitError(code: Int, description: String) {
        currentInvoiceId?.let { invoiceId ->
            _paymentResults.tryEmit(PaymentResult.Error(code, description, invoiceId))
            currentInvoiceId = null
        }
    }
}
