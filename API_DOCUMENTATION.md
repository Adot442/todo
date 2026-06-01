# Todo API Documentation

## Overview

This is an in-memory Todo List REST API built with Micronaut Framework and Kotlin. The API provides full CRUD operations for managing todo items.

## Features

- ✅ Create, Read, Update, and Delete todo items
- ✅ In-memory storage (no database required)
- ✅ Thread-safe concurrent operations
- ✅ RESTful API design
- ✅ OpenAPI/Swagger annotations
- ✅ JSON serialization with Micronaut Serde
- ✅ Statistics endpoint

## Running the Application

### Build the project:
```bash
./gradlew clean build -x test
```

### Run the application:
```bash
java -jar build/libs/todo-server-0.1-all.jar
```

The server will start on `http://localhost:8080`

## API Endpoints

### 1. Get All Todos
**GET** `/api/todos`

Returns a list of all todos.

**Example:**
```bash
curl http://localhost:8080/api/todos
```

**Response:**
```json
[
  {
    "id": 1,
    "title": "Buy groceries",
    "description": "Milk and eggs",
    "completed": false,
    "createdAt": "2026-06-01T00:28:01.262841",
    "updatedAt": "2026-06-01T00:28:01.262877"
  }
]
```

### 2. Get Todo by ID
**GET** `/api/todos/{id}`

Returns a specific todo by ID.

**Example:**
```bash
curl http://localhost:8080/api/todos/1
```

**Response:**
```json
{
  "id": 1,
  "title": "Buy groceries",
  "description": "Milk and eggs",
  "completed": false,
  "createdAt": "2026-06-01T00:28:01.262841",
  "updatedAt": "2026-06-01T00:28:01.262877"
}
```

**Status Codes:**
- `200 OK` - Todo found
- `404 Not Found` - Todo not found

### 3. Create Todo
**POST** `/api/todos`

Creates a new todo item.

**Request Body:**
```json
{
  "title": "Buy groceries",
  "description": "Milk and eggs"
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/todos \
  -H "Content-Type: application/json" \
  -d '{"title":"Buy groceries","description":"Milk and eggs"}'
```

**Response:** (Status: 201 Created)
```json
{
  "id": 1,
  "title": "Buy groceries",
  "description": "Milk and eggs",
  "completed": false,
  "createdAt": "2026-06-01T00:28:01.262841",
  "updatedAt": "2026-06-01T00:28:01.262877"
}
```

### 4. Update Todo
**PUT** `/api/todos/{id}`

Updates an existing todo. All fields are optional.

**Request Body:**
```json
{
  "title": "Updated title",
  "description": "Updated description",
  "completed": true
}
```

**Example:**
```bash
curl -X PUT http://localhost:8080/api/todos/1 \
  -H "Content-Type: application/json" \
  -d '{"completed":true}'
```

**Response:**
```json
{
  "id": 1,
  "title": "Buy groceries",
  "description": "Milk and eggs",
  "completed": true,
  "createdAt": "2026-06-01T00:28:01.262841",
  "updatedAt": "2026-06-01T00:28:16.283301"
}
```

**Status Codes:**
- `200 OK` - Todo updated
- `404 Not Found` - Todo not found

### 5. Delete Todo
**DELETE** `/api/todos/{id}`

Deletes a specific todo by ID.

**Example:**
```bash
curl -X DELETE http://localhost:8080/api/todos/1
```

**Status Codes:**
- `204 No Content` - Todo deleted successfully
- `404 Not Found` - Todo not found

### 6. Delete All Todos
**DELETE** `/api/todos`

Deletes all todos from the in-memory store.

**Example:**
```bash
curl -X DELETE http://localhost:8080/api/todos
```

**Response:** `204 No Content`

### 7. Get Statistics
**GET** `/api/todos/stats`

Returns statistics about todos.

**Example:**
```bash
curl http://localhost:8080/api/todos/stats
```

**Response:**
```json
{
  "total": 2,
  "completed": 1,
  "pending": 1
}
```

## Data Models

### Todo
```json
{
  "id": "Long - Auto-generated unique identifier",
  "title": "String - Required - Todo title",
  "description": "String - Optional - Todo description",
  "completed": "Boolean - Default: false - Completion status",
  "createdAt": "LocalDateTime - Auto-generated - Creation timestamp",
  "updatedAt": "LocalDateTime - Auto-generated - Last update timestamp"
}
```

### CreateTodoRequest
```json
{
  "title": "String - Required",
  "description": "String - Optional"
}
```

### UpdateTodoRequest
```json
{
  "title": "String - Optional",
  "description": "String - Optional",
  "completed": "Boolean - Optional"
}
```

## Project Structure

```
src/main/kotlin/io/adot442/todo/
├── Application.kt              # Application entry point
├── controller/
│   └── TodoController.kt       # REST API endpoints
├── service/
│   └── TodoService.kt         # Business logic layer
└── model/
    └── Todo.kt                # Data models
```

## Technical Details

- **Framework**: Micronaut 4.6.2
- **Language**: Kotlin 1.9.25
- **Storage**: ConcurrentHashMap (in-memory, thread-safe)
- **Serialization**: Micronaut Serde with Jackson
- **Documentation**: OpenAPI 3.0 annotations
- **Server**: Netty

## Thread Safety

The API uses `ConcurrentHashMap` and `AtomicLong` for thread-safe operations, making it safe for concurrent access.

## Testing the API

You can use the following commands to test all endpoints:

```bash
# Create todos
curl -X POST http://localhost:8080/api/todos \
  -H "Content-Type: application/json" \
  -d '{"title":"Buy groceries","description":"Milk and eggs"}'

curl -X POST http://localhost:8080/api/todos \
  -H "Content-Type: application/json" \
  -d '{"title":"Write code","description":"Finish API implementation"}'

# Get all todos
curl http://localhost:8080/api/todos

# Get specific todo
curl http://localhost:8080/api/todos/1

# Update todo (mark as completed)
curl -X PUT http://localhost:8080/api/todos/1 \
  -H "Content-Type: application/json" \
  -d '{"completed":true}'

# Get statistics
curl http://localhost:8080/api/todos/stats

# Delete a todo
curl -X DELETE http://localhost:8080/api/todos/1

# Delete all todos
curl -X DELETE http://localhost:8080/api/todos
```

## Notes

- Data is stored in-memory only and will be lost when the application restarts
- IDs are auto-incrementing and start from 1
- All timestamps are in ISO-8601 format
- The API accepts and returns JSON only

