package com.example.credencialholder.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.credencialholder.R

class CredentialWidgetPinReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        val credentialId = intent.getStringExtra(EXTRA_CREDENTIAL_ID)

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && !credentialId.isNullOrEmpty()) {
            val prefs = WidgetPrefsManager(context)
            prefs.setCredentialId(appWidgetId, credentialId)

            val appWidgetManager = AppWidgetManager.getInstance(context)
            CredentialWidgetProvider.updateWidget(context, appWidgetManager, appWidgetId)

            Toast.makeText(context, R.string.widget_pin_success, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val ACTION_WIDGET_PINNED = "com.example.credencialholder.ACTION_WIDGET_PINNED"
        const val EXTRA_CREDENTIAL_ID = "credential_id"
    }
}
