package com.example.share.ui.volunteer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
fun ConsumerVolunteerRegisterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val authRepository = RepositoryProvider.authRepository
    val volunteerRepository = RepositoryProvider.volunteerRepository

    val currentUser by authRepository.currentUser.collectAsStateWithLifecycle(initialValue = null)

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("Weekends") }
    var emergencyContact by remember { mutableStateOf("") }
    var previousExperience by remember { mutableStateOf("") }
    val selectedSkills = remember { mutableStateListOf<String>() }
    val selectedLanguages = remember { mutableStateListOf<String>() }
    val selectedAreas = remember { mutableStateListOf<String>() }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val skills = listOf("Teaching", "First Aid", "Event Support", "Food Distribution", "Relief Work", "Social Media")
    val languages = listOf("English", "Hindi", "Bengali", "Tamil", "Telugu", "Kannada")
    val areas = listOf("Education", "Flood Relief", "Medical Emergency", "Food Distribution", "Animal Rescue", "Women Empowerment")
    val availabilityOptions = listOf("Weekends", "Weekdays", "Flexible", "Emergency only")

    LaunchedEffect(currentUser) {
        currentUser?.let {
            name = it.name
            phone = it.phone
            email = it.email
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Volunteer registration") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = { WhatsAppSupportFab(source = "volunteer registration support") }
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
                        eyebrow = "People-powered impact",
                        title = "Join the volunteer network",
                        subtitle = "This form is redesigned to feel less bureaucratic and more like a real onboarding flow for community action."
                    )
                }

                item {
                    SectionCard {
                        SectionHeading(title = "About you")
                        VolunteerField(value = name, onValueChange = { name = it }, label = "Full name")
                        VolunteerField(value = phone, onValueChange = { phone = it }, label = "Phone number")
                        VolunteerField(value = email, onValueChange = { email = it }, label = "Email address")
                        VolunteerField(value = ageStr, onValueChange = { ageStr = it }, label = "Age")
                        VolunteerField(value = occupation, onValueChange = { occupation = it }, label = "Occupation")
                    }
                }

                item {
                    SectionCard {
                        SectionHeading(title = "Location and availability")
                        VolunteerField(value = city, onValueChange = { city = it }, label = "City")
                        VolunteerField(value = state, onValueChange = { state = it }, label = "State")
                        VolunteerField(value = emergencyContact, onValueChange = { emergencyContact = it }, label = "Emergency contact")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            availabilityOptions.forEach { option ->
                                SelectablePill(
                                    text = option,
                                    selected = availability == option,
                                    onClick = { availability = option }
                                )
                            }
                        }
                    }
                }

                item {
                    SectionCard {
                        SectionHeading(title = "Skills and preferences")
                        VolunteerChipGroup(title = "Skills", options = skills, selected = selectedSkills)
                        VolunteerChipGroup(title = "Languages", options = languages, selected = selectedLanguages)
                        VolunteerChipGroup(title = "Preferred areas", options = areas, selected = selectedAreas)
                        VolunteerField(
                            value = previousExperience,
                            onValueChange = { previousExperience = it },
                            label = "Previous experience",
                            minLines = 3
                        )
                    }
                }

                item {
                    InlineInfoBanner(
                        title = "Clearer next steps",
                        body = "Once submitted, your profile can move into review and then to active status. The app now makes that journey much easier to understand."
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
                        text = if (isSubmitting) "Submitting registration..." else "Submit registration",
                        onClick = {
                            val age = ageStr.toIntOrNull() ?: 0
                            if (name.isBlank() || phone.isBlank() || email.isBlank() || age <= 0 || city.isBlank() || state.isBlank() || selectedSkills.isEmpty() || selectedLanguages.isEmpty() || selectedAreas.isEmpty()) {
                                errorMessage = "Complete the form and choose at least one skill, language, and focus area."
                            } else {
                                errorMessage = ""
                                isSubmitting = true
                                coroutineScope.launch {
                                    val result = volunteerRepository.registerVolunteer(
                                        name = name,
                                        phone = phone,
                                        email = email,
                                        age = age,
                                        gender = "Prefer not to say",
                                        occupation = occupation,
                                        city = city,
                                        state = state,
                                        skills = selectedSkills.toList(),
                                        languages = selectedLanguages.toList(),
                                        availability = availability,
                                        emergencyContact = emergencyContact,
                                        previousExperience = previousExperience,
                                        preferredAreas = selectedAreas.toList(),
                                        isAgreedToTerms = true
                                    )
                                    isSubmitting = false
                                    result.onSuccess { onNavigateToDetail(it.id) }
                                        .onFailure { errorMessage = it.message ?: "We could not submit your registration right now." }
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

                item { SupportStrip(source = "volunteer onboarding support") }
            }
        }
    }
}

@Composable
private fun VolunteerChipGroup(
    title: String,
    options: List<String>,
    selected: MutableList<String>
) {
    Text(text = title, style = MaterialTheme.typography.titleSmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            SelectablePill(
                text = option,
                selected = selected.contains(option),
                onClick = {
                    if (selected.contains(option)) selected.remove(option) else selected.add(option)
                }
            )
        }
    }
}

@Composable
private fun VolunteerField(
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
        minLines = minLines,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Evergreen,
            unfocusedBorderColor = BorderSoft
        )
    )
}
