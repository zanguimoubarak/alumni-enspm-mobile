package com.enspm.alumni.core.database

import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase

@Entity("posts")
data class PostEntity(
    @PrimaryKey val id: Long,
    val content: String,
    val authorName: String,
    val authorAvatar: String?,
    val likesCount: Int,
    val commentsCount: Int,
    val isLiked: Boolean,
    val createdAt: String?,
    val cachedAt: Long = System.currentTimeMillis(),
)

@Entity("post_medias", primaryKeys = ["id", "postId"])
data class PostMediaEntity(val id: Long, val postId: Long, val thumbnailUrl: String?, val fullUrl: String?, val type: String?)

@Entity("post_comments")
data class PostCommentEntity(
    @PrimaryKey val id: Long,
    val postId: Long,
    val content: String,
    val authorName: String,
    val createdAt: String?,
)

@Entity("pagination_metadata")
data class PaginationMetadataEntity(
    @PrimaryKey val resource: String,
    val currentPage: Int,
    val lastPage: Int,
    val total: Int,
    val perPage: Int,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Database(
    entities = [PostEntity::class, PostMediaEntity::class, PostCommentEntity::class, PaginationMetadataEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AlumniDatabase : RoomDatabase() {
    abstract fun feedDao(): FeedDao
}
