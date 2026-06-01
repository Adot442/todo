package io.adot442.todo.service

import io.adot442.todo.model.CreateTodoRequest
import io.adot442.todo.model.Todo
import io.adot442.todo.model.UpdateTodoRequest
import jakarta.inject.Singleton
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Singleton
class TodoService {
    private val todos = ConcurrentHashMap<Long, Todo>()
    private val idGenerator = AtomicLong(1)

    fun findAll(): List<Todo> {
        return todos.values.sortedBy { it.createdAt }
    }

    fun findById(id: Long): Todo? {
        return todos[id]
    }

    fun create(request: CreateTodoRequest): Todo {
        val id = idGenerator.getAndIncrement()
        val todo = Todo(
            id = id,
            title = request.title,
            description = request.description
        )
        todos[id] = todo
        return todo
    }

    fun update(id: Long, request: UpdateTodoRequest): Todo? {
        val todo = todos[id] ?: return null

        request.title?.let { todo.title = it }
        request.description?.let { todo.description = it }
        request.completed?.let { todo.completed = it }
        todo.updatedAt = LocalDateTime.now()

        return todo
    }

    fun delete(id: Long): Boolean {
        return todos.remove(id) != null
    }

    fun deleteAll() {
        todos.clear()
    }

    fun countCompleted(): Long {
        return todos.values.count { it.completed }.toLong()
    }

    fun countPending(): Long {
        return todos.values.count { !it.completed }.toLong()
    }
}

