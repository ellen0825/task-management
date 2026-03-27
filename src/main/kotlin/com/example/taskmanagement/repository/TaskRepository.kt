package com.example.taskmanagement.repository

import com.example.taskmanagement.model.Task
import com.example.taskmanagement.model.TaskStatus
import org.springframework.r2dbc.core.DatabaseClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

class TaskRepository(private val client: DatabaseClient) {

    fun save(task: Task): Mono<Task> {
        val sql = """
            INSERT INTO tasks (title, description, status, created_at, updated_at) 
            VALUES (:title, :description, :status, :createdAt, :updatedAt)
            RETURNING id
        """.trimIndent()

        return client.sql(sql)
            .bind("title", task.title)
            .bind("description", task.description)
            .bind("status", task.status.name)
            .bind("createdAt", task.createdAt)
            .bind("updatedAt", task.updatedAt)
            .map { row -> task.copy(id = row.get("id", java.lang.Long::class.java)?.toLong()) }
            .one()
    }

    fun findById(id: Long): Mono<Task> {
        val sql = "SELECT * FROM tasks WHERE id = :id"
        return client.sql(sql)
            .bind("id", id)
            .map { row ->
                Task(
                    id = row.get("id", java.lang.Long::class.java)?.toLong(),
                    title = row.get("title", String::class.java)!!,
                    description = row.get("description", String::class.java),
                    status = TaskStatus.valueOf(row.get("status", String::class.java)!!),
                    createdAt = row.get("created_at", LocalDateTime::class.java)!!,
                    updatedAt = row.get("updated_at", LocalDateTime::class.java)!!
                )
            }
            .one()
    }

    fun findAll(status: TaskStatus? = null, page: Int = 0, size: Int = 10): Flux<Task> {
        val sql = StringBuilder("SELECT * FROM tasks")
        if (status != null) sql.append(" WHERE status = :status")
        sql.append(" ORDER BY created_at DESC LIMIT :size OFFSET :offset")

        val query = client.sql(sql.toString())
        if (status != null) query.bind("status", status.name)
        query.bind("size", size)
        query.bind("offset", page * size)

        return query.map { row ->
            Task(
                id = row.get("id", java.lang.Long::class.java)?.toLong(),
                title = row.get("title", String::class.java)!!,
                description = row.get("description", String::class.java),
                status = TaskStatus.valueOf(row.get("status", String::class.java)!!),
                createdAt = row.get("created_at", LocalDateTime::class.java)!!,
                updatedAt = row.get("updated_at", LocalDateTime::class.java)!!
            )
        }.all()
    }

    fun updateStatus(id: Long, status: TaskStatus): Mono<Task> {
        val sql = "UPDATE tasks SET status = :status, updated_at = :updatedAt WHERE id = :id"
        return client.sql(sql)
            .bind("status", status.name)
            .bind("updatedAt", LocalDateTime.now())
            .bind("id", id)
            .then(findById(id))
    }

    fun deleteById(id: Long): Mono<Void> {
        val sql = "DELETE FROM tasks WHERE id = :id"
        return client.sql(sql)
            .bind("id", id)
            .then()
    }
}