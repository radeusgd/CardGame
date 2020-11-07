package com.radeusgd.trachonline.gamearea

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.radeusgd.trachonline.board.BoardArea
import com.radeusgd.trachonline.board.BoardEntity
import com.radeusgd.trachonline.board.Card
import com.radeusgd.trachonline.board.CardVisuals
import com.radeusgd.trachonline.board.EntityDestination
import com.radeusgd.trachonline.board.PlacedEntity
import com.radeusgd.trachonline.board.Position

/**
 * @param playerId owner's id
 * @param privateArea the private area (hand)
 * @param personalArea the players area that everybody can see
 */
data class PlayerAreas(val playerId: Uuid, val privateArea: BoardArea, val personalArea: BoardArea) {
    companion object {
        fun empty(playerId: Uuid): PlayerAreas {
            // TODO remove
            val mockedPrivCard =
                PlacedEntity(Card(uuid4(), CardVisuals("trach-cards/atak.jpg", "back.jpg"), true), Position(3f, 1f, 0))
            val mockedPersCard =
                PlacedEntity(Card(uuid4(), CardVisuals("trach-cards/obrona.jpg", "back.jpg"), true), Position(1f, 1f, 0))
//            return PlayerAreas(playerId, BoardArea.empty(), BoardArea.empty())
            return PlayerAreas(playerId, BoardArea.empty().add(mockedPrivCard), BoardArea.empty().add(mockedPersCard))
        }
    }
}

sealed class AreaLocationDescription

data class PrivateArea(val playerId: Uuid) : AreaLocationDescription()
data class PersonalArea(val playerId: Uuid) : AreaLocationDescription()
object MainArea : AreaLocationDescription()

data class GameArea(val mainArea: BoardArea, val playerAreas: List<PlayerAreas>) {
    fun addPlayer(playerId: Uuid): GameArea = copy(playerAreas = playerAreas + PlayerAreas.empty(playerId))

//    fun removeEntity(entityId: Uuid): AreaLocationDescription? = TODO()

    /** Adds an entity to the specific part of game area.
     *
     * Returns true if it succeeded. False if the destination was not found within the area.
     */
    fun addEntity(destination: EntityDestination, entity: BoardEntity): Pair<GameArea, Boolean> = TODO()
}
