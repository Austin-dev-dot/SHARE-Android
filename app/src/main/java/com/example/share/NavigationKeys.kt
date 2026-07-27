package com.example.share

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Main : NavKey
@Serializable data object Login : NavKey
@Serializable data class OtpVerification(val contact: String) : NavKey
@Serializable data class RegisterDetails(val contact: String) : NavKey
@Serializable data object PickupForm : NavKey
@Serializable data class PickupDetail(val requestId: String) : NavKey
@Serializable data class FundraiserDetail(val fundraiserId: String) : NavKey
@Serializable data object CreateFundraiser : NavKey
@Serializable data object VolunteerRegister : NavKey
@Serializable data class VolunteerDetail(val volunteerId: String) : NavKey
