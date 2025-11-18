package com.smwu.bigsister.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 루틴 테이블 (e.g., "학교 가기", "알바 가기")
 * (시트 기준에 따라 title, createdAt 필드 및 snake_case 적용)
 */
@Entity(tableName = "routine_table")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "title")
    val title: String, // 'routineName' -> 'title'

    @ColumnInfo(name = "created_at")
    val createdAt: Long // 'createdAt' 필드 추가
)