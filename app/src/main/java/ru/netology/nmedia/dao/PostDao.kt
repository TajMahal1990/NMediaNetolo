package ru.netology.nmedia.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {

    @Query("SELECT * FROM PostEntity WHERE viewed = 0 ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT * FROM PostEntity WHERE viewed = 0 ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, PostEntity>

    @Insert(onConflict = REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("UPDATE PostEntity SET content = :content WHERE id = :id")
    suspend fun updateContentById(id: Long, content: String)

    suspend fun save(post: PostEntity) =
        if (post.id == 0L) insert(post) else updateContentById(post.id, post.content)

    @Query("""
        UPDATE PostEntity SET
        likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id
        """)
    suspend fun likeById(id: Long)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("UPDATE PostEntity SET viewed = 0 WHERE viewed = 1")
    suspend fun viewedPosts()

    @Query("DELETE FROM PostEntity")
    suspend fun clear()

//    fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
//    fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)
}