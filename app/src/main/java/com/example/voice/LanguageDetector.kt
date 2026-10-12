package com.example.voice

object LanguageDetector {
    private val DEVANAGARI_REGEX = Regex("[\\u0900-\\u097F]")

    private val HINGLISH_KEYWORDS = setOf(
        "kya", "kaise", "kaisa", "kaisi", "kholo", "khol", "karo", "karna", "hai", "hain", "ho", "hoon",
        "nahi", "shukriya", "namaste", "accha", "theek", "batao", "bataiye", "bata", "sun", "suno", "yaar",
        "bhai", "mera", "meri", "mere", "hum", "aap", "tum", "dhanyawad", "kaun", "kahan", "kab",
        "kuch", "bolo", "chup", "ruko", "ruk", "par", "ko", "se", "mein", "ka", "ki", "ke",
        "chahiye", "kar", "dikhau", "dikhao", "madad", "samjhe", "samajh", "bhejo", "karein", "dekh"
    )

    fun detectLanguage(text: String, preferredMode: LanguageMode = LanguageMode.AUTO): LanguageMode {
        if (preferredMode != LanguageMode.AUTO) {
            return preferredMode
        }

        val trimmed = text.trim()
        if (trimmed.isEmpty()) return LanguageMode.ENGLISH

        // Check for Devanagari characters
        if (DEVANAGARI_REGEX.containsMatchIn(trimmed)) {
            return LanguageMode.HINDI
        }

        // Tokenize and check for Hinglish markers
        val words = trimmed.lowercase().split(Regex("[^a-zA-Z]+")).filter { it.isNotEmpty() }
        val hinglishCount = words.count { it in HINGLISH_KEYWORDS }

        return if (hinglishCount >= 1 || (words.isNotEmpty() && hinglishCount.toDouble() / words.size > 0.15)) {
            LanguageMode.HINGLISH
        } else {
            LanguageMode.ENGLISH
        }
    }
}
