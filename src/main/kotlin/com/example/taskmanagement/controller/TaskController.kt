package com.example.taskmanagement.controller

import com.example.taskmanagement.dto.TaskRequest
import com.example.taskmanagement.dto.TaskResponse
import com.example.taskmanagement.service.TaskService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/tasks")
class TaskController(private val taskService: TaskService) {

    @PostMapping
    fun createTask(@Valid @RequestBody request: TaskRequest): Mono<TaskResponse> {
        return taskService.createTask(request)
    }

    @GetMapping("/{id}")
    fun getTask(@PathVariable id: Long): Mono<TaskResponse> {
        return taskService.getTaskById(id)
    }

}