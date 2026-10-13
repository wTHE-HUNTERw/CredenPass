package com.example.credencialholder

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.credencialholder.ui.detail.CredentialDetailScreen
import com.example.credencialholder.ui.edit.CredentialEditScreen
import com.example.credencialholder.ui.fullscreen.FullScreenPhotoScreen
import com.example.credencialholder.ui.main.CredentialListScreen

@Composable
fun MainNavigation(
    initialKey: NavKey = CredentialListKey
) {
    val backStack = rememberNavBackStack(initialKey)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = Modifier.fillMaxSize(),
        entryProvider = entryProvider {
            entry<CredentialListKey> {
                CredentialListScreen(
                    onNavigateToDetail = { id -> backStack.add(CredentialDetailKey(id)) },
                    onNavigateToCreate = { backStack.add(CredentialEditKey(null)) },
                    onNavigateToFullScreen = { id, isBack -> backStack.add(FullScreenPhotoKey(id, isBack)) }
                )
            }
            entry<CredentialDetailKey> { key ->
                CredentialDetailScreen(
                    credentialId = key.credentialId,
                    onBack = { backStack.removeLastOrNull() },
                    onEdit = { id -> backStack.add(CredentialEditKey(id)) },
                    onFullScreen = { id, isBack -> backStack.add(FullScreenPhotoKey(id, isBack)) }
                )
            }
            entry<CredentialEditKey> { key ->
                CredentialEditScreen(
                    credentialId = key.credentialId,
                    onBack = { backStack.removeLastOrNull() },
                    onSaved = { id ->
                        backStack.removeLastOrNull()
                        backStack.add(CredentialDetailKey(id))
                    }
                )
            }
            entry<FullScreenPhotoKey> { key ->
                FullScreenPhotoScreen(
                    credentialId = key.credentialId,
                    initialIsBack = key.isBack,
                    onClose = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
