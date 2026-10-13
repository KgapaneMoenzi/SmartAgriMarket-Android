package com.smartagrimarket.screens.account

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.model.network.FarmProfileDto
import com.smartagrimarket.screens.driver.LocationTracker
import com.smartagrimarket.ui.theme.*

@Composable
fun FarmDetailsScreen(
    farmProfile: FarmProfileDto?,
    isLoading: Boolean = false,
    isSaving: Boolean = false,
    errorMessage: String? = null,
    onBack: () -> Unit = {},
    onSave: (FarmProfileDto) -> Unit = {}
) {
    val context = LocalContext.current

    var farmName by remember(farmProfile) { mutableStateOf(farmProfile?.farmName.orEmpty()) }
    var sizeText by remember(farmProfile) { mutableStateOf(farmProfile?.farmSizeHectares?.toString().orEmpty()) }
    var town by remember(farmProfile) { mutableStateOf(farmProfile?.town.orEmpty()) }
    var cropsText by remember(farmProfile) { mutableStateOf(farmProfile?.primaryCrops?.joinToString(", ").orEmpty()) }

    // ── Farm coordinates ──
    var farmLat by remember(farmProfile) { mutableStateOf(farmProfile?.farmLatitude) }
    var farmLng by remember(farmProfile) { mutableStateOf(farmProfile?.farmLongitude) }

    var showValidationError by remember { mutableStateOf(false) }

    // ── Location tracker ──
    val tracker = remember { LocationTracker(context) }
    var hasPermission by remember { mutableStateOf(tracker.hasPermission()) }
    var isCapturing by remember { mutableStateOf(false) }
    var captureError by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted =
            grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasPermission = granted
        if (granted) {
            isCapturing = true
            tracker.start { lat, lng ->
                farmLat = lat
                farmLng = lng
                isCapturing = false
                tracker.stop()
            }
        } else {
            isCapturing = false
            captureError = "Location permission denied."
        }
    }

    DisposableEffect(Unit) {
        onDispose { tracker.stop() }
    }

    fun captureLocation() {
        captureError = null
        if (hasPermission) {
            isCapturing = true
            tracker.start { lat, lng ->
                farmLat = lat
                farmLng = lng
                isCapturing = false
                tracker.stop()
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
        }
    }

    val isFormValid = farmName.isNotBlank() && town.isNotBlank()

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
                modifier = Modifier.clickable(onClick = onBack)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = AgriTextSecondary)
                Spacer(Modifier.width(4.dp))
                Text("Back", color = AgriTextSecondary)
            }
            Spacer(Modifier.height(12.dp))
            Text("Farm details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AgriTextPrimary)
                }
            } else {
                Field("Farm name", farmName, { farmName = it })
                Spacer(Modifier.height(12.dp))
                Field("Size (hectares)", sizeText, { sizeText = it }, keyboardType = KeyboardType.Decimal)
                Spacer(Modifier.height(12.dp))
                Field("Town", town, { town = it })
                Spacer(Modifier.height(12.dp))
                Field("Primary crops (comma-separated)", cropsText, { cropsText = it })

                Spacer(Modifier.height(16.dp))

                // ── Farm location card ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AgriCardBg)
                        .padding(14.dp)
                ) {
                    Text(
                        "📍  Farm location",
                        style = MaterialTheme.typography.labelSmall,
                        color = AgriTextSecondary
                    )
                    Spacer(Modifier.height(4.dp))

                    when {
                        farmLat != null && farmLng != null -> {
                            Text(
                                "Coordinates captured",
                                fontWeight = FontWeight.Bold,
                                color = AgriGreen
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "%.5f, %.5f".format(farmLat, farmLng),
                                style = MaterialTheme.typography.bodySmall,
                                color = AgriTextSecondary
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Update location",
                                color = AgriGreen,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable(onClick = { captureLocation() })
                            )
                        }
                        isCapturing -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = AgriGreen
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "Getting your location…",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AgriTextSecondary
                                )
                            }
                        }
                        else -> {
                            Text(
                                "Buyers pay a distance-based delivery fee. Pin your farm so the price is accurate.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AgriTextSecondary
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Use my current location",
                                color = AgriGreen,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable(onClick = { captureLocation() })
                            )
                        }
                    }

                    captureError?.let {
                        Spacer(Modifier.height(6.dp))
                        Text(it, color = AgriRedAlert, style = MaterialTheme.typography.bodySmall)
                    }
                }

                if (showValidationError && !isFormValid) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Farm name and town are required.",
                        color = AgriRedAlert,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(errorMessage, color = AgriRedAlert, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        if (isFormValid) {
                            val crops = cropsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            onSave(
                                FarmProfileDto(
                                    farmName = farmName.trim(),
                                    farmSizeHectares = sizeText.toDoubleOrNull(),
                                    town = town.trim(),
                                    primaryCrops = crops,
                                    farmLatitude = farmLat,
                                    farmLongitude = farmLng,
                                )
                            )
                        } else showValidationError = true
                    },
                    enabled = !isSaving,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AgriYellow,
                        contentColor = AgriTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = AgriTextPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Saving…", fontWeight = FontWeight.Bold)
                    } else {
                        Text("Save farm details", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun Field(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
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