package com.enspm.alumni.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {
    @Query("SELECT * FROM posts ORDER BY datetime(createdAt) DESC, id DESC") fun observePosts(): Flow<List<PostEntity>>
    @Query("SELECT * FROM posts WHERE id = :id") fun observePost(id: Long): Flow<PostEntity?>
    @Query("SELECT * FROM post_medias WHERE postId = :postId") fun observeMedia(postId: Long): Flow<List<PostMediaEntity>>
    @Query("SELECT * FROM post_comments WHERE postId = :postId ORDER BY datetime(createdAt) ASC, id ASC") fun observeComments(postId: Long): Flow<List<PostCommentEntity>>
    @Query("SELECT * FROM pagination_metadata WHERE resource = :resource") suspend fun pagination(resource: String): PaginationMetadataEntity?
    @Upsert suspend fun upsertPosts(posts: List<PostEntity>)
    @Upsert suspend fun upsertPost(post: PostEntity)
    @Upsert suspend fun upsertMedia(media: List<PostMediaEntity>)
    @Upsert suspend fun upsertComments(comments: List<PostCommentEntity>)
    @Upsert suspend fun upsertPagination(meta: PaginationMetadataEntity)
    @Query("DELETE FROM post_medias WHERE postId IN (:postIds)") suspend fun deleteMediaFor(postIds: List<Long>)
    @Query("UPDATE posts SET isLiked = :liked, likesCount = :likesCount WHERE id = :postId") suspend fun setLike(postId: Long, liked: Boolean, likesCount: Int)
    @Query("UPDATE posts SET commentsCount = commentsCount + 1 WHERE id = :postId") suspend fun incrementComments(postId: Long)

    @Transaction suspend fun cachePosts(posts: List<PostEntity>, media: List<PostMediaEntity>, meta: PaginationMetadataEntity, replacePageOne: Boolean) {
        if (replacePageOne) clearPosts()
        upsertPosts(posts)
        deleteMediaFor(posts.map { it.id })
        upsertMedia(media)
        upsertPagination(meta)
    }

    @Query("DELETE FROM posts") suspend fun clearPosts()
}
