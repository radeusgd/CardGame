package com.radeusgd.trachonline.board

import kotlinx.serialization.Serializable

@Serializable
data class BoardArea(val cards: List<PlacedCard>) {
    companion object Factory {
        fun empty(): BoardArea = BoardArea(listOf())
    }
}
