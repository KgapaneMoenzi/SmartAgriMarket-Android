package com.smartagrimarket.screens.driver

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.DriverRepository
import com.smartagrimarket.model.network.DriverVerificationMeResponse
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DriverVerificationScreen(
    onBack: () -> Unit,
    onSubmitted: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var existingStatus by remember { mutableStateOf<String>("not_submitted") }
    var existingVerification by remember { mutableStateOf<DriverVerificationMeResponse?>(null) }
    var isLoadingStatus by remember { mutableStateOf(true) }

    var licenceUri by remember { mutableStateOf<Uri?>(null) }
    var permitUri by remember { mutableStateOf<Uri?>(null) }
    var registrationUri by remember { mutableStateOf<Uri?>(null) }
    var carImageUri by remember { mutableStateOf<Uri?>(null) }
    var carImage2Uri by remember { mutableStateOf<Uri?>(null) }
    var carImage3Uri by remember { mutableStateOf<Uri?>(null) }

    var vehicleMake by remember { mutableStateOf("") }
    var vehicleModel by remember { mutableStateOf("") }
    var vehiclePlate by remember { mutableStateOf("") }
    var vehicleColor by remember { mutableStateOf("") }
    var addressLine1 by remember { mutableStateOf("") }
    var addressLine2 by remember { mutableStateOf("") }
    var addressCity by remember { mutableStateOf("") }
    var addressProvince by remember { mutableStateOf("") }
    var addressPostalCode by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val pickLicence = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) licenceUri = uri }

    val pickPermit = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) permitUri = uri }

    val pickRegistration = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) registrationUri = uri }

    val pickCar1 = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) carImageUri = uri }

    val pickCar2 = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) carImage2Uri = uri }

    val pickCar3 = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) carImage3Uri = uri }

    LaunchedEffect(Unit) {
        when (val r = DriverRepository.fetchVerificationStatus()) {
            is ApiResult.Success -> {
                existingVerification = r.data
                existingStatus = r.data.status
            }
            is ApiResult.Error -> { }
        }
        isLoadingStatus = false
    }

    val canSubmit = licenceUri != null &&
            permitUri != null &&
            registrationUri != null &&
            carImageUri != null &&
            vehiclePlate.isNotBlank() &&
            addressLine1.isNotBlank() &&
            addressCity.isNotBlank() &&
            addressProvince.isNotBlank() &&
            !isSubmitting

    Scaffold(containerColor = AgriCreamBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(AgriCardBg)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AgriTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "Driver verification",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    "We review your documents to keep buyers and farmers safe. Takes 1–2 business days.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriTextSecondary
                )

                Spacer(Modifier.height(16.dp))

                when (existingStatus) {
                    "pending" -> StatusBanner(
                        title = "Under review",
                        body = "We'll notify you when it's reviewed.",
                        bg = AgriMintLight,
                        fg = AgriGreenDark,
                        emoji = "⏳"
                    )
                    "approved" -> StatusBanner(
                        title = "You're verified!",
                        body = "You can now accept deliveries.",
                        bg = AgriMintLight,
                        fg = AgriGreenDark,
                        emoji = "✅"
                    )
                    "rejected" -> StatusBanner(
                        title = "Verification rejected",
                        body = existingVerification?.verification?.rejectionReason
                            ?: "Please review your documents and resubmit.",
                        bg = AgriRedAlert.copy(alpha = 0.12f),
                        fg = AgriRedAlert,
                        emoji = "⚠️"
                    )
                }

                Spacer(Modifier.height(20.dp))

                SectionHeader("1. Documents")
                Spacer(Modifier.height(12.dp))

                PhotoPickerRow(
                    label = "Driver's licence",
                    sublabel = "Clear photo of your licence card",
                    uri = licenceUri,
                    onClick = { pickLicence.launch("image/*") }
                )
                Spacer(Modifier.height(10.dp))

                PhotoPickerRow(
                    label = "Professional driving permit",
                    sublabel = "Your PrDP certificate",
                    uri = permitUri,
                    onClick = { pickPermit.launch("image/*") }
                )
                Spacer(Modifier.height(10.dp))

                PhotoPickerRow(
                    label = "Vehicle registration",
                    sublabel = "Registration certificate",
                    uri = registrationUri,
                    onClick = { pickRegistration.launch("image/*") }
                )

                Spacer(Modifier.height(24.dp))

                SectionHeader("2. Vehicle photos")
                Spacer(Modifier.height(4.dp))
                Text(
                    "Front required. Side and interior optional.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriTextSecondary
                )
                Spacer(Modifier.height(12.dp))

                PhotoPickerRow(
                    label = "Front of vehicle",
                    sublabel = "Required",
                    uri = carImageUri,
                    onClick = { pickCar1.launch("image/*") }
                )
                Spacer(Modifier.height(10.dp))

                PhotoPickerRow(
                    label = "Side view",
                    sublabel = "Optional",
                    uri = carImage2Uri,
                    onClick = { pickCar2.launch("image/*") }
                )
                Spacer(Modifier.height(10.dp))

                PhotoPickerRow(
                    label = "Interior / back",
                    sublabel = "Optional",
                    uri = carImage3Uri,
                    onClick = { pickCar3.launch("image/*") }
                )

                Spacer(Modifier.height(24.dp))

                SectionHeader("3. Vehicle details")
                Spacer(Modifier.height(12.dp))

                FormField("Make", vehicleMake, { vehicleMake = it }, "Toyota, Ford, Isuzu…")
                Spacer(Modifier.height(10.dp))
                FormField("Model", vehicleModel, { vehicleModel = it }, "Hilux, Ranger…")
                Spacer(Modifier.height(10.dp))
                FormField("Registration plate", vehiclePlate, { vehiclePlate = it }, "ABC 123 GP")
                Spacer(Modifier.height(10.dp))
                FormField("Colour", vehicleColor, { vehicleColor = it }, "White, silver…")

                Spacer(Modifier.height(24.dp))

                SectionHeader("4. Home address")
                Spacer(Modifier.height(12.dp))

                FormField("Street address", addressLine1, { addressLine1 = it }, "12 Main Rd")
                Spacer(Modifier.height(10.dp))
                FormField("Suburb / complex (optional)", addressLine2, { addressLine2 = it }, "Unit 4, Rose Court")
                Spacer(Modifier.height(10.dp))
                FormField("City / town", addressCity, { addressCity = it }, "Mbombela")
                Spacer(Modifier.height(10.dp))
                FormField("Province", addressProvince, { addressProvince = it }, "Mpumalanga")
                Spacer(Modifier.height(10.dp))
                FormField(
                    "Postal code",
                    addressPostalCode,
                    { addressPostalCode = it },
                    "1200",
                    KeyboardType.Number
                )

                Spacer(Modifier.height(24.dp))
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Button(
                    onClick = {
                        errorMessage = null
                        successMessage = null
                        isSubmitting = true
                        scope.launch {
                            val result = DriverRepository.submitVerification(
                                context = context,
                                licence = licenceUri!!,
                                permit = permitUri!!,
                                registration = registrationUri!!,
                                carImage = carImageUri!!,
                                carImage2 = carImage2Uri,
                                carImage3 = carImage3Uri,
                                vehicleMake = vehicleMake.trim(),
                                vehicleModel = vehicleModel.trim(),
                                vehiclePlate = vehiclePlate.trim().uppercase(),
                                vehicleColor = vehicleColor.trim(),
                                addressLine1 = addressLine1.trim(),
                                addressLine2 = addressLine2.trim(),
                                addressCity = addressCity.trim(),
                                addressProvince = addressProvince.trim(),
                                addressPostalCode = addressPostalCode.trim()
                            )
                            when (result) {
                                is ApiResult.Success -> {
                                    successMessage = "Submitted! We'll review it soon."
                                    onSubmitted()
                                }
                                is ApiResult.Error -> errorMessage = result.message
                            }
                            isSubmitting = false
                        }
                    },
                    enabled = canSubmit,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AgriGreen,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .padding(bottom = 16.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Uploading…", fontWeight = FontWeight.Bold)
                    } else {
                        Text("Submit for review", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                errorMessage?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AgriRedAlert.copy(alpha = 0.12f))
                            .padding(12.dp)
                    ) {
                        Text(it, color = AgriRedAlert, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = AgriTextPrimary
    )
}

@Composable
private fun StatusBanner(
    title: String,
    body: String,
    bg: Color,
    fg: Color,
    emoji: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = fg, fontSize = 15.sp)
            Spacer(Modifier.height(2.dp))
            Text(body, fontSize = 13.sp, color = fg.copy(alpha = 0.85f))
        }
    }
}

@Composable
private fun PhotoPickerRow(
    label: String,
    sublabel: String,
    uri: Uri?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AgriCardBg)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AgriMintSoft),
            contentAlignment = Alignment.Center
        ) {
            if (uri != null) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("📷", fontSize = 26.sp)
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = AgriTextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(sublabel, fontSize = 12.sp, color = AgriTextSecondary)
        }
        Text(
            if (uri == null) "Add" else "Change",
            color = AgriGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = AgriCardBg,
            focusedContainerColor = AgriCardBg,
            unfocusedBorderColor = AgriBorderLight,
            focusedBorderColor = AgriGreen,
            cursorColor = AgriGreen
        ),
        modifier = Modifier.fillMaxWidth()
    )
}