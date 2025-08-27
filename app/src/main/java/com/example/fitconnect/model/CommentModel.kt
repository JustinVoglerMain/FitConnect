// CommentModel.kt
package com.example.fitconnect.model

import java.util.Date

data class CommentModel(
    val documentId: String = "",
    val userName: String,
    val userId: String = "",  // Ensure default values for Firebase deserialization
    val text: String = "",
    val timestamp: String = Date().toString()
)
