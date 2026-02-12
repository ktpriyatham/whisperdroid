package com.whisperdroid.api

import com.whisperdroid.api.models.ClaudeRequest
import com.whisperdroid.api.models.ClaudeResponse
import com.whisperdroid.api.models.Message
import com.whisperdroid.api.models.WhisperResponse
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

interface WhisperService {
    @Multipart
    @POST("v1/audio/transcriptions")
    suspend fun transcribe(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody
    ): WhisperResponse
}

interface ClaudeService {
    @POST("v1/messages")
    suspend fun refineText(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Body request: ClaudeRequest
    ): ClaudeResponse
}

object WhisperApiClient {
    private val client by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val service by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WhisperService::class.java)
    }

    suspend fun transcribe(apiKey: String, audioFile: File): String = safeApiCall {
        val requestFile = audioFile.asRequestBody("audio/m4a".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", audioFile.name, requestFile)
        val model = "whisper-1".toRequestBody("text/plain".toMediaTypeOrNull())

        val response = service.transcribe("Bearer $apiKey", body, model)
        response.text
    }
}

object ClaudeApiClient {
    private val client by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val service by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.anthropic.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClaudeService::class.java)
    }

    suspend fun cleanUp(apiKey: String, text: String): String = safeApiCall {
        val request = ClaudeRequest(
            model = "claude-3-haiku-20240307",
            max_tokens = 1024,
            system = "You are a helpful keyboard assistant. Your task is to take raw voice-to-text transcriptions and clean them up. Add proper punctuation, correct obvious grammatical errors, and fix spelling. Do not add any conversational filler or meta-comments. Return ONLY the cleaned-up text.",
            messages = listOf(Message(role = "user", content = text))
        )

        val response = service.refineText(apiKey = apiKey, request = request)
        response.content.firstOrNull { it.type == "text" }?.text ?: ""
    }
}

private suspend fun <T> safeApiCall(block: suspend () -> T): T {
    var lastException: Exception? = null
    val delays = listOf(1000L, 2000L, 4000L)
    val maxAttempts = 4 // 1 initial + 3 retries = 4 attempts

    for (attempt in 0 until maxAttempts) {
        try {
            return block()
        } catch (e: Exception) {
            lastException = e
            if (shouldRetry(e) && attempt < maxAttempts - 1) {
                delay(delays[attempt])
                continue
            }
            throw translateException(e)
        }
    }
    throw translateException(lastException!!)
}

private fun shouldRetry(e: Exception): Boolean {
    return e is IOException || (e is HttpException && e.code() >= 500)
}

private fun translateException(e: Exception): Exception {
    return when (e) {
        is HttpException -> {
            when (e.code()) {
                401 -> Exception("Invalid API key")
                429 -> Exception("Rate limited, try again")
                in 500..599 -> Exception("Server error, retrying...")
                else -> e
            }
        }
        else -> e
    }
}
