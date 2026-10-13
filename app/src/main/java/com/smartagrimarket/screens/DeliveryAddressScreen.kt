package com.smartagrimarket.screens.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.model.network.AddressDto
import com.smartagrimarket.model.network.AddressRequest
import com.smartagrimarket.ui.theme.*

@Composable
fun DeliveryAddressScreen(
    address: AddressDto?,
    isLoading: Boolean = false,
    isSaving: Boolean = false,
    errorMessage: String? = null,
    onBack: () -> Unit = {},
    onPickOnMap: () -> Unit = {},
    onSave: (AddressRequest) -> Unit = {}
) {
    var line1 by remember(address) { mutableStateOf(address?.line1.orEmpty()) }
    var line2 by remember(address) { mutableStateOf(address?.line2.orEmpty()) }
    var city by remember(address) { mutableStateOf(address?.city.orEmpty()) }
    var province by remember(address) { mutableStateOf(address?.province.orEmpty()) }
    var postalCode by remember(address) { mutableStateOf(address?.postalCode.orEmpty()) }

    // Local pin state — seeded from the saved address, updated when
    // the picker returns. Kept in the screen so a save fires with
    // whatever pin the user sees on screen right now.
    var pickedLat by remember(address) { mutableStateOf(address?.latitude) }
    var pickedLng by remember(address) { mutableStateOf(address?.longitude) }
    var pickedLabel by remember(address) { mutableStateOf<String?>(null) }

    var showValidationError by remember { mutableStateOf(false) }

    val isFormValid = line1.isNotBlank() && city.isNotBlank() && province.isNotBlank() && postalCode.isNotBlank()
    val hasPin = pickedLat != null && pickedLng != null

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
            Spacer(Modifier.height(12.dp))
            Text("Delivery address", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AgriTextPrimary)
                }
            } else {
                Field("Address line 1", line1, { line1 = it })
                Spacer(Modifier.height(12.dp))
                Field("Address line 2 (optional)", line2, { line2 = it })
                Spacer(Modifier.height(12.dp))
                Field("City", city, { city = it })
                Spacer(Modifier.height(12.dp))
                Field("Province", province, { province = it })
                Spacer(Modifier.height(12.dp))
                Field("Postal code", postalCode, { postalCode = it })

                Spacer(Modifier.height(20.dp))

                // ── Map pin card ──
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AgriCardBg),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📍", fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Exact location on map",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (hasPin)
                                "Pinned: ${"%.5f".format(pickedLat)}, ${"%.5f".format(pickedLng)}"
                            else
                                "Not pinned yet — couriers rely on this to find you. " +
                                        "Without a pin, delivery distance and fees can't be computed accurately.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (hasPin) AgriGreen else AgriTextSecondary
                        )
                        pickedLabel?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = AgriTextSecondary
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onPickOnMap,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AgriTextPrimary
                            )
                        ) {
                            Text(
                                if (hasPin) "Change pin on map" else "Pin on map",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                if (showValidationError && !isFormValid) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Please fill in address line 1, city, province, and postal code.",
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
                            onSave(
                                AddressRequest(
                                    line1 = line1.trim(),
                                    line2 = line2.trim().ifBlank { null },
                                    city = city.trim(),
                                    province = province.trim(),
                                    postalCode = postalCode.trim(),
                                    latitude = pickedLat,
                                    longitude = pickedLng,
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
                        Text("Save address", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun Field(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = AgriCardBg,
            focusedContainerColor = AgriCardBg
        ),
        modifier = Modifier.fillMaxWidth()
    )
}