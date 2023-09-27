package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _data = MutableLiveData<User>()
    val data: LiveData<User>
        get() = _data

    private val _state = MutableLiveData<FeedModelState>()
    val state: LiveData<FeedModelState>
        get() = _state

    fun registrationUser(login: String, password: String, name: String) {
        viewModelScope.launch {
            try {
                val user = authRepository.registrationUser(login, password, name)
                _data.value = user
            } catch (e: Exception) {
                _state.postValue(FeedModelState(registrationError = true))
            }
        }
    }
}