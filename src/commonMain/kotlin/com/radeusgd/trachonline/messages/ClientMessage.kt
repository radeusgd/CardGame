package com.radeusgd.trachonline.messages

import kotlinx.serialization.Serializable

@Serializable
sealed class ClientMessage

@Serializable
data class SendChatMessage(val text: String) : ClientMessage()

@Serializable
data class SetNickName(val newNickname: String) : ClientMessage()

@Serializable
object Joined : ClientMessage()

@Serializable
object Exited : ClientMessage()
