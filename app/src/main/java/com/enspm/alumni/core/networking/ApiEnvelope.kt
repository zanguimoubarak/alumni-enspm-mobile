package com.enspm.alumni.core.networking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiEnvelope<T>(
    val type: ApiResponseType,
    val status: Int,
    val message: String,
    val data: T?,
)

enum class ApiResponseType {
    @Json(name = "success") Success,
    @Json(name = "error") Error,
    @Json(name = "validation_error") ValidationError,
}

data class ApiError(
    val message: String,
    val validationErrors: Map<String, String> = emptyMap(),
    val status: Int? = null,
)

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T, val message: String) : ApiResult<T>
    data class Failure(val error: ApiError) : ApiResult<Nothing>
}
