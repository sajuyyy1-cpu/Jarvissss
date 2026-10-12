package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.avatar.Realistic3DAvatarView
import com.example.ui.theme.*
import com.example.voice.AssistantState
import kotlin.math.*

private data class Point3D(val x: Float, val y: Float, val z: Float)

/**
 * Base Compose UI for the futuristic black-and-blue HUD with states for:
 * - Ready
 * - Listening
 * - Thinking
 * - Speaking
 */
@Composable
fun FuturisticJarvisHud(
    assistantState: AssistantState,
    audioRms: Float,
    onStateSelect: ((AssistantState) -> Unit)? = null,
    onMicClick: (() -> Unit)? = null,
    onEmergencyStop: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // 3D rotation angles (touch draggable)
    var manualRotY by remember { mutableFloatStateOf(0f) }
    var manualRotX by remember { mutableFloatStateOf(0f) }

    // Infinite animation transitions for state animations
    val infiniteTransition = rememberInfiniteTransition(label = "hud_animations")

    val idleRotZ by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "idle_rot_z"
    )

    val thinkingSpeedZ by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fast_rot_z"
    )

    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    val scanlineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanline"
    )

    val wavePulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_pulse"
    )

    var isHumanoidMode by remember { mutableStateOf(true) }

    val statePrimaryColor = when (assistantState) {
        AssistantState.READY -> JarvisCyan
        AssistantState.LISTENING -> JarvisNeonGreen
        AssistantState.THINKING -> JarvisNeonAmber
        AssistantState.SPEAKING -> JarvisElectricBlue
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(JarvisSurfaceElevated, JarvisBackground, JarvisSurface)
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(JarvisBorder, statePrimaryColor.copy(alpha = 0.6f), JarvisBorder)
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("futuristic_jarvis_hud"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP HUD HEADER & TELEMETRY
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statePrimaryColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "HUD.AI // ${assistantState.name}",
                    color = statePrimaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = JarvisSurfaceVariant,
                    border = BorderStroke(0.8.dp, statePrimaryColor),
                    modifier = Modifier.clickable { isHumanoidMode = !isHumanoidMode }
                ) {
                    Text(
                        text = if (isHumanoidMode) "► 3D HUMANOID" else "► 3D CORE",
                        color = statePrimaryColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "CORE: 100%",
                    color = JarvisTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // MAIN HOLOGRAPHIC CANVAS WITH 3D AVATAR & HUD RETICLES
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        manualRotY += dragAmount.x * 0.45f
                        manualRotX = (manualRotX - dragAmount.y * 0.45f).coerceIn(-40f, 40f)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (isHumanoidMode) {
                // Realistic 3D Humanoid Talking Avatar with WebGL, blinking and lip-sync
                Realistic3DAvatarView(
                    assistantState = assistantState,
                    audioRms = audioRms,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Hardware-accelerated Procedural 3D Holographic Arc Reactor Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val baseRadius = min(size.width, size.height) * 0.40f

                    // 1. Draw HUD Grid & Holographic Scanlines
                    drawHudGrid(size.width, size.height, scanlineY, statePrimaryColor)

                    // 2. Draw Holographic Corner Brackets
                    drawHudCornerBrackets(size.width, size.height, statePrimaryColor)

                    // 3. Draw Concentric Arc Reactor & Orbital Degree Ticks
                    val rotZAngle = if (assistantState == AssistantState.THINKING) thinkingSpeedZ else idleRotZ
                    drawArcReactorSystem(
                        cx = cx,
                        cy = cy,
                        radius = baseRadius,
                        state = assistantState,
                        primaryColor = statePrimaryColor,
                        audioRms = audioRms,
                        rotZ = Math.toRadians(rotZAngle.toDouble()).toFloat(),
                        breathing = breathingPulse,
                        wavePulse = wavePulse
                    )

                    // 4. Draw 3D Cybernetic Humanoid Face
                    val totalRotY = Math.toRadians((rotZAngle * 0.3 + manualRotY).toDouble()).toFloat()
                    val totalRotX = Math.toRadians(manualRotX.toDouble()).toFloat()

                    draw3DCyberneticHumanoidHead(
                        cx = cx,
                        cy = cy,
                        scale = baseRadius * 0.70f,
                        rotX = totalRotX,
                        rotY = totalRotY,
                        state = assistantState,
                        primaryColor = statePrimaryColor,
                        audioRms = audioRms,
                        breathing = breathingPulse
                    )

                    // 5. Draw Live Acoustic Spectrum Bars if LISTENING or SPEAKING
                    if (assistantState == AssistantState.LISTENING || assistantState == AssistantState.SPEAKING) {
                        drawLiveSpectrumBars(
                            cx = cx,
                            cy = size.height - 18f,
                            width = size.width * 0.75f,
                            audioRms = audioRms,
                            primaryColor = statePrimaryColor,
                            state = assistantState
                        )
                    }
                }
            }

            // Central State Badge HUD Overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = JarvisSurface.copy(alpha = 0.85f),
                    border = BorderStroke(0.8.dp, statePrimaryColor)
                ) {
                    val statusMessage = when (assistantState) {
                        AssistantState.READY -> "READY // QUANTUM RECEPTORS STANDBY"
                        AssistantState.LISTENING -> "LISTENING // CAPTURING AUDIO STREAM"
                        AssistantState.THINKING -> "THINKING // NEURAL CORE PROCESSING"
                        AssistantState.SPEAKING -> "SPEAKING // VOCAL SYNTHESIZER ACTIVE"
                    }
                    Text(
                        text = "● $statusMessage",
                        color = statePrimaryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // INTERACTIVE STATE SWITCHER BAR (Ready, Listening, Thinking, Speaking)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AssistantState.values().forEach { state ->
                val isCurrent = assistantState == state
                val buttonColor = when (state) {
                    AssistantState.READY -> JarvisCyan
                    AssistantState.LISTENING -> JarvisNeonGreen
                    AssistantState.THINKING -> JarvisNeonAmber
                    AssistantState.SPEAKING -> JarvisElectricBlue
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onStateSelect?.invoke(state) }
                        .border(
                            width = if (isCurrent) 1.5.dp else 0.6.dp,
                            color = if (isCurrent) buttonColor else JarvisBorder,
                            shape = RoundedCornerShape(6.dp)
                        ),
                    color = if (isCurrent) buttonColor.copy(alpha = 0.2f) else JarvisSurfaceVariant
                ) {
                    Text(
                        text = state.name,
                        color = if (isCurrent) buttonColor else JarvisTextMuted,
                        fontSize = 10.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }

        // MIC & CONTROLS ROW
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TARGET: SAJAD (सज्जाद)",
                    color = JarvisTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Tap state to preview or tap mic to speak",
                    color = JarvisTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Emergency Stop
                AnimatedVisibility(visible = assistantState == AssistantState.SPEAKING || assistantState == AssistantState.LISTENING) {
                    IconButton(
                        onClick = { onEmergencyStop?.invoke() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(JarvisLaserRed.copy(alpha = 0.25f))
                            .border(BorderStroke(1.dp, JarvisLaserRed), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Voice Output",
                            tint = JarvisLaserRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Microphone Button
                val isMicActive = assistantState == AssistantState.LISTENING
                IconButton(
                    onClick = { onMicClick?.invoke() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isMicActive) JarvisNeonGreen.copy(alpha = 0.25f) else JarvisSurfaceElevated)
                        .border(
                            BorderStroke(1.5.dp, if (isMicActive) JarvisNeonGreen else JarvisCyan),
                            CircleShape
                        )
                        .testTag("hud_mic_button")
                ) {
                    Icon(
                        imageVector = if (isMicActive) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Microphone Directive",
                        tint = if (isMicActive) JarvisNeonGreen else JarvisCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

// ---------------- CANVAS DRAWING HELPERS ---------------- //

private fun DrawScope.drawHudGrid(w: Float, h: Float, scanlineY: Float, tint: Color) {
    // Subtle background matrix dots
    val step = 32f
    for (x in step.toInt() until w.toInt() step step.toInt()) {
        for (y in step.toInt() until h.toInt() step step.toInt()) {
            drawCircle(
                color = JarvisBorder.copy(alpha = 0.18f),
                radius = 1f,
                center = Offset(x.toFloat(), y.toFloat())
            )
        }
    }

    // Holographic Scanline
    val sy = h * scanlineY
    drawLine(
        color = tint.copy(alpha = 0.25f),
        start = Offset(0f, sy),
        end = Offset(w, sy),
        strokeWidth = 1.2f
    )
}

private fun DrawScope.drawHudCornerBrackets(w: Float, h: Float, color: Color) {
    val bLen = 18f
    val bPad = 10f
    val stroke = 1.5f
    val bracketColor = color.copy(alpha = 0.65f)

    // Top-Left
    drawLine(bracketColor, Offset(bPad, bPad), Offset(bPad + bLen, bPad), stroke)
    drawLine(bracketColor, Offset(bPad, bPad), Offset(bPad, bPad + bLen), stroke)

    // Top-Right
    drawLine(bracketColor, Offset(w - bPad, bPad), Offset(w - bPad - bLen, bPad), stroke)
    drawLine(bracketColor, Offset(w - bPad, bPad), Offset(w - bPad, bPad + bLen), stroke)

    // Bottom-Left
    drawLine(bracketColor, Offset(bPad, h - bPad), Offset(bPad + bLen, h - bPad), stroke)
    drawLine(bracketColor, Offset(bPad, h - bPad), Offset(bPad, h - bPad - bLen), stroke)

    // Bottom-Right
    drawLine(bracketColor, Offset(w - bPad, h - bPad), Offset(w - bPad - bLen, h - bPad), stroke)
    drawLine(bracketColor, Offset(w - bPad, h - bPad), Offset(w - bPad, h - bPad - bLen), stroke)
}

private fun DrawScope.drawArcReactorSystem(
    cx: Float,
    cy: Float,
    radius: Float,
    state: AssistantState,
    primaryColor: Color,
    audioRms: Float,
    rotZ: Float,
    breathing: Float,
    wavePulse: Float
) {
    // Outer Degree Calibration Ticks (24 segments)
    val segments = 24
    for (i in 0 until segments) {
        val angle = (i * (360f / segments) + Math.toDegrees(rotZ.toDouble())).toFloat()
        val rad = Math.toRadians(angle.toDouble())
        val r1 = radius * breathing
        val r2 = (radius - (if (i % 6 == 0) 10f else 5f)) * breathing

        val x1 = cx + cos(rad).toFloat() * r1
        val y1 = cy + sin(rad).toFloat() * r1
        val x2 = cx + cos(rad).toFloat() * r2
        val y2 = cy + sin(rad).toFloat() * r2

        drawLine(
            color = primaryColor.copy(alpha = if (i % 6 == 0) 0.85f else 0.35f),
            start = Offset(x1, y1),
            end = Offset(x2, y2),
            strokeWidth = if (i % 6 == 0) 2.2f else 1f
        )
    }

    // Outer Concentric Rings
    drawCircle(
        color = primaryColor.copy(alpha = 0.30f),
        radius = radius * 0.90f * breathing,
        center = Offset(cx, cy),
        style = Stroke(width = 1.2f)
    )

    drawCircle(
        color = primaryColor.copy(alpha = 0.50f),
        radius = radius * 0.78f,
        center = Offset(cx, cy),
        style = Stroke(width = 1f)
    )

    // Dynamic Ripple Waves for Listening / Speaking
    if (state == AssistantState.LISTENING || state == AssistantState.SPEAKING) {
        val rippleR = (radius * 0.5f) + (wavePulse * radius * 0.55f) + (audioRms * 25f)
        drawCircle(
            color = primaryColor.copy(alpha = (1f - wavePulse) * 0.7f),
            radius = rippleR,
            center = Offset(cx, cy),
            style = Stroke(width = 2.dp.toPx())
        )
    }

    // Central Core Glow
    val coreRadius = (radius * 0.16f * breathing) + (audioRms * 14f)
    drawCircle(
        color = primaryColor.copy(alpha = 0.20f),
        radius = coreRadius * 2.2f,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = primaryColor.copy(alpha = 0.65f),
        radius = coreRadius,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = Color.White,
        radius = coreRadius * 0.40f,
        center = Offset(cx, cy)
    )
}

private fun DrawScope.draw3DCyberneticHumanoidHead(
    cx: Float,
    cy: Float,
    scale: Float,
    rotX: Float,
    rotY: Float,
    state: AssistantState,
    primaryColor: Color,
    audioRms: Float,
    breathing: Float
) {
    val headNodes = listOf(
        Point3D(0f, -1.0f, 0f),
        Point3D(-0.4f, -0.85f, 0.3f),
        Point3D(0.4f, -0.85f, 0.3f),
        Point3D(-0.6f, -0.6f, 0.2f),
        Point3D(0.6f, -0.6f, 0.2f),
        Point3D(-0.35f, -0.4f, 0.6f),
        Point3D(0f, -0.4f, 0.7f),
        Point3D(0.35f, -0.4f, 0.6f),
        Point3D(-0.3f, -0.15f, 0.65f), // Left Optic
        Point3D(0.3f, -0.15f, 0.65f),  // Right Optic
        Point3D(0f, -0.1f, 0.75f),     // Nose Bridge
        Point3D(-0.55f, 0.05f, 0.5f),
        Point3D(0.55f, 0.05f, 0.5f),
        Point3D(-0.35f, 0.45f + (audioRms * 0.12f), 0.4f),
        Point3D(0.35f, 0.45f + (audioRms * 0.12f), 0.4f),
        Point3D(0f, 0.7f + (audioRms * 0.28f), 0.5f), // Chin
        Point3D(-0.7f, -0.1f, -0.1f),
        Point3D(0.7f, -0.1f, -0.1f)
    )

    val edges = listOf(
        Pair(0, 1), Pair(0, 2), Pair(1, 3), Pair(2, 4),
        Pair(1, 5), Pair(2, 7), Pair(5, 6), Pair(6, 7),
        Pair(5, 8), Pair(7, 9), Pair(8, 10), Pair(9, 10),
        Pair(8, 11), Pair(9, 12), Pair(11, 13), Pair(12, 14),
        Pair(13, 15), Pair(14, 15), Pair(10, 15),
        Pair(3, 16), Pair(4, 17), Pair(16, 13), Pair(17, 14)
    )

    val projected = headNodes.map { p ->
        val cosY = cos(rotY)
        val sinY = sin(rotY)
        val x1 = p.x * cosY + p.z * sinY
        val z1 = -p.x * sinY + p.z * cosY

        val cosX = cos(rotX)
        val sinX = sin(rotX)
        val y2 = p.y * cosX - z1 * sinX
        val z2 = p.y * sinX + z1 * cosX

        val distance = 2.5f
        val depthScale = distance / (distance + z2)

        val screenX = cx + x1 * scale * depthScale * breathing
        val screenY = cy + y2 * scale * depthScale * breathing
        Triple(screenX, screenY, z2)
    }

    // Draw Wireframe Lines
    edges.forEach { (i1, i2) ->
        val p1 = projected[i1]
        val p2 = projected[i2]
        val avgZ = (p1.third + p2.third) / 2f
        val alpha = ((avgZ + 1f) / 2f).coerceIn(0.2f, 0.88f)

        drawLine(
            color = primaryColor.copy(alpha = alpha),
            start = Offset(p1.first, p1.second),
            end = Offset(p2.first, p2.second),
            strokeWidth = 1.6f
        )
    }

    // Draw Optics & Nodes
    projected.forEachIndexed { index, (px, py, _) ->
        val isOptic = (index == 8 || index == 9)
        val nodeRadius = if (isOptic) 5f + (audioRms * 3.5f) else 2.8f
        val nodeColor = if (isOptic) Color.White else primaryColor

        drawCircle(
            color = nodeColor.copy(alpha = if (isOptic) 1f else 0.7f),
            radius = nodeRadius,
            center = Offset(px, py)
        )
        if (isOptic) {
            drawCircle(
                color = primaryColor.copy(alpha = 0.5f),
                radius = nodeRadius * 2.2f,
                center = Offset(px, py)
            )
        }
    }
}

private fun DrawScope.drawLiveSpectrumBars(
    cx: Float,
    cy: Float,
    width: Float,
    audioRms: Float,
    primaryColor: Color,
    state: AssistantState
) {
    val barCount = 28
    val barWidth = width / barCount
    val startX = cx - (width / 2f)

    for (i in 0 until barCount) {
        val normalizedIndex = i.toFloat() / barCount
        // Sine wave modulation with RMS audio height
        val heightFactor = sin((normalizedIndex * Math.PI).toDouble()).toFloat()
        val dynRms = if (audioRms > 0.05f) audioRms else 0.15f
        val barHeight = (heightFactor * dynRms * 28f) + 3f

        val x = startX + (i * barWidth)
        drawRect(
            color = primaryColor.copy(alpha = (0.35f + heightFactor * 0.55f).coerceIn(0f, 1f)),
            topLeft = Offset(x, cy - barHeight),
            size = Size(barWidth * 0.65f, barHeight)
        )
    }
}
