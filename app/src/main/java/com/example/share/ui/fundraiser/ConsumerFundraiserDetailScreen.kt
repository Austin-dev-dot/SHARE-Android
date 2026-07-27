package com.example.share.ui.fundraiser

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.share.data.RepositoryProvider
import com.example.share.theme.BorderSoft
import com.example.share.theme.Evergreen
import com.example.share.theme.EvergreenDeep
import com.example.share.ui.common.BalancedRow
import com.example.share.ui.common.HeroCard
import com.example.share.ui.common.InlineInfoBanner
import com.example.share.ui.common.PrimaryAppButton
import com.example.share.ui.common.SecondaryAppButton
import com.example.share.ui.common.SectionCard
import com.example.share.ui.common.SectionHeading
import com.example.share.ui.common.SelectablePill
import com.example.share.ui.common.ShareBackground
import com.example.share.ui.common.SupportStrip
import com.example.share.ui.common.WhatsAppSupportFab
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumerFundraiserDetailScreen(
    fundraiserId: String,
    onNavigateBack: () -> Unit
) {
    val fundraiserRepository = RepositoryProvider.fundraiserRepository
    val fundraisers by fundraiserRepository.fundraisers.collectAsStateWithLifecycle(initialValue = emptyList())
    val fundraiser = fundraisers.firstOrNull { it.id == fundraiserId }
    val coroutineScope = rememberCoroutineScope()

    var showDonateSheet by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var donatedAmount by remember { mutableStateOf(0.0) }
    var transactionId by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Campaign details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = { WhatsAppSupportFab(source = "fundraiser detail support") }
    ) { paddingValues ->
        ShareBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (fundraiser == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading campaign...")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        HeroCard(
                            eyebrow = if (fundraiser.isVerified) "Verified campaign" else "Campaign",
                            title = fundraiser.title,
                            subtitle = "${fundraiser.location} • by ${fundraiser.creatorName}"
                        )
                    }

                    item {
                        SectionCard {
                            SectionHeading(title = "Progress")
                            LinearProgressIndicator(
                                progress = { fundraiser.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(CircleShape),
                                color = Evergreen,
                                trackColor = BorderSoft
                            )
                            BalancedRow {
                                Text(
                                    text = consumerAmount(fundraiser.raisedAmount),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Evergreen,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "of ${consumerAmount(fundraiser.targetAmount)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            PrimaryAppButton(
                                text = "Donate now",
                                onClick = { showDonateSheet = true },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    item {
                        SectionCard {
                            SectionHeading(title = "Campaign story")
                            Text(
                                text = fundraiser.story,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    item {
                        SectionCard {
                            SectionHeading(title = "Trust signals")
                            InlineInfoBanner(
                                title = "Reviewed before publishing",
                                body = "Public visibility happens after campaign documents are reviewed by the foundation."
                            )
                            Text(
                                text = "Documents on file: ${fundraiser.documents.size}. Updates posted: ${fundraiser.updates.size}. Community comments: ${fundraiser.comments.size}.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (fundraiser.updates.isNotEmpty()) {
                        item {
                            SectionCard {
                                SectionHeading(title = "Latest updates")
                                fundraiser.updates.take(3).forEach { update ->
                                    Text(
                                        text = "${update.timestamp} • ${update.text}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item { SupportStrip(source = "fundraiser donor support") }
                }
            }
        }
    }

    if (showDonateSheet && fundraiser != null) {
        ModalBottomSheet(onDismissRequest = { showDonateSheet = false }) {
            var amount by remember { mutableStateOf("500") }
            var donorName by remember { mutableStateOf("") }
            var isPaying by remember { mutableStateOf(false) }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SectionHeading(
                        title = "Complete your donation",
                        subtitle = "A cleaner checkout keeps the experience focused and reassuring."
                    )
                }
                item {
                    OutlinedTextField(
                        value = donorName,
                        onValueChange = { donorName = it },
                        label = { Text("Your name (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Evergreen,
                            unfocusedBorderColor = BorderSoft
                        )
                    )
                }
                item {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount in ₹") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Evergreen,
                            unfocusedBorderColor = BorderSoft
                        )
                    )
                }
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("500", "1000", "2500", "5000").forEach { preset ->
                            SelectablePill(
                                text = "₹$preset",
                                selected = amount == preset,
                                onClick = { amount = preset }
                            )
                        }
                    }
                }
                item {
                    PrimaryAppButton(
                        text = if (isPaying) "Processing donation..." else "Pay securely",
                        onClick = {
                            val value = amount.toDoubleOrNull() ?: 0.0
                            if (value <= 0.0) return@PrimaryAppButton
                            isPaying = true
                            coroutineScope.launch {
                                delay(1200)
                                val result = fundraiserRepository.donate(
                                    id = fundraiser.id,
                                    amount = value,
                                    donorName = donorName
                                )
                                isPaying = false
                                result.onSuccess {
                                    donatedAmount = value
                                    transactionId = "pay_${System.currentTimeMillis().toString().takeLast(8)}"
                                    showDonateSheet = false
                                    showSuccessDialog = true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isPaying
                    )
                    if (isPaying) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Evergreen)
                        }
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Donation successful") },
            text = {
                Text(
                    "Thank you for contributing ${consumerAmount(donatedAmount)}. Receipt reference: $transactionId"
                )
            },
            confirmButton = {
                PrimaryAppButton(
                    text = "Done",
                    onClick = { showSuccessDialog = false }
                )
            }
        )
    }
}

private fun consumerAmount(amount: Double): String = "₹${amount.toInt()}"
