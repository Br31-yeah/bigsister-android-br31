package com.smwu.bigsister.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "completion_table",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routine_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("routine_id"),
        Index("completed_at")
    ]
)
data class CompletionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "routine_id")
    val routineId: Int,

    // 완료 시각 (timestamp)
    @ColumnInfo(name = "completed_at")
    val completedAt: Long,

    // 실제 수행 시간 (sec 또는 min – 팀에서 정하기 나름)
    @ColumnInfo(name = "total_time")
    val totalTime: Int,

    @ColumnInfo(name = "was_late")
    val wasLate: Boolean
)