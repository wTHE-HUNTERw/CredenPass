package com.example.credencialholder.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import com.example.credencialholder.R

object WidgetPinHelper {

    fun pinCredentialToHomeScreen(context: Context, credentialId: String, credentialTitle: String) {
        val appWidgetManager = AppWidgetManager.getInstance(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appWidgetManager.isRequestPinAppWidgetSupported) {
            val provider = ComponentName(context, CredentialWidgetProvider::class.java)

            val callbackIntent = Intent(context, CredentialWidgetPinReceiver::class.java).apply {
                action = CredentialWidgetPinReceiver.ACTION_WIDGET_PINNED
                putExtra(CredentialWidgetPinReceiver.EXTRA_CREDENTIAL_ID, credentialId)
            }

            val successPendingIntent = PendingIntent.getBroadcast(
                context,
                credentialId.hashCode(),
                callbackIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            val extras = Bundle().apply {
                putString(CredentialWidgetPinReceiver.EXTRA_CREDENTIAL_ID, credentialId)
            }

            appWidgetManager.requestPinAppWidget(provider, extras, successPendingIntent)
        } else {
            Toast.makeText(context, R.string.widget_pin_not_supported, Toast.LENGTH_LONG).show()
        }
    }
}
