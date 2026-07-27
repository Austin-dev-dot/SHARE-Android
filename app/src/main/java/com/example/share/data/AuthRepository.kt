package com.example.share.data

import android.app.Activity
import com.example.share.data.models.User
import com.google.firebase.FirebaseException
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun sendOtp(contact: String, activity: Activity): Result<Boolean>
    suspend fun verifyOtp(contact: String, otp: String): Result<User?> // Returns User if registered, null if new user
    suspend fun completeRegistration(contact: String, name: String, age: Int): Result<User>
    suspend fun logout(): Result<Unit>
}

class FirebaseAuthRepository : AuthRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance(com.google.firebase.FirebaseApp.getInstance(), "default")

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: Flow<User?> = _currentUser.asStateFlow()

    private var storedVerificationId: String? = null

    init {
        // Listen to Firebase auth state changes
        firebaseAuth.addAuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                // Fetch details from Firestore
                firestore.collection("users").document(firebaseUser.uid).get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val user = User(
                                id = firebaseUser.uid,
                                name = doc.getString("name") ?: "",
                                email = doc.getString("email") ?: "",
                                phone = doc.getString("phone") ?: "",
                                isVerified = doc.getBoolean("isVerified") ?: true,
                                isAdmin = doc.getBoolean("isAdmin") ?: false
                            )
                            _currentUser.value = user
                        } else {
                            // User signed in via Auth but no Firestore record yet (onboarding incomplete)
                            _currentUser.value = User(
                                id = firebaseUser.uid,
                                name = "",
                                email = firebaseUser.email ?: "",
                                phone = firebaseUser.phoneNumber ?: "",
                                isVerified = true,
                                isAdmin = false
                            )
                        }
                    }
                    .addOnFailureListener {
                        _currentUser.value = User(
                            id = firebaseUser.uid,
                            name = "",
                            email = firebaseUser.email ?: "",
                            phone = firebaseUser.phoneNumber ?: "",
                            isVerified = true,
                            isAdmin = false
                        )
                    }
            } else {
                _currentUser.value = null
            }
        }
    }

    override suspend fun sendOtp(contact: String, activity: Activity): Result<Boolean> = suspendCancellableCoroutine { continuation ->
        val isEmail = contact.contains("@")
        if (isEmail) {
            // Email link authentication (Passwordless)
            val actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl("https://sharejanmanas.page.link/login")
                .setHandleCodeInApp(true)
                .setAndroidPackageName("com.example.share", true, "24")
                .build()

            firebaseAuth.sendSignInLinkToEmail(contact, actionCodeSettings)
                .addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(Result.success(true))
                }
                .addOnFailureListener { e ->
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
        } else {
            // Phone verification
            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(contact)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        // In case of instant auto-verification, we can sign in right away
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        if (continuation.isActive) continuation.resume(Result.failure(e))
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        storedVerificationId = verificationId
                        if (continuation.isActive) continuation.resume(Result.success(true))
                    }
                })
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    override suspend fun verifyOtp(contact: String, otp: String): Result<User?> = suspendCancellableCoroutine { continuation ->
        val isEmail = contact.contains("@")
        if (isEmail) {
            // In a production email link verification, the app handles the incoming dynamic link.
            // For general verification flow in UI, if email input was mock-entered with 1234, we resolve:
            if (otp == "1234") {
                // Check if user document exists in Firestore
                firestore.collection("users")
                    .whereEqualTo("email", contact)
                    .get()
                    .addOnSuccessListener { query ->
                        if (!query.isEmpty) {
                            val doc = query.documents[0]
                            val user = User(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                email = doc.getString("email") ?: "",
                                phone = doc.getString("phone") ?: "",
                                isVerified = doc.getBoolean("isVerified") ?: true,
                                isAdmin = doc.getBoolean("isAdmin") ?: false
                            )
                            _currentUser.value = user
                            if (continuation.isActive) continuation.resume(Result.success(user))
                        } else {
                            if (continuation.isActive) continuation.resume(Result.success(null))
                        }
                    }
                    .addOnFailureListener { e ->
                        if (continuation.isActive) continuation.resume(Result.failure(e))
                    }
            } else {
                if (continuation.isActive) continuation.resume(Result.failure(Exception("Invalid OTP code.")))
            }
        } else {
            // Real Phone Auth OTP Verification
            val verificationId = storedVerificationId
            if (verificationId == null) {
                if (continuation.isActive) continuation.resume(Result.failure(Exception("No active verification code session found.")))
                return@suspendCancellableCoroutine
            }

            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener { authResult ->
                    val firebaseUser = authResult.user
                    if (firebaseUser != null) {
                        // Check if registered in Firestore
                        firestore.collection("users").document(firebaseUser.uid).get()
                            .addOnSuccessListener { doc ->
                                if (doc.exists()) {
                                    val user = User(
                                        id = firebaseUser.uid,
                                        name = doc.getString("name") ?: "",
                                        email = doc.getString("email") ?: "",
                                        phone = doc.getString("phone") ?: "",
                                        isVerified = doc.getBoolean("isVerified") ?: true,
                                        isAdmin = doc.getBoolean("isAdmin") ?: false
                                    )
                                    _currentUser.value = user
                                    if (continuation.isActive) continuation.resume(Result.success(user))
                                } else {
                                    // Signed in but new user (needs to fill Name/Age)
                                    if (continuation.isActive) continuation.resume(Result.success(null))
                                }
                            }
                            .addOnFailureListener { e ->
                                if (continuation.isActive) continuation.resume(Result.failure(e))
                            }
                    } else {
                        if (continuation.isActive) continuation.resume(Result.failure(Exception("Firebase user is null.")))
                    }
                }
                .addOnFailureListener { e ->
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
        }
    }

    override suspend fun completeRegistration(contact: String, name: String, age: Int): Result<User> = suspendCancellableCoroutine { continuation ->
        val firebaseUser = firebaseAuth.currentUser
        val uid = firebaseUser?.uid ?: "u_${System.currentTimeMillis()}"
        val isEmail = contact.contains("@")

        val userData = hashMapOf(
            "name" to name,
            "age" to age,
            "email" to if (isEmail) contact else (firebaseUser?.email ?: ""),
            "phone" to if (!isEmail) contact else (firebaseUser?.phoneNumber ?: ""),
            "isVerified" to true,
            "isAdmin" to false
        )

        firestore.collection("users").document(uid).set(userData)
            .addOnSuccessListener {
                val user = User(
                    id = uid,
                    name = name,
                    email = userData["email"] as String,
                    phone = userData["phone"] as String,
                    isVerified = true,
                    isAdmin = false
                )
                _currentUser.value = user
                if (continuation.isActive) continuation.resume(Result.success(user))
            }
            .addOnFailureListener { e ->
                if (continuation.isActive) continuation.resume(Result.failure(e))
            }
    }

    override suspend fun logout(): Result<Unit> {
        firebaseAuth.signOut()
        _currentUser.value = null
        return Result.success(Unit)
    }
}
