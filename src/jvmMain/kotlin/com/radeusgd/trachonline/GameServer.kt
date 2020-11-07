package com.radeusgd.trachonline

import messages.*
import java.util.concurrent.atomic.AtomicInteger

data class GameClient(var nickName: String)

class GameServer : Server<GameClient>() {
    override fun onMessage(client: Client, message: Message) {
        when(message) {
            is Error -> unexpectedMessage(message)
            is LogMessage -> unexpectedMessage(message)
            is ChatMessage -> broadcast(message)
            is UpdateTable -> unexpectedMessage(message)
            is SetNickName -> setNickName(client, message)
            Joined -> playerJoined(client)
            Exited -> playerExited(client)
        }
    }

    private fun unexpectedMessage(message: Message) {
        System.err.println("Unexpected message $message")
    }

    private fun setNickName(client: Client, message: SetNickName) {
        val data = getClientData(client)
        val oldNickName = data.nickName
        data.nickName = message.newNickname
        log("Player $oldNickName is now called ${message.newNickname}.")
    }

    private fun playerJoined(client: Client) {
        log("Player ${client.nickName()} has joined.")
    }

    private fun playerExited(client: Client) {
        log("Player ${client.nickName()} has joined.")
    }

    override fun initializeClientData(client: Client): GameClient = GameClient(freshTemporaryNickname())

    val atomicNickNameCounter = AtomicInteger(1)
    private fun freshTemporaryNickname(): String {
        val counter = atomicNickNameCounter.getAndIncrement()
        return "Guest $counter"
    }

    fun Client.nickName(): String = getClientData(this).nickName

    fun log(message: String) {
        broadcast(LogMessage(message))
    }
}
