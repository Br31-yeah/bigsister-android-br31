package com.smwu.bigsister.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smwu.bigsister.data.local.ReservationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(reservation: ReservationEntity) // 'schedule' -> 'reservation'

    @Query("DELETE FROM reservation_table WHERE id = :reservationId")
    suspend fun deleteReservationById(reservationId: Int) // Long -> Int

    /**
     * [수정됨] 'date'(String) 필드로 특정 날짜의 예약을 조회
     */
    @Query("SELECT * FROM reservation_table WHERE date = :date ORDER BY start_time ASC")
    fun getReservationsForDate(date: String): Flow<List<ReservationEntity>>

    /**
     * [수정됨] 'date' 필드와 LIKE 검색으로 특정 월의 예약을 조회 (e.g., "2025-11%")
     */
    @Query("SELECT * FROM reservation_table WHERE date LIKE :month || '%'")
    fun getReservationsForMonth(month: String): Flow<List<ReservationEntity>>
}