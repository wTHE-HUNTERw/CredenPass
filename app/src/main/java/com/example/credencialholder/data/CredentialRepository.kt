package com.example.credencialholder.data

import android.content.Context
import android.graphics.Color
import com.example.credencialholder.model.Credential
import com.example.credencialholder.model.CredentialCategory
import com.example.credencialholder.widget.CredentialWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.util.UUID

class CredentialRepository(
    private val context: Context,
    val imageStorageManager: ImageStorageManager
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val storeFile = File(context.filesDir, "credentials_store.json")
    private val prefs = context.getSharedPreferences("cred_repo_prefs", Context.MODE_PRIVATE)

    private val _credentialsFlow = MutableStateFlow<List<Credential>>(emptyList())
    val credentialsFlow: Flow<List<Credential>> = _credentialsFlow.asStateFlow()

    init {
        loadFromDisk()
    }

    private fun loadFromDisk() {
        val migratedToSingleSample = prefs.getBoolean("samples_single_v2", false)

        if (!storeFile.exists() || !migratedToSingleSample) {
            // First run or migration to single sample: populate only 1 realistic sample
            prefs.edit().putBoolean("samples_single_v2", true).apply()
            populateInitialSamples()
            return
        }

        try {
            val text = storeFile.readText()
            val array = JSONArray(text)
            val list = mutableListOf<Credential>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(Credential.fromJson(obj))
            }
            _credentialsFlow.value = list.sortedByDescending { it.updatedAt }
        } catch (e: Exception) {
            e.printStackTrace()
            _credentialsFlow.value = emptyList()
        }
    }

    private fun persistToDisk(list: List<Credential>) {
        try {
            val array = JSONArray()
            for (c in list) {
                array.put(c.toJson())
            }
            val tempFile = File(context.filesDir, "credentials_store.tmp")
            tempFile.writeText(array.toString(2))
            if (storeFile.exists()) storeFile.delete()
            tempFile.renameTo(storeFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getAllCredentials(): List<Credential> = withContext(Dispatchers.IO) {
        _credentialsFlow.value
    }

    fun getCredentialById(id: String): Credential? {
        return _credentialsFlow.value.firstOrNull { it.id == id }
    }

    fun saveCredential(credential: Credential) {
        scope.launch {
            val current = _credentialsFlow.value.toMutableList()
            val index = current.indexOfFirst { it.id == credential.id }
            val updated = credential.copy(updatedAt = System.currentTimeMillis())

            if (index >= 0) {
                current[index] = updated
            } else {
                current.add(0, updated)
            }

            _credentialsFlow.value = current.sortedByDescending { it.updatedAt }
            persistToDisk(current)

            // Notify home screen widgets that credential was updated
            CredentialWidgetProvider.updateAllWidgets(context)
        }
    }

    fun deleteCredential(id: String) {
        scope.launch {
            val current = _credentialsFlow.value.toMutableList()
            val toRemove = current.firstOrNull { it.id == id }
            if (toRemove != null) {
                imageStorageManager.deleteCredentialImages(toRemove.frontImagePath, toRemove.backImagePath)
                current.remove(toRemove)
                _credentialsFlow.value = current
                persistToDisk(current)

                // Refresh widgets
                CredentialWidgetProvider.updateAllWidgets(context)
            }
        }
    }

    fun toggleFavorite(id: String) {
        scope.launch {
            val current = _credentialsFlow.value.toMutableList()
            val index = current.indexOfFirst { it.id == id }
            if (index >= 0) {
                val item = current[index]
                current[index] = item.copy(isFavorite = !item.isFavorite, updatedAt = System.currentTimeMillis())
                _credentialsFlow.value = current.sortedByDescending { it.updatedAt }
                persistToDisk(current)
            }
        }
    }

    /**
     * Creates only 1 realistic sample credential with both Front and Back photos
     * so the user can immediately test front and back flip on the widget and in app.
     */
    fun populateInitialSamples() {
        scope.launch {
            val list = mutableListOf<Credential>()

            // Única Credencial de Ejemplo: Identificación Oficial (INE / DNI)
            val id1 = UUID.randomUUID().toString()
            val front1 = imageStorageManager.createSampleCardImage(
                credentialId = id1,
                isBack = false,
                title = "Credencial de Elector / INE",
                name = "CARLOS MENDOZA RÍOS",
                number = "IDMEX 1984021984<<8746",
                primaryHex = Color.rgb(30, 58, 138), // Deep Blue
                secondaryHex = Color.rgb(239, 68, 68) // Red
            )
            val back1 = imageStorageManager.createSampleCardImage(
                credentialId = id1,
                isBack = true,
                title = "Credencial de Elector",
                name = "CARLOS MENDOZA RÍOS",
                number = "04829104810294",
                primaryHex = Color.rgb(30, 58, 138),
                secondaryHex = Color.rgb(239, 68, 68)
            )

            list.add(
                Credential(
                    id = id1,
                    title = "INE / Identificación Oficial",
                    holderName = "Carlos Mendoza Ríos",
                    category = CredentialCategory.IDENTIFICATION,
                    frontImagePath = front1,
                    backImagePath = back1,
                    identifierNumber = "IDMEX 1984021984",
                    issueDate = "2022-04-15",
                    expiryDate = "2032-12-31",
                    notes = "Identificación oficial con fotografía vigente. Tiene anverso y reverso.",
                    isFavorite = true,
                    accentHex = "#2563EB"
                )
            )

            _credentialsFlow.value = list
            persistToDisk(list)
            CredentialWidgetProvider.updateAllWidgets(context)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: CredentialRepository? = null

        fun getInstance(context: Context): CredentialRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CredentialRepository(
                    context.applicationContext,
                    ImageStorageManager(context.applicationContext)
                ).also { INSTANCE = it }
            }
        }
    }
}
