package com.radeusgd.trachonline.messages

import com.benasher44.uuid.Uuid
import com.radeusgd.trachonline.board.EntityDestination
import com.radeusgd.trachonline.util.UuidSerializer
import kotlinx.serialization.Serializable

@Serializable
sealed class ClientMessage

@Serializable
data class SendChatMessage(val text: String) : ClientMessage()

@Serializable
data class SetNickName(val newNickname: String) : ClientMessage()

@Serializable
data class MoveEntity(
    @Serializable(with = UuidSerializer::class)
    val entityUuid: Uuid,
    val destination: EntityDestination
) : ClientMessage()

@Serializable
object Joined : ClientMessage()

@Serializable
object Exited : ClientMessage()
