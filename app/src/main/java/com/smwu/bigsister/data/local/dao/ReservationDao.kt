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
    suspend fun insertReservation(reservation: ReservationEntity): Long

    @Query("DELETE FROM reservation_table WHERE id = :reservationId")
    suspend fun deleteReservationById(reservationId: Long)

    @Query("DELETE FROM reservation_table WHERE userId = :userId")
    suspend fun deleteReservationsByUserId(userId: String)

    @Query("""
        SELECT *
        FROM reservation_table
        WHERE date = :date AND userId = :userId
        ORDER BY startTime ASC
    """)
    fun getReservationsForDate(date: String, userId: String): Flow<List<ReservationEntity>>

    @Query("""
        SELECT *
        FROM reservation_table
        WHERE date LIKE :month || '%' AND userId = :userId
        ORDER BY date ASC, startTime ASC
    """)
    fun getReservationsForMonth(month: String, userId: String): Flow<List<ReservationEntity>>

    /** ✅ [추가됨] 기간별 예약 조회 함수 */
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

    @Query("SELECT * FROM reservation_table WHERE userId = :userId ORDER BY date ASC, startTime ASC")
    fun getReservationsByUserId(userId: String): Flow<List<ReservationEntity>>

    @Query("SELECT * FROM reservation_table WHERE id = :id LIMIT 1")
    suspend fun getReservationById(id: Long): ReservationEntity?
}