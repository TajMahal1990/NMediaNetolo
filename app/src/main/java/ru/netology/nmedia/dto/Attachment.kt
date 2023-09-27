package ru.netology.nmedia.dto

data class Attachment(
    val url: String,
    val type: TypeAttachment,
) {
    fun toDto() = Attachment(url, type)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            Attachment(it.url, it.type)
        }
    }
}

enum class TypeAttachment {
    IMAGE
}