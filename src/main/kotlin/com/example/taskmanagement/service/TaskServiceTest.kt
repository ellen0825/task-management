package com.example.taskmanagement.service

import com.example.taskmanagement.dto.TaskRequest
import com.example.taskmanagement.model.Task
import com.example.taskmanagement.model.TaskStatus
import com.example.taskmanagement.repository.TaskRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime

class TaskServiceTest {

    private val repository = mockk<TaskRepository>()
    private val service = TaskService(repository)

    @Test
    fun `createTask should return TaskResponse`() {
        val request = TaskRequest("Test Task", "Desc")
        val savedTask = Task(1, "Test Task", "Desc", TaskStatus.NEW, LocalDateTime.now(), LocalDateTime.now())

        every { repository.save(any()) } returns Mono.just(savedTask)

        val result = service.createTask(request)

        StepVerifier.create(result)
            .expectNextMatches { it.id == 1L && it.title == "Test Task" }
            .verifyComplete()
    }
}