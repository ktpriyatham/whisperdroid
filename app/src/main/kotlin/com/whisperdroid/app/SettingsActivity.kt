package com.whisperdroid.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.whisperdroid.app.ui.theme.WhisperDroidTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WhisperDroidTheme {
                Text(text = "Settings Placeholder")
            }
        }
    }
}
