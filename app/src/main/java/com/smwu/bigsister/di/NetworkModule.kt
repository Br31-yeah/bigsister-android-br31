package com.smwu.bigsister.di

import com.smwu.bigsister.data.network.ODsayService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // ODsay 기본 주소
    private const val BASE_URL = "https://api.odsay.com/v1/api/"

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideODsayService(retrofit: Retrofit): ODsayService {
        return retrofit.create(ODsayService::class.java)
    }
}