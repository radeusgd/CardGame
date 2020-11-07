import com.radeusgd.trachonline.board.BoardArea
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState

// TODO may need to add size hints here ?
data class BoardAreaViewProps(var board: BoardArea, var entityBaseSize: Float) : RProps

object BoardAreaViewState : RState

class BoardAreaView(props: BoardAreaViewProps) : RComponent<BoardAreaViewProps, BoardAreaViewState>(props) {

    override fun RBuilder.render() {
        props.board.entities.forEach {
            child(EntityView::class) {
                attrs {
                    placedEntity = it
                    baseSize = props.entityBaseSize
                }
            }
        }
    }
}
