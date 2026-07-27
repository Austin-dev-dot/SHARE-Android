package com.example.share.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.share.data.RepositoryProvider
import com.example.share.theme.BorderSoft
import com.example.share.theme.Evergreen
import com.example.share.ui.common.HeroCard
import com.example.share.ui.common.PrimaryAppButton
import com.example.share.ui.common.SectionCard
import com.example.share.ui.common.SectionHeading
import com.example.share.ui.common.ShareBackground
import com.example.share.ui.common.WhatsAppSupportFab
import androidx.compose.material3.ExperimentalMaterial3Api
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    contact: String,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToRegisterDetails: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val authRepository = RepositoryProvider.authRepository

    var otpInput by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = { WhatsAppSupportFab(source = "verification help") }
    ) { paddingValues ->
        ShareBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                HeroCard(
                    eyebrow = "Secure access",
                    title = "Check your inbox or messages",
                    subtitle = "We sent a one-time code to $contact. Enter it below to continue into the app."
                )

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(18.dp))

                SectionCard {
                    SectionHeading(
                        title = "Enter verification code",
                        subtitle = "For this demo flow, you can use 1234."
                    )
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = { if (it.length <= 6) otpInput = it },
                            label = { Text("Code") },
                            modifier = Modifier.width(220.dp),
                            singleLine = true,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Evergreen,
                                unfocusedBorderColor = BorderSoft
                            ),
                            textStyle = MaterialTheme.typography.titleLarge.copy(
                                textAlign = TextAlign.Center,
                                letterSpacing = 6.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    PrimaryAppButton(
                        text = if (isVerifying) "Verifying..." else "Verify and continue",
                        onClick = {
                            val otp = otpInput.trim()
                            if (otp.length < 4) {
                                errorMessage = "Enter the 4-digit code to continue."
                            } else {
                                errorMessage = ""
                                isVerifying = true
                                coroutineScope.launch {
                                    val result = authRepository.verifyOtp(contact, otp)
                                    isVerifying = false
                                    result.onSuccess { user ->
                                        if (user != null) onNavigateToHome() else onNavigateToRegisterDetails(contact)
                                    }.onFailure {
                                        errorMessage = it.message ?: "We could not verify that code."
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isVerifying
                    )
                    if (isVerifying) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Evergreen)
                        }
                    }
                }
            }
        }
    }
}
