package com.radeusgd.trachonline.board

import kotlinx.serialization.Serializable

@Serializable
data class GameSnapshot(val mainBoardArea: BoardArea, val privateArea: BoardArea, val players: List<Player>) {
    companion object Factory {
        fun empty(): GameSnapshot = GameSnapshot(BoardArea.empty(), BoardArea.empty(), listOf())
    }
}
