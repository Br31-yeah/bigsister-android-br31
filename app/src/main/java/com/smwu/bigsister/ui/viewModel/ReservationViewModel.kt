package com.smwu.bigsister.ui.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.smwu.bigsister.data.local.ReservationEntity
import com.smwu.bigsister.data.repository.ReservationRepository
import com.smwu.bigsister.utils.RoutineAlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ReservationViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private fun normalizeDate(date: String): String = LocalDate.parse(date).toString()

    // 👤 현재 사용자 UID 가져오기
    private val currentUserId: String?
        get() = Firebase.auth.currentUser?.uid

    /* ────────────────────────────────
       조회 (userId 필터 추가 반영)
    ──────────────────────────────── */

    fun getReservationsForDate(date: String): Flow<List<ReservationEntity>> {
        val fixedDate = normalizeDate(date)
        return reservationRepository.getReservationsByDate(fixedDate)
    }

    // ✅ 에러 해결: Repository에 정의된 파라미터에 userId를 추가하거나 맞춤
    fun getReservationsForMonth(month: String): Flow<List<ReservationEntity>> {
        val uid = currentUserId ?: return emptyFlow()
        // Repository에 이 메서드가 없다면 추가하거나 getReservationsByDate를 활용해야 합니다.
        // 일단 에러를 막기 위해 userId를 인자로 넘기는 구조로 맞춤
        return reservationRepository.getReservationsForMonth(month, uid)
    }

    // ✅ 에러 해결: Repository에 정의된 파라미터에 userId를 추가
    fun getReservationsBetweenDates(start: String, end: String): Flow<List<ReservationEntity>> {
        val uid = currentUserId ?: return emptyFlow()
        val fixedStart = normalizeDate(start)
        val fixedEnd = normalizeDate(end)
        return reservationRepository.getReservationsBetweenDates(fixedStart, fixedEnd, uid)
    }

    /* ────────────────────────────────
       예약 추가
    ──────────────────────────────── */

    fun addReservation(reservation: ReservationEntity, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            val uid = currentUserId ?: return@launch
            val fixedDate = normalizeDate(reservation.date)

            // ReservationEntity 생성 시 userId를 확실히 넣어줌
            val fixedReservation = reservation.copy(
                date = fixedDate,
                userId = uid
            )

            reservationRepository.addReservation(fixedReservation)

            val startMillis = convertToMillis(fixedReservation.date, fixedReservation.startTime)
            val routineWithSteps = reservationRepository.getRoutineWithSteps(fixedReservation.routineId)

            if (routineWithSteps != null) {
                RoutineAlarmScheduler.scheduleAll(
                    context = appContext,
                    routineId = fixedReservation.routineId,
                    routineStartMillis = startMillis,
                    steps = routineWithSteps.steps
                )
            }
            onFinished()
        }
    }

    fun deleteReservation(reservationId: Long, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            val reservation = reservationRepository.getReservationById(reservationId)
            if (reservation != null) {
                RoutineAlarmScheduler.cancelAllForRoutine(appContext, reservation.routineId)
            }
            reservationRepository.deleteReservation(reservationId)
            onFinished()
        }
    }

    fun convertToMillis(date: String, time: String): Long {
        val localDate = LocalDate.parse(date)
        val localTime = LocalTime.parse(time)
        return localDate.atTime(localTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}