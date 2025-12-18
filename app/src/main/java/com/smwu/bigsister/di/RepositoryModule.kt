package com.smwu.bigsister.di

import com.smwu.bigsister.data.repository.MapRepository
import com.smwu.bigsister.data.repository.MapRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindMapRepository(
        impl: MapRepositoryImpl
    ): MapRepository
}