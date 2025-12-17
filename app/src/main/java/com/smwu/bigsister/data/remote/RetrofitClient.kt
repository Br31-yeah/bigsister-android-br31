package com.smwu.bigsister.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 네이버 지도 API (Directions 5)의 기본 URL
    private const val BASE_URL = "https://naveropenapi.apigw.ntruss.com/"

    // TODO: 네이버 클라우드 플랫폼에서 발급받은 API 키를 여기에 입력하세요.
    // 경고: 이 키는 절대 Git에 커밋하면 안 됩니다! (build.gradle에서 숨겨야 함)
    private const val NAVER_CLIENT_ID = "YOUR_NAVER_CLIENT_ID"
    private const val NAVER_CLIENT_SECRET = "YOUR_NAVER_CLIENT_SECRET"

    /**
     * 모든 요청에 네이버 API 인증 헤더를 자동으로 추가하는 Interceptor
     */
    private class NaverAuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder()
                .addHeader("X-NCP-APIGW-API-KEY-ID", NAVER_CLIENT_ID)
                .addHeader("X-NCP-APIGW-API-KEY", NAVER_CLIENT_SECRET)
                .build()
            return chain.proceed(request)
        }
    }

    // Interceptor를 탑재한 OkHttpClient 생성
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(NaverAuthInterceptor())
        .build()

    // Retrofit 인스턴스 생성
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient) // 인증 헤더를 포함한 클라이언트 사용
        .addConverterFactory(GsonConverterFactory.create()) // JSON 변환기
        .build()

    // ===============================
    // ODsay API
    // ===============================
    private const val ODSAY_BASE_URL = "https://api.odsay.com/v1/api/"

    val odsayRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(ODSAY_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // ===============================
    // Google Directions API
    // ===============================
    private const val GOOGLE_BASE_URL = "https://maps.googleapis.com/"

    val googleRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(GOOGLE_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}