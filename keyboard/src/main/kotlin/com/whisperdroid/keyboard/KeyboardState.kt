package com.whisperdroid.keyboard

enum class ShiftState {
    NONE,
    SHIFTED,
    CAPS_LOCK
}

enum class KeyboardMode {
    ALPHA,
    NUMERIC,
    SYMBOLS
}

enum class VoiceState {
    IDLE,
    RECORDING,
    PROCESSING,
    SUCCESS
}
