package com.example.credencialholder

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object CredentialListKey : NavKey

@Serializable
data class CredentialDetailKey(val credentialId: String) : NavKey

@Serializable
data class CredentialEditKey(val credentialId: String? = null) : NavKey

@Serializable
data class FullScreenPhotoKey(val credentialId: String, val isBack: Boolean = false) : NavKey
