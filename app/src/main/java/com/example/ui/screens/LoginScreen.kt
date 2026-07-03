package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isFirebaseMissing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            FirebaseApp.getInstance()
            // Check if already logged in
            if (FirebaseAuth.getInstance().currentUser != null) {
                onLoginSuccess()
            }
        } catch (e: IllegalStateException) {
            isFirebaseMissing = true
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "RepoMuse Sync",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Log in with GitHub to sync your portfolio case studies across devices.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (isFirebaseMissing) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "Firebase is not configured. Please add your google-services.json file to the app folder to enable authentication.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Button(
                onClick = {
                    if (isFirebaseMissing) return@Button
                    isLoading = true
                    errorMessage = ""
                    val provider = OAuthProvider.newBuilder("github.com")
                    val auth = FirebaseAuth.getInstance()
                    
                    val pendingResultTask = auth.pendingAuthResult
                    if (pendingResultTask != null) {
                        pendingResultTask.addOnSuccessListener {
                            isLoading = false
                            onLoginSuccess()
                        }.addOnFailureListener {
                            isLoading = false
                            errorMessage = it.message ?: "Login failed"
                        }
                    } else {
                        val activity = context.findActivity()
                        if (activity != null) {
                            auth.startActivityForSignInWithProvider(activity, provider.build())
                                .addOnSuccessListener {
                                    isLoading = false
                                    onLoginSuccess()
                                }
                                .addOnFailureListener {
                                    isLoading = false
                                    errorMessage = it.message ?: "Login failed"
                                }
                        } else {
                            isLoading = false
                            errorMessage = "Could not find Activity context"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && !isFirebaseMissing,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Continue with GitHub")
                }
            }

            if (errorMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onSkip) {
                Text("Continue Offline", color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}
