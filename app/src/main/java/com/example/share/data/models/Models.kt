package com.example.share.data.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val avatarUrl: String = "",
    val isVerified: Boolean = false,
    val isAdmin: Boolean = false
)

enum class PickupStatus {
    PENDING,
    ASSIGNED,
    PICKED_UP,
    DELIVERED,
    COMPLETED
}

@Serializable
data class PickupRequest(
    val id: String,
    val donorName: String,
    val phone: String,
    val email: String,
    val address: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val date: String,
    val time: String,
    val items: List<String>,
    val estimatedQuantity: String,
    val imageUris: List<String> = emptyList(),
    val specialInstructions: String = "",
    val status: PickupStatus = PickupStatus.PENDING
)

@Serializable
data class FundraiserComment(
    val id: String,
    val authorName: String,
    val text: String,
    val timestamp: String
)

@Serializable
data class FundraiserUpdate(
    val id: String,
    val text: String,
    val timestamp: String
)

@Serializable
data class Fundraiser(
    val id: String,
    val coverPhotoUrl: String,
    val title: String,
    val story: String,
    val targetAmount: Double,
    val raisedAmount: Double,
    val progress: Float, // from 0.0f to 1.0f
    val documents: List<String> = emptyList(),
    val upiId: String = "donate@upi",
    val location: String,
    val updates: List<FundraiserUpdate> = emptyList(),
    val comments: List<FundraiserComment> = emptyList(),
    val isVerified: Boolean = false,
    val category: String,
    val creatorName: String
)

@Serializable
data class Volunteer(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val age: Int,
    val gender: String,
    val occupation: String,
    val city: String,
    val state: String,
    val skills: List<String>,
    val languages: List<String>,
    val availability: String,
    val emergencyContact: String,
    val previousExperience: String,
    val preferredAreas: List<String>,
    val isAgreedToTerms: Boolean,
    val joinedDate: String,
    val status: String = "Active", // "Active", "Pending", "Inactive"
    val hoursTracked: Int = 0
)
