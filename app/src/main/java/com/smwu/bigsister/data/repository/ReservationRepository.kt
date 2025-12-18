package com.smwu.bigsister.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.smwu.bigsister.data.local.ReservationEntity
import com.smwu.bigsister.data.local.dao.ReservationDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReservationRepository @Inject constructor(
    private val reservationDao: ReservationDao,
    private val auth: FirebaseAuth
) {

    /** ✅ 특정 날짜의 내 예약 목록 가져오기 */
    fun getReservationsByDate(date: String): Flow<List<ReservationEntity>> {
        val user = auth.currentUser
        return if (user != null) {
            reservationDao.getReservationsForDate(date, user.uid)
        } else {
            emptyFlow()
        }
    }

    /** ✅ 특정 월의 내 예약 목록 가져오기 */
    fun getReservationsByMonth(month: String): Flow<List<ReservationEntity>> {
        val user = auth.currentUser
        return if (user != null) {
            reservationDao.getReservationsForMonth(month, user.uid)
        } else {
            emptyFlow()
        }
    }

    /** ✅ 기간별 예약 조회 */
    fun getReservationsBetweenDates(startDate: String, endDate: String): Flow<List<ReservationEntity>> {
        val user = auth.currentUser
        return if (user != null) {
            reservationDao.getReservationsBetweenDates(startDate, endDate, user.uid)
        } else {
            emptyFlow()
        }
    }

    /** ✅ [추가] 단일 예약 정보 조회 (ViewModel 에러 해결) */
    suspend fun getReservationById(id: Long): ReservationEntity? {
        return reservationDao.getReservationById(id)
    }

    /** ✅ 예약 정보 저장 */
    suspend fun insertReservation(reservation: ReservationEntity): Long {
        return reservationDao.insertReservation(reservation)
    }

    /** ✅ 예약 정보 삭제 */
    suspend fun deleteReservation(reservationId: Long) {
        reservationDao.deleteReservationById(reservationId)
    }
}