package com.smartagrimarket.screens.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.smartagrimarket.ui.theme.*

/**
 * Change password. Requires the current password (server-side re-auth,
 * not just a client-side check) plus a new one, confirmed twice.
 */
@Composable
fun ChangePasswordScreen(
    isSaving: Boolean = false,
    errorMessage: String? = null,
    successMessage: String? = null,
    onBack: () -> Unit = {},
    onSubmit: (currentPassword: String, newPassword: String) -> Unit = { _, _ -> }
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var showValidationError by remember { mutableStateOf(false) }

    val passwordsMatch = newPassword == confirmPassword
    val isFormValid = currentPassword.isNotBlank() && newPassword.length >= 6 && passwordsMatch

    Scaffold(containerColor = AgriCreamBg) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onBack)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = AgriTextSecondary)
                Spacer(Modifier.width(4.dp))
                Text("Back", color = AgriTextSecondary)
            }

            Spacer(Modifier.height(12.dp))
            Text("Change password", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))

            PasswordField(
                label = "Current password",
                value = currentPassword,
                onValueChange = { currentPassword = it },
                visible = currentVisible,
                onToggleVisible = { currentVisible = !currentVisible }
            )
            Spacer(Modifier.height(16.dp))
            PasswordField(
                label = "New password",
                value = newPassword,
                onValueChange = { newPassword = it },
                visible = newVisible,
                onToggleVisible = { newVisible = !newVisible }
            )
            Spacer(Modifier.height(16.dp))
            PasswordField(
                label = "Confirm new password",
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                visible = newVisible,
                onToggleVisible = { newVisible = !newVisible }
            )

            if (showValidationError && !isFormValid) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = when {
                        currentPassword.isBlank() -> "Enter your current password."
                        newPassword.length < 6 -> "New password must be at least 6 characters."
                        !passwordsMatch -> "New passwords don't match."
                        else -> "Please check the form."
                    },
                    color = AgriRedAlert,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (errorMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(errorMessage, color = AgriRedAlert, style = MaterialTheme.typography.bodySmall)
            }
            if (successMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(successMessage, color = AgriGreen, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    if (isFormValid) onSubmit(currentPassword, newPassword) else showValidationError = true
                },
                enabled = !isSaving,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgriYellow, contentColor = AgriTextPrimary),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = AgriTextPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Updating…", fontWeight = FontWeight.Bold)
                } else {
                    Text("Update password", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisible: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onToggleVisible) {
                Icon(
                    imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (visible) "Hide" else "Show"
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = AgriCardBg,
            focusedContainerColor = AgriCardBg
        ),
        modifier = Modifier.fillMaxWidth()
    )
}