package com.radeusgd.trachonline.gamearea

import com.benasher44.uuid.Uuid
import com.radeusgd.trachonline.board.BoardArea
import com.radeusgd.trachonline.board.BoardDestination
import com.radeusgd.trachonline.board.BoardEntity
import com.radeusgd.trachonline.board.PlacedEntity

/**
 * @param playerId owner's id
 * @param privateArea the private area (hand)
 * @param personalArea the players area that everybody can see
 */
data class PlayerAreas(val playerId: Uuid, val privateArea: BoardArea, val personalArea: BoardArea) {
    companion object {
        fun empty(playerId: Uuid): PlayerAreas {
            return PlayerAreas(playerId, BoardArea.empty(), BoardArea.empty())
        }
    }
}

sealed class AreaLocationDescription {
    abstract val public: Boolean
}

data class PrivateArea(val playerId: Uuid) : AreaLocationDescription() {
    override val public = false
}

data class PersonalArea(val playerId: Uuid) : AreaLocationDescription() {
    override val public = true
}

object MainArea : AreaLocationDescription() {
    override val public = true
}

data class RemovalResult(
    val newArea: GameArea,
    val locationDescription: AreaLocationDescription,
    val locationId: Uuid,
    val entity: PlacedEntity
)

data class GameArea(val mainArea: BoardArea, val playerAreas: List<PlayerAreas>) {
    fun addPlayer(playerId: Uuid): GameArea = copy(playerAreas = playerAreas + PlayerAreas.empty(playerId))

    private data class BoardLens(
        val board: BoardArea,
        val locationDescription: AreaLocationDescription,
        val updateItself: (BoardArea) -> GameArea
    )

    private fun modifyPlayer(playerId: Uuid, newValue: PlayerAreas): GameArea {
        val otherPlayers = playerAreas.filter { it.playerId != playerId }
        return copy(playerAreas = otherPlayers + newValue)
    }

    private fun allBoards(): List<BoardLens> {
        val mainAreaLens = BoardLens(mainArea, MainArea) { mod -> copy(mainArea = mod) }
        val playerLenses = playerAreas.flatMap { player ->
            val privateLens = BoardLens(player.privateArea, PrivateArea(player.playerId)) { mod ->
                modifyPlayer(player.playerId, player.copy(privateArea = mod))
            }
            val publicLens = BoardLens(player.personalArea, PersonalArea(player.playerId)) { mod ->
                modifyPlayer(player.playerId, player.copy(personalArea = mod))
            }

            listOf(privateLens, publicLens)
        }
        return listOf(mainAreaLens) + playerLenses
    }

    fun removeEntity(entityId: Uuid): RemovalResult? {
        allBoards().forEach { boardLens ->
            val updated = boardLens.board.remove(entityId)
            if (updated != null) {
                val (newBoard, entity) = updated
                return RemovalResult(
                    newArea = boardLens.updateItself(newBoard),
                    locationDescription = boardLens.locationDescription,
                    locationId = boardLens.board.uuid,
                    entity = entity
                )
            }
        }

        return null
    }

    /** Adds an entity to the specific part of game area.
     *
     * Returns a new area if it succeeded
     */
    fun addEntity(destination: BoardDestination, entity: BoardEntity): GameArea? {
        val placed = PlacedEntity(entity, destination.position)
        allBoards().forEach { boardLens ->
            if (boardLens.board.uuid == destination.boardId) {
                val newBoard = boardLens.board.add(placed)
                return boardLens.updateItself(newBoard)
            }
        }
        return null
    }

    fun describeBoard(uuid: Uuid): AreaLocationDescription? {
        val boardLens = allBoards().find { it.board.uuid == uuid }
        return boardLens?.locationDescription
    }
}
