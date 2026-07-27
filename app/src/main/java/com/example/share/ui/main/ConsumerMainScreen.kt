package com.example.share.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.share.CreateFundraiser
import com.example.share.FundraiserDetail
import com.example.share.PickupDetail
import com.example.share.PickupForm
import com.example.share.VolunteerDetail
import com.example.share.VolunteerRegister
import com.example.share.data.models.Fundraiser
import com.example.share.data.models.PickupRequest
import com.example.share.data.models.PickupStatus
import com.example.share.data.models.User
import com.example.share.data.models.Volunteer
import com.example.share.theme.BorderSoft
import com.example.share.theme.Evergreen
import com.example.share.theme.EvergreenDeep
import com.example.share.theme.MintWash
import com.example.share.theme.SkyWash
import com.example.share.theme.Linen
import com.example.share.ui.common.BalancedRow
import com.example.share.ui.common.DetailLine
import com.example.share.ui.common.HeroCard
import com.example.share.ui.common.InlineInfoBanner
import com.example.share.ui.common.MetricPill
import com.example.share.ui.common.PrimaryAppButton
import com.example.share.ui.common.SecondaryAppButton
import com.example.share.ui.common.SectionCard
import com.example.share.ui.common.SectionHeading
import com.example.share.ui.common.SelectablePill
import com.example.share.ui.common.ShareBackground
import com.example.share.ui.common.SupportStrip
import com.example.share.ui.common.WhatsAppSupportFab

@Composable
fun ConsumerMainScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (state is MainScreenUiState.Success) {
                WhatsAppSupportFab(source = "the SHARE home screen")
            }
        },
        bottomBar = {
            if (state is MainScreenUiState.Success) {
                ConsumerMainTabBar(
                    currentTab = (state as MainScreenUiState.Success).currentTab,
                    onTabSelected = viewModel::selectTab
                )
            }
        }
    ) { paddingValues ->
        ShareBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val currentState = state) {
                MainScreenUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Evergreen)
                    }
                }

                is MainScreenUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        SectionCard(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "We couldn’t load your dashboard right now.",
                                style = MaterialTheme.typography.titleLarge,
                                color = EvergreenDeep
                            )
                            Text(
                                text = currentState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is MainScreenUiState.Success -> {
                    when (currentState.currentTab) {
                        MainTab.HOME -> ConsumerHomeTabContent(state = currentState, onItemClick = onItemClick)
                        MainTab.FUNDRAISERS -> ConsumerFundraisersTabContent(
                            fundraisers = currentState.fundraisers,
                            onItemClick = onItemClick
                        )
                        MainTab.VOLUNTEER -> ConsumerVolunteerTabContent(
                            volunteers = currentState.volunteers,
                            currentUser = currentState.currentUser,
                            onItemClick = onItemClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsumerMainTabBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    Surface(color = Color.White) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(
                MainTab.HOME to "Home",
                MainTab.FUNDRAISERS to "Fundraisers",
                MainTab.VOLUNTEER to "Volunteer"
            ).forEach { (tab, label) ->
                SelectablePill(
                    text = label,
                    selected = currentTab == tab,
                    modifier = Modifier.weight(1f),
                    onClick = { onTabSelected(tab) }
                )
            }
        }
    }
}

@Composable
private fun ConsumerHomeTabContent(
    state: MainScreenUiState.Success,
    onItemClick: (NavKey) -> Unit
) {
    val userPickups = state.pickupRequests.filter {
        it.email == state.currentUser?.email || it.phone == state.currentUser?.phone
    }
    val verifiedFundraisers = state.fundraisers.filter { it.isVerified }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeroCard(
                eyebrow = "Jan Manas Foundation",
                title = "Welcome back, ${state.currentUser?.name?.substringBefore(" ") ?: "friend"}",
                subtitle = "A calmer place to give. Discover verified fundraisers, schedule pickups, and stay close to real-world impact."
            ) {
                BalancedRow {
                    MetricPill(value = verifiedFundraisers.size.toString(), label = "live appeals", modifier = Modifier.weight(1f))
                    MetricPill(value = state.volunteers.count { it.status == "Active" }.toString(), label = "active volunteers", modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            SectionHeading(
                title = "Choose how you want to help",
                subtitle = "Every path is designed to feel straightforward, trustworthy, and human."
            )
        }

        item {
            BalancedRow {
                ConsumerActionCard(
                    title = "Schedule a pickup",
                    body = "Donate clothes, books, toys, and essentials from your doorstep.",
                    kicker = "Donation pickup",
                    backgroundColor = SkyWash,
                    modifier = Modifier.weight(1f),
                    onClick = { onItemClick(PickupForm) }
                )
                ConsumerActionCard(
                    title = "Start a fundraiser",
                    body = "Launch a polished campaign and submit documents for verification.",
                    kicker = "Raise support",
                    backgroundColor = Linen,
                    modifier = Modifier.weight(1f),
                    onClick = { onItemClick(CreateFundraiser) }
                )
            }
        }

        item {
            ConsumerActionCard(
                title = "Join the volunteer network",
                body = "Support drives, relief work, and local delivery efforts where community matters most.",
                kicker = "Volunteer",
                backgroundColor = MintWash,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onItemClick(VolunteerRegister) }
            )
        }

        if (userPickups.isNotEmpty()) {
            item {
                SectionHeading(
                    title = "Your latest pickups",
                    subtitle = "Track each request without digging through menus."
                )
            }
            items(userPickups.take(3)) { pickup ->
                ConsumerPickupTrackingCard(
                    pickup = pickup,
                    onClick = { onItemClick(PickupDetail(pickup.id)) }
                )
            }
        }

        if (verifiedFundraisers.isNotEmpty()) {
            item {
                SectionHeading(
                    title = "Verified campaigns",
                    subtitle = "A few urgent causes people are rallying behind right now."
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(verifiedFundraisers.take(5)) { fundraiser ->
                        ConsumerFeaturedCampaignCard(
                            fundraiser = fundraiser,
                            onClick = { onItemClick(FundraiserDetail(fundraiser.id)) }
                        )
                    }
                }
            }
        }

        item {
            SectionCard {
                SectionHeading(
                    title = "What trust looks like here",
                    subtitle = "Every public campaign passes a document review before it appears inside SHARE."
                )
                BalancedRow {
                    MetricPill(value = "Reviewed", label = "documents", modifier = Modifier.weight(1f))
                    MetricPill(value = "Transparent", label = "progress", modifier = Modifier.weight(1f))
                    MetricPill(value = "Direct", label = "updates", modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            SectionCard {
                SectionHeading(
                    title = "Recent impact",
                    subtitle = "A cleaner storytelling layer makes the app feel alive, not transactional."
                )
                ConsumerStoryLine(
                    title = "Flood relief kits reached families in Assam",
                    body = "Recent donations supported ration kits, blankets, and water purification supplies for households affected by flooding."
                )
                ConsumerStoryLine(
                    title = "Medical giving moved faster this week",
                    body = "Supporters helped one family cross a critical treatment milestone after a verified campaign update."
                )
            }
        }

        item { SupportStrip(source = "home support") }
    }
}

@Composable
private fun ConsumerActionCard(
    title: String,
    body: String,
    kicker: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    SectionCard(
        modifier = modifier.clickable(onClick = onClick),
        containerColor = backgroundColor
    ) {
        SelectablePill(text = kicker, selected = true)
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = EvergreenDeep)
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = "Open", style = MaterialTheme.typography.labelLarge, color = Evergreen)
    }
}

@Composable
private fun ConsumerPickupTrackingCard(
    pickup: PickupRequest,
    onClick: () -> Unit
) {
    SectionCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = pickup.items.joinToString(", "),
                    style = MaterialTheme.typography.titleMedium,
                    color = EvergreenDeep,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${pickup.date} • ${pickup.time}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = pickup.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            ConsumerStatusBadge(status = pickup.status)
        }
    }
}

