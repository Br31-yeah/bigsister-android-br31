package com.smwu.bigsister.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_table")
data class StepEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val routineId: Long,

    /** 단계 이름 (예: 세면, 이동, 준비 등) */
    val name: String,

    /** 기준 소요 시간 (분)
     * - 일반 step: 사용자가 입력한 시간
     * - 이동 step: 최초 계산된 이동 시간
     */
    val baseDuration: Long,

    /** 순서 */
    val orderIndex: Int = 0,

    /* ───────── 이동 step 전용 필드 ───────── */

    /** 이동 단계 여부 */
    val isTransport: Boolean = false,

    /** 출발지 (stationName|x,y) */
    val from: String? = null,

    /** 도착지 (stationName|x,y) */
    val to: String? = null,

    /** 이동 수단 (walking / transit / driving) */
    val transportMode: String? = null,

    /** 기준 출발 시간 (HH:mm)
     * 이동 step 생성 시 확정
     */
    val baseDepartureTime: String? = null,

    /** 최근 교통 정보 기준 이동 시간 (분)
     * 새로고침 버튼 눌렀을 때만 갱신
     */
    val calculatedDuration: Long? = null,

    /** 메모 */
    val memo: String? = null
)