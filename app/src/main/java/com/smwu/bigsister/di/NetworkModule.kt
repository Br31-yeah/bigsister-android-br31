package com.smwu.bigsister.di

import com.smwu.bigsister.data.network.GoogleDirectionsService
import com.smwu.bigsister.data.network.GoogleRoutesService
import com.smwu.bigsister.data.network.ODsayService
import com.smwu.bigsister.data.remote.RetrofitClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /* ───────── ODsay ───────── */

    @Provides
    @Singleton
    fun provideODsayService(): ODsayService {
        return RetrofitClient.odsayRetrofit
            .create(ODsayService::class.java)
    }

    /* ───────── Google Directions ───────── */

    @Provides
    @Singleton
    fun provideGoogleDirectionsService(): GoogleDirectionsService {
        return RetrofitClient.googleRetrofit
            .create(GoogleDirectionsService::class.java)
    }

    /* ───────── Google Routes (NEW) ───────── */

    @Provides
    @Singleton
    fun provideGoogleRoutesService(): GoogleRoutesService =
        RetrofitClient.googleRoutesRetrofit.create(GoogleRoutesService::class.java)
}
