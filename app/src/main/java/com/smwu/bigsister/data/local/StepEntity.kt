package com.smwu.bigsister.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "step_table",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routine_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("routine_id")
    ]
)
data class StepEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "routine_id")
    val routineId: Int,

    @ColumnInfo(name = "icon")
    val icon: String,

    @ColumnInfo(name = "name")
    val name: String,

    // 예상 소요 시간(분)
    @ColumnInfo(name = "duration")
    val duration: Int,

    @ColumnInfo(name = "memo")
    val memo: String,

    // 이동 단계 여부
    @ColumnInfo(name = "is_transport")
    val isTransport: Boolean,

    @ColumnInfo(name = "from")
    val from: String,

    @ColumnInfo(name = "to")
    val to: String,

    @ColumnInfo(name = "transport_mode")
    val transportMode: String,

    // 자동 계산된 이동 시간(분) – 이동 단계 아닐 수도 있으니 nullable
    @ColumnInfo(name = "calculated_duration")
    val calculatedDuration: Int? = null
)