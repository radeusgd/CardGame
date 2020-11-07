package messages

import kotlinx.serialization.Serializable

@Serializable
sealed class Message

@Serializable
data class Error(val message: String) : Message()

@Serializable
data class LogMessage(val message: String) : Message()

@Serializable
data class ChatMessage(val nickname: String, val message: String) : Message()

@Serializable
data class UpdateTable(val tableState: Int) : Message()

@Serializable
data class SetNickName(val newNickname: String) : Message()

@Serializable
object Joined : Message()

@Serializable
object Exited : Message()

