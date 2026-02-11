package com.whisperdroid.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.material3.MaterialTheme
import kotlinx.coroutines.delay
import androidx.compose.material3.Surface
import com.whisperdroid.keyboard.ui.KeyboardAction
import com.whisperdroid.keyboard.ui.KeyboardLayout
import android.view.KeyEvent

class WhisperDroidInputMethodService : InputMethodService() {

    private val viewModel = KeyboardViewModel()

    override fun onCreateInputView(): View {
        val frameLayout = FrameLayout(this)
        val composeView = ComposeView(this).apply {
            setContent {
                WhisperDroidKeyboardTheme {
                    KeyboardScreen(viewModel)
                }
            }
        }
        frameLayout.addView(composeView)
        return frameLayout
    }

    @Composable
    fun KeyboardScreen(kvm: KeyboardViewModel) {
        LaunchedEffect(kvm.voiceState) {
            if (kvm.voiceState == VoiceState.SUCCESS) {
                delay(800)
                kvm.voiceState = VoiceState.IDLE
            }
        }

        Surface {
            KeyboardLayout(
                shiftState = kvm.shiftState,
                keyboardMode = kvm.keyboardMode,
                voiceState = kvm.voiceState,
                onKeyClick = { text ->
                    currentInputConnection?.commitText(text, 1)
                    if (kvm.shiftState == ShiftState.SHIFTED) {
                        kvm.shiftState = ShiftState.NONE
                    }
                },
                onActionClick = { action ->
                    handleAction(action, kvm)
                },
                onVoiceStart = {
                    kvm.voiceState = VoiceState.RECORDING
                    // TODO: Trigger actual recording
                },
                onVoiceStop = {
                    kvm.voiceState = VoiceState.PROCESSING
                    // TODO: Stop recording and start processing
                }
            )
        }
    }

    private fun handleAction(action: KeyboardAction, kvm: KeyboardViewModel) {
        when (action) {
            KeyboardAction.SHIFT -> kvm.handleShift()
            KeyboardAction.BACKSPACE -> {
                val selectedText = currentInputConnection?.getSelectedText(0)
                if (selectedText.isNullOrEmpty()) {
                    currentInputConnection?.deleteSurroundingText(1, 0)
                } else {
                    currentInputConnection?.commitText("", 1)
                }
            }
            KeyboardAction.ENTER -> {
                val editorInfo = currentInputEditorInfo ?: return
                val actionId = editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION
                if (actionId != EditorInfo.IME_ACTION_NONE && actionId != EditorInfo.IME_ACTION_UNSPECIFIED) {
                    currentInputConnection?.performEditorAction(actionId)
                } else {
                    currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                    currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                }
            }
            KeyboardAction.SPACE -> {
                currentInputConnection?.commitText(" ", 1)
            }
            KeyboardAction.SWITCH_ALPHA -> kvm.keyboardMode = KeyboardMode.ALPHA
            KeyboardAction.SWITCH_NUMERIC -> kvm.keyboardMode = KeyboardMode.NUMERIC
            KeyboardAction.SWITCH_SYMBOLS -> kvm.keyboardMode = KeyboardMode.SYMBOLS
        }
    }

    @Composable
    fun WhisperDroidKeyboardTheme(content: @Composable () -> Unit) {
        MaterialTheme(content = content)
    }
}
