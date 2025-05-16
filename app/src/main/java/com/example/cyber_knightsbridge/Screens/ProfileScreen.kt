package com.example.cyber_knightsbridge.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("👤 Welcome to Profile Screen", style = MaterialTheme.typography.headlineMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenMockPreview() {
    MaterialTheme {
        Column(Modifier.padding(16.dp)) {
            Button(onClick = {}) { Text("Select Fingerprint Image") }
            Spacer(Modifier.height(16.dp))
            Button(onClick = {}, enabled = true) { Text("Upload & Match") }
            Spacer(Modifier.height(16.dp))
            Text("Preview Result")
        }
    }
}
