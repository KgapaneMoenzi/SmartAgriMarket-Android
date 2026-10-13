package com.smartagrimarket.model.network

// ---- Product Q&A (buyer-facing) ----

data class QuestionDto(
    val id: String,
    val question: String,
    val askedBy: String,
    val answer: String?,
    val answeredBy: String?,
    val createdAt: String,
    val answeredAt: String?
)

data class QuestionsListResponse(
    val questions: List<QuestionDto>
)

data class CreateQuestionRequest(
    val question: String
)

data class CreateQuestionResponse(
    val question: QuestionDto
)

// ---- Farmer-side questions (dashboard) ----

data class FarmerQuestionDto(
    val id: String,
    val productId: String,
    val productName: String,
    val question: String,
    val askedBy: String,
    val answer: String?,
    val answeredBy: String?,
    val createdAt: String,
    val answeredAt: String?
)

data class FarmerQuestionsListResponse(
    val questions: List<FarmerQuestionDto>
)

data class AnswerQuestionRequest(
    val answer: String
)

data class AnswerQuestionResponse(
    val question: FarmerQuestionDto
)