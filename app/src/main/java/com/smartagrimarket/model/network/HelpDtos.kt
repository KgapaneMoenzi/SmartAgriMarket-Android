package com.smartagrimarket.model.network

data class HelpArticleDto(
    val id: String,
    val question: String,
    val answer: String,
    val category: String
)

data class HelpArticlesResponse(
    val articles: List<HelpArticleDto>
)

data class SupportStatusResponse(
    val isSupportOnline: Boolean,
    val avgResponseTime: String
)

data class SupportMessageDto(
    val id: String,
    val body: String,
    val isSupport: Boolean,
    val createdAt: String
)

data class SupportChatResponse(
    val messages: List<SupportMessageDto>
)

data class SendSupportMessageRequest(
    val body: String
)

data class SendSupportMessageResponse(
    val messages: List<SupportMessageDto>
)