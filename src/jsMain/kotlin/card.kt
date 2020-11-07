import com.radeusgd.trachonline.board.Card
import com.radeusgd.trachonline.board.CardStack
import com.radeusgd.trachonline.board.PlacedEntity
import kotlinx.html.js.onDragEndFunction
import org.w3c.dom.DragEvent
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.br
import styled.css
import styled.styledDiv
import styled.styledImg

data class EntityProps(var placedEntity: PlacedEntity, var baseSize: Float) : RProps

interface EntityState : RState

@JsExport
class EntityView(props: EntityProps) : RComponent<EntityProps, EntityState>(props) {

    override fun RBuilder.render() {
        styledDiv {
            css {
                +GameStyles.placedEntityCss(props.placedEntity)
            }
            val entity = props.placedEntity.entity

            when (entity) {
                is CardStack ->
                    styledDiv {
                        css {
                            +GameStyles.cardStackBorderStyle
                        }
                        +"(${entity.cards.size} cards)"
                        br { }
                        styledImg(src = "/static/" + (entity.cards.firstOrNull()?.getCurrentImage() ?: "error.jpg")) {
                            css {
                                +GameStyles.cardStyle(props.baseSize)
                            }
                        }
                    }
                is Card ->
                    styledImg(src = "/static/" + entity.getCurrentImage()) {
                        css {
                            +GameStyles.cardStyle(props.baseSize)
                        }
                        attrs {
                            onDragEndFunction = { event ->
                                console.log(event)
                                val dragged = (event.asDynamic().nativeEvent as? DragEvent)
                                console.log(dragged?.pageX)
                                console.log(dragged?.offsetX)
                                console.log(dragged?.clientX)
                            }
                        }
                    }
            }
        }
    }
}
