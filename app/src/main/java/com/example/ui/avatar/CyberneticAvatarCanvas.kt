package com.example.ui.avatar

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.voice.AssistantState
import kotlin.math.*

private data class Point3D(val x: Float, val y: Float, val z: Float)

@Composable
fun CyberneticAvatarCanvas(
    assistantState: AssistantState,
    audioRms: Float,
    modifier: Modifier = Modifier
) {
    // Interactive drag rotation angles
    var manualRotY by remember { mutableFloatStateOf(0f) }
    var manualRotX by remember { mutableFloatStateOf(0f) }

    // Continuous idle rotation and pulse animations
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_core")
    
    val baseAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbital_rot"
    )

    val fastAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = when (assistantState) {
                AssistantState.THINKING -> 1500
                AssistantState.SPEAKING -> 3000
                else -> 6000
            }, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rot"
    )

    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_pulse"
    )

    val wavePulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "voice_wave"
    )

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    manualRotY += dragAmount.x * 0.5f
                    manualRotX = (manualRotX - dragAmount.y * 0.5f).coerceIn(-45f, 45f)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = min(size.width, size.height) * 0.42f

            val totalRotY = Math.toRadians((baseAngle + manualRotY).toDouble()).toFloat()
            val totalRotX = Math.toRadians(manualRotX.toDouble()).toFloat()

            // 1. Draw Holographic Arc Reactor Outer Rings
            drawArcReactorRings(
                cx, cy, radius,
                rotY = totalRotY,
                rotZ = Math.toRadians(fastAngle.toDouble()).toFloat(),
                state = assistantState,
                audioRms = audioRms,
                breathing = breathingPulse,
                wavePulse = wavePulse
            )

            // 2. Draw 3D Wireframe Cybernetic Humanoid Face
            draw3DCyberneticHead(
                cx, cy, radius * 0.72f,
                rotX = totalRotX,
                rotY = totalRotY,
                state = assistantState,
                audioRms = audioRms,
                breathing = breathingPulse
            )

            // 3. Central Core Glowing Node
            val coreColor = when (assistantState) {
                AssistantState.LISTENING -> JarvisCyan
                AssistantState.THINKING -> JarvisPurple
                AssistantState.SPEAKING -> JarvisElectricBlue
                AssistantState.READY -> JarvisCyan
            }
            val dynamicCoreRadius = (radius * 0.15f * breathingPulse) + (audioRms * 15f)

            drawCircle(
                color = coreColor.copy(alpha = 0.25f),
                radius = dynamicCoreRadius * 1.8f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = coreColor.copy(alpha = 0.6f),
                radius = dynamicCoreRadius,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = Color.White,
                radius = dynamicCoreRadius * 0.35f,
                center = Offset(cx, cy)
            )
        }

        // HUD State Badge overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
        ) {
            val stateText = when (assistantState) {
                AssistantState.READY -> "READY // STANDBY"
                AssistantState.LISTENING -> "LISTENING // AUDIO RECEPTOR ACTIVE"
                AssistantState.THINKING -> "THINKING // NEURAL CORE PROCESSING"
                AssistantState.SPEAKING -> "SPEAKING // VOCAL SYNTHESIZER ACTIVE"
            }
            val stateColor = when (assistantState) {
                AssistantState.READY -> JarvisCyan
                AssistantState.LISTENING -> JarvisNeonGreen
                AssistantState.THINKING -> JarvisNeonAmber
                AssistantState.SPEAKING -> JarvisElectricBlue
            }

            Text(
                text = "● $stateText",
                color = stateColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}

private fun DrawScope.drawArcReactorRings(
    cx: Float,
    cy: Float,
    radius: Float,
    rotY: Float,
    rotZ: Float,
    state: AssistantState,
    audioRms: Float,
    breathing: Float,
    wavePulse: Float
) {
    val ringColor = when (state) {
        AssistantState.LISTENING -> JarvisCyan
        AssistantState.THINKING -> JarvisPurple
        AssistantState.SPEAKING -> JarvisElectricBlue
        AssistantState.READY -> JarvisCyan
    }

    // Outer Segmented Calibration Ring
    val segments = 24
    for (i in 0 until segments) {
        val angle = (i * (360f / segments) + Math.toDegrees(rotZ.toDouble())).toFloat()
        val rad = Math.toRadians(angle.toDouble())
        val r1 = radius * breathing
        val r2 = (radius - 8f) * breathing

        val x1 = cx + cos(rad).toFloat() * r1
        val y1 = cy + sin(rad).toFloat() * r1
        val x2 = cx + cos(rad).toFloat() * r2
        val y2 = cy + sin(rad).toFloat() * r2

        drawLine(
            color = ringColor.copy(alpha = if (i % 4 == 0) 0.9f else 0.4f),
            start = Offset(x1, y1),
            end = Offset(x2, y2),
            strokeWidth = if (i % 4 == 0) 2.5f else 1.2f
        )
    }

    // Concentric Orbit Circles
    drawCircle(
        color = ringColor.copy(alpha = 0.35f),
        radius = radius * 0.88f * breathing,
        center = Offset(cx, cy),
        style = Stroke(width = 1.5f)
    )

    drawCircle(
        color = ringColor.copy(alpha = 0.5f),
        radius = radius * 0.76f,
        center = Offset(cx, cy),
        style = Stroke(width = 1f)
    )

    // Voice Reactive Waves when SPEAKING or LISTENING
    if (state == AssistantState.SPEAKING || state == AssistantState.LISTENING) {
        val rippleRadius = (radius * 0.6f) + (wavePulse * radius * 0.4f) + (audioRms * 20f)
        drawCircle(
            color = ringColor.copy(alpha = (1f - wavePulse) * 0.6f),
            radius = rippleRadius,
            center = Offset(cx, cy),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

private fun DrawScope.draw3DCyberneticHead(
    cx: Float,
    cy: Float,
    scale: Float,
    rotX: Float,
    rotY: Float,
    state: AssistantState,
    audioRms: Float,
    breathing: Float
) {
    // 3D Model Wireframe Points for Cybernetic Head (normalized coordinates)
    val headNodes = listOf(
        // Cranium top
        Point3D(0f, -1.0f, 0f),
        Point3D(-0.4f, -0.85f, 0.3f),
        Point3D(0.4f, -0.85f, 0.3f),
        Point3D(-0.6f, -0.6f, 0.2f),
        Point3D(0.6f, -0.6f, 0.2f),

        // Forehead
        Point3D(-0.35f, -0.4f, 0.6f),
        Point3D(0f, -0.4f, 0.7f),
        Point3D(0.35f, -0.4f, 0.6f),

        // Eye / Optic Sensors
        Point3D(-0.3f, -0.15f, 0.65f), // Left optic
        Point3D(0.3f, -0.15f, 0.65f),  // Right optic
        Point3D(0f, -0.1f, 0.75f),     // Bridge of nose

        // Cheekbones
        Point3D(-0.55f, 0.05f, 0.5f),
        Point3D(0.55f, 0.05f, 0.5f),

        // Jaw and Chin (vocal audio displacement)
        Point3D(-0.35f, 0.45f + (audioRms * 0.1f), 0.4f),
        Point3D(0.35f, 0.45f + (audioRms * 0.1f), 0.4f),
        Point3D(0f, 0.7f + (audioRms * 0.25f), 0.5f), // Chin tip

        // Temporal / Ears
        Point3D(-0.7f, -0.1f, -0.1f),
        Point3D(0.7f, -0.1f, -0.1f)
    )

    // Wireframe connection edges between nodes
    val edges = listOf(
        Pair(0, 1), Pair(0, 2), Pair(1, 3), Pair(2, 4),
        Pair(1, 5), Pair(2, 7), Pair(5, 6), Pair(6, 7),
        Pair(5, 8), Pair(7, 9), Pair(8, 10), Pair(9, 10),
        Pair(8, 11), Pair(9, 12), Pair(11, 13), Pair(12, 14),
        Pair(13, 15), Pair(14, 15), Pair(10, 15),
        Pair(3, 16), Pair(4, 17), Pair(16, 13), Pair(17, 14)
    )

    // Project 3D points with rotation to 2D screen coordinates
    val projected = headNodes.map { p ->
        // Y-axis rotation
        val cosY = cos(rotY)
        val sinY = sin(rotY)
        val x1 = p.x * cosY + p.z * sinY
        val z1 = -p.x * sinY + p.z * cosY

        // X-axis rotation
        val cosX = cos(rotX)
        val sinX = sin(rotX)
        val y2 = p.y * cosX - z1 * sinX
        val z2 = p.y * sinX + z1 * cosX

        // Perspective depth projection
        val distance = 2.5f
        val depthScale = distance / (distance + z2)

        val screenX = cx + x1 * scale * depthScale * breathing
        val screenY = cy + y2 * scale * depthScale * breathing
        Triple(screenX, screenY, z2)
    }

    val wireColor = when (state) {
        AssistantState.LISTENING -> JarvisCyan
        AssistantState.THINKING -> JarvisPurple
        AssistantState.SPEAKING -> JarvisElectricBlue
        AssistantState.READY -> JarvisCyan
    }

    // Draw Wireframe Lines
    edges.forEach { (i1, i2) ->
        val p1 = projected[i1]
        val p2 = projected[i2]
        // Alpha based on depth
        val avgZ = (p1.third + p2.third) / 2f
        val alpha = ((avgZ + 1f) / 2f).coerceIn(0.2f, 0.9f)

        drawLine(
            color = wireColor.copy(alpha = alpha),
            start = Offset(p1.first, p1.second),
            end = Offset(p2.first, p2.second),
            strokeWidth = 1.8f
        )
    }

    // Draw Glowing Optics & Neural Nodes
    projected.forEachIndexed { index, (px, py, pz) ->
        val isOptic = (index == 8 || index == 9)
        val isChin = (index == 15)

        val nodeRadius = if (isOptic) 5f + (audioRms * 3f) else 3f
        val nodeColor = if (isOptic) Color.White else wireColor

        drawCircle(
            color = nodeColor.copy(alpha = if (isOptic) 1f else 0.7f),
            radius = nodeRadius,
            center = Offset(px, py)
        )

        if (isOptic) {
            drawCircle(
                color = wireColor.copy(alpha = 0.5f),
                radius = nodeRadius * 2.5f,
                center = Offset(px, py)
            )
        }
    }
}
