package com.pikachu.financify.models

import kotlinx.serialization.Serializable

@Serializable
data class GoalRequest(
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadline: String, // ISO format: 2024-06-30
    val category: String = "OTHER",
    val isNoSpendChallenge: Boolean = false
)

@Serializable
data class GoalUpdateRequest(
    val title: String? = null,
    val targetAmount: Double? = null,
    val currentAmount: Double? = null,
    val deadline: String? = null,
    val category: String? = null,
    val isActive: Boolean? = null,
    val streakDays: Int? = null
)

@Serializable
data class AddSavingsRequest(
    val amount: Double
)

@Serializable
data class GoalResponse(
    val id: Long,
    val userId: Long,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadline: String,
    val category: String,
    val isActive: Boolean,
    val isNoSpendChallenge: Boolean,
    val streakDays: Int,
    val createdAt: String
)

@Serializable
data class GoalListResponse(
    val goals: List<GoalResponse>,
    val success: Boolean = true
)
