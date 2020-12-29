package com.radeusgd.trachonline

import com.benasher44.uuid.Uuid
import com.radeusgd.trachonline.board.BoardArea
import com.radeusgd.trachonline.board.BoardDestination
import com.radeusgd.trachonline.board.Card
import com.radeusgd.trachonline.board.CardStack
import com.radeusgd.trachonline.board.GameSnapshot
import com.radeusgd.trachonline.board.Player
import com.radeusgd.trachonline.gamearea.AreaLocationDescription
import com.radeusgd.trachonline.gamearea.GameArea
import com.radeusgd.trachonline.gamearea.MainArea
import com.radeusgd.trachonline.gamearea.PersonalArea
import com.radeusgd.trachonline.gamearea.PlayerAreas
import com.radeusgd.trachonline.gamearea.PrivateArea
import com.radeusgd.trachonline.gamedefinition.Deck
import com.radeusgd.trachonline.gamedefinition.GameDefinition
import com.radeusgd.trachonline.messages.ChatMessage
import com.radeusgd.trachonline.messages.ClientMessage
import com.radeusgd.trachonline.messages.Exited
import com.radeusgd.trachonline.messages.FlipCard
import com.radeusgd.trachonline.messages.Joined
import com.radeusgd.trachonline.messages.LogMessage
import com.radeusgd.trachonline.messages.MakeStack
import com.radeusgd.trachonline.messages.MoveEntity
import com.radeusgd.trachonline.messages.PickStack
import com.radeusgd.trachonline.messages.PutOnStack
import com.radeusgd.trachonline.messages.SendChatMessage
import com.radeusgd.trachonline.messages.SetNickName
import com.radeusgd.trachonline.messages.ShuffleStack
import com.radeusgd.trachonline.messages.UpdateGameState
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger

data class GameClient(var nickName: String, var connected: Boolean) {
    fun render(): String = if (connected) nickName else "$nickName (disconnected)"
}

class GameServer(val gameDefinitions: List<GameDefinition>) : Server<Unit>() {

    class LogicError(message: String) : RuntimeException(message)

    override fun handleMessage(client: Client, message: ClientMessage) {
        try {
            when (message) {
                is SendChatMessage ->
                    if (message.text.startsWith("/")) {
                        handleCommand(client, message.text)
                    } else {
                        broadcast(ChatMessage(client.nickName(), message.text))
                    }
                is SetNickName -> setNickName(client, message)
                is MoveEntity -> moveEntity(client, message)
                is PickStack -> pickStack(client, message)
                is ShuffleStack -> shuffleStack(client, message)
                is PutOnStack -> putOnStack(client, message)
                is FlipCard -> flipCard(client, message)
                is MakeStack -> makeStack(client, message)
                Joined -> playerJoined(client)
                Exited -> playerExited(client)
            }
        } catch (e: RuntimeException) {
            log("Error: $e")
            e.printStackTrace()
        }
    }

    var area: GameArea = GameArea(BoardArea.empty(), listOf())

    init {
        try {
            restartGame(gameDefinitions.first().name)
        } catch (exception: Throwable) {
            exception.printStackTrace()
        }
    }

    fun handleCommand(client: Client, text: String) {
        val restartCommand = "/restart-game "
        if (text.startsWith(restartCommand)) {
            val arg = text.substring(restartCommand.length)
            restartGame(arg)
        } else {
            client.sendMessage(LogMessage("Invalid command: $text"))
        }
    }

    fun restartGame(name: String) {
        val game = gameDefinitions.find { it.name == name } ?: throw LogicError("Game not found")
        val clearedPlayers = area.playerAreas.map { PlayerAreas(it.playerId, BoardArea.empty(), BoardArea.empty()) }
        area = GameArea(game.prepareMainBoard(), clearedPlayers)
        log("Started new $name game")
        broadcastGameUpdates()
    }

