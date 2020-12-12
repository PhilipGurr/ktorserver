package com.philipgurr

import com.philipgurr.auth.SimpleJWT
import com.philipgurr.database.SnippetRepository
import com.philipgurr.database.UserRepository
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import java.io.File
import javax.naming.AuthenticationException

fun Route.routeUser(jwt: SimpleJWT, userRepository: UserRepository) {
    post("/authenticate") {
        val post = call.receive<UsernamePassword>()
        val user = userRepository.getByUsername(post.username) ?: throw BadRequestException("User not found")
        if (user.password == post.password) {
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
                if (snippet.userId != userId) throw ForbiddenException()
                val new = snippetRepository.add(snippet.copy(userId = userId))
                    ?: throw BadRequestException("Could not add snippet")
                call.respond(new)
            }
        }
    }
}

fun Route.routeLiveSnippet() {
    webSocket("/live") {
        val textFile = tempFile()
        while (true) {
            when (val frame = incoming.receive()) {
                is Frame.Text -> {
                    val text = frame.readText()
                    textFile.appendText(text)
                    outgoing.send(Frame.Text(textFile.readText()))
                }
            }
        }
    }
}

private fun tempFile(text: String = ""): File {
    val file = File.createTempFile("live", ".txt")
    file.deleteOnExit()
    file.appendText(text)
    return file
}