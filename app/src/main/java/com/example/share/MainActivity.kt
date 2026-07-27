package com.example.share

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.example.share.data.backend.PaymentResult
import com.example.share.data.backend.RazorpayPaymentManager
import com.example.share.theme.SHARETheme
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity(), PaymentResultListener {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    setContent {
      SHARETheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          MainNavigation()
        }
      }
    }
  }

  override fun onPaymentSuccess(razorpayPaymentId: String?) {
    runBlocking {
      RazorpayPaymentManager.emitResult(PaymentResult.Success(razorpayPaymentId ?: "unknown_payment_id"))
    }
  }

  override fun onPaymentError(code: Int, response: String?) {
    runBlocking {
      RazorpayPaymentManager.emitResult(PaymentResult.Error(code, response ?: "unknown_error"))
    }
  }
}
