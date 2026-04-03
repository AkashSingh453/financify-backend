package com.pikachu.financify.models

import kotlinx.serialization.Serializable

@Serializable
data class TransactionRequest(
    val amount: Double,
    val type: String,
    val category: String,
    val date: String, // ISO format: 2024-01-15
    val note: String = ""
)

@Serializable
data class TransactionResponse(
    val id: Long,
    val userId: Long,
    val amount: Double,
    val type: String,
    val category: String,
    val date: String,
    val note: String,
    val createdAt: String
)

@Serializable
data class TransactionListResponse(
    val transactions: List<TransactionResponse>,
    val success: Boolean = true
)
