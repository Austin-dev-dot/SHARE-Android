package com.example.share.ui.volunteer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WorkspacePremium
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
import com.example.share.theme.SlateMedium

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerDetailScreen(
    volunteerId: String,
    onNavigateBack: () -> Unit
) {
    val volunteerRepository = RepositoryProvider.volunteerRepository
    val volunteers by volunteerRepository.volunteers.collectAsStateWithLifecycle(initialValue = emptyList())
    val volunteer = volunteers.firstOrNull { it.id == volunteerId }

    var certificateStatusMsg by remember { mutableStateOf("") }
    var isCertError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Volunteer Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    IconButton(onClick = {
                        volunteer?.let { vol ->
                            val sendIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, "I just registered as a Volunteer on SHARE! My Volunteer ID: ${vol.id}")
                                type = "text/plain"
                            }
                            val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        if (volunteer == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading volunteer profile...")
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
                // Header Profile Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(GoFundMeGreenLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    volunteer.name.take(2).uppercase(),
                                    style = MaterialTheme.typography.headlineMedium.copy(color = GoFundMeGreen, fontWeight = FontWeight.Bold)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(volunteer.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                            Text("Volunteer ID: ${volunteer.id}", style = MaterialTheme.typography.bodySmall.copy(color = SlateLight))
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF9F9F9), RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "${volunteer.hoursTracked} hrs",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GoFundMeGreen)
                                    )
                                    Text("Hours Logged", style = MaterialTheme.typography.bodySmall.copy(color = SlateLight))
                                }
                                Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color(0xFFEEEEEE)))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (volunteer.status == "Active") GoFundMeGreenLight else Color(0xFFFFF9C4))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            volunteer.status,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (volunteer.status == "Active") GoFundMeGreen else Color(0xFFF57F17),
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                    Text("Status", style = MaterialTheme.typography.bodySmall.copy(color = SlateLight))
                                }
                            }
                        }
                    }
                }

                // Status info box
                if (volunteer.status == "Pending") {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(Icons.Default.Info, contentDescription = "Pending Status", tint = Color(0xFFF57F17))
                                Column {
                                    Text("Pending Approval", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFFF57F17)))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("The Jan Manas Foundation is reviewing your registration documents. Once approved by the NGO team on the Web Admin Dashboard, your profile status will update to Active.", style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray, lineHeight = 16.sp))
                                }
                            }
                        }
                    }
                }

                // Volunteer Details List
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Profile Information", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            
                            VolunteerDetailRow(label = "Email Address", value = volunteer.email)
                            VolunteerDetailRow(label = "Phone Number", value = volunteer.phone)
                            VolunteerDetailRow(label = "Age & Gender", value = "${volunteer.age} | ${volunteer.gender}")
                            VolunteerDetailRow(label = "Occupation", value = volunteer.occupation)
                            VolunteerDetailRow(label = "Location", value = "${volunteer.city}, ${volunteer.state}")
                            VolunteerDetailRow(label = "Availability", value = volunteer.availability)
                            VolunteerDetailRow(label = "Emergency Contact", value = volunteer.emergencyContact)
                            VolunteerDetailRow(label = "Skills", value = volunteer.skills.joinToString(", "))
                            VolunteerDetailRow(label = "Languages", value = volunteer.languages.joinToString(", "))
                            VolunteerDetailRow(label = "Preferred Fields", value = volunteer.preferredAreas.joinToString(", "))
                            
                            if (volunteer.previousExperience.isNotEmpty()) {
                                VolunteerDetailRow(label = "Previous Experience", value = volunteer.previousExperience)
                            }
                        }
                    }
                }

                // Certificate Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Work Certificate", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("A completion certificate will be generated when the volunteer completes more than 10 hours of logged community service.", style = MaterialTheme.typography.bodySmall.copy(color = SlateLight), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            if (volunteer.hoursTracked >= 10) {
                                Button(
                                    onClick = {
                                        certificateStatusMsg = "Certificate generated and downloaded successfully. Verified by UID ${volunteer.id}."
                                        isCertError = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoFundMeGreen),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.WorkspacePremium, contentDescription = "Award")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Download Certificate")
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        certificateStatusMsg = "Need at least 10 logged hours. (Simulate adding hours by toggling Admin Mode, selecting the Admin tab, and logging hours under the Volunteers list)."
                                        isCertError = true
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateLight)
                                ) {
                                    Text("Certificate Locked (${volunteer.hoursTracked}/10h)")
                                }
                            }

                            if (certificateStatusMsg.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = certificateStatusMsg,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isCertError) MaterialTheme.colorScheme.error else GoFundMeGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VolunteerDetailRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall.copy(color = SlateLight))
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = Color.DarkGray))
        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(color = Color(0xFFF1F1F1))
    }
}
