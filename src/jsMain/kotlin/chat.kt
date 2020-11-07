import kotlinx.html.InputType
import kotlinx.html.js.*
import com.radeusgd.trachonline.messages.SendChatMessage
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import styled.css
import styled.styledInput
import styled.styledDiv
import react.dom.p

data class ChatProps(
    var messages: List<String>
) : RProps

data class ChatState(
    val text: String
) : RState

@JsExport
class Chat(props: ChatProps) : RComponent<ChatProps, ChatState>(props) {

    init {
        setState(ChatState(text = ""))
    }

    override fun RBuilder.render() {

        styledDiv {
            css {
                +GameStyles.chatContainer
            }
            props.messages.forEach { message ->
                p {
                    +message
                }
            }
        }

        styledInput {
            css {
                +GameStyles.textInput
            }
            attrs {
                type = InputType.text
                value = state.text
                onChangeFunction = { event ->
                    setState(ChatState(text = (event.target as HTMLInputElement).value))
                }

                onKeyDownFunction = { event ->
                    val code = (event.asDynamic().nativeEvent as? KeyboardEvent)?.keyCode
                    if (code == 13) {
                        sendChatMessage(event)
                    }
                }
            }
        }

        styledInput {
            css {
                +GameStyles.buttonInput
            }
            attrs {
                type = InputType.button
                value = "Send"
                onClickFunction = ::sendChatMessage
            }
        }
    }

    private fun sendChatMessage(event: Event) {
        sendMessage(SendChatMessage(text = state.text))
        setState(ChatState(text = ""))
    }
}
