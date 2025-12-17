package com.smwu.bigsister.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.smwu.bigsister.data.local.UserEntity
import com.smwu.bigsister.data.local.dao.UserDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Firebase 로그인 + 로컬 UserEntity(Room) 통합 저장소 */
@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val userDao: UserDao
) {

    /** FirebaseAuth 로그인 상태 */
    private val _firebaseUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val firebaseUser: StateFlow<FirebaseUser?> = _firebaseUser.asStateFlow()

    /** 로컬 DB의 UserEntity 흐름 */
    val localUser: Flow<UserEntity?> = userDao.getCurrentUser()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _firebaseUser.value = firebaseAuth.currentUser
        }
    }

    /** 이메일 로그인 + 로컬 DB 저장 */
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: error("로그인 후 사용자 정보 없음")

            saveToLocal(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** 이메일 회원가입 + 로컬 DB 저장 */
    suspend fun signUpWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: error("회원가입 후 사용자 정보 없음")

            saveToLocal(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** 로그아웃 + 로컬 DB의 유저 삭제 */
    suspend fun signOut() {
        auth.signOut()
        userDao.clearUser() // 로컬 유저 정보 삭제
        _firebaseUser.value = null
    }

    /** 회원탈퇴 (계정 삭제) */
    suspend fun deleteAccount(): Result<Unit> = runCatching {
        val user = auth.currentUser ?: throw IllegalStateException("로그인된 사용자가 없습니다.")

        // 1. Firebase Auth 계정 영구 삭제
        user.delete().await()

        // 2. 로컬 DB 데이터 정리
        userDao.clearUser()
        _firebaseUser.value = null
    }

    /** * ✅ [추가] 비밀번호 재설정 이메일 발송
     * Firebase가 해당 이메일로 비밀번호 재설정 링크를 보냅니다.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        if (email.isBlank()) throw IllegalArgumentException("이메일을 입력해주세요.")
        auth.sendPasswordResetEmail(email).await()
    }

    /** FirebaseUser → UserEntity 변환 후 DB 저장 */
    private suspend fun saveToLocal(user: FirebaseUser) {
        val entity = UserEntity(
            id = user.uid.hashCode().toLong(),       // String UID → 고유 Long 값 변환
            nickname = user.displayName,
            email = user.email,
            profileImageUrl = user.photoUrl?.toString()
        )
        userDao.upsertUser(entity)
    }
}