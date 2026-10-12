package com.example.voice

enum class AssistantState {
    READY,
    LISTENING,
    THINKING,
    SPEAKING
}

enum class LanguageMode(val displayName: String, val code: String) {
    AUTO("Auto Detect", "auto"),
    ENGLISH("English", "en"),
    HINDI("हिंदी (Hindi)", "hi"),
    HINGLISH("Hinglish", "hinglish")
}

data class VoiceSettings(
    val languageMode: LanguageMode = LanguageMode.AUTO,
    val pitch: Float = 0.95f, // Calm, British/JARVIS cadence
    val speechRate: Float = 1.0f,
    val callAnnouncementEnabled: Boolean = true,
    val whatsappAnnouncementEnabled: Boolean = true,
    val readWhatsAppContent: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val isMicMuted: Boolean = false,
    val isSpeechMuted: Boolean = false
)
