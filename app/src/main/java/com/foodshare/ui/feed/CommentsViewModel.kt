package com.foodshare.ui.feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodshare.data.model.Comment
import com.foodshare.data.repository.PostRepository
import com.foodshare.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentsViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _comments = MutableLiveData<Resource<List<Comment>>>()
    val comments: LiveData<Resource<List<Comment>>> = _comments

    private val _addCommentResult = MutableLiveData<Resource<Comment>>()
    val addCommentResult: LiveData<Resource<Comment>> = _addCommentResult

    private val _deleteCommentResult = MutableLiveData<Resource<Unit>>()
    val deleteCommentResult: LiveData<Resource<Unit>> = _deleteCommentResult

    fun loadComments(postId: String) {
        viewModelScope.launch {
            postRepository.getComments(postId).collectLatest { result ->
                _comments.value = result
            }
        }
    }

    fun addComment(postId: String, content: String) {
        viewModelScope.launch {
            postRepository.addComment(postId, content).collectLatest { result ->
                _addCommentResult.value = result
            }
        }
    }

    fun deleteComment(postId: String, commentId: String) {
        viewModelScope.launch {
            postRepository.deleteComment(postId, commentId).collectLatest { result ->
                _deleteCommentResult.value = result
            }
        }
    }
}
