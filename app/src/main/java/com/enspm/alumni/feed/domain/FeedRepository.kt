package com.enspm.alumni.feed.domain

import com.enspm.alumni.core.database.*
import com.enspm.alumni.core.networking.ApiResult
import com.enspm.alumni.core.networking.safeApiCall
import com.enspm.alumni.feed.data.*
import com.squareup.moshi.Moshi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepository @Inject constructor(
    private val postsApi: PostsApi,
    private val commentsApi: CommentsApi,
    private val likesApi: LikesApi,
    private val dao: FeedDao,
    private val moshi: Moshi,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeFeed(): Flow<List<FeedPost>> = dao.observePosts().flatMapLatest { posts ->
        if (posts.isEmpty()) flowOf(emptyList()) else combine(
            posts.map { post -> dao.observeMedia(post.id).map { media -> post.toDomain(media) } },
        ) { it.toList() }
    }

    fun observePost(postId: Long): Flow<FeedPost?> = combine(dao.observePost(postId), dao.observeMedia(postId)) { post, media -> post?.toDomain(media) }
    fun observeComments(postId: Long): Flow<List<FeedComment>> = dao.observeComments(postId).map { it.map { c -> c.toDomain() } }

    suspend fun refreshFirstPage(): ApiResult<Unit> = fetchPage(1)

    suspend fun loadNextPage(): ApiResult<Unit> {
        val meta = dao.pagination(FEED_RESOURCE) ?: return fetchPage(1)
        return if (meta.currentPage < meta.lastPage) fetchPage(meta.currentPage + 1) else ApiResult.Success(Unit, "Dernière page atteinte.")
    }

    suspend fun refreshPost(postId: Long): ApiResult<Unit> = when (val result = safeApiCall(moshi) { postsApi.post(postId) }) {
        is ApiResult.Success -> {
            result.data.post?.let { dto ->
                dao.upsertPost(dto.toEntity())
                dao.deleteMediaFor(listOf(dto.id))
                dao.upsertMedia(dto.mediaEntities())
            }
            ApiResult.Success(Unit, result.message)
        }
        is ApiResult.Failure -> result
    }

    suspend fun refreshComments(postId: Long): ApiResult<Unit> = when (val result = safeApiCall(moshi) { commentsApi.comments(postId) }) {
        is ApiResult.Success -> { dao.upsertComments(result.data.comments.map { it.toEntity(postId) }); ApiResult.Success(Unit, result.message) }
        is ApiResult.Failure -> result
    }

    suspend fun addComment(postId: Long, content: String): ApiResult<Unit> = when (val result = safeApiCall(moshi) { commentsApi.create(CreateCommentRequest(commentableId = postId, content = content)) }) {
        is ApiResult.Success -> { dao.upsertComments(listOf(result.data.toEntity(postId))); dao.incrementComments(postId); ApiResult.Success(Unit, result.message) }
        is ApiResult.Failure -> result
    }

    suspend fun toggleLike(post: FeedPost): ApiResult<Unit> {
        val optimisticLiked = !post.isLiked
        val optimisticCount = (post.likesCount + if (optimisticLiked) 1 else -1).coerceAtLeast(0)
        dao.setLike(post.id, optimisticLiked, optimisticCount)
        return when (val result = safeApiCall(moshi) { likesApi.toggle(ToggleLikeRequest(likeableId = post.id)) }) {
            is ApiResult.Success -> {
                dao.setLike(post.id, result.data.liked ?: optimisticLiked, result.data.likesCount ?: optimisticCount)
                ApiResult.Success(Unit, result.message)
            }
            is ApiResult.Failure -> { dao.setLike(post.id, post.isLiked, post.likesCount); result }
        }
    }

    private suspend fun fetchPage(page: Int): ApiResult<Unit> = when (val result = safeApiCall(moshi) { postsApi.posts(page = page) }) {
        is ApiResult.Success -> {
            val posts = result.data.posts.map { it.toEntity() }
            dao.cachePosts(posts, result.data.posts.flatMap { it.mediaEntities() }, result.data.pagination.toEntity(), page == 1)
            ApiResult.Success(Unit, result.message)
        }
        is ApiResult.Failure -> result
    }

    private companion object { const val FEED_RESOURCE = "feed_posts" }
}

private fun PostDto.toEntity() = PostEntity(id, content.orEmpty(), (author ?: user)?.name ?: "Alumni ENSPM", (author ?: user)?.avatar, likesCount, commentsCount, isLiked, createdAt)
private fun PostDto.mediaEntities() = (medias.ifEmpty { media }).mapIndexed { index, m -> PostMediaEntity(m.id ?: (id * 1000 + index), id, m.thumbnailUrl ?: m.thumbUrl ?: m.url, m.url, m.type) }
private fun PaginationDto.toEntity() = PaginationMetadataEntity("feed_posts", currentPage, lastPage, total, perPage)
private fun PostEntity.toDomain(media: List<PostMediaEntity>) = FeedPost(id, content, authorName, authorAvatar, media.map { FeedMedia(it.thumbnailUrl, it.fullUrl, it.type) }, likesCount, commentsCount, isLiked, createdAt)
private fun CommentDto.toEntity(postId: Long) = PostCommentEntity(id, postId, content.orEmpty(), (author ?: user)?.name ?: "Alumni ENSPM", createdAt)
private fun PostCommentEntity.toDomain() = FeedComment(id, postId, content, authorName, createdAt)
