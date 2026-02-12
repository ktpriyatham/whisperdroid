# WhisperDroid

WhisperDroid is an AI-powered Android Keyboard (IME) that brings world-class voice transcription and text refinement directly to your fingertips. By leveraging OpenAI's Whisper API and Anthropic's Claude API, WhisperDroid delivers high-accuracy transcriptions that are automatically cleaned up for grammar, style, and clarity.

## Features

- **High-Accuracy Voice Input**: Uses OpenAI Whisper for industry-leading voice-to-text.
- **AI Text Refinement**: Optional cleanup via Anthropic Claude to fix "umms," "ahhs," and grammatical errors.
- **Secure by Design**: API keys are stored locally using Android's `EncryptedSharedPreferences`.
- **Modern Keyboard UI**: Built with Jetpack Compose, featuring:
    - Long-press numeric input on the top row (Q-P).
    - Customizable haptic feedback.
    - Dark mode support.
    - Accessory row with real-time status and error messages.
- **Customizable Pipeline**:
    - Toggle text refinement on/off.
    - Choose between Claude models (Haiku, Sonnet, Opus).
    - Provide a custom system prompt for tailored refinement.
    - Optional "Clipboard Output" to copy transcribed text while typing.
- **Robust Networking**: Automatic retries and offline detection.

## Project Structure

This project follows a multi-module architecture:

- `:app`: Main application, settings UI, and first-time setup wizard.
- `:keyboard`: The `InputMethodService` and Compose-based keyboard UI.
- `:api`: Retrofit clients for OpenAI and Anthropic APIs.
- `:security`: Secure storage layer for API keys.
- `:core`: Shared theme, constants, and utility classes.

## Requirements

- Android Min SDK: 24 (Android 7.0+)
- Android Target SDK: 34 (Android 14)
- OpenAI API Key (for Whisper)
- Anthropic API Key (for Claude)

## Installation & Setup

1. **Build the APK**:
   ```bash
   ./gradlew :app:assembleDebug
   ```
2. **Install**: Sideload the generated APK onto your Android device.
3. **Enable Keyboard**:
   - Go to Android Settings > System > Languages & input > On-screen keyboard > Manage on-screen keyboards.
   - Toggle **WhisperDroid** to ON.
4. **Configuration**:
   - Open the WhisperDroid app from your launcher.
   - Follow the Setup Wizard to enter your OpenAI and Anthropic API keys.
   - Ensure the microphone permission is granted when prompted.
5. **Usage**:
   - Switch to the WhisperDroid keyboard in any text field.
   - **Hold the Record Button** (Microphone icon) to start recording.
   - **Release** to stop and trigger the transcription/refinement pipeline.

## Contributing

We welcome contributions! To get started:

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/amazing-feature`).
3. Commit your changes (`git commit -m 'Add amazing feature'`).
4. Push to the branch (`git push origin feature/amazing-feature`).
5. Open a Pull Request.

### Development Guidelines
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).
- Ensure all new features are accompanied by unit tests where applicable.
- Use `./gradlew test` to run the test suite.
