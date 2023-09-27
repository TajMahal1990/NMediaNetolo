package ru.netology.nmedia.model

import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.RetryTypes

data class FeedModelState(
    val refreshing: Boolean = false,
    val error: Boolean = false,
    val loading: Boolean = false,
    val loginError: Boolean = false,
    val registrationError: Boolean = false,

    val retryId: Long = 0,
    val retryType: RetryTypes? = null,
    val retryPost: Post? = null,
)