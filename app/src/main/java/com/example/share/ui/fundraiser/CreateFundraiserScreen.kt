package com.example.share.ui.fundraiser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.share.data.RepositoryProvider
import com.example.share.theme.GoFundMeGreen
import com.example.share.theme.GoFundMeGreenLight
import com.example.share.theme.SlateLight
import kotlinx.coroutines.launch

private fun getFileNameFromUri(context: android.content.Context, uri: android.net.Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    result = cursor.getString(index)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result ?: "document"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFundraiserScreen(
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val authRepository = RepositoryProvider.authRepository
    val fundraiserRepository = RepositoryProvider.fundraiserRepository
    
    val currentUser by authRepository.currentUser.collectAsStateWithLifecycle(initialValue = null)

    var title by remember { mutableStateOf("") }
    var story by remember { mutableStateOf("") }
    var targetAmountStr by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var creatorName by remember { mutableStateOf("") }
    
    val categories = listOf("Medical Emergency", "Education", "Flood Relief", "Animal Rescue", "Women Empowerment", "Food Distribution")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    // Toggle: Raise funds as Individual vs NGO
    var raiseAsNgo by remember { mutableStateOf(false) } // false = Individual, true = NGO

    // Uploaded doc URI states
    var governmentIdUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var ngoRegUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var panUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var bankDetailsUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current

    val governmentIdName = governmentIdUri?.let { getFileNameFromUri(context, it) }
    val ngoRegName = ngoRegUri?.let { getFileNameFromUri(context, it) }
    val panName = panUri?.let { getFileNameFromUri(context, it) }
    val bankDetailsName = bankDetailsUri?.let { getFileNameFromUri(context, it) }

    val govtIdLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { governmentIdUri = it }
    }
    val ngoRegLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { ngoRegUri = it }
    }
    val panLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { panUri = it }
    }
    val bankLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { bankDetailsUri = it }
    }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            creatorName = it.name
            location = "New Delhi, Delhi"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Start a Fundraiser", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        if (successMessage.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(GoFundMeGreenLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Success", tint = GoFundMeGreen, modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Campaign Submitted!", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    successMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(color = SlateLight),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(30.dp))
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = GoFundMeGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Return to Dashboard", style = MaterialTheme.typography.titleMedium.copy(color = Color.White))
                }
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
                // Individual vs NGO Selection Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Raise Funds As", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Individual Card Button
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { raiseAsNgo = false },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (!raiseAsNgo) GoFundMeGreenLight else Color(0xFFF5F5F5)
                                    ),
                                    border = if (!raiseAsNgo) CardDefaults.outlinedCardBorder(enabled = true).copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(GoFundMeGreen)
                                    ) else null
                                ) {
                                    Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                                        Text(
                                            "Individual",
                                            fontWeight = FontWeight.Bold,
                                            color = if (!raiseAsNgo) GoFundMeGreen else Color.DarkGray
                                        )
                                    }
                                }

                                // NGO Card Button
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { raiseAsNgo = true },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (raiseAsNgo) GoFundMeGreenLight else Color(0xFFF5F5F5)
                                    ),
                                    border = if (raiseAsNgo) CardDefaults.outlinedCardBorder(enabled = true).copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(GoFundMeGreen)
                                    ) else null
                                ) {
                                    Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                                        Text(
                                            "Registered NGO",
                                            fontWeight = FontWeight.Bold,
                                            color = if (raiseAsNgo) GoFundMeGreen else Color.DarkGray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Campaign details card
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
                            Text("Campaign Details", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Campaign Title") },
                                placeholder = { Text("e.g. ₹25,000 for child's heart surgery") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen)
                            )

                            // Category Dropdown
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = selectedCategory,
                                    onValueChange = {},
                                    label = { Text("Category") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    readOnly = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded)
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen)
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { isCategoryDropdownExpanded = !isCategoryDropdownExpanded }
                                )
                                DropdownMenu(
                                    expanded = isCategoryDropdownExpanded,
                                    onDismissRequest = { isCategoryDropdownExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    categories.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(category) },
                                            onClick = {
                                                selectedCategory = category
                                                isCategoryDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = targetAmountStr,
                                onValueChange = { targetAmountStr = it },
                                label = { Text("Target Amount (₹)") },
                                placeholder = { Text("e.g. 50000") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen)
                            )
                            
                            OutlinedTextField(
                                value = story,
                                onValueChange = { story = it },
                                label = { Text("Campaign Story / Description") },
                                placeholder = { Text("Explain why you are raising funds, how the funds will be used, and the urgency...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                minLines = 4,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen)
                            )
                        }
                    }
                }

                // Creator details
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
                            Text(
                                text = if (raiseAsNgo) "NGO Information" else "Individual Information",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            
                            OutlinedTextField(
                                value = creatorName,
                                onValueChange = { creatorName = it },
                                label = { Text(if (raiseAsNgo) "NGO Registered Name" else "Full Legal Name") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen)
                            )
                            
                            OutlinedTextField(
                                value = location,
                                onValueChange = { location = it },
                                label = { Text("Campaign Location") },
                                placeholder = { Text("e.g. Noida, Uttar Pradesh") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen)
                            )
                        }
                    }
                }

                // Government Verification Documents
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("KYC & Trust Verification", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Only verified campaigns become public. Upload proof to request approval:", style = MaterialTheme.typography.bodySmall.copy(color = SlateLight))
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            if (!raiseAsNgo) {
                                // Individual requires Aadhaar / Govt ID
                                VerificationUploadRow(
                                    title = "Government Issued ID (Aadhaar/DL)",
                                    fileName = governmentIdName,
                                    onClick = { govtIdLauncher.launch("*/*") }
                                )
                            } else {
                                // NGO requires Registration Certificate
                                VerificationUploadRow(
                                    title = "NGO Registration Certificate",
                                    fileName = ngoRegName,
                                    onClick = { ngoRegLauncher.launch("*/*") }
                                )
                            }
                            
                            VerificationUploadRow(
                                title = "PAN Card verification",
                                fileName = panName,
                                onClick = { panLauncher.launch("*/*") }
                            )
                            
                            VerificationUploadRow(
                                title = "Cancelled Cheque / Bank verification",
                                fileName = bankDetailsName,
                                onClick = { bankLauncher.launch("*/*") }
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
                            val target = targetAmountStr.toDoubleOrNull() ?: 0.0
                            if (title.isBlank() || story.isBlank() || target <= 0.0 || location.isBlank() || creatorName.isBlank()) {
                                errorMessage = "Please fill in all details with a valid target amount."
                                return@Button
                            }
                            
                            // Check document upload compliance based on type
                            val isDocCompliant = if (raiseAsNgo) {
                                ngoRegUri != null && panUri != null && bankDetailsUri != null
                            } else {
                                governmentIdUri != null && panUri != null && bankDetailsUri != null
                            }

                            if (!isDocCompliant) {
                                errorMessage = if (raiseAsNgo) {
                                    "Please attach NGO Registration, PAN, and Bank details."
                                } else {
                                    "Please attach Government ID, PAN, and Bank details."
                                }
                                return@Button
                            }
                            
                            errorMessage = ""
                            isSubmitting = true
                            
                            coroutineScope.launch {
                                val docList = mutableListOf<String>()
                                try {
                                    val govtUri = governmentIdUri
                                    if (!raiseAsNgo && govtUri != null) {
                                        val urlResult = com.example.share.data.backend.FirebaseStorageService.uploadFile("kyc", govtUri)
                                        urlResult.onSuccess { docList.add(it) }.onFailure { throw it }
                                    }
                                    val ngoUri = ngoRegUri
                                    if (raiseAsNgo && ngoUri != null) {
                                        val urlResult = com.example.share.data.backend.FirebaseStorageService.uploadFile("kyc", ngoUri)
                                        urlResult.onSuccess { docList.add(it) }.onFailure { throw it }
                                    }
                                    val pUri = panUri
                                    if (pUri != null) {
                                        val urlResult = com.example.share.data.backend.FirebaseStorageService.uploadFile("kyc", pUri)
                                        urlResult.onSuccess { docList.add(it) }.onFailure { throw it }
                                    }
                                    val bUri = bankDetailsUri
                                    if (bUri != null) {
                                        val urlResult = com.example.share.data.backend.FirebaseStorageService.uploadFile("kyc", bUri)
                                        urlResult.onSuccess { docList.add(it) }.onFailure { throw it }
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Failed to upload verification documents: ${e.message}"
                                    isSubmitting = false
                                    return@launch
                                }

                                val result = fundraiserRepository.createFundraiser(
                                    title = title,
                                    story = story,
                                    targetAmount = target,
                                    coverPhotoUrl = "",
                                    location = location,
                                    category = selectedCategory,
                                    creatorName = creatorName + (if (raiseAsNgo) " (NGO)" else " (Individual)"),
                                    documents = docList
                                )
                                isSubmitting = false
                                result.onSuccess {
                                    successMessage = "Your fundraiser has been successfully created. The Jan Manas Foundation admin team has been notified to review your verification documents. Once approved on the Web Admin Dashboard, the fundraiser will be listed publicly."
                                }.onFailure {
                                    errorMessage = it.message ?: "Failed to submit fundraiser"
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
                            Text("Submit Fundraiser for Approval", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun VerificationUploadRow(
    title: String,
    fileName: String?,
    onClick: () -> Unit
) {
    val isUploaded = fileName != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isUploaded) GoFundMeGreenLight.copy(alpha = 0.6f) else Color(0xFFF1F1F1))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
            Text(
                if (isUploaded) "File: $fileName" else "Not uploaded",
                style = MaterialTheme.typography.bodySmall.copy(color = if (isUploaded) GoFundMeGreen else SlateLight)
            )
        }
        Text(
            if (isUploaded) "Attached" else "Attach",
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (isUploaded) GoFundMeGreen else Color.Gray,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
