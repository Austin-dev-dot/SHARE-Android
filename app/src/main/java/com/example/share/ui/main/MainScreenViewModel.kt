package com.example.share.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.share.data.AuthRepository
import com.example.share.data.FundraiserRepository
import com.example.share.data.PickupRepository
import com.example.share.data.RepositoryProvider
import com.example.share.data.VolunteerRepository
import com.example.share.data.models.Fundraiser
import com.example.share.data.models.PickupRequest
import com.example.share.data.models.User
import com.example.share.data.models.Volunteer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

sealed interface MainScreenUiState {
    object Loading : MainScreenUiState
    
    data class Success(
        val currentUser: User?,
        val fundraisers: List<Fundraiser>,
        val pickupRequests: List<PickupRequest>,
        val volunteers: List<Volunteer>,
        val currentTab: MainTab = MainTab.HOME
    ) : MainScreenUiState
    
    data class Error(val message: String) : MainScreenUiState
}

enum class MainTab {
    HOME,
    FUNDRAISERS,
    VOLUNTEER
}

class MainScreenViewModel(
    private val authRepository: AuthRepository = RepositoryProvider.authRepository,
    private val pickupRepository: PickupRepository = RepositoryProvider.pickupRepository,
    private val fundraiserRepository: FundraiserRepository = RepositoryProvider.fundraiserRepository,
    private val volunteerRepository: VolunteerRepository = RepositoryProvider.volunteerRepository
) : ViewModel() {

    private val _currentTab = MutableStateFlow(MainTab.HOME)

    val uiState: StateFlow<MainScreenUiState> = combine(
        authRepository.currentUser,
        fundraiserRepository.fundraisers,
        pickupRepository.pickupRequests,
        volunteerRepository.volunteers,
        _currentTab
    ) { user, funds, pickups, vols, tab ->
        MainScreenUiState.Success(
            currentUser = user,
            fundraisers = funds,
            pickupRequests = pickups,
            volunteers = vols,
            currentTab = tab
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainScreenUiState.Loading
    )

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }
}