    fun takeSnapshot(playerId: Uuid): GameSnapshot {
        val currentArea = area
        val currentPlayerAreas = currentArea.playerAreas.find { it.playerId == playerId }
            ?: throw IllegalStateException("Player is not part of this GameArea")
        return GameSnapshot(
            mainArea = currentArea.mainArea,
            privateArea = currentPlayerAreas.privateArea,
            players = currentArea.playerAreas.map { areas ->
                Player(
                    name = getPlayerData(areas.playerId)?.render() ?: "Unknown player?!",
                    personalArea = areas.personalArea,
                    privateAreaCount = areas.privateArea.countCards()
                )
            }
        )
    }

    private fun moveEntity(client: Client, message: MoveEntity) {
        val removalResult = area.removeEntity(message.entityUuid)
            ?: throw LogicError("Entity to move, ${message.entityUuid}, could not be found.")
        val destination = message.destination
        val entity = removalResult.entity
        val movedArea = removalResult.newArea.addEntity(destination, entity.entity)
            ?: throw LogicError("Entity was supposed to be moved to $destination which could not be found.")
        area = movedArea
        broadcastGameUpdates()
        val destinationDescription = area.describeBoard(destination.boardId)
        if (removalResult.locationDescription.public || destinationDescription?.public != false) {
            val source = renderLocation(removalResult.locationDescription)
            val renderedDestination = destinationDescription?.let { renderLocation(it) } ?: "Unknown board"
            log("${client.nickName()} moves a card from $source to $renderedDestination")
        }
    }

    private fun pickStack(client: Client, message: PickStack) {
        val removalResult = area.removeEntity(message.stackUuid)
            ?: throw LogicError("Entity to pick from, ${message.stackUuid}, could not be found.")
        val entity = removalResult.entity
        val stack = entity.entity as? CardStack ?: throw LogicError("Entity to pick was not a stack")
        val card = stack.cards.first()
        val rest = stack.cards.drop(1)

        val cardDestination = BoardDestination(removalResult.locationId, entity.position.move(3f, 5f).moveUp())
        val addedArea =
            removalResult.newArea.addEntity(cardDestination, card) ?: throw IllegalStateException("Board disappeared?!")

        val leftOverEntity =
            if (rest.isEmpty()) null else if (rest.size == 1) rest.first() else stack.copy(cards = rest)
        val destination = BoardDestination(removalResult.locationId, entity.position)

        val finalArea = leftOverEntity?.let {
            addedArea.addEntity(destination, it) ?: throw IllegalStateException("Board disappeared?!")
        }
        area = finalArea ?: addedArea
        broadcastGameUpdates()
        if (removalResult.locationDescription.public) {
            val location = renderLocation(removalResult.locationDescription)
            log("${client.nickName()} picks a card from a stack in $location")
        }
    }

    private fun flipCard(client: Client, message: FlipCard) {
        val removalResult = area.removeEntity(message.cardUuid)
            ?: throw LogicError("Entity to flip, ${message.cardUuid}, could not be found.")
        val entity = removalResult.entity
        val card = entity.entity as? Card ?: throw LogicError("Entity to flip was not a card")
        val newCard = card.copy(isShowingFront = !card.isShowingFront)

        val destination = BoardDestination(removalResult.locationId, entity.position)

        val updatedArea =
            removalResult.newArea.addEntity(destination, newCard) ?: throw IllegalStateException("Board disappeared?!")

        area = updatedArea
        broadcastGameUpdates()
        if (removalResult.locationDescription.public) {
            val location = renderLocation(removalResult.locationDescription)
            log("${client.nickName()} flips a card in $location")
        }
    }

    private fun shuffleStack(client: Client, message: ShuffleStack) {
        val removalResult = area.removeEntity(message.stackUuid)
            ?: throw LogicError("Entity to shuffle, ${message.stackUuid}, could not be found.")
        val entity = removalResult.entity
        val stack = entity.entity as? CardStack ?: throw LogicError("Entity to shuffle was not a stack")
        val shuffled = stack.copy(cards = Deck.shuffle(stack.cards))

        val destination = BoardDestination(removalResult.locationId, entity.position)

        val finalArea =
            removalResult.newArea.addEntity(destination, shuffled) ?: throw IllegalStateException("Board disappeared?!")
        area = finalArea
        broadcastGameUpdates()
        if (removalResult.locationDescription.public) {
            val location = renderLocation(removalResult.locationDescription)
            log("${client.nickName()} shuffles a stack in $location")
        }
    }

