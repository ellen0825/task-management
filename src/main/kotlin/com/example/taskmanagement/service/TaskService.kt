package com.example.taskmanagement.service

import com.example.taskmanagement.dto.TaskRequest
import com.example.taskmanagement.dto.TaskResponse
import com.example.taskmanagement.model.Task
import com.example.taskmanagement.model.TaskStatus
import com.example.taskmanagement.repository.TaskRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
class TaskService(private val repository: TaskRepository) {

    fun createTask(request: TaskRequest): Mono<TaskResponse> =
        Mono.fromCallable {
            repository.save(Task(title = request.title, description = request.description))
                .toResponse()
        }.subscribeOn(Schedulers.boundedElastic())

    fun getTaskById(id: Long): Mono<TaskResponse> =
        Mono.fromCallable {
            repository.findById(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Task $id not found")
        }.map { it.toResponse() }.subscribeOn(Schedulers.boundedElastic())

    fun getTasks(status: TaskStatus?, page: Int, size: Int): Flux<TaskResponse> =
        Mono.fromCallable { repository.findAll(status, page, size) }
            .flatMapIterable { it }
            .map { it.toResponse() }
            .subscribeOn(Schedulers.boundedElastic())

    fun updateStatus(id: Long, status: TaskStatus): Mono<TaskResponse> =
        Mono.fromCallable {
            repository.updateStatus(id, status) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Task $id not found")
        }.map { it.toResponse() }.subscribeOn(Schedulers.boundedElastic())

    fun deleteTask(id: Long): Mono<Void> =
        Mono.fromCallable { repository.deleteById(id) }
            .flatMap { count ->
                if (count == 0) Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Task $id not found"))
                else Mono.empty()
            }
            .subscribeOn(Schedulers.boundedElastic())
            .then()

    private fun Task.toResponse() = TaskResponse(
        id = id!!,
        title = title,
        description = description,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
