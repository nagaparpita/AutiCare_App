package com.example.auticare.data.reward

data class Sticker(
    val id: String,
    val name: String,
    val imageRes: Int,
    var isUnlocked: Boolean = false
)
