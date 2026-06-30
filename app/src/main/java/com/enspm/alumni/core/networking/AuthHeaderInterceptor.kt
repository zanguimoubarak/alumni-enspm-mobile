package com.enspm.alumni.core.networking

import com.enspm.alumni.core.datastore.SessionDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthHeaderInterceptor @Inject constructor(
    private val sessionDataStore: SessionDataStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { sessionDataStore.tokenFlow.first() }
        val requestBuilder = chain.request().newBuilder()
            .header("Accept", "application/json")
        if (!token.isNullOrBlank()) requestBuilder.header("Authorization", "Bearer $token")
        return chain.proceed(requestBuilder.build())
    }
}
