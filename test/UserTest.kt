package com.philipgurr

import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UserTest {
    @Test
    fun `test create user`() {
        withTestApplication({ module() }) {
            val username = "Frank"
            val password = "abc123"
            createUser(username, password)
        }
    }

    @Test
    fun `test login user`() {
        withTestApplication({ module() }) {
            val username = "Roland"
            val password = "abc123"
            createUser(username, password)
            authenticate(username, password)
        }
    }

    @Test
    fun `test get user`() {
        withTestApplication({ module() }) {
            val username = "Ralf"
            val password = "abc123"
            val userId = createUser(username, password).userId
            val token = authenticate(username, password)

            handleRequest(HttpMethod.Get, "/users/$userId") {
                addHeader("Accept", "application/json")
                addHeader("Authorization", "Bearer $token")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertNotNull(response.content)
                assert(response.content!!.contains(username))
            }
        }
    }
}