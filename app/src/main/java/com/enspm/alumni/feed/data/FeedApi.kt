package com.enspm.alumni.feed.data

import com.enspm.alumni.core.networking.ApiEnvelope
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PostsApi {
    @GET("posts") suspend fun posts(@Query("per_page") perPage: Int = 15, @Query("page") page: Int): Response<ApiEnvelope<PostsPageDto>>
    @GET("posts/{id}") suspend fun post(@Path("id") id: Long): Response<ApiEnvelope<PostDataDto>>
}

interface CommentsApi {
    @GET("posts/{id}/comments") suspend fun comments(@Path("id") postId: Long): Response<ApiEnvelope<CommentsDataDto>>
    @POST("comments") suspend fun create(@Body request: CreateCommentRequest): Response<ApiEnvelope<CommentDto>>
}

interface LikesApi {
    @GET("posts/{id}/likes") suspend fun likes(@Path("id") postId: Long): Response<ApiEnvelope<LikesDataDto>>
    @POST("likes/toggle") suspend fun toggle(@Body request: ToggleLikeRequest): Response<ApiEnvelope<ToggleLikeDataDto>>
}
