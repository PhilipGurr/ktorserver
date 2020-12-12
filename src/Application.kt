package com.philipgurr

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import com.fasterxml.jackson.databind.*
import com.philipgurr.auth.SimpleJWT
import com.philipgurr.database.*
import com.philipgurr.di.applicationModule
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.jackson.*
import io.ktor.features.*
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import org.koin.logger.slf4jLogger
import java.io.File
import javax.naming.AuthenticationException

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

private val jwt = SimpleJWT(secret = "SomeSecret321")

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    initDatabase()
    installFeatures()

    val userRepo by inject<UserRepository>()
    val snippetRepo by inject<SnippetRepository>()

    routing {
        routeUser(jwt, userRepo)
        routeSnippets(snippetRepo)
        routeLiveSnippet()
    }
}

private fun initDatabase() {
    Database.connect("jdbc:h2:mem:regular;DB_CLOSE_DELAY=-1", "org.h2.Driver")
    transaction {
        SchemaUtils.create(Users, Snippets)
    }
}

private fun Application.installFeatures() {
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
        exception<ForbiddenException> {
            call.respond(HttpStatusCode.Forbidden)
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
    install(Koin) {
        slf4jLogger()
        modules(applicationModule)
    }
    install(WebSockets)
}

class ForbiddenException : Exception()