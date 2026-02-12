package com.whisperdroid.core

object Constants {
    const val PREFS_NAME = "whisperdroid_prefs"
    const val KEY_OPENAI_API_KEY = "openai_api_key"
    const val KEY_CLAUDE_API_KEY = "claude_api_key"

    const val KEY_REFINEMENT_ENABLED = "refinement_enabled"
    const val KEY_CLIPBOARD_OUTPUT = "clipboard_output"
    const val KEY_HAPTICS_ENABLED = "haptics_enabled"
    const val KEY_SYSTEM_PROMPT = "system_prompt"
    const val KEY_CLAUDE_MODEL = "claude_model"

    const val DEFAULT_SYSTEM_PROMPT = "You are a helpful keyboard assistant. Your task is to take raw voice-to-text transcriptions and clean them up. Add proper punctuation, correct obvious grammatical errors, and fix spelling. Do not add any conversational filler or meta-comments. Return ONLY the cleaned-up text."
    const val DEFAULT_CLAUDE_MODEL = "claude-3-haiku-20240307"
}
