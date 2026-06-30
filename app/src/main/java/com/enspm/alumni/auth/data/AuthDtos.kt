package com.enspm.alumni.auth.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true) data class LoginRequest(val email: String, val password: String)
@JsonClass(generateAdapter = true) data class ForgotPasswordRequest(val email: String)
@JsonClass(generateAdapter = true) data class VerifyOtpRequest(val email: String, val otp: String)
@JsonClass(generateAdapter = true) data class ResendOtpRequest(val email: String)
@JsonClass(generateAdapter = true) data class ResetPasswordRequest(val email: String, val otp: String, val password: String, @Json(name = "password_confirmation") val passwordConfirmation: String)

@JsonClass(generateAdapter = true)
data class AuthUserDto(
    val id: Long? = null,
    val name: String? = null,
    val email: String? = null,
)

@JsonClass(generateAdapter = true)
data class LoginDataDto(
    val user: AuthUserDto? = null,
    val token: String,
)

@JsonClass(generateAdapter = true)
data class MessageDataDto(val acknowledged: Boolean? = null)
