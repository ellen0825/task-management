package com.example.taskmanagement.controller

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
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@WebFluxTest(TaskController::class)
class TaskControllerTest {

    @Autowired
    lateinit var client: WebTestClient

    @MockitoBean
    lateinit var service: TaskService

    private val sampleResponse = TaskResponse(
        id = 1L, title = "Test", description = "Desc",
        status = TaskStatus.NEW,
        createdAt = LocalDateTime.now(), updatedAt = LocalDateTime.now()
    )

    @Test
    fun `POST api-tasks creates task and returns 201`() {
        whenever(service.createTask(any())).thenReturn(Mono.just(sampleResponse))

        client.post().uri("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TaskRequest("Test", "Desc"))
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)
            .jsonPath("$.title").isEqualTo("Test")
            .jsonPath("$.status").isEqualTo("NEW")
    }

    @Test
    fun `POST api-tasks returns 400 on invalid request`() {
        client.post().uri("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("title" to "ab")) // too short
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `GET api-tasks-id returns task`() {
        whenever(service.getTaskById(1L)).thenReturn(Mono.just(sampleResponse))

        client.get().uri("/api/tasks/1")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)
    }

    @Test
    fun `GET api-tasks returns list with pagination`() {
        whenever(service.getTasks(null, 0, 10)).thenReturn(Flux.just(sampleResponse))

        client.get().uri("/api/tasks?page=0&size=10")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(TaskResponse::class.java)
            .hasSize(1)
    }

    @Test
    fun `GET api-tasks filters by status`() {
        whenever(service.getTasks(eq(TaskStatus.NEW), eq(0), eq(10)))
            .thenReturn(Flux.just(sampleResponse))

        client.get().uri("/api/tasks?page=0&size=10&status=NEW")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(TaskResponse::class.java)
            .hasSize(1)
    }

    @Test
    fun `PATCH api-tasks-id-status updates status`() {
        val updated = sampleResponse.copy(status = TaskStatus.DONE)
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
    fun `DELETE api-tasks-id returns 204`() {
        whenever(service.deleteTask(1L)).thenReturn(Mono.empty())

        client.delete().uri("/api/tasks/1")
            .exchange()
            .expectStatus().isNoContent
    }
}
