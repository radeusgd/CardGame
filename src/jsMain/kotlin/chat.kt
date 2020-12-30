import Game.Companion.CHAT_ELEMENT_ID
import com.radeusgd.trachonline.messages.SendChatMessage
import com.radeusgd.trachonline.messages.SetNickName
import kotlinx.browser.window
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onKeyDownFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.get
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.p
import styled.css
import styled.styledDiv
import styled.styledInput

data class ChatProps(
    var messages: List<String>
) : RProps

data class ChatState(
    val text: String
) : RState

@JsExport
class Chat(props: ChatProps) : RComponent<ChatProps, ChatState>(props) {

    init {
        state = ChatState(text = "")
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

            styledDiv {
                attrs {
                    id = CHAT_ELEMENT_ID
                }

                css {
                    +GameStyles.chatActions
                }

                styledInput {
                    css {
                        +GameStyles.input
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
                        +GameStyles.input
                    }
                    attrs {
                        type = InputType.button
                        value = "Send"
                        onClickFunction = ::sendChatMessage
                    }
                }

                styledInput {
                    css {
                        +GameStyles.input
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
    }

    private fun sendChatMessage(event: Event) {
        sendMessage(SendChatMessage(text = state.text))
        setState(ChatState(text = ""))
    }
}
