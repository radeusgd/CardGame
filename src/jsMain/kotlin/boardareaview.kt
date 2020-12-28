import Gameboard.Companion.BoardAreaClass
import com.radeusgd.trachonline.board.BoardArea
import kotlinx.css.Color
import kotlinx.css.Position
import kotlinx.css.backgroundColor
import kotlinx.css.height
import kotlinx.css.pct
import kotlinx.css.position
import kotlinx.css.width
import kotlinx.html.classes
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import styled.css
import styled.styledDiv

// TODO may need to add size hints here ?
data class BoardAreaViewProps(var board: BoardArea, var entityBaseSize: Float, var name: String?, var background: Color) : RProps

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
