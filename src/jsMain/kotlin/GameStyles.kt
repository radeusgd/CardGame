import kotlinx.css.*
import styled.StyleSheet

object GameStyles : StyleSheet("WelcomeStyles", isStatic = true) {
    val textContainer by css {
        padding(5.px)

        backgroundColor = rgb(8, 97, 22)
        color = rgb(56, 246, 137)
    }

    val textInput by css {
        margin(vertical = 5.px)

        fontSize = 14.px
    }

    val buttonInput by css {
        margin(vertical = 5.px)

        fontSize = 14.px
    }

    val chatContainer by css {
        padding(5.px)

        backgroundColor = rgb(8, 97, 22)
        color = rgb(56, 246, 137)
    }

    val gameBoardContainer by css {
        padding(5.px)

        backgroundColor = rgb(8, 97, 90)
        color = rgb(56, 246, 137)
    }

    val playersContainer by css {
        padding(5.px)

        backgroundColor = rgb(8, 97, 22)
        color = rgb(56, 246, 137)
    }

    val myCardsContainer by css {
        padding(5.px)

        backgroundColor = rgb(98, 27, 22)
        color = rgb(56, 246, 137)
    }
} 
