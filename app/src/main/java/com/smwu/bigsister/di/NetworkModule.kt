package com.smwu.bigsister.di

import com.smwu.bigsister.data.remote.NaverMapApi
import com.smwu.bigsister.data.remote.RetrofitClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return RetrofitClient.retrofit
    }

    @Provides
    @Singleton
    fun provideNaverMapApi(retrofit: Retrofit): NaverMapApi {
        return retrofit.create(NaverMapApi::class.java)
    }
}