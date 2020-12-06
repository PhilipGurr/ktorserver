package com.philipgurr

import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SnippetTest {
    @Test
    fun `test create snippet`() {
        withTestApplication({ module() }) {
            val username = "Mike"
            val password = "abc123"
            val userId = createUser(username, password).userId
            val token = authenticate(username, password)

            createSnippet("First snippet", userId, token)
        }
    }

    @Test
    fun `test get all snippets for user`() {
        withTestApplication({ module() }) {
            val username = "Sarah"
            val password = "abc123"
            val userId = createUser(username, password).userId
            val token = authenticate(username, password)

            createSnippet("Some snippet", userId, token)

            handleRequest(HttpMethod.Get, "/snippets") {
                addHeader("Authorization", "Bearer $token")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertNotNull(response.content)
            }
        }
    }

    @Test
    fun `test create invalid snippet with valid token but for another user`() {
        withTestApplication({ module() }) {
            val username = "Mandy"
            val password = "abc123"
            createUser(username, password).userId
            val token = authenticate(username, password)

            handleRequest(HttpMethod.Post, "/snippets") {
                addHeader("Authorization", "Bearer $token")
                addHeader("Content-Type", "application/json")

                setBody("{\n" +
                        "    \"text\" : \"My first Snippet\",\n" +
                        "    \"userId\": 222\n" +
                        "}")
            }.apply {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }
    }
}

private fun TestApplicationEngine.createSnippet(text: String, userId: String, token: String) {
    handleRequest(HttpMethod.Post, "/snippets") {
        addHeader("Authorization", "Bearer $token")
        addHeader("Content-Type", "application/json")

        setBody("{\n" +
                "    \"text\" : \"$text\",\n" +
                "    \"userId\": $userId\n" +
                "}")
    }.apply {
        assertEquals(HttpStatusCode.OK, response.status())
        assertNotNull(response.content)
        assert(response.content!!.contains(userId))
        assert(response.content!!.contains(text))
    }
}