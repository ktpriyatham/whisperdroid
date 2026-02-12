package com.whisperdroid.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.whisperdroid.core.ui.theme.WhisperDroidTheme
import com.whisperdroid.core.Constants
import com.whisperdroid.security.EncryptedPreferencesManager

enum class SetupStep(val progress: Float) {
    WELCOME(0.25f),
    OPENAI_KEY(0.5f),
    CLAUDE_KEY(0.75f),
    COMPLETION(1.0f)
}

class SetupWizardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = EncryptedPreferencesManager(this)
        
        setContent {
            WhisperDroidTheme {
                SetupWizardScreen(
                    prefs = prefs,
                    onFinish = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun SetupWizardScreen(
    prefs: EncryptedPreferencesManager,
    onFinish: () -> Unit
) {
    var currentStep by remember { mutableStateOf(SetupStep.WELCOME) }
    var openAIKey by remember { mutableStateOf("") }
    var claudeKey by remember { mutableStateOf("") }
    var showSkipWarning by remember { mutableStateOf(false) }

    BackHandler(enabled = currentStep != SetupStep.WELCOME) {
        currentStep = when (currentStep) {
            SetupStep.OPENAI_KEY -> SetupStep.WELCOME
            SetupStep.CLAUDE_KEY -> SetupStep.OPENAI_KEY
            SetupStep.COMPLETION -> SetupStep.CLAUDE_KEY
            else -> SetupStep.WELCOME
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { currentStep.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
            )

            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState.ordinal > initialState.ordinal) {
                            (slideInHorizontally { it } + fadeIn())
                                .togetherWith(slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn())
                                .togetherWith(slideOutHorizontally { it } + fadeOut())
                        }
                    },
                    label = "StepTransition"
                ) { step ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (step) {
                            SetupStep.WELCOME -> WelcomeStep()
                            SetupStep.OPENAI_KEY -> KeyInputStep(
                                title = "OpenAI API Key",
                                description = "Used for high-quality speech-to-text transcription. You can get one from the OpenAI dashboard.",
                                value = openAIKey,
                                onValueChange = { openAIKey = it }
                            )
                            SetupStep.CLAUDE_KEY -> KeyInputStep(
                                title = "Claude API Key",
                                description = "Used for intelligent text refinement. You can get one from the Anthropic console.",
                                value = claudeKey,
                                onValueChange = { claudeKey = it }
                            )
                            SetupStep.COMPLETION -> CompletionStep()
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep != SetupStep.COMPLETION) {
                    TextButton(onClick = { showSkipWarning = true }) {
                        Text("Skip")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        when (currentStep) {
                            SetupStep.WELCOME -> currentStep = SetupStep.OPENAI_KEY
                            SetupStep.OPENAI_KEY -> currentStep = SetupStep.CLAUDE_KEY
                            SetupStep.CLAUDE_KEY -> {
                                if (openAIKey.isNotBlank()) {
                                    prefs.saveString(Constants.KEY_OPENAI_API_KEY, openAIKey)
                                }
                                if (claudeKey.isNotBlank()) {
                                    prefs.saveString(Constants.KEY_CLAUDE_API_KEY, claudeKey)
                                }
                                currentStep = SetupStep.COMPLETION
                            }
                            SetupStep.COMPLETION -> onFinish()
                        }
                    }
                ) {
                    Text(if (currentStep == SetupStep.COMPLETION) "Get Started" else "Next")
                }
            }
        }
    }

    if (showSkipWarning) {
        AlertDialog(
            onDismissRequest = { showSkipWarning = false },
            title = { Text("Skip Setup?") },
            text = { Text("WhisperDroid requires API keys to function. You can add them later in Settings, but voice input will not work until then.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSkipWarning = false
                        onFinish()
                    }
                ) {
                    Text("Skip Anyway")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSkipWarning = false }) {
                    Text("Go Back")
                }
            }
        )
    }
}

@Composable
fun WelcomeStep() {
    Text(
        text = "Welcome to WhisperDroid",
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Experience the power of AI-driven voice input. Transcribe your speech with OpenAI Whisper and refine it with Anthropic Claude.",
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
}

@Composable
fun KeyInputStep(
    title: String,
    description: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var keyVisible by remember { mutableStateOf(false) }

    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(24.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("API Key") },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { keyVisible = !keyVisible }) {
                Icon(
                    imageVector = if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (keyVisible) "Hide API Key" else "Show API Key"
                )
            }
        }
    )
}

@Composable
fun CompletionStep() {
    Text(
        text = "Setup Complete!",
        style = MaterialTheme.typography.headlineMedium
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "You're all set to use WhisperDroid. Enable the keyboard in your device settings and start talking!",
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
}
