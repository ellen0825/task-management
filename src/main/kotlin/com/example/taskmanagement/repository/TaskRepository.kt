package com.example.taskmanagement.repository

import com.example.taskmanagement.model.Task
import com.example.taskmanagement.model.TaskStatus
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.LocalDateTime

@Repository
class TaskRepository(private val jdbcClient: JdbcClient) {

    private fun ResultSet.toTask() = Task(
        id = getLong("id"),
        title = getString("title"),
        description = getString("description"),
        status = TaskStatus.valueOf(getString("status")),
        createdAt = getObject("created_at", LocalDateTime::class.java),
        updatedAt = getObject("updated_at", LocalDateTime::class.java)
    )

    fun save(task: Task): Task {
        val keyHolder = GeneratedKeyHolder()
        jdbcClient.sql(
            "INSERT INTO tasks (title, description, status, created_at, updated_at) " +
            "VALUES (:title, :description, :status, :createdAt, :updatedAt)"
        )
            .param("title", task.title)
            .param("description", task.description)
            .param("status", task.status.name)
            .param("createdAt", task.createdAt)
            .param("updatedAt", task.updatedAt)
            .update(keyHolder, "id")
        return task.copy(id = keyHolder.key!!.toLong())
    }

    fun findById(id: Long): Task? =
        jdbcClient.sql("SELECT * FROM tasks WHERE id = :id")
            .param("id", id)
            .query { rs, _ -> rs.toTask() }
            .optional()
            .orElse(null)

    fun findAll(status: TaskStatus?, page: Int, size: Int): List<Task> {
        val where = if (status != null) " WHERE status = :status" else ""
        return jdbcClient.sql(
            "SELECT * FROM tasks$where ORDER BY created_at DESC LIMIT :size OFFSET :offset"
        )
            .apply { if (status != null) param("status", status.name) }
            .param("size", size)
            .param("offset", page * size)
            .query { rs, _ -> rs.toTask() }
            .list()
    }

    fun count(status: TaskStatus?): Long {
        val where = if (status != null) " WHERE status = :status" else ""
        return jdbcClient.sql("SELECT COUNT(*) FROM tasks$where")
            .apply { if (status != null) param("status", status.name) }
            .query(Long::class.java)
            .single()
    }

    fun updateStatus(id: Long, status: TaskStatus): Task? {
        jdbcClient.sql(
            "UPDATE tasks SET status = :status, updated_at = :updatedAt WHERE id = :id"
        )
            .param("status", status.name)
            .param("updatedAt", LocalDateTime.now())
            .param("id", id)
            .update()
        return findById(id)
    }

    fun deleteById(id: Long): Int =
        jdbcClient.sql("DELETE FROM tasks WHERE id = :id")
            .param("id", id)
            .update()
}