    private fun putOnStack(client: Client, message: PutOnStack) {
        val stackRemovalResult = area.removeEntity(message.stackUuid)
            ?: throw LogicError("Stack to put on, ${message.stackUuid}, could not be found.")
        val cardRemovalResult = stackRemovalResult.newArea.removeEntity(message.cardUuid)
            ?: throw LogicError("Card to put on a stack, ${message.cardUuid}, could not be found")
        val areaPrim = cardRemovalResult.newArea
        val stackEntity = stackRemovalResult.entity
        val stack = stackEntity.entity as? CardStack ?: throw LogicError("Entity was not a stack")
        val card = cardRemovalResult.entity.entity as? Card ?: throw LogicError("Entity was not a card")
        val updatedStack = stack.copy(cards = listOf(card) + stack.cards)

        val destination = BoardDestination(stackRemovalResult.locationId, stackEntity.position)

        val finalArea =
            areaPrim.addEntity(destination, updatedStack) ?: throw IllegalStateException("Board disappeared?!")
        area = finalArea
        broadcastGameUpdates()
        if (stackRemovalResult.locationDescription.public || cardRemovalResult.locationDescription.public) {
            val stackLocation = renderLocation(stackRemovalResult.locationDescription)
            val cardLocation = renderLocation(cardRemovalResult.locationDescription)
            log("${client.nickName()} places a card from $cardLocation onto a stack in $stackLocation")
        }
    }

    private fun makeStack(client: Client, message: MakeStack) {
        val removalResult = area.removeEntity(message.cardUuid)
            ?: throw LogicError("Card to put on a new stack, ${message.cardUuid}, could not be found")
        val areaPrim = removalResult.newArea
        val card = removalResult.entity.entity as? Card ?: throw LogicError("Entity was not a card")
        val stack = CardStack.make(listOf(card))

        val destination = BoardDestination(removalResult.locationId, removalResult.entity.position)

        val finalArea =
            areaPrim.addEntity(destination, stack) ?: throw IllegalStateException("Board disappeared?!")
        area = finalArea
        broadcastGameUpdates()
    }

    private fun renderLocation(areaLocationDescription: AreaLocationDescription): String =
        when (areaLocationDescription) {
            is PrivateArea -> {
                val id = areaLocationDescription.playerId
                val player = getPlayerData(id) ?: throw LogicError("Player $id not found!")
                player.nickName + "'s hand"
            }
            is PersonalArea -> {
                val id = areaLocationDescription.playerId
                val player = getPlayerData(id) ?: throw LogicError("Player $id not found!")
                player.nickName + "'s visible cards"
            }
            MainArea -> "Main area"
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
        broadcastGameUpdates()
    }

    private val players = HashMap<Uuid, GameClient>()

    private fun playerJoined(client: Client) {
        val existingEntry = players[client.uuid()]
        if (existingEntry == null) {
            players[client.uuid()] = GameClient(freshTemporaryNickname(), true)
            log("Player ${client.nickName()} has joined.")
            area = area.addPlayer(client.uuid())
        } else {
            players[client.uuid()] = existingEntry.copy(connected = true)
            log("Player ${client.nickName()} has reconnected.")
        }
        broadcastGameUpdates() // TODO it seems that the re-joined player very rarely does not get refreshed board state immediately
    }

    private fun playerExited(client: Client) {
        log("Player ${client.nickName()} has been disconnected.")
        val entry = players[client.uuid()]
        if (entry != null) {
            players[client.uuid()] = entry.copy(connected = false)
            broadcastGameUpdates()
        }
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
        val time = ZonedDateTime.now(ZoneId.of("Europe/Warsaw")).withNano(0).format(DateTimeFormatter.ISO_LOCAL_TIME)
        broadcast(LogMessage("($time) $message"))
    }
}
