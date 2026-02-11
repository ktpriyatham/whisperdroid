package com.whisperdroid.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.whisperdroid.app.ui.theme.WhisperDroidTheme

class SetupWizardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WhisperDroidTheme {
                Text(text = "Setup Wizard Placeholder")
            }
        }
    }
}
