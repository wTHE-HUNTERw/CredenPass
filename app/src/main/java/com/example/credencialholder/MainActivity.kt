package com.example.credencialholder

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import com.example.credencialholder.theme.CredencialHolderTheme
import com.example.credencialholder.widget.CredentialWidgetProvider

class MainActivity : ComponentActivity() {

    private var targetKey by mutableStateOf<NavKey>(CredentialListKey)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            CredencialHolderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(initialKey = targetKey)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val credId = intent?.getStringExtra(CredentialWidgetProvider.EXTRA_CREDENTIAL_ID)
        if (!credId.isNullOrEmpty()) {
            targetKey = CredentialDetailKey(credId)
        }
    }
}
