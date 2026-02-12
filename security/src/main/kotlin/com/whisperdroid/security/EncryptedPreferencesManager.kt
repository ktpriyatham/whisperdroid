package com.whisperdroid.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.whisperdroid.core.Constants

class EncryptedPreferencesManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        Constants.PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    fun hasKeys(): Boolean {
        val openAIKey = getString(Constants.KEY_OPENAI_API_KEY)
        val claudeKey = getString(Constants.KEY_CLAUDE_API_KEY)
        return !openAIKey.isNullOrEmpty() && !claudeKey.isNullOrEmpty()
    }

    fun clearKeys() {
        sharedPreferences.edit()
            .remove(Constants.KEY_OPENAI_API_KEY)
            .remove(Constants.KEY_CLAUDE_API_KEY)
            .apply()
    }

    fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }
}
