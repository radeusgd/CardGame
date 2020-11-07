package com.radeusgd.trachonline

import messages.Message
import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.*
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.websocket.*
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.consumeEach
import kotlinx.html.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

fun HTML.index() {
    head {
        title("Hello from Ktor!")
    }
    body {
        div {
            +"Hello from Ktor"
        }
        div {
            id = "root"
        }
        script(src = "/static/output.js") {}
    }
}

fun main() {
    val server = GameServer()
    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        routing {
            get("/") {
                call.respondHtml(HttpStatusCode.OK, HTML::index)
            }
            static("/static") {
                resources()
            }
            webSocket("/socket") {
                val uuid = UUID.randomUUID()
                val socket = this
                val client = object : Client {
                    override fun uuid() = uuid

                    override fun sendMessage(message: Message) {
                        async { socket.send(Frame.Text(Json.encodeToString(message))) }
                    }
                }
                server.onJoined(client)

                try {
                    incoming.consumeEach { frame ->
                        if (frame is Frame.Text) {
                            val message = Json.decodeFromString<Message>(frame.readText())
                            server.onMessage(client, message)
                        } else {
                            System.err.println("Unexpected message $frame")
                        }
                    }
                } catch(e: Exception) {
                    e.printStackTrace()
                } finally {
                    server.onExited(client)
                }

            }
        }
    }.start(wait = true)
}
