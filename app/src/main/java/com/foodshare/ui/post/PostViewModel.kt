package com.foodshare.ui.post

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodshare.data.model.Post
import com.foodshare.data.repository.PostRepository
import com.foodshare.util.Resource
import com.foodshare.util.getFileFromUri
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

                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
                val mealNameBody = mealName.toRequestBody("text/plain".toMediaTypeOrNull())
                val descriptionBody = description?.toRequestBody("text/plain".toMediaTypeOrNull())

                postRepository.createPost(imagePart, mealNameBody, descriptionBody).collectLatest { result ->
                    _createPostResult.value = result
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
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
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

    fun deletePost(postId: String) {
        viewModelScope.launch {
            postRepository.deletePost(postId).collectLatest { result ->
                _deletePostResult.value = result
            }
        }
    }

    fun getNutritionTips(mealName: String) {
        viewModelScope.launch {
            postRepository.getNutritionTips(mealName).collectLatest { result ->
                _nutritionTips.value = result
            }
        }
    }
}
