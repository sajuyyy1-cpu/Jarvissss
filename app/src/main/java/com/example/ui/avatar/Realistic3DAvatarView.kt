package com.example.ui.avatar

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.*
import com.example.voice.AssistantState

enum class AvatarRenderMode {
    REALISTIC_3D_HUMANOID, // Three.js / WebGL Realistic Human Face with Visemes & Blinking
    CYBERNETIC_HOLOGRAM     // Canvas Procedural 3D Holographic Arc Reactor
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun Realistic3DAvatarView(
    assistantState: AssistantState,
    audioRms: Float,
    modifier: Modifier = Modifier
) {
    var renderMode by remember { mutableStateOf(AvatarRenderMode.REALISTIC_3D_HUMANOID) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isWebViewLoaded by remember { mutableStateOf(false) }

    // Sync state changes to WebGL Three.js instance
    LaunchedEffect(assistantState, isWebViewLoaded) {
        if (isWebViewLoaded) {
            webViewRef?.evaluateJavascript("if(window.updateState) window.updateState('${assistantState.name}');", null)
            if (assistantState == AssistantState.SPEAKING) {
                webViewRef?.evaluateJavascript("if(window.setSpeaking) window.setSpeaking(true);", null)
            } else {
                webViewRef?.evaluateJavascript("if(window.setSpeaking) window.setSpeaking(false);", null)
            }
        }
    }

    // Sync audio RMS for live lip-sync jaw displacement
    LaunchedEffect(audioRms, isWebViewLoaded) {
        if (isWebViewLoaded && (assistantState == AssistantState.SPEAKING || assistantState == AssistantState.LISTENING)) {
            webViewRef?.evaluateJavascript("if(window.updateAudioRms) window.updateAudioRms($audioRms);", null)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(JarvisBackground)
            .testTag("realistic_3d_avatar_view")
    ) {
        when (renderMode) {
            AvatarRenderMode.REALISTIC_3D_HUMANOID -> {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            setLayerType(View.LAYER_TYPE_HARDWARE, null)
                            setBackgroundColor(0) // Transparent background
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                allowFileAccess = true
                                allowContentAccess = true
                                cacheMode = WebSettings.LOAD_NO_CACHE
                            }
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isWebViewLoaded = true
                                    view?.evaluateJavascript("if(window.updateState) window.updateState('${assistantState.name}');", null)
                                }
                            }
                            webChromeClient = WebChromeClient()
                            loadUrl("file:///android_asset/avatar_3d.html")
                            webViewRef = this
                        }
                    },
                    update = { view ->
                        webViewRef = view
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            AvatarRenderMode.CYBERNETIC_HOLOGRAM -> {
                CyberneticAvatarCanvas(
                    assistantState = assistantState,
                    audioRms = audioRms,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Mode Switcher Pill in Top-Right Corner
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .clip(RoundedCornerShape(6.dp))
                .clickable {
                    renderMode = if (renderMode == AvatarRenderMode.REALISTIC_3D_HUMANOID) {
                        AvatarRenderMode.CYBERNETIC_HOLOGRAM
                    } else {
                        AvatarRenderMode.REALISTIC_3D_HUMANOID
                    }
                }
                .border(BorderStroke(0.8.dp, JarvisCyan.copy(alpha = 0.5f)), RoundedCornerShape(6.dp)),
            color = JarvisSurface.copy(alpha = 0.85f)
        ) {
            Text(
                text = if (renderMode == AvatarRenderMode.REALISTIC_3D_HUMANOID) "► 3D HUMANOID" else "► 3D HOLOGRAM",
                color = JarvisCyan,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }
    }
}
