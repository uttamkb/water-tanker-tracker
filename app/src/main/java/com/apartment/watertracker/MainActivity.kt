package com.apartment.watertracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.apartment.watertracker.core.navigation.WaterTrackerNavHost
import com.apartment.watertracker.core.ui.theme.WaterTrackerTheme
import com.apartment.watertracker.domain.repository.AuthRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import com.apartment.watertracker.core.payment.PaymentEventBus

@AndroidEntryPoint
class MainActivity : ComponentActivity(), PaymentResultWithDataListener {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Checkout.preload(applicationContext)

        enableEdgeToEdge()

        registerFcmToken()

        setContent {
            WaterTrackerTheme {
                WaterTrackerNavHost()
            }
        }
    }

    private fun registerFcmToken() {
        lifecycleScope.launch {
            runCatching {
                val token = FirebaseMessaging.getInstance().token.await()
                authRepository.registerFcmToken(token)
            }
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        PaymentEventBus.emitSuccess(razorpayPaymentId ?: "unknown_txn_id")
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        PaymentEventBus.emitError(code, response ?: "Payment Failed")
    }
}
