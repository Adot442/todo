package io.adot442.todo.service

import io.adot442.todo.model.CreateTodoRequest
import io.adot442.todo.model.UpdateTodoRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("TodoService")
class TodoServiceTest {

    private lateinit var service: TodoService

    @BeforeEach
    fun setUp() {
        service = TodoService()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findAll
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findAll()")
    inner class FindAll {

        @Test
        @DisplayName("returns empty list when store is empty")
        fun returnsEmptyListWhenStoreIsEmpty() {
            assertTrue(service.findAll().isEmpty())
        }

        @Test
        @DisplayName("returns all created todos")
        fun returnsAllCreatedTodos() {
            service.create(CreateTodoRequest("First"))
            service.create(CreateTodoRequest("Second"))
            assertEquals(2, service.findAll().size)
        }

        @Test
        @DisplayName("returns todos sorted by createdAt ascending")
        fun returnsTodosSortedByCreatedAtAscending() {
            service.create(CreateTodoRequest("First"))
            service.create(CreateTodoRequest("Second"))
            val todos = service.findAll()
            assertEquals("First", todos[0].title)
            assertEquals("Second", todos[1].title)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findById
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findById()")
    inner class FindById {

        @Test
        @DisplayName("returns the todo when it exists")
        fun returnsTodoWhenExists() {
            val created = service.create(CreateTodoRequest("My Task"))
            val found = service.findById(created.id)
            assertNotNull(found)
            assertEquals(created.id, found!!.id)
            assertEquals("My Task", found.title)
        }

        @Test
        @DisplayName("returns null for an unknown id")
        fun returnsNullForUnknownId() {
            assertNull(service.findById(999L))
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // create
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("create()")
    inner class Create {

        @Test
        @DisplayName("creates a todo with just a title")
        fun createsTodoWithTitle() {
            val todo = service.create(CreateTodoRequest("Buy milk"))
            assertEquals("Buy milk", todo.title)
            assertNull(todo.description)
            assertFalse(todo.completed)
            assertTrue(todo.id > 0)
        }

        @Test
        @DisplayName("creates a todo with title and description")
        fun createsTodoWithTitleAndDescription() {
            val todo = service.create(CreateTodoRequest("Buy milk", "Whole milk 2L"))
            assertEquals("Buy milk", todo.title)
            assertEquals("Whole milk 2L", todo.description)
        }

        @Test
        @DisplayName("assigns unique incrementing ids to each todo")
        fun assignsUniqueIncrementingIds() {
            val t1 = service.create(CreateTodoRequest("A"))
            val t2 = service.create(CreateTodoRequest("B"))
            val t3 = service.create(CreateTodoRequest("C"))
            assertNotEquals(t1.id, t2.id)
            assertNotEquals(t2.id, t3.id)
            assertTrue(t2.id > t1.id)
            assertTrue(t3.id > t2.id)
        }

        @Test
        @DisplayName("persists todo so it can be found by id")
        fun persistsTodoFindableById() {
            val todo = service.create(CreateTodoRequest("Task"))
            assertNotNull(service.findById(todo.id))
        }

        @Test
        @DisplayName("sets timestamps on creation")
        fun setsTimestampsOnCreation() {
            val todo = service.create(CreateTodoRequest("Task"))
            assertNotNull(todo.createdAt)
            assertNotNull(todo.updatedAt)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // update
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("update()")
    inner class Update {

        @Test
        @DisplayName("returns null when todo does not exist")
        fun returnsNullWhenTodoDoesNotExist() {
            assertNull(service.update(999L, UpdateTodoRequest(title = "X")))
        }

        @Test
        @DisplayName("updates the title when provided")
        fun updatesTitle() {
            val todo = service.create(CreateTodoRequest("Old Title"))
            val updated = service.update(todo.id, UpdateTodoRequest(title = "New Title"))
            assertNotNull(updated)
            assertEquals("New Title", updated!!.title)
        }

        @Test
        @DisplayName("updates the description when provided")
        fun updatesDescription() {
            val todo = service.create(CreateTodoRequest("Title", "Old Desc"))
            val updated = service.update(todo.id, UpdateTodoRequest(description = "New Desc"))
            assertEquals("New Desc", updated!!.description)
        }

        @Test
        @DisplayName("marks a todo as completed")
        fun marksTodoAsCompleted() {
            val todo = service.create(CreateTodoRequest("Task"))
            assertFalse(todo.completed)
            val updated = service.update(todo.id, UpdateTodoRequest(completed = true))
            assertTrue(updated!!.completed)
        }

        @Test
        @DisplayName("marks a completed todo as pending")
        fun marksTodoAsPending() {
            val todo = service.create(CreateTodoRequest("Task"))
            service.update(todo.id, UpdateTodoRequest(completed = true))
            val updated = service.update(todo.id, UpdateTodoRequest(completed = false))
            assertFalse(updated!!.completed)
        }

        @Test
        @DisplayName("does not overwrite fields that are not provided")
        fun doesNotOverwriteOmittedFields() {
            val todo = service.create(CreateTodoRequest("Original Title", "Original Desc"))
            val updated = service.update(todo.id, UpdateTodoRequest(completed = true))
            assertEquals("Original Title", updated!!.title)
            assertEquals("Original Desc", updated.description)
        }

        @Test
        @DisplayName("updates the updatedAt timestamp after modification")
        fun updatesTimestamp() {
            val todo = service.create(CreateTodoRequest("Task"))
            val before = todo.updatedAt
            Thread.sleep(5)
            val updated = service.update(todo.id, UpdateTodoRequest(title = "Changed"))
            assertTrue(updated!!.updatedAt >= before)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // delete
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("delete()")
    inner class Delete {

        @Test
        @DisplayName("returns true and removes the todo when it exists")
        fun returnsTrueAndRemovesExistingTodo() {
            val todo = service.create(CreateTodoRequest("Task"))
            assertTrue(service.delete(todo.id))
            assertNull(service.findById(todo.id))
        }

        @Test
        @DisplayName("returns false for an unknown id")
        fun returnsFalseForUnknownId() {
            assertFalse(service.delete(999L))
        }

        @Test
        @DisplayName("decrements the total count")
        fun decrementsCount() {
            val todo = service.create(CreateTodoRequest("Task"))
            service.create(CreateTodoRequest("Task 2"))
            service.delete(todo.id)
            assertEquals(1, service.findAll().size)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteAll
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("deleteAll()")
    inner class DeleteAll {

        @Test
        @DisplayName("clears the store completely")
        fun clearsStoreCompletely() {
            service.create(CreateTodoRequest("A"))
            service.create(CreateTodoRequest("B"))
            service.create(CreateTodoRequest("C"))
            service.deleteAll()
            assertTrue(service.findAll().isEmpty())
        }

        @Test
        @DisplayName("is idempotent on an already empty store")
        fun isIdempotentOnEmptyStore() {
            assertDoesNotThrow { service.deleteAll() }
            assertTrue(service.findAll().isEmpty())
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // countCompleted
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("countCompleted()")
    inner class CountCompleted {

        @Test
        @DisplayName("returns 0 when store is empty")
        fun returnsZeroWhenEmpty() {
            assertEquals(0L, service.countCompleted())
        }

        @Test
        @DisplayName("returns 0 when no todo is completed")
        fun returnsZeroWhenNoneCompleted() {
            service.create(CreateTodoRequest("A"))
            service.create(CreateTodoRequest("B"))
            assertEquals(0L, service.countCompleted())
        }

        @Test
        @DisplayName("counts only completed todos")
        fun countsOnlyCompleted() {
            val t1 = service.create(CreateTodoRequest("A"))
            service.create(CreateTodoRequest("B"))
            service.update(t1.id, UpdateTodoRequest(completed = true))
            assertEquals(1L, service.countCompleted())
        }

        @Test
        @DisplayName("returns total when all todos are completed")
        fun returnsTotalWhenAllCompleted() {
            val t1 = service.create(CreateTodoRequest("A"))
            val t2 = service.create(CreateTodoRequest("B"))
            service.update(t1.id, UpdateTodoRequest(completed = true))
            service.update(t2.id, UpdateTodoRequest(completed = true))
            assertEquals(2L, service.countCompleted())
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // countPending
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("countPending()")
    inner class CountPending {

        @Test
        @DisplayName("returns 0 when store is empty")
        fun returnsZeroWhenEmpty() {
            assertEquals(0L, service.countPending())
        }

        @Test
        @DisplayName("returns total when none are completed")
        fun returnsTotalWhenNoneCompleted() {
            service.create(CreateTodoRequest("A"))
            service.create(CreateTodoRequest("B"))
            assertEquals(2L, service.countPending())
        }

        @Test
        @DisplayName("counts only incomplete todos")
        fun countsOnlyIncomplete() {
            val t1 = service.create(CreateTodoRequest("A"))
            service.create(CreateTodoRequest("B"))
            service.update(t1.id, UpdateTodoRequest(completed = true))
            assertEquals(1L, service.countPending())
        }

        @Test
        @DisplayName("returns 0 when all todos are completed")
        fun returnsZeroWhenAllCompleted() {
            val t1 = service.create(CreateTodoRequest("A"))
            val t2 = service.create(CreateTodoRequest("B"))
            service.update(t1.id, UpdateTodoRequest(completed = true))
            service.update(t2.id, UpdateTodoRequest(completed = true))
            assertEquals(0L, service.countPending())
        }
    }
}

