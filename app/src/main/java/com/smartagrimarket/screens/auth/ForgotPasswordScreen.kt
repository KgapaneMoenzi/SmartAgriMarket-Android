package com.smartagrimarket.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smartagrimarket.ui.theme.*

private const val CODE_LENGTH = 6

private enum class ForgotPasswordStep {
    REQUEST_EMAIL,
    RESET_PASSWORD
}

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit = {},
    onSendResetCode: (email: String) -> Unit = {},
    onResetPassword: (email: String, code: String, newPassword: String) -> Unit = { _, _, _ -> }
) {
    var step by remember { mutableStateOf(ForgotPasswordStep.REQUEST_EMAIL) }
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }

    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var resetError by remember { mutableStateOf<String?>(null) }

    Scaffold(containerColor = AgriCreamBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    if (step == ForgotPasswordStep.RESET_PASSWORD) {
                        step = ForgotPasswordStep.REQUEST_EMAIL
                    } else {
                        onBack()
                    }
                }
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = AgriTextSecondary)
                Spacer(Modifier.width(4.dp))
                Text("Back", color = AgriTextSecondary)
            }

            Spacer(Modifier.height(24.dp))

            when (step) {
                ForgotPasswordStep.REQUEST_EMAIL -> {
                    HeaderIcon()
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Forgot password?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Enter the email linked to your account and we'll send you a reset code.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AgriTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(28.dp))

                    SectionLabel("EMAIL")
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        placeholder = { Text("thabo@example.com") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = AgriCardBg,
                            focusedContainerColor = AgriCardBg
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    emailError?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = AgriRedAlert, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (email.isNotBlank() && email.contains("@")) {
                                onSendResetCode(email)
                                step = ForgotPasswordStep.RESET_PASSWORD
                            } else {
                                emailError = "Enter a valid email address."
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
                        Text("Send reset code", fontWeight = FontWeight.Bold)
                    }
                }

                ForgotPasswordStep.RESET_PASSWORD -> {
                    HeaderIcon()
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Reset your password",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Enter the $CODE_LENGTH-digit code sent to $email and choose a new password.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AgriTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(24.dp))

                    SectionLabel("RESET CODE")
                    Spacer(Modifier.height(6.dp))

                    // ── OTP input ──
                    OtpInput(
                        value = code,
                        onValueChange = {
                            resetError = null
                            code = it
                        },
                        length = CODE_LENGTH,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    SectionLabel("NEW PASSWORD")
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            resetError = null
                        },
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

                    Spacer(Modifier.height(16.dp))

                    SectionLabel("CONFIRM NEW PASSWORD")
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            resetError = null
                        },
                        placeholder = { Text("••••••••••") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = AgriCardBg,
                            focusedContainerColor = AgriCardBg
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    resetError?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = AgriRedAlert, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            resetError = when {
                                code.length < CODE_LENGTH -> "Enter all $CODE_LENGTH digits of the code."
                                newPassword.length < 6 -> "Password must be at least 6 characters."
                                newPassword != confirmPassword -> "Passwords don't match."
                                else -> null
                            }
                            if (resetError == null) {
                                onResetPassword(email, code, newPassword)
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
                        Text("Reset password", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Didn't get a code? Resend",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onSendResetCode(email) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeaderIcon() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AgriYellow.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.LockReset,
                contentDescription = null,
                tint = AgriTextPrimary
            )
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