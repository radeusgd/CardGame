import com.radeusgd.trachonline.messages.ClientMessage
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.WebSocket
import react.dom.render

val socketUrl = "ws://" + window.location.host + "/socket"
val socket: WebSocket = WebSocket(socketUrl)

fun sendMessage(message: ClientMessage) {
    socket.send(Json.encodeToString(message))
}

fun main() {
    window.onload = {
        document.cookie
        render(document.getElementById("root")) {
            child(Game::class) {
                attrs {
//                    name = "Kotlin/JS"
                }
            }
        }
    }
}
