package com.example.auticare.data.reward

data class RewardProgress(
    val childId: String = "",
    val sourceGame: String = "",
    val stars: Int = 0,
    val badgeId: String? = null,
    val stickerId: String? = null,
    val date: Long = System.currentTimeMillis()
)
