package com.smartagrimarket.screens.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.smartagrimarket.model.network.ProductRequestDto
import com.smartagrimarket.model.network.RequestsRepository
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun FarmerRequestsScreen(
    onBack: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var requests by remember { mutableStateOf<List<ProductRequestDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var replyingTo by remember { mutableStateOf<ProductRequestDto?>(null) }

    fun load() {
        scope.launch {
            isLoading = true
            errorMessage = null
            when (val r = RequestsRepository.fetchFarmerRequests()) {
                is ApiResult.Success -> requests = r.data
                is ApiResult.Error -> errorMessage = r.message
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    Scaffold(containerColor = AgriCreamBg, bottomBar = bottomBar) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onBack)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = AgriTextSecondary)
                Spacer(Modifier.width(4.dp))
                Text("Buyer requests", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
                requests.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AgriCardBg)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🌱", style = MaterialTheme.typography.headlineLarge)
                            Spacer(Modifier.height(8.dp))
                            Text("No requests yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "When a buyer asks for a product, it'll show up here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AgriTextSecondary
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(requests, key = { it.id }) { req ->
                            RequestCard(
                                request = req,
                                onReplyClick = { replyingTo = req }
                            )
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }

    replyingTo?.let { req ->
        ReplyDialog(
            request = req,
            onDismiss = { replyingTo = null },
            onSubmit = { text ->
                scope.launch {
                    when (val r = RequestsRepository.reply(req.id, text)) {
                        is ApiResult.Success -> {
                            requests = requests.map { if (it.id == r.data.id) r.data else it }
                            replyingTo = null
                        }
                        is ApiResult.Error -> errorMessage = r.message
                    }
                }
            }
        )
    }
}

@Composable
private fun RequestCard(request: ProductRequestDto, onReplyClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AgriCardBg)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🌱 ${request.productName}", fontWeight = FontWeight.Bold)
            StatusChip(request.status)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "From ${request.buyerName}",
            style = MaterialTheme.typography.labelSmall,
            color = AgriTextSecondary
        )
        request.note?.let {
            Spacer(Modifier.height(6.dp))
            Text(it, style = MaterialTheme.typography.bodySmall, color = AgriTextSecondary)
        }

        request.replyBody?.let {
            Spacer(Modifier.height(10.dp))
            Text("Your reply: $it", style = MaterialTheme.typography.bodySmall, color = AgriGreen)
        }

        if (request.status == "open") {
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onReplyClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgriYellow,
                    contentColor = AgriTextPrimary
                ),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text("Reply", fontWeight = FontWeight.Bold)
            }
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

@Composable
private fun ReplyDialog(
    request: ProductRequestDto,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Reply to ${request.buyerName}", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Request: ${request.productName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AgriTextSecondary
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Yes, I can supply that...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = text.isNotBlank() && !isSubmitting,
                onClick = {
                    isSubmitting = true
                    onSubmit(text.trim())
                }
            ) { Text("Send") }
        },
        dismissButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = onDismiss
            ) { Text("Cancel") }
        }
    )
}