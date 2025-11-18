package com.smwu.bigsister.di

// --- ▼ DataStore 관련 import 3줄 삭제 ▼ ---
// import androidx.datastore.core.DataStore
// import androidx.datastore.preferences.core.Preferences
// import androidx.datastore.preferences.preferencesDataStore
// --- ▲ DataStore 관련 import 3줄 삭제 ▲ ---
import android.content.Context
import androidx.room.Room
import com.smwu.bigsister.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// --- ▼ DataStore 인스턴스 정의 삭제 ▼ ---
// private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // --- ▼ DataStore 제공자 (Provides) 함수 삭제 ▼ ---
    // @Provides
    // @Singleton
    // fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
    //     return context.dataStore
    // }
    // --- ▲ DataStore 제공자 (Provides) 함수 삭제 ▲ ---


    // --- (이하 기존 Room DB 관련 코드는 동일) ---
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "bigsister_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideRoutineDao(db: AppDatabase) = db.routineDao()

    @Provides
    @Singleton
    fun provideStepDao(db: AppDatabase) = db.stepDao()

    @Provides
    @Singleton
    fun provideReservationDao(db: AppDatabase) = db.reservationDao()

    @Provides
    @Singleton
    fun provideCompletionDao(db: AppDatabase) = db.completionDao()
}