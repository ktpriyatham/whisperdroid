package com.whisperdroid.keyboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.material3.MaterialTheme
import kotlinx.coroutines.delay
import androidx.compose.material3.Surface
import com.whisperdroid.keyboard.ui.KeyboardAction
import com.whisperdroid.keyboard.ui.KeyboardLayout
import android.view.KeyEvent
import com.whisperdroid.api.ClaudeApiClient
import com.whisperdroid.api.WhisperApiClient
import com.whisperdroid.core.Constants
import com.whisperdroid.keyboard.audio.AudioHandler
import com.whisperdroid.security.EncryptedPreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WhisperDroidInputMethodService : InputMethodService() {

    private val viewModel = KeyboardViewModel()
    private lateinit var audioHandler: AudioHandler
    private lateinit var prefs: EncryptedPreferencesManager
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        audioHandler = AudioHandler(this)
        prefs = EncryptedPreferencesManager(this)
    }

    override fun onDestroy() {
        audioHandler.release()
        serviceScope.cancel()
        super.onDestroy()
    }

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
                    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        kvm.voiceState = VoiceState.RECORDING
                        try {
                            audioHandler.startRecording()
                        } catch (e: Exception) {
                            Toast.makeText(this, "Failed to start recording", Toast.LENGTH_SHORT).show()
                            kvm.voiceState = VoiceState.IDLE
                        }
                    } else {
                        Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, PermissionActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                        kvm.voiceState = VoiceState.IDLE
                    }
                },
                onVoiceStop = {
                    if (kvm.voiceState == VoiceState.RECORDING) {
                        kvm.voiceState = VoiceState.PROCESSING
                        val audioFile = audioHandler.stopRecording()
                        if (audioFile != null && audioFile.exists()) {
                            Log.d("WhisperDroid", "Recording saved to: ${audioFile.absolutePath}")
                            processAudio(audioFile)
                        } else {
                            Toast.makeText(this, "Recording failed", Toast.LENGTH_SHORT).show()
                            kvm.voiceState = VoiceState.IDLE
                        }
                    }
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

    private fun processAudio(audioFile: java.io.File) {
        viewModel.voiceState = VoiceState.PROCESSING
        serviceScope.launch {
            try {
                val openAiKey = prefs.getString(Constants.KEY_OPENAI_API_KEY)
                val claudeKey = prefs.getString(Constants.KEY_CLAUDE_API_KEY)

                if (openAiKey.isNullOrBlank() || claudeKey.isNullOrBlank()) {
                    Toast.makeText(this@WhisperDroidInputMethodService, "Please set API keys in settings", Toast.LENGTH_LONG).show()
                    viewModel.voiceState = VoiceState.IDLE
                    return@launch
                }

                val transcription = withContext(Dispatchers.IO) {
                    WhisperApiClient.transcribe(openAiKey, audioFile)
                }

                if (transcription.isBlank()) {
                    Toast.makeText(this@WhisperDroidInputMethodService, "No speech detected", Toast.LENGTH_SHORT).show()
                    viewModel.voiceState = VoiceState.IDLE
                    return@launch
                }

                val cleanedText = withContext(Dispatchers.IO) {
                    ClaudeApiClient.cleanUp(claudeKey, transcription)
                }

                if (cleanedText.isNotBlank()) {
                    currentInputConnection?.commitText(cleanedText, 1)
                    viewModel.voiceState = VoiceState.SUCCESS
                } else {
                    currentInputConnection?.commitText(transcription, 1)
                    viewModel.voiceState = VoiceState.SUCCESS
                }

            } catch (e: Exception) {
                Log.e(Constants.LOG_TAG, "Error processing audio", e)
                Toast.makeText(this@WhisperDroidInputMethodService, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                viewModel.voiceState = VoiceState.IDLE
            } finally {
                if (audioFile.exists()) {
                    audioFile.delete()
                }
            }
        }
    }

    @Composable
    fun WhisperDroidKeyboardTheme(content: @Composable () -> Unit) {
        MaterialTheme(content = content)
    }
}
