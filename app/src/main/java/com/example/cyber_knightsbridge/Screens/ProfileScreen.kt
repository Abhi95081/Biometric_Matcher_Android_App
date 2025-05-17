package com.example.cyber_knightsbridge.Screens

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.cyber_knightsbridge.R

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val pref = remember { SharedPrefManager(context) }

    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(pref.getName()) }
    var age by remember { mutableStateOf(pref.getAge().toString()) }
    var department by remember { mutableStateOf(pref.getDepartment()) }
    var photoUri by remember { mutableStateOf(pref.getPhotoUri()) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { photoUri = it.toString() }
    }

    val shimmerBrush = rememberShimmerBrush()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E3C72), Color(0xFF2A5298))
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(12.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile image with shimmer overlay
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .clickable(enabled = isEditing) {
                                imagePickerLauncher.launch("image/*")
                            }
                            .background(shimmerBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        val painter = if (photoUri.isNotEmpty())
                            rememberAsyncImagePainter(photoUri)
                        else
                            painterResource(R.drawable.img_1)

                        Image(
                            painter = painter,
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color(0x80000000))
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isEditing) {
                        GradientOutlinedField(label = "Name", value = name) { name = it }
                        GradientOutlinedField(label = "Age", value = age) { age = it }
                        GradientOutlinedField(label = "Department", value = department) { department = it }

                        Button(
                            onClick = {
                                pref.saveProfile(name, age.toIntOrNull() ?: 0, department, photoUri)
                                isEditing = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("💾 Save", fontSize = 16.sp)
                        }
                    } else {
                        Text("👤 $name", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("🎂 Age: $age", style = MaterialTheme.typography.bodyLarge)
                        Text("🏢 Department: $department", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("🖼️ Tap image to change", style = MaterialTheme.typography.bodySmall)

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { isEditing = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("✏️ Edit", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GradientOutlinedField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF42A5F5),
            unfocusedBorderColor = Color.LightGray
        )
    )
}

@Composable
fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    return Brush.linearGradient(
        colors = listOf(Color.LightGray.copy(alpha = 0.4f), Color.White, Color.LightGray.copy(alpha = 0.4f)),
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 200f, translateAnim + 200f)
    )
}


class SharedPrefManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)

    fun saveProfile(name: String, age: Int, department: String, photoUri: String) {
        prefs.edit()
            .putString("name", name)
            .putInt("age", age)
            .putString("department", department)
            .putString("photoUri", photoUri)
            .apply()
    }

    fun getName(): String = prefs.getString("name", "") ?: ""
    fun getAge(): Int = prefs.getInt("age", 0)
    fun getDepartment(): String = prefs.getString("department", "") ?: ""
    fun getPhotoUri(): String = prefs.getString("photoUri", "") ?: ""
}


