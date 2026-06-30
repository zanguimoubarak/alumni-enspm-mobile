package com.enspm.alumni.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enspm.alumni.auth.domain.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShellViewModel @Inject constructor(
    val authRepository: AuthRepository,
) : ViewModel() {
    fun logout() = viewModelScope.launch { authRepository.logout() }
}
