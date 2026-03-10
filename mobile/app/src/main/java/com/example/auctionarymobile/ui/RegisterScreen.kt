package com.example.auctionarymobile.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auctionarymobile.ui.theme.*
import com.example.auctionarymobile.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: MainViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Sign Up", color = PrimaryGold, fontSize = 36.sp, fontWeight = FontWeight.Bold)
            Text(text = "Join the world of Collectioners", color = LightTextSecondary, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(48.dp))


            OutlinedTextField(
                value = username, onValueChange = { username = it },
                label = { Text("Username", color = LightTextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGold, unfocusedBorderColor = DividerColor,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))


            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("E-mail", color = LightTextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGold, unfocusedBorderColor = DividerColor,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))


            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password", color = LightTextSecondary) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Text(
                        text = if (passwordVisible) "Hide" else "Show",
                        color = PrimaryGold,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { passwordVisible = !passwordVisible }
                            .padding(8.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGold, unfocusedBorderColor = DividerColor,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword, onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password", color = LightTextSecondary) },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Text(
                        text = if (confirmPasswordVisible) "Hide" else "Show",
                        color = PrimaryGold,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { confirmPasswordVisible = !confirmPasswordVisible }
                            .padding(8.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGold, unfocusedBorderColor = DividerColor,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage!!, color = StatusError, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    when {
                        username.isBlank() || email.isBlank() || password.isBlank() -> errorMessage = "Fill each blank."
                        password.length < 6 -> errorMessage = "Password must be at least 6 characters."
                        password != confirmPassword -> errorMessage = "Passwords doesn't match!"
                        else -> {
                            errorMessage = null
                            viewModel.register(username, email, password) { success, msg ->
                                if (success) onRegisterSuccess() else errorMessage = msg
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Sign Up", color = DarkBackground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account? ", color = LightTextSecondary, fontSize = 14.sp)
                Text(
                    text = "Login", color = PrimaryGold, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}