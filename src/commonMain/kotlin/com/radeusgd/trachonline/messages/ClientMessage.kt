package com.radeusgd.trachonline.messages

import com.benasher44.uuid.Uuid
import com.radeusgd.trachonline.board.BoardDestination
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
    val destination: BoardDestination
) : ClientMessage()

@Serializable
data class PickStack(
    @Serializable(with = UuidSerializer::class)
    val stackUuid: Uuid
) : ClientMessage()

@Serializable
data class ShuffleStack(
    @Serializable(with = UuidSerializer::class)
    val stackUuid: Uuid
) : ClientMessage()

@Serializable
data class PutOnStack(
    @Serializable(with = UuidSerializer::class)
    val stackUuid: Uuid,
    @Serializable(with = UuidSerializer::class)
    val cardUuid: Uuid
) : ClientMessage()

@Serializable
data class FlipCard(
    @Serializable(with = UuidSerializer::class)
    val cardUuid: Uuid
) : ClientMessage()

@Serializable
data class MakeStack(
    @Serializable(with = UuidSerializer::class)
    val cardUuid: Uuid
) : ClientMessage()

@Serializable
object Joined : ClientMessage()

@Serializable
object Exited : ClientMessage()
