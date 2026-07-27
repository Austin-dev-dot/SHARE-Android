package com.example.share.ui.fundraiser

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.share.data.RepositoryProvider
import com.example.share.data.models.Fundraiser
import com.example.share.theme.GoFundMeGreen
import com.example.share.theme.GoFundMeGreenLight
import com.example.share.theme.SlateLight
import com.example.share.theme.SlateMedium
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundraiserDetailScreen(
    fundraiserId: String,
    onNavigateBack: () -> Unit
) {
    val fundraiserRepository = RepositoryProvider.fundraiserRepository
    val fundraisers by fundraiserRepository.fundraisers.collectAsStateWithLifecycle(initialValue = emptyList())
    val fundraiser = fundraisers.firstOrNull { it.id == fundraiserId }
    val coroutineScope = rememberCoroutineScope()

    var showCheckoutSheet by remember { mutableStateOf(false) }
    var showPaymentSuccessDialog by remember { mutableStateOf(false) }
    var transactionId by remember { mutableStateOf("") }
    var donationAmountReceived by remember { mutableStateOf(0.0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fundraiser Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* mock share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        if (fundraiser == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading fundraiser detail...")
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color(0xFFF8F9FA)),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    // Cover photo card
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .background(GoFundMeGreen.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = "Cover photo", tint = GoFundMeGreen, modifier = Modifier.size(60.dp))
                        }
                    }

                    // Content card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .offset(y = (-20).dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(GoFundMeGreenLight)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            fundraiser.category,
                                            style = MaterialTheme.typography.labelSmall.copy(color = GoFundMeGreen, fontWeight = FontWeight.Bold)
                                        )
                                    }

                                    if (fundraiser.isVerified) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = GoFundMeGreen, modifier = Modifier.size(16.dp))
                                            Text("Verified Campaign", style = MaterialTheme.typography.labelSmall.copy(color = GoFundMeGreen, fontWeight = FontWeight.Bold))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(fundraiser.title, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp))
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = SlateLight, modifier = Modifier.size(14.dp))
                                    Text(fundraiser.location, style = MaterialTheme.typography.bodySmall.copy(color = SlateLight))
                                    Text("•", style = MaterialTheme.typography.bodySmall.copy(color = SlateLight))
                                    Text("By ${fundraiser.creatorName}", style = MaterialTheme.typography.bodySmall.copy(color = SlateLight, fontWeight = FontWeight.Bold))
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Progress tracker
                                LinearProgressIndicator(
                                    progress = { fundraiser.progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape),
                                    color = GoFundMeGreen,
                                    trackColor = Color(0xFFEEEEEE),
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "₹${fundraiser.raisedAmount.toInt().toString()} raised",
                                        style = MaterialTheme.typography.titleMedium.copy(color = GoFundMeGreen, fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "of ₹${fundraiser.targetAmount.toInt().toString()} target",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = SlateLight)
                                    )
                                }
                            }
                        }
                    }

                    // Details story
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Campaign Story", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(fundraiser.story, style = MaterialTheme.typography.bodyMedium.copy(color = SlateMedium, lineHeight = 22.sp))
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(12.dp)) }

                    // Documents/Verification Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Campaign Verification Documents", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(12.dp))
                                VerificationDocRow(label = "Government ID / PAN card verified", isDone = true)
                                VerificationDocRow(label = "NGO Registration Certificate", isDone = true)
                                VerificationDocRow(label = "Bank Account verification completed", isDone = true)
                                VerificationDocRow(label = "PAN Card 80G Tax Exemption (ex-12A)", isDone = true)
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(12.dp)) }

                    // Updates block
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Updates", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(10.dp))
                                if (fundraiser.updates.isEmpty()) {
                                    Text("No updates posted yet.", style = MaterialTheme.typography.bodySmall.copy(color = SlateLight))
                                } else {
                                    fundraiser.updates.forEach { update ->
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Update", style = MaterialTheme.typography.labelSmall.copy(color = GoFundMeGreen, fontWeight = FontWeight.Bold))
                                                Text(update.timestamp, style = MaterialTheme.typography.bodySmall.copy(color = SlateLight))
                                            }
                                            Text(update.text, style = MaterialTheme.typography.bodyMedium.copy(color = SlateMedium))
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(12.dp)) }

                    // Comments block
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Comments & Donations (${fundraiser.comments.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(10.dp))
                                if (fundraiser.comments.isEmpty()) {
                                    Text("No comments yet. Support this campaign by leaving a message!", style = MaterialTheme.typography.bodySmall.copy(color = SlateLight))
                                } else {
                                    fundraiser.comments.forEach { comment ->
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(comment.authorName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                                Text(comment.timestamp, style = MaterialTheme.typography.bodySmall.copy(color = SlateLight))
                                            }
                                            Text(comment.text, style = MaterialTheme.typography.bodyMedium.copy(color = SlateMedium))
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Sticky bottom Donate Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.White)
                        .padding(20.dp)
                ) {
                    Button(
                        onClick = { showCheckoutSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GoFundMeGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Donate Now", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }

    // Razorpay Mock Checkout Bottom Sheet Overlay
    if (showCheckoutSheet && fundraiser != null) {
        ModalBottomSheet(
            onDismissRequest = { showCheckoutSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            var amountStr by remember { mutableStateOf("") }
            var donorName by remember { mutableStateOf("") }
            var selectedPaymentMethod by remember { mutableStateOf("UPI") } // "UPI", "CARD", "NETBANK"
            var isPaying by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Razorpay Secure Checkout", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F1E36)))
                        Text("UPI, Cards, Net Banking enabled", style = MaterialTheme.typography.bodySmall.copy(color = SlateLight))
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Test Mode", style = MaterialTheme.typography.labelSmall.copy(color = GoFundMeGreen, fontWeight = FontWeight.Bold))
                    }
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))

                // Donor name
                OutlinedTextField(
                    value = donorName,
                    onValueChange = { donorName = it },
                    label = { Text("Your Name (Optional)") },
                    placeholder = { Text("Anonymous") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen)
                )

                // Donation Amount
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Donation Amount (₹)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        placeholder = { Text("Enter amount, e.g. 500") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen)
                    )

                    // Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("100", "500", "1000", "5000").forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (amountStr == preset) GoFundMeGreenLight else Color(0xFFF1F1F1))
                                    .clickable { amountStr = preset }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "₹$preset",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (amountStr == preset) GoFundMeGreen else Color.DarkGray,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                // Payment Method
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Payment Method", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PaymentMethodSelectCard(label = "UPI / QR", isSelected = selectedPaymentMethod == "UPI", onClick = { selectedPaymentMethod = "UPI" }, modifier = Modifier.weight(1f))
                        PaymentMethodSelectCard(label = "Card", isSelected = selectedPaymentMethod == "CARD", onClick = { selectedPaymentMethod = "CARD" }, modifier = Modifier.weight(1f))
                        PaymentMethodSelectCard(label = "NetBanking", isSelected = selectedPaymentMethod == "NETBANK", onClick = { selectedPaymentMethod = "NETBANK" }, modifier = Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val amt = amountStr.toDoubleOrNull() ?: 0.0
                        if (amt <= 0.0) return@Button
                        isPaying = true
                        
                        coroutineScope.launch {
                            // Simulate Razorpay processing delay
                            kotlinx.coroutines.delay(1500)
                            
                            val result = fundraiserRepository.donate(
                                id = fundraiser.id,
                                amount = amt,
                                donorName = donorName
                            )
                            isPaying = false
                            showCheckoutSheet = false
                            
                            result.onSuccess {
                                donationAmountReceived = amt
                                transactionId = "pay_rzp_${System.currentTimeMillis().toString().takeLast(8)}"
                                showPaymentSuccessDialog = true
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F60FF)), // Razorpay Blue
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isPaying && amountStr.toDoubleOrNull() != null
                ) {
                    if (isPaying) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Pay Securely ₹${amountStr.ifEmpty { "0" }}", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Payment Success Alert dialog
    if (showPaymentSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentSuccessDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = GoFundMeGreen, modifier = Modifier.size(48.dp)) },
            title = { Text("Donation Successful!", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Thank you for your generous contribution of ₹${donationAmountReceived.toInt().toString()} to help this cause.", style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Razorpay Receipt ID:", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                    Text(transactionId, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.DarkGray))
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPaymentSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GoFundMeGreen)
                ) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
fun VerificationDocRow(label: String, isDone: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.VerifiedUser, contentDescription = "Doc", tint = GoFundMeGreen, modifier = Modifier.size(16.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(color = SlateMedium))
    }
}

@Composable
fun PaymentMethodSelectCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFE8F0FE) else Color.White),
        border = CardDefaults.outlinedCardBorder(enabled = true).copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) Color(0xFF0F60FF) else Color(0xFFEEEEEE))
        )
    ) {
        Box(
            modifier = Modifier.padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isSelected) Color(0xFF0F60FF) else Color.DarkGray,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
