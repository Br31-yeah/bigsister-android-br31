package com.smwu.bigsister.di

import android.content.Context
import androidx.room.Room
import com.smwu.bigsister.data.local.AppDatabase
import com.smwu.bigsister.data.local.dao.*
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
        ).build()

    @Provides fun provideRoutineDao(db: AppDatabase): RoutineDao = db.routineDao()
    @Provides fun provideStepDao(db: AppDatabase): StepDao = db.stepDao()
    @Provides fun provideReservationDao(db: AppDatabase): ReservationDao = db.reservationDao()
    @Provides fun provideCompletionDao(db: AppDatabase): CompletionDao = db.completionDao()
}