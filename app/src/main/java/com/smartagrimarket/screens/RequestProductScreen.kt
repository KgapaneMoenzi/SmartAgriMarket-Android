package com.smartagrimarket.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.FarmerListItemDto
import com.smartagrimarket.model.network.ProductRequestDto
import com.smartagrimarket.model.network.RequestsRepository
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun RequestProductScreen(
    onBack: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var productName by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    var farmers by remember { mutableStateOf<List<FarmerListItemDto>>(emptyList()) }
    var selectedFarmerEmail by remember { mutableStateOf<String?>(null) }
    var farmerPickerExpanded by remember { mutableStateOf(false) }

    var myRequests by remember { mutableStateOf<List<ProductRequestDto>>(emptyList()) }

    fun load() {
        scope.launch {
            when (val r = RequestsRepository.fetchFarmers()) {
                is ApiResult.Success -> farmers = r.data
                is ApiResult.Error -> { /* silent */ }
            }
            when (val r = RequestsRepository.fetchMine()) {
                is ApiResult.Success -> myRequests = r.data
                is ApiResult.Error -> { /* silent */ }
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    Scaffold(containerColor = AgriCreamBg, bottomBar = bottomBar) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onBack)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = AgriTextSecondary)
                Spacer(Modifier.width(4.dp))
                Text("Request a product", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Can't find what you're looking for? Tell us what you want and a farmer will get back to you.",
                style = MaterialTheme.typography.bodySmall,
                color = AgriTextSecondary
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it; errorMessage = null },
                label = { Text("What are you looking for?") },
                placeholder = { Text("e.g. Baby spinach") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = AgriCardBg,
                    focusedContainerColor = AgriCardBg
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Notes (optional)") },
                placeholder = { Text("How much, when, any other details") },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = AgriCardBg,
                    focusedContainerColor = AgriCardBg
                ),
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )

            Spacer(Modifier.height(16.dp))
            Text("Send to", style = MaterialTheme.typography.labelMedium, color = AgriTextSecondary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AgriCardBg)
                    .clickable { farmerPickerExpanded = !farmerPickerExpanded }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        selectedFarmerEmail?.let { email ->
                            farmers.find { it.email == email }?.name ?: email
                        } ?: "Any farmer (open request)",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("Change", color = AgriGreen, fontWeight = FontWeight.SemiBold)
                }
                if (farmerPickerExpanded) {
                    Spacer(Modifier.height(8.dp))
                    Divider(color = AgriCreamBg, thickness = 1.dp)
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedFarmerEmail = null
                                farmerPickerExpanded = false
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedFarmerEmail == null,
                            onClick = {
                                selectedFarmerEmail = null
                                farmerPickerExpanded = false
                            }
                        )
                        Text("Any farmer", style = MaterialTheme.typography.bodyMedium)
                    }

                    farmers.forEach { f ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedFarmerEmail = f.email
                                    farmerPickerExpanded = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedFarmerEmail == f.email,
                                onClick = {
                                    selectedFarmerEmail = f.email
                                    farmerPickerExpanded = false
                                }
                            )
                            Column {
                                Text(f.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(f.email, style = MaterialTheme.typography.labelSmall, color = AgriTextSecondary)
                            }
                        }
                    }
                }
            }

            errorMessage?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = AgriRedAlert, style = MaterialTheme.typography.bodySmall)
            }
            successMessage?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = AgriGreen, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (productName.isBlank()) {
                        errorMessage = "Please describe what you're looking for."
                        return@Button
                    }
                    scope.launch {
                        isSubmitting = true
                        errorMessage = null
                        successMessage = null
                        when (val r = RequestsRepository.create(
                            productName = productName.trim(),
                            note = note.trim().ifBlank { null },
                            farmerEmail = selectedFarmerEmail
                        )) {
                            is ApiResult.Success -> {
                                successMessage = "Request sent! Farmers will reply soon."
                                productName = ""
                                note = ""
                                selectedFarmerEmail = null
                                load()
                            }
                            is ApiResult.Error -> errorMessage = r.message
                        }
                        isSubmitting = false
                    }
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgriYellow,
                    contentColor = AgriTextPrimary
                ),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = AgriTextPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Sending…", fontWeight = FontWeight.Bold)
                } else {
                    Text("Send request", fontWeight = FontWeight.Bold)
                }
            }

            if (myRequests.isNotEmpty()) {
                Spacer(Modifier.height(28.dp))
                Text("Your past requests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AgriCardBg)
                        .padding(vertical = 4.dp)
                ) {
                    myRequests.forEachIndexed { i, req ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(req.productName, fontWeight = FontWeight.SemiBold)
                                StatusChip(req.status)
                            }
                            req.note?.let {
                                Spacer(Modifier.height(2.dp))
                                Text(it, style = MaterialTheme.typography.bodySmall, color = AgriTextSecondary)
                            }
                            req.replyBody?.let {
                                Spacer(Modifier.height(6.dp))
                                Text("Farmer: $it", style = MaterialTheme.typography.bodySmall, color = AgriGreen)
                            }
                        }
                        if (i != myRequests.lastIndex) Divider(color = AgriCreamBg, thickness = 1.dp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val (label, color) = when (status) {
        "open" -> "Open" to AgriYellow
        "replied" -> "Replied" to AgriGreen
        "fulfilled" -> "Fulfilled" to AgriGreen
        else -> status to AgriTextSecondary
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
    }
}