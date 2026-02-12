# WhisperDroid Release Documentation

This document describes how to build, sign, and install WhisperDroid for production/distribution.

## Version Information
- **Version Name:** 1.0.0
- **Version Code:** 1

## Build Instructions

To build the release APK, run the following command from the project root:

```bash
./gradlew :app:assembleRelease
```

The resulting APK will be located at:
`app/build/outputs/apk/release/app-release.apk` (if signed) or `app/build/outputs/apk/release/app-release-unsigned.apk`.

## Signing Requirements

The release build requires a signing configuration. The current `app/build.gradle.kts` expects the following environment variables to be set:

- `RELEASE_KEYSTORE_PATH`: Path to your `.keystore` or `.jks` file.
- `RELEASE_KEYSTORE_PASSWORD`: Password for the keystore.
- `RELEASE_KEY_ALIAS`: Alias for the signing key.
- `RELEASE_KEY_PASSWORD`: Password for the signing key.

### Example (Bash/Zsh):
```bash
export RELEASE_KEYSTORE_PATH="/path/to/whisperdroid.jks"
export RELEASE_KEYSTORE_PASSWORD="your_keystore_password"
export RELEASE_KEY_ALIAS="whisperdroid_key"
export RELEASE_KEY_PASSWORD="your_key_password"

./gradlew :app:assembleRelease
```

If these environment variables are not set, the build may fail or produce an unsigned APK depending on the environment.

## Installation Steps

1. **Transfer the APK:** Copy the generated `app-release.apk` to your Android device.
2. **Enable Unknown Sources:** If installing for the first time, you may need to enable "Install unknown apps" for your file manager or browser in Android settings.
3. **Install:** Open the APK file on your device and follow the prompts to install.
4. **Permissions:**
   - Launch the **WhisperDroid** app.
   - Follow the Setup Wizard to configure API keys.
   - When prompted, enable the **WhisperDroid Keyboard** in system settings.
   - Grant **Microphone** permission when requested (usually via the keyboard's voice button for the first time).

## Troubleshooting

- **Build Failures:** Ensure all environment variables for signing are correctly exported.
- **App Not Installed:** This can happen if there's a signature mismatch with an existing version. Uninstall any debug versions of WhisperDroid before installing the release version.
- **Voice Input Not Working:** Check if you've entered valid API keys for OpenAI and Anthropic in the app settings.
