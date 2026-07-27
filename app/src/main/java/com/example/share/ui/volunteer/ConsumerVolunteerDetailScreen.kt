package com.example.share.ui.volunteer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.share.data.RepositoryProvider
import com.example.share.theme.Evergreen
import com.example.share.ui.common.BalancedRow
import com.example.share.ui.common.DetailLine
import com.example.share.ui.common.HeroCard
import com.example.share.ui.common.InlineInfoBanner
import com.example.share.ui.common.MetricPill
import com.example.share.ui.common.PrimaryAppButton
import com.example.share.ui.common.SecondaryAppButton
import com.example.share.ui.common.SectionCard
import com.example.share.ui.common.SectionHeading
import com.example.share.ui.common.ShareBackground
import com.example.share.ui.common.SupportStrip
import com.example.share.ui.common.WhatsAppSupportFab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumerVolunteerDetailScreen(
    volunteerId: String,
    onNavigateBack: () -> Unit
) {
    val volunteerRepository = RepositoryProvider.volunteerRepository
    val volunteers by volunteerRepository.volunteers.collectAsStateWithLifecycle(initialValue = emptyList())
    val volunteer = volunteers.firstOrNull { it.id == volunteerId }

    var certificateMessage by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Volunteer profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = { WhatsAppSupportFab(source = "volunteer profile support") }
    ) { paddingValues ->
        ShareBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (volunteer == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading volunteer profile...")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        HeroCard(
                            eyebrow = "Volunteer profile",
                            title = volunteer.name,
                            subtitle = "Volunteer ID ${volunteer.id} • ${volunteer.city}, ${volunteer.state}"
                        ) {
                            BalancedRow {
                                MetricPill(value = "${volunteer.hoursTracked}h", label = "logged", modifier = Modifier.weight(1f))
                                MetricPill(value = volunteer.status, label = "status", modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    if (volunteer.status != "Active") {
                        item {
                            InlineInfoBanner(
                                title = "Review in progress",
                                body = "Your application is still being reviewed. The redesigned status treatment makes that state more understandable and less alarming."
                            )
                        }
                    }

                    item {
                        SectionCard {
                            SectionHeading(title = "Profile details")
                            DetailLine(label = "Email", value = volunteer.email)
                            DetailLine(label = "Phone", value = volunteer.phone)
                            DetailLine(label = "Occupation", value = volunteer.occupation)
                            DetailLine(label = "Availability", value = volunteer.availability)
                            DetailLine(label = "Emergency contact", value = volunteer.emergencyContact)
                            DetailLine(label = "Skills", value = volunteer.skills.joinToString(", "))
                            DetailLine(label = "Languages", value = volunteer.languages.joinToString(", "))
                            DetailLine(label = "Preferred areas", value = volunteer.preferredAreas.joinToString(", "))
                            if (volunteer.previousExperience.isNotBlank()) {
                                DetailLine(label = "Previous experience", value = volunteer.previousExperience)
                            }
                        }
                    }

                    item {
                        SectionCard {
                            SectionHeading(title = "Recognition")
                            Text(
                                text = "Once a volunteer crosses 10 tracked hours, the app can generate a certificate with a much clearer explanation of eligibility.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (volunteer.hoursTracked >= 10) {
                                PrimaryAppButton(
                                    text = "Generate service certificate",
                                    onClick = { certificateMessage = "Certificate generated successfully for ${volunteer.name}." },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                SecondaryAppButton(
                                    text = "Certificate locked (${volunteer.hoursTracked}/10h)",
                                    onClick = { certificateMessage = "Keep volunteering to unlock the service certificate after 10 hours." },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (certificateMessage.isNotBlank()) {
                                Text(
                                    text = certificateMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Evergreen
                                )
                            }
                        }
                    }

                    item { SupportStrip(source = "volunteer detail support") }
                }
            }
        }
    }
}
