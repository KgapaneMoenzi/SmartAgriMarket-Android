package com.smartagrimarket.screens.farmer

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.VerificationRepository
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun FarmerVerificationScreen(
    onBack: () -> Unit = {},
    onSubmitted: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var idUri by remember { mutableStateOf<Uri?>(null) }
    var certUri by remember { mutableStateOf<Uri?>(null) }

    var farmName by remember { mutableStateOf("") }
    var farmTown by remember { mutableStateOf("") }
    var farmCrops by remember { mutableStateOf("") }
    var farmSize by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val pickId = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { idUri = it }

    val pickCert = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { certUri = it }

    val canSubmit = idUri != null &&
            certUri != null &&
            farmName.isNotBlank() &&
            farmTown.isNotBlank() &&
            !isSubmitting

    Scaffold(containerColor = AgriCreamBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Top bar ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AgriCardBg)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text("←", fontSize = 20.sp, color = AgriTextPrimary)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "Verify your account",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "To sell on SmartAgriMarket, we need to verify your identity and farm details. " +
                        "Please fill in all fields and upload both documents.",
                style = MaterialTheme.typography.bodySmall,
                color = AgriTextSecondary
            )

            Spacer(Modifier.height(24.dp))

            // ── Section 1: Farm details ──
            SectionHeader(number = "1", title = "Farm details")
            Spacer(Modifier.height(6.dp))
            Text(
                "This information appears on your public farmer profile.",
                style = MaterialTheme.typography.bodySmall,
                color = AgriTextSecondary
            )
            Spacer(Modifier.height(14.dp))

            VerificationField(
                value = farmName,
                onValueChange = { farmName = it },
                label = "Farm name *",
                placeholder = "e.g. Green Valley Farm"
            )
            Spacer(Modifier.height(10.dp))
            VerificationField(
                value = farmTown,
                onValueChange = { farmTown = it },
                label = "Town / region *",
                placeholder = "e.g. Mbombela, Mpumalanga"
            )
            Spacer(Modifier.height(10.dp))
            VerificationField(
                value = farmCrops,
                onValueChange = { farmCrops = it },
                label = "Primary crops",
                placeholder = "e.g. Maize, Tomatoes, Spinach",
                supporting = "Separate with commas"
            )
            Spacer(Modifier.height(10.dp))
            VerificationField(
                value = farmSize,
                onValueChange = { farmSize = it },
                label = "Farm size (hectares)",
                placeholder = "e.g. 4.5"
            )
            Spacer(Modifier.height(10.dp))
            VerificationField(
                value = phone,
                onValueChange = { phone = it },
                label = "Contact phone number",
                placeholder = "e.g. 082 123 4567"
            )

            Spacer(Modifier.height(28.dp))

            // ── Section 2: ID document ──
            SectionHeader(number = "2", title = "ID document")
            Spacer(Modifier.height(6.dp))
            Text(
                "National ID, passport, or driver's licence. Photo must be legible.",
                style = MaterialTheme.typography.bodySmall,
                color = AgriTextSecondary
            )
            Spacer(Modifier.height(12.dp))
            DocumentPickerButton(
                hasFile = idUri != null,
                emptyLabel = "Choose ID photo",
                emptyIcon = "📄"
            ) { pickId.launch("image/*") }
            idUri?.let {
                Spacer(Modifier.height(10.dp))
                AsyncImage(
                    model = it,
                    contentDescription = "ID preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── Section 3: Certificate ──
            SectionHeader(number = "3", title = "Farming certificate")
            Spacer(Modifier.height(6.dp))
            Text(
                "Any proof that you farm — a certificate, permit, or a photo of your farm gate sign.",
                style = MaterialTheme.typography.bodySmall,
                color = AgriTextSecondary
            )
            Spacer(Modifier.height(12.dp))
            DocumentPickerButton(
                hasFile = certUri != null,
                emptyLabel = "Choose certificate photo",
                emptyIcon = "🌾"
            ) { pickCert.launch("image/*") }
            certUri?.let {
                Spacer(Modifier.height(10.dp))
                AsyncImage(
                    model = it,
                    contentDescription = "Certificate preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            if (errorMessage != null) {
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AgriRedAlert.copy(alpha = 0.12f))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚠️", fontSize = 16.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        errorMessage!!,
                        color = AgriRedAlert,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            if (successMessage != null) {
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AgriGreen.copy(alpha = 0.15f))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✅", fontSize = 16.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        successMessage!!,
                        color = AgriGreenDark,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    if (!canSubmit) return@Button
                    errorMessage = null
                    successMessage = null
                    scope.launch {
                        isSubmitting = true

                        val cropsList = farmCrops
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }

                        when (val r = VerificationRepository.submitVerification(
                            context = context,
                            idDocumentUri = idUri!!,
                            certificateUri = certUri!!,
                            farmName = farmName.trim(),
                            farmTown = farmTown.trim(),
                            farmPrimaryCrops = cropsList,
                            farmSizeHectares = farmSize.trim(),
                            phone = phone.trim()
                        )) {
                            is ApiResult.Success -> {
                                successMessage = "Submitted! We'll review within 24 hours."
                                onSubmitted()
                            }
                            is ApiResult.Error -> errorMessage = r.message
                        }
                        isSubmitting = false
                    }
                },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgriYellow, contentColor = AgriTextPrimary
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = AgriTextPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Submitting…", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                } else {
                    Text("Submit for review", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(number: String, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(AgriGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                number,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = AgriGreenDark
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AgriTextPrimary
        )
    }
}

@Composable
private fun VerificationField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    supporting: String? = null
) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = AgriTextSecondary,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = AgriTextTertiary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = AgriCardBg,
                focusedContainerColor = AgriCardBg,
                unfocusedBorderColor = AgriTextPrimary.copy(alpha = 0.15f),
                focusedBorderColor = AgriGreen,
                cursorColor = AgriGreen
            )
        )
        if (supporting != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                supporting,
                style = MaterialTheme.typography.bodySmall,
                color = AgriTextSecondary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun DocumentPickerButton(
    hasFile: Boolean,
    emptyLabel: String,
    emptyIcon: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().height(50.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = AgriTextPrimary
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (hasFile) AgriGreen.copy(alpha = 0.5f)
            else AgriTextPrimary.copy(alpha = 0.15f)
        )
    ) {
        Text(
            text = if (hasFile) "✓  Change photo" else "$emptyIcon  $emptyLabel",
            fontWeight = FontWeight.SemiBold
        )
    }
}