package com.example.credencialholder.model

import org.json.JSONObject
import java.util.UUID

data class Credential(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val holderName: String = "",
    val category: CredentialCategory = CredentialCategory.IDENTIFICATION,
    val frontImagePath: String,
    val backImagePath: String? = null,
    val identifierNumber: String? = null,
    val issueDate: String? = null,
    val expiryDate: String? = null,
    val notes: String? = null,
    val isFavorite: Boolean = false,
    val accentHex: String = "#2563EB",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("title", title)
        obj.put("holderName", holderName)
        obj.put("category", category.id)
        obj.put("frontImagePath", frontImagePath)
        obj.put("backImagePath", backImagePath ?: JSONObject.NULL)
        obj.put("identifierNumber", identifierNumber ?: JSONObject.NULL)
        obj.put("issueDate", issueDate ?: JSONObject.NULL)
        obj.put("expiryDate", expiryDate ?: JSONObject.NULL)
        obj.put("notes", notes ?: JSONObject.NULL)
        obj.put("isFavorite", isFavorite)
        obj.put("accentHex", accentHex)
        obj.put("createdAt", createdAt)
        obj.put("updatedAt", updatedAt)
        return obj
    }

    companion object {
        fun fromJson(json: JSONObject): Credential {
            return Credential(
                id = json.optString("id", UUID.randomUUID().toString()),
                title = json.optString("title", "Sin título"),
                holderName = json.optString("holderName", ""),
                category = CredentialCategory.fromId(json.optString("category", "id")),
                frontImagePath = json.optString("frontImagePath", ""),
                backImagePath = if (json.has("backImagePath") && !json.isNull("backImagePath")) json.getString("backImagePath") else null,
                identifierNumber = if (json.has("identifierNumber") && !json.isNull("identifierNumber")) json.getString("identifierNumber") else null,
                issueDate = if (json.has("issueDate") && !json.isNull("issueDate")) json.getString("issueDate") else null,
                expiryDate = if (json.has("expiryDate") && !json.isNull("expiryDate")) json.getString("expiryDate") else null,
                notes = if (json.has("notes") && !json.isNull("notes")) json.getString("notes") else null,
                isFavorite = json.optBoolean("isFavorite", false),
                accentHex = json.optString("accentHex", "#2563EB"),
                createdAt = json.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = json.optLong("updatedAt", System.currentTimeMillis())
            )
        }
    }
}
