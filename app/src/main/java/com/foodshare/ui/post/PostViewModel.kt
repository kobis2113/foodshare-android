package com.foodshare.ui.post

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodshare.data.model.NutritionResponse
import com.foodshare.data.model.Post
import com.foodshare.data.repository.PostRepository
import com.foodshare.util.Resource
import com.foodshare.util.getFileFromUri
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _post = MutableLiveData<Resource<Post>>()
    val post: LiveData<Resource<Post>> = _post

    private val _createPostResult = MutableLiveData<Resource<Post>>()
    val createPostResult: LiveData<Resource<Post>> = _createPostResult

    private val _updatePostResult = MutableLiveData<Resource<Post>>()
    val updatePostResult: LiveData<Resource<Post>> = _updatePostResult

    private val _deletePostResult = MutableLiveData<Resource<Unit>>()
    val deletePostResult: LiveData<Resource<Unit>> = _deletePostResult

    private val _nutritionTips = MutableLiveData<Resource<String>>()
    val nutritionTips: LiveData<Resource<String>> = _nutritionTips

    // Store the raw nutrition data for sending with post
    private var currentNutritionData: NutritionResponse? = null

    fun loadPost(postId: String) {
        viewModelScope.launch {
            postRepository.getPost(postId).collectLatest { result ->
                _post.value = result
            }
        }
    }

    fun createPost(context: Context, imageUri: Uri, mealName: String, description: String?) {
        viewModelScope.launch {
            _createPostResult.value = Resource.Loading()

            try {
                val file = getFileFromUri(context, imageUri)
                if (file == null) {
                    _createPostResult.value = Resource.Error("Failed to process image")
                    return@launch
                }

                val mimeType = getMimeType(file.name)
                val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
                val mealNameBody = mealName.toRequestBody("text/plain".toMediaTypeOrNull())
                val descriptionBody = description?.toRequestBody("text/plain".toMediaTypeOrNull())

                // Convert nutrition data to JSON if available
                val nutritionBody = currentNutritionData?.let { nutrition ->
                    val nutritionJson = Gson().toJson(mapOf(
                        "calories" to nutrition.calories,
                        "protein" to nutrition.protein,
                        "carbs" to nutrition.carbs,
                        "fat" to nutrition.fat,
                        "fiber" to nutrition.fiber,
                        "sugar" to nutrition.sugar,
                        "healthTips" to nutrition.healthTips
                    ))
                    nutritionJson.toRequestBody("text/plain".toMediaTypeOrNull())
                }

                postRepository.createPost(imagePart, mealNameBody, descriptionBody, nutritionBody).collectLatest { result ->
                    _createPostResult.value = result
                    if (result is Resource.Success) {
                        // Clear nutrition data after successful post
                        currentNutritionData = null
                    }
                }
            } catch (e: Exception) {
                _createPostResult.value = Resource.Error(e.message ?: "Failed to create post")
            }
        }
    }

    fun updatePost(
        context: Context,
        postId: String,
        imageUri: Uri?,
        mealName: String,
        description: String?
    ) {
        viewModelScope.launch {
            _updatePostResult.value = Resource.Loading()

            try {
                var imagePart: MultipartBody.Part? = null

                imageUri?.let { uri ->
                    val file = getFileFromUri(context, uri)
                    if (file != null) {
                        val mimeType = getMimeType(file.name)
                        val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                        imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
                    }
                }

                val mealNameBody = mealName.toRequestBody("text/plain".toMediaTypeOrNull())
                val descriptionBody = description?.toRequestBody("text/plain".toMediaTypeOrNull())

                postRepository.updatePost(postId, imagePart, mealNameBody, descriptionBody).collectLatest { result ->
                    _updatePostResult.value = result
                }
            } catch (e: Exception) {
                _updatePostResult.value = Resource.Error(e.message ?: "Failed to update post")
            }
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

    fun deletePost(postId: String) {
        viewModelScope.launch {
            postRepository.deletePost(postId).collectLatest { result ->
                _deletePostResult.value = result
            }
        }
    }

    fun getNutritionTips(mealName: String) {
        viewModelScope.launch {
            // First get the raw nutrition data and store it
            postRepository.getNutritionData(mealName).collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        _nutritionTips.value = Resource.Loading()
                    }
                    is Resource.Success -> {
                        currentNutritionData = result.data
                        // Format tips for display
                        val nutrition = result.data
                        val tips = buildString {
                            append("Calories: ${nutrition?.calories ?: "N/A"} kcal\n")
                            append("Protein: ${nutrition?.protein ?: "N/A"}g\n")
                            append("Carbs: ${nutrition?.carbs ?: "N/A"}g\n")
                            append("Fat: ${nutrition?.fat ?: "N/A"}g\n")
                            // Add health tips from AI
                            nutrition?.healthTips?.let { healthTips ->
                                if (healthTips.isNotEmpty()) {
                                    append("\nHealth Tips:\n")
                                    healthTips.forEach { tip ->
                                        append("• $tip\n")
                                    }
                                }
                            }
                        }
                        _nutritionTips.value = Resource.Success(tips)
                    }
                    is Resource.Error -> {
                        _nutritionTips.value = Resource.Error(result.message ?: "Failed to get nutrition tips")
                    }
                }
            }
        }
    }

    fun clearNutritionData() {
        currentNutritionData = null
        _nutritionTips.value = null
    }
}
