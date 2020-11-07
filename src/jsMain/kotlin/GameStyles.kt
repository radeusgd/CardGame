import kotlinx.css.*
import styled.StyleSheet

object GameStyles : StyleSheet("GameStyles", isStatic = true) {


    val textInput by css {
        margin(vertical = 5.px, horizontal = 5.px)

        fontSize = 14.px
    }

    val buttonInput by css {
        margin(vertical = 5.px, horizontal = 5.px)

        fontSize = 14.px
    }

    val chatActions by css {
        bottom = LinearDimension("1%")
        height =L
    }


    val chatContainer by css {
        padding(5.px)

        backgroundColor = Color("#F0E9E3")
        color = Color("#1A1813")
        position = Position.absolute
        bottom = LinearDimension("1%")
        right = LinearDimension("1%")
        width = LinearDimension("35%")
        height = LinearDimension("35%")

        boxSizing = BoxSizing.borderBox
        overflowY = Overflow.scroll
        overflowX = Overflow.hidden
    }

    val mainAreaContainer by css {
        padding(5.px)

        backgroundColor = Color("#DFAEB1")

        top = LinearDimension("1%")
        left = LinearDimension("1%")
        width = LinearDimension("55%")
        height = LinearDimension("55%")
    }

    val playersContainer by css {
        padding(5.px)

        backgroundColor = Color("#BFCEDF")
        position = Position.absolute
        top = LinearDimension("1%")
        right = LinearDimension("1%")
        height = LinearDimension("55%")
        width = LinearDimension("35%")

        overflowY = Overflow.scroll
    }

    val privateAreaContainer by css {
        padding(5.px)

        backgroundColor = Color("#DFD7AC")
        position = Position.absolute
        left = LinearDimension("1%")
        bottom = LinearDimension("1%")
        width = LinearDimension("55%")
        height = LinearDimension("22%")
    }
}
