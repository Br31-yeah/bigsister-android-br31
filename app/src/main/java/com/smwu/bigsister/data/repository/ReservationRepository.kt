package com.smwu.bigsister.data.repository

import com.smwu.bigsister.data.local.AppDatabase
import com.smwu.bigsister.data.local.ReservationEntity
import com.smwu.bigsister.data.local.dao.ReservationDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReservationRepository @Inject constructor(
    db: AppDatabase
) {
    private val reservationDao: ReservationDao = db.reservationDao()

    fun getReservationsByDate(date: String): Flow<List<ReservationEntity>> =
        reservationDao.getReservationsByDate(date)

    fun getAllReservations(): Flow<List<ReservationEntity>> =
        reservationDao.getAllReservations()

    suspend fun addReservation(reservation: ReservationEntity): Long =
        reservationDao.insertReservation(reservation)

    suspend fun deleteReservation(id: Long) =
        reservationDao.deleteReservationById(id)
}