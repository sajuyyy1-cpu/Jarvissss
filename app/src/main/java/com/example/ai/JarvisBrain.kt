package com.example.ai

import android.content.Context
import com.example.actions.ActionResult
import com.example.actions.AndroidActionManager
import com.example.data.model.MemoryEntity
import com.example.data.model.TaskEntity
import com.example.data.repository.JarvisRepository
import com.example.voice.LanguageDetector
import com.example.voice.LanguageMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class JarvisResponse(
    val replyText: String,
    val spokenText: String,
    val languageMode: LanguageMode,
    val actionResult: ActionResult? = null,
    val newlyAddedTask: TaskEntity? = null,
    val newlyAddedMemory: MemoryEntity? = null
)

class JarvisBrain(
    private val context: Context,
    private val repository: JarvisRepository
) {

    suspend fun processUserPrompt(
        prompt: String,
        preferredLanguage: LanguageMode = LanguageMode.AUTO
    ): JarvisResponse = withContext(Dispatchers.IO) {
        val trimmed = prompt.trim()
        val detectedLang = LanguageDetector.detectLanguage(trimmed, preferredLanguage)
        val lower = trimmed.lowercase()

        // 1. Emergency stop check
        if (lower.contains("stop") || lower.contains("chup") || lower.contains("ruk jao") || lower.contains("shant")) {
            return@withContext when (detectedLang) {
                LanguageMode.HINDI -> JarvisResponse(
                    replyText = "ऑडियो आउटपुट रोक दिया गया है, सज्जाद जी।",
                    spokenText = "ऑडियो आउटपुट रोक दिया गया है, सज्जाद जी।",
                    languageMode = LanguageMode.HINDI
                )
                LanguageMode.HINGLISH -> JarvisResponse(
                    replyText = "Stopping audio output immediately, Sajjad bhai.",
                    spokenText = "Stopping audio output immediately, Sajjad bhai.",
                    languageMode = LanguageMode.HINGLISH
                )
                else -> JarvisResponse(
                    replyText = "All vocal outputs ceased, Sajjad.",
                    spokenText = "All vocal outputs ceased, Sajjad.",
                    languageMode = LanguageMode.ENGLISH
                )
            }
        }

        // 2. Real Phone App Actions & Intent Recognitions
        // A. YouTube
        if (lower.contains("youtube")) {
            val query = extractYouTubeQuery(trimmed)
            val action = AndroidActionManager.openYouTube(context, query)
            return@withContext formatActionResponse(action, detectedLang)
        }

        // B. Instagram
        if (lower.contains("instagram") || lower.contains("insta")) {
            val action = AndroidActionManager.openInstagram(context)
            return@withContext formatActionResponse(action, detectedLang)
        }

        // C. WhatsApp
        if (lower.contains("whatsapp") || lower.contains("watsapp")) {
            val action = AndroidActionManager.openWhatsApp(context)
            return@withContext formatActionResponse(action, detectedLang)
        }

        // D. Maps / Navigation
        if (lower.contains("rasta") || lower.contains("navigation") || lower.contains("maps") || lower.contains("map")) {
            val dest = extractDestination(trimmed)
            val action = AndroidActionManager.openMapsNavigation(context, dest)
            return@withContext formatActionResponse(action, detectedLang)
        }

        // E. Camera
        if (lower.contains("camera") || lower.contains("photo") || lower.contains("tasveer")) {
            val action = AndroidActionManager.openCamera(context)
            return@withContext formatActionResponse(action, detectedLang)
        }

        // F. Settings
        if (lower.contains("settings") || lower.contains("setting")) {
            val action = AndroidActionManager.openSystemSettings(context)
            return@withContext formatActionResponse(action, detectedLang)
        }

        // G. Web Search
        if (lower.startsWith("search") || lower.contains("google") || lower.contains("khojo") || lower.contains("dhoondo")) {
            val cleanQuery = trimmed.replace(Regex("(?i)^(search|google|khojo|dhoondo)\\s+(for|par)?\\s*"), "")
            val action = AndroidActionManager.searchWeb(context, if (cleanQuery.isNotBlank()) cleanQuery else trimmed)
            return@withContext formatActionResponse(action, detectedLang)
        }

        // 3. Memory storage intent ("remember that...", "yaad rakhna ki...")
        if (lower.contains("remember") || lower.contains("yaad rakh") || lower.contains("yaad rakho")) {
            val contentToRemember = extractMemoryContent(trimmed)
            val newMemory = MemoryEntity(
                title = "Note from Conversation",
                content = contentToRemember,
                category = "NOTE",
                tags = "user_prompt"
            )
            repository.insertMemory(newMemory)
            return@withContext when (detectedLang) {
                LanguageMode.HINDI -> JarvisResponse(
                    replyText = "सज्जाद जी, मैंने यह जानकारी अपनी मेमोरी में सुरक्षित कर ली है: \"$contentToRemember\"",
                    spokenText = "सज्जाद जी, मैंने यह जानकारी सुरक्षित कर ली है।",
                    languageMode = LanguageMode.HINDI,
                    newlyAddedMemory = newMemory
                )
                LanguageMode.HINGLISH -> JarvisResponse(
                    replyText = "Bilkul Sajjad bhai, memory bank mein save kar liya: \"$contentToRemember\"",
                    spokenText = "Bilkul Sajjad bhai, save kar liya.",
                    languageMode = LanguageMode.HINGLISH,
                    newlyAddedMemory = newMemory
                )
                else -> JarvisResponse(
                    replyText = "Memory bank updated, Sajjad: \"$contentToRemember\"",
                    spokenText = "Memory bank updated, Sajjad.",
                    languageMode = LanguageMode.ENGLISH,
                    newlyAddedMemory = newMemory
                )
            }
        }

        // 4. Task creation intent ("task add karo...", "remind me to...")
        if (lower.contains("task") || lower.contains("remind") || lower.contains("yaad dilana")) {
            val taskTitle = extractTaskTitle(trimmed)
            val newTask = TaskEntity(
                title = taskTitle,
                description = "Created via JARVIS vocal command",
                priority = if (lower.contains("urgent") || lower.contains("zaroori")) "HIGH" else "MEDIUM",
                dueDate = "Today"
            )
            repository.insertTask(newTask)
            return@withContext when (detectedLang) {
                LanguageMode.HINDI -> JarvisResponse(
                    replyText = "सज्जाद जी, नया टास्क जोड़ दिया गया है: \"$taskTitle\"",
                    spokenText = "सज्जाद जी, नया टास्क जोड़ दिया गया है।",
                    languageMode = LanguageMode.HINDI,
                    newlyAddedTask = newTask
                )
                LanguageMode.HINGLISH -> JarvisResponse(
                    replyText = "Sajjad bhai, task add ho gaya hai: \"$taskTitle\"",
                    spokenText = "Sajjad bhai, task add ho gaya hai.",
                    languageMode = LanguageMode.HINGLISH,
                    newlyAddedTask = newTask
                )
                else -> JarvisResponse(
                    replyText = "Directives protocol updated. Task added: \"$taskTitle\"",
                    spokenText = "Task added to directives, Sajjad.",
                    languageMode = LanguageMode.ENGLISH,
                    newlyAddedTask = newTask
                )
            }
        }

        // 5. Check Memories for Contextual Answers
        val allMemories = repository.getAllMemoriesList()
        val relevantMemory = allMemories.find { memory ->
            memory.title.lowercase().split(" ").any { lower.contains(it) && it.length > 3 } ||
            memory.content.lowercase().split(" ").any { lower.contains(it) && it.length > 3 }
        }

        // 6. Conversational / Assistant Q&A
        if (lower.contains("kaise ho") || lower.contains("how are you") || lower.contains("kya haal")) {
            return@withContext when (detectedLang) {
                LanguageMode.HINDI -> JarvisResponse(
                    replyText = "मैं बिल्कुल ठीक हूँ, सज्जाद जी। सभी जार्विस सिस्टम सामान्य गति से काम कर रहे हैं। बताइए, आज क्या सेवा करूँ?",
                    spokenText = "मैं बिल्कुल ठीक हूँ, सज्जाद जी। बताइए, आज क्या सेवा करूँ?",
                    languageMode = LanguageMode.HINDI
                )
                LanguageMode.HINGLISH -> JarvisResponse(
                    replyText = "Main 100% operational hoon, Sajjad bhai! Arc Reactor aur AI Cores bilkul stable hain. Kahiye, aaj kya plan hai?",
                    spokenText = "Main 100% operational hoon, Sajjad bhai! Kahiye, aaj kya plan hai?",
                    languageMode = LanguageMode.HINGLISH
                )
                else -> JarvisResponse(
                    replyText = "I am operating at peak efficiency, Sajjad. All diagnostic subsystems are green. How may I be of assistance?",
                    spokenText = "I am operating at peak efficiency, Sajjad. How may I assist you?",
                    languageMode = LanguageMode.ENGLISH
                )
            }
        }

        if (lower.contains("mud art") || lower.contains("majikhan") || lower.contains("lippan")) {
            return@withContext when (detectedLang) {
                LanguageMode.HINDI -> JarvisResponse(
                    replyText = "सज्जाद जी, 'माजीखान मड आर्ट' बिज़नेस सेक्शन में ऑर्डर्स, कस्टमर्स और डिज़ाइन्स का रिकॉर्ड सुरक्षित है। आप MUD ART टैब में विवरण देख सकते हैं।",
                    spokenText = "सज्जाद जी, माजीखान मड आर्ट का पूरा रिकॉर्ड सुरक्षित है।",
                    languageMode = LanguageMode.HINDI
                )
                LanguageMode.HINGLISH -> JarvisResponse(
                    replyText = "Sajjad bhai, Majikhan Mud Art archive ready hai. Orders, customers aur Lippan art techniques Mud Art section mein safe hain.",
                    spokenText = "Sajjad bhai, Majikhan Mud Art archive ready hai.",
                    languageMode = LanguageMode.HINGLISH
                )
                else -> JarvisResponse(
                    replyText = "Majikhan Mud Art business records are securely archived on-device, Sajjad. You can manage orders and client records in the Mud Art section.",
                    spokenText = "Majikhan Mud Art records are up to date, Sajjad.",
                    languageMode = LanguageMode.ENGLISH
                )
            }
        }

        if (lower.contains("who are you") || lower.contains("tum kaun ho") || lower.contains("aap kaun")) {
            return@withContext when (detectedLang) {
                LanguageMode.HINDI -> JarvisResponse(
                    replyText = "मैं जार्विस (JARVIS) हूँ — सज्जाद जी का व्यक्तिगत एआई सहायक। मैं आपकी आवाज़, टास्क, मड आर्ट बिज़नेस और फोन एक्शन्स को प्रबंधित करने के लिए तैनात हूँ।",
                    spokenText = "मैं जार्विस हूँ, सज्जाद जी का व्यक्तिगत एआई सहायक।",
                    languageMode = LanguageMode.HINDI
                )
                LanguageMode.HINGLISH -> JarvisResponse(
                    replyText = "Main JARVIS hoon — Sajjad bhai ka personal AI assistant. Voice commands, tasks, phone intents aur Majikhan Mud Art manage karne ke liye hamesha ready!",
                    spokenText = "Main JARVIS hoon, Sajjad bhai ka personal AI assistant.",
                    languageMode = LanguageMode.HINGLISH
                )
                else -> JarvisResponse(
                    replyText = "I am JARVIS — Just A Rather Very Intelligent System, personal assistant to Sajjad. Standing by for your directives.",
                    spokenText = "I am JARVIS, personal assistant to Sajjad. Standing by.",
                    languageMode = LanguageMode.ENGLISH
                )
            }
        }

        // Context-aware fallback referencing memories if applicable
        val memoryContext = relevantMemory?.let {
            "\n\n[Retrieved Memory: ${it.title} - ${it.content}]"
        } ?: ""

        when (detectedLang) {
            LanguageMode.HINDI -> JarvisResponse(
                replyText = "सज्जाद जी, मैंने आपकी बात समझी: \"$trimmed\"। क्या आप चाहते हैं कि मैं इसके लिए कोई फोन एक्शन चलाऊँ या इसे नोट्स में लिख दूँ?$memoryContext",
                spokenText = "सज्जाद जी, मैंने आपकी बात समझी। क्या आप कोई एक्शन चलाना चाहते हैं?",
                languageMode = LanguageMode.HINDI
            )
            LanguageMode.HINGLISH -> JarvisResponse(
                replyText = "Samajh gaya Sajjad bhai: \"$trimmed\"। Kya iske liye koi Android app kholna hai ya task add karna hai?$memoryContext",
                spokenText = "Samajh gaya Sajjad bhai. Kahiye kya karna hai?",
                languageMode = LanguageMode.HINGLISH
            )
            else -> JarvisResponse(
                replyText = "Understood, Sajjad: \"$trimmed\". Would you like me to execute an Android action or archive this in your memory bank?$memoryContext",
                spokenText = "Understood, Sajjad. Standing by for further parameters.",
                languageMode = LanguageMode.ENGLISH
            )
        }
    }

    private fun formatActionResponse(action: ActionResult, lang: LanguageMode): JarvisResponse {
        val reply = when (lang) {
            LanguageMode.HINDI -> action.feedbackMessageHi
            LanguageMode.HINGLISH -> action.feedbackMessageHinglish
            else -> action.feedbackMessageEn
        }
        return JarvisResponse(
            replyText = reply,
            spokenText = reply,
            languageMode = lang,
            actionResult = action
        )
    }

    private fun extractYouTubeQuery(text: String): String? {
        val lower = text.lowercase()
        val regex = Regex("(?i)(youtube\\s+(par|pe|me|mein)?\\s*(search|kholo)?|search\\s+youtube\\s+for|open\\s+youtube\\s+and\\s+search)\\s*(.*)")
        val match = regex.find(text)
        val extracted = match?.groupValues?.lastOrNull()?.trim()
        if (!extracted.isNullOrBlank() && extracted != "kholo" && extracted != "open") {
            return extracted.replace(Regex("(?i)(search\\s+karo|karo|dekho)"), "").trim()
        }
        return null
    }

    private fun extractDestination(text: String): String {
        val cleaned = text.replace(Regex("(?i)(mujhe|ka|rasta|batao|bataiye|dikhao|navigate\\s+to|directions\\s+to|maps|map)"), "").trim()
        return if (cleaned.isNotBlank()) cleaned else "Home"
    }

    private fun extractMemoryContent(text: String): String {
        return text.replace(Regex("(?i)^(remember\\s+(that)?|yaad\\s+rakhna\\s+(ki)?|yaad\\s+rakho\\s*(ki)?)\\s*"), "").trim()
    }

    private fun extractTaskTitle(text: String): String {
        return text.replace(Regex("(?i)^(add\\s+task|task\\s+add\\s+karo|remind\\s+me\\s+to|yaad\\s+dilana\\s*(ki)?)\\s*"), "").trim()
            .ifEmpty { "New Directive" }
    }

    fun generateIncomingCallAnnouncement(callerName: String?, lang: LanguageMode): String {
        return when (lang) {
            LanguageMode.HINDI -> {
                if (!callerName.isNullOrBlank()) {
                    "सज्जाद जी, आपको $callerName कॉल कर रहा है।"
                } else {
                    "सज्जाद जी, आपको एक कॉल आ रहा है।"
                }
            }
            LanguageMode.HINGLISH -> {
                if (!callerName.isNullOrBlank()) {
                    "Sajjad bhai, aapko $callerName call kar raha hai."
                } else {
                    "Sajjad bhai, aapko ek call aa raha hai."
                }
            }
            else -> {
                if (!callerName.isNullOrBlank()) {
                    "Sajjad, you have an incoming call from $callerName."
                } else {
                    "Sajjad, you have an incoming phone call."
                }
            }
        }
    }

    fun generateWhatsAppAnnouncement(sender: String, message: String, readContent: Boolean, lang: LanguageMode): String {
        return when (lang) {
            LanguageMode.HINDI -> {
                if (readContent && message.isNotBlank()) {
                    "सज्जाद जी, WhatsApp पर $sender का संदेश आया है: $message"
                } else {
                    "सज्जाद जी, WhatsApp पर $sender का नया संदेश प्राप्त हुआ है।"
                }
            }
            LanguageMode.HINGLISH -> {
                if (readContent && message.isNotBlank()) {
                    "Sajjad bhai, WhatsApp par $sender ka message aaya hai: $message"
                } else {
                    "Sajjad bhai, WhatsApp par $sender ka naya notification hai."
                }
            }
            else -> {
                if (readContent && message.isNotBlank()) {
                    "Sajjad, WhatsApp message from $sender: $message"
                } else {
                    "Sajjad, you received a new WhatsApp message from $sender."
                }
            }
        }
    }
}
