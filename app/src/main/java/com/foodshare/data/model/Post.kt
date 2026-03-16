package com.foodshare.data.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "posts")
data class Post(
    @PrimaryKey
    @SerializedName("_id")
    val id: String,
    val mealName: String,
    val description: String? = null,
    val image: String,
    @Embedded(prefix = "nutrition_")
    val nutrition: Nutrition? = null,
    @Embedded(prefix = "author_")
    val author: PostAuthor,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false,
    val createdAt: String,
    val updatedAt: String? = null
) : Parcelable

@Parcelize
data class PostAuthor(
    @SerializedName("_id")
    val id: String,
    val displayName: String,
    val profileImage: String? = null
) : Parcelable

@Parcelize
data class Nutrition(
    val calories: Int? = null,
    val protein: Double? = null,
    val carbs: Double? = null,
    val fat: Double? = null,
    val fiber: Double? = null,
    val sugar: Double? = null,
    val healthTips: List<String>? = null
) : Parcelable
