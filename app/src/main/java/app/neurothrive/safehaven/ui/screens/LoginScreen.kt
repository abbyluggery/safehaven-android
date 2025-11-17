package app.neurothrive.safehaven.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.neurothrive.safehaven.data.session.UserSession
import app.neurothrive.safehaven.ui.viewmodels.LoginViewModel

/**
 * Login Screen
 * Secure password entry for SafeHaven access
 *
 * CRITICAL: Supports dual password system
 * - Real password: Shows actual data
 * - Duress password: Shows fake/empty data (safety feature)
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    userSession: UserSession = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    var userId by remember { mutableStateOf("default_user") } // TODO: Support multiple users
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Collect state from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val authResult by viewModel.authResult.collectAsState()

    // Handle authentication result
    LaunchedEffect(authResult) {
        when (val result = authResult) {
            is LoginViewModel.AuthResult.Success -> {
                // Set current user in session
                userSession.setCurrentUser(result.userId, result.isDuress)

                if (result.isDuress) {
                    // TODO: Show decoy/empty data mode
                    // Could show a subtle indicator or just navigate normally
                }

                onLoginSuccess()
                viewModel.resetAuthResult()
            }
            is LoginViewModel.AuthResult.Failure -> {
                // Error already shown in UI via uiState.error
            }
            null -> {
                // No result yet
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Text(
                text = "SafeHaven",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible)
                                "Hide password"
                            else
                                "Show password"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.error != null
            )

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Account locked warning
            if (uiState.isLocked) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "🔒 Too many failed attempts. Account locked temporarily.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login Button
            Button(
                onClick = {
                    if (password.isNotBlank()) {
                        viewModel.login(userId, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isAuthenticating && !uiState.isLocked && password.isNotBlank()
            ) {
                if (uiState.isAuthenticating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Unlock SafeHaven")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Duress Password Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🔐 Dual Password Protection",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "If forced to open the app, use your duress password to show empty/fake data.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Failed attempts indicator
            if (uiState.failedAttempts > 0 && !uiState.isLocked) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Failed attempts: ${uiState.failedAttempts}/5",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
