package com.philipgurr

import io.ktor.http.cio.websocket.*
import io.ktor.server.testing.*
import org.junit.Test

class LiveWebSocketTest {
    @Test
    fun `test live editing`() {
        withTestApplication({ module() }) {
            val testText = "Hello One"
            val testText2 = "Hello Two"

            handleWebSocketConversation("/live") { incoming, outgoing ->
                outgoing.send(Frame.Text(testText))
                incoming.receive()
                outgoing.send(Frame.Text(testText2))

                val response = (incoming.receive() as Frame.Text).readText()
                assert(response.contains(testText))
                assert(response.contains(testText2))
            }
        }
    }
}