package com.smwu.bigsister.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smwu.bigsister.data.local.ReservationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservationDao {

    @Query("SELECT * FROM reservation_table WHERE date = :date")
    fun getReservationsByDate(date: String): Flow<List<ReservationEntity>>

    @Query("SELECT * FROM reservation_table")
    fun getAllReservations(): Flow<List<ReservationEntity>>

    @Query("SELECT * FROM reservation_table WHERE date BETWEEN :startDate AND :endDate")
    fun getReservationsBetweenDates(
        startDate: String,
        endDate: String
    ): Flow<List<ReservationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(reservation: ReservationEntity)

    @Query("DELETE FROM reservation_table WHERE id = :id")
    suspend fun deleteReservationById(id: Long)
}