package io.adot442.todo.controller

import io.adot442.todo.model.CreateTodoRequest
import io.adot442.todo.model.Todo
import io.adot442.todo.model.UpdateTodoRequest
import io.adot442.todo.service.TodoService
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@MicronautTest
@DisplayName("TodoController")
class TodoControllerTest {

    @Inject
    @field:Client("/")
    lateinit var client: HttpClient

    @Inject
    lateinit var todoService: TodoService

    @AfterEach
    fun tearDown() {
        todoService.deleteAll()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/todos
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/todos")
    inner class GetAll {

        @Test
        @DisplayName("returns 200 with an empty list when no todos exist")
        fun returns200WithEmptyList() {
            val response = client.toBlocking()
                .exchange(HttpRequest.GET<Any>("/api/todos"), Argument.listOf(Todo::class.java))
            assertEquals(HttpStatus.OK, response.status)
            assertTrue(response.body()!!.isEmpty())
        }

        @Test
        @DisplayName("returns 200 with all todos")
        fun returns200WithAllTodos() {
            todoService.create(CreateTodoRequest("First"))
            todoService.create(CreateTodoRequest("Second"))
            val response = client.toBlocking()
                .exchange(HttpRequest.GET<Any>("/api/todos"), Argument.listOf(Todo::class.java))
            assertEquals(HttpStatus.OK, response.status)
            assertEquals(2, response.body()!!.size)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/todos/{id}
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/todos/{id}")
    inner class GetById {

        @Test
        @DisplayName("returns 200 with the todo when it exists")
        fun returns200ForExistingTodo() {
            val created = todoService.create(CreateTodoRequest("My Task", "Details"))
            val response = client.toBlocking().exchange("/api/todos/${created.id}", Todo::class.java)
            assertEquals(HttpStatus.OK, response.status)
            val body = response.body()!!
            assertEquals(created.id, body.id)
            assertEquals("My Task", body.title)
            assertEquals("Details", body.description)
            assertFalse(body.completed)
        }

        @Test
        @DisplayName("returns 404 when the todo does not exist")
        fun returns404ForMissingTodo() {
            val ex = assertThrows<HttpClientResponseException> {
                client.toBlocking().exchange("/api/todos/999999", Todo::class.java)
            }
            assertEquals(HttpStatus.NOT_FOUND, ex.status)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/todos
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/todos")
    inner class CreateTodo {

        @Test
        @DisplayName("returns 201 with the created todo (title only)")
        fun returns201WithTitleOnly() {
            val request = HttpRequest.POST("/api/todos", CreateTodoRequest("Buy milk"))
            val response = client.toBlocking().exchange(request, Todo::class.java)
            assertEquals(HttpStatus.CREATED, response.status)
            val body = response.body()!!
            assertEquals("Buy milk", body.title)
            assertNull(body.description)
            assertFalse(body.completed)
            assertTrue(body.id > 0)
        }

        @Test
        @DisplayName("returns 201 with the created todo (title + description)")
        fun returns201WithTitleAndDescription() {
            val request = HttpRequest.POST(
                "/api/todos",
                CreateTodoRequest("Buy milk", "Whole milk 2L")
            )
            val response = client.toBlocking().exchange(request, Todo::class.java)
            assertEquals(HttpStatus.CREATED, response.status)
            val body = response.body()!!
            assertEquals("Buy milk", body.title)
            assertEquals("Whole milk 2L", body.description)
        }

        @Test
        @DisplayName("persists the todo so it is retrievable afterwards")
        fun persistsTodo() {
            val request = HttpRequest.POST("/api/todos", CreateTodoRequest("Persisted Task"))
            val created = client.toBlocking().exchange(request, Todo::class.java).body()!!
            assertNotNull(todoService.findById(created.id))
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/todos/{id}
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("PUT /api/todos/{id}")
    inner class UpdateTodo {

        @Test
        @DisplayName("returns 200 and updates title")
        fun returns200AndUpdatesTitle() {
            val created = todoService.create(CreateTodoRequest("Old Title"))
            val request = HttpRequest.PUT("/api/todos/${created.id}", UpdateTodoRequest(title = "New Title"))
            val response = client.toBlocking().exchange(request, Todo::class.java)
            assertEquals(HttpStatus.OK, response.status)
            assertEquals("New Title", response.body()!!.title)
        }

        @Test
        @DisplayName("returns 200 and marks todo as completed")
        fun returns200AndMarkAsCompleted() {
            val created = todoService.create(CreateTodoRequest("Task"))
            val request = HttpRequest.PUT("/api/todos/${created.id}", UpdateTodoRequest(completed = true))
            val response = client.toBlocking().exchange(request, Todo::class.java)
            assertEquals(HttpStatus.OK, response.status)
            assertTrue(response.body()!!.completed)
        }

        @Test
        @DisplayName("returns 200 and updates all fields at once")
        fun returns200AndUpdatesAllFields() {
            val created = todoService.create(CreateTodoRequest("Old", "Old desc"))
            val request = HttpRequest.PUT(
                "/api/todos/${created.id}",
                UpdateTodoRequest(title = "New", description = "New desc", completed = true)
            )
            val response = client.toBlocking().exchange(request, Todo::class.java)
            val body = response.body()!!
            assertEquals("New", body.title)
            assertEquals("New desc", body.description)
            assertTrue(body.completed)
        }

        @Test
        @DisplayName("returns 404 for a non-existent todo")
        fun returns404ForMissingTodo() {
            val request = HttpRequest.PUT("/api/todos/999999", UpdateTodoRequest(title = "X"))
            val ex = assertThrows<HttpClientResponseException> {
                client.toBlocking().exchange(request, Todo::class.java)
            }
            assertEquals(HttpStatus.NOT_FOUND, ex.status)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/todos/{id}
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/todos/{id}")
    inner class DeleteById {

        @Test
        @DisplayName("returns 204 and removes the todo")
        fun returns204AndRemovesTodo() {
            val created = todoService.create(CreateTodoRequest("Task"))
            val response = client.toBlocking()
                .exchange(HttpRequest.DELETE<Void>("/api/todos/${created.id}"), Void::class.java)
            assertEquals(HttpStatus.NO_CONTENT, response.status)
            assertNull(todoService.findById(created.id))
        }

        @Test
        @DisplayName("returns 404 for a non-existent todo")
        fun returns404ForMissingTodo() {
            val ex = assertThrows<HttpClientResponseException> {
                client.toBlocking()
                    .exchange(HttpRequest.DELETE<Void>("/api/todos/999999"), Void::class.java)
            }
            assertEquals(HttpStatus.NOT_FOUND, ex.status)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/todos
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/todos")
    inner class DeleteAll {

        @Test
        @DisplayName("returns 204 and empties the store")
        fun returns204AndEmptiesStore() {
            todoService.create(CreateTodoRequest("A"))
            todoService.create(CreateTodoRequest("B"))
            val response = client.toBlocking()
                .exchange(HttpRequest.DELETE<Void>("/api/todos"), Void::class.java)
            assertEquals(HttpStatus.NO_CONTENT, response.status)
            assertTrue(todoService.findAll().isEmpty())
        }

        @Test
        @DisplayName("returns 204 even when store is already empty")
        fun returns204OnEmptyStore() {
            val response = client.toBlocking()
                .exchange(HttpRequest.DELETE<Void>("/api/todos"), Void::class.java)
            assertEquals(HttpStatus.NO_CONTENT, response.status)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/todos/stats
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/todos/stats")
    inner class GetStats {

        @Test
        @DisplayName("returns 200 with zeroed stats on empty store")
        fun returnsZeroedStatsOnEmptyStore() {
            val response = client.toBlocking()
                .exchange(HttpRequest.GET<Any>("/api/todos/stats"), Argument.mapOf(String::class.java, Any::class.java))
            assertEquals(HttpStatus.OK, response.status)
            val stats = response.body()!!
            assertEquals(0, (stats["total"] as Number).toInt())
            assertEquals(0, (stats["completed"] as Number).toInt())
            assertEquals(0, (stats["pending"] as Number).toInt())
        }

        @Test
        @DisplayName("returns 200 with correct completed and pending counts")
        fun returnsCorrectStats() {
            val t1 = todoService.create(CreateTodoRequest("A"))
            todoService.create(CreateTodoRequest("B"))
            todoService.update(t1.id, UpdateTodoRequest(completed = true))
            val response = client.toBlocking()
                .exchange(HttpRequest.GET<Any>("/api/todos/stats"), Argument.mapOf(String::class.java, Any::class.java))
            val stats = response.body()!!
            assertEquals(2, (stats["total"] as Number).toInt())
            assertEquals(1, (stats["completed"] as Number).toInt())
            assertEquals(1, (stats["pending"] as Number).toInt())
        }
    }
}
