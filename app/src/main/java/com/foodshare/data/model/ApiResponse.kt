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

data class UserPostsResponse(
    val posts: List<Post>
)

data class CommentResponse(
    val message: String? = null,
    val comment: Comment
)

data class UserProfileResponse(
    val id: String,
    val email: String,
    val displayName: String,
    val profileImage: String? = null,
    val stats: UserStats? = null,
    val createdAt: String? = null
)

data class UserStats(
    val posts: Int = 0,
    val likes: Int = 0
)

data class UpdateProfileResponse(
    val message: String? = null,
    val user: User
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

data class LikesResponse(
    val users: List<LikeUser>
)

data class LikeUser(
    val _id: String,
    val displayName: String,
    val profileImage: String?
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
