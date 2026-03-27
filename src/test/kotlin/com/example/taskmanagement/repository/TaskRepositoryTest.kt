package com.example.taskmanagement.repository

import com.example.taskmanagement.model.Task
import com.example.taskmanagement.model.TaskStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.time.LocalDateTime
import java.util.Optional

class TaskRepositoryTest {

    // Helpers to build a fluent mock chain for JdbcClient
    private fun mockSpec(result: JdbcClient.MappedQuerySpec<*>? = null): Triple<JdbcClient, JdbcClient.StatementSpec, JdbcClient.MappedQuerySpec<Task>> {
        val jdbcClient: JdbcClient = mock()
        val statementSpec: JdbcClient.StatementSpec = mock()
        @Suppress("UNCHECKED_CAST")
        val querySpec: JdbcClient.MappedQuerySpec<Task> = mock()

        whenever(jdbcClient.sql(any<String>())).thenReturn(statementSpec)
        whenever(statementSpec.param(any<String>(), anyOrNull())).thenReturn(statementSpec)
        whenever(statementSpec.query(any<org.springframework.jdbc.core.RowMapper<Task>>())).thenReturn(querySpec)

        return Triple(jdbcClient, statementSpec, querySpec)
    }

    private val now = LocalDateTime.now()
    private val task = Task(id = 1L, title = "Test", description = "Desc", status = TaskStatus.NEW, createdAt = now, updatedAt = now)

    @Test
    fun `findById returns task when found`() {
        val (jdbcClient, _, querySpec) = mockSpec()
        whenever(querySpec.optional()).thenReturn(Optional.of(task))

        val repo = TaskRepository(jdbcClient)
        val result = repo.findById(1L)

        assertNotNull(result)
        assertEquals(1L, result?.id)
        assertEquals("Test", result?.title)
        verify(jdbcClient).sql("SELECT * FROM tasks WHERE id = :id")
    }

    @Test
    fun `findById returns null when not found`() {
        val (jdbcClient, _, querySpec) = mockSpec()
        whenever(querySpec.optional()).thenReturn(Optional.empty())

        val result = TaskRepository(jdbcClient).findById(99L)

        assertNull(result)
    }

    @Test
    fun `findAll without status returns list`() {
        val (jdbcClient, _, querySpec) = mockSpec()
        whenever(querySpec.list()).thenReturn(listOf(task))

        val result = TaskRepository(jdbcClient).findAll(null, 0, 10)

        assertEquals(1, result.size)
        verify(jdbcClient).sql(argThat { contains("ORDER BY created_at DESC LIMIT :size OFFSET :offset") && !contains("WHERE") })
    }

    @Test
    fun `findAll with status adds WHERE clause`() {
        val (jdbcClient, _, querySpec) = mockSpec()
        whenever(querySpec.list()).thenReturn(listOf(task))

        TaskRepository(jdbcClient).findAll(TaskStatus.NEW, 0, 10)

        verify(jdbcClient).sql(argThat { contains("WHERE status = :status") })
    }

    @Test
    fun `count without status queries total`() {
        val jdbcClient: JdbcClient = mock()
        val statementSpec: JdbcClient.StatementSpec = mock()
        @Suppress("UNCHECKED_CAST")
        val querySpec: JdbcClient.MappedQuerySpec<Long> = mock()

        whenever(jdbcClient.sql(any<String>())).thenReturn(statementSpec)
        whenever(statementSpec.param(any<String>(), anyOrNull())).thenReturn(statementSpec)
        whenever(statementSpec.query(Long::class.java)).thenReturn(querySpec)
        whenever(querySpec.single()).thenReturn(5L)

        val result = TaskRepository(jdbcClient).count(null)

        assertEquals(5L, result)
        verify(jdbcClient).sql("SELECT COUNT(*) FROM tasks")
    }

    @Test
    fun `deleteById executes delete SQL`() {
        val jdbcClient: JdbcClient = mock()
        val statementSpec: JdbcClient.StatementSpec = mock()

        whenever(jdbcClient.sql(any<String>())).thenReturn(statementSpec)
        whenever(statementSpec.param(any<String>(), anyOrNull())).thenReturn(statementSpec)
        whenever(statementSpec.update()).thenReturn(1)

        val result = TaskRepository(jdbcClient).deleteById(1L)

        assertEquals(1, result)
        verify(jdbcClient).sql("DELETE FROM tasks WHERE id = :id")
    }
}
