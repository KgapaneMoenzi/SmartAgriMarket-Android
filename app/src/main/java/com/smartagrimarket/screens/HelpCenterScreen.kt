package com.smartagrimarket.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.smartagrimarket.model.network.ApiResult
import com.smartagrimarket.model.network.HelpArticleDto
import com.smartagrimarket.model.network.HelpRepository
import com.smartagrimarket.model.network.SupportMessageDto
import com.smartagrimarket.model.network.TokenStore
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HelpCenterScreen(
    onBack: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var articles by remember { mutableStateOf<List<HelpArticleDto>>(emptyList()) }
    var messages by remember { mutableStateOf<List<SupportMessageDto>>(emptyList()) }
    var isSupportOnline by remember { mutableStateOf(false) }
    var avgResponseTime by remember { mutableStateOf("2 min") }
    var searchQuery by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }
    var expandedArticleId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val audience = if (TokenStore.currentUser?.role == "FARMER") "FARMER" else "BUYER"

    fun load() {
        scope.launch {
            isLoading = true
            errorMessage = null
            when (val a = HelpRepository.fetchArticles(audience)) {
                is ApiResult.Success -> articles = a.data
                is ApiResult.Error -> errorMessage = a.message
            }
            when (val s = HelpRepository.fetchStatus()) {
                is ApiResult.Success -> {
                    isSupportOnline = s.data.isSupportOnline
                    avgResponseTime = s.data.avgResponseTime
                }
                is ApiResult.Error -> { /* silent */ }
            }
            when (val c = HelpRepository.fetchChat()) {
                is ApiResult.Success -> messages = c.data
                is ApiResult.Error -> { /* silent */ }
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    val filteredArticles = remember(searchQuery, articles) {
        if (searchQuery.isBlank()) articles
        else articles.filter { it.question.contains(searchQuery, ignoreCase = true) }
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
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onBack)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = AgriTextSecondary)
                Spacer(Modifier.width(4.dp))
                Text("Help center", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search help articles...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = AgriCardBg,
                    focusedContainerColor = AgriCardBg
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))
            Text("Common questions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = AgriTextPrimary) }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AgriCardBg)
                        .padding(vertical = 4.dp)
                ) {
                    if (filteredArticles.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No articles match \"$searchQuery\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = AgriTextSecondary
                            )
                        }
                    } else {
                        filteredArticles.forEachIndexed { i, article ->
                            val expanded = expandedArticleId == article.id
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expandedArticleId = if (expanded) null else article.id
                                        }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(article.question, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                    Icon(
                                        Icons.Filled.ChevronRight,
                                        contentDescription = null,
                                        tint = AgriTextSecondary
                                    )
                                }
                                if (expanded) {
                                    Text(
                                        article.answer,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AgriTextSecondary,
                                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
                                    )
                                }
                            }
                            if (i != filteredArticles.lastIndex) Divider(color = AgriCreamBg, thickness = 1.dp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Still need help?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AgriCardBg)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(AgriYellow.copy(alpha = 0.3f))
                        .padding(10.dp)
                ) {
                    Icon(Icons.Filled.Chat, contentDescription = null, tint = AgriTextPrimary)
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Chat with support", fontWeight = FontWeight.Bold)
                    Text("Avg. response time: $avgResponseTime", style = MaterialTheme.typography.bodySmall, color = AgriTextSecondary)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background((if (isSupportOnline) AgriGreen else AgriTextSecondary).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        if (isSupportOnline) "Online" else "Offline",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSupportOnline) AgriGreen else AgriTextSecondary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AgriCardBg)
                    .padding(14.dp)
            ) {
                if (messages.isEmpty()) {
                    Text(
                        "No messages yet. Send us a question and we'll reply.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AgriTextSecondary
                    )
                } else {
                    messages.forEach { msg ->
                        Column(modifier = Modifier.padding(bottom = 10.dp)) {
                            Text(
                                (if (msg.isSupport) "Support" else "You") + " · " + shortTime(msg.createdAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = AgriTextSecondary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(msg.body, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Type your message...") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = AgriCardBg,
                    focusedContainerColor = AgriCardBg
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (messageText.isNotBlank()) {
                        val text = messageText.trim()
                        messageText = ""
                        scope.launch {
                            isSending = true
                            when (val r = HelpRepository.sendMessage(text)) {
                                is ApiResult.Success -> {
                                    messages = messages + r.data
                                }
                                is ApiResult.Error -> errorMessage = r.message
                            }
                            isSending = false
                        }
                    }
                }),
                trailingIcon = {
                    if (messageText.isNotBlank()) {
                        TextButton(
                            enabled = !isSending,
                            onClick = {
                                val text = messageText.trim()
                                messageText = ""
                                scope.launch {
                                    isSending = true
                                    when (val r = HelpRepository.sendMessage(text)) {
                                        is ApiResult.Success -> messages = messages + r.data
                                        is ApiResult.Error -> errorMessage = r.message
                                    }
                                    isSending = false
                                }
                            }
                        ) { Text("Send") }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = AgriRedAlert, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun shortTime(iso: String): String {
    return try {
        val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
        val then = parser.parse(iso)?.time ?: return ""
        val diff = System.currentTimeMillis() - then
        when {
            diff < 60_000 -> "now"
            diff < 3_600_000 -> "${diff / 60_000}m ago"
            diff < 86_400_000 -> "${diff / 3_600_000}h ago"
            else -> "${diff / 86_400_000}d ago"
        }
    } catch (_: Exception) { "" }
}