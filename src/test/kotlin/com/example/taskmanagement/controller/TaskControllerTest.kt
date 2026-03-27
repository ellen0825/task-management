package com.example.taskmanagement.controller

import com.example.taskmanagement.dto.PageResponse
import com.example.taskmanagement.dto.TaskRequest
import com.example.taskmanagement.dto.TaskResponse
import com.example.taskmanagement.model.TaskStatus
import com.example.taskmanagement.service.TaskService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import com.example.taskmanagement.exception.GlobalExceptionHandler
import org.springframework.http.HttpStatus

@WebFluxTest(TaskController::class)
@Import(GlobalExceptionHandler::class)
class TaskControllerTest {

    @Autowired
    lateinit var client: WebTestClient

    @MockitoBean
    lateinit var service: TaskService

    private val now = LocalDateTime.now()
    private val response = TaskResponse(1L, "Test", "Desc", TaskStatus.NEW, now, now)
    private val page = PageResponse(listOf(response), 0, 10, 1L, 1)

    @Test
    fun `POST creates task and returns 201`() {
        whenever(service.createTask(any())).thenReturn(Mono.just(response))

        client.post().uri("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TaskRequest("Test", "Desc"))
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)
            .jsonPath("$.status").isEqualTo("NEW")
    }

    @Test
    fun `POST returns 400 when title is blank`() {
        client.post().uri("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("title" to "", "description" to "x"))
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `POST returns 400 when title is too short`() {
        client.post().uri("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("title" to "ab", "description" to "x"))
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `GET by id returns task`() {
        whenever(service.getTaskById(1L)).thenReturn(Mono.just(response))

        client.get().uri("/api/tasks/1")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)
            .jsonPath("$.title").isEqualTo("Test")
    }

    @Test
    fun `GET by id returns 404 when not found`() {
        whenever(service.getTaskById(99L))
            .thenReturn(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Task 99 not found")))

        client.get().uri("/api/tasks/99")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
    }

    @Test
    fun `GET list returns page response`() {
        whenever(service.getTasks(null, 0, 10)).thenReturn(Mono.just(page))

        client.get().uri("/api/tasks?page=0&size=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.content.length()").isEqualTo(1)
            .jsonPath("$.totalElements").isEqualTo(1)
            .jsonPath("$.totalPages").isEqualTo(1)
            .jsonPath("$.page").isEqualTo(0)
            .jsonPath("$.size").isEqualTo(10)
    }

    @Test
    fun `GET list filters by status`() {
        whenever(service.getTasks(eq(TaskStatus.NEW), eq(0), eq(10))).thenReturn(Mono.just(page))

        client.get().uri("/api/tasks?page=0&size=10&status=NEW")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.content[0].status").isEqualTo("NEW")
    }

    @Test
    fun `PATCH updates status`() {
        val updated = response.copy(status = TaskStatus.DONE)
        whenever(service.updateStatus(1L, TaskStatus.DONE)).thenReturn(Mono.just(updated))

        client.patch().uri("/api/tasks/1/status")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("status" to "DONE"))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("DONE")
    }

    @Test
    fun `PATCH returns 400 on unknown status`() {
        client.patch().uri("/api/tasks/1/status")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("status" to "INVALID"))
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `PATCH returns 404 when task not found`() {
        whenever(service.updateStatus(eq(99L), any()))
            .thenReturn(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Task 99 not found")))

        client.patch().uri("/api/tasks/99/status")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("status" to "DONE"))
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `DELETE returns 204`() {
        whenever(service.deleteTask(1L)).thenReturn(Mono.empty())

        client.delete().uri("/api/tasks/1")
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `DELETE returns 404 when task not found`() {
        whenever(service.deleteTask(99L))
            .thenReturn(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Task 99 not found")))

        client.delete().uri("/api/tasks/99")
            .exchange()
            .expectStatus().isNotFound
    }
}
