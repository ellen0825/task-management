package com.example.taskmanagement.controller

import com.example.taskmanagement.dto.TaskRequest
import com.example.taskmanagement.dto.TaskResponse
import com.example.taskmanagement.model.TaskStatus
import com.example.taskmanagement.service.TaskService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/tasks")
class TaskController(private val service: TaskService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createTask(@Valid @RequestBody request: TaskRequest): Mono<TaskResponse> =
        service.createTask(request)

    @GetMapping("/{id}")
    fun getTaskById(@PathVariable id: Long): Mono<TaskResponse> =
        service.getTaskById(id)

    @GetMapping
    fun getTasks(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) status: TaskStatus?
    ): Flux<TaskResponse> =
        service.getTasks(status, page, size)

    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestBody body: Map<String, String>
    ): Mono<TaskResponse> =
        service.updateStatus(id, TaskStatus.valueOf(body["status"]
            ?: throw IllegalArgumentException("status is required")))

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteTask(@PathVariable id: Long): Mono<Void> =
        service.deleteTask(id)
}
