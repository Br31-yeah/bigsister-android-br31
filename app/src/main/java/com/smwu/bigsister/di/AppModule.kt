package com.smwu.bigsister.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.smwu.bigsister.data.local.AppDatabase
import com.smwu.bigsister.data.local.dao.RoutineDao
import com.smwu.bigsister.data.local.dao.StepDao
import com.smwu.bigsister.data.repository.ReservationRepository
import com.smwu.bigsister.data.repository.RoutineRepository
import com.smwu.bigsister.data.repository.StepRepository
import com.smwu.bigsister.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ---------------- Firebase 기본 객체 ----------------

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth =
        FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore =
        FirebaseFirestore.getInstance()

    // ---------------- Repository 제공 ----------------

    /**
     * 루틴 + 스텝 관리용 Repository
     * Room(RoutineDao, StepDao) + Firestore 백업 사용
     */
    @Provides
    @Singleton
    fun provideRoutineRepository(
        routineDao: RoutineDao,
        stepDao: StepDao,
        firestore: FirebaseFirestore
    ): RoutineRepository =
        RoutineRepository(
            routineDao = routineDao,
            stepDao = stepDao,
            firestore = firestore
        )

    /**
     * 스텝 관련 Repository
     * (생성자가 AppDatabase 하나만 받으므로 그대로 주입)
     */
    @Provides
    @Singleton
    fun provideStepRepository(
        db: AppDatabase
    ): StepRepository =
        StepRepository(db)

    /**
     * 예약(스케줄) 관련 Repository
     * (생성자가 AppDatabase 하나만 받으므로 그대로 주입)
     */
    @Provides
    @Singleton
    fun provideReservationRepository(
        db: AppDatabase
    ): ReservationRepository =
        ReservationRepository(db)

    /**
     * 로그인 / 사용자 세션 관리 Repository
     * (FirebaseAuth만 사용)
     */
    @Provides
    @Singleton
    fun provideUserRepository(
        auth: FirebaseAuth
    ): UserRepository =
        UserRepository(auth)
}