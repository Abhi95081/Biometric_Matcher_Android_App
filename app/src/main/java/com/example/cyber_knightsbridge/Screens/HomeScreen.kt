package com.example.cyber_knightsbridge.Screens

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.example.cyber_knightsbridge.AiModels.RetrofitInstance
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var matchResult by remember { mutableStateOf<FingerprintMatchResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, storagePermission) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        errorMessage = if (!granted) "Permission denied. Please allow storage permission." else null
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
        matchResult = null
        errorMessage = null
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Fingerprint Matcher", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "App Icon",
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(24.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedButton(
                    onClick = {
                        if (!hasPermission) {
                            permissionLauncher.launch(storagePermission)
                        } else {
                            imagePickerLauncher.launch("image/*")
                        }
                    },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("Select Fingerprint Image")
                }

                Spacer(Modifier.height(24.dp))

                if (selectedImageUri != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(12.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(context)
                                    .data(selectedImageUri)
                                    .crossfade(true)
                                    .build()
                            ),
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.LightGray.copy(alpha = 0.1f))
                        )
                    }
                } else {
                    Spacer(Modifier.height(220.dp))
                    Text(
                        "No image selected",
                        color = Color.Gray,
                        fontStyle = FontStyle.Italic
                    )
                }

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = {
                        selectedImageUri?.let { uri ->
                            coroutineScope.launch {
                                isLoading = true
                                errorMessage = null
                                matchResult = null
                                try {
                                    val file = createTempFileFromUri(context, uri)
                                    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                                    val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestBody)

                                    val response = RetrofitInstance.api.matchFingerprint(multipartBody)
                                    if (response.isSuccessful) {
                                        val profile = response.body()?.data
                                        profile?.let {
                                            Log.d("FingerprintMatch", "Name: ${it.name}, Age: ${it.age}, Photo: ${it.photo}")
                                        }
                                        matchResult = if (profile != null)
                                            FingerprintMatchResult.Success(profile.name, profile.age, profile.photo)
                                        else FingerprintMatchResult.NotFound
                                    } else {
                                        errorMessage = "Server error: ${response.code()} - ${response.message()}"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Error: ${e.localizedMessage ?: "Unknown error"}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    enabled = selectedImageUri != null && !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Upload & Match")
                    }
                }

                Spacer(Modifier.height(32.dp))

                when {
                    errorMessage != null -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Close, tint = MaterialTheme.colorScheme.error, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    matchResult != null -> {
                        when (val result = matchResult!!) {
                            is FingerprintMatchResult.Success -> ProfileCard(result)
                            is FingerprintMatchResult.NotFound -> Text(
                                "No matching profile found.",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    )
}

sealed class FingerprintMatchResult {
    data class Success(val name: String, val age: Int?, val photo: String) : FingerprintMatchResult()
    object NotFound : FingerprintMatchResult()
}

@Composable
fun ProfileCard(result: FingerprintMatchResult.Success) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(result.photo)
                        .crossfade(true)
                        .transformations(CircleCropTransformation())
                        .build()
                ),
                contentDescription = "User Photo",
                modifier = Modifier
                    .size(90.dp)
                    .background(Color.White, CircleShape)
                    .padding(4.dp)
            )
            Spacer(Modifier.width(20.dp))
            Column {
                Text("Name:", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                Text(result.name, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Spacer(Modifier.height(8.dp))
//                Text("Age:", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
//                Text(result.age?.toString() ?: "Unknown", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            }
        }
    }
}

@SuppressLint("Recycle")
fun createTempFileFromUri(context: Context, uri: Uri): File {
    context.contentResolver.openInputStream(uri).use { inputStream ->
        requireNotNull(inputStream) { "Failed to open input stream from URI" }

        val fileName = getFileName(context, uri).takeIf { it.isNotBlank() } ?: "image_temp.jpg"
        val extension = fileName.substringAfterLast('.', "")
        val suffix = if (extension.isNotEmpty()) ".$extension" else ".tmp"
        val prefixRaw = fileName.substringBeforeLast('.', fileName)
        val prefix = if (prefixRaw.length >= 3) prefixRaw else "img"

        val tempFile = File.createTempFile(prefix, suffix, context.cacheDir)
        tempFile.outputStream().use { output -> inputStream.copyTo(output) }
        return tempFile
    }
}

@SuppressLint("Recycle")
fun getFileName(context: Context, uri: Uri): String {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst() && nameIndex != -1) {
            return it.getString(nameIndex)
        }
    }
    return ""
}
