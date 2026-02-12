package com.whisperdroid.keyboard

import android.Manifest
import android.content.Context
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
import com.whisperdroid.core.NetworkUtils
import com.whisperdroid.keyboard.audio.AudioHandler
import com.whisperdroid.security.EncryptedPreferencesManager
import android.provider.Settings
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class WhisperDroidInputMethodService : InputMethodService() {

    enum class HapticType {
        KEY_PRESS,
        VOICE_START,
        VOICE_STOP,
        SUCCESS,
        ERROR
    }

    private val viewModel = KeyboardViewModel()
    private lateinit var audioHandler: AudioHandler
    private lateinit var prefs: EncryptedPreferencesManager
    private lateinit var vibrator: Vibrator
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        audioHandler = AudioHandler(this)
        prefs = EncryptedPreferencesManager(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
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

    private fun performHapticFeedback(type: HapticType = HapticType.KEY_PRESS) {
        if (prefs.getBoolean(Constants.KEY_HAPTICS_ENABLED, true) && isSystemHapticFeedbackEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (type) {
                    HapticType.KEY_PRESS -> VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
                    HapticType.VOICE_START -> VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                    HapticType.VOICE_STOP -> VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
                    HapticType.SUCCESS -> VibrationEffect.createWaveform(longArrayOf(0, 50, 100, 50), -1)
                    HapticType.ERROR -> VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                when (type) {
                    HapticType.KEY_PRESS -> vibrator.vibrate(30)
                    HapticType.VOICE_START -> vibrator.vibrate(50)
                    HapticType.VOICE_STOP -> vibrator.vibrate(30)
                    HapticType.SUCCESS -> vibrator.vibrate(longArrayOf(0, 50, 100, 50), -1)
                    HapticType.ERROR -> vibrator.vibrate(100)
                }
            }
        }
    }

    private fun isSystemHapticFeedbackEnabled(): Boolean {
        return try {
            Settings.System.getInt(contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) != 0
        } catch (e: Exception) {
            true
        }
    }

    @Composable
    fun KeyboardScreen(kvm: KeyboardViewModel) {
        LaunchedEffect(kvm.voiceState) {
            when (kvm.voiceState) {
                VoiceState.SUCCESS -> {
                    delay(800)
                    kvm.voiceState = VoiceState.IDLE
                }
                VoiceState.OFFLINE, VoiceState.ERROR -> {
                    delay(3000)
                    kvm.voiceState = VoiceState.IDLE
                }
                else -> {}
            }
        }

        Surface {
            KeyboardLayout(
                shiftState = kvm.shiftState,
                keyboardMode = kvm.keyboardMode,
                voiceState = kvm.voiceState,
                errorMessage = kvm.errorMessage,
                onKeyClick = { text ->
                    performHapticFeedback()
                    currentInputConnection?.commitText(text, 1)
                    if (kvm.shiftState == ShiftState.SHIFTED) {
                        kvm.shiftState = ShiftState.NONE
                    }
                },
                onKeyLongClick = { text ->
                    performHapticFeedback()
                    currentInputConnection?.commitText(text, 1)
                },
                onActionClick = { action ->
                    handleAction(action, kvm)
                },
                onVoiceStart = {
                    performHapticFeedback(HapticType.VOICE_START)
                    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        kvm.voiceState = VoiceState.RECORDING
                        try {
                            audioHandler.startRecording()
                        } catch (e: Exception) {
                            showError("Failed to start recording")
                        }
                    } else {
                        showError("Microphone permission required")
                        val intent = Intent(this, PermissionActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                    }
                },
                onVoiceStop = {
                    if (kvm.voiceState == VoiceState.RECORDING) {
                        performHapticFeedback(HapticType.VOICE_STOP)
                        val audioFile = audioHandler.stopRecording()
                        
                        if (!NetworkUtils.isOnline(this)) {
                            showError("No internet connection", VoiceState.OFFLINE)
                            if (audioFile?.exists() == true) {
                                audioFile.delete()
                            }
                            return@KeyboardLayout
                        }

                        kvm.voiceState = VoiceState.TRANSCRIBING
                        if (audioFile != null && audioFile.exists()) {
                            Log.d("WhisperDroid", "Recording saved to: ${audioFile.absolutePath}")
                            processAudio(audioFile)
                        } else {
                            showError("Recording failed")
                        }
                    }
                }
            )
        }
    }

    private fun showError(message: String, state: VoiceState = VoiceState.ERROR) {
        performHapticFeedback(HapticType.ERROR)
        viewModel.errorMessage = message
        viewModel.voiceState = state
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun handleAction(action: KeyboardAction, kvm: KeyboardViewModel) {
        performHapticFeedback()
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
        serviceScope.launch {
            try {
                withTimeout(30000) {
                    val openAiKey = prefs.getString(Constants.KEY_OPENAI_API_KEY)
                    val claudeKey = prefs.getString(Constants.KEY_CLAUDE_API_KEY)

                    if (openAiKey.isNullOrBlank()) {
                        showError("OpenAI API key not set")
                        return@withTimeout
                    }

                    // Transcription step
                    viewModel.voiceState = VoiceState.TRANSCRIBING
                    val transcription = withContext(Dispatchers.IO) {
                        WhisperApiClient.transcribe(openAiKey, audioFile)
                    }

                    if (transcription.isBlank()) {
                        showError("No speech detected")
                        return@withTimeout
                    }

                    val refinementEnabled = prefs.getBoolean(Constants.KEY_REFINEMENT_ENABLED, true)
                    var resultText = transcription

                    if (refinementEnabled && !claudeKey.isNullOrBlank()) {
                        // Cleaning up step
                        viewModel.voiceState = VoiceState.CLEANING_UP
                        val systemPrompt = prefs.getString(Constants.KEY_SYSTEM_PROMPT)
                            .takeUnless { it.isNullOrBlank() } ?: Constants.DEFAULT_SYSTEM_PROMPT
                        val cleanedText = try {
                            withContext(Dispatchers.IO) {
                                ClaudeApiClient.cleanUp(claudeKey, transcription, systemPrompt)
                            }
                        } catch (e: Exception) {
                            Log.e(Constants.LOG_TAG, "Claude cleanup failed, using raw transcription", e)
                            transcription
                        }
                        if (cleanedText.isNotBlank()) {
                            resultText = cleanedText
                        }
                    }

                    currentInputConnection?.commitText(resultText, 1)

                    val clipboardOutput = prefs.getBoolean(Constants.KEY_CLIPBOARD_OUTPUT, false)
                    if (clipboardOutput) {
                        val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("WhisperDroid Transcription", resultText)
                        clipboard.setPrimaryClip(clip)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@WhisperDroidInputMethodService, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    }

                    performHapticFeedback(HapticType.SUCCESS)
                    viewModel.voiceState = VoiceState.SUCCESS
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(Constants.LOG_TAG, "Audio processing timed out", e)
                showError("Processing timed out")
            } catch (e: Exception) {
                Log.e(Constants.LOG_TAG, "Error processing audio", e)
                showError("Transcription failed")
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
