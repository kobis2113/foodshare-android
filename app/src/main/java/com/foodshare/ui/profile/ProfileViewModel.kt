package com.foodshare.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodshare.data.model.Post
import com.foodshare.data.model.User
import com.foodshare.data.repository.AuthRepository
import com.foodshare.data.repository.UserRepository
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
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _profile = MutableLiveData<Resource<User>>()
    val profile: LiveData<Resource<User>> = _profile

    private val _updateResult = MutableLiveData<Resource<User>>()
    val updateResult: LiveData<Resource<User>> = _updateResult

    private val _myPosts = MutableLiveData<Resource<List<Post>>>()
    val myPosts: LiveData<Resource<List<Post>>> = _myPosts

    private val _likedPosts = MutableLiveData<Resource<List<Post>>>()
    val likedPosts: LiveData<Resource<List<Post>>> = _likedPosts

    private val _logoutResult = MutableLiveData<Resource<Unit>>()
    val logoutResult: LiveData<Resource<Unit>> = _logoutResult

    fun loadProfile() {
        viewModelScope.launch {
            userRepository.getMyProfile().collectLatest { result ->
                _profile.value = result
            }
        }
    }

    fun updateProfile(context: Context, displayName: String?, avatarUri: Uri?) {
        viewModelScope.launch {
            _updateResult.value = Resource.Loading()

            try {
                var imagePart: MultipartBody.Part? = null
                var displayNameBody: okhttp3.RequestBody? = null

                avatarUri?.let { uri ->
                    val file = getFileFromUri(context, uri)
                    if (file != null) {
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        imagePart = MultipartBody.Part.createFormData("profileImage", file.name, requestFile)
                    }
                }

                displayName?.let {
                    displayNameBody = it.toRequestBody("text/plain".toMediaTypeOrNull())
                }

                userRepository.updateProfile(displayNameBody, imagePart).collectLatest { result ->
                    _updateResult.value = result
                }
            } catch (e: Exception) {
                _updateResult.value = Resource.Error(e.message ?: "Failed to update profile")
            }
        }
    }

    fun loadMyPosts() {
        viewModelScope.launch {
            userRepository.getMyPosts().collectLatest { result ->
                _myPosts.value = result
            }
        }
    }

    fun loadLikedPosts() {
        viewModelScope.launch {
            userRepository.getLikedPosts().collectLatest { result ->
                _likedPosts.value = result
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _logoutResult.value = Resource.Loading()
            try {
                authRepository.logout()
                _logoutResult.value = Resource.Success(Unit)
            } catch (e: Exception) {
                _logoutResult.value = Resource.Error(e.message ?: "Logout failed")
            }
        }
    }
}
