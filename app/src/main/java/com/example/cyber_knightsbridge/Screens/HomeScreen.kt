package com.example.cyber_knightsbridge.Screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.cyber_knightsbridge.AiModels.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var resultText by remember { mutableStateOf("No result") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val storagePermission = if (android.os.Build.VERSION.SDK_INT >= 33) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasStoragePermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, storagePermission) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasStoragePermission = granted
        println("Permission granted? $granted")
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedImageUri = uri
        println("Image selected: $uri")
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Has permission: $hasStoragePermission")

        Button(onClick = {
            println("Button clicked, hasPermission=$hasStoragePermission")
            if (!hasStoragePermission) {
                permissionLauncher.launch(storagePermission)
            } else {
                launcher.launch("image/*")
            }
        }) {
            Text("Select Fingerprint Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                selectedImageUri?.let { uri ->
                    scope.launch(Dispatchers.IO) {
                        withContext(Dispatchers.Main) { isLoading = true }
                        try {
                            val file = createTempFileFromUri(context, uri)
                            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                            val response = RetrofitInstance.api.matchFingerprint(body)
                            withContext(Dispatchers.Main) {
                                resultText = if (response.isSuccessful) {
                                    val profile = response.body()
                                    "Name: ${profile?.name}\nAge: ${profile?.age}"
                                } else {
                                    "No match found"
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                resultText = "Error: ${e.message}"
                            }
                        }
                        withContext(Dispatchers.Main) { isLoading = false }
                    }
                } ?: run {
                    resultText = "Please select an image first"
                }
            },
            enabled = selectedImageUri != null && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Upload & Match")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(resultText)
    }
}

fun createTempFileFromUri(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: throw Exception("Failed to open input stream")

    val fileName = getFileName(context, uri)
    val extension = fileName.substringAfterLast('.', "")
    val suffix = if (extension.isNotEmpty()) ".$extension" else ".tmp"

    // prefix must be >= 3 chars for createTempFile
    val prefixRaw = fileName.substringBeforeLast('.', fileName)
    val prefix = if (prefixRaw.length >= 3) prefixRaw else "file"

    val tempFile = File.createTempFile(prefix, suffix, context.cacheDir)
    tempFile.outputStream().use { output ->
        inputStream.copyTo(output)
    }
    return tempFile
}

fun getFileName(context: Context, uri: Uri): String {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst() && nameIndex != -1) {
            return it.getString(nameIndex)
        }
    }
    // fallback filename
    return "temp_file.jpg"
}
