package com.example.credencialholder.widget

import android.content.Context
import android.content.SharedPreferences

class WidgetPrefsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("credential_widget_prefs", Context.MODE_PRIVATE)

    fun setCredentialId(appWidgetId: Int, credentialId: String) {
        prefs.edit().putString("widget_${appWidgetId}_cred_id", credentialId).apply()
    }

    fun getCredentialId(appWidgetId: Int): String? {
        return prefs.getString("widget_${appWidgetId}_cred_id", null)
    }

    fun setShowBack(appWidgetId: Int, showBack: Boolean) {
        prefs.edit().putBoolean("widget_${appWidgetId}_show_back", showBack).apply()
    }

    fun getShowBack(appWidgetId: Int): Boolean {
        return prefs.getBoolean("widget_${appWidgetId}_show_back", false)
    }

    fun removeWidget(appWidgetId: Int) {
        prefs.edit()
            .remove("widget_${appWidgetId}_cred_id")
            .remove("widget_${appWidgetId}_show_back")
            .apply()
    }

    fun getAllWidgetIdsForCredential(credentialId: String): List<Int> {
        val result = mutableListOf<Int>()
        for ((key, value) in prefs.all) {
            if (key.startsWith("widget_") && key.endsWith("_cred_id") && value == credentialId) {
                val idStr = key.removePrefix("widget_").removeSuffix("_cred_id")
                idStr.toIntOrNull()?.let { result.add(it) }
            }
        }
        return result
    }
}
