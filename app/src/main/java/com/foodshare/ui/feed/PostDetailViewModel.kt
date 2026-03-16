package com.foodshare.ui.feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
}
