package com.philipgurr

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import com.fasterxml.jackson.databind.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.jackson.*
import io.ktor.features.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    install(CallLogging)
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    val jwt = SimpleJWT(secret = "SomeSecret321")
    install(Authentication) {
        jwt {
            verifier(jwt.verifier)
            validate {
                UserIdPrincipal(it.payload.getClaim("id").asString())
            }
        }
    }

    val users = mutableListOf<UsernamePassword>()

    routing {
        authenticate {
            get("/") {
                val principal = call.principal<UserIdPrincipal>() ?: error("No Principal!")
                call.respondText("HELLO ${principal.name}!", contentType = ContentType.Text.Plain)
            }
        }

        auth(jwt, users)
    }
}

fun Route.auth(jwt: SimpleJWT, users: MutableList<UsernamePassword>) {
    post("/authenticate") {
        val post = call.receive<UsernamePassword>()
        val user = if(users.contains(post)) post else {
            users.add(post)
            post
        }
        call.respond(mapOf("token" to jwt.sign(user.username)))
    }
}

data class UsernamePassword(
    val id: Int = 0,
    val username: String,
    val password: String
)

open class SimpleJWT(val secret: String) {
    private val algorithm = Algorithm.HMAC256(secret)
    val verifier: JWTVerifier = JWT.require(algorithm).build()
    fun sign(name: String): String = JWT.create().withClaim("id", name).sign(algorithm)
}

