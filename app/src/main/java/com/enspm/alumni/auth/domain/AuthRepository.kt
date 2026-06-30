package com.enspm.alumni.auth.domain

import com.enspm.alumni.auth.data.*
import com.enspm.alumni.core.datastore.SessionDataStore
import com.enspm.alumni.core.networking.ApiResult
import com.enspm.alumni.core.networking.safeApiCall
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val sessionDataStore: SessionDataStore,
    private val moshi: Moshi,
) {
    suspend fun login(email: String, password: String): ApiResult<LoginDataDto> =
        when (val result = safeApiCall(moshi) { authApi.login(LoginRequest(email, password)) }) {
            is ApiResult.Success -> {
                sessionDataStore.saveToken(result.data.token)
                result
            }
            is ApiResult.Failure -> result
        }

    suspend fun currentUser() = safeApiCall(moshi) { authApi.currentUser() }

    suspend fun logout() {
        runCatching { authApi.logout() }
        sessionDataStore.clearSession()
    }

    suspend fun forgotPassword(email: String) = safeApiCall(moshi) { authApi.forgotPassword(ForgotPasswordRequest(email)) }
    suspend fun verifyOtp(email: String, otp: String) = safeApiCall(moshi) { authApi.verifyOtp(VerifyOtpRequest(email, otp)) }
    suspend fun resendOtp(email: String) = safeApiCall(moshi) { authApi.resendOtp(ResendOtpRequest(email)) }
    suspend fun resetPassword(email: String, otp: String, password: String, confirmation: String) =
        safeApiCall(moshi) { authApi.resetPassword(ResetPasswordRequest(email, otp, password, confirmation)) }
}
