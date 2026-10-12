package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.JarvisBrain
import com.example.data.db.JarvisDatabase
import com.example.data.model.*
import com.example.data.repository.JarvisRepository
import com.example.service.JarvisNotificationListenerService
import com.example.service.PhoneCallReceiver
import com.example.voice.AssistantState
import com.example.voice.JarvisSpeechManager
import com.example.voice.LanguageMode
import com.example.voice.VoiceSettings
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: JarvisRepository
    val speechManager: JarvisSpeechManager
    val brain: JarvisBrain

    val assistantState: StateFlow<AssistantState>
    val audioRms: StateFlow<Float>
    val voiceSettings: StateFlow<VoiceSettings>

    val allMemories: StateFlow<List<MemoryEntity>>
    val allTasks: StateFlow<List<TaskEntity>>
    val allOrders: StateFlow<List<MudArtOrderEntity>>
    val allCustomers: StateFlow<List<MudArtCustomerEntity>>
    val allNotes: StateFlow<List<MudArtNoteEntity>>
    val chatMessages: StateFlow<List<ChatMessageEntity>>

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _systemStatusLog = MutableStateFlow("JARVIS CORE V3.4.0 ONLINE // AUDIO RECEPTORS STANDBY")
    val systemStatusLog: StateFlow<String> = _systemStatusLog.asStateFlow()

    init {
        val db = JarvisDatabase.getInstance(application)
        repository = JarvisRepository(db)
        speechManager = JarvisSpeechManager(application)
        brain = JarvisBrain(application, repository)

        assistantState = speechManager.assistantState
        audioRms = speechManager.audioRms
        voiceSettings = speechManager.voiceSettings

        allMemories = repository.getAllMemories().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allTasks = repository.getAllTasks().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allOrders = repository.getAllOrders().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allCustomers = repository.getAllCustomers().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allNotes = repository.getAllNotes().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        chatMessages = repository.getAllMessages().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

        // Connect Speech Recognizer Callbacks
        speechManager.onSpeechRecognized = { text, lang ->
            sendUserMessage(text, isVoice = true, languageMode = lang)
        }

        speechManager.onErrorOccurred = { errorMsg ->
            _systemStatusLog.value = "ALERT: $errorMsg"
        }

        // Connect Background Notification Listener Callback
        JarvisNotificationListenerService.listenerCallback = { sender, message ->
            handleIncomingWhatsApp(sender, message)
        }

        // Connect Phone Call Receiver Callback
        PhoneCallReceiver.callListenerCallback = { callerName ->
            handleIncomingCall(callerName)
        }
    }

    fun setSelectedTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun setAssistantState(state: AssistantState) {
        speechManager.setState(state)
        _systemStatusLog.value = "STATE SWITCH: ${state.name} // CALIBRATION ACTIVE"
    }

    fun startListening() {
        _systemStatusLog.value = "LISTENING // AUDIO RECEPTORS SYNCHRONIZED"
        speechManager.startListening()
    }

    fun stopListening() {
        speechManager.stopListening()
        _systemStatusLog.value = "READY // STANDBY"
    }

    fun emergencyStop() {
        speechManager.emergencyStop()
        _systemStatusLog.value = "EMERGENCY STOP // ALL VOCAL OUTPUTS MUTED"
    }

    fun sendUserMessage(
        text: String,
        isVoice: Boolean = false,
        languageMode: LanguageMode = voiceSettings.value.languageMode
    ) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            // Save User Message
            repository.insertMessage(
                ChatMessageEntity(
                    sender = "USER",
                    text = trimmed,
                    language = languageMode.code,
                    isVoice = isVoice
                )
            )

            _systemStatusLog.value = "PROCESSING INTENT: \"$trimmed\""

            // Process with JARVIS Brain
            val response = brain.processUserPrompt(trimmed, languageMode)

            // Save JARVIS Response
            repository.insertMessage(
                ChatMessageEntity(
                    sender = "JARVIS",
                    text = response.replyText,
                    language = response.languageMode.code,
                    isVoice = isVoice
                )
            )

            _systemStatusLog.value = "DIRECTIVE EXECUTED // RESPONSE GENERATED"

            // Speak response aloud (stops speech recognition while speaking to prevent self-hearing!)
            speechManager.speak(response.spokenText, response.languageMode)
        }
    }

    fun replayMessage(message: ChatMessageEntity) {
        val lang = when (message.language) {
            "hi" -> LanguageMode.HINDI
            "hinglish" -> LanguageMode.HINGLISH
            else -> LanguageMode.ENGLISH
        }
        speechManager.speak(message.text, lang)
    }

    fun handleIncomingCall(callerName: String?) {
        val settings = voiceSettings.value
        if (!settings.callAnnouncementEnabled || settings.quietHoursEnabled) return

        val announcement = brain.generateIncomingCallAnnouncement(callerName, settings.languageMode)
        _systemStatusLog.value = "CALL DETECTED: $announcement"

        viewModelScope.launch {
            repository.insertMessage(
                ChatMessageEntity(
                    sender = "JARVIS",
                    text = "Incoming Call: $announcement",
                    language = settings.languageMode.code,
                    isVoice = true
                )
            )
            speechManager.speak(announcement, settings.languageMode)
        }
    }

    fun handleIncomingWhatsApp(sender: String, message: String) {
        val settings = voiceSettings.value
        if (!settings.whatsappAnnouncementEnabled || settings.quietHoursEnabled) return

        val announcement = brain.generateWhatsAppAnnouncement(
            sender, message, settings.readWhatsAppContent, settings.languageMode
        )
        _systemStatusLog.value = "WHATSAPP DETECTED: from $sender"

        viewModelScope.launch {
            repository.insertMessage(
                ChatMessageEntity(
                    sender = "JARVIS",
                    text = "WhatsApp: $announcement",
                    language = settings.languageMode.code,
                    isVoice = true
                )
            )
            speechManager.speak(announcement, settings.languageMode)
        }
    }

    // Memory operations
    fun addMemory(title: String, content: String, category: String, tags: String = "") {
        viewModelScope.launch {
            repository.insertMemory(
                MemoryEntity(
                    title = title,
                    content = content,
                    category = category,
                    tags = tags
                )
            )
            _systemStatusLog.value = "MEMORY ARCHIVED: \"$title\""
        }
    }

    fun deleteMemory(memory: MemoryEntity) {
        viewModelScope.launch {
            repository.deleteMemory(memory)
            _systemStatusLog.value = "MEMORY PURGED: \"${memory.title}\""
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.clearAllMemories()
            _systemStatusLog.value = "ALL MEMORY BANKS CLEARED"
        }
    }

    // Task operations
    fun addTask(title: String, description: String, priority: String, dueDate: String) {
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    title = title,
                    description = description,
                    priority = priority,
                    dueDate = dueDate
                )
            )
            _systemStatusLog.value = "DIRECTIVE LOGGED: \"$title\""
        }
    }

    fun toggleTask(id: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.setTaskCompleted(id, isCompleted)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // Mud Art Business operations
    fun addMudArtOrder(
        customerName: String,
        artPieceTitle: String,
        dimensions: String,
        amount: Double,
        status: String,
        orderDate: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.insertOrder(
                MudArtOrderEntity(
                    customerName = customerName,
                    artPieceTitle = artPieceTitle,
                    dimensions = dimensions,
                    amount = amount,
                    status = status,
                    orderDate = orderDate,
                    notes = notes
                )
            )
            _systemStatusLog.value = "MUD ART ORDER LOGGED: \"$artPieceTitle\""
        }
    }

    fun updateOrderStatus(id: Long, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(id, status)
            _systemStatusLog.value = "ORDER STATUS UPDATED -> $status"
        }
    }

    fun deleteMudArtOrder(order: MudArtOrderEntity) {
        viewModelScope.launch {
            repository.deleteOrder(order)
        }
    }

    fun addMudArtCustomer(name: String, phone: String, email: String, city: String, notes: String) {
        viewModelScope.launch {
            repository.insertCustomer(
                MudArtCustomerEntity(
                    name = name,
                    phone = phone,
                    email = email,
                    city = city,
                    notes = notes
                )
            )
            _systemStatusLog.value = "MUD ART CLIENT RECORDED: \"$name\""
        }
    }

    fun deleteMudArtCustomer(customer: MudArtCustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    fun addMudArtNote(title: String, content: String, category: String) {
        viewModelScope.launch {
            repository.insertNote(
                MudArtNoteEntity(
                    title = title,
                    content = content,
                    category = category
                )
            )
        }
    }

    fun deleteMudArtNote(note: MudArtNoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun updateVoiceSettings(settings: VoiceSettings) {
        speechManager.updateSettings(settings)
    }

    override fun onCleared() {
        speechManager.destroy()
        super.onCleared()
    }
}
