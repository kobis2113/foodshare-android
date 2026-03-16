package com.foodshare.data.repository

import com.foodshare.data.api.FoodShareApi
import com.foodshare.data.local.UserDao
import com.foodshare.data.model.User
import com.foodshare.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: FoodShareApi,
    private val userDao: UserDao,
    private val firebaseAuth: FirebaseAuth
) {

    val currentFirebaseUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    fun isLoggedIn(): Boolean = currentFirebaseUser != null

    suspend fun login(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val response = api.syncFirebaseUser()
            if (response.isSuccessful && response.body()?.user != null) {
                val user = response.body()!!.user
                userDao.insertUser(user)
                emit(Resource.Success(user))
            } else {
                emit(Resource.Error(response.message() ?: "Login failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Login failed"))
        }
    }

    suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()
            }

            val response = api.syncFirebaseUser()
            if (response.isSuccessful && response.body()?.user != null) {
                val user = response.body()!!.user
                userDao.insertUser(user)
                emit(Resource.Success(user))
            } else {
                emit(Resource.Error(response.message() ?: "Registration failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Registration failed"))
        }
    }

    suspend fun syncUser(): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.syncFirebaseUser()
            if (response.isSuccessful && response.body()?.user != null) {
                val user = response.body()!!.user
                userDao.insertUser(user)
                emit(Resource.Success(user))
            } else {
                emit(Resource.Error(response.message() ?: "Sync failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Sync failed"))
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    fun getCachedUser(): Flow<User?> = userDao.getCurrentUser()
}
