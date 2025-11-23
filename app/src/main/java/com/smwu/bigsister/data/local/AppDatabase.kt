// app/src/main/java/com/smwu/bigsister/data/local/AppDatabase.kt
package com.smwu.bigsister.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smwu.bigsister.data.local.dao.*

@Database(
    entities = [
        RoutineEntity::class,
        StepEntity::class,
        ReservationEntity::class,
        CompletionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun routineDao(): RoutineDao
    abstract fun stepDao(): StepDao
    abstract fun reservationDao(): ReservationDao
    abstract fun completionDao(): CompletionDao
}