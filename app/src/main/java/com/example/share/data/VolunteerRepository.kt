package com.example.share.data

import com.example.share.data.models.Volunteer
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume

interface VolunteerRepository {
    val volunteers: Flow<List<Volunteer>>
    
    suspend fun registerVolunteer(
        name: String,
        phone: String,
        email: String,
        age: Int,
        gender: String,
        occupation: String,
        city: String,
        state: String,
        skills: List<String>,
        languages: List<String>,
        availability: String,
        emergencyContact: String,
        previousExperience: String,
        preferredAreas: List<String>,
        isAgreedToTerms: Boolean
    ): Result<Volunteer>

    suspend fun trackHours(id: String, hours: Int): Result<Volunteer>
    suspend fun updateStatus(id: String, status: String): Result<Volunteer>
    suspend fun generateCertificate(id: String): Result<String>
}

class FirebaseVolunteerRepository : VolunteerRepository {
    private val firestore = FirebaseFirestore.getInstance(com.google.firebase.FirebaseApp.getInstance(), "default")

    override val volunteers: Flow<List<Volunteer>> = callbackFlow {
        val listenerRegistration = firestore.collection("volunteers")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val skillsList = (doc.get("skills") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                            val langList = (doc.get("languages") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                            val areasList = (doc.get("preferredAreas") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                            
                            Volunteer(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                phone = doc.getString("phone") ?: "",
                                email = doc.getString("email") ?: "",
                                age = doc.getLong("age")?.toInt() ?: 0,
                                gender = doc.getString("gender") ?: "",
                                occupation = doc.getString("occupation") ?: "",
                                city = doc.getString("city") ?: "",
                                state = doc.getString("state") ?: "",
                                skills = skillsList,
                                languages = langList,
                                availability = doc.getString("availability") ?: "",
                                emergencyContact = doc.getString("emergencyContact") ?: "",
                                previousExperience = doc.getString("previousExperience") ?: "",
                                preferredAreas = areasList,
                                isAgreedToTerms = doc.getBoolean("isAgreedToTerms") ?: true,
                                joinedDate = doc.getString("joinedDate") ?: "",
                                status = doc.getString("status") ?: "Pending",
                                hoursTracked = doc.getLong("hoursTracked")?.toInt() ?: 0
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

    override suspend fun registerVolunteer(
        name: String,
        phone: String,
        email: String,
        age: Int,
        gender: String,
        occupation: String,
        city: String,
        state: String,
        skills: List<String>,
        languages: List<String>,
        availability: String,
        emergencyContact: String,
        previousExperience: String,
        preferredAreas: List<String>,
        isAgreedToTerms: Boolean
    ): Result<Volunteer> = suspendCancellableCoroutine { continuation ->
        val docRef = firestore.collection("volunteers").document()
        val joinedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        val data = hashMapOf(
            "name" to name,
            "phone" to phone,
            "email" to email,
            "age" to age,
            "gender" to gender,
            "occupation" to occupation,
            "city" to city,
            "state" to state,
            "skills" to skills,
            "languages" to languages,
            "availability" to availability,
            "emergencyContact" to emergencyContact,
            "previousExperience" to previousExperience,
            "preferredAreas" to preferredAreas,
            "isAgreedToTerms" to isAgreedToTerms,
            "joinedDate" to joinedDate,
            "status" to "Pending", // Requires NGO approval on Web Dashboard
            "hoursTracked" to 0,
            "timestamp" to FieldValue.serverTimestamp()
        )

        docRef.set(data)
            .addOnSuccessListener {
                val volunteer = Volunteer(
                    id = docRef.id,
                    name = name,
                    phone = phone,
                    email = email,
                    age = age,
                    gender = gender,
                    occupation = occupation,
                    city = city,
                    state = state,
                    skills = skills,
                    languages = languages,
                    availability = availability,
                    emergencyContact = emergencyContact,
                    previousExperience = previousExperience,
                    preferredAreas = preferredAreas,
                    isAgreedToTerms = isAgreedToTerms,
                    joinedDate = joinedDate,
                    status = "Pending",
                    hoursTracked = 0
                )
                if (continuation.isActive) continuation.resume(Result.success(volunteer))
            }
            .addOnFailureListener { e ->
                if (continuation.isActive) continuation.resume(Result.failure(e))
            }
    }

    override suspend fun trackHours(id: String, hours: Int): Result<Volunteer> = suspendCancellableCoroutine { continuation ->
        val docRef = firestore.collection("volunteers").document(id)
        docRef.update("hoursTracked", FieldValue.increment(hours.toLong()))
            .addOnSuccessListener {
                docRef.get().addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val skillsList = (doc.get("skills") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                        val langList = (doc.get("languages") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                        val areasList = (doc.get("preferredAreas") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                        
                        val volunteer = Volunteer(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            phone = doc.getString("phone") ?: "",
                            email = doc.getString("email") ?: "",
                            age = doc.getLong("age")?.toInt() ?: 0,
                            gender = doc.getString("gender") ?: "",
                            occupation = doc.getString("occupation") ?: "",
                            city = doc.getString("city") ?: "",
                            state = doc.getString("state") ?: "",
                            skills = skillsList,
                            languages = langList,
                            availability = doc.getString("availability") ?: "",
                            emergencyContact = doc.getString("emergencyContact") ?: "",
                            previousExperience = doc.getString("previousExperience") ?: "",
                            preferredAreas = areasList,
                            isAgreedToTerms = doc.getBoolean("isAgreedToTerms") ?: true,
                            joinedDate = doc.getString("joinedDate") ?: "",
                            status = doc.getString("status") ?: "Pending",
                            hoursTracked = doc.getLong("hoursTracked")?.toInt() ?: 0
                        )
                        if (continuation.isActive) continuation.resume(Result.success(volunteer))
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

    override suspend fun updateStatus(id: String, status: String): Result<Volunteer> = suspendCancellableCoroutine { continuation ->
        val docRef = firestore.collection("volunteers").document(id)
        docRef.update("status", status)
            .addOnSuccessListener {
                docRef.get().addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val skillsList = (doc.get("skills") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                        val langList = (doc.get("languages") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                        val areasList = (doc.get("preferredAreas") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                        
                        val volunteer = Volunteer(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            phone = doc.getString("phone") ?: "",
                            email = doc.getString("email") ?: "",
                            age = doc.getLong("age")?.toInt() ?: 0,
                            gender = doc.getString("gender") ?: "",
                            occupation = doc.getString("occupation") ?: "",
                            city = doc.getString("city") ?: "",
                            state = doc.getString("state") ?: "",
                            skills = skillsList,
                            languages = langList,
                            availability = doc.getString("availability") ?: "",
                            emergencyContact = doc.getString("emergencyContact") ?: "",
                            previousExperience = doc.getString("previousExperience") ?: "",
                            preferredAreas = areasList,
                            isAgreedToTerms = doc.getBoolean("isAgreedToTerms") ?: true,
                            joinedDate = doc.getString("joinedDate") ?: "",
                            status = doc.getString("status") ?: "Pending",
                            hoursTracked = doc.getLong("hoursTracked")?.toInt() ?: 0
                        )
                        if (continuation.isActive) continuation.resume(Result.success(volunteer))
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

    override suspend fun generateCertificate(id: String): Result<String> = suspendCancellableCoroutine { continuation ->
        val docRef = firestore.collection("volunteers").document(id)
        docRef.get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val hours = doc.getLong("hoursTracked")?.toInt() ?: 0
                    if (hours >= 10) {
                        val certId = "CERT-JMF-${id.takeLast(4).uppercase()}-${System.currentTimeMillis().toString().takeLast(4)}"
                        if (continuation.isActive) continuation.resume(Result.success(certId))
                    } else {
                        if (continuation.isActive) continuation.resume(Result.failure(Exception("Volunteer needs at least 10 service hours to unlock a certificate.")))
                    }
                } else {
                    if (continuation.isActive) continuation.resume(Result.failure(Exception("Volunteer profile not found.")))
                }
            }
            .addOnFailureListener { e ->
                if (continuation.isActive) continuation.resume(Result.failure(e))
            }
    }
}
