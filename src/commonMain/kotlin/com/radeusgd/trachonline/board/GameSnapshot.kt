package com.radeusgd.trachonline.board

import kotlinx.serialization.Serializable

@Serializable
data class GameSnapshot(val mainArea: BoardArea, val privateArea: BoardArea, val players: List<Player>) {
    companion object Factory {
        fun emptyPending(): GameSnapshot = GameSnapshot(BoardArea.empty(), BoardArea.empty(), listOf())
    }
}
