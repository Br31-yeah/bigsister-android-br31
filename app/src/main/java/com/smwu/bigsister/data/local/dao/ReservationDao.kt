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
    suspend fun insertReservation(reservation: ReservationEntity)

    @Query("DELETE FROM reservation_table WHERE id = :reservationId")
    suspend fun deleteReservationById(reservationId: Long)

    // 특정 날짜 조회 (정렬됨)
    @Query("SELECT * FROM reservation_table WHERE date = :date ORDER BY startTime ASC")
    fun getReservationsForDate(date: String): Flow<List<ReservationEntity>>

    // 특정 월 조회 (ex: 2025-11)
    @Query("SELECT * FROM reservation_table WHERE date LIKE :month || '%' ORDER BY date ASC")
    fun getReservationsForMonth(month: String): Flow<List<ReservationEntity>>

    // 옵션: 필요하면 유지할 기간 조회
    @Query("SELECT * FROM reservation_table WHERE date BETWEEN :startDate AND :endDate")
    fun getReservationsBetweenDates(
        startDate: String,
        endDate: String
    ): Flow<List<ReservationEntity>>

    //  예약 1개 조회 (알림 취소용)
    @Query("SELECT * FROM reservation_table WHERE id = :id LIMIT 1")
    suspend fun getReservationById(id: Long): ReservationEntity?
}