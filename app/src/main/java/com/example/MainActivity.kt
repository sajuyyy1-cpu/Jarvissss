package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.JarvisForegroundService
import com.example.ui.MainViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*

data class NavTabItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start background assistant service
        try {
            JarvisForegroundService.startService(this)
        } catch (e: Exception) {
            // Handle background start restriction gracefully
        }

        setContent {
            MyApplicationTheme {
                val currentTab by viewModel.selectedTab.collectAsStateWithLifecycle()
                val assistantState by viewModel.assistantState.collectAsStateWithLifecycle()
                val audioRms by viewModel.audioRms.collectAsStateWithLifecycle()
                val systemLog by viewModel.systemStatusLog.collectAsStateWithLifecycle()
                val voiceSettings by viewModel.voiceSettings.collectAsStateWithLifecycle()

                val memories by viewModel.allMemories.collectAsStateWithLifecycle()
                val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
                val orders by viewModel.allOrders.collectAsStateWithLifecycle()
                val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
                val notes by viewModel.allNotes.collectAsStateWithLifecycle()
                val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()

                // Permission Request Launcher for RECORD_AUDIO & Phone/Notifications
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
                    if (audioGranted) {
                        viewModel.startListening()
                    }
                }

                // Handle back press to return to HOME if on sub-tab
                BackHandler(enabled = currentTab != 0) {
                    viewModel.setSelectedTab(0)
                }

                val navItems = listOf(
                    NavTabItem("HOME", Icons.Default.Home, "nav_home"),
                    NavTabItem("CHAT", Icons.Default.Chat, "nav_chat"),
                    NavTabItem("MEMORY", Icons.Default.Storage, "nav_memory"),
                    NavTabItem("TASKS", Icons.Default.Checklist, "nav_tasks"),
                    NavTabItem("TOOLS", Icons.Default.Build, "nav_tools"),
                    NavTabItem("MUD ART", Icons.Default.Brush, "nav_mud_art"),
                    NavTabItem("SETTINGS", Icons.Default.Settings, "nav_settings")
                )

                fun handleMicClick() {
                    val hasAudioPermission = ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasAudioPermission) {
                        if (assistantState == com.example.voice.AssistantState.LISTENING) {
                            viewModel.stopListening()
                        } else {
                            viewModel.startListening()
                        }
                    } else {
                        val permsToRequest = mutableListOf(Manifest.permission.RECORD_AUDIO)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        permissionLauncher.launch(permsToRequest.toTypedArray())
                    }
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(JarvisBackground),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        NavigationBar(
                            containerColor = JarvisSurface,
                            contentColor = JarvisCyan,
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("jarvis_bottom_nav")
                        ) {
                            navItems.forEachIndexed { index, item ->
                                val selected = currentTab == index
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { viewModel.setSelectedTab(index) },
                                    icon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.title,
                                            tint = if (selected) JarvisCyan else JarvisTextMuted,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.title,
                                            color = if (selected) JarvisCyan else JarvisTextMuted,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            maxLines = 1
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = JarvisSurfaceElevated
                                    ),
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(JarvisBackground)
                    ) {
                        when (currentTab) {
                            0 -> HomeScreen(
                                assistantState = assistantState,
                                audioRms = audioRms,
                                systemLog = systemLog,
                                memories = memories,
                                tasks = tasks,
                                onMicClick = { handleMicClick() },
                                onEmergencyStop = { viewModel.emergencyStop() },
                                onQuickCommand = { cmd -> viewModel.sendUserMessage(cmd, isVoice = false) },
                                onNavigateTab = { tab -> viewModel.setSelectedTab(tab) },
                                onStateSelect = { state -> viewModel.setAssistantState(state) }
                            )
                            1 -> ChatScreen(
                                messages = chatMessages,
                                assistantState = assistantState,
                                audioRms = audioRms,
                                currentLanguageMode = voiceSettings.languageMode,
                                onLanguageChange = { mode ->
                                    viewModel.updateVoiceSettings(voiceSettings.copy(languageMode = mode))
                                },
                                onSendMessage = { text -> viewModel.sendUserMessage(text, isVoice = false) },
                                onMicClick = { handleMicClick() },
                                onEmergencyStop = { viewModel.emergencyStop() },
                                onReplayMessage = { msg -> viewModel.replayMessage(msg) }
                            )
                            2 -> MemoryScreen(
                                memories = memories,
                                onAddMemory = { title, content, cat, tags ->
                                    viewModel.addMemory(title, content, cat, tags)
                                },
                                onDeleteMemory = { mem -> viewModel.deleteMemory(mem) },
                                onClearAll = { viewModel.clearAllMemories() }
                            )
                            3 -> TasksScreen(
                                tasks = tasks,
                                onAddTask = { title, desc, prio, due ->
                                    viewModel.addTask(title, desc, prio, due)
                                },
                                onToggleTask = { id, comp -> viewModel.toggleTask(id, comp) },
                                onDeleteTask = { task -> viewModel.deleteTask(task) }
                            )
                            4 -> ToolsScreen(
                                onSimulateIncomingCall = { name -> viewModel.handleIncomingCall(name) },
                                onSimulateWhatsApp = { sender, msg -> viewModel.handleIncomingWhatsApp(sender, msg) }
                            )
                            5 -> MudArtScreen(
                                orders = orders,
                                customers = customers,
                                notes = notes,
                                onAddOrder = { cust, title, dim, amt, status, date, orderNotes ->
                                    viewModel.addMudArtOrder(cust, title, dim, amt, status, date, orderNotes)
                                },
                                onUpdateOrderStatus = { id, status -> viewModel.updateOrderStatus(id, status) },
                                onDeleteOrder = { order -> viewModel.deleteMudArtOrder(order) },
                                onAddCustomer = { name, phone, email, city, custNotes ->
                                    viewModel.addMudArtCustomer(name, phone, email, city, custNotes)
                                },
                                onDeleteCustomer = { cust -> viewModel.deleteMudArtCustomer(cust) },
                                onAddNote = { title, content, cat -> viewModel.addMudArtNote(title, content, cat) },
                                onDeleteNote = { note -> viewModel.deleteMudArtNote(note) }
                            )
                            6 -> SettingsScreen(
                                voiceSettings = voiceSettings,
                                onUpdateVoiceSettings = { s -> viewModel.updateVoiceSettings(s) },
                                onTestVoice = {
                                    val testGreeting = when (voiceSettings.languageMode) {
                                        com.example.voice.LanguageMode.HINDI -> "नमस्ते सज्जाद जी, जार्विस सिस्टम सक्रिय है और आपकी सेवा के लिए तत्पर है।"
                                        com.example.voice.LanguageMode.HINGLISH -> "Sab badhiya hai Sajjad bhai, JARVIS vocal system ekdam perfect kaam kar raha hai!"
                                        else -> "Vocal synthesis test sequence complete, Sajjad. All auditory frequencies calibrated."
                                    }
                                    viewModel.speechManager.speak(testGreeting, voiceSettings.languageMode)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
