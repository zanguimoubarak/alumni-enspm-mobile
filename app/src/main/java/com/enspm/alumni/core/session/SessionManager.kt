package com.enspm.alumni.core.session

import com.enspm.alumni.core.datastore.SessionDataStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val sessionDataStore: SessionDataStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _sessionExpiredEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpiredEvents: SharedFlow<Unit> = _sessionExpiredEvents

    val sessionState: StateFlow<SessionState> = sessionDataStore.tokenFlow
        .map { token -> if (token.isNullOrBlank()) SessionState.Unauthenticated else SessionState.Authenticated }
        .stateIn(scope, SharingStarted.Eagerly, SessionState.Loading)

    suspend fun clearSession(expired: Boolean = false) {
        sessionDataStore.clearSession()
        if (expired) _sessionExpiredEvents.tryEmit(Unit)
    }
}
