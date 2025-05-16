package com.example.cyber_knightsbridge.AiModels

// ApiService.kt
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class FingerprintProfile(
    val name: String,
    val age: Int,
    val photo: String,
    val fingerprint: String
)

interface ApiService {
    @Multipart
    @POST("/match-fingerprint/")
    suspend fun matchFingerprint(
        @Part file: MultipartBody.Part
    ): Response<FingerprintProfile>
}
