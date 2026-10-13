package com.smartagrimarket.model.network

data class VerificationRecordDto(
    val id: String,
    val farmerEmail: String,
    val idDocumentUrl: String? = null,
    val certificateUrl: String? = null,
    val selfieUrl: String? = null,
    val status: String,
    val rejectionReason: String? = null,
    val submittedAt: String
)

data class VerificationSubmitResponse(
    val verification: VerificationRecordDto
)

data class VerificationStatusResponse(
    val status: String,
    val verification: VerificationRecordDto?
)