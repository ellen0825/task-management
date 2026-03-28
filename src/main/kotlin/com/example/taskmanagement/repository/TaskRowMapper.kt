package com.example.taskmanagement.repository

import com.example.taskmanagement.model.Task
import com.example.taskmanagement.model.TaskStatus
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.time.LocalDateTime

object TaskRowMapper : RowMapper<Task> {
    override fun map(rs: ResultSet, rowNum: Int) = Task(
        id = rs.getLong("id"),
        title = rs.getString("title"),
        description = rs.getString("description"),
        status = TaskStatus.valueOf(rs.getString("status")),
        createdAt = rs.getObject("created_at", LocalDateTime::class.java),
        updatedAt = rs.getObject("updated_at", LocalDateTime::class.java)
    )
}
