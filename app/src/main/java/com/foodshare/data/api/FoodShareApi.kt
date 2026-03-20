package com.foodshare.data.api

import com.foodshare.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface FoodShareApi {

    // Mobile Auth
    @POST("api/mobile/auth/sync")
    suspend fun syncFirebaseUser(): Response<AuthResponse>

    @GET("api/mobile/auth/me")
    suspend fun getCurrentUser(): Response<AuthResponse>

    // Posts
    @GET("api/posts")
    suspend fun getPosts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<PostsResponse>

    @GET("api/posts/search")
    suspend fun searchPosts(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<PostsResponse>

    @GET("api/posts/{id}")
    suspend fun getPost(@Path("id") postId: String): Response<Post>

    @Multipart
    @POST("api/posts")
    suspend fun createPost(
        @Part("mealName") mealName: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("nutrition") nutrition: RequestBody?,
        @Part image: MultipartBody.Part
    ): Response<PostResponse>

    @Multipart
    @PUT("api/posts/{id}")
    suspend fun updatePost(
        @Path("id") postId: String,
        @Part("mealName") mealName: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("nutrition") nutrition: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Response<PostResponse>

    @DELETE("api/posts/{id}")
    suspend fun deletePost(@Path("id") postId: String): Response<MessageResponse>

    @POST("api/posts/{id}/like")
    suspend fun toggleLike(@Path("id") postId: String): Response<LikeResponse>

    // Comments
    @GET("api/posts/{id}/comments")
    suspend fun getComments(@Path("id") postId: String): Response<CommentsResponse>

    @FormUrlEncoded
    @POST("api/posts/{id}/comments")
    suspend fun addComment(
        @Path("id") postId: String,
        @Field("text") text: String
    ): Response<CommentResponse>

    @FormUrlEncoded
    @PUT("api/posts/{postId}/comments/{commentId}")
    suspend fun updateComment(
        @Path("postId") postId: String,
        @Path("commentId") commentId: String,
        @Field("text") text: String
    ): Response<CommentResponse>

    @DELETE("api/posts/{postId}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("postId") postId: String,
        @Path("commentId") commentId: String
    ): Response<MessageResponse>

    @GET("api/posts/{id}/likes")
    suspend fun getPostLikes(@Path("id") postId: String): Response<LikesResponse>

    // Users
    @GET("api/users/me")
    suspend fun getMyProfile(): Response<UserProfileResponse>

    @Multipart
    @PUT("api/users/me")
    suspend fun updateProfile(
        @Part("displayName") displayName: RequestBody?,
        @Part profileImage: MultipartBody.Part?
    ): Response<UpdateProfileResponse>

    @GET("api/users/me/posts")
    suspend fun getMyPosts(): Response<UserPostsResponse>

    @GET("api/users/me/liked")
    suspend fun getLikedPosts(): Response<UserPostsResponse>

    @GET("api/users/{id}")
    suspend fun getUserProfile(@Path("id") userId: String): Response<User>

    // Nutrition
    @GET("api/nutrition")
    suspend fun getNutrition(@Query("query") query: String): Response<NutritionResponse>
}
