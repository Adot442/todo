package io.adot442.todo.model

import io.micronaut.serde.annotation.Serdeable
import java.time.LocalDateTime

@Serdeable
data class Todo(
    val id: Long,
    var title: String,
    var description: String? = null,
    var completed: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

@Serdeable
data class CreateTodoRequest(
    val title: String,
    val description: String? = null
)

@Serdeable
data class UpdateTodoRequest(
    val title: String? = null,
    val description: String? = null,
    val completed: Boolean? = null
)

