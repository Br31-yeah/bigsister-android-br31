// app/src/main/java/com/smwu/bigsister/data/local/AppDatabase.kt
package com.smwu.bigsister.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smwu.bigsister.data.local.dao.CompletionDao
import com.smwu.bigsister.data.local.dao.ReservationDao
import com.smwu.bigsister.data.local.dao.RoutineDao
import com.smwu.bigsister.data.local.dao.StepDao
import com.smwu.bigsister.data.local.dao.UserDao

@Database(
    entities = [
        RoutineEntity::class,
        StepEntity::class,
        ReservationEntity::class,
        CompletionEntity::class,
        UserEntity::class
    ],
    version = 5,  // UserEntity 추가로 인한 버전 변경
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun routineDao(): RoutineDao
    abstract fun stepDao(): StepDao
    abstract fun reservationDao(): ReservationDao
    abstract fun completionDao(): CompletionDao
    abstract fun userDao(): UserDao
}