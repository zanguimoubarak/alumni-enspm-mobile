package com.enspm.alumni.core.networking

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import retrofit2.Response
import java.io.IOException

suspend fun <T : Any> safeApiCall(
    moshi: Moshi,
    defaultData: T? = null,
    call: suspend () -> Response<ApiEnvelope<T>>,
): ApiResult<T> {
    return try {
        val response = call()
        val envelope = response.body()
        if (response.isSuccessful && envelope?.type == ApiResponseType.Success && (envelope.data != null || defaultData != null)) {
            ApiResult.Success(envelope.data ?: defaultData!!, envelope.message)
        } else {
            val parsed = envelope?.toApiError() ?: parseErrorBody(moshi, response)
            ApiResult.Failure(parsed.copy(status = parsed.status ?: response.code()))
        }
    } catch (_: IOException) {
        ApiResult.Failure(ApiError("Connexion indisponible. Vérifiez votre réseau."))
    } catch (_: Exception) {
        ApiResult.Failure(ApiError("Une erreur inattendue est survenue."))
    }
}

private fun <T> ApiEnvelope<T>.toApiError(): ApiError {
    val errors = if (type == ApiResponseType.ValidationError) validationMapFrom(data) else emptyMap()
    return ApiError(message = message, validationErrors = errors, status = status)
}

private fun parseErrorBody(moshi: Moshi, response: Response<*>): ApiError {
    val raw = response.errorBody()?.string().orEmpty()
    if (raw.isBlank()) return ApiError(response.message().ifBlank { "Erreur serveur." }, status = response.code())
    return runCatching {
        val typeRef = Types.newParameterizedType(ApiEnvelope::class.java, Map::class.java)
        val adapter = moshi.adapter<ApiEnvelope<Map<String, Any?>>>(typeRef)
        val envelope = adapter.fromJson(raw)
        ApiError(
            message = envelope?.message ?: "Erreur serveur.",
            validationErrors = if (envelope?.type == ApiResponseType.ValidationError) validationMapFrom(envelope.data) else emptyMap(),
            status = envelope?.status ?: response.code(),
        )
    }.getOrElse { ApiError("Erreur serveur.", status = response.code()) }
}

@Suppress("UNCHECKED_CAST")
private fun validationMapFrom(data: Any?): Map<String, String> {
    return (data as? Map<*, *>)?.mapNotNull { (key, value) ->
        val name = key as? String ?: return@mapNotNull null
        val message = when (value) {
            is String -> value
            is List<*> -> value.firstOrNull() as? String
            else -> value?.toString()
        } ?: return@mapNotNull null
        name to message
    }?.toMap().orEmpty()
}
