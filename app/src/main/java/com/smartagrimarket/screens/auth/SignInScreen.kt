package com.smartagrimarket.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.R
import com.smartagrimarket.ui.theme.*

/**
 * Sign-in screen — email/password only.
 * The role is determined by the backend based on the account, not chosen here.
 */
@Composable
fun SignInScreen(
    onSignIn: (identifier: String, password: String) -> Unit = { _, _ -> },
    onForgotPassword: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {}
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showValidationError by remember { mutableStateOf(false) }

    val isFormValid = identifier.isNotBlank() && password.isNotBlank()

    Scaffold(containerColor = AgriCreamBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.agrimarket_logo),
                    contentDescription = "AgriMarket logo",
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .heightIn(max = 100.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Welcome back",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Sign in to continue to AgriMarket",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AgriTextSecondary
                )
            }

            Spacer(Modifier.height(32.dp))

            SectionLabel("EMAIL")
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = identifier,
                onValueChange = { identifier = it },
                placeholder = { Text("thabo@example.com") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = AgriCardBg,
                    focusedContainerColor = AgriCardBg
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            SectionLabel("PASSWORD")
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("••••••••••") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = AgriCardBg,
                    focusedContainerColor = AgriCardBg
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "Forgot password?",
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriTextSecondary,
                    modifier = Modifier.clickable(onClick = onForgotPassword)
                )
            }

            if (showValidationError && !isFormValid) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Enter both your email/phone and password to sign in.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriRedAlert
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (isFormValid) {
                        onSignIn(identifier.trim(), password)
                    } else {
                        showValidationError = true
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgriYellow,
                    contentColor = AgriTextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Sign in", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("New here? ", color = AgriTextSecondary)
                Text(
                    text = "Create an account",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onNavigateToSignUp)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = AgriTextSecondary,
        fontWeight = FontWeight.SemiBold
    )
}