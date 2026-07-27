package com.example.share.ui.pickup

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.share.data.RepositoryProvider
import com.example.share.data.models.PickupRequest
import com.example.share.data.models.PickupStatus
import com.example.share.theme.GoFundMeGreen
import com.example.share.theme.GoFundMeGreenLight
import com.example.share.theme.SlateLight
import com.example.share.theme.SlateMedium

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickupDetailScreen(
    requestId: String,
    onNavigateBack: () -> Unit
) {
    val pickupRepository = RepositoryProvider.pickupRepository
    val requests by pickupRepository.pickupRequests.collectAsStateWithLifecycle(initialValue = emptyList())
    val request = requests.firstOrNull { it.id == requestId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Donation Pickup", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* share mock */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        if (request == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading request details...")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF8F9FA)),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Status Stepper Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Pickup Status", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(20.dp))
                            PickupStepper(currentStatus = request.status)
                        }
                    }
                }

                // 2. Google Maps Mockup Card (With Route Drawing!)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Map Canvas drawing grid and route
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Draw mock gridlines
                                val gridColor = Color(0xFFC8E6C9)
                                val w = size.width
                                val h = size.height
                                
                                for (i in 0..w.toInt() step 80) {
                                    drawLine(gridColor, Offset(i.toFloat(), 0f), Offset(i.toFloat(), h), 1.dp.toPx())
                                }
                                for (i in 0..h.toInt() step 80) {
                                    drawLine(gridColor, Offset(0f, i.toFloat()), Offset(w, i.toFloat()), 1.dp.toPx())
                                }

                                // Draw Route Line
                                val routeColor = GoFundMeGreen
                                drawLine(
                                    color = routeColor,
                                    start = Offset(50f, h - 50f),
                                    end = Offset(w / 2, h / 2),
                                    strokeWidth = 6.dp.toPx()
                                )
                                drawLine(
                                    color = routeColor,
                                    start = Offset(w / 2, h / 2),
                                    end = Offset(w - 120f, 60f),
                                    strokeWidth = 6.dp.toPx()
                                )
                            }

                            // Address Location Pin
                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 24.dp, end = 60.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = "Address Pin", tint = Color.Red, modifier = Modifier.size(36.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Your Address",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            // Driver Location Pin (Only if Assigned, Picked up, etc.)
                            if (request.status != PickupStatus.PENDING && request.status != PickupStatus.COMPLETED) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .offset(x = (-30).dp, y = (20).dp)
                                        .size(36.dp)
                                        .background(Color.White, CircleShape)
                                        .clip(CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Navigation, contentDescription = "Driver", tint = GoFundMeGreen, modifier = Modifier.size(20.dp))
                                }
                            }

                            // Maps Watermark
                            Text(
                                "Google Maps SDK Mockup",
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp)
                                    .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // 3. Driver Info Card (If assigned)
                if (request.status != PickupStatus.PENDING) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .background(GoFundMeGreenLight, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "RS",
                                            style = MaterialTheme.typography.titleMedium.copy(color = GoFundMeGreen, fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Column {
                                        Text("Ramesh Singh", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                        Text("Vehicle: DL 3C AB 1234 (E-Rickshaw)", style = MaterialTheme.typography.bodySmall.copy(color = SlateLight))
                                    }
                                }
                                
                                IconButton(
                                    onClick = { /* dial number mock */ },
                                    modifier = Modifier
                                        .background(GoFundMeGreen, CircleShape)
                                        .size(40.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Call Driver", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }

                // 4. Details Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text("Donation Summary", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            
                            DetailRow(label = "Items Donated", value = request.items.joinToString(", "))
                            DetailRow(label = "Estimated Qty", value = request.estimatedQuantity)
                            DetailRow(label = "Scheduled Date", value = request.date)
                            DetailRow(label = "Scheduled Time", value = request.time)
                            DetailRow(label = "Pickup Address", value = request.address)
                            
                            if (request.specialInstructions.isNotEmpty()) {
                                DetailRow(label = "Special Instructions", value = request.specialInstructions)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall.copy(color = SlateLight))
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = Color.DarkGray))
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Color(0xFFF1F1F1))
    }
}

@Composable
fun PickupStepper(currentStatus: PickupStatus) {
    val statuses = listOf(
        PickupStatus.PENDING,
        PickupStatus.ASSIGNED,
        PickupStatus.PICKED_UP,
        PickupStatus.DELIVERED,
        PickupStatus.COMPLETED
    )
    val labels = listOf("Pending", "Assigned", "Picked Up", "Delivered", "Completed")
    val currentIndex = statuses.indexOf(currentStatus)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        statuses.forEachIndexed { index, status ->
            val isActive = index <= currentIndex
            val isCurrent = index == currentIndex
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Stepper bubble and connecting line
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                if (isActive) GoFundMeGreen else Color(0xFFEEEEEE),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isActive) {
                            Icon(Icons.Default.Check, contentDescription = "Active", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                
                // Text Label
                Text(
                    text = labels[index],
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (isActive) GoFundMeGreen else SlateMedium
                    )
                )
            }
        }
    }
}
