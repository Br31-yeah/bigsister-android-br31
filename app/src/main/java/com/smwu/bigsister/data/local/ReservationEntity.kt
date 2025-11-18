package com.smwu.bigsister.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 예약된 루틴 테이블 (구: ScheduleEntity)
 * [수정됨] 시트 기준(Reservation)에 따라 이름 및 필드(date, startTime) 변경
 */
@Entity(
    tableName = "reservation_table", // 'schedule_table' -> 'reservation_table'
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routine_id"],
            onDelete = ForeignKey.CASCADE // ERD 시트 기준
        )
    ],
    indices = [Index(value = ["routine_id"]), Index(value = ["date"])]
)
data class ReservationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0, // Long -> Int

    @ColumnInfo(name = "routine_id")
    val routineId: Int, // Long -> Int

    @ColumnInfo(name = "date")
    val date: String, // YYYY-MM-DD 형식 (ZonedDateTime -> String)

    @ColumnInfo(name = "start_time")
    val startTime: String // HH:mm 형식 (ZonedDateTime -> String)
)