package com.smartagrimarket.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
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
import com.smartagrimarket.model.network.QuestionDto
import com.smartagrimarket.model.network.QuestionsRepository
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProductQAScreen(
    productId: String,
    productName: String,
    onBack: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var questions by remember { mutableStateOf<List<QuestionDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var questionText by remember { mutableStateOf("") }
    var isAsking by remember { mutableStateOf(false) }
    var askError by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            isLoading = true
            errorMessage = null
            when (val result = QuestionsRepository.fetchQuestions(productId)) {
                is ApiResult.Success -> questions = result.data
                is ApiResult.Error -> errorMessage = result.message
            }
            isLoading = false
        }
    }

    LaunchedEffect(productId) { load() }

    Scaffold(containerColor = AgriCreamBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Header ──
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Questions",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AgriTextPrimary
                    )
                    Text(
                        productName,
                        style = MaterialTheme.typography.bodySmall,
                        color = AgriTextSecondary,
                        maxLines = 1
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                // ── Ask question field ──
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it; askError = null },
                    placeholder = { Text("Ask the farmer a question…") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.HelpOutline,
                            contentDescription = null,
                            tint = AgriTextTertiary
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = AgriCardBg,
                        focusedContainerColor = AgriCardBg,
                        unfocusedBorderColor = AgriBorderLight,
                        focusedBorderColor = AgriGreen,
                        cursorColor = AgriGreen
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (questionText.isNotBlank()) {
                            Button(
                                enabled = !isAsking,
                                onClick = {
                                    scope.launch {
                                        isAsking = true
                                        askError = null
                                        when (val result = QuestionsRepository.askQuestion(productId, questionText.trim())) {
                                            is ApiResult.Success -> {
                                                questions = listOf(result.data) + questions
                                                questionText = ""
                                            }
                                            is ApiResult.Error -> askError = result.message
                                        }
                                        isAsking = false
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AgriGreen,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                if (isAsking) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Ask", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                )

                askError?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, color = AgriRedAlert, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(18.dp))
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AgriGreen)
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AgriRedAlert.copy(alpha = 0.12f))
                            .padding(14.dp)
                    ) {
                        Text(
                            errorMessage!!,
                            color = AgriRedAlert,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                questions.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(AgriMintLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💬", fontSize = 42.sp)
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "No questions yet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AgriTextPrimary
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Be the first to ask about this product.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AgriTextSecondary
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(questions, key = { it.id }) { q -> QuestionCard(q) }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionCard(q: QuestionDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AgriCardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Question header with "Q" badge
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(AgriMintLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Q",
                        fontWeight = FontWeight.Bold,
                        color = AgriGreenDark,
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        q.question,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AgriTextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Asked by ${q.askedBy}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AgriTextTertiary
                    )
                }
            }

            if (q.answer != null) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AgriMintSoft)
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(AgriGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "A",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            q.answer,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AgriTextPrimary
                        )
                        if (q.answeredBy != null) {
                            Spacer(Modifier.height(3.dp))
                            Text(
                                "— ${q.answeredBy}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AgriGreenDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(AgriOrange.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Awaiting farmer reply",
                        style = MaterialTheme.typography.labelSmall,
                        color = AgriOrange,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}