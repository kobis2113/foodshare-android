package com.foodshare.data.model

data class AuthResponse(
    val user: User,
    val accessToken: String? = null,
    val refreshToken: String? = null
)

data class PostsResponse(
    val posts: List<Post>,
    val pagination: Pagination
)

data class Pagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    val pages: Int,
    val hasMore: Boolean
)

data class LikeResponse(
    val isLiked: Boolean,
    val likesCount: Int
)

data class PostResponse(
    val message: String? = null,
    val post: Post
)

data class CommentsResponse(
    val comments: List<Comment>
)

data class NutritionResponse(
    val calories: Int?,
    val protein: Double?,
    val carbs: Double?,
    val fat: Double?,
    val fiber: Double?,
    val sugar: Double?,
    val healthTips: List<String>?,
    val tips: String?
)

data class MessageResponse(
    val message: String
)

data class ErrorResponse(
    val message: String,
    val errors: List<ValidationError>? = null
)

data class ValidationError(
    val field: String,
    val message: String
)
