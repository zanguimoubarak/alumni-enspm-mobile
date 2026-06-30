package com.enspm.alumni.feed.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PaginationDto(
    val total: Int = 0,
    @Json(name = "per_page") val perPage: Int = 15,
    @Json(name = "current_page") val currentPage: Int = 1,
    @Json(name = "last_page") val lastPage: Int = 1,
)

@JsonClass(generateAdapter = true)
data class PostsPageDto(val posts: List<PostDto> = emptyList(), val pagination: PaginationDto)

@JsonClass(generateAdapter = true)
data class PostDataDto(val post: PostDto? = null)

@JsonClass(generateAdapter = true)
data class CommentsDataDto(val comments: List<CommentDto> = emptyList())

@JsonClass(generateAdapter = true)
data class LikesDataDto(val likes: List<LikeDto> = emptyList(), @Json(name = "likes_count") val likesCount: Int? = null)

@JsonClass(generateAdapter = true)
data class ToggleLikeDataDto(val liked: Boolean? = null, @Json(name = "likes_count") val likesCount: Int? = null)

@JsonClass(generateAdapter = true)
data class PostDto(
    val id: Long,
    val content: String? = null,
    val author: FeedUserDto? = null,
    val user: FeedUserDto? = null,
    val medias: List<MediaDto> = emptyList(),
    @Json(name = "media") val media: List<MediaDto> = emptyList(),
    @Json(name = "comments_count") val commentsCount: Int = 0,
    @Json(name = "likes_count") val likesCount: Int = 0,
    @Json(name = "is_liked") val isLiked: Boolean = false,
    @Json(name = "created_at") val createdAt: String? = null,
)

@JsonClass(generateAdapter = true)
data class FeedUserDto(val id: Long? = null, val name: String? = null, val email: String? = null, val avatar: String? = null)

@JsonClass(generateAdapter = true)
data class MediaDto(
    val id: Long? = null,
    val url: String? = null,
    @Json(name = "thumbnail_url") val thumbnailUrl: String? = null,
    @Json(name = "thumb_url") val thumbUrl: String? = null,
    val type: String? = null,
)

@JsonClass(generateAdapter = true)
data class CommentDto(
    val id: Long,
    val content: String? = null,
    val author: FeedUserDto? = null,
    val user: FeedUserDto? = null,
    @Json(name = "created_at") val createdAt: String? = null,
)

@JsonClass(generateAdapter = true)
data class LikeDto(val id: Long? = null, val user: FeedUserDto? = null)

@JsonClass(generateAdapter = true)
data class CreateCommentRequest(
    @Json(name = "commentable_type") val commentableType: String = "post",
    @Json(name = "commentable_id") val commentableId: Long,
    val content: String,
)

@JsonClass(generateAdapter = true)
data class ToggleLikeRequest(
    @Json(name = "likeable_type") val likeableType: String = "post",
    @Json(name = "likeable_id") val likeableId: Long,
)
