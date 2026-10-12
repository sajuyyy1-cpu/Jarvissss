package com.example.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class JarvisNotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "JarvisNotification"
        private const val WHATSAPP_PKG = "com.whatsapp"
        private const val WHATSAPP_BUSINESS_PKG = "com.whatsapp.w4b"

        var isNotificationServiceRunning = false
            private set

        var listenerCallback: ((sender: String, message: String) -> Unit)? = null
        private val announcedKeys = mutableMapOf<String, Long>()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isNotificationServiceRunning = true
        Log.d(TAG, "JARVIS Notification Listener Connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isNotificationServiceRunning = false
        Log.d(TAG, "JARVIS Notification Listener Disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val pkg = sbn.packageName ?: return

        if (pkg == WHATSAPP_PKG || pkg == WHATSAPP_BUSINESS_PKG) {
            val notification = sbn.notification ?: return
            val extras = notification.extras ?: return

            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim() ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim() ?: ""

            // Skip group summary notifications or empty messages
            if (title.isEmpty() || title == "WhatsApp" || text.isEmpty()) return

            // Prevent duplicate announcements within 10 seconds for same key
            val key = "${sbn.id}_${title}_${text.take(20)}"
            val currentTime = System.currentTimeMillis()
            val lastAnnounced = announcedKeys[key] ?: 0L

            if (currentTime - lastAnnounced < 10000L) {
                return
            }
            announcedKeys[key] = currentTime

            // Clean old entries
            if (announcedKeys.size > 50) {
                announcedKeys.clear()
            }

            Log.d(TAG, "WhatsApp Notification from $title: $text")
            listenerCallback?.invoke(title, text)
        }
    }
}
