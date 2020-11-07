package com.radeusgd.trachonline.gamearea

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.radeusgd.trachonline.board.BoardArea
import com.radeusgd.trachonline.board.BoardDestination
import com.radeusgd.trachonline.board.BoardEntity
import com.radeusgd.trachonline.board.Card
import com.radeusgd.trachonline.board.CardVisuals
import com.radeusgd.trachonline.board.EntityDestination
import com.radeusgd.trachonline.board.PlacedEntity
import com.radeusgd.trachonline.board.Position
import com.radeusgd.trachonline.board.StackDestination

/**
 * @param playerId owner's id
 * @param privateArea the private area (hand)
 * @param personalArea the players area that everybody can see
 */
data class PlayerAreas(val playerId: Uuid, val privateArea: BoardArea, val personalArea: BoardArea) {
    companion object {
        fun empty(playerId: Uuid): PlayerAreas {
            // TODO remove
//            val mockedPrivCard =
//                PlacedEntity(Card(uuid4(), CardVisuals("trach-cards/atak.jpg", "back.jpg"), true), Position(3f, 1f, 0))
//            val mockedPersCard =
//                PlacedEntity(Card(uuid4(), CardVisuals("trach-cards/obrona.jpg", "back.jpg"), true), Position(1f, 1f, 0))
//            return PlayerAreas(playerId, BoardArea.empty().add(mockedPrivCard), BoardArea.empty().add(mockedPersCard))
            return PlayerAreas(playerId, BoardArea.empty(), BoardArea.empty())
        }
    }
}

sealed class AreaLocationDescription

data class PrivateArea(val playerId: Uuid) : AreaLocationDescription()
data class PersonalArea(val playerId: Uuid) : AreaLocationDescription()
object MainArea : AreaLocationDescription()

data class RemovalResult(val newArea: GameArea, val locationDescription: AreaLocationDescription, val entity: PlacedEntity)

data class GameArea(val mainArea: BoardArea, val playerAreas: List<PlayerAreas>) {
    fun addPlayer(playerId: Uuid): GameArea = copy(playerAreas = playerAreas + PlayerAreas.empty(playerId))

    fun removeEntity(entityId: Uuid): RemovalResult? {
        val mainResult = mainArea.remove(entityId)
        if (mainResult != null) {
            return RemovalResult(
                newArea = copy(mainArea = mainResult.first),
                locationDescription = MainArea,
                entity = mainResult.second
            )
        } else {
            // TODO check also players
            return null
        }
    }

    /** Adds an entity to the specific part of game area.
     *
     * Returns a new area if it succeeded
     */
    fun addEntity(destination: EntityDestination, entity: BoardEntity): GameArea? {
        when (destination) {
            is BoardDestination -> {
                val placed = PlacedEntity(entity, destination.position)
                if (mainArea.uuid == destination.boardId) {
                    return copy(mainArea = mainArea.add(placed))
                } else {
                    // TODO player areas
                    return null
                }
            }
            is StackDestination ->
            // TODO("moving onto stack not implemented")
            return null
        }
    }
}
