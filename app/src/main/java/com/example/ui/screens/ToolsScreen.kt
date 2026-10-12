package com.example.ui.screens

import android.content.Context
import android.hardware.camera2.CameraManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.actions.AndroidActionManager
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

@Composable
fun ToolsScreen(
    onSimulateIncomingCall: (String) -> Unit,
    onSimulateWhatsApp: (String, String) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var isDiagnosticRunning by remember { mutableStateOf(false) }
    var diagnosticProgress by remember { mutableFloatStateOf(0f) }
    var diagnosticReport by remember { mutableStateOf("All subsystems operational.") }

    var isTorchOn by remember { mutableStateOf(false) }
    var frequencyHz by remember { mutableFloatStateOf(440f) }

    val infiniteTransition = rememberInfiniteTransition(label = "oscilloscope")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "osc_wave"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("tools_screen")
    ) {
        // Header
        Text(
            text = "SYSTEM TOOLS & SENSORS",
            color = JarvisCyan,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Text(
            text = "Diagnostics, Telemetry & Android Phone Integrations",
            color = JarvisTextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(16.dp))

        // SYSTEM SELF-DIAGNOSTIC PROTOCOL
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, JarvisBorder), RoundedCornerShape(12.dp)),
            color = JarvisSurface
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DIAGNOSTIC SELF-TEST",
                            color = JarvisTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = diagnosticReport,
                            color = if (isDiagnosticRunning) JarvisNeonAmber else JarvisNeonGreen,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Button(
                        onClick = {
                            if (!isDiagnosticRunning) {
                                isDiagnosticRunning = true
                                diagnosticProgress = 0f
                                coroutineScope.launch {
                                    diagnosticReport = "Testing audio receptors..."
                                    delay(400)
                                    diagnosticProgress = 0.25f
                                    diagnosticReport = "Verifying neural memory cache..."
                                    delay(400)
                                    diagnosticProgress = 0.50f
                                    diagnosticReport = "Scanning Android telephony & intents..."
                                    delay(400)
                                    diagnosticProgress = 0.75f
                                    diagnosticReport = "Calibrating Arc Reactor..."
                                    delay(400)
                                    diagnosticProgress = 1.0f
                                    diagnosticReport = "Self-test complete. 100% OPERATIONAL."
                                    isDiagnosticRunning = false
                                }
                            }
                        },
                        enabled = !isDiagnosticRunning,
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isDiagnosticRunning) "TESTING..." else "RUN TEST",
                            color = JarvisBackground,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                if (isDiagnosticRunning || diagnosticProgress > 0f) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { diagnosticProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = JarvisCyan,
                        trackColor = JarvisSurfaceElevated
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ANNOUNCEMENT SIMULATORS & TEST
        Text(
            text = "// ANNOUNCEMENT PROTOCOL TEST",
            color = JarvisCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, JarvisBorder), RoundedCornerShape(12.dp)),
            color = JarvisSurface
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Verify caller announcements & WhatsApp vocal alerts addressing Sajjad (सज्जाद जी):",
                    color = JarvisTextSecondary,
                    fontSize = 12.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onSimulateIncomingCall("Rahul Sharma") },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceElevated),
                        border = BorderStroke(1.dp, JarvisCyan),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Call", color = JarvisTextPrimary, fontSize = 11.sp)
                    }

                    Button(
                        onClick = { onSimulateWhatsApp("Vikram", "Order confirmation for Peacock Lippan Art panel.") },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceElevated),
                        border = BorderStroke(1.dp, JarvisNeonGreen),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = JarvisNeonGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test WhatsApp", color = JarvisTextPrimary, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // HARDWARE OSCILLOSCOPE & FREQUENCY SCANNER
        Text(
            text = "// FREQUENCY OSCILLOSCOPE",
            color = JarvisCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, JarvisBorder), RoundedCornerShape(12.dp)),
            color = JarvisSurface
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HARMONIC FREQUENCY: ${frequencyHz.toInt()} Hz",
                        color = JarvisCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "RESONANCE: LOCK",
                        color = JarvisNeonGreen,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Canvas Oscilloscope Wave
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisBackground)
                        .border(BorderStroke(0.6.dp, JarvisBorder), RoundedCornerShape(8.dp))
                ) {
                    val w = size.width
                    val h = size.height
                    val midY = h / 2f
                    val waveFreq = frequencyHz / 100f

                    for (x in 0 until w.toInt() step 2) {
                        val rad = Math.toRadians((x * waveFreq + waveOffset).toDouble())
                        val y = midY + (sin(rad).toFloat() * (h * 0.35f))

                        drawCircle(
                            color = JarvisCyan,
                            radius = 1.2f,
                            center = Offset(x.toFloat(), y)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = frequencyHz,
                    onValueChange = { frequencyHz = it },
                    valueRange = 100f..1000f,
                    colors = SliderDefaults.colors(
                        thumbColor = JarvisCyan,
                        activeTrackColor = JarvisCyan,
                        inactiveTrackColor = JarvisSurfaceElevated
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // QUICK ANDROID ACTIONS
        Text(
            text = "// REAL ANDROID ACTIONS",
            color = JarvisCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionLauncherButton(
                title = "YouTube",
                icon = Icons.Default.SmartDisplay,
                onClick = { AndroidActionManager.openYouTube(context) },
                modifier = Modifier.weight(1f)
            )
            ActionLauncherButton(
                title = "Instagram",
                icon = Icons.Default.CameraAlt,
                onClick = { AndroidActionManager.openInstagram(context) },
                modifier = Modifier.weight(1f)
            )
            ActionLauncherButton(
                title = "WhatsApp",
                icon = Icons.Default.Chat,
                onClick = { AndroidActionManager.openWhatsApp(context) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionLauncherButton(
                title = "Maps",
                icon = Icons.Default.Navigation,
                onClick = { AndroidActionManager.openMapsNavigation(context, "Home") },
                modifier = Modifier.weight(1f)
            )
            ActionLauncherButton(
                title = "Camera",
                icon = Icons.Default.PhotoCamera,
                onClick = { AndroidActionManager.openCamera(context) },
                modifier = Modifier.weight(1f)
            )
            ActionLauncherButton(
                title = "Settings",
                icon = Icons.Default.Settings,
                onClick = { AndroidActionManager.openSystemSettings(context) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ActionLauncherButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(BorderStroke(0.8.dp, JarvisBorder), RoundedCornerShape(8.dp)),
        color = JarvisSurface
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, color = JarvisTextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
    }
}
