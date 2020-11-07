package com.radeusgd.trachonline

import com.radeusgd.trachonline.gamedefinition.GameDefinition
import com.radeusgd.trachonline.messages.ClientMessage
import com.radeusgd.trachonline.messages.ServerMessage
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.consumeEach
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.script
import kotlinx.html.title
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

fun HTML.index() {
    head {
        title("Hello from Trach Online!")
    }
    body {
        div {
            id = "root"
        }
        script(src = "/static/output.js") {}
    }
}

fun main() {
    val definition =
        GameDefinition.loadResource("trach.json") ?: throw IllegalStateException("Could not find game description")
    println(definition)
    val server = GameServer(definition)
    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        install(WebSockets)
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

                    override fun sendMessage(message: ServerMessage) {
                        async { socket.send(Frame.Text(Json.encodeToString(message))) }
                    }
                }
                server.onJoined(client)

                try {
                    incoming.consumeEach { frame ->
                        if (frame is Frame.Text) {
                            val message = Json.decodeFromString<ClientMessage>(frame.readText())
                            server.onMessage(client, message)
                        } else {
                            System.err.println("Unexpected message $frame")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    server.onExited(client)
                }
            }
        }
    }.start(wait = true)
}
