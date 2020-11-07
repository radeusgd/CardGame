package com.radeusgd.trachonline.board

import kotlinx.serialization.Serializable

@Serializable
data class Player(val name: String, val personalArea: BoardArea)
