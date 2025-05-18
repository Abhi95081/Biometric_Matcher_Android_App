package com.example.cyber_knightsbridge.Screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.cyber_knightsbridge.R
import kotlinx.coroutines.launch

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
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            photoUri = it.toString()
        }
    }

    // Background gradient
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE3F2FD), Color(0xFFFFFDE7))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Profile Image inside card
            Card(
                modifier = Modifier
                    .size(160.dp)
                    .clickable(enabled = isEditing) { imagePickerLauncher.launch("image/*") },
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val painter = if (photoUri.isNotEmpty())
                        rememberAsyncImagePainter(photoUri)
                    else painterResource(R.drawable.img_1)

                    Image(
                        painter = painter,
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isEditing) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset((-10).dp, (-10).dp)
                                .size(32.dp)
                                .background(Color(0xFF64B5F6), shape = CircleShape)
                                .padding(6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            if (isEditing) {
                AnimatedEditFields(
                    name = name,
                    age = age,
                    department = department,
                    onNameChange = { name = it },
                    onAgeChange = { age = it },
                    onDeptChange = { department = it }
                )

                GradientButton(
                    text = "Save Changes",
                    icon = Icons.Default.Check,
                    gradient = Brush.horizontalGradient(listOf(Color(0xFF64B5F6), Color(0xFF81C784))),
                    onClick = {
                        pref.saveProfile(name, age.toIntOrNull() ?: 0, department, photoUri)
                        isEditing = false
                    }
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = name.ifBlank { "Your Name" },
                        fontSize = 26.sp,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Age: ${age.ifBlank { "--" }}", color = Color.DarkGray, fontSize = 16.sp)
                    Text("Department: ${department.ifBlank { "--" }}", color = Color.DarkGray, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))

                GradientButton(
                    text = "Edit Profile",
                    icon = Icons.Default.Edit,
                    gradient = Brush.horizontalGradient(listOf(Color(0xFFFF8A65), Color(0xFFFFD54F))),
                    onClick = { isEditing = true }
                )
            }
        }
    }
}
@Composable
fun GradientButton(
    text: String,
    icon: ImageVector,
    gradient: Brush,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.White)
            }
        }
    }
}


@Composable
fun AnimatedEditFields(
    name: String,
    age: String,
    department: String,
    onNameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onDeptChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GradientOutlinedField(label = "Full Name", value = name, onValueChange = onNameChange)
        GradientOutlinedField(label = "Age", value = age, onValueChange = onAgeChange)
        GradientOutlinedField(label = "Department", value = department, onValueChange = onDeptChange)
    }
}

@Composable
fun GradientOutlinedField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.Black) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF42A5F5),
            unfocusedBorderColor = Color(0xFF90CAF9),
            cursorColor = Color.Black, // Make cursor white too
            focusedTextColor = Color.Blue,
            unfocusedTextColor = Color.Black,
            focusedLabelColor = Color(0xFF42A5F5),
            unfocusedLabelColor = Color(0xFF90CAF9),
        ),
        textStyle = TextStyle(fontSize = 16.sp)
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

// Shared Pref Manager
class SharedPrefManager(context: Context) {
    private val prefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)

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
