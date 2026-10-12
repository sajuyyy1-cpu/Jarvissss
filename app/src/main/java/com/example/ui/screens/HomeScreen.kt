package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MemoryEntity
import com.example.data.model.TaskEntity
import com.example.ui.avatar.CyberneticAvatarCanvas
import com.example.ui.components.FuturisticJarvisHud
import com.example.ui.theme.*
import com.example.voice.AssistantState

@Composable
fun HomeScreen(
    assistantState: AssistantState,
    audioRms: Float,
    systemLog: String,
    memories: List<MemoryEntity>,
    tasks: List<TaskEntity>,
    onMicClick: () -> Unit,
    onEmergencyStop: () -> Unit,
    onQuickCommand: (String) -> Unit,
    onNavigateTab: (Int) -> Unit,
    onStateSelect: ((AssistantState) -> Unit)? = null
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("home_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP HUD BAR: Prominent "WELCOME, SAJAD"
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, JarvisBorder), RoundedCornerShape(12.dp)),
            color = JarvisSurface
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(JarvisNeonGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SYS ONLINE // CORES ENERGIZED",
                            color = JarvisNeonGreen,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = JarvisSurfaceElevated,
                        border = BorderStroke(0.8.dp, JarvisCyan)
                    ) {
                        Text(
                            text = "सज्जाद",
                            color = JarvisCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "WELCOME, SAJAD",
                    color = JarvisCyan,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("welcome_sajad_header")
                )

                Text(
                    text = "J.A.R.V.I.S. QUANTUM NEURAL ASSISTANT",
                    color = JarvisTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // BASE FUTURISTIC BLACK-AND-BLUE HUD WITH STATES (Ready, Listening, Thinking, Speaking)
        FuturisticJarvisHud(
            assistantState = assistantState,
            audioRms = audioRms,
            onStateSelect = onStateSelect,
            onMicClick = onMicClick,
            onEmergencyStop = onEmergencyStop,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // SYSTEM STATUS LIVE LOG
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(BorderStroke(0.8.dp, JarvisBorder), RoundedCornerShape(8.dp)),
            color = JarvisSurfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = JarvisCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = systemLog,
                    color = JarvisTextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // BIG GLOWING MICROPHONE BUTTON WITH HUD RING
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emergency Stop Button
            AnimatedVisibility(visible = assistantState == AssistantState.SPEAKING || assistantState == AssistantState.LISTENING) {
                IconButton(
                    onClick = onEmergencyStop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(JarvisLaserRed.copy(alpha = 0.2f))
                        .border(BorderStroke(1.dp, JarvisLaserRed), CircleShape)
                        .testTag("emergency_stop_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop JARVIS Speech",
                        tint = JarvisLaserRed
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Main Microphone Button
            val isListening = assistantState == AssistantState.LISTENING
            val micColor = if (isListening) JarvisNeonGreen else JarvisCyan

            Surface(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .shadow(elevation = 16.dp, shape = CircleShape, spotColor = micColor)
                    .border(
                        BorderStroke(if (isListening) 3.dp else 1.5.dp, micColor),
                        CircleShape
                    )
                    .clickable { onMicClick() }
                    .testTag("microphone_button"),
                color = JarvisSurfaceElevated
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Activate Voice Assistant",
                        tint = micColor,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Quick Chat Button
            IconButton(
                onClick = { onNavigateTab(1) },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(JarvisSurfaceElevated)
                    .border(BorderStroke(1.dp, JarvisBorder), CircleShape)
                    .testTag("chat_shortcut_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Open Chat HUD",
                    tint = JarvisCyan
                )
            }
        }

        Text(
            text = if (assistantState == AssistantState.LISTENING)
                "TAP TO TRANSMIT DIRECTIVE"
            else
                "TAP MICROPHONE TO SPEAK (EN / HI / HINGLISH)",
            color = JarvisTextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 10.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // HUD TELEMETRY METRICS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TelemetryCard(
                title = "CORE TEMP",
                value = "38.2°C",
                status = "OPTIMAL",
                statusColor = JarvisNeonGreen,
                icon = Icons.Default.Thermostat,
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                title = "REACTOR FREQ",
                value = "98.7%",
                status = "RESONANT",
                statusColor = JarvisCyan,
                icon = Icons.Default.Bolt,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TelemetryCard(
                title = "MEMORY VAULT",
                value = "${memories.size} ENTRIES",
                status = "ENCRYPTED",
                statusColor = JarvisPurple,
                icon = Icons.Default.Storage,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateTab(2) }
            )
            TelemetryCard(
                title = "DIRECTIVES",
                value = "${tasks.count { !it.isCompleted }} PENDING",
                status = "MONITORED",
                statusColor = JarvisNeonAmber,
                icon = Icons.Default.Checklist,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateTab(3) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // QUICK ACTION CHIPS
        Text(
            text = "// QUICK COMMAND DIRECTIVES",
            color = JarvisCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        val quickCommands = listOf(
            "YouTube par cricket search karo",
            "Instagram kholo",
            "WhatsApp kholo",
            "Majikhan Mud Art orders",
            "Bhai, kaise ho?",
            "Aaj ke tasks batao"
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickCommands.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { cmd ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(BorderStroke(0.8.dp, JarvisBorder), RoundedCornerShape(8.dp))
                                .clickable { onQuickCommand(cmd) },
                            color = JarvisSurface
                        ) {
                            Text(
                                text = "► $cmd",
                                color = JarvisTextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun TelemetryCard(
    title: String,
    value: String,
    status: String,
    statusColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(BorderStroke(1.dp, JarvisBorder), RoundedCornerShape(10.dp)),
        color = JarvisSurface
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = JarvisTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = JarvisTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "● $status",
                color = statusColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
