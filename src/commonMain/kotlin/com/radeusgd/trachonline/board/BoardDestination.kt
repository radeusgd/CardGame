package com.radeusgd.trachonline.board

import com.benasher44.uuid.Uuid
import com.radeusgd.trachonline.util.UuidSerializer
import kotlinx.serialization.Serializable

@Serializable
data class BoardDestination(
    @Serializable(with = UuidSerializer::class)
    val boardId: Uuid,
    val position: Position
)
