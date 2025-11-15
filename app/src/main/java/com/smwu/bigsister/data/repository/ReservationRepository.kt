package com.smwu.bigsister.data.repository

import com.smwu.bigsister.data.local.ReservationEntity
import com.smwu.bigsister.data.local.dao.ReservationDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReservationRepository @Inject constructor(
    private val reservationDao: ReservationDao
) {

    fun getReservationsByDate(date: String): Flow<List<ReservationEntity>> =
        reservationDao.getReservationsByDate(date)

    fun getAllReservations(): Flow<List<ReservationEntity>> =
        reservationDao.getAllReservations()

    fun getReservationsBetweenDates(
        startDate: String,
        endDate: String
    ): Flow<List<ReservationEntity>> =
        reservationDao.getReservationsBetweenDates(startDate, endDate)

    suspend fun addReservation(reservation: ReservationEntity) {
        reservationDao.insertReservation(reservation)
    }

    suspend fun deleteReservation(id: Long) {
        reservationDao.deleteReservationById(id)
    }
}