package com.radeusgd.trachonline.board

import com.benasher44.uuid.Uuid
import com.radeusgd.trachonline.util.UuidSerializer
import kotlinx.serialization.Serializable

@Serializable
sealed class EntityDestination

@Serializable
data class BoardDestination(
    @Serializable(with = UuidSerializer::class)
    val boardId: Uuid,
    val position: Position
) : EntityDestination()

@Serializable
data class StackDestination(
    @Serializable(with = UuidSerializer::class)
    val stackId: Uuid
) : EntityDestination()
