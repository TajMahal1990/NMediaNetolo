package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val authorId: Long,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val viewed: Boolean = false,
    @Embedded
    val attachment: Attachment?,
    val ownedByMe: Boolean,
) {
    fun toDto() = Post(id,
        author,
        authorId,
        authorAvatar,
        content,
        published,
        likedByMe,
        likes,
        viewed,
        attachment?.toDto(),
        ownedByMe)

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                dto.id,
                dto.author,
                dto.authorId,
                dto.authorAvatar,
                dto.content,
                dto.published,
                dto.likedByMe,
                dto.likes,
                dto.viewed,
                Attachment.fromDto(dto.attachment),
                dto.ownedByMe
            )
    }
}

fun List<PostEntity>.toDto() = map { it.toDto() }
fun List<Post>.toEntity() = map { PostEntity.fromDto(it) }