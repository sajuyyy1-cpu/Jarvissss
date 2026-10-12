package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.actions.AndroidActionManager
import com.example.service.JarvisForegroundService
import com.example.service.JarvisNotificationListenerService
import com.example.ui.theme.*
import com.example.voice.LanguageMode
import com.example.voice.VoiceSettings

@Composable
fun SettingsScreen(
    voiceSettings: VoiceSettings,
    onUpdateVoiceSettings: (VoiceSettings) -> Unit,
    onTestVoice: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("settings_screen")
    ) {
        // Header
        Text(
            text = "JARVIS CONFIGURATION",
            color = JarvisCyan,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Text(
            text = "Personal Voice, Privacy & Background Protocols",
            color = JarvisTextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(16.dp))

        // OPERATOR PROFILE
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
                            text = "PRIMARY OPERATOR",
                            color = JarvisTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Sajjad (सज्जाद)",
                            color = JarvisCyan,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = JarvisSurfaceElevated,
                        border = BorderStroke(0.8.dp, JarvisCyan)
                    ) {
                        Text(
                            text = "AUTHORITY: LEVEL 10",
                            color = JarvisNeonGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Vocal announcements address operator as \"सज्जाद जी\" in Hindi and \"Sajjad\" in English.",
                    color = JarvisTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // LANGUAGE PROTOCOL
        Text(
            text = "// TRILINGUAL SPEECH MODE",
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
                LanguageMode.values().forEach { mode ->
                    val isSelected = voiceSettings.languageMode == mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = mode.displayName, color = JarvisTextPrimary, fontSize = 13.sp)
                            Text(
                                text = when (mode) {
                                    LanguageMode.AUTO -> "Detects English, Hindi (Devanagari) or Roman Hinglish"
                                    LanguageMode.HINDI -> "Replies in pure Hindi with Devanagari pronunciation"
                                    LanguageMode.HINGLISH -> "Conversational Hindi written in Roman script"
                                    LanguageMode.ENGLISH -> "Default JARVIS British / international cadence"
                                },
                                color = JarvisTextMuted,
                                fontSize = 10.sp
                            )
                        }
                        RadioButton(
                            selected = isSelected,
                            onClick = { onUpdateVoiceSettings(voiceSettings.copy(languageMode = mode)) },
                            colors = RadioButtonDefaults.colors(selectedColor = JarvisCyan, unselectedColor = JarvisBorder)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SPEECH SYNTHESIZER CALIBRATION
        Text(
            text = "// VOICE SYNTHESIZER CALIBRATION",
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
                // Pitch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Voice Pitch", color = JarvisTextPrimary, fontSize = 12.sp)
                    Text("${"%.2f".format(voiceSettings.pitch)}x", color = JarvisCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
                Slider(
                    value = voiceSettings.pitch,
                    onValueChange = { onUpdateVoiceSettings(voiceSettings.copy(pitch = it)) },
                    valueRange = 0.7f..1.3f,
                    colors = SliderDefaults.colors(thumbColor = JarvisCyan, activeTrackColor = JarvisCyan)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Rate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Speech Rate", color = JarvisTextPrimary, fontSize = 12.sp)
                    Text("${"%.2f".format(voiceSettings.speechRate)}x", color = JarvisCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
                Slider(
                    value = voiceSettings.speechRate,
                    onValueChange = { onUpdateVoiceSettings(voiceSettings.copy(speechRate = it)) },
                    valueRange = 0.7f..1.4f,
                    colors = SliderDefaults.colors(thumbColor = JarvisCyan, activeTrackColor = JarvisCyan)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onTestVoice,
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceElevated),
                    border = BorderStroke(1.dp, JarvisCyan),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("TEST VOCAL OUTPUT", color = JarvisTextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ANNOUNCEMENTS & PRIVACY CONTROLS
        Text(
            text = "// ANNOUNCEMENTS & PRIVACY CONTROLS",
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
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Incoming Call Announcements
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Incoming Call Announcements", color = JarvisTextPrimary, fontSize = 13.sp)
                        Text("Announces caller name: \"सज्जाद जी, आपको [नाम] कॉल कर रहा है\"", color = JarvisTextMuted, fontSize = 10.sp)
                    }
                    Switch(
                        checked = voiceSettings.callAnnouncementEnabled,
                        onCheckedChange = { onUpdateVoiceSettings(voiceSettings.copy(callAnnouncementEnabled = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = JarvisCyan, checkedTrackColor = JarvisSurfaceElevated)
                    )
                }

                Divider(color = JarvisBorder, thickness = 0.6.dp)

                // WhatsApp Announcements
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("WhatsApp Vocal Alerts", color = JarvisTextPrimary, fontSize = 13.sp)
                        Text("Announces sender upon notification", color = JarvisTextMuted, fontSize = 10.sp)
                    }
                    Switch(
                        checked = voiceSettings.whatsappAnnouncementEnabled,
                        onCheckedChange = { onUpdateVoiceSettings(voiceSettings.copy(whatsappAnnouncementEnabled = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = JarvisCyan, checkedTrackColor = JarvisSurfaceElevated)
                    )
                }

                Divider(color = JarvisBorder, thickness = 0.6.dp)

                // Read Content
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Read WhatsApp Message Text", color = JarvisTextPrimary, fontSize = 13.sp)
                        Text("Speaks message content aloud (Disable for privacy)", color = JarvisTextMuted, fontSize = 10.sp)
                    }
                    Switch(
                        checked = voiceSettings.readWhatsAppContent,
                        onCheckedChange = { onUpdateVoiceSettings(voiceSettings.copy(readWhatsAppContent = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = JarvisCyan, checkedTrackColor = JarvisSurfaceElevated)
                    )
                }

                Divider(color = JarvisBorder, thickness = 0.6.dp)

                // Quiet hours
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Quiet Hours Mode", color = JarvisTextPrimary, fontSize = 13.sp)
                        Text("Silences all vocal announcements", color = JarvisTextMuted, fontSize = 10.sp)
                    }
                    Switch(
                        checked = voiceSettings.quietHoursEnabled,
                        onCheckedChange = { onUpdateVoiceSettings(voiceSettings.copy(quietHoursEnabled = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = JarvisLaserRed, checkedTrackColor = JarvisSurfaceElevated)
                    )
                }

                Divider(color = JarvisBorder, thickness = 0.6.dp)

                // Background notification access button
                Button(
                    onClick = { AndroidActionManager.openNotificationSettings(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceElevated),
                    border = BorderStroke(1.dp, JarvisBorder),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("MANAGE NOTIFICATION LISTENER PERMISSIONS", color = JarvisTextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
