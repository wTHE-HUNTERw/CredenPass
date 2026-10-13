package com.example.credencialholder.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.example.credencialholder.MainActivity
import com.example.credencialholder.R
import com.example.credencialholder.data.CredentialRepository
import com.example.credencialholder.data.ImageStorageManager

class CredentialWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_FLIP_CARD) {
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val prefs = WidgetPrefsManager(context)
                val currentShowBack = prefs.getShowBack(appWidgetId)
                prefs.setShowBack(appWidgetId, !currentShowBack)

                val appWidgetManager = AppWidgetManager.getInstance(context)
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        // Called when user resizes the widget on home screen
        updateWidget(context, appWidgetManager, appWidgetId)
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val prefs = WidgetPrefsManager(context)
        for (id in appWidgetIds) {
            prefs.removeWidget(id)
        }
    }

    companion object {
        const val EXTRA_CREDENTIAL_ID = "credential_id"
        const val ACTION_FLIP_CARD = "com.example.credencialholder.ACTION_FLIP_CARD"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, CredentialWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (id in appWidgetIds) {
                updateWidget(context, appWidgetManager, id)
            }
        }

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val prefs = WidgetPrefsManager(context)
            val credentialId = prefs.getCredentialId(appWidgetId)
            val showBack = prefs.getShowBack(appWidgetId)

            val views = RemoteViews(context.packageName, R.layout.widget_credential_layout)

            if (credentialId.isNullOrEmpty()) {
                // Not yet configured: show empty prompt to configure
                views.setViewVisibility(R.id.widget_empty_view, View.VISIBLE)
                views.setViewVisibility(R.id.widget_credential_image, View.GONE)
                views.setViewVisibility(R.id.widget_overlay_banner, View.GONE)
                views.setViewVisibility(R.id.widget_side_badge, View.GONE)

                val configIntent = Intent(context, CredentialWidgetConfigureActivity::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val configPending = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    configIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, configPending)
                views.setOnClickPendingIntent(R.id.widget_empty_view, configPending)
                appWidgetManager.updateAppWidget(appWidgetId, views)
                return
            }

            val repository = CredentialRepository.getInstance(context)
            val credential = repository.getCredentialById(credentialId)

            if (credential == null) {
                // Credential was deleted
                views.setViewVisibility(R.id.widget_empty_view, View.VISIBLE)
                views.setViewVisibility(R.id.widget_credential_image, View.GONE)
                views.setViewVisibility(R.id.widget_overlay_banner, View.GONE)
                views.setViewVisibility(R.id.widget_side_badge, View.GONE)

                val configIntent = Intent(context, CredentialWidgetConfigureActivity::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                val configPending = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    configIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, configPending)
                views.setOnClickPendingIntent(R.id.widget_empty_view, configPending)
                appWidgetManager.updateAppWidget(appWidgetId, views)
                return
            }

            // Normal state: display credential
            views.setViewVisibility(R.id.widget_empty_view, View.GONE)
            views.setViewVisibility(R.id.widget_credential_image, View.VISIBLE)
            views.setViewVisibility(R.id.widget_overlay_banner, View.VISIBLE)
            views.setViewVisibility(R.id.widget_side_badge, View.VISIBLE)

            val hasBackImage = !credential.backImagePath.isNullOrEmpty()
            val isCurrentlyBack = showBack && hasBackImage

            // Side Badge with clear instruction
            if (hasBackImage) {
                if (isCurrentlyBack) {
                    views.setTextViewText(R.id.widget_side_badge, "🔄 REVERSO • Toca para voltear")
                } else {
                    views.setTextViewText(R.id.widget_side_badge, "🪪 FRENTE • Toca para voltear")
                }
            } else {
                views.setTextViewText(R.id.widget_side_badge, "🪪 FRENTE")
            }

            // Load and display the active face image (Front or Back)
            val imagePath = if (isCurrentlyBack) credential.backImagePath else credential.frontImagePath
            val storageManager = ImageStorageManager(context)
            val bitmap = storageManager.loadBitmap(imagePath, reqWidth = 900, reqHeight = 600)
            if (bitmap != null) {
                views.setImageViewBitmap(R.id.widget_credential_image, bitmap)
            } else {
                views.setImageViewResource(R.id.widget_credential_image, R.drawable.ic_card_placeholder)
            }

            // Titles
            views.setTextViewText(R.id.widget_credential_title, credential.title)
            val subText = if (credential.holderName.isNotEmpty()) {
                "${credential.holderName} • ${credential.category.title}"
            } else {
                credential.category.title
            }
            views.setTextViewText(R.id.widget_credential_holder, subText)

            // 1. Flip Intent: Pressing anywhere on the card photo or badge flips the card!
            if (hasBackImage) {
                val flipIntent = Intent(context, CredentialWidgetProvider::class.java).apply {
                    action = ACTION_FLIP_CARD
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                val flipPending = PendingIntent.getBroadcast(
                    context,
                    appWidgetId * 100 + 1,
                    flipIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_credential_image, flipPending)
                views.setOnClickPendingIntent(R.id.widget_side_badge, flipPending)
            } else {
                // If only 1 side exists, clicking the image opens the app
                val openIntent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(EXTRA_CREDENTIAL_ID, credential.id)
                }
                val openPending = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_credential_image, openPending)
            }

            // 2. Open App Intent: ONLY clicking the dedicated "🔍 Abrir App" button opens the app!
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_CREDENTIAL_ID, credential.id)
            }
            val openAppPending = PendingIntent.getActivity(
                context,
                appWidgetId * 100 + 2,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_open_app, openAppPending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
