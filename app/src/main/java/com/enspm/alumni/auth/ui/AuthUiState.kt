package com.enspm.alumni.auth.ui

import com.enspm.alumni.core.networking.ApiError

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val message: String? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
)

data class PasswordResetUiState(
    val email: String = "",
    val otp: String = "",
    val password: String = "",
    val confirmation: String = "",
    val loading: Boolean = false,
    val message: String? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
    val step: PasswordResetStep = PasswordResetStep.Email,
)

enum class PasswordResetStep { Email, Otp, Reset, Done }

fun ApiError.fieldOrMessage(field: String): String? = validationErrors[field]
