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
class LoginViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, fieldErrors = it.fieldErrors - "email") }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, fieldErrors = it.fieldErrors - "password") }

    fun login() = viewModelScope.launch {
        val state = _uiState.value
        _uiState.update { it.copy(loading = true, message = null, fieldErrors = emptyMap()) }
        when (val result = authRepository.login(state.email.trim(), state.password)) {
            is ApiResult.Success -> _uiState.update { it.copy(loading = false, message = result.message) }
            is ApiResult.Failure -> _uiState.update { it.copy(loading = false, message = result.error.message, fieldErrors = result.error.validationErrors) }
        }
    }
}
