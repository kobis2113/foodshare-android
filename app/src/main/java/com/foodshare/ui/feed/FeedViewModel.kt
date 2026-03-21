package com.foodshare.ui.feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodshare.data.model.LikeResponse
import com.foodshare.data.model.Post
import com.foodshare.data.model.PostsResponse
import com.foodshare.data.repository.PostRepository
import com.foodshare.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _postsState = MutableLiveData<Resource<PostsResponse>>()
    val postsState: LiveData<Resource<PostsResponse>> = _postsState

    private val _likeState = MutableLiveData<Resource<LikeResponse>>()
    val likeState: LiveData<Resource<LikeResponse>> = _likeState

    private val _posts = MutableLiveData<MutableList<Post>>(mutableListOf())
    val posts: LiveData<MutableList<Post>> = _posts

    private val _searchQuery = MutableLiveData<String?>(null)
    val searchQuery: LiveData<String?> = _searchQuery

    private var currentPage = 1
    private var hasMore = true
    private var searchCurrentPage = 1
    private var searchHasMore = true
    private var isLoading = false

    fun setSearchQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            if (_searchQuery.value != null) {
                _searchQuery.value = null
                loadPosts(refresh = true)
            }
        } else {
            _posts.value = mutableListOf()
            _searchQuery.value = trimmed
            loadSearchPosts(refresh = true)
        }
    }

    fun refresh() {
        if (_searchQuery.value != null) {
            loadSearchPosts(refresh = true)
        } else {
            loadPosts(refresh = true)
        }
    }

    fun loadPosts(refresh: Boolean = false) {
        if (_searchQuery.value != null) return
        if (isLoading) return

        if (refresh) {
            currentPage = 1
            hasMore = true
            _posts.value = mutableListOf()
        }

        if (!hasMore) return

        isLoading = true
        viewModelScope.launch {
            postRepository.getPosts(currentPage).collectLatest { result ->
                isLoading = false
                _postsState.value = result

                if (result is Resource.Success) {
                    result.data?.let { response ->
                        val currentList = _posts.value ?: mutableListOf()
                        currentList.addAll(response.posts)
                        _posts.value = currentList
                        hasMore = response.pagination.hasMore
                        currentPage++
                    }
                }
            }
        }
    }

    private fun loadSearchPosts(refresh: Boolean = false) {
        val q = _searchQuery.value ?: return
        if (isLoading) return

        if (refresh) {
            searchCurrentPage = 1
            searchHasMore = true
            _posts.value = mutableListOf()
        }

        if (!searchHasMore) return

        isLoading = true
        viewModelScope.launch {
            postRepository.searchPosts(q, searchCurrentPage).collectLatest { result ->
                isLoading = false
                _postsState.value = result

                if (result is Resource.Success) {
                    result.data?.let { response ->
                        val currentList = _posts.value ?: mutableListOf()
                        currentList.addAll(response.posts)
                        _posts.value = currentList
                        searchHasMore = response.pagination.hasMore
                        searchCurrentPage++
                    }
                }
            }
        }
    }

    fun loadMore() {
        if (_searchQuery.value != null) {
            loadSearchPosts(refresh = false)
        } else {
            loadPosts(refresh = false)
        }
    }

    fun toggleLike(postId: String, position: Int) {
        viewModelScope.launch {
            postRepository.toggleLike(postId).collectLatest { result ->
                if (result is Resource.Success) {
                    result.data?.let { likeResponse ->
                        val currentList = _posts.value ?: return@collectLatest
                        val postIndex = currentList.indexOfFirst { it.id == postId }
                        if (postIndex != -1) {
                            val post = currentList[postIndex]
                            currentList[postIndex] = post.copy(
                                isLiked = likeResponse.isLiked,
                                likesCount = likeResponse.likesCount
                            )
                            _posts.value = currentList
                        }
                    }
                }
                _likeState.value = result
            }
        }
    }
}
