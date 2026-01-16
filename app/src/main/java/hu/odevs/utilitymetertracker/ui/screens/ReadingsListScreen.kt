package hu.odevs.utilitymetertracker.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import hu.odevs.utilitymetertracker.viewmodel.MeterReadingViewModel
import hu.odevs.utilitymetertracker.viewmodel.ProviderViewModel
import hu.odevs.utilitymetertracker.data.MeterReadingEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.background



fun copyImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(context.filesDir, "IMG_$timeStamp.jpg")
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun ReadingsListScreen() {
    val context = LocalContext.current
    val readingViewModel: MeterReadingViewModel = viewModel()
    val providerViewModel: ProviderViewModel = viewModel()

    var readingToDelete by remember { mutableStateOf<MeterReadingEntity?>(null) }

    val readings by readingViewModel.readings.observeAsState(emptyList())
    val providers by providerViewModel.providers.observeAsState(emptyList())

    var currentImageReading by remember { mutableStateOf<MeterReadingEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val cameraFile = remember { mutableStateOf<File?>(null) }
    var selectedImagePath by remember { mutableStateOf<String?>(null) }

    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            currentImageReading?.let { reading ->
                cameraFile.value?.absolutePath?.let { path ->
                    readingViewModel.updateImagePath(reading.id, path)
                }
            }
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            currentImageReading?.let { reading ->
                val copiedPath = copyImageToInternalStorage(context, uri)
                copiedPath?.let {
                    readingViewModel.updateImagePath(reading.id, it)
                }
            }
        }
    }

    fun createImageFile(context: Context): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)
    }

    LaunchedEffect(Unit) {
        readingViewModel.loadReadings()
        providerViewModel.loadProviders()
    }

    Column(modifier = Modifier
        .background(Color(0xFFFFF3E0).copy(alpha = 0.95f))
        .padding(16.dp)) {
        Text("List of values", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(readings) { reading ->
                var billAmount by remember { mutableStateOf(reading.billAmount?.toString() ?: "") }
                val providerName = providers.find { it.id == reading.providerId }?.name ?: "Unknown"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Date: ${reading.date}")
                            Text("Value: ${reading.value}")
                            Text("Provider: $providerName")

                            if (reading.imagePath != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(File(reading.imagePath)),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clickable {
                                            selectedImagePath = reading.imagePath
                                        }
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            TextField(
                                value = billAmount,
                                onValueChange = {
                                    billAmount = it
                                    readingViewModel.updateBillAmount(reading.id, it.toDoubleOrNull())
                                },
                                label = { Text("Bill") },
                                modifier = Modifier.width(120.dp)
                            )
                            Row {
                                IconButton(onClick = {
                                    currentImageReading = reading
                                    val file = createImageFile(context)
                                    cameraFile.value = file
                                    cameraImageUri.value = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        file
                                    )
                                    showDialog = true
                                }) {
                                    Icon(Icons.Default.Image, contentDescription = "Add image")
                                }

                                IconButton(onClick = {
                                    readingToDelete = reading
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {},
            title = { Text("Kép hozzáadása") },
            text = {
                Column {
                    TextButton(onClick = {
                        takePictureLauncher.launch(cameraImageUri.value)
                        showDialog = false
                    }) {
                        Text("Fotó készítése")
                    }
                    TextButton(onClick = {
                        pickImageLauncher.launch("image/*")
                        showDialog = false
                    }) {
                        Text("Kép kiválasztása")
                    }
                }
            }
        )
    }
    if (selectedImagePath != null) {
        Dialog(
            onDismissRequest = { selectedImagePath = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .clickable { selectedImagePath = null },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(selectedImagePath),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
    if (readingToDelete != null) {
        AlertDialog(
            onDismissRequest = { readingToDelete = null },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this reading?") },
            confirmButton = {
                TextButton(onClick = {
                    readingToDelete?.let {
                        readingViewModel.deleteReading(it)
                    }
                    readingToDelete = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    readingToDelete = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

}
