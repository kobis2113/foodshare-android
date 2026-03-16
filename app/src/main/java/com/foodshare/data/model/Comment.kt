package com.foodshare.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Comment(
    @SerializedName("_id")
    val id: String,
    val text: String,
    val author: CommentAuthor,
    val post: String,
    val createdAt: String
) : Parcelable

@Parcelize
data class CommentAuthor(
    @SerializedName("_id")
    val id: String,
    val displayName: String,
    val profileImage: String? = null
) : Parcelable
