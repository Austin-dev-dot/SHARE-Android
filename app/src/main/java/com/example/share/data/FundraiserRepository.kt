package com.example.share.data

import com.example.share.data.models.Fundraiser
import com.example.share.data.models.FundraiserComment
import com.example.share.data.models.FundraiserUpdate
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

interface FundraiserRepository {
    val fundraisers: Flow<List<Fundraiser>>
    
    suspend fun createFundraiser(
        title: String,
        story: String,
        targetAmount: Double,
        coverPhotoUrl: String,
        location: String,
        category: String,
        creatorName: String,
        documents: List<String>
    ): Result<Fundraiser>

    suspend fun donate(id: String, amount: Double, donorName: String): Result<Boolean>
    suspend fun addComment(id: String, authorName: String, text: String): Result<FundraiserComment>
    suspend fun addUpdate(id: String, text: String): Result<FundraiserUpdate>
    suspend fun approveFundraiser(id: String): Result<Fundraiser>
}

class FirebaseFundraiserRepository : FundraiserRepository {
    private val firestore = FirebaseFirestore.getInstance(com.google.firebase.FirebaseApp.getInstance(), "default")

    override val fundraisers: Flow<List<Fundraiser>> = callbackFlow {
        val listenerRegistration = firestore.collection("fundraisers")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val target = doc.getDouble("targetAmount") ?: 1.0
                            val raised = doc.getDouble("raisedAmount") ?: 0.0
                            val progress = (raised / target).toFloat().coerceIn(0.0f, 1.0f)
                            
                            val rawComments = doc.get("comments") as? List<*> ?: emptyList<Any>()
                            val comments = rawComments.mapNotNull { item ->
                                val map = item as? Map<*, *> ?: return@mapNotNull null
                                FundraiserComment(
                                    id = map["id"]?.toString() ?: "",
                                    authorName = map["authorName"]?.toString() ?: "",
                                    text = map["text"]?.toString() ?: "",
                                    timestamp = map["timestamp"]?.toString() ?: ""
                                )
                            }

                            val rawUpdates = doc.get("updates") as? List<*> ?: emptyList<Any>()
                            val updates = rawUpdates.mapNotNull { item ->
                                val map = item as? Map<*, *> ?: return@mapNotNull null
                                FundraiserUpdate(
                                    id = map["id"]?.toString() ?: "",
                                    text = map["text"]?.toString() ?: "",
                                    timestamp = map["timestamp"]?.toString() ?: ""
                                )
                            }

                            Fundraiser(
                                id = doc.id,
                                title = doc.getString("title") ?: "",
                                story = doc.getString("story") ?: "",
                                targetAmount = target,
                                raisedAmount = raised,
                                progress = progress,
                                coverPhotoUrl = doc.getString("coverPhotoUrl") ?: "",
                                location = doc.getString("location") ?: "",
                                category = doc.getString("category") ?: "",
                                creatorName = doc.getString("creatorName") ?: "",
                                isVerified = doc.getBoolean("isVerified") ?: false,
                                comments = comments,
                                updates = updates
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(list)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun createFundraiser(
        title: String,
        story: String,
        targetAmount: Double,
        coverPhotoUrl: String,
        location: String,
        category: String,
        creatorName: String,
        documents: List<String>
    ): Result<Fundraiser> = suspendCancellableCoroutine { continuation ->
        val docRef = firestore.collection("fundraisers").document()
        val data = hashMapOf(
            "title" to title,
            "story" to story,
            "targetAmount" to targetAmount,
            "raisedAmount" to 0.0,
            "coverPhotoUrl" to coverPhotoUrl,
            "location" to location,
            "category" to category,
            "creatorName" to creatorName,
            "isVerified" to false, // Requires admin approval via Web Dashboard
            "documents" to documents,
            "comments" to emptyList<Map<String, String>>(),
            "updates" to emptyList<Map<String, String>>(),
            "timestamp" to FieldValue.serverTimestamp()
        )

        docRef.set(data)
            .addOnSuccessListener {
                val fundraiser = Fundraiser(
                    id = docRef.id,
                    title = title,
                    story = story,
                    targetAmount = targetAmount,
                    raisedAmount = 0.0,
                    progress = 0.0f,
                    coverPhotoUrl = coverPhotoUrl,
                    location = location,
                    category = category,
                    creatorName = creatorName,
                    isVerified = false
                )
                if (continuation.isActive) continuation.resume(Result.success(fundraiser))
            }
            .addOnFailureListener { e ->
                if (continuation.isActive) continuation.resume(Result.failure(e))
            }
    }

    override suspend fun donate(id: String, amount: Double, donorName: String): Result<Boolean> = suspendCancellableCoroutine { continuation ->
        val docRef = firestore.collection("fundraisers").document(id)
        
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val currentRaised = snapshot.getDouble("raisedAmount") ?: 0.0
            val newRaised = currentRaised + amount
            transaction.update(docRef, "raisedAmount", newRaised)
            
            // Add automated comment about donation
            val commentId = "c_${System.currentTimeMillis()}"
            val commentMap = mapOf(
                "id" to commentId,
                "authorName" to donorName,
                "text" to "Contributed ₹${amount.toInt()} to this fundraiser.",
                "timestamp" to "Just now"
            )
            transaction.update(docRef, "comments", FieldValue.arrayUnion(commentMap))
        }.addOnSuccessListener {
            if (continuation.isActive) continuation.resume(Result.success(true))
        }.addOnFailureListener { e ->
            if (continuation.isActive) continuation.resume(Result.failure(e))
        }
    }

    override suspend fun addComment(id: String, authorName: String, text: String): Result<FundraiserComment> = suspendCancellableCoroutine { continuation ->
        val docRef = firestore.collection("fundraisers").document(id)
        val comment = FundraiserComment(
            id = "c_${System.currentTimeMillis()}",
            authorName = authorName,
            text = text,
            timestamp = "Just now"
        )
        val commentMap = mapOf(
            "id" to comment.id,
            "authorName" to comment.authorName,
            "text" to comment.text,
            "timestamp" to comment.timestamp
        )

        docRef.update("comments", FieldValue.arrayUnion(commentMap))
            .addOnSuccessListener {
                if (continuation.isActive) continuation.resume(Result.success(comment))
            }
            .addOnFailureListener { e ->
                if (continuation.isActive) continuation.resume(Result.failure(e))
            }
    }

    override suspend fun addUpdate(id: String, text: String): Result<FundraiserUpdate> = suspendCancellableCoroutine { continuation ->
        val docRef = firestore.collection("fundraisers").document(id)
        val update = FundraiserUpdate(
            id = "u_${System.currentTimeMillis()}",
            text = text,
            timestamp = "Just now"
        )
        val updateMap = mapOf(
            "id" to update.id,
            "text" to update.text,
            "timestamp" to update.timestamp
        )

        docRef.update("updates", FieldValue.arrayUnion(updateMap))
            .addOnSuccessListener {
                if (continuation.isActive) continuation.resume(Result.success(update))
            }
            .addOnFailureListener { e ->
                if (continuation.isActive) continuation.resume(Result.failure(e))
            }
    }

    override suspend fun approveFundraiser(id: String): Result<Fundraiser> = suspendCancellableCoroutine { continuation ->
        val docRef = firestore.collection("fundraisers").document(id)
        docRef.update("isVerified", true)
            .addOnSuccessListener {
                docRef.get().addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val target = doc.getDouble("targetAmount") ?: 1.0
                        val raised = doc.getDouble("raisedAmount") ?: 0.0
                        val progress = (raised / target).toFloat().coerceIn(0.0f, 1.0f)
                        val fundraiser = Fundraiser(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            story = doc.getString("story") ?: "",
                            targetAmount = target,
                            raisedAmount = raised,
                            progress = progress,
                            coverPhotoUrl = doc.getString("coverPhotoUrl") ?: "",
                            location = doc.getString("location") ?: "",
                            category = doc.getString("category") ?: "",
                            creatorName = doc.getString("creatorName") ?: "",
                            isVerified = true
                        )
                        if (continuation.isActive) continuation.resume(Result.success(fundraiser))
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
