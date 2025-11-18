package com.smwu.bigsister.data.repository

import com.smwu.bigsister.data.local.RoutineEntity
import com.smwu.bigsister.data.local.dao.ReservationDao
import com.smwu.bigsister.data.local.dao.RoutineDao
import com.smwu.bigsister.data.local.ReservationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * 루틴 예약 정보(스케줄) 관리 Repository
 * [수정됨] 이름 변경 및 새 DAO/Entity 참조
 */
class ReservationRepository @Inject constructor(
    private val reservationDao: ReservationDao,
    private val routineDao: RoutineDao
) {

    /**
     * '홈 화면'에 표시될 예약된 루틴의 상세 정보
     * (ReservationEntity와 RoutineEntity를 조합)
     */
    data class ScheduledRoutineInfo(
        val reservationId: Int,
        val routineId: Int,
        val routineTitle: String,
        // val routineIcon: String, // TODO: RoutineEntity에 아이콘 필드 추가 시
        val date: String,
        val startTime: String
        // val totalDuration: Int // TODO: RoutineEntity에 총 소요시간 필드 추가 시
    )

    /**
     * [수정됨] 'date'(String)를 인자로 받아 조합된 데이터를 반환
     */
    fun getScheduledRoutinesForDate(date: String): Flow<List<ScheduledRoutineInfo>> {
        val reservationsFlow = reservationDao.getReservationsForDate(date)
        val routinesFlow = routineDao.getAllRoutines() // TODO: 최적화 필요

        return combine(reservationsFlow, routinesFlow) { reservations, routines ->
            reservations.mapNotNull { reservation ->
                val routine = routines.find { it.id == reservation.routineId }
                routine?.let {
                    ScheduledRoutineInfo(
                        reservationId = reservation.id,
                        routineId = it.id,
                        routineTitle = it.title,
                        date = reservation.date,
                        startTime = reservation.startTime
                    )
                }
            }
        }
    }

    suspend fun addReservation(reservation: ReservationEntity) {
        reservationDao.insertReservation(reservation)
    }

    suspend fun deleteReservation(reservationId: Int) {
        reservationDao.deleteReservationById(reservationId)
    }
}