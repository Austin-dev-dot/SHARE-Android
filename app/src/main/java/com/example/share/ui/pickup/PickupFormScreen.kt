package com.example.share.ui.pickup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.share.data.RepositoryProvider
import com.example.share.theme.GoFundMeGreen
import com.example.share.theme.GoFundMeGreenLight
import com.example.share.theme.SlateLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickupFormScreen(
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
    var estimatedQuantity by remember { mutableStateOf("Medium Box (~5kg)") }
    var specialInstructions by remember { mutableStateOf("") }
    
    // Auto-fill user details if logged in
    LaunchedEffect(currentUser) {
        currentUser?.let {
            donorName = it.name
            phone = it.phone
            email = it.email
        }
    }

    // Available items to donate
    val availableItems = listOf(
        "Clothes", "Books", "Toys", "Furniture", 
        "Medicines", "Blankets", "Stationery", 
        "Electronics", "Kitchen utensils", 
        "Groceries", "Wheelchairs", "Bicycles"
    )
    val selectedItems = remember { mutableStateListOf<String>() }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Doorstep Pickup Form", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA)),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Donor Details", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        
                        OutlinedTextField(
                            value = donorName,
                            onValueChange = { donorName = it },
                            label = { Text("Donor Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen)
                        )
                        
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen)
                        )
                        
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen)
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Pickup Location & Time", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Pickup Address") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            minLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = date,
                                onValueChange = { date = it },
                                label = { Text("Date") },
                                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = "Date", modifier = Modifier.size(16.dp)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen)
                            )
                            
                            OutlinedTextField(
                                value = timeSlot,
                                onValueChange = { timeSlot = it },
                                label = { Text("Time Slot") },
                                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = "Time", modifier = Modifier.size(16.dp)) },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen)
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Items to Donate", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("Select all items you want to donate:", style = MaterialTheme.typography.bodySmall.copy(color = SlateLight))
                        
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableItems.forEach { item ->
                                val isSelected = selectedItems.contains(item)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) GoFundMeGreenLight else Color(0xFFF1F1F1))
                                        .clickable {
                                            if (isSelected) selectedItems.remove(item) else selectedItems.add(item)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = item,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (isSelected) GoFundMeGreen else Color.DarkGray,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        OutlinedTextField(
                            value = estimatedQuantity,
                            onValueChange = { estimatedQuantity = it },
                            label = { Text("Estimated Quantity") },
                            placeholder = { Text("e.g. 2 boxes, 1 large bag") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen)
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Additional Info", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        
                        OutlinedTextField(
                            value = specialInstructions,
                            onValueChange = { specialInstructions = it },
                            label = { Text("Special Instructions (Optional)") },
                            placeholder = { Text("e.g. gate code, specific drop point, instructions for driver") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen)
                        )
                    }
                }
            }
            
            if (errorMessage.isNotEmpty()) {
                item {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 8.dp))
                }
            }

            item {
                Button(
                    onClick = {
                        if (donorName.isBlank() || phone.isBlank() || address.isBlank() || selectedItems.isEmpty()) {
                            errorMessage = "Please fill in all details and select at least one item."
                            return@Button
                        }
                        errorMessage = ""
                        isSubmitting = true
                        
                        coroutineScope.launch {
                            val result = pickupRepository.submitPickupRequest(
                                donorName = donorName,
                                phone = phone,
                                email = email,
                                address = address,
                                latitude = 28.6289, // mock lat
                                longitude = 77.2244, // mock lng
                                date = date,
                                time = timeSlot,
                                items = selectedItems.toList(),
                                estimatedQuantity = estimatedQuantity,
                                imageUris = emptyList(),
                                specialInstructions = specialInstructions
                            )
                            isSubmitting = false
                            result.onSuccess { newRequest ->
                                onNavigateToDetail(newRequest.id)
                            }.onFailure {
                                errorMessage = it.message ?: "Failed to submit request"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoFundMeGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Schedule Free Pickup", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
