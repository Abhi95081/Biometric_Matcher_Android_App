package com.example.cyber_knightsbridge.Screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.example.cyber_knightsbridge.AiModels.RetrofitInstance
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var matchResult by remember { mutableStateOf<FingerprintMatchResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE

    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, storagePermission) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasPermission = it
        if (!it) errorMessage = "Permission denied. Please grant storage access."
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
        matchResult = null
        errorMessage = null
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            selectedImageUri = tempCameraUri
            matchResult = null
            errorMessage = null
        }
    }

    fun createImageUri(): Uri {
        val fileName = "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"
        val file = File(context.cacheDir, fileName)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file).also {
            tempCameraUri = it
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "🔍 Fingerprint Matcher",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.padding(horizontal = 12.dp))
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFBBDEFB))
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            OutlinedButton(
                onClick = {
                    if (!hasPermission) permissionLauncher.launch(storagePermission)
                    else imagePickerLauncher.launch("image/*")
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFFB2EBF2), contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Default.AccountBox, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text("Select Fingerprint from Gallery")
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    tempCameraUri = createImageUri()
                    cameraLauncher.launch(tempCameraUri!!)
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFFFFF59D), contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text("Capture Fingerprint from Camera")
            }

            Spacer(Modifier.height(24.dp))

            selectedImageUri?.let { uri ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4C3)),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data(uri)
                                .crossfade(true)
                                .build()
                        ),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray.copy(alpha = 0.1f))
                    )
                }
            } ?: run {
                Spacer(Modifier.height(220.dp))
                Text("No image selected", color = Color.Gray, fontStyle = FontStyle.Italic)
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
                                val multipart = MultipartBody.Part.createFormData("file", file.name, requestBody)

                                val response = RetrofitInstance.api.matchFingerprint(multipart)
                                if (response.isSuccessful) {
                                    response.body()?.data?.let {
                                        matchResult = FingerprintMatchResult.Success(it.name, it.age, it.photo)
                                    } ?: run {
                                        matchResult = FingerprintMatchResult.NotFound
                                    }
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Upload & Match")
                }
            }

            Spacer(Modifier.height(32.dp))

            when {
                errorMessage != null -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, tint = MaterialTheme.colorScheme.error, contentDescription = null)
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD1C4E9))
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
                    .clip(CircleShape)
                    .background(Color.White, CircleShape)
                    .padding(4.dp)
            )
            Spacer(Modifier.width(20.dp))
            Column {
                Text("Name:", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text(result.name, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF4A148C))
                Spacer(Modifier.height(8.dp))
                Text("Age:", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text(result.age?.toString() ?: "Unknown", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF4A148C))
            }
        }
    }
}


@SuppressLint("Recycle")
fun createTempFileFromUri(context: Context, uri: Uri): File {
    context.contentResolver.openInputStream(uri).use { input ->
        requireNotNull(input) { "Failed to open input stream from URI" }

        val name = getFileName(context, uri).takeIf { it.isNotBlank() } ?: "image_temp.jpg"
        val extension = name.substringAfterLast('.', "")
        val prefix = name.substringBeforeLast('.', "img").takeIf { it.length >= 3 } ?: "img"
        val suffix = if (extension.isNotEmpty()) ".$extension" else ".tmp"

        return File.createTempFile(prefix, suffix, context.cacheDir).apply {
            outputStream().use { output -> input.copyTo(output) }
        }
    }
}

@SuppressLint("Recycle")
fun getFileName(context: Context, uri: Uri): String {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    return cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst() && nameIndex != -1) it.getString(nameIndex) else ""
    } ?: ""
}
