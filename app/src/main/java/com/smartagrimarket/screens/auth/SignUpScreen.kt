package com.smartagrimarket.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.R
import com.smartagrimarket.model.UserRole
import com.smartagrimarket.ui.theme.*

/**
 * Buyer / Farmer / Driver sign-up screen.
 */
data class SignUpFormState(
    val role: UserRole,
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String
)

@Composable
fun SignUpScreen(
    onCancel: () -> Unit = {},
    onCreateAccount: (SignUpFormState) -> Unit = {},
    onNavigateToSignIn: () -> Unit = {}
) {
    var role by remember { mutableStateOf(UserRole.BUYER) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }
    var showValidationError by remember { mutableStateOf(false) }

    val passwordsMatch = password == confirmPassword
    val hasStartedConfirming = confirmPassword.isNotEmpty()

    // Once the user starts typing in Confirm Password, lock the
    // primary Password field so the entered password can't be revealed.
    // Clearing Confirm restores the toggle.
    val passwordRevealAllowed = !hasStartedConfirming
    val effectivePasswordVisible = passwordVisible && passwordRevealAllowed

    val isFormValid = fullName.isNotBlank() &&
            email.isNotBlank() &&
            phone.length == 10 &&
            password.length >= 6 &&
            confirmPassword.isNotEmpty() &&
            passwordsMatch &&
            termsAccepted

    Scaffold(containerColor = AgriCreamBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onCancel)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = AgriTextSecondary)
                Spacer(Modifier.width(4.dp))
                Text("Cancel", color = AgriTextSecondary)
            }

            Spacer(Modifier.height(12.dp))

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
                    text = "Create your account",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Join AgriMarket in a few taps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AgriTextSecondary
                )
            }

            Spacer(Modifier.height(24.dp))

            SectionLabel("I AM SIGNING UP AS")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RoleOptionCard(
                    role = UserRole.BUYER,
                    selected = role == UserRole.BUYER,
                    modifier = Modifier.weight(1f),
                    onClick = { role = UserRole.BUYER }
                )
                RoleOptionCard(
                    role = UserRole.FARMER,
                    selected = role == UserRole.FARMER,
                    modifier = Modifier.weight(1f),
                    onClick = { role = UserRole.FARMER }
                )
                RoleOptionCard(
                    role = UserRole.DRIVER,
                    selected = role == UserRole.DRIVER,
                    modifier = Modifier.weight(1f),
                    onClick = { role = UserRole.DRIVER }
                )
            }

            Spacer(Modifier.height(20.dp))

            LabeledField(
                label = "FULL NAME",
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = "Thabo Nkosi",
                leadingIcon = Icons.Filled.Person
            )

            Spacer(Modifier.height(16.dp))

            LabeledField(
                label = "EMAIL",
                value = email,
                onValueChange = { email = it },
                placeholder = "thabo@example.com",
                leadingIcon = Icons.Filled.Email,
                keyboardType = KeyboardType.Email
            )

            Spacer(Modifier.height(16.dp))

            // ── Phone number — max 10 digits ──
            LabeledField(
                label = "PHONE NUMBER",
                value = phone,
                onValueChange = { input ->
                    phone = input.filter { it.isDigit() }.take(10)
                },
                placeholder = "0825550134",
                leadingIcon = Icons.Filled.Phone,
                keyboardType = KeyboardType.Phone
            )

            if (phone.isNotEmpty() && phone.length < 10) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${10 - phone.length} more digit${if (10 - phone.length == 1) "" else "s"} needed",
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriTextSecondary
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Password ──
            SectionLabel("PASSWORD")
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("••••••••••") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    // The reveal toggle disappears once the user starts
                    // typing in Confirm Password. Clearing Confirm brings
                    // it back.
                    if (passwordRevealAllowed) {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Filled.VisibilityOff
                                else
                                    Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible)
                                    "Hide password"
                                else
                                    "Show password"
                            )
                        }
                    }
                },
                visualTransformation = if (effectivePasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = AgriCardBg,
                    focusedContainerColor = AgriCardBg
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (password.isNotEmpty() && password.length < 6) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Use at least 6 characters",
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriTextSecondary
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Confirm Password — no reveal toggle ──
            SectionLabel("CONFIRM PASSWORD")
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = { Text("Re-enter your password") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = hasStartedConfirming && !passwordsMatch,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = AgriCardBg,
                    focusedContainerColor = AgriCardBg,
                    errorContainerColor = AgriCardBg
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (hasStartedConfirming && !passwordsMatch) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Passwords don't match",
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriRedAlert,
                    fontWeight = FontWeight.SemiBold
                )
            } else if (hasStartedConfirming && passwordsMatch) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "✓ Passwords match",
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriGreenDark,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = termsAccepted, onCheckedChange = { termsAccepted = it })
                Text(
                    text = "I agree to the Terms & Conditions and Privacy Policy",
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriTextSecondary
                )
            }

            if (showValidationError && !isFormValid) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Please fill in every field, use a 10-digit phone number, a password with 6+ characters, confirm it correctly, and accept the terms.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriRedAlert
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (isFormValid) {
                        onCreateAccount(SignUpFormState(role, fullName, email, phone, password))
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
                Text("Create account", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Already have an account? ", color = AgriTextSecondary)
                Text(
                    text = "Sign in",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onNavigateToSignIn)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RoleOptionCard(
    role: UserRole,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) AgriYellow.copy(alpha = 0.25f) else AgriCardBg)
            .then(
                if (selected) Modifier.border(2.dp, AgriYellow, RoundedCornerShape(14.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = role.emoji, fontSize = 26.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            text = role.label,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    SectionLabel(label)
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(leadingIcon, contentDescription = null) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = AgriCardBg,
            focusedContainerColor = AgriCardBg
        ),
        modifier = Modifier.fillMaxWidth()
    )
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