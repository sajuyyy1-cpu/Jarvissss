package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String, // "PREFERENCE", "NOTE", "SYSTEM", "IDENTITY", "PROTOCOL"
    val timestamp: Long = System.currentTimeMillis(),
    val tags: String = ""
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
    val isCompleted: Boolean = false,
    val dueDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mud_art_customers")
data class MudArtCustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val city: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mud_art_orders")
data class MudArtOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerName: String,
    val artPieceTitle: String,
    val dimensions: String = "", // e.g. "36x24 inch Lippan Art"
    val amount: Double = 0.0,
    val status: String = "PENDING", // "PENDING", "IN_PROGRESS", "COMPLETED", "DISPATCHED"
    val orderDate: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mud_art_notes")
data class MudArtNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "MATERIAL", // "MATERIAL", "TECHNIQUE", "EXHIBITION", "FINANCE"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "USER" or "JARVIS"
    val text: String,
    val language: String = "EN", // "EN", "HI", "HINGLISH"
    val timestamp: Long = System.currentTimeMillis(),
    val isVoice: Boolean = false
)
