package com.whisperdroid.api

import com.whisperdroid.api.models.ClaudeRequest
import com.whisperdroid.api.models.ClaudeResponse
import com.whisperdroid.api.models.WhisperResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

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
