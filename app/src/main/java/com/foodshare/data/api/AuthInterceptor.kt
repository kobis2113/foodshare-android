package com.foodshare.data.api

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking {
            try {
                firebaseUser.getIdToken(false).await().token
            } catch (e: Exception) {
                null
            }
        }

        if (token == null) {
            return chain.proceed(originalRequest)
        }

        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .header("x-auth-type", "firebase")
            .build()

        return chain.proceed(newRequest)
    }
}
