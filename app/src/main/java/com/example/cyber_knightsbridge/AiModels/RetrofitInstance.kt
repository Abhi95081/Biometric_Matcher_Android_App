// RetrofitInstance.kt
package com.example.cyber_knightsbridge.AiModels

import android.os.Build
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private const val TAG = "RetrofitInstance"
    private const val SERVER_IP = "192.168.0.100"  // Your laptop/server IP
    private const val PORT = "8000"
    private val BASE_URL = "http://$SERVER_IP:$PORT/"

    private val ADB_REVERSE_URL = "http://127.0.0.1:$PORT/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                Build.PRODUCT == "google_sdk")
    }

    // You can toggle this flag manually or implement logic to detect adb reverse use
    private const val USE_ADB_REVERSE = false

    val currentBaseUrl: String
        get() = when {
            isEmulator() -> "http://10.0.2.2:$PORT/"
            USE_ADB_REVERSE -> ADB_REVERSE_URL
            else -> BASE_URL
        }

    val api: ApiService by lazy {
        Log.d(TAG, "Using base URL: $currentBaseUrl")

        Retrofit.Builder()
            .baseUrl(currentBaseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
