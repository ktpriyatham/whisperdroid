# WhisperDroid

WhisperDroid is an Android Input Method (Keyboard) that uses OpenAI's Whisper API for high-quality voice transcription and Anthropic's Claude API for text refinement.

## Project Structure

This project follows a multi-module architecture:

- `:app`: Main application, settings, and setup wizard.
- `:keyboard`: The InputMethodService and keyboard UI.
- `:api`: Retrofit clients for Whisper and Claude APIs.
- `:security`: Secure storage of API keys using EncryptedSharedPreferences.
- `:core`: Base classes, constants, and shared utilities.

## Requirements

- Android Min SDK: 24
- Android Target SDK: 34
- Kotlin 1.9.22
- Jetpack Compose with Material 3

## Build Instructions

To build the project, run:

```bash
./gradlew assembleDebug
```

## Setup

1. Enable the WhisperDroid keyboard in Android Settings.
2. Open the WhisperDroid app to configure OpenAI and Anthropic API keys.
3. Start using voice transcription by tapping the record button on the keyboard.
