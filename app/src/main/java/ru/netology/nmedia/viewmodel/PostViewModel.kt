package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.RetryTypes
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    authorId = 0L,
    authorAvatar = "",
    likedByMe = false,
    likes = 0,
    published = "",
    attachment = null
)

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
) : ViewModel() {

    private val cached = repository.data.cachedIn(viewModelScope)

    val data: Flow<PagingData<FeedItem>> = cached

    private val edited = MutableLiveData(empty)

    private val _state = MutableLiveData<FeedModelState>()
    val state: LiveData<FeedModelState>
        get() = _state

//    val newerCount: LiveData<Int> = data.switchMap {
//        repository.getNewerCount(it.posts.firstOrNull()?.id ?: 0L)
//            .asLiveData(Dispatchers.Default)
//    }

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val scope = MainScope()

    private val withoutPhoto = PhotoModel()
    private val _photo = MutableLiveData(withoutPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    init {
        loadPosts()
    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _state.value = FeedModelState(loading = true)
            repository.getNewPosts()
            _state.value = FeedModelState()
        } catch (e: Exception) {
            _state.value = FeedModelState(error = true)
        }
    }

    fun save() {
        edited.value?.let {
            viewModelScope.launch {
                try {
                    when (_photo.value) {
                        withoutPhoto -> repository.save(it)
                        else -> _photo.value?.file?.let { file ->
                            repository.saveWithAttachment(it, MediaUpload(file))
                        }
                    }
                    _state.value = FeedModelState()
                } catch (e: Exception) {
                    _state.value = FeedModelState(error = true)
                }
                _postCreated.value = Unit
            }
        }
        edited.value = empty
        _photo.value = withoutPhoto
    }

    fun retrySave(post: Post?) {
        viewModelScope.launch {
            try {
                if (post != null) {
                    save()
                    loadPosts()
                }
            } catch (e: Exception) {
                _state.value =
                    FeedModelState(error = true, retryPost = post)
            }
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = if (uri != null && file != null) {
            PhotoModel(uri, file)
        } else {
            null
        }
    }

    fun likeById(id: Long) = viewModelScope.launch {
        try {
            repository.likeById(id)
        } catch (e: Exception) {
            _state.value = FeedModelState(error = true)
        }
    }

    fun unlikeById(id: Long) = viewModelScope.launch {
        try {
            repository.unlikeById(id)
        } catch (e: Exception) {
            _state.value = FeedModelState(error = true, retryId = id)
        }
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            repository.removeById(id)
        } catch (e: Exception) {
            _state.value = FeedModelState(error = true, retryType = RetryTypes.REMOVE, retryId = id)
        }
    }

    fun loadNewPosts() = viewModelScope.launch {
        try {
            _state.value = FeedModelState(loading = true)
            repository.getNewPosts()
            _state.value = FeedModelState()
        } catch (e: Exception) {
            _state.value = FeedModelState(error = true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}