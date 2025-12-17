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
       ❗ REPLACE 절대 금지
       autoGenerate PK + REPLACE = 덮어쓰기 발생
    ──────────────────────────────── */
    @Insert
    suspend fun insertReservation(reservation: ReservationEntity)

    /* ────────────────────────────────
       예약 삭제
    ──────────────────────────────── */
    @Query("DELETE FROM reservation_table WHERE id = :reservationId")
    suspend fun deleteReservationById(reservationId: Long)

    /* ────────────────────────────────
       날짜별 예약 조회
       ✔ 같은 루틴
       ✔ 다른 시작 시간
       ✔ 전부 반환됨
    ──────────────────────────────── */
    @Query("""
        SELECT *
        FROM reservation_table
        WHERE date = :date
        ORDER BY startTime ASC
    """)
    fun getReservationsForDate(date: String): Flow<List<ReservationEntity>>

    /* ────────────────────────────────
       월별 예약 조회 (Stats / Calendar 용)
       ex) month = "2025-12"
    ──────────────────────────────── */
    @Query("""
        SELECT *
        FROM reservation_table
        WHERE date LIKE :month || '%'
        ORDER BY date ASC, startTime ASC
    """)
    fun getReservationsForMonth(month: String): Flow<List<ReservationEntity>>

    /* ────────────────────────────────
       기간 조회 (옵션)
    ──────────────────────────────── */
    @Query("""
        SELECT *
        FROM reservation_table
        WHERE date BETWEEN :startDate AND :endDate
        ORDER BY date ASC, startTime ASC
    """)
    fun getReservationsBetweenDates(
        startDate: String,
        endDate: String
    ): Flow<List<ReservationEntity>>

    /* ────────────────────────────────
       단일 예약 조회 (알람 취소용)
    ──────────────────────────────── */
    @Query("""
        SELECT *
        FROM reservation_table
        WHERE id = :id
        LIMIT 1
    """)
    suspend fun getReservationById(id: Long): ReservationEntity?
}