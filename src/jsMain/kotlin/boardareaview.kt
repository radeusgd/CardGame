import Gameboard.Companion.BoardAreaClass
import Gameboard.Companion.StackMakerClass
import com.radeusgd.trachonline.board.BoardArea
import kotlinx.css.Color
import kotlinx.css.Position
import kotlinx.css.backgroundColor
import kotlinx.css.bottom
import kotlinx.css.height
import kotlinx.css.paddingLeft
import kotlinx.css.paddingTop
import kotlinx.css.pct
import kotlinx.css.position
import kotlinx.css.right
import kotlinx.css.width
import kotlinx.html.classes
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import styled.css
import styled.styledDiv

// TODO may need to add size hints here ?
data class BoardAreaViewProps(
    var board: BoardArea,
    var entityBaseSize: Float,
    var name: String?,
    var background: Color,
    var includeStackMaker: Boolean
) : RProps

object BoardAreaViewState : RState

class BoardAreaView(props: BoardAreaViewProps) : RComponent<BoardAreaViewProps, BoardAreaViewState>(props) {

    override fun RBuilder.render() {
        styledDiv {
            val name = props.name
            if (name != null) {
                +name
            }
            attrs {
                classes += BoardAreaClass
                attributes.set("data-boardid", props.board.uuid.toString())
            }
            css {
                position = Position.relative
                width = 100.pct
                height = 100.pct
                backgroundColor = props.background
            }

            if (props.includeStackMaker) {
                styledDiv {
                    css {
                        position = Position.absolute
                        bottom = 0.pct
                        right = 0.pct
                        backgroundColor = Color("#00B5AB")
                        paddingLeft = 1.pct
                        paddingTop = 1.pct
                    }
                    attrs { classes += StackMakerClass }
                    +"Make a stack"
                }
            }

            props.board.entities.forEach {
                child(EntityView::class) {
                    attrs {
                        placedEntity = it
                        baseSize = props.entityBaseSize
                        parentBoardId = props.board.uuid
                    }
                }
            }
        }
    }
}
