package com.radeusgd.trachonline.messages

import com.radeusgd.trachonline.board.GameSnapshot
import kotlinx.serialization.Serializable

@Serializable
sealed class ServerMessage

@Serializable
data class Error(val errorText: String) : ServerMessage()

@Serializable
data class LogMessage(val text: String) : ServerMessage()

@Serializable
data class ChatMessage(val nickname: String, val text: String) : ServerMessage()

@Serializable
data class UpdateGameState(val gameState: GameSnapshot) : ServerMessage()
