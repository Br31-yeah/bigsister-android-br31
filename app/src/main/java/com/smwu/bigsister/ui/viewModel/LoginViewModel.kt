package com.smwu.bigsister.ui.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.smwu.bigsister.data.repository.RoutineRepository
import com.smwu.bigsister.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val routineRepository: RoutineRepository
) : ViewModel() {

    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var nickname by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    val currentUser: StateFlow<FirebaseUser?> = userRepository.firebaseUser

    fun onEmailChange(newEmail: String) { email = newEmail }
    fun onPasswordChange(newPassword: String) { password = newPassword }
    fun onNicknameChange(newNickname: String) { nickname = newNickname }

    fun signIn(onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "이메일과 비밀번호를 입력해주세요."
            return
        }
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val result = userRepository.signInWithEmail(email.trim(), password)
            result.onSuccess { user ->
                try {
                    // 로그인 후 서버 데이터 동기화
                    routineRepository.syncWithServer(user.uid)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                isLoading = false
                onSuccess()
            }.onFailure { e ->
                isLoading = false
                errorMessage = "로그인 실패: ${e.message}"
            }
        }
    }

    fun signUp(onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank() || nickname.isBlank()) {
            errorMessage = "모든 정보를 입력해주세요."
            return
        }
        if (password.length < 6) {
            errorMessage = "비밀번호는 6자리 이상이어야 합니다."
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            val result = userRepository.signUpWithEmail(email.trim(), password)
            result.onSuccess { user ->
                try {
                    // 1. Firebase Auth 프로필에 닉네임 저장
                    val profileUpdates = userProfileChangeRequest {
                        displayName = nickname
                    }
                    user.updateProfile(profileUpdates).await()

                    // ✅ 2. 중요: 변경된 프로필 정보를 로컬 메모리에 즉시 동기화
                    user.reload().await()

                    isLoading = false
                    onSuccess()
                } catch (e: Exception) {
                    isLoading = false
                    errorMessage = "프로필 설정 실패: ${e.message}"
                }
            }.onFailure { e ->
                isLoading = false
                errorMessage = "회원가입 실패: ${e.message}"
            }
        }
    }
}