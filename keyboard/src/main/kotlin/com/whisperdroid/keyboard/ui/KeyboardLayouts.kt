package com.whisperdroid.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whisperdroid.keyboard.KeyboardMode
import com.whisperdroid.keyboard.ShiftState
import com.whisperdroid.keyboard.VoiceState

@Composable
fun KeyboardLayout(
    shiftState: ShiftState,
    keyboardMode: KeyboardMode,
    voiceState: VoiceState,
    errorMessage: String,
    onKeyClick: (String) -> Unit,
    onKeyLongClick: (String) -> Unit,
    onActionClick: (KeyboardAction) -> Unit,
    onVoiceStart: () -> Unit,
    onVoiceStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(4.dp)
    ) {
        // Accessory/Suggestion Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            val statusText = when (voiceState) {
                VoiceState.RECORDING -> "Recording..."
                VoiceState.TRANSCRIBING -> "Transcribing..."
                VoiceState.CLEANING_UP -> "Cleaning up..."
                VoiceState.SUCCESS -> "Done!"
                VoiceState.OFFLINE -> if (errorMessage.isNotEmpty()) errorMessage else "Offline"
                VoiceState.ERROR -> errorMessage
                else -> ""
            }

            if (statusText.isNotEmpty()) {
                val textColor = if (voiceState == VoiceState.ERROR || voiceState == VoiceState.OFFLINE) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }

            VoiceButton(
                state = voiceState,
                onStartRecording = onVoiceStart,
                onStopRecording = onVoiceStop
            )
        }

        when (keyboardMode) {
            KeyboardMode.ALPHA -> AlphaLayout(shiftState, onKeyClick, onKeyLongClick, onActionClick)
            KeyboardMode.NUMERIC -> NumericLayout(onKeyClick, onActionClick)
            KeyboardMode.SYMBOLS -> SymbolsLayout(onKeyClick, onActionClick)
        }
    }
}

@Composable
fun AlphaLayout(
    shiftState: ShiftState,
    onKeyClick: (String) -> Unit,
    onKeyLongClick: (String) -> Unit,
    onActionClick: (KeyboardAction) -> Unit
) {
    val rows = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("Shift", "z", "x", "c", "v", "b", "n", "m", "Backspace")
    )

    val topRowHints = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")

    rows.forEachIndexed { rowIndex, row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            row.forEachIndexed { colIndex, key ->
                val weight = when (key) {
                    "Shift", "Backspace" -> 1.5f
                    else -> 1f
                }
                
                val displayText = when (key) {
                    "Shift" -> "↑"
                    "Backspace" -> "⌫"
                    else -> if (shiftState != ShiftState.NONE) key.uppercase() else key
                }

                val hint = if (rowIndex == 0) topRowHints.getOrNull(colIndex) else null

                Key(
                    text = displayText,
                    onClick = {
                        when (key) {
                            "Shift" -> onActionClick(KeyboardAction.SHIFT)
                            "Backspace" -> onActionClick(KeyboardAction.BACKSPACE)
                            else -> onKeyClick(displayText)
                        }
                    },
                    onLongClick = if (hint != null) {
                        { onKeyLongClick(hint) }
                    } else null,
                    hint = hint,
                    modifier = Modifier.weight(weight),
                    isFunctional = key == "Shift" || key == "Backspace"
                )
            }
        }
    }

    // Bottom row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Key(text = "?123", onClick = { onActionClick(KeyboardAction.SWITCH_NUMERIC) }, modifier = Modifier.weight(1.5f), isFunctional = true)
        Key(text = ",", onClick = { onKeyClick(",") }, modifier = Modifier.weight(1f))
        Key(text = " ", onClick = { onActionClick(KeyboardAction.SPACE) }, modifier = Modifier.weight(4f))
        Key(text = ".", onClick = { onKeyClick(".") }, modifier = Modifier.weight(1f))
        Key(text = "Enter", onClick = { onActionClick(KeyboardAction.ENTER) }, modifier = Modifier.weight(1.5f), isFunctional = true)
    }
}

