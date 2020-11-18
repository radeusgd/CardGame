package com.radeusgd.trachonline

import com.benasher44.uuid.Uuid
import com.radeusgd.trachonline.board.BoardDestination
import com.radeusgd.trachonline.board.Card
import com.radeusgd.trachonline.board.CardStack
import com.radeusgd.trachonline.board.GameSnapshot
import com.radeusgd.trachonline.board.Player
import com.radeusgd.trachonline.gamearea.AreaLocationDescription
import com.radeusgd.trachonline.gamearea.GameArea
import com.radeusgd.trachonline.gamedefinition.Deck
import com.radeusgd.trachonline.gamedefinition.GameDefinition
import com.radeusgd.trachonline.messages.ChatMessage
import com.radeusgd.trachonline.messages.ClientMessage
import com.radeusgd.trachonline.messages.Exited
import com.radeusgd.trachonline.messages.FlipCard
import com.radeusgd.trachonline.messages.Joined
import com.radeusgd.trachonline.messages.LogMessage
import com.radeusgd.trachonline.messages.MoveEntity
import com.radeusgd.trachonline.messages.PickStack
import com.radeusgd.trachonline.messages.PutOnStack
import com.radeusgd.trachonline.messages.SendChatMessage
import com.radeusgd.trachonline.messages.SetNickName
import com.radeusgd.trachonline.messages.ShuffleStack
import com.radeusgd.trachonline.messages.UpdateGameState
import java.util.concurrent.atomic.AtomicInteger

data class GameClient(var nickName: String)

class GameServer(gameDefinition: GameDefinition) : Server<Unit>() {

    override fun handleMessage(client: Client, message: ClientMessage) {
        when (message) {
            is SendChatMessage -> broadcast(ChatMessage(client.nickName(), message.text))
            is SetNickName -> setNickName(client, message)
            is MoveEntity -> moveEntity(client, message)
            is PickStack -> pickStack(client, message)
            is ShuffleStack -> shuffleStack(client, message)
            is PutOnStack -> putOnStack(client, message)
            is FlipCard -> flipCard(client, message)
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
        val removalResult = area.removeEntity(message.entityUuid)
        if (removalResult != null) {
            val destination = message.destination
            val entity = removalResult.entity
            val movedArea = removalResult.newArea.addEntity(destination, entity.entity)
            if (movedArea != null) {
                area = movedArea
                val source = renderSourceLocation(removalResult.locationDescription)
                // TODO better wording of destination too
                log("${client.nickName()} moves a card from $source to $destination")
                broadcastGameUpdates()
            } else {
                log("Entity was supposed to be moved to $destination which could not be found.")
            }
        } else {
            log("Entity to move, ${message.entityUuid}, could not be found.")
        }
    }

    private fun pickStack(client: Client, message: PickStack) {
        val removalResult = area.removeEntity(message.stackUuid)
        if (removalResult != null) {
            val entity = removalResult.entity
            val stack = entity.entity as? CardStack
            if (stack != null) {
                val card = stack.cards.first()
                val rest = stack.cards.drop(1)

                // TODO not sure what is the right value for this shift
                // TODO it could be a different board!
                val cardDestination = BoardDestination(area.mainArea.uuid, entity.position.move(3f,5f).moveUp())
                val addedArea = removalResult.newArea.addEntity(cardDestination, card) ?: throw IllegalStateException("Board disappeared?!")

                val leftOverEntity = if (rest.size == 1) rest.first() else stack.copy(cards = rest)
                val destination = BoardDestination(area.mainArea.uuid, entity.position)

                val finalArea = addedArea.addEntity(destination, leftOverEntity) ?: throw IllegalStateException("Board disappeared?!")
                area = finalArea
                log("${client.nickName()} picks a card from a stack")
                broadcastGameUpdates()
            } else {
                log("was not a stack") // TODO
            }
        } else {
            log("Entity to pick from, ${message.stackUuid}, could not be found.")
        }
    }

    private fun flipCard(client: Client, message: FlipCard) {
        val removalResult = area.removeEntity(message.cardUuid)
        if (removalResult != null) {
            val entity = removalResult.entity
            val card = entity.entity as? Card
            if (card != null) {
                val newCard = card.copy(isShowingFront = !card.isShowingFront)

                // TODO it could be a different destination! check the location!
                val destination = BoardDestination(area.mainArea.uuid, entity.position)

                val updatedArea = removalResult.newArea.addEntity(destination, newCard) ?: throw IllegalStateException("Board disappeared?!")

                area = updatedArea
                log("${client.nickName()} flips a card")
                broadcastGameUpdates()
            } else {
                log("was not a card") // TODO
            }
        } else {
            log("Entity to flip, ${message.cardUuid}, could not be found.")
        }
    }

    private fun shuffleStack(client: Client, message: ShuffleStack) {
        val removalResult = area.removeEntity(message.stackUuid)
        if (removalResult != null) {
            val entity = removalResult.entity
            val stack = entity.entity as? CardStack
            if (stack != null) {
                val shuffled = stack.copy(cards = Deck.shuffle(stack.cards))

                // TODO destination uuid!
                val destination = BoardDestination(area.mainArea.uuid, entity.position)

                val finalArea = removalResult.newArea.addEntity(destination, shuffled) ?: throw IllegalStateException("Board disappeared?!")
                area = finalArea
                log("${client.nickName()} shuffles a stack")
                broadcastGameUpdates()
            } else {
                log("was not a stack") // TODO
            }
        } else {
            log("Entity to shuffle, ${message.stackUuid}, could not be found.")
        }
    }

    private fun putOnStack(client: Client, message: PutOnStack) {
        val stackRemovalResult = area.removeEntity(message.stackUuid)
        if (stackRemovalResult != null) {
            val cardRemovalResult = stackRemovalResult.newArea.removeEntity(message.cardUuid)
            if (cardRemovalResult != null) {
                val areaPrim = cardRemovalResult.newArea
                val stackEntity = stackRemovalResult.entity
                val stack = stackEntity.entity as? CardStack
                val card = cardRemovalResult.entity.entity as? Card
                if (stack != null && card != null) {
                    val updatedStack = stack.copy(cards = listOf(card) + stack.cards)

                    // TODO destination uuid!
                    val destination = BoardDestination(area.mainArea.uuid, stackEntity.position)

                    val finalArea = areaPrim.addEntity(destination, updatedStack) ?: throw IllegalStateException("Board disappeared?!")
                    area = finalArea
                    log("${client.nickName()} places a card into a stack")
                    broadcastGameUpdates()
                } else {
                    log("Can only put a CARD on STACK")
                }
            } else {
                log("Card to put, ${message.cardUuid}, could not be found")
            }
        } else {
            log("Stack to put on, ${message.stackUuid}, could not be found.")
        }
    }


    // TODO prettier wording here
    private fun renderSourceLocation(areaLocationDescription: AreaLocationDescription): String = areaLocationDescription.toString()

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
