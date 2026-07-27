package com.example.share.ui.auth

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.share.data.RepositoryProvider
import com.example.share.theme.BorderSoft
import com.example.share.theme.Evergreen
import com.example.share.theme.EvergreenDeep
import com.example.share.ui.common.BalancedRow
import com.example.share.ui.common.HeroCard
import com.example.share.ui.common.MetricPill
import com.example.share.ui.common.PrimaryAppButton
import com.example.share.ui.common.SectionCard
import com.example.share.ui.common.SectionHeading
import com.example.share.ui.common.ShareBackground
import com.example.share.ui.common.WhatsAppSupportFab
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToOtp: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val authRepository = RepositoryProvider.authRepository

    var contactInput by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val activity = context as? Activity

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = { WhatsAppSupportFab(source = "signing in") }
    ) { paddingValues ->
        ShareBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    HeroCard(
                        eyebrow = "Trusted giving",
                        title = "Support people faster, with dignity.",
                        subtitle = "SHARE brings donations, fundraisers, and volunteers into one calm and reliable giving experience."
                    ) {
                        BalancedRow {
                            MetricPill(value = "Verified", label = "campaign review", modifier = Modifier.weight(1f))
                            MetricPill(value = "Doorstep", label = "pickup support", modifier = Modifier.weight(1f))
                        }
                    }

                    SectionCard {
                        SectionHeading(
                            title = "Sign in to continue",
                            subtitle = "Use your mobile number or email and we’ll send a one-time code."
                        )
                        OutlinedTextField(
                            value = contactInput,
                            onValueChange = { contactInput = it },
                            label = { Text("Mobile number or email") },
                            placeholder = { Text("+91 98765 43210 or name@example.com") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Evergreen,
                                unfocusedBorderColor = BorderSoft
                            )
                        )
                        Text(
                            text = "We only use this to keep your giving journey secure.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        PrimaryAppButton(
                            text = if (isSending) "Sending code..." else "Send verification code",
                            onClick = {
                                val input = contactInput.trim()
                                if (input.isBlank()) {
                                    errorMessage = "Enter your mobile number or email to continue."
                                    return@PrimaryAppButton
                                }

                                val isEmail = input.contains("@")
                                val isPhone = input.replace("+", "").all { it.isDigit() } && input.length >= 10
                                if (!isEmail && !isPhone) {
                                    errorMessage = "Enter a valid mobile number or email address."
                                    return@PrimaryAppButton
                                }

                                if (activity == null) {
                                    errorMessage = "Unable to start verification on this device."
                                    return@PrimaryAppButton
                                }

                                errorMessage = ""
                                isSending = true
                                coroutineScope.launch {
                                    val result = authRepository.sendOtp(input, activity)
                                    isSending = false
                                    result.onSuccess { onNavigateToOtp(input) }
                                        .onFailure { errorMessage = it.message ?: "Could not send the verification code." }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = !isSending
                        )
                        if (isSending) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Evergreen)
                            }
                        }
                    }
                }

                Text(
                    text = "By continuing, you agree to SHARE’s terms and privacy policy.",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = EvergreenDeep.copy(alpha = 0.72f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
