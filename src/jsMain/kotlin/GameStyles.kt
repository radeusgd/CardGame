import com.radeusgd.trachonline.board.PlacedEntity
import kotlinx.css.BoxSizing
import kotlinx.css.CSSBuilder
import kotlinx.css.Color
import kotlinx.css.Cursor
import kotlinx.css.Display
import kotlinx.css.LinearDimension
import kotlinx.css.Overflow
import kotlinx.css.Position
import kotlinx.css.backgroundColor
import kotlinx.css.bottom
import kotlinx.css.boxSizing
import kotlinx.css.color
import kotlinx.css.cursor
import kotlinx.css.display
import kotlinx.css.float
import kotlinx.css.fontSize
import kotlinx.css.height
import kotlinx.css.left
import kotlinx.css.margin
import kotlinx.css.maxWidth
import kotlinx.css.minHeight
import kotlinx.css.minWidth
import kotlinx.css.opacity
import kotlinx.css.overflowX
import kotlinx.css.overflowY
import kotlinx.css.padding
import kotlinx.css.pct
import kotlinx.css.position
import kotlinx.css.px
import kotlinx.css.right
import kotlinx.css.top
import kotlinx.css.vmin
import kotlinx.css.width
import kotlinx.css.zIndex
import styled.StyleSheet

object GameStyles : StyleSheet("GameStyles", isStatic = true) {

    val boardAreaColor = Color("#938581")

    val mainStyle by css {
        backgroundColor = Color("#0C7C59")
        width = 99.vmin
        height = 99.vmin
        margin = "auto"
    }

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

        backgroundColor = Color("#FFFFFB")
        color = Color("#2B303A")
        float = kotlinx.css.Float.right
//        position = Position.absolute
//        bottom = 1.pct
//        right = 1.pct
        width = 42.pct
        height = 35.pct

        boxSizing = BoxSizing.borderBox
        overflowY = Overflow.scroll
        overflowX = Overflow.hidden
    }

    val mainAreaContainer by css {
        padding(5.px)
//        position = Position.absolute
//        top = 1.pct
//        left = 1.pct
        float = kotlinx.css.Float.left
        width = 55.pct
        height = 55.pct
    }

    fun personalArea(baseSize: Float): CSSBuilder.() -> Unit = {
        display = Display.inlineBlock
        width = 40.vmin
        height = 15.vmin
    }

    val playersContainer by css {
        padding(5.px)

        backgroundColor = Color("#FFC145")
//        position = Position.absolute
//        top = 1.pct
//        right = 1.pct
        float = kotlinx.css.Float.right
        height = 55.pct
        width = 42.pct

        boxSizing = BoxSizing.borderBox
        overflowY = Overflow.scroll
        overflowX = Overflow.hidden
    }

    val privateAreaContainer by css {
        padding(5.px)
//        position = Position.absolute
//        left = 1.pct
//        bottom = 1.pct
        float = kotlinx.css.Float.left
        width = 55.pct
        height = 22.pct
    }

    fun cardStyle(baseSize: kotlin.Float): CSSBuilder.() -> Unit = {
        display = Display.inlineBlock
        maxWidth = baseSize.vmin
//        maxHeight = baseSize.px
        width = LinearDimension.auto
        height = LinearDimension.auto
    }

    fun placedEntityCss(placedEntity: PlacedEntity): CSSBuilder.() -> Unit = {
        position = Position.absolute
        val x = placedEntity.position.x
        val y = placedEntity.position.y
        left = x.vmin
        top = y.vmin
        zIndex = placedEntity.position.depth
    }

    val cardStackBorderStyle by css {
        display = Display.inlineBlock
        padding(5.px)
        backgroundColor = Color("#00B5AB")
        hover {
            descendants("a") {
                opacity = 1
            }
        }
    }

    val hiddenAStyle by css {
        opacity = 0
        float = kotlinx.css.Float.right
        cursor = Cursor.pointer
    }
}
