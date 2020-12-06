package com.philipgurr

import io.ktor.http.*
import io.ktor.server.testing.*
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal val String.userId: String
    get() {
        StringReader(this).use {
            return it.readLines()[1]
                .replace("{", "")
                .replace("}", "")
                .replace("\"", "")
                .replace("userId : ", "")
                .replace(",", "")
                .trim()
        }
    }

internal fun TestApplicationEngine.createUser(username: String, password: String): String {
    handleRequest(HttpMethod.Post, "/users") {
        addHeader("Content-Type", "application/json")
        addHeader("Accept", "application/json")

        setBody("{\n" +
                "    \"username\": \"$username\",\n" +
                "    \"password\": \"$password\"\n" +
                "}")
    }.apply {
        assertEquals(HttpStatusCode.OK, response.status())
        assertNotNull(response.content)
        assert(response.content!!.contains(username))

        return response.content!!
    }
}

internal fun TestApplicationEngine.authenticate(username: String, password: String): String {
    handleRequest(HttpMethod.Post, "/authenticate") {
        addHeader("Content-Type", "application/json")
        addHeader("Accept", "application/json")

        setBody("{\n" +
                "    \"username\": \"$username\",\n" +
                "    \"password\": \"$password\"\n" +
                "}")
    }.apply {
        assertEquals(HttpStatusCode.OK, response.status())
        assertNotNull(response.content)
        assert(response.content!!.contains("token"))

        // Strip token from json
        return response.content!!.replace("{", "")
            .replace("}", "")
            .replace("\"", "")
            .replace("token :", "")
            .trim()

    }
}