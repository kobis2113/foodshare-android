package com.foodshare.ui.feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodshare.data.model.LikeUser
import com.foodshare.data.model.Nutrition
import com.foodshare.data.model.NutritionResponse
import com.foodshare.data.model.Post
import com.foodshare.data.repository.PostRepository
import com.foodshare.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _post = MutableLiveData<Resource<Post>>()
    val post: LiveData<Resource<Post>> = _post

    private val _likeResult = MutableLiveData<Resource<Unit>>()
    val likeResult: LiveData<Resource<Unit>> = _likeResult

    private val _nutritionResult = MutableLiveData<Resource<NutritionResponse>>()
    val nutritionResult: LiveData<Resource<NutritionResponse>> = _nutritionResult

    private val _likesUsers = MutableLiveData<Resource<List<LikeUser>>?>()
    val likesUsers: LiveData<Resource<List<LikeUser>>?> = _likesUsers

    private var currentPostId: String? = null

    fun loadPost(postId: String) {
        currentPostId = postId
        viewModelScope.launch {
            postRepository.getPost(postId).collectLatest { result ->
                _post.value = result
            }
        }
    }

    fun toggleLike() {
        val postId = currentPostId ?: return
        val currentPost = (_post.value as? Resource.Success)?.data ?: return

        viewModelScope.launch {
            postRepository.toggleLike(postId).collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        _likeResult.value = Resource.Loading()
                    }
                    is Resource.Success -> {
                        // Update post with new like state
                        val newIsLiked = !currentPost.isLiked
                        val newLikesCount = if (newIsLiked) {
                            currentPost.likesCount + 1
                        } else {
                            (currentPost.likesCount - 1).coerceAtLeast(0)
                        }

                        val updatedPost = currentPost.copy(
                            isLiked = newIsLiked,
                            likesCount = newLikesCount
                        )
                        _post.value = Resource.Success(updatedPost)
                        _likeResult.value = Resource.Success(Unit)
                    }
                    is Resource.Error -> {
                        _likeResult.value = Resource.Error(result.message ?: "Failed to update like")
                    }
                }
            }
        }
    }

    fun refreshPost() {
        currentPostId?.let { loadPost(it) }
    }

    fun loadNutrition(mealName: String) {
        viewModelScope.launch {
            postRepository.getNutritionData(mealName).collectLatest { result ->
                _nutritionResult.value = result

                // Update post with nutrition data on success
                if (result is Resource.Success && result.data != null) {
                    val currentPost = (_post.value as? Resource.Success)?.data
                    currentPost?.let { post ->
                        val nutrition = Nutrition(
                            calories = result.data.calories,
                            protein = result.data.protein,
                            carbs = result.data.carbs,
                            fat = result.data.fat,
                            fiber = result.data.fiber,
                            sugar = result.data.sugar,
                            healthTips = result.data.healthTips
                        )
                        val updatedPost = post.copy(nutrition = nutrition)
                        _post.value = Resource.Success(updatedPost)
                    }
                }
            }
        }
    }

    fun loadLikesUsers() {
        val postId = currentPostId ?: return
        viewModelScope.launch {
            postRepository.getPostLikes(postId).collectLatest { result ->
                _likesUsers.value = result
            }
        }
    }

    fun clearLikesUsers() {
        _likesUsers.value = null
    }
}
