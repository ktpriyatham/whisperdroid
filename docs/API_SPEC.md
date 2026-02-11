# WhisperDroid API Specification

## OpenAI Whisper Integration

Used for converting captured audio into raw text.

- **Endpoint**: `POST https://api.openai.com/v1/audio/transcriptions`
- **Method**: `POST`
- **Headers**:
  - `Authorization`: `Bearer {OPENAI_API_KEY}`
  - `Content-Type`: `multipart/form-data`
- **Request Body**:
  - `file`: The audio file (supported: mp3, mp4, mpeg, mpga, m4a, wav, webm). Recommended: `.m4a`.
  - `model`: `whisper-1`
  - `response_format`: `json`
- **Response Format**:
  ```json
  {
    "text": "The transcribed text goes here."
  }
  ```

## Claude API Integration

Used for cleaning up, punctuating, and formatting the raw transcription.

- **Endpoint**: `https://api.anthropic.com/v1/messages`
- **Method**: `POST`
- **Headers**:
  - `x-api-key`: `{CLAUDE_API_KEY}`
  - `anthropic-version`: `2023-06-01`
  - `Content-Type`: `application/json`
- **Request Body**:
  ```json
  {
    "model": "claude-3-haiku-20240307",
    "max_tokens": 1024,
    "system": "You are a helpful keyboard assistant. Your task is to take raw voice-to-text transcriptions and clean them up. Add proper punctuation, correct obvious grammatical errors, and fix spelling. Do not add any conversational filler or meta-comments. Return ONLY the cleaned-up text.",
    "messages": [
      {
        "role": "user",
        "content": "{RAW_TRANSCRIPTION}"
      }
    ]
  }
  ```
- **Response Format**:
  ```json
  {
    "id": "msg_...",
    "content": [
      {
        "text": "The cleaned up text.",
        "type": "text"
      }
    ],
    ...
  }
  ```

## Error Handling & Reliability

### Status Codes
- **401 Unauthorized**: Notify user that API keys are invalid or expired.
- **429 Too Many Requests**: Implement rate limiting UI; notify user to wait.
- **5xx Server Errors**: Automatic retry with exponential backoff.
- **Network Unavailable**: Detect offline state before attempting API calls and show an appropriate error toast in the keyboard UI.

### Timeout Strategy
- **Whisper**: 30-second timeout (accommodates larger audio uploads).
- **Claude**: 15-second timeout.

### Retry Logic
- Maximum of **3 retries** for transient network failures or 5xx server responses.
- Uses exponential backoff (1s, 2s, 4s).

## Security Note

API keys must never be hardcoded. They are injected at runtime from `EncryptedSharedPreferences`. Users are warned that their voice data is sent to external APIs (OpenAI and Anthropic) for processing.
