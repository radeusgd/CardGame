package com.radeusgd.trachonline.board

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.radeusgd.trachonline.util.UuidSerializer
import kotlinx.serialization.Serializable

@Serializable
data class BoardArea(
    @Serializable(with = UuidSerializer::class)
    val uuid: Uuid,
    val entities: List<PlacedEntity>
) {
    companion object Factory {
        fun empty(): BoardArea = BoardArea(uuid4(), listOf())
    }

    fun add(list: List<PlacedEntity>): BoardArea = copy(entities = entities + list)

    fun add(entity: PlacedEntity): BoardArea = add(listOf(entity))

    /** Counts all cards contained within this area, including ones inside of stacks. */
    fun countCards(): Int = entities.map {
        when (it.entity) {
            is Card -> 1
            is CardStack -> it.entity.cards.size
        }
    }.sum()
}
