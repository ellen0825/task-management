package com.example.taskmanagement.model

import java.time.LocalDateTime

enum class TaskStatus {
    NEW, IN_PROGRESS, DONE, CANCELLED
}

data class Task(
    val id: Long? = null,
    val title: String,
    val description: String?,
    val status: TaskStatus = TaskStatus.NEW,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)