package com.enspm.alumni.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enspm.alumni.auth.domain.AuthRepository
import com.enspm.alumni.core.networking.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordResetViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PasswordResetUiState())
    val uiState: StateFlow<PasswordResetUiState> = _uiState.asStateFlow()

    fun setEmail(value: String) = _uiState.update { it.copy(email = value, fieldErrors = it.fieldErrors - "email") }
    fun setOtp(value: String) = _uiState.update { it.copy(otp = value, fieldErrors = it.fieldErrors - "otp") }
    fun setPassword(value: String) = _uiState.update { it.copy(password = value, fieldErrors = it.fieldErrors - "password") }
    fun setConfirmation(value: String) = _uiState.update { it.copy(confirmation = value, fieldErrors = it.fieldErrors - "password_confirmation") }

    fun forgotPassword() = call(next = PasswordResetStep.Otp) { authRepository.forgotPassword(_uiState.value.email.trim()) }
    fun verifyOtp() = call(next = PasswordResetStep.Reset) { authRepository.verifyOtp(_uiState.value.email.trim(), _uiState.value.otp.trim()) }
    fun resendOtp() = call(next = PasswordResetStep.Otp) { authRepository.resendOtp(_uiState.value.email.trim()) }
    fun resetPassword() = call(next = PasswordResetStep.Done) {
        val s = _uiState.value
        authRepository.resetPassword(s.email.trim(), s.otp.trim(), s.password, s.confirmation)
    }

    private fun call(next: PasswordResetStep, block: suspend () -> ApiResult<*>) = viewModelScope.launch {
        _uiState.update { it.copy(loading = true, message = null, fieldErrors = emptyMap()) }
        when (val result = block()) {
            is ApiResult.Success -> _uiState.update { it.copy(loading = false, message = result.message, step = next) }
            is ApiResult.Failure -> _uiState.update { it.copy(loading = false, message = result.error.message, fieldErrors = result.error.validationErrors) }
        }
    }
}
