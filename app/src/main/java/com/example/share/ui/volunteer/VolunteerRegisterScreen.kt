package com.example.share.ui.volunteer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.share.data.RepositoryProvider
import com.example.share.theme.GoFundMeGreen
import com.example.share.theme.GoFundMeGreenLight
import com.example.share.theme.SlateLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerRegisterScreen(
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
    var gender by remember { mutableStateOf("Male") }
    var occupation by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("Weekends") }
    var emergencyContact by remember { mutableStateOf("") }
    var previousExperience by remember { mutableStateOf("") }
    var isAgreedToTerms by remember { mutableStateOf(false) }

    val genders = listOf("Male", "Female", "Other")
    var isGenderDropdownExpanded by remember { mutableStateOf(false) }
    
    val availabilities = listOf("Weekends", "Weekdays", "Flexible / Remote", "On Call / Emergencies")
    var isAvailabilityDropdownExpanded by remember { mutableStateOf(false) }

    // Auto-fill details if logged in
    LaunchedEffect(currentUser) {
        currentUser?.let {
            name = it.name
            phone = it.phone
            email = it.email
        }
    }

    // Skills
    val availableSkills = listOf("Teaching", "Social Media", "First Aid", "Web Development", "Event Management", "Disaster Relief", "Food Distribution", "Logistic Support")
    val selectedSkills = remember { mutableStateListOf<String>() }

    // Languages
    val availableLanguages = listOf("English", "Hindi", "Bengali", "Telugu", "Tamil", "Kannada", "Malayalam")
    val selectedLanguages = remember { mutableStateListOf<String>() }

    // Preferred Areas
    val availableAreas = listOf("Animal Rescue", "Education", "Flood Relief", "Food Distribution", "Medical Emergency", "Women Empowerment")
    val selectedAreas = remember { mutableStateListOf<String>() }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Volunteer Registration", fontWeight = FontWeight.Bold) },
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
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Info Card
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
                        Text("General Information", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Phone") },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen)
                            )
                            
                            OutlinedTextField(
                                value = ageStr,
                                onValueChange = { ageStr = it },
                                label = { Text("Age") },
                                modifier = Modifier.weight(0.8f),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen)
                            )
                        }

                        // Gender Dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = gender,
                                onValueChange = {},
                                label = { Text("Gender") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                readOnly = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGenderDropdownExpanded) }
                            )
                            Box(modifier = Modifier.matchParentSize().clickable { isGenderDropdownExpanded = !isGenderDropdownExpanded })
                            DropdownMenu(
                                expanded = isGenderDropdownExpanded,
                                onDismissRequest = { isGenderDropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                genders.forEach { g ->
                                    DropdownMenuItem(
                                        text = { Text(g) },
                                        onClick = {
                                            gender = g
                                            isGenderDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen)
                        )

                        OutlinedTextField(
                            value = occupation,
                            onValueChange = { occupation = it },
                            label = { Text("Occupation") },
                            placeholder = { Text("e.g. Student, Engineer, Teacher") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen)
                        )
                    }
                }
            }

            // Location & Availability
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
                        Text("Location & Availability", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("City") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen)
                            )
                            
                            OutlinedTextField(
                                value = state,
                                onValueChange = { state = it },
                                label = { Text("State") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen)
                            )
                        }

                        // Availability Dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = availability,
                                onValueChange = {},
                                label = { Text("Availability") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                readOnly = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isAvailabilityDropdownExpanded) }
                            )
                            Box(modifier = Modifier.matchParentSize().clickable { isAvailabilityDropdownExpanded = !isAvailabilityDropdownExpanded })
                            DropdownMenu(
                                expanded = isAvailabilityDropdownExpanded,
                                onDismissRequest = { isAvailabilityDropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                availabilities.forEach { a ->
                                    DropdownMenuItem(
                                        text = { Text(a) },
                                        onClick = {
                                            availability = a
                                            isAvailabilityDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = emergencyContact,
                            onValueChange = { emergencyContact = it },
                            label = { Text("Emergency Contact (Name & Phone)") },
                            placeholder = { Text("e.g. Spouse: +919876543200") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen)
                        )
                    }
                }
            }

            // Skills & Preferences
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
                        Text("Skills & Experience", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        
                        Text("Select your Skills:", style = MaterialTheme.typography.bodySmall.copy(color = SlateLight))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableSkills.forEach { skill ->
                                val isSelected = selectedSkills.contains(skill)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) GoFundMeGreenLight else Color(0xFFF1F1F1))
                                        .clickable { if (isSelected) selectedSkills.remove(skill) else selectedSkills.add(skill) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(skill, style = MaterialTheme.typography.bodyMedium.copy(color = if (isSelected) GoFundMeGreen else Color.DarkGray, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal))
                                }
                            }
                        }

                        Text("Languages Spoken:", style = MaterialTheme.typography.bodySmall.copy(color = SlateLight))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableLanguages.forEach { lang ->
                                val isSelected = selectedLanguages.contains(lang)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) GoFundMeGreenLight else Color(0xFFF1F1F1))
                                        .clickable { if (isSelected) selectedLanguages.remove(lang) else selectedLanguages.add(lang) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(lang, style = MaterialTheme.typography.bodyMedium.copy(color = if (isSelected) GoFundMeGreen else Color.DarkGray, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal))
                                }
                            }
                        }

                        Text("Preferred Volunteer Areas:", style = MaterialTheme.typography.bodySmall.copy(color = SlateLight))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableAreas.forEach { area ->
                                val isSelected = selectedAreas.contains(area)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) GoFundMeGreenLight else Color(0xFFF1F1F1))
                                        .clickable { if (isSelected) selectedAreas.remove(area) else selectedAreas.add(area) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(area, style = MaterialTheme.typography.bodyMedium.copy(color = if (isSelected) GoFundMeGreen else Color.DarkGray, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal))
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        OutlinedTextField(
                            value = previousExperience,
                            onValueChange = { previousExperience = it },
                            label = { Text("Previous Experience (Optional)") },
                            placeholder = { Text("Mention any ngo volunteer work you have done before...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoFundMeGreen)
                        )
                    }
                }
            }

            // Agreement
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = isAgreedToTerms,
                        onCheckedChange = { isAgreedToTerms = it },
                        colors = CheckboxDefaults.colors(checkedColor = GoFundMeGreen)
                    )
                    Text("I agree to the volunteer terms & service code of conduct.", style = MaterialTheme.typography.bodyMedium.copy(color = Color.DarkGray))
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
                        val age = ageStr.toIntOrNull() ?: 0
                        if (name.isBlank() || phone.isBlank() || email.isBlank() || age <= 0 || city.isBlank() || state.isBlank() || selectedSkills.isEmpty() || selectedLanguages.isEmpty() || selectedAreas.isEmpty() || !isAgreedToTerms) {
                            errorMessage = "Please fill in all details, select at least one skill/language/area, and agree to the terms."
                            return@Button
                        }
                        errorMessage = ""
                        isSubmitting = true
                        
                        coroutineScope.launch {
                            val result = volunteerRepository.registerVolunteer(
                                name = name,
                                phone = phone,
                                email = email,
                                age = age,
                                gender = gender,
                                occupation = occupation,
                                city = city,
                                state = state,
                                skills = selectedSkills.toList(),
                                languages = selectedLanguages.toList(),
                                availability = availability,
                                emergencyContact = emergencyContact,
                                previousExperience = previousExperience,
                                preferredAreas = selectedAreas.toList(),
                                isAgreedToTerms = isAgreedToTerms
                            )
                            isSubmitting = false
                            result.onSuccess { newVol ->
                                onNavigateToDetail(newVol.id)
                            }.onFailure {
                                errorMessage = it.message ?: "Failed to register"
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
                        Text("Submit Registration", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
