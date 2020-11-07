package com.radeusgd.trachonline.board

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.radeusgd.trachonline.util.UuidSerializer
import kotlinx.serialization.Serializable

@Serializable
data class Position(val x: Float, val y: Float, val depth: Int)

@Serializable
data class CardVisuals(val frontImage: String, val backImage: String)

@Serializable
sealed class BoardEntity {
    abstract val uuid: Uuid
}

@Serializable
data class Card(
    @Serializable(with = UuidSerializer::class)
    override val uuid: Uuid,
    val visuals: CardVisuals,
    val isShowingFront: Boolean
) : BoardEntity() {
    fun getCurrentImage(): String = if (isShowingFront) visuals.frontImage else visuals.backImage
}

@Serializable
data class CardStack(
    @Serializable(with = UuidSerializer::class)
    override val uuid: Uuid,
    val cards: List<Card>
) : BoardEntity() {
    companion object {
        fun make(cards: List<Card>): CardStack = CardStack(uuid4(), cards)
    }
}

@Serializable
data class PlacedEntity(val entity: BoardEntity, val position: Position)
