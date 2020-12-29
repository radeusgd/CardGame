package com.radeusgd.trachonline

import com.radeusgd.trachonline.messages.ServerMessage
import java.util.UUID

interface Client {
    fun uuid(): UUID
    fun sendMessage(message: ServerMessage)
    fun disconnect(): Unit
}
