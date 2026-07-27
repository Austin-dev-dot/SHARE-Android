package com.example.share.data

import com.example.share.data.models.PickupRequest
import com.example.share.data.models.PickupStatus
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

interface PickupRepository {
    val pickupRequests: Flow<List<PickupRequest>>
    suspend fun submitPickupRequest(
        donorName: String,
        phone: String,
        email: String,
        address: String,
        latitude: Double,
        longitude: Double,
        date: String,
        time: String,
        items: List<String>,
        estimatedQuantity: String,
        imageUris: List<String>,
        specialInstructions: String
    ): Result<PickupRequest>

    suspend fun updatePickupStatus(id: String, status: PickupStatus): Result<PickupRequest>
}

class FirebasePickupRepository : PickupRepository {
    private val firestore = FirebaseFirestore.getInstance(com.google.firebase.FirebaseApp.getInstance(), "default")

    override val pickupRequests: Flow<List<PickupRequest>> = callbackFlow {
        val listenerRegistration = firestore.collection("pickups")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val requests = snapshot.documents.mapNotNull { doc ->
                        try {
                            val itemsList = (doc.get("items") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                            val imageList = (doc.get("imageUris") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                            
                            PickupRequest(
                                id = doc.id,
                                donorName = doc.getString("donorName") ?: "",
                                phone = doc.getString("phone") ?: "",
                                email = doc.getString("email") ?: "",
                                address = doc.getString("address") ?: "",
                                latitude = doc.getDouble("latitude") ?: 0.0,
                                longitude = doc.getDouble("longitude") ?: 0.0,
                                date = doc.getString("date") ?: "",
                                time = doc.getString("time") ?: "",
                                items = itemsList,
                                estimatedQuantity = doc.getString("estimatedQuantity") ?: "",
                                imageUris = imageList,
                                specialInstructions = doc.getString("specialInstructions") ?: "",
                                status = PickupStatus.valueOf(doc.getString("status") ?: "PENDING")
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(requests)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun submitPickupRequest(
        donorName: String,
        phone: String,
        email: String,
        address: String,
        latitude: Double,
        longitude: Double,
        date: String,
        time: String,
        items: List<String>,
        estimatedQuantity: String,
        imageUris: List<String>,
        specialInstructions: String
    ): Result<PickupRequest> = suspendCancellableCoroutine { continuation ->
        val docRef = firestore.collection("pickups").document()
        val data = hashMapOf(
            "donorName" to donorName,
            "phone" to phone,
            "email" to email,
            "address" to address,
            "latitude" to latitude,
            "longitude" to longitude,
            "date" to date,
            "time" to time,
            "items" to items,
            "estimatedQuantity" to estimatedQuantity,
            "imageUris" to imageUris,
            "specialInstructions" to specialInstructions,
            "status" to PickupStatus.PENDING.name,
            "timestamp" to FieldValue.serverTimestamp()
        )

        docRef.set(data)
            .addOnSuccessListener {
                val request = PickupRequest(
                    id = docRef.id,
                    donorName = donorName,
                    phone = phone,
                    email = email,
                    address = address,
                    latitude = latitude,
                    longitude = longitude,
                    date = date,
                    time = time,
                    items = items,
                    estimatedQuantity = estimatedQuantity,
                    imageUris = imageUris,
                    specialInstructions = specialInstructions,
                    status = PickupStatus.PENDING
                )
                if (continuation.isActive) continuation.resume(Result.success(request))
            }
            .addOnFailureListener { e ->
                if (continuation.isActive) continuation.resume(Result.failure(e))
            }
    }

    override suspend fun updatePickupStatus(id: String, status: PickupStatus): Result<PickupRequest> = suspendCancellableCoroutine { continuation ->
        val docRef = firestore.collection("pickups").document(id)
        docRef.update("status", status.name)
            .addOnSuccessListener {
                docRef.get().addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val itemsList = (doc.get("items") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                        val imageList = (doc.get("imageUris") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                        val request = PickupRequest(
                            id = doc.id,
                            donorName = doc.getString("donorName") ?: "",
                            phone = doc.getString("phone") ?: "",
                            email = doc.getString("email") ?: "",
                            address = doc.getString("address") ?: "",
                            latitude = doc.getDouble("latitude") ?: 0.0,
                            longitude = doc.getDouble("longitude") ?: 0.0,
                            date = doc.getString("date") ?: "",
                            time = doc.getString("time") ?: "",
                            items = itemsList,
                            estimatedQuantity = doc.getString("estimatedQuantity") ?: "",
                            imageUris = imageList,
                            specialInstructions = doc.getString("specialInstructions") ?: "",
                            status = PickupStatus.valueOf(doc.getString("status") ?: "PENDING")
                        )
                        if (continuation.isActive) continuation.resume(Result.success(request))
                    } else {
                        if (continuation.isActive) continuation.resume(Result.failure(Exception("Document not found")))
                    }
                }.addOnFailureListener { e ->
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
            }
            .addOnFailureListener { e ->
                if (continuation.isActive) continuation.resume(Result.failure(e))
            }
    }
}
