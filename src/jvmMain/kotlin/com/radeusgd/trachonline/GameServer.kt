package com.radeusgd.trachonline

import com.benasher44.uuid.Uuid
import com.radeusgd.trachonline.board.GameSnapshot
import com.radeusgd.trachonline.board.Player
import com.radeusgd.trachonline.gamearea.GameArea
import com.radeusgd.trachonline.gamedefinition.GameDefinition
import com.radeusgd.trachonline.messages.ChatMessage
import com.radeusgd.trachonline.messages.ClientMessage
import com.radeusgd.trachonline.messages.Exited
import com.radeusgd.trachonline.messages.Joined
import com.radeusgd.trachonline.messages.LogMessage
import com.radeusgd.trachonline.messages.MoveEntity
import com.radeusgd.trachonline.messages.SendChatMessage
import com.radeusgd.trachonline.messages.SetNickName
import com.radeusgd.trachonline.messages.UpdateGameState
import java.util.concurrent.atomic.AtomicInteger

data class GameClient(var nickName: String)

class GameServer(gameDefinition: GameDefinition) : Server<Unit>() {

    override fun handleMessage(client: Client, message: ClientMessage) {
        when (message) {
            is SendChatMessage -> broadcast(ChatMessage(client.nickName(), message.text))
            is SetNickName -> setNickName(client, message)
            is MoveEntity -> moveEntity(client, message)
            Joined -> playerJoined(client)
            Exited -> playerExited(client)
        }
    }

    var area: GameArea = GameArea(gameDefinition.prepareMainBoard(), listOf())

    fun takeSnapshot(playerId: Uuid): GameSnapshot {
        val currentArea = area
        val currentPlayerAreas = currentArea.playerAreas.find { it.playerId == playerId }
            ?: throw IllegalStateException("Player is not part of this GameArea")
        return GameSnapshot(
            mainArea = currentArea.mainArea,
            privateArea = currentPlayerAreas.privateArea,
            players = currentArea.playerAreas.map { areas ->
                Player(
                    name = getPlayerData(areas.playerId)?.nickName!!,
                    personalArea = areas.personalArea,
                    privateAreaCount = areas.privateArea.countCards()
                )
            }
        )
    }

    private fun moveEntity(client: Client, message: MoveEntity) {
        TODO("moveentity")
        broadcastGameUpdates()
    }

    private fun broadcastGameUpdates() {
        connectedClients().forEach {
            val snapshot = takeSnapshot(it.uuid())
            it.sendMessage(UpdateGameState(snapshot))
        }
    }

    private fun setNickName(client: Client, message: SetNickName) {
        val data = getPlayerData(client)
        val oldNickName = data.nickName
        data.nickName = message.newNickname
        log("Player $oldNickName is now called ${message.newNickname}.")
    }

    private val players = HashMap<Uuid, GameClient>()

    private fun playerJoined(client: Client) {
        players.put(client.uuid(), GameClient(freshTemporaryNickname()))
        log("Player ${client.nickName()} has joined.")
        area = area.addPlayer(client.uuid())
        broadcastGameUpdates()
    }

    private fun playerExited(client: Client) {
        log("Player ${client.nickName()} has joined.")
        // TODO we may want to clean cards of that player
    }

    override fun initializeClientData(client: Client): Unit = Unit

    val atomicNickNameCounter = AtomicInteger(1)
    private fun freshTemporaryNickname(): String {
        val counter = atomicNickNameCounter.getAndIncrement()
        return "Guest $counter"
    }

    fun getPlayerData(uuid: Uuid): GameClient? = players.get(uuid)
    fun getPlayerData(client: Client): GameClient =
        getPlayerData(client.uuid()) ?: throw IllegalStateException("Missing data for client $client")

    fun Client.nickName(): String = getPlayerData(this).nickName

    fun log(message: String) {
        System.out.println(message)
        broadcast(LogMessage(message))
    }
}
