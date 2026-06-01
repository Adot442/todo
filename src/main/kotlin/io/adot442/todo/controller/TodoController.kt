package io.adot442.todo.controller

import io.adot442.todo.model.CreateTodoRequest
import io.adot442.todo.model.Todo
import io.adot442.todo.model.UpdateTodoRequest
import io.adot442.todo.service.TodoService
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag

@Controller("/api/todos")
@Tag(name = "Todo", description = "Todo management endpoints")
class TodoController(private val todoService: TodoService) {

    @Get
    @Operation(summary = "Get all todos", description = "Retrieves all todos from the in-memory store")
    @ApiResponse(responseCode = "200", description = "List of todos")
    fun getAll(): List<Todo> {
        return todoService.findAll()
    }

    @Get("/{id}")
    @Operation(summary = "Get todo by ID", description = "Retrieves a single todo by its ID")
    @ApiResponse(responseCode = "200", description = "Todo found")
    @ApiResponse(responseCode = "404", description = "Todo not found")
    fun getById(@PathVariable id: Long): HttpResponse<Todo> {
        val todo = todoService.findById(id)
        return if (todo != null) {
            HttpResponse.ok(todo)
        } else {
            HttpResponse.notFound()
        }
    }

    @Post
    @Operation(summary = "Create a new todo", description = "Creates a new todo item")
    @ApiResponse(
        responseCode = "201",
        description = "Todo created",
        content = [Content(schema = Schema(implementation = Todo::class))]
    )
    @Status(HttpStatus.CREATED)
    fun create(@Body request: CreateTodoRequest): Todo {
        return todoService.create(request)
    }

    @Put("/{id}")
    @Operation(summary = "Update a todo", description = "Updates an existing todo item")
    @ApiResponse(responseCode = "200", description = "Todo updated")
    @ApiResponse(responseCode = "404", description = "Todo not found")
    fun update(@PathVariable id: Long, @Body request: UpdateTodoRequest): HttpResponse<Todo> {
        val todo = todoService.update(id, request)
        return if (todo != null) {
            HttpResponse.ok(todo)
        } else {
            HttpResponse.notFound()
        }
    }

    @Delete("/{id}")
    @Operation(summary = "Delete a todo", description = "Deletes a todo item by ID")
    @ApiResponse(responseCode = "204", description = "Todo deleted")
    @ApiResponse(responseCode = "404", description = "Todo not found")
    fun delete(@PathVariable id: Long): HttpResponse<Void> {
        return if (todoService.delete(id)) {
            HttpResponse.noContent()
        } else {
            HttpResponse.notFound()
        }
    }

    @Delete
    @Operation(summary = "Delete all todos", description = "Deletes all todos from the in-memory store")
    @ApiResponse(responseCode = "204", description = "All todos deleted")
    fun deleteAll(): HttpResponse<Void> {
        todoService.deleteAll()
        return HttpResponse.noContent()
    }

    @Get("/stats")
    @Operation(summary = "Get todo statistics", description = "Returns statistics about todos")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved")
    fun getStats(): Map<String, Any> {
        return mapOf(
            "total" to todoService.findAll().size,
            "completed" to todoService.countCompleted(),
            "pending" to todoService.countPending()
        )
    }
}

