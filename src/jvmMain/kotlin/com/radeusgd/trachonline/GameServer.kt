package com.radeusgd.trachonline

import com.radeusgd.trachonline.messages.*
import java.util.concurrent.atomic.AtomicInteger

data class GameClient(var nickName: String)

class GameServer : Server<GameClient>() {

    override fun onMessage(client: Client, message: ClientMessage) {
        when(message) {
            is SendChatMessage -> broadcast(ChatMessage(client.nickName(), message.text))
            is SetNickName -> setNickName(client, message)
            Joined -> playerJoined(client)
            Exited -> playerExited(client)
        }
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
        // TODO we may want to clean cards of that player
    }

    override fun initializeClientData(client: Client): GameClient = GameClient(freshTemporaryNickname())

    val atomicNickNameCounter = AtomicInteger(1)
    private fun freshTemporaryNickname(): String {
        val counter = atomicNickNameCounter.getAndIncrement()
        return "Guest $counter"
    }

    fun Client.nickName(): String = getClientData(this).nickName

    fun log(message: String) {
        System.out.println(message)
        broadcast(LogMessage(message))
    }
}
