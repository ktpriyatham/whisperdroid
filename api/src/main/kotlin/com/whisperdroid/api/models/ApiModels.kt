package com.whisperdroid.api.models

data class WhisperResponse(
    val text: String
)

data class ClaudeRequest(
    val model: String,
    val system: String,
    val messages: List<Message>,
    val max_tokens: Int
)

data class Message(
    val role: String,
    val content: String
)

data class ClaudeResponse(
    val content: List<Content>
)

data class Content(
    val text: String,
    val type: String
)
