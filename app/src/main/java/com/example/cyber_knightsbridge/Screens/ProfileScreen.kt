package com.example.cyber_knightsbridge.Screens

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val pref = remember { SharedPrefManager(context) }

    var isEditing by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf(pref.getName()) }
    var age by remember { mutableStateOf(pref.getAge().toString()) }
    var department by remember { mutableStateOf(pref.getDepartment()) }
    var photoUri by remember { mutableStateOf(pref.getPhotoUri()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (isEditing) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") })
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = department, onValueChange = { department = it }, label = { Text("Department") })
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = photoUri, onValueChange = { photoUri = it }, label = { Text("Photo URI") })
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                pref.saveProfile(name, age.toIntOrNull() ?: 0, department, photoUri)
                isEditing = false
            }) {
                Text("Save")
            }
        } else {
            Text("👤 Name: $name", style = MaterialTheme.typography.headlineSmall)
            Text("🎂 Age: $age", style = MaterialTheme.typography.bodyLarge)
            Text("🏢 Department: $department", style = MaterialTheme.typography.bodyLarge)
            Text("🖼️ Photo URI: $photoUri", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { isEditing = true }) {
                Text("Edit")
            }
        }
    }
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

