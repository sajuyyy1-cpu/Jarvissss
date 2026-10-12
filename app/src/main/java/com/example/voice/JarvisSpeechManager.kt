package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class JarvisSpeechManager(private val context: Context) : RecognitionListener, TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "JarvisSpeechManager"
        private const val UTTERANCE_ID = "JARVIS_RESPONSE_UTTERANCE"
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val _assistantState = MutableStateFlow(AssistantState.READY)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val _voiceSettings = MutableStateFlow(VoiceSettings())
    val voiceSettings: StateFlow<VoiceSettings> = _voiceSettings.asStateFlow()

    fun setState(state: AssistantState) {
        _assistantState.value = state
    }

    var onSpeechRecognized: ((text: String, detectedLang: LanguageMode) -> Unit)? = null
    var onErrorOccurred: ((errorMessage: String) -> Unit)? = null

    init {
        initSpeechRecognizer()
        initTextToSpeech()
    }

    private fun initSpeechRecognizer() {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(this@JarvisSpeechManager)
                }
            } else {
                Log.w(TAG, "SpeechRecognizer is not available on this device.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize SpeechRecognizer: ${e.message}")
        }
    }

    private fun initTextToSpeech() {
        try {
            textToSpeech = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init TextToSpeech: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            textToSpeech?.let { tts ->
                tts.setPitch(_voiceSettings.value.pitch)
                tts.setSpeechRate(_voiceSettings.value.speechRate)
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _assistantState.value = AssistantState.SPEAKING
                    }

                    override fun onDone(utteranceId: String?) {
                        _assistantState.value = AssistantState.READY
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _assistantState.value = AssistantState.READY
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _assistantState.value = AssistantState.READY
                    }
                })
            }
            Log.d(TAG, "TextToSpeech successfully initialized.")
        } else {
            Log.e(TAG, "TextToSpeech initialization failed with status: $status")
            isTtsReady = false
        }
    }

    fun updateSettings(settings: VoiceSettings) {
        _voiceSettings.value = settings
        textToSpeech?.let {
            it.setPitch(settings.pitch)
            it.setSpeechRate(settings.speechRate)
        }
    }

    fun startListening() {
        if (_voiceSettings.value.isMicMuted) {
            onErrorOccurred?.invoke("Microphone is currently muted in JARVIS settings.")
            return
        }

        // CRITICAL: Stop any ongoing speech to prevent self-hearing!
        stopSpeaking()

        if (speechRecognizer == null) {
            initSpeechRecognizer()
        }

        val recognizer = speechRecognizer
        if (recognizer == null) {
            onErrorOccurred?.invoke("Speech recognition service unavailable on this device. Please use text input.")
            return
        }

        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

                when (_voiceSettings.value.languageMode) {
                    LanguageMode.HINDI -> {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                    }
                    LanguageMode.HINGLISH -> {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
                    }
                    LanguageMode.ENGLISH -> {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                    }
                    LanguageMode.AUTO -> {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
                        putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("hi-IN", "en-US", "en-IN"))
                    }
                }
            }

            _assistantState.value = AssistantState.LISTENING
            _audioRms.value = 0f
            recognizer.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition: ${e.message}")
            _assistantState.value = AssistantState.READY
            onErrorOccurred?.invoke("Failed to start listening: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping listening: ${e.message}")
        }
        if (_assistantState.value == AssistantState.LISTENING) {
            _assistantState.value = AssistantState.READY
        }
        _audioRms.value = 0f
    }

    fun cancelListening() {
        try {
            speechRecognizer?.cancel()
        } catch (e: Exception) {
            Log.w(TAG, "Error canceling listening: ${e.message}")
        }
        _assistantState.value = AssistantState.READY
        _audioRms.value = 0f
    }

    fun speak(text: String, languageMode: LanguageMode = _voiceSettings.value.languageMode) {
        if (_voiceSettings.value.isSpeechMuted) {
            return
        }

        // CRITICAL: Stop speech recognition before speaking to prevent self-hearing!
        cancelListening()

        if (!isTtsReady || textToSpeech == null) {
            Log.w(TAG, "TTS not ready to speak.")
            return
        }

        val tts = textToSpeech ?: return
        val targetLocale = when (languageMode) {
            LanguageMode.HINDI -> Locale("hi", "IN")
            LanguageMode.HINGLISH -> {
                // Check if Hindi voice is available, else English (India)
                if (tts.isLanguageAvailable(Locale("hi", "IN")) >= TextToSpeech.LANG_AVAILABLE) {
                    Locale("hi", "IN")
                } else {
                    Locale("en", "IN")
                }
            }
            LanguageMode.ENGLISH -> Locale.US
            LanguageMode.AUTO -> {
                val detected = LanguageDetector.detectLanguage(text)
                if (detected == LanguageMode.HINDI) Locale("hi", "IN") else Locale.US
            }
        }

        try {
            if (tts.isLanguageAvailable(targetLocale) >= TextToSpeech.LANG_AVAILABLE) {
                tts.language = targetLocale
            } else {
                tts.language = Locale.US
            }
        } catch (e: Exception) {
            tts.language = Locale.US
        }

        _assistantState.value = AssistantState.SPEAKING
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    fun stopSpeaking() {
        if (_assistantState.value == AssistantState.SPEAKING) {
            textToSpeech?.stop()
            _assistantState.value = AssistantState.READY
        }
    }

    fun emergencyStop() {
        cancelListening()
        stopSpeaking()
        _assistantState.value = AssistantState.READY
        _audioRms.value = 0f
    }

    // RecognitionListener Callbacks
    override fun onReadyForSpeech(params: Bundle?) {
        _assistantState.value = AssistantState.LISTENING
    }

    override fun onBeginningOfSpeech() {
        _assistantState.value = AssistantState.LISTENING
    }

    override fun onRmsChanged(rmsdB: Float) {
        if (_assistantState.value == AssistantState.LISTENING) {
            // Normalize RMS roughly between 0.0 and 1.0 for the HUD visualizer
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            _audioRms.value = normalized
        }
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _assistantState.value = AssistantState.THINKING
        _audioRms.value = 0f
    }

    override fun onError(error: Int) {
        _audioRms.value = 0f
        _assistantState.value = AssistantState.READY

        val errorDesc = when (error) {
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Standing by."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Microphone timed out. Standing by."
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Check microphone."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "RECORD_AUDIO permission required."
            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network audio stream glitch. Text input ready."
            else -> "Speech recognition standby (Code $error)."
        }
        Log.w(TAG, "SpeechRecognizer Error: $errorDesc ($error)")
        if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
            onErrorOccurred?.invoke(errorDesc)
        }
    }

    override fun onResults(results: Bundle?) {
        _audioRms.value = 0f
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val spokenText = matches[0].trim()
            val detected = LanguageDetector.detectLanguage(spokenText, _voiceSettings.value.languageMode)
            _assistantState.value = AssistantState.THINKING
            onSpeechRecognized?.invoke(spokenText, detected)
        } else {
            _assistantState.value = AssistantState.READY
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {}

    override fun onEvent(eventType: Int, params: Bundle?) {}

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up SpeechManager: ${e.message}")
        }
    }
}
