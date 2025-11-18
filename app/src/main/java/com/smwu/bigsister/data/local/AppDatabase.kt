package com.smwu.bigsister.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smwu.bigsister.data.local.dao.CompletionDao
import com.smwu.bigsister.data.local.dao.ReservationDao
import com.smwu.bigsister.data.local.dao.RoutineDao
import com.smwu.bigsister.data.local.dao.StepDao

/**
 * Room 데이터베이스의 메인 설정 파일입니다.
 * [수정됨] 우리가 만든 4개의 Entity와 4개의 Dao를 등록합니다.
 */
@Database(
    entities = [
        RoutineEntity::class,
        StepEntity::class,
        ReservationEntity::class,
        CompletionEntity::class
    ],
    version = 1, // DB 스키마 변경 시 이 버전을 올려야 합니다.
    exportSchema = false // 스키마 백업은 일단 비활성화
)
abstract class AppDatabase : RoomDatabase() {

    // Hilt가 이 DAO들을 자동으로 주입할 수 있도록 추상 함수를 선언합니다.
    abstract fun routineDao(): RoutineDao
    abstract fun stepDao(): StepDao
    abstract fun reservationDao(): ReservationDao
    abstract fun completionDao(): CompletionDao

    // (참고: Reservation/Completion의 날짜/시간을 String과 Long으로 처리했기 때문에
    // 별도의 TypeConverter가 필요 없습니다.)
}