package com.foodshare.data.repository

import com.foodshare.data.api.FoodShareApi
import com.foodshare.data.local.PostDao
import com.foodshare.data.model.*
import com.foodshare.data.model.LikeUser
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
class PostRepository @Inject constructor(
    private val api: FoodShareApi,
    private val postDao: PostDao
) {

    suspend fun getPosts(page: Int = 1, limit: Int = 10): Flow<Resource<PostsResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getPosts(page, limit)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                if (page == 1) {
                    postDao.clearAllPosts()
                }
                postDao.insertPosts(data.posts)
                emit(Resource.Success(data))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to load posts"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load posts"))
        }
    }

    suspend fun searchPosts(query: String, page: Int = 1): Flow<Resource<PostsResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.searchPosts(query, page)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Search failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Search failed"))
        }
    }

    suspend fun getPost(postId: String): Flow<Resource<Post>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getPost(postId)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to load post"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load post"))
        }
    }

    suspend fun createPost(
        mealName: String,
        description: String?,
        imageFile: File,
        nutritionJson: String? = null
    ): Flow<Resource<Post>> = flow {
        emit(Resource.Loading())
        try {
            val mealNameBody = mealName.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionBody = description?.toRequestBody("text/plain".toMediaTypeOrNull())
            val nutritionBody = nutritionJson?.toRequestBody("text/plain".toMediaTypeOrNull())
            val mimeType = getMimeType(imageFile.name)
            val imageBody = imageFile.asRequestBody(mimeType.toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, imageBody)

            val response = api.createPost(mealNameBody, descriptionBody, nutritionBody, imagePart)
            if (response.isSuccessful && response.body()?.post != null) {
                val post = response.body()!!.post
                postDao.insertPost(post)
                emit(Resource.Success(post))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to create post"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to create post"))
        }
    }

    suspend fun updatePost(
        postId: String,
        mealName: String,
        description: String?,
        imageFile: File?,
        nutritionJson: String? = null
    ): Flow<Resource<Post>> = flow {
        emit(Resource.Loading())
        try {
            val mealNameBody = mealName.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionBody = description?.toRequestBody("text/plain".toMediaTypeOrNull())
            val nutritionBody = nutritionJson?.toRequestBody("text/plain".toMediaTypeOrNull())
            val imagePart = imageFile?.let {
                val mimeType = getMimeType(it.name)
                val imageBody = it.asRequestBody(mimeType.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("image", it.name, imageBody)
            }

            val response = api.updatePost(postId, mealNameBody, descriptionBody, nutritionBody, imagePart)
            if (response.isSuccessful && response.body()?.post != null) {
                val post = response.body()!!.post
                postDao.insertPost(post)
                emit(Resource.Success(post))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to update post"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update post"))
        }
    }

    suspend fun deletePost(postId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.deletePost(postId)
            if (response.isSuccessful) {
                postDao.deletePostById(postId)
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to delete post"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to delete post"))
        }
    }

    suspend fun toggleLike(postId: String): Flow<Resource<LikeResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.toggleLike(postId)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to toggle like"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to toggle like"))
        }
    }

    suspend fun getComments(postId: String): Flow<Resource<List<Comment>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getComments(postId)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.comments))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to load comments"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load comments"))
        }
    }

    suspend fun addComment(postId: String, text: String): Flow<Resource<Comment>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.addComment(postId, text)
            if (response.isSuccessful && response.body()?.comment != null) {
                emit(Resource.Success(response.body()!!.comment))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to add comment"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to add comment"))
        }
    }

    suspend fun deleteComment(postId: String, commentId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.deleteComment(postId, commentId)
            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to delete comment"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to delete comment"))
        }
    }

    suspend fun getPostLikes(postId: String): Flow<Resource<List<LikeUser>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getPostLikes(postId)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.users))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to get likes"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get likes"))
        }
    }

    fun getCachedPosts(): Flow<List<Post>> = postDao.getAllPosts()

    private fun getMimeType(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            else -> "image/jpeg" // Default to JPEG
        }
    }

    // Overloaded createPost with pre-prepared parts
    suspend fun createPost(
        imagePart: MultipartBody.Part,
        mealNameBody: okhttp3.RequestBody,
        descriptionBody: okhttp3.RequestBody?,
        nutritionBody: okhttp3.RequestBody? = null
    ): Flow<Resource<Post>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.createPost(mealNameBody, descriptionBody, nutritionBody, imagePart)
            if (response.isSuccessful && response.body()?.post != null) {
                val post = response.body()!!.post
                postDao.insertPost(post)
                emit(Resource.Success(post))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to create post"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to create post"))
        }
    }

    // Overloaded updatePost with pre-prepared parts
    suspend fun updatePost(
        postId: String,
        imagePart: MultipartBody.Part?,
        mealNameBody: okhttp3.RequestBody,
        descriptionBody: okhttp3.RequestBody?,
        nutritionBody: okhttp3.RequestBody? = null
    ): Flow<Resource<Post>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.updatePost(postId, mealNameBody, descriptionBody, nutritionBody, imagePart)
            if (response.isSuccessful && response.body()?.post != null) {
                val post = response.body()!!.post
                postDao.insertPost(post)
                emit(Resource.Success(post))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to update post"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update post"))
        }
    }

    suspend fun getNutritionTips(mealName: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getNutrition(mealName)
            if (response.isSuccessful && response.body() != null) {
                val nutritionData = response.body()!!
                val tips = buildString {
                    append("Calories: ${nutritionData.calories ?: "N/A"} kcal\n")
                    append("Protein: ${nutritionData.protein ?: "N/A"}g\n")
                    append("Carbs: ${nutritionData.carbs ?: "N/A"}g\n")
                    append("Fat: ${nutritionData.fat ?: "N/A"}g\n")
                    nutritionData.tips?.let { append("\n$it") }
                }
                emit(Resource.Success(tips))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to get nutrition tips"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get nutrition tips"))
        }
    }

    suspend fun getNutritionData(mealName: String): Flow<Resource<NutritionResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getNutrition(mealName)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to get nutrition data"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get nutrition data"))
        }
    }
}
