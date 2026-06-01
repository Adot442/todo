package io.adot442.todo

import io.micronaut.runtime.Micronaut
import io.micronaut.runtime.Micronaut.run

import io.swagger.v3.oas.annotations.*
import io.swagger.v3.oas.annotations.info.*

@OpenAPIDefinition(
    info = Info(
            title = "todo-server",
            version = "0.0"
    )
)
object Api {
}


fun main(args: Array<String>) {
	run(*args)
}
