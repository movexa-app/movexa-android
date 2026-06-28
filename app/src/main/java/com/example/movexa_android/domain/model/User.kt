package com.example.movexa_android.domain.model

data class User(
    val id: String,
    val email: String,
    val name: String,
    val token: String? = null
)