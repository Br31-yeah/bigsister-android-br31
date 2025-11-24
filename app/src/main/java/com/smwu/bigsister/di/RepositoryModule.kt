package com.smwu.bigsister.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.google.firebase.auth.FirebaseAuth
import com.smwu.bigsister.data.local.dao.*
import com.smwu.bigsister.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideRoutineRepository(
        routineDao: RoutineDao,
        stepDao: StepDao
    ): RoutineRepository = RoutineRepository(routineDao, stepDao)

    @Provides
    @Singleton
    fun provideReservationRepository(
        reservationDao: ReservationDao,
        routineDao: RoutineDao
    ): ReservationRepository = ReservationRepository(reservationDao, routineDao)

    @Provides
    @Singleton
    fun provideCompletionRepository(
        completionDao: CompletionDao
    ): CompletionRepository = CompletionRepository(completionDao)

    @Provides
    @Singleton
    fun provideUserRepository(
        auth: FirebaseAuth,
        userDao: UserDao
    ): UserRepository = UserRepository(auth, userDao)

    @Provides
    @Singleton
    fun provideSettingsRepository(
        dataStore: DataStore<Preferences>
    ): SettingsRepository = SettingsRepository(dataStore)
}