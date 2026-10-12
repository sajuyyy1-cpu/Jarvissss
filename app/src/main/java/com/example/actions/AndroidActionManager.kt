package com.example.actions

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log

data class ActionResult(
    val success: Boolean,
    val feedbackMessageEn: String,
    val feedbackMessageHi: String,
    val feedbackMessageHinglish: String,
    val requiresConfirmation: Boolean = false
)

object AndroidActionManager {
    private const val TAG = "ActionManager"

    fun openYouTube(context: Context, searchQuery: String? = null): ActionResult {
        return try {
            if (!searchQuery.isNullOrBlank()) {
                // Search on YouTube app or browser fallback
                val searchIntent = Intent(Intent.ACTION_SEARCH).apply {
                    setPackage("com.google.android.youtube")
                    putExtra("query", searchQuery)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                if (searchIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(searchIntent)
                } else {
                    val webIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(searchQuery)}")
                    ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                    context.startActivity(webIntent)
                }
                ActionResult(
                    success = true,
                    feedbackMessageEn = "Searching YouTube for '$searchQuery'.",
                    feedbackMessageHi = "YouTube पर '$searchQuery' खोजा जा रहा है।",
                    feedbackMessageHinglish = "YouTube par '$searchQuery' search kar raha hoon, Sajjad bhai."
                )
            } else {
                val launchIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
                if (launchIntent != null) {
                    launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(launchIntent)
                    ActionResult(
                        success = true,
                        feedbackMessageEn = "Opening YouTube.",
                        feedbackMessageHi = "YouTube खोला जा रहा है।",
                        feedbackMessageHinglish = "YouTube khol raha hoon, Sajjad bhai."
                    )
                } else {
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(webIntent)
                    ActionResult(
                        success = true,
                        feedbackMessageEn = "YouTube app not installed. Opening YouTube in browser.",
                        feedbackMessageHi = "YouTube ऐप नहीं मिला। ब्राउज़र में खोला जा रहा है।",
                        feedbackMessageHinglish = "YouTube app nahi mila, browser mein khol diya hai."
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open YouTube: ${e.message}")
            ActionResult(
                success = false,
                feedbackMessageEn = "Unable to open YouTube: ${e.localizedMessage}",
                feedbackMessageHi = "YouTube खोलने में असमर्थ।",
                feedbackMessageHinglish = "YouTube open karne mein problem aayi."
            )
        }
    }

    fun openInstagram(context: Context, query: String? = null): ActionResult {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage("com.instagram.android")
            if (launchIntent != null) {
                launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(launchIntent)
                ActionResult(
                    success = true,
                    feedbackMessageEn = "Opening Instagram.",
                    feedbackMessageHi = "Instagram खोला जा रहा है।",
                    feedbackMessageHinglish = "Instagram khol diya hai, Sajjad bhai."
                )
            } else {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                ActionResult(
                    success = true,
                    feedbackMessageEn = "Instagram app not installed. Opening Instagram on web.",
                    feedbackMessageHi = "Instagram ऐप स्थापित नहीं है। ब्राउज़र में खोला जा रहा है।",
                    feedbackMessageHinglish = "Instagram app installed nahi hai, web par khol diya hai."
                )
            }
        } catch (e: Exception) {
            ActionResult(
                success = false,
                feedbackMessageEn = "Could not open Instagram.",
                feedbackMessageHi = "Instagram खोलने में विफल।",
                feedbackMessageHinglish = "Instagram open nahi ho paya."
            )
        }
    }

    fun openWhatsApp(context: Context): ActionResult {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
                ?: context.packageManager.getLaunchIntentForPackage("com.whatsapp.w4b")
            if (launchIntent != null) {
                launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(launchIntent)
                ActionResult(
                    success = true,
                    feedbackMessageEn = "Opening WhatsApp.",
                    feedbackMessageHi = "WhatsApp खोला जा रहा है।",
                    feedbackMessageHinglish = "WhatsApp open kar diya hai, Sajjad bhai."
                )
            } else {
                ActionResult(
                    success = false,
                    feedbackMessageEn = "WhatsApp is not installed on this device.",
                    feedbackMessageHi = "WhatsApp इस डिवाइस पर इंस्टॉल नहीं है।",
                    feedbackMessageHinglish = "WhatsApp is device par installed nahi hai."
                )
            }
        } catch (e: Exception) {
            ActionResult(
                success = false,
                feedbackMessageEn = "Could not open WhatsApp.",
                feedbackMessageHi = "WhatsApp खोलने में विफल।",
                feedbackMessageHinglish = "WhatsApp open nahi ho paya."
            )
        }
    }

    fun searchWeb(context: Context, query: String): ActionResult {
        return try {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, query)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")
                ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                context.startActivity(browserIntent)
            }
            ActionResult(
                success = true,
                feedbackMessageEn = "Searching the web for '$query'.",
                feedbackMessageHi = "वेब पर '$query' खोजा जा रहा है।",
                feedbackMessageHinglish = "Google par '$query' search kar raha hoon, Sajjad bhai."
            )
        } catch (e: Exception) {
            ActionResult(
                success = false,
                feedbackMessageEn = "Search failed: ${e.localizedMessage}",
                feedbackMessageHi = "खोज विफल रही।",
                feedbackMessageHinglish = "Web search fail ho gaya."
            )
        }
    }

    fun openMapsNavigation(context: Context, destination: String): ActionResult {
        return try {
            val gmmIntentUri = Uri.parse("google.navigation:q=${Uri.encode(destination)}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                val geoIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(destination)}")
                ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                context.startActivity(geoIntent)
            }
            ActionResult(
                success = true,
                feedbackMessageEn = "Starting navigation to '$destination'.",
                feedbackMessageHi = "'$destination' के लिए नेविगेशन शुरू किया जा रहा है।",
                feedbackMessageHinglish = "'$destination' ke liye Maps rasta dikha raha hai, Sajjad bhai."
            )
        } catch (e: Exception) {
            ActionResult(
                success = false,
                feedbackMessageEn = "Could not launch maps navigation.",
                feedbackMessageHi = "नेविगेशन खोलने में असमर्थ।",
                feedbackMessageHinglish = "Maps open nahi ho paya."
            )
        }
    }

    fun openCamera(context: Context): ActionResult {
        return try {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ActionResult(
                success = true,
                feedbackMessageEn = "Camera optical sensors activated.",
                feedbackMessageHi = "कैमरा खोला जा रहा है।",
                feedbackMessageHinglish = "Camera sensors active kar diye hain, Sajjad bhai."
            )
        } catch (e: Exception) {
            ActionResult(
                success = false,
                feedbackMessageEn = "Camera launch failed.",
                feedbackMessageHi = "कैमरा खोलने में असमर्थ।",
                feedbackMessageHinglish = "Camera open nahi ho paya."
            )
        }
    }

    fun openSystemSettings(context: Context): ActionResult {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ActionResult(
                success = true,
                feedbackMessageEn = "Opening Android System Settings.",
                feedbackMessageHi = "सिस्टम सेटिंग्स खोली जा रही हैं।",
                feedbackMessageHinglish = "Android settings open kar di hain."
            )
        } catch (e: Exception) {
            ActionResult(
                success = false,
                feedbackMessageEn = "Could not open Settings.",
                feedbackMessageHi = "सेटिंग्स खोलने में विफल।",
                feedbackMessageHinglish = "Settings open nahi hui."
            )
        }
    }

    fun dialPhoneNumber(context: Context, number: String): ActionResult {
        return try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(number)}")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ActionResult(
                success = true,
                feedbackMessageEn = "Opening dialer with $number.",
                feedbackMessageHi = "$number के लिए डायलर खोला जा रहा है।",
                feedbackMessageHinglish = "$number ke liye dialer open kar diya hai."
            )
        } catch (e: Exception) {
            ActionResult(
                success = false,
                feedbackMessageEn = "Could not launch dialer.",
                feedbackMessageHi = "डायलर खोलने में असमर्थ।",
                feedbackMessageHinglish = "Dialer open nahi ho saka."
            )
        }
    }

    fun openNotificationSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open notification listener settings: ${e.message}")
        }
    }
}
