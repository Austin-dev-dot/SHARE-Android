package com.example.share.data.backend

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object FirebaseStorageService {
    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadFile(folder: String, fileUri: Uri): Result<String> = suspendCancellableCoroutine { continuation ->
        val filename = "${System.currentTimeMillis()}_${fileUri.lastPathSegment ?: "file"}"
        val ref = storage.reference.child("$folder/$filename")

        ref.putFile(fileUri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                ref.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                if (continuation.isActive) continuation.resume(Result.success(downloadUri.toString()))
            }
            .addOnFailureListener { e ->
                if (continuation.isActive) continuation.resume(Result.failure(e))
            }
    }
}
