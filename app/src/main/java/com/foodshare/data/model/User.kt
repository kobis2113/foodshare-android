package com.foodshare.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String,
    val email: String,
    val displayName: String,
    val profileImage: String? = null,
    val authProvider: String? = null,
    val createdAt: String? = null,
    val postsCount: Int = 0,
    val likesReceived: Int = 0
) : Parcelable {
    // Alias for profileImage for convenience
    val avatar: String? get() = profileImage
}
