package com.philipgurr

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import com.fasterxml.jackson.databind.*
import com.philipgurr.auth.SimpleJWT
import com.philipgurr.database.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.jackson.*
import io.ktor.features.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import javax.naming.AuthenticationException

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

private val jwt = SimpleJWT(secret = "SomeSecret321")

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    initDatabase()

    install(CallLogging)
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }
    install(StatusPages) {
        exception<Throwable> {
            call.respond(HttpStatusCode.InternalServerError)
        }
        exception<AuthenticationException> {
            call.respond(HttpStatusCode.Unauthorized)
        }
        exception<BadRequestException> {
            call.respond(HttpStatusCode.BadRequest)
        }
    }
    install(Authentication) {
        jwt {
            verifier(jwt.verifier)
            validate {
                UserIdPrincipal(it.payload.getClaim("id").asString())
            }
        }
    }

    routing {
        routeUser(jwt, ExposedUserRepository())
        routeSnippets(ExposedSnippetRepository())
    }
}

private fun initDatabase() {
    Database.connect("jdbc:h2:mem:regular;DB_CLOSE_DELAY=-1", "org.h2.Driver")
    transaction {
        SchemaUtils.create(Users, Snippets)
    }
}

fun Route.routeUser(jwt: SimpleJWT, userRepository: UserRepository) {
    post("/authenticate") {
        val post = call.receive<UsernamePassword>()
        val user = userRepository.getByUsername(post.username) ?: throw BadRequestException("User not found")
        if(user.password == post.password) {
            call.respond(mapOf("token" to jwt.sign(user.userId.toString())))
        } else {
            throw AuthenticationException()
        }
    }
    route("/users") {
        post {
            val user = call.receive<User>()
            val new = userRepository.add(user)
            call.respond(new)
        }
        authenticate {
            get("/{userId}") {
                val userId = call.parameters["userId"]?.toInt() ?: -1
                call.respond(userRepository.getById(userId) ?: throw BadRequestException("User not found"))
            }
        }
    }
}

fun Route.routeSnippets(snippetRepository: SnippetRepository) {
    authenticate {
        route("/snippets") {
            get {
                val userId = call.principal<UserIdPrincipal>()?.name?.toInt() ?: throw AuthenticationException()
                call.respond(snippetRepository.getAllForUser(userId))
            }
            get("/{snippetId}") {
                val snippetId = call.parameters["snippetId"]?.toInt() ?: -1
                call.respond(snippetRepository.getById(snippetId) ?: throw BadRequestException("Snippet not found"))
            }
            post {
                val userId = call.principal<UserIdPrincipal>()?.name?.toInt() ?: throw AuthenticationException()
                val snippet = call.receive<Snippet>()
                val new = snippetRepository.add(snippet.copy(userId = userId)) ?: throw BadRequestException("Could not add snippet")
                call.respond(new)
            }
        }
    }
}