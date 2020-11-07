import react.dom.render
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import messages.Message
import messages.SetNickName
import org.w3c.dom.WebSocket

val socketUrl = "ws://" + window.location.host + "/socket"
val socket: WebSocket = WebSocket(socketUrl)

fun sendMessage(message: Message) {
    socket.send(Json.encodeToString(message))
}

fun main() {
    window.onload = {
//        sendMessage(SetNickName("Osiem123"))
        render(document.getElementById("root")) {
            child(Welcome::class) {
                attrs {
                    name = "Kotlin/JS"
                }
            }
        }
    }
}
