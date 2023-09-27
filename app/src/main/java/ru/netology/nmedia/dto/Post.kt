package ru.netology.nmedia.dto

sealed interface FeedItem {
    val id: Long
}

data class Post(
    override val id: Long,
    val author: String,
    val authorId: Long,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val viewed: Boolean = false,
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false,
) : FeedItem

data class Ad(
    override val id: Long,
    val image: String,
) : FeedItem