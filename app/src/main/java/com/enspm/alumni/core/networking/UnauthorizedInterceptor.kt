package com.enspm.alumni.core.networking

import com.enspm.alumni.core.session.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class UnauthorizedInterceptor @Inject constructor(
    private val sessionManager: SessionManager,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401 && chain.request().header(SKIP_401_LOGOUT_HEADER) == null) {
            runBlocking { sessionManager.clearSession(expired = true) }
        }
        return response
    }

    companion object {
        const val SKIP_401_LOGOUT_HEADER = "X-Skip-401-Logout"
    }
}
