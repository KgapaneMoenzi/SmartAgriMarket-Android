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
import com.smartagrimarket.model.network.FarmerQuestionDto
import com.smartagrimarket.model.network.QuestionsRepository
import com.smartagrimarket.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun FarmerQuestionsScreen(
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var questions by remember { mutableStateOf<List<FarmerQuestionDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            isLoading = true
            errorMessage = null
            when (val result = QuestionsRepository.fetchFarmerQuestions()) {
                is ApiResult.Success -> questions = result.data
                is ApiResult.Error -> errorMessage = result.message
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    Scaffold(
        containerColor = AgriCreamBg,
        bottomBar = bottomBar
    ) { padding ->
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
                Text("Questions from buyers", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
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
                questions.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AgriCardBg)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("❓", style = MaterialTheme.typography.headlineLarge)
                            Spacer(Modifier.height(8.dp))
                            Text("No questions yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "When buyers ask about your products, they'll show up here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AgriTextSecondary
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(questions, key = { it.id }) { q ->
                            FarmerQuestionCard(
                                question = q,
                                onAnswer = { answerText ->
                                    scope.launch {
                                        when (val r = QuestionsRepository.answerQuestion(q.id, answerText)) {
                                            is ApiResult.Success -> {
                                                // Replace with answered version
                                                questions = questions.map {
                                                    if (it.id == q.id) r.data else it
                                                }
                                            }
                                            is ApiResult.Error -> errorMessage = r.message
                                        }
                                    }
                                }
                            )
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FarmerQuestionCard(
    question: FarmerQuestionDto,
    onAnswer: (String) -> Unit
) {
    var replyText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AgriCardBg)
            .padding(14.dp)
    ) {
        Text(
            question.productName,
            style = MaterialTheme.typography.labelSmall,
            color = AgriTextSecondary
        )
        Spacer(Modifier.height(6.dp))
        Text("Q: ${question.question}", fontWeight = FontWeight.Bold)
        Text(
            "Asked by ${question.askedBy}",
            style = MaterialTheme.typography.labelSmall,
            color = AgriTextSecondary
        )

        if (question.answer != null) {
            Spacer(Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp)
            ) {
                Text("Your reply:", style = MaterialTheme.typography.labelSmall, color = AgriGreen, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(question.answer, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = replyText,
                onValueChange = { replyText = it },
                placeholder = { Text("Type your answer...") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = AgriCreamBg,
                    focusedContainerColor = AgriCreamBg
                )
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (replyText.isNotBlank()) {
                        isSubmitting = true
                        onAnswer(replyText.trim())
                        replyText = ""
                        isSubmitting = false
                    }
                },
                enabled = replyText.isNotBlank() && !isSubmitting,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgriYellow,
                    contentColor = AgriTextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send reply", fontWeight = FontWeight.Bold)
            }
        }
    }
}