package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.Log

class PhoneCallReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "PhoneCallReceiver"
        var callListenerCallback: ((callerName: String?) -> Unit)? = null
        private var lastState = TelephonyManager.EXTRA_STATE_IDLE
        private var lastAnnouncedTime = 0L
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return

            if (state == TelephonyManager.EXTRA_STATE_RINGING) {
                val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                val now = System.currentTimeMillis()

                // Prevent repeat announcements within 5 seconds for the same ring
                if (now - lastAnnouncedTime > 5000L) {
                    lastAnnouncedTime = now
                    val callerName = getContactName(context, incomingNumber)
                    Log.d(TAG, "Incoming Call: number=$incomingNumber, contactName=$callerName")
                    callListenerCallback?.invoke(callerName)
                }
            }
            lastState = state
        }
    }

    private fun getContactName(context: Context, phoneNumber: String?): String? {
        if (phoneNumber.isNullOrEmpty()) return null

        try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )
            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
            val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)

            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        return it.getString(nameIndex)
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Contacts permission not granted: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error looking up contact: ${e.message}")
        }
        return null
    }
}
