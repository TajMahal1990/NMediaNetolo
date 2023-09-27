package ru.netology.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data: Flow<PagingData<FeedItem>>
    suspend fun getNewPosts()
    suspend fun getAll()
    fun getNewerCount(firstId: Long): Flow<Int>
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, upload: MediaUpload)
    suspend fun uploadWithContent(upload: MediaUpload): Media
    suspend fun likeById(id: Long)
    suspend fun unlikeById(id: Long)
    suspend fun removeById(id: Long)
}