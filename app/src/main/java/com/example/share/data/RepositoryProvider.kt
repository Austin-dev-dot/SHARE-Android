package com.example.share.data

object RepositoryProvider {
    val authRepository: AuthRepository by lazy { FirebaseAuthRepository() }
    val pickupRepository: PickupRepository by lazy { FirebasePickupRepository() }
    val fundraiserRepository: FundraiserRepository by lazy { FirebaseFundraiserRepository() }
    val volunteerRepository: VolunteerRepository by lazy { FirebaseVolunteerRepository() }
}
