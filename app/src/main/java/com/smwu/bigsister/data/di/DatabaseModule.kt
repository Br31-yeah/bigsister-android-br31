package com.smwu.bigsister.data.di

import android.content.Context
import androidx.room.Room
import com.smwu.bigsister.data.local.AppDatabase
import com.smwu.bigsister.data.local.dao.CompletionDao
import com.smwu.bigsister.data.local.dao.ReservationDao
import com.smwu.bigsister.data.local.dao.RoutineDao
import com.smwu.bigsister.data.local.dao.StepDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "big_sister_db"
        )
            .fallbackToDestructiveMigration() // 개발 단계에서는 편의상
            .build()

    @Provides
    fun provideRoutineDao(db: AppDatabase): RoutineDao = db.routineDao()

    @Provides
    fun provideStepDao(db: AppDatabase): StepDao = db.stepDao()

    @Provides
    fun provideReservationDao(db: AppDatabase): ReservationDao = db.reservationDao()

    @Provides
    fun provideCompletionDao(db: AppDatabase): CompletionDao = db.completionDao()
}