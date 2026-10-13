package com.example.credencialholder.ui.detail

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.credencialholder.data.CredentialRepository
import com.example.credencialholder.model.Credential
import com.example.credencialholder.ui.components.CredentialCardView
import com.example.credencialholder.ui.components.ExpirationBadge
import com.example.credencialholder.widget.WidgetPinHelper
import com.example.credencialholder.widget.WidgetPrefsManager
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialDetailScreen(
    credentialId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onFullScreen: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val repository = remember { CredentialRepository.getInstance(context) }
    val allCredentials by repository.credentialsFlow.collectAsState(initial = emptyList())
    val credential = remember(allCredentials, credentialId) {
        allCredentials.firstOrNull { it.id == credentialId }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    val prefsManager = remember { WidgetPrefsManager(context) }
    val activeWidgetCount = remember(credential) {
        if (credential != null) prefsManager.getAllWidgetIdsForCredential(credential.id).size else 0
    }

    if (credential == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Credencial no encontrada")
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBack) { Text("Volver") }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(credential.title, maxLines = 1, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { repository.toggleFavorite(credential.id) }) {
                        Icon(
                            if (credential.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorito",
                            tint = if (credential.isFavorite) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onEdit(credential.id) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card Preview with 3D Flip
            CredentialCardView(
                credential = credential,
                onClick = { /* already in detail */ },
                onToggleFavorite = { repository.toggleFavorite(credential.id) },
                onFullScreen = { isBack -> onFullScreen(credential.id, isBack) }
            )

            // Pin to Home Screen as Widget Button (Highlighted Call to Action)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Widgets,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Widget de Inicio Redimensionable",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (activeWidgetCount > 0)
                                "Fijado en $activeWidgetCount widget(s) de inicio"
                            else
                                "Ten esta credencial a la mano en tu pantalla",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = {
                            WidgetPinHelper.pinCredentialToHomeScreen(
                                context,
                                credential.id,
                                credential.title
                            )
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Fijar Widget", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Quick Actions Bar: Full Screen & Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onFullScreen(credential.id, false) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Fullscreen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ver Grande", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = {
                        shareCredentialPhoto(context, credential)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Compartir", fontSize = 13.sp)
                }
            }

            // Details Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Información de la Credencial",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Titular
                    if (credential.holderName.isNotEmpty()) {
                        DetailItem(
                            label = "Nombre del Titular",
                            value = credential.holderName,
                            icon = Icons.Default.Person
                        )
                    }

                    // Identifier / Number
                    if (!credential.identifierNumber.isNullOrEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DetailItem(
                                label = "Folio / Identificador",
                                value = credential.identifierNumber,
                                icon = Icons.Default.Numbers,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(credential.identifierNumber))
                                Toast.makeText(context, "Folio copiado al portapapeles", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copiar",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Category
                    DetailItem(
                        label = "Categoría",
                        value = credential.category.title,
                        icon = credential.category.getIcon(),
                        valueColor = credential.category.getColor()
                    )

                    // Dates
                    if (!credential.issueDate.isNullOrEmpty()) {
                        DetailItem(
                            label = "Fecha de Emisión",
                            value = credential.issueDate,
                            icon = Icons.Default.CalendarToday
                        )
                    }

                    if (!credential.expiryDate.isNullOrEmpty()) {
                        Column {
                            DetailItem(
                                label = "Fecha de Expiración / Vigencia",
                                value = credential.expiryDate,
                                icon = Icons.Default.EventBusy
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            ExpirationBadge(expiryDateStr = credential.expiryDate)
                        }
                    }

                    // Notes
                    if (!credential.notes.isNullOrEmpty()) {
                        DetailItem(
                            label = "Notas y Observaciones",
                            value = credential.notes,
                            icon = Icons.Default.Notes
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar credencial?") },
            text = { Text("Se eliminará esta credencial y sus fotos. Si está en un widget de tu pantalla de inicio, el widget se reiniciará.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        repository.deleteCredential(credential.id)
                        showDeleteDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun DetailItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = valueColor
            )
        }
    }
}

private fun shareCredentialPhoto(context: android.content.Context, credential: Credential) {
    val file = File(credential.frontImagePath)
    if (!file.exists()) {
        Toast.makeText(context, "No se encontró el archivo de la foto", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Credencial: ${credential.title} - ${credential.holderName}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Compartir credencial"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error al compartir foto: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
