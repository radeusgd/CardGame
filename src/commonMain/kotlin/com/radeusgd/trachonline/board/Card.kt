package com.radeusgd.trachonline.board

import com.benasher44.uuid.Uuid
import kotlinx.serialization.Serializable
import com.radeusgd.trachonline.util.UuidSerializer

@Serializable
data class Position(val x: Int, val y: Int)

@Serializable
data class CardVisuals(val frontImage: String, val backImage: String)

@Serializable
data class Card(
    @Serializable(with = UuidSerializer::class)
    val uuid: Uuid,
    val visuals: CardVisuals,
    val isShowingFront: Boolean
)

@Serializable
data class PlacedCard(val card: Card, val position: Position)
