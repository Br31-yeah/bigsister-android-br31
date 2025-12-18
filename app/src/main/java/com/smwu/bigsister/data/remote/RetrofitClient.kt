package com.smwu.bigsister.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    /* ────────────────────────────────
       Logging Interceptor (중요)
    ──────────────────────────────── */

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /* ────────────────────────────────
       Naver Map API (기존 유지)
    ──────────────────────────────── */

    private const val NAVER_BASE_URL = "https://naveropenapi.apigw.ntruss.com/"
    private const val NAVER_CLIENT_ID = "YOUR_NAVER_CLIENT_ID"
    private const val NAVER_CLIENT_SECRET = "YOUR_NAVER_CLIENT_SECRET"

    private class NaverAuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder()
                .addHeader("X-NCP-APIGW-API-KEY-ID", NAVER_CLIENT_ID)
                .addHeader("X-NCP-APIGW-API-KEY", NAVER_CLIENT_SECRET)
                .build()
            return chain.proceed(request)
        }
    }

    private val naverOkHttpClient = OkHttpClient.Builder()
        .addInterceptor(NaverAuthInterceptor())
        .addInterceptor(loggingInterceptor)
        .build()

    val naverRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(NAVER_BASE_URL)
        .client(naverOkHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /* ────────────────────────────────
       ODsay API (🔥 핵심)
    ──────────────────────────────── */

    private const val ODSAY_BASE_URL = "https://api.odsay.com/v1/api/"

    private val odsayOkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // ⭐ 반드시 필요
        .build()

    val odsayRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(ODSAY_BASE_URL)
        .client(odsayOkHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /* ────────────────────────────────
       Google Directions API
    ──────────────────────────────── */

    private const val GOOGLE_BASE_URL = "https://maps.googleapis.com/"

    private val googleOkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val googleRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(GOOGLE_BASE_URL)
        .client(googleOkHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    /* ────────────────────────────────
   Google Routes API (NEW)
──────────────────────────────── */

    private const val GOOGLE_ROUTES_BASE_URL = "https://routes.googleapis.com/"

    private val googleRoutesOkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val googleRoutesRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(GOOGLE_ROUTES_BASE_URL)
        .client(googleRoutesOkHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}