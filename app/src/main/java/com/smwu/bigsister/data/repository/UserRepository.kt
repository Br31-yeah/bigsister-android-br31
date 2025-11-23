package com.smwu.bigsister.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth
) {

    // 현재 로그인한 FirebaseUser 를 감시하기 위한 StateFlow
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        // 로그인 상태 실시간 반영
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    /** 이메일 + 비밀번호로 로그인 */
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: error("로그인 후 사용자 정보 없음")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** 이메일 + 비밀번호로 회원가입 (필요 없으면 안 써도 됨) */
    suspend fun signUpWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: error("회원가입 후 사용자 정보 없음")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** 로그아웃 */
    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }
}