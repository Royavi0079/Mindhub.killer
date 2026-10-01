package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// --- Request Models ---

@JsonClass(generateAdapter = true)
data class SignupRequest(
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class LogoutRequest(
    @Json(name = "token") val token: String
)

@JsonClass(generateAdapter = true)
data class ForgotPasswordRequest(
    @Json(name = "email") val email: String
)

@JsonClass(generateAdapter = true)
data class ResetPasswordRequest(
    @Json(name = "token") val token: String,
    @Json(name = "new_password") val newPassword: String
)

@JsonClass(generateAdapter = true)
data class RegisterDeviceRequest(
    @Json(name = "user_id") val userId: String,
    @Json(name = "device_token") val deviceToken: String
)

// --- Response Models ---

@JsonClass(generateAdapter = true)
data class SimpleAuthResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String?,
    @Json(name = "error") val error: String? = null
)

@JsonClass(generateAdapter = true)
data class LoginData(
    @Json(name = "user_id") val userId: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "email") val email: String?,
    @Json(name = "token") val token: String?
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String?,
    @Json(name = "error") val error: String? = null,
    @Json(name = "data") val data: LoginData? = null
)

// --- Retrofit Service ---

interface AuthApiService {

    @Headers("Content-Type: application/json", "User-Agent: MindMatrix-Android/1.0")
    @POST("signup")
    suspend fun signup(@Body request: SignupRequest): Response<SimpleAuthResponse>

    @Headers("Content-Type: application/json", "User-Agent: MindMatrix-Android/1.0")
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @Headers("Content-Type: application/json", "User-Agent: MindMatrix-Android/1.0")
    @POST("logout")
    suspend fun logout(@Body request: LogoutRequest): Response<SimpleAuthResponse>

    @Headers("Content-Type: application/json", "User-Agent: MindMatrix-Android/1.0")
    @POST("forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<SimpleAuthResponse>

    @Headers("Content-Type: application/json", "User-Agent: MindMatrix-Android/1.0")
    @POST("reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<SimpleAuthResponse>

    @Headers("Content-Type: application/json", "User-Agent: MindMatrix-Android/1.0")
    @POST("register-device")
    suspend fun registerDevice(@Body request: RegisterDeviceRequest): Response<SimpleAuthResponse>

    companion object {
        private const val BASE_URL = "https://mypuzzlehub.royabhay0079.workers.dev/"

        private val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val instance: AuthApiService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(AuthApiService::class.java)
        }
    }
}
