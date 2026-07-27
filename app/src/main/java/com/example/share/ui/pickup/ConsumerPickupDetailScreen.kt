package com.example.share.ui.pickup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.share.data.RepositoryProvider
import com.example.share.data.models.PickupRequest
import com.example.share.data.models.PickupStatus
import com.example.share.theme.BorderSoft
import com.example.share.theme.Evergreen
import com.example.share.theme.EvergreenDeep
import com.example.share.theme.MintWash
import com.example.share.theme.SkyWash
import com.example.share.ui.common.DetailLine
import com.example.share.ui.common.HeroCard
import com.example.share.ui.common.SectionCard
import com.example.share.ui.common.SectionHeading
import com.example.share.ui.common.ShareBackground
import com.example.share.ui.common.SupportStrip
import com.example.share.ui.common.WhatsAppSupportFab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumerPickupDetailScreen(
    requestId: String,
    onNavigateBack: () -> Unit
) {
    val pickupRepository = RepositoryProvider.pickupRepository
    val requests by pickupRepository.pickupRequests.collectAsStateWithLifecycle(initialValue = emptyList())
    val request = requests.firstOrNull { it.id == requestId }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Pickup tracking") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = { WhatsAppSupportFab(source = "pickup tracking support") }
    ) { paddingValues ->
        ShareBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (request == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading your request...")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        HeroCard(
                            eyebrow = "Pickup status",
                            title = pickupHeadline(request.status),
                            subtitle = "This view is redesigned to make the flow feel obvious at a glance, especially for non-technical users."
                        )
                    }

                    item {
                        SectionCard {
                            SectionHeading(title = "Progress", subtitle = "Follow every stage without ambiguity.")
                            PickupProgressStack(status = request.status)
                        }
                    }

                    item {
                        SectionCard {
                            SectionHeading(title = "Donation summary")
                            DetailLine(label = "Items", value = request.items.joinToString(", "))
                            DetailLine(label = "Estimated quantity", value = request.estimatedQuantity)
                            DetailLine(label = "Pickup date", value = request.date)
                            DetailLine(label = "Time slot", value = request.time)
                            DetailLine(label = "Address", value = request.address)
                            if (request.specialInstructions.isNotBlank()) {
                                DetailLine(label = "Instructions", value = request.specialInstructions)
                            }
                        }
                    }

                    item {
                        SectionCard {
                            SectionHeading(title = "Support touchpoint")
                            Text(
                                text = "If timing changes, access gets tricky, or you need to speak to someone fast, the WhatsApp support button stays available on this screen.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    item { SupportStrip(source = "pickup detail support") }
                }
            }
        }
    }
}

@Composable
private fun PickupProgressStack(status: PickupStatus) {
    val steps = listOf(
        PickupStatus.PENDING to "Request received",
        PickupStatus.ASSIGNED to "Pickup partner assigned",
        PickupStatus.PICKED_UP to "Items collected",
        PickupStatus.DELIVERED to "Delivered to sorting or relief point",
        PickupStatus.COMPLETED to "Completed"
    )
    val activeIndex = steps.indexOfFirst { it.first == status }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        steps.forEachIndexed { index, (_, label) ->
            val isActive = index <= activeIndex
            val background = when {
                isActive && index == activeIndex -> Evergreen
                isActive -> MintWash
                else -> Color.White
            }
            val textColor = when {
                isActive && index == activeIndex -> Color.White
                isActive -> EvergreenDeep
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(background, RoundedCornerShape(18.dp))
                    .then(
                        if (!isActive) Modifier.background(Color.White, RoundedCornerShape(18.dp))
                        else Modifier
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(text = label, style = MaterialTheme.typography.titleMedium, color = textColor)
            }
        }
    }
}

private fun pickupHeadline(status: PickupStatus): String = when (status) {
    PickupStatus.PENDING -> "We’ve received your request"
    PickupStatus.ASSIGNED -> "Your pickup is now assigned"
    PickupStatus.PICKED_UP -> "Your items have been collected"
    PickupStatus.DELIVERED -> "Your donation is on the way"
    PickupStatus.COMPLETED -> "Your donation journey is complete"
}
