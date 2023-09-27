package ru.netology.nmedia.service.notifications

data class Push(
    val content: String,
    val recipientId: Long?,
)