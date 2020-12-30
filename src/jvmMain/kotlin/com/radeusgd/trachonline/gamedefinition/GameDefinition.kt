package com.radeusgd.trachonline.gamedefinition

import com.benasher44.uuid.uuid4
import com.radeusgd.trachonline.board.BoardArea
import com.radeusgd.trachonline.board.CardStack
import com.radeusgd.trachonline.board.CardVisuals
import com.radeusgd.trachonline.board.PlacedEntity
import com.radeusgd.trachonline.board.Position
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import com.radeusgd.trachonline.board.Card as MaterializedCard

@Serializable
data class Card(val name: String, val count: Int) {
    fun spawn(parentDeck: Deck): MaterializedCard {
        val frontPath = parentDeck.basePath + "/" + name
        val backPath = parentDeck.basePath + "/" + parentDeck.backName
        return MaterializedCard(
            uuid = uuid4(),
            visuals = CardVisuals(frontImage = frontPath, backImage = backPath),
            isShowingFront = false
        )
    }
}

@Serializable
data class Deck(val basePath: String, val backName: String, val cards: List<Card>) {
    fun spawn(): List<MaterializedCard> = cards.flatMap { card ->
        (1..card.count).map { card.spawn(this) }
    }

    companion object {
        fun shuffle(cards: List<MaterializedCard>): List<MaterializedCard> {
            val mutable = cards.toMutableList()
            mutable.shuffle()
            return mutable
        }
    }
}

@Serializable
data class GameDefinition(val name: String, val decks: List<Deck>) {
    fun spawnDecks(): List<List<MaterializedCard>> = decks.map { it.spawn() }

    fun prepareMainBoard(): BoardArea {
        val stacks = spawnDecks().map { CardStack.make(Deck.shuffle(it)) }
        // TODO better placement
        val placed = stacks.withIndex().map { (index, stack) ->
            PlacedEntity(stack, Position(5f + index * 11f, 10f, index))
        }
        return BoardArea.empty().add(placed)
    }

    companion object {
        fun parse(json: String): GameDefinition = Json.decodeFromString(json)
        fun loadResource(name: String): GameDefinition? {
            val text = this::class.java.classLoader.getResource(name)?.readText()
                ?.lines()?.filter { !it.trimStart().startsWith("//") }?.joinToString("\n")
            return text?.let { parse(it) }
        }
    }
}
