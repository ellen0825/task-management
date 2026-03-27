package com.example.taskmanagement.repository

import com.example.taskmanagement.model.Task
import com.example.taskmanagement.model.TaskStatus
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.time.LocalDateTime

@Repository
class TaskRepository(private val client: DatabaseClient) {

    fun save(task: Task): Mono<Task> {
        val sql = """
            INSERT INTO tasks (title, description, status, created_at, updated_at)
            VALUES (:title, :desc, :status, :createdAt, :updatedAt)
            RETURNING id
        """.trimIndent()

        return client.sql(sql)
            .bind("title", task.title)
            .bind("desc", task.description)
            .bind("status", task.status.name)
            .bind("createdAt", task.createdAt)
            .bind("updatedAt", task.updatedAt)
            .map { row -> task.copy(id = row["id"] as Long) }
            .one()
    }

    fun findById(id: Long): Mono<Task> {
        val sql = "SELECT * FROM tasks WHERE id = :id"
        return client.sql(sql)
            .bind("id", id)
            .map { row ->
                Task(
                    id = row["id"] as Long,
                    title = row["title"] as String,
                    description = row["description"] as String?,
                    status = TaskStatus.valueOf(row["status"] as String),
                    createdAt = row["created_at"] as LocalDateTime,
                    updatedAt = row["updated_at"] as LocalDateTime
                )
            }.one()
    }

}