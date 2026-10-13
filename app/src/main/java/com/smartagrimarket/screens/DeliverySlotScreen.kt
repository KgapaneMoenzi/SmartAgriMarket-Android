package com.smartagrimarket.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.smartagrimarket.model.network.DeliverySlotDto
import com.smartagrimarket.model.network.DeliverySlotRepository
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DeliverySlotScreen(
    orderId: String,
    onBack: () -> Unit = {},
    onConfirmed: (DeliverySlotDto) -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var slots by remember { mutableStateOf<List<DeliverySlotDto>>(emptyList()) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(orderId) {
        isLoading = true
        when (val result = DeliverySlotRepository.fetchDeliverySlots(orderId)) {
            is ApiResult.Success -> {
                slots = result.data.slots
                selectedId = result.data.selectedSlotId
            }
            is ApiResult.Error -> errorMessage = result.message
        }
        isLoading = false
    }

    val selectedSlot = slots.find { it.id == selectedId }

    Scaffold(containerColor = AgriCreamBg, bottomBar = bottomBar) { padding ->
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
                Text("Pick a delivery window", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Choose a 2-hour window today when someone will be available to collect the parcel.",
                style = MaterialTheme.typography.bodySmall,
                color = AgriTextSecondary
            )

            Spacer(Modifier.height(20.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AgriTextPrimary)
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AgriRedAlert.copy(alpha = 0.12f))
                            .padding(14.dp)
                    ) {
                        Text(errorMessage!!, color = AgriRedAlert, style = MaterialTheme.typography.bodySmall)
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AgriCardBg)
                            .padding(vertical = 4.dp)
                    ) {
                        slots.forEachIndexed { i, slot ->
                            val label = slot.label
                                ?: "${slot.startTime.orEmpty()} – ${slot.endTime.orEmpty()}"
                            SlotRow(
                                label = label,
                                selected = slot.id == selectedId,
                                onClick = {
                                    selectedId = slot.id
                                    successMessage = null
                                }
                            )
                            if (i != slots.lastIndex) HorizontalDivider(color = AgriCreamBg, thickness = 1.dp)
                        }
                    }

                    successMessage?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(it, color = AgriGreen, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    selectedSlot?.let { slot ->
                        scope.launch {
                            isSaving = true
                            errorMessage = null
                            successMessage = null
                            when (val r = DeliverySlotRepository.selectDeliverySlot(
                                orderId = orderId,
                                start = slot.startTime.orEmpty(),
                                end = slot.endTime.orEmpty()
                            )) {
                                is ApiResult.Success -> {
                                    successMessage = "Saved: ${slot.startTime.orEmpty()} – ${slot.endTime.orEmpty()}"
                                    onConfirmed(slot)
                                }
                                is ApiResult.Error -> errorMessage = r.message
                            }
                            isSaving = false
                        }
                    }
                },
                enabled = selectedSlot != null && !isSaving && !isLoading,
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
                } else {
                    Text("Confirm slot", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SlotRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
        val (text, color) = if (selected) "Selected" to AgriGreen else "Available" to AgriYellow
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(text, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}