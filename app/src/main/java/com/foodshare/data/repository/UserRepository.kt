package com.foodshare.data.repository

import com.foodshare.data.api.FoodShareApi
import com.foodshare.data.local.UserDao
import com.foodshare.data.model.Post
import com.foodshare.data.model.User
import com.foodshare.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val api: FoodShareApi,
    private val userDao: UserDao
) {

    suspend fun getMyProfile(): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getMyProfile()
            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!
                val user = User(
                    id = profile.id,
                    email = profile.email,
                    displayName = profile.displayName,
                    profileImage = profile.profileImage,
                    createdAt = profile.createdAt,
                    postsCount = profile.stats?.posts ?: 0,
                    likesReceived = profile.stats?.likes ?: 0
                )
                userDao.insertUser(user)
                emit(Resource.Success(user))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to load profile"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load profile"))
        }
    }

    suspend fun updateProfile(
        displayName: String?,
        profileImageFile: File?
    ): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val displayNameBody = displayName?.toRequestBody("text/plain".toMediaTypeOrNull())
            val imagePart = profileImageFile?.let {
                val mimeType = getMimeType(it.name)
                val imageBody = it.asRequestBody(mimeType.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("profileImage", it.name, imageBody)
            }

            val response = api.updateProfile(displayNameBody, imagePart)
            if (response.isSuccessful && response.body()?.user != null) {
                val user = response.body()!!.user
                userDao.insertUser(user)
                emit(Resource.Success(user))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to update profile"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update profile"))
        }
    }

    private fun getMimeType(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }
    }

    suspend fun getMyPosts(): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getMyPosts()
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.posts))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to load posts"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load posts"))
        }
    }

    suspend fun getLikedPosts(): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getLikedPosts()
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.posts))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to load liked posts"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load liked posts"))
        }
    }

    fun getCachedUser(): Flow<User?> = userDao.getCurrentUser()

    // Overloaded updateProfile with pre-prepared parts
    suspend fun updateProfile(
        displayNameBody: okhttp3.RequestBody?,
        imagePart: MultipartBody.Part?
    ): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.updateProfile(displayNameBody, imagePart)
            if (response.isSuccessful && response.body()?.user != null) {
                val user = response.body()!!.user
                userDao.insertUser(user)
                emit(Resource.Success(user))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to update profile"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update profile"))
        }
    }
}
