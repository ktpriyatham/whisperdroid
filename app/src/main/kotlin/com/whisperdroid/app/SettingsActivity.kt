package com.whisperdroid.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.whisperdroid.app.ui.theme.WhisperDroidTheme
import com.whisperdroid.core.Constants
import com.whisperdroid.security.EncryptedPreferencesManager

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = EncryptedPreferencesManager(this)
        
        setContent {
            WhisperDroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingsScreen(prefs)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(prefs: EncryptedPreferencesManager) {
    var openAIKey by remember { mutableStateOf(prefs.getString(Constants.KEY_OPENAI_API_KEY) ?: "") }
    var claudeKey by remember { mutableStateOf(prefs.getString(Constants.KEY_CLAUDE_API_KEY) ?: "") }
    
    var openAIKeyVisible by remember { mutableStateOf(false) }
    var claudeKeyVisible by remember { mutableStateOf(false) }
    
    var showClearConfirmation by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WhisperDroid Settings") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "API Configuration",
                style = MaterialTheme.typography.titleMedium
            )
            
            // OpenAI API Key
            OutlinedTextField(
                value = openAIKey,
                onValueChange = { openAIKey = it },
                label = { Text("OpenAI API Key") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (openAIKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { openAIKeyVisible = !openAIKeyVisible }) {
                        Icon(
                            imageVector = if (openAIKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (openAIKeyVisible) "Hide" else "Show"
                        )
                    }
                }
            )
            
            // Claude API Key
            OutlinedTextField(
                value = claudeKey,
                onValueChange = { claudeKey = it },
                label = { Text("Claude API Key") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (claudeKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { claudeKeyVisible = !claudeKeyVisible }) {
                        Icon(
                            imageVector = if (claudeKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (claudeKeyVisible) "Hide" else "Show"
                        )
                    }
                }
            )
            
            Button(
                onClick = {
                    prefs.saveString(Constants.KEY_OPENAI_API_KEY, openAIKey)
                    prefs.saveString(Constants.KEY_CLAUDE_API_KEY, claudeKey)
                    scope.launch {
                        snackbarHostState.showSnackbar("Settings saved")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Keys")
            }
            
            HorizontalDivider()
            
            Text(
                text = "Actions",
                style = MaterialTheme.typography.titleMedium
            )
            
            OutlinedButton(
                onClick = { showClearConfirmation = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Clear Keys")
            }
            
            HorizontalDivider()
            
            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = "Version: ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "WhisperDroid uses OpenAI Whisper for transcription and Anthropic Claude for text refinement.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
    
    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Clear API Keys") },
            text = { Text("Are you sure you want to clear both API keys? You will need to re-enter them to use the voice input.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        prefs.clearKeys()
                        openAIKey = ""
                        claudeKey = ""
                        showClearConfirmation = false
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
