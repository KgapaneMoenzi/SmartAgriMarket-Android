package com.smartagrimarket.model.network

object QuestionsRepository {
    private val api = RetrofitClient.api

    // ---- Buyer-facing ----

    suspend fun fetchQuestions(productId: String): ApiResult<List<QuestionDto>> {
        return try {
            val response = api.getProductQuestions(productId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body.questions)
            } else {
                ApiResult.Error("Could not load questions (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun askQuestion(productId: String, question: String): ApiResult<QuestionDto> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.createQuestion(
                "Bearer $token",
                productId,
                CreateQuestionRequest(question)
            )
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body.question)
            } else {
                ApiResult.Error("Could not save question (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    // ---- Farmer-facing ----

    suspend fun fetchFarmerQuestions(): ApiResult<List<FarmerQuestionDto>> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.getFarmerQuestions("Bearer $token")
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body.questions)
            } else {
                ApiResult.Error("Could not load questions (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun answerQuestion(questionId: String, answer: String): ApiResult<FarmerQuestionDto> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.answerQuestion(
                "Bearer $token",
                questionId,
                AnswerQuestionRequest(answer)
            )
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body.question)
            } else {
                ApiResult.Error("Could not save your answer (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }
}