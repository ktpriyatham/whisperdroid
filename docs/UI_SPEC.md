# WhisperDroid UI Specification

## Keyboard Layout

The WhisperDroid keyboard follows the standard Android QWERTY layout conventions to ensure a low learning curve for users.

### Main Keyboard
- **Top Row**: Numbers or QWERTY letters (depending on shift/mode).
- **Middle Rows**: Standard alpha keys.
- **Bottom Row**: Shift, Symbols/Emoji, Spacebar, Period, and Enter.
- **Voice Button**: Located in the top-right corner of the keyboard's suggestion/accessory bar.

## Voice Interaction States

The Voice Button is the primary interface for WhisperDroid's core feature. It transitions through four distinct visual states:

1. **Idle**: 
    - **Visual**: Standard microphone icon (`ic_mic_none`).
    - **Description**: Waiting for user interaction.
2. **Recording**: 
    - **Visual**: Red microphone icon with a soft red pulsing circle animation behind it.
    - **Interaction**: Triggered by holding the button. Audio is captured as long as the button is held.
3. **Processing**: 
    - **Visual**: The microphone icon is replaced by a circular indeterminate progress spinner.
    - **Description**: Audio has been sent to the APIs; waiting for transcription and refinement.
4. **Done/Success**: 
    - **Visual**: A green checkmark icon appears briefly (800ms) before returning to Idle.
    - **Description**: Text has been successfully inserted.

## Settings Application

The companion app provides the necessary configuration for the keyboard.

### 1. Welcome / Setup Wizard
- Step-by-step guide to:
    - Enable WhisperDroid in Language & Input settings.
    - Set WhisperDroid as the default keyboard.
    - Request Microphone permissions.

### 2. API Configuration
- **OpenAI API Key**: Password-masked input field with a "Test" button.
- **Claude API Key**: Password-masked input field with a "Test" button.
- **Storage**: Keys are persisted in `EncryptedSharedPreferences`.

### 3. About
- Displays version information.
- Lists technical specifications: Min SDK 24, Target SDK 34.
- Links to privacy policy and source code.

### 4. Preferences
- **Haptic Feedback**: Toggle for vibration on keypress/recording start.
- **Output Target**: Toggle between "Direct Input" and "Clipboard" (or both).
- **Refinement Toggle**: Option to disable Claude refinement and use raw Whisper output.
- **System Prompt**: Editable field to customize how Claude cleans up the text.

## Theming and Design System

- **Compose Material 3**: Uses Material 3 components for the settings app.
- **Dynamic Color**: Supports Android 12+ dynamic color (Material You).
- **Dark/Light Mode**: Full support for system-wide dark/light mode switching.
- **Keyboard Themes**:
    - *Light*: Light gray background with white keys.
    - *Dark*: Dark gray/black background with charcoal keys.

## Responsive Design

- **Phones**: Standard full-width keyboard.
- **Foldables (Fold 7)**: 
    - *Unfolded*: Support for a "Split" layout to allow thumb typing on larger screens.
- **Tablets**: 
    - Centered "Floating" keyboard option or split layout.
    - Keyboard height is adjustable in settings to avoid taking up too much vertical space.
