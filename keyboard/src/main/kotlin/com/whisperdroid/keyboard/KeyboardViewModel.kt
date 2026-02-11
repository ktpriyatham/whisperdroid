package com.whisperdroid.keyboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class KeyboardViewModel {
    var shiftState by mutableStateOf(ShiftState.NONE)
    var keyboardMode by mutableStateOf(KeyboardMode.ALPHA)
    var voiceState by mutableStateOf(VoiceState.IDLE)

    fun handleShift() {
        shiftState = when (shiftState) {
            ShiftState.NONE -> ShiftState.SHIFTED
            ShiftState.SHIFTED -> ShiftState.CAPS_LOCK
            ShiftState.CAPS_LOCK -> ShiftState.NONE
        }
    }

}
