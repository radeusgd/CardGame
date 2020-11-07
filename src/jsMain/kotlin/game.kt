import com.radeusgd.trachonline.messages.*
import kotlinx.browser.window
import kotlinx.html.InputType
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.w3c.dom.MessageEvent
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import styled.css
import styled.styledDiv
import styled.styledInput

external interface ViewProps : RProps

data class ViewState(
    val messages: List<String>) : RState

@JsExport
class Game(props: ViewProps) : RComponent<ViewProps, ViewState>(props) {

    init {
        state = ViewState(messages = listOf())
        socket.onmessage = { event: MessageEvent ->
            val data = event.data
            if (data is String) {
                val deserialized = Json.decodeFromString<ServerMessage>(data)
                handleMessage(deserialized)
            } else {
                console.error("Unknown websocket message: ${event.data}")
            }
        }
    }

    private fun handleMessage(message: ServerMessage): Unit = when(message) {
        is Error -> addChatMessage("Error: ${message.errorText}")
        is LogMessage -> addChatMessage(message.text) // TODO this would be nice with cursive, need to encode basic formatting in state
        is ChatMessage -> addChatMessage("[${message.nickname}]: ${message.text}")
        is UpdateTable -> TODO()
    }

    private fun addChatMessage(message: String) {
        console.log(message)
        setState(ViewState(messages = state.messages + message))
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                +GameStyles.textContainer
            }
            +"Hello, world"
        }

        child(Chat::class) {
            attrs {
                messages = state.messages
            }
        }

        styledInput {
            css {
                +GameStyles.buttonInput
            }
            attrs {
                type = InputType.button
                value = "Set nickname"
                onClickFunction = { event ->
                    window.prompt("Set your nickname")?.let {
                        sendMessage(SetNickName(it))
                    }
                }
            }
        }
    }
}
