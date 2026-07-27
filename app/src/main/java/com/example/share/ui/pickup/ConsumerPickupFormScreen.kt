package com.example.share.ui.pickup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.share.data.RepositoryProvider
import com.example.share.theme.BorderSoft
import com.example.share.theme.Evergreen
import com.example.share.ui.common.HeroCard
import com.example.share.ui.common.InlineInfoBanner
import com.example.share.ui.common.PrimaryAppButton
import com.example.share.ui.common.SectionCard
import com.example.share.ui.common.SectionHeading
import com.example.share.ui.common.SelectablePill
import com.example.share.ui.common.ShareBackground
import com.example.share.ui.common.SupportStrip
import com.example.share.ui.common.WhatsAppSupportFab
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ConsumerPickupFormScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val authRepository = RepositoryProvider.authRepository
    val pickupRepository = RepositoryProvider.pickupRepository

    val currentUser by authRepository.currentUser.collectAsStateWithLifecycle(initialValue = null)

    var donorName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-07-17") }
    var timeSlot by remember { mutableStateOf("10:00 AM - 01:00 PM") }
    var estimatedQuantity by remember { mutableStateOf("Medium box (~5kg)") }
    var specialInstructions by remember { mutableStateOf("") }
    val selectedItems = remember { mutableStateListOf<String>() }

    val availableItems = listOf(
        "Clothes",
        "Books",
        "Toys",
        "Blankets",
        "Stationery",
        "Groceries",
        "Kitchenware",
        "Electronics"
    )

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            donorName = it.name
            phone = it.phone
            email = it.email
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Schedule a pickup") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = { WhatsAppSupportFab(source = "pickup help") }
    ) { paddingValues ->
        ShareBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    HeroCard(
                        eyebrow = "Doorstep giving",
                        title = "Arrange a smooth donation pickup",
                        subtitle = "Share what you’re donating and when you’ll be available. The flow is streamlined to feel easy for first-time donors."
                    )
                }

                item {
                    SectionCard {
                        SectionHeading(title = "Your details", subtitle = "We’ll use these for pickup coordination only.")
                        ConsumerField(value = donorName, onValueChange = { donorName = it }, label = "Full name")
                        ConsumerField(value = phone, onValueChange = { phone = it }, label = "Phone number")
                        ConsumerField(value = email, onValueChange = { email = it }, label = "Email address")
                    }
                }

                item {
                    SectionCard {
                        SectionHeading(title = "Pickup plan", subtitle = "A little structure makes the experience feel much more dependable.")
                        ConsumerField(value = address, onValueChange = { address = it }, label = "Pickup address", minLines = 3)
                        ConsumerField(value = date, onValueChange = { date = it }, label = "Pickup date")
                        ConsumerField(value = timeSlot, onValueChange = { timeSlot = it }, label = "Preferred time slot")
                    }
                }

                item {
                    SectionCard {
                        SectionHeading(title = "What are you donating?", subtitle = "Choose everything that applies.")
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableItems.forEach { item ->
                                SelectablePill(
                                    text = item,
                                    selected = selectedItems.contains(item),
                                    onClick = {
                                        if (selectedItems.contains(item)) selectedItems.remove(item) else selectedItems.add(item)
                                    }
                                )
                            }
                        }
                        ConsumerField(
                            value = estimatedQuantity,
                            onValueChange = { estimatedQuantity = it },
                            label = "Estimated quantity"
                        )
                        ConsumerField(
                            value = specialInstructions,
                            onValueChange = { specialInstructions = it },
                            label = "Special instructions",
                            minLines = 3
                        )
                    }
                }

                item {
                    InlineInfoBanner(
                        title = "After you submit",
                        body = "You’ll get a cleaner tracking screen where you can follow your request status and reach support if anything changes."
                    )
                }

                if (errorMessage.isNotEmpty()) {
                    item {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                item {
                    PrimaryAppButton(
                        text = if (isSubmitting) "Scheduling pickup..." else "Schedule free pickup",
                        onClick = {
                            if (donorName.isBlank() || phone.isBlank() || address.isBlank() || selectedItems.isEmpty()) {
                                errorMessage = "Fill in your details, address, and at least one donation category."
                            } else {
                                errorMessage = ""
                                isSubmitting = true
                                coroutineScope.launch {
                                    val result = pickupRepository.submitPickupRequest(
                                        donorName = donorName,
                                        phone = phone,
                                        email = email,
                                        address = address,
                                        latitude = 28.6289,
                                        longitude = 77.2244,
                                        date = date,
                                        time = timeSlot,
                                        items = selectedItems.toList(),
                                        estimatedQuantity = estimatedQuantity,
                                        imageUris = emptyList(),
                                        specialInstructions = specialInstructions
                                    )
                                    isSubmitting = false
                                    result.onSuccess { onNavigateToDetail(it.id) }
                                        .onFailure { errorMessage = it.message ?: "We could not schedule your pickup right now." }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isSubmitting
                    )
                    if (isSubmitting) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Evergreen)
                        }
                    }
                }

                item { SupportStrip(source = "pickup request support") }
            }
        }
    }
}

@Composable
private fun ConsumerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        minLines = minLines,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Evergreen,
            unfocusedBorderColor = BorderSoft
        )
    )
}
