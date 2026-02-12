package com.whisperdroid.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.whisperdroid.core.Constants
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EncryptedPreferencesManagerTest {

    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var manager: EncryptedPreferencesManager

    @BeforeEach
    fun setUp() {
        context = mockk(relaxed = true)
        sharedPreferences = mockk(relaxed = true)
        editor = mockk(relaxed = true)

        mockkStatic(EncryptedSharedPreferences::class)
        mockkConstructor(MasterKey.Builder::class)

        // Mock MasterKey.Builder
        every { anyConstructed<MasterKey.Builder>().setKeyScheme(any()).build() } returns mockk()
        
        // Mock EncryptedSharedPreferences.create
        every {
            EncryptedSharedPreferences.create(
                any<Context>(),
                any<String>(),
                any<MasterKey>(),
                any<EncryptedSharedPreferences.PrefKeyEncryptionScheme>(),
                any<EncryptedSharedPreferences.PrefValueEncryptionScheme>()
            )
        } returns sharedPreferences

        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.remove(any()) } returns editor
        every { editor.apply() } just Runs

        manager = EncryptedPreferencesManager(context)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `saveString calls sharedPreferences`() {
        manager.saveString("test_key", "test_value")
        verify { editor.putString("test_key", "test_value") }
        verify { editor.apply() }
    }

    @Test
    fun `getString returns value from sharedPreferences`() {
        every { sharedPreferences.getString("test_key", null) } returns "test_value"
        assertEquals("test_value", manager.getString("test_key"))
    }

    @Test
    fun `saveBoolean calls sharedPreferences`() {
        manager.saveBoolean("test_key", true)
        verify { editor.putBoolean("test_key", true) }
        verify { editor.apply() }
    }

    @Test
    fun `getBoolean returns value from sharedPreferences`() {
        every { sharedPreferences.getBoolean("test_key", false) } returns true
        assertTrue(manager.getBoolean("test_key", false))
    }

    @Test
    fun `hasKeys returns true when both keys are present`() {
        every { sharedPreferences.getString(Constants.KEY_OPENAI_API_KEY, null) } returns "sk-123"
        every { sharedPreferences.getString(Constants.KEY_CLAUDE_API_KEY, null) } returns "sk-ant-456"
        
        assertTrue(manager.hasKeys())
    }

    @Test
    fun `hasKeys returns false when one key is missing`() {
        every { sharedPreferences.getString(Constants.KEY_OPENAI_API_KEY, null) } returns "sk-123"
        every { sharedPreferences.getString(Constants.KEY_CLAUDE_API_KEY, null) } returns null
        
        assertFalse(manager.hasKeys())
    }

    @Test
    fun `clearKeys removes both keys`() {
        manager.clearKeys()
        verify { editor.remove(Constants.KEY_OPENAI_API_KEY) }
        verify { editor.remove(Constants.KEY_CLAUDE_API_KEY) }
        verify { editor.apply() }
    }
}
