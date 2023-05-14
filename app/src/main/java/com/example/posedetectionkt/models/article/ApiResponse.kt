package com.example.posedetectionkt.models.article

data class ApiResponse(
    val articles: List<Article>,
    val status: String,
    val totalResults: Int
)