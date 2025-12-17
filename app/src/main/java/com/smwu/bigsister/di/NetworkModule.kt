package com.smwu.bigsister.di

import com.smwu.bigsister.data.network.GoogleDirectionsService
import com.smwu.bigsister.data.network.ODsayService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /* ───────── ODsay ───────── */

    @Provides
    @Singleton
    @Named("ODsay")
    fun provideODsayRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.odsay.com/v1/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideODsayService(
        @Named("ODsay") retrofit: Retrofit
    ): ODsayService {
        return retrofit.create(ODsayService::class.java)
    }

    /* ───────── Google Directions ───────── */

    @Provides
    @Singleton
    @Named("Google")
    fun provideGoogleRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGoogleDirectionsService(
        @Named("Google") retrofit: Retrofit
    ): GoogleDirectionsService {
        return retrofit.create(GoogleDirectionsService::class.java)
    }
}