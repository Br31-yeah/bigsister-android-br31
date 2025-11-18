package com.smwu.bigsister.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 루틴 완료 기록 테이블
 * [수정됨] 시트 기준(Completion) 및 ERD 기준(ForeignKey) 적용
 */
@Entity(
    tableName = "completion_table",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routine_id"],
            onDelete = ForeignKey.CASCADE // ERD 시트 기준
        )
    ],
    indices = [Index(value = ["routine_id"])]
)
data class CompletionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0, // Long -> Int

    @ColumnInfo(name = "routine_id")
    val routineId: Int, // Long -> Int

    @ColumnInfo(name = "completed_at")
    val completedAt: Long, // 'completionTime' (ZonedDateTime) -> 'completedAt' (Long)

    @ColumnInfo(name = "total_time")
    val totalTime: Int, // 'totalTime' 필드 추가

    @ColumnInfo(name = "was_late")
    val wasLate: Boolean // 'wasLate' 필드 유지
)