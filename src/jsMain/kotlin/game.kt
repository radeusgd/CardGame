import com.radeusgd.trachonline.board.GameSnapshot
import com.radeusgd.trachonline.messages.ChatMessage
import com.radeusgd.trachonline.messages.Error
import com.radeusgd.trachonline.messages.LogMessage
import com.radeusgd.trachonline.messages.ServerMessage
import com.radeusgd.trachonline.messages.UpdateGameState
import kotlinx.css.script
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.w3c.dom.MessageEvent
import react.RBuilder
import react.RComponent
import react.RProps
import kotlinx.browser.document
import react.RState
import styled.css
import styled.styledDiv


external interface ViewProps : RProps

data class ViewState(
    val messages: List<String>,
    val gameSnapshot: GameSnapshot
) : RState

@JsExport
class Game(props: ViewProps) : RComponent<ViewProps, ViewState>(props) {

    init {
        state = ViewState(messages = listOf(), gameSnapshot = GameSnapshot.empty())
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

    private fun handleMessage(message: ServerMessage): Unit = when (message) {
        is Error -> addChatMessage("Error: ${message.errorText}")
        is LogMessage -> addChatMessage(message.text) // TODO this would be nice with cursive, need to encode basic formatting in state
        is ChatMessage -> addChatMessage("[${message.nickname}]: ${message.text}")
        is UpdateGameState -> updateTable(message.gameState)
    }

    private fun updateTable(newGameSnapshot: GameSnapshot) {
        console.log(newGameSnapshot)
        setState(ViewState(messages = state.messages, gameSnapshot = newGameSnapshot))
    }

    private fun addChatMessage(message: String) {
        console.log(message)
        setState(ViewState(messages = state.messages + message, gameSnapshot = state.gameSnapshot))
        scrollChatToBottom()
    }

    private fun scrollChatToBottom() : Unit = document.getElementById(CHAT_ELEMENT_ID)?.run {
        scrollIntoView(false)
    }?: console.error("Failed to scroll")

    override fun RBuilder.render() {
        styledDiv {
            css {
                +GameStyles.mainStyle
            }

            child(Gameboard::class) {
                attrs {
                    gameSnapshot = state.gameSnapshot
                }
            }

            child(Chat::class) {
                attrs {
                    messages = state.messages
                }
            }
        }
    }

    companion object {
        const val CHAT_ELEMENT_ID = "chat"
    }
}
