package com.example.share.ui.fundraiser

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.share.data.RepositoryProvider
import com.example.share.data.backend.FirebaseStorageService
import com.example.share.theme.BorderSoft
import com.example.share.theme.Evergreen
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumerCreateFundraiserScreen(
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val authRepository = RepositoryProvider.authRepository
    val fundraiserRepository = RepositoryProvider.fundraiserRepository

    val currentUser by authRepository.currentUser.collectAsStateWithLifecycle(initialValue = null)
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var story by remember { mutableStateOf("") }
    var targetAmountStr by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var creatorName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Medical Emergency") }
    var raiseAsNgo by remember { mutableStateOf(false) }
    var governmentIdUri by remember { mutableStateOf<Uri?>(null) }
    var ngoRegUri by remember { mutableStateOf<Uri?>(null) }
    var panUri by remember { mutableStateOf<Uri?>(null) }
    var bankUri by remember { mutableStateOf<Uri?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    val categories = listOf(
        "Medical Emergency",
        "Education",
        "Flood Relief",
        "Animal Rescue",
        "Women Empowerment",
        "Food Distribution"
    )

    LaunchedEffect(currentUser) {
        currentUser?.let {
            creatorName = it.name
            location = "New Delhi, Delhi"
        }
    }

    val governmentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        governmentIdUri = it
    }
    val ngoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        ngoRegUri = it
    }
    val panLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        panUri = it
    }
    val bankLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        bankUri = it
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Start a fundraiser") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = { WhatsAppSupportFab(source = "fundraiser setup help") }
    ) { paddingValues ->
        ShareBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (successMessage.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    SectionCard(modifier = Modifier.padding(20.dp)) {
                        SectionHeading(title = "Campaign submitted", subtitle = successMessage)
                        PrimaryAppButton(
                            text = "Return to dashboard",
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        HeroCard(
                            eyebrow = "Polished fundraising",
                            title = "Launch a cause people can trust",
                            subtitle = "This flow is redesigned to feel more like a real donor platform: clearer structure, better trust cues, and less clutter."
                        )
                    }

                    item {
                        SectionCard {
                            SectionHeading(title = "Who is raising funds?")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SelectablePill(text = "Individual", selected = !raiseAsNgo, onClick = { raiseAsNgo = false })
                                SelectablePill(text = "Registered NGO", selected = raiseAsNgo, onClick = { raiseAsNgo = true })
                            }
                        }
                    }

                    item {
                        SectionCard {
                            SectionHeading(title = "Campaign details")
                            ConsumerFundraiserField(value = title, onValueChange = { title = it }, label = "Campaign title")
                            ConsumerFundraiserField(value = targetAmountStr, onValueChange = { targetAmountStr = it }, label = "Target amount (₹)")
                            ConsumerFundraiserField(value = story, onValueChange = { story = it }, label = "Campaign story", minLines = 5)
                            ConsumerFundraiserField(value = creatorName, onValueChange = { creatorName = it }, label = if (raiseAsNgo) "NGO name" else "Organizer name")
                            ConsumerFundraiserField(value = location, onValueChange = { location = it }, label = "Campaign location")
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), userScrollEnabled = false) {
                                item { Text("Category", style = MaterialTheme.typography.titleSmall) }
                                item {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        categories.forEach { category ->
                                            SelectablePill(
                                                text = category,
                                                selected = selectedCategory == category,
                                                onClick = { selectedCategory = category }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        SectionCard {
                            SectionHeading(
                                title = "Trust and verification",
                                subtitle = "Only verified campaigns go public. Upload the right documents to speed up review."
                            )
                            ConsumerUploadRow(
                                title = if (raiseAsNgo) "NGO registration certificate" else "Government ID",
                                fileName = rememberFileName(context, if (raiseAsNgo) ngoRegUri else governmentIdUri),
                                onClick = {
                                    if (raiseAsNgo) ngoLauncher.launch("*/*") else governmentLauncher.launch("*/*")
                                }
                            )
                            ConsumerUploadRow(
                                title = "PAN card verification",
                                fileName = rememberFileName(context, panUri),
                                onClick = { panLauncher.launch("*/*") }
                            )
                            ConsumerUploadRow(
                                title = "Bank verification document",
                                fileName = rememberFileName(context, bankUri),
                                onClick = { bankLauncher.launch("*/*") }
                            )
                        }
                    }

                    item {
                        InlineInfoBanner(
                            title = "Review-first publishing",
                            body = "The redesigned flow makes it explicit that public visibility only starts after the Jan Manas Foundation review step."
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
                            text = if (isSubmitting) "Submitting campaign..." else "Submit fundraiser for review",
                            onClick = {
                                val target = targetAmountStr.toDoubleOrNull() ?: 0.0
                                val hasRequiredDocs = if (raiseAsNgo) {
                                    ngoRegUri != null && panUri != null && bankUri != null
                                } else {
                                    governmentIdUri != null && panUri != null && bankUri != null
                                }

                                if (title.isBlank() || story.isBlank() || creatorName.isBlank() || location.isBlank() || target <= 0.0) {
                                    errorMessage = "Fill in all campaign details and enter a valid target amount."
                                } else if (!hasRequiredDocs) {
                                    errorMessage = "Upload all required verification documents before submitting."
                                } else {
                                    errorMessage = ""
                                    isSubmitting = true
                                    coroutineScope.launch {
                                        val documents = mutableListOf<String>()
                                        try {
                                            if (!raiseAsNgo) {
                                                governmentIdUri?.let { FirebaseStorageService.uploadFile("kyc", it).getOrThrow().also(documents::add) }
                                            } else {
                                                ngoRegUri?.let { FirebaseStorageService.uploadFile("kyc", it).getOrThrow().also(documents::add) }
                                            }
                                            panUri?.let { FirebaseStorageService.uploadFile("kyc", it).getOrThrow().also(documents::add) }
                                            bankUri?.let { FirebaseStorageService.uploadFile("kyc", it).getOrThrow().also(documents::add) }
                                        } catch (e: Exception) {
                                            isSubmitting = false
                                            errorMessage = "We could not upload one of the verification files: ${e.message}"
                                            return@launch
                                        }

                                        val result = fundraiserRepository.createFundraiser(
                                            title = title,
                                            story = story,
                                            targetAmount = target,
                                            coverPhotoUrl = "",
                                            location = location,
                                            category = selectedCategory,
                                            creatorName = creatorName + if (raiseAsNgo) " (NGO)" else " (Individual)",
                                            documents = documents
                                        )
                                        isSubmitting = false
                                        result.onSuccess {
                                            successMessage = "Your campaign is now in the review queue. Once the trust check is complete, it can appear publicly in the app."
                                        }.onFailure {
                                            errorMessage = it.message ?: "We could not submit your fundraiser right now."
                                        }
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

                    item { SupportStrip(source = "fundraiser submission support") }
                }
            }
        }
    }
}

@Composable
private fun ConsumerFundraiserField(
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

@Composable
private fun ConsumerUploadRow(
    title: String,
    fileName: String?,
    onClick: () -> Unit
) {
    SectionCard(contentPadding = PaddingValues(16.dp), modifier = Modifier.fillMaxWidth()) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        Text(
            text = fileName ?: "No file attached yet",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SecondaryAppButton(
            text = if (fileName == null) "Attach file" else "Replace file",
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun rememberFileName(context: Context, uri: Uri?): String? {
    if (uri == null) return null
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                return cursor.getString(index)
            }
        }
    }
    return uri.lastPathSegment
}
