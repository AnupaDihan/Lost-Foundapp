package com.example.myapplication.models

data class Item(
    val id: Long = 0,
    val title: String,
    val description: String,
    val category: String,
    val type: String, // "lost" or "found"
    val location: String,
    val contactInfo: String,
    val date: String,
    val imagePath: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false
)
