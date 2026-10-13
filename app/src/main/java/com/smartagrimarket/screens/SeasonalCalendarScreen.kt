package com.smartagrimarket.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.InSeasonItemDto
import com.smartagrimarket.model.network.PlantingReminderDto
import com.smartagrimarket.model.network.SeasonalRepository
import com.smartagrimarket.ui.theme.*

@Composable
fun SeasonalCalendarScreen(
    bottomBar: @Composable () -> Unit = {}
) {
    var inSeason by remember { mutableStateOf<List<InSeasonItemDto>>(emptyList()) }
    var upcoming by remember { mutableStateOf<List<PlantingReminderDto>>(emptyList()) }
    var region by remember { mutableStateOf("Mpumalanga") }
    var monthLabel by remember { mutableStateOf("") }
    var monthTip by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        val month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
        when (val r = SeasonalRepository.fetch(month, "Mpumalanga")) {
            is ApiResult.Success -> {
                inSeason = r.data.inSeason
                upcoming = r.data.upcoming
                region = r.data.region
                monthLabel = r.data.monthLabel
                monthTip = r.data.monthTip
            }
            is ApiResult.Error -> errorMessage = r.message
        }
        isLoading = false
    }

    Scaffold(containerColor = AgriCreamBg, bottomBar = bottomBar) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("What's in season", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("📍 $region", style = MaterialTheme.typography.bodySmall, color = AgriTextSecondary)
            }

            Spacer(Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = AgriTextPrimary) }
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
                            .clip(RoundedCornerShape(16.dp))
                            .background(AgriYellow)
                            .padding(16.dp)
                    ) {
                        Text("🌱 $monthLabel", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(2.dp))
                        Text(monthTip, style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(Modifier.height(20.dp))
                    Text("In season now", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AgriCardBg)
                            .padding(vertical = 4.dp)
                    ) {
                        if (inSeason.isEmpty()) {
                            Text(
                                "Nothing in season yet this month.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AgriTextSecondary,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            inSeason.forEachIndexed { i, item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(item.emoji, fontSize = 18.sp)
                                        Spacer(Modifier.width(8.dp))
                                        Text(item.name, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    StatusBadge(item.status)
                                }
                                if (i != inSeason.lastIndex) Divider(color = AgriCreamBg, thickness = 1.dp)
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Text("Planning ahead", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AgriCardBg)
                            .padding(vertical = 4.dp)
                    ) {
                        if (upcoming.isEmpty()) {
                            Text(
                                "No reminders this month.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AgriTextSecondary,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            upcoming.forEachIndexed { i, reminder ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🗓️", fontSize = 16.sp)
                                    Spacer(Modifier.width(10.dp))
                                    Text(reminder.label, style = MaterialTheme.typography.bodyMedium)
                                }
                                if (i != upcoming.lastIndex) Divider(color = AgriCreamBg, thickness = 1.dp)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val color = when (status) {
        "Peak" -> AgriGreen
        "Coming soon" -> AgriYellow
        else -> AgriTextSecondary
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(status, style = MaterialTheme.typography.labelSmall, color = color)
    }
}