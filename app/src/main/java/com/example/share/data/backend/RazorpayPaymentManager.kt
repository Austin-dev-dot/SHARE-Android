package com.example.share.data.backend

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object RazorpayPaymentManager {
    private val _paymentResult = MutableSharedFlow<PaymentResult>()
    val paymentResult: SharedFlow<PaymentResult> = _paymentResult.asSharedFlow()

    suspend fun emitResult(result: PaymentResult) {
        _paymentResult.emit(result)
    }
}

sealed interface PaymentResult {
    data class Success(val paymentId: String) : PaymentResult
    data class Error(val code: Int, val description: String) : PaymentResult
}