@Composable
private fun ConsumerStatusBadge(status: PickupStatus) {
    val (background, textColor, label) = when (status) {
        PickupStatus.PENDING -> Triple(Color(0xFFFFF5D7), Color(0xFF9A6700), "Pending")
        PickupStatus.ASSIGNED -> Triple(SkyWash, Color(0xFF1D4ED8), "Assigned")
        PickupStatus.PICKED_UP -> Triple(MintWash, Evergreen, "Picked up")
        PickupStatus.DELIVERED -> Triple(Color(0xFFE6FFFB), Color(0xFF0F766E), "Delivered")
        PickupStatus.COMPLETED -> Triple(MintWash, EvergreenDeep, "Completed")
    }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(background)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = textColor)
    }
}

@Composable
private fun ConsumerFeaturedCampaignCard(
    fundraiser: Fundraiser,
    onClick: () -> Unit
) {
    SectionCard(
        modifier = Modifier
            .width(290.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectablePill(text = fundraiser.category, selected = false)
            Text(
                text = fundraiser.location,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = fundraiser.title,
            style = MaterialTheme.typography.titleLarge,
            color = EvergreenDeep,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = fundraiser.story,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        LinearProgressIndicator(
            progress = { fundraiser.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = Evergreen,
            trackColor = BorderSoft
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = consumerCurrency(fundraiser.raisedAmount), style = MaterialTheme.typography.titleMedium, color = Evergreen)
                Text(
                    text = "raised of ${consumerCurrency(fundraiser.targetAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${(fundraiser.progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                color = EvergreenDeep
            )
        }
    }
}

@Composable
private fun ConsumerStoryLine(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = EvergreenDeep)
        Text(text = body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ConsumerFundraisersTabContent(
    fundraisers: List<Fundraiser>,
    onItemClick: (NavKey) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val categories = remember(fundraisers) {
        listOf("All") + fundraisers.map { it.category }.distinct().sorted()
    }
    var selectedCategory by remember(categories) { mutableStateOf(categories.firstOrNull() ?: "All") }

    val filteredFundraisers = fundraisers.filter {
        val matchesSearch = it.title.contains(searchQuery, ignoreCase = true) || it.story.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "All" || it.category == selectedCategory
        it.isVerified && matchesSearch && matchesCategory
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeroCard(
                eyebrow = "Verified fundraising",
                title = "Give with more clarity",
                subtitle = "Browse causes with stronger structure, clearer progress, and a more trustworthy feel throughout."
            )
        }

        item {
            SectionCard {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search campaigns") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Evergreen,
                        unfocusedBorderColor = BorderSoft
                    )
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { category ->
                        SelectablePill(
                            text = category,
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }
            }
        }

        if (filteredFundraisers.isEmpty()) {
            item {
                InlineInfoBanner(
                    title = "No campaigns matched your search",
                    body = "Try a broader keyword or switch categories to see more verified causes."
                )
            }
        } else {
            items(filteredFundraisers) { fundraiser ->
                ConsumerCampaignListCard(
                    fundraiser = fundraiser,
                    onClick = { onItemClick(FundraiserDetail(fundraiser.id)) }
                )
            }
        }

        item { SupportStrip(source = "fundraiser browsing") }
    }
}

@Composable
private fun ConsumerCampaignListCard(
    fundraiser: Fundraiser,
    onClick: () -> Unit
) {
    SectionCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectablePill(text = fundraiser.category, selected = false)
            Text(
                text = fundraiser.location,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(text = fundraiser.title, style = MaterialTheme.typography.titleLarge, color = EvergreenDeep)
        Text(
            text = fundraiser.story,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        LinearProgressIndicator(
            progress = { fundraiser.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = Evergreen,
            trackColor = BorderSoft
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = consumerCurrency(fundraiser.raisedAmount), style = MaterialTheme.typography.titleMedium, color = Evergreen)
                Text(
                    text = "of ${consumerCurrency(fundraiser.targetAmount)} target",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            SecondaryAppButton(text = "View campaign", onClick = onClick)
        }
    }
}

@Composable
private fun ConsumerVolunteerTabContent(
    volunteers: List<Volunteer>,
    currentUser: User?,
    onItemClick: (NavKey) -> Unit
) {
    val myProfile = volunteers.firstOrNull {
        it.email == currentUser?.email || it.phone == currentUser?.phone
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeroCard(
                eyebrow = "Community network",
                title = if (myProfile == null) "Volunteer with confidence" else "Your volunteer profile",
                subtitle = if (myProfile == null) {
                    "Join local drives, support relief operations, and help this app feel powered by real people."
                } else {
                    "Your work, status, and next milestones all live in one cleaner place now."
                }
            )
        }

        if (myProfile == null) {
            item {
                SectionCard {
                    SectionHeading(
                        title = "Why this feels different",
                        subtitle = "The experience is designed to be supportive for first-time volunteers too."
                    )
                    BalancedRow {
                        MetricPill(value = "Flexible", label = "availability", modifier = Modifier.weight(1f))
                        MetricPill(value = "Trusted", label = "matching", modifier = Modifier.weight(1f))
                        MetricPill(value = "Clear", label = "status", modifier = Modifier.weight(1f))
                    }
                    PrimaryAppButton(
                        text = "Register as a volunteer",
                        onClick = { onItemClick(VolunteerRegister) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            item {
                ConsumerVolunteerProfileSummary(profile = myProfile, onItemClick = onItemClick)
            }
        }

        item { SupportStrip(source = "volunteer support") }
    }
}

@Composable
private fun ConsumerVolunteerProfileSummary(
    profile: Volunteer,
    onItemClick: (NavKey) -> Unit
) {
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = profile.name, style = MaterialTheme.typography.titleLarge, color = EvergreenDeep)
                Text(
                    text = "Volunteer ID ${profile.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            ConsumerStatusBadge(status = if (profile.status == "Active") PickupStatus.COMPLETED else PickupStatus.PENDING)
        }
        BalancedRow {
            MetricPill(value = "${profile.hoursTracked}h", label = "logged", modifier = Modifier.weight(1f))
            MetricPill(value = profile.availability, label = "availability", modifier = Modifier.weight(1f))
        }
        DetailLine(label = "Skills", value = profile.skills.joinToString(", "))
        DetailLine(label = "Preferred areas", value = profile.preferredAreas.joinToString(", "))
        PrimaryAppButton(
            text = "Open full volunteer profile",
            onClick = { onItemClick(VolunteerDetail(profile.id)) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun consumerCurrency(amount: Double): String = "₹${amount.toInt()}"
