package com.example.escom_appcelular

data class Message(
    val text: String,
    val isBot: Boolean,
    val isTyping: Boolean = false  // ← AGREGAR
)