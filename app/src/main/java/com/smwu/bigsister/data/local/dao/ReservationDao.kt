package com.smwu.bigsister.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.smwu.bigsister.data.local.ReservationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservationDao {

    /* ────────────────────────────────
       예약 추가
    ──────────────────────────────── */
    @Insert
    suspend fun insertReservation(reservation: ReservationEntity)

    /* ────────────────────────────────
       예약 삭제
    ──────────────────────────────── */
    @Query("DELETE FROM reservation_table WHERE id = :reservationId")
    suspend fun deleteReservationById(reservationId: Long)

    /* ────────────────────────────────
       날짜별 예약 조회 ✅ [수정] userId 필터 추가
    ──────────────────────────────── */
    @Query("""
        SELECT *
        FROM reservation_table
        WHERE date = :date AND userId = :userId
        ORDER BY startTime ASC
    """)
    fun getReservationsForDate(date: String, userId: String): Flow<List<ReservationEntity>>

    /* ────────────────────────────────
       월별 예약 조회 ✅ [수정] userId 필터 추가
    ──────────────────────────────── */
    @Query("""
        SELECT *
        FROM reservation_table
        WHERE date LIKE :month || '%' AND userId = :userId
        ORDER BY date ASC, startTime ASC
    """)
    fun getReservationsForMonth(month: String, userId: String): Flow<List<ReservationEntity>>

    /* ────────────────────────────────
       기간 조회 ✅ [수정] userId 필터 추가
    ──────────────────────────────── */
    @Query("""
        SELECT *
        FROM reservation_table
        WHERE (date BETWEEN :startDate AND :endDate) AND userId = :userId
        ORDER BY date ASC, startTime ASC
    """)
    fun getReservationsBetweenDates(
        startDate: String,
        endDate: String,
        userId: String
    ): Flow<List<ReservationEntity>>

    /* ────────────────────────────────
       단일 예약 조회
    ──────────────────────────────── */
    @Query("""
        SELECT *
        FROM reservation_table
        WHERE id = :id
        LIMIT 1
    """)
    suspend fun getReservationById(id: Long): ReservationEntity?
}