package com.example.taskmanagement.service

import com.example.taskmanagement.dto.TaskRequest
import com.example.taskmanagement.model.Task
import com.example.taskmanagement.model.TaskStatus
import com.example.taskmanagement.repository.TaskRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.web.server.ResponseStatusException
import reactor.test.StepVerifier
import java.time.LocalDateTime

class TaskServiceTest {

    private val repository: TaskRepository = mock()
    private val service = TaskService(repository)

    private val now = LocalDateTime.now()
    private val task = Task(id = 1L, title = "Test", description = "Desc", status = TaskStatus.NEW, createdAt = now, updatedAt = now)

    @Test
    fun `createTask saves and returns response`() {
        whenever(repository.save(any())).thenReturn(task)

        StepVerifier.create(service.createTask(TaskRequest("Test", "Desc")))
            .assertNext {
                assert(it.id == 1L)
                assert(it.title == "Test")
                assert(it.status == TaskStatus.NEW)
            }
            .verifyComplete()
    }

    @Test
    fun `getTaskById returns response when found`() {
        whenever(repository.findById(1L)).thenReturn(task)

        StepVerifier.create(service.getTaskById(1L))
            .assertNext { assert(it.id == 1L) }
            .verifyComplete()
    }

    @Test
    fun `getTaskById throws 404 when not found`() {
        whenever(repository.findById(99L)).thenReturn(null)

        StepVerifier.create(service.getTaskById(99L))
            .expectErrorMatches { it is ResponseStatusException && it.statusCode.value() == 404 }
            .verify()
    }

    @Test
    fun `getTasks returns page response with pagination`() {
        whenever(repository.findAll(null, 0, 10)).thenReturn(listOf(task))
        whenever(repository.count(null)).thenReturn(1L)

        StepVerifier.create(service.getTasks(null, 0, 10))
            .assertNext {
                assert(it.content.size == 1)
                assert(it.totalElements == 1L)
                assert(it.totalPages == 1)
                assert(it.page == 0)
                assert(it.size == 10)
            }
            .verifyComplete()
    }

    @Test
    fun `getTasks filters by status`() {
        whenever(repository.findAll(TaskStatus.DONE, 0, 5)).thenReturn(emptyList())
        whenever(repository.count(TaskStatus.DONE)).thenReturn(0L)

        StepVerifier.create(service.getTasks(TaskStatus.DONE, 0, 5))
            .assertNext {
                assert(it.content.isEmpty())
                assert(it.totalElements == 0L)
            }
            .verifyComplete()

        verify(repository).findAll(TaskStatus.DONE, 0, 5)
        verify(repository).count(TaskStatus.DONE)
    }

    @Test
    fun `updateStatus returns updated response`() {
        val updated = task.copy(status = TaskStatus.DONE)
        whenever(repository.updateStatus(1L, TaskStatus.DONE)).thenReturn(updated)

        StepVerifier.create(service.updateStatus(1L, TaskStatus.DONE))
            .assertNext { assert(it.status == TaskStatus.DONE) }
            .verifyComplete()
    }

    @Test
    fun `updateStatus throws 404 when task not found`() {
        whenever(repository.updateStatus(99L, TaskStatus.DONE)).thenReturn(null)

        StepVerifier.create(service.updateStatus(99L, TaskStatus.DONE))
            .expectErrorMatches { it is ResponseStatusException && it.statusCode.value() == 404 }
            .verify()
    }

    @Test
    fun `deleteTask completes when task exists`() {
        whenever(repository.deleteById(1L)).thenReturn(1)

        StepVerifier.create(service.deleteTask(1L))
            .verifyComplete()
    }

    @Test
    fun `deleteTask throws 404 when task not found`() {
        whenever(repository.deleteById(99L)).thenReturn(0)

        StepVerifier.create(service.deleteTask(99L))
            .expectErrorMatches { it is ResponseStatusException && it.statusCode.value() == 404 }
            .verify()
    }
}
