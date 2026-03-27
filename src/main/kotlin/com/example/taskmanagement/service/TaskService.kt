package com.example.taskmanagement.service

import com.example.taskmanagement.dto.TaskRequest
import com.example.taskmanagement.dto.TaskResponse
import com.example.taskmanagement.model.Task
import com.example.taskmanagement.model.TaskStatus
import com.example.taskmanagement.repository.TaskRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class TaskService(private val repository: TaskRepository) {

    fun createTask(request: TaskRequest): Mono<TaskResponse> =
        repository.save(Task(title = request.title, description = request.description))
            .map { it.toResponse() }

    fun getTaskById(id: Long): Mono<TaskResponse> =
        repository.findById(id)
            .map { it.toResponse() }

    fun getTasks(status: TaskStatus?, page: Int, size: Int): Flux<TaskResponse> =
        repository.findAll(status, page, size)
            .map { it.toResponse() }

    fun updateStatus(id: Long, status: TaskStatus): Mono<TaskResponse> =
        repository.updateStatus(id, status)
            .map { it.toResponse() }

    fun deleteTask(id: Long): Mono<Void> =
        repository.deleteById(id)

    private fun Task.toResponse() = TaskResponse(
        id = this.id!!,
        title = this.title,
        description = this.description,
        status = this.status,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}