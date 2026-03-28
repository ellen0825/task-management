package com.example.taskmanagement.repository

import com.example.taskmanagement.model.TaskStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.sql.ResultSet
import java.time.LocalDateTime

class TaskRowMapperTest {

    private val now = LocalDateTime.of(2026, 3, 26, 12, 0, 0)

    private fun mockResultSet(
        id: Long = 1L,
        title: String = "Test",
        description: String? = "Desc",
        status: String = "NEW",
        createdAt: LocalDateTime = now,
        updatedAt: LocalDateTime = now
    ): ResultSet = mock {
        whenever(it.getLong("id")).thenReturn(id)
        whenever(it.getString("title")).thenReturn(title)
        whenever(it.getString("description")).thenReturn(description)
        whenever(it.getString("status")).thenReturn(status)
        whenever(it.getObject("created_at", LocalDateTime::class.java)).thenReturn(createdAt)
        whenever(it.getObject("updated_at", LocalDateTime::class.java)).thenReturn(updatedAt)
    }

    @Test
    fun `maps all fields correctly`() {
        val rs = mockResultSet()
        val task = TaskRowMapper.map(rs, 0)

        assertEquals(1L, task.id)
        assertEquals("Test", task.title)
        assertEquals("Desc", task.description)
        assertEquals(TaskStatus.NEW, task.status)
        assertEquals(now, task.createdAt)
        assertEquals(now, task.updatedAt)
    }

    @Test
    fun `maps null description`() {
        val rs = mockResultSet(description = null)
        val task = TaskRowMapper.map(rs, 0)
        assertNull(task.description)
    }

    @Test
    fun `maps all TaskStatus values`() {
        TaskStatus.entries.forEach { status ->
            val rs = mockResultSet(status = status.name)
            val task = TaskRowMapper.map(rs, 0)
            assertEquals(status, task.status)
        }
    }
}
