package com.whisperdroid.keyboard

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AbstractComposeView
import com.whisperdroid.core.ui.theme.WhisperDroidTheme

@SuppressLint("ViewConstructor")
class ComposeKeyboardView(
    private val service: WhisperDroidInputMethodService,
    private val viewModel: KeyboardViewModel
) : AbstractComposeView(service) {
    
    @Composable
    override fun Content() {
        WhisperDroidTheme {
            service.KeyboardScreen(viewModel)
        }
    }
}
