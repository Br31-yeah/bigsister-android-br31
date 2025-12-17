package com.smwu.bigsister.ui.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smwu.bigsister.data.local.ReservationEntity
import com.smwu.bigsister.data.repository.ReservationRepository
import com.smwu.bigsister.utils.RoutineAlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
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

    /* ────────────────────────────────
       🔑 날짜 정규화 (핵심)
       yyyy-MM-dd 로 강제 통일
    ──────────────────────────────── */

    private fun normalizeDate(date: String): String =
        LocalDate.parse(date).toString()

    /* ────────────────────────────────
       조회
    ──────────────────────────────── */

    fun getReservationsForDate(date: String): Flow<List<ReservationEntity>> {
        val fixedDate = normalizeDate(date)
        Log.d("RESERVATION", "QUERY date = $fixedDate")
        return reservationRepository.getReservationsByDate(fixedDate)
    }

    fun getReservationsForMonth(month: String): Flow<List<ReservationEntity>> {
        Log.d("RESERVATION", "QUERY month = $month")
        return reservationRepository.getReservationsForMonth(month)
    }

    fun getReservationsBetweenDates(
        start: String,
        end: String
    ): Flow<List<ReservationEntity>> {
        val fixedStart = normalizeDate(start)
        val fixedEnd = normalizeDate(end)

        Log.d("RESERVATION", "QUERY range = $fixedStart ~ $fixedEnd")

        return reservationRepository.getReservationsBetweenDates(
            fixedStart,
            fixedEnd
        )
    }

    /* ────────────────────────────────
       예약 추가
    ──────────────────────────────── */

    fun addReservation(
        reservation: ReservationEntity,
        onFinished: () -> Unit = {}
    ) {
        viewModelScope.launch {

            val fixedDate = normalizeDate(reservation.date)

            val fixedReservation = reservation.copy(
                date = fixedDate
            )

            Log.d(
                "RESERVATION",
                "SAVE reservation → date=$fixedDate, time=${reservation.startTime}, routineId=${reservation.routineId}"
            )

            reservationRepository.addReservation(fixedReservation)

            // 알람 시간 계산
            val startMillis = convertToMillis(
                fixedReservation.date,
                fixedReservation.startTime
            )

            Log.d("RESERVATION", "ALARM startMillis = $startMillis")

            // 루틴 + 스텝 조회
            val routineWithSteps =
                reservationRepository.getRoutineWithSteps(fixedReservation.routineId)

            if (routineWithSteps != null) {
                RoutineAlarmScheduler.scheduleAll(
                    context = appContext,
                    routineId = fixedReservation.routineId,
                    routineStartMillis = startMillis,
                    steps = routineWithSteps.steps
                )
            } else {
                Log.e(
                    "RESERVATION",
                    "RoutineWithSteps not found for routineId=${fixedReservation.routineId}"
                )
            }

            onFinished()
        }
    }

    /* ────────────────────────────────
       예약 삭제
    ──────────────────────────────── */

    fun deleteReservation(
        reservationId: Long,
        onFinished: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val reservation =
                reservationRepository.getReservationById(reservationId)

            if (reservation != null) {
                Log.d(
                    "RESERVATION",
                    "DELETE reservationId=$reservationId, routineId=${reservation.routineId}"
                )

                RoutineAlarmScheduler.cancelAllForRoutine(
                    context = appContext,
                    routineId = reservation.routineId
                )
            }

            reservationRepository.deleteReservation(reservationId)
            onFinished()
        }
    }

    /* ────────────────────────────────
       시간 → millis 변환
    ──────────────────────────────── */

    fun convertToMillis(date: String, time: String): Long {
        val localDate = LocalDate.parse(date)
        val localTime = LocalTime.parse(time)

        return localDate
            .atTime(localTime)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}