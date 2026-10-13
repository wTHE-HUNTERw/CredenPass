package com.example.credencialholder.ui.edit

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.credencialholder.data.CredentialRepository
import com.example.credencialholder.data.ImageStorageManager
import com.example.credencialholder.model.Credential
import com.example.credencialholder.model.CredentialCategory
import java.io.File
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialEditScreen(
    credentialId: String?,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { CredentialRepository.getInstance(context) }
    val storageManager = remember { ImageStorageManager(context) }

    val existing = remember(credentialId) {
        if (!credentialId.isNullOrEmpty()) repository.getCredentialById(credentialId) else null
    }

    val workingId = remember { existing?.id ?: UUID.randomUUID().toString() }

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var holderName by remember { mutableStateOf(existing?.holderName ?: "") }
    var identifierNumber by remember { mutableStateOf(existing?.identifierNumber ?: "") }
    var selectedCategory by remember { mutableStateOf(existing?.category ?: CredentialCategory.IDENTIFICATION) }
    var issueDate by remember { mutableStateOf(existing?.issueDate ?: "") }
    var expiryDate by remember { mutableStateOf(existing?.expiryDate ?: "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }

    var frontImagePath by remember { mutableStateOf(existing?.frontImagePath ?: "") }
    var backImagePath by remember { mutableStateOf(existing?.backImagePath ?: "") }

    // Dialogs / Pickers
    var targetSideIsBack by remember { mutableStateOf(false) }
    var showPhotoSourceSheet by remember { mutableStateOf(false) }
    var currentCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Gallery Picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = storageManager.saveImageFromUri(uri, workingId, targetSideIsBack)
            if (targetSideIsBack) {
                backImagePath = savedPath
            } else {
                frontImagePath = savedPath
            }
        }
    }

    // Camera Capture launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && currentCameraUri != null) {
            val savedPath = storageManager.saveImageFromUri(currentCameraUri!!, workingId, targetSideIsBack)
            if (targetSideIsBack) {
                backImagePath = savedPath
            } else {
                frontImagePath = savedPath
            }
        }
    }

    fun launchCamera() {
        val tempFile = storageManager.createTempCameraFile()
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
        currentCameraUri = uri
        cameraLauncher.launch(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (existing != null) "Editar Credencial" else "Nueva Credencial",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Cancelar")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isBlank()) {
                                Toast.makeText(context, "Por favor escribe un título para la credencial", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }
                            if (frontImagePath.isBlank()) {
                                Toast.makeText(context, "Por favor toma o selecciona la foto frontal", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }

                            val newCredential = Credential(
                                id = workingId,
                                title = title.trim(),
                                holderName = holderName.trim(),
                                category = selectedCategory,
                                frontImagePath = frontImagePath,
                                backImagePath = backImagePath.ifBlank { null },
                                identifierNumber = identifierNumber.trim().ifBlank { null },
                                issueDate = issueDate.trim().ifBlank { null },
                                expiryDate = expiryDate.trim().ifBlank { null },
                                notes = notes.trim().ifBlank { null },
                                isFavorite = existing?.isFavorite ?: false,
                                accentHex = selectedCategory.hexColor
                            )
                            repository.saveCredential(newCredential)
                            Toast.makeText(context, "Credencial guardada correctamente", Toast.LENGTH_SHORT).show()
                            onSaved(workingId)
                        }
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
            // Photo Selection Section (Front & Back)
            Text(
                text = "Fotos de la Credencial",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            // Front Image Card
            PhotoBox(
                title = "Foto Frontal (Obligatoria)",
                subtitle = "Anverso de la credencial / cara con fotografía",
                imagePath = frontImagePath,
                isRequired = true,
                onPick = {
                    targetSideIsBack = false
                    showPhotoSourceSheet = true
                },
                onClear = { frontImagePath = "" }
            )

            // Back Image Card
            PhotoBox(
                title = "Foto Trasera (Opcional)",
                subtitle = "Reverso / código de barras / firma",
                imagePath = backImagePath,
                isRequired = false,
                onPick = {
                    targetSideIsBack = true
                    showPhotoSourceSheet = true
                },
                onClear = { backImagePath = "" }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Information fields
            Text(
                text = "Detalles de la Credencial",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título de la Credencial *") },
                placeholder = { Text("Ej. INE, Licencia, Gafete Laboral, UNAM...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) }
            )

            // Holder Name
            OutlinedTextField(
                value = holderName,
                onValueChange = { holderName = it },
                label = { Text("Nombre del Titular") },
                placeholder = { Text("Ej. Erick Morales") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
            )

            // Category Picker
            Column {
                Text(
                    text = "Categoría",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CredentialCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            leadingIcon = {
                                Icon(
                                    imageVector = cat.getIcon(),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (selectedCategory == cat) MaterialTheme.colorScheme.primary else cat.getColor()
                                )
                            },
                            label = { Text(cat.title) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Folio / ID Number
            OutlinedTextField(
                value = identifierNumber,
                onValueChange = { identifierNumber = it },
                label = { Text("Número de Folio / Identificador") },
                placeholder = { Text("Ej. IDMEX 1984021984 o LIC-8921") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) }
            )

            // Expiry Date & Issue Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { expiryDate = it },
                    label = { Text("Vigencia (AAAA-MM-DD)") },
                    placeholder = { Text("2028-12-31") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.EventBusy, contentDescription = null) }
                )

                OutlinedTextField(
                    value = issueDate,
                    onValueChange = { issueDate = it },
                    label = { Text("Emisión (Opcional)") },
                    placeholder = { Text("2024-01-15") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
                )
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas u Observaciones") },
                placeholder = { Text("Información adicional, tipo de sangre, sede...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal Bottom Sheet for Camera / Gallery
    if (showPhotoSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoSourceSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (targetSideIsBack) "Foto de Reverso" else "Foto Frontal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )

                ListItem(
                    headlineContent = { Text("Tomar Foto con la Cámara", fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("Abre la cámara para fotografiar tu credencial") },
                    leadingContent = {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showPhotoSourceSheet = false
                            launchCamera()
                        }
                )

                ListItem(
                    headlineContent = { Text("Elegir de la Galería", fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("Selecciona una foto ya guardada en tu teléfono") },
                    leadingContent = {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showPhotoSourceSheet = false
                            galleryLauncher.launch("image/*")
                        }
                )

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun PhotoBox(
    title: String,
    subtitle: String,
    imagePath: String,
    isRequired: Boolean,
    onPick: () -> Unit,
    onClear: () -> Unit
) {
    val bitmap = remember(imagePath) {
        if (imagePath.isNotEmpty() && File(imagePath).exists()) {
            BitmapFactory.decodeFile(imagePath)
        } else null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onPick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isRequired && imagePath.isEmpty()) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (imagePath.isNotEmpty()) {
                    TextButton(onClick = onClear) {
                        Text("Quitar", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.7f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Toca para agregar foto",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
