package com.smwu.bigsister.ui.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.smwu.bigsister.data.repository.RoutineRepository
import com.smwu.bigsister.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val routineRepository: RoutineRepository // ✅ 동기화용
) : ViewModel() {

    // 입력 필드 상태
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var nickname by mutableStateOf("") // 회원가입용
        private set

    // 로딩 및 에러 상태
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // 현재 로그인된 유저 상태
    val currentUser: StateFlow<FirebaseUser?> = userRepository.firebaseUser

    // ────────────────────────────
    // 입력 이벤트 처리
    // ────────────────────────────
    fun onEmailChange(newEmail: String) { email = newEmail }
    fun onPasswordChange(newPassword: String) { password = newPassword }
    fun onNicknameChange(newNickname: String) { nickname = newNickname }

    // ────────────────────────────
    // 로그인
    // ────────────────────────────
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
                // 🔥 로그인 성공 시: 서버에 있는 내 루틴 데이터 가져오기 (동기화)
                try {
                    routineRepository.syncWithServer(user.uid)
                } catch (e: Exception) {
                    e.printStackTrace() // 동기화 실패해도 로그인은 성공 처리
                }
                isLoading = false
                onSuccess()
            }.onFailure { e ->
                isLoading = false
                errorMessage = "로그인 실패: ${e.message}"
            }
        }
    }

    // ────────────────────────────
    // 회원가입
    // ────────────────────────────
    fun signUp(onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "이메일과 비밀번호를 입력해주세요."
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
                // 닉네임 저장 로직이 있다면 여기서 처리 (Firestore User 컬렉션 등)
                // 지금은 바로 성공 처리
                isLoading = false
                onSuccess()
            }.onFailure { e ->
                isLoading = false
                errorMessage = "회원가입 실패: ${e.message}"
            }
        }
    }
}