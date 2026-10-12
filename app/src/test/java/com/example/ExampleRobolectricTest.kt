package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("JARVIS AI", appName)
  }

  @Test
  fun `test language detection`() {
    val hindiText = "नमस्ते सज्जाद जी"
    val hinglishText = "bhai kaise ho"
    val englishText = "open youtube and search"

    assertEquals(com.example.voice.LanguageMode.HINDI, com.example.voice.LanguageDetector.detectLanguage(hindiText))
    assertEquals(com.example.voice.LanguageMode.HINGLISH, com.example.voice.LanguageDetector.detectLanguage(hinglishText))
    assertEquals(com.example.voice.LanguageMode.ENGLISH, com.example.voice.LanguageDetector.detectLanguage(englishText))
  }
}
