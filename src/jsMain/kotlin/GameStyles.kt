import com.radeusgd.trachonline.board.PlacedEntity
import kotlinx.css.BoxSizing
import kotlinx.css.CSSBuilder
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.LinearDimension
import kotlinx.css.Overflow
import kotlinx.css.Position
import kotlinx.css.backgroundColor
import kotlinx.css.bottom
import kotlinx.css.boxSizing
import kotlinx.css.color
import kotlinx.css.display
import kotlinx.css.fontSize
import kotlinx.css.height
import kotlinx.css.left
import kotlinx.css.margin
import kotlinx.css.maxWidth
import kotlinx.css.overflowX
import kotlinx.css.overflowY
import kotlinx.css.padding
import kotlinx.css.pct
import kotlinx.css.position
import kotlinx.css.px
import kotlinx.css.right
import kotlinx.css.top
import kotlinx.css.vw
import kotlinx.css.width
import styled.StyleSheet

object GameStyles : StyleSheet("GameStyles", isStatic = true) {

    val input by css {
        margin(vertical = 5.px, horizontal = 5.px)
        fontSize = 14.px
    }

    val chatActions by css {
        bottom = 1.pct
        height = LinearDimension.auto
    }

    val chatContainer by css {
        padding(5.px)

        backgroundColor = Color("#F0E9E3")
        color = Color("#1A1813")
        position = Position.absolute
        bottom = 1.pct
        right = 1.pct
        width = 35.pct
        height = 35.pct

        boxSizing = BoxSizing.borderBox
        overflowY = Overflow.scroll
        overflowX = Overflow.hidden
    }

    val mainAreaContainer by css {
        padding(5.px)

        backgroundColor = Color("#DFAEB1")

        position = Position.absolute
        top = 1.pct
        left = 1.pct
        width = 55.pct
        height = 55.pct
    }

    val playersContainer by css {
        padding(5.px)

        backgroundColor = Color("#BFCEDF")
        position = Position.absolute
        top = 1.pct
        right = 1.pct
        height = 55.pct
        width = 35.pct

        boxSizing = BoxSizing.borderBox
        overflowY = Overflow.scroll
        overflowX = Overflow.hidden
    }

    val privateAreaContainer by css {
        padding(5.px)

        backgroundColor = Color("#DFD7AC")
        position = Position.absolute
        left = 1.pct
        bottom = 1.pct
        width = 55.pct
        height = 22.pct
    }

    fun cardStyle(baseSize: Float): CSSBuilder.() -> Unit = {
        display = Display.inlineBlock
        maxWidth = baseSize.vw
//        maxHeight = baseSize.px
        width = LinearDimension.auto
        height = LinearDimension.auto
    }

    fun placedEntityCss(placedEntity: PlacedEntity): CSSBuilder.() -> Unit = {
        position = Position.relative
        val x = placedEntity.position.x
        val y = placedEntity.position.y
        left = x.vw
        top = y.vw
    }

    val cardStackBorderStyle by css {
        display = Display.inlineBlock
        padding(5.px)
        backgroundColor = Color("#00B5AB")
    }
}
