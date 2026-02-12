package com.whisperdroid.core

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ConstantsTest {

    @Test
    fun `preference keys are not null or empty`() {
        assertNotNull(Constants.PREFS_NAME)
        assertFalse(Constants.PREFS_NAME.isEmpty())
        
        assertNotNull(Constants.KEY_OPENAI_API_KEY)
        assertFalse(Constants.KEY_OPENAI_API_KEY.isEmpty())
        
        assertNotNull(Constants.KEY_CLAUDE_API_KEY)
        assertFalse(Constants.KEY_CLAUDE_API_KEY.isEmpty())
        
        assertNotNull(Constants.KEY_REFINEMENT_ENABLED)
        assertFalse(Constants.KEY_REFINEMENT_ENABLED.isEmpty())
        
        assertNotNull(Constants.KEY_CLIPBOARD_OUTPUT)
        assertFalse(Constants.KEY_CLIPBOARD_OUTPUT.isEmpty())
        
        assertNotNull(Constants.KEY_HAPTICS_ENABLED)
        assertFalse(Constants.KEY_HAPTICS_ENABLED.isEmpty())
        
        assertNotNull(Constants.KEY_SYSTEM_PROMPT)
        assertFalse(Constants.KEY_SYSTEM_PROMPT.isEmpty())
        
        assertNotNull(Constants.KEY_CLAUDE_MODEL)
        assertFalse(Constants.KEY_CLAUDE_MODEL.isEmpty())
    }

    @Test
    fun `default values are valid`() {
        assertNotNull(Constants.DEFAULT_SYSTEM_PROMPT)
        assertFalse(Constants.DEFAULT_SYSTEM_PROMPT.isEmpty())
        
        assertNotNull(Constants.DEFAULT_CLAUDE_MODEL)
        assertFalse(Constants.DEFAULT_CLAUDE_MODEL.isEmpty())
    }
}
