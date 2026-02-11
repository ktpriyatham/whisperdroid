package com.whisperdroid.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme

class WhisperDroidInputMethodService : InputMethodService() {

    override fun onCreateInputView(): View {
        val frameLayout = FrameLayout(this)
        val composeView = ComposeView(this).apply {
            setContent {
                WhisperDroidKeyboardTheme {
                    KeyboardView()
                }
            }
        }
        frameLayout.addView(composeView)
        return frameLayout
    }

    @Composable
    fun KeyboardView() {
        Column {
            Text(text = "WhisperDroid Keyboard")
            Button(onClick = { /* TODO: Start Recording */ }) {
                Text(text = "Record")
            }
        }
    }

    @Composable
    fun WhisperDroidKeyboardTheme(content: @Composable () -> Unit) {
        MaterialTheme(content = content)
    }
}
