package com.example.auticare.data.reward

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int,
    var isEarned: Boolean = false
)
