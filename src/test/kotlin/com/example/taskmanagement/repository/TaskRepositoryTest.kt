package com.example.taskmanagement.repository

import com.example.taskmanagement.model.Task
import com.example.taskmanagement.model.TaskStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.jdbc.core.simple.JdbcClient
import java.time.LocalDateTime
import java.util.Optional

class TaskRepositoryTest {

    private val now = LocalDateTime.now()
    private val task = Task(id = 1L, title = "Test", description = "Desc", status = TaskStatus.NEW, createdAt = now, updatedAt = now)

    private fun buildMockChain(): Pair<JdbcClient, JdbcClient.StatementSpec> {
        val client: JdbcClient = mock()
        val spec: JdbcClient.StatementSpec = mock {
            on { param(any<String>(), anyOrNull()) } doReturn it
        }
        whenever(client.sql(any<String>())).thenReturn(spec)
        return client to spec
    }

    @Test
    fun `findById returns task when found`() {
        val (client, spec) = buildMockChain()
        @Suppress("UNCHECKED_CAST")
        val querySpec: JdbcClient.MappedQuerySpec<Task> = mock {
            on { optional() } doReturn Optional.of(task)
        }
        whenever(spec.query(any<org.springframework.jdbc.core.RowMapper<Task>>())).thenReturn(querySpec)

        val result = TaskRepository(client).findById(1L)

        assertNotNull(result)
        assertEquals(1L, result?.id)
        verify(client).sql("SELECT * FROM tasks WHERE id = :id")
        verify(spec).param("id", 1L)
    }

    @Test
    fun `findById returns null when not found`() {
        val (client, spec) = buildMockChain()
        @Suppress("UNCHECKED_CAST")
        val querySpec: JdbcClient.MappedQuerySpec<Task> = mock {
            on { optional() } doReturn Optional.empty()
        }
        whenever(spec.query(any<org.springframework.jdbc.core.RowMapper<Task>>())).thenReturn(querySpec)

        val result = TaskRepository(client).findById(99L)

        assertNull(result)
    }

    @Test
    fun `findAll without status uses no WHERE clause`() {
        val (client, spec) = buildMockChain()
        @Suppress("UNCHECKED_CAST")
        val querySpec: JdbcClient.MappedQuerySpec<Task> = mock {
            on { list() } doReturn listOf(task)
        }
        whenever(spec.query(any<org.springframework.jdbc.core.RowMapper<Task>>())).thenReturn(querySpec)

        val result = TaskRepository(client).findAll(null, 0, 10)

        assertEquals(1, result.size)
        verify(client).sql(argThat { !contains("WHERE") && contains("ORDER BY created_at DESC") })
    }

    @Test
    fun `findAll with status adds WHERE clause`() {
        val (client, spec) = buildMockChain()
        @Suppress("UNCHECKED_CAST")
        val querySpec: JdbcClient.MappedQuerySpec<Task> = mock {
            on { list() } doReturn listOf(task)
        }
        whenever(spec.query(any<org.springframework.jdbc.core.RowMapper<Task>>())).thenReturn(querySpec)

        TaskRepository(client).findAll(TaskStatus.NEW, 0, 10)

        verify(client).sql(argThat { contains("WHERE status = :status") })
        verify(spec).param("status", "NEW")
    }

    @Test
    fun `count without status returns total`() {
        val (client, spec) = buildMockChain()
        @Suppress("UNCHECKED_CAST")
        val querySpec: JdbcClient.MappedQuerySpec<Long> = mock {
            on { single() } doReturn 5L
        }
        whenever(spec.query(Long::class.java)).thenReturn(querySpec)

        val result = TaskRepository(client).count(null)

        assertEquals(5L, result)
        verify(client).sql("SELECT COUNT(*) FROM tasks")
    }

    @Test
    fun `count with status adds WHERE clause`() {
        val (client, spec) = buildMockChain()
        @Suppress("UNCHECKED_CAST")
        val querySpec: JdbcClient.MappedQuerySpec<Long> = mock {
            on { single() } doReturn 2L
        }
        whenever(spec.query(Long::class.java)).thenReturn(querySpec)

        val result = TaskRepository(client).count(TaskStatus.DONE)

        assertEquals(2L, result)
        verify(client).sql("SELECT COUNT(*) FROM tasks WHERE status = :status")
        verify(spec).param("status", "DONE")
    }

    @Test
    fun `deleteById returns affected row count`() {
        val (client, spec) = buildMockChain()
        whenever(spec.update()).thenReturn(1)

        val result = TaskRepository(client).deleteById(1L)

        assertEquals(1, result)
        verify(client).sql("DELETE FROM tasks WHERE id = :id")
        verify(spec).param("id", 1L)
    }

    @Test
    fun `deleteById returns 0 when task not found`() {
        val (client, spec) = buildMockChain()
        whenever(spec.update()).thenReturn(0)

        val result = TaskRepository(client).deleteById(99L)

        assertEquals(0, result)
    }
}
