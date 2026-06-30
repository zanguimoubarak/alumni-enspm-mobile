package com.enspm.alumni.feed.domain

data class FeedPost(
    val id: Long,
    val content: String,
    val authorName: String,
    val authorAvatar: String?,
    val media: List<FeedMedia>,
    val likesCount: Int,
    val commentsCount: Int,
    val isLiked: Boolean,
    val createdAt: String?,
)

data class FeedMedia(val thumbnailUrl: String?, val fullUrl: String?, val type: String?)
data class FeedComment(val id: Long, val postId: Long, val content: String, val authorName: String, val createdAt: String?)
