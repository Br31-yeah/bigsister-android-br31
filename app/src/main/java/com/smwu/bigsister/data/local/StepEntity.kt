package com.smwu.bigsister.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 루틴 내 개별 단계 테이블
 * [수정됨] 시트 기준(Step)에 따라 필드명(name, duration 등) 및 타입(Int) 변경
 */
@Entity(
    tableName = "step_table",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"], // Long -> Int
            childColumns = ["routine_id"],
            onDelete = ForeignKey.CASCADE // ERD 시트 기준
        )
    ],
    indices = [Index(value = ["routine_id"])]
)
data class StepEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0, // Long -> Int

    @ColumnInfo(name = "routine_id")
    val routineId: Int, // Long -> Int

    @ColumnInfo(name = "icon")
    val icon: String, // 'icon' 필드 추가

    @ColumnInfo(name = "name")
    val name: String, // 'stepName' -> 'name'

    @ColumnInfo(name = "duration")
    val duration: Int, // 'durationInMinutes' -> 'duration'

    @ColumnInfo(name = "memo")
    val memo: String?, // 'memo' 필드 추가

    @ColumnInfo(name = "is_transport")
    val isTransport: Boolean, // 'isMovementStep' -> 'isTransport'

    @ColumnInfo(name = "from_location") // 'from'이 SQL 예약어일 수 있어 'from_location' 사용
    val from: String?, // 'startLocation' -> 'from'

    @ColumnInfo(name = "to_location") // 'to'가 SQL 예약어일 수 있어 'to_location' 사용
    val to: String?, // 'endLocation' -> 'to'

    @ColumnInfo(name = "transport_mode")
    val transportMode: String?, // 'transportMode' 필드 추가

    @ColumnInfo(name = "calculated_duration")
    val calculatedDuration: Int?, // 'calculatedDuration' 필드 추가

    // 기능 구현에 필수적이므로 'stepOrder' 유지
    @ColumnInfo(name = "step_order")
    val stepOrder: Int
)