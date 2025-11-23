package com.smwu.bigsister.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey
    val id: Long,                // Kakao user id

    val nickname: String?,
    val email: String?,
    val profileImageUrl: String?
)