package com.smwu.bigsister.di

// --- ▼ 2줄 Import 추가 ▼ ---
// --- ▲ 2줄 Import 추가 ▲ ---
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.smwu.bigsister.data.local.dao.CompletionDao
import com.smwu.bigsister.data.local.dao.ReservationDao
import com.smwu.bigsister.data.local.dao.RoutineDao
import com.smwu.bigsister.data.local.dao.StepDao
import com.smwu.bigsister.data.repository.CompletionRepository
import com.smwu.bigsister.data.repository.ReservationRepository
import com.smwu.bigsister.data.repository.RoutineRepository
import com.smwu.bigsister.data.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    // ... (provideRoutineRepository, provideReservationRepository, provideCompletionRepository는 동일) ...
    @Provides
    @Singleton
    fun provideRoutineRepository(
        routineDao: RoutineDao,
        stepDao: StepDao
    ): RoutineRepository {
        return RoutineRepository(routineDao, stepDao)
    }

    @Provides
    @Singleton
    fun provideReservationRepository(
        reservationDao: ReservationDao,
        routineDao: RoutineDao
    ): ReservationRepository {
        return ReservationRepository(reservationDao, routineDao)
    }

    @Provides
    @Singleton
    fun provideCompletionRepository(
        completionDao: CompletionDao
    ): CompletionRepository {
        return CompletionRepository(completionDao)
    }

    // --- ▼ 이 함수를 수정합니다 (Context -> DataStore) ▼ ---
    @Provides
    @Singleton
    fun provideSettingsRepository(
        dataStore: DataStore<Preferences> // [수정됨] Context 대신 DataStore 주입
    ): SettingsRepository {
        return SettingsRepository(dataStore) // [수정됨] 주입받은 dataStore 전달
    }
}