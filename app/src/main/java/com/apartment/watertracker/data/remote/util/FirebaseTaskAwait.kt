package com.apartment.watertracker.data.remote.util

import com.google.android.gms.tasks.Task
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        val exception = task.exception
        when {
            exception != null -> continuation.resumeWithException(exception)
            task.isSuccessful -> continuation.resume(task.result)
            else -> continuation.resumeWithException(IllegalStateException("Firebase task failed without exception"))
        }
    }
}
