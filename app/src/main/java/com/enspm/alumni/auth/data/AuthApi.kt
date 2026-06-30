package com.enspm.alumni.auth.data

import com.enspm.alumni.core.networking.ApiEnvelope
import com.enspm.alumni.core.networking.UnauthorizedInterceptor
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApi {
    @Headers("X-Skip-401-Logout: true")
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<ApiEnvelope<LoginDataDto>>

    @POST("logout")
    suspend fun logout(): Response<ApiEnvelope<MessageDataDto>>

    @GET("user")
    suspend fun currentUser(): Response<ApiEnvelope<AuthUserDto>>

    @Headers("X-Skip-401-Logout: true")
    @POST("forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiEnvelope<MessageDataDto>>

    @Headers("X-Skip-401-Logout: true")
    @POST("verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<ApiEnvelope<MessageDataDto>>

    @Headers("X-Skip-401-Logout: true")
    @POST("resend-otp")
    suspend fun resendOtp(@Body request: ResendOtpRequest): Response<ApiEnvelope<MessageDataDto>>

    @Headers("X-Skip-401-Logout: true")
    @POST("reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiEnvelope<MessageDataDto>>
}
