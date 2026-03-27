package com.example.taskmanagement.service

import com.example.taskmanagement.dto.TaskRequest
import com.example.taskmanagement.dto.TaskResponse
import com.example.taskmanagement.model.Task
import com.example.taskmanagement.repository.TaskRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class TaskService(private val taskRepository: TaskRepository) {

    fun createTask(request: TaskRequest): Mono<TaskResponse> {
        val task = Task(title = request.title, description = request.description)
        return taskRepository.save(task)
            .map { saved ->
                TaskResponse(saved.id!!, saved.title, saved.description, saved.status, saved.createdAt, saved.updatedAt)
            }
    }

    fun getTaskById(id: Long): Mono<TaskResponse> {
        return taskRepository.findById(id)
            .map { t ->
                TaskResponse(t.id!!, t.title, t.description, t.status, t.createdAt, t.updatedAt)
            }
    }

}