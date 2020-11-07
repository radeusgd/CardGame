package com.radeusgd.trachonline

import messages.Message
import java.util.*

interface Client {
    fun uuid(): UUID
    fun sendMessage(message: Message)
}
