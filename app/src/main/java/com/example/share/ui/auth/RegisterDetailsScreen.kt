package com.example.share.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import com.example.share.data.RepositoryProvider
import com.example.share.theme.BorderSoft
import com.example.share.theme.Evergreen
import com.example.share.ui.common.HeroCard
import com.example.share.ui.common.PrimaryAppButton
import com.example.share.ui.common.SectionCard
import com.example.share.ui.common.SectionHeading
import com.example.share.ui.common.ShareBackground
import com.example.share.ui.common.WhatsAppSupportFab
import kotlinx.coroutines.launch

@Composable
fun RegisterDetailsScreen(
    contact: String,
    onNavigateToHome: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val authRepository = RepositoryProvider.authRepository

    var nameInput by remember { mutableStateOf("") }
    var ageInput by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = { WhatsAppSupportFab(source = "finishing profile setup") }
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
                verticalArrangement = Arrangement.Center
            ) {
                HeroCard(
                    eyebrow = "One last step",
                    title = "Complete your supporter profile",
                    subtitle = "A simple profile helps us personalize campaigns, pickups, and volunteer opportunities for you."
                )

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(18.dp))

                SectionCard {
                    SectionHeading(
                        title = "Your basic details",
                        subtitle = "Signing in with $contact"
                    )
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Full name") },
                        placeholder = { Text("Rahul Sen") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Evergreen,
                            unfocusedBorderColor = BorderSoft
                        )
                    )
                    OutlinedTextField(
                        value = ageInput,
                        onValueChange = { ageInput = it },
                        label = { Text("Age") },
                        placeholder = { Text("25") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Evergreen,
                            unfocusedBorderColor = BorderSoft
                        )
                    )
                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    PrimaryAppButton(
                        text = if (isRegistering) "Completing profile..." else "Complete sign in",
                        onClick = {
                            val name = nameInput.trim()
                            val age = ageInput.toIntOrNull() ?: 0
                            if (name.isBlank() || age <= 0) {
                                errorMessage = "Enter a valid name and age to continue."
                            } else {
                                errorMessage = ""
                                isRegistering = true
                                coroutineScope.launch {
                                    val result = authRepository.completeRegistration(contact, name, age)
                                    isRegistering = false
                                    result.onSuccess { onNavigateToHome() }
                                        .onFailure { errorMessage = it.message ?: "We could not complete registration." }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isRegistering
                    )
                    if (isRegistering) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Evergreen)
                        }
                    }
                }
            }
        }
    }
}
