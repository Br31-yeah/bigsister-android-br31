package com.smwu.bigsister.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.smwu.bigsister.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    val currentUser: StateFlow<FirebaseUser?> = userRepository.currentUser

    fun onEmailChange(newEmail: String) {
        email = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
    }

    fun signIn(onSuccess: () -> Unit = {}) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "이메일과 비밀번호를 모두 입력해 주세요."
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            val result = userRepository.signInWithEmail(email.trim(), password)
            isLoading = false

            result
                .onSuccess {
                    onSuccess()
                }
                .onFailure { e ->
                    errorMessage = e.localizedMessage ?: "로그인에 실패했습니다."
                }
        }
    }

    fun signUp(onSuccess: () -> Unit = {}) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "이메일과 비밀번호를 모두 입력해 주세요."
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            val result = userRepository.signUpWithEmail(email.trim(), password)
            isLoading = false

            result
                .onSuccess {
                    onSuccess()
                }
                .onFailure { e ->
                    errorMessage = e.localizedMessage ?: "회원가입에 실패했습니다."
                }
        }
    }

    fun signOut() {
        userRepository.signOut()
    }
}