@Composable
fun NumericLayout(
    onKeyClick: (String) -> Unit,
    onActionClick: (KeyboardAction) -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("@", "#", "$", "_", "&", "-", "+", "(", ")", "/"),
        listOf("=\\<", "*", "\"", "'", ":", ";", "!", "?", "Backspace")
    )

    rows.forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            row.forEach { key ->
                val weight = when (key) {
                    "=\\<", "Backspace" -> 1.5f
                    else -> 1f
                }
                val displayText = if (key == "Backspace") "⌫" else key
                
                Key(
                    text = displayText,
                    onClick = {
                        when (key) {
                            "=\\<" -> onActionClick(KeyboardAction.SWITCH_SYMBOLS)
                            "Backspace" -> onActionClick(KeyboardAction.BACKSPACE)
                            else -> onKeyClick(key)
                        }
                    },
                    modifier = Modifier.weight(weight),
                    isFunctional = key == "=\\<" || key == "Backspace"
                )
            }
        }
    }

    // Bottom row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Key(text = "ABC", onClick = { onActionClick(KeyboardAction.SWITCH_ALPHA) }, modifier = Modifier.weight(1.5f), isFunctional = true)
        Key(text = ",", onClick = { onKeyClick(",") }, modifier = Modifier.weight(1f))
        Key(text = " ", onClick = { onActionClick(KeyboardAction.SPACE) }, modifier = Modifier.weight(4f))
        Key(text = ".", onClick = { onKeyClick(".") }, modifier = Modifier.weight(1f))
        Key(text = "Enter", onClick = { onActionClick(KeyboardAction.ENTER) }, modifier = Modifier.weight(1.5f), isFunctional = true)
    }
}

@Composable
fun SymbolsLayout(
    onKeyClick: (String) -> Unit,
    onActionClick: (KeyboardAction) -> Unit
) {
     val rows = listOf(
        listOf("~", "`", "|", "•", "√", "π", "÷", "×", "{", "}"),
        listOf("£", "¢", "€", "¥", "^", "°", "=", "[", "]", " "),
        listOf("?123", "™", "®", "©", "¶", "\\", "<", ">", "Backspace")
    )

    rows.forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            row.forEach { key ->
                val weight = when (key) {
                    "?123", "Backspace" -> 1.5f
                    else -> 1f
                }
                val displayText = if (key == "Backspace") "⌫" else key
                
                Key(
                    text = displayText,
                    onClick = {
                        when (key) {
                            "?123" -> onActionClick(KeyboardAction.SWITCH_NUMERIC)
                            "Backspace" -> onActionClick(KeyboardAction.BACKSPACE)
                            else -> if (key.isNotBlank()) onKeyClick(key)
                        }
                    },
                    modifier = Modifier.weight(weight),
                    isFunctional = key == "?123" || key == "Backspace"
                )
            }
        }
    }

    // Bottom row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Key(text = "ABC", onClick = { onActionClick(KeyboardAction.SWITCH_ALPHA) }, modifier = Modifier.weight(1.5f), isFunctional = true)
        Key(text = ",", onClick = { onKeyClick(",") }, modifier = Modifier.weight(1f))
        Key(text = " ", onClick = { onActionClick(KeyboardAction.SPACE) }, modifier = Modifier.weight(4f))
        Key(text = ".", onClick = { onKeyClick(".") }, modifier = Modifier.weight(1f))
        Key(text = "Enter", onClick = { onActionClick(KeyboardAction.ENTER) }, modifier = Modifier.weight(1.5f), isFunctional = true)
    }
}

enum class KeyboardAction {
    SHIFT,
    BACKSPACE,
    ENTER,
    SPACE,
    SWITCH_ALPHA,
    SWITCH_NUMERIC,
    SWITCH_SYMBOLS
}
