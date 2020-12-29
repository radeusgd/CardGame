package com.radeusgd.trachonline

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.radeusgd.trachonline.gamedefinition.GameDefinition
import com.radeusgd.trachonline.messages.ClientMessage
import com.radeusgd.trachonline.messages.ServerMessage
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import java.util.UUID
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.consumeEach
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.script
import kotlinx.html.title
import kotlinx.html.unsafe
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun index(): (HTML.() -> Unit) {
    return {
        head {
            title("Trach Online")
        }
        body {
            div {
                id = "root"
            }
            script(src = "/static/output.js") {}
        }
    }
}

fun cookieQuestion(): (HTML.() -> Unit) {
    return {
        head {
            title("Trach Online")
        }
        body {
            div {
                +"Set your cookie preferences"
            }
            val cookieMessage =
                "Cookies are required to persist player id. Do you consent to store a randomly generated id that is only used for restoring your session in case of refreshing the page/network failure?"

            unsafe {
                +"<script>if (confirm(\"$cookieMessage\")) { window.location.href = \"/cookie-ok\"; } else { window.location.href = \"/cookie-denied\"; }</script>"
            }
        }
    }
}

data class PlayerSession(val playerId: Uuid) {
    companion object Factory {
        fun makeRandom(): PlayerSession {
            return PlayerSession(uuid4())
        }
    }
}

fun main() {
    val definition =
        GameDefinition.loadResource("trach.json") ?: throw IllegalStateException("Could not find game description")
    println(definition)
    val server = GameServer(definition)
    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        install(WebSockets)
        install(Sessions) {
            cookie<PlayerSession>("PLAYER_SESSION")
        }
        routing {
            get("/cookie-ok") {
                val session = call.sessions.get<PlayerSession>() ?: PlayerSession.makeRandom()
                call.sessions.set<PlayerSession>(session)
                call.respondHtml(HttpStatusCode.OK, index())
            }
            get("/cookie-denied") {
                call.respondHtml(HttpStatusCode.OK, index())
            }
            get("/") {
                val session = call.sessions.get<PlayerSession>()
                if (session == null) {
                    call.respondHtml(HttpStatusCode.OK, cookieQuestion())
                } else {
                    call.respondHtml(HttpStatusCode.OK, index())
                }
            }
            static("/static") {
                resources()
            }
            webSocket("/socket") {
                val sessionId = call.sessions.get<PlayerSession>()?.playerId
                val uuid = if (sessionId != null) {
                    System.err.println("Player with id $sessionId joined.")
                    sessionId
                } else {
                    val randomId = UUID.randomUUID()
                    System.err.println("Player with random-id $randomId joined.")
                    randomId
                }
                val socket = this
                var isClosed = false
                val client = object : Client {
                    override fun uuid() = uuid

                    override fun sendMessage(message: ServerMessage) {
                        async { socket.send(Frame.Text(Json.encodeToString(message))) }
                    }

                    override fun disconnect() {
                        isClosed = true
                        async { socket.close() }
                    }
                }
                server.onJoined(client)

                try {
                    incoming.consumeEach { frame ->
                        if (isClosed) return@consumeEach
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
