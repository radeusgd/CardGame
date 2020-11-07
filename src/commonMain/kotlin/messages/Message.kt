package messages

import kotlinx.serialization.Serializable

@Serializable
sealed class Message

data class Error(val message: String) : Message()
data class LogMessage(val message: String) : Message()
data class ChatMessage(val nickname: String, val message: String) : Message()

data class UpdateTable(val tableState: Any) : Message()
data class SetNickName(val newNickname: String) : Message()

object Joined : Message()
object Exited : Message()

