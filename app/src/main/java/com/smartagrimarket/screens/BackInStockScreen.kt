package com.smartagrimarket.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.BackInStockRepository
import com.smartagrimarket.model.network.ProductImage
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun BackInStockScreen(
    productId: String,
    productName: String,
    farmName: String,
    expectedBackLabel: String,
    imageUrl: String? = null,
    onBack: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var isSubscribed by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(productId) {
        isLoading = true
        when (val result = BackInStockRepository.checkBackInStock(productId)) {
            is ApiResult.Success -> isSubscribed = result.data.subscribed
            is ApiResult.Error -> { }
        }
        isLoading = false
    }

    Scaffold(containerColor = AgriCreamBg, bottomBar = bottomBar) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Header with circular back ──
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
                    "Back in stock",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AgriTextPrimary
                )
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                // ── Product image hero ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AgriMintSoft),
                    contentAlignment = Alignment.Center
                ) {
                    ProductImage(
                        imageUrl = imageUrl,
                        productName = productName,
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        fallbackFontSize = 60.sp
                    )
                }

                Spacer(Modifier.height(18.dp))

                // ── Product name + status pill ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        productName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AgriTextPrimary
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AgriRedAlert.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "Out of stock",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgriRedAlert
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "$farmName · $expectedBackLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriTextSecondary
                )

                Spacer(Modifier.height(24.dp))

                // ── Notify card ──
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AgriCardBg),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(AgriMintLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Notifications,
                                    contentDescription = null,
                                    tint = AgriGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Notify me when back in stock",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriTextPrimary
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    "One email + one in-app alert the moment it returns.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AgriTextSecondary
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    isSaving = true
                                    errorMessage = null
                                    message = null
                                    when (val r = BackInStockRepository.subscribeBackInStock(productId)) {
                                        is ApiResult.Success -> {
                                            isSubscribed = true
                                            message = r.data.message ?: "You'll be notified."
                                        }
                                        is ApiResult.Error -> errorMessage = r.message
                                    }
                                    isSaving = false
                                }
                            },
                            enabled = !isLoading && !isSaving && !isSubscribed,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSubscribed) AgriMintLight else AgriGreen,
                                contentColor = if (isSubscribed) AgriGreenDark else Color.White,
                                disabledContainerColor = AgriMintLight,
                                disabledContentColor = AgriGreenDark
                            ),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    if (isSubscribed) "✓  You'll be notified"
                                    else "Notify me",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        message?.let {
                            Spacer(Modifier.height(10.dp))
                            Text(it, color = AgriGreen, style = MaterialTheme.typography.bodySmall)
                        }
                        errorMessage?.let {
                            Spacer(Modifier.height(10.dp))
                            Text(it, color = AgriRedAlert, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}