package com.smartagrimarket.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.delay

private const val CODE_LENGTH = 6
private const val RESEND_COOLDOWN_SECONDS = 30

/**
 * "Confirm your email" step shown right after sign-up.
 */
@Composable
fun ConfirmEmailScreen(
    email: String,
    onBack: () -> Unit = {},
    onVerify: (code: String) -> Unit = {},
    onResendCode: () -> Unit = {},
    onChangeEmail: () -> Unit = {}
) {
    var code by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var secondsRemaining by remember { mutableStateOf(RESEND_COOLDOWN_SECONDS) }

    LaunchedEffect(secondsRemaining) {
        if (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining -= 1
        }
    }

    val isComplete = code.length == CODE_LENGTH

    Scaffold(containerColor = AgriCreamBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onBack)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = AgriTextSecondary)
                Spacer(Modifier.width(4.dp))
                Text("Back", color = AgriTextSecondary)
            }

            Spacer(Modifier.height(24.dp))

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
                        Icons.Filled.MarkEmailRead,
                        contentDescription = null,
                        tint = AgriTextPrimary
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Confirm your email",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "We sent a $CODE_LENGTH-digit code to",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AgriTextSecondary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── OTP input ──
            OtpInput(
                value = code,
                onValueChange = {
                    errorMessage = null
                    code = it
                },
                length = CODE_LENGTH,
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = AgriRedAlert, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isComplete) {
                        onVerify(code)
                    } else {
                        errorMessage = "Enter all $CODE_LENGTH digits to continue."
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
                Text("Verify email", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (secondsRemaining > 0) {
                    Text(
                        text = "Resend code in ${secondsRemaining}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = AgriTextSecondary
                    )
                } else {
                    Text(
                        text = "Resend code",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            secondsRemaining = RESEND_COOLDOWN_SECONDS
                            onResendCode()
                        }
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Wrong email? Change it",
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriTextSecondary,
                    modifier = Modifier.clickable(onClick = onChangeEmail)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